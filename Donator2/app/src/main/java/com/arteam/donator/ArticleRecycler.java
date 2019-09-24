package com.arteam.donator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



public class ArticleRecycler extends RecyclerView.Adapter<ArticleRecycler.ViewHolder> {

    private Context context;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private final String TAG = "Article";

    private Map<Integer,Article> list;
    private Map<Integer,Article> listPomOne;//samo jedan element ce biti tu uvek
    private Map<Integer, Article> listArticlesForOffer;
    private ConstraintLayout relativeLayout;
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
    public ArrayList<Article> tmp;

    //u DonateFragment objasnjeno cemu sta sluzi
    private ConstraintLayout relViewArticle;
    private LinearLayout linearLayout4;
    private LinearLayout linearLayout5;
    private Button btnCancel2;
    private Button btnAsk;
    private Button btnOk3;
    private boolean relViewLayoutActive;
    private TextView txtDescription2;

    private Button btnOffer;

    private FirebaseStorage storage;
    private StorageReference storageReference;


    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";

    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
    private static SecureRandom random = new SecureRandom();
    private String clickedArticleId = "";
    private String clickedArticleName = "";

    private ScrollView scrollViewMap;
    private ConstraintLayout constraintBigMap;
    private Button btnCancelOffer;
    private Button btnOKOffer;
    private LinearLayout linearLayoutScrol;


    //poziva se iz NecessaryFragment
    public ArticleRecycler(Map<Integer, Article> listArticles, ConstraintLayout relAddArticle, TextView txtName, TextView txtSize, TextView txtDescription, Button btnOk, Button btnOk2, Button btnCancel, FloatingActionButton fab, LinearLayout lin2, LinearLayout lin3,
                           ConstraintLayout relViewArticle, LinearLayout linearLayout4, LinearLayout linearLayout5, Button btnOffer, Button btnOk3, Button btnCancel2, TextView txtDescription2, String type) {

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
    public ArticleRecycler(ImageView imageDonate, Map<Integer, Article> listArticles, ConstraintLayout relAddArticle, TextView txtName, TextView txtSize, TextView txtDescription, Button btnOk, Button btnOk2, Button btnCancel, FloatingActionButton fab, LinearLayout lin2, LinearLayout lin3,
                           ConstraintLayout relViewArticle, LinearLayout linearLayout4, LinearLayout linearLayout5, Button btnAsk, Button btnOk3, Button btnCancel2, TextView txtDescription2, String type) {

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

        listPomOne = new HashMap<Integer, Article>();
        listArticlesForOffer = new HashMap<Integer, Article>();

        if(type.matches("donate")) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data_article, viewGroup, false);
            context = viewGroup.getContext();
        } else if(type.matches("necessary")){
             view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.one_data_necessary, viewGroup, false);
            context = viewGroup.getContext();

            scrollViewMap = ((Activity) context).findViewById(R.id.scrolMap1);
            constraintBigMap = ((Activity) context).findViewById(R.id.constraintBigMap1);
            linearLayoutScrol = scrollViewMap.findViewById(R.id.linearLayoutScrollMap1);
            btnCancelOffer = ((Activity) context).findViewById(R.id.btnCancelOfferOrAskMaps1);
            btnOKOffer = ((Activity) context).findViewById(R.id.btnOfferOrAskMaps1);
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
        final String description = list.get(position).getDescription();
        final String value = list.get(position).getValue();


        holder.setTxtDisplay(name, size);

