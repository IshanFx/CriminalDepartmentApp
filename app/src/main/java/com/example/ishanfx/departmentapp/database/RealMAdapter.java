package com.example.ishanfx.departmentapp.database;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.Crime;
import com.example.ishanfx.departmentapp.DAO.User;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by IshanFx on 2/9/2016.
 */
public class RealMAdapter  {
    RequestQueue queue;
    static Realm protectRMDB;
    Boolean check=false;
    Context context;
    public RealMAdapter(Context context){
        this.context = context;
        protectRMDB =  Realm.getInstance(
                        new RealmConfiguration.Builder(context)
                                .name("protectmeapp.realm")
                                .schemaVersion(2) // Must be bumped when the schema changes
                                .deleteRealmIfMigrationNeeded()
                                .build()
                );

        // Create an object
    }
    public List<Crime> getDummy(){
        List<Crime> list = new ArrayList<>();
        RealmResults<Crime> results = protectRMDB.where(Crime.class).findAll();
        for (Crime s:results
                ){
            list.add(s);
            Log.d("Dummy", s.toString());
            break;
        }
        return  list;
    }

    public Integer getType(Integer caseid){
         Crime re =  protectRMDB.where(Crime.class)
                .equalTo("caseid", caseid)
                .findFirst();
        return re.getCaseid();
    }

    public Crime getAllData(Integer caseid){
        Crime crime = protectRMDB.where(Crime.class)
                .equalTo("caseid", caseid)
                .findFirst();

        return crime;
    }

    public Integer casecount(){
        Number noOfRecords =
                protectRMDB.where(Crime.class)
                        .count();
        return  noOfRecords.intValue();
    }
    public RealmResults<Crime> getAllCrimeData(){
        RealmResults<Crime> crimedata = protectRMDB.where(Crime.class)
                .findAll();

        return crimedata;
    }

    public void insertData(Crime crime){
        protectRMDB.beginTransaction();
        Crime crimescene = protectRMDB.createObject(Crime.class);
        // Set its fields
        crimescene.setCaseid(crime.getCaseid());
        crimescene.setType(crime.getType());
        crimescene.setDate(crime.getDate());
        crimescene.setStatus(crime.getStatus());
        crimescene.setLatitude(crime.getLatitude());
        crimescene.setLongitude(crime.getLongitude());
        protectRMDB.commitTransaction();
        Log.d("Dummy", String.valueOf(crime.getCaseid()));
    }

    public void removeData(){
        RealmResults<Crime> results = protectRMDB.where(Crime.class).findAll();

        protectRMDB.beginTransaction();
        results.clear();
        protectRMDB.commitTransaction();
    }

    public void loadNewData(){
        removeData();
        new CaseAsync().execute();
/*
            queue = Volley.newRequestQueue(context);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, NetworkAdapter.url_getopencases, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("opencase");
                    Integer localcasecount  = casecount();
                    Integer remotecasecount =  jsonArray.length();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            RealmResults<Crime> checklocalData = protectRMDB.where(Crime.class)
                                    .equalTo("caseid",jsonObject.getInt("id"))
                                    .findAll();
                            if(checklocalData.isEmpty()){

                                protectRMDB.beginTransaction();
                                Crime crime = protectRMDB.createObject(Crime.class);
                                // Set its fields
                                crime.setCaseid(jsonObject.getInt("id"));
                                crime.setType(jsonObject.getString("type"));
                                crime.setDate(jsonObject.getString("date"));
                                crime.setStatus(jsonObject.getString("status"));
                                crime.setLatitude(jsonObject.getString("latitude"));
                                crime.setLongitude(jsonObject.getString("longitude"));
                                protectRMDB.commitTransaction();
                            }
                    }

                    response.getString("casecount");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // Toast.makeText(MainActivity.this, "Responce error", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);*/
      /*  CaseAsync caseAsync  = new CaseAsync();
        caseAsync.execute();*/
    }

    public boolean checkSecondLogin() {
        User results = protectRMDB.where(User.class).findFirst();
        if (results == null) {
            Log.d("Realm", "local user Not found");
            return false;

        } else {
            Log.d("Realm", "local user found");
            return true;
        }
    }

    public void removeUser() {
        RealmResults<User> results = protectRMDB.where(User.class).findAll();
        protectRMDB.beginTransaction();
        results.clear();
        Log.d("Realm", "Remove Sucess "+results.size() );
        protectRMDB.commitTransaction();
    }

    public void insertUser(User user) {
        protectRMDB.beginTransaction();
        User userObject = protectRMDB.createObject(User.class);
        userObject.setId(user.getId());
        //userObject.setEmail(user.getEmail());
        //userObject.setPassword(user.getPassword());
        protectRMDB.commitTransaction();
        Log.d("Realm", "Insert Success " + user.getId());
    }
    public class CaseAsync extends AsyncTask<Void,Crime,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            synchronized (this) {
                queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.POST, NetworkAdapter.url_getopencases, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject crimejsonObject =  new JSONObject(response);
                            JSONArray jsonArray = crimejsonObject.getJSONArray("opencase");
                           /* Integer localcasecount = casecount();
                            Integer remotecasecount = jsonArray.length();

                            if(localcasecount<=remotecasecount){
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Crime crime = new Crime();
                                    crime.setCaseid(jsonObject.getInt("crimeid"));
                                    crime.setType(jsonObject.getString("type"));
                                    crime.setDate(jsonObject.getString("date"));
                                    crime.setStatus(jsonObject.getString("status"));
                                    crime.setLatitude(jsonObject.getString("latitude"));
                                    crime.setLongitude(jsonObject.getString("longitude"));
                                    insertData(crime);
                                }
                            }*/
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                RealmResults<Crime> checklocalData = protectRMDB.where(Crime.class)
                                        .equalTo("caseid", jsonObject.getInt("crimeid"))
                                        .findAll();

                                if (checklocalData.isEmpty()) {
                                    protectRMDB.beginTransaction();
                                    Crime crime = protectRMDB.createObject(Crime.class);
                                    // Set its fields
                                    crime.setCaseid(jsonObject.getInt("crimeid"));
                                    crime.setType(jsonObject.getString("type"));
                                    crime.setDate(jsonObject.getString("date"));
                                    crime.setStatus(jsonObject.getString("status"));
                                    crime.setLatitude(jsonObject.getString("latitude"));
                                    crime.setLongitude(jsonObject.getString("longitude"));
                                    protectRMDB.commitTransaction();
                                }
                            }

                            //response.getString("casecount");
                        } catch (Exception e) {

                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

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
                queue.add(stringRequest);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Crime... values) {

        }
    }

    public Integer getUserId(){
        User results = protectRMDB.where(User.class).findFirst();
        return  results.getId();
    }
}
