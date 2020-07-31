package com.example.telecommapping.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.MapmyIndia.getApplicationContext;

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
    SupportMapFragment mapFragment = null;
    private List<LatLng> latLngList = new ArrayList<>();
    String[] range = { "5 km", "10 km", "15 km"};



    private boolean hidden = true;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mCircularReveal = root.findViewById(R.id.reveal_items);
        mCircularReveal.setVisibility(View.GONE);
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
        Toast.makeText(getApplicationContext(),range[position] , Toast.LENGTH_LONG).show();
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
                get_places("telephone exchange");
                break;
            case R.id.csc_button:
                get_places("telecommunication services");
                break;
            case R.id.wifi_button:
                get_places("WiFi");
                break;

        }

    }

    public void getTowers(){
        InputStream inputStream = getClass().getResourceAsStream("/assets/fianldemorange.csv");
        List<LocationModel> resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            int i =0 ;
            while ((csvLine = reader.readLine()) != null) {
                if(i>0) {
                    String[] row = csvLine.split(",");
                    Log.i("LATLNG", "getTowers: "+csvLine);
                    resultList.add(new LocationModel(Double.parseDouble(row[3]),Double.parseDouble(row[2]), row[0]));
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
            for(int i=0; i<list.size();i++){
                LatLng latLng = new LatLng(list.get(i).lat, list.get(i).lng);

                Marker marker = mapmyIndiaMap.addMarker(new MarkerOptions().position(latLng).title(list.get(i).radio).snippet("tower"));


            }
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 12));

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
                            .setLocation(Double.parseDouble(lat), Double.parseDouble(lng))
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
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 12));

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