package com.arteam.donator;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class RequestRecycler extends RecyclerView.Adapter<RequestRecycler.ViewHolder> {
    @NonNull
    @Override
    public RequestRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RequestRecycler.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return 0;
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
