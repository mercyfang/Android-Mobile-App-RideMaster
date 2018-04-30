package edu.duke.compsci290.ridermaster.Activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.NoSuchElementException;

import FirebaseDatabase.FindMatches;
import FirebaseDatabase.FirebaseDatabaseReaderWriter;
import FirebaseDatabase.Request;
import FirebaseDatabase.User;

import Utilities.UtilityFunctions;
import edu.duke.compsci290.ridermaster.R;

public class RideRequestActivity extends BaseNavDrawerActivity {

    private String startingLocText;
    private String destinationLocText;

    private final String timePrompt = "Choose a time";
    private final String locationPrompt = "Choose a location";
    private final String distancePrompt = "Within -- miles";

    private TextView mDatePicker;

    private Spinner mStartTimeSpinner;
    private Spinner mEndTimeSpinner;
    private TextView mLocationTextView;
    private Spinner mDistanceFromUserSpinner;
    private TextView mDestinationTextView;
    private Spinner mDistanceFromDestSpinner;

    private Button mEnableGoogleMapButton;
    private Button mFindDestinationButton;
    private Button mSubmitButton;
    // Only for developer purpose.
    // TODO: remove later.
    private Button mSignOutButton;

    private Calendar mCalendar;

    private static final String TAG = "RideRequestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadData();
        // Sets activity main view.
        FrameLayout activityContainer = findViewById(R.id.activity_content);
        View.inflate(this, R.layout.activity_ride_request, activityContainer);

        mDatePicker = findViewById(R.id.choose_date_field_text_view);
        mStartTimeSpinner = findViewById(R.id.start_time_spinner);
        mEndTimeSpinner = findViewById(R.id.end_time_spinner);
        mLocationTextView = findViewById(R.id.user_location_text_view);
        mDistanceFromUserSpinner = findViewById(R.id.distance_from_user_spinner);

        mDestinationTextView = findViewById(R.id.user_destination_text_view);
        mDistanceFromDestSpinner = findViewById(R.id.distance_from_dest_spinner);

        mEnableGoogleMapButton = findViewById(R.id.google_map_location_button);
        mFindDestinationButton = findViewById(R.id.find_destination_button);
        mSubmitButton = findViewById(R.id.find_a_share_button);
        // TODO: remove later.
        mSignOutButton = findViewById(R.id.sign_out_button);

        mLocationTextView.setText(startingLocText);
        mDestinationTextView.setText(destinationLocText);

