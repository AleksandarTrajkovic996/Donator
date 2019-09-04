package com.arteam.donator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.Random;

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
    private String clickedArticleId ="";
    private String clickedUserID = "";
    private String clickedArticleName = "";


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

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.getArticles();

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

                    final Map<String, String> request = new HashMap<>();

                    if(SearchType.matches("donate")){ //korisnik moze da donira
                        ArrayList<Article> articleArrayList = new ArrayList<>();
                        String[] cmp = clickedArticleName.split(", ");
                        for(Article a : DonateArticles){
                            if(a.getName().matches(cmp[0]) && a.getSize().matches(cmp[1])){
                                articleArrayList.add(a);
                            }
                        }
                        offerArticles(articleArrayList, clickedUserID);

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
                    }
                }else{
                    Toast.makeText(getActivity(), "You have to choose one article!", Toast.LENGTH_SHORT).show();
                }
                constraintBigMap.setVisibility(View.INVISIBLE);
                linearLayoutScrol.removeAllViews();
                clickedArticleId="";
                clickedUserID = "";
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

                         googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerRadius, 10));
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

           // googleMap.setMyLocationEnabled(true);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerRadius, 10));

        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centerRadius, 10));
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
                String uid = markerPlaceIdMap.get(marker);
                clickedUserID = uid;
                returnUserArticles(uid, SearchedText, new ArticleListCallback() {
                    @Override
                    public void onCallback(List<Article> list) {
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

    public void setUser(User u) {this.userMAIN = u;}

    public void setLatLnt(LatLng l) { this.centerRadius = l; }

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

    private void returnUserArticles(String userID, String nameA, final ArticleListCallback articleListCallback) {

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

    private void showArticles(final List<Article> articles){

        if(constraintBigMap.getVisibility() == View.INVISIBLE) {
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
        }else{
            Toast.makeText(getActivity(), "You need to close the window first!", Toast.LENGTH_SHORT).show();
        }


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

    private void offerArticles(List<Article> listArticles, String userID){

        for (int i = 0; i < listArticles.size(); i++) { //saljemo sve ponude koje imamo
            String tmp = getNewID();
            final Map<String, String> request = new HashMap<>();
            request.put("fromID", mAuth.getCurrentUser().getUid());
            request.put("articleID", listArticles.get(i).articleID);
            request.put("type", "active-offer");

            firebaseFirestore.collection("Users/" + userID + "/Requests").document(tmp).set(request);


            final Map<String, String> notificationOffer = new HashMap<>();
            notificationOffer.put("text", "Imate novu ponudu");

            firebaseFirestore.collection("Users/" + userID + "/Notifications").document(tmp).set(notificationOffer); //id ce da budi isti kao i kod request-a
        }

    }

    private void askForArticle(String askArticleID, String userIdClicked, boolean isInList){

        final Map<String, String> request = new HashMap<>();
        request.put("type", "active-ask");
        request.put("fromID", mAuth.getCurrentUser().getUid());
        request.put("articleID", askArticleID);

        String tmp = getNewID();
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

                                String tmpN = getNewID();
                                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(tmpN).set(articleForAdd);

                            }else{

                            }
                        }
                    });
        }
    }

    public String getNewID(){
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt((15 - 10) + 1) + 10;
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void refresh(){
        if(googleMap!=null) {
            googleMap.clear();
            onMapReady(this.googleMap);

        }

    }

}
