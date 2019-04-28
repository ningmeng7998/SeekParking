package com.graceli.parkinghunt;


import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CounterDown extends AppCompatActivity {

    String Time_Str_ParkMax = "";
    String Park_BayNo = "";
    String Park_Address = "";
    String Noti_Time_Min = "";
    String Last_Timer_Min = "";
    int NotiTimeMin_int = 0;
    int LastTimeMin_int = 0;

    TextView txt_Start_Parking_Time;
    TextView txt_End_Parking_Time;
    TextView txt_Notification_Time;
    TextView txt_Timeout_Notification;
    TextView txt_Parking_Spot_BayID;
    TextView txt_Address;

    TextView txt_Timer_Hour;
    TextView txt_Timer_Min;
    TextView txt_Timer_Sec;

    Button btn_Cancel;

    int Time_Hour = 0;
    int TIme_Min = 0;
    int Time_Sec = 0;

    int noti_1 = 0;
    int noti_2 = 0;

    LinearLayout lyTC;
    CountDownTimer cTimer = null;


    private static final int notifyid = 1;
    public static String CHANNEL_ID = "default_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_counter_down);
        createNotificationChannel();
        Intent senderIntent=getIntent();
        Time_Str_ParkMax = senderIntent.getStringExtra("TimeMin");
        Park_BayNo = senderIntent.getStringExtra("bayNo");
        Park_Address = senderIntent.getStringExtra("address");
        Noti_Time_Min = senderIntent.getStringExtra("notitim");
        Last_Timer_Min = senderIntent.getStringExtra("UserTime");
        NotiTimeMin_int = Integer.parseInt(Noti_Time_Min);
        LastTimeMin_int = Integer.parseInt(Last_Timer_Min);
        txt_Start_Parking_Time = findViewById(R.id.txt_SPT);
        txt_End_Parking_Time = findViewById(R.id.txt_EPT);
        txt_Notification_Time = findViewById(R.id.txt_NT);
        txt_Timeout_Notification = findViewById(R.id.txt_TOUTN);
        txt_Parking_Spot_BayID = findViewById(R.id.txt_PS_bay);
        txt_Address = findViewById(R.id.txt_Address);
        txt_Timer_Hour = findViewById(R.id.txt_Hour);
        txt_Timer_Min = findViewById(R.id.txt_Min);
        txt_Timer_Sec = findViewById(R.id.txt_Sec);
        lyTC = findViewById(R.id.ly_TimerColor);
        btn_Cancel = findViewById(R.id.btn_Cancel);
        btn_Cancel.setText("Stop Parking Timer");
        btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    cancelTimer();
                    finish();
                } catch (Exception e) {
                    Log.i("EMAS", "Noti Error");
                    Log.i("EMAS", e.getMessage());
                }
            }
        });
        lyTC.setBackgroundColor(Color.parseColor("#1b5f8a"));
        noti_1=0;
        noti_2=0;
        txt_Parking_Spot_BayID.setText("Parking Spot :  ( " + Park_BayNo.trim() + " )");
        txt_Address.setText(Park_Address.trim() + "\n");
        txt_Timeout_Notification.setText(Noti_Time_Min +" Min");
        TIme_Min = LastTimeMin_int % 60;
        Time_Hour = LastTimeMin_int / 60;
        Time_Sec = 0;
        String tmhrB = String.valueOf(Time_Hour);
        tmhrB = tmhrB.length() == 1 ? "0" + tmhrB : tmhrB;
        txt_Timer_Hour.setText(tmhrB);
        String tmmnB = String.valueOf(TIme_Min);
        tmmnB = tmmnB.length() == 1 ? "0" + tmmnB : tmmnB;
        txt_Timer_Min.setText(tmmnB);
        txt_Timer_Sec.setText("00");
        Time time = new Time();
        time.setToNow();
        String currentTime_H = String.valueOf(time.hour);
        String currentTime_M = String.valueOf(time.minute);
        currentTime_H = currentTime_H.length() == 1 ? "0" + currentTime_H : currentTime_H;
        currentTime_M = currentTime_M.length() == 1 ? "0" + currentTime_M : currentTime_M;
        txt_Start_Parking_Time.setText(currentTime_H + ":" + currentTime_M);
        time.hour = time.hour + Time_Hour;
        time.minute = time.minute + TIme_Min;
        if(time.minute==60)
        { time.minute=0; time.hour++; }
        if(time.minute>60)
        { time.minute = time.minute - 60; time.hour++; }
        while(time.hour>24)
        { time.hour = time.hour - 24; }
        if(time.hour==24)
        { time.hour = 0; }
        time.toMillis(false);
        String dureTime_H = String.valueOf(time.hour);
        String dureTime_M = String.valueOf(time.minute);
        dureTime_H = dureTime_H.length() == 1 ? "0" + dureTime_H : dureTime_H;
        dureTime_M = dureTime_M.length() == 1 ? "0" + dureTime_M : dureTime_M;
        txt_End_Parking_Time.setText(dureTime_H + ":" + dureTime_M);
        time.minute = time.minute - NotiTimeMin_int;
        if (time.minute<0)
        {
            time.minute = 60 + time.minute; time.hour--;
            if (time.hour<0) { time.hour = 24 + time.hour; }
        }
        time.toMillis(false);
        String dureTime_H_N = String.valueOf(time.hour);
        String dureTime_M_N = String.valueOf(time.minute);
        dureTime_H_N = dureTime_H_N.length() == 1 ? "0" + dureTime_H_N : dureTime_H_N;
        dureTime_M_N = dureTime_M_N.length() == 1 ? "0" + dureTime_M_N : dureTime_M_N;
        txt_Notification_Time.setText(dureTime_H_N + ":" + dureTime_M_N);
        startTimer();

    }


    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void ShowNoti(String txt_Title,String txt_Body,int Noti_ID){
        try{
            NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info) // notification icon
                    .setContentTitle(txt_Title.trim()) // title for notification
                    .setContentText(txt_Body.trim()) // message for notification
                    .setAutoCancel(true); // clear notification after click
            //Intent intent = new Intent(this, CounterDown.class);
            //PendingIntent pi = PendingIntent.getActivity(this,0,intent,0);
            //mBuilder.setContentIntent(pi);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(Noti_ID, mBuilder.build());
        }catch (Exception e){
            Log.i("EMAS", "Noti Error");
            Log.i("EMAS", e.getMessage());
        }
    }

    void startTimer() {
        cTimer = new CountDownTimer( LastTimeMin_int * 60000, 1000) {
            public void onTick(long millisUntilFinished) {
                long NowSec = millisUntilFinished / 1000;
                long l_Time_Hour = NowSec / 3600;
                long l_Time_Min = (NowSec / 60) % 60;
                long l_Time_Sec = NowSec % 60;
                String tth = String.valueOf(l_Time_Hour);
                String ttm = String.valueOf(l_Time_Min);
                String tts = String.valueOf(l_Time_Sec);
                tth = tth.length() == 1 ? "0" + tth : tth;
                ttm = ttm.length() == 1 ? "0" + ttm : ttm;
                tts = tts.length() == 1 ? "0" + tts : tts;
                txt_Timer_Hour.setText(tth);
                txt_Timer_Min.setText(ttm);
                txt_Timer_Sec.setText(tts);
                if(noti_1==0)
                {
                    if(l_Time_Min<NotiTimeMin_int)
                    {
                        lyTC.setBackgroundColor(Color.parseColor("#FF8A65"));
                        noti_1=1;
                        noti_2=0;
                        ShowNoti("Parking Hunt","Parking Time remaining : " + String.valueOf(NotiTimeMin_int) + " Min",1);
                    }
                }
            }
            public void onFinish() {
                txt_Timer_Hour.setText("00");
                txt_Timer_Min.setText("00");
                txt_Timer_Sec.setText("00");
                btn_Cancel.setText("Back to Parking Hunt");
                lyTC.setBackgroundColor(Color.parseColor("#FF6E6E"));
                if(noti_2==0)
                {
                    noti_2 = 1;
                    ShowNoti("Parking Hunt","Your parking time is over",2);
                }
            }
        };
        cTimer.start();
    }

    void cancelTimer() {
        if(cTimer!=null) { cTimer.cancel(); }
    }

    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "Please Stop Parking Timer", Toast.LENGTH_SHORT).show();
        return;
    }
}
