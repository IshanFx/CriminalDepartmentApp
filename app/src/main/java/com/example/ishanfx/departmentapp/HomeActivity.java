package com.example.ishanfx.departmentapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.database.RealMAdapter;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener {// , LocationListener need realtime track
    ListView crimeList;
    EditText txt;

    //public static Handler messageHandler = new MessageHandler();
    private static final String TAG = "BroadcastTest";
    private Intent intent;
    RequestQueue queue;
    private SwipeRefreshLayout swipeContainer;
    static RealMAdapter realMAdapter;
    ArrayAdapter<Crime> adapter;
    List<Crime> crimeLocalList;
    // DepHomeAdapter depHomeAdapter;
    CrimeAdapter depHomeAdapter;


    //Location Stuff
    private LocationManager locationManager;
    static Location mLastLocation;
    static LocationRequest mLocationRequest;
    public static GoogleApiClient mGoogleApiClient;
    public boolean isconnected = false;
    boolean startRealTimeTrack;
    static int userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            intent = new Intent(this, ProtectService.class);
            txt = (EditText) findViewById(R.id.editText);
            startRealTimeTrack = false;

            swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
            realMAdapter = new RealMAdapter(getApplicationContext());
            userId = realMAdapter.getUserId();
            locationManager =
                    (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            buildGoogleApiClient();
            createLocationRequest();
           /* List<Crime> li = new ArrayList<>();
            li.add(new Crime(1,"sss"));
            li.add(new Crime(2,"dddd"));*/
            //    crimeLocalList = realMAdapter.getAllCrimeData();
            // adapter = new CrimeAdapter(getApplicationContext(), 0, li);
            //   crimeList.setAdapter(adapter);

            List<Crime> list = realMAdapter.getDummy();

            // depHomeAdapter = new DepHomeAdapter(this, 1, list);
            depHomeAdapter = new CrimeAdapter(this, 1, list);
            crimeList = (ListView) findViewById(android.R.id.list);
            crimeList.setAdapter(depHomeAdapter);
            new DepartHomeAsync().execute();

            crimeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Crime crime = (Crime) crimeList.getItemAtPosition(position);
                    Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                    intent.putExtra("caseid", String.valueOf(crime.getCaseid()));
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
            });
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //realMAdapter.removeData();
                    new DepartHomeAsync().execute();
                }
            });

            // Configure the refreshing colors
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            unregisterReceiver(broadcastReceiver);
        }
        if (id == R.id.action_remove) {
            RealMAdapter realMAdapter = new RealMAdapter(this);
            realMAdapter.removeData();
        }
        if (id == R.id.action_track) {
          /* Track user in timely manner
          if (isconnected) {
                if (startRealTimeTrack) {
                    startRealTimeTrack = false;
                } else {

                    startRealTimeTrack = true;
                }

            }*/

            new  LastLocationInsertOnce().execute();
        }
        if (id == R.id.action_assign) {
            Intent intent = new Intent(this,AssignActivity.class);
            startActivity(intent);
        }
        if(id==R.id.action_logout){
            realMAdapter.removeUser();
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(intent);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.run)
                            .setVibrate(new long[]{1000, 1000, 1000})
                            .setContentTitle("Volume Press")
                            .setContentText("OOPS");
            Intent resultIntent = new Intent();
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(HomeActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        startService(intent);
        registerReceiver(broadcastReceiver, new IntentFilter(ProtectService.BROADCAST_ACTION));

        /*
        * Need realtime track
        * */
       /* if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }*/

    }


    private void updateUI(Intent intent) {
        String counter = intent.getStringExtra("counter");
        showNotification(counter);

    }

    /*
    * Show notification when new message came
    * */
    private void showNotification(String counter) {

        if (counter.equals("visible")) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.warning)
                            .setVibrate(new long[]{1000, 1000, 1000})
                            .setContentTitle("New Crime Happen")
                            .setContentText(counter);
            Intent resultIntent = new Intent();
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(HomeActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
            mNotificationManager.notify(1, mBuilder.build());
        }

        txt.setText(counter);
    }


