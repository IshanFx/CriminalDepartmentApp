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
import android.util.Log;
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
import org.json.JSONStringer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.internal.android.JsonUtils;

public class CrimeMapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    LocationManager locationManager;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    RequestQueue queue;
    Integer caseid = 0;
    static Crime crime;
    private static final double DEFAULT_PRECISION = 1E5;
    static float c = 21, x = 57;
    static List<Point> locationpoints;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_map);
        setUpMapIfNeeded();
        crime = new Crime();

        crime.setLatitude( getIntent().getStringExtra("LATITUDE"));
        crime.setLongitude(getIntent().getStringExtra("LONGITUDE"));
        locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        buildGoogleApiClient();
        createLocationRequest();
       /* mLastLocation.setLatitude(Double.parseDouble(crime.getLatitude()));
        mLastLocation.setLongitude(Double.parseDouble(crime.getLongitude()));*/
        //setUpMap();
        locationpoints = new ArrayList<>();
       // new DirectionAsync().execute();
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
                try {
                    setUpMap();
                    //   mMap.setMyLocationEnabled(true);
                } catch (Exception ex) {


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
        final LatLng TutorialsPoint = new LatLng(Double.parseDouble(crime.getLatitude()), Double.parseDouble(crime.getLongitude()));
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(new LatLng(21, 57))
                .add(new LatLng(21, 58));
        mMap.addMarker(new MarkerOptions()
                .position(TutorialsPoint)
                .title("crime")
                );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TutorialsPoint, 15));
        // mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(crime.getLatitude()), Double.parseDouble(crime.getLongitude()))).title("1"));
        // mMap.addPolyline(polylineOptions);
        // mMap.addMarker(new MarkerOptions().position(TutorialsPoint).title("1"));
        Toast.makeText(this, "Lati" + crime.getLatitude() + " Long" + crime.getLongitude(), Toast.LENGTH_SHORT).show();



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
        setUpMap();
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

    public class MapAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            queue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, NetworkAdapter.url_getopencases,
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
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("caseid", String.valueOf(caseid));

                    return parameters;
                }
            };

            queue.add(jsonObjectRequest);
            return null;
        }
    }

    public static List<Point> decode(String encoded) {
        return decode(encoded, DEFAULT_PRECISION);
    }

    public static List<Point> decode(String encoded, double precision) {

        int index = 0;
        int lat = 0, lng = 0;

        while (index < encoded.length()) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            Point p = new Point((double) lat / precision, (double) lng / precision);
            Log.d("Decode", p.getLatitude() + " " + p.getLongitude());
            locationpoints .add(p);
        }
        return locationpoints;
    }

    public class DirectionAsync extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, NetworkAdapter.url_direction,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject jsonObject = new JSONObject();
                               JSONArray routes = response.getJSONArray("routes");
                                Log.d("direction", "1 ok");
                               // jsonObject.put("routesdata",);
                                JSONArray arr2 = jsonObject.getJSONArray("routesdata");
                               // JSONObject o = routes.getJSONObject(0).toJSONArray("array");
                                Log.d("direction", routes.get(0).toString());

                                Log.d("direction", "2 ok");
                             //   JSONArray legs =  ob.getJSONArray("legs");

                                  //  Log.d("direction",routes. .get(1).toString());
                             /*  JSONArray  stepsArray = legs.getJSONArray("steps");
                                Log.d("direction", "3");
                                for(int c=0;c<stepsArray.length();c++){
                                    JSONObject step = stepsArray.getJSONObject(c);
                                    JSONObject polyine =  step.getJSONObject("polyline");
                                    String points =  polyine.getString("points");
                                    publishProgress(points);
                                }*/
                            } catch (Exception e) {
                                Log.d("direction", e.getMessage().toString());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("direction", error.getMessage().toString());
                        }
                    });

            queue.add(jsonObjectRequest);
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            decode(values[0]);
        }
    }
}
