package com.arteam.donator;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;


public class NavigationMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CHECK_SETTINGS = 2;
    private static final String TAG = "NavigationMainActivity";
    private FirebaseAuth mAuth;
    private TextView fullNameNavigationMain;
    private TextView emailNavigationMain;
    private ImageView profileImage;
    FragmentManager fragmentManager =getSupportFragmentManager();
    private FirebaseFirestore firebaseFirestore;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private User user;
    private Context ctx = this;
    public static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private NotificationManager notifManager;
    private final int NOTIFY_ID = 123321; // ID of notification

    private Intent serviceIntent;
    private NecessaryArticlesService service;
    private Intent serviceLocationIntent;
    private LocationService loactionService;

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

        serviceIntent = new Intent(this, NecessaryArticlesService.class);
        service = new NecessaryArticlesService();

        serviceLocationIntent = new Intent(this, LocationService.class);
        loactionService = new LocationService();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        fullNameNavigationMain = hView.findViewById(R.id.fullNameTxt);
        emailNavigationMain = hView.findViewById(R.id.emailNavigationMainTxt);
        profileImage = hView.findViewById(R.id.imageViewNavigationMainProfil);

        registerIntents();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_ranking);

        emailNavigationMain.setText(mAuth.getCurrentUser().getEmail());

        String tappedUserId = getIntent().getStringExtra("userID");

        if(tappedUserId==null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new RankingFragment())
                    .commit();
        }else{ //korisnik je kliknuo na notifikaciju
            Bundle bundle = new Bundle();
            bundle.putString("userID", tappedUserId);
            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setArguments(bundle);
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, profileFragment)
                    .commit();
        }

        this.fillData(new UserCallback() {
            @Override
            public void onCallback(User u) {
                user = u;
            }
        });
    }

    private void registerIntents(){
        IntentFilter mainFilter = new IntentFilter("friends.filter");
        IntentFilter filterAccepted = new IntentFilter("friendship.accepted");
        IntentFilter filterDenied = new IntentFilter("friendship.denied");


        BroadcastReceiver receiverAccepted = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String userID = intent.getStringExtra("userID");
                String myID = FirebaseSingleton.getInstance().mAuth.getUid();
                makeFriendship(myID, userID); //dodajem sebi njega
                makeFriendship(userID, myID); //dodajem njemu sebe
                notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                notifManager.cancel(123321);
            }
        };

        BroadcastReceiver receiverDenied = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                notifManager.cancel(123321);
            }
        };


        BroadcastReceiver receiverMain = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String userID = intent.getStringExtra("userID");
                String userFirstName = intent.getStringExtra("first_name");
                String userLastName = intent.getStringExtra("last_name");
                createNotification(userID, userFirstName, userLastName, ctx);
            }
        };
        registerReceiver(receiverMain, mainFilter);
        registerReceiver(receiverAccepted, filterAccepted);
        registerReceiver(receiverDenied, filterDenied);
    }

    private void makeFriendship(String myID, String friendsID){
        HashMap<String, String> map = new HashMap<>();
        map.put("friendID", friendsID);

        FirebaseSingleton.getInstance().firebaseFirestore.collection("Users")
                .document(myID)
                .collection("Friends")
                .document(friendsID)
                .set(map);
    }

    private void createNotification(String userID, String userFirstName, String userLastName, Context context){

        String id = NOTIFICATION_CHANNEL_ID; // default_channel_id
        String title = "TITLE"; // Default Channel
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notifManager == null) {
            notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, FriendsFragment.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Intent acceptIntent = new Intent("friendship.accepted");
            Intent denyIntent = new Intent("friendship.denied");

            acceptIntent.putExtra("userID", userID);

            pendingIntent = PendingIntent.getBroadcast(ctx, 1002, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(ctx, 1003, denyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentTitle("Friend request!")                            // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder);   // required
            builder.setContentText(userFirstName + " " + userLastName + " wants to be your friend!")
                    .setDefaults(android.app.Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            builder.addAction(R.drawable.common_google_signin_btn_text_dark_normal_background, "Deny!", pendingIntent2);
            builder.addAction(R.drawable.common_google_signin_btn_text_dark_normal_background, "Accept!", pendingIntent);
        } else {
            Intent acceptIntent = new Intent("friendship.accepted");
            Intent denyIntent = new Intent("friendship.denied");
            acceptIntent.putExtra("userID", userID);
            pendingIntent = PendingIntent.getBroadcast(ctx, 1002, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent pendingIntent2 = PendingIntent.getBroadcast(ctx, 1003, denyIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            builder = new NotificationCompat.Builder(ctx, NOTIFICATION_CHANNEL_ID )
                    .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                    .setContentTitle("Friend request")
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            builder.addAction(R.drawable.common_google_signin_btn_text_dark_normal_background, "Accept!", pendingIntent);
            builder.addAction(R.drawable.common_google_signin_btn_text_dark_normal_background, "Deny!", pendingIntent2);
            builder.setContentText(userFirstName + " " + userLastName + " wants to be your friend!");

            int NOTIFICATION_ID = 101;
            android.app.Notification n = builder.build();
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(ctx);
            notificationManagerCompat.notify(NOTIFICATION_ID, n);
        }
        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);

    }
    

    private void fillData(final UserCallback userCallback){
        String userID = mAuth.getUid();


        firebaseFirestore.collection("Users")
                .document(userID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        String firstNameTmp = documentSnapshot.getString("first_name");
                        String lastNameTmp = documentSnapshot.getString("last_name");
                        String userID = documentSnapshot.getId();
                        User u = documentSnapshot.toObject(User.class).withId(userID,0);
                        fullNameNavigationMain.setText(firstNameTmp + " " + lastNameTmp);

                        userCallback.onCallback(u);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            loadPicture(mAuth.getCurrentUser().getUid());
        }
    }

    private void loadPicture(String uid){

        storageReference.child("profile_images/" + uid)
                .getDownloadUrl()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Neuspesno ucitavanje profilne slike!");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        if (uri!=null) {
                            Log.i("profile_images", "Profilna slika skinuta!");
                            Glide.with(getApplicationContext())
                                    .load(uri)
                                    .into(profileImage);
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
                    .replace(R.id.nav_main, new DonateFragment(), "Donate_Fragment_TAG")
                    .commit();
        } else if (id == R.id.nav_necessary) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new NecessaryFragment())
                    .commit();
        } else if (id == R.id.nav_map) {

            MapsFragment mapsFragment = new MapsFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, mapsFragment)
                    .commit();

        } else if (id == R.id.nav_request) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new RequestFragment())
                    .commit();
        } else if (id == R.id.nav_notification) {
            fragmentManager.beginTransaction()
                    .replace(R.id.nav_main, new NotificationFragment())
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
            stopService(serviceLocationIntent);
            stopService(serviceIntent);
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }


        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        stopService(serviceLocationIntent);
        stopService(serviceIntent);

        super.onDestroy();
    }
}
