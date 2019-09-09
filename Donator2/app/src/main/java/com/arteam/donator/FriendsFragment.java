package com.arteam.donator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
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


        pribaviPrijatelje(new FriendsListCallback() {
            @Override
            public void onCallback() {

                for (int i = 0; i < listFriends.size(); i++){

                    final int b = i;
                    firebaseFirestore.collection("Users").document(listFriends.get(i).getFriendID()).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        if (task.getResult().exists()) {

                                            User user = task.getResult().toObject(User.class).withId(listFriends.get(b).getFriendID(), 0);


                                            listFriends.put(b, user);


                                            friendsRecycler.notifyDataSetChanged();
                                        } else {

                                        }

                                    } else {

                                    }


                                }
                            });


                }
            }

            @Override
            public void onCallback(Map<Integer, User> l) {

            }
        });



        return view;
    }

    public void pribaviPrijatelje(final FriendsListCallback friendsListCallback){
        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Friends")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {



                                String friendID = doc.getDocument().getId();
                                User friend = doc.getDocument().toObject(User.class).withId(friendID, i);

                                //to do: ovde pribaviti sve podatke o prijateljima

                                listFriends.put(i, friend);
                                i++;


                            }
                        }

                        friendsListCallback.onCallback();
                    }
                });
    }

}
