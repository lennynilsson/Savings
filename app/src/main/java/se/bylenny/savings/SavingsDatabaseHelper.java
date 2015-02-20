package se.bylenny.savings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import se.bylenny.savings.models.internal.SavingsGoal;
import se.bylenny.savings.models.internal.User;

public class SavingsDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private final String TAG = "SavingsDatabaseHelper";
    private static final String DATABASE_NAME = "savings.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<User, Integer> userDao;
    private Dao<SavingsGoal, Integer> savingsGoalDao;

    public SavingsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, SavingsGoal.class);
        } catch (SQLException e) {
            Log.e(TAG, "Could not create new table", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, SavingsGoal.class, true);
        } catch (SQLException e) {
            Log.e(TAG, "Could not upgrade the table", e);
        }
    }

    public Dao<SavingsGoal, Integer> getSavingsGoalDao() throws SQLException {
        if (savingsGoalDao == null) {
            savingsGoalDao = getDao(SavingsGoal.class);
        }
        return savingsGoalDao;
    }

    public Dao<User, Integer> getUserDao() throws SQLException {
        if (userDao == null) {
            userDao = getDao(User.class);
        }
        return userDao;
    }
}
