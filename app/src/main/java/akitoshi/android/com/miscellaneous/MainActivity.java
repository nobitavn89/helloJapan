package akitoshi.android.com.miscellaneous;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import static android.widget.Toast.makeText;

/*
@author: nobitavn89
 */

public class MainActivity extends Activity {
    public static final String LOG_TAG = MainActivity.class.toString();
    public static final int NOTIFICATION_ID = 101;
    Context mContext;

    private TextView mTextName, mTextCurrentLocation, mTextArrivalDate, mTextHowLong;
    private TextView mNoticeWhenBlank;
    private ImageView mProfileView;
    private String mName, mCurrentLocation, mArrivalDate, mHowLong;
    private int mDayArrival, mMonthArrival, mYearArrival;
    private int mHowMany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        if(!isProfileSet()) {
            //setContentView(R.layout.profile_settings);
            setContentView(R.layout.blank_layout);
            mNoticeWhenBlank = (TextView) findViewById(R.id.txt_blank_notice);
            getActionBar().hide();
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage(R.string.profile_setting_alert_message).setTitle(R.string.profile_setting_alert_title);
            builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startProfileSetting();
                }
            });

            builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(mContext, R.string.app_exit_noti, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            setContentView(R.layout.profile_view);
            mTextName = (TextView) findViewById(R.id.txtName);
            mTextCurrentLocation = (TextView) findViewById(R.id.txtCountry);
            mTextArrivalDate = (TextView) findViewById(R.id.txtArrivalTime);
            mTextHowLong = (TextView) findViewById(R.id.txtLivingTime);
            mProfileView = (ImageView) findViewById(R.id.img_profile_view);
            parseData();
            refreshNotification();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            startProfileSetting();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                setContentView(R.layout.profile_view);
                mTextName = (TextView) findViewById(R.id.txtName);
                mTextCurrentLocation = (TextView) findViewById(R.id.txtCountry);
                mTextArrivalDate = (TextView) findViewById(R.id.txtArrivalTime);
                mTextHowLong = (TextView) findViewById(R.id.txtLivingTime);
                mProfileView = (ImageView) findViewById(R.id.img_profile_view);
                parseData();
                refreshNotification();
            } else if ((resultCode == RESULT_CANCELED) && (!isProfileSet())) {
                setContentView(R.layout.blank_layout);
                mNoticeWhenBlank = (TextView) findViewById(R.id.txt_blank_notice);
                mNoticeWhenBlank.setText(getString(R.string.profile_empty_notice));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
        //refreshNotification();
    }

    public void refreshData() {
        if(isProfileSet()) {
            setContentView(R.layout.profile_view);
            mTextName = (TextView) findViewById(R.id.txtName);
            mTextCurrentLocation = (TextView) findViewById(R.id.txtCountry);
            mTextArrivalDate = (TextView) findViewById(R.id.txtArrivalTime);
            mTextHowLong = (TextView) findViewById(R.id.txtLivingTime);
            mProfileView = (ImageView) findViewById(R.id.img_profile_view);
            parseData();
        } else {
            setContentView(R.layout.blank_layout);
            mNoticeWhenBlank = (TextView) findViewById(R.id.txt_blank_notice);
            mNoticeWhenBlank.setText(getString(R.string.profile_empty_notice));
        }
    }

    public void refreshNotification() {
        if(isProfileSet()) {
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_views);
            remoteViews.setImageViewResource(R.id.notif_image, R.drawable.nobita);
            remoteViews.setTextViewText(R.id.notif_title, mName);
            Resources res = getResources();
            String notificationString = String.format(res.getString(R.string.how_long_string_noti), mCurrentLocation, mHowMany);
            remoteViews.setTextViewText(R.id.notif_text,notificationString);

            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(mContext);
            notiBuilder.setSmallIcon(R.drawable.ic_launcher);
            notiBuilder.setContent(remoteViews);


            //Create an explicit Intent
            Intent intent = new Intent(mContext, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(intent);
            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.noti_layout, pendingIntent);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = notiBuilder.setOngoing(true).build();
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void parseData(){
        SharedPreferences profilePref = mContext.getSharedPreferences(getString(R.string.shared_pref_key),Context.MODE_PRIVATE);
        mName = profilePref.getString(getString(R.string.key_user_name),"Unknown");
        mCurrentLocation = profilePref.getString(getString(R.string.key_user_location), "Unknown");
        mDayArrival = profilePref.getInt(getString(R.string.key_day_arrival),25);
        mMonthArrival = profilePref.getInt(getString(R.string.key_month_arrival),10);
        mYearArrival = profilePref.getInt(getString(R.string.key_year_arrival), 1989);
        mArrivalDate = Utilities.convertToDayString(mYearArrival, mMonthArrival, mDayArrival);

        //how long you have been here?
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        calendar.set(Calendar.YEAR, mYearArrival);
        calendar.set(Calendar.MONTH, mMonthArrival);
        calendar.set(Calendar.DAY_OF_MONTH, mDayArrival);

        long arrivalTime = calendar.getTimeInMillis();

        mHowMany = (int) ((currentTime - arrivalTime) / (DateUtils.DAY_IN_MILLIS));
        Log.d(LOG_TAG, "day arrival " + mDayArrival + ", monthArrival: " + mMonthArrival + "yearArrival:  " + mYearArrival);
        Log.d(LOG_TAG, "currentTime: " + currentTime + ", arrival time: " + arrivalTime + "how many: " + mHowMany);
        mTextName.setText(mName);
        mTextCurrentLocation.setText(mCurrentLocation);
        mTextArrivalDate.setText(mArrivalDate);
        Resources res = getResources();
        String howManyDays = String.format(res.getString(R.string.how_long_string), mHowMany);
        mTextHowLong.setText(howManyDays);
        //just temp
        mProfileView.setImageResource(R.drawable.nobita);

        //set alarm
        Utilities.setAlarm(MainActivity.this);

    }

    private void startProfileSetting() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("akitoshi.android.com.miscellaneous", "akitoshi.android.com.miscellaneous.ProfileSettingsActivity"));
        startActivityForResult(intent, 1);
    }

    private boolean isProfileSet () {
        //check whether the profile is set or not
        SharedPreferences pref = mContext.getSharedPreferences(getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        boolean isProfileSet = pref.getBoolean(getString(R.string.key_profile_settings_done), false);
        Log.d(LOG_TAG, "profile is set = " + isProfileSet);
        return isProfileSet;
    }
}
