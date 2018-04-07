package edu.duke.compsci290.ridermaster.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

import edu.duke.compsci290.ridermaster.R;

public class RideRequestActivity extends AppCompatActivity {

    private Spinner mStartTimeSpinner;
    private Spinner mEndTimeSpinner;
    private Spinner mLocationSpinner;

    private Button mEnableGoogleMapButton;
    private Button mSubmitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request);

        mStartTimeSpinner = findViewById(R.id.start_time_spinner);
        mEndTimeSpinner = findViewById(R.id.end_time_spinner);
        mLocationSpinner = findViewById(R.id.location_spinner);

        mEnableGoogleMapButton = findViewById(R.id.google_map_location_button);
        mSubmitButton = findViewById(R.id.find_a_share_button);

        ArrayList<String> times = getTimeSpinnerElements();
        ArrayList<String> locations = getLocationElements();

        // Creates adapters for spinners.
        ArrayAdapter<String> timeAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> locationAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mStartTimeSpinner.setAdapter(timeAdapter);
        mEndTimeSpinner.setAdapter(timeAdapter);
        mLocationSpinner.setAdapter(locationAdapter);
    }

    private ArrayList<String> getTimeSpinnerElements() {
        ArrayList<String> times = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute = 0; minute <= 50; minute = minute + 10) {
                String hourString = hour < 10
                        ? ("0" + Integer.toString(hour)) : Integer.toString(hour);
                String minuteString = minute == 0 ? "00" : Integer.toString(minute);
                times.add(hourString + ":" + minuteString);
            }
        }
        return times;
    }

    private ArrayList<String> getLocationElements() {
        ArrayList<String> locations = new ArrayList<>();
        locations.add("West Campus Bus Stop");
        locations.add("East Campus Bus Stop");
        // TODO: adds more locations.
        return locations;
    }
}
