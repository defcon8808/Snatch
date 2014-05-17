package com.factorysoft.snatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;


import java.util.ArrayList;
import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {
    private AlarmReceiver alarm = new AlarmReceiver();
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private ArrayList<String> dateTime = new ArrayList<String>();
    private ArrayList <Calendar> cal = new ArrayList<Calendar>();
    private ArrayList<Integer> _id = new ArrayList<Integer>();
    private ArrayList<Register> reg = new ArrayList<Register>();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        dbHelper = new DBHelper(context);

        try {
            db = dbHelper.getWritableDatabase();
            Cursor cursor = db.rawQuery("SELECT _id, time FROM memo WHERE time > " +
                    "strftime('%Y-%m-%d %H:%M:%S', 'now', 'localtime')", null);

            while(cursor.moveToNext()) {
                _id.add(cursor.getInt(cursor.getColumnIndex("_id")));
                dateTime.add(cursor.getString(cursor.getColumnIndex("time")));
            }

            for(String _dateTime : dateTime) {
                cal.add(getCalendar(_dateTime));
            }

            int index=0;
            while(index < _id.size()) {
                reg.add(new Register(_id.get(index), cal.get(index)));
                index++;
            }

            /*
                For Debugging Code

            for(Register _reg : reg) {
                Log.d("BootReceiver", "ID : " + _reg.getId() + ", Calendar : " +
                        _reg.getCal().getTime());
            }
            */

        } catch (SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }

        // an Intent broadcast.
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Set Alarm
            for(Register _reg : reg) {
                alarm.setAlarm(context, _reg.getCal(), _reg.getId());
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

    private class Register {
        private int id;
        private Calendar cal;

        private Register(int _id, Calendar _cal) {
            this.id = _id;
            this.cal = _cal;
        }

        public int getId() {
            return id;
        }

        public Calendar getCal() {
            return cal;
        }
    }
}
