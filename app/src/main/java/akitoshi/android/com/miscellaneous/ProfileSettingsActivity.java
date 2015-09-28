package akitoshi.android.com.miscellaneous;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import akitoshi.android.com.miscellaneous.countrypicker.CountryPicker;
import akitoshi.android.com.miscellaneous.countrypicker.CountryPickerListener;


public class ProfileSettingsActivity extends FragmentActivity {
    private static final String LOG_TAG = ProfileSettingsActivity.class.toString();
    private static final int DATE_DIALOG_SHOW = 1;
    private Context mContext;
    private int mDay, mMonth, mYear;
    private Button mBtnArrivalDatePicker;
    EditText mEdtUserName, mEdtCountry;
    ImageView mProfileImg;
    CountryPicker mCountryPicker;
    String mCountryName, mCountryCode;

    private LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "activity is created!");
        mCountryPicker = CountryPicker.newInstance("Select Country");
        setTitle(R.string.title_activity_profile_settings);
        setContentView(R.layout.profile_settings);

        mContext = this;

        mEdtUserName = (EditText) findViewById(R.id.edt_usr_name);
        mEdtCountry = (EditText) findViewById(R.id.edt_country);

        Button btnLocationDetect = (Button) findViewById(R.id.btn_location_detect);
        mBtnArrivalDatePicker = (Button) findViewById(R.id.btn_arrival_date_picker);
        Button btnOK = (Button) findViewById(R.id.btn_ok);
        Button btnCancel = (Button) findViewById(R.id.btn_cancel);
        mProfileImg = (ImageView) findViewById(R.id.img_profile);
        mProfileImg.setImageResource(R.drawable.nobita);
        //set current day to DatePicker
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String currentDay = Utilities.convertToDayString(year, month, day);

        final Looper looper = Looper.myLooper();
        final android.os.Handler handler = new android.os.Handler(looper);

        mBtnArrivalDatePicker.setText(currentDay);

        btnLocationDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(mContext,"This feature is coming soon, :))", Toast.LENGTH_SHORT).show();
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setPowerRequirement(Criteria.POWER_HIGH);

                final ProgressDialog locationProgressDialog = ProgressDialog.show(ProfileSettingsActivity.this, "Please wait...", "Detecting your location...");
                final LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        handler.removeCallbacksAndMessages(null);
                        if(locationProgressDialog.isShowing()) {
                            locationProgressDialog.dismiss();
                        }
                        String countryName="", countryCode="", cityName="";

                        //get location information
                        List<Address> addresses = null;
                        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if(addresses != null) {
                                countryName = addresses.get(0).getCountryName();
                                countryCode = addresses.get(0).getCountryCode();
                                cityName = addresses.get(0).getLocality();
                                Log.d(LOG_TAG, "city: " + cityName + ", countryName: " + countryName + ", countryCode: " + countryCode);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(!countryName.equals("")) {
                            StringBuilder builder = new StringBuilder();
                            builder.append(cityName).append(", ").append(countryName);
                            mCountryName = builder.toString();
                            mCountryCode = countryCode;
                            Toast.makeText(mContext, mCountryName, Toast.LENGTH_SHORT).show();
                            mEdtCountry.setText(mCountryName);
                        } else {
                            Toast.makeText(mContext, "Can not get the location!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, looper);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLocationManager.removeUpdates(locationListener);
                        if(locationProgressDialog.isShowing()) {
                            locationProgressDialog.dismiss();
                        }
                        Toast.makeText(mContext, "Can not detect the location!", Toast.LENGTH_SHORT).show();
                    }
                }, 5000);
            }
        });


        mBtnArrivalDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment dateFragment = new DatePickerFragment();
                dateFragment.show(getFragmentManager(), "start_date_picker");
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check the condition first
                if(mEdtCountry.getText().equals("") || mEdtUserName.getText().equals("")){
                    Toast.makeText(mContext,"Please fill in the blank form!", Toast.LENGTH_SHORT).show();
                } else {
                    SharedPreferences pref = mContext.getSharedPreferences(getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(getString(R.string.key_profile_settings_done),true);
                    editor.putString(getString(R.string.key_user_name), mEdtUserName.getText().toString());
                    editor.putString(getString(R.string.key_user_location), mEdtCountry.getText().toString());
                    editor.putInt(getString(R.string.key_day_arrival), mDay);
                    editor.putInt(getString(R.string.key_month_arrival), mMonth);
                    editor.putInt(getString(R.string.key_year_arrival),mYear);
                    editor.commit();
                    Toast.makeText(mContext,"Your profile is set!", Toast.LENGTH_SHORT).show();
                    //set notification

                    Utilities.setAlarm(ProfileSettingsActivity.this);
                    //return to the main activity
                    Intent returnIntent = new Intent();
                    setResult(RESULT_OK,returnIntent);
                    finish();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                setResult(RESULT_CANCELED,returnIntent);
                finish();
            }
        });

        mProfileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"This feature is coming soon, :))", Toast.LENGTH_SHORT).show();
            }
        });

        mEdtCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "select country should be shown!");
                mCountryPicker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
            }
        });

        mCountryPicker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code) {
                mCountryName =  name;
                mCountryCode = code;
                Toast.makeText(mContext, "Country selected: " + name + ", code: " + code, Toast.LENGTH_SHORT).show();
                mEdtCountry.setText(mCountryName);
                mCountryPicker.dismiss();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_settings, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, mYear, mMonth, mDay);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            mYear = year;
            mMonth = month;
            mDay = day;

            String arrivalDay = Utilities.convertToDayString(mYear, mMonth, mDay);
            Toast.makeText(mContext, "date is set: " + arrivalDay,Toast.LENGTH_SHORT).show();
            mBtnArrivalDatePicker.setText(arrivalDay);
        }
    }

}
