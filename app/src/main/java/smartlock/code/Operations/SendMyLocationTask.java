package smartlock.code.Operations;

/**
 * This class sends the server the current logged in user's locations.
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
        Boolean ans = null;
        try
        {
            setConnectionInfo();
            createJsonData();
            ans = trySendLoc();
        }
        catch(Exception ex)
        {

        }

        return ans;
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
            //do nothing- the location will be sent in the next task
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

            if (output.contains("true"))
            {
                return true;
            }
            else
            {
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
        //do nothing.
    }
}