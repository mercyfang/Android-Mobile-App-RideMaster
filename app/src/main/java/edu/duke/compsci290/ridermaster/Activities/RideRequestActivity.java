package edu.duke.compsci290.ridermaster.Activities;

import android.app.DatePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import edu.duke.compsci290.ridermaster.R;

public class RideRequestActivity extends AppCompatActivity {

    private TextView mDatePicker;

    private Spinner mStartTimeSpinner;
    private Spinner mEndTimeSpinner;
    private Spinner mLocationSpinner;

    private Button mEnableGoogleMapButton;
    private Button mSubmitButton;

    private Calendar mCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request);

        mDatePicker = findViewById(R.id.choose_date_field_text_view);

        mStartTimeSpinner = findViewById(R.id.start_time_spinner);
        mEndTimeSpinner = findViewById(R.id.end_time_spinner);
        mLocationSpinner = findViewById(R.id.location_spinner);

        mEnableGoogleMapButton = findViewById(R.id.google_map_location_button);
        mSubmitButton = findViewById(R.id.find_a_share_button);

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
                new DatePickerDialog(
                        RideRequestActivity.this,
                        date,
                        mCalendar.get(Calendar.YEAR),
                        mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

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
        mStartTimeSpinner.setSelection(0);
        mEndTimeSpinner.setAdapter(timeAdapter);
        mEndTimeSpinner.setSelection(0);
        mLocationSpinner.setAdapter(locationAdapter);
        mLocationSpinner.setSelection(0);
    }

    private void updateLabel() {
        String dateFormat = "MM/dd/yy";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        mDatePicker.setText(sdf.format(mCalendar.getTime()));
    }

    private ArrayList<String> getTimeSpinnerElements() {
        ArrayList<String> times = new ArrayList<>();
        times.add("Choose a time");
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
        locations.add("Choose a location");
        locations.add("West Campus Bus Stop");
        locations.add("East Campus Bus Stop");
        // TODO: adds more locations.

        return locations;
    }
}
