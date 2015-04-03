package ua.kpi.kbis.campus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFloatSmall;
import com.melnykov.fab.FloatingActionButton;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


public class ViewBulletin extends ActionBarActivity {

    private Toolbar mToolbar;
    public static TextView mTitle;
    private Bundle mExtras;
    private JSONObject mItem;
    private TextView mText;
    private TextView mFrom;
    private TextView mTo;
    private FloatingActionButton mEditButton;
    private CircleImageView mAuthorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bulletin);
        mToolbar = (Toolbar) findViewById(R.id.app_bar_view_bulletin);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        mTitle = (TextView)findViewById(R.id.view_bulletin_title);
        mText = (TextView) findViewById(R.id.view_bulletin_text);
        mFrom = (TextView) findViewById(R.id.view_bulletin_from);
        mTo = (TextView) findViewById(R.id.view_bulletin_to);
        mEditButton = (FloatingActionButton) findViewById(R.id.view_bulletin_edit_button);
        mAuthorImage = (CircleImageView) findViewById(R.id.view_bulletin_author);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mEditButton.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        params.setMargins(24, displaymetrics.heightPixels / 3 - 40, 8, 0);
        mEditButton.setLayoutParams(params);
        ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF,0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(500);
        anim.setStartOffset(200);
        mEditButton.setVisibility(View.VISIBLE);
        mEditButton.startAnimation(anim);
        mExtras = getIntent().getExtras();
        try {
            mItem = new JSONObject(mExtras.getString("item"));
            mEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getBaseContext(), EditBulletin.class).putExtra("item", mItem.toString()));
                }
            });
            ImageLoader.getInstance().displayImage("http://campus-api.azurewebsites.net/Storage/GetUserProfileImage?userId="+mItem.getString("CreatorId"), mAuthorImage);
            mTitle.setText(mItem.getString("Subject"));
            mText.setText(mItem.getString("Text"));
            mFrom.setText(mItem.getString("StartDate").substring(0,10));
            mTo.setText(mItem.getString("EndDate").substring(0,10));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_view_bulletin, menu);
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
}
