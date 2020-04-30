package com.mobilespark.master;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            new Server();
        } catch (IOException e) {
            Log.e(TAG, String.valueOf(e), e);
        }
        setContentView(R.layout.activity_main);
    }
}
