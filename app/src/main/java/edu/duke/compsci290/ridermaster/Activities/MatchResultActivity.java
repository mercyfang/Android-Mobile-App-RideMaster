package edu.duke.compsci290.ridermaster.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import FirebaseDatabase.FirebaseDatabaseReaderWriter;
import Utilities.Toaster;
import Utilities.UtilityFunctions;
import edu.duke.compsci290.ridermaster.R;

public class MatchResultActivity extends BaseNavDrawerActivity {

    ArrayList<LatLng> pickUpPointsLatLng = new ArrayList<>();

    private ConstraintLayout mBackButton;
    private ConstraintLayout mNewMatchButton;
    private ConstraintLayout mNewTripButton;

    private String uid;
    private String date;
    private String requestId;

    private static TextView mStatusText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        populatePickUpPoints();
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState, R.layout.activity_match_result);

        // Sets activity main view.
//        FrameLayout activityContainer = findViewById(R.id.activity_content);
//        View.inflate(this, R.layout.activity_match_result, activityContainer);



        //if found match put in the TextView
        //retrieve intent
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            uid = extras.getString("uid");
            date = extras.getString("date");
            requestId = extras.getString("requestid");
        }else{
            Toast toast =  Toast.makeText(getApplicationContext(), "Error in getting this request basic, request again!", Toast.LENGTH_SHORT);
            toast.show();
        }


        mStatusText = findViewById(R.id.match_status_text_view);

        //TODO: MAKE ONLICK FOR TEXTVIEW so it copies email to clickboard

/*
        //set my new match button for the same trip, should just find new user with same time/loc info
        mNewMatchButton = findViewById(R.id.request_another_user_button);
        mNewMatchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Writes user and request data into Firebase Realtime Database.
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                //get info from shared argument
                String uid = sharedPref.getString("uid","none");
                String data = sharedPref.getString("data","none");
                String startTime= sharedPref.getString( "startTime","none");
                String endTime = sharedPref.getString( "endTime","none");
                String location = sharedPref.getString( "location","none");
                String distanceFromUser = sharedPref.getString("distanceFromUser" ,"none");
                String destination = sharedPref.getString( "destination","none");
                String distanceFromDestination = sharedPref.getString( "distanceFromDestination","none");

                //check if data is there
                if (uid.equals("none") || data.equals("none") || startTime.equals("none") || endTime.equals("none") ||
                        location.equals("none") || distanceFromDestination.equals("none") || distanceFromUser.equals("none") ||
                        destination.equals("none") ) {


                    Toast.makeText(MatchResultActivity.this,
                            "Failed to request same match, please click cancel to enter your information",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Request request = new Request(uid, data, startTime, endTime, location, distanceFromUser,
                        destination, distanceFromDestination);

                FirebaseDatabaseReaderWriter firebaseDatabaseReaderWriter =
                        new FirebaseDatabaseReaderWriter();
                firebaseDatabaseReaderWriter.writeUserAndRideRequest(request);

                FindMatches finder = new FindMatches(firebaseDatabaseReaderWriter);
                User user;
                try {
                    user = finder.findMatches(request);
                } catch (NoSuchElementException e) {

                    return;
                }

                Intent intent = new Intent(getApplicationContext(), MatchResultActivity.class);
                // TODO: passes objects to another activity using Parcelable or Serializable class.
                // intent.putExtra(getApplicationContext().getString(R.string.matchResult), user);
                startActivity(intent);
            }
        });
*/

        //set my new trip button for going back to request for new trip
        mNewTripButton = findViewById(R.id.request_another_trip_button);
        mNewTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
                Intent intent = new Intent(MatchResultActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });


        //set my Back Button, delete this request from database + go back to map
        mBackButton = findViewById(R.id.match_result_back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();

                //delete this request
                FirebaseDatabaseReaderWriter firebaseDatabaseReaderWriter = new FirebaseDatabaseReaderWriter();
                firebaseDatabaseReaderWriter.deleteDate(date, requestId);
                firebaseDatabaseReaderWriter.deleteRideRequest(requestId);
                firebaseDatabaseReaderWriter.deleteUserAndRideRequest(uid, requestId);

                //go back to map
                Intent intent = new Intent(MatchResultActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

    }

    //Temporary Pickup Points Latitude and Longitude
    public void populatePickUpPoints(){
        pickUpPointsLatLng.add(new LatLng(35.975036, -78.924251)); //West Bus Stop
        pickUpPointsLatLng.add(new LatLng(36.005210, -78.914750)); //East Bus Stop
        pickUpPointsLatLng.add(new LatLng(36.005136, -78.925678)); //Central (Alexander Avenue) Bus Stop
        pickUpPointsLatLng.add(new LatLng(36.010424, -78.923344)); //9th Street Harris Teeter
        pickUpPointsLatLng.add(new LatLng(36.009485, -78.943577)); //Trinity Commons
        pickUpPointsLatLng.add(new LatLng(36.007840, -78.938019)); //Duke Hospital
    }

    public void saveInfo(){
        SharedPreferences sharedPref = getSharedPreferences("PickUpPointInfo", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor = UtilityFunctions.putDouble(editor, "PickUp Location Latitude", pickUpPointsLatLng.get(0).latitude);
        editor = UtilityFunctions.putDouble(editor, "PickUp Location Longitude", pickUpPointsLatLng.get(0).longitude);
        editor.commit();

    }

    public static void updateStatusTextView(String userEmail) {
        if (userEmail.equals("none") || userEmail.equals("") || userEmail == null) {
            mStatusText.setText("No Match Found");
        } else {
            mStatusText.setText("Found Match: " + userEmail);
        }
    }

    // TODO: disables back button.
}
