package com.mobilespark.slave;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {
    private static final String TAG = "Server";
    private Context applicationContext;
    private String ip;
    private TextView master;

    Server(Context applicationContext, String ip, TextView master) throws IOException {
        super(8080);
//        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
//        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        this.applicationContext = applicationContext;
        this.ip = ip;
        this.master = master;
    }


    @Override
    public Response serve(IHTTPSession session) {
        Method method = session.getMethod();
        String uri = session.getUri();
        Log.d(TAG, "serve: " + uri);
        switch (method) {
            case GET:
                /*
                Write all the GET APIS here
                Using uri, redirect to different methods and return from here
                */
                switch (uri) {
                    case "/":
                        return newFixedLengthResponse("<div>Came to a get request</div>" + uri);
                }
                return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, NanoHTTPD.MIME_PLAINTEXT, "HTTP " + uri);
            case POST:
                Map<String, String> bodyParams = readBodyParams(session);
                /*
                Write all the POST APIS here
                Using uri, redirect to different methods and return from here
                */
                switch (uri) {
                    case "/ping":
                        return ping(bodyParams);
                    case "/unregister":
                        return unRegister();
                    case "/status":
                        return status(bodyParams);
                    case "/calculate":
                        return calculateMatrixMultiplication(bodyParams);
                }
                return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, NanoHTTPD.MIME_PLAINTEXT, "HTTP " + uri);
            default:
                Log.e(TAG, "serve: " + "Error in serving");
                return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, NanoHTTPD.MIME_PLAINTEXT, "HTTP " + method);
        }
    }

    private Response unRegister() {
        changeStatusOfClient(null);
        return newFixedLengthResponse("ok");
    }

    private Response calculateMatrixMultiplication(Map<String, String> bodyParams) {

        BatteryManager bm = (BatteryManager) applicationContext.getSystemService(Context.BATTERY_SERVICE);
        int startX = 0;
        int endX = 4;
        int startY = 5;
        int endY = 9;
        Gson gson = new Gson();
        JSONObject response = new JSONObject();
        int[][] matrixA = gson.fromJson(bodyParams.get("A"), int[][].class);
        int[][] matrixB = gson.fromJson(bodyParams.get("B"), int[][].class);
        Log.e(TAG, "calculateMatrixMultiplication: " + matrixA);

        int initPower = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        long start = Calendar.getInstance().getTimeInMillis();
        int[][] result = MatrixMultiplication.multiply(matrixA, matrixB);
        long end = Calendar.getInstance().getTimeInMillis();
        int finalPower = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);

        String finalResult = gson.toJson(result);
        float powerConsumed = ( initPower - finalPower ) / 3600;

        try {
            response.put("power_consumed", powerConsumed);
            response.put("execution_time", end - start);
            response.put("result", finalResult);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
    }

    private Response status(Map<String, String> bodyParams) {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = applicationContext.registerReceiver(null, iFilter);
        String model = Build.MODEL;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        String masterIP = bodyParams.get("ip");
        changeStatusOfClient(masterIP);
        float batteryPct = level * 100 / (float) scale;
        JSONObject response = new JSONObject();
        try {
            response.put("deviceName", model);
            response.put("battery", String.valueOf(batteryPct));
            response.put("ip", ip);
        } catch (JSONException e) {
            Log.e(TAG, "status: " + e.getMessage());
        }

        return newFixedLengthResponse(Response.Status.OK, "application/json", response.toString());
    }

    private void changeStatusOfClient(String masterIP) {
        Log.e(TAG, "changeStatusOfClient: " + masterIP);
        if (masterIP == null) {
            master.setText("Not Connected");
            master.setTextColor(Color.parseColor("#bf1f1f"));
        } else {
            String text = "Connected to : " + masterIP;
            master.setText(text);
            master.setTextColor(Color.parseColor("#10b542"));
        }
    }

    private Response ping(Map<String, String> bodyParams) {
        return newFixedLengthResponse("Came to ping" + bodyParams);
    }

    private Map<String, String> readBodyParams(IHTTPSession session) {
        HashMap<String, String> postData = new HashMap<>();
        try {
            session.parseBody(postData);
        } catch (Exception e) {
            Log.d(TAG, "serve: " + e.getMessage());
        }
        String postBody = postData.get("postData");
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return new Gson().fromJson(postBody, type);
    }
}
