package com.mobilespark.slave;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {
    private static final String TAG = "Server";
    private Context applicationContext;

    Server(Context applicationContext) throws IOException {
        super(8080);
//        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
//        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
        this.applicationContext = applicationContext;
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
                    case "/status":
                        return status();
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

    private Response status() {
        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = applicationContext.registerReceiver(null, iFilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level * 100 / (float) scale;
        String msg = "{\"Battery\": " + batteryPct + "}";
        return newFixedLengthResponse(Response.Status.OK, "application/json", msg);
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
