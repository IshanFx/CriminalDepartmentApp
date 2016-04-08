package com.example.ishanfx.departmentapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduledService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener
{

    final  class threadClass implements  Runnable{

        int service_id;
        public threadClass(int service_id){
            this.service_id = service_id;
        }

        @Override
        public void run() {

        }
    }

    private LocationManager locationManager;
    static Location mLastLocation;
    static LocationRequest mLocationRequest;
    public static GoogleApiClient mGoogleApiClient;
    public boolean isconnected = false;
    boolean startRealTimeTrack;


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        Log.d("LocCheck","API");
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Log.d("LocCheck", "API2");


    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("LocCheck", "API3");
        startLocationUpdates();
        this.isconnected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return 0;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Log.d("OwnerInsert",String.valueOf( mLastLocation.getLatitude()));
        updateLocation();
    }

    private void updateLocation() {
        Log.d("OwnerInsert", "Start Track 0");
        Log.d("OwnerInsert", String.valueOf(mLastLocation.getLatitude()));
           /* if (startCycle == 1) {
                smsManage.sendSMS("Latitude:" + String.valueOf(mLastLocation.getLatitude()) + "Longitude:" + String.valueOf(mLastLocation.getLongitude()));
                startCycle += 2;
               Log.d("SMSLogin", String.valueOf(startCycle));
            }*/

    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d("OwnerInsert", String.valueOf(mLastLocation.getLatitude()));

    }
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        buildGoogleApiClient();
        createLocationRequest();
    }
}