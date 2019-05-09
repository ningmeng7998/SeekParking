package com.graceli.parkinghunt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class Links extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);
        ImageButton backBtn = findViewById(R.id.m_t_backug);
        backBtn.setOnClickListener(new  View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        TextView link1 = findViewById(R.id.info_link1);
        link1.setMovementMethod(LinkMovementMethod.getInstance());
        TextView link2 = findViewById(R.id.info_link2);
        link2.setMovementMethod(LinkMovementMethod.getInstance());
    }
}