package com.example.ishanfx.departmentapp;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
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

import com.directions.route.Route;
import com.directions.route.Routing;
import com.directions.route.RouteException;
import com.directions.route.RoutingListener;

import com.example.ishanfx.departmentapp.network.NetworkAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class CrimeMapActivity extends FragmentActivity implements RoutingListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
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
    static LatLng startPoint;
    static LatLng endPoint;
    LatLng endlocation;
    static boolean isRunOneTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_map);
        setUpMapIfNeeded();
        crime = new Crime();
        crime.setLatitude( getIntent().getStringExtra("LATITUDE"));
        crime.setLongitude(getIntent().getStringExtra("LONGITUDE"));
        isRunOneTime = false;
        endlocation = new LatLng(Double.parseDouble(crime.getLatitude()), Double.parseDouble(crime.getLongitude()));
        locationManager =
                (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        buildGoogleApiClient();
        createLocationRequest();
        locationpoints = new ArrayList<>();
        callAsynchronousTask();
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
        if (!isRunOneTime){
            setUpMap();
        }

    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        Log.d("MapSet", "OK");
        if(mLastLocation!=null) {
            LatLng  testlocation = new LatLng(Double.parseDouble("6.9443083"), Double.parseDouble("79.8766223"));
        isRunOneTime = true;
        Log.d("MapSet", crime.getLatitude() + " " + crime.getLongitude());

        /*mMap.addMarker(new MarkerOptions()
                        .position(endlocation)
                        .title("crime")
        );*/
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endlocation, 15));
        Toast.makeText(this, "Lati" + crime.getLatitude() + " Long" + crime.getLongitude(), Toast.LENGTH_SHORT).show();

        startPoint = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());


        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .key("AIzaSyD5S1_sclTRhSA2crRAdGLmJ-2Vp7dBajE")
                .waypoints(testlocation, endlocation)
                .withListener(this)
                .build();
        routing.execute();


        }
        else{
            Toast.makeText(this,"Cannot get details",Toast.LENGTH_SHORT).show();
        }

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
        try {
            mMap.setMyLocationEnabled(true);
            setUpMapIfNeeded();
        }
        catch (Exception ex){
            Log.d("Map",ex.getMessage().toString());
        }
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

    @Override
    public void onRoutingFailure(RouteException e) {
         Toast.makeText(getApplicationContext(),e.getMessage().toString(),Toast.LENGTH_SHORT).show();
        Log.d("Route",e.getMessage().toString());
    }

    @Override
    public void onRoutingStart() {

    }
    protected LatLng start;
    protected LatLng end;
    private ArrayList<Polyline> polylines;
    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int i) {
        /*CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);*/
        Log.d("Route", "In sucess");
       // mMap.moveCamera(center);
        try {

           /* if (polylines.size() > 0) {
                for (Polyline poly : polylines) {
                    poly.remove();
                }
            }*/
            Log.d("Route","In sucess2");
            polylines = new ArrayList<>();
            //add route(s) to the map.
            for (int y = 0; y < route.size(); y++) {
                Log.d("Route","In sucess3");
                //In case of more than 5 alternative routes


                PolylineOptions polyOptions = new PolylineOptions();
                polyOptions.color(getResources().getColor(R.color.colorAccent));
                polyOptions.width(15);
                polyOptions.addAll(route.get(y).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylines.add(polyline);

                Toast.makeText(getApplicationContext(), "Route " + (y + 1) + ": distance - " + route.get(y).getDistanceValue() + ": duration - " + route.get(y).getDurationValue(), Toast.LENGTH_SHORT).show();
            }
            Log.d("Route","In sucess4");
            // Start marker
        MarkerOptions options = new MarkerOptions();
        options.position(startPoint);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pointstart));
        mMap.addMarker(options);
            // End marker
        options = new MarkerOptions();
        options.position(endPoint);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.pointend));
        mMap.addMarker(options);
        }
        catch (Exception e){
            Log.d("Route",e.getMessage().toString().toString());
        }
    }

    @Override
    public void onRoutingCancelled() {

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

    public class LastLocation extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("MapsTask","Run");
            /*RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, NetworkAdapter.url_getMovingLocation,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject crimeObject = response.getJSONObject("movinglocation");
                                Log.d("Maps", crimeObject.getString("latitude"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("direction", error.getMessage().toString());
                        }
                    });

            queue.add(jsonObjectRequest);*/
            return null;
        }


    }



    public void callAsynchronousTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            LastLocation performBackgroundTask = new LastLocation();
                            // PerformBackgroundTask this class is the class that extends AsynchTask
                            performBackgroundTask.execute();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 1000); //execute in every 50000 ms

    }


}
