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
    ListView otherStats;
    Button _homeButton;
    Gson gson = new Gson();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat_result);

        clientStats = findViewById(R.id.statList);
        otherStats = findViewById(R.id.statList2);
        _homeButton = findViewById(R.id.homeButton);

        Log.i("statData", TaskMonitor.statsData.toString());
        Log.i("object", TaskMonitor.statsData.get(0).toString());
        Log.i("name", TaskMonitor.statsData.get(0).clientName);
        Log.i("power", Float.toString(TaskMonitor.statsData.get(0).powerConsumed));
        Log.i("time", Long.toString(TaskMonitor.statsData.get(0).timeTaken));

        ClientStatsAdapter adapter = new ClientStatsAdapter(this, TaskMonitor.statsData.subList(0,TaskMonitor.statsData.size()-2) );
        ClientStatsAdapter adapter2 = new ClientStatsAdapter(this, TaskMonitor.statsData.subList(TaskMonitor.statsData.size()-2,TaskMonitor.statsData.size()) );
        final String matrix = gson.toJson(GenerateMatrix.createMatrix(1000, 1000));

        clientStats.setAdapter(adapter);
        otherStats.setAdapter(adapter2);

        _homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(getApplicationContext(), Popup.class);
                //intent.putExtra("matrix", Integer.toString(matrix.length()));

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