        if (!list.get(position).getType().matches("necessary")) {

            storageReference.child("article_images/" + articleId)
                    .getDownloadUrl()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i("ArticleRec", "Neuspesno ucitavanje slike");
                            holder.imageViewArticle.setImageResource(R.drawable.hand_heart_donate_icon);
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.i("ArticleRec", "Slika artikla je null!");
                            if (uri!=null) {
                               Log.i("ArticleRec", "Slika artikla skinuta!");
                               Glide.with(context)
                                       .load(uri)
                                       .into(holder.imageViewArticle);
                            }
                        }
                    });;
        }

        //ovako zabranjujemo kliktanje na artikle kad je otvoren fab

        holder.view.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View view) {

                if (relativeLayout.getVisibility() == View.INVISIBLE) {

                    //1. slucaj - kod prijatelja je u donate listi
                    if (!mAuth.getCurrentUser().getUid().matches(userID) && type.matches("donate")) {
                        if (!relViewLayoutActive) {

                            firebaseFirestore.collection("Users/" + userID + "/Articles").document(articleId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.isSuccessful()) {

                                                if (task.getResult().exists()) {

                                                    Article article = task.getResult().toObject(Article.class);
                                                    listPomOne.put(0, article);

                                                    txtDescription2.setText(article.getDescription());

                                                    relViewArticle.setVisibility(View.VISIBLE);
                                                    linearLayout4.setVisibility(View.VISIBLE);
                                                    linearLayout5.setVisibility(View.INVISIBLE);
                                                    relViewLayoutActive = true;
                                                    txtDescription2.setEnabled(false);
                                                }
                                            }
                                        }
                                    });

                            btnAsk.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    final Map<String, String> pomoc = new HashMap<>(); //pomoc da zeznemo snapshot kod dodavanaj artikla

                                    final Map<String, String> request = new HashMap<>();
                                    request.put("type", "active-ask");
                                    request.put("fromID", mAuth.getCurrentUser().getUid());
                                    request.put("articleID", articleId);

                                    String tmp = generateRandomString(8);

                                    firebaseFirestore.collection("Users/" + userID + "/Requests").document(tmp).set(request)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });


                                    final Map<String, String> notificationAsked = new HashMap<>();
                                    notificationAsked.put("text", "Od Vas se trazi: " + listPomOne.get(0).getName() + ", " + listPomOne.get(0).getSize());

                                    firebaseFirestore.collection("Users/" + userID + "/Notifications").document(tmp).set(notificationAsked) //id ce da budi isti kao i kod request-a
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });

                                    //dodavanje artikla koji trazimo u necessary ako ga vec nemamo
                                    firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles")
                                            .whereEqualTo("name", name).whereEqualTo("size", size).whereEqualTo("type", "necessary")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.getResult().size() == 0) {

                                                        final Map<String, String> articleForAdd = new HashMap<>();
                                                        articleForAdd.put("name", name);
                                                        articleForAdd.put("size", size);
                                                        articleForAdd.put("description", description);
                                                        articleForAdd.put("type", "necessary");

                                                        String tmp = generateRandomString(8);

                                                        firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles").document(tmp).set(articleForAdd)
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {

                                                                    }
                                                                });

                                                    }
                                                }
                                            });

                                    if (imageViewDonate != null) {
                                        imageViewDonate.setImageResource(R.drawable.hand_heart_donate_icon);
                                        imageViewDonate.setEnabled(true);
                                    }
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
                    else if (!mAuth.getCurrentUser().getUid().matches(userID) && type.matches("necessary")) {
                        if (constraintBigMap.getVisibility() == View.INVISIBLE) {

                            Article articlePOM = new Article();
                            articlePOM.setName(name);
                            articlePOM.setDescription(description);
                            articlePOM.setSize(size);
                            articlePOM.articleID = articleId;
                            articlePOM.setType("necessary");
                            articlePOM.setValue(value);

                            listArticlesForOffer.clear();
                            listPomOne.clear();

                            getArticlesForDonate(articlePOM, new ArticleListCallback() {
                                @Override
                                public void onCallback(List<Article> list) {

                                }

                                @Override
                                public void onCallback(Article article) {

                                }

                                @Override
                                public void onCallback(Map<Integer, Article> map) {
                                    Log.i("ArticleRec", articleId);
                                    if (map.size() > 0) {
                                        constraintBigMap.setVisibility(View.VISIBLE);

                                        showArticles(map);

                                        btnOKOffer.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                if(!clickedArticleId.isEmpty()){

                                                    relViewLayoutActive = false;
                                                    constraintBigMap.setVisibility(View.INVISIBLE);

                                                    Log.i("ArticleRec", "rec " + articleId);
                                                    Log.i("ArticleRec", "clicked " + clickedArticleId);

                                                    offerArticle(clickedArticleId);

                                                    Toast.makeText(context, "You have successfully sent the offer!", Toast.LENGTH_SHORT).show();

                                                }else{
                                                    Toast.makeText(context, "You must choose article!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                        btnCancelOffer.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                constraintBigMap.setVisibility(View.INVISIBLE);
                                                relViewLayoutActive = false;
                                                clickedArticleId = "";
                                                clickedArticleName = "";
                                            }
                                        });
                                    } else {
                                        Toast.makeText(context, "You don't have that type of articles for donate!", Toast.LENGTH_SHORT).show();
                                    }
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
                                                    txtDescription2.setText(article.getDescription());

                                                    relViewArticle.setVisibility(View.VISIBLE);
                                                    linearLayout4.setVisibility(View.INVISIBLE);
                                                    linearLayout5.setVisibility(View.VISIBLE);
                                                    relViewLayoutActive = true;
                                                    txtDescription2.setEnabled(false);


                                                    //novo:
                                                    fab.setEnabled(false);
                                                }
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
                                    txtDescription2.setEnabled(true);
                                    relViewArticle.setVisibility(View.INVISIBLE);
                                    linearLayout4.setVisibility(View.INVISIBLE);
                                    linearLayout5.setVisibility(View.INVISIBLE);
                                    relViewLayoutActive = false;

                                    //novo:
                                    fab.setEnabled(true);
                                }
                            });
                        }
                    }
                }
            }
        });


    }

    private void offerArticle(String articleID) {

        final Map<String, String> request = new HashMap<>();
        request.put("fromID", mAuth.getCurrentUser().getUid());
        request.put("articleID", articleID);
        request.put("type", "active-offer");

        String tmp = generateRandomString(8);
        firebaseFirestore.collection("Users/" + userID + "/Requests").document(tmp).set(request);

        final Map<String, String> notificationOffer = new HashMap<>();
        notificationOffer.put("text", "Imate novu ponudu");

        firebaseFirestore.collection("Users/" + userID + "/Notifications").document(tmp).set(notificationOffer); //id ce da budi isti kao i kod request-a
    }

    public void getArticlesForDonate(Article article, final ArticleListCallback articleListCallback) {
        final Map<Integer, Article> map = new HashMap<>();

        if (listArticlesForOffer.size() == 0 && article!=null) {
            firebaseFirestore.collection("Users/" + mAuth.getCurrentUser().getUid() + "/Articles")
                    .whereEqualTo("name", article.name)
                    .whereEqualTo("size", article.size)
                    .whereEqualTo("type", "donate")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            int i = 0;
                            for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()){
                                switch (document.getType()) {
                                    case REMOVED:
                                        break;

                                    case MODIFIED:
                                        break;

                                    case ADDED:
                                        String articleID = document.getDocument().getId();
                                        Article article = document.getDocument().toObject(Article.class).withId(articleID, i);
                                        listArticlesForOffer.put(i, article);
                                        map.put(i, article);
                                        i++;
                                        break;
                                }
                            }
                            articleListCallback.onCallback(map);

                        }
                    });
        }
    }

    private void getArticle(String articleId, final ArticleListCallback callback){

        firebaseFirestore.collection("Users/" + userID + "/Articles").document(articleId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Article article = null;
                        if (task.isSuccessful()) {

                            if (task.getResult().exists()) {

                                article = task.getResult().toObject(Article.class);

                                //txtDescription2.setText(article.getDescription());

                                //relViewArticle.setVisibility(View.VISIBLE);
                                //linearLayout4.setVisibility(View.VISIBLE);
                                //linearLayout5.setVisibility(View.INVISIBLE);
                                //relViewLayoutActive = true;
                                //txtDescription2.setEnabled(false);
                            }
                        }
                        if(article!=null)
                        callback.onCallback(article);
                    }
                });

    }

    private void showArticles(final Map<Integer, Article> articles){

        linearLayoutScrol.removeAllViews();
        //if(constraintBigMap.getVisibility() == View.INVISIBLE) {
        for (int i = 0; i < articles.size(); ++i) {
            String ID_1 = articles.get(i).articleID;

            final ToggleButton button = new ToggleButton(context);
            String ns = articles.get(i).name + ", " + articles.get(i).size;

            button.setText(ns);
            button.setTextOn(ns);
            button.setTextOff(ns);
            button.setTag(ID_1);
            button.setId(i);

            linearLayoutScrol.addView(button);
        }

        //pribavljaju se svi ToggleButton-i koji se prikazuju u LinearLayout-u
        int viewsInLinLayCount = linearLayoutScrol.getChildCount();
        for(int i=0; i<viewsInLinLayCount; i++){

            final ToggleButton buttonChild = (ToggleButton) linearLayoutScrol.getChildAt(i); //izdvaja se svaki ToggleButton posebno

            buttonChild.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { //stavlja se listener
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){

                        int btnId = buttonChild.getId();

                        for(int k = 0; k<articles.size(); k++){ //svi ostali se gase
                            if(k!=btnId){
                                ToggleButton tb = (ToggleButton) linearLayoutScrol.getChildAt(k);
                                tb.setChecked(false);
                            }
                            clickedArticleId = (String) buttonChild.getTag();
                            clickedArticleName = (String) buttonChild.getText();
                        }
                        buttonView.setBackgroundColor(Color.BLUE);
                    }else{
                        buttonView.setBackgroundColor(Color.LTGRAY);
                        clickedArticleId = "";
                    }
                }
            });
        }

    }

    public void treca() {
        if (listPomOne.size() == 1 && listArticlesForOffer.size() > 0) {


            for (int i = 0; i < listArticlesForOffer.size(); i++) { //saljemo sve ponude koje imamo

                String tmp = generateRandomString(8);
                final Map<String, String> request = new HashMap<>();
                request.put("fromID", mAuth.getCurrentUser().getUid());
                request.put("articleID", listArticlesForOffer.get(i).articleID);
                request.put("type", "active-offer");

                firebaseFirestore.collection("Users/" + userID + "/Requests").document(tmp).set(request)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                            }
                        });

                final Map<String, String> notificationOffer = new HashMap<>();
                notificationOffer.put("text", "Imate novu ponudu");

                firebaseFirestore.collection("Users/" + userID + "/Notifications").document(tmp).set(notificationOffer) //id ce da budi isti kao i kod request-a
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

            ////!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            listArticlesForOffer.clear();
            //!!!!!!!!!!!!!!!!!!!!!!!!
            listPomOne.clear();
        }
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
