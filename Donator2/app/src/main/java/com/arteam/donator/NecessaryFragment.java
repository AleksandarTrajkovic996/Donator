package com.arteam.donator;

import android.annotation.SuppressLint;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.SecureRandom;
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
    private ConstraintLayout relAddArticle;
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
    private Map<String, String> listOfValue;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static SecureRandom random = new SecureRandom();



    private boolean relViewLayoutActive;
    private Button btnOffer;
    private Button btnOk3;
    private Button btnCancel2;
    private TextView txtDescription2;
    private LinearLayout linearLayout4;
    private LinearLayout linearLayout5;
    private ConstraintLayout relViewArticle;

    @SuppressLint("RestrictedApi")
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

        relViewLayoutActive = false; // novo!!!
        btnOffer = view.findViewById(R.id.btnOffer);//offer kad trazimo iz necije liste za doniranje - novo!!!
        btnOk3 = view.findViewById(R.id.btnOk);//ok kad se prikazuje samo - novo!!!
        btnCancel2 = view.findViewById(R.id.btnCancel);//cancel kad trazimo iz necije liste za doniranje - novo!!!
        txtDescription2 = view.findViewById(R.id.txtDesc2);//description kada se otvara za pregled samo - novo!!!
        linearLayout4 = view.findViewById(R.id.linOfferCancel);//ask i cancel kad pregledavamo kod trugog nekog - novo!!!
        linearLayout5 = view.findViewById(R.id.linOk);//ok kad se prikazuje samo - novo!!!
        relViewArticle = view.findViewById(R.id.relViewArticle); //novo!!!

        listOfValue = new HashMap<>();
        listOfValue.put("patike", "5");
        listOfValue.put("jakna", "10");
        listOfValue.put("pantalone", "4");
        listOfValue.put("kosulja", "4");
        listOfValue.put("bluza", "3");
        listOfValue.put("majica", "2");
        listOfValue.put("bunda", "12");
        listOfValue.put("default", "5");

        recyclerView = view.findViewById(R.id.listArticleRecycler);

        listArticles = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();


        articleRecycler = new ArticleRecycler(listArticles, relAddArticle, txtName, txtSize, txtDescription, btnOk, btnOk2, btnCancel, fab, linearLayout2, linearLayout3,
                        relViewArticle, linearLayout4, linearLayout5, btnOffer, btnOk3, btnCancel2, txtDescription2,"necessary");


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
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

        String userID=null;
        final String userID1;
        String userType;

        //postavljaju se promenljive, da li je korisnik usao kod svoje potrebne stvari ili kod nekog drugog korisnika
        Bundle bundle = getArguments();
        if(bundle!=null){
            userID = bundle.getString("userID");
            userID1 = userID;
            userType = bundle.getString("type");
        }else {
            userID = mAuth.getUid();
            userID1 = userID;
        }

        if(!mAuth.getCurrentUser().getUid().matches(userID)) {
            fab.setVisibility(View.INVISIBLE);
        }


        //popunjavanje podataka
        firebaseFirestore.collection("Users/" + userID + "/Articles")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            String articleID = doc.getDocument().getId();
                            Article article = doc.getDocument().toObject(Article.class).withId(articleID, i);
                            switch (doc.getType()){
                                case ADDED:
                                     if(article.getType().matches("necessary")){
                                         Log.i("Necessary", "Added");
                                        listArticles.put(i, article);
                                        i++;
                                        articleRecycler.userID=userID1;
                                        articleRecycler.notifyDataSetChanged();
                                    }
                                    break;
                                case REMOVED:
                                    Log.i("Necessary", "REMOVED");
                                    break;
                                case MODIFIED:
                                    if(!article.getType().matches("necessary")) {
                                        Log.i("Necessary", "MODIFIED");
                                        listArticles.remove(article);
                                        articleRecycler.userID = userID1;
                                        //ostaje refresh prikaza!!!!
                                        articleRecycler.notifyItemRemoved(article.id);
                                        articleRecycler.notifyItemRangeChanged(article.id, listArticles.size());
                                        articleRecycler.notifyDataSetChanged();
                                        break;
                                    }
                            }
                        }
                    }
                });


        //dodavanje potrebnog artikla
        final Map<String, String> articleForAdd = new HashMap<>();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                articleForAdd.put("name", txtName.getText().toString());
                articleForAdd.put("size", txtSize.getText().toString());
                articleForAdd.put("description", txtDescription.getText().toString());
                articleForAdd.put("type", "necessary");
                if (listOfValue.get(txtName.getText().toString()) != null)
                    articleForAdd.put("value", listOfValue.get(txtName.getText().toString()));
                else
                    articleForAdd.put("value", listOfValue.get("default"));

                String tmp = generateRandomString(8); //////////////////////////PREPRAVITI FUNKCIJU ZA ID!


                firebaseFirestore.collection("Users/" + userID1 + "/Articles").document(tmp).set(articleForAdd)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("Necessary", "Dodavanje uspesno");
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.nav_main, new NecessaryFragment())
                                        .commit();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Necessary", "Greska prilikom dodavanja");
                            }
                });

                relAddArticle.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                relLayoutActive=false;
            }


        });

        return view;
    }

    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {

            // 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            // debug
            System.out.format("%d\t:\t%c%n", rndCharAt, rndChar);

            sb.append(rndChar);
        }

        return sb.toString();

    }



}
