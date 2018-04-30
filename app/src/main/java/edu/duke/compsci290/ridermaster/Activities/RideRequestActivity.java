package edu.duke.compsci290.ridermaster.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
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
    private ConstraintLayout mCalendarButton;

    private ConstraintLayout mBeginTimeButton;
    private TextView mBeginTimeTextView;

    private ConstraintLayout mEndTimeButton;
    private TextView mEndTimeTextView;

    private ConstraintLayout mUserRangeButton;
    private TextView mUserRangeTextView;

    private ConstraintLayout mDestinationRangeButton;
    private TextView mDestinationRangeTextView;

    private TextView mLocationTextView;
    private TextView mDestinationTextView;

    private ConstraintLayout mEnableGoogleMapButton;
    private ConstraintLayout mFindMatchesButton;

    private ConstraintLayout mSignOutButton;

    private Calendar mCalendar;

    private int startTime;





    private int endTime;


    private static final String TAG = "RideRequestActivity";

    /**
     * Variables for firebase
     *
     *
     */

    private int startTimeHours;
    private int startTimeMinutes;

    private int endTimeHours;
    private int endTimeMinutes;

    private double myStartingLat;
    private double myStartingLng;

    private double myDestinationLat;
    private double myDestinationLng;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadData();
        // Sets activity main view.
        setContentView(R.layout.activity_ride_request);


        mDatePicker = findViewById(R.id.choose_date_field_text_view);
        mCalendarButton = findViewById(R.id.calendarButton);

        mBeginTimeButton = findViewById(R.id.beginTimeButton);
        mBeginTimeTextView = findViewById(R.id.choose_begin_time_field_text_view);

        mEndTimeButton = findViewById(R.id.endTimeButton);
        mEndTimeTextView = findViewById(R.id.choose_end_time_field_text_view);

        mUserRangeButton = findViewById(R.id.user_range_button);
        mUserRangeTextView = findViewById(R.id.user_range_value_text_view);

        mDestinationRangeButton = findViewById(R.id.destination_range_button);
        mDestinationRangeTextView = findViewById(R.id.destination_range_value_text_view);

        mLocationTextView = findViewById(R.id.user_location_text_view);
        mDestinationTextView = findViewById(R.id.user_destination_text_view);
        mEnableGoogleMapButton = findViewById(R.id.backToMapButton);
        mFindMatchesButton = findViewById(R.id.findMatchesButton);
        // TODO: remove later.
        mSignOutButton = findViewById(R.id.sign_out_button);

        mLocationTextView.setText(startingLocText);
        mDestinationTextView.setText(destinationLocText);

        try {
            String[] f = getAssets().list("");
            for (String f1 : f) {

                Log.v("names", f1);
            }
        }catch(Exception e){

        }

