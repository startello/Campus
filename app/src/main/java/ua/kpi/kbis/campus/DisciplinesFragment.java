package ua.kpi.kbis.campus;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class DisciplinesFragment extends Fragment {

    private static final String MZ_URL = "http://campus-api.azurewebsites.net/MZSearch/GetDiscList";
    private RecyclerView mRecyclerView;
    private DisciplinesAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private JSONParser jsonParser = new JSONParser();
    private DisciplinesLoadTask mDiscTask;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public DisciplinesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        for (int i = 1; i < 100; i++) {
//            disciplineList.add(new Discipline(1, "Назва дисципліни " + i, "Опис дисципліни " + i, R.drawable.ic_book_grey600_24dp));
//        }
        new DisciplinesLoadTask().execute();
        View layout = inflater.inflate(R.layout.fragment_disciplines, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipe_refresh_view);
        mRecyclerView = (RecyclerView) layout.findViewById(R.id.recycler_view);
        mAdapter = new DisciplinesAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager = new LinearLayoutManager(inflater.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
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
        mSwipeRefreshLayout.setProgressViewOffset(false, (int) (getResources().getDimension(R.dimen.app_bar_top_padding)), (int) (getResources().getDimension(R.dimen.app_bar_top_padding)
                + getResources().getDimension(R.dimen.actionBarSize)) + 48);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d("lol", "refreshing");
                //mDiscTask.cancel(true);
                mDiscTask = new DisciplinesLoadTask();
                mDiscTask.execute((Void) null);
            }
        });
//        mDiscTask = new DisciplinesLoadTask();
//        mDiscTask.execute((Void) null);
        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.title_disciplines_fragment);
        ((MainActivity) getActivity()).showToolbar();
    }

    public class DisciplinesLoadTask extends AsyncTask<Void, Void, Boolean> {
        private JSONArray disciplines = new JSONArray();

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                JSONObject json = jsonParser.makeHttpRequest(
                        MZ_URL, "GET");
                JSONArray result = json.getJSONArray("Data");
                for (int i = 0; i < result.length(); i++) {
                    disciplines.put(result.getJSONObject(i));
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
                    MainActivity.currentUser.put("disciplines", disciplines);
                    MainActivity.prefs.edit().putString(MainActivity.prefs.getString("userId", null), MainActivity.currentUser.toString()).commit();
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.menu_main_activity, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mAdapter.getFilter().filter(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.getFilter().filter(s);
                return true;
            }
        });
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }
}
