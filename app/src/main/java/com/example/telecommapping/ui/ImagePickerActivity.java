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

import android.os.Bundle;

public class ImagePickerActivity extends AppCompatActivity {


    private static final int IMAGE_PICK_CODE = 1000;
    private static final int GALLERY_REQUEST_CODE =  123;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private ImageView imageView;
    Button sendSuggestionButton,photoButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);
        imageView = findViewById(R.id.imageView);


        photoButton = findViewById(R.id.photo_picker);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            ==PackageManager.PERMISSION_DENIED){
                        requestPermission();
                    }else {
//permission granted
                        pickImage();
                    }
                }else {
                    //os issue
                    pickImage();
                }


            }
        });
        sendSuggestionButton = findViewById(R.id.send_suggestion1);
        sendSuggestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast=Toast. makeText(getApplicationContext(),"Suggestion Sent",Toast. LENGTH_SHORT);
                toast. show();

            }
        });
    }//Oncreateends


    private void pickImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_CODE);
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                PERMISSION_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    pickImage();
                }else {
                    Toast toast=Toast. makeText(getApplicationContext(),"Permission Denied",Toast. LENGTH_SHORT);
                    toast. show();
                }
                break;

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null){

            Uri imageData = data.getData();
            imageView.setImageURI(imageData);
        }
    }
}