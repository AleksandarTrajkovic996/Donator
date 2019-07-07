package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class RequestRecycler extends RecyclerView.Adapter<RequestRecycler.ViewHolder> {


    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;



    private Map<Integer,User> list;

    @NonNull
    @Override
    public RequestRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data_user, viewGroup, false);
        context = viewGroup.getContext();

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new RequestRecycler.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestRecycler.ViewHolder viewHolder, int i) {

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

            txtDisplayName = (TextView) view.findViewById(R.id.txtDisplayName);
            txtDisplaySize = (TextView) view.findViewById(R.id.txtDisplaySize);
        }


        public void setTxtDisplay(String txt, String size){

            txtDisplaySize.setText(size);
            txtDisplayName.setText(txt);
        }
    }
}
