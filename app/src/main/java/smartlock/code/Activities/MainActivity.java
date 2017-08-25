package smartlock.code.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.location.LocationListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;

import smartlock.code.Classes.JsonReader;
import smartlock.code.Classes.MyLocation;
import smartlock.code.Classes.SmartLockServer;
import smartlock.code.Classes.UsersLocations;
import smartlock.code.Operations.SendMyLocationTask;
import smartlock.code.R;


import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener
{
    private Boolean m_DoorStatus;
    private Boolean m_LocationDidntSent = true;
    private Boolean m_IsFirstDoorStatusChecking;
    private Boolean m_IsActivityFinished=  false;
    private HttpURLConnection m_UrlConnection = null;
    private Thread m_DoorStatusThread;
    private MyLocation m_MyLocation;
    private SendMyLocationTask m_AuthSendLocTask = null;
    private GetUsersLocationTask m_AuthGetLocTask = null;
    private View m_ProgressView;
    private View m_MainView;
    private UsersLocations m_UsersLocations;
    private GetUsersLocationTask m_GetUserLocationsTask;
    private LocationManager m_LocationManager;

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLocationAndSendFirstTime();
        m_IsFirstDoorStatusChecking = true;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        m_ProgressView = findViewById(R.id.main_progress);
        m_MainView = findViewById(R.id.main_form);
        setLocationManager();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        startDoorCheckAsync();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    //endregion

    //region Ask for locations permissions and set locations manager
    private void setLocationManager()
    {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
                    1 );
        }
        else
        {
            m_LocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, this);
            m_LocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            m_LocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, this);
            m_LocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    private void setLocationAndSendFirstTime()
    {
        m_MyLocation = new MyLocation();
        m_AuthSendLocTask = new SendMyLocationTask(m_MyLocation);
    }
    //endregion

    //region Check Door Async Thread, Push Notifications, Send Locations to server
    private void startDoorCheckAsync()
    {
        m_DoorStatusThread = new Thread(() ->
        {
            InputStream is = null;
            URL _url;
            Boolean currentStatus;
            String url = SmartLockServer.Ip + "/smartLock/servlets/isdoorlocked";
        while(true)
        {
            try
            {
                //if(m_IsActivityFinished)
                //{
                //    break; //kill the thread.
                //}
                _url = new URL(url);
                m_UrlConnection = (HttpURLConnection) _url.openConnection();
                //urlConnection.setReadTimeout(2*1000);
                m_UrlConnection.setConnectTimeout(2 * 1000);
                m_UrlConnection.setRequestMethod("GET");
            }
            catch (Exception ex)
            {
                Log.d("connection failed", "connection failed");
            }
            try
            {
                int status = m_UrlConnection.getResponseCode();
                InputStream s = m_UrlConnection.getInputStream();
                is = new BufferedInputStream(s);
                String output = JsonReader.ReadJsonFromHttp(is);
                //BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                //StringBuilder total = null;
                //total = new StringBuilder(is.available());
                //String line;
                //while ((line = reader.readLine()) != null) {
                //    total.append(line).append('\n');
                //}
                //TODO- delete notes
                //String output = total.toString();
                if (output.contains("true"))
                {
                    currentStatus = true;
                }
                else
                {
                    currentStatus = false;
                }
                if(m_IsFirstDoorStatusChecking)
                {
                    m_DoorStatus = currentStatus;
                    m_IsFirstDoorStatusChecking = false;
                    Log.d("first door check","first door check");
                    continue;
                }
                else
                {
                    if(doorStatusChanged(currentStatus))
                    {
                        if(currentStatus) //the door was opened and just locked
                        {
                            pushNotifyUserDoorStatusChanged("locked");
                        }
                        else
                        {
                            pushNotifyUserDoorStatusChanged("opened");
                        }
                        m_DoorStatus = currentStatus;
                        //TODO- check if location works
                        m_AuthSendLocTask = new SendMyLocationTask(m_MyLocation);
                        m_AuthSendLocTask.execute((Void) null);

                        //TODO - push notification
                        //TODO - notes
                    }
                }
            }
            catch (Exception ex)
            {
                Log.d("connection failed", "connection failed");
            }
            finally
            {
                m_UrlConnection.disconnect();
            }
        }
        });

        m_DoorStatusThread.start();
    }

    private Boolean doorStatusChanged(Boolean currentStatus)
    {
        return currentStatus != m_DoorStatus;
    }

    private void pushNotifyUserDoorStatusChanged(String status)
    {
        String msgToUser;
        Intent resultIntent;
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        int doorStatus = status.compareTo("opened");
        if(doorStatus == 0)
        {
            msgToUser = "Your door just opened";
        }
        else
        {
            msgToUser = "Door Locked, please view your notes.";
        }
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.twitter_door)
                        .setContentTitle(msgToUser)
                        .setSound(alarmSound);
        if(doorStatus == 0) //door opened
        {
            resultIntent = new Intent(this, ItemIsMyDoorLockedListActivity.class);//TODO: intent to notes if locked
        }
        else
        {
            resultIntent = new Intent(this, NotesActivity.class);//TODO: intent to notes if locked
            //TODO: Intent to notes.
        }
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ItemIsMyDoorLockedListActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0 , mBuilder.build());
    }
    //endregion

    //region Go Back, On Destroy, goBackTOLogin methods
    private void goBackToLogin()
    {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        finish();  //Kill main activity
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        m_IsActivityFinished = true;
        m_UrlConnection.disconnect();
    }
    //endregion

    //region Location Changed Events
    @Override
    public void onLocationChanged(Location location)
    {
        m_MyLocation.SetLatitude(location.getLatitude());
        m_MyLocation.SetLongtitude(location.getLongitude());
        if(m_LocationDidntSent)
        {
            m_AuthSendLocTask.execute((Void) null);
            m_LocationDidntSent = false;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }
    //endregion

    //region Navigation Selected Changed
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent i;
        int test;
        switch (id)
        {
            case R.id.nav_notes:
                i = new Intent(MainActivity.this, NotesActivity.class);
                startActivity(i);
                break;
            case R.id.nav_is_my_door_locked:
                i = new Intent(MainActivity.this, ItemIsMyDoorLockedListActivity.class);
                startActivity(i);
                break;
            case R.id.nav_details_product:
                //TODO;
                break;
            case R.id.nav_about:
                i = new Intent(MainActivity.this, AppInformationActivity.class);
                startActivity(i);
                break;
            case R.id.register_new_user:
                i = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(i);
                break;
            case R.id.nav_log_off:
                goBackToLogin();
                break;
            case R.id.nav_location:
                showProgress(true);
                m_AuthGetLocTask = new GetUsersLocationTask();
                try
                {
                    m_AuthGetLocTask.execute((Void) null); //TODO- get rid of the get
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
                //TODO
                //start task to get locations
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //endregion

    //region Get Users Location Task
    public class GetUsersLocationTask extends AsyncTask<Void, Void, Boolean>
    {
        private URL mUrl;
        private HttpURLConnection mUrlConnection;
        private String m_Json;

        public GetUsersLocationTask()
        {

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
                //TODO: THIS IS TEST
                Log.d("in registerCon", "in registerCon");
                Log.d(mUrl.toString(), mUrl.toString());
                ans = tryGetLoc();
            }
            catch(Exception ex)
            {

            }
            // TODO: register the new account here.
            return ans;//TODO: return the answer from server
        }

        private void createJsonData() throws JSONException
        {
            ArrayList<MyLocation> usersLoc = new ArrayList<MyLocation>();
            usersLoc.add(new MyLocation("daniel",m_MyLocation.GetLat(),m_MyLocation.GetLot()));
            usersLoc.add(new MyLocation("adi",31.995758,34.948840));
            usersLoc.add(new MyLocation("ben",32.051814,34.761493));
            Type listType = new TypeToken<ArrayList<MyLocation>>() {}.getType();
            m_Json = new Gson().toJson(usersLoc, listType);
        }

        private void setConnectionInfo()
        {
            try
            {
                mUrl = new URL(SmartLockServer.Ip + "/smartLock/servlets/getuserslocation");
                mUrlConnection = (HttpURLConnection) mUrl.openConnection();
                //urlConnection.setReadTimeout(2*1000);
                mUrlConnection .setConnectTimeout(2*1000);
                mUrlConnection .setRequestMethod("GET");
            }
            catch(Exception ex)
            {
                //TODO: popup- login failure
            }
        }

        private Boolean tryGetLoc()
        {
            try {
                //TODO- THIS IS JUST TEST
                //createJsonData();
               //Type listType = new TypeToken<ArrayList<MyLocation>>() {}.getType();
               //ArrayList<MyLocation> usersLoc = new Gson().fromJson(m_Json, listType);
               // UsersLocations.m_UsersLocations = usersLoc;

                int status = mUrlConnection.getResponseCode();
                InputStream s = mUrlConnection.getInputStream();
                InputStream is = null;
                is = new BufferedInputStream(s);
                String output = JsonReader.ReadJsonFromHttp(is);
                Type listType = new TypeToken<ArrayList<MyLocation>>() {}.getType();
                ArrayList<MyLocation> usersLoc = new Gson().fromJson(output, listType);
                UsersLocations.m_UsersLocations = usersLoc;

                //TODO: finish task
                //just for tests -----


                return true;
                //TODO- deserialize list of locations and set it in UsersLocations class
            }
            catch(Exception ex)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            showProgress(false);
            Intent i;
            i = new Intent(MainActivity.this, WhoIsAtHomeActivity.class);
            startActivity(i);
            //TODO
        }
    }
    //endregion

    //region OnClick Methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClick_IsMyDoorLockedButton(View v)
    {
        Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_LONG).show();
        Intent i = new Intent(MainActivity.this, ItemIsMyDoorLockedListActivity.class);
        startActivity(i);
    }

    public void onClick_NotesButton(View v)
    {
        Intent i = new Intent(MainActivity.this, NotesActivity.class);
        startActivity(i);
        //TODO- activity for notes
    }

    public void onClick_InfoButton(View v)
    {
        Intent i = new Intent(MainActivity.this, AppInformationActivity.class);
        startActivity(i);
    }
    //endregion

    //region ShowProgressBar
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            m_MainView.setVisibility(show ? View.GONE : View.VISIBLE);
            m_MainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_MainView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            m_ProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            m_ProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    m_ProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            m_ProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            m_ProgressView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    //endregion
}
