package com.arteam.donator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class DonateFragment extends Fragment {

    View view;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recyclerView;
    private Map<Integer, Article> listArticles;
    private ArticleRecycler articleRecycler;
    private RelativeLayout relAddArticle;
    private RelativeLayout relViewArticle;
    private LinearLayout linearLayout2;
    private LinearLayout linearLayout3;
    private LinearLayout linearLayout4;
    private LinearLayout linearLayout5;
    private Button btnOk;
    private Button btnOk2;
    private Button btnCancel;
    private Button btnAsk;
    private Button btnOk3;
    private Button btnCancel2;
    private TextView txtDescription2;
    private TextView txtName;
    private TextView txtSize;
    private TextView txtDescription;
    private ImageView imageDonate;
    private Bitmap bitmap = null;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri = null;
    private byte[] bytesProfile = null;
    FloatingActionButton fab;
    private String userID = null;
    private String userType = null;
    private boolean relAddLayoutActive;
    private boolean relViewLayoutActive;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_donate, container, false);

        relAddLayoutActive = false;
        relViewLayoutActive = false;   //novo!!!
        imageDonate = view.findViewById(R.id.imageViewDonatePhoto);
        relAddArticle = view.findViewById(R.id.relAddArticle);
        relViewArticle = view.findViewById(R.id.relViewArticle); //novo!!!
        btnOk = view.findViewById(R.id.allow);//ok kad se dodaje novi
        btnOk2 = view.findViewById(R.id.allow2);//ok kad se prikazuje samo - staro!!!
        btnCancel = view.findViewById(R.id.deny);//cancel kad se dodaje novi
        btnAsk = view.findViewById(R.id.btnAsk);//ask kad trazimo iz necije liste za doniranje - novo!!!
        btnOk3 = view.findViewById(R.id.btnOk);//ok kad se prikazuje samo - novo!!!
        btnCancel2 = view.findViewById(R.id.btnCancel);//cancel kad trazimo iz necije liste za doniranje - novo!!!
        txtDescription2 = view.findViewById(R.id.txtDesc2);//description kada se otvara za pregled samo - novo!!!
        txtName = view.findViewById(R.id.txtName);
        txtSize = view.findViewById(R.id.txtSize);
        txtDescription = view.findViewById(R.id.txtDesc);
        linearLayout3 = view.findViewById(R.id.lin3);//ok kad se prikazuje samo - staro!!!
        linearLayout2 = view.findViewById(R.id.lin2);//ok i cancel kad se dodaje novi
        linearLayout4 = view.findViewById(R.id.linAskCancel);//ask i cancel kad pregledavamo kod trugog nekog - novo!!!
        linearLayout5 = view.findViewById(R.id.linOk);//ok kad se prikazuje samo - novo!!!
        fab = view.findViewById(R.id.fab);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        recyclerView = view.findViewById(R.id.listArticleRecycler);


        listArticles = new HashMap<>();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();

        articleRecycler = new ArticleRecycler(imageDonate, listArticles, relAddArticle, txtName, txtSize, txtDescription, btnOk, btnOk2, btnCancel, fab, linearLayout2, linearLayout3,
                                                relViewArticle, linearLayout4, linearLayout5, btnAsk, btnOk3, btnCancel2, txtDescription2, "donate");


        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(articleRecycler);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!relAddLayoutActive){
                    txtName.setText("");
                    txtSize.setText("");
                    txtDescription.setText("");
                    imageDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                    relAddArticle.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    linearLayout3.setVisibility(View.INVISIBLE);
                    relAddLayoutActive = true;
                }


            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                relAddArticle.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                relAddLayoutActive =false;
            }
        });

        final Map<String, String> articleForAdd = new HashMap<>();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                articleForAdd.put("name", txtName.getText().toString());
                articleForAdd.put("size", txtSize.getText().toString());
                articleForAdd.put("description", txtDescription.getText().toString());
                articleForAdd.put("type", "donate");

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

                if(imageUri != null)
                {
                    StorageReference ref = storageReference.child("article_images/" + tmp);
                    ref.putFile(imageUri);
                }


                relAddArticle.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                relAddLayoutActive =false;


                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, new DonateFragment())
                        .commit();
            }
        });

        Bundle bundle = getArguments();
        if(bundle!=null){
            userID = bundle.getString("userID");
            userType = bundle.getString("type");
            this.fillData(userID, userType);
        }


        if(imageUri==null && bundle == null) {
            imageDonate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                2000);
                    } else {
                        pickFromGallery();
                    }
                }
            });
        }

        if(bundle==null){
            this.fillData(mAuth.getCurrentUser().getUid(), "donate");
        }

        imageUri = null;

        return view;
    }

    private void fillData(final String uID, final String type){ //mAuth.getCurrentUser().getUid()
        firebaseFirestore.collection("Users/" + uID + "/Articles")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {

                                String articleID = doc.getDocument().getId();
                                Article article = doc.getDocument().toObject(Article.class).withId(articleID, i);

                                if(article.getType().matches(type)){
                                    listArticles.put(i, article);
                                    i++;
                                    articleRecycler.userID = uID;
                                    articleRecycler.notifyDataSetChanged();

                                }
                            }
                        }
                    }
                });
    }

    private void pickFromGallery(){

        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent,1000);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result code is RESULT_OK only if the user selects an Image
        if(resultCode == getActivity().RESULT_OK) {
            if(requestCode == 1000){
                imageUri = data.getData();
                bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageDonate.setImageBitmap(bitmap);
//                articleRecycler.imageUri = this.imageUri;
//                articleRecycler.notifyDataSetChanged();
            }
        }
    }

}