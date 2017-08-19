package smartlock.code.Classes;

/**
 * Created by danie on 18-Aug-17.
 */

public final class MyLocation
{
    private  String m_UserName;
    private  double m_Latitude;
    private  double m_Longtitude;

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
        m_Longtitude = lot;
    }

    public double GetLat()
    {
        return m_Latitude;
    }

    public double GetLot()
    {
        return m_Longtitude;
    }

    public MyLocation()
    {

    }

    public MyLocation(String user, double lat, double lot)
    {
        m_UserName = user;
        m_Latitude = lat;
        m_Longtitude = lot;
    }

}
