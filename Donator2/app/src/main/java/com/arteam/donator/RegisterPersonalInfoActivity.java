package com.arteam.donator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterPersonalInfoActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener{

    private static final int PERMISSIONS = 1;
    private EditText firstName;
    private EditText lastName;
    private EditText country;
    private EditText address;
    private EditText phoneNumber;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private FusedLocationProviderClient fusedLocationClient;
    private final String TAG = "RegisterActivity";
    private LatLng latLng;
    private boolean isEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_personal_info);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.constraintRegisterPersonalInfo);
        progressBar = (ProgressBar) findViewById(R.id.progressBarPersonalInfo);
        firstName = (EditText) findViewById(R.id.firstNameTxt);
        lastName = (EditText) findViewById(R.id.lastNameTxt);
        country = (EditText) findViewById(R.id.countryTxt);
        address = (EditText) findViewById(R.id.addressTxt);
        phoneNumber = (EditText) findViewById(R.id.phoneNumberTxt);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        layout.setOnClickListener(this);
        phoneNumber.setOnKeyListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user==null){
            startActivity(new Intent(RegisterPersonalInfoActivity.this, MainActivity.class));
            finish();
        }

    }

    public void createUser(View view){
        if(validation()){
            checkPerm();
        }
    }

    public void checkPerm(){

        if (ActivityCompat.checkSelfPermission(RegisterPersonalInfoActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RegisterPersonalInfoActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(RegisterPersonalInfoActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS);

        }else{
            getLastLoc();
            create();
            startService(new Intent(RegisterPersonalInfoActivity.this, LocationService.class));
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(RegisterPersonalInfoActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(RegisterPersonalInfoActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        getLastLoc();
                        create();
                        startService(new Intent(RegisterPersonalInfoActivity.this, LocationService.class));
                    }

                }else{
                    Toast.makeText(getApplication(), "You need to enable finding locations!", Toast.LENGTH_SHORT).show();
                    create();
                }

            }
        }
    }

    public void create(){

        String userID = mAuth.getCurrentUser().getUid();

        String first_name_user = firstName.getText().toString();
        String last_name_user = lastName.getText().toString();
        String country_user = country.getText().toString();
        String address_user = address.getText().toString();
        String phone_number_user = phoneNumber.getText().toString();
        String lat = null;
        String lon = null;
        if(latLng!=null) {
            lat = String.valueOf(latLng.latitude);
            lon = String.valueOf(latLng.longitude);
        }else{
            lat = "43.314140";
            lon = "21.895238";
        }
        Map<String, String> mapObjekat = new HashMap<>();

        mapObjekat.put("first_name", first_name_user);
        mapObjekat.put("last_name", last_name_user);
        mapObjekat.put("country", country_user);
        mapObjekat.put("address", address_user);
        mapObjekat.put("phone_number", phone_number_user);
        mapObjekat.put("points", "0");
        mapObjekat.put("latitude", lat);
        mapObjekat.put("longitude", lon);

        progressBar.setVisibility(View.VISIBLE);

        firebaseFirestore.collection("Users").document(userID).set(mapObjekat)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()){
                            startActivity(new Intent(RegisterPersonalInfoActivity.this, MainActivity.class));
                        }else{
                            Toast.makeText(RegisterPersonalInfoActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });


    }

    @SuppressLint("MissingPermission")
    private void getLastLoc() {
        fusedLocationClient.getLastLocation()
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplication(), "Last known location is null!", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Last known location is null!");
                    }
                })
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location==null){
                            Toast.makeText(getApplication(), "Last known location is null!", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Last known location is null!");
                        }else{
                            Log.i(TAG, String.valueOf(location.getLatitude()));
                            Log.i(TAG, String.valueOf(location.getLongitude()));

                            LatLng l = new LatLng(location.getLatitude(),location.getLongitude());
                            latLng = l;
                        }
                    }
                });
    }

    public boolean validation(){

        if(firstName.getText().toString().isEmpty() && lastName.getText().toString().isEmpty()  && phoneNumber.getText().toString().isEmpty()
                && address.getText().toString().isEmpty() && country.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.sviPodaciReg, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(firstName.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.imeReg2, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(lastName.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.prezimeReg2, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(country.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.countryRegPersonal, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(address.getText().toString().isEmpty()){
              Toast.makeText(this, R.string.addressRegPersonal, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(phoneNumber.getText().toString().isEmpty()){
              Toast.makeText(this, R.string.phoneRegPersonal, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void cancelRegistration(View view){
        startActivity(new Intent(RegisterPersonalInfoActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.constraintRegisterPersonalInfo){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
            createUser(v);
        }
        return false;
    }


}
