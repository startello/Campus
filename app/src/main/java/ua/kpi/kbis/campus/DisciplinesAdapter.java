package ua.kpi.kbis.campus;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Stanislav on 18.02.2015.
 */
class DisciplinesAdapter extends RecyclerView.Adapter<DisciplinesAdapter.DisciplinesViewHolder> implements Filterable {
    private final Context mContext;
    private JSONArray mData = new JSONArray();

    public DisciplinesAdapter(Context context) {
        try {
            mData = MainActivity.currentUser.getJSONArray("disciplines");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mContext = context;
    }

    @Override
    public DisciplinesViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.discipline_row, viewGroup, false);
        DisciplinesViewHolder viewHolder = new DisciplinesViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DisciplinesViewHolder holder, final int position) {
        try {
            holder.bindDisciplinesItem(mData.getJSONObject(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mData.length();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                JSONArray resultData = new JSONArray();
                Log.v("lol",mData.toString());
                try {
                    JSONArray originalData = MainActivity.currentUser.getJSONArray("disciplines");
                    for (int i = 0; i< originalData.length(); i++) {
                        JSONObject item =originalData.getJSONObject(i);
                        if (item.getString("Name").toLowerCase().contains(constraint.toString().toLowerCase()))
                            resultData.put(item);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = resultData;
                filterResults.count = resultData.length();
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mData = (JSONArray) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class DisciplinesViewHolder extends RecyclerView.ViewHolder {
        TextView mName;
        TextView mInfo;
        ImageView mIcon;
        View mItemView;

        public DisciplinesViewHolder(View itemView) {
            super(itemView);
            mName = (TextView) itemView.findViewById(R.id.discipline_name);
            mInfo = (TextView) itemView.findViewById(R.id.discipline_info);
            mIcon = (ImageView) itemView.findViewById(R.id.discipline_icon);
            mItemView = itemView;
        }

        public void bindDisciplinesItem(JSONObject item) {
            if (getPosition() == 0) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mItemView.getLayoutParams();
                params.setMargins(0, (int) (mContext.getResources().getDimension(R.dimen.app_bar_top_padding)
                        + mContext.getResources().getDimension(R.dimen.actionBarSize)) + 8, 0, 0); //substitute parameters for left, top, right, bottom
                mItemView.setLayoutParams(params);
            } else {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mItemView.getLayoutParams();
                params.setMargins(0, 0, 0, 0); //substitute parameters for left, top, right, bottom
                mItemView.setLayoutParams(params);
            }
            mItemView.findViewById(R.id.list_item_info).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.mInfoDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    MainActivity.mInfoDrawerLayout.openDrawer(GravityCompat.END);
                }
            });
            try {
                mName.setText(item.getString("Name"));
                mInfo.setText(item.getString("Name") + " опис");
                mIcon.setImageResource(R.drawable.ic_book_grey600_24dp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
