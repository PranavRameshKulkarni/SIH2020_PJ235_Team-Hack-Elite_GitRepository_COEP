package com.example.telecommapping;

import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class FetchData {

    public Double lat, lng;
    public int radius = 10000;
    public String query;
    private final String url = "https://hackelite.herokuapp.com/places";
    private JSONArray jsonObject;


    public FetchData(Double lat, Double lng, String query){
        this.lat = lat;
        this.lng = lng;
        this.query = query;
    }
    public FetchData(Double lat, Double lng, String query, int radius){
        this.lat = lat;
        this.lng = lng;
        this.query = query;
        this.radius = radius;
    }

    public JSONArray fetchData(){
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        requestParams.put("lat", lat);
        requestParams.put("lng", lng);
        requestParams.put("query", query);
        requestParams.put("radius", radius);
        client.addHeader("Authorization","Token e37a46932ad72db1a61f972d96c4d5ab85f96b9199ac8bf977" );
        client.get(url,requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    jsonObject = new JSONArray(new String(responseBody));
                    Log.i("JDON", "on success"+jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                jsonObject = new JSONArray();
                Log.i("ERROR", String.valueOf(error));
            }
        });
        return jsonObject;
    }
}
