package ua.kpi.kbis.campus;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.gc.materialdesign.views.ButtonFlat;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class EditBulletin extends ActionBarActivity implements DatePickerDialog.OnDateSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    private static final String SB_URL = "http://campus-api.azurewebsites.net/BulletinBoard/Update";
    private final Calendar calendarFrom = Calendar.getInstance();
    private final Calendar calendarTo = Calendar.getInstance();
    private TextView mAvailableFor;
    private TextView mFrom;
    private TextView mTo;
    private Toolbar mToolbar;
    private ButtonFlat mSave;
    private DatePickerDialog datePickerDialogFrom;
    private DatePickerDialog datePickerDialogTo;
    private ua.kpi.kbis.campus.JSONParser jsonParser = new JSONParser();
    private MaterialEditText mCaption;
    private MaterialEditText mText;
    private SaveBulletinTask mSaveTask;
    private Bundle mExtras;
    private JSONObject mItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bulletin);
        mToolbar = (Toolbar) findViewById(R.id.app_bar_edit_bulletin);
        setSupportActionBar(mToolbar);
        mAvailableFor = (TextView) findViewById(R.id.edit_bulletin_available_for);
        mCaption = (MaterialEditText) findViewById(R.id.edit_bulletin_caption);
        mText = (MaterialEditText) findViewById(R.id.edit_bulletin_text);
        mExtras = getIntent().getExtras();
        try {
            mItem = new JSONObject(mExtras.getString("item"));
            mCaption.setText(mItem.getString("Subject"));
            mText.setText(mItem.getString("Text"));
            mCaption.setSelection(mCaption.getText().length());
            calendarFrom.set(Integer.parseInt(mItem.getString("StartDate").substring(0, 4)), Integer.parseInt(mItem.getString("StartDate").substring(5, 7)), Integer.parseInt(mItem.getString("StartDate").substring(8, 10)));
            calendarTo.set(Integer.parseInt(mItem.getString("EndDate").substring(0, 4)), Integer.parseInt(mItem.getString("EndDate").substring(5, 7)), Integer.parseInt(mItem.getString("EndDate").substring(8, 10)));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, availableFor.toArray(new String[availableFor.size()]));
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mAvailableFor.setAdapter(adapter);*/
        datePickerDialogFrom = DatePickerDialog.newInstance(this,
                calendarFrom.get(Calendar.YEAR) >= 1985 ? calendarFrom.get(Calendar.YEAR) : 1985,
                calendarFrom.get(Calendar.MONTH), calendarFrom.get(Calendar.DAY_OF_MONTH), false);
        mFrom = (TextView) findViewById(R.id.edit_bulletin_from);
        mFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialogFrom.setVibrate(false);
                datePickerDialogFrom.setYearRange(1985, 2028);
                datePickerDialogFrom.setCloseOnSingleTapDay(false);
                datePickerDialogFrom.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });
        datePickerDialogTo = DatePickerDialog.newInstance(this,
                calendarTo.get(Calendar.YEAR) >= 1985 ? calendarTo.get(Calendar.YEAR) : 1985,
                calendarTo.get(Calendar.MONTH), calendarTo.get(Calendar.DAY_OF_MONTH), false);
        mTo = (TextView) findViewById(R.id.edit_bulletin_to);
        mTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialogTo.setVibrate(false);
                datePickerDialogTo.setYearRange(1985, 2028);
                datePickerDialogTo.setCloseOnSingleTapDay(false);
                datePickerDialogTo.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });
        onDateSet(datePickerDialogFrom, calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH), calendarFrom.get(Calendar.DAY_OF_MONTH));
        onDateSet(datePickerDialogTo, calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH), calendarTo.get(Calendar.DAY_OF_MONTH));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        //MainActivity.mToolbar.setTitle("Нове оголошення");
        mSave = (ButtonFlat) findViewById(R.id.action_save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSaveTask = new SaveBulletinTask();
                mSaveTask.execute((Void) null);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_add_bulletin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
        if (datePickerDialog == datePickerDialogFrom) {
            calendarFrom.set(year, month, day);
            mFrom.setText(day + " " + String.format(Locale.getDefault(), "%tB", calendarFrom) + " " + year);
        }
        if (datePickerDialog == datePickerDialogTo) {
            calendarTo.set(year, month, day);
            mTo.setText(day + " " + String.format(Locale.getDefault(), "%tB", calendarTo) + " " + year);
        }
    }

    public class SaveBulletinTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                List<NameValuePair> reqString = new ArrayList<NameValuePair>();
                /*Calendar calendarNow = Calendar.getInstance();
                reqString.add(new BasicNameValuePair("creatorId", MainActivity.currentUser.getString("UserAccountId")));
                reqString.add(new BasicNameValuePair("creatorName", MainActivity.currentUser.getString("FullName")));
                reqString.add(new BasicNameValuePair("creationDate",String.format("%04d-%02d-%02dT00:00:00", calendarNow.get(Calendar.YEAR), calendarNow.get(Calendar.MONTH)+1, calendarNow.get(Calendar.DAY_OF_MONTH))));
                reqString.add(new BasicNameValuePair("startDate",String.format("%04d-%02d-%02dT00:00:00", calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH)+1, calendarFrom.get(Calendar.DAY_OF_MONTH))));
                reqString.add(new BasicNameValuePair("endDate",String.format("%04d-%02d-%02dT00:00:00", calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH)+1, calendarTo.get(Calendar.DAY_OF_MONTH))));
                reqString.add(new BasicNameValuePair("subject", String.valueOf(mCaption.getText())));
                reqString.add(new BasicNameValuePair("text", String.valueOf(mText.getText())));
                reqString.add(new BasicNameValuePair("link", null));*/
                reqString.add(new BasicNameValuePair("sessionId", MainActivity.sessionId));
                reqString.add(new BasicNameValuePair("bulletinBoardId", mItem.getString("BulletinId")));
                reqString.add(new BasicNameValuePair("bulletinSubjet", String.valueOf(mCaption.getText())));
                reqString.add(new BasicNameValuePair("dateStop", String.format("%04d-%02d-%02dT00:00:00", calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH) + 1, calendarTo.get(Calendar.DAY_OF_MONTH))));
                reqString.add(new BasicNameValuePair("dateStart", String.format("%04d-%02d-%02dT00:00:00", calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH) + 1, calendarFrom.get(Calendar.DAY_OF_MONTH))));
                reqString.add(new BasicNameValuePair("text", String.valueOf(mText.getText())));
                reqString.add(new BasicNameValuePair("dcProfileId", "2"));
                reqString.add(new BasicNameValuePair("rtProfilePermissionId", "1"));
                for (NameValuePair pair : reqString) {
                    Log.d("lolo", pair.getName() + " " + pair.getValue());
                }
                JSONObject json = jsonParser.makeHttpRequest(
                        SB_URL, "GET", reqString);
                Log.d("lolo", json.toString());
                if (json.getInt("StatusCode") != 200) return false;
                return true;
            } catch (Exception e) {
                Log.v("lolo", e.toString());
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            if (success) {
                finish();
            }
        }

        @Override
        protected void onCancelled() {
        }
    }
}
