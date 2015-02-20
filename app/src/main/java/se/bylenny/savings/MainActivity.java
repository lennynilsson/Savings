package se.bylenny.savings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

public class MainActivity extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";

    private static final String STATE_LIST = "STATE_LIST";

    private SavingsCursorAdapter cursorAdapter;
    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isChangingQuery = false;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle(R.string.app_name);
        setTitle(R.string.app_name);

        // Setup Picasso and http client cache
        RestRequest.setup(getApplicationContext());
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.white);
        swipeRefreshLayout.setOnRefreshListener(this);

        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Click");
                SavingsCursorAdapter.Tag tag = ((SavingsCursorAdapter.Tag) view.getTag());
                Intent intent = ImageActivity.getIntent(
                        getApplicationContext(), tag.title, tag.image);
                startActivity(intent, null);
            }
        });
        listView.setDividerHeight(0);
        cursorAdapter = new SavingsCursorAdapter(getApplicationContext(), null, false);
        listView.setAdapter(cursorAdapter);

        initCursor();
    }

    private void setRefreshing(boolean refreshing) {
        swipeRefreshLayout.setRefreshing(refreshing);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessageBus.subscribe(this);
        reloadCursor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MessageBus.unsubscribe(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        listView.onRestoreInstanceState(savedInstanceState.getParcelable(STATE_LIST));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_LIST, listView.onSaveInstanceState());
    }

    private void fetch() {
        SavingsService.startActionFetch(getApplicationContext());
        setRefreshing(true);
    }

    @Subscribe
    public void onUpdate(Integer status) {
        switch (status) {
            case SavingsService.RESULT_FETCH_SUCCESS:
                reloadCursor();
            case SavingsService.RESULT_FETCH_ERROR:
                break;
        }
        setRefreshing(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new SavingsCursorLoader(getApplicationContext(),
                SavingsService.getHelper(getApplicationContext()));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        setCursor(cursor);
        if (null != cursor && cursor.getCount() == 0) {
            fetch();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        setCursor(null);
    }

    @Override
    public void onRefresh() {
        fetch();
    }

    private void initCursor() {
        getSupportLoaderManager().initLoader(1, null, this);
    }

    private void reloadCursor() {
        Loader<Object> loader = getSupportLoaderManager().getLoader(1);
        if (loader != null && ! loader.isReset()) {
            getSupportLoaderManager().restartLoader(1, null, this);
        } else {
            getSupportLoaderManager().initLoader(1, null, this);
        }
    }

    private void setCursor(Cursor cursor) {
        Cursor oldCursor = cursorAdapter.swapCursor(cursor);
        if (oldCursor != null) {
            oldCursor.close();
        }
        listView.setSelection(0);
    }

    private void destroyCursor() {
        getSupportLoaderManager().destroyLoader(1);
    }



}