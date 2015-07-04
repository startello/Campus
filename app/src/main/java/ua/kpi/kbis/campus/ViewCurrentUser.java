package ua.kpi.kbis.campus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ViewCurrentUser extends BaseActivity implements ObservableScrollViewCallbacks {

    private static final String UI_URL = "http://campus-api.azurewebsites.net/User/Get";
    private Bundle mExtras;
    private JSONParser jsonParser = new JSONParser();
    private View mProgressView;
    private View mMainView;
    private ObservableScrollView mScrollView;
    private View mFab;
    private TextView mTitle;
    private View mOverlayView;
    private int mActionBarSize;
    private int mFlexibleSpaceShowFabOffset;
    private int mFlexibleSpaceImageHeight;
    private int mFabMargin;
    private boolean mFabIsShown;
    private CircleImageView mUserImage;
    private boolean mTitleIsShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_current_user);
        setSupportActionBar((Toolbar) findViewById(R.id.app_bar_view_current_user));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mFlexibleSpaceShowFabOffset = getResources().getDimensionPixelSize(R.dimen.flexible_space_show_fab_offset);
        mActionBarSize = getActionBarSize();

        mProgressView = findViewById(R.id.view_current_user_load_progress);
        mMainView = findViewById(R.id.view_current_user_container);
        //mTitleView = (TextView) findViewById(R.id.view_current_user_name);
        //mImageView = findViewById(R.id.view_current_user_image);
        mOverlayView = findViewById(R.id.view_current_user_overlay);
        mScrollView = (ObservableScrollView) findViewById(R.id.view_current_user_scroll_view);
        mFab = findViewById(R.id.view_current_user_edit);
        mTitle = (TextView) findViewById(R.id.view_current_user_title);

        mUserImage = (CircleImageView) findViewById(R.id.view_current_user_image);

        mExtras = getIntent().getExtras();


        mScrollView.setScrollViewCallbacks(this);
        setTitle(null);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ViewCurrentUser.this, "FAB is clicked", Toast.LENGTH_SHORT).show();
            }
        });
        mFabMargin = getResources().getDimensionPixelSize(R.dimen.margin_standard);
        ViewHelper.setScaleX(mFab, 0);
        ViewHelper.setScaleY(mFab, 0);
        ViewHelper.setAlpha(mTitle, 0);


        showProgress(false);
        if (MainActivity.prefs.getString(mExtras.getInt("userId") + "", null) == null) {
            showProgress(true);
            new UserInfoLoadTask().execute();
        } else {
            try {
                PopulateView();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void showProgress(final boolean show) {
        if (this.getCurrentFocus() != null)
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void PopulateView() throws JSONException {
        JSONObject mUser = new JSONObject(MainActivity.prefs.getString(mExtras.getInt("userId") + "", null));
        mTitle.setText(mUser.getString("FullName"));
        ((TextView) findViewById(R.id.view_current_user_name)).setText(mUser.getString("FullName"));
        ((TextView) findViewById(R.id.view_current_user_credo)).setText(mUser.getString("Credo"));
        CardView mCardView = (CardView) findViewById(R.id.work_place_card);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mCardView.getLayoutParams();
        params.setMargins(8, 8, 8, 0);
        mCardView.setLayoutParams(params);
        LinearLayout mCardViewLinearLayout = (LinearLayout) mCardView.findViewById(R.id.work_place_card_items);
        JSONArray employees = mUser.getJSONArray("Employees");
        LinearLayout mCardItem;
        for (int i = 0; i < employees.length(); i++) {
            mCardItem = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.work_place_item, null);
            ((TextView) mCardItem.findViewById(R.id.work_place_item_place)).setText(employees.getJSONObject(i).getString("SubdivisionName"));
            ((TextView) mCardItem.findViewById(R.id.work_place_item_position)).setText(employees.getJSONObject(i).getString("Position"));
            ((TextView) mCardItem.findViewById(R.id.work_place_item_grade)).setText(employees.getJSONObject(i).getString("AcademicDegree"));
            mCardViewLinearLayout.addView(mCardItem);
        }
        mCardView = (CardView) findViewById(R.id.contacts_card);
        params = (LinearLayout.LayoutParams) mCardView.getLayoutParams();
        params.setMargins(8, 0, 8, 16);
        mCardView.setLayoutParams(params);
        mCardViewLinearLayout = (LinearLayout) mCardView.findViewById(R.id.contacts_card_items);
        JSONArray profiles = mUser.getJSONArray("Contacts");
        for (int i = 0; i < mUser.getJSONArray("Contacts").length(); i++) {
            mCardItem = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.contacts_item, null);
            ((TextView) mCardItem.findViewById(R.id.contacts_item_type)).setText(profiles.getJSONObject(i).getString("ContactTypeName"));
            ((TextView) mCardItem.findViewById(R.id.contacts_item_number)).setText(profiles.getJSONObject(i).getString("UserContactValue"));
            mCardViewLinearLayout.addView(mCardItem);
        }
        //mTitleView.setText(mUser.getString("FullName"));
        ImageLoader.getInstance().displayImage("http://campus-api.azurewebsites.net/Storage/GetUserProfileImage?userId=" + mExtras.getInt("userId"), mUserImage);
        ScrollUtils.addOnGlobalLayoutListener(mScrollView, new Runnable() {
            @Override
            public void run() {
                onScrollChanged(0, false, false);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_current_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
        int minOverlayTransitionY = mActionBarSize - mOverlayView.getHeight();
        ViewHelper.setTranslationY(mOverlayView, ScrollUtils.getFloat(-scrollY, minOverlayTransitionY, 0));
        int appBarPadding = (int) getResources().getDimension(R.dimen.app_bar_top_padding);

        // Translate FAB
        int maxFabTranslationY = mFlexibleSpaceImageHeight - mFab.getHeight() / 2 + appBarPadding / 2;
        float fabTranslationY = ScrollUtils.getFloat(
                -scrollY + maxFabTranslationY,
                mActionBarSize - mFab.getHeight() / 2,
                maxFabTranslationY);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // On pre-honeycomb, ViewHelper.setTranslationX/Y does not set margin,
            // which causes FAB's OnClickListener not working.
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mFab.getLayoutParams();
            lp.leftMargin = mOverlayView.getWidth() - mFabMargin - mFab.getWidth();
            lp.topMargin = (int) fabTranslationY;
            mFab.requestLayout();
        } else {
            ViewHelper.setTranslationX(mFab, mOverlayView.getWidth() - mFabMargin - mFab.getWidth());
            ViewHelper.setTranslationY(mFab, fabTranslationY);
        }

        // Show/hide FAB
        if (fabTranslationY < mFlexibleSpaceShowFabOffset) {
            hideFab();
        } else {
            showFab();
        }

        // Translate Image
        int maxImageTranslationY = mFlexibleSpaceImageHeight - mUserImage.getHeight() - 48;
        int minImageTranslationY = -mUserImage.getHeight();
        float imageTranslationY = ScrollUtils.getFloat(
                -scrollY + maxImageTranslationY,
                minImageTranslationY,
                maxImageTranslationY);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            // On pre-honeycomb, ViewHelper.setTranslationX/Y does not set margin,
            // which causes FAB's OnClickListener not working.
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mUserImage.getLayoutParams();
            lp.leftMargin = mOverlayView.getWidth() / 2 - mUserImage.getWidth() / 2;
            lp.topMargin = (int) imageTranslationY;
            mFab.requestLayout();
        } else {
            ViewHelper.setTranslationX(mUserImage, mOverlayView.getWidth() / 2 - mUserImage.getWidth() / 2);
            ViewHelper.setTranslationY(mUserImage, imageTranslationY);
        }
        if (imageTranslationY <= minImageTranslationY + 1) {
            showTitle();
        } else {
            hideTitle();
        }
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    private void showFab() {
        if (!mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(1).scaleY(1).setDuration(300).start();
            mFabIsShown = true;
        }
    }

    private void hideFab() {
        if (mFabIsShown) {
            ViewPropertyAnimator.animate(mFab).cancel();
            ViewPropertyAnimator.animate(mFab).scaleX(0).scaleY(0).setDuration(300).start();
            mFabIsShown = false;
        }
    }

    private void showTitle() {
        if (!mTitleIsShown) {
            ViewPropertyAnimator.animate(mTitle).cancel();
            ViewPropertyAnimator.animate(mTitle).alpha(1).setDuration(100).start();
            mTitleIsShown = true;
        }
    }

    private void hideTitle() {
        if (mTitleIsShown) {
            ViewPropertyAnimator.animate(mTitle).cancel();
            ViewPropertyAnimator.animate(mTitle).alpha(0).setDuration(100).start();
            mTitleIsShown = false;
        }
    }

    public class UserInfoLoadTask extends AsyncTask<Void, Void, Boolean> {
        private JSONObject result = new JSONObject();

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                List<NameValuePair> reqString = new ArrayList<>();
                reqString.add(new BasicNameValuePair("sessionId", MainActivity.sessionId));
                reqString.add(new BasicNameValuePair("userId", mExtras.getInt("userId") + ""));
                JSONObject json = jsonParser.makeHttpRequest(
                        UI_URL, "GET", reqString);
                result = json.getJSONObject("Data");
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                MainActivity.prefs.edit().putString(mExtras.getInt("userId") + "", result.toString()).commit();
                try {
                    PopulateView();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                finish();
            }
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            showProgress(false);
        }
    }
}
