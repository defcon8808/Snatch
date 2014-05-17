package com.factorysoft.snatch;

import android.content.DialogInterface;
import android.content.Intent;
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


public class EditMemo extends FragmentActivity {
    private EditText title, content;
    private String strTitle, strContent, strRgb, strDate;
    private LinearLayout llDiv;
    private Button btnAlarmCancel;
    private TextView tvAlarmView;
    private DBHelper helper;
    private SQLiteDatabase db;
    private AlarmDialog dialog;
    private ColorPickerDialog colorPicker;
    private int Id;
    public AlarmReceiver alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_memo);

        title = (EditText)findViewById(R.id.title);
        content = (EditText)findViewById(R.id.content);

        tvAlarmView = (TextView)findViewById(R.id.alarm_view);
        llDiv = (LinearLayout)findViewById(R.id.alarm_div);
        btnAlarmCancel = (Button)findViewById(R.id.alarm_cancel);

        alarm = new AlarmReceiver();

        dialog = new AlarmDialog(EditMemo.this);

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
                tvAlarmView.setText(null);
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

        Intent intent = getIntent();

        if(intent != null) {
            Id = intent.getIntExtra("id", 0);

            Cursor cursor;
            cursor = db.rawQuery("SELECT title, content, rgb, time FROM memo where _id="+Id, null);
            cursor.moveToNext();

            title.setText(cursor.getString(0));
            content.setText(cursor.getString(1));
            strRgb = cursor.getString(2);
            strDate = cursor.getString(cursor.getColumnIndex("time"));
            tvAlarmView.setText(cursor.getString(3));

            cursor.close();
        }

        colorPicker = ColorPickerInit();
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

        return cal;
    }

    public ColorPickerDialog ColorPickerInit() {
        final ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
        colorPickerDialog.initialize(R.string.dialog_title, new int[] { Color.CYAN, Color.LTGRAY, Color.BLACK,
                Color.BLUE, Color.GREEN, Color.MAGENTA, Color.RED, Color.GRAY, Color.YELLOW },
                Integer.parseInt(strRgb), 3, 2);
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
        getMenuInflater().inflate(R.menu.edit_memo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.memo_modify) {
            strTitle = title.getText().toString();
            strContent = content.getText().toString();

            try {
                if(strDate == null) {
                    db.execSQL("UPDATE memo SET title='" + strTitle + "', content='" + strContent
                            + "', rgb='"+strRgb+"', time=null where _id=" + Id);
                    alarm.cancelAlarm(getBaseContext());
                    //Log.d("EditMemo", "알람취소");
                } else {
                    db.execSQL("UPDATE memo SET title='" + strTitle + "', content='" + strContent
                            + "', rgb='" + strRgb + "', time=DATETIME('" + strDate + "') where _id="
                            + Id);
                    alarm.setAlarm(getBaseContext(), getCalendar(), Id);
                    //Log.d("EditMemo", "알람등록");
                }
                finish();
            } catch(SQLiteException e) {
                Log.e("EditMemo", e.toString());
            }
        } else if (id == R.id.color_picker) {
            colorPicker.show(getSupportFragmentManager(), "colorpicker");
        } else if (id == R.id.alarm_add) {
            dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
