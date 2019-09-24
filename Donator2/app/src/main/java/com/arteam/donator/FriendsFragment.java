package com.arteam.donator;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FriendsFragment extends Fragment {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DISCOVERABLE = 2;
    private static final int SELECT_SERVER = 3;
    private boolean isDiscoverable = false;

    View view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private Map<Integer, User> listFriends;
    private Map<Integer, User> listFriends2;
    private UserRecycler friendsRecycler;
    private FloatingActionButton btnAddFriend;
    private BluetoothAdapter bluetoothAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView = view.findViewById(R.id.listFriendsRecycler);
        btnAddFriend = view.findViewById(R.id.floatBtnAddFriend);

        listFriends = new HashMap<>();
        listFriends2 = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();
        final FriendsService friendsService = new FriendsService();
        friendsRecycler = new UserRecycler(listFriends2);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(friendsRecycler);

        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        TextView fullNameNavigationMain = hView.findViewById(R.id.fullNameTxt);
        String tmp = fullNameNavigationMain.getText().toString();
        String[] t = tmp.split(" ");
        final String userFirstName = t[0];
        final String userLastName = t[1];

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                if (bluetoothAdapter != null) {

                    if(!bluetoothAdapter.isEnabled()){ //ako nije ukljucen

                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

                    }else{ //ako jeste

                        if(!isMyServiceRunning(friendsService.getClass())) {
                            getActivity().startService(new Intent(getActivity(), FriendsService.class));
                        }
                        Intent intent = new Intent(getActivity(), PickDevices.class);
                        intent.putExtra("first", userFirstName);
                        intent.putExtra("last", userLastName);
                        startActivity(intent);
                    }
                }else{
                    // Device doesn't support Bluetooth
                    Toast.makeText(getActivity(), "Device doesn't support Bluetooth!", Toast.LENGTH_SHORT).show();
                }

            }
        });


        pribaviPrijatelje(new FriendsListCallback() {
            @Override
            public void onCallback() {
                recyclerView.removeAllViews();

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
                                            Log.i("FriendsFragment", "REMOVED");
                                            break;
                                        case ADDED:
                                            Log.i("FriendsFragment", "ADDED");
                                            for (int k = 0; k < listFriends.size(); k++) {
                                                if (friendID.matches(listFriends.get(k).getFriendID())){
                                                    listFriends2.put(i, friend);
                                                    i++;
                                                    friendsRecycler.notifyDataSetChanged();
                                                }
                                            }
                                            break;
                                        case MODIFIED:
                                            Log.i("FriendsFragment", "MODIFIED");
                                            for (int k = 0; k < listFriends.size(); k++) {
                                                if (friendID.matches(listFriends.get(k).userID)){
                                                    listFriends2.get(k).setPoints(friend.getPoints());
                                                    friendsRecycler.notifyDataSetChanged();
                                                }
                                            }
                                            break;
                                    }
                                }
                            }
                        });


            }

            @Override
            public void onCallback(Map<Integer, User> l) {

            }
        });

        return view;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("Service status", "Running");
                return true;
            }
        }
        Log.i ("Service status", "Not running");
        return false;
    }

    public void pribaviPrijatelje(final FriendsListCallback friendsListCallback){
        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Friends")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            String friendID = doc.getDocument().getId();
                            User friend = doc.getDocument().toObject(User.class).withId(friendID, i);

                            switch (doc.getType()) {
                                case ADDED:
                                        listFriends.put(i, friend);
                                        i++;
                                    break;
                                case MODIFIED:
                                    for(int j=0; j<listFriends.size(); j++){
                                        if(listFriends.get(j).getFriendID().matches(friendID)){
                                            Log.i("FriendsFragment", friendID);
                                            Log.i("FriendsFragment", friend.getFirst_name() + " " + friend.getPoints());
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    break;
                            }
                        }
                        friendsListCallback.onCallback();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == getActivity().RESULT_OK) {

            if(requestCode == REQUEST_ENABLE_BT){

                getActivity().startService(new Intent(getActivity(), FriendsService.class));

            }//end if

        }

    }//end



}
