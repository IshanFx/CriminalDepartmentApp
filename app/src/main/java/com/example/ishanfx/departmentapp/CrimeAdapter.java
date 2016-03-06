package com.example.ishanfx.departmentapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by IshanFx on 2/9/2016.
 */
public class CrimeAdapter  extends ArrayAdapter<Crime> {

    Context context;
    int layoutResourceId;
    List<Crime> objects;

    public CrimeAdapter(Context context, int layoutResourceId,List<Crime> objects) {
        super(context, layoutResourceId, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Crime crime = objects.get(position);
        String dateTime = new String(crime.getDate());
        String DT[] = dateTime.split("\\s+");
        String caseName="";
        Log.d("Dummy", crime.toString());
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.listview_item, null);
        TextView txt = (TextView) view.findViewById(R.id.txtTitle);
        TextView txtDate = (TextView) view.findViewById(R.id.txtDate);
        TextView txtType  = (TextView) view.findViewById(R.id.txtType);
        ImageView img = (ImageView) view.findViewById(R.id.imgIcon);
        int res = 0;
        txt.setText("Time: "+DT[1].toString());
        txtDate.setText("Date: "+DT[0].toString());
        switch (crime.getType()){
            case "E":
                caseName="Evidence";
                res = context.getResources().getIdentifier("camera", "drawable", context.getPackageName());
                break;
            case "R":
                caseName="Robbery";
                res = context.getResources().getIdentifier("marker", "drawable", context.getPackageName());
                break;
            case "K":
                caseName="Kidnap";
                res = context.getResources().getIdentifier("run", "drawable", context.getPackageName());
                break;
        }
        txtType.setText(caseName);
        img.setImageResource(res);
        return view;
    }

}
