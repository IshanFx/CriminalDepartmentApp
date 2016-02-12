package com.example.ishanfx.departmentapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.database.RealMAdapter;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by IshanFx on 2/5/2016.
 */
public class ProtectService extends Service {
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.example.ishanfx.departmentapp";
    private final Handler handler = new Handler();
    Intent intent;
    int counter = 0;
    RequestQueue queue;
    static int localCaseCount;
    static int remoteCaseCount;
    public static int c = 1;
    public String msg = "ss";

    @Override
    public void onCreate() {
        intent = new Intent(BROADCAST_ACTION);
        queue = Volley.newRequestQueue(getApplicationContext());
    }

    @Override
    public void onStart(Intent intent, int startId) {

    }


    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            DisplayLoggingInfo();
            handler.postDelayed(this, 5000); // 5 seconds
        }
    };

    private void DisplayLoggingInfo() {
        RealMAdapter realMAdapter = new RealMAdapter(getApplicationContext());
        localCaseCount = realMAdapter.casecount();
        Toast.makeText(getApplicationContext(), "Local:" + String.valueOf(localCaseCount), Toast.LENGTH_SHORT).show();

        // Log.d(TAG, "entered DisplayLoggingInfo");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, NetworkAdapter.url_getopencasecount, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    remoteCaseCount = response.getInt("casecount");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                msg = "1";
                // Toast.makeText(MainActivity.this, "Responce error", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);
        Toast.makeText(getApplicationContext(), "Remote:" + remoteCaseCount, Toast.LENGTH_SHORT).show();
        if (localCaseCount < remoteCaseCount) {
            msg = "visible";
            realMAdapter.loadNewData();
        } else {
            if (msg.equals("1")) {
                msg = "Error";
            } else
                msg = "invisible";
        }
        //Toast.makeText(MainActivity.this,"Finish",Toast.LENGTH_SHORT).show();

        intent.putExtra("time", new Date().toLocaleString());
        intent.putExtra("counter", msg);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

        Toast.makeText(getApplicationContext(), "start Service", Toast.LENGTH_SHORT).show();
     /*   Thread thread = new Thread(new MyThreadClass(startId));
        thread.start();*/
        /*int i=0;
        synchronized (this){
            while (i<10){

            }
        }*/
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "stop Service", Toast.LENGTH_SHORT).show();

    }
}
