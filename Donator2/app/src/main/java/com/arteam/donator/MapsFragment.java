package com.arteam.donator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.constraint.Constraints.TAG;

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


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);
        setHasOptionsMenu(true);

        btnSearch = view.findViewById(R.id.search_button);
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

        this.getArticles();
        getUserPosition();


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
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                           // googleMap.setMyLocationEnabled(true);

                     }
                 }
                 return;
             }
         }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCES_FINE_LOCATION);

            //googleMap.setMyLocationEnabled(true);

        } else {

        }

    }

    private void searchDataAndType(final String data, final String searchingForType){

        markerPlaceIdMap = new HashMap<Marker, String>();

        firebaseFirestore.collection("Users") //obilaze se svi useri
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            int i = 0;
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) { //foreach petlja kroz Users

                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    final String userID = doc.getDocument().getId();
                                    final User user1 = doc.getDocument().toObject(User.class).withId(userID, i);
                                    ++i;

                                    if (!userID.matches(mAuth.getCurrentUser().getUid())) {

                                        if (searchingForType.matches("donate") || (searchingForType.matches("necessary") && isThere(data))) {

                                            firebaseFirestore.collection("Users/" + userID + "/Articles").whereEqualTo("type", SearchingFor).whereEqualTo("name", data)
                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                                                            int con = queryDocumentSnapshots.size();
                                                            if (con > 0) {
                                                                addMarker(user1);
                                                            }
                                                                                                                   }
                                                    });
                                        }
                                    }
                                }
                            }
                        }
                    });
    }

    public void setUser(User u){
        this.userMAIN = u;
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

    private void searchType(final String type) {

        List<Article> tmpList = new ArrayList<>();
        markerPlaceIdMap = new HashMap<Marker, String>();

        if (type.matches("necessary") && NecessaryArticles.size() > 0) {
            tmpList = NecessaryArticles;
        } else if (type.matches("donate") && DonateArticles.size() > 0) {
            tmpList = DonateArticles;
        }

        if(tmpList.size()>0) {
            final List<Article> finalTmpList = tmpList;
            firebaseFirestore.collection("Users") //obilaze se svi useri
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                            int i = 0;
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) { //foreach petlja kroz Users


                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    final String userID = doc.getDocument().getId();
                                    final User user = doc.getDocument().toObject(User.class).withId(userID, i);
                                    ++i;

                                    if(!userID.matches(mAuth.getCurrentUser().getUid())) {
                                        for (Article a : finalTmpList) { //finalTmpList i tmpList su iste, a sadrze ili listu potrebnih stvari, ili listu stvari za doniranje
                                            firebaseFirestore.collection("Users/" + userID + "/Articles").whereEqualTo("type", SearchingFor).whereEqualTo("name", a.name)
                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                                                            int con = queryDocumentSnapshots.size();
                                                            if(con>0){
                                                                addMarker(user);
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                }
                            }
                        }

                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.action_map_Donate){ //zeli da donira nesto, prikazuju se stvari koje su potrebne ostalima
            this.SearchType = "donate";
            this.SearchingFor = "necessary";
            constraintBigMap.setVisibility(View.INVISIBLE);
            linearLayoutScrol.removeAllViews();
            refresh();
        }else if(id == R.id.action_map_Friends){
            //this.SearchType = "friends";
            refresh();
        }else if(id==R.id.action_map_Necessary){ //trazi stvari, prikazuju se donatori
            this.SearchType = "necessary";
            this.SearchingFor = "donate";
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.navigation_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void addMarker(User u){

        Geocoder coder = new Geocoder(getActivity());
        List<Address> address;
        GeoPoint p1 = null;
        Address location = null;
        try {
            address = coder.getFromLocationName(u.getAddress(), 5);
            if (address == null) {
                return;
            }
            location = address.get(0);
            if (location == null) {
                return;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
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
//                Bundle bundle = new Bundle();
//                String uid = markerPlaceIdMap.get(marker);
//                bundle.putString("userID", uid);
//                ProfileFragment profileFragment = new ProfileFragment();
//                profileFragment.setArguments(bundle);
//
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                fragmentManager.beginTransaction()
//                        .replace(R.id.nav_main, profileFragment)
//                        .commit();

                String uid = markerPlaceIdMap.get(marker);
                returnUserArticles(uid, SearchedText, new ArticleListCallback() {
                    @Override
                    public void onCallback(List<Article> list) {
                        Log.i("Article", String.valueOf(list.size()));
                        if (list.size()>0) {
                            showArticles(list);
                            constraintBigMap.setVisibility(View.VISIBLE);
                        }
                    }
                });

                return true;
            }
        });

    }

    private void getUserPosition(){
        Geocoder coder = new Geocoder(getActivity());
        List<Address> address;
        GeoPoint p1 = null;
        Address location = null;
        try {
            address = coder.getFromLocationName(userMAIN.getAddress(), 5);
            if (address == null) {
                return;
            }
            location = address.get(0);
            if (location == null) {
                return;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        centerRadius = new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void drawCircle(int radius){

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

    private void returnUserArticles(String userID,String nameA, final ArticleListCallback articleListCallback) {
        tmp = new ArrayList<Article>();


        //final CountDownLatch countDownLatch = new CountDownLatch(1);
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
                            // countDownLatch.countDown();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void showArticles(List<Article> articles){

        if(constraintBigMap.getVisibility() == View.INVISIBLE) {
            for (int i = 0; i < articles.size(); ++i) {
                String ID_1 = articles.get(i).articleID;

                Button button = new Button(getActivity());
                button.setText(articles.get(i).name + ",  " + articles.get(i).size);
                button.setTag(ID_1);
                button.setId(i);
                linearLayoutScrol.addView(button);
            }
        }else{
            Toast.makeText(getActivity(), "You need to close the window first!", Toast.LENGTH_SHORT).show();
        }

    }

    private void refresh(){
        if(googleMap!=null) {
            googleMap.clear();
            onMapReady(this.googleMap);
        }

    }

}
