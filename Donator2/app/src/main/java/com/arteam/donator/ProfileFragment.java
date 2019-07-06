package com.arteam.donator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment implements View.OnClickListener, View.OnKeyListener {

    View view;
    private ConstraintLayout layoutPhoto;
    private ConstraintLayout layoutProfileInfo;
    private EditText firstName;
    private EditText lastName;
    private EditText address;
    private TextView email;
    private TextView phoneNumber;
    private FirebaseAuth mAuth;
    private Button btnCancel;
    private Button btnEdit;
    private FirebaseFirestore firebaseFirestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore= FirebaseFirestore.getInstance();
        final NavigationView navigationView = getActivity().findViewById(R.id.nav_view);


        layoutPhoto = view.findViewById(R.id.layoutPhotoProfile);
        layoutProfileInfo = view.findViewById(R.id.layoutProfileInformation);
        firstName = view.findViewById(R.id.firstNameProfileTxt);
        lastName = view.findViewById(R.id.lastNameProfileTxt);
        address = view.findViewById(R.id.addressProfileTxt);
        email = view.findViewById(R.id.emailProfileTxt);
        phoneNumber = view.findViewById(R.id.phoneNumberProfileTxt);
        btnCancel = view.findViewById(R.id.btnCancelEditProfile);
        btnEdit = view.findViewById(R.id.btnEditProfile);

        email.setText(mAuth.getCurrentUser().getEmail());
        layoutPhoto.setOnClickListener(this);
        layoutProfileInfo.setOnClickListener(this);
        phoneNumber.setOnKeyListener(this);

        this.fillData();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), NavigationMainActivity.class));
                getActivity().finish();
            }
        });


        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userID = mAuth.getCurrentUser().getUid();

                firebaseFirestore.collection("Users").document(userID)
                        .update("first_name", firstName.getText().toString(), "last_name",
                                lastName.getText().toString(), "address", address.getText().toString(), "phone_number", phoneNumber.getText().toString());

                startActivity(new Intent(getActivity(), NavigationMainActivity.class));
                getActivity().finish();
            }
        });

        return view;
    }

    private void fillData(){
        String userID = mAuth.getUid();


            firebaseFirestore.collection("Users").document(userID).get()
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

}
