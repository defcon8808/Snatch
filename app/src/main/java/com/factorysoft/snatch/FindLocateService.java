package com.factorysoft.snatch;

import android.app.Service;

import android.content.Intent;

import android.location.Location;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class FindLocateService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {

    protected static final String TAG = "FindLocateService";
    public static final String ACTION_NEW_LOCATION = "com.manuelpeinado.locationservice.action.newlocation";
    public static final String EXTRA_LOCATION = "location";
    private LocationClient mLocationClient;
    private LocationRequest mLocationRequest;
    private Location Address = new Location("original_address");
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

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");

        super.onCreate();

        if (!servicesAvailable()) {
            stopSelf();
            return;
        }

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                //.setInterval(15 * 60 * 1000)
                //.setFastestInterval(1 * 60 * 1000);
                .setInterval(1000)
                .setFastestInterval(1000);

        mLocationClient = new LocationClient(this, this, this);
        mLocationClient.connect();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        if (mLocationClient != null && mLocationClient.isConnected()) {
            mLocationClient.removeLocationUpdates(this);
            mLocationClient.disconnect();
        }
        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to Google Play Services.");
        Location lastLocation = mLocationClient.getLastLocation();
        Log.d(TAG, "Last location is: " + (lastLocation == null ? "null" : Utils.format(lastLocation)));
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
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
        Intent intent = new Intent(ACTION_NEW_LOCATION).putExtra(EXTRA_LOCATION, location);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        Log.d(TAG, "위도 : " + location.getLatitude() + ", 경도 : " + location.getLongitude());
        double distance;

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
}
