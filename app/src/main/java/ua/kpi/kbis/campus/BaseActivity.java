package ua.kpi.kbis.campus;

import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Stanislav on 06.04.2015.
 */
public abstract class BaseActivity extends ActionBarActivity {
    private static final int NUM_OF_ITEMS = 100;
    private static final int NUM_OF_ITEMS_FEW = 3;

    public static ArrayList<String> getDummyData() {
        return getDummyData(NUM_OF_ITEMS);
    }

    protected void setDummyData(GridView gridView) {
        gridView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getDummyData()));
    }

    public static ArrayList<String> getDummyData(int num) {
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 1; i <= num; i++) {
            items.add("Item " + i);
        }
        return items;
    }

    protected int getActionBarSize() {
        return (int) (getResources().getDimension(R.dimen.actionBarSize) + getResources().getDimension(R.dimen.app_bar_top_padding));
    }

    protected int getScreenHeight() {
        return findViewById(android.R.id.content).getHeight();
    }

    protected void setDummyData(ListView listView) {
        setDummyData(listView, NUM_OF_ITEMS);
    }

    protected void setDummyDataFew(ListView listView) {
        setDummyData(listView, NUM_OF_ITEMS_FEW);
    }

    protected void setDummyData(ListView listView, int num) {
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getDummyData(num)));
    }

    protected void setDummyDataWithHeader(ListView listView, int headerHeight) {
        setDummyDataWithHeader(listView, headerHeight, NUM_OF_ITEMS);
    }

    protected void setDummyDataWithHeader(ListView listView, int headerHeight, int num) {
        View headerView = new View(this);
        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerHeight));
        headerView.setMinimumHeight(headerHeight);
        // This is required to disable header's list selector effect
        headerView.setClickable(true);
        setDummyDataWithHeader(listView, headerView, num);
    }

    protected void setDummyDataWithHeader(ListView listView, View headerView, int num) {
        listView.addHeaderView(headerView);
        setDummyData(listView, num);
    }

    /*protected void setDummyData(RecyclerView recyclerView) {
        setDummyData(recyclerView, NUM_OF_ITEMS);
    }

    protected void setDummyDataFew(RecyclerView recyclerView) {
        setDummyData(recyclerView, NUM_OF_ITEMS_FEW);
    }*/

    /*protected void setDummyData(RecyclerView recyclerView, int num) {
        recyclerView.setAdapter(new SimpleRecyclerAdapter(this, getDummyData(num)));
    }*/

    /*protected void setDummyDataWithHeader(RecyclerView recyclerView, int headerHeight) {
        View headerView = new View(this);
        headerView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, headerHeight));
        headerView.setMinimumHeight(headerHeight);
        // This is required to disable header's list selector effect
        headerView.setClickable(true);
        setDummyDataWithHeader(recyclerView, headerView);
    }*/

    /*protected void setDummyDataWithHeader(RecyclerView recyclerView, View headerView) {
        recyclerView.setAdapter(new SimpleHeaderRecyclerAdapter(this, getDummyData(), headerView));
    }*/
}
