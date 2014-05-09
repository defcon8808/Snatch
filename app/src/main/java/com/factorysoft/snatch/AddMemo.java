package com.factorysoft.snatch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.colorpicker.ColorPickerDialog;
import com.fourmob.colorpicker.ColorPickerSwatch;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class AddMemo extends FragmentActivity {
    private EditText title, content;
    private String strTitle, strContent, strRgb, strDate;
    private LinearLayout llDiv;
    private Button btnAlarmCancel;
    private TextView tvAlarmView;
    private DBHelper helper;
    private SQLiteDatabase db;
    private AlarmDialog dialog;
    private ColorPickerDialog colorPicker;
    private PendingIntent pendingIntent;
    private AlarmManager am;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_memo);

        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);
        tvAlarmView = (TextView)findViewById(R.id.alarm_view);
        llDiv = (LinearLayout)findViewById(R.id.alarm_div);
        btnAlarmCancel = (Button)findViewById(R.id.alarm_cancel);


        colorPicker = ColorPickerInit();

        dialog = new AlarmDialog(AddMemo.this);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                strDate = dialog.getStrDate();
                tvAlarmView.setText(strDate);
                llDiv.setVisibility(View.VISIBLE);

                Intent intent = new Intent(AddMemo.this, AlarmReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(AddMemo.this, 0, intent, 0);

                am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
                am.set(AlarmManager.RTC_WAKEUP, (getCalendar(strDate)).getTimeInMillis(), pendingIntent);
                //am.setRepeating(AlarmManager.RTC_WAKEUP, (getCalendar(strDate).getTimeInMillis()), 1000*60, pendingIntent);
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //Toast.makeText(getApplicationContext(), "알람설정이 취소 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        btnAlarmCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strDate = null;
                llDiv.setVisibility(View.GONE);
                if(am != null) {
                    am.cancel(pendingIntent);
                }
            }
        });

        helper = new DBHelper(this);

        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = helper.getReadableDatabase();
        }
    }

    private Calendar getCalendar(String calendar) {
        Calendar cal = Calendar.getInstance();

        String[] div = calendar.split(" ");
        String[] date = div[0].split("-");
        String[] time = div[1].split(":");

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.YEAR, AlarmDialog.YEAR);
        cal.set(Calendar.MONTH, AlarmDialog.MONTH);
        cal.set(Calendar.DATE, AlarmDialog.DAYOFMONTH);
        cal.set(Calendar.HOUR_OF_DAY, AlarmDialog.HOUR);
        cal.set(Calendar.MINUTE, AlarmDialog.MINUTE);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 00);


        //Log.d("calendar", cal.get(Calendar.YEAR) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.DATE) + " " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND));
        /*
        cal.set(Integer.parseInt(date[0]),
                Integer.parseInt(date[1]),
                Integer.parseInt(date[2]),
                Integer.parseInt(time[0]),
                Integer.parseInt(time[1]));
        cal.set(Calendar.SECOND, 0);
        */
        return cal;
    }

    public ColorPickerDialog ColorPickerInit() {
        final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
        colorPickerDialog.initialize(R.string.dialog_title, new int[] { Color.CYAN, Color.LTGRAY, Color.BLACK, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.RED, Color.GRAY, Color.YELLOW }, Color.BLACK, 3, 2);
        colorPickerDialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                strRgb = String.valueOf(color);
            }
        });

        return colorPickerDialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_memo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.memo_add) {
            strTitle = title.getText().toString();
            strContent = content.getText().toString();

            try {
                if(strDate == null) {
                    db.execSQL("INSERT INTO memo VALUES(null, '" + strTitle + "', '" + strContent + "', '" + strRgb + "', null);");
                } else {
                    db.execSQL("INSERT INTO memo VALUES(null, '" + strTitle + "', '" + strContent + "', '" + strRgb + "', DATETIME('" + strDate + "'));");
                }
                finish();
            } catch(SQLiteException e) {
                Log.e("AddMemo", e.toString());
            }
        } else if (id == R.id.color_picker) {
            colorPicker.show(getSupportFragmentManager(), "colorpicker");
        } else if (id == R.id.alarm_add) {
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

}