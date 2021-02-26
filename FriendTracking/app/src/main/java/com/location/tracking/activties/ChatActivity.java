package com.location.tracking.activties;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.location.tracking.R;
import com.location.tracking.constants.AppConstants;
import com.location.tracking.constants.AppPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 1023 on 3/12/2018.
 */

public class ChatActivity extends BaseActivity {

    private AppPreferences appPreferences;
    private BaseActivity activity;
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView sendButton;
    EditText messageArea;
    ScrollView scrollView;
    Firebase reference1, reference2;
    private String senderId;
    private String recieverId;
    private String senderName;
    private String recieverName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        activity = (BaseActivity) this;
        appPreferences = new AppPreferences();

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        Firebase.setAndroidContext(this);
        senderId= appPreferences.getString(activity, AppConstants.FIREBASE_USER_ID);
        recieverId = appPreferences.getString(activity, AppConstants.RECIEVER_USER_ID);
        senderName = appPreferences.getString(activity, AppConstants.USER_NAME);
        recieverName = appPreferences.getString(activity, AppConstants.RECIEVER_NAME);
        getSupportActionBar().setTitle(recieverName);
        reference1 = new Firebase("https://friendtracking-199003.firebaseio.com/" + senderId + "_" + recieverId);
        reference2 = new Firebase("https://friendtracking-199003.firebaseio.com/" + recieverId + "_" + senderId);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("senderId", senderId);
                    map.put("recieverId", recieverId);
                    map.put("senderName", senderName);
                    map.put("recieverName", recieverName);
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    messageArea.setText("");
                }
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String sender = map.get("senderId").toString();
                String reciever = map.get("recieverId").toString();

                if(sender.equals(senderId)){
                    addMessageBox("You:-\n" + message, 1);
                }
                else{
                    addMessageBox(recieverName + ":-\n" + message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(activity);
        textView.setText(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.RIGHT;
            textView.setBackgroundResource(R.drawable.bubble_in);
        }
        else{
            lp2.gravity = Gravity.LEFT;
            textView.setBackgroundResource(R.drawable.bubble_out);
        }
        textView.setLayoutParams(lp2);
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}