package edu.duke.compsci290.partyappandroid;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddPartyActivity extends AppCompatActivity {
    private Button mStartTimeButton;
    private Button mEndTimeButton;
    private Button mStartDateButton;
    private Button mEndDateButton;
    private TextView mStartDateText;
    private TextView mEndDateText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_party);
        mStartTimeButton = findViewById(R.id.choose_start_time_button);
        mStartDateButton = findViewById(R.id.choose_start_date_button);
        mEndTimeButton = findViewById(R.id.choose_end_time_button);
        mEndDateButton = findViewById(R.id.choose_end_date_button);

        mStartDateText = findViewById(R.id.start_date_text);
        mEndDateText = findViewById(R.id.end_date_text);
        Calendar myCal = Calendar.getInstance();
        myCal.set(Calendar.MILLISECOND, 0);
        mStartDateText.setText(myCal.getTime().toString());
        mEndDateText.setText(myCal.getTime().toString());
        mStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStartTime();
            }
        });
        mStartDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setStartDate();
            }
        });
        mEndTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEndTime();
            }
        });
        mEndDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setEndDate();
            }
        });
    }

    private void setStartTime(){
        TimePickerStartFragment newFragment = new TimePickerStartFragment();
        newFragment.show(getFragmentManager(), "timePicker");

    }

    private void setStartDate(){
        DatePickerStartFragment newFragment = new DatePickerStartFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void setEndTime(){
        TimePickerEndFragment newFragment = new TimePickerEndFragment();
        newFragment.show(getFragmentManager(), "timePicker");

    }

    private void setEndDate(){
        DatePickerEndFragment newFragment = new DatePickerEndFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }



    public static class DatePickerStartFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            TextView startTextView = getActivity().findViewById(R.id.start_date_text);
            String startText = startTextView.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(startText));
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                startTextView.setText(cal.getTime().toString());

            } catch (ParseException e) {
                e.printStackTrace();
            }

            Log.d("TestTest",cal.getTime().toString());
            // Do something with the date chosen by the user
        }
    }

    public static class DatePickerEndFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
        }
    }

    public static class TimePickerStartFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            TextView startTextView = getActivity().findViewById(R.id.start_date_text);
            String startText = startTextView.getText().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdf.parse(startText));
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                startTextView.setText(cal.getTime().toString());

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public static class TimePickerEndFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
    }


}
