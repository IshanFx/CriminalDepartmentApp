package com.example.ishanfx.departmentapp;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import com.example.ishanfx.departmentapp.database.RealMAdapter;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {
    TextView txtCaseID,txtType,txtDate,txtStatus,txtLongitude,txtLatitude;
    Button btnFollowMap,btnAssign;
    Integer caseid=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String caseid =  getIntent().getStringExtra("caseid");

        txtCaseID = (TextView) findViewById(R.id.txtCaseID);
        txtType   = (TextView) findViewById(R.id.txtType);
        txtDate   = (TextView) findViewById(R.id.txtDate);
        txtLatitude = (TextView) findViewById(R.id.txtLatitude);
        txtLongitude = (TextView) findViewById(R.id.txtLongitude);
        txtStatus = (TextView) findViewById(R.id.txtStatus);


        getCrimeData(Integer.parseInt(caseid));

    }

    private void getCrimeData(Integer caseid) {
        RealMAdapter realMAdapter = new RealMAdapter(getApplicationContext());
        Crime crime = realMAdapter.getAllData(caseid);

        txtCaseID.setText(String.valueOf(crime.getCaseid()));
        txtType.setText(crime.getType());
        txtStatus.setText(crime.getStatus());
        txtLongitude.setText(crime.getLongitude());
        txtLatitude.setText(crime.getLatitude());
        this.caseid = crime.getCaseid();
    }

    public void showMap(View view) {
        Intent intent = new Intent(this,CrimeMapActivity.class);
        startActivity(intent);
           /*     Uri gmmIntentUri = Uri.parse("google.navigation:q="+txtLatitude.getText().toString()+","+txtLongitude.getText().toString() );

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }*/
    }

    public void ownerAssign(View view) {
        new DetailAsync().execute();
    }

    class  DetailAsync extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            StringRequest request = new StringRequest(Request.Method.POST, NetworkAdapter.url_setAssign, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try{
                        JSONObject resposeJSON = new JSONObject(response);
                        if(resposeJSON.names().get(0).equals("status")){

                        }
                    }
                    catch(Exception ex){

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
                    parameters.put("caseid",String.valueOf( caseid));

                    return parameters;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 2, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            queue.add(request);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

             Toast.makeText(getApplicationContext(),"Successfully Added",Toast.LENGTH_SHORT).show();

        }
    }
}
