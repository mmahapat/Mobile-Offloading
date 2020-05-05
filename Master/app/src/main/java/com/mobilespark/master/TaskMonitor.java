package com.mobilespark.master;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Collections;
import java.util.Comparator;
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
    private int slaves;
    private int[][] inputMatrixA;
    private int[][] inputMatrixB;
    private int[][] outputMatrix;
    //key: "0-250" value: "S": Success, "F": Failure, "P": Pending
    private Map<String,String> outputMatrixStatusMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_monitor);
        clientData = ClientList.clientData;
        slaves = getIntent().getExtras().getInt("Slaves");

        activeServerslist = findViewById(R.id.activeServers);
        fallbackServerslist = findViewById(R.id.fallbackServers);
        _assignTaskButton = findViewById(R.id.assignbutton);
        _mergeTaskButton = findViewById(R.id.mergebutton);

        int i = 0;

        activeClientData = new ArrayList<>();
        fallbackClientData = new ArrayList<>();
        activeClientMap = new HashMap<>();
        fallbackClientMap = new HashMap<>();

        //Sort in Decreasing battery power
        Collections.sort(clientData,new batterySort());
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


                ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
                ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();


                for(int count = 0 ; count < activeClientData.size();count++) {
                    addtoTaskqueue(count);
                }

                Handler handler = new Handler();
                handler.postDelayed(updatestatus, 3000);

                //Initiate Matrix and Status Map
                initliaizeMatrixParams(1000);
                //((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
            }
        });

        _mergeTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mergeMatrix();
            }
        });

    }

    public void mergeMatrix(){

    }
    public void initliaizeMatrixParams(int size){
        inputMatrixA = GenerateMatrix.createMatrix(size,size);
        inputMatrixB = GenerateMatrix.createMatrix(size,size);
        outputMatrixStatusMap = new HashMap<>();
        int start = 0;
        int end = 0;
        int range = size / slaves;
        int reminder = size % slaves;
        for(int i = 0; i < slaves ; i++) {
            if( i == 0)
                end += range + reminder;

            end += range;
            outputMatrixStatusMap.put(String.valueOf(start)+"-"+String.valueOf(end), "F");
            start = end;
        }

    }

    public View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition ) {
            return listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
    private void addtoTaskqueue(int  count){
        try {
            String clientIp = activeClientData.get(count).clientIp;
            String url = "http://" + clientIp + ":8080/calculate";
//            url = "http://192.168.0.6:8080/calculate";
            VolleyController volleyController = new VolleyController(getApplicationContext());
            JSONObject body = new JSONObject();
//            body.put("startX", 0);
////            body.put("endX", 4);
////            body.put("startY", 5);
////            body.put("endY", 9);
            int start = 0;
            int end = 0;
            StringBuilder inputParameter = new StringBuilder(clientIp);
            for(String key : outputMatrixStatusMap.keySet()){
                if(outputMatrixStatusMap.get(key).equals("F")){
                    inputParameter.append(" ");
                    inputParameter.append(key);
                    String[] parts = key.split("-");
                    start = Integer.parseInt(parts[0]);
                    end = Integer.parseInt(parts[0]);
                    break;
                }
            }
            GenerateMatrix.splitMatrixHorizontally(start,end,inputMatrixA);
            volleyController.makeRequest(url, body, TaskMonitor.this, inputParameter.toString());
        }
        catch (Throwable t) {
            Log.e(TAG, "Well that's not good.", t);
        }
    }
    private Runnable loaddata = new Runnable() {
        public void run() {
            for (int count = 0; count < activeClientData.size(); count++) {
                //activeServerslist.getChildAt(count).setBackgroundColor(Color.YELLOW);
                getViewByPosition(count,activeServerslist).setBackgroundColor(Color.YELLOW);
                activeClientMap.put(activeClientData.get(count).clientIp, new int[]{count,0});
            }
            for (int count = 0; count < fallbackClientData.size(); count++) {
                //fallbackServerslist.getChildAt(count).setBackgroundColor(Color.GRAY);
                getViewByPosition(count,fallbackServerslist).setBackgroundColor(Color.GRAY);
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
                    //activeServerslist.getChildAt(data[0]).setBackgroundColor(Color.RED);
                    getViewByPosition(data[0],activeServerslist).setBackgroundColor(Color.RED);
                }
            }
            ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
            ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();

            Handler handler = new Handler();
            handler.postDelayed(fallback, 3000);

        }
    };
    private Runnable updatestatus2 = new Runnable() {
        public void run() {
            // Mark failed servers as red based on timeout
            for (int count = 0; count < activeClientData.size(); count++) {
                int[] data = activeClientMap.get(activeClientData.get(count).clientIp);

                if(data[1] == 0){
                    //activeServerslist.getChildAt(data[0]).setBackgroundColor(Color.RED);
                    getViewByPosition(data[0],activeServerslist).setBackgroundColor(Color.RED);

                }
            }
            ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
            ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();

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
            for(int i = 0; i < (slaves -activeServers.size()) && i < fallbackClientData.size(); i++){
                int pos = activeClientData.size();
                activeClientData.add(fallbackClientData.get(0));
                activeClientMap.put(fallbackClientData.get(0).clientIp, new int[]{pos,0});
                fallbackClientMap.remove(fallbackClientData.get(0).clientIp);
                fallbackClientData.remove(0);

                //activeServerslist.getChildAt(pos).setBackgroundColor(Color.YELLOW);


                addtoTaskqueue(pos);

            }

            ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
            ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();
            Handler handler = new Handler();
            handler.postDelayed(updatestatus2, 3000);


        }
    };

    public void updateMatrix(JSONObject jsonObject, String identifier){
        // Update the output Matrix
        outputMatrixStatusMap.put(identifier,"S");
        for(String val : outputMatrixStatusMap.values()){
            if(val.equals("P") || val.equals("F"))
                break;
            //If all rows are filled merge matrix;
            mergeMatrix();
        }

    }

    @Override
    public void onSuccess(JSONObject jsonObject, String identifier) {
        String clientIp = "Empty";

        try {
            clientIp = (String) jsonObject.get("ip");

        } catch (JSONException e) {
            Log.e(TAG, "onSuccess: " + "Could not pass JSON");
        }

        if(activeClientMap.containsKey(clientIp)) {
            int[] data = activeClientMap.get(clientIp);
            //activeServerslist.getChildAt(data[0]).setBackgroundColor(Color.GREEN);
            getViewByPosition(data[0],activeServerslist).setBackgroundColor(Color.GREEN);
            activeClientMap.put(clientIp,new int[]{data[0],1});
        }

        ((ClientListAdapter) (activeServerslist.getAdapter())).notifyDataSetChanged();
        ((ClientListAdapter) (fallbackServerslist.getAdapter())).notifyDataSetChanged();

        updateMatrix(jsonObject, identifier);

    }

    @Override
    public void onFailure(VolleyError error, String identifier) {
        System.out.println("Error"+ error);

    }

    class batterySort implements Comparator<ClientListData>{

        @Override
        public int compare(ClientListData t1, ClientListData t2) {
            return (int) (Float.parseFloat(t2.batteryPercentage) - Float.parseFloat(t1.batteryPercentage));
        }
    }
}
