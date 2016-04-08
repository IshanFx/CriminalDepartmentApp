package com.example.ishanfx.departmentapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignActivity extends AppCompatActivity {
    static RealMAdapter realMAdapter;
    CrimeAdapter depHomeAdapter;
    ListView crimeList;
    RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        realMAdapter = new RealMAdapter(getApplicationContext());
        List<Crime> list = realMAdapter.getDummy();

        // depHomeAdapter = new DepHomeAdapter(this, 1, list);
        depHomeAdapter = new CrimeAdapter(this, 1, list);
        crimeList = (ListView) findViewById(android.R.id.list);
        crimeList.setAdapter(depHomeAdapter);
        new DepartHomeAsync().execute();
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
                StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkAdapter.url_getassignedcases,
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
            /*for (int i=0;i<10;i++){
                publishProgress(i);
            }*/

        }

        @Override
        protected void onPostExecute(String txt) {
            //swipeContainer.setRefreshing(false);
        }

        @Override
        protected void onProgressUpdate(Crime... values) {
            adapter.add(values[0]);

            //super.onProgressUpdate(values);
        }


    }

}
