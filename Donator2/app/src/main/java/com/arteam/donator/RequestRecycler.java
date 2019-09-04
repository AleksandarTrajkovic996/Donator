package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import static android.support.constraint.Constraints.TAG;

public class RequestRecycler extends RecyclerView.Adapter<RequestRecycler.ViewHolder> {


    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;


    public String userID = null;

    private Map<Integer,Request> list;
    private Map<Integer, Article> listA;
    FragmentManager fragmentManager;

    public RequestRecycler(Map<Integer,Request> l, Map<Integer,Article> la, FragmentManager fm){
        list = l;
        listA = la;
        fragmentManager = fm;
    }

    @NonNull
    @Override
    public RequestRecycler.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data_request, viewGroup, false);
        context = viewGroup.getContext();

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        return new RequestRecycler.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestRecycler.ViewHolder holder, final int position) {

        final String name = listA.get(position).getName();
        final String size = listA.get(position).getSize();
        final String desc = listA.get(position).getDescription();
        final String typeArticle = listA.get(position).getType();
        final String articleId = listA.get(position).articleID;
        final String value = listA.get(position).getValue();

        final String typeRequest = list.get(position).getType();
        final String fromId = list.get(position).getFromID();
        final String requestId = list.get(position).requestID;

        holder.setTxtDisplay(name, size);


        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Requests").document(requestId)
                        .update("type", "passive");

                String tmp = getNewID();

                if (typeRequest.matches("active-ask")){//nesto mi traze

                    //update points
                    nabaviUsera(mAuth.getUid(), new UserCallback() {
                        @Override
                        public void onCallback(User user) {
                            firebaseFirestore.collection("Users").document(mAuth.getUid())
                                    .update("points", Integer.toString(Integer.parseInt(user.getPoints()) + Integer.parseInt(value)));
                        }
                    });


                    //donate->donated
                    firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(articleId)
                            .update("type", "donated");

                    //necessary->received
                    firebaseFirestore.collection("Users/" + fromId + "/Articles")
                            .whereEqualTo("name", name).whereEqualTo("size", size).whereEqualTo("type", "necessary")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            firebaseFirestore.collection("Users/" + fromId + "/Articles").document(document.getId())
                                                    .update("type", "received");
                                            break; //samo jednom treba da se promeni
                                        }
                                    } else {

                                    }
                                }
                            });



                    final Map<String, String> notificationOffer = new HashMap<>();
                    notificationOffer.put("text", "Potrazivanje prihvaceno");

                    firebaseFirestore.collection("Users/" + fromId + "/Notifications").document(tmp).set(notificationOffer)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });

                }
                //active-offer
                else{ //nesto mi nude

                    //update points
                    nabaviUsera(fromId, new UserCallback() {
                        @Override
                        public void onCallback(User user) {
                            firebaseFirestore.collection("Users").document(fromId)
                                    .update("points", Integer.toString(Integer.parseInt(user.getPoints()) + Integer.parseInt(value)));
                        }
                    });

                    //necessary->received
                    firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles")
                            .whereEqualTo("name", name).whereEqualTo("size", size).whereEqualTo("type", "necessary")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(document.getId())
                                                    .update("type", "received");
                                            break; //samo jednom treba da se promeni
                                        }
                                    } else {

                                    }
                                }
                            });

                    //donate->donated
                    firebaseFirestore.collection("Users/" + fromId + "/Articles")
                            .whereEqualTo("name", name).whereEqualTo("size", size).whereEqualTo("type", "donate")
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            firebaseFirestore.collection("Users/" + fromId + "/Articles").document(document.getId())
                                                    .update("type", "donated");
                                            break; //samo jednom treba da se promeni
                                        }
                                    } else {

                                    }
                                }
                            });


                    final Map<String, String> notificationOffer = new HashMap<>();
                    notificationOffer.put("text", "Ponuda prihvacena");

                    firebaseFirestore.collection("Users/" + fromId + "/Notifications").document(tmp).set(notificationOffer)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                }

                //sprijateljivanje
                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Friends")
                        .whereEqualTo("friendID", fromId)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.getResult().size() == 0) {

                                    final Map<String, String> prijatelj = new HashMap<>();
                                    prijatelj.put("friendID", fromId);


                                    firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Friends").document(fromId).set(prijatelj)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });

                                    final Map<String, String> ja = new HashMap<>();
                                    ja.put("friendID", mAuth.getCurrentUser().getUid());
                                    firebaseFirestore.collection("Users/" + fromId + "/Friends").document(mAuth.getCurrentUser().getUid()).set(ja)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                }
                            }
                        });


                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, new RequestFragment())
                        .commit();
            }
        });

        holder.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Requests").document(requestId)
                        .update("type", "passive");


                String tmp = getNewID();

                if (typeRequest.matches("active-ask")) {
                    final Map<String, String> notificationOffer = new HashMap<>();
                    notificationOffer.put("text", "Potrazivanjenja odbijeno");

                    firebaseFirestore.collection("Users/" + fromId + "/Notifications").document(tmp).set(notificationOffer)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                }
                else{//active-offer
                    final Map<String, String> notificationOffer = new HashMap<>();
                    notificationOffer.put("text", "Ponuda odbijena");

                    firebaseFirestore.collection("Users/" + fromId + "/Notifications").document(tmp).set(notificationOffer)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, new RequestFragment())
                        .commit();
            }
        });
    }



    public void nabaviUsera(String uid, final UserCallback userCallback) {
            firebaseFirestore.collection("Users").document(uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {

                                User usr = task.getResult().toObject(User.class);

                                userCallback.onCallback(usr);
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });

    }

    @Override
    public int getItemCount() {
            return list.size();
    }

    public String getNewID(){
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt((15 - 10) + 1) + 10;
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{


        private TextView txtDisplayName;
        private View view;
        private TextView txtDisplaySize;
        private Button btnAccept;
        private Button btnCancel;

        @SuppressLint("ResourceType")
        public ViewHolder(View itemView) {
            super(itemView);

            view = itemView;

            btnAccept = (Button) view.findViewById(R.id.btnAcceptRequest);

            btnCancel = (Button) view.findViewById(R.id.btnCancelRequest);

            txtDisplayName = (TextView) view.findViewById(R.id.txtDisplayName);
            txtDisplaySize = (TextView) view.findViewById(R.id.txtDisplaySize);
        }


        public void setTxtDisplay(String txt, String size){

            txtDisplaySize.setText(size);
            txtDisplayName.setText(txt);
        }
    }
}
