package com.example.ishanfx.departmentapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignActivity extends AppCompatActivity {
    static RealMAdapter realMAdapter;
    CrimeAdapter depHomeAdapter;
    ListView crimeList;
    RequestQueue queue;
    Crime finishcrime;
    private SwipeRefreshLayout swipeContainer;
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
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        realMAdapter = new RealMAdapter(getApplicationContext());
        List<Crime> list = realMAdapter.getDummy();

        // depHomeAdapter = new DepHomeAdapter(this, 1, list);
        depHomeAdapter = new CrimeAdapter(this, 1, list);
        crimeList = (ListView) findViewById(android.R.id.list);
        crimeList.setAdapter(depHomeAdapter);
        new DepartHomeAsync().execute();
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

        crimeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                finishcrime = (Crime) crimeList.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(AssignActivity.this);
                builder.setTitle("Close case");

                builder.setMessage("Are your sure to close this case");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        new closeAssignTaskAsync().execute();

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();
            }
        });
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
            swipeContainer.setRefreshing(false);
        }

        @Override
        protected void onProgressUpdate(Crime... values) {
            adapter.add(values[0]);

            //super.onProgressUpdate(values);
        }


    }


    class closeAssignTaskAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest request = new StringRequest(Request.Method.POST, NetworkAdapter.url_setFinished, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject resposeJSON = new JSONObject(response);
                        if (resposeJSON.names().get(0).equals("status") ) {

                        }
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
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("caseid", String.valueOf(finishcrime.getCaseid()));

                    return parameters;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            Toast.makeText(getApplicationContext(), "Successfully Added", Toast.LENGTH_SHORT).show();

        }
    }

}
