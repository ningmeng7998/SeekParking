package com.graceli.seekparking;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;

public class Splash extends AppCompatActivity implements OnRequestPermissionsResultCallback {
    private boolean readyToStart = false;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] { Manifest.permission.ACCESS_FINE_LOCATION };
    private int DenyCountF = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
        }
        else { readyToStart = true; }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (readyToStart) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i=new Intent(Splash.this,UserGuide.class);
                    startActivity(i);
                    finish();
                }
            },2000);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        DenyCountF++;
                        if (DenyCountF<3)
                        { ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS); }
                        else
                        { finish(); }
                    }
                    else
                    {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent i=new Intent(Splash.this,UserGuide.class);
                                startActivity(i);
                                finish();
                            }
                        },2000);
                    }
                }
            }
        }
    }

}
