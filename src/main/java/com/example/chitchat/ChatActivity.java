package com.example.chitchat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chitchat.adapter.ChatRecyclerAdapter;
import com.example.chitchat.model.ChatMessageModel;
import com.example.chitchat.model.ChatroomModel;
import com.example.chitchat.model.UserModel;
import com.example.chitchat.utils.AndroidUtil;
import com.example.chitchat.utils.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;


import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    EditText messageInput;
    ImageButton sendMessageBtn;


    ImageButton backbtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ChatRecyclerAdapter adapter;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        otherUser = AndroidUtil.getUserModelFromIntent(getIntent());
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backbtn = findViewById(R.id.back_btn);
        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(), otherUser.getUserId());
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        imageView = findViewById(R.id.profile_pic_image_view);


        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Uri uri = t.getResult();
                        AndroidUtil.setProfilePic(this, uri, imageView);
                    }
                });

        backbtn.setOnClickListener((v) -> {
            onBackPressed();
        });
        otherUsername.setText(otherUser.getUsername());


        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty())
                return;
            sendMessageToUser(message);
        });






        getOrCreateChatroomModel();
        setupChatRecyclerView();

    }






    void setupChatRecyclerView(){
        Query query= FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatMessageModel> options= new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();

        adapter=new ChatRecyclerAdapter(options,getApplicationContext());
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }
    void sendMessageToUser(String message){
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
        ChatMessageModel chatMessageModel=new ChatMessageModel(message,FirebaseUtil.currentUserId(),Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        messageInput.setText("");
                        sendNotification(message);

                    }
                });
    }

    void getOrCreateChatroomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {
                    chatroomModel = new ChatroomModel(
                            chatroomId,
                            Arrays.asList(FirebaseUtil.currentUserId(), otherUser.getUserId()),
                            Timestamp.now(),
                            ""

                    );
                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }
    void  sendNotification(String message){
  FirebaseUtil.currentUserDetails().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
      @Override
      public void onComplete(@NonNull Task<DocumentSnapshot> task) {
          if(task.isSuccessful()){
              UserModel currentUser=task.getResult().toObject(UserModel.class);
              try {
                  JSONObject jsonObject=new JSONObject();
                  JSONObject notificationObj=new JSONObject();
                  notificationObj.put("title",currentUser.getUsername());
                  notificationObj.put("body",message);
                  JSONObject dataObj=new JSONObject();
                  dataObj.put("userId",currentUser.getUserId());

                  jsonObject.put("notification",notificationObj);
                  jsonObject.put("data",dataObj);
                  jsonObject.put("to",otherUser.getFcmToken());
                  callApi(jsonObject);

              }catch (Exception e){

              }
          }
      }
  });
    }


    void callApi(JSONObject jsonObject){
         final MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();
        String url="https://fcm.googleapis.com/fcm/send";
        RequestBody body=RequestBody.create(jsonObject.toString(),JSON);
        Request request=new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAA9vRAgxw:APA91bEHXF0VlXtdydbwqZtzaBbexINyHQve89Ymy9FwF0UClLQZjK3DmZdQ0FISrGAI9gNm_nxqBqAFupX-4NWUQCwHXJq5zPKS1ca3XOOqM-F50-Oz-irLvziBTKDeIQQhCAc3wQom")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });
    }
}