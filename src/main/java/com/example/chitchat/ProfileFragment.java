package com.example.chitchat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.chitchat.model.UserModel;
import com.example.chitchat.utils.AndroidUtil;
import com.example.chitchat.utils.FirebaseUtil;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class ProfileFragment extends Fragment {

    ImageView profilePic;
    EditText phoneInput;
    EditText usernameInput;
    Button updateProfileBtn;
    ProgressBar progressBar;
    TextView logoutBtn;
    UserModel currentUserModel;
    ActivityResultLauncher<Intent> imagePickLauncher;
    Uri selectImageUri;

    public ProfileFragment() {

    }

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        imagePickLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result->{
                    if(result.getResultCode()== Activity.RESULT_OK){
                        Intent data=result.getData();
                        if(data!=null && data.getData()!=null){
                            selectImageUri=data.getData();
                            AndroidUtil.setProfilePic(getContext(),selectImageUri,profilePic);
                        }
                    }
                });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_profile, container, false);
        profilePic= view.findViewById(R.id.profile_image_view);
        usernameInput=view.findViewById(R.id.profile_username);
        phoneInput=view.findViewById(R.id.profile_phone);
        updateProfileBtn=view.findViewById(R.id.profile_update_btn);
        progressBar=view.findViewById(R.id.profile_progress_bar);
        logoutBtn=view.findViewById(R.id.logout_btn);

        getUserData();

        updateProfileBtn.setOnClickListener((v -> {
            updateBtnClick();
        }));


        logoutBtn.setOnClickListener((v)->{
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        FirebaseUtil.logout();
                        Intent intent=new Intent(getContext(),SplashActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);


                    }
                }
            });


        });

        profilePic.setOnClickListener((v)->{
            ImagePicker.with(this).cropSquare().compress(512).maxResultSize(512,512)
                    .createIntent(new Function1<Intent, Unit>() {
                        @Override
                        public Unit invoke(Intent intent) {
                            imagePickLauncher.launch(intent);
                            return null;
                        }
                    });
        });

        return view;
    }

    void updateBtnClick(){
        String newUsername=usernameInput.getText().toString();
        if(newUsername.isEmpty()|| newUsername.length()<3){
            usernameInput.setError("username should be at least 3 chars");
            return;
        }
        currentUserModel.setUsername(newUsername);
        setInProgress(true);

        if(selectImageUri!=null){
            FirebaseUtil.getCurrentProfilePicStorageRef().putFile(selectImageUri)
                    .addOnCompleteListener(task -> {
                        updateToFirestore();
                    });
        }else{
            updateToFirestore();
        }

        updateToFirestore();
    }

    void updateToFirestore(){
        FirebaseUtil.currentUserDetails().set(currentUserModel)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()){
                        AndroidUtil.showToast(getContext(),"Update Successfully");
                    }
                    else {
                        AndroidUtil.showToast(getContext(),"Update Failed");
                    }
                });
    }

    void  getUserData(){
        setInProgress(true);
        FirebaseUtil.getCurrentProfilePicStorageRef().getDownloadUrl()
                .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
                        Uri uri=task.getResult();
                        AndroidUtil.setProfilePic(getContext(),uri,profilePic);
                    }
                });
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
            setInProgress(false);
            currentUserModel=task.getResult().toObject(UserModel.class);
            usernameInput.setText(currentUserModel.getUsername());
            phoneInput.setText(currentUserModel.getPhone());
        });
    }
    void setInProgress(boolean inProgress) {
        if (inProgress) {
            progressBar.setVisibility(View.VISIBLE);
            updateProfileBtn.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            updateProfileBtn.setVisibility(View.VISIBLE);
        }
    }
}