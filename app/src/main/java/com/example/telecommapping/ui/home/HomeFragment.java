package com.example.telecommapping.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.Button;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
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
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
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
import com.mmi.services.api.Place;
import com.mmi.services.api.PlaceResponse;
import com.mmi.services.api.autosuggest.AutoSuggestCriteria;
import com.mmi.services.api.autosuggest.model.ELocation;
import com.mmi.services.api.nearby.MapmyIndiaNearby;
import com.mmi.services.api.nearby.model.NearbyAtlasResponse;
import com.mmi.services.api.nearby.model.NearbyAtlasResult;
import com.mmi.services.api.reversegeocode.MapmyIndiaReverseGeoCode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

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
    Polyline polyline;
    SupportMapFragment mapFragment = null;
    private List<LatLng> latLngList = new ArrayList<>();
    String[] range = { "5 km", "10 km", "15 km"};

    private final String url = "https://hackelite.herokuapp.com/places";

//    Icon icon = new Icon(R.drawable.blue2_marker);


    private boolean hidden = true;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mCircularReveal = root.findViewById(R.id.reveal_items);
        mCircularReveal.setVisibility(View.GONE);;
        progressBar = root.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);
        location = new Location("");
        location.setLatitude(28.7041);
        location.setLongitude(77.1025);
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
        Fragment fragment = this;
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        hideRevealView();
        switch (v.getId()){
            case R.id.towers_button:
//                showdialog();
                progressBar.setVisibility(View.VISIBLE);
                getTowers("");

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
//                get_places("wifi");
                try {
                    get_places_clone("wifi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

        }

    }

    public void showdialog(){
        String[] arrayList = new String[23];
        arrayList[0] = "Ambala";
        arrayList[1] = "Bhiwani";
        arrayList[2] = "Charkhi";
        arrayList[3] = "Dadri";
        arrayList[4] = "Faridabad";
        arrayList[5] = "Fatehabad";
        arrayList[6] = "Gurugram";
        arrayList[7] = "Hisar";
        arrayList[8] = "Jhajjar";
        arrayList[9] = "Jind";
        arrayList[10] = "Kaithal";
        arrayList[11] = "Karnal";
        arrayList[12] = "Kurukshetra";
        arrayList[13] = "Mahendragarh";
        arrayList[14] = "Nuh";
        arrayList[15] = "Palwal";
        arrayList[16] = "Panchkula";
        arrayList[17] = "Panipat";
        arrayList[18] = "Rewari";
        arrayList[19] = "Rohtak";
        arrayList[20] = "Sirsa";
        arrayList[21] = "Sonipat";
        arrayList[22] = "Yamunanagar";

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(getContext());
        //alt_bld.setIcon(R.drawable.icon);
        alt_bld.setTitle("Select a Group Name");
        alt_bld.setSingleChoiceItems(arrayList, -1, new DialogInterface
                .OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getContext(),
                        "Group Name = "+arrayList[item], Toast.LENGTH_SHORT).show();
                dialog.dismiss();// dismiss the alertbox after chose option
                getTowers(arrayList[item]);
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
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
    public void getLatLng(String address){
        Geocoder geocoder =  new Geocoder(getContext());
        List<Address> addresses;
        LatLng p1 = null;
        try {
            // May throw an IOException
            addresses = geocoder.getFromLocationName(address, 5);
            if (address == null) {
//                return null
            }

            Address location = addresses.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }
//        return p1;
        Toast.makeText(getContext(), "Found Address"+p1, Toast.LENGTH_LONG).show();

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getTowers(String name){
        InputStream inputStream = getClass().getResourceAsStream("/assets/govtdata.csv");
        List<LocationModel> resultList = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            HashMap<LatLng, Double> hash_for_distance = new HashMap<>();
            HashMap<LatLng, String> hash_normal = new HashMap<>();
            String csvLine;
            int i =0 ;
            while ((csvLine = reader.readLine()) != null) {
                if(i>0) {
                    String[] row = csvLine.split(",");
                    double lat = Double.parseDouble(row[3]);
                    double lng = Double.parseDouble(row[4]);
                    Location locationA = new Location("point A");
                    locationA.setLatitude(lat);
                    locationA.setLongitude(lng);
                    Location locationB = new Location("Ambala");
                    locationB.setLongitude(location.getLongitude());
                    locationB.setLatitude(location.getLatitude());
                    double distance = locationA.distanceTo(locationB);
                    Log.i("VALUE OF DISTANCE", "getTowers: "+distance);
                    hash_normal.put(new LatLng(locationA.getLatitude(), locationA.getLongitude()), row[5]);
                    hash_for_distance.put(new LatLng(locationA.getLatitude(), locationA.getLongitude()), distance);
//                    if (row[1].equals(name)) {
//                        Log.i("LATLNG", "getTowers: " + csvLine);
//                        resultList.add(new LocationModel(Double.parseDouble(row[2]), Double.parseDouble(row[3]), row[1]));
//                    }
                }
                i++;
            }
            hash_for_distance = sortByValue(hash_for_distance);
            ArrayList<LocationModel> locationModels = new ArrayList<>();

            for (Map.Entry<LatLng, Double> entry : hash_for_distance.entrySet())
            {
                Log.i("RESULT", " "+entry.getKey()+" value:"+entry.getValue()+" "+hash_normal.get(entry.getKey()));
                locationModels.add(new LocationModel().setTower(entry.getKey(), entry.getValue(),hash_normal.get(entry.getKey())));
            }
            showDialogFortowers(locationModels);
//            addMarkersForTowers(locationModels);
        } catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Error while closing input stream: " + e);
            }
        }

    }

    public void showDialogFortowers(ArrayList<LocationModel>array){
        progressBar.setVisibility(View.GONE);
        String[] arrayList = new String[array.size()];
        for(int i = 0; i<array.size();i++){
            arrayList[i] = "Tower No: "+(i+1)+" ,name: "+array.get(i).name+" ,distance = "+array.get(i).distance+" m";
        }
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(getContext());
        //alt_bld.setIcon(R.drawable.icon);
        alt_bld.setTitle("Select a Telecom infrastructure");
        alt_bld.setSingleChoiceItems(arrayList, -1, new DialogInterface
                .OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(getContext(),
                        "Group Name = "+arrayList[item], Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                addMarkersForTowers(array);
// dismiss the alertbox after chose option
            }
        });
        AlertDialog alert = alt_bld.create();
        alert.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        HashMap<K, V> result = new LinkedHashMap<>();
        int i = 0;
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
            if(i==5){
                break;
            }
            i++;
        }

        return result;
    }

    public void addMarkersForTowers(List<LocationModel> list){
        if (mapmyIndiaMap != null) {
            mapmyIndiaMap.clear();
            IconFactory iconFactory = IconFactory.getInstance(getContext());
            Icon icon;

            Log.i("LOCATION", "addMarkersForTowers: "+list.size());
            for(int i=0; i<list.size();i++){

                LatLng latLng = new LatLng(list.get(i).lat, list.get(i).lng);
                if(list.get(i).name.equalsIgnoreCase("LPBTS"))   {
                    icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_blue_2));
                }
                else if(list.get(i).name.equalsIgnoreCase("RTP"))   {
                    icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_darkblue_2));
                }
                else if(list.get(i).name.equalsIgnoreCase("RTT"))   {
                     icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_green_2));
                }
                else if(list.get(i).name.equalsIgnoreCase("GBM"))   {
                     icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_orange_2));
                }
                else if(list.get(i).name.equalsIgnoreCase("GBT"))   {
                    icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_pink_2));
                }
                else if(list.get(i).name.equalsIgnoreCase("COW(GBT)"))   {
                    icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_purple_2));
                }
                else if(list.get(i).name.equalsIgnoreCase("Wall Mount"))   {
                    icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_red_2));
                }
                else    {
                    icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_person));
                }
                Marker marker = mapmyIndiaMap.addMarker(new MarkerOptions().position(latLng)
                        .title(list.get(i).name).snippet("tower"+i+"\n distance= "+list.get(i).distance).icon(icon));

            }

