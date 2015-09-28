package akitoshi.android.com.miscellaneous.countrypicker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

import akitoshi.android.com.miscellaneous.MainActivity;
import akitoshi.android.com.miscellaneous.R;

/**
 * Created by nobitavn89 on 15/04/18.
 */
public class AlarmUpdateReceiver extends BroadcastReceiver{

    private static final String DEBUG_TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(DEBUG_TAG, "OnReceive, String Intent:" + intent.getAction().toString());
        String actions = intent.getAction();
        SharedPreferences profilePref = context.getSharedPreferences(context.getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        boolean isProfileset = profilePref.getBoolean(context.getString(R.string.key_profile_settings_done), false);
        Log.d(DEBUG_TAG, "isProfile set: " + isProfileset);
        String name = profilePref.getString(context.getString(R.string.key_user_name),"Unknown");
        String currentLocation = profilePref.getString(context.getString(R.string.key_user_location), "Unknown");
        int dayArrival = profilePref.getInt(context.getString(R.string.key_day_arrival),25);
        int monthArrival = profilePref.getInt(context.getString(R.string.key_month_arrival),10);
        int yearArrival = profilePref.getInt(context.getString(R.string.key_year_arrival), 1989);


        //how long you have been here?
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        calendar.set(Calendar.YEAR, yearArrival);
        calendar.set(Calendar.MONTH, monthArrival);
        calendar.set(Calendar.DAY_OF_MONTH, dayArrival);

        long arrivalTime = calendar.getTimeInMillis();

        int howMany = (int) ((currentTime - arrivalTime) / (DateUtils.DAY_IN_MILLIS));

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_views);
        remoteViews.setImageViewResource(R.id.notif_image, R.drawable.nobita);
        remoteViews.setTextViewText(R.id.notif_title, name);
        Resources res = context.getResources();
        String notificationString = String.format(res.getString(R.string.how_long_string_noti), currentLocation, howMany);
        remoteViews.setTextViewText(R.id.notif_text,notificationString);

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context);
        notiBuilder.setSmallIcon(R.drawable.ic_launcher);
        notiBuilder.setContent(remoteViews);


        //Create an explicit Intent
        Intent intentMain = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intentMain);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noti_layout, pendingIntent);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = notiBuilder.setOngoing(true).build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(MainActivity.NOTIFICATION_ID, notification);
    }
}
