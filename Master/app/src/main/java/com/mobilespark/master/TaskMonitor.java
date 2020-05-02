package com.mobilespark.master;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import com.mobilespark.master.Pojos.ClientListData;

import java.util.ArrayList;

public class TaskMonitor extends AppCompatActivity {

    private ListView activeServerslist;
    private ListView fallbackServerslist;
    private ArrayList<ClientListData> clientData;
    private ArrayList<ClientListData> activeClientData;
    private ArrayList<ClientListData> fallbackClientData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_monitor);
        clientData = ClientList.clientData;
        int Slaves = getIntent().getExtras().getInt("Slaves");

        activeServerslist = findViewById(R.id.activeServers);
        fallbackServerslist = findViewById(R.id.fallbackServers);

        int i = 0;
        activeClientData = new ArrayList<>();
        while(i < Slaves && i < clientData.size())
            activeClientData.add(clientData.get(i++));

        fallbackClientData = new ArrayList<>();
        while(i < clientData.size())
            fallbackClientData.add(clientData.get(i++));

        ClientListAdapter activeServerAdapter = new ClientListAdapter(this, activeClientData);
        activeServerslist.setAdapter(activeServerAdapter);

        ClientListAdapter fallbackServerAdapter = new ClientListAdapter(this, fallbackClientData);
        fallbackServerslist.setAdapter(fallbackServerAdapter);

        //((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();

    }
}
