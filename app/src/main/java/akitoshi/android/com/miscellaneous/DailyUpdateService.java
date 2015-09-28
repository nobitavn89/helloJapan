package akitoshi.android.com.miscellaneous;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
/*
For daily update. Fix update time to 7h00
*/

public class DailyUpdateService extends Service {
    public static final String TAG = Service.class.toString();

    public DailyUpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "service is created");

        SharedPreferences profilePref = this.getSharedPreferences(getString(R.string.shared_pref_key), Context.MODE_PRIVATE);
        String name = profilePref.getString(getString(R.string.key_user_name),"Unknown");
        String currentLocation = profilePref.getString(getString(R.string.key_user_location), "Unknown");
        int dayArrival = profilePref.getInt(getString(R.string.key_day_arrival),25);
        int monthArrival = profilePref.getInt(getString(R.string.key_month_arrival),10);
        int yearArrival = profilePref.getInt(getString(R.string.key_year_arrival), 1989);


        //how long you have been here?
        Calendar calendar = Calendar.getInstance();
        long currentTime = calendar.getTimeInMillis();
        calendar.set(Calendar.YEAR, yearArrival);
        calendar.set(Calendar.MONTH, monthArrival);
        calendar.set(Calendar.DAY_OF_MONTH, dayArrival);

        long arrivalTime = calendar.getTimeInMillis();

        int howMany = (int) ((currentTime - arrivalTime) / (DateUtils.DAY_IN_MILLIS));

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification_views);
        remoteViews.setImageViewResource(R.id.notif_image, R.drawable.nobita);
        remoteViews.setTextViewText(R.id.notif_title, name);
        Resources res = getResources();
        String notificationString = String.format(res.getString(R.string.how_long_string_noti), currentLocation, howMany);
        remoteViews.setTextViewText(R.id.notif_text,notificationString);

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this);
        notiBuilder.setSmallIcon(R.drawable.ic_launcher);
        notiBuilder.setContent(remoteViews);


        //Create an explicit Intent
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.noti_layout, pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = notiBuilder.setOngoing(true).build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notificationManager.notify(MainActivity.NOTIFICATION_ID, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
