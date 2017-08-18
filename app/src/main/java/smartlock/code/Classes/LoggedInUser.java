package smartlock.code.Classes;

/**
 * Created by danie on 18-Aug-17.
 */

public final class LoggedInUser
{
    private static String m_LoggedInUser;
    private static String m_LoggedInPassword;

    public static void SetLoggedInUser(String user)
    {
        m_LoggedInUser = user;
    }

    public static void SetLoggedInPassword(String password)
    {
        m_LoggedInPassword = password;
    }

    public static String getLoggedInUser()
    {
        return m_LoggedInUser;
    }

    public static String getLoggedInPassword()
    {
        return m_LoggedInPassword;
    }
}
