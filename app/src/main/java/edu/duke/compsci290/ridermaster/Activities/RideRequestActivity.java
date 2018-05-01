package edu.duke.compsci290.ridermaster.Activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.DrawerLayout;
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

    DrawerLayout mDrawerLayout;

    long milliss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.onCreate(savedInstanceState, R.layout.activity_ride_request);


        // Sets activity main view.








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
                final CharSequence distances[] = new CharSequence[] {
                        "Within 0.1 miles", "Within 0.3 miles", "Within 0.7 miles",
                        "Within 1 miles", "Within 1.5 miles", "Within 3 miles"};

                AlertDialog.Builder builder = new AlertDialog.Builder(RideRequestActivity.this);
                builder.setTitle("How far away can youfr match be?");
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
                final CharSequence distances[] = new CharSequence[] {
                        "Within 0.1 miles", "Within 0.3 miles", "Within 0.7 miles",
                        "Within 1 miles", "Within 1.5 miles", "Within 3 miles"};

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
                Log.d(TAG, "firebaseRequest: " +  mUserRangeTextView.getText().toString().replaceAll("[^0-9]",""));
                Log.d(TAG, "firebaseRequest: " +  mUserRangeTextView.getText().toString().replaceAll("[^0-9]",""));
                Request request = new Request(
                        firebaseUser.getUid(),
                        mDatePicker.getText().toString(),
                        String.format("%02d:%02d", startTimeHours, startTimeMinutes),
                        String.format("%02d:%02d", endTimeHours, endTimeMinutes),

                        String.format("%f;%f", myStartingLat , myStartingLng),
                        // Only stores the miles number into request.
                        String.format("%f",(Double.valueOf(mUserRangeTextView.getText().toString().replaceAll("[^0-9]",""))/69)),
                        String.format("%f;%f", myDestinationLat, myDestinationLng),

                        String.format("%f",(Double.valueOf(mDestinationRangeTextView.getText().toString().replaceAll("[^0-9]",""))/69))
                );



                FirebaseDatabaseReaderWriter firebaseDatabaseReaderWriter =
                        new FirebaseDatabaseReaderWriter();
                firebaseDatabaseReaderWriter.writeUserAndRideRequest(request);

                FindMatches finder = new FindMatches(firebaseDatabaseReaderWriter);
                String uid = firebaseUser.getUid();
                String requestId = request.getRequestId();
                String date = mDatePicker.getText().toString();
                String email = "";

                try {
                    finder.findMatches(request);
                } catch (NoSuchElementException e) {
                    // TODO: jane display message "no match is found".
                    //TODO: jane added this, but not responding?
                    MatchResultActivity.updateStatusTextView("none");
                }

                Intent intent = new Intent(getApplicationContext(), MatchResultActivity.class);
                // TODO: passes objects to another activity using Parcelable or Serializable class.

                intent.putExtra("uid",
                        uid);
                intent.putExtra("requestid",
                        requestId);
                intent.putExtra("date",
                        date);
                //need to pass the find matches user in
                restoreDefaults();
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
    protected void onStart() {
        super.onStart();
        loadData();

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
                || mBeginTimeTextView.getText().toString().equals(getString(R.string.begin_time_prompt))
                || mEndTimeTextView.getText().toString().equals(getString(R.string.end_time_prompt))
                || mLocationTextView.getText().toString().equals(getString(R.string.starting_point_prompt))
                || mDestinationTextView.getText().toString().equals(getString(R.string.ending_point_prompt))
                || mUserRangeTextView.toString().equals(getString(R.string.choose_miles_prompt))
                || mDestinationRangeTextView.toString().equals(getString(R.string.choose_miles_prompt))) {
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

    @Override
    protected void onPause() {
        super.onPause();

        saveInfo();





    }

    private void loadData(){
        SharedPreferences sharedPref = getSharedPreferences("UserDateTimeMilesInfo", Context.MODE_PRIVATE);
        Editor editor = sharedPref.edit();


        startTime = sharedPref.getInt("startTime", 0);
        Log.d(TAG, "loadDate:startTime:" + startTime);


        endTime = sharedPref.getInt("endTime", 0);
        Log.d(TAG, "loadDate:endTime:" + endTime);


        startTimeHours = sharedPref.getInt("startTimeHours", 0);
        Log.d(TAG, "loadDate:startTimeHours:" + startTimeHours);


        startTimeMinutes = sharedPref.getInt("startTimeMinutes", 0);
        Log.d(TAG, "loadDate:startTimeMinutes:" + startTimeMinutes);


        endTimeHours = sharedPref.getInt("endTimeHours", 0);
        Log.d(TAG, "loadDate:endTimeHours:" + endTimeHours);


        endTimeMinutes = sharedPref.getInt("endTimeMinutes", 0);
        Log.d(TAG, "loadDate:endTimeMinutes:" + endTimeMinutes);

        myStartingLat = UtilityFunctions.getDouble(sharedPref, "myStartingLat", 0);
        Log.d(TAG, "loadDate:myStartingLat:" + myStartingLat);


        myStartingLng = UtilityFunctions.getDouble(sharedPref, "myStartingLng", 0);
        Log.d(TAG, "loadDate:myStartingLng:" + myStartingLng);

        myDestinationLat = UtilityFunctions.getDouble(sharedPref, "myDestinationLat", 0);
        Log.d(TAG, "loadDate:myDestinationLat:" + myDestinationLat);

        myDestinationLng = UtilityFunctions.getDouble(sharedPref, "myDestinationLng", 0);
        Log.d(TAG, "loadDate:myDestinationLng:" + myDestinationLng);

        mDatePicker.setText(sharedPref.getString("mDatePicker", getString(R.string.choose_a_date)));
        Log.d(TAG, "loadDate:mDatePicker:" + mDatePicker.getText().toString());

        mBeginTimeTextView.setText(sharedPref.getString("mBeginTimeTextView", getString(R.string.begin_time_prompt)));
        Log.d(TAG, "loadDate:mBeginTimeTextView:" + mBeginTimeTextView.getText().toString());


        mEndTimeTextView.setText(sharedPref.getString("mEndTimeTextView", getString(R.string.end_time_prompt)));
        Log.d(TAG, "loadDate:mEndTimeTextView:" + mEndTimeTextView.getText().toString());


        mUserRangeTextView.setText(sharedPref.getString("mUserRangeTextView", getString(R.string.choose_miles_prompt)));
        Log.d(TAG, "loadDate:mUserRangeTextView:" + mUserRangeTextView.getText().toString());


        mDestinationRangeTextView.setText(sharedPref.getString("mDestinationRangeTextView", getString(R.string.choose_miles_prompt)));
        Log.d(TAG, "loadDate:mDestinationRangeTextView:" + mDestinationRangeTextView.getText().toString());


        mLocationTextView.setText(sharedPref.getString("mLocationTextView", getString(R.string.starting_point_prompt)));
        Log.d(TAG, "loadDate:mLocationTextView:" + mLocationTextView.getText().toString());


        mDestinationTextView.setText(sharedPref.getString("mDestinationTextView", getString(R.string.end_time_prompt)));
        Log.d(TAG, "loadDate:mDestinationTextView:" + mDestinationTextView.getText().toString());

        sharedPref = getSharedPreferences("UserPathInfo", Context.MODE_PRIVATE);

        editor = sharedPref.edit();
        myStartingLat = UtilityFunctions.getDouble(sharedPref, "Starting Location Latitude", 0);
        myStartingLng = UtilityFunctions.getDouble(sharedPref, "Starting Location Longitude", 0);
        startingLocText = sharedPref.getString("Starting Location Text", getString(R.string.starting_point_prompt));
        myDestinationLat = UtilityFunctions.getDouble(sharedPref, "Destination Location Latitude", 0);
        myDestinationLng = UtilityFunctions.getDouble(sharedPref, "Destination Location Longitude", 0);
        destinationLocText = sharedPref.getString("Destination Location Text", getString(R.string.ending_point_prompt));

        double myStartingLat = UtilityFunctions.getDouble(
                sharedPref, "Starting Location Latitude", 0);
        double myStartingLng = UtilityFunctions.getDouble(
                sharedPref, "Starting Location Longitude", 0);
        startingLocText = sharedPref.getString(
                "Starting Location Text", getString(R.string.starting_point_prompt));

        mLocationTextView.setText(startingLocText);



        double myDestinationLat = UtilityFunctions.getDouble(
                sharedPref, "Destination Location Latitude", 0);
        double myDestinationLng = UtilityFunctions.getDouble(
                sharedPref, "Destination Location Longitude", 0);
        destinationLocText = sharedPref.getString(
                "Destination Location Text", getString(R.string.ending_point_prompt));

        mDestinationTextView.setText(destinationLocText);

        Log.d(TAG, "loadData: " + myStartingLat + myStartingLng + startingLocText +
                myDestinationLat + myDestinationLng + destinationLocText);
        Log.d(TAG, "saveInfo: "+ sharedPref.getAll().toString());
    }







    private void saveInfo(){
        SharedPreferences sharedPref = getSharedPreferences("UserDateTimeMilesInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        Log.d(TAG, "saveInfo:startTime:" + startTime);
        editor.putInt("startTime", startTime);

        Log.d(TAG, "saveInfo:endTime:" + endTime);
        editor.putInt("endTime", endTime);

        Log.d(TAG, "saveInfo:startTimeHours:" + startTimeHours);
        editor.putInt("startTimeHours", startTimeHours);

        Log.d(TAG, "saveInfo:startTimeMinutes:" + startTimeMinutes);
        editor.putInt("startTimeMinutes", startTimeMinutes);

        Log.d(TAG, "saveInfo:endTimeHours:" + endTimeHours);
        editor.putInt("endTimeHours", endTimeHours);

        Log.d(TAG, "saveInfo:endTimeMinutes:" + endTimeMinutes);
        editor.putInt("endTimeMinutes", endTimeMinutes);

        Log.d(TAG, "saveInfo:myStartingLat:" + myStartingLat);
        editor = UtilityFunctions.putDouble(editor, "myStartingLat", myStartingLat);

        Log.d(TAG, "saveInfo:myStartingLng:" + myStartingLng);
        editor = UtilityFunctions.putDouble(editor, "myStartingLng", myStartingLng);

        Log.d(TAG, "saveInfo:myDestinationLat:" + myDestinationLat);
        editor = UtilityFunctions.putDouble(editor, "myDestinationLat", myDestinationLat);

        Log.d(TAG, "saveInfo:myDestinationLng:" + myDestinationLng);
        editor = UtilityFunctions.putDouble(editor, "myDestinationLng", myDestinationLng);

        Log.d(TAG, "saveInfo:mDatePicker:" + mDatePicker.getText().toString());
        editor.putString("mDatePicker", mDatePicker.getText().toString());

        Log.d(TAG, "saveInfo:mBeginTimeTextView:" + mBeginTimeTextView.getText().toString());
        editor.putString("mBeginTimeTextView", mBeginTimeTextView.getText().toString());

        Log.d(TAG, "saveInfo:mEndTimeTextView:" + mEndTimeTextView.getText().toString());
        editor.putString("mEndTimeTextView", mEndTimeTextView.getText().toString());

        Log.d(TAG, "saveInfo:mUserRangeTextView:" + mUserRangeTextView.getText().toString());
        editor.putString("mUserRangeTextView", mUserRangeTextView.getText().toString());

        Log.d(TAG, "saveInfo:mDestinationRangeTextView:" + mDestinationRangeTextView.getText().toString());
        editor.putString("mDestinationRangeTextView", mDestinationRangeTextView.getText().toString());

        Log.d(TAG, "saveInfo:mLocationTextView:" + mLocationTextView.getText().toString());
        editor.putString("mLocationTextView", mLocationTextView.getText().toString());

        Log.d(TAG, "saveInfo:mDestinationTextView:" + mDestinationTextView.getText().toString());
        editor.putString("mDestinationTextView", mDestinationTextView.getText().toString());


        editor.commit();


        //Log.d(TAG, "saveInfo: "+ sharedPref.getAll().toString());
    }

    private void restoreDefaults() {
        SharedPreferences sharedPref = getSharedPreferences("UserDateTimeMilesInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        startTime = 0;
        Log.d(TAG, "restoreDefaults:startTime:" + startTime);
        editor.putInt("startTime", startTime);

        endTime = 0;
        Log.d(TAG, "restoreDefaults:endTime:" + endTime);
        editor.putInt("endTime", endTime);

        startTimeHours = 0;
        Log.d(TAG, "restoreDefaults:startTimeHours:" + startTimeHours);
        editor.putInt("startTimeHours", startTimeHours);

        startTimeMinutes = 0;
        Log.d(TAG, "restoreDefaults:startTimeMinutes:" + startTimeMinutes);
        editor.putInt("startTimeMinutes", startTimeMinutes);

        endTimeHours = 0;
        Log.d(TAG, "restoreDefaults:endTimeHours:" + endTimeHours);
        editor.putInt("endTimeHours", endTimeHours);

        endTimeMinutes = 0;
        Log.d(TAG, "restoreDefaults:endTimeMinutes:" + endTimeMinutes);
        editor.putInt("endTimeMinutes", endTimeMinutes);

        myStartingLat = 0;
        Log.d(TAG, "restoreDefaults:myStartingLat:" + myStartingLat);
        editor = UtilityFunctions.putDouble(editor, "myStartingLat", myStartingLat);

        myStartingLng = 0;
        Log.d(TAG, "restoreDefaults:myStartingLng:" + myStartingLng);
        editor = UtilityFunctions.putDouble(editor, "myStartingLng", myStartingLng);

        myDestinationLat = 0;
        Log.d(TAG, "restoreDefaults:myDestinationLat:" + myDestinationLat);
        editor = UtilityFunctions.putDouble(editor, "myDestinationLat", myDestinationLat);

        myDestinationLng = 0;
        Log.d(TAG, "restoreDefaults:myDestinationLng:" + myDestinationLng);
        editor = UtilityFunctions.putDouble(editor, "myDestinationLng", myDestinationLng);

        mDatePicker.setText(getString(R.string.choose_a_date));
        Log.d(TAG, "restoreDefaults:mDatePicker:" + mDatePicker.getText().toString());
        editor.putString("mDatePicker", mDatePicker.getText().toString());

        mBeginTimeTextView.setText(getString(R.string.begin_time_prompt));
        Log.d(TAG, "restoreDefaults:mBeginTimeTextView:" + mBeginTimeTextView.getText().toString());
        editor.putString("mBeginTimeTextView", mBeginTimeTextView.getText().toString());

        mEndTimeTextView.setText(getString(R.string.end_time_prompt));
        Log.d(TAG, "restoreDefaults:mEndTimeTextView:" + mEndTimeTextView.getText().toString());
        editor.putString("mEndTimeTextView", mEndTimeTextView.getText().toString());

        mUserRangeTextView.setText(getString(R.string.choose_miles_prompt));
        Log.d(TAG, "restoreDefaults:mUserRangeTextView:" + mUserRangeTextView.getText().toString());
        editor.putString("mUserRangeTextView", mUserRangeTextView.getText().toString());

        mDestinationRangeTextView.setText(getString(R.string.choose_miles_prompt));
        Log.d(TAG, "restoreDefaults:mDestinationRangeTextView:" + mDestinationRangeTextView.getText().toString());
        editor.putString("mDestinationRangeTextView", mDestinationRangeTextView.getText().toString());

        mLocationTextView.setText(getString(R.string.starting_point_prompt));
        Log.d(TAG, "restoreDefaults:mLocationTextView:" + mLocationTextView.getText().toString());
        editor.putString("mLocationTextView", mLocationTextView.getText().toString());

        mDestinationTextView.setText(getString(R.string.end_time_prompt));
        Log.d(TAG, "restoreDefaults:mDestinationTextView:" + mDestinationTextView.getText().toString());
        editor.putString("mDestinationTextView", mDestinationTextView.getText().toString());


        editor.commit();
    }

}
