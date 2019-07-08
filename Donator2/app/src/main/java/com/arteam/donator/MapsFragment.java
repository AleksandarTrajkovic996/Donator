package com.arteam.donator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final int PERMISSION_ACCES_FINE_LOCATION = 1;
    View view;
    GoogleMap googleMap;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private Map<String, User> listUsers = new HashMap<>();
    private Button btnSearch;
    private EditText searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_maps, container, false);

        btnSearch = view.findViewById(R.id.search_button);
        searchBar = view.findViewById(R.id.searchBarMaps);
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                searchData(searchBar.getText().toString(), "necessary");
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
                //fillData("necessary");
        }

    }

    private void fillData(final String type){

        firebaseFirestore.collection("Users") //obilaze se svi useri
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                final String userID = doc.getDocument().getId();
                                final User user = doc.getDocument().toObject(User.class);


                                firebaseFirestore.collection("Users/" + userID + "/Articles").whereEqualTo("type", type)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                                                int con = queryDocumentSnapshots.size();
                                                if(con>0 && !userID.matches(mAuth.getCurrentUser().getUid())){

                                                        Geocoder coder = new Geocoder(getActivity());
                                                        List<Address> address;
                                                        GeoPoint p1 = null;
                                                        Address location = null;
                                                        try {
                                                            address = coder.getFromLocationName(user.getAddress(),5);
                                                            if (address==null) {
                                                                return;
                                                            }
                                                            location=address.get(0);
                                                            if(location==null){
                                                                return;
                                                            }
                                                        }catch (IOException e1) {
                                                            e1.printStackTrace();
                                                        }
                                                        LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                                                        MarkerOptions markerOptions = new MarkerOptions();
                                                        markerOptions.position(loc);
                                                        markerOptions.title(user.getFirst_name());
                                                        Marker marker = googleMap.addMarker(markerOptions);

                                                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                            @Override
                                                            public boolean onMarkerClick(Marker marker) {
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString("userID", userID);
                                                               // bundle.putString("type", "received");
                                                                ProfileFragment profileFragment = new ProfileFragment();
                                                                profileFragment.setArguments(bundle);

                                                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                                fragmentManager.beginTransaction()
                                                                        .replace(R.id.nav_main, profileFragment)
                                                                        .commit();

                                                                return true;
                                                            }
                                                        });
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });


    }


    private void searchData(final String data, final String type){
        firebaseFirestore.collection("Users") //obilaze se svi useri
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                final String userID = doc.getDocument().getId();
                                final User user = doc.getDocument().toObject(User.class);

                                firebaseFirestore.collection("Users/" + userID + "/Articles").whereEqualTo("type", type).whereEqualTo("name", data)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                                                int con = queryDocumentSnapshots.size();
                                                if(con>0 && !userID.matches(mAuth.getCurrentUser().getUid())){

                                                    Geocoder coder = new Geocoder(getActivity());
                                                    List<Address> address;
                                                    GeoPoint p1 = null;
                                                    Address location = null;
                                                    try {
                                                        address = coder.getFromLocationName(user.getAddress(),5);
                                                        if (address==null) {
                                                            return;
                                                        }
                                                        location=address.get(0);
                                                        if(location==null){
                                                            return;
                                                        }
                                                    }catch (IOException e1) {
                                                        e1.printStackTrace();
                                                    }
                                                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                                                    MarkerOptions markerOptions = new MarkerOptions();
                                                    markerOptions.position(loc);
                                                    markerOptions.title(user.getFirst_name());
                                                    Marker marker = googleMap.addMarker(markerOptions);

                                                    googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                        @Override
                                                        public boolean onMarkerClick(Marker marker) {
                                                            Bundle bundle = new Bundle();
                                                            bundle.putString("userID", userID);
                                                            // bundle.putString("type", "received");
                                                            ProfileFragment profileFragment = new ProfileFragment();
                                                            profileFragment.setArguments(bundle);

                                                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                                            fragmentManager.beginTransaction()
                                                                    .replace(R.id.nav_main, profileFragment)
                                                                    .commit();

                                                            return true;
                                                        }
                                                    });
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });



    }


}
