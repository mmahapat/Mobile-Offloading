package com.mobilespark.master;

import com.android.volley.VolleyError;

import org.json.JSONObject;

public interface ClientResponse {
    public void onSuccess(JSONObject jsonObject);
    public void onFailure(VolleyError error);
}
