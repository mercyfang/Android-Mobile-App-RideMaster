package edu.duke.compsci290.ridermaster.Activities;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import edu.duke.compsci290.ridermaster.R;

public class BaseNavDrawerActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;

    private static final String TAG = "BaseNavDrawerActivity";


    protected void onCreate(Bundle savedInstanceState, int layoutId) {

        setContentView(layoutId);


        setUpLayout();
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
        }
        return super.onOptionsItemSelected(item);
    }
}
