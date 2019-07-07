package com.arteam.donator;

import android.Manifest;
import android.app.ProgressDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
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
    private ImageView imageDonate;
    private Bitmap bitmap = null;
    private StorageReference storageReference;
    private Uri imageUri = null;
    private byte[] bytesProfile = null;
    FloatingActionButton fab;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_donate, container, false);

        imageDonate = view.findViewById(R.id.imageViewDonatePhoto);
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



        articleRecycler = new ArticleRecycler(imageDonate, listArticles, relAddArticle, txtName, txtSize, txtDescription, btnOk, btnOk2, btnCancel, fab, linearLayout2, linearLayout3, "donate");


        recyclerView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        recyclerView.setAdapter(articleRecycler);

        imageDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            2000);
                }
                else {
                    pickFromGallery();
                }
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

                                if(article.getType().matches("donate")){
                                      listArticles.put(i, article);
                                      i++;
                                      articleRecycler.notifyDataSetChanged();

                                }
                            }
                        }
                    }
                });
        return view;
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
                articleRecycler.imageUri = this.imageUri;
                articleRecycler.notifyDataSetChanged();
            }
        }
    }

}