package smartlock.code.Operations;

/**
 * Created by danie on 18-Aug-17.
 */

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import smartlock.code.Classes.JsonReader;
import smartlock.code.Classes.LoggedInUser;
import smartlock.code.Classes.LoginRegisterHttpConfiguration;
import smartlock.code.Classes.MyLocation;
import smartlock.code.Classes.SmartLockServer;

public class SendMyLocationTask extends AsyncTask<Void, Void, Boolean>
{
    private URL mUrl;
    private HttpURLConnection mUrlConnection;
    private JSONObject mJsonObj;
    private MyLocation m_MyLocation;

    public SendMyLocationTask(MyLocation location)
    {
        mJsonObj = new JSONObject();
        m_MyLocation = location;
    }

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
            ans = trySendLoc();
        }
        catch(Exception ex)
        {

        }
        // TODO: register the new account here.
        return ans;//TODO: return the answer from server
    }

    private void createJsonData() throws JSONException
    {
        mJsonObj.put("username", LoggedInUser.getLoggedInUser());
        mJsonObj.put("latitude", m_MyLocation.GetLat());
        mJsonObj.put("longitude", m_MyLocation.GetLot());
    }

    private void setConnectionInfo()
    {
        try
        {
            mUrl = new URL(SmartLockServer.Ip + "/smartLock/servlets/setmylocation");
            mUrlConnection = LoginRegisterHttpConfiguration.SetUrlConnectionInfo(mUrl, "POST");
        }
        catch(Exception ex)
        {
            //TODO: popup- login failure
        }
    }

    private Boolean trySendLoc()
    {
        try {
            OutputStream os = mUrlConnection.getOutputStream();
            os.write(mJsonObj.toString().getBytes("UTF-8"));
            os.close();

            InputStream is = null;
            InputStream s = mUrlConnection.getInputStream();
            is = new BufferedInputStream(s);
            String output = JsonReader.ReadJsonFromHttp(is);

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
        //TODO
    }
}