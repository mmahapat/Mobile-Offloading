package com.mobilespark.master;


import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mobilespark.master.WebUtils.VolleyController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public static Server server;
    private ImageButton imgButton;
    private TextView status;
    public static boolean serverRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgButton = findViewById(R.id.startStopButton);
        status = findViewById(R.id.status);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!serverRunning) {
                    try {
                        server = new Server();
                        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                        Toast.makeText(MainActivity.this, "Running on http://localhost:8081",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onCreate: \\nRunning! Point your browsers to http://localhost:8081/ \\n\"");
                        serverRunning = true;
                        changeStatus();
                        Intent intent = new Intent(MainActivity.this, ClientList.class);
                        startActivity(intent);
                    } catch (IOException e) {
                        serverRunning = false;
                        changeStatus();
                        Toast.makeText(MainActivity.this, "Cannot start the server on port 8080, Something went wrong",
                                Toast.LENGTH_LONG).show();
                        Log.e(TAG, String.valueOf(e), e);
                    }
                } else {
                    if (server != null)
                        server.stop();
                    serverRunning = false;
                    Log.d(TAG, "onCreate: Stopping server");
                    Toast.makeText(MainActivity.this, "Server Stopped",
                            Toast.LENGTH_LONG).show();
                    changeStatus();
                }
            }
        });
    }


    private void changeStatus() {
        if (serverRunning) {
            imgButton.setImageResource(R.drawable.poweroff);
            status.setText("Server Status : ON");
            status.setTextColor(Color.parseColor("#10b542"));
        } else {
            imgButton.setImageResource(R.drawable.poweron);
            status.setText("Server Status : OFF");
            status.setTextColor(Color.parseColor("#bf1f1f"));
        }
    }

    //    Stop the Server if the application is closed or destroyed
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null)
            server.stop();
        Log.d(TAG, "onCreate: Stopping server");
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeStatus();
        Log.e(TAG, "onResume: came here");
    }
}
