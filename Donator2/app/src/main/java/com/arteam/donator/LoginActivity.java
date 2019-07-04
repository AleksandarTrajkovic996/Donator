package com.arteam.donator;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener{

    private EditText emailTxt;
    private EditText passwordTxt;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
        emailTxt = (EditText) findViewById(R.id.emailLoginTxt);
        passwordTxt = (EditText) findViewById(R.id.passwordLoginTxt);
        ConstraintLayout loginLay = (ConstraintLayout) findViewById(R.id.constraintLogin);

        loginLay.setOnClickListener(this);
        passwordTxt.setOnKeyListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    public void logIN(View view){

        if (validation()) {
                this.userLogin();
        }
    }

    public void registerAccount(View view){
        startActivity(new Intent(LoginActivity.this, RegisterAccountActivity.class));
    }

    public void userLogin(){

        String email = emailTxt.getText().toString();
        String lozinka = passwordTxt.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email,lozinka)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(task.isSuccessful()){

                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();

                        }else{
                            Toast.makeText(LoginActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                        }

                    }
                });
        progressBar.setVisibility(View.INVISIBLE);
    }

    public boolean validateEmailAddress(String emailAddress) {

        Pattern regexPattern = Pattern.compile("^[(a-zA-Z-0-9-\\_\\+\\.)]+@[(a-z-A-z)]+\\.[(a-zA-z)]{2,3}$");
        Matcher regMatcher   = regexPattern.matcher(emailAddress);
        if(regMatcher.matches()) {
            return true;
        } else {
            return false;
        }
    }

    public boolean validation(){
        if(passwordTxt.getText().toString().isEmpty() && emailTxt.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.sviPodaciLogin, Toast.LENGTH_SHORT).show();
            return false;
        } else if(emailTxt.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.emailLogin, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(passwordTxt.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.lozinkaLoginAct, Toast.LENGTH_SHORT).show();
            return false;
        }else if(!validateEmailAddress(emailTxt.getText().toString())){
            Toast.makeText(this, R.string.emailValidacija, Toast.LENGTH_SHORT).show();
            return false;
        }
        return  true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.constraintLogin || v.getId() == R.id.loginImageView){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
            logIN(v);
        }
        return false;
    }


}
