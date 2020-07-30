package com.example.telecommapping.ui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.telecommapping.R;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapmyindia.sdk.plugins.places.placepicker.PlacePicker;
import com.mapmyindia.sdk.plugins.places.placepicker.model.PlacePickerOptions;
import com.mmi.services.api.Place;

public class PickerActivity extends AppCompatActivity {
    private TextView tvSelectedPlace;

        private Button sendSuggestionButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        tvSelectedPlace = findViewById(R.id.selected_place);
        Button button = findViewById(R.id.place_picker);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new PlacePicker.IntentBuilder()
                        .placeOptions(PlacePickerOptions.builder()
                                .statingCameraPosition(new CameraPosition.Builder()
                                        .target(new LatLng(27.00, 75.0)).zoom(16).build())
                                .build()).build(PickerActivity.this);
                startActivityForResult(intent, 101);

            }
        });

//        Suggestions Btn
        sendSuggestionButton = findViewById(R.id.send_suggestion);
        sendSuggestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast=Toast. makeText(getApplicationContext(),"Suggestion Sent",Toast. LENGTH_SHORT);
                toast. show();

            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {

            Place place = PlacePicker.getPlace(data);
            tvSelectedPlace.setText(place.getFormattedAddress());


        }
    }
}
