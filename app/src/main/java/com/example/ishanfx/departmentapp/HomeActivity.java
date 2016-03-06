package com.example.ishanfx.departmentapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.database.RealMAdapter;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class HomeActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            intent = new Intent(this, ProtectService.class);
            txt = (EditText) findViewById(R.id.editText);


            swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
            realMAdapter = new RealMAdapter(getApplicationContext());
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
                    realMAdapter.removeData();
                    new DepartHomeAsync().execute();
                }
            });

            // Configure the refreshing colors
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }
        catch(Exception ex){
             Toast.makeText(getApplicationContext(),ex.getMessage(),Toast.LENGTH_SHORT).show();
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
        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.run)
                            .setVibrate(new long[]{1000,1000,1000})
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
    }


    private void updateUI(Intent intent) {
        String counter = intent.getStringExtra("counter");
        showNotification(counter);

    }

    /*
    * Show notification when new message came
    * */
    private void showNotification(String counter) {

        if(counter.equals("visible")) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.run)
                            .setVibrate(new long[]{1000,1000,1000})
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



    public class DepartHomeAsync extends AsyncTask<Void,Crime,String> {

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
                                    JSONObject crimejsonObject =  new JSONObject(response);
                                    JSONArray array = crimejsonObject.getJSONArray("opencase");
                                    Log.d("Dip",String.valueOf( array.length()));
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
                                Log.d("history1",error.toString());
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Log.d("Dip","Dip start");
                        Map<String, String> parameters = new HashMap<String, String>();
                        parameters.put("ownerid","1");
                        return parameters;
                    }
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                queue.add(stringRequest);
                return "ok";
            } catch (Exception ex) {
                return ex.getMessage();
            }
            /*for (int i=0;i<10;i++){
                publishProgress(i);
            }*/

        }

        @Override
        protected void onPostExecute(String txt) {
          swipeContainer.setRefreshing(false);
        }

        @Override
        protected void onProgressUpdate(Crime... values) {
            adapter.add(values[0]);
            realMAdapter.insertData(values[0]);
            //super.onProgressUpdate(values);
        }


    }


}
