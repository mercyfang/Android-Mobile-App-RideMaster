package edu.duke.compsci290.ridermaster.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;

import edu.duke.compsci290.ridermaster.R;

public class BaseNavDrawerActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    private static final String TAG = "BaseNavDrawerActivity";


    protected void onCreate(Bundle savedInstanceState, int layoutId) {

        setContentView(layoutId);


        setUpLayout();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_drawer, menu);
        return true;
    }
    protected void onCreateFrameLayout(Bundle savedInstanceState, int layoutId) {

        FrameLayout activityContainer = findViewById(R.id.activity_content);
        View.inflate(this, R.layout.activity_match_result, activityContainer);


        setUpLayout();
    }

    public void setUpLayout(){
        mDrawerLayout = findViewById(R.id.base_navigation_drawer_layout);

        // Sets up toolbar and its actions for navigation drawer.
        Toolbar toolbar = findViewById(R.id.navigation_toolbar);
        Log.d(TAG, "setUpLayout: " + toolbar.toString());
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }



    // Opens the drawer when button at ToolBar is tapped.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.nav_find_ride:
                Intent intent1 = new Intent(this, RideRequestActivity.class);
                startActivity(intent1);
                return true;


            case R.id.nav_ride_history:
                //mDrawerLayout.openDrawer(GravityCompat.START);
                Intent intent2 = new Intent(this, HistoryActivity.class);
                startActivity(intent2);
                return true;

            case R.id.nav_sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Intent intent3 = new Intent(this,MainActivity.class);
                startActivity(intent3);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}
