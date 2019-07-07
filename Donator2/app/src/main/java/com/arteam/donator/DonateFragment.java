package com.arteam.donator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DonateFragment extends Fragment {

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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_donate, container, false);

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



        articleRecycler = new ArticleRecycler(listArticles, relAddArticle, txtName, txtSize, txtDescription, btnOk, btnOk2, btnCancel, fab, linearLayout2, linearLayout3);


        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(articleRecycler);


        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                //       Article konkurs = doc.getDocument().toObject(Article.class);
                                //       listArticles.add(konkurs);

                                //       vlasnikKonkursRecycler.notifyDataSetChanged();
                                //       String tip = doc.getDocument().getString("tip");
                                //      String zaposljen = doc.getDocument().getString("zaposljen");
                               // if (tip.matches("radnik") && zaposljen.matches("ne")) {

                                    String articleID = doc.getDocument().getId();
                                    Article article = doc.getDocument().toObject(Article.class).withId(articleID, i);
                                    listArticles.put(i, article);
                                    i++;
                                    articleRecycler.notifyDataSetChanged();

                               // }
                            }
                        }
                    }
                });



        return view;
    }



}