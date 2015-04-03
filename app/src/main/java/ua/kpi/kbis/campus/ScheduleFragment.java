package ua.kpi.kbis.campus;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleFragment extends Fragment {


    private static final String SCHEDULE_URL = "http://campus-api.azurewebsites.net/Schedule/GetLessons?uid=485";
    private ViewPager mViewPager;
    private SlidingTabLayout mSlidingTabLayout;
    private JSONParser jsonParser = new JSONParser();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SchedulePagerAdapter mAdapter;
    private ScheduleLoadTask mScheduleTask;

    public ScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        new ScheduleLoadTask().execute();
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_view_schedule);
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mAdapter = new SchedulePagerAdapter(1);
        mViewPager.setAdapter(mAdapter);
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setCustomTabView(R.layout.pager_tab, R.id.pager_tab_text);
        mSlidingTabLayout.setViewPager(mViewPager);
        mSlidingTabLayout.setPadding(0, (int) (getResources().getDimension(R.dimen.app_bar_top_padding)
                + getResources().getDimension(R.dimen.actionBarSize)), 0, 0);
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
        mSlidingTabLayout.setDividerColors(getResources().getColor(R.color.text_icon));
        mSwipeRefreshLayout.setProgressViewOffset(false, (int) (getResources().getDimension(R.dimen.app_bar_top_padding)
                + getResources().getDimension(R.dimen.actionBarSize)) + 48 + 48, (int) (getResources().getDimension(R.dimen.app_bar_top_padding)
                + getResources().getDimension(R.dimen.actionBarSize)) + 48 + 48 + 48);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new ScheduleLoadTask().execute();
            }
        });
        mSwipeRefreshLayout.requestDisallowInterceptTouchEvent(false);
        mSwipeRefreshLayout.setEnabled(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_schedule_fragment);
        ((MainActivity) getActivity()).showToolbar();
    }
    public class ScheduleLoadTask extends AsyncTask<Void, Void, Boolean> {
        private JSONObject schedule = new JSONObject();

        //int mSize = 0;
        @Override
        protected Boolean doInBackground(Void... params) {
            //mSize = disciplineList.size();
            try {
                JSONObject json = jsonParser.makeHttpRequest(
                        SCHEDULE_URL, "GET");
                JSONArray result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject object = result.getJSONObject(i);
                    if (schedule.opt(object.getString("lesson_week")) == null)
                        schedule.put(object.getString("lesson_week"), new JSONArray());
                    if (schedule.getJSONArray(object.getString("lesson_week")).opt(object.getInt("day_number")) == null)
                        schedule.getJSONArray(object.getString("lesson_week")).put(object.getInt("day_number"), new JSONArray());
                    schedule.getJSONArray(object.getString("lesson_week")).getJSONArray(object.getInt("day_number")).put(object);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mSwipeRefreshLayout.setRefreshing(false);
            if (success) {
                try {
                    MainActivity.currentUser.put("schedule", schedule);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MainActivity.prefs.edit().putString(MainActivity.prefs.getString("login",null),MainActivity.currentUser.toString()).commit();
                mAdapter = new SchedulePagerAdapter(1);
                mViewPager.setAdapter(mAdapter);
                mSlidingTabLayout.setViewPager(mViewPager);
            } else {
            }
        }

        @Override
        protected void onCancelled() {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    class SchedulePagerAdapter extends PagerAdapter {
        List<String> mDaysOfWeek = new ArrayList<>();
        private RecyclerView mRecyclerView;
        private LinearLayoutManager mLayoutManager;
        private ScheduleAdapter mAdapter;
        private View[] mPages;
        private int mWeek;
        SchedulePagerAdapter(int week) {
            mWeek = week;
            try {
                if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek+"").opt(1) != null)
                    mDaysOfWeek.add("Понеділок".toUpperCase());
                if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek+"").opt(2) != null)
                    mDaysOfWeek.add("Вівторок".toUpperCase());
                if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek+"").opt(3) != null)
                    mDaysOfWeek.add("Середа".toUpperCase());
                if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek+"").opt(4) != null)
                    mDaysOfWeek.add("Четвер".toUpperCase());
                if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek+"").opt(5) != null)
                    mDaysOfWeek.add("П'ятниця".toUpperCase());
                if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek+"").opt(6) != null)
                    mDaysOfWeek.add("Субота".toUpperCase());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mPages = new View[getCount()];
        }

        /*
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return mDaysOfWeek.size();
        }

        /**
         * @return true if the value returned from {@link #instantiateItem(ViewGroup, int)} is the
         * same object as the {@link View} added to the {@link ViewPager}.
         */
        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)

        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p/>
         * Here we construct one using the position value, but for real application the title should
         * refer to the item's contents.
         */
        public View getPage(int position) {
            return mPages[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDaysOfWeek.get(position);
        }
        // END_INCLUDE (pageradapter_getpagetitle)

        /**
         * Instantiate the {@link View} which should be displayed at {@code position}. Here we
         * inflate a layout from the apps resources and then change the text view to signify the position.
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Inflate a new layout from our resources
            View view = getActivity().getLayoutInflater().inflate(R.layout.pager_item,
                    container, false);
            // Add the newly created View to the ViewPager
            container.addView(view);
            mRecyclerView = (RecyclerView) view.findViewById(R.id.schedule_recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(getActivity());
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mRecyclerView.setLayoutManager(mLayoutManager);
            try {
                mAdapter = new ScheduleAdapter(MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek+"").getJSONArray((position + 1)), getActivity());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mRecyclerView.setAdapter(mAdapter);
            mPages[position] = view;
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
}
