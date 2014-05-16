package com.factorysoft.snatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    private AlarmReceiver alarm = new AlarmReceiver();
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private ArrayList<String> dateTime = new ArrayList<String>();
    private ArrayList <Calendar> cal = new ArrayList<Calendar>();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        dbHelper = new DBHelper(context);

        try {
            db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT time FROM memo WHERE time > strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')", null);

            while(cursor.moveToNext()) {
                Log.d("BootReceiver", "Time Compare : " + cursor.getString(0));
                dateTime.add(cursor.getString(0));
            }

            for(String _dateTime : dateTime) {
                Log.d("BootReceiver", _dateTime);
                cal.add(getCalendar(_dateTime));
            }
            /*
            for(int index=0; index < dateTime.size(); index++) {
                Log.d("BootReceiver", dateTime.get(index));
                cal.add(getCalendar(dateTime.get(index)));
            }
            */
        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }

        // an Intent broadcast.
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d("BOOT_COMPLETED", "부팅후 리시버로딩");
            //Set Alarm
            Log.d("BootReceiver", "cal size : " + cal.size());
            /*
            for(int index=0; index < cal.size(); index++) {
                alarm.setAlarm(context, cal.get(index));
            }
            */
            for(Calendar _cal : cal) {
                alarm.setAlarm(context, _cal);
            }
        }
    }

    private Calendar getCalendar(String _dateTime) {
        String[] dateTime = _dateTime.split(" ");
        String[] date = dateTime[0].split("-");
        String[] time = dateTime[1].split(":");

        if(date[1].substring(0, 1).equals("0")) {
            date[1] = date[1].substring(1);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());


        cal.set(Calendar.YEAR, Integer.parseInt(date[0]));
        cal.set(Calendar.MONTH, Integer.parseInt(date[1])-1);
        cal.set(Calendar.DATE, Integer.parseInt(date[2]));
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }
}
