package ua.kpi.kbis.campus;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity {
    public static Toolbar mToolbar;
    public static ActionBarDrawerToggle mDrawerToggle;
    public static Fragment currentFragment;
    static DrawerLayout mDrawerLayout;
    static DrawerLayout mInfoDrawerLayout;
    static ActionBarDrawerToggle mInfoDrawerToggle;
    static String sessionId;
    static SharedPreferences prefs;
    static JSONObject currentUser;
    private ArrayList<NavMenuItem> menuList;
    private RecyclerView mRecyclerView;
    private MenuAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private LinearLayout mInfoDrawerView;
    private JSONParser jsonParser = new JSONParser();
    private GetSessionIdTask mSessionIdTask;
    private View mLogout;
    private View mToolbarContainer;

    public void showToolbar() {
        mToolbarContainer.animate().cancel();
        mToolbarContainer.animate().translationY(0).setDuration(100);
        //mFragmentContainer.setPadding(0, mToolbar.getHeight(), 0, 0);
    }

    public void hideToolbar() {
        mToolbarContainer.animate().cancel();
        mToolbarContainer.animate().translationY(-mToolbarContainer.getHeight()).setDuration(100);
        //mFragmentContainer.setPadding(0,0,0,0);
    }

    public void finishMoveToolbar() {
        if (mToolbarContainer.getTranslationY() > -mToolbarContainer.getHeight() / 2) {
            showToolbar();
        } else {
            hideToolbar();
        }
    }

    public void moveToolbar(int dy) {
        mToolbarContainer.animate().cancel();
        if ((dy > 0) && (mToolbarContainer.getTranslationY() + mToolbarContainer.getHeight() > 0)) {
            if (mToolbarContainer.getTranslationY() + mToolbarContainer.getHeight() - dy < 0) {
                mToolbarContainer.setTranslationY(-mToolbarContainer.getHeight());
            } else {
                mToolbarContainer.setTranslationY(mToolbarContainer.getTranslationY() - dy);
            }
        }
        if ((dy < 0) && (mToolbarContainer.getTranslationY() < 0)) {
            if (mToolbarContainer.getTranslationY() - dy > 0) {
                mToolbarContainer.setTranslationY(0);

            } else {
                mToolbarContainer.setTranslationY(mToolbarContainer.getTranslationY() - dy);
            }
        }
        //mFragmentContainer.setPadding(0, (int)( mToolbar.getHeight() + mToolbar.getTranslationY()), 0, 0);
//        mFragmentContainer.setTranslationY(mToolbar.getTranslationY());
//        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFragmentContainer.getLayoutParams();
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        lp.height = (int) -mToolbar.getTranslationY() + size.y - lp.topMargin;
//        mFragmentContainer.requestLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(options).build();
        ImageLoader.getInstance().init(config);
        MainActivity.prefs = new ObscuredSharedPreferences(
                this, this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE));
        try {
            currentUser = new JSONObject(prefs.getString(prefs.getString("userId", null), null));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mInfoDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_sidebar);
        mInfoDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mInfoDrawerView = (LinearLayout) findViewById(R.id.info_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) mInfoDrawerView.getLayoutParams();
        int display_mode = getResources().getConfiguration().orientation;

        if (display_mode == 1) {
            params.width = width;
        } else {
            params.width = height;
        }
        mInfoDrawerView.setLayoutParams(params);
        mInfoDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                mInfoDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mInfoDrawerLayout.setDrawerListener(mInfoDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        menuList = new ArrayList<NavMenuItem>();
        menuList.add(new NavMenuItem(getResources().getString(R.string.title_main_page_fragment), R.drawable.ic_school_grey600_24dp, new MainPageFragment()));
        //menuList.add(new NavMenuItem(getResources().getString(R.string.title_messenger_fragment), R.drawable.ic_messenger_grey600_24dp, new MessengerFragment()));
        menuList.add(new NavMenuItem(getResources().getString(R.string.title_schedule_fragment), R.drawable.ic_event_note_grey600_24dp, new ScheduleFragment()));
        menuList.add(new NavMenuItem(getResources().getString(R.string.title_disciplines_fragment), R.drawable.ic_book_grey600_24dp, new DisciplinesFragment()));
        //menuList.add(new NavMenuItem(getResources().getString(R.string.title_control_fragment), R.drawable.ic_check_grey600_24dp, new ControlFragment()));
        mRecyclerView = (RecyclerView) findViewById(R.id.menu_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MenuAdapter(menuList, getSupportFragmentManager(), this);
        mRecyclerView.setAdapter(mAdapter);
        mLogout = findViewById(R.id.logout_container);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                sessionId = null;
                MainActivity.prefs.edit().clear().commit();
                MainActivity.this.startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        getSessionId();
        mToolbarContainer = findViewById(R.id.toolbar_conatiner);
        showToolbar();
    }

    @Override
    public void onBackPressed() {
        if (mInfoDrawerLayout.isDrawerOpen(GravityCompat.END)) { //replace this with actual function which returns if the drawer is open
            mInfoDrawerLayout.closeDrawer(GravityCompat.END);   // replace this with actual function which closes drawer
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return super.onCreateView(parent, name, context, attrs);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void getSessionId() {
        mSessionIdTask = new GetSessionIdTask(MainActivity.prefs.getString("login", null), MainActivity.prefs.getString("password", null));
        mSessionIdTask.execute();
    }

    public class GetSessionIdTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLogin;
        private final String mPassword;

        GetSessionIdTask(String login, String password) {
            mLogin = login;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                List<NameValuePair> reqString = new ArrayList<NameValuePair>();
                reqString.add(new BasicNameValuePair("login", mLogin));
                reqString.add(new BasicNameValuePair("password", mPassword));

                Log.d("lol", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        LoginActivity.LOGIN_URL, "GET", reqString);
                // check your log for json response
                Log.d("lol", json.toString());
                if (json.getInt("StatusCode") == 200) {
                    MainActivity.sessionId = json.getString("Data");
                    reqString.clear();
                    reqString.add(new BasicNameValuePair("sessionId", MainActivity.sessionId));
                    return true;
                }
            } catch (Exception e) {
                Log.d("lol", e.toString());
                return false;
            }
            return false;
        }
    }
}

