package com.arteam.donator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class NecessaryFragment extends Fragment {

    View view;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private Map<Integer, Article> listArticles;
    private ArticleRecycler articleRecycler;
    private RelativeLayout relAddArticle;
    private LinearLayout linearLayout2;
    private LinearLayout linearLayout3;
    private Button btnOk;
    private Button btnOk2;
    private Button btnCancel;
    private TextView txtName;
    private TextView txtSize;
    private TextView txtDescription;
    FloatingActionButton fab;
    private boolean relLayoutActive;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_necessary, container, false);

        relLayoutActive = false;
        relAddArticle = view.findViewById(R.id.relAddArticle);
        btnOk = view.findViewById(R.id.allow);
        btnOk2 = view.findViewById(R.id.allow2);
        btnCancel = view.findViewById(R.id.deny);
        txtName = view.findViewById(R.id.txtName);
        txtSize = view.findViewById(R.id.txtSize);
        txtDescription = view.findViewById(R.id.txtDesc);
        linearLayout3 = view.findViewById(R.id.lin3);
        linearLayout2 = view.findViewById(R.id.lin2);
        fab = view.findViewById(R.id.fab);


        recyclerView = view.findViewById(R.id.listArticleRecycler);


        listArticles = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();


        articleRecycler = new ArticleRecycler(listArticles, relAddArticle, txtName, txtSize, txtDescription, btnOk, btnOk2, btnCancel, fab, linearLayout2, linearLayout3, "necessary");



        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(articleRecycler);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!relLayoutActive){
                    txtName.setText("");
                    txtSize.setText("");
                    txtDescription.setText("");
                    relAddArticle.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    linearLayout3.setVisibility(View.INVISIBLE);
                    relLayoutActive = true;
                }


            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relAddArticle.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                relLayoutActive=false;
            }
        });

        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String articleID = doc.getDocument().getId();
                                Article article = doc.getDocument().toObject(Article.class).withId(articleID, i);
                                if(article.getType().matches("necessary")){
                                    listArticles.put(i, article);
                                    i++;
                                    articleRecycler.notifyDataSetChanged();

                                }

                            }
                        }
                    }
                });


        final Map<String, String> articleForAdd = new HashMap<>();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                articleForAdd.put("name", txtName.getText().toString());
                articleForAdd.put("size", txtSize.getText().toString());
                articleForAdd.put("description", txtDescription.getText().toString());
                articleForAdd.put("type", "necessary");

                Random generator = new Random();
                StringBuilder randomStringBuilder = new StringBuilder();
                int randomLength = generator.nextInt((15 - 10) + 1) + 10;
                char tempChar;
                for (int i = 0; i < randomLength; i++){
                    tempChar = (char) (generator.nextInt(96) + 32);
                    randomStringBuilder.append(tempChar);
                }
                String tmp = randomStringBuilder.toString();

                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(tmp).set(articleForAdd)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                relAddArticle.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                relLayoutActive=false;


                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, new NecessaryFragment())
                        .commit();
            }
        });

        return view;
    }



}
