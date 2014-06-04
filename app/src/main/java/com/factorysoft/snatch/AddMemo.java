package com.factorysoft.snatch;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
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
import android.widget.Toast;

import com.fourmob.colorpicker.ColorPickerDialog;
import com.fourmob.colorpicker.ColorPickerSwatch;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AddMemo extends FragmentActivity {
    private EditText title, content;
    private String strTitle, strContent, strRgb, strDate;
    private int _id = 0;
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
    private String Address = null;
    // Store a list of geofences to add
    static List<Geofence> mCurrentGeofences = new ArrayList<Geofence>();

    // Add geofences handler
    private GeofenceRequester mGeofenceRequester;

    /*
     * An instance of an inner class that receives broadcasts from listeners and from the
     * IntentService that receives geofence transition events
     */
    private GeofenceSampleReceiver mBroadcastReceiver;

    // An intent filter for the broadcast receiver
    private IntentFilter mIntentFilter;

    /*
     * Internal lightweight geofence objects for geofence 1 and 2
     */
    private SimpleGeofence mGeofence1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("AddMemo", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_memo);

        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);
        tvAlarmView = (TextView)findViewById(R.id.alarm_view);
        llDiv = (LinearLayout)findViewById(R.id.alarm_div);
        btnAlarmCancel = (Button)findViewById(R.id.alarm_cancel);

        // Create a new broadcast receiver to receive updates from the listeners and service
        mBroadcastReceiver = new GeofenceSampleReceiver();

        // Create an intent filter for the broadcast receiver
        mIntentFilter = new IntentFilter();

        // Action for broadcast Intents that report successful addition of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_ADDED);

        // Action for broadcast Intents that report successful removal of geofences
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCES_REMOVED);

        // Action for broadcast Intents containing various types of geofencing errors
        mIntentFilter.addAction(GeofenceUtils.ACTION_GEOFENCE_ERROR);

        // All Location Services sample apps use this category
        mIntentFilter.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        // Instantiate the current List of geofences
        //mCurrentGeofences = new ArrayList<Geofence>();

        // Instantiate a Geofence requester
        mGeofenceRequester = new GeofenceRequester(this);

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

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            // In debug mode, log the status
            Log.d(GeofenceUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;

            // Google Play services was not available for some reason
        } else {

            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), GeofenceUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Define a Broadcast receiver that receives updates from connection listeners and
     * the geofence transition service.
     */
    public class GeofenceSampleReceiver extends BroadcastReceiver {
        /*
         * Define the required method for broadcast receivers
         * This method is invoked when a broadcast Intent triggers the receiver
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("AddMemo", "onReceive");

            // Check the action code and determine what to do
            String action = intent.getAction();

            // Intent contains information about errors in adding or removing geofences
            if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_ERROR)) {

                handleGeofenceError(context, intent);

                // Intent contains information about successful addition or removal of geofences
            } else if (
                    TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_ADDED)
                            ||
                            TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCES_REMOVED)) {

                handleGeofenceStatus(context, intent);

                // Intent contains information about a geofence transition
            } else if (TextUtils.equals(action, GeofenceUtils.ACTION_GEOFENCE_TRANSITION)) {

                handleGeofenceTransition(context, intent);

                // The Intent contained an invalid action
            } else {
                Log.e(GeofenceUtils.APPTAG, getString(R.string.invalid_action_detail, action));
                Toast.makeText(context, R.string.invalid_action, Toast.LENGTH_LONG).show();
            }
        }

        /**
         * If you want to display a UI message about adding or removing geofences, put it here.
         *
         * @param context A Context for this component
         * @param intent The received broadcast Intent
         */
        private void handleGeofenceStatus(Context context, Intent intent) {

        }

        /**
         * Report geofence transitions to the UI
         *
         * @param context A Context for this component
         * @param intent The Intent containing the transition
         */
        private void handleGeofenceTransition(Context context, Intent intent) {
            /*
             * If you want to change the UI when a transition occurs, put the code
             * here. The current design of the app uses a notification to inform the
             * user that a transition has occurred.
             */
        }

        /**
         * Report addition or removal errors to the UI, using a Toast
         *
         * @param intent A broadcast Intent sent by ReceiveTransitionsIntentService
         */
        private void handleGeofenceError(Context context, Intent intent) {
            String msg = intent.getStringExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS);
            Log.e(GeofenceUtils.APPTAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
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


        if (id == R.id.memo_add) {
            strTitle = title.getText().toString();
            strContent = content.getText().toString();

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

                    if(geoPoint != null) {
                        /*
                        * Check for Google Play services. Do this after
                        * setting the request type. If connecting to Google Play services
                        * fails, onActivityResult is eventually called, and it needs to
                        * know what type of request was in progress.
                        */
                        if (!servicesConnected()) {
                            return super.onOptionsItemSelected(item);
                        }

                        /*
                        * Create a version of geofence 1 that is "flattened" into individual fields. This
                        * allows it to be stored in SharedPreferences.
                        */
                        mGeofence1 = new SimpleGeofence(
                                String.valueOf(_id+1), //Request ID
                                // Get latitude, longitude, and radius from the UI
                                geoPoint.getLat(), // Latitude
                                geoPoint.getLng(), // Longitude
                                500f, // Radius
                                // Set the expiration time
                                Geofence.NEVER_EXPIRE, // Expiration time
                                // Only detect entry transitions
                                Geofence.GEOFENCE_TRANSITION_ENTER); // Transition state
                        Log.d("AddMemo", "Request ID : " + (_id+1));
                        /*
                        * Add Geofence objects to a List. toGeofence()
                        * creates a Location Services Geofence object from a
                        * flat object
                        */
                        mCurrentGeofences.add(mGeofence1.toGeofence());

                        // Start the request. Fail if there's already a request in progress
                        try {
                            // Try to add geofences
                            mGeofenceRequester.addGeofences(mCurrentGeofences, _id+1);
                        } catch (UnsupportedOperationException e) {
                            // Notify user that previous request hasn't finished.
                            Toast.makeText(this, R.string.add_geofences_already_requested_error,
                                    Toast.LENGTH_LONG).show();
                        }
                    } // End IF conditional

                    if(delete) {
                        _id += 1;
                        db.execSQL("INSERT INTO memo VALUES("+ _id +", '" + strTitle + "', '" + strContent + "', '" + strRgb + "', '" + Address + "', null);");
                    } else {
                        _id += 1;
                        db.execSQL("INSERT INTO memo VALUES("+ _id +", '" + strTitle + "', '" + strContent + "', '" + strRgb + "', '" + Address + "', null);");
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