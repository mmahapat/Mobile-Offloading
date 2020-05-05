package com.mobilespark.master;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.mobilespark.master.Pojos.ClientStatData;

import java.util.List;

public class ResultStatistics extends AppCompatActivity {

    ListView clientStats;
    Button _showMatrix;
    Gson gson = new Gson();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat_result);

        clientStats = findViewById(R.id.statList);
        _showMatrix = findViewById(R.id.showMatrix);

        Log.i("statData", TaskMonitor.statsData.toString());
        Log.i("object", TaskMonitor.statsData.get(0).toString());
        Log.i("name", TaskMonitor.statsData.get(0).clientName);
        Log.i("power", Float.toString(TaskMonitor.statsData.get(0).powerConsumed));
        Log.i("time", Long.toString(TaskMonitor.statsData.get(0).timeTaken));

        ClientStatsAdapter adapter = new ClientStatsAdapter(this, TaskMonitor.statsData );
        final String matrix = gson.toJson(GenerateMatrix.createMatrix(1000, 1000));

        clientStats.setAdapter(adapter);

        _showMatrix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Popup.class);
                intent.putExtra("matrix", Integer.toString(matrix.length()));
                startActivity(intent);
            }
        });
    }
}
