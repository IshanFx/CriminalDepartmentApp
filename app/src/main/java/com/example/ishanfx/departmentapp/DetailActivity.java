package com.example.ishanfx.departmentapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.Handler.VariableManager;
import com.example.ishanfx.departmentapp.database.RealMAdapter;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    TextView txtCaseID, txtType, txtDate, txtStatus, txtLongitude, txtLatitude, txtTime;
    Button btnFollowMap, btnAssign,btnCloseCase;
    String caseid = "0";
    View btn_select;
    static Crime crime = new Crime();
    static String  CRIME_TYPE = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        caseid = getIntent().getStringExtra("caseid");

        btnCloseCase = (Button) findViewById(R.id.btnCloseCase);
        btnAssign = (Button) findViewById(R.id.btnAssign);
        txtCaseID = (TextView) findViewById(R.id.txtCaseID);
        txtType = (TextView) findViewById(R.id.txtType);
        txtDate = (TextView) findViewById(R.id.txtDate);
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtTime = (TextView) findViewById(R.id.txtTime);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        btnCloseCase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_select = v;
                new CloseAssignTaskAsync().execute();
            }
        });
        actionBar.setDisplayHomeAsUpEnabled(true);
        loadData();
        //getCrimeData();
        //new LoadOneTaskAsync().execute();
       // getCrimeData(Integer.parseInt(caseid),activityType);

    }

   /* private void getCrimeData(Integer caseid,String activityType) {
        String caseType = "";
        String status = "";
        RealMAdapter realMAdapter = new RealMAdapter(getApplicationContext());
        Crime crime = realMAdapter.getAllData(caseid);
        String dateTime = new String(crime.getDate());
        String dateTimeArray[] = dateTime.split("\\s+");
        switch (crime.getType()){
            case "E":
                caseType="Evidence";
                break;
            case "R":
                caseType = "Robbery";
                break;
            case "K":
                caseType = "Kidnap";
                break;
        }
        txtCaseID.setText(String.valueOf(crime.getCaseid()));
        txtType.setText(caseType);
        switch (crime.getStatus()){
            case "P":
                status = "Pending";
                break;
            case "A":
                status = "Assigned";
                break;

        }
        if(activityType.equals("A")){
            status = "Assigned";
        }
        txtStatus.setText(status.toString());
        txtDate.setText(dateTimeArray[0].toString());
        txtTime.setText(dateTimeArray[1].toString());
        txtLongitude.setText(crime.getLongitude());
        txtLatitude.setText(crime.getLatitude());
        this.caseid = crime.getCaseid();
    }*/

    private void getCrimeData(Crime crime) {
        String caseType = "";
        String status = "";
        Log.d("crime",crime.getDate().toString());
        String dateTime = new String(crime.getDate());
        String dateTimeArray[] = dateTime.split("\\s+");
        CRIME_TYPE = crime.getType();
        switch (crime.getType()){
            case "E":
                caseType="Evidence";
                break;
            case "R":
                caseType = "Robbery";
                break;
            case "K":
                caseType = "Kidnap";
                break;
        }
        txtCaseID.setText(String.valueOf(crime.getCaseid()));
        txtType.setText(caseType);
        switch (crime.getStatus()){
            case "P":
                status = "Pending";
                break;
            case "A":
                status = "Assigned";
                break;
        }
        if(crime.getStatus().equals("A")){
            btnAssign.setVisibility(View.INVISIBLE);
        }

        txtStatus.setText(status.toString());
        txtDate.setText(dateTimeArray[0].toString());
        txtTime.setText(dateTimeArray[1].toString());
        txtLongitude.setText(crime.getLongitude());
        txtLatitude.setText(crime.getLatitude());

    }

    public synchronized void loadData(){
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, NetworkAdapter.url_getonetask, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject crimejsonObject = new JSONObject(response);
                    Log.d("crime",crimejsonObject.toString());
                    JSONArray array = crimejsonObject.getJSONArray("case");
                    crimejsonObject = array.getJSONObject(0);
                    Log.d("crime", crimejsonObject.toString());

                    crime.setCaseid(crimejsonObject.getInt("crimeid"));
                    crime.setType(crimejsonObject.getString("type"));
                    crime.setDate(crimejsonObject.getString("date"));
                    crime.setStatus(crimejsonObject.getString("status"));
                    crime.setLatitude(crimejsonObject.getString("latitude"));
                    crime.setLongitude(crimejsonObject.getString("longitude"));
                    getCrimeData(crime);

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
                parameters.put("caseid",caseid);
                return parameters;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    public void showMap(View view) {
        Intent intent = new Intent(this, CrimeMapActivity.class);
        intent.putExtra("LATITUDE",txtLatitude.getText());
        intent.putExtra("LONGITUDE",txtLongitude.getText());
        intent.putExtra("TYPE",CRIME_TYPE);
        intent.putExtra("CASEID",caseid);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    public void ownerAssign(View view) {
        new DetailAsync().execute();
        txtStatus.setText("Assigned");
    }

    public void showNavigation(View view) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+Double.parseDouble(txtLatitude.getText().toString())+","+Double.parseDouble(txtLongitude.getText().toString()));

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    class DetailAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest request = new StringRequest(Request.Method.POST, NetworkAdapter.url_setAssign, new Response.Listener<String>() {
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
                    parameters.put("caseid", String.valueOf(caseid));

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    class CloseAssignTaskAsync extends AsyncTask<Void, Void, Void> {

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
                    Map<String, String> parameters = new HashMap<String, String>();
                    parameters.put("caseid", String.valueOf(caseid));
                    return parameters;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
           new VariableManager().customeToast(btn_select,"Task Closed",1);
        }
    }

    class LoadOneTaskAsync extends AsyncTask<Void, Crime, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest request = new StringRequest(Request.Method.POST, NetworkAdapter.url_getonetask, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject crimejsonObject = new JSONObject(response);
                        Log.d("crime",crimejsonObject.toString());
                        JSONArray array = crimejsonObject.getJSONArray("case");
                        crimejsonObject = array.getJSONObject(0);
                        Log.d("crime",crimejsonObject.toString());
                        crime.setCaseid(crimejsonObject.getInt("crimeid"));
                        crime.setType(crimejsonObject.getString("type"));
                        crime.setDate(crimejsonObject.getString("date"));
                        crime.setStatus(crimejsonObject.getString("status"));
                        crime.setLatitude(crimejsonObject.getString("latitude"));
                        crime.setLongitude(crimejsonObject.getString("longitude"));
                        publishProgress(crime);
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
                    parameters.put("caseid",caseid);
                    return parameters;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }

        @Override
        protected void onProgressUpdate(Crime... values) {

        }

        @Override
        protected void onPostExecute(Void aVoid) {

           //new VariableManager().customeToast(btn_select, "Task Closed", 1);

              //  getCrimeData();

        }
    }
}
