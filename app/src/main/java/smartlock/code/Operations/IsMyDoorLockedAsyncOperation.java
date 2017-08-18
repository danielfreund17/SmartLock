package smartlock.code.Operations;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import smartlock.code.Activities.ItemIsMyDoorLockedListActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by danie on 30-Apr-17.
 */



public class IsMyDoorLockedAsyncOperation  extends AsyncTask<String, Void, Boolean> {
    private Boolean m_DoorStatus;
    private Exception m_Exception;

    public Boolean GetDoorStatus()
    {
        return m_DoorStatus;
    }

    @Override
    protected Boolean doInBackground(String[] urls) {
        Log.d("in doInBackground", "in doInBackground");
        InputStream is = null;
        URL _url;
        HttpURLConnection urlConnection;
        try {
            _url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) _url.openConnection();
            //urlConnection.setReadTimeout(2*1000);
            urlConnection.setConnectTimeout(2*1000);
            urlConnection.setRequestMethod("GET");
            Log.d("in doInBackgroundCon", "in doInBackgroundCon");
            Log.d(_url.toString(), _url.toString());
        }
        catch(Exception ex)
        {
            Log.d("in doInBackgroundEx", "in doInBackgroundEx");
            Log.d(ex.getMessage(),ex.getMessage());
            this.m_Exception=ex;
            return null;
        }
        try {
            int status = urlConnection.getResponseCode();
            Log.d("the response code is:" + status, "the response code is:" + status);
            Log.d("in doInBackgroundGet", "in doInBackgroundGet");
            InputStream s = urlConnection.getInputStream();
            Log.d("after doInBackgroundGet", "after doInBackgroundGet");
            is = new BufferedInputStream(s);
            Log.d("after doInBackgroundGet", "after doInBackgroundGet");

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = null;
            total = new StringBuilder(is.available());
            String line;
            while ((line = reader.readLine()) != null) {
                total.append(line).append('\n');
            }

            String output = total.toString();
            Log.d("in doInBackgroundRet", "in doutput=oInBackgroundRet");
            Log.d("output = " + output,"output = " + output);
            if(output.contains("true"))
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
         catch (Exception ex) {
             Log.d("in doInBackgroundEx", "in doInBackgroundEx");
             Log.d(ex.getMessage(),ex.getMessage());
             this.m_Exception=ex;
             return null;
        }
        finally {
            Log.d("in doInBackgroundFIn", "in doInBackgroundFin");
            urlConnection.disconnect();
        }
    }

    @Override
    protected void onPostExecute(Boolean response) {
        if(response != null)
        {
            m_DoorStatus = response;
            Log.d("in doInBackgroundAns", "in doInBackgroundAns");
            Log.d("response is:" + response.toString(), "response is:" + response.toString());
        }
    }
}