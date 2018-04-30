package edu.duke.compsci290.ridermaster.Activities;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import edu.duke.compsci290.ridermaster.R;

public class BaseNavDrawerActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;


    protected void onCreate(Bundle savedInstanceState, int layoutId) {
        setContentView(layoutId);


        mDrawerLayout = findViewById(R.id.base_navigation_drawer_layout);

        // Sets up toolbar and its actions for navigation drawer.
        Toolbar toolbar = findViewById(R.id.navigation_toolbar);
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
