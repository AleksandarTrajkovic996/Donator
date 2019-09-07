package com.arteam.donator;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if(user==null){ //ako ne postoji user

            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();

        }else{

            final String uID = user.getUid();

            firebaseFirestore.collection("Users").document(uID).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if(task.isSuccessful())
                            {
                                if(task.getResult().exists()) {//postoji user, ima osnovne podatke

                                    Intent service = new Intent(MainActivity.this, MainService.class);
                                    startService(service);
                                    startActivity(new Intent(MainActivity.this, NavigationMainActivity.class));
                                    finish();
                                    }

                                else {//postoji user, nema nikakvih podataka o sebi
                                    Intent ne = new Intent(MainActivity.this, RegisterPersonalInfoActivity.class);
                                    startActivity(ne);
                                    finish();
                                }

                            }else{

                                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }


    }




}
