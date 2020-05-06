package com.mobilespark.slave;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
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
    public String masterName;
    private ImageButton imgButton;
    private TextView status;
    TextView master;
    private boolean serverRunning = false;
    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
        imgButton = findViewById(R.id.startStopButton);
        status = findViewById(R.id.status);
        master = findViewById(R.id.master);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        final String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!serverRunning) {
                    try {
                        server = new Server(getApplicationContext(), ip, master);
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

    public void showMainDialog(final String ip) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage(ip + " wants to assign task of matrix multiplication. Will use your battery information.");
        builder1.setCancelable(true);
        builder1.setPositiveButton(
                "Give Permission",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        changeStatusOfClient(ip);
                        dialog.cancel();
                    }
                });

        builder1.setNegativeButton(
                "Deny",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        changeStatusOfClient(null);
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void changeStatus() {
        if (serverRunning) {
            imgButton.setImageResource(R.drawable.poweroff);
            status.setText("Client Status : ON");
            status.setTextColor(Color.parseColor("#10b542"));
        } else {
            imgButton.setImageResource(R.drawable.poweron);
            status.setText("Client Status : OFF");
            master.setText("Not Connected to Master");
            master.setTextColor(Color.parseColor("#bf1f1f"));
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

    public String getMaster() {
        return masterName;
    }

    private void changeStatusOfClient(String masterIP) {
        if (masterIP == null) {
            masterName = null;
            master.setText("Not Connected to Master");
            master.setTextColor(Color.parseColor("#bf1f1f"));
        } else {
            String text = "Connected to : " + masterIP;
            masterName = masterIP;
            master.setText(text);
            master.setTextColor(Color.parseColor("#10b542"));
        }
    }
}
