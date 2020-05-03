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
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private int Slaves;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_monitor);
        clientData = ClientList.clientData;
        Slaves = getIntent().getExtras().getInt("Slaves");

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
        handler.postDelayed(loaddata, 2000);

        _assignTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
                ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();


                for(int count = 0 ; count < activeClientData.size();count++) {
                    addtoTaskqueue(count);
                }

                Handler handler = new Handler();
                handler.postDelayed(updatestatus, 3000);


                //((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
            }
        });

    }
    private void addtoTaskqueue(int  count){
        try {
            String url = "http://" + activeClientData.get(count).clientIp + ":8080/calculate";
            url = "http://192.168.0.6:8080/calculate";
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
    private Runnable loaddata = new Runnable() {
        public void run() {
            for (int count = 0; count < activeClientData.size(); count++) {
                activeServerslist.getChildAt(count).setBackgroundColor(Color.YELLOW);
                activeClientMap.put(activeClientData.get(count).clientIp, new int[]{count,0});
            }
            for (int count = 0; count < fallbackClientData.size(); count++) {
                fallbackServerslist.getChildAt(count).setBackgroundColor(Color.GRAY);
                fallbackClientMap.put(fallbackClientData.get(count).clientIp, new int[]{count,0});
            }
        }
    };


    private Runnable updatestatus = new Runnable() {
        public void run() {
            // Mark failed servers as red based on timeout
            for (int count = 0; count < activeClientData.size(); count++) {
                int[] data = activeClientMap.get(activeClientData.get(count).clientIp);

                if(data[1] == 0){
                    activeServerslist.getChildAt(data[0]).setBackgroundColor(Color.RED);
                }
            }

            Handler handler = new Handler();
            handler.postDelayed(fallback, 3000);

        }
    };

    private Runnable fallback = new Runnable() {
        public void run() {

            //Check number of failed Servers
            List<Integer> activeServers = new ArrayList<>();
            for(String key: activeClientMap.keySet()){
                int[]data = activeClientMap.get(key);
                if(data[1] == 1)
                    activeServers.add(data[0]);
            }
            //Move fallbackClientData  into activeClientData
            for(int i = 0 ; i < (Slaves-activeServers.size()) && i < fallbackClientData.size(); i++){
                int pos = activeClientData.size();
                activeClientData.add(fallbackClientData.get(i));
                activeClientMap.put(fallbackClientData.get(i).clientIp, new int[]{pos,0});
                fallbackClientMap.remove(fallbackClientData.get(i).clientIp);
                fallbackClientData.remove(i);

                //activeServerslist.getChildAt(pos).setBackgroundColor(Color.YELLOW);


                addtoTaskqueue(pos);

            }

            ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
            ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();
            Handler handler = new Handler();
            handler.postDelayed(updatestatus, 3000);


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

        if(activeClientMap.containsKey(clientIp)) {
            int[] data = activeClientMap.get(clientIp);
            activeServerslist.getChildAt(data[0]).setBackgroundColor(Color.GREEN);
            activeClientMap.put(clientIp,new int[]{data[0],1});
        }

    }

    @Override
    public void onFailure(VolleyError error) {
        System.out.println("Error"+ error);

    }
}
