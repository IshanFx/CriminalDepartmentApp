package com.example.ishanfx.departmentapp.network;

/**
 * Created by IshanFx on 2/9/2016.
 */
public class NetworkAdapter {
    public static final String host = "192.168.42.200:8082";
    public static final String url_getopencasecount = "http://"+host+"/ProtectApp/public/policeopencasecount";
    public static final String url_getopencases     = "http://"+host+"/ProtectApp/public/policeopencase";
    public static final String url_setAssign        = "http://"+host+"/ProtectApp/public/policecaseupdate";


    public static final String url_direction = "https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyD3x7xCyqfVRojZ5YATBP0sMsvcmU0QHl4";
}
