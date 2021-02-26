package com.location.tracking.activties;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.location.tracking.R;
import com.location.tracking.constants.AppConstants;
import com.location.tracking.constants.AppPreferences;
import com.location.tracking.service.MyLocationService;

public class MainActivity extends BaseActivity {

    private AppPreferences appPreferences;
    private BaseActivity activity;
    private static final String TAG = "MainActivity";
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        activity = (BaseActivity) this;
        appPreferences = new AppPreferences();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }


        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        String token = FirebaseInstanceId.getInstance().getToken();
        userId = appPreferences.getString(activity, AppConstants.FIREBASE_USER_ID);
        appPreferences.putString(activity, AppConstants.DEVICE_TOKEN, token);
        if (userId != null){
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId);
            mDatabase.child("device_token").setValue(token);
        }
        //startService(new Intent(this, MyLocationService.class));
        Thread back = new Thread() {
            public void run() {
                try {
                    sleep(1 * 2000);
                    if (userId.isEmpty()) {
                        Intent goRegister = new Intent(getApplicationContext(), RegisterActivity.class);
                        startActivity(goRegister);
                        finish();
                    } else {
                        Intent goHome = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(goHome);
                        finish();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        back.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startService(new Intent(this, MyLocationService.class));
    }
}