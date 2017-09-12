package smartlock.code.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import smartlock.code.R;

//This is the Application information activity.
public class AppInformationActivity extends AppCompatActivity
{

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_information);
    }
    //endregion

}
