package com.mobilespark.master;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class Server extends NanoHTTPD {
    private static final String TAG = "Server";

    Server() throws IOException {
        super(8080);
//        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
//        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
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
                }
                return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, NanoHTTPD.MIME_PLAINTEXT, "HTTP " + uri);
            default:
                Log.e(TAG, "serve: " + "Error in serving");
                return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, NanoHTTPD.MIME_PLAINTEXT, "HTTP " + method);
        }
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
