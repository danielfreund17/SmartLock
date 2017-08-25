package smartlock.code.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import smartlock.code.Classes.JsonReader;
import smartlock.code.Classes.LoggedInUser;
import smartlock.code.Classes.LoginRegisterHttpConfiguration;
import smartlock.code.Classes.SmartLockServer;
import smartlock.code.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private eTaskType mTaskType;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private View mErrorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    try
                    {
                        mTaskType = eTaskType.Login;
                        attemptLogin();
                    }
                    catch(Exception ex)
                    {
                        return  false;
                    }
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener((view) ->
        {
                try
                {
                    mTaskType = eTaskType.Login;
                    attemptLogin();
                }
                catch(Exception ex)
                {

                }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        mEmailView.requestFocus();
    }

    private void populateAutoComplete()
    {
        if (!mayRequestContacts())
        {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS))
        {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        }
        else
        {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_READ_CONTACTS)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() throws ExecutionException, InterruptedException
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        boolean cancel = checkLoginValidationInput(email, password);

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            mErrorView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);//TODO: get rid of the get()- the get() makes it sync instead of async
        }
    }

    private Boolean checkLoginValidationInput(String email, String password)
    {
        boolean cancel = false;
        //focusView.requestFocus();
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mErrorView = mPasswordView;
            cancel = true;
        }

        if(TextUtils.isEmpty(password))
        {
            mPasswordView.setError("This field is required");
            mErrorView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_field_required));
            mErrorView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email))
        {
            mEmailView.setError(getString(R.string.error_invalid_email));
            mErrorView = mEmailView;
            cancel = true;
        }

        return cancel;
    }

    private boolean isEmailValid(String email)
    {
        //TODO: Replace this with your own logic
        return true;
       /// return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() >= 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection)
    {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery
    {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public enum eTaskType {
        Login,Register;
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     * The class is here because if operation success we do redirection
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean>
    {

        private final String mEmail;
        private final String mPassword;
        private URL mUrl;
        private HttpURLConnection mUrlConnection;
        private JSONObject mJsonObj;

        UserLoginTask(String email, String password)
        {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            // TODO: attempt authentication against a network service.
            Boolean ans = null;
            Log.d("in logingAsync", "in loginAsync");
            try
            {
                setConnectionInfo();
                createJsonData();
                Log.d("in LoginCon", "in LoginCon");
                Log.d(mUrl.toString(), mUrl.toString());
                ans = tryLoginOrRegister();
            }
            catch(Exception ex)
            {

            }
            // TODO: register the new account here.
            return ans;//TODO: return the answer from server
        }

        private void createJsonData() throws JSONException
        {
            mJsonObj = new JSONObject();
            mJsonObj.put("password",mPassword.toString());
            mJsonObj.put("username",mEmail.toString());
            mJsonObj.put("usermail", "@");
            mJsonObj.put("userismanager","true");
            String test = mJsonObj.toString();
        }

        private void setConnectionInfo()
        {
            try
            {
                mUrl = new URL(SmartLockServer.Ip + "/smartLock/servlets/login");
                mUrlConnection = LoginRegisterHttpConfiguration.SetUrlConnectionInfo(mUrl, "POST");
                int test = 6;
               //mUrlConnection = (HttpURLConnection) mUrl.openConnection();
               //mUrlConnection.setConnectTimeout(5000);
               //mUrlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
               //mUrlConnection.setDoOutput(true);
               //mUrlConnection.setDoInput(true);
               //mUrlConnection.setRequestMethod("POST");
            }
            catch(Exception ex)
            {
                //TODO: popup- login failure
            }
        }

        private Boolean tryLoginOrRegister()
        {
            try {
               // int status = mUrlConnection.getResponseCode();

                //Write to POST
                //int code = mUrlConnection.getResponseCode();
                OutputStream os = mUrlConnection.getOutputStream();
                os.write(mJsonObj.toString().getBytes("UTF-8"));
                os.close();

                InputStream is = null;
                InputStream s = mUrlConnection.getInputStream();
                is = new BufferedInputStream(s);
                String output = JsonReader.ReadJsonFromHttp(is);
                //Recive answer from GET
               //BufferedReader reader = new BufferedReader(new InputStreamReader(is));
               //StringBuilder total = null;
               //total = new StringBuilder(is.available());
               //String line;
               //while ((line = reader.readLine()) != null) {
               //    total.append(line).append('\n');
               //}

               //String output = total.toString();
                Log.d("in in loginRet", "in in loginRet");
                Log.d("output = " + output, "output = " + output);
                if (output.contains("true")) {
                    Log.d("returning true", "returning true");
                    return true;
                } else {
                    Log.d("returning false", "returning false");
                    return false;
                }
            }
            catch(Exception ex)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;
            showProgress(false);
            handleLoginAnswer(success);
        }


        private void handleLoginAnswer(Boolean success)
        {
            if (success  || success == null)//TODO- CHANGE THE NULL CONDITION, JUST FOR CHECKS
            {
                LoggedInUser.SetLoggedInUser(mEmail);
                LoggedInUser.SetLoggedInPassword(mPassword);
                Toast.makeText(getApplicationContext(), "Redirecting...",
                        Toast.LENGTH_SHORT).show();
                Intent i = new Intent(LoginActivity.this, MainActivity.class);
                finish();
                startActivity(i);
                //finish();
            }
            else if(success == false)
            {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Connection Failure!",
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

