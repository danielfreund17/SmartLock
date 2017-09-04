package smartlock.code.Activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.widget.Toast;

import smartlock.code.R;


/**
 * An activity representing a single ItemIsMyDoorLocked detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ItemIsMyDoorLockedListActivity}.
 */
public class ItemIsMyDoorLockedDetailActivity extends AppCompatActivity {

    private static String m_DoorMessage;

    public static void SetDoorMessage(String message)
    {
        m_DoorMessage = message;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itemismydoorlocked_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendMessageOnWhatsapp();
            }

            private void sendMessageOnWhatsapp()
            {
                PackageManager pm = getPackageManager();
                try
                {
                    Intent waIntent = new Intent(Intent.ACTION_SEND);
                    waIntent.setType("text/plain");
                    String text = "This message sent by SmartLock App.\n";
                    text += m_DoorMessage;

                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    waIntent.setPackage("com.whatsapp");

                    waIntent.putExtra(Intent.EXTRA_TEXT, text);
                    startActivity(Intent.createChooser(waIntent, "Share with"));

                }
                catch (PackageManager.NameNotFoundException e)
                {
                    Toast.makeText(getApplicationContext(), "WhatsApp not Installed", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null)
        {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ItemIsMyDoorLockedDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(ItemIsMyDoorLockedDetailFragment.ARG_ITEM_ID));
            ItemIsMyDoorLockedDetailFragment fragment = new ItemIsMyDoorLockedDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.itemismydoorlocked_detail_container, fragment)
                    .commit();
            int text=7;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            NavUtils.navigateUpTo(this, new Intent(this, ItemIsMyDoorLockedListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
