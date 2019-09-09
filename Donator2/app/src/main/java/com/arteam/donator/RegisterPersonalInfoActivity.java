package com.arteam.donator;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterPersonalInfoActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener{

    private EditText firstName;
    private EditText lastName;
    private EditText country;
    private EditText address;
    private EditText phoneNumber;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

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
            create();
        }
    }

    public void create(){

        String userID = mAuth.getCurrentUser().getUid();

        String first_name_user = firstName.getText().toString();
        String last_name_user = lastName.getText().toString();
        String country_user = country.getText().toString();
        String address_user = address.getText().toString();
        String phone_number_user = phoneNumber.getText().toString();

        Map<String, String> mapObjekat = new HashMap<>();

        mapObjekat.put("first_name", first_name_user);
        mapObjekat.put("last_name", last_name_user);
        mapObjekat.put("country", country_user);
        mapObjekat.put("address", address_user);
        mapObjekat.put("phone_number", phone_number_user);
        mapObjekat.put("points", "0");

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
