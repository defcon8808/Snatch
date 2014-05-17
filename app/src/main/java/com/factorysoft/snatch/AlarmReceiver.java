package com.factorysoft.snatch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Vibrator;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmReceiver extends BroadcastReceiver {
    static AlarmManager alarmMgr;
    static PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Toast.makeText(context, "알람울림", Toast.LENGTH_LONG).show();
        /*
            이 사이에 Notification bar 에 띄우는 코드 삽입.

         */
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }

    public void setAlarm(Context context, Calendar cal, int requestCode) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, AlarmReceiver.class);

        alarmIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);

        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelAlarm(Context context) {
        if(alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }

        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}