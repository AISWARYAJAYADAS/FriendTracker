package com.location.tracking.activties;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.location.tracking.R;
import com.location.tracking.adapter.MessageAdapter;
import com.location.tracking.constants.AppConstants;
import com.location.tracking.constants.AppPreferences;
import com.location.tracking.constants.Config;
import com.location.tracking.model.UserModel;
import com.location.tracking.model.Users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 1023 on 3/8/2018.
 */

public class MessageActivity extends BaseActivity {

    ListView usersList;
    TextView noUsersText;
    ArrayList<String> al = new ArrayList<>();
    int totalUsers = 0;
    ProgressDialog pd;
    private BaseActivity activity;
    private AppPreferences appPreferences;
    private ArrayList<UserModel> userModelArrayList = new ArrayList<>();
    private Firebase reference;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    ArrayList<String> list = new ArrayList<>();
    private List<Users> messageList = null;
    private RecyclerView recyclerMessages;
    private MessageAdapter messageAdapter;
    ArrayList<HashMap<String, String>> hashMapArrayList = new ArrayList<HashMap<String, String>>();
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        activity = (BaseActivity) this;
        Firebase.setAndroidContext(this);
        appPreferences = new AppPreferences();
        usersList = (ListView) findViewById(R.id.usersList);
        noUsersText = (TextView) findViewById(R.id.noUsersText);
        recyclerMessages = (RecyclerView) findViewById(R.id.recycler_messages);
        pd = new ProgressDialog(activity);
        pd.setMessage("Loading...");
        pd.show();

        reference = new Firebase("https://friendtracking-199003.firebaseio.com/users");
        getAllUsers(reference);


    }

    /**
     * This method is invoke get all user details from firebase database
     *
     * @param reference firebase referance
     */
    private void getAllUsers(Firebase reference) {
        int size = 0;
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userId = appPreferences.getString(activity, AppConstants.FIREBASE_USER_ID);
                try {
                    String key = dataSnapshot.getKey();
                    if (!userId.equals(key)) {
                        Map map = dataSnapshot.getValue(Map.class);
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("user_key", key);
                        hashMap.put("name", map.get("name").toString());
                        hashMap.put("device_token", map.get("device_token").toString());
                        hashMap.put("device", map.get("device").toString());
                        hashMap.put("phno", map.get("phno").toString());
                        if (map.get("profile_pic").toString() != null) {
                            hashMap.put("image", map.get("profile_pic").toString());
                        }
                        hashMapArrayList.add(hashMap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("STatus", " GETSTATUS");
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("STatus", " GETSTATUS");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("STatus", " GETSTATUS");
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("STatus", " GETSTATUS");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLoadStatus = true;
        this.mHandler = new Handler();
        m_Runnable.run();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isLoadStatus = false;
    }

    @Override
    protected void onDestroy() {
        Glide.get(this).clearMemory();
        super.onDestroy();
    }

    private boolean isLoadStatus = true;
    private final Runnable m_Runnable = new Runnable() {
        public void run() {
            if (isLoadStatus) {
                Log.d("STatus", " GETSTATUS " + hashMapArrayList);
                if (!hashMapArrayList.isEmpty()) {
                    setRecyclerView(hashMapArrayList);
                    isLoadStatus = false;
                }
                mHandler.postDelayed(m_Runnable, 2000);
            }
        }
    };

    private void setRecyclerView(ArrayList<HashMap<String, String>> hashMapArrayList) {
        if (hashMapArrayList.isEmpty()) {
            pd.dismiss();
            noUsersText.setVisibility(View.VISIBLE);
            recyclerMessages.setVisibility(View.GONE);
        } else {
            pd.dismiss();
            messageAdapter = new MessageAdapter(hashMapArrayList, activity);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            recyclerMessages.setLayoutManager(mLayoutManager);
            recyclerMessages.setItemAnimator(new DefaultItemAnimator());
            recyclerMessages.setAdapter(messageAdapter);
        }
    }

}