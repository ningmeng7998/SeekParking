package com.graceli.parkinghunt;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

public class HelpPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helpph_f);
        ImageButton backBtn = findViewById(R.id.m_t_backh);
        backBtn.setOnClickListener(new  View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}