//        Typeface myCustomFont = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Light.tff");
//
//
//
//        mLocationTextView.setTypeface(myCustomFont);
//
//        mDestinationTextView.setTypeface(myCustomFont);
//
//        mEnableGoogleMapButton.setTypeface(myCustomFont);
//        mFindDestinationButton.setTypeface(myCustomFont);
//        mFindMatchesButton.setTypeface(myCustomFont);
//        // TODO: remove later.
//        mSignOutButton.setTypeface(myCustomFont);

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
        mCalendarButton.setOnClickListener(new View.OnClickListener() {
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

        final TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = view.getHour() % 12;
                String timeOfDay = "";
                if(view.getHour() > 12){
                    timeOfDay = "PM";
                }else{
                    timeOfDay = "AM";
                }
                if(hour == 0){
                    hour = 12;
                }


                startTime = view.getHour() * 60 + view.getMinute();
                startTimeHours = view.getHour();
                startTimeMinutes = view.getMinute();



                updateBeginTime(String.format("%d:%02d %s", hour, view.getMinute(), timeOfDay));

            }
        };
        mBeginTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog =  new TimePickerDialog(
                        RideRequestActivity.this,
                        time,
                        mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE),
                        false);
                // Disables past dates in date picker.

                timePickerDialog.show();
            }
        });

        final TimePickerDialog.OnTimeSetListener time2 = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = view.getHour() % 12;
                String timeOfDay = "";
                if(view.getHour() > 12){
                    timeOfDay = "PM";
                }else{
                    timeOfDay = "AM";
                }
                if(hour == 0){
                    hour = 12;
                }

                endTime = view.getHour()*60 + view.getMinute();

                endTimeHours = view.getHour();
                endTimeMinutes = view.getMinute();

                updateEndTime(String.format("%d:%02d %s", hour, view.getMinute(), timeOfDay));

            }
        };
        mEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog =  new TimePickerDialog(
                        RideRequestActivity.this,
                        time2,
                        mCalendar.get(Calendar.HOUR_OF_DAY),
                        mCalendar.get(Calendar.MINUTE),
                        false);
                // Disables past dates in date picker.




                timePickerDialog.show();
            }
        });

        mUserRangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence distances[] = new CharSequence[] {"Within 0.1 miles", "Within 0.3 miles", "Within 0.7 miles", "Within 1 miles", "Within 1.5 miles", "Within 3 miles"};


                AlertDialog.Builder builder = new AlertDialog.Builder(RideRequestActivity.this);
                builder.setTitle("How far away can your match be?");
                builder.setItems(distances, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateUserRange(distances[which].toString().replace("Within", ""));
                    }
                });
                builder.show();
            }
        });

        mDestinationRangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence distances[] = new CharSequence[] {"Within 0.1 miles", "Within 0.3 miles", "Within 0.7 miles", "Within 1 miles", "Within 1.5 miles", "Within 3 miles"};

                AlertDialog.Builder builder = new AlertDialog.Builder(RideRequestActivity.this);
                builder.setTitle("How far can your match's Destination be?");
                builder.setItems(distances, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateDestinationRange(distances[which].toString().replace("Within", ""));
                    }
                });
                builder.show();
            }
        });


        mEnableGoogleMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RideRequestActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        mFindMatchesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onDatePicker: " + mDatePicker);
                Log.d(TAG, "onStartTimeHours: " + startTimeHours);
                Log.d(TAG, "onStartTimeMinutes: " + startTimeMinutes);
                Log.d(TAG, "onEndTimeHours: " + endTimeHours);
                Log.d(TAG, "onEndTimeMinutes: " + endTimeMinutes);
                Log.d(TAG, "onStartLat" + myStartingLat);
                Log.d(TAG, "onStartLng " + myStartingLng);
                Log.d(TAG, "mUserRange: " + mUserRangeTextView.getText().toString());
                Log.d(TAG, "onEndLat" + myDestinationLat);
                Log.d(TAG, "onEndLng" + myDestinationLng);
                Log.d(TAG, "mDestinationRange: " + mDestinationRangeTextView.getText().toString());
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
                        String.format("%02d:%02d", startTimeHours, startTimeMinutes),
                        String.format("%02d:%02d", endTimeHours, endTimeMinutes),

                        String.format("%f.%f", myStartingLat , myStartingLng),
                        // Only stores the miles number into request.
                        String.format("%f",(Double.valueOf(mUserRangeTextView.getText().toString().split(" ")[1])/69)),
                        String.format("%f.%f", myDestinationLat, myDestinationLng),

                        String.format("%f",(Double.valueOf(mDestinationRangeTextView.getText().toString().split(" ")[1])/69))
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

    private void updateBeginTime(String s) {

        mBeginTimeTextView.setText(s);
    }

    private void updateEndTime(String s) {

        mEndTimeTextView.setText(s);
    }

    private void updateUserRange(String s){
        mUserRangeTextView.setText(s);
    }

    private void updateDestinationRange(String s){
        mDestinationRangeTextView.setText(s);
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
                || mBeginTimeTextView.getText().toString().equals(R.string.begin_time_prompt)
                || mEndTimeTextView.getText().toString().equals(R.string.end_time_prompt)
                || mLocationTextView.getText().toString().equals(R.string.starting_point_prompt)
                || mDestinationTextView.getText().toString().equals(R.string.ending_point_prompt)
                || mUserRangeTextView.toString().equals(R.string.choose_miles_prompt)
                || mDestinationRangeTextView.toString().equals(R.string.choose_miles_prompt)) {
            return false;
        }
        return true;
    }

    // Verifies user input end time is the same as, or after start time.
    private boolean verifyTime() {
        if (endTime < startTime) {
            return false;
        }
        return true;
    }

    private void loadData(){
        SharedPreferences sharedPref = getSharedPreferences("UserPathInfo", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        myStartingLat = UtilityFunctions.getDouble(sharedPref, "Starting Location Latitude", 0);
        myStartingLng = UtilityFunctions.getDouble(sharedPref, "Starting Location Longitude", 0);
        startingLocText = sharedPref.getString("Starting Location Text", "Durham");
        myDestinationLat = UtilityFunctions.getDouble(sharedPref, "Destination Location Latitude", 0);
        myDestinationLng = UtilityFunctions.getDouble(sharedPref, "Destination Location Longitude", 0);
        destinationLocText = sharedPref.getString("Destination Location Text", "Raleigh");
        Log.d(TAG, "loadData: " + myStartingLat + myStartingLng + startingLocText +
                myDestinationLat + myDestinationLng + destinationLocText);
        Log.d(TAG, "saveInfo: "+ sharedPref.getAll().toString());
    }
}
