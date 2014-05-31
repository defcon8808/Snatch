package com.factorysoft.snatch;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fourmob.colorpicker.ColorPickerDialog;
import com.fourmob.colorpicker.ColorPickerSwatch;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;


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
    private GeoPoint geoPoint = null;
    private String Address;

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

    private GeoPoint findGeoPoint(String address) {
        Geocoder geocoder = new Geocoder(this);
        Address addr;
        GeoPoint location = null;

        try {
            List<Address> listAddress = geocoder.getFromLocationName(address, 1);

            if (listAddress.size() > 0) { // 주소값이 존재 하면
                addr = listAddress.get(0); // Address형태로
                double lat = addr.getLatitude();
                double lng = addr.getLongitude();
                location = new GeoPoint(lat, lng);

                Log.d("findGeoPoint", "주소로부터 취득한 위도 : " + lat + ", 경도 : " + lng);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return location;
    }

    private class GeoPoint {
        private double lat;
        private double lng;

        public GeoPoint(double _lat, double _lng) {
            this.lat = _lat;
            this.lng = _lng;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }

    private void showDialog(Context context) {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.locate_dialog, (ViewGroup)findViewById(R.id.root_linear));

        final EditText locate = (EditText)view.findViewById(R.id.locate);

        builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setTitle("위치 설정");
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String findAddress = locate.getText().toString();
                Address = findAddress;
                geoPoint = findGeoPoint(findAddress);
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
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

            if(geoPoint != null) {
                Intent locService = new Intent(this, FindLocateService.class);

                locService.putExtra("Latitude", geoPoint.getLat());
                locService.putExtra("Longitude", geoPoint.getLng());
                locService.putExtra("location", Address);

                startService(locService);
            }

            try {
                if(strDate == null) {
                    result = db.rawQuery("SELECT * FROM memo", null);

                    Log.d("onOptionsItemSelected1", "count : " + result.getCount());

                    while(result.moveToNext()) {
                        int ID = result.getInt(result.getColumnIndex("_id"));

                        if(ID > result.getCount()) {
                            _id = ID;
                        } else {
                            _id = ID;
                        }
                    }
                    //_id = result.getCount();

                    if(delete) {
                        _id += 1;
                        db.execSQL("INSERT INTO memo VALUES("+ _id +", '" + strTitle + "', '" + strContent + "', '" + strRgb + "', null);");
                    } else {
                        _id += 1;
                        db.execSQL("INSERT INTO memo VALUES("+ _id +", '" + strTitle + "', '" + strContent + "', '" + strRgb + "', null);");
                    }

                } else {
                    result = db.rawQuery("SELECT * FROM memo", null);

                    Log.d("onOptionsItemSelected2", "count : " + result.getCount());

                    while(result.moveToNext()) {
                        int ID = result.getInt(result.getColumnIndex("_id"));

                        if(ID > result.getCount()) {
                            _id = ID;
                        } else {
                            _id = ID;
                        }
                    }
                   // _id = result.getCount();

                    if(delete) {
                        _id += 1;
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
        } else if (id == R.id.locate_add) {
            showDialog(AddMemo.this);
        }

        return super.onOptionsItemSelected(item);
    }

}