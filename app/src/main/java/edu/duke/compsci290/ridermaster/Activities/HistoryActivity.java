package edu.duke.compsci290.ridermaster.Activities;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.duke.compsci290.ridermaster.R;





public class HistoryActivity extends AppCompatActivity {


    final public DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users");
    final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        mDrawerLayout = findViewById(R.id.base_navigation_drawer_layout);

        // Sets up toolbar and its actions for navigation drawer.
        Toolbar toolbar = findViewById(R.id.navigation_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);





        DatabaseReference root = FirebaseDatabase.getInstance().getReference();

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

//                String uId = firebaseUser.getUid();
//                if (dataSnapshot.hasChild(uId)) {
//                    FirebaseDatabase.User user = usersRef.child(uId).;//.child("requests");//.child(requestId).setValue(true);
//                } else {
//                    FirebaseDatabase.User user = new FirebaseDatabase.User(firebaseUser);
//                    usersRef.child(uId).setValue(user);
//                    usersRef.child(uId).child("requests").child(requestId)
//                            .setValue(true);
//                }




                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String uid = mAuth.getCurrentUser().getUid();
                String a = (String)dataSnapshot.child("email").getValue();//FirebaseDatabase.User.class);
                showHis(a);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }


        });

    }


    public void showHis(String history){

        TextView test = findViewById(R.id.test_text);
        test.setText(history);
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
                startActivity(intent3);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


}
