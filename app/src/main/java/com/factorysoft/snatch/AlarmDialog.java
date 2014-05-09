package com.factorysoft.snatch;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by defcon-Dev on 2014-05-04.
 */
public class AlarmDialog extends Dialog {
    private Button set_date, set_time, confirm, cancel;
    private DatePicker date_picker;
    private TimePicker time_picker;
    public static int YEAR, MONTH, DAYOFMONTH, HOUR, MINUTE;
    private String strDate;

    public AlarmDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_dialog);

        set_date = (Button)findViewById(R.id.set_date);
        set_time = (Button)findViewById(R.id.set_time);
        confirm = (Button)findViewById(R.id.confirm);
        cancel = (Button)findViewById(R.id.cancel);
        date_picker = (DatePicker)findViewById(R.id.date_picker);
        time_picker = (TimePicker)findViewById(R.id.time_picker);

        set_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date_picker.setVisibility(View.VISIBLE);
                time_picker.setVisibility(View.GONE);
            }
        });

        set_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                date_picker.setVisibility(View.GONE);
                time_picker.setVisibility(View.VISIBLE);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SimpleDateFormat dateViewFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:00", Locale.KOREA);
                Calendar choosenDate = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());

                YEAR = date_picker.getYear();
                MONTH = date_picker.getMonth();
                DAYOFMONTH = date_picker.getDayOfMonth();
                HOUR = time_picker.getCurrentHour();
                MINUTE = time_picker.getCurrentMinute();

                choosenDate.set(YEAR, MONTH, DAYOFMONTH, HOUR, MINUTE);
                setStrDate(dateViewFormatter.format(choosenDate.getTime()).toString());
                //Log.d("Date_Picker", dateViewFormatter.format(choosenDate.getTime()).toString());

                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    public String getStrDate() {
        return strDate;
    }

    public void setStrDate(String strDate) {
        this.strDate = strDate;
    }
}
