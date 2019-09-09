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
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NecessaryArticlesService extends Service {

    private static final String CHANNEL_ID = "ID_CHANNEL";
    private boolean isRunning = true;
    private List<Article> necessaryArticles = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();

       //startForeground(0, new Notification());
        this.createNotificationChannel();
    }

    private void fillData(){
        String userID = FirebaseSingleton.getInstance().mAuth.getCurrentUser().getUid();

        FirebaseSingleton.getInstance().firebaseFirestore.collection("Users/" + userID + "/Articles").whereEqualTo("type", "necessary")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        int i = 0;
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            String articleID = doc.getDocument().getId();
                            Article article = doc.getDocument().toObject(Article.class).withId(articleID, i);

                            switch (doc.getType()) {
                                case MODIFIED:
                                    Log.i("Service", "MODIFIED " + article.getType());
                                    break;
                                case REMOVED:
                                    Log.i("Service", "REMOVED " + article.getType());
                                    necessaryArticles.remove(i);
                                    findNecessary();
                                    break;
                                case ADDED:
                                    necessaryArticles.add(article);
                                    findNecessary();
                                    break;
                            }
                        }
                    }
                });
    }

    private boolean isThere(Article article){
        for(Article a : necessaryArticles){
            if(a.getName().matches(article.getName()) && a.getSize().matches(article.getSize()))
                return true;
        }
        return false;
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

                            FirebaseSingleton.getInstance().firebaseFirestore.collection("Users/" + userID + "/Articles")
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
                                                                sendNotification(article, user.getFirst_name(), userID, j); //obavesti da postoji neko sa tim artiklom
                                                            }
                                                            Log.i("Service", "Added");
                                                        }
                                                        break;
                                                    case REMOVED:

                                                        Log.i("Service", "Removed");
                                                        break;
                                                    case MODIFIED:

                                                        Log.i("Service", "Modified");
                                                        break;
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         super.onStartCommand(intent, flags, startId);

          //startForeground(0, new Notification());

            Thread threadFillData = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        fillData();
                        try {
                            Thread.sleep(2000);
                            for (Article a : necessaryArticles) {
                                Log.i("Service", a.getName() + " " + a.getSize());
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            Thread threadFindNecessary = new Thread(new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        findNecessary();
                    }
                }
            });

            threadFillData.start();
            threadFindNecessary.start();
         return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("Service", "onDestroy");
        isRunning = false;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendNotification(Article a, String userName, String userID, int j) {

        Intent intent = new Intent(this, NavigationMainActivity.class);
        intent.putExtra("userID", userID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.necessary)
                .setContentTitle("The item was found!")
                .setContentText("User " + userName + " has the item you need!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(j, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "necessary";
            String description = "article_found";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