//            getLatLng(list.get(0).radio);
            icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_person));
            mapmyIndiaMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Picked Location").icon(icon));
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12));

        }
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
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
            points.add(new LocationModel().setLocation(place.getLatitude(), place.getLongitude(), place.getPlaceAddress(), place.getPlaceName()));
        }
        addOverLay(points, false);
    }


    void addOverLay(List<LocationModel> list, boolean showInfo) {
        if (mapmyIndiaMap != null) {
            mapmyIndiaMap.clear();
            for(int i=0; i<list.size();i++){
                LatLng latLng = new LatLng(list.get(i).lat, list.get(i).lng);

                Marker marker = mapmyIndiaMap.addMarker(new MarkerOptions().position(latLng).title(list.get(i).name).snippet(list.get(i).address));


            }
            mapmyIndiaMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 11));

        }
    }


    @Override
    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//        locationEngine.requestLocationUpdates();

    }

    @Override
    public void onLocationChanged(Location location) {
//        this.location = location;
//        this.location.setLatitude(28.551087);
//        this.location.setLongitude(77.257373);
//        currentLocation = this.location;
//        mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.551087, 77.257373), 9));
//        locationEngine.removeLocationEngineListener(this);
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

        mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),9));

        mapmyIndiaMap.setPadding(20, 20, 20, 20);

        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {
//            enableLocation();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }

        mapmyIndiaMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {

              //Toast.makeText(getActivity(), marker.getPosition().toString(), Toast.LENGTH_LONG).show();
                LatLng circle_center = new LatLng(marker.getPosition().getLatitude(),marker.getPosition().getLongitude());
                String tower_type = marker.getTitle();
                double circle_radius = 0;
                if(tower_type.equalsIgnoreCase("LPBTS"))   {
                    circle_radius = 1.0;
                }
                else if(tower_type.equalsIgnoreCase("RTP"))   {
                    circle_radius = 2.0;
                }
                else if(tower_type.equalsIgnoreCase("RTT"))   {
                    circle_radius = 3.0;
                }
                else if(tower_type.equalsIgnoreCase("GBM"))   {
                    circle_radius = 1.0;
                }
                else if(tower_type.equalsIgnoreCase("GBT"))   {
                    circle_radius = 4.0;
                }
                else if(tower_type.equalsIgnoreCase("COW(GBT)"))   {
                    circle_radius = 2.0;
                }
                else if(tower_type.equalsIgnoreCase("Wall Mount"))   {
                    circle_radius = 2.0;
                }
                Log.i("Tower Type : ", tower_type+"\t\t"+"Coverage Radius : "+circle_radius);
                addCircle(circle_center,circle_radius);
                return false;
            }
        });

