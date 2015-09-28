package akitoshi.android.com.miscellaneous;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import akitoshi.android.com.miscellaneous.countrypicker.AlarmUpdateReceiver;

/**
 * Created by nobitavn89 on 15/04/05.
 * reusable functions
 */
public class Utilities {

    public static final String ACTION_DAILY_UPDATE = "akitoshi.android.com.miscellaneous.ACTION_DAILY_UPDATE";
    public static final int LOCATION_REFRESH_TIME = 50;
    public static final int LOCATION_REFRESH_DISTANCE=100;

    public static String convertToDayString(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,year);
        calendar.set(Calendar.MONTH,month);
        calendar.set(Calendar.DAY_OF_MONTH,day);
        Date date = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        return dateFormat.format(date);
    }

    public static void setAlarm(Context context) {
        Intent serviceIntent = new Intent(context, AlarmUpdateReceiver.class);
        serviceIntent.setAction(Utilities.ACTION_DAILY_UPDATE);
        PendingIntent servicePendingIntent = PendingIntent.getBroadcast(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND,0);

        alarmManager.cancel(servicePendingIntent);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, servicePendingIntent);
        Log.d(MainActivity.LOG_TAG, "nobita alarm is set at: " + calendar.getTimeInMillis());
    }


}
