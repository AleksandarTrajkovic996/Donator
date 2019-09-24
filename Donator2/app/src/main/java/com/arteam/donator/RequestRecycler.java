package com.arteam.donator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

public class RequestRecycler extends RecyclerView.Adapter<RequestRecycler.ViewHolder> {


    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static SecureRandom random = new SecureRandom();


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

        Article articleTmp = listA.get(position);

        final String name = articleTmp.getName();
        final String size = articleTmp.getSize();
        final String desc = articleTmp.getDescription();
        final String typeArticle = articleTmp.getType();
        final String articleId = articleTmp.articleID;
        final String value = articleTmp.getValue();

        final String typeRequest = list.get(position).getType();
        final String fromId = list.get(position).getFromID();
        final String requestId = list.get(position).requestID;


        holder.setTxtDisplay(name, size);

        if(typeRequest.matches("active-ask")){
            holder.setBtnText("ACCEPT REQUEST");
        }else if(typeRequest.matches("active-offer")){
            holder.setBtnText("ACCEPT OFFER");
        }

        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Requests").document(requestId)
                        .update("type", "passive");

                String tmp = generateRandomString(8);

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
                            .whereEqualTo("name", name)
                            .whereEqualTo("size", size)
                            .whereEqualTo("type", "necessary")
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


                String tmp = generateRandomString(8);

                if (typeRequest.matches("active-ask")) {
                    final Map<String, String> notificationOffer = new HashMap<>();
                    String msg = "Your request for item '" + name +"' was denied.";
                    notificationOffer.put("text", msg);

                    firebaseFirestore.collection("Users/" + fromId + "/Notifications").document(tmp).set(notificationOffer);

                }
                else{//active-offer
                    final Map<String, String> notificationOffer = new HashMap<>();
                    String msg = "Your offer for item '" + name +"' was denied.";
                    notificationOffer.put("text", msg);

                    firebaseFirestore.collection("Users/" + fromId + "/Notifications").document(tmp).set(notificationOffer);

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
                                Log.d("Req", "Error getting documents: ", task.getException());
                            }
                        }
                    });

    }

    @Override
    public int getItemCount() {
            return list.size();
    }

    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {

            // 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            // debug
            System.out.format("%d\t:\t%c%n", rndCharAt, rndChar);

            sb.append(rndChar);
        }

        return sb.toString();

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

        public void setBtnText(String txt){
            btnAccept.setText(txt);
        }
    }
}
