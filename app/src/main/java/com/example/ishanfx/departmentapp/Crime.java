package com.example.ishanfx.departmentapp;

import io.realm.RealmObject;

/**
 * Created by IshanFx on 2/9/2016.
 */
public class Crime extends RealmObject {
    private int icon;
    private String title;
    private int caseid;
    private String date;
    private String type;
    private String latitude;
    private String longitude;
    private String status;


    public Crime(){
        super();
    }

    public Crime(int icon, String title) {
        super();
        this.setIcon(icon);
        this.setTitle(title);
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCaseid() {
        return caseid;
    }

    public void setCaseid(int caseid) {
        this.caseid = caseid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
