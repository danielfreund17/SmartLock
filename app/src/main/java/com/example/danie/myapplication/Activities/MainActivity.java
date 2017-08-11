package com.example.danie.myapplication.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
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

import com.example.danie.myapplication.Classes.SmartLockServer;
import com.example.danie.myapplication.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private Boolean m_DoorStatus;
    private Boolean m_IsFirstDoorStatusChecking;
    private Boolean m_IsActivityFinished=  false;
    private HttpURLConnection m_UrlConnection = null;
    private Thread m_DoorStatusThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_IsFirstDoorStatusChecking = true;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
                if(m_IsActivityFinished)
                {
                    break; //kill the thread.
                }
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

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder total = null;
                total = new StringBuilder(is.available());
                String line;
                while ((line = reader.readLine()) != null) {
                    total.append(line).append('\n');
                }

                String output = total.toString();
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
            resultIntent = new Intent(this, ItemIsMyDoorLockedListActivity.class);//TODO: intent to notes if locked
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        int test;
        if (id == R.id.nav_notes) {
           //TODO
            // Handle the camera action
        } else if (id == R.id.nav_is_my_door_locked) {
            Intent i = new Intent(MainActivity.this, ItemIsMyDoorLockedListActivity.class);
            startActivity(i);
            //test=4;
            //TODO
        } else if (id == R.id.nav_details_product) {
            //TODO
        } else if (id == R.id.nav_about) {
            Intent i = new Intent(MainActivity.this, AppInformationActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_product_details) {
            //NOT IN USE- CHANGE IT TO USE OTHER NEED
            //TODO
        } else if (id == R.id.nav_log_off) {
            goBackToLogin();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClick_IsMyDoorLockedButton(View v)
    {
        Toast.makeText(getApplicationContext(), "Loading...", Toast.LENGTH_LONG).show();
        Intent i = new Intent(MainActivity.this, ItemIsMyDoorLockedListActivity.class);
        startActivity(i);
    }

    public void onClick_NotesButton(View v)
    {
        //TODO- activity for notes
    }

    public void onClick_InfoButton(View v)
    {
        Intent i = new Intent(MainActivity.this, AppInformationActivity.class);
        startActivity(i);
    }

    private void goBackToLogin()
    {
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        finish();  //Kill main activity
        startActivity(i);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        m_IsActivityFinished = true;
        m_UrlConnection.disconnect();
    }
}
