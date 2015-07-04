package ua.kpi.kbis.campus;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainPageFragment extends Fragment {

    private static final String BL_URL = "http://campus-api.azurewebsites.net/BulletinBoard/DeskGetActualBulletins?userId=";
    private static final String BL_PROFILE_URL = "http://campus-api.azurewebsites.net/BulletinBoard/GetAllowedProfiles?sessionId=";
    private static final String BL_FACULTY_URL = "http://campus-api.azurewebsites.net/BulletinBoard/GetAllowedSubdivisions?sessionId=";
    private static final String BL_GROUPS_URL = "http://campus-api.azurewebsites.net/BulletinBoard/GetAllowedGroups?sessionId=";
    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mButtonFloat;
    private View mProgressView;

    public MainPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page, container, false);
        mapViews(view);
        setTitle();
        initRecyclerView();
        initButtonFloat();
        initSwipeRefreshLayout();
        mProgressView.setVisibility(View.VISIBLE);
        try {
            MainActivity.currentUser.getJSONArray("bulletins");
            mProgressView.setVisibility(View.INVISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
            new BulletinsLoadTask().execute();
        }
        return view;
    }

    private void mapViews(View view) {
        mButtonFloat = (FloatingActionButton) view.findViewById(R.id.buttonFloat);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.bulletin_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_bulletins);
        mProgressView = view.findViewById(R.id.schedule_progress_wheel);
    }

    private void initRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        mAdapter = new MainAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }

    private void initButtonFloat() {
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
        mButtonFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddBulletin.class));
            }
        });
        mButtonFloat.setScaleX(0);
        mButtonFloat.setScaleY(0);
        mButtonFloat.animate().scaleX(1).scaleY(1).setDuration(500).setStartDelay(200).start();
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setProgressViewOffset(false, (int) (getResources().getDimension(R.dimen.app_bar_top_padding)), (int) (getResources().getDimension(R.dimen.app_bar_top_padding)
                + getResources().getDimension(R.dimen.actionBarSize)) + 48);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new BulletinsLoadTask().execute();
            }
        });
    }

    private void setTitle() {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_main_page_fragment);
        ((MainActivity) getActivity()).showToolbar();
    }

    public class BulletinsLoadTask extends AsyncTask<Void, Void, Boolean> {
        private JSONArray bulletins = new JSONArray();
        private JSONArray availableList = new JSONArray();
        private JSONArray availableListFaculty = new JSONArray();
        private JSONArray availableListGroup = new JSONArray();

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                JSONObject json = new JSONParser().makeHttpRequest(
                        BL_URL + MainActivity.prefs.getString("userId", null), "GET");
                JSONArray result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    bulletins.put(result.getJSONObject(i));
                }
                json = new JSONParser().makeHttpRequest(
                        BL_PROFILE_URL+ MainActivity.sessionId, "GET");
                result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    availableList.put(result.getString(i));
                }
                json = new JSONParser().makeHttpRequest(
                        BL_FACULTY_URL+ MainActivity.sessionId, "GET");
                result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    availableListFaculty.put(result.getString(i));
                }
                json = new JSONParser().makeHttpRequest(
                        BL_GROUPS_URL+ MainActivity.sessionId, "GET");
                result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    availableListGroup.put(result.getString(i));
                }
                MainActivity.currentUser.put("bulletins", bulletins);
                MainActivity.currentUser.put("availableFor", availableList);
                MainActivity.currentUser.put("availableForFaculty", availableListFaculty);
                MainActivity.currentUser.put("availableForGroup", availableListGroup);
                MainActivity.prefs.edit().putString(MainActivity.prefs.getString("userId", null), MainActivity.currentUser.toString()).commit();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                mAdapter.notifyDataSetChanged();
            }
            mProgressView.setVisibility(View.INVISIBLE);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

}
