package ua.kpi.kbis.campus;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import fr.ganfra.materialspinner.MaterialSpinner;
import me.drakeet.materialdialog.MaterialDialog;


public class AddBulletin extends ActionBarActivity implements DatePickerDialog.OnDateSetListener {

    public static final String DATEPICKER_TAG = "datepicker";
    private static final String AB_URL = "http://campus-api.azurewebsites.net/BulletinBoard/CreateBulletin";
    private final Calendar calendarFrom = Calendar.getInstance();
    private final Calendar calendarTo = Calendar.getInstance();
    private LinearLayout mAvailableFor;
    private TextView mFrom;
    private TextView mTo;
    private Toolbar mToolbar;
    private ButtonFlat mAdd;
    private DatePickerDialog datePickerDialogFrom;
    private DatePickerDialog datePickerDialogTo;
    private ua.kpi.kbis.campus.JSONParser jsonParser = new JSONParser();
    private TextView mCaption;
    private TextView mText;
    private AddBulletinTask mAddTask;
    private ButtonFlat mAddForButton;
    private MaterialDialog mMaterialDialog;
    private View mDialogView;
    private ArrayAdapter<String> mAdapterProfile;
    private MaterialSpinner mSpinnerProfile;
    private ArrayList<String> mItemsProfile;
    private ArrayList<String> mItemsFaculty;
    private ArrayAdapter<String> mAdapterFaculty;
    private MaterialSpinner mSpinnerFaculty;
    private ArrayList<String> mItemsGroup;
    private ArrayAdapter<String> mAdapterGroup;
    private MaterialSpinner mSpinnerGroup;
    private boolean selectedProfile = false;
    private boolean selectedFaculty = false;
    private boolean selectedGroup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bulletin);
        mToolbar = (Toolbar) findViewById(R.id.app_bar_add_bulletin);
        setSupportActionBar(mToolbar);
        mAvailableFor = (LinearLayout) findViewById(R.id.add_bulletin_available_for);
        mCaption = (MaterialEditText) findViewById(R.id.add_bulletin_caption);
        mText = (MaterialEditText) findViewById(R.id.add_bulletin_text);
        mAddForButton = (ButtonFlat) findViewById(R.id.action_add_available_for);

        mDialogView = LayoutInflater.from(AddBulletin.this).inflate(R.layout.add_view_dialog, null);

        mItemsFaculty = new ArrayList<String>();
        try {
            for (int i = 0; i < MainActivity.currentUser.getJSONArray("availableForFaculty").length(); i++) {
                mItemsFaculty.add(MainActivity.currentUser.getJSONArray("availableForFaculty").getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapterFaculty = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                mItemsFaculty.toArray(new String[mItemsFaculty.size()]));
        mAdapterFaculty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerFaculty = (MaterialSpinner) mDialogView.findViewById(R.id.spinner_dist);
        mSpinnerFaculty.setAdapter(mAdapterFaculty);
        mSpinnerFaculty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFaculty = (position != -1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedFaculty = false;
            }
        });

        mItemsGroup = new ArrayList<String>();
        try {
            for (int i = 0; i < MainActivity.currentUser.getJSONArray("availableForGroup").length(); i++) {
                mItemsGroup.add(MainActivity.currentUser.getJSONArray("availableForGroup").getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapterGroup = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                mItemsGroup.toArray(new String[mItemsGroup.size()]));
        mAdapterGroup.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerGroup = (MaterialSpinner) mDialogView.findViewById(R.id.spinner_group);
        mSpinnerGroup.setAdapter(mAdapterGroup);
        mSpinnerGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGroup = (position != -1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGroup = false;
            }
        });

        mItemsProfile = new ArrayList<String>();
        try {
            for (int i = 0; i < MainActivity.currentUser.getJSONArray("availableFor").length(); i++) {
                mItemsProfile.add(MainActivity.currentUser.getJSONArray("availableFor").getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mAdapterProfile = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                mItemsProfile.toArray(new String[mItemsProfile.size()]));
        mAdapterProfile.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerProfile = (MaterialSpinner) mDialogView.findViewById(R.id.spinner_facculty);
        mSpinnerProfile.setAdapter(mAdapterProfile);
        mSpinnerProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProfile = (position != -1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedProfile = false;
            }
        });

        mMaterialDialog = new MaterialDialog(AddBulletin.this).
                setView(mDialogView);
        mMaterialDialog.setTitle("Додати одержувача");
        mMaterialDialog.setPositiveButton("Додати", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedProfile && selectedFaculty && selectedGroup) {
                    View view = LayoutInflater.from(AddBulletin.this).inflate(R.layout.bulletin_available_for_row, null);
                    ((TextView) view.findViewById(R.id.add_bulletin_available_for_text))
                            .setText(mSpinnerProfile.getSelectedItem().toString() + " " + mSpinnerFaculty.getSelectedItem().toString() + " " + mSpinnerGroup.getSelectedItem().toString());
                    ImageView removeIcon = (ImageView) view.findViewById(R.id.action_remove_group);
                    removeIcon.setColorFilter(R.color.text_primary);
                    removeIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mAvailableFor.removeView((View) v.getParent().getParent());
                            Log.d("lol", v.toString());
                        }
                    });
                    mAvailableFor.addView(view, mAvailableFor.getChildCount() - 1);
                }
                mMaterialDialog.dismiss();
            }
        });
        mMaterialDialog.setNegativeButton("Відмінити", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaterialDialog.dismiss();
            }
        });
        mAddForButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaterialDialog.show();
            }
        });
        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_item, availableFor.toArray(new String[availableFor.size()]));
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mAvailableFor.setAdapter(adapter);*/

        calendarTo.add(Calendar.MONTH, 1);
        datePickerDialogFrom = DatePickerDialog.newInstance(this, calendarFrom.get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH), calendarFrom.get(Calendar.DAY_OF_MONTH), false);
        mFrom = (TextView) findViewById(R.id.add_bulletin_from);
        mFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialogFrom.setVibrate(false);
                datePickerDialogFrom.setYearRange(1985, 2028);
                datePickerDialogFrom.setCloseOnSingleTapDay(false);
                datePickerDialogFrom.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });
        datePickerDialogTo = DatePickerDialog.newInstance(this, calendarTo.get(Calendar.YEAR), calendarTo.get(Calendar.MONTH), calendarTo.get(Calendar.DAY_OF_MONTH), false);
        mTo = (TextView) findViewById(R.id.add_bulletin_to);
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
        mAdd = (ButtonFlat) findViewById(R.id.action_add);
        mAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddTask = new AddBulletinTask();
                mAddTask.execute((Void) null);
            }
        });
        mCaption.requestFocus();
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

    public class AddBulletinTask extends AsyncTask<Void, Void, Boolean> {
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
                        AB_URL, "POST", reqString);
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
