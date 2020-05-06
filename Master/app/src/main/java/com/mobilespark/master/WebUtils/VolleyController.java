package com.mobilespark.master.WebUtils;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mobilespark.master.ClientResponse;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class VolleyController {
    private static VolleyController mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;
    public static final int timeout = 30;

    public VolleyController(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyController getInstance(Context context) {
        // If instance is not available, create it. If available, reuse and return the object.
        if (mInstance == null) {
            mInstance = new VolleyController(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key. It should not be activity context,
            // or else RequestQueue won’t last for the lifetime of your app
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public void addToRequestQueue(Request req) {
        getRequestQueue().add(req);
    }

    public void makeRequest(String url, JSONObject body, final ClientResponse clientResponse, final String identifier) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, body, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        clientResponse.onSuccess(response, identifier);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        clientResponse.onFailure(error, identifier);
                    }
                });
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                (int) TimeUnit.SECONDS.toMillis(timeout),
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        addToRequestQueue(jsonObjectRequest);
    }

}
