package com.example.telecommapping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class CheckPermissions extends AppCompatActivity {
    String[] PERMISSIONS_REQUIRED = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_permissions);
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean permissionGranted = true;

        if (grantResults.length > 0) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PERMISSION_GRANTED) {
                    permissionGranted = false;
                    break;
                }
            }
        } else {
            //PERMISSION REQ
            permissionGranted = false;
        }

        if (!permissionGranted) {
            new AlertDialog.Builder(this).setTitle("Give permissions!!")
                    .setPositiveButton("OK", (dialog, which) -> requestPermissions()).show();
        } else {
            redirect();
        }
    }

    void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, PERMISSIONS_REQUIRED[0]) == PERMISSION_GRANTED) {
            redirect();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS_REQUIRED[0])) {
                //showPermissionRequiredDialog
                new AlertDialog.Builder(this).setTitle("Please give the permissions")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(CheckPermissions.this, PERMISSIONS_REQUIRED, 100)).show();
            } else {
                //askPermission
                ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, 100);
            }
        }
    }
    private void redirect() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            startActivity(new Intent(getApplicationContext(), NavigationActivity.class));
            finish();
        }, 5);
    }
}