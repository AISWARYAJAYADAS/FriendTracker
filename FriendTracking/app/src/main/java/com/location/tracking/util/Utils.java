package com.location.tracking.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;


/**
 * Created by 1023 on 3/3/2018
 */

public class Utils {
	/**
	 * This Method is used to show toast message
	 * @param context Activity context
	 * @param message Toast Message
	 */
	public static void showToastMessage (Context context, String message ) {
		Toast.makeText ( context,  message, Toast.LENGTH_SHORT).show ();
	}

	/**
	 * This method is used to show and hide prograss
	 * @param prograssBar Prograss Bar
	 * @param isTrue status is true or false
	 */
	public static void showPrograssBar(ProgressBar prograssBar, boolean isTrue) {
		if (isTrue){
			prograssBar.setVisibility(View.VISIBLE);
		}else {
			prograssBar.setVisibility(View.GONE);
		}
	}


	public static Bitmap decodeBitmap(String base64Image) {
		byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
		return decodedByte;
	}


	/**
	 * This method is used to string to bitmap
	 * @param encodedString
	 * @return bitmap (from given string)
	 */
	public Bitmap StringToBitMap(String encodedString){
		try {
			byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
			Bitmap bitmap=BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
			return bitmap;
		} catch(Exception e) {
			e.getMessage();
			return null;
		}
	}

	/**
	 * This methos is invoke convert bit map to string
	 * @param bitmap image
	 * @return
	 */
	public static String BitMapToString(Bitmap bitmap)
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
		return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
	}


}
