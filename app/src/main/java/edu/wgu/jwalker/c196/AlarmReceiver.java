package edu.wgu.jwalker.c196;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

//class to receive alarm for notifications
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        //create notifcation and relevant text fields
        createNotification(context, "WGU Term Tracker", "Date alert! Check your courses and assessments!", "Alert");
    }

    //method to create notification
    public void createNotification(Context context, String msg, String msgText, String msgAlert) {

        //send user to main activity onclick
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        //build notification
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.wgulogosmall)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);     //use default notification sound
        mBuilder.setAutoCancel(true);                               //erase notification on click
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
    }
}
