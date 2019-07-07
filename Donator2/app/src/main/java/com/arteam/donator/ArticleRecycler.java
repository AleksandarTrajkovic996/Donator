package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.util.Map;

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
    public void onBindViewHolder(@NonNull ArticleRecycler.ViewHolder holder, int position) {

        final String articleId = list.get(position).articleID;

        final String name = list.get(position).getName();
        final String size = list.get(position).getSize();

        holder.setTxtDisplay(name, size);

        holder.txtDisplayName.setOnClickListener(new View.OnClickListener() {
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

                                            if(!article.getType().matches("necessary") ) {
                                                imageViewDonate.setEnabled(false);
                                                storageReference.child("article_images/" + articleId).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                    @Override
                                                    public void onSuccess(byte[] bytes) {
                                                        byte[] bytesImg = bytes;
                                                        if (bytes != null) {
                                                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                            imageViewDonate.setImageBitmap(bm);
                                                        } else {
                                                            imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                                                        }
                                                    }
                                                });
                                            }
                                            relativeLayout.setVisibility(View.VISIBLE);
                                            linearLayout2.setVisibility(View.INVISIBLE);
                                            linearLayout3.setVisibility(View.VISIBLE);
                                            relLayoutActive = true;
                                            txtName.setEnabled(false);
                                            txtSize.setEnabled(false);
                                            txtDescription.setEnabled(false);
                                        } else {

                                        }

                                    } else {

                                    }


                                }
                            });

                    btnOk2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(imageViewDonate != null) {
                                imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                                imageViewDonate.setEnabled(true);
                            }
                            txtName.setEnabled(true);
                            txtSize.setEnabled(true);
                            txtDescription.setEnabled(true);
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


        private TextView txtDisplayName;
        private View view;
        private TextView txtDisplaySize;


        @SuppressLint("ResourceType")
        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            txtDisplaySize = (TextView) view.findViewById(R.id.txtDisplaySize);
        }


        public void setTxtDisplay(String txt, String size){

            txtDisplayName = view.findViewById(R.id.txtDisplayName);
            txtDisplaySize = (TextView) view.findViewById(R.id.txtDisplaySize);

            txtDisplaySize.setText(size);
            txtDisplayName.setText(txt);
        }
    }
}