//        Pick Custom location
        mapmyIndiaMap.setCameraPosition(setCameraAndTilt());


        mapmyIndiaMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                mapmyIndiaMap.clear();
                    reverseGeocode(latLng.getLatitude(), latLng.getLongitude());
                    addcustMarker(latLng.getLatitude(), latLng.getLongitude());


            }
        });

    }
//    pick Custom loc
protected CameraPosition setCameraAndTilt() {
    CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(
            28.551087, 77.257373)).zoom(14).tilt(0).build();
    return cameraPosition;
}

    private void reverseGeocode(Double latitude, Double longitude) {
        MapmyIndiaReverseGeoCode.builder()
                .setLocation(latitude, longitude)
                .build().enqueueCall(new Callback<PlaceResponse>() {
            @Override
            public void onResponse(Call<PlaceResponse> call, Response<PlaceResponse> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        List<Place> placesList = response.body().getPlaces();
                        Place place = placesList.get(0);
                        String add = place.getFormattedAddress();
                        Toast.makeText(getContext(), add, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Not able to get value, Try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), response.message(), Toast.LENGTH_LONG).show();
                }


            }
            @Override
            public void onFailure(Call<PlaceResponse> call, Throwable t) {
                Toast.makeText(getContext(), t.toString(), Toast.LENGTH_LONG).show();
            }
        });

    }
//    pick custom loc
private void addcustMarker(double latitude, double longitude) {
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        IconFactory iconFactory = IconFactory.getInstance(getContext());
        Icon icon = iconFactory.fromBitmap(getBitmap(R.drawable.ic_marker_person));
        mapmyIndiaMap.addMarker(new MarkerOptions().position(new LatLng(
            latitude, longitude)).icon(icon));
//        mapmyIndiaMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Picked Location"));
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
        if(location!=null){
            mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),5));
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

    private void addCircle(LatLng latLng, double radius)
    {
        double R = 6371d; // earth's mean radius in km
        double d = radius/R; //radius given in km
        double lat1 = Math.toRadians(latLng.getLatitude());
        double lon1 = Math.toRadians(latLng.getLongitude());
        PolygonOptions options = new PolygonOptions();
        for (int x = 0; x <= 360; x++)
        {
            double brng = Math.toRadians(x);
            double latitudeRad = Math.asin(Math.sin(lat1)*Math.cos(d) + Math.cos(lat1)*Math.sin(d)*Math.cos(brng));
            double longitudeRad = (lon1 + Math.atan2(Math.sin(brng)*Math.sin(d)*Math.cos(lat1), Math.cos(d)-Math.sin(lat1)*Math.sin(latitudeRad)));
            options.add(new LatLng(Math.toDegrees(latitudeRad), Math.toDegrees(longitudeRad)));
            options.fillColor(Color.parseColor("#0FFF0000"));
            options.strokeColor(Color.parseColor("#FFFF0000"));
        }
        mapmyIndiaMap.addPolygon(options);

        if(polyline != null)    {
            mapmyIndiaMap.removePolyline(polyline);
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(new LatLng(location.getLatitude(),location.getLongitude()));
        polylineOptions.add(latLng);
        polyline = mapmyIndiaMap.addPolyline(polylineOptions.color(Color.BLUE).width(2));
    }
}