package com.location.tracking.activties;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
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
import com.location.tracking.R;
import com.location.tracking.constants.AppConstants;
import com.location.tracking.constants.AppPreferences;
import com.location.tracking.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Created by 1023 on 3/15/2018.
 */

public class ProfileActivity extends BaseActivity {

    private ImageView imageUpload;
    private EditText editName;
    private Button btnSave;
    private ProgressBar prograssBar;
    private SwitchCompat switchCompat;

    private AppPreferences appPreferences;
    private BaseActivity activity;
    private static final int RESULT_LOAD_IMAGE = 125;
    private String imageName;
    private String userId;
    private String username;
    private String base64Image;
    private String switchStatus = "0";
    private ProgressDialog pd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        activity = (BaseActivity) this;
        appPreferences = new AppPreferences();
        setDesignBindView();
        pd = new ProgressDialog(activity);
        pd.setMessage("Loading...");
        pd.show();
        getUserDetails(userId, 0);
        imageUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pd.show();
                String name = editName.getText().toString();
                String userKey = appPreferences.getString(getApplicationContext(), AppConstants.FIREBASE_USER_ID);
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userKey);
                mDatabase.child("name").setValue(name);
                mDatabase.child("profile_pic").setValue(base64Image);
                mDatabase.child("status").setValue(switchStatus);
                getUserDetails(userId, 1);
                appPreferences.putString(getApplicationContext(), AppConstants.USER_PROFILE_PIC, base64Image);
                appPreferences.putString(getApplicationContext(), AppConstants.USER_NAME, name);

            }
        });

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchStatus = "0";
                } else {
                    switchStatus = "1";
                }
            }
        });

    }

    /**
     * This method is invoke set findview by id
     */
    private void setDesignBindView() {
        imageUpload = (ImageView) findViewById(R.id.image_upload);
        editName = (EditText) findViewById(R.id.edit_name);
        switchCompat = (SwitchCompat) findViewById(R.id.switch_status);
        btnSave = (Button) findViewById(R.id.btn_save);
        prograssBar = (ProgressBar) findViewById(R.id.prograssBar);
        userId = appPreferences.getString(activity, AppConstants.FIREBASE_USER_ID);
        username = appPreferences.getString(activity, AppConstants.USER_NAME);
        base64Image = appPreferences.getString(activity, AppConstants.USER_PROFILE_PIC);
        editName.setText(username);
        setBitmapImageToImageView(base64Image);
    }

    /**
     * This method is used to get registred user details from firebase database
     *
     * @param userId
     */
    private void getUserDetails(final String userId, final int i) {
        String url = "https://friendtracking-199003.firebaseio.com/users/" + userId + ".json";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                doOnSuccess(userId, s, i);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                pd.dismiss();
                Utils.showToastMessage(activity, AppConstants.ERR_PROFILE_INFO);
                System.out.println("" + volleyError);
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(ProfileActivity.this);
        rQueue.add(request);
    }

    /**
     * This method is used to get user details from firebase server
     *
     * @param userId user id
     * @param s      user details like json
     * @param i
     */
    private void doOnSuccess(String userId, String s, int i) {
        try {
            pd.dismiss();
            JSONObject obj = new JSONObject(s);
            String status = obj.getString("status");
            String image = obj.getString("profile_pic");
            String name = obj.getString("name");
            if (i == 1) {
                Utils.showToastMessage(activity, AppConstants.MSG_PROFILE_UPDATE_SUCCESSFULLY);
            }
            editName.setText(name);
            setBitmapImageToImageView(image);
            if (status.equals("0")) {
                switchCompat.setChecked(true);
            } else {
                switchCompat.setChecked(false);
            }


        } catch (JSONException e) {
            pd.dismiss();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), " Large Image! Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * This method is used to set base64 image to image view using glide library
     *
     * @param base64Image
     */
    private void setBitmapImageToImageView(String base64Image) {
        Bitmap decodedByte = Utils.decodeBitmap(base64Image);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
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
