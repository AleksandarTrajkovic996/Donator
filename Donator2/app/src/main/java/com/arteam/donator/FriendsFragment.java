package com.arteam.donator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class FriendsFragment extends Fragment {

    View view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private Map<Integer, User> listFriends;
    private UserRecycler friendsRecycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerView = view.findViewById(R.id.listFriendsRecycler);


        listFriends = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();

        friendsRecycler = new UserRecycler(listFriends);


        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(friendsRecycler);


        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Friends")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {



                                String friendID = doc.getDocument().getId();
                                User friend = doc.getDocument().toObject(User.class).withId(friendID, i);

                           //     if(friend.getType().matches("donate")){
                                    listFriends.put(i, friend);
                                    i++;
                                    friendsRecycler.notifyDataSetChanged();

                              //  }
                            }
                        }
                    }
                });



        return view;
    }



}