        // Creates DatePicker Dialog.
        mCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        mDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerdialog =  new DatePickerDialog(
                        RideRequestActivity.this,
                        date,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH));
                // Disables past dates in date picker.
                datePickerdialog.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerdialog.show();
            }
        });

        mEnableGoogleMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RideRequestActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!verifyMatchInputs()) {
                    Toast.makeText(RideRequestActivity.this, "Please fill out all information",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!verifyTime()) {
                    Toast.makeText(RideRequestActivity.this,
                            "Please make sure your end time is after your start time",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Writes user and request data into Firebase Realtime Database.
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                Request request = new Request(
                        firebaseUser.getUid(),
                        mDatePicker.getText().toString(),
                        mStartTimeSpinner.getSelectedItem().toString(),
                        mEndTimeSpinner.getSelectedItem().toString(),
                        mLocationTextView.getText().toString(),
                        // Only stores the miles number into request.
                        mDistanceFromUserSpinner.getSelectedItem().toString().split(" ")[1],
                        mDestinationTextView.getText().toString(),
                        mDistanceFromDestSpinner.getSelectedItem().toString().split(" ")[1]
                );
                FirebaseDatabaseReaderWriter firebaseDatabaseReaderWriter =
                        new FirebaseDatabaseReaderWriter();
                firebaseDatabaseReaderWriter.writeUserAndRideRequest(request);

                FindMatches finder = new FindMatches(firebaseDatabaseReaderWriter);
                User user;
                try {
                    user = finder.findMatches(request);
                } catch (NoSuchElementException e) {
                    Toast.makeText(RideRequestActivity.this,
                            "No match is found. Maybe try different time slots, or try again later?",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getApplicationContext(), MatchResultActivity.class);
                // TODO: passes objects to another activity using Parcelable or Serializable class.
                // intent.putExtra(getApplicationContext().getString(R.string.matchResult), user);
                startActivity(intent);
            }
        });

        // TODO: remove later.
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        ArrayList<String> times = getTimeSpinnerElements();
        ArrayList<String> locations = getLocationElements();
        ArrayList<String> destinations = getDestinationElements();
        ArrayList<String> distances = getDistanceElements();

        // Creates adapters for spinners.
        ArrayAdapter<String> timeAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, times);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> locationAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> distanceAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, distances);
        distanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> destinationAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, destinations);
        destinationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mStartTimeSpinner.setAdapter(timeAdapter);
        mStartTimeSpinner.setSelection(0);
        mEndTimeSpinner.setAdapter(timeAdapter);
        mEndTimeSpinner.setSelection(0);

        mDistanceFromUserSpinner.setAdapter(distanceAdapter);
        mDistanceFromUserSpinner.setSelection(0);

        mDistanceFromDestSpinner.setAdapter(distanceAdapter);
        mDistanceFromDestSpinner.setSelection(0);
    }

    @Override
    public void onBackPressed() {
        // Do nothing. Disables back button.
    }

    private void updateLabel() {
        String dateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        mDatePicker.setText(sdf.format(mCalendar.getTime()));
    }

    private ArrayList<String> getTimeSpinnerElements() {
        ArrayList<String> times = new ArrayList<>();
        times.add(timePrompt);
        // Time options starts from 00:00 to 23:50, 10 minutes apart.
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
        locations.add(locationPrompt);
        locations.add("West Campus Bus Stop");
        locations.add("East Campus Bus Stop");
        // TODO: adds more locations.

        return locations;
    }

    private ArrayList<String> getDistanceElements() {
        ArrayList<String> distances = new ArrayList<>();
        distances.add(distancePrompt);
        distances.add("Within 0.1 miles");
        distances.add("Within 0.3 miles");
        distances.add("Within 0.7 miles");
        distances.add("Within 1 miles");
        distances.add("Within 1.5 miles");
        distances.add("Within 3 miles");
        return distances;
    }

    private ArrayList<String> getDestinationElements() {
        ArrayList<String> destinations = new ArrayList<>();
        destinations.add(locationPrompt);
        destinations.add("RDU airport");
        // TODO: adds more locations.

        return destinations;
    }

    private boolean verifyMatchInputs() {
        if (mDatePicker.getText().toString().equals(getString(R.string.choose_a_date))
                || mEndTimeSpinner.getSelectedItem().toString().equals(timePrompt)
                || mStartTimeSpinner.getSelectedItem().toString().equals(timePrompt)
                || mLocationTextView.getText().toString().equals("")
                || mDistanceFromUserSpinner.getSelectedItem().toString().equals(distancePrompt)
                || mDestinationTextView.getText().toString().equals("")
                || mDistanceFromDestSpinner.getSelectedItem().toString().equals(distancePrompt)) {
            return false;
        }
        return true;
    }

    // Verifies user input end time is the same as, or after start time.
    private boolean verifyTime() {
        if (mEndTimeSpinner.getSelectedItem().toString().compareTo(
                mStartTimeSpinner.getSelectedItem().toString()) < 0) {
            return false;
        }
        return true;
    }

    private void loadData(){
        SharedPreferences sharedPref = getSharedPreferences("UserPathInfo", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        double myStartingLat = UtilityFunctions.getDouble(sharedPref, "Starting Location Latitude", 0);
        double myStartingLng = UtilityFunctions.getDouble(sharedPref, "Starting Location Longitude", 0);
        startingLocText = sharedPref.getString("Starting Location Text", "Durham");
        double myDestinationLat = UtilityFunctions.getDouble(sharedPref, "Destination Location Latitude", 0);
        double myDestinationLng = UtilityFunctions.getDouble(sharedPref, "Destination Location Longitude", 0);
        destinationLocText = sharedPref.getString("Destination Location Text", "Raleigh");
        Log.d(TAG, "loadData: " + myStartingLat + myStartingLng + startingLocText +
                myDestinationLat + myDestinationLng + destinationLocText);
        Log.d(TAG, "saveInfo: "+ sharedPref.getAll().toString());
    }
}
