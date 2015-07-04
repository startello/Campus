package ua.kpi.kbis.campus;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by Stanislav on 07.04.2015.
 */
public class SchedulePagerAdapter extends PagerAdapter {
    List<String> mDaysOfWeek = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ScheduleAdapter mAdapter;
    private View[] mPages;
    private int mWeek;
    private Context mContext;
    private Calendar mCalendar;
    private int mCurrentWeek;
    private int mCurrentWeekDay;

    public SchedulePagerAdapter(int week, Context context) {
        mWeek = week;
        mCalendar = Calendar.getInstance();
        mCurrentWeek = mCalendar.get(Calendar.WEEK_OF_YEAR) % 2;
        mCurrentWeekDay = mCalendar.get(Calendar.DAY_OF_WEEK) - 2;
        try {
            if (mCurrentWeekDay < 0) mCurrentWeekDay = 7 - mCurrentWeekDay;
            Log.v("lol", mCurrentWeek + " " + mCurrentWeekDay);
            if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek + 1 + "").opt(1) != null)
                mDaysOfWeek.add("ПН".toUpperCase());
            if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek + 1 + "").opt(2) != null)
                mDaysOfWeek.add("ВТ".toUpperCase());
            if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek + 1 + "").opt(3) != null)
                mDaysOfWeek.add("СР".toUpperCase());
            if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek + 1 + "").opt(4) != null)
                mDaysOfWeek.add("ЧТ".toUpperCase());
            if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek + 1 + "").opt(5) != null)
                mDaysOfWeek.add("ПТ".toUpperCase());
            if (MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek + 1 + "").opt(6) != null)
                mDaysOfWeek.add("СБ".toUpperCase());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mPages = new View[getCount()];
        mContext = context;
    }

    /*
     * @return the number of pages to display
     */
    @Override
    public int getCount() {
        return mDaysOfWeek.size();
    }

    /**
     * @return true if the value returned from {@link #instantiateItem(android.view.ViewGroup, int)} is the
     * same object as the {@link View} added to the {@link android.support.v4.view.ViewPager}.
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
        View view = ((MainActivity) mContext).getLayoutInflater().inflate(R.layout.pager_item,
                container, false);
        // Add the newly created View to the ViewPager
        container.addView(view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.schedule_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(mContext);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setBackgroundColor(mContext.getResources().getColor(R.color.background_material_light));
        mRecyclerView.setLayoutManager(mLayoutManager);
        try {
            mAdapter = new ScheduleAdapter(MainActivity.currentUser.getJSONObject("schedule").getJSONArray(mWeek + 1 + "").getJSONArray((position + 1)), mContext);
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

    public String getPageDate(int i) {
        mCalendar = Calendar.getInstance();
        mCalendar.add(Calendar.WEEK_OF_YEAR, mCurrentWeek != mWeek ? 1 : 0);
        mCalendar.add(Calendar.DAY_OF_WEEK, i - mCurrentWeekDay);
        return mCalendar.get(Calendar.DAY_OF_MONTH) + " " + mCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
    }
}
