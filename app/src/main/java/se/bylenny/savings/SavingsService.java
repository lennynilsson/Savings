package se.bylenny.savings;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import se.bylenny.savings.models.external.SavingsGoalResponse;
import se.bylenny.savings.models.external.SavingsGoalsResponse;
import se.bylenny.savings.models.external.UserResponse;
import se.bylenny.savings.models.internal.SavingsGoal;
import se.bylenny.savings.models.internal.Status;
import se.bylenny.savings.models.internal.User;

public class SavingsService extends Service {
    public static final int RESULT_FETCH_SUCCESS = 1;
    public static final int RESULT_FETCH_ERROR = -1;
    private static final String TAG = "SavingsService";
    private static final String ACTION_FETCH = "se.bylenny.flickrer.action.FETCH";
    private static SavingsDatabaseHelper helper;
    private final IBinder binder = new SavingsBinder();
    private boolean isFetching = false;
    private ThreadPoolExecutor executor;

    public SavingsService() {
        executor = new ThreadPoolExecutor(0, 3, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Starts this service to perform action Fetch with the given parameters. If
     * the service is already performing a task this action will be queued.
     */
    public static void startActionFetch(Context context) {
        Intent intent = new Intent(context, SavingsService.class);
        intent.setAction(ACTION_FETCH);
        context.startService(intent);
    }

    public static SavingsDatabaseHelper getHelper(Context context) {
        if (helper == null) {
            helper = OpenHelperManager.getHelper(context, SavingsDatabaseHelper.class);
        }
        return helper;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_FETCH.equals(intent.getAction())) {
            if (requestLock()) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        fetchSavingsGoals();
                    }
                });
            }
        }
        return START_NOT_STICKY;
    }


    private synchronized boolean requestLock() {
        if (isFetching) {
            Log.d(TAG, "Unable to aquire lock");
            return false;
        } else {
            Log.d(TAG, "Lock aquired");
        }
        isFetching = true;
        return true;
    }

    private synchronized void releaseLock() {
        if (!isFetching) {
            throw new IllegalStateException("Cannot release unlocked lock");
        }
        Log.d(TAG, "Lock releases");
        isFetching = false;
    }

    private void fetchUser(final Integer userId) {
        RestRequest<UserResponse> request = new RestRequest<UserResponse>();
        Log.d(TAG, "Requesting user " + userId);
        request.call(getUserUrl(userId), UserResponse.class, executor,
                new RestRequest.ResponseListener<UserResponse>() {
                    @Override
                    public void onSuccess(UserResponse response) {
                        try {
                            Dao<User, Integer> userDao = getHelper(getApplicationContext()).getUserDao();
                            User user = userDao.queryForId(userId);
                            if (null == user) {
                                user = new User();
                                user.setId(userId);
                            }
                            user.setDisplayName(response.displayName);
                            user.setAvatarUri(response.avatarUrl);
                            userDao.createOrUpdate(user);
                            MessageBus.send(RESULT_FETCH_SUCCESS);
                        } catch (SQLException e) {
                            Log.e(TAG, "Unable to save user", e);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, error);
                    }
                });
    }

    private void fetchSavingsGoals() {
        RestRequest<SavingsGoalsResponse> request = new RestRequest<SavingsGoalsResponse>();
        request.call(getSavingsUrl(), SavingsGoalsResponse.class, executor,
                new RestRequest.ResponseListener<SavingsGoalsResponse>() {
                    @Override
                    public void onSuccess(final SavingsGoalsResponse response) {
                        if (null == response) {
                            MessageBus.send(RESULT_FETCH_ERROR);
                            releaseLock();
                            destroyHelper();
                        } else {
                            try {
                                translateAndStore(response);
                                MessageBus.send(RESULT_FETCH_SUCCESS);
                            } catch (SQLException e) {
                                Log.e(TAG, "Unable to translate response", e);
                                MessageBus.send(RESULT_FETCH_ERROR);
                            } finally {
                                releaseLock();
                                destroyHelper();
                            }
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e(TAG, error == null ? "" : error);
                        MessageBus.send(RESULT_FETCH_ERROR);
                    }
                });
    }

    private void destroyHelper() {
        OpenHelperManager.releaseHelper();
        helper = null;
    }

    private String getSavingsUrl() {
        return getString(R.string.endpoint_savings);
    }

    private String getUserUrl(int userId) {
        return Uri.parse(getString(R.string.endpoint_users)).buildUpon()
                .appendPath(String.valueOf(userId)).build().toString();
    }

    private void translateAndStore(SavingsGoalsResponse response) throws SQLException {
        SavingsDatabaseHelper helper = getHelper(getApplicationContext());
        Dao<SavingsGoal, Integer> savingsGoalDao = helper.getSavingsGoalDao();
        Dao<User, Integer> userDao = helper.getUserDao();
        User user = null;
        SavingsGoal savingsGoal = null;
        for (SavingsGoalResponse savingGoal : response.savingsGoals) {
            savingsGoal = new SavingsGoal();
            savingsGoal.setId(savingGoal.id);
            savingsGoal.setName(savingGoal.name);
            savingsGoal.setGoalImageUri(savingGoal.goalImageURL);
            savingsGoal.setStatus(Status.valueOf(savingGoal.status));
            savingsGoal.setTargetAmount(savingGoal.targetAmount);
            savingsGoal.setCurrentBalance(savingGoal.currentBalance);
            savingsGoalDao.createOrUpdate(savingsGoal);
            savingsGoalDao.refresh(savingsGoal);
            if (null != savingGoal.connectedUsers) {
                for (Integer userId : savingGoal.connectedUsers) {
                    user = userDao.queryForId(userId);
                    if (null == user) {
                        user = new User();
                        user.setId(userId);
                        userDao.create(user);
                    }
                    user.setSavingsGoal(savingsGoal);
                    userDao.createOrUpdate(user);
                }
                savingsGoal.getConnectedUsers().updateAll();
            }
            user = userDao.queryForId(savingGoal.userId);
            if (null == user) {
                user = new User();
                user.setId(savingGoal.userId);
                userDao.create(user);
            }
            savingsGoal.setUser(user);
            savingsGoalDao.createOrUpdate(savingsGoal);
        }
        for (User u : userDao.queryForAll()) {
            String image = u.getAvatarUri();
            if (null == image) {
                fetchUser(u.getId());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class SavingsBinder extends Binder {
        SavingsBinder getService() {
            return SavingsBinder.this;
        }
    }
}
