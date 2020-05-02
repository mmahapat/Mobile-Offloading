package com.mobilespark.master;


import androidx.appcompat.app.AppCompatActivity;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mobilespark.master.WebUtils.NetworkCalls;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    public Server server;
    private ImageButton imgButton;
    private TextView status;
    private boolean serverRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Simple implemetation of a request can be found at the below function using Volley
        makeRequest();
         */
        NetworkCalls call = new NetworkCalls();
        Log.d(TAG, "onCreate: " + call.makeGetRequest(MainActivity.this, "http://google.com"));

        imgButton = findViewById(R.id.startStopButton);
        status = findViewById(R.id.status);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!serverRunning) {
                    try {
                        server = new Server();
                        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                        Toast.makeText(MainActivity.this, "Running on http://localhost:8080",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onCreate: \\nRunning! Point your browsers to http://localhost:8080/ \\n\"");
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

    private void makeRequest() {
        final TextView textView = findViewById(R.id.status);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://www.google.com";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textView.setText("Response is: " + response.substring(0, 500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
                textView.setText("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
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
}
