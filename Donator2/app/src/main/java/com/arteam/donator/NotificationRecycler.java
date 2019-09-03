package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class NotificationRecycler extends RecyclerView.Adapter<NotificationRecycler.ViewHolder>  {



    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    public String userID = null;

    private Map<Integer,Notification> list;

    public NotificationRecycler(Map<Integer, Notification> l){
        list = l;
    }

    @NonNull
    @Override
    public NotificationRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data_notification, viewGroup, false);
        context = viewGroup.getContext();

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new NotificationRecycler.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationRecycler.ViewHolder holder, int position) {
        String text = list.get(position).text;
        holder.setTxtDisplay(text);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        private TextView txtDisplayText;
        private View view;

        @SuppressLint("ResourceType")
        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;
            txtDisplayText = view.findViewById(R.id.txtNotification);
        }


        public void setTxtDisplay(String txt){

            txtDisplayText.setText(txt);
        }
    }
}
