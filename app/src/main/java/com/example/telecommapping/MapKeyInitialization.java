package com.example.telecommapping;

import android.app.Application;

import com.mapbox.mapboxsdk.MapmyIndia;
import com.mmi.services.account.MapmyIndiaAccountManager;

public class MapKeyInitialization extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        MapmyIndiaAccountManager.getInstance().setRestAPIKey("zop5xx1fd65nyqaz47my7cc7k66bgcsr");
        MapmyIndiaAccountManager.getInstance().setMapSDKKey("zuvwr8o4es4cipcywawfxkmtp4hhrl8r");
        MapmyIndiaAccountManager.getInstance().setAtlasClientId("YRMdfLcMlLIIM93eG9D5ms3nehMzxv-TplTnnTY68bwcNCU7k2-G65p7ciBcfiZf7jMEgxYWwyKICg9kv3KZzg==");
        MapmyIndiaAccountManager.getInstance().setAtlasClientSecret("ebEc8GH231deOymil5QAZQkzJdXrNHfXofuV30k2vdZIIP5GGwFEbM7iESnboxcVXl-uFVyaZ7Nao9ejrGYCwLmbWCk5rrLv");
        MapmyIndiaAccountManager.getInstance().setAtlasGrantType("client_credentials");
        MapmyIndia.getInstance(this);
    }
}
