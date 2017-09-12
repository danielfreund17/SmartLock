package smartlock.code.Classes;

import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

/**
 * This class used in order to save the configurations of the login and registrations requests,
 * in order to prevent code reuse.
 */

public final class LoginRegisterHttpConfiguration
{
    public static HttpURLConnection SetUrlConnectionInfo(URL url, String postOrGet) throws IOException
    {
        HttpURLConnection urlConnection;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection .setConnectTimeout(5000);
        urlConnection .setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlConnection .setDoOutput(true);
        urlConnection .setDoInput(true);
        urlConnection .setRequestMethod(postOrGet);

        return urlConnection;
    }
}
