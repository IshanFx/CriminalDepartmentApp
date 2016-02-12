package com.example.ishanfx.departmentapp.database;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.Crime;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

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
    public Integer getType(Integer caseid){
         Crime re =  protectRMDB.where(Crime.class)
                .equalTo("caseid",caseid)
                .findFirst();
        return re.getCaseid();
    }

    public Crime getAllData(Integer caseid){
        Crime crime = protectRMDB.where(Crime.class)
                .equalTo("caseid",caseid)
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


    public void removeData(){
        RealmResults<Crime> results = protectRMDB.where(Crime.class).findAll();

        protectRMDB.beginTransaction();
        results.clear();
        protectRMDB.commitTransaction();
    }

    public void loadNewData(){

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
        queue.add(request);
      /*  CaseAsync caseAsync  = new CaseAsync();
        caseAsync.execute();*/
    }

    public class CaseAsync extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            queue = Volley.newRequestQueue(context);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, NetworkAdapter.url_getopencases, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONArray jsonArray = response.getJSONArray("opencase");
                        Number maxCaseNo  =
                                protectRMDB.where(Crime.class)
                                .max("caseid");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            if(maxCaseNo.intValue()<jsonObject.getInt("id")){
                                protectRMDB.beginTransaction();
                                Crime crime = protectRMDB.createObject(Crime.class);
                                // Set its fields
                                    crime.setCaseid(jsonObject.getInt("id"));
                                    crime.setType(jsonObject.getString("type"));
                                    crime.setDate(jsonObject.getString("date"));
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
            queue.add(request);
            return null;
        }
    }
}
