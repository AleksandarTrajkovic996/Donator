package com.arteam.donator;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class NavigationMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private TextView fullNameNavigationMain;
    private TextView emailNavigationMain;
    FragmentManager fragmentManager =getSupportFragmentManager();
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_ranking);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        NavigationView nv = (NavigationView) findViewById(R.id.nav_view);
        View hView =  nv.getHeaderView(0);


        fullNameNavigationMain = hView.findViewById(R.id.fullNameTxt);
        emailNavigationMain = hView.findViewById(R.id.emailNavigationMainTxt);


        emailNavigationMain.setText(mAuth.getCurrentUser().getEmail());

        fragmentManager.beginTransaction()
                .replace(R.id.nav_main, new RankingFragment())
                .commit();

        this.fillData();
    }

    private void fillData(){
        String userID = mAuth.getUid();


        firebaseFirestore.collection("Users").document(userID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if(task.isSuccessful()){

                            if(task.getResult().exists()){

                                String firstNameTmp = task.getResult().getString("first_name");
                                String lastNameTmp = task.getResult().getString("last_name");

                                 fullNameNavigationMain.setText(firstNameTmp + " " + lastNameTmp);
                            }else{

                                Toast.makeText(NavigationMainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }else if(id == R.id.action_profile) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_ranking) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new RankingFragment())
                    .commit();
        } else if (id == R.id.nav_donate) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new DonateFragment())
                    .commit();
        } else if (id == R.id.nav_necessary) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new NecessaryFragment())
                    .commit();
        } else if (id == R.id.nav_map) {
            Toast.makeText(this, "MAPA", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_request) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new RequestFragment())
                    .commit();
        } else if (id == R.id.nav_profile) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new ProfileFragment())
                    .commit();
        } else if (id == R.id.nav_friends) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new FriendsFragment())
                    .commit();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
