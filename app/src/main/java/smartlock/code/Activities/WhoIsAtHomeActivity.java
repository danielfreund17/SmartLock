package smartlock.code.Activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import smartlock.code.Classes.MyLocation;
import smartlock.code.Classes.UsersLocations;
import smartlock.code.R;

/**
 * This activity shows the user who is actually at home.
 * the activity requests from the server all the locations of the users that register to the specific door.
 * after reciving all the locations (user name, alt, lot),
 * than the activity sets all the location in the map, using google maps api.
 * the user sees all the door's users locations in different colors, and can see exactly where everyone is.
 */
public class WhoIsAtHomeActivity extends FragmentActivity implements OnMapReadyCallback
{

    private GoogleMap mMap;
    private LatLng TheAcademicCollege;
    private float[] m_Colors;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        TheAcademicCollege = new LatLng(32.047728, 34.760965);
        m_Colors = new float[10];
        setColors();

        setContentView(R.layout.activity_who_is_at_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    //Set different color for each user
    private void setColors()
    {
        m_Colors[0] = 0.0f;
        m_Colors[1] = 30.0f;
        m_Colors[2] = 60.0f;
        m_Colors[3] = 120.0f;
        m_Colors[4] = 180.0f;
        m_Colors[5] = 210.0f;
        m_Colors[6] = 240.0f;
        m_Colors[7] = 270.0f;
        m_Colors[8] = 300.0f;
        m_Colors[9] = 330.0f;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        int color = 10;
        int i = 1;
        mMap = googleMap;
        ArrayList<MyLocation> usersLoc = UsersLocations.m_UsersLocations;
        for(MyLocation loc : usersLoc)
        {
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(loc.GetLat(),loc.GetLot()))
                    .title(loc.GetUserName())
                    .icon(BitmapDescriptorFactory.defaultMarker(m_Colors[i % color])));
            i++;
        }

        // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(TheAcademicCollege).title("Home"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(TheAcademicCollege));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TheAcademicCollege, 12.0f));
    }
}
