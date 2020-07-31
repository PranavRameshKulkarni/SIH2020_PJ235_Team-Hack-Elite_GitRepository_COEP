package com.example.telecommapping.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.telecommapping.LocationModel;
import com.example.telecommapping.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapmyindia.sdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapmyindia.sdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment;
import com.mapmyindia.sdk.plugins.places.autocomplete.ui.PlaceSelectionListener;
import com.mmi.services.api.autosuggest.AutoSuggestCriteria;
import com.mmi.services.api.autosuggest.model.ELocation;
import com.mmi.services.api.nearby.MapmyIndiaNearby;
import com.mmi.services.api.nearby.model.NearbyAtlasResponse;
import com.mmi.services.api.nearby.model.NearbyAtlasResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class HomeFragment extends Fragment implements OnMapReadyCallback, PermissionsListener, LocationEngineListener,View.OnClickListener,   AdapterView.OnItemSelectedListener{
    private MapboxMap mapmyIndiaMap;
    private TextView autoSuggestText;;
    private Handler handler;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private LocationEngine locationEngine;
    private Location location, currentLocation;
    private FloatingActionButton fab;
    private LinearLayout mCircularReveal;
    private ImageButton  wifi_btn, towers_button, telephone_exchange_btn, csc_button;
    private ProgressBar progressBar;
    private FrameLayout frameLayout;
    SupportMapFragment mapFragment = null;
    private List<LatLng> latLngList = new ArrayList<>();
    String[] range = { "5 km", "10 km", "15 km"};

    private final String url = "https://hackelite.herokuapp.com/places";


    private boolean hidden = true;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mCircularReveal = root.findViewById(R.id.reveal_items);
        mCircularReveal.setVisibility(View.GONE);;
        progressBar = root.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        progressBar.getIndeterminateDrawable().setColorFilter(0xF2f20000,
                android.graphics.PorterDuff.Mode.MULTIPLY);
        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(mapFragment == null) {
            mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_view);
            mapFragment.getMapAsync(this);
        }
        autoSuggestText = view.findViewById(R.id.auto_suggest);
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentLocationCamera();
            }
        });
        handler = new Handler();
        autoSuggestText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (location != null) {
                    PlaceOptions placeOptions = PlaceOptions.builder()
                            .location(Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                            .pod(AutoSuggestCriteria.POD_CITY)
                            .backgroundColor(ContextCompat.getColor(getContext(), android.R.color.white))
                            .build();
                    PlaceAutocompleteFragment placeAutocompleteFragment = PlaceAutocompleteFragment.newInstance(placeOptions);
                    placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                        @Override
                        public void onPlaceSelected(ELocation eLocation) {

                            autoSuggestText.setText(eLocation.placeName);
                            if (mapmyIndiaMap != null) {
                                mapmyIndiaMap.clear();
                                location = new Location("");
                                location.setLatitude(Double.parseDouble(eLocation.latitude));
                                location.setLongitude(Double.parseDouble(eLocation.longitude));
                                LatLng latLng = new LatLng(Double.parseDouble(eLocation.latitude), Double.parseDouble(eLocation.longitude));
                                mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                mapmyIndiaMap.addMarker(new MarkerOptions().position(latLng).title(eLocation.placeName).snippet(eLocation.placeAddress));
                                getActivity().getSupportFragmentManager().popBackStack(PlaceAutocompleteFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        }

                        @Override
                        public void onCancel() {
                            getActivity().getSupportFragmentManager().popBackStack(PlaceAutocompleteFragment.class.getSimpleName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    });


                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_home, placeAutocompleteFragment, PlaceAutocompleteFragment.class.getSimpleName())
                            .addToBackStack(PlaceAutocompleteFragment.class.getSimpleName())
                            .commit();
                } else {
                    Toast.makeText(getContext(), "Please wait for getting current location", Toast.LENGTH_SHORT).show();
                }
                return false;
            }});
        wifi_btn = getActivity().findViewById(R.id.wifi_button);
        towers_button = getActivity().findViewById(R.id.towers_button);
        telephone_exchange_btn = getActivity().findViewById(R.id.telephone_exchange_button);
        csc_button = getActivity().findViewById(R.id.csc_button);
        wifi_btn.setOnClickListener(this);
        towers_button.setOnClickListener(this);
        telephone_exchange_btn.setOnClickListener(this);
        csc_button.setOnClickListener(this);
        BottomNavigationView bottomNavigationView = getActivity().findViewById(R.id.bottom_nav_view);
        bottomNavigationView.setOnNavigationItemReselectedListener(new BottomNavigationView.OnNavigationItemReselectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.nav_explore){
                    hideRevealView();
                }
            }
        });
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                Log.i("tag", "keyCode: " + keyCode);
                if( keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    Log.i("tag", "onKey Back listener is working!!!");
                    if(mCircularReveal.getVisibility()==View.VISIBLE){
                        mCircularReveal.setVisibility(View.GONE);
                        fab.setVisibility(View.VISIBLE);
                    }
                    else {
                        getChildFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        System.exit(1);
                    }
                    return true;
                }
                return true;
            }
        });

        //spinner
        Spinner spin = getActivity().findViewById(R.id.spinner);
        spin.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) this);

        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this.getActivity(),android.R.layout.simple_spinner_item, range);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spin.setAdapter(aa);
        //spinner ends


    }
    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
        Toast.makeText(getContext(),range[position] , Toast.LENGTH_LONG).show();
    }
    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }


















    @Override
    public void onClick(View v) {
        hideRevealView();
        switch (v.getId()){
            case R.id.towers_button:
                getTowers();

                break;
            case R.id.telephone_exchange_button:
//                get_places("bsnl");
                try {
                    get_places_clone("telephone exchange");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.csc_button:
//                get_places("seva");
                try {
                    get_places_clone("common service center");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.wifi_button:
                try {
                    get_places_clone("wifi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

        }

    }

    public void get_places_clone(String data) throws JSONException {
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getContext(), "Fetching data", Toast.LENGTH_LONG).show();
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams requestParams = new RequestParams();
        requestParams.put("lat", location.getLatitude());
        requestParams.put("lng", location.getLongitude());
        requestParams.put("query", data);
        requestParams.put("radius", 10000);
        client.addHeader("Authorization","Token e37a46932ad72db1a61f972d96c4d5ab85f96b9199ac8bf977" );
        client.get(url,requestParams, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONArray jsonObject = new JSONArray(new String(responseBody));
                    List<LocationModel> loc = new ArrayList<>();
                    for(int i=0; i< jsonObject.length(); i++){
                        JSONObject obj = jsonObject.getJSONObject(i);
                        String name = obj.getString("name");
                        Double la = obj.getDouble("lat");
                        Double ln = obj.getDouble("lng");
                        String addr = obj.getString("formatted_address");
                        loc.add(new LocationModel().setLocation(la, ln, addr, name));
                    }
                    addMarkersFor(loc);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("ERROR", String.valueOf(error));
            }
        });

    }

    public void addMarkersFor(List<LocationModel>list){
        progressBar.setVisibility(View.GONE);
        Log.i("INSIDE MARKER", "addMarkersFor: success");
        if (mapmyIndiaMap != null) {
            for(int i=0; i<list.size();i++){
                LatLng latLng = new LatLng(list.get(i).lat, list.get(i).lng);
                mapmyIndiaMap.addMarker(new MarkerOptions().position(latLng).title(list.get(i).name).snippet(list.get(i).address));
            }
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 9));

        }
    }

    public void getTowers(){
        InputStream inputStream = getClass().getResourceAsStream("/assets/Haryanatowers.csv");
        List<LocationModel> resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            int i =0 ;
            while ((csvLine = reader.readLine()) != null) {
                if(i>0) {
                    String[] row = csvLine.split(",");
                    if (row[1].equals("Yamunanagar")) {
                        Log.i("LATLNG", "getTowers: " + csvLine);
                        resultList.add(new LocationModel(Double.parseDouble(row[2]), Double.parseDouble(row[3]), row[1]));
                    }
                }
                i++;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }
        addMarkersForTowers(resultList);

    }

    public void addMarkersForTowers(List<LocationModel> list){
        if (mapmyIndiaMap != null) {
            mapmyIndiaMap.clear();
//            IconFactory iconFactory = IconFactory.getInstance(getContext());
//            Icon icon = iconFactory.fromResource(R.drawable.tower);
            for(int i=0; i<list.size();i++){
                LatLng latLng = new LatLng(list.get(i).lat, list.get(i).lng);

                Marker marker = mapmyIndiaMap.addMarker(new MarkerOptions().position(latLng).title(list.get(i).radio).snippet("tower"));


            }
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(list.get(0).lat, list.get(0).lng), 7));

        }
    }



    public void get_places(String keyword)    {
        try {
            String keywords = keyword;
            String lat = "18.5204"; //+currentLocation.getLatitude();
            String lng = "73.8567"; //+currentLocation.getLongitude();

            if (lat.length() > 0 && lng.length() > 0) {
                if ((keywords != null && keywords.length() > 0)) {
                    MapmyIndiaNearby.builder()
                            .setLocation(location.getLatitude(), location.getLongitude())
                            .radius(5000)
                            .keyword(keywords)
                            .build()
                            .enqueueCall(new Callback<NearbyAtlasResponse>() {
                                @Override
                                public void onResponse(Call<NearbyAtlasResponse> call, Response<NearbyAtlasResponse> response) {

                                    if (response.code() == 200) {
                                        if (response.body() != null) {
                                            ArrayList<NearbyAtlasResult> nearByList = response.body().getSuggestedLocations();
                                            if (nearByList.size() > 0) {
                                                Toast.makeText(getContext(), ""+nearByList.size(), Toast.LENGTH_LONG).show();
                                                addOverLays(nearByList);
                                            }
                                        } else {
                                            Toast.makeText(getContext(), "Not able to get value, Try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getContext(), response.message(), Toast.LENGTH_LONG).show();
                                    }
                                }


                                @Override
                                public void onFailure(Call<NearbyAtlasResponse> call, Throwable t) {
                                    Log.i("Error", "Error while fetching", t);
                                }
                            });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addOverLays(ArrayList<NearbyAtlasResult> places) {
        ArrayList<LocationModel> points = new ArrayList<>();

        for (NearbyAtlasResult place : places) {
            points.add(new LocationModel(place.getLatitude(), place.getLongitude(), place.getPlaceName()));
        }
        addOverLay(points, false);
    }


    void addOverLay(List<LocationModel> list, boolean showInfo) {
        if (mapmyIndiaMap != null) {
            mapmyIndiaMap.clear();
            for(int i=0; i<list.size();i++){
                LatLng latLng = new LatLng(list.get(i).lat, list.get(i).lng);

                Marker marker = mapmyIndiaMap.addMarker(new MarkerOptions().position(latLng).title(list.get(i).radio).snippet("tower"));


            }
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

        }
    }


    @Override
    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationEngine.requestLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        currentLocation = location;
        setCurrentLocationCamera();
        mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
        locationEngine.removeLocationEngineListener(this);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapmyIndiaMap = mapboxMap;


        mapmyIndiaMap.setPadding(20, 20, 20, 20);

        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
            enableLocation();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }


        mapmyIndiaMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

              Toast.makeText(getActivity(), marker.getPosition().toString(), Toast.LENGTH_LONG).show();

                return false;
            }
        });

    }

    @Override
    public void onMapError(int i, String s) {

    }

    private void enableLocation() {
        LocationComponentOptions options = LocationComponentOptions.builder(getContext())
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(getContext(), R.color.colorAccent))
                .build();
        locationComponent = mapmyIndiaMap.getLocationComponent();
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationComponent.activateLocationComponent(getContext(), options);
        locationComponent.setLocationComponentEnabled(true);
        locationEngine = locationComponent.getLocationEngine();

        locationEngine.addLocationEngineListener(this);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    void setCurrentLocationCamera(){
        if(currentLocation!=null){
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),15));
        }
    }

    private void hideRevealView() {
        if (mCircularReveal.getVisibility() == View.VISIBLE) {
            mCircularReveal.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            hidden = true;
        }
        else{
            mCircularReveal.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
        }
    }


}