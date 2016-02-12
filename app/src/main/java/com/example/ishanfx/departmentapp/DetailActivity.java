package com.example.ishanfx.departmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ishanfx.departmentapp.database.RealMAdapter;

public class DetailActivity extends AppCompatActivity {
    TextView txtCaseID,txtType,txtDate,txtStatus,txtLongitude,txtLatitude;
    Button btnFollowMap,btnAssign;

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

        //txtCaseID.setText(caseid);
        getCrimeData(Integer.parseInt(caseid));
    }

    private void getCrimeData(Integer caseid) {
        RealMAdapter realMAdapter = new RealMAdapter(getApplicationContext());
        Crime crime = realMAdapter.getAllData(caseid);
        Toast.makeText(getApplicationContext(), crime.toString(), Toast.LENGTH_SHORT).show();
        txtCaseID.setText(String.valueOf(crime.getCaseid()));
        txtType.setText(crime.getType());
        txtStatus.setText(crime.getStatus());
        txtLongitude.setText(crime.getLongitude());
        txtLatitude.setText(crime.getLatitude());
    }

}
