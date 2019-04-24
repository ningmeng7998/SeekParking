package com.graceli.parkinghunt;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CounterConfig extends AppCompatActivity {

    String timeLst = "";
    String mLat = "";
    String mLon = "";
    String bayno = "";
    String parkAddress = "";

    Button btn_Start;
    Button btn_Hour_P;
    Button btn_Hour_M;
    Button btn_Min_P;
    Button btn_Min_M;
    TextView txt_Hour;
    TextView txt_Min;
    RadioGroup rb_Group;
    RadioButton rb_5min;
    RadioButton rb_10min;
    RadioButton rb_15min;
    RadioButton rb_30min;

    int time_Hour = 0;
    int time_Min = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter_config);
        ImageButton backBtn = findViewById(R.id.m_t_backcc);
        backBtn.setOnClickListener(new  View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        time_Hour = 0;
        time_Min = 0;
        btn_Start = findViewById(R.id.btn_Start);
        btn_Hour_P = findViewById(R.id.btn_hour_P);
        btn_Hour_M = findViewById(R.id.btn_hour_M);
        btn_Min_P = findViewById(R.id.btn_min_P);
        btn_Min_M = findViewById(R.id.btn_min_M);
        txt_Hour = findViewById(R.id.txt_hour);
        txt_Min = findViewById(R.id.txt_min);
        rb_Group = findViewById(R.id.group_RB);
        rb_5min = findViewById(R.id.rb_5min);
        rb_10min = findViewById(R.id.rb_10min);
        rb_15min = findViewById(R.id.rb_15min);
        rb_30min = findViewById(R.id.rb_30min);
        try{
            Intent senderIntent=getIntent();
            timeLst = senderIntent.getStringExtra("TimeMin");
            mLat = senderIntent.getStringExtra("mLat");
            mLon = senderIntent.getStringExtra("Mlon");
            bayno = senderIntent.getStringExtra("bayNo");
            parkAddress = Getlatlon_Address(Double.parseDouble(mLat),Double.parseDouble(mLon));
            TextView txtMaxtime = findViewById(R.id.txt_maxtime);
            txtMaxtime.setText("Maximum Time : " + timeLst + " (Min)");
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error to Set Parking Time", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        int lstmincon = Integer.parseInt(timeLst);
        time_Min = lstmincon % 60;
        time_Hour = lstmincon / 60;
        String tmhrB = String.valueOf(time_Hour);
        tmhrB = tmhrB.length() == 1 ? "0" + tmhrB : tmhrB;
        txt_Hour.setText(tmhrB);
        String tmmnB = String.valueOf(time_Min);
        tmmnB = tmmnB.length() == 1 ? "0" + tmmnB : tmmnB;
        txt_Min.setText(tmmnB);
        rb_15min.setChecked(true);
        btn_Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if((time_Min==0) && (time_Hour==0)) { Toast.makeText(getApplicationContext(), "Parking Timer Incorrect", Toast.LENGTH_SHORT).show(); return; }
                    int noti_min = 0;
                    int lsttimminfb = 0;
                    lsttimminfb = (time_Hour * 60) + time_Min;
                    if(rb_5min.isChecked()){noti_min=5;}
                    if(rb_10min.isChecked()){noti_min=10;}
                    if(rb_15min.isChecked()){noti_min=15;}
                    if(rb_30min.isChecked()){noti_min=30;}
                    Intent GoActivity = new Intent(CounterConfig.this, CounterDown.class);
                    GoActivity.putExtra("TimeMin",timeLst);
                    GoActivity.putExtra("bayNo",bayno);
                    GoActivity.putExtra("address",parkAddress);
                    GoActivity.putExtra("notitim",String.valueOf(noti_min));
                    GoActivity.putExtra("UserTime",String.valueOf(lsttimminfb));
                    startActivity(GoActivity);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_Hour_P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(time_Hour<99){time_Hour++;}
                    String tmhr = String.valueOf(time_Hour);
                    tmhr = tmhr.length() == 1 ? "0" + tmhr : tmhr;
                    txt_Hour.setText(tmhr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_Hour_M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(time_Hour>0){time_Hour--;}
                    String tmhr = String.valueOf(time_Hour);
                    tmhr = tmhr.length() == 1 ? "0" + tmhr : tmhr;
                    txt_Hour.setText(tmhr);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_Min_P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(time_Min<59){time_Min++;}
                    String tmmn = String.valueOf(time_Min);
                    tmmn = tmmn.length() == 1 ? "0" + tmmn : tmmn;
                    txt_Min.setText(tmmn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btn_Min_M.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if(time_Min>0){time_Min--;}
                    String tmmn = String.valueOf(time_Min);
                    tmmn = tmmn.length() == 1 ? "0" + tmmn : tmmn;
                    txt_Min.setText(tmmn);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String Getlatlon_Address(double lat, double lon)
    {
        try {
            Geocoder geo = new Geocoder(this.getApplicationContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(lat, lon, 1);
            if (addresses.isEmpty()) {
                return "Location Not Founded";
            }
            else {
                if (addresses.size() > 0) {
                    return addresses.get(0).getAddressLine(0) + ", " + addresses.get(0).getAdminArea();
                }
                else
                { return "Address Not Founded"; }
            }
        }
        catch (Exception e) {
            e.printStackTrace(); // getFromLocation() may sometimes fail
            return "Address Not Founded";
        }
    }
}
