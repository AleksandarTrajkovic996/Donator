package com.arteam.donator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class NavigationMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private TextView fullNameNavigationMain;
    private TextView emailNavigationMain;
    private ImageView profileImage;
    FragmentManager fragmentManager =getSupportFragmentManager();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private byte[] bytesImg = null;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        fullNameNavigationMain = hView.findViewById(R.id.fullNameTxt);
        emailNavigationMain = hView.findViewById(R.id.emailNavigationMainTxt);
        profileImage = hView.findViewById(R.id.imageViewNavigationMainProfil);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_ranking);


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
                                final User u = task.getResult().toObject(User.class);
                                user = u;
                                fullNameNavigationMain.setText(firstNameTmp + " " + lastNameTmp);
                            }else{

                                Toast.makeText(NavigationMainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });

        storageReference.child("profile_images/" + userID).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                bytesImg = bytes;
                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                profileImage.setImageBitmap(bm);
            }
        }) ;

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

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
            MapsFragment mapsFragment = new MapsFragment();
            mapsFragment.setUser(user);

            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, mapsFragment)
                    .commit();
        } else if (id == R.id.nav_request) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new RequestFragment())
                    .commit();
        } else if (id == R.id.nav_profile) {

            if(bytesImg!=null) {
                Bundle bundle = new Bundle();
                bundle.putByteArray("profileImg", this.bytesImg);
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);

                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, profileFragment)
                        .commit();
            }else{
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, new ProfileFragment())
                        .commit();
            }
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
