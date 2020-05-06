package com.mobilespark.master;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class ResultStatistics extends AppCompatActivity {

    ListView clientStats;
    ListView allClientStats;
    ListView masterStats;
    Button _homeButton;
    private ClientStatsAdapter masteradapter;
    private ClientStatsAdapter individualadapter;
    private ClientStatsAdapter allClientsadapter;
    Gson gson = new Gson();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat_result);

        clientStats = findViewById(R.id.individual);
        allClientStats = findViewById(R.id.all);
        masterStats = findViewById(R.id.master);
        _homeButton = findViewById(R.id.homeButton);

        Log.i("statData", TaskMonitor.statsData.toString());
        Log.i("object", TaskMonitor.statsData.get(0).toString());
        Log.i("name", TaskMonitor.statsData.get(0).clientName);
        Log.i("power", Float.toString(TaskMonitor.statsData.get(0).powerConsumed));
        Log.i("time", Long.toString(TaskMonitor.statsData.get(0).timeTaken));

        int val = 1;
        if(TaskMonitor.flagmaster) {
             val = 2;
             masteradapter = new ClientStatsAdapter(this, TaskMonitor.statsData.subList(TaskMonitor.statsData.size()-1,TaskMonitor.statsData.size()) );
             masterStats.setAdapter(masteradapter);
        }


        individualadapter = new ClientStatsAdapter(this, TaskMonitor.statsData.subList(0,TaskMonitor.statsData.size()-val) );
        allClientsadapter = new ClientStatsAdapter(this, TaskMonitor.statsData.subList(TaskMonitor.statsData.size()-val,TaskMonitor.statsData.size()-(val - 1)) );

        clientStats.setAdapter(individualadapter);
        allClientStats.setAdapter(allClientsadapter);

        _homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ResultStatistics.this, ClientList.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Cannot go back when the server is running",
                Toast.LENGTH_LONG).show();
        Log.e("ResultStatistics", "onBackPressed: " + "Back button pressed when server is running");

    }
}
