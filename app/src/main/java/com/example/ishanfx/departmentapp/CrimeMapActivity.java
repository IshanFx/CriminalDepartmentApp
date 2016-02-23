package com.example.ishanfx.departmentapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class CrimeMapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    LocationManager locationManager;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    RequestQueue queue;
    Integer caseid=0;
    static Crime crime;

    static float c=21,x=57;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_map);
        setUpMapIfNeeded();
        crime = new Crime();
        crime.setLatitude("6.9124745");
        crime.setLongitude("79.861436");
        locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        buildGoogleApiClient();
        createLocationRequest();
       /* mLastLocation.setLatitude(Double.parseDouble(crime.getLatitude()));
        mLastLocation.setLongitude(Double.parseDouble(crime.getLongitude()));*/
        //setUpMap();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                try{
                    setUpMap();
                 //   mMap.setMyLocationEnabled(true);
                }  catch (Exception ex){


                }
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //if(mLastLocation!=null) {
            final LatLng TutorialsPoint = new LatLng(21 , 57);
            PolylineOptions polylineOptions = new PolylineOptions()
                .add(new LatLng(21 , 57))
                .add(new LatLng(21,58));
           // mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("1"));
           // mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(crime.getLatitude()), Double.parseDouble(crime.getLongitude()))).title("1"));
           // mMap.addPolyline(polylineOptions);
           // mMap.addMarker(new MarkerOptions().position(TutorialsPoint).title("1"));
            Toast.makeText(this,"Lati"+ crime.getLatitude() + " Long" + crime.getLongitude() , Toast.LENGTH_SHORT).show();
       new Thread(new Runnable() {
            @Override
            public void run() {
                mMap.addMarker(new MarkerOptions().position(TutorialsPoint).title("6"));
            }
        }).start();


       /* }
        else{
            Toast.makeText(this,"Cannot get details",Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
      //  setUpMap();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    protected void startLocationUpdates() {
       LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public class MapAsync extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            queue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, NetworkAdapter.url_getopencases ,
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                JSONObject crimeObject = response.getJSONObject("caselocation");
                                                crime.setLongitude(crimeObject.getString("longitude"));
                                                crime.setLatitude(crimeObject.getString("latitude"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            // Handle error
                                        }
                                    }){
                                        @Override
                                        protected Map<String, String> getParams() throws AuthFailureError {

                                            Map<String, String> parameters = new HashMap<String, String>();
                                            parameters.put("caseid",String.valueOf( caseid));

                                            return parameters;
                                        }
                                    };

                            queue.add(jsonObjectRequest);
            return null;
        }
    }
}
