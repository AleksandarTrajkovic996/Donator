package com.arteam.donator;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;


public class LocationService extends Service {

    private final String TAG = "LocationService";
    private FusedLocationProviderClient fusedLocationClient;
    private boolean isRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "LocationService created!");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        isRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.i(TAG, "LocationService destroyed!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Log.i(TAG, "LocationService started!");

        if(fusedLocationClient==null){
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    Looper.prepare();

                    getLocation();

                    Looper.loop();
                }
            }
        });

        thread.start();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void getLocation() {

        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(3000);
        mLocationRequestHighAccuracy.setSmallestDisplacement(100);
        mLocationRequestHighAccuracy.setFastestInterval(1000);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "stopping the location service.");

            return;
        }

        Log.d(TAG, "getting location information.");
        fusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {

                        Log.d(TAG, "got location result.");

                        Location location = locationResult.getLastLocation();

                        if (location != null) {
                            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                            saveUserLocaiton(geoPoint);
                        }
                    }
                },
                Looper.myLooper());
    }

    protected void saveUserLocaiton(GeoPoint geoPoint){

        String lat = String.valueOf(geoPoint.getLatitude());
        String lon = String.valueOf(geoPoint.getLongitude());

        if(FirebaseSingleton.getInstance().mAuth.getCurrentUser()!=null) {
            FirebaseSingleton.getInstance().firebaseFirestore.collection("Users")
                    .document(FirebaseSingleton.getInstance().mAuth.getCurrentUser().getUid())
                    .update("latitude", lat,
                            "longitude", lon)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Neuspesno azuriranje!");
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Uspesno azuriranje!");
                        }
                    });
        }
    }

}
