package com.arteam.donator;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class RankingFragment extends Fragment {

    View view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private Map<Integer, User> listFriends;
    private Map<Integer, User> listFriends2;
    private UserRecycler friendsRecycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_ranking, container, false);

        recyclerView = view.findViewById(R.id.listFriendsRecycler);

        listFriends = new HashMap<>();
        listFriends2 = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();
        friendsRecycler = new UserRecycler(listFriends2, "Y");

        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(friendsRecycler);

        listFriends.clear();
        listFriends2.clear();
        friendsRecycler.notifyDataSetChanged();

        sortirajPrijatelje(new FriendsListCallback() {
            @Override
            public void onCallback() {

            }

            @Override
            public void onCallback(Map<Integer, User> l) {
                recyclerView.removeAllViews();
                int n = l.size();
                for (int i = 1; i < n; ++i) {
                    User key = l.get(i);
                    int j = i - 1;


                    while (j >= 0 && l.get(j).compareTo(key) < 0) {
                        l.put(j + 1, l.get(j));
                        j = j - 1;
                    }
                    l.put(j + 1, key);
                }
                friendsRecycler.notifyDataSetChanged();

            }
        });

        return view;
    }

    public void sortirajPrijatelje(final FriendsListCallback friendsListCallback){

        pribaviPrijatelje(new FriendsListCallback() {
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
                                            Log.i("Ranking", "REMOVED");
                                            break;
                                        case ADDED:
                                            Log.i("Ranking", "ADDED");
                                                for (int k = 0; k < listFriends.size(); k++) {
                                                    if (friendID.matches(listFriends.get(k).getFriendID())){
                                                        listFriends2.put(i, friend);
                                                        i++;
                                                    }
                                                }
                                            break;
                                        case MODIFIED:
                                            for (int k = 0; k < listFriends2.size(); k++) {
                                                if (friendID.matches(listFriends2.get(k).userID)){
                                                    listFriends2.get(k).setPoints(friend.getPoints());
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
                                listFriends.put(i, friend);
                                i++;
                            }
                        }
                        listFriends.put(listFriends.size(), new User(mAuth.getUid())); //dodajemo i sebe na kraj listte
                        friendsListCallback.onCallback();
                    }
                });
    }

}
