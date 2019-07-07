package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
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

    public UserRecycler (Map<Integer, User> map){
        this.list = map;
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

        holder.setTxtDisplay(first_name);

        holder.one_data_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(!relLayoutActive){
//
//                    firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(articleId).get()
//                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//
//                                    if (task.isSuccessful()) {
//
//                                        if (task.getResult().exists()) {
//
//                                            Article article = task.getResult().toObject(Article.class);
//
//                                            txtName.setText(article.getName());
//                                            txtSize.setText(article.getSize());
//                                            txtDescription.setText(article.getDescription());
//
//                                            relativeLayout.setVisibility(View.VISIBLE);
//                                            linearLayout2.setVisibility(View.INVISIBLE);
//                                            linearLayout3.setVisibility(View.VISIBLE);
//                                            relLayoutActive = true;
//                                        } else {
//
//                                        }
//
//                                    } else {
//
//                                    }
//
//
//                                }
//                            });
//
//
//
//                    btnOk2.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            relativeLayout.setVisibility(View.INVISIBLE);
//                            linearLayout2.setVisibility(View.INVISIBLE);
//                            linearLayout3.setVisibility(View.INVISIBLE);
//                            relLayoutActive=false;
//                        }
//                    });
//
//                }
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


            public void setTxtDisplay (String txt){

            txtDisplay = view.findViewById(R.id.txtDisplay);

            txtDisplay.setText(txt);
        }
    }
}