//    Location Related Stuff

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        Log.d("LocCheck", "API");
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
      /*  mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);*/

        /*
        * For reltime track
        * */
        startLocationUpdates();
        this.isconnected = true;
    }

    /*
    * Only for update when click
    * */


    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        //updateLocation();
    }
    protected void startLocationUpdates() {

      LocationServices.FusedLocationApi.requestLocationUpdates(
        mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }
 /*

    private void updateLocation() {
        Log.d("OwnerInsert", "Start Track 0");
        if (startRealTimeTrack) {
            callAsynchronousTask();

            Log.d("OwnerInsert", "Start Track");
            LastLocationInsert loc = new LastLocationInsert();
            loc.execute();
           *//* if (startCycle == 1) {
                smsManage.sendSMS("Latitude:" + String.valueOf(mLastLocation.getLatitude()) + "Longitude:" + String.valueOf(mLastLocation.getLongitude()));
                startCycle += 2;
               Log.d("SMSLogin", String.valueOf(startCycle));
            }*//*
        }
    }

    protected void startLocationUpdates() {
        mLastLocation = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
       *//* mLastLocation = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);*//*
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }






    *//*
    * Update user location in timely manner
    * *//*
    public void callAsynchronousTask() {

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            LastLocationInsert performBackgroundTask = new LastLocationInsert();
                            // PerformBackgroundTask this class is the class that extends AsynchTask
                            performBackgroundTask.execute();
                            Log.d("Long", String.valueOf(mLastLocation.getLongitude()));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 50000); //execute in every 50000 ms*//*

       *//* handler.postDelayed(new Runnable() {
            public void run() {
                LastLocationInsert performBackgroundTask = new LastLocationInsert();
                // PerformBackgroundTask this class is the class that extends AsynchTask
                performBackgroundTask.execute();

                Log.d("Long",String.valueOf(mLastLocation.getLongitude()));
                handler.postDelayed(this, 80000); //now is every 2 minutes
            }
        }, 10000); //Every 120000 ms (2 minutes)*//*
    }



        @Override
        protected void onPostExecute(String txt) {
            swipeContainer.setRefreshing(false);
        }

        @Override
        protected void onProgressUpdate(Crime... values) {
            adapter.add(values[0]);
            // realMAdapter.insertData(values[0]);
            //super.onProgressUpdate(values);
        }


    }


    public class LastLocationInsert extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("OwnerInsert", "work1");
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            StringRequest request = new StringRequest(Request.Method.POST, NetworkAdapter.url_setOwnerLocation, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.d("OwnerInsert", "work2");
                       *//* JSONObject resposeJSON = new JSONObject(response);
                        if (resposeJSON.names().get(0).equals("status") ) {

                        }*//*
                    } catch (Exception ex) {

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("latitude", String.valueOf(mLastLocation.getLatitude()));
                    parameters.put("longitude", String.valueOf(mLastLocation.getLongitude()));
                    parameters.put("ownerid", String.valueOf(1));

                    return parameters;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }


    }*/

    /*
    * insert Last location when click
    * */
    public class LastLocationInsertOnce extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            Log.d("OwnerInsert", "work1");
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            StringRequest request = new StringRequest(Request.Method.POST, NetworkAdapter.url_setOwnerLocation, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.d("OwnerInsert", "work2");
                       /* JSONObject resposeJSON = new JSONObject(response);
                        if (resposeJSON.names().get(0).equals("status") ) {

                        }*/
                    } catch (Exception ex) {

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {

                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("latitude", String.valueOf(mLastLocation.getLatitude()));
                    parameters.put("longitude", String.valueOf(mLastLocation.getLongitude()));
                    parameters.put("ownerid", String.valueOf(userId));

                    return parameters;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }


    }

    public class DepartHomeAsync extends AsyncTask<Void, Crime, String> {

        ArrayAdapter<Crime> adapter;

        @Override
        protected void onPreExecute() {
            // adapter = (DepHomeAdapter) crimeList.getAdapter();
            adapter = (CrimeAdapter) crimeList.getAdapter();
            adapter.clear();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                queue = Volley.newRequestQueue(getApplicationContext());
                StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkAdapter.url_getopencases,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    Log.d("Dip", response.toString());
                                    JSONObject crimejsonObject = new JSONObject(response);
                                    JSONArray array = crimejsonObject.getJSONArray("opencase");
                                    Log.d("Dip", String.valueOf(array.length()));
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject jsonObject = array.getJSONObject(i);

                                        Crime crime = new Crime();
                                        crime.setCaseid(jsonObject.getInt("crimeid"));
                                        crime.setType(jsonObject.getString("type"));
                                        crime.setDate(jsonObject.getString("date"));
                                        crime.setStatus(jsonObject.getString("status"));
                                        crime.setLatitude(jsonObject.getString("latitude"));
                                        crime.setLongitude(jsonObject.getString("longitude"));
                                        publishProgress(crime);

                                    }
                                } catch (Exception e) {
                                    Log.d("Dip", e.toString());
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("history1", error.toString());
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Log.d("Dip", "Dip start");
                        Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put("ownerid", "1");
                        return parameters;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(stringRequest);
                return "ok";
            } catch (Exception ex) {
                return ex.getMessage();
            }

        }

        @Override
        protected void onProgressUpdate(Crime... values) {
            adapter.add(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeContainer.setRefreshing(false);
        }
    }
}
