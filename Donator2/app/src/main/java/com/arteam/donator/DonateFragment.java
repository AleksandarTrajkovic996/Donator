package com.arteam.donator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.IOException;
import java.security.SecureRandom;
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
    private ConstraintLayout relAddArticle;
    private ConstraintLayout relViewArticle;
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
    private Map<String, String> listOfValue;
    private RecyclerView contentDonate;


    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static SecureRandom random = new SecureRandom();


    @SuppressLint("RestrictedApi")
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


        listOfValue = new HashMap<>();
        listOfValue.put("patike", "5");
        listOfValue.put("jakna", "10");
        listOfValue.put("pantalone", "4");
        listOfValue.put("kosulja", "4");
        listOfValue.put("bluza", "3");
        listOfValue.put("majica", "2");
        listOfValue.put("bunda", "12");
        listOfValue.put("default", "5");

        articleRecycler = new ArticleRecycler(imageDonate, listArticles, relAddArticle, txtName, txtSize, txtDescription, btnOk, btnOk2, btnCancel, fab, linearLayout2, linearLayout3,
                                                relViewArticle, linearLayout4, linearLayout5, btnAsk, btnOk3, btnCancel2, txtDescription2, "donate");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(container.getContext());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(dividerItemDecoration);
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
                    recyclerView.setEnabled(false);
                    relAddLayoutActive = true;
                }


            }
        });

        //odustajanje
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                relAddArticle.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                recyclerView.setEnabled(true);
                relAddLayoutActive =false;
            }
        });


        //dodavanje artikla u bazi
        final Map<String, String> articleForAdd = new HashMap<>();
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                articleForAdd.put("name", txtName.getText().toString());
                articleForAdd.put("size", txtSize.getText().toString());
                articleForAdd.put("description", txtDescription.getText().toString());
                articleForAdd.put("type", "donate");
                if (listOfValue.get(txtName.getText().toString()) != null)
                    articleForAdd.put("value", listOfValue.get(txtName.getText().toString()));
                else
                    articleForAdd.put("value", listOfValue.get("default"));


                //dodavanje artikla i slike sa istim ID-em
                String tmp = generateRandomString(8);
                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(tmp).set(articleForAdd)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Donate", "Neuspesno dodavanje artikla");
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i("Donate", "Uspesno dodavanje artikla");
                                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentManager.beginTransaction()
                                        .replace(R.id.nav_main, new DonateFragment(), "Donate_Fragment_TAG")
                                        .commit();
                            }
                });

                writeImageOnStorage(tmp);

                relAddArticle.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                recyclerView.setEnabled(true);
                relAddLayoutActive =false;
            }
        });


        //popunjavanje podataka drugog korisnika
        Bundle bundle = getArguments();
        if(bundle!=null){
            userID = bundle.getString("userID");
            userType = bundle.getString("type");
            this.fillData(userID, userType);
        }

        //zabrana fab-u
        if(userID != null)
            if(!mAuth.getCurrentUser().getUid().matches(userID) || userType.matches("donated") || userType.matches("received")) {
                fab.setVisibility(View.INVISIBLE);
          }

        //biranje slike
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

        //popunjavanje svojih podataka iz baze
        if(bundle==null){
            this.fillData(mAuth.getCurrentUser().getUid(), "donate");
        }

        imageUri = null;

        return view;
    }

    private void fillData(final String uID, final String type){
        firebaseFirestore.collection("Users/" + uID + "/Articles")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            String articleID = doc.getDocument().getId();
                            Article article = doc.getDocument().toObject(Article.class).withId(articleID, i);

                            switch (doc.getType()) {
                                case ADDED:
                                    if (article.getType().matches(type)) {
                                        Log.i(type, "Added");
                                        listArticles.put(i, article);
                                        i++;
                                        articleRecycler.userID = uID;
                                        articleRecycler.notifyDataSetChanged();
                                    }
                                    break;
                                case MODIFIED:
                                    if(!article.getType().matches(type)) {
                                        //promenio se tip u medjuvremenu, npr. korisnik je u svoje 'donate' artikle, neko je prihvatio jedan njegov artikl, tada se menja tip: donate -> donated
                                        Log.i(type, "Modified");
                                        listArticles.remove(article);
                                        articleRecycler.userID = uID;
                                        //ostaje refresh prikaza!!!!
                                        articleRecycler.notifyItemRemoved(article.id);
                                        articleRecycler.notifyItemRangeChanged(article.id, listArticles.size());
                                        articleRecycler.notifyDataSetChanged();
                                    }
                                    break;
                                case REMOVED:
                                    if (article.getType().matches(type)) {
                                        Log.i(type, "Removed");
                                    }
                                    break;
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

    private void writeImageOnStorage(String id){

        if(imageUri != null)
        {
            StorageReference ref = storageReference.child("article_images/" + id);
            UploadTask uploadTask = ref.putFile(imageUri);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("Article", "Neuspesno dodavanje slike artikla");
                    Toast.makeText(getActivity(), "Something is wrong, try again to add photo!", Toast.LENGTH_SHORT).show();
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.i("Article", "Uspesno dodata slika artikla");
                }
            });

        }
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
            }
        }
    }

}