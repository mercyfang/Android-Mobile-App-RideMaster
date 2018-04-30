package edu.duke.compsci290.ridermaster.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import Utilities.UtilityFunctions;
import edu.duke.compsci290.ridermaster.R;

public class MatchResultActivity extends BaseNavDrawerActivity {

    ArrayList<LatLng> pickUpPointsLatLng = new ArrayList<>();



    private Button mBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        populatePickUpPoints();
        super.onCreate(savedInstanceState);

        // Sets activity main view.
        FrameLayout activityContainer = findViewById(R.id.activity_content);
        View.inflate(this, R.layout.activity_match_result, activityContainer);
        mBackButton = findViewById(R.id.match_result_back_button);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
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




    // TODO: disables back button.
}
