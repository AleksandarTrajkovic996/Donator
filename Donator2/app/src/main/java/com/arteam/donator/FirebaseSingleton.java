package com.arteam.donator;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseSingleton {

    private static final FirebaseSingleton instance = new FirebaseSingleton();

    public DatabaseReference databaseReference;
    public StorageReference storageReference;
    public FirebaseFirestore firebaseFirestore;
    public FirebaseAuth mAuth;
    private FirebaseSingleton() {

        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

    }

    public static FirebaseSingleton getInstance() { return instance; }

    public static String escapeSpecialCharacters(String email) {
        return email.replaceAll("%", "%25")
                .replaceAll("\\.", "%2E")
                .replaceAll("#", "%23")
                .replaceAll("\\$", "%24")
                .replaceAll("/", "%2F")
                .replaceAll("\\[", "%5B")
                .replaceAll("]", "%5D");
    }


}