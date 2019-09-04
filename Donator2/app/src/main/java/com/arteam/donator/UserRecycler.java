package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class UserRecycler extends RecyclerView.Adapter<UserRecycler.ViewHolder> {


    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    private Map<Integer,User> list;
    private Map<Integer,User> list2;

    public UserRecycler (Map<Integer, User> map){
        this.list = map;
    }
    public UserRecycler (Map<Integer, User> map, Map<Integer, User> map2){
        this.list = map;
        this.list2 = map2;
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
    public void onBindViewHolder(@NonNull UserRecycler.ViewHolder holder, int i) {
        final String userId = list.get(i).userID;

        final String first_name = list.get(i).getFirst_name();
        final String last_name = list.get(i).getLast_name();
        final String points = list.get(i).getPoints();


        holder.setTxtDisplay(first_name, last_name + ", " + points);

        holder.one_data_user.setOnClickListener(new View.OnClickListener() {
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

        private TextView txtDisplay;
        private View view;
        private TextView one_data_user;


        @SuppressLint("ResourceType")
        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            one_data_user = (TextView) view.findViewById(R.id.txtDisplay);
        }


            public void setTxtDisplay (String txt, String txt2){

            txtDisplay = view.findViewById(R.id.txtDisplay);

            txtDisplay.setText(txt + " " + txt2);
        }
    }
}
