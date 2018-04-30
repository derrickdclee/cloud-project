package edu.duke.compsci290.partyappandroid;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.duke.compsci290.partyappandroid.EventPackage.Party;
import edu.duke.compsci290.partyappandroid.EventPackage.Service;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class AddPartyActivity extends AppCompatActivity {
    private Button mStartTimeButton;
    private Button mEndTimeButton;
    private Button mStartDateButton;
    private Button mEndDateButton;
    private Button mLocationButton;
    private TextView mLocationText;
    private TextView mStartDateText;
    private TextView mEndDateText;
    private TextView mHiddenStartDate;
    private TextView mHiddenEndDate;
    private Button mSubmitButton;
    private Button mCancelButton;
    private Party mParty;
    private EditText mPartyName;
    private EditText mPartyLocation;
    private EditText mPartyDescription;
    private Button mUploadImageButton;
    private ImageView mPartyImage;
    private RequestQueue queue;
    private final int IMAGE_REQUEST = 5;
    private Uri mImageUri;
    private static final SimpleDateFormat sdfForDjango = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssXXX", Locale.getDefault());
    private static final int PICK_IMAGE = 100;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 7;
    private Service service;
    private MultipartBody.Part mImageRequestBody;
    private Place mPlace;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_add_party);
        mStartTimeButton = findViewById(R.id.choose_start_time_button);
        mStartDateButton = findViewById(R.id.choose_start_date_button);
        mEndTimeButton = findViewById(R.id.choose_end_time_button);
        mEndDateButton = findViewById(R.id.choose_end_date_button);
        mSubmitButton = findViewById(R.id.new_party_submit_button);
        mCancelButton = findViewById(R.id.new_party_cancel_button);
        mStartDateText = findViewById(R.id.start_date_text);
        mEndDateText = findViewById(R.id.end_date_text);
        mPartyName = findViewById(R.id.add_party_name_box);
        mPartyDescription = findViewById(R.id.add_party_description_box);
        mHiddenStartDate = findViewById(R.id.hidden_start_date);
        mHiddenEndDate = findViewById(R.id.hidden_end_date);
        mPartyImage = findViewById(R.id.uploaded_party_image);
        mUploadImageButton = findViewById(R.id.upload_image_button);

        mLocationText = findViewById(R.id.chosen_location_text);
        mLocationButton = findViewById(R.id.choose_location_button);

        Calendar myCal = Calendar.getInstance();
        myCal.set(Calendar.SECOND, 0);
        mHiddenStartDate.setText(sdfForDjango.format(myCal.getTime()));
        mHiddenEndDate.setText(sdfForDjango.format(myCal.getTime()));
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
        mUploadImageButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocation();
            }
        });
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onSubmit();
                onSubmitv2();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            if (result != PackageManager.PERMISSION_GRANTED){
                try {
                    requestPermissionForReadExtertalStorage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            try {
                requestPermissionForReadExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setupretrofit();
    }

    private void requestPermissionForReadExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    11);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
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


    private void uploadImage(){
        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , IMAGE_REQUEST);

    }


    public static class DatePickerStartFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            TextView startTextHiddenView = getActivity().findViewById(R.id.hidden_start_date);
            String startTextHidden = startTextHiddenView.getText().toString();
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdfForDjango.parse(startTextHidden));

            } catch (ParseException e){
                Log.d("PARSEECXEPTION", e.toString());
            }
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            TextView startTextHiddenView = getActivity().findViewById(R.id.hidden_start_date);
            String startTextHidden = startTextHiddenView.getText().toString();
            TextView startTextView = getActivity().findViewById(R.id.start_date_text);
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdfForDjango.parse(startTextHidden));
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                startTextView.setText(cal.getTime().toString());
                startTextHiddenView.setText(sdfForDjango.format(cal.getTime()));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("TestTest",cal.getTime().toString());
        }
    }

    public static class DatePickerEndFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            TextView endTextHiddenView = getActivity().findViewById(R.id.hidden_end_date);
            String endTextHidden = endTextHiddenView.getText().toString();
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdfForDjango.parse(endTextHidden));

            } catch (ParseException e){
                Log.d("PARSEECXEPTION", e.toString());
            }
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            TextView endTextHiddenView = getActivity().findViewById(R.id.hidden_end_date);
            String endTextHidden = endTextHiddenView.getText().toString();
            TextView endTextView = getActivity().findViewById(R.id.end_date_text);
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdfForDjango.parse(endTextHidden));
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                endTextView.setText(cal.getTime().toString());
                endTextHiddenView.setText(sdfForDjango.format(cal.getTime()));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            Log.d("TestTest",cal.getTime().toString());
        }
    }

    public static class TimePickerStartFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker

            TextView startTextHiddenView = getActivity().findViewById(R.id.hidden_start_date);
            String startTextHidden = startTextHiddenView.getText().toString();
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdfForDjango.parse(startTextHidden));

            } catch (ParseException e){
                Log.d("PARSEECXEPTION", e.toString());
            }
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            TextView startTextHiddenView = getActivity().findViewById(R.id.hidden_start_date);
            String startTextHidden = startTextHiddenView.getText().toString();
            TextView startTextView = getActivity().findViewById(R.id.start_date_text);

            //TextView startTextView = getActivity().findViewById(R.id.start_date_text);
            //String startText = startTextView.getText().toString();
            //SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdfForDjango.parse(startTextHidden));
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                startTextView.setText(cal.getTime().toString());
                startTextHiddenView.setText(sdfForDjango.format(cal.getTime()));

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

            TextView endTextHiddenView = getActivity().findViewById(R.id.hidden_end_date);
            String endTextHidden = endTextHiddenView.getText().toString();
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(sdfForDjango.parse(endTextHidden));

            } catch (ParseException e){
                Log.d("PARSEECXEPTION", e.toString());
            }
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            TextView endTextHiddenView = getActivity().findViewById(R.id.hidden_end_date);
            String endTextHidden = endTextHiddenView.getText().toString();
            TextView endTextView = getActivity().findViewById(R.id.end_date_text);

            //TextView startTextView = getActivity().findViewById(R.id.start_date_text);
            //String startText = startTextView.getText().toString();
            //SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
            Calendar cal = Calendar.getInstance();
            try {
                cal.setTime(sdfForDjango.parse(endTextHidden));
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                endTextView.setText(cal.getTime().toString());
                endTextHiddenView.setText(sdfForDjango.format(cal.getTime()));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void cancel(){
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case IMAGE_REQUEST:
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    mImageUri = data.getData();
                    mPartyImage.setImageURI(selectedImage);
                    mPartyImage.requestLayout();
                    //mPartyImage.getLayoutParams().height = 100;
                    //mPartyImage.getLayoutParams().width = 100;
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    android.database.Cursor cursor = getContentResolver().query(mImageUri, filePathColumn, null, null, null);
                    if (cursor == null)
                        return;

                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();

                    File file = new File(filePath);
                    /*
                    RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
                    mImageRequestBody = MultipartBody.Part.createFormData("image", file.getName(), reqFile);*/

                    Disposable mydisp = new Compressor(this)
                            .setQuality(75)
                            .compressToFileAsFlowable(file)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(t -> {
                                Log.d("HOPFULLY", "THIS ONLY GETS CALLED ONCE");
                                RequestBody reqFile = RequestBody.create(MediaType.parse("image/jpeg"), t);
                                mImageRequestBody = MultipartBody.Part.createFormData("image", t.getName(), reqFile);
                            }, e -> {

                            });
                    compositeDisposable.add(mydisp);

                }
                break;

            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    mLocationText.setText(place.getAddress());
                    mPlace = place;

                    Geocoder myg = new Geocoder(this);
                    try {
                        List<Address> myaddr = myg.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1);
                        String addr = "";
                        for (int i=0;i<=myaddr.get(0).getMaxAddressLineIndex();i++){
                            addr += myaddr.get(0).getAddressLine(i);
                        }
                        Log.d("Place", addr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.i("PLACE", "Place: " + place.getName());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Log.d("STATUS", status.toString());
                } else if (resultCode == RESULT_CANCELED) {
                    // The user canceled the operation.
                    Log.d("ErROR", "canceled");
                }
                break;

        }
    }

    private void onSubmitv2(){
        String accessToken = "";
        SharedPreferences  mPrefs = getSharedPreferences("app_tokens", MODE_PRIVATE);
        if (mPrefs.contains("access_token") && !mPrefs.getString("access_token", "").equals("")){
            accessToken = mPrefs.getString("access_token", "");
        }
        if (mPlace==null || mPartyName.getText().equals("")){
            return;
        }
        Log.d("latitude", mPlace.getLatLng().latitude+"");
        Log.d("lng", mPlace.getLatLng().longitude+"");

        DecimalFormat df = new DecimalFormat("#.#######");
        String latString = df.format(mPlace.getLatLng().latitude);
        String lngString = df.format(mPlace.getLatLng().longitude);


        RequestBody name = RequestBody.create(MediaType.parse("text/plain"), mPartyName.getText().toString());
        RequestBody description = RequestBody.create(MediaType.parse("text/plain"), mPartyDescription.getText().toString());
        RequestBody lat = RequestBody.create(MediaType.parse("text/plain"), latString);
        RequestBody lng = RequestBody.create(MediaType.parse("text/plain"), lngString);
        RequestBody startDate = RequestBody.create(MediaType.parse("text/plain"), mHiddenStartDate.getText().toString());
        RequestBody endDate = RequestBody.create(MediaType.parse("text/plain"), mHiddenEndDate.getText().toString());


        retrofit2.Call<okhttp3.ResponseBody> req = service.postImage("Bearer "+accessToken, name, description, lat, lng, startDate, endDate, mImageRequestBody);
        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                finish();
            }
            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setupretrofit(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        // Change base URL to your upload server URL.
        service = new Retrofit.Builder().baseUrl("http://party-app-dev.us-west-2.elasticbeanstalk.com").client(client).build().create(Service.class);

    }

    private void setLocation(){

        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
            Log.d("ERROR", "MORE ERRORS");
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            Log.d("NEED TO ADD KEY", "ADD API KEY");
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        compositeDisposable.clear();
    }

}
