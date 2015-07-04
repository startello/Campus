package ua.kpi.kbis.campus;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Stanislav on 17.02.2015.
 */
class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context mContext;
    private JSONArray mScheduleItems = new JSONArray();

    public ScheduleAdapter(JSONArray scheduleItems, Context context) {
        mContext = context;
        mScheduleItems = scheduleItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_row, parent, false);
        ScheduleViewHolder viewHolder = new ScheduleViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        try {
            ((ScheduleViewHolder) holder).bindScheduleItem(mScheduleItems.getJSONObject(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mScheduleItems.length();
    }

    class ScheduleViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        TextView mId;
        TextView mTime;
        TextView mName;
        TextView mTeacher;
        TextView mAuditory;
        View mItemView;

        public ScheduleViewHolder(View itemView) {
            super(itemView);
            mId = (TextView) itemView.findViewById(R.id.event_id);
            mTime = (TextView) itemView.findViewById(R.id.event_time);
            mName = (TextView) itemView.findViewById(R.id.event_name);
            mTeacher = (TextView) itemView.findViewById(R.id.event_teacher);
            mAuditory = (TextView) itemView.findViewById(R.id.event_auditory);
            mItemView = itemView;
            ((ImageView) itemView.findViewById(R.id.auditory_icon)).setColorFilter(mContext.getResources().getColor(R.color.text_secondary));
            ((ImageView) itemView.findViewById(R.id.time_icon)).setColorFilter(mContext.getResources().getColor(R.color.text_secondary));
            //itemView.setOnClickListener(this);
        }

        public void bindScheduleItem(JSONObject scheduleItem) {
            if (getPosition() == 0) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mItemView.getLayoutParams();
                params.setMargins(8, (int) (mContext.getResources().getDimension(R.dimen.app_bar_top_padding)
                        + mContext.getResources().getDimension(R.dimen.actionBarSize)) + 112, 8, 0); //substitute parameters for left, top, right, bottom
                mItemView.setLayoutParams(params);
            } else {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) mItemView.getLayoutParams();
                params.setMargins(8, 0, 8, 0); //substitute parameters for left, top, right, bottom
                mItemView.setLayoutParams(params);
            }
            try {
                mId.setText(scheduleItem.getString("lesson_number"));
                mTime.setText(scheduleItem.getString("time_start").substring(0, 5) + " - " + scheduleItem.getString("time_end").substring(0, 5));
                mName.setText(scheduleItem.getString("lesson_name"));
                mTeacher.setText(scheduleItem.getString("teacher_name"));
                if (mTeacher.getText().length() < 3) mTeacher.setHeight(0);
                mAuditory.setText(scheduleItem.getString("lesson_room"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
        }
    }
}
