package smartlock.code.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import java.util.concurrent.ExecutionException;

public class RegisterActivity extends AppCompatActivity
{

    private RegisterActivity.UserRegisterTask mAuthTask = null;

    // UI references.
    private JSONObject m_RegisterJson;
    private AutoCompleteTextView m_EmailView;
    private EditText m_PasswordView;
    private EditText m_UserView;
    private CheckBox m_IsManager;
    private View m_ProgressView;
    private View m_RegisterFormView;
    private View m_ErrorView;
    private Button m_RegisterButton;

    public RegisterActivity()
    {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setButtons();
    }

    private void setButtons()
    {
        m_EmailView = (AutoCompleteTextView) findViewById(R.id.register_email);
        m_PasswordView = (EditText) findViewById(R.id.register_password);
        m_UserView = (EditText) findViewById(R.id.register_user_name);
        m_IsManager = (CheckBox)findViewById(R.id.register_is_manager);
        m_RegisterFormView = findViewById(R.id.register_form);
        m_ProgressView = findViewById(R.id.register_progress);
        Button mEmailSignInButton = (Button) findViewById(R.id.register_button);
        mEmailSignInButton.setOnClickListener((view) ->
        {
            try
            {
                attemptRegister();
            }
            catch(Exception ex)
            {

            }
        });
        m_EmailView.requestFocus();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            m_RegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            m_RegisterFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_RegisterFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            m_ProgressView .setVisibility(show ? View.VISIBLE : View.GONE);
            m_ProgressView .animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_ProgressView .setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            m_ProgressView .setVisibility(show ? View.VISIBLE : View.GONE);
            m_ProgressView .setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void attemptRegister() throws JSONException, ExecutionException, InterruptedException
    {
        Boolean cancel = checkLoginValidationInput();
        if (cancel)
        {
            m_ErrorView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new RegisterActivity.UserRegisterTask();
            showProgress(true);
            mAuthTask.execute((Void) null);//TODO: get rid of the get()- the get() makes it sync instead of async
        }

    }

    private Boolean checkLoginValidationInput()
    {
        boolean cancel = false;
        String password = m_PasswordView.getText().toString();
        String email = m_EmailView.getText().toString();
        String userName = m_UserView.getText().toString();
        //focusView.requestFocus();
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            m_PasswordView.setError(getString(R.string.error_invalid_password));
            m_ErrorView = m_PasswordView;
            cancel = true;
        }

        if(TextUtils.isEmpty(password))
        {
            m_PasswordView.setError("This field is required");
            m_ErrorView = m_PasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            m_EmailView.setError(getString(R.string.error_field_required));
            m_ErrorView = m_EmailView;
            cancel = true;
        } else if (!isEmailValid(email))
        {
            m_EmailView.setError(getString(R.string.error_invalid_email));
            m_ErrorView = m_EmailView;
            cancel = true;
        }
        if (TextUtils.isEmpty(userName))
        {
            m_UserView.setError(getString(R.string.error_field_required));
            m_ErrorView =  m_UserView;
            cancel = true;
        }

        return cancel;
    }

    private boolean isEmailValid(String email)
    {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        return password.length() >= 4;
    }


    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean>
    {
        private URL mUrl;
        private HttpURLConnection mUrlConnection;
        private JSONObject mJsonObj;

        @Override
        protected Boolean doInBackground(Void... params)
        {
            // TODO: attempt authentication against a network service.
            Boolean ans = null;
            Log.d("in registerAsync", "in registerAsync");
            try
            {
                setConnectionInfo();
                createJsonData();
                Log.d("in registerCon", "in registerCon");
                Log.d(mUrl.toString(), mUrl.toString());
                ans = tryRegister();
            }
            catch(Exception ex)
            {

            }
            // TODO: register the new account here.
            return ans;//TODO: return the answer from server
        }

        private void createJsonData() throws JSONException
        {
            String loggedInUser = LoggedInUser.getLoggedInUser();
            String loggedInUserPassword = LoggedInUser.getLoggedInPassword();
            m_RegisterJson =  new JSONObject();
            m_RegisterJson.put("managername", loggedInUser);
            m_RegisterJson.put("managerpassword", loggedInUserPassword);
            m_RegisterJson.put("usermailtoadd",m_EmailView.getText().toString());
            m_RegisterJson.put("usernametoadd",m_UserView.getText().toString());
            m_RegisterJson.put("userismanagertoadd",m_IsManager.isChecked());
        }

        private void setConnectionInfo()
        {
            try
            {
                mUrl = new URL(SmartLockServer.Ip + "/smartLock/servlets/register");
                mUrlConnection = LoginRegisterHttpConfiguration.SetUrlConnectionInfo(mUrl, "POST");
            }
            catch(Exception ex)
            {
                //TODO: popup- login failure
            }
        }

        private Boolean tryRegister()
        {
            try {
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
                Log.d("in in registerRet", "in in registerRet");
                Log.d("output = " + output, "output = " + output);
                if (output.contains("true"))
                {
                    Log.d("returning true", "returning true");
                    return true;
                }
                else
                {
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
            handleRegisterAnswer(success);
        }

        private void handleRegisterAnswer(Boolean success)
        {
            if (success == null || (success))//TODO- CHANGE THE NULL CONDITION, JUST FOR CHECKS
            {
                Toast.makeText(getApplicationContext(), "Registretion Succeeded...",
                        Toast.LENGTH_SHORT).show();
                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                finish();
                startActivity(i);
            }
            else if(success == false)
            {
                m_EmailView.setError("Email adress already exists");
                m_EmailView.requestFocus();
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
