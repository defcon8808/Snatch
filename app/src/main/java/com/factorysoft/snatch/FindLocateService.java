package com.factorysoft.snatch;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.location.Location;

import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;

import java.util.ArrayList;

public class FindLocateService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener,
        OnAddGeofencesResultListener {

    protected static final String TAG = "FindLocateService";
    //public static final String ACTION_NEW_LOCATION = "com.manuelpeinado.locationservice.action.newlocation";
    //public static final String EXTRA_LOCATION = "location";
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private Location Address = new Location("original_address");
    private ArrayList<Geofence> mList;
    private IntentFilter mFilter;
    private GeofenceEventReceiver mReceiver;
    private String address;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");

        Address.setLatitude(intent.getDoubleExtra("Latitude", 0));
        Address.setLongitude(intent.getDoubleExtra("Longitude", 0));
        address = intent.getStringExtra("location");

        mLocationClient.connect();
        registerReceiver(mReceiver, mFilter);

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        super.onCreate();

        mList = new ArrayList<Geofence>();

        if (!servicesAvailable()) {
            stopSelf();
            return;
        }

        mLocationRequest = LocationRequest.create()
                //.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5 * 60 * 1000)
                .setFastestInterval(1 * 60 * 1000);
                //.setInterval(1000)
                //.setFastestInterval(1000);

        // Register the broadcast receiver
        mFilter = new IntentFilter(GeofenceEventReceiver.GEOFENCE_EVENTS);
        mFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mReceiver = new GeofenceEventReceiver();

        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        if (mLocationClient != null && mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
            mLocationClient.disconnect();
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to Google Play Services.");
        Location lastLocation = mLocationClient.getLastLocation();
        Log.d(TAG, "Last location is: " + (lastLocation == null ? "null" : Utils.format(lastLocation)));
        mLocationClient.requestLocationUpdates(mLocationRequest, this);

        // Build a Geofence
        Geofence fence = new Geofence.Builder()
                .setRequestId("1")
                .setCircularRegion(Address.getLatitude(), Address.getLongitude(), 500)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
        mList.add(fence);

        // Method 2: Using Broadcast
        Intent intent = new Intent();
        intent.setAction(GeofenceEventReceiver.GEOFENCE_EVENTS); // Specify the action, a.k.a. receivers
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("Location", address);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Send out the Geofence request
        mLocationClient.addGeofences(mList, pendingIntent, this);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Disconnected from Google Play Services.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Connection to Google Play Services failed.");
    }

    @Override
    public void onLocationChanged(Location location) {
        double distance;
        //Toast.makeText(this, location.toString(), Toast.LENGTH_LONG).show();
        distance = AddressDistanceTo(Address, location);
        Toast.makeText(getApplicationContext(), "두 지점간 거리 : " + Math.round(distance) + "m", Toast.LENGTH_LONG).show();
    }

    private boolean servicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            Log.d(TAG, "Google Play services is available.");
            return true;
        }
        Log.e(TAG, "Google Play services NOT available.");
        return false;
    }

    private double AddressDistanceTo(Location pointA, Location pointB) {
        return pointA.distanceTo(pointB);
    }

    @Override
    public void onAddGeofencesResult(int i, String[] strings) {

    }

    // Broadcast receiver used to receive broadcast sent from the GeofenceIntentService
    public class GeofenceEventReceiver extends BroadcastReceiver {
        public static final String GEOFENCE_EVENTS = "com.factory.snatch.GeofenceEvents";

        @Override
        public void onReceive(Context context, Intent intent) {
             showNotification(context, intent);
             stopSelf();
        }
    }

    public void showNotification(Context context, Intent receiveIntent) {
        //queryDB(context, requestCode);

        Intent intent = new Intent(context, EditMemo.class);
        //intent.putExtra("id", requestCode);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("위치알림")
                        .setContentText(receiveIntent.getStringExtra("Location") + " 도착함");

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
