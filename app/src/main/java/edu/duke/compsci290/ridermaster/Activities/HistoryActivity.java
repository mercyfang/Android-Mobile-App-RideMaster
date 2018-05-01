package edu.duke.compsci290.ridermaster.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.vision.text.Line;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import FirebaseDatabase.FirebaseDatabaseReaderWriter;
import edu.duke.compsci290.ridermaster.R;


public class HistoryActivity extends AppCompatActivity {


    private DrawerLayout mDrawerLayout;
    private static Context historyActivityContext;
    private static LinearLayout layout;
    private static HistoryActivity historyActivity;


    public static HistoryActivity getInstance() {
        return historyActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        historyActivityContext = getApplicationContext();
        layout = (LinearLayout) findViewById(R.id.request_history_linear_layout);
        historyActivity = this;

        mDrawerLayout = findViewById(R.id.base_navigation_drawer_layout);

        // Sets up toolbar and its actions for navigation drawer.
        Toolbar toolbar = findViewById(R.id.navigation_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);


        //call funtion readerwriter on user id/requests
        FirebaseDatabaseReaderWriter fbReaderWriter =  new FirebaseDatabase.FirebaseDatabaseReaderWriter();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        fbReaderWriter.addRequestHistory(firebaseUser.getUid());

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    // Opens the drawer when button at ToolBar is tapped.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

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
                Log.d("tag", "tapped here");
                startActivity(intent3);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    public void addRequestButton(final String requestId, final String uid, final String date, final String text, final boolean isMatched){
        Button myButton = new Button(historyActivityContext);
        myButton.setText(text);
        if (isMatched){
            myButton.setBackgroundResource(R.drawable.round_button_dark);
        }else {
            myButton.setBackgroundResource(R.drawable.round_button);
        }
        myButton.setContentDescription(requestId + " ; " + uid + " ; " + date + " ; " + text);
        myButton.setTextColor(Color.parseColor("#FFFFFF"));
        myButton.getPaddingTop();
        myButton.getPaddingBottom();
        myButton.getPaddingLeft();
        myButton.getPaddingRight();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(historyActivity, MatchResultActivity.class);
                // TODO: passes objects to another activity using Parcelable or Serializable class.

                intent.putExtra("uid",
                        uid);
                intent.putExtra("requestid",
                        requestId);
                intent.putExtra("date",
                        date);
                intent.putExtra("information", text);
                //need to pass the find matches user in

                startActivity(intent);
            }
        });


        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParam.setMargins(0,30,0,10);

        layout.addView(myButton, layoutParam);
        Log.d("tag", "added new button");
    }




}
