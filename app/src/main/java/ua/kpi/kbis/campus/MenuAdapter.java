package ua.kpi.kbis.campus;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SingleSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Stanislav on 17.02.2015.
 */
class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static MultiSelector mMultiSelector = new SingleSelector();
    private final FragmentManager mFragmentManager;
    private final Context mContext;
    private List<NavMenuItem> mMenuItems = new ArrayList<>();

    public MenuAdapter(List<NavMenuItem> menuItems, FragmentManager fragmentManager, Context context) {
        mFragmentManager = fragmentManager;
        mContext = context;
        mMenuItems = menuItems;
        mMultiSelector.setSelectable(true);
        if (mMultiSelector.getSelectedPositions().isEmpty()) {
            mFragmentManager.beginTransaction().replace(R.id.fragment_container, mMenuItems.get(0).menuFragment).commit();
            mMultiSelector.setSelected(1, 0, true);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item_row, parent, false);
            return new MenuViewHolder(view);
        } else if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_nav_image, parent, false);
            return new MenuHeaderHolder(view);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof MenuViewHolder) {
            ((MenuViewHolder) holder).bindMenuItem(getItem(position));
        } else if (holder instanceof MenuHeaderHolder) {
            ((MenuHeaderHolder) holder).bindMenuHeader();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private NavMenuItem getItem(int position) {
        return mMenuItems.get(position - 1);
    }

    @Override
    public int getItemCount() {
        return mMenuItems.size() + 1;
    }

    class MenuViewHolder extends SwappingHolder
            implements View.OnClickListener {
        private RelativeLayout mRelativeLayout;
        private TextView mName;
        private ImageView mIcon;

        public MenuViewHolder(View itemView) {
            super(itemView, mMultiSelector);
            mName = (TextView) itemView.findViewById(R.id.menu_item_name);
            mIcon = (ImageView) itemView.findViewById(R.id.menu_item_icon);
            mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.menu_item_relative);
            itemView.setOnClickListener(this);
        }

        public void bindMenuItem(NavMenuItem menuItem) {
            mName.setText(menuItem.name);
            mIcon.setImageResource(menuItem.icon);

        }

        @Override
        public void setActivated(boolean activated) {
            if (activated) {
                mName.setTextColor(mContext.getResources().getColor(R.color.primary));
                mRelativeLayout.setBackgroundColor(mContext.getResources().getColor(R.color.menu_item_checked));
                mIcon.setColorFilter(mContext.getResources().getColor(R.color.primary));
            } else {
                mName.setTextColor(mContext.getResources().getColor(R.color.text_primary));
                mIcon.setColorFilter(mContext.getResources().getColor(R.color.text_secondary));
                mRelativeLayout.setBackgroundColor(mContext.getResources().getColor(R.color.menu_item_unchecked));
            }
        }
        @Override
        public void onClick(View v) {
            if (mMultiSelector.isSelectable()) {
                mMultiSelector.setSelected(getPosition(), 0, true);
                MainActivity.mDrawerLayout.closeDrawers();
                MainActivity.currentFragment = mMenuItems.get(getPosition() - 1).menuFragment;
                mFragmentManager.beginTransaction().replace(R.id.fragment_container, mMenuItems.get(getPosition() - 1).menuFragment).commit();
            }
        }
    }

    class MenuHeaderHolder extends RecyclerView.ViewHolder {
        private final CircleImageView mImage;
        private final ImageView mBg;
        private final TextView mName;
        private final TextView mInfo;
        private com.nostra13.universalimageloader.core.ImageLoader imageLoader;

        public MenuHeaderHolder(View itemView) {
            super(itemView);
            mImage = (CircleImageView) itemView.findViewById(R.id.nav_user_image);
            mBg = (ImageView) itemView.findViewById(R.id.nav_bg);
            mName = (TextView) itemView.findViewById(R.id.nav_user_name);
            mInfo = (TextView) itemView.findViewById(R.id.nav_user_info);
        }
        public void bindMenuHeader() {
            try {
                mName.setText(MainActivity.currentUser.getString("FullName"));
                mInfo.setText(MainActivity.currentUser.getString("Position"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mBg.setColorFilter(Color.rgb(160, 160, 160), android.graphics.PorterDuff.Mode.MULTIPLY);
            try {
                ImageLoader.getInstance().displayImage(MainActivity.currentUser.getString("Photo"), mImage);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
