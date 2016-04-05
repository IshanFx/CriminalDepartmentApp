package com.example.ishanfx.departmentapp.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by IshanFx on 2/9/2016.
 */
public class NetworkAdapter {
    public static final String host = "192.168.42.200:8082/ProtectApp/public";
    //public static final String host = "protectmelkapp.xyz";
    public static final String url_getopencasecount = "http://"+host+"/policeopencasecount";
    public static final String url_getopencases     = "http://"+host+"/policeopencase";
    public static final String  url_setAssign        = "http://"+host+"/policecaseupdate";
    public static final String  url_setOwnerLocation        = "http://"+host+"/policemovinglocationadd";
    public static String url_getMovingLocation      = "http://"+host+"/policemovinglocation/467";

    public static final String url_direction = "https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyD3x7xCyqfVRojZ5YATBP0sMsvcmU0QHl4";

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
