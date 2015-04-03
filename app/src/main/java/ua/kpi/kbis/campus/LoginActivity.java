package ua.kpi.kbis.campus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonFlat;
import com.rengwuxian.materialedittext.MaterialAutoCompleteTextView;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A login screen that offers login via login/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {
    public static final String LOGIN_URL = "http://campus-api.azurewebsites.net/User/Auth";
    public static final String CURRENT_USER_URL = "http://campus-api.azurewebsites.net/User/GetCurrentUser";
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "test:test", "bar@example.com:world"
    };
    JSONParser jsonParser = new JSONParser();
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private MaterialAutoCompleteTextView mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MainActivity.prefs = new ObscuredSharedPreferences(
                this, this.getSharedPreferences("LOCAL_DATA", Context.MODE_PRIVATE));

        // Set up the login form.
        mLoginView = (MaterialAutoCompleteTextView) findViewById(R.id.login);
        populateAutoComplete();

        mPasswordView = (MaterialEditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                Log.v("lol", id + "");
                if (id == 6 || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        ButtonFlat mLoginSignInButton = (ButtonFlat) findViewById(R.id.login_sign_in_button);
        mLoginSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        if ((MainActivity.prefs.getString("login", null) != null) && (MainActivity.prefs.getString("password", null) != null))
            if ((!MainActivity.prefs.getString("login", null).isEmpty()) && (!MainActivity.prefs.getString("password", null).isEmpty())) {

                Log.v("lol", MainActivity.prefs.getString("login", null) + MainActivity.prefs.getString("password", null));
                showProgress(true);
            /*mAuthTask = new UserLoginTask(MainActivity.prefs.getString("login", null), MainActivity.prefs.getString("password", null));
            mAuthTask.execute((Void) null);*/
                mAuthTask = new UserLoginTask(MainActivity.prefs.getString("login", null), MainActivity.prefs.getString("password", null));
                mAuthTask.onPostExecute(true);
            }
        }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid login, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid login address.
        if (TextUtils.isEmpty(login)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (!isLoginValid(login)) {
            mLoginView.setError(getString(R.string.error_invalid_login));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(login, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isLoginValid(String login) {
        //TODO: Replace this with your own logic
        return login.length() > 0;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 0;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        if (this.getCurrentFocus() != null)
            ((InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only login addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Nickname
                .CONTENT_ITEM_TYPE},

                // Show primary login first. Note that there won't be
                // a primary login if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> logins = new ArrayList<String>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            logins.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addLoginsToAutoComplete(logins);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addLoginsToAutoComplete(List<String> loginAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, loginAddressCollection);

        mLoginView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Nickname.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mLogin;
        private final String mPassword;

        UserLoginTask(String login, String password) {
            mLogin = login;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                List<NameValuePair> reqString = new ArrayList<NameValuePair>();
                reqString.add(new BasicNameValuePair("login", mLogin));
                reqString.add(new BasicNameValuePair("password", mPassword));

                Log.d("lol", "starting");
                // getting product details by making HTTP request
                JSONObject json = jsonParser.makeHttpRequest(
                        LOGIN_URL, "GET", reqString);
                // check your log for json response
                Log.d("lol", json.toString());
                if (json.getInt("StatusCode") == 200) {
                    MainActivity.sessionId = json.getString("Data");
                    reqString.clear();
                    reqString.add(new BasicNameValuePair("sessionId", MainActivity.sessionId));
                    JSONObject currentUser = jsonParser.makeHttpRequest(
                            CURRENT_USER_URL, "GET", reqString).getJSONObject("Data");
                    MainActivity.prefs.edit().putString("login", mLogin).commit();
                    MainActivity.prefs.edit().putString("password", mPassword).commit();
                    if (MainActivity.prefs.getString(mLogin, null) == null ) {
                        MainActivity.prefs.edit().putString(mLogin, currentUser.toString()).commit();
                        Log.i("lollogin", currentUser + " " + MainActivity.currentUser);
                    }
                    return true;
                }
            } catch (Exception e) {
                Log.d("lol", e.toString());
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mLogin)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            if (success) {
                finish();
                LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class));
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
            showProgress(false);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}



