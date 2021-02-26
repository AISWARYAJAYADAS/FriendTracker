package com.location.tracking.activties;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.client.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.location.tracking.R;
import com.location.tracking.constants.AppConstants;
import com.location.tracking.constants.AppPreferences;
import com.location.tracking.constants.Config;
import com.location.tracking.direction.GPSTracker;
import com.location.tracking.model.Users;
import com.location.tracking.util.Utils;
import com.vistrav.ask.Ask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by 1023 on 3/3/2018.
 */

public class RegisterActivity extends BaseActivity {

    private BaseActivity activity;
    private EditText editName;
    private EditText editMobileNumber;
    private Button btnRegister;
    private ImageView imageUpload;
    private ProgressBar prograssBar;

    private GPSTracker gps;
    private double latitude;
    private double longtitude;
    private static final String TAG = MainActivity.class.getSimpleName();
    private AppPreferences appPreferences;
    private String firebaseRegId;
    private String mMobileNUmber;
    private String mName;
    private String mRegCode;
    private String userId;
    private static final int RESULT_LOAD_IMAGE = 125;
    private String imageName = "";
    private ProgressDialog pd = null;
    private String attachedFile;
    private byte[] imageBytes;
    private String base64Image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        activity = (BaseActivity) this;
        Firebase.setAndroidContext(this);
        pd = new ProgressDialog(activity);
        appPreferences = new AppPreferences();
        editName = (EditText) findViewById(R.id.edit_name);
        editMobileNumber = (EditText) findViewById(R.id.edit_mobile_number);
        btnRegister = (Button) findViewById(R.id.btn_register);
        imageUpload = (ImageView) findViewById(R.id.image_upload);
        prograssBar = (ProgressBar) findViewById(R.id.prograssBar);
        checkMarshmellowPermission();
        getFirebaseRegId();
        getCurrentLatLong();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isValidate()) {
                    Utils.showToastMessage(activity, "Something went wrong");
                    return;
                }
                pd.show();
                pd.setMessage("Loading... Please wait...");
                doFireBaseRegistration();
            }
        });

        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
    }

    /**
     * Check permission higher than Marshmellow
     */
    private void checkMarshmellowPermission() {
        Ask.on(this)
                .forPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CALL_PHONE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .withRationales(AppConstants.MESSAGE_PERMISSION) //optional
                .go();
    }

    /**
     * Register user details to firebase
     */
    private void doFireBaseRegistration() {

//        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        String url = "https://friendtracking-199003.firebaseio.com/users.json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equals("null")) {
                    pd.dismiss();
                    // reference.child(mName).child("name").setValue(mName);
                    String userId = mDatabase.push().getKey();
                    Users usersData = new Users();
                    usersData.setName(mName);
                    usersData.setProfile_pic(base64Image);
                    usersData.setPhno(mMobileNUmber);
                    usersData.setLatitude(String.valueOf(latitude));
                    usersData.setLongitude(String.valueOf(longtitude));
                    usersData.setDevice_token(String.valueOf(firebaseRegId));
                    usersData.setDevice("Android");
                    usersData.setStatus("0");
                    usersData.setUserKey(userId);
                    if (base64Image.equals("")) {
                        pd.dismiss();
                        Utils.showToastMessage(activity, "Please choose any profile image");
                    } else {
                        pd.dismiss();
                        mDatabase.child(userId).setValue(usersData);
                        appPreferences.putString(getApplicationContext(), AppConstants.FIREBASE_USER_ID, userId);
                        appPreferences.putString(getApplicationContext(), AppConstants.USER_NAME, mName);
                        appPreferences.putString(getApplicationContext(), AppConstants.USER_PROFILE_PIC, base64Image);
                        Utils.showToastMessage(activity, "registration successful");
                        startActivity(new Intent(activity, HomeActivity.class));
                        finish();
                    }
                } else {
                    try {
                        pd.dismiss();
                        JSONObject obj = new JSONObject(s);
                        if (!obj.has(mMobileNUmber)) {
                            pd.dismiss();
                            String userId = mDatabase.push().getKey();
                            Users usersData = new Users();
                            usersData.setName(mName);
                            usersData.setProfile_pic(base64Image);
                            usersData.setPhno(mMobileNUmber);
                            usersData.setLatitude(String.valueOf(latitude));
                            usersData.setLongitude(String.valueOf(longtitude));
                            usersData.setDevice_token(firebaseRegId);
                            usersData.setDevice("Android");
                            usersData.setStatus("0");
                            usersData.setUserKey(userId);
                            if (base64Image.equals("")) {
                                Utils.showToastMessage(activity, "Please choose any profile image");
                            } else {
                                mDatabase.child(userId).setValue(usersData);
                                appPreferences.putString(getApplicationContext(), AppConstants.USER_NAME, mName);
                                appPreferences.putString(getApplicationContext(), AppConstants.FIREBASE_USER_ID, userId);
                                appPreferences.putString(getApplicationContext(), AppConstants.USER_PROFILE_PIC, base64Image);
                                Utils.showToastMessage(activity, "registration successful");
                                startActivity(new Intent(activity, HomeActivity.class));
                                finish();
                            }
                        } else {
                            pd.dismiss();
                            Utils.showToastMessage(activity, "mobile number already exists");
                        }
                    } catch (JSONException e) {
                        pd.dismiss();
                        e.printStackTrace();
                    }
                }
                pd.dismiss();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
                pd.dismiss();
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(activity);
        rQueue.add(request);
    }

    private boolean isValidate() {
        boolean isFlag = false;
        mMobileNUmber = editMobileNumber.getText().toString();
        mName = editName.getText().toString();
        if (mMobileNUmber.isEmpty()) {
            editMobileNumber.setError("Invalid");
            editMobileNumber.requestFocus();
            isFlag = false;
        } else if (mName.isEmpty()) {
            editName.setError("Invalid");
            editName.requestFocus();
            isFlag = false;
        } else {
            isFlag = true;
        }
        return isFlag;
    }

    private void getFirebaseRegId() {
        firebaseRegId = appPreferences.getString(getApplicationContext(), "regId");
        Log.d("FireBaseKey ", "FireBaseKey " + firebaseRegId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getCurrentLatLong();
    }

    private void getCurrentLatLong() {
        gps = new GPSTracker(getApplicationContext());
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longtitude = gps.getLongitude();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            pd.show();
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String picturePath = cursor.getString(columnIndex);
            cursor.close();

            String filename = picturePath.substring(picturePath.lastIndexOf("/") + 1);
            String extension = filename.substring(filename.lastIndexOf(".") + 1);
            File newFile = new File(picturePath);
            Uri filePath = data.getData();

            if (!(" " + newFile).isEmpty()) {
                if (picturePath != null) {
                    try {
                        File uploadFile = new File(picturePath);
                        String compressedImage = compressImage(uploadFile.getAbsolutePath());
                        Bitmap bitmap = BitmapFactory.decodeFile(compressedImage);
                        base64Image = Utils.BitMapToString(bitmap);
                        setBitmapImageToImageView(base64Image);
                        //imageUpload.setImageBitmap(decodedByte);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), " Large Image! Please try again ", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setBitmapImageToImageView(String base64Image) {
        Bitmap decodedByte = Utils.decodeBitmap(base64Image);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
        pd.dismiss();
        Glide.with(activity).load(stream.toByteArray()).
                asBitmap().placeholder(R.drawable.avatar).centerCrop().into(new BitmapImageViewTarget(imageUpload) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                imageUpload.setImageDrawable(circularBitmapDrawable);
            }
        });
    }

    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }


}