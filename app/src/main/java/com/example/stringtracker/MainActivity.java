package com.example.stringtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void gotoConfiguration(View v){
        Intent intent = new Intent(this, Configuration.class);
        startActivity(intent);
    }

    public void gotoAnalytics(View v){
        Intent intent = new Intent(this, Analytics.class);
        startActivity(intent);
    }
}