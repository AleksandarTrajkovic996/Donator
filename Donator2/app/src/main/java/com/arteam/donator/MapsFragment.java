package com.arteam.donator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;


public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSION_ACCES_FINE_LOCATION = 1;
    private String SearchType = "necessary";
    private String SearchingFor = "donate";
    private List<Article> DonateArticles; //artikli koji su za doniranje
    private List<Article> NecessaryArticles; //artikli koji su potrebni
    private HashMap<Marker, String> markerPlaceIdMap;
    private LinearLayout linearLayoutScrol;
    private ConstraintLayout constraintBigMap;
    private ScrollView scrollViewMap;
    private int RadiusInKM = 0; // moze biti 30, 50 ili
    ArrayList<Article> tmp;
    private Button btnOK;
    private Button btnCancel;
    private String SearchedText = "";
    View view;
    GoogleMap googleMap;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private Button btnSearch;
    private EditText searchBar;
    private LatLng centerRadius = null;
    private User userMAIN;
    private static final String TAG = "MapsFragment";
    private String clickedArticleId ="";
    private String clickedUserID = "";
    private String clickedArticleName = "";
    private static boolean showFriends = false;
    private static boolean clickedMyArticle = false;
    private Map<Integer, User> listFriends;
    private Map<Integer, User> listFriends2;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";

    private LocationService locationService = new LocationService();

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static SecureRandom random = new SecureRandom();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);
        setHasOptionsMenu(true);

        btnSearch = view.findViewById(R.id.search_buttonMAPS);
        searchBar = view.findViewById(R.id.searchBarMaps);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();
        DonateArticles = new ArrayList<Article>();
        NecessaryArticles = new ArrayList<Article>();
        scrollViewMap = view.findViewById(R.id.scrolMap);
        constraintBigMap = view.findViewById(R.id.constraintBigMap);
        linearLayoutScrol = scrollViewMap.findViewById(R.id.linearLayoutScrollMap);
        btnCancel = view.findViewById(R.id.btnCancelOfferOrAskMaps);
        btnOK = view.findViewById(R.id.btnOfferOrAskMaps);

        listFriends = new HashMap<>();
        listFriends2 = new HashMap<>();

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.getArticles();

        this.getUser(new UserCallback() {
            @Override
            public void onCallback(User user) {

                LatLng l = new LatLng(Double.parseDouble(user.getLatitude()), Double.parseDouble(user.getLongitude()));
                centerRadius = l;
                userMAIN = user;
                Log.i(TAG, "Radius usera je: " + centerRadius.latitude + ", " + centerRadius.longitude);
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (constraintBigMap.getVisibility() == View.INVISIBLE) {
                        refresh();
                        searchDataAndType(searchBar.getText().toString(), SearchingFor);
                        SearchedText = searchBar.getText().toString();
                        if (RadiusInKM > 0) {
                            drawCircle(RadiusInKM); //crta se krug
                        }
                    } else {
                        Toast.makeText(getActivity(), "You need to close the window first!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constraintBigMap.setVisibility(View.INVISIBLE);
                linearLayoutScrol.removeAllViews();
                clickedArticleId="";
                clickedUserID = "";
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!clickedArticleId.isEmpty()) {

                    if(SearchType.matches("donate")){ //korisnik moze da donira

                        if(clickedMyArticle) {

                            offerArticle(clickedArticleId, clickedUserID);
                            clickedUserID = "";
                            constraintBigMap.setVisibility(View.INVISIBLE);
                            clickedMyArticle = false;
                        }else {
                            ArrayList<Article> articleArrayList = new ArrayList<>();
                            String[] cmp = clickedArticleName.split(", ");

                            for (Article a : DonateArticles) {
                                if (a.getName().matches(cmp[0]) && a.getSize().matches(cmp[1])) {
                                    articleArrayList.add(a);
                                }
                            }
                            if (articleArrayList.size() > 0) {
                                showArticles(articleArrayList);
                                btnOK.setText("SEND");
                                clickedMyArticle = true;
                            }else{
                                Toast.makeText(getActivity(), "You don't have that article!", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }else{ //korisnik moze da trazi
                        ArrayList<Article> articleArrayList = new ArrayList<>();
                        String[] cmp = clickedArticleName.split(", ");
                        boolean alreadyInNecessaryList = false;
                        for(Article a : NecessaryArticles){
                            if(a.getName().matches(cmp[0]) && a.getSize().matches(cmp[1])){
                                alreadyInNecessaryList = true;
                            }
                        }
                        askForArticle(clickedArticleId, clickedUserID, alreadyInNecessaryList);
                        clickedUserID = "";
                        constraintBigMap.setVisibility(View.INVISIBLE);
                    }
                }else{
                    Toast.makeText(getActivity(), "You have to choose one article!", Toast.LENGTH_SHORT).show();
                }
                clickedArticleId="";

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(this);

    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
         switch (requestCode) {

             case PERMISSION_ACCES_FINE_LOCATION: {

                 if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                     if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                             && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                         googleMap.setMyLocationEnabled(true);
                         getActivity().startService(new Intent(getActivity(),LocationService.class));
                     }
                 }

             }
         }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCES_FINE_LOCATION);

        }else{
            googleMap.setMyLocationEnabled(true);
            getActivity().startService(new Intent(getActivity(),LocationService.class));
        }

    }



    private void searchDataAndType(final String data, final String searchingForType){

        markerPlaceIdMap = new HashMap<Marker, String>();

        firebaseFirestore.collection("Users") //obilaze se svi useri
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            int i = 0;
                            //refresh();
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) { //foreach petlja kroz Users

                                final String userID = doc.getDocument().getId();
                                final User user1 = doc.getDocument().toObject(User.class).withId(userID, i);
                                ++i;

                                if (userID.matches(mAuth.getCurrentUser().getUid()))
                                    continue; //trebalo bi da preskoci trenutnu iteraciju i nastavi sa sledecom

                                if (searchingForType.matches("donate") || (searchingForType.matches("necessary") && isThere(data))) {

                                    firebaseFirestore.collection("Users/" + userID + "/Articles").whereEqualTo("type", SearchingFor).whereEqualTo("name", data)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                                    int con = queryDocumentSnapshots.size();

                                                    for (DocumentChange d : queryDocumentSnapshots.getDocumentChanges()) {
                                                        switch (d.getType()) {
                                                            case ADDED:
                                                                if (con > 0) {
                                                                    Log.i("Type", "Added");
                                                                    addMarker(user1);
                                                                }
                                                                break;
                                                            case REMOVED:
                                                                Log.i("Type", "Removed");
                                                                removeMarker(user1);
                                                                break;
                                                            case MODIFIED:
                                                                Log.i("Type", "Modified");
                                                                break;
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    });

    }

    private boolean isThere(String data){
        for(Article a : DonateArticles){
            if(a.getName().matches(data)){
                return true;
            }
        }

        return false;
    }

    private void getArticles(){ //vratice sve proizvode sa tipom necessary i donate;

        String userID = mAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users/" + userID + "/Articles")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String articleID = doc.getDocument().getId();
                                Article article = doc.getDocument().toObject(Article.class).withId(articleID, i);
                                if(article.getType().matches("necessary")){
                                    NecessaryArticles.add(article);
                                }else if(article.getType().matches("donate")){
                                    DonateArticles.add(article);
                                }
                            }
                        }
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_map_Donate){ //zeli da donira nesto, prikazuju se stvari koje su potrebne ostalima
            this.SearchType = "donate";
            this.SearchingFor = "necessary";
            this.showFriends = false;
            constraintBigMap.setVisibility(View.INVISIBLE);
            linearLayoutScrol.removeAllViews();
            refresh();
        }else if(id == R.id.action_map_Friends){
            this.showFriends = true;
            //this.SearchType = "friends";
            //this.searchFriends("nesto", "nista");

            this.getFriends(new FriendsListCallback() {
                @Override
                public void onCallback() {

                }

                @Override
                public void onCallback(Map<Integer, User> l) {

                    for(int i=0; i<l.size(); i++){
                        User u = l.get(i);
                        removeMarker(u);
                        addMarker(u);
                    }
                }
            });

            refresh();
        }else if(id==R.id.action_map_Necessary){ //trazi stvari, prikazuju se donatori
            this.SearchType = "necessary";
            this.SearchingFor = "donate";
            this.showFriends = false;
            constraintBigMap.setVisibility(View.INVISIBLE);
            linearLayoutScrol.removeAllViews();
            refresh();
        }else if(id==R.id.action_map_radius_30_km){ //pretrazivanje po radijusu do 30km
            this.RadiusInKM = 30;
            constraintBigMap.setVisibility(View.INVISIBLE);
            linearLayoutScrol.removeAllViews();
            refresh();
        }else if(id==R.id.action_map_radius_50_km){ //pretrazivanje po radijusu do 50km
            this.RadiusInKM = 50;
            constraintBigMap.setVisibility(View.INVISIBLE);
            linearLayoutScrol.removeAllViews();
            refresh();
        }else if(id==R.id.action_map_radius_100_km){ //pretrazivanje po radijusu do 100km
            this.RadiusInKM = 100;
            constraintBigMap.setVisibility(View.INVISIBLE);
            linearLayoutScrol.removeAllViews();
            refresh();
        }else if(id==R.id.action_map_radius_500_km){ //pretrazivanje po radijusu do 100km
            this.RadiusInKM = 500;
            constraintBigMap.setVisibility(View.INVISIBLE);
            linearLayoutScrol.removeAllViews();
            refresh();
        }else if(id==R.id.action_map_exit_radius){ //pretrazivanje po radijusu do 100km
            this.RadiusInKM = 0;
            constraintBigMap.setVisibility(View.INVISIBLE);
            linearLayoutScrol.removeAllViews();
            refresh();
        }

        return super.onOptionsItemSelected(item);
    } //prepraviti poziv f-je searchFriends!!!!!!!!!!!

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.navigation_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void getFriendsIDs(final FriendsListCallback friendsListCallback){

        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Friends")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String friendID = doc.getDocument().getId();
                                User friend = doc.getDocument().toObject(User.class).withId(friendID, i);
                                listFriends.put(i, friend);
                                i++;
                            }
                        }
                        friendsListCallback.onCallback();
                    }
                });

    }

    public void getFriends(final FriendsListCallback friendsListCallback) {

        markerPlaceIdMap = new HashMap<Marker, String>();

        getFriendsIDs(new FriendsListCallback() {
            @Override
            public void onCallback() {

                firebaseFirestore.collection("Users")
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                                int i = 0;
                                for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                    String friendID = doc.getDocument().getId();
                                    User friend = doc.getDocument().toObject(User.class).withId(friendID, i);
                                    switch (doc.getType()) {
                                        case REMOVED:
                                            Log.i(TAG, "REMOVED");
                                            break;
                                        case ADDED:
                                            for (int k = 0; k < listFriends.size(); k++) {
                                                if (friendID.matches(listFriends.get(k).getFriendID())){
                                                    Log.i(TAG, "ADDED" + friendID);
                                                    listFriends2.put(i, friend);
                                                    i++;
                                                }
                                            }
                                            break;
                                        case MODIFIED:
                                            for (int k = 0; k < listFriends2.size(); k++) {
                                                if (friendID.matches(listFriends2.get(k).userID)){
                                                    Log.i(TAG, "MODIFIED" + friendID);
                                                    listFriends2.get(k).setLatitude(friend.getLatitude());
                                                    listFriends2.get(k).setLongitude(friend.getLongitude());
                                                }
                                            }
                                            break;
                                    }
                                }
                                friendsListCallback.onCallback(listFriends2);

                            }
                        });
            }

            @Override
            public void onCallback(Map<Integer, User> l) {

            }
        });

    }

    private void getUser(final UserCallback userCallback){
        String userID = mAuth.getUid();


        firebaseFirestore.collection("Users")
                .document(userID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                        String id = documentSnapshot.getId();
                        User user = documentSnapshot.toObject(User.class).withId(id, 0);

                        LatLng l = new LatLng(Double.parseDouble(user.getLatitude()), Double.parseDouble(user.getLongitude()));
                        centerRadius = l;
                        userMAIN = user;
                        if(RadiusInKM>0)
                            drawCircle(RadiusInKM);

                        userCallback.onCallback(user);
                    }
                });

    }

    private void addMarker(User u){

        LatLng loc = new LatLng(Double.parseDouble(u.getLatitude()), Double.parseDouble(u.getLongitude()));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(loc);
        markerOptions.title(u.getFirst_name());


        if(RadiusInKM==0 || (RadiusInKM>0 && isInCircle(this.centerRadius, loc, this.RadiusInKM))) { // ukljucena pretraga po radijusu
            Marker marker = googleMap.addMarker(markerOptions);
            markerPlaceIdMap.put(marker, u.userID);
        }


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String uid = markerPlaceIdMap.get(marker);
                clickedUserID = uid;
                if (showFriends) {

                    AppCompatActivity compatActivity = (AppCompatActivity) view.getContext();
                    Bundle bundle = new Bundle();
                    bundle.putString("userID", uid);
                    ProfileFragment profileFragment = new ProfileFragment();
                    profileFragment.setArguments(bundle);

                    FragmentManager fragmentManager = compatActivity.getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.nav_main, profileFragment)
                            .commit();

                } else {
                    returnUserArticles(uid, SearchedText, new ArticleListCallback() {
                        @Override
                        public void onCallback(List<Article> list) {
                            if (list.size() > 0) {
                                showArticles(list);
                                constraintBigMap.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onCallback(Article article) {

                        }

                        @Override
                        public void onCallback(Map<Integer, Article> map) {

                        }
                    });
                }
                    return true;
            }
        });
    }

    private void addMarkerIcon(Marker marker, String userID){

        final ImageView finalImg = new ImageView(getActivity());

        FirebaseSingleton.getInstance().storageReference.child("profile_images/" + userID)
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
                            Log.i(TAG, "uspesno ucitavanje profilne slike!!");

                            Glide.with(getContext())
                                    .load(uri)
                                    .into(finalImg);
                        }
                    }
                });

        if(finalImg.getDrawable()!=null) {
            Bitmap bitmap = ((BitmapDrawable)finalImg.getDrawable()).getBitmap();
            Bitmap smallB = Bitmap.createScaledBitmap(bitmap, 100, 100,false);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallB));
        }else{
            BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.profile);
            Bitmap b=bitmapdraw.getBitmap();
            Bitmap smallB = Bitmap.createScaledBitmap(b, 100, 100,false);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallB));
        }

    }

    private void removeMarker(User u) {
        if(markerPlaceIdMap.size()>0) {
            Marker m = null;
            for (Map.Entry<Marker, String> entry : markerPlaceIdMap.entrySet()) {
                if (entry.getValue().matches(u.userID)) {
                    m = entry.getKey();
                }
            }
            if (m != null) {
                m.remove();
            }
        }
    }

    private void drawCircle(int radius){
        refresh();
        int radiusInMeters = radius * 1000;
        googleMap.addCircle(new CircleOptions()
                .center(centerRadius)
                .radius(radiusInMeters)
                .strokeWidth(0f)
                .fillColor(0x550000FF));
    }

    private boolean isInCircle(LatLng center, LatLng test, int km){
        float[] results = new float[1];
        Location.distanceBetween(center.latitude, center.longitude, test.latitude, test.longitude, results);
        float distanceInMeters = results[0];
        if(distanceInMeters < km * 1000)
            return true;

        return false;
    }

    private void returnUserArticles(String userID, String nameA, final ArticleListCallback articleListCallback) {

        tmp = new ArrayList<Article>();

        firebaseFirestore.collection("Users/" + userID + "/Articles")
                .whereEqualTo("type", SearchingFor)
                .whereEqualTo("name", nameA)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            for (QueryDocumentSnapshot document : task.getResult()) {
                              //  Log.d(TAG, document.getId() + " => " + document.getData());
                                String artID = document.getId();
                                Article a = document.toObject(Article.class).withId(artID, i);
                                i++;
                                tmp.add(a);
                            }
                            articleListCallback.onCallback(tmp);
                        } else {
                            Log.d("MAPS", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void showArticles(final List<Article> articles){

        linearLayoutScrol.removeAllViews();
        //if(constraintBigMap.getVisibility() == View.INVISIBLE) {
            for (int i = 0; i < articles.size(); ++i) {
                String ID_1 = articles.get(i).articleID;

                final ToggleButton button = new ToggleButton(getActivity());
                String ns = articles.get(i).name + ", " + articles.get(i).size;

                button.setText(ns);
                button.setTextOn(ns);
                button.setTextOff(ns);
                button.setTag(ID_1);
                button.setId(i);

                linearLayoutScrol.addView(button);

            }
            if(SearchingFor.matches("donate")){
                btnOK.setText("ASK");
            }else{
                btnOK.setText("OFFER");
            }
//        }else{
//            Toast.makeText(getActivity(), "You need to close the window first!", Toast.LENGTH_SHORT).show();
//        }


        //pribavljaju se svi ToggleButton-i koji se prikazuju u LinearLayout-u
        int viewsInLinLayCount = linearLayoutScrol.getChildCount();
        for(int i=0; i<viewsInLinLayCount; i++){

            final ToggleButton buttonChild = (ToggleButton) linearLayoutScrol.getChildAt(i); //izdvaja se svaki ToggleButton posebno

            buttonChild.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { //stavlja se listener
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){

                        int btnId = buttonChild.getId();

                        for(int k = 0; k<articles.size(); k++){ //svi ostali se gase
                            if(k!=btnId){
                            ToggleButton tb = (ToggleButton) linearLayoutScrol.getChildAt(k);
                            tb.setChecked(false);
                            }
                            clickedArticleId = (String) buttonChild.getTag();
                            clickedArticleName = (String) buttonChild.getText();
                        }
                        buttonView.setBackgroundColor(Color.BLUE);
                    }else{
                        buttonView.setBackgroundColor(Color.LTGRAY);
                        clickedArticleId = "";
                    }
                }
            });



        }




    }

    private void offerArticle(String articleID, String userID) {

            final Map<String, String> request = new HashMap<>();
            request.put("fromID", mAuth.getCurrentUser().getUid());
            request.put("articleID", articleID);
            request.put("type", "active-offer");

            String tmp = generateRandomString(8);
            firebaseFirestore.collection("Users/" + userID + "/Requests").document(tmp).set(request);

            final Map<String, String> notificationOffer = new HashMap<>();
            notificationOffer.put("text", "Imate novu ponudu");

            firebaseFirestore.collection("Users/" + userID + "/Notifications").document(tmp).set(notificationOffer); //id ce da budi isti kao i kod request-a
    }

    private void askForArticle(String askArticleID, String userIdClicked, boolean isInList){

        final Map<String, String> request = new HashMap<>();
        request.put("type", "active-ask");
        request.put("fromID", mAuth.getCurrentUser().getUid());
        request.put("articleID", askArticleID);

        String tmp = generateRandomString(8);
        String[] articleNameAndSize = clickedArticleName.split(", ");
        firebaseFirestore.collection("Users/" + userIdClicked + "/Requests").document(tmp).set(request);

        final Map<String, String> notificationAsked = new HashMap<>();
        notificationAsked.put("text", "Od Vas se trazi: " + articleNameAndSize[0] + ", " + articleNameAndSize[1]);

        firebaseFirestore.collection("Users/" + userIdClicked + "/Notifications").document(tmp).set(notificationAsked); //id ce da budi isti kao i kod request-a

        if(!isInList){//ako nije u listi, mora da se kreira kod sebe u listi necessary

            firebaseFirestore.collection("Users/" + userIdClicked + "/Articles").document(askArticleID).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){

                                Article a = task.getResult().toObject(Article.class);

                                final Map<String, String> articleForAdd = new HashMap<>();
                                articleForAdd.put("name", a.name);
                                articleForAdd.put("size", a.size);
                                articleForAdd.put("description", a.description);
                                articleForAdd.put("type", "necessary");

                                String tmpN = generateRandomString(8);
                                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(tmpN).set(articleForAdd);

                            }else{

                            }
                        }
                    });
        }
    }

    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {

            // 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            // debug
            System.out.format("%d\t:\t%c%n", rndCharAt, rndChar);

            sb.append(rndChar);
        }

        return sb.toString();

    }


    private void refresh(){
        if(googleMap!=null) {
            googleMap.clear();
            onMapReady(this.googleMap);
        }

    }

}
