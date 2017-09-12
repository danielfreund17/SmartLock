package smartlock.code.Classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This is a generic class to handle the jsons reading from the server.
 */

public final class JsonReader
{
    public static String ReadJsonFromHttp(InputStream is) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = null;
        total = new StringBuilder(is.available());
        String line;
        while ((line = reader.readLine()) != null) {
            total.append(line).append('\n');
        }
        total.deleteCharAt(total.length()-1);

        String output = total.toString();
        return output;
    }
}
