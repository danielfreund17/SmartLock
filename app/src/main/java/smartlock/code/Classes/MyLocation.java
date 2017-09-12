package smartlock.code.Classes;

import com.google.gson.annotations.SerializedName;

/**
 * This class saves the user name, and his lat and lon, in order to send the server by json the user's location.
 * than other door users can know exactly who is at home.
 */

public final class MyLocation
{
    @SerializedName("username")
    private  String m_UserName;
    @SerializedName("latitude")
    private  double m_Latitude;
    @SerializedName("longitude")
    private  double m_Longitude;

    public void SetUserName(String username)
    {
        m_UserName = username;
    }

    public String GetUserName()
    {
        return m_UserName;
    }

    public void SetLatitude(double lat)
    {
        m_Latitude = lat;
    }

    public void SetLongtitude(double lot)
    {
        m_Longitude = lot;
    }

    public double GetLat()
    {
        return m_Latitude;
    }

    public double GetLot()
    {
        return m_Longitude;
    }

    public MyLocation()
    {

    }

    public MyLocation(String user, double lat, double lot)
    {
        m_UserName = user;
        m_Latitude = lat;
        m_Longitude = lot;
    }

}
