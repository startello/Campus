package ua.kpi.kbis.campus;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Stanislav on 31.03.2015.
 */
public class MainAdapter extends RecyclerView.Adapter<MainAdapter.MainViewHolder> {
    private Context mContext;

    public MainAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        CardView view = (CardView) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.bulletin_card, viewGroup, false);
        MainViewHolder viewHolder = new MainViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        try {
            holder.bindMainItem(MainActivity.currentUser.getJSONArray("bulletins").getJSONObject(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        try {
            return MainActivity.currentUser.getJSONArray("bulletins").length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public class MainViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final TextView mCaption;
        private final TextView mText;
        private final TextView mDate;
        private final TextView mAuthor;

        public MainViewHolder(CardView itemView) {
            super(itemView);
            mCaption = (TextView) itemView.findViewById(R.id.bulletin_caption);
            mText = (TextView) itemView.findViewById(R.id.bulletin_text);
            mDate = (TextView) itemView.findViewById(R.id.bulletin_date);
            mAuthor = (TextView) itemView.findViewById(R.id.bulletin_author);
            itemView.setPreventCornerOverlap(false);
            itemView.setOnClickListener(this);
        }

        public void bindMainItem(JSONObject item) {
            if (getPosition() == 0) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                params.setMargins(8, (int) (mContext.getResources().getDimension(R.dimen.app_bar_top_padding)
                        + mContext.getResources().getDimension(R.dimen.actionBarSize)) + 8, 8, 0);
                itemView.setLayoutParams(params);
            } else {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) itemView.getLayoutParams();
                params.setMargins(8, 0, 8, 0);
                itemView.setLayoutParams(params);
            }
            try {
                mCaption.setText(item.getString("Subject"));
                mText.setText(item.getString("Text"));
                mDate.setText(item.getString("CreationDate").substring(0, 10));
                mAuthor.setText(" " + item.getString("CreatorName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            try {
                mContext.startActivity(new Intent(mContext, ViewBulletin.class).putExtra("item",
                        MainActivity.currentUser.getJSONArray("bulletins").getJSONObject(getPosition()).toString()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
