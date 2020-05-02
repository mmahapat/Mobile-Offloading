package com.mobilespark.slave;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

import static android.content.Context.WIFI_SERVICE;

public class Server extends NanoHTTPD {
    private static final String TAG = "Server";
    private Context applicationContext;
    private String ip;

    Server(Context applicationContext, String ip) throws IOException {
        super(8080);
//        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
//        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        this.applicationContext = applicationContext;
        this.ip = ip;
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
                readBodyParams(session);
                Map<String, String> bodyParams = session.getParms();

                /*
                Write all the POST APIS here
                Using uri, redirect to different methods and return from here
                */
                switch (uri) {
                    case "/ping":
                        return ping(bodyParams);
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

    private Response calculateMatrixMultiplication(Map<String, String> bodyParams) {
        int startX = 0;
        int endX = 4;
        int startY = 5;
        int endY = 9;
        Gson gson = new Gson();
        JSONObject response = new JSONObject();

        int[][] result = MatrixMultiplication.multiply(startX, endX, startY, endY);
        String finalResult = gson.toJson(result);

        try {
            response.put("power_consumed", "12");
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

    private Response ping(Map<String, String> bodyParams) {
        return newFixedLengthResponse("Came to ping" + bodyParams);
    }

    private void readBodyParams(IHTTPSession session) {
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (Exception e) {
            Log.d(TAG, "serve: " + e.getMessage());
        }
        String postBody = session.getQueryParameterString();
        Log.d(TAG, "readBodyParams: " + postBody);
    }
}
