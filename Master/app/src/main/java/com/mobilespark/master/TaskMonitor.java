package com.mobilespark.master;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.mobilespark.master.Pojos.ClientListData;

import java.util.ArrayList;

public class TaskMonitor extends AppCompatActivity {

    private ListView activeServerslist;
    private ListView fallbackServerslist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_monitor);
        ArrayList<ClientListData> clientData = ClientList.clientData;
        activeServerslist = findViewById(R.id.activeServers);
        fallbackServerslist = findViewById(R.id.fallbackServers);

        ClientListAdapter activeServerAdapter = new ClientListAdapter(this, clientData);
        activeServerslist.setAdapter(activeServerAdapter);

        ClientListAdapter fallbackServerAdapter = new ClientListAdapter(this, clientData);
        fallbackServerslist.setAdapter(fallbackServerAdapter);

    }
}
