package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.example.chitchat.model.UserModel;
import com.example.chitchat.utils.AndroidUtil;
import com.example.chitchat.utils.FirebaseUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class SplashActivity extends AppCompatActivity {
    //Handler handler;
    Animation topAnim;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        topAnim= AnimationUtils.loadAnimation(this,R.anim.top_animationn);
//        bottomAnim= AnimationUtils.loadAnimation(this,R.anim.bottom_animation);
        image=findViewById(R.id.imageView);
        image.setAnimation(topAnim);

           if(FirebaseUtil.isLoggedIn() && getIntent().getExtras()!=null){
               String userId=getIntent().getExtras().getString("userId");
               FirebaseUtil.allUserCollectionReference().document(userId).get()
                       .addOnCompleteListener(task -> {
                           if(task.isSuccessful()){
                               UserModel model=task.getResult().toObject(UserModel.class);

                               Intent mainIntent=new Intent(this,MainActivity.class);
                               mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                               startActivity(mainIntent);

                               Intent intent=new Intent(this,ChatActivity.class);
                               AndroidUtil.passUserModelAsIntent(intent,model);
                               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                               startActivity(intent);
                               finish();
                           }
                       });
           }
           else {

               new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       if (FirebaseUtil.isLoggedIn()) {
                           startActivity(new Intent(SplashActivity.this, MainActivity.class));
                       } else {
                           startActivity(new Intent(SplashActivity.this, SignupActivity.class));
                       }
                   }
               }, 2500);

           }
    }

}


