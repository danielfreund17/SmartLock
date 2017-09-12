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
 * This is the async task for getting the information from the server if the door is locked.
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
        InputStream is = null;
        URL _url;
        HttpURLConnection urlConnection;
        try {
            _url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) _url.openConnection();
            urlConnection.setConnectTimeout(2*1000);
            urlConnection.setRequestMethod("GET");
        }
        catch(Exception ex)
        {
            this.m_Exception=ex;
            return null;
        }
        try
        {
            int status = urlConnection.getResponseCode();
            InputStream s = urlConnection.getInputStream();
            is = new BufferedInputStream(s);

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = null;
            total = new StringBuilder(is.available());
            String line;
            while ((line = reader.readLine()) != null) {
                total.append(line).append('\n');
            }

            String output = total.toString();
            if(output.contains("true"))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
         catch (Exception ex)
         {
             this.m_Exception=ex;
             return null;
        }
        finally
        {
            urlConnection.disconnect();
        }
    }

    @Override
    protected void onPostExecute(Boolean response) {
        if(response != null)
        {
            m_DoorStatus = response;
        }
    }
}