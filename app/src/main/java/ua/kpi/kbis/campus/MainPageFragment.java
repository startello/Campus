package ua.kpi.kbis.campus;


import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import com.gc.materialdesign.views.ButtonFloat;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainPageFragment extends Fragment {

    private static final String BL_URL = "http://campus-api.azurewebsites.net/BulletinBoard/DeskGetActualBulletins?userId=";
    private static final String BL_GROUPS_URL = "http://campus-api.azurewebsites.net/BulletinBoard/DeskGetProfileTypesList";
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private MainAdapter mAdapter;
    private JSONParser jsonParser = new JSONParser();
    private BulletinsLoadTask mDiscTask;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mButtonFloat;
    private View mView;
    private FragmentActivity mContext;

    public MainPageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        mContext = (FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new BulletinsLoadTask().execute();
        mView = inflater.inflate(R.layout.fragment_main_page, container, false);
        mButtonFloat = (FloatingActionButton) mView.findViewById(R.id.buttonFloat);
        mButtonFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddBulletin.class));
            }
        });
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(500);
        anim.setStartOffset(200);
        mButtonFloat.startAnimation(anim);
        mRecyclerView = (RecyclerView) mView.findViewById(R.id.bulletin_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(), 1);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MainAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mButtonFloat.attachToRecyclerView(mRecyclerView, new ScrollDirectionListener() {
            @Override
            public void onScrollDown() {
            }
            @Override
            public void onScrollUp() {
            }
        }, new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerView.getChildAt(0) != null)
                    if (recyclerView.getChildAt(0).getTop() < 0) {
                        ((MainActivity) getActivity()).finishMoveToolbar();
                    } else {
                        ((MainActivity) getActivity()).showToolbar();
                    }
            }
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                ((MainActivity) getActivity()).moveToolbar(dy);
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) mView.findViewById(R.id.swipe_refresh_bulletins);
        mSwipeRefreshLayout.setProgressViewOffset(false, (int) (getResources().getDimension(R.dimen.app_bar_top_padding)), (int) (getResources().getDimension(R.dimen.app_bar_top_padding)
                + getResources().getDimension(R.dimen.actionBarSize)) + 48);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new BulletinsLoadTask().execute();
            }
        });
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_main_page_fragment);
        ((MainActivity) getActivity()).showToolbar();
    }

    public class BulletinsLoadTask extends AsyncTask<Void, Void, Boolean> {
        private JSONArray bulletins = new JSONArray();
        private JSONArray availableList = new JSONArray();

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                JSONObject json = jsonParser.makeHttpRequest(
                        BL_URL + MainActivity.currentUser.getString("UserAccountId"), "GET");
                JSONArray result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    bulletins.put(result.getJSONObject(i));
                }
                json = jsonParser.makeHttpRequest(
                        BL_GROUPS_URL, "GET");
                result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    availableList.put(result.getJSONObject(i));
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
                    MainActivity.currentUser.put("bulletins", bulletins);
                    MainActivity.currentUser.put("availableFor", availableList);
                    MainActivity.prefs.edit().putString(MainActivity.prefs.getString("login",null),MainActivity.currentUser.toString()).commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mAdapter.notifyDataSetChanged();
            } else {
            }
        }

        @Override
        protected void onCancelled() {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

}
