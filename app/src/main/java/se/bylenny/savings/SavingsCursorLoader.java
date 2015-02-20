package se.bylenny.savings;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.j256.ormlite.android.AndroidDatabaseResults;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;

import se.bylenny.savings.models.internal.SavingsGoal;

public class SavingsCursorLoader extends CursorLoader {

    private static final String TAG = "SavingsCursorLoader";
    private final SavingsDatabaseHelper helper;

    public SavingsCursorLoader(Context context, SavingsDatabaseHelper helper) {
        super(context);
        this.helper = helper;
    }

    @Override
    public Cursor loadInBackground() {
        try {
            Dao<SavingsGoal, Integer> dao = helper.getSavingsGoalDao();
            PreparedQuery<SavingsGoal> query = dao.queryBuilder().prepare();
            CloseableIterator<SavingsGoal> iterator = dao.iterator(query);
            AndroidDatabaseResults results = (AndroidDatabaseResults)iterator.getRawResults();
            Cursor cursor = results.getRawCursor();
            return cursor;
        } catch (SQLException e) {
            Log.e(TAG, "Unable to load cursor", e);
            return null;
        } catch (NullPointerException e) {
            Log.e(TAG, "Database helper not loaded", e);
            return null;
        }
    }
}
