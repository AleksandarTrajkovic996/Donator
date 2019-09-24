package com.arteam.donator;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NecessaryArticlesService extends Service {

    private static final String CHANNEL_ID = "ID_CHANNEL";
    private boolean isRunning = true;
    private List<Article> necessaryArticles;
    private static int notificationID ;
    private boolean isCalled = false;



    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("ServiceArticles", "OnCreate");
        this.createNotificationChannel();
        necessaryArticles = new ArrayList<>();
        notificationID = 0;
        isCalled = false;
    }

    private void findNecessary(){

        FirebaseSingleton.getInstance().firebaseFirestore.collection("Users") //prolazi kroz sve korisnike
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        int i=0;
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){

                            final String userID = doc.getDocument().getId();

                            if(userID.matches(FirebaseSingleton.getInstance().mAuth.getUid()))
                                continue; //preskace sebe

                            final User user = doc.getDocument().toObject(User.class).withId(userID, i);
                            i++;

                            FirebaseSingleton.getInstance().firebaseFirestore
                                    .collection("Users/" + userID + "/Articles")
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                            int j=0;
                                            for (DocumentChange d : queryDocumentSnapshots.getDocumentChanges()) {

                                                String articleID = d.getDocument().getId();
                                                Article article = d.getDocument().toObject(Article.class).withId(articleID, j);
                                                j++;

                                                switch (d.getType()) {
                                                    case ADDED:
                                                        if(article.getType().matches("donate")) { //korisnik dodao artikl
                                                            if(isThere(article)){   //bas taj je meni potreban
                                                                User u = new User();
                                                                u = user;
                                                                sendNotification(article, u); //obavesti da postoji neko sa tim artiklom
                                                                Log.i("ServiceArticles", "user " + u.userID);
                                                            }
                                                            Log.i("Service", "Added");
                                                        }
                                                        break;
                                                    case REMOVED:

                                                        Log.i("ServiceArticles", "Removed");
                                                        break;
                                                    case MODIFIED:

                                                        Log.i("ServiceArticles", "Modified");
                                                        break;
                                                }
                                            }
                                        }
                                    });
                        }
                        isCalled = true;
                    }
                });
    }

    private boolean isThere(Article article){
        for(Article a : necessaryArticles){
            if(a.getName().matches(article.getName()))
                return true;
        }
        return false;
    }

    private void fillData(){
        String userID = FirebaseSingleton.getInstance().mAuth.getCurrentUser().getUid();

        FirebaseSingleton.getInstance().firebaseFirestore
                .collection("Users/" + userID + "/Articles")
                .whereEqualTo("type", "necessary")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            String articleID = doc.getDocument().getId();
                            Article article = doc.getDocument().toObject(Article.class).withId(articleID, i);

                            switch (doc.getType()) {
                                case MODIFIED:
                                    Log.i("ServiceArticles", "MODIFIED " + article.getType());
                                    break;
                                case REMOVED:
                                    Log.i("ServiceArticles", "REMOVED " + article.getType());
                                    necessaryArticles.remove(i);
                                    if(isCalled)
                                    findNecessary();
                                    break;
                                case ADDED:
                                    Log.i("ServiceArticles", "ADDED " + article.getType());
                                    necessaryArticles.add(article);
                                    if(isCalled)
                                    findNecessary();
                                    break;
                            }
                        }
                    }
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("ServiceArticles", "OnStartCommand");
        necessaryArticles = new ArrayList<>();
        notificationID = 0;

        Thread threadFillData = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    Looper.prepare();
                    fillData();

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for(Article a : necessaryArticles){
                        Log.i("ServiceArticles", a.getName());
                    }

                    findNecessary();
                }
                Looper.loop();
            }
        });

        threadFillData.start();
         return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("ServiceArticles", "onDestroy");
        isRunning = false;
        isCalled = false;
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendNotification(Article a, User user) {
        notificationID ++;

        Intent intent = new Intent(this, NavigationMainActivity.class);
        intent.putExtra("userID", user.userID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, notificationID , intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.necessary)
                .setContentTitle("The item " + a.getName() + " was found!")
                .setContentText("User " + user.getFirst_name() + " has the item you need!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "necessary";
            String description = "article_found";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
