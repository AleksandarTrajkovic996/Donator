package com.arteam.donator;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ArticleRecycler extends RecyclerView.Adapter<ArticleRecycler.ViewHolder> {

    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    private Map<Integer,Article> list;

    private RelativeLayout relativeLayout;
    private TextView txtName;
    private TextView txtSize;
    private TextView txtDescription;
    private Button btnOk;
    private Button btnOk2;
    private Button btnCancel;
    private FloatingActionButton fab;
    private LinearLayout linearLayout3;
    private LinearLayout linearLayout2;
    private boolean relLayoutActive;
    private ImageView imageViewDonate;
    public Uri imageUri;
    private String type;
    private FirebaseStorage storage;
    private StorageReference storageReference;


    public ArticleRecycler(ImageView imageDonate, Map<Integer, Article> listArticles, RelativeLayout relAddArticle, TextView txtName, TextView txtSize, TextView txtDescription, Button btnOk, Button btnOk2, Button btnCancel, FloatingActionButton fab, LinearLayout lin2, LinearLayout lin3, String type) {
        this.imageUri = null;
        this.imageViewDonate = imageDonate;
        this.list = listArticles;
        this.relativeLayout = relAddArticle;
        this.txtName = txtName;
        this.txtSize = txtSize;
        this.txtDescription = txtDescription;
        this.btnOk = btnOk;
        this.btnOk2 = btnOk2;
        this.btnCancel = btnCancel;
        this.relLayoutActive = false;
        this.linearLayout3 = lin3;
        this.linearLayout2 = lin2;
        this.fab = fab;
        this.type = type;
    }

    public ArticleRecycler(Map<Integer, Article> listArticles, RelativeLayout relAddArticle, TextView txtName, TextView txtSize, TextView txtDescription, Button btnOk, Button btnOk2, Button btnCancel, FloatingActionButton fab, LinearLayout lin2, LinearLayout lin3, String type) {
        this.list = listArticles;
        this.relativeLayout = relAddArticle;
        this.txtName = txtName;
        this.txtSize = txtSize;
        this.txtDescription = txtDescription;
        this.btnOk = btnOk;
        this.btnOk2 = btnOk2;
        this.btnCancel = btnCancel;
        this.relLayoutActive = false;
        this.linearLayout3 = lin3;
        this.linearLayout2 = lin2;
        this.fab = fab;
        this.type = type;
    }


    @NonNull
    @Override
    public ArticleRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data, viewGroup, false);
        context = viewGroup.getContext();

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleRecycler.ViewHolder holder, int position) {

        final String articleId = list.get(position).articleID;

        final String name = list.get(position).getName();

        holder.setTxtDisplay(name);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!relLayoutActive){
                    txtName.setText("");
                    txtSize.setText("");
                    txtDescription.setText("");
                    imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                    relativeLayout.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    linearLayout3.setVisibility(View.INVISIBLE);
                    relLayoutActive = true;
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
                articleForAdd.put("type", type);

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


                relativeLayout.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                relLayoutActive=false;
                imageUri = null;
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                relativeLayout.setVisibility(View.INVISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);
                relLayoutActive=false;
            }
        });

        holder.one_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!relLayoutActive){

                    firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(articleId).get()
                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                    if (task.isSuccessful()) {

                                        if (task.getResult().exists()) {

                                            Article article = task.getResult().toObject(Article.class);

                                            txtName.setText(article.getName());
                                            txtSize.setText(article.getSize());
                                            txtDescription.setText(article.getDescription());

                                            relativeLayout.setVisibility(View.VISIBLE);
                                            linearLayout2.setVisibility(View.INVISIBLE);
                                            linearLayout3.setVisibility(View.VISIBLE);
                                            relLayoutActive = true;
                                        } else {

                                        }

                                    } else {

                                    }


                                }
                            });



                    btnOk2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            relativeLayout.setVisibility(View.INVISIBLE);
                            linearLayout2.setVisibility(View.INVISIBLE);
                            linearLayout3.setVisibility(View.INVISIBLE);
                            relLayoutActive=false;
                        }
                    });

                }
            }
        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        private TextView txtDisplay;
        private View view;
        private TextView one_data;


        @SuppressLint("ResourceType")
        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            one_data = (TextView) view.findViewById(R.id.txtDisplay);
        }


        public void setTxtDisplay(String txt){

            txtDisplay = view.findViewById(R.id.txtDisplay);

            txtDisplay.setText(txt);
        }
    }
}
