package com.location.tracking.activties;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.location.tracking.R;
import com.location.tracking.constants.AppConstants;
import com.location.tracking.constants.AppPreferences;
import com.location.tracking.direction.DataParser;
import com.location.tracking.helper.SlidingPanel;
import com.location.tracking.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends FragmentActivity implements OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Marker mAllUserMarker;
    private AppPreferences appPreferences;
    Animation animShow, animHide;
    private Boolean ispopedup = false;
    ArrayList<HashMap<String, String>> hashMapArrayList = new ArrayList<HashMap<String, String>>();
    private Firebase reference;
    private Handler mHandler;
    ArrayList<LatLng> MarkerPoints;


    private Toolbar toolbar;
    private ProgressBar prograssBar;
    private ImageView imageMessage;
    private SlidingPanel popup;
    private TextView textProfile;
    private TextView textMessage;
    private double currentLatitude;
    private double currentLongtitude;
    private View mCustomMarkerView;
    private CircleImageView mMarkerImageView;
    private MarkerOptions markerOptions;


    // For update location
    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    private boolean isLocaFlag = true;

    @SuppressLint("RestrictedApi")
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate ...............................");
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        appPreferences = new AppPreferences();
        Firebase.setAndroidContext(getApplicationContext());
        reference = new Firebase("https://friendtracking-199003.firebaseio.com/users");
        getAllUsers(reference);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        prograssBar = (ProgressBar) findViewById(R.id.prograssBar);
        imageMessage = (ImageView) findViewById(R.id.image_message);
        popup = (SlidingPanel) findViewById(R.id.popup_window_dropdown);
        textProfile = (TextView) findViewById(R.id.text_profile);
        textMessage = (TextView) findViewById(R.id.text_message);
        MarkerPoints = new ArrayList<>();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        mCustomMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.view_custom_marker, null);
        mMarkerImageView = (CircleImageView) mCustomMarkerView.findViewById(R.id.profile_image);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        imageMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!ispopedup) {
                    showSubCategoryPopup();
                    ispopedup = true;
                } else {
                    popup.startAnimation(animHide);
                    popup.setVisibility(View.GONE);
                    ispopedup = false;
                }
            }
        });
        textProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.startAnimation(animHide);
                popup.setVisibility(View.GONE);
                ispopedup = false;
                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(i);
            }
        });
        textMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popup.startAnimation(animHide);
                popup.setVisibility(View.GONE);
                ispopedup = false;
                Intent i = new Intent(getApplicationContext(), MessageActivity.class);
                startActivity(i);
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //buildGoogleApiClient();
                mGoogleApiClient.connect();
                // mMap.setMyLocationEnabled(true);
            }
        } else {
            //buildGoogleApiClient();
            mGoogleApiClient.connect();
            // mMap.setMyLocationEnabled(true);

        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Utils.showPrograssBar(prograssBar, true);
                if (!marker.getTitle().equals(appPreferences.getString(getApplicationContext(), AppConstants.USER_NAME))) {
                    getUserDetails(marker.getSnippet());
                }
                return false;
            }
        });
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mCurrentLocation = location;
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                //Place current location marker
               updateLocation(mCurrentLocation);            }
        });
    }

    private void updateLocation(Location mCurrentLocation) {
        currentLatitude = mCurrentLocation.getLatitude();
        currentLongtitude = mCurrentLocation.getLongitude();
        LatLng currentPosition = new LatLng(currentLatitude, currentLongtitude);
        getAllUsers(reference);
        if (mCurrLocationMarker != null) {
            String userKey = appPreferences.getString(getApplicationContext(), AppConstants.FIREBASE_USER_ID);
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userKey);
            mDatabase.child("latitude").setValue(mCurrentLocation.getLatitude() + "");
            mDatabase.child("longitude").setValue(mCurrentLocation.getLongitude() + "");
            mCurrLocationMarker.remove();
            animateMarker(mCurrLocationMarker, currentPosition, true);
        }

        if (isLocaFlag != false){
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(currentPosition).zoom(18).build();
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }
        isLocaFlag = false;

        Bitmap bitmapImage = Utils.decodeBitmap(appPreferences.getString(getApplicationContext(), AppConstants.USER_PROFILE_PIC));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
        mCurrLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .title(appPreferences.getString(getApplicationContext(), AppConstants.USER_NAME))
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, bitmapImage))));
        // Log.e("lat", "" + point);
        //updateUI();

    }

    /**
     * This method is used to get user details from firebase database
     *
     * @param userId firebase key id
     */
    private void getUserDetails(final String userId) {
        String url = "https://friendtracking-199003.firebaseio.com/users/" + userId + ".json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                doOnSuccess(userId, s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(HomeActivity.this);
        rQueue.add(request);
    }

    private void doOnSuccess(String userId, String s) {
        try {
            JSONObject obj = new JSONObject(s);
            String name = obj.getString("name");
            String device_token = obj.getString("device_token");
            String device = obj.getString("device");
            String latitude = obj.getString("latitude");
            String longitude = obj.getString("longitude");
            String phno = obj.getString("phno");
            String profile_pic = obj.getString("profile_pic");
            markerClickShowDialog(userId, name, device_token, device, latitude, longitude, phno, profile_pic);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is invoke show dialog box when click marker view
     *
     * @param userId       firebase id for get all user details
     * @param name         user name
     * @param device_token token
     * @param device       device type
     * @param latitude     current lat on our friend
     * @param longitude    current long on our friend
     * @param phno         contact number of our friend
     * @param profile_pic  profile picture of our friend
     */
    private void markerClickShowDialog(final String userId, final String name, String device_token, String device, final String latitude,
                                       final String longitude, final String phno, String profile_pic) {
        Utils.showPrograssBar(prograssBar, false);
        // Create custom dialog object
        final Dialog dialog = new Dialog(HomeActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.custom_dialog_view_details);
        // Set dialog title
        dialog.setTitle("Custom Dialog");

        // set values for custom dialog components - text, image and button
        TextView textName = (TextView) dialog.findViewById(R.id.text_name);
        final LinearLayout layoutCall = (LinearLayout) dialog.findViewById(R.id.layout_call);
        final LinearLayout layoutMessage = (LinearLayout) dialog.findViewById(R.id.layout_message);
        final LinearLayout layoutDirection = (LinearLayout) dialog.findViewById(R.id.layout_direction);
        textName.setText(name);
        final ImageView imageProfile = (ImageView) dialog.findViewById(R.id.image_profile);
        setBitmapImageToImageView(profile_pic, imageProfile);
        dialog.show();
        final String fireBaseId = appPreferences.getString(getApplicationContext(), AppConstants.FIREBASE_USER_ID);
        // if call Layout is clicked, close the custom dialog
        layoutCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                if (!fireBaseId.equals(userId)) {
                    callToNearestFriend(phno);
                } else {
                    Utils.showToastMessage(getApplicationContext(), "You cannot contact to this number");
                }
                dialog.dismiss();
            }
        });
        // if message Layout is clicked, close the custom dialog
        layoutMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                if (!fireBaseId.equals(userId)) {
                    appPreferences.putString(getApplicationContext(), AppConstants.RECIEVER_USER_ID,
                            userId);
                    appPreferences.putString(getApplicationContext(), AppConstants.RECIEVER_NAME,
                            name);
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(intent);
                    dialog.dismiss();
                } else {
                    Utils.showToastMessage(getApplicationContext(), "Not Available this operation");
                    dialog.dismiss();
                }
                dialog.dismiss();
            }
        });
        // if direction Layout is clicked, close the custom dialog
        layoutDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                if (!fireBaseId.equals(userId)) {
                    directionToFriend(latitude, longitude);
                } else {
                    Utils.showToastMessage(getApplicationContext(), "Only for find your friend");
                }
                dialog.dismiss();
            }
        });
    }

    /**
     * This method is used to set base64 image to image view using glide library
     *
     * @param base64Image
     */
    private void setBitmapImageToImageView(String base64Image, final ImageView imageProfile) {
        if (base64Image.equals("")) {
            Glide.with(getApplicationContext()).load(R.drawable.ic_placeholder)
                    .thumbnail(0.5f)
                    .crossFade()
                    .placeholder(R.drawable.ic_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageProfile);
        } else {
            Bitmap decodedByte = Utils.decodeBitmap(base64Image);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Glide.with(getApplicationContext()).load(stream.toByteArray())
                    .thumbnail(0.5f)
                    .crossFade()
                    .placeholder(R.drawable.ic_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageProfile);
        }
    }

    private void callToNearestFriend(String phno) {
        Uri call = Uri.parse("tel:" + phno);
        Intent callTo = new Intent(Intent.ACTION_DIAL, call);
        startActivity(callTo);
    }

    private void directionToFriend(String latitude, String logitude) {

        // Add a marker in Sydney and move the camera
        LatLng currentLatLong = new LatLng(currentLatitude, currentLongtitude);
        LatLng destination = new LatLng(Double.valueOf(latitude),
                Double.valueOf(logitude));
        if (MarkerPoints.size() > 1) {
            MarkerPoints.clear();
            mMap.clear();
        }
        MarkerPoints.add(currentLatLong);
        MarkerPoints.add(destination);


        Utils.showPrograssBar(prograssBar, true);
        // Getting URL to the Google Directions API
        //LatLng origin = new LatLng(currentLatitude, currentLogitude);
        String url = getUrl(currentLatLong, destination);
        FetchUrl FetchUrl = new FetchUrl();
        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);

    }

    private void getAllUsers(Firebase reference) {
        if (!hashMapArrayList.isEmpty())
            hashMapArrayList.clear();
        int size = 0;
        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userId = appPreferences.getString(getApplicationContext(), AppConstants.FIREBASE_USER_ID);
                try {
                    String key = dataSnapshot.getKey();
                    if (!userId.equals(key)) {
                        Map map = dataSnapshot.getValue(Map.class);
                        String number = map.get("phno").toString();
                        String status = map.get("status").toString();
                        if (status.equals("0")) {
                            HashMap<String, String> hashMap = new HashMap<String, String>();
                            hashMap.put("user_key", key);
                            hashMap.put("name", map.get("name").toString());
                            hashMap.put("device_token", map.get("device_token").toString());
                            hashMap.put("device", map.get("device").toString());
                            hashMap.put("latitude", map.get("latitude").toString());
                            hashMap.put("longitude", map.get("longitude").toString());
                            hashMap.put("phno", map.get("phno").toString());
                            hashMap.put("image", map.get("profile_pic").toString());
                            hashMap.put("status", map.get("status").toString());
                            hashMapArrayList.add(hashMap);
                        }
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


    private void showSubCategoryPopup() {
        popup.setVisibility(View.GONE);
        animShow = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.show);
        animHide = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.hide);
        popup.setVisibility(View.VISIBLE);
        popup.startAnimation(animShow);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }


    /**
     * @param view   is custom marker layout which we will convert into bitmap.
     * @param bitmap is the image which you want to show in marker.
     * @return
     */
    private Bitmap getMarkerBitmapFromView(View view, Bitmap bitmap) {

        mMarkerImageView.setImageBitmap(bitmap);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    private void setBitmapImageToRoundImageView(String profilePic, final ImageView imageView) {
        if (profilePic != null) {
            Bitmap decodedByte = Utils.decodeBitmap(profilePic);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Glide.with(getApplicationContext()).load(stream.toByteArray()).
                    asBitmap().placeholder(R.drawable.avatar).centerCrop().into(new BitmapImageViewTarget(imageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    imageView.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
    }

    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection projection = mMap.getProjection();
        Point startPoint = projection.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = projection.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
        isLoadStatus = true;
        isCheckArray = true;
        this.mHandler = new Handler();
        m_Runnable.run();
    }

    private boolean isLoadStatus = true;
    private boolean isCheckArray = true;
    private final Runnable m_Runnable = new Runnable() {
        public void run() {
            if (isLoadStatus) {
                if (isCheckArray) {
                    if (!hashMapArrayList.isEmpty()) {
                        setMarker(hashMapArrayList);
                        isCheckArray = false;
                    }
                }
                mHandler.postDelayed(m_Runnable, 2000);
            }
        }
    };

    /**
     * This method is used to set marker to corresponding user
     *
     * @param hashMapArrayList user list
     */
    private void setMarker(ArrayList<HashMap<String, String>> hashMapArrayList) {
        String userIdKey = appPreferences.getString(getApplicationContext(), AppConstants.FIREBASE_USER_ID);
        for (int i = 0; i < hashMapArrayList.size(); i++) {
            if (mMap != null) {
                String latitude = hashMapArrayList.get(i).get("latitude");
                String logitude = hashMapArrayList.get(i).get("longitude");
                String name = hashMapArrayList.get(i).get("name");
                String profilePic = hashMapArrayList.get(i).get("image");
                String phone = hashMapArrayList.get(i).get("phno");
                String userKey = hashMapArrayList.get(i).get("user_key");
                String status = hashMapArrayList.get(i).get("status");
                createMarker(latitude, logitude, name, profilePic, phone, userKey);
            }

        }
    }

    /**
     * This method is used to create marker in google map
     *
     * @param latitude   user lat
     * @param longitude  user long
     * @param name       of the user
     * @param profilePic
     * @param phno
     * @param userId
     * @return
     */
    protected Marker createMarker(String latitude, String longitude, String name, String profilePic, String phno, String userId) {
        if (mAllUserMarker != null){

        }
        LatLng newLatLong = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(newLatLong);
        markerOptions.snippet(userId);
        markerOptions.title(name);
        if (profilePic.equals("")) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, R.drawable.avatar)));
        } else {
            Bitmap bitmapImage = Utils.decodeBitmap(profilePic);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, stream);

            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(mCustomMarkerView, bitmapImage)));
        }


        return mMap.addMarker(markerOptions);
    }

    private Bitmap getMarkerBitmapFromView(View view, @DrawableRes int resId) {

        mMarkerImageView.setImageResource(resId);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = view.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            // buildGoogleApiClient();
                            mGoogleApiClient.connect();
                        }
                        // mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }


    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        Toast.makeText ( this,  "Location changed", Toast.LENGTH_SHORT).show ();
        mCurrentLocation = location;
        updateLocation(location);
    }


    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }


    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                Utils.showPrograssBar(prograssBar, false);
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                Utils.showPrograssBar(prograssBar, false);
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

}