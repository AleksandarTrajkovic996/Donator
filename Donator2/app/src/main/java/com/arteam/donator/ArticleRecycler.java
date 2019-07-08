package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    public String userID = null;


    //u DonateFragment objasnjeno cemu sta sluzi
    private RelativeLayout relViewArticle;
    private LinearLayout linearLayout4;
    private LinearLayout linearLayout5;
    private Button btnAsk;
    private Button btnCancel2;
    private Button btnOk3;
    private boolean relViewLayoutActive;
    private TextView txtDescription2;

    private Button btnOffer;

    private FirebaseStorage storage;
    private StorageReference storageReference;


    public ArticleRecycler(ImageView imageDonate, Map<Integer, Article> listArticles, RelativeLayout relAddArticle, TextView txtName, TextView txtSize, TextView txtDescription, Button btnOk, Button btnOk2, Button btnCancel, FloatingActionButton fab, LinearLayout lin2, LinearLayout lin3, String type) {

        this.mAuth = FirebaseAuth.getInstance();
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

    //poziva se iz NecessaryFragment
    public ArticleRecycler(Map<Integer, Article> listArticles, RelativeLayout relAddArticle, TextView txtName, TextView txtSize, TextView txtDescription, Button btnOk, Button btnOk2, Button btnCancel, FloatingActionButton fab, LinearLayout lin2, LinearLayout lin3,
                           RelativeLayout relViewArticle, LinearLayout linearLayout4, LinearLayout linearLayout5, Button btnOffer, Button btnOk3, Button btnCancel2, TextView txtDescription2, String type) {

        this.mAuth = FirebaseAuth.getInstance();
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

        this.relViewArticle = relViewArticle;
        this.linearLayout4 = linearLayout4;
        this.linearLayout5 = linearLayout5;
        this.btnOffer = btnOffer;
        this.btnOk3 = btnOk3;
        this.btnCancel2 = btnCancel2;
        this.relViewLayoutActive = false;
        this.txtDescription2 = txtDescription2;

        this.type = type;
    }

    //poziva se iz DonateFragment
    public ArticleRecycler(ImageView imageDonate, Map<Integer, Article> listArticles, RelativeLayout relAddArticle, TextView txtName, TextView txtSize, TextView txtDescription, Button btnOk, Button btnOk2, Button btnCancel, FloatingActionButton fab, LinearLayout lin2, LinearLayout lin3,
                           RelativeLayout relViewArticle, LinearLayout linearLayout4, LinearLayout linearLayout5, Button btnAsk, Button btnOk3, Button btnCancel2, TextView txtDescription2, String type) {

        this.mAuth = FirebaseAuth.getInstance();
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

        this.relViewArticle = relViewArticle;
        this.linearLayout4 = linearLayout4;
        this.linearLayout5 = linearLayout5;
        this.btnAsk = btnAsk;
        this.btnOk3 = btnOk3;
        this.btnCancel2 = btnCancel2;
        this.relViewLayoutActive = false;
        this.txtDescription2 = txtDescription2;

        this.type = type;
    }
    @NonNull
    @Override
    public ArticleRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = null;
        if(type.matches("donate")) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data_article, viewGroup, false);
            context = viewGroup.getContext();
        }else if(type.matches("necessary")){
             view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data_necessary, viewGroup, false);
            context = viewGroup.getContext();
        }
        if(imageViewDonate != null) {
            imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
        }
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ArticleRecycler.ViewHolder holder, int position) {

        final String articleId = list.get(position).articleID;

        final String name = list.get(position).getName();
        final String size = list.get(position).getSize();

        holder.setTxtDisplay(name, size);

            if(!list.get(position).getType().matches("necessary") ) {
        storageReference.child("article_images/" + articleId).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                byte[] bytesImg = bytes;
                if (bytes != null) {
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    holder.imageViewArticle.setImageBitmap(bm);
                } else {
                    holder.imageViewArticle.setImageResource(R.drawable.hand_heart_donate_icon);
                }
            }
        });
    }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //1. slucaj - kod prijatelja je u donate listi
                if(!mAuth.getCurrentUser().getUid().matches(userID) && type.matches("donate")){
                    if (!relViewLayoutActive) {

                        firebaseFirestore.collection("Users/" + userID + "/Articles").document(articleId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            if (task.getResult().exists()) {

                                                Article article = task.getResult().toObject(Article.class);

                                                //     txtName.setText(article.getName());
                                                //     txtSize.setText(article.getSize());
                                                //     txtDescription.setText(article.getDescription());

                                                txtDescription2.setText(article.getDescription());

                                                relViewArticle.setVisibility(View.VISIBLE);
                                                linearLayout4.setVisibility(View.VISIBLE);
                                                linearLayout5.setVisibility(View.INVISIBLE);
                                                relViewLayoutActive = true;
                                                //      txtName.setEnabled(false);
                                                //      txtSize.setEnabled(false);
                                                txtDescription2.setEnabled(false);
                                            } else {

                                            }

                                        } else {

                                        }


                                    }
                                });


                        btnAsk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final Map<String, String> articleAsked1 = new HashMap<>();
                                articleAsked1.put("from", userID);
                                articleAsked1.put("idArticle", articleId);

                                final Map<String, String> articleAsked2 = new HashMap<>();
                                articleAsked2.put("for", mAuth.getCurrentUser().getUid());
                                articleAsked2.put("idArticle", articleId);

                                Random generator = new Random();
                                StringBuilder randomStringBuilder = new StringBuilder();
                                int randomLength = generator.nextInt((15 - 10) + 1) + 10;
                                char tempChar;
                                for (int i = 0; i < randomLength; i++){
                                    tempChar = (char) (generator.nextInt(96) + 32);
                                    randomStringBuilder.append(tempChar);
                                }
                                String tmp = randomStringBuilder.toString();

                                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Requests").document(tmp).set(articleAsked1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });

                                firebaseFirestore.collection("Users/" + userID + "/Requests").document(tmp).set(articleAsked2)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });

                                if (imageViewDonate != null) {
                                    imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                                    imageViewDonate.setEnabled(true);
                                }
                                //         txtName.setEnabled(true);
                                //         txtSize.setEnabled(true);
                                txtDescription2.setEnabled(true);
                                relViewArticle.setVisibility(View.INVISIBLE);
                                linearLayout4.setVisibility(View.INVISIBLE);
                                linearLayout5.setVisibility(View.INVISIBLE);
                                relViewLayoutActive = false;
                            }
                        });

                        btnCancel2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (imageViewDonate != null) {
                                    imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                                    imageViewDonate.setEnabled(true);
                                }
                                //         txtName.setEnabled(true);
                                //         txtSize.setEnabled(true);
                                txtDescription2.setEnabled(true);
                                relViewArticle.setVisibility(View.INVISIBLE);
                                linearLayout4.setVisibility(View.INVISIBLE);
                                linearLayout5.setVisibility(View.INVISIBLE);
                                relViewLayoutActive = false;
                            }
                        });

                    }
                }
                //2. slucaj - kod prijatelja je u necessary listi
                else if(!mAuth.getCurrentUser().getUid().matches(userID) && type.matches("necessary")){
                    if (!relViewLayoutActive) {

                        firebaseFirestore.collection("Users/" + userID + "/Articles").document(articleId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            if (task.getResult().exists()) {

                                                Article article = task.getResult().toObject(Article.class);

                                                //     txtName.setText(article.getName());
                                                //     txtSize.setText(article.getSize());
                                                //     txtDescription.setText(article.getDescription());

                                                txtDescription2.setText(article.getDescription());

                                                relViewArticle.setVisibility(View.VISIBLE);
                                                linearLayout4.setVisibility(View.VISIBLE);
                                                linearLayout5.setVisibility(View.INVISIBLE);
                                                relViewLayoutActive = true;
                                                //      txtName.setEnabled(false);
                                                //      txtSize.setEnabled(false);
                                                txtDescription2.setEnabled(false);
                                            } else {

                                            }

                                        } else {

                                        }


                                    }
                                });


                        btnOffer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final Map<String, String> articleAsked1 = new HashMap<>();
                                articleAsked1.put("for", userID);
                                articleAsked1.put("idArticle", articleId);

                                final Map<String, String> articleAsked2 = new HashMap<>();
                                articleAsked2.put("from", mAuth.getCurrentUser().getUid());
                                articleAsked2.put("idArticle", articleId);

                                Random generator = new Random();
                                StringBuilder randomStringBuilder = new StringBuilder();
                                int randomLength = generator.nextInt((15 - 10) + 1) + 10;
                                char tempChar;
                                for (int i = 0; i < randomLength; i++){
                                    tempChar = (char) (generator.nextInt(96) + 32);
                                    randomStringBuilder.append(tempChar);
                                }
                                String tmp = randomStringBuilder.toString();

                                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Requests").document(tmp).set(articleAsked1)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });

                                firebaseFirestore.collection("Users/" + userID + "/Requests").document(tmp).set(articleAsked2)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        });


                                if (imageViewDonate != null) {
                                    imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                                    imageViewDonate.setEnabled(true);
                                }
                                //         txtName.setEnabled(true);
                                //         txtSize.setEnabled(true);
                                txtDescription2.setEnabled(true);
                                relViewArticle.setVisibility(View.INVISIBLE);
                                linearLayout4.setVisibility(View.INVISIBLE);
                                linearLayout5.setVisibility(View.INVISIBLE);
                                relViewLayoutActive = false;
                            }
                        });

                        btnCancel2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (imageViewDonate != null) {
                                    imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                                    imageViewDonate.setEnabled(true);
                                }
                                //         txtName.setEnabled(true);
                                //         txtSize.setEnabled(true);
                                txtDescription2.setEnabled(true);
                                relViewArticle.setVisibility(View.INVISIBLE);
                                linearLayout4.setVisibility(View.INVISIBLE);
                                linearLayout5.setVisibility(View.INVISIBLE);
                                relViewLayoutActive = false;
                            }
                        });

                    }
                }
                //3. slucaj - sve ostalo (kod prijatelja u donated i recived listama ili kod sebe u svim listama)
                else {
                    if (!relViewLayoutActive) {

                        firebaseFirestore.collection("Users/" + userID + "/Articles").document(articleId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            if (task.getResult().exists()) {

                                                Article article = task.getResult().toObject(Article.class);

                                                //     txtName.setText(article.getName());
                                                //     txtSize.setText(article.getSize());
                                                //     txtDescription.setText(article.getDescription());

                                                txtDescription2.setText(article.getDescription());

                                                relViewArticle.setVisibility(View.VISIBLE);
                                                linearLayout4.setVisibility(View.INVISIBLE);
                                                linearLayout5.setVisibility(View.VISIBLE);
                                                relViewLayoutActive = true;
                                                //      txtName.setEnabled(false);
                                                //      txtSize.setEnabled(false);
                                                txtDescription2.setEnabled(false);
                                            } else {

                                            }

                                        } else {

                                        }


                                    }
                                });

                        btnOk3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                if (imageViewDonate != null) {
                                    imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                                    imageViewDonate.setEnabled(true);
                                }
                                //         txtName.setEnabled(true);
                                //         txtSize.setEnabled(true);
                                txtDescription2.setEnabled(true);
                                relViewArticle.setVisibility(View.INVISIBLE);
                                linearLayout4.setVisibility(View.INVISIBLE);
                                linearLayout5.setVisibility(View.INVISIBLE);
                                relViewLayoutActive = false;
                            }
                        });

                    }

                }

            }
        });



    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        private TextView txtDisplayName;
        private View view;
        private TextView txtDisplaySize;
        private ImageView imageViewArticle;

        @SuppressLint("ResourceType")
        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            txtDisplayName = view.findViewById(R.id.txtDisplayName);
            txtDisplaySize = (TextView) view.findViewById(R.id.txtDisplaySize);
            imageViewArticle = (ImageView) view.findViewById(R.id.imageViewDisplayArticle);
        }


        public void setTxtDisplay(String txt, String size){

            txtDisplayName = view.findViewById(R.id.txtDisplayName);
            txtDisplaySize = (TextView) view.findViewById(R.id.txtDisplaySize);

            txtDisplaySize.setText(size);
            txtDisplayName.setText(txt);
        }
    }
}
