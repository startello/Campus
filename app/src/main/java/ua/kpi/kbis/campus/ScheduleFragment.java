package ua.kpi.kbis.campus;


import android.animation.Animator;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.mrengineer13.snackbar.SnackBar;
import com.kyleduo.switchbutton.switchbutton.SwitchButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class ScheduleFragment extends Fragment {


    private static final String SCHEDULE_URL = "http://campus-api.azurewebsites.net/Schedule/GetLessons?uid=485";
    private ViewPager mFirstWeekViewPager;
    private SlidingTabLayout mFirstWeekSlidingTabLayout;
    private ViewPager mSecondWeekViewPager;
    private SlidingTabLayout mSecondWeekSlidingTabLayout;
    private SchedulePagerAdapter mFirstWeekAdapter;
    private SchedulePagerAdapter mSecondWeekAdapter;
    private SwitchButton mSwitch;
    private View mProgressView;
    private int mCurrentWeek;
    private int mCurrentWeekDay;
    private View mView;

    public ScheduleFragment() {
        mCurrentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) % 2;
        mCurrentWeekDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
        if (mCurrentWeekDay < 0) mCurrentWeekDay = 7 - mCurrentWeekDay;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_schedule, container, false);
        mapViews();
        initSwitch();
        setTitle();
        mProgressView.setVisibility(View.VISIBLE);
        try {
            MainActivity.currentUser.getJSONObject("schedule");
            initAdapters();
            initSlidingTabLayout();
            selectCurrentDay();
            mProgressView.setVisibility(View.INVISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
            new ScheduleLoadTask().execute();
        }
        return mView;
    }

    private void mapViews() {
        mFirstWeekSlidingTabLayout = (SlidingTabLayout) mView.findViewById(R.id.sliding_tabs_first);
        mFirstWeekViewPager = (ViewPager) mView.findViewById(R.id.viewpager_first);
        mSecondWeekSlidingTabLayout = (SlidingTabLayout) mView.findViewById(R.id.sliding_tabs_second);
        mSecondWeekViewPager = (ViewPager) mView.findViewById(R.id.viewpager_second);
        mSwitch = (SwitchButton) MainActivity.mToolbar.findViewById(R.id.schedule_switch_button);
        mProgressView = mView.findViewById(R.id.schedule_progress_wheel);
        getActivity().findViewById(R.id.drop_shadow).setVisibility(View.INVISIBLE);
    }

    private void initAdapters() {
        mFirstWeekAdapter = new SchedulePagerAdapter(0, getActivity());
        mSecondWeekAdapter = new SchedulePagerAdapter(1, getActivity());
        mFirstWeekViewPager.setAdapter(mFirstWeekAdapter);
        mSecondWeekViewPager.setAdapter(mSecondWeekAdapter);
    }

    private void showViewPager(boolean week) {
        if (week) {
            mSecondWeekViewPager.animate().cancel();
            mSecondWeekSlidingTabLayout.setVisibility(View.VISIBLE);
            mSecondWeekViewPager.setVisibility(View.VISIBLE);
            mSecondWeekViewPager.animate().scaleX(1).scaleY(1).setDuration(200).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mFirstWeekSlidingTabLayout.setVisibility(View.INVISIBLE);
                    mFirstWeekViewPager.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        } else {
            mSecondWeekViewPager.animate().cancel();
            mFirstWeekSlidingTabLayout.setVisibility(View.VISIBLE);
            mFirstWeekViewPager.setVisibility(View.VISIBLE);
            mSecondWeekViewPager.animate().scaleX(0).scaleY(0).setDuration(200).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mSecondWeekSlidingTabLayout.setVisibility(View.INVISIBLE);
                    mSecondWeekViewPager.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
    }

    private void selectCurrentDay() {
        if (mCurrentWeek == 0) {
            if (mCurrentWeekDay < mFirstWeekAdapter.getCount()) {
                mSwitch.setChecked(false);
                mFirstWeekViewPager.setCurrentItem(mCurrentWeekDay);
                ((TextView) mFirstWeekSlidingTabLayout.getTabStrip().getChildAt(mCurrentWeekDay).findViewById(R.id.pager_tab_text)).
                        setTypeface(Typeface.DEFAULT_BOLD);
                ((TextView) mFirstWeekSlidingTabLayout.getTabStrip().getChildAt(mCurrentWeekDay).findViewById(R.id.pager_tab_date)).
                        setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                mSwitch.setChecked(true);
            }
        } else {
            if (mCurrentWeekDay < mSecondWeekAdapter.getCount()) {
                mSwitch.setChecked(true);
                mSecondWeekViewPager.setCurrentItem(mCurrentWeekDay);
                ((TextView) mSecondWeekSlidingTabLayout.getTabStrip().getChildAt(mCurrentWeekDay).findViewById(R.id.pager_tab_text)).
                        setTypeface(Typeface.DEFAULT_BOLD);
                ((TextView) mSecondWeekSlidingTabLayout.getTabStrip().getChildAt(mCurrentWeekDay).findViewById(R.id.pager_tab_date)).
                        setTypeface(Typeface.DEFAULT_BOLD);
            } else {
                mSwitch.setChecked(false);
            }
        }
        mSwitch.setEnabled(true);
    }

    private void initSlidingTabLayout() {
        mFirstWeekSlidingTabLayout.setCustomTabView(R.layout.pager_tab, R.id.pager_tab_text);
        mFirstWeekSlidingTabLayout.setPadding(0, (int) (getResources().getDimension(R.dimen.app_bar_top_padding)
                + getResources().getDimension(R.dimen.actionBarSize)), 0, 0);
        mFirstWeekSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.text_icon));
        mSecondWeekSlidingTabLayout.setCustomTabView(R.layout.pager_tab, R.id.pager_tab_text);
        mSecondWeekSlidingTabLayout.setPadding(0, (int) (getResources().getDimension(R.dimen.app_bar_top_padding)
                + getResources().getDimension(R.dimen.actionBarSize)), 0, 0);
        mSecondWeekSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.text_icon));
        mFirstWeekSlidingTabLayout.setViewPager(mFirstWeekViewPager);
        mSecondWeekSlidingTabLayout.setViewPager(mSecondWeekViewPager);
        showViewPager(mSwitch.isChecked());
    }

    private void initSwitch() {
        mSwitch.setVisibility(View.VISIBLE);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showViewPager(isChecked);
            }
        });
        mSwitch.setEnabled(false);
    }

    private void setTitle() {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_schedule_fragment);
        ((MainActivity) getActivity()).showToolbar();
    }
    @Override
    public void onDetach() {
        super.onDetach();
        mSwitch.setVisibility(View.INVISIBLE);
        getActivity().findViewById(R.id.drop_shadow).setVisibility(View.VISIBLE);
    }

    public class ScheduleLoadTask extends AsyncTask<Void, Void, Boolean> {
        private JSONObject schedule = new JSONObject();

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                JSONObject json = new JSONParser().makeHttpRequest(
                        SCHEDULE_URL, "GET");
                JSONArray result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject object = result.getJSONObject(i);
                    if (schedule.opt(object.getString("lesson_week")) == null)
                        schedule.put(object.getString("lesson_week"), new JSONArray());
                    if (schedule.getJSONArray(object.getString("lesson_week")).opt(object.getInt("day_number")) == null)
                        schedule.getJSONArray(object.getString("lesson_week")).put(object.getInt("day_number"), new JSONArray());
                    schedule.getJSONArray(object.getString("lesson_week")).getJSONArray(object.getInt("day_number")).put(object);
                    MainActivity.currentUser.put("schedule", schedule);
                    MainActivity.prefs.edit().putString(MainActivity.prefs.getString("userId", null), MainActivity.currentUser.toString()).commit();
                }
                return true;
            } catch (Exception e) {
/*                new SnackBar.Builder(getActivity().getApplicationContext(), mView)
                        .withMessage(e.toString())
//                        .withMessageId(messageId)
                        .withTextColorId(R.color.accent)
                        .show();*/
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                initAdapters();
                initSlidingTabLayout();
                selectCurrentDay();
            }
            mProgressView.setVisibility(View.INVISIBLE);
        }
    }
}
