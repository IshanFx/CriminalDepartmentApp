package com.example.ishanfx.departmentapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.ishanfx.departmentapp.database.RealMAdapter;
import com.example.ishanfx.departmentapp.network.NetworkAdapter;

import org.json.JSONObject;

import java.net.Inet4Address;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IshanFx on 2/5/2016.
 */
public class ProtectService extends Service  {
    private static final String TAG = "BroadcastService";
    public static final String BROADCAST_ACTION = "com.example.ishanfx.departmentapp";
    private final Handler handler = new Handler();
    private final Handler handlerLocation = new Handler();
    Intent intent;
    int counter = 0;
    RequestQueue queue;
    static int localCaseCount;
    static int remoteCaseCount;
    public static int c = 1;
    public String notificationState = "ss";

    @Override
    public void onCreate() {
        intent = new Intent(BROADCAST_ACTION);
        queue = Volley.newRequestQueue(getApplicationContext());
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
            StringRequest request = new StringRequest(Request.Method.POST, NetworkAdapter.url_getopencasecount, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    remoteCaseCount = Integer.parseInt(jsonObject.getString("casecount"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Log.d("Dip", "Dip start");
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("ownerid","1");
                return parameters;
            }
        };
        queue.add(request);

        Toast.makeText(getApplicationContext(), "Remote:" + remoteCaseCount, Toast.LENGTH_SHORT).show();

        if (localCaseCount < remoteCaseCount)
        {
            notificationState = "visible";
            realMAdapter.loadNewData();
        }

        else
        {
            if (notificationState.equals("1")) {
                notificationState = "Please Refresh";
            } else
                notificationState = "invisible";
        }

        //Toast.makeText(MainActivity.this,"Finish",Toast.LENGTH_SHORT).show();

        intent.putExtra("time", new Date().toLocaleString());
        intent.putExtra("counter", notificationState);

        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second

        Toast.makeText(getApplicationContext(), "start Service", Toast.LENGTH_SHORT).show();

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
