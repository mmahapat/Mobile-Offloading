package com.mobilespark.slave;


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
        imgButton = findViewById(R.id.startStopButton);
        status = findViewById(R.id.status);

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!serverRunning) {
                    try {
                        server = new Server(getApplicationContext());
                        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
                        Toast.makeText(MainActivity.this, "Client Started",
                                Toast.LENGTH_LONG).show();
                        Log.d(TAG, "onCreate: \\nRunning! Point your browsers to http://localhost:8080/ \\n\"");
                        serverRunning = true;
                        changeStatus();
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
            status.setText("Client Status : ON");
            status.setTextColor(Color.parseColor("#10b542"));
        } else {
            imgButton.setImageResource(R.drawable.poweron);
            status.setText("Client Status : OFF");
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
