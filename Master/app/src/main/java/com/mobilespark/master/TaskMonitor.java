package com.mobilespark.master;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.mobilespark.master.Pojos.ClientListData;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TaskMonitor extends AppCompatActivity {

    private ListView activeServerslist;
    private ListView fallbackServerslist;
    private ArrayList<ClientListData> clientData;
    private ArrayList<ClientListData> activeClientData;
    private ArrayList<ClientListData> fallbackClientData;
    private Button _assignTaskButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_monitor);
        clientData = ClientList.clientData;
        int Slaves = getIntent().getExtras().getInt("Slaves");

        activeServerslist = findViewById(R.id.activeServers);
        fallbackServerslist = findViewById(R.id.fallbackServers);
        _assignTaskButton = findViewById(R.id.assignbutton);

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
        _assignTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                System.out.println("To do /calculate");

                for(int count = 0 ; count < activeClientData.size();count++)
                    activeServerslist.getChildAt(count).setBackgroundColor(Color.YELLOW);
                for(int count = 0 ; count < fallbackClientData.size();count++)
                    fallbackServerslist.getChildAt(count).setBackgroundColor(Color.GRAY);

                ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
                ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();


                for(int count = 0 ; count < activeClientData.size();count++) {

                    activeServerslist.getChildAt(count).setBackgroundColor(Color.GREEN);

                }

                //fallbackServerslist.setBackgroundColor(Color.BLUE);
                //activeServerslist.setBackgroundColor(Color.parseColor("#9fe7ff"));
                //((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
            }
        });

    }
}
