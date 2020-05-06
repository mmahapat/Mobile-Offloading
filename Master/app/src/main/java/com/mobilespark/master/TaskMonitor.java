package com.mobilespark.master;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.mobilespark.master.Pojos.ClientListData;
import com.mobilespark.master.Pojos.ClientStatData;
import com.mobilespark.master.WebUtils.VolleyController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskMonitor extends AppCompatActivity implements ClientResponse {

    private static final String TAG = "TaskMonitor";
    private ListView activeServerslist;
    private ListView fallbackServerslist;
    private ArrayList<ClientListData> clientData;
    private ArrayList<ClientListData> activeClientData;
    private ArrayList<ClientListData> fallbackClientData;
    //int[] stores the postion in array and status
    private Map<String, int[]> activeClientMap;
    private Map<String, int[]> fallbackClientMap;
    private Button _assignTaskButton;
    private Button _mergeTaskButton;
    private Button _useMasterButton;
    private int slaves;
    private int[][] inputMatrixA;
    private int[][] inputMatrixB;
    private int[][] outputMatrix;
    //key: "0-250" value: "S": Success, "F": Failure, "P": Pending
    private Map<String, String> outputMatrixStatusMap;
    public static boolean flagmaster = false;

    //Stats data
    public static List<ClientStatData> statsData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_monitor);
        clientData = ClientList.clientWithConsentData;
        slaves = getIntent().getExtras().getInt("Slaves");

        activeServerslist = findViewById(R.id.activeServers);
        fallbackServerslist = findViewById(R.id.fallbackServers);
        _assignTaskButton = findViewById(R.id.assignbutton);
        _mergeTaskButton = findViewById(R.id.mergebutton);
        _useMasterButton = findViewById(R.id.useMasterButton);

        int i = 0;

        activeClientData = new ArrayList<>();
        fallbackClientData = new ArrayList<>();
        activeClientMap = new HashMap<>();
        fallbackClientMap = new HashMap<>();

        //Stats Data
        statsData = new ArrayList<>();


        //Sort in Decreasing battery power
        Collections.sort(clientData, new BatteryComparator());
        while (i < slaves && i < clientData.size())
            activeClientData.add(clientData.get(i++));


        while (i < clientData.size())
            fallbackClientData.add(clientData.get(i++));


        ClientListAdapter activeServerAdapter = new ClientListAdapter(this, activeClientData);
        activeServerslist.setAdapter(activeServerAdapter);

        ClientListAdapter fallbackServerAdapter = new ClientListAdapter(this, fallbackClientData);
        fallbackServerslist.setAdapter(fallbackServerAdapter);

        Handler handler = new Handler();
        handler.postDelayed(loaddata, 2000);

        _assignTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                for (int count = 0; count < activeClientData.size(); count++) {
                    addtoTaskqueue(count);
                }

            }
        });

        _useMasterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                flagmaster = true;
                //Calculate total Slave Data
                float totalPowerConsumed = 0;
                long totalTimeTaken = 0;
                for(ClientStatData csData: statsData){
                    totalPowerConsumed += csData.powerConsumed;
                    totalTimeTaken += csData.timeTaken;
                }
                // Add total Data
                statsData.add(new ClientStatData("All Clients", totalPowerConsumed, totalTimeTaken));

                BatteryManager bm = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);

                int initPower = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                long start = Calendar.getInstance().getTimeInMillis();
                GenerateMatrix.multiplyMatrix(inputMatrixA, inputMatrixB);
                long end = Calendar.getInstance().getTimeInMillis();
                int finalPower = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);

                float powerConsumed = (initPower - finalPower) / 3600;
                int timeTaken = (int) (end - start);
                Log.i("Power consumed", Float.toString(powerConsumed));
                ClientStatData data = new ClientStatData("Master", powerConsumed, timeTaken);
                statsData.add(data);
                Intent statistics = new Intent(TaskMonitor.this, ResultStatistics.class);
                startActivity(statistics);

            }
        });

        _mergeTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flagmaster = true;
                //Calculate total Slave Data
                float totalPowerConsumed = 0;
                long totalTimeTaken = 0;
                for(ClientStatData csData: statsData){
                    totalPowerConsumed += csData.powerConsumed;
                    totalTimeTaken += csData.timeTaken;
                }
                // Add total Data
                statsData.add(new ClientStatData("All Clients", totalPowerConsumed, totalTimeTaken));

                Intent statScreen = new Intent(TaskMonitor.this, ResultStatistics.class);
                startActivity(statScreen);
            }
        });

    }

    //Set background color to Yellow and gray for Slave and Fallback Servers
    private Runnable loaddata = new Runnable() {
        public void run() {
            for (int count = 0; count < activeClientData.size(); count++) {

                getViewByPosition(count, activeServerslist).setBackgroundColor(Color.YELLOW);
                activeClientMap.put(activeClientData.get(count).clientIp, new int[]{count, 0});
            }
            for (int count = 0; count < fallbackClientData.size(); count++) {

                getViewByPosition(count, fallbackServerslist).setBackgroundColor(Color.GRAY);
                fallbackClientMap.put(fallbackClientData.get(count).clientIp, new int[]{count, 0});
            }

            initializeMatrixParams(300);
        }
    };


    public void initializeMatrixParams(int size) {
        inputMatrixA = GenerateMatrix.createMatrix(size, size);
        inputMatrixB = GenerateMatrix.createMatrix(size, size);
        outputMatrixStatusMap = new HashMap<>();
        outputMatrix = new int[size][size];
        int start = 0;
        int end = 0;
        int range = size / slaves;
        int reminder = size % slaves;
        for (int i = 0; i < slaves; i++) {
            if (i == 0)
                end += range + reminder;
            else
                end += range;
            outputMatrixStatusMap.put(String.valueOf(start) + "-" + String.valueOf(end), "F");
            start = end;
        }

    }

    //Get the view not in the visible pane
    public View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition) {
            return listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    // Add task from Active server list VolleyController Request Queue
    private void addtoTaskqueue(int count) {
        try {
            String clientIp = activeClientData.get(count).clientIp;
            String url = "http://" + clientIp + ":8080/calculate";
            VolleyController volleyController = new VolleyController(getApplicationContext());
            JSONObject body = new JSONObject();
            int start = 0;
            int end = 0;
            StringBuilder inputParameter = new StringBuilder(clientIp);
            for (String key : outputMatrixStatusMap.keySet()) {
                if (outputMatrixStatusMap.get(key).equals("F")) {
                    outputMatrixStatusMap.put(key, "P");
                    inputParameter.append(" ");
                    inputParameter.append(key);
                    String[] parts = key.split("-");
                    start = Integer.parseInt(parts[0]);
                    end = Integer.parseInt(parts[1]);
                    break;
                }
            }
            int[][] splitA = GenerateMatrix.splitMatrixHorizontally(start, end, inputMatrixA);
            body.put("A", Arrays.deepToString(splitA));
            body.put("B", Arrays.deepToString(inputMatrixB));
            volleyController.makeRequest(url, body, TaskMonitor.this, inputParameter.toString());
        } catch (Throwable t) {
            Log.e(TAG, "Well that's not good.", t);
        }
    }


    //Update Output Matrix and tracking parameters on Success
    public void updateMatrix(JSONObject jsonObject, String identifier) {
        // Update the output Matrix
        String[] parts = identifier.trim().split(" ");
        String[] range = parts[1].split("-");
        int start = Integer.parseInt(range[0]);
        int end = Integer.parseInt(range[1]);
        Gson gson = new Gson();
        try {

            int[][] rangeoutputMatrix = gson.fromJson((String) jsonObject.get("result"), int[][].class);
            int k = 0;
            for (int i = start; i < end; i++) {
                outputMatrix[i] = rangeoutputMatrix[k++];
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        outputMatrixStatusMap.put(parts[1], "S");
        for (String val : outputMatrixStatusMap.values()) {
            if (val.equals("P") || val.equals("F"))
                break;
            Toast.makeText(TaskMonitor.this, "Task Complete",
                    Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onSuccess(JSONObject jsonObject, String identifier) {
        String[] parts = identifier.trim().split(" ");
        String clientIp = null;
        String powerConsumed = null;
        String timeTaken = null;
        try {
            clientIp = parts[0];
            powerConsumed = String.valueOf(jsonObject.get("power_consumed"));
            timeTaken = String.valueOf(jsonObject.get("execution_time"));

            ClientStatData clientData = new ClientStatData(ClientList.clientMap.get(clientIp),
                    Float.valueOf(powerConsumed), Long.valueOf(timeTaken));
            statsData.add(clientData);

        } catch (Exception e) {
            Log.e(TAG, "onSuccess: " + "Could not pass JSON");
        }

        if (activeClientMap.containsKey(clientIp)) {
            int[] data = activeClientMap.get(clientIp);

            getViewByPosition(data[0], activeServerslist).setBackgroundColor(Color.GREEN);
            activeClientMap.put(clientIp, new int[]{data[0], 1});
        }
        ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
        ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();

        updateMatrix(jsonObject, identifier);

    }

    @Override
    public void onFailure(VolleyError error, String identifier) {
        String[] parts = identifier.trim().split(" ");
        String clientIp = parts[0];
        outputMatrixStatusMap.put(parts[1], "F");

        //Update the Error Status
        if (activeClientMap.containsKey(clientIp)) {
            int[] data = activeClientMap.get(clientIp);
            getViewByPosition(data[0], activeServerslist).setBackgroundColor(Color.RED);
            activeClientMap.put(clientIp, new int[]{data[0], 0});
        }

        ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
        ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();

        //Fallback if there is  available client
        if (fallbackClientData.size() > 0) {
            int pos = activeClientData.size();
            activeClientData.add(fallbackClientData.get(0));
            activeClientMap.put(fallbackClientData.get(0).clientIp, new int[]{pos, 0});
            fallbackClientMap.remove(fallbackClientData.get(0).clientIp);
            fallbackClientData.remove(0);
            addtoTaskqueue(pos);
        }
        ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
        ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();

        System.out.println("Error" + error);
    }
}
