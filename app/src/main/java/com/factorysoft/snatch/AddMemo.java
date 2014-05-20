package com.factorysoft.snatch;


import android.content.DialogInterface;

import android.database.Cursor;
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
import com.fourmob.colorpicker.ColorPickerDialog;
import com.fourmob.colorpicker.ColorPickerSwatch;
import java.util.Calendar;


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
    public AlarmReceiver alarm;
    public static Boolean delete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_memo);

        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);
        tvAlarmView = (TextView)findViewById(R.id.alarm_view);
        llDiv = (LinearLayout)findViewById(R.id.alarm_div);
        btnAlarmCancel = (Button)findViewById(R.id.alarm_cancel);


        alarm = new AlarmReceiver();

        colorPicker = ColorPickerInit();

        dialog = new AlarmDialog(AddMemo.this);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                strDate = dialog.getStrDate();
                tvAlarmView.setText(strDate);
                llDiv.setVisibility(View.VISIBLE);

            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                //Set OnCancel Code
            }
        });

        btnAlarmCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                strDate = null;
                llDiv.setVisibility(View.GONE);
            }
        });

        helper = new DBHelper(this);

        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = helper.getReadableDatabase();
        }
    }

    private Calendar getCalendar() {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.YEAR, AlarmDialog.YEAR);
        cal.set(Calendar.MONTH, AlarmDialog.MONTH);
        cal.set(Calendar.DATE, AlarmDialog.DAYOFMONTH);
        cal.set(Calendar.HOUR_OF_DAY, AlarmDialog.HOUR);
        cal.set(Calendar.MINUTE, AlarmDialog.MINUTE);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        //Log.d("AddMemo", "cal set dateTime : " + AlarmDialog.YEAR + "/" + AlarmDialog.MONTH + "/" + AlarmDialog.DAYOFMONTH + " " + AlarmDialog.HOUR + ":" + AlarmDialog.MINUTE + ":00");
        return cal;
    }

    public ColorPickerDialog ColorPickerInit() {
        final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();

        colorPickerDialog.initialize(R.string.dialog_title, new int[] { Color.CYAN, Color.LTGRAY, Color.BLACK, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.RED, Color.GRAY, Color.YELLOW }, Color.BLACK, 3, 2);

        if(strRgb == null) {
            strRgb = String.valueOf(Color.BLACK);
        }
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
        Cursor result;
        int _id = 0;

        if (id == R.id.memo_add) {
            strTitle = title.getText().toString();
            strContent = content.getText().toString();


            try {
                if(strDate == null) {
                    result = db.rawQuery("SELECT * FROM memo", null);

                    Log.d("onOptionsItemSelected", "count : " + result.getCount());

                    _id = result.getCount();

                    if(delete) {
                        db.execSQL("INSERT INTO memo VALUES("+ _id +", '" + strTitle + "', '" + strContent + "', '" + strRgb + "', null);");
                    } else {
                        _id += 1;
                        db.execSQL("INSERT INTO memo VALUES("+ _id +", '" + strTitle + "', '" + strContent + "', '" + strRgb + "', null);");
                    }

                } else {
                    result = db.rawQuery("SELECT * FROM memo", null);

                    //Log.d("onOptionsItemSelected", "count : " + result.getCount());

                    _id = result.getCount();

                    if(delete) {
                        db.execSQL("INSERT INTO memo VALUES("+ _id + ", '" + strTitle + "', '" + strContent + "', '" + strRgb + "', DATETIME('" + strDate + "'));");
                    } else {
                        _id += 1;
                        db.execSQL("INSERT INTO memo VALUES("+ _id + ", '" + strTitle + "', '" + strContent + "', '" + strRgb + "', DATETIME('" + strDate + "'));");
                    }

                    alarm.setAlarm(getBaseContext(), getCalendar(), _id);

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