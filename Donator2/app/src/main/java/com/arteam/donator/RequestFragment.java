package com.arteam.donator;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.HashMap;
import java.util.Map;

public class RequestFragment extends Fragment {

    View view;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;

    private Map<Integer, Request> listRequests;
    private Map<Integer, Article> listArticles;
    private RequestRecycler requestRecycler;
    private FragmentManager fragmentManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();
        listRequests = new HashMap<>();
        listArticles = new HashMap<>();
        fragmentManager = getActivity().getSupportFragmentManager();


        requestRecycler = new RequestRecycler(listRequests, listArticles, fragmentManager);
        recyclerView = view.findViewById(R.id.listRequestRecycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(requestRecycler);



        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Requests")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {


                                String requestID = doc.getDocument().getId();
                                Request request = doc.getDocument().toObject(Request.class).withId(requestID, i);

                                final String articleId = request.articleID;
                                final String fromId = request.fromID;
                                final String type = request.type;

                                if (!type.matches("passive")) {
                                    listRequests.put(i, request);

                                    //requestRecycler.notifyDataSetChanged();

                                    final int b = i;


                                    if (type.matches("active-ask"))//ovi se traze od mene
                                        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(articleId).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        //int b = i;
                                                        if (task.isSuccessful()) {

                                                            if (task.getResult().exists()) {

                                                                Article article = task.getResult().toObject(Article.class).withId(articleId, b);

                                                                listArticles.put(b, article);
                                                               // b++;

                                                                requestRecycler.notifyDataSetChanged();
                                                            } else {

                                                            }

                                                        } else {

                                                        }


                                                    }
                                                });
                                    else if (type.matches("active-offer"))
                                        firebaseFirestore.collection("Users/" + fromId + "/Articles").document(articleId).get()
                                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        //int b = i;
                                                        if (task.isSuccessful()) {

                                                            if (task.getResult().exists()) {

                                                                Article article = task.getResult().toObject(Article.class).withId(articleId, b);

                                                                listArticles.put(b, article);
                                                                // b++;

                                                                requestRecycler.notifyDataSetChanged();
                                                            } else {

                                                            }

                                                        } else {

                                                        }


                                                    }
                                                });

                                    i++;

                                }





                            }
                        }
                    }
                });


        return view;
    }
}
