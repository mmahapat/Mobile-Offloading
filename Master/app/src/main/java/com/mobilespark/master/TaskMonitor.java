package com.mobilespark.master;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.mobilespark.master.Pojos.ClientListData;
import com.mobilespark.master.WebUtils.VolleyController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TaskMonitor extends AppCompatActivity implements ClientResponse {

    private static final String TAG = "TaskMonitor";
    private ListView activeServerslist;
    private ListView fallbackServerslist;
    private ArrayList<ClientListData> clientData;
    private ArrayList<ClientListData> activeClientData;
    private ArrayList<ClientListData> fallbackClientData;
    private Map<String, Integer> activeClientMap;
    private Map<String, Integer> fallbackClientMap;
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
        fallbackClientData = new ArrayList<>();
        activeClientMap = new HashMap<>();
        fallbackClientMap = new HashMap<>();


        while (i < Slaves && i < clientData.size())
            activeClientData.add(clientData.get(i++));


        while (i < clientData.size())
            fallbackClientData.add(clientData.get(i++));


        ClientListAdapter activeServerAdapter = new ClientListAdapter(this, activeClientData);
        activeServerslist.setAdapter(activeServerAdapter);

        ClientListAdapter fallbackServerAdapter = new ClientListAdapter(this, fallbackClientData);
        fallbackServerslist.setAdapter(fallbackServerAdapter);

        Handler handler = new Handler();
        handler.postDelayed(task, 2000);

        _assignTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
                ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();


                for(int count = 0 ; count < activeClientData.size();count++) {
                    try {
                        String url = "http://" + activeClientData.get(count).clientIp + ":8080/calculate";
                        VolleyController volleyController = new VolleyController(getApplicationContext());
                        JSONObject body = new JSONObject();
                        body.put("startX", 0);
                        body.put("endX", 4);
                        body.put("startY", 5);
                        body.put("endY", 9);
                        volleyController.makeRequest(url, body, TaskMonitor.this);
                    }
                    catch (Throwable t) {
                        Log.e(TAG, "Well that's not good.", t);
                    }
                }



                //fallbackServerslist.setBackgroundColor(Color.BLUE);
                //activeServerslist.setBackgroundColor(Color.parseColor("#9fe7ff"));
                //((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
            }
        });

    }

    private Runnable task = new Runnable() {
        public void run() {
            for (int count = 0; count < activeClientData.size(); count++) {
                activeServerslist.getChildAt(count).setBackgroundColor(Color.YELLOW);
                activeClientMap.put(activeClientData.get(count).clientIp, count);
            }
            for (int count = 0; count < fallbackClientData.size(); count++) {
                fallbackServerslist.getChildAt(count).setBackgroundColor(Color.GRAY);
                fallbackClientMap.put(fallbackClientData.get(count).clientIp, count);
            }
        }
    };


    @Override
    public void onSuccess(JSONObject jsonObject) {
        String clientIp = "Empty";

        try {
            clientIp = (String) jsonObject.get("ip");

        } catch (JSONException e) {
            Log.e(TAG, "onSuccess: " + "Could not pass JSON");
        }

        int index = activeClientMap.get(clientIp);
        activeServerslist.getChildAt(index).setBackgroundColor(Color.GREEN);

    }

    @Override
    public void onFailure(VolleyError error) {

    }
}
