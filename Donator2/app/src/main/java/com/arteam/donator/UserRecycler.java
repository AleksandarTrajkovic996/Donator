package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class UserRecycler extends RecyclerView.Adapter<UserRecycler.ViewHolder> {


    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private Map<Integer,User> list;
    private String rang;

    public UserRecycler (Map<Integer, User> map){
        this.list = map;
        this.rang = null;
    }

    public UserRecycler (Map<Integer, User> map, String rang){
        this.list = map;
        this.rang = rang;
    }

    @NonNull
    @Override
    public UserRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data_user, viewGroup, false);
        context = viewGroup.getContext();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new UserRecycler.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserRecycler.ViewHolder holder, int i) {
        final String userId = list.get(i).userID;

        final String first_name = list.get(i).getFirst_name();
        final String last_name = list.get(i).getLast_name();
        final String points = list.get(i).getPoints();

        holder.setTxtDisplay(points, first_name + " "+ last_name);

        if(rang!=null && rang.matches("Y")){ //znaci da je pozvan za sortiranje
            if(i==0){
                holder.rankingImageView.setImageResource(R.drawable.gold_medal);
            }else if(i==1){
                holder.rankingImageView.setImageResource(R.drawable.silver_medal);
            }else if(i==2){
                holder.rankingImageView.setImageResource(R.drawable.bronze_medal);
            }
        }

        FirebaseSingleton.getInstance().storageReference
                .child("profile_images/" + userId)
                .getDownloadUrl()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("UserRecycler", "Download failed!");
                        holder.profileImageView.setImageResource(R.drawable.profile);
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.i("UserRecycler", "Profilna slika skinuta!");
                        if (uri != null) {
                            Glide.with(context)
                                    .load(uri)
                                    .into(holder.profileImageView);
                        }
                    }
        });

        holder.linUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AppCompatActivity compatActivity = (AppCompatActivity) view.getContext();
                Bundle bundle = new Bundle();
                bundle.putString("userID", userId);
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);

                FragmentManager fragmentManager = compatActivity.getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, profileFragment)
                        .commit();

            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View view;

        private ImageView profileImageView;
        private ImageView rankingImageView;
        private TextView userName;
        private TextView userPoints;
        private LinearLayout linUser;

        @SuppressLint("ResourceType")
        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            profileImageView = view.findViewById(R.id.user_profile);
            rankingImageView = view.findViewById(R.id.ranking_image);
            userName = view.findViewById(R.id.userName);
            userPoints = view.findViewById(R.id.userPoints);
            linUser = view.findViewById(R.id.linUser);
        }


            public void setTxtDisplay (String txtPoints, String txtName){
                userPoints.setText(txtPoints);
                userName.setText(txtName);
        }
    }
}
