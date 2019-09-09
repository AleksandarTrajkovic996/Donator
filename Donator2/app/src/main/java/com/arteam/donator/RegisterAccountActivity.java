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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterAccountActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener{


    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_account);

        mAuth = FirebaseAuth.getInstance();

        progressBar = (ProgressBar) findViewById(R.id.progressBarRegisterAccount);
        email = (EditText) findViewById(R.id.emailRegisterTxt);
        password = (EditText) findViewById(R.id.passwordRegisterTxt);
        confirmPassword = (EditText) findViewById(R.id.confirmPasswordRegisterTxt);

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.constraintRegister);
        layout.setOnClickListener(this);
        confirmPassword.setOnKeyListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user!=null){
            startActivity(new Intent(RegisterAccountActivity.this, MainActivity.class));
            finish();
        }
    }

    public void registerAccount(View view){
        if(validation()){
            register();
        }
    }

    public void cancelRegistration(View view){
        startActivity(new Intent(RegisterAccountActivity.this, MainActivity.class));
        finish();
    }

    private void register(){

        String emailTmp = email.getText().toString();
        String lozinkaPotvrda = confirmPassword.getText().toString();

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(emailTmp, lozinkaPotvrda)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            startActivity(new Intent(RegisterAccountActivity.this, MainActivity.class));
                            finish();

                        } else {
                            Toast.makeText(RegisterAccountActivity.this, R.string.error, Toast.LENGTH_LONG).show();

                        }

                        progressBar.setVisibility(View.INVISIBLE);

                    }

                });


    }

    public boolean validation(){
        if(email.getText().toString().isEmpty() && password.getText().toString().isEmpty()
                && confirmPassword.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.sviPodaciReg, Toast.LENGTH_SHORT).show();
            return false;
        }else if(email.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.emailRegi, Toast.LENGTH_SHORT).show();
            return false;
        }else if(password.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.lozReg, Toast.LENGTH_SHORT).show();
            return false;
        }   else if(confirmPassword.getText().toString().isEmpty()){
            Toast.makeText(this, R.string.lozPotReg, Toast.LENGTH_SHORT).show();
            return false;
        }else if(password.getText().length() < 6){
            Toast.makeText(this, R.string.lozViseSest, Toast.LENGTH_SHORT).show();
            return false;
        }else if(!password.getText().toString().matches(confirmPassword.getText().toString())){
            Toast.makeText(this, R.string.lozPoklapanje, Toast.LENGTH_SHORT).show();
            return false;
        }else if(!validateEmailAddress(email.getText().toString())){
            Toast.makeText(this, R.string.emailValidacijaReg, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
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

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.constraintRegister){
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
            registerAccount(v);
        }
        return false;
    }
}
