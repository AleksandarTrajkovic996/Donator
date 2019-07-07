package com.arteam.donator;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.IOException;

public class ProfileFragment extends Fragment implements View.OnClickListener, View.OnKeyListener {

    View view;
    private ConstraintLayout layoutPhoto;
    private ConstraintLayout layoutProfileInfo;
    private ImageView profilePhoto;
    private EditText firstName;
    private EditText lastName;
    private EditText address;
    private TextView addPhotoTextView;
    private TextView numberDonatedProducts;
    private TextView numberReceivedProducts;
    private TextView phoneNumber;
    private TextView clickToAdd;
    private FirebaseAuth mAuth;
    private Button btnCancel;
    private Button btnEdit;
    private FirebaseFirestore firebaseFirestore;
    private Bitmap bitmap = null;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri = null;
    private byte[] bytesProfile = null;
    private String userID = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        final NavigationView navigationView = getActivity().findViewById(R.id.nav_view);

        clickToAdd = view.findViewById(R.id.addPhotoTxt);
        layoutPhoto = view.findViewById(R.id.layoutPhotoProfile);
        layoutProfileInfo = view.findViewById(R.id.layoutProfileInformation);
        firstName = view.findViewById(R.id.firstNameProfileTxt);
        lastName = view.findViewById(R.id.lastNameProfileTxt);
        address = view.findViewById(R.id.addressProfileTxt);
        phoneNumber = view.findViewById(R.id.phoneNumberProfileTxt);
        btnCancel = view.findViewById(R.id.btnCancelEditProfile);
        btnEdit = view.findViewById(R.id.btnEditProfile);
        profilePhoto = view.findViewById(R.id.profilePhotoAddImageView);
        addPhotoTextView = view.findViewById(R.id.addPhotoTxt);
        numberDonatedProducts = view.findViewById(R.id.numberDonatedProducts);
        numberReceivedProducts = view.findViewById(R.id.numberReceivedProducts);

        layoutPhoto.setOnClickListener(this);
        layoutProfileInfo.setOnClickListener(this);
        phoneNumber.setOnKeyListener(this);

        Bundle bundle = getArguments();
        if(bundle != null) {

            bytesProfile = bundle.getByteArray("profileImg");
            userID = bundle.getString("userID");
            if(bytesProfile!=null) {
                Bitmap bm = BitmapFactory.decodeByteArray(bytesProfile, 0, bytesProfile.length);
                DisplayMetrics dm = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
                profilePhoto.setImageBitmap(bm);
            }

            if(userID!=null){
                this.fillData(userID);
                clickToAdd.setVisibility(View.INVISIBLE);
                btnCancel.setVisibility(View.INVISIBLE);
                btnEdit.setVisibility(View.INVISIBLE);
                firstName.setEnabled(false);
                lastName.setEnabled(false);
                address.setEnabled(false);
                phoneNumber.setEnabled(false);
            }
        }

        if(userID==null){
            this.fillData(mAuth.getCurrentUser().getUid());
            clickToAdd.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
            firstName.setEnabled(true);
            lastName.setEnabled(true);
            address.setEnabled(true);
            phoneNumber.setEnabled(true);
        }


        numberDonatedProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idTmp = "";

                if(userID == null){
                     idTmp = mAuth.getUid();
                }else{
                    idTmp = userID;
                }

                Bundle bundle = new Bundle();
                bundle.putString("userID", idTmp);
                bundle.putString("type", "donated");
                DonateFragment donateFragment = new DonateFragment();
                donateFragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, donateFragment)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_donate);
            }
        });

        numberReceivedProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String idTmp = "";

                if(userID == null){
                    idTmp = mAuth.getUid();
                }else{
                    idTmp = userID;
                }

                Bundle bundle = new Bundle();
                bundle.putString("userID", idTmp);
                bundle.putString("type", "received");
                DonateFragment donateFragment = new DonateFragment();
                donateFragment.setArguments(bundle);

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, donateFragment)
                        .commit();
                navigationView.setCheckedItem(R.id.nav_donate);
            }
        });


        if(userID==null) {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.nav_main, new RankingFragment())
                        .commit();
                navigationView.setCheckedItem(R.id.nav_ranking);
            }
        });

            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (validation()) {
                        String userID = mAuth.getCurrentUser().getUid();

                        firebaseFirestore.collection("Users").document(userID)
                                .update("first_name", firstName.getText().toString(), "last_name",
                                        lastName.getText().toString(), "address", address.getText().toString(), "phone_number", phoneNumber.getText().toString());

                        writeImageOnStorage();
                        startActivity(new Intent(getActivity(), NavigationMainActivity.class));
                        getActivity().finish();
                    }
                }
            });


            addPhotoTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                2000);
                    } else {
                        pickFromGallery();
                    }
                }
            });

            profilePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                2000);
                    } else {
                        pickFromGallery();
                    }
                }
            });
        }

        return view;
    }

    private void writeImageOnStorage(){

        if(imageUri != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            
            StorageReference ref = storageReference.child("profile_images/"+ mAuth.getCurrentUser().getUid());
            ref.putFile(imageUri);
        }


    }

    private void pickFromGallery(){

        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        startActivityForResult(intent,1000);

    }

    private void fillData(String user_id){
       // String userID = mAuth.getUid();


            firebaseFirestore.collection("Users").document(user_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){

                        if(task.getResult().exists()){

                            String firstNameTmp = task.getResult().getString("first_name");
                            String lastNameTmp = task.getResult().getString("last_name");
                            String addressTmp = task.getResult().getString("address");
                            String phoneNumberTmp = task.getResult().getString("phone_number");

                            firstName.setText(firstNameTmp);
                            lastName.setText(lastNameTmp);
                            address.setText(addressTmp);
                            phoneNumber.setText(phoneNumberTmp);

                        }else{

                            Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            });


            firebaseFirestore.collection("Users/" + user_id + "/Articles").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {



                        }
                    });

    }

    public boolean validation(){

        if(firstName.getText().toString().isEmpty() && lastName.getText().toString().isEmpty()  && phoneNumber.getText().toString().isEmpty()
                && address.getText().toString().isEmpty()){
            Toast.makeText(getActivity(), R.string.sviPodaciReg, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(firstName.getText().toString().isEmpty()){
            Toast.makeText(getActivity(), R.string.imeReg2, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(lastName.getText().toString().isEmpty()){
            Toast.makeText(getActivity(), R.string.prezimeReg2, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(address.getText().toString().isEmpty()){
            Toast.makeText(getActivity(), R.string.addressRegPersonal, Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(phoneNumber.getText().toString().isEmpty()){
            Toast.makeText(getActivity(), R.string.phoneRegPersonal, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.layoutPhotoProfile || v.getId() == R.id.layoutProfileInformation){
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
           btnEdit.callOnClick();
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result code is RESULT_OK only if the user selects an Image
        if(resultCode == getActivity().RESULT_OK) {
            if(requestCode == 1000){
                imageUri = data.getData();
                bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                profilePhoto.setImageBitmap(bitmap);
            }
        }
    }


}
