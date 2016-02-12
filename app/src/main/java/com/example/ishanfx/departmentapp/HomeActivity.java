package com.example.ishanfx.departmentapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.database.RealMAdapter;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            intent = new Intent(this, ProtectService.class);
            txt = (EditText) findViewById(R.id.editText);

            crimeList = (ListView) findViewById(R.id.crimelistview);
            swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
            realMAdapter = new RealMAdapter(getApplicationContext());
           /* List<Crime> li = new ArrayList<>();
            li.add(new Crime(1,"sss"));
            li.add(new Crime(2,"dddd"));*/
        //    crimeLocalList = realMAdapter.getAllCrimeData();
           // adapter = new CrimeAdapter(getApplicationContext(), 0, li);
         //   crimeList.setAdapter(adapter);


            createListview();
            crimeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Integer caseid = realMAdapter.getType(Integer.parseInt(crimeList.getItemAtPosition(position).toString()));
                    Toast.makeText(getApplicationContext(), caseid.toString(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),DetailActivity.class);
                    intent.putExtra("caseid",String.valueOf(caseid));
                    startActivity(intent);
                /*Uri gmmIntentUri = Uri.parse("google.navigation:q=6.9058123,79.9095672");
               // Uri gmmIntentUri = Uri.parse("google.navigation:q=6.8727581,79.8814815(Ishan)&mode=d");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }*/

                }
            });
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    // Your code to refresh the list here.
                    // Make sure you call swipeContainer.setRefreshing(false)
                    // once the network request has completed successfully.
                    createListview();
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
    public void onResume() {
        super.onResume();
        startService(intent);
        registerReceiver(broadcastReceiver, new IntentFilter(ProtectService.BROADCAST_ACTION));
    }

    public void stop(){
        unregisterReceiver(broadcastReceiver);
    }
    private void updateUI(Intent intent) {
        String counter = intent.getStringExtra("counter");
        showNotification(counter);

    }

    private void showNotification(String counter) {

        if(counter.equals("visible")) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.location)
                            .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
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


    public void createListview(){
        adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,new ArrayList());
        crimeList.setAdapter(adapter);
        new DepartHomeAsync().execute();
        swipeContainer.setRefreshing(false);
        /*Crime weather_data[] = new Crime[]
                {
                        new Crime(R.drawable.icon_e, "Cloudy"),
                        new Crime(R.drawable.shop, "Showers"),
                        new Crime(R.drawable.icon_k, "Snow"),
                        new Crime(R.drawable.icon_k, "OOps"),
                        new Crime(R.drawable.icon_k, "Hello"),
                        new Crime(R.drawable.icon_k, "Camp"),
                        new Crime(R.drawable.icon_k, "Game"),
                };
        CrimeAdapter adapter = new CrimeAdapter(this,
                R.layout.listview_item, weather_data);
        crimeList = (ListView)findViewById(R.id.crimelist);
        crimeList.setAdapter(adapter);*/
    }

    public class DepartHomeAsync extends AsyncTask<Void,String,String> {

        ArrayAdapter<String> adapter;

        @Override
        protected void onPreExecute() {
            adapter = (ArrayAdapter<String>) crimeList.getAdapter();
            // adapter.add("ghghh");
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                queue = Volley.newRequestQueue(getApplicationContext());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, NetworkAdapter.url_getopencases ,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray array = response.getJSONArray("opencase");
                                    for (int i = 0; i < array.length(); i++) {

                                        JSONObject jsonObject = array.getJSONObject(i);
                                        publishProgress(jsonObject.getString("id"));

                                    }
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
                        });

                queue.add(jsonObjectRequest);
                return "ok";
            }
            catch (Exception ex){
                return ex.getMessage();
            }
            /*for (int i=0;i<10;i++){
                publishProgress(i);
            }*/

        }

        @Override
        protected void onPostExecute(String txt) {
            Toast.makeText(getApplicationContext(),txt,Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            adapter.add(values[0]);

            //super.onProgressUpdate(values);
        }


    }
}
