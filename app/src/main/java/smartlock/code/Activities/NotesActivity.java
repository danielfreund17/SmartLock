package smartlock.code.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import smartlock.code.R;


/**
 * This is the notes activity.
 * this activity is made in order to let the user insert notes, and important things that he wants
 * to make sure he's not forgetting while leaving home
 */
public class NotesActivity extends AppCompatActivity
{
    EditText EditText1;
    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_notes);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_notes);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Save("Note1.txt");
            }
        });

        EditText1 = (EditText) findViewById(R.id.EditText1);
        EditText1.setText(Open("Note1.txt"));
    }
    //endregion

    //region onCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_notes, menu);
        return true;
    }
    //endregion

    //This method is saving the note to the local user phone.
    //region Save Method
    public void Save(String fileName) {
        try {
           //File f = new File(fileName);
           //f.createNewFile(); //will do nothing if exists
            OutputStreamWriter out =
                    new OutputStreamWriter(openFileOutput(fileName, 0));
            out.write(EditText1.getText().toString());
            out.close();
            Toast.makeText(this, "Note saved!", Toast.LENGTH_SHORT).show();
        } catch (Throwable t) {
            Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
        }
    }
    //endregion

    //this mathod opens the local file notes and shows the user the things he has to remember.
    //region Open File Method
    public String Open(String fileName) {
        String content = "";
        if (FileExists(fileName)) {
            try {
                InputStream in = openFileInput(fileName);
                if ( in != null) {
                    InputStreamReader tmp = new InputStreamReader( in );
                    BufferedReader reader = new BufferedReader(tmp);
                    String str;
                    StringBuilder buf = new StringBuilder();
                    while ((str = reader.readLine()) != null) {
                        buf.append(str + "\n");
                    } in .close();
                    content = buf.toString();
                }
            } catch (java.io.FileNotFoundException e) {} catch (Throwable t) {
                Toast.makeText(this, "Exception: " + t.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return content;
    }
    //endregion

    //this method checks if the file exists
    //region File Exists
    public boolean FileExists(String fname) {
        File file = getBaseContext().getFileStreamPath(fname);
        try
        {
            if (file == null || !file.exists())
            {
                return false;
            }
            return true;
        }
        catch(Exception ex)
        {
            return false;
        }
    }
    //endregion

    //region onOptionsItemSelected
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
    //endregion
}


