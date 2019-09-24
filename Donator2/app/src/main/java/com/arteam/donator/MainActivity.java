package com.arteam.donator;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private Intent serviceIntent;
    private NecessaryArticlesService service;

    private Intent serviceLocationIntent;
    private LocationService loactionService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        serviceIntent = new Intent(this, NecessaryArticlesService.class);
        service = new NecessaryArticlesService();

        serviceLocationIntent = new Intent(this, LocationService.class);
        loactionService = new LocationService();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        serviceIntent = new Intent(this, NecessaryArticlesService.class);
        service = new NecessaryArticlesService();

        if (isMyServiceRunning(loactionService.getClass())) {
            stopService(serviceLocationIntent);
        }

        if (isMyServiceRunning(service.getClass())) {
            stopService(serviceIntent);
        }

        if (user == null) { //ako ne postoji user

            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
            finish();

        } else {

            final String uID = user.getUid();

            firebaseFirestore.collection("Users").document(uID).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                            if (task.isSuccessful()) {
                                if (task.getResult().exists()) {//postoji user, ima osnovne podatke

                                    startService(serviceLocationIntent);
                                    startService(serviceIntent);
                                    startActivity(new Intent(MainActivity.this, NavigationMainActivity.class));
                                    finish();
                                } else {//postoji user, nema nikakvih podataka o sebi
                                    Intent ne = new Intent(MainActivity.this, RegisterPersonalInfoActivity.class);
                                    startActivity(ne);
                                    finish();
                                }

                            } else {

                                Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }


    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
