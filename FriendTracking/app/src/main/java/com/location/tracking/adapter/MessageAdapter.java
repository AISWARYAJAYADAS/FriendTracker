package com.location.tracking.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.location.tracking.R;
import com.location.tracking.activties.ChatActivity;
import com.location.tracking.constants.AppConstants;
import com.location.tracking.constants.AppPreferences;
import com.location.tracking.util.Utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    private Context mContext;
    private ArrayList<HashMap<String, String>> messagList;
    private AppPreferences appPreferences;


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textName;
        public ImageView imageLogo;
        public ImageView imageviewChat;
        public TextView textUsername;
        public TextView textviewView;
        public TextView date;
        public ImageView online;
        public RelativeLayout ralativeLayout;

        public MyViewHolder(View view) {
            super(view);
            imageviewChat = (ImageView) view.findViewById(R.id.imageviewChat);
            textUsername = (TextView) view.findViewById(R.id.text_username);
            textviewView = (TextView) view.findViewById(R.id.textviewView);
            online = (ImageView) view.findViewById(R.id.online);
            date = (TextView) view.findViewById(R.id.date);
            ralativeLayout = (RelativeLayout) view.findViewById(R.id.ralativeLayout);
        }
    }


    public MessageAdapter(ArrayList<HashMap<String, String>> messagList, Context context) {
        this.messagList = messagList;
        this.mContext = context;
        appPreferences = new AppPreferences();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_messages, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        // final Users messageList = messagList.get(position);

        holder.textUsername.setText(messagList.get(position).get("name"));
        setBitmapImageToImageView(messagList.get(position).get("image"), holder.imageviewChat);
        holder.ralativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appPreferences.putString(mContext, AppConstants.RECIEVER_USER_ID,
                        messagList.get(position).get("user_key"));
                appPreferences.putString(mContext, AppConstants.RECIEVER_NAME,
                        messagList.get(position).get("name"));
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                mContext.startActivity(intent);
            }
        });
    }

    /**
     * This method is used to set base64 image to image view using glide library
     * @param base64Image
     */
    private void setBitmapImageToImageView(String base64Image, final ImageView imageView) {
        if (base64Image.equals("")){
            Glide.with(mContext).load(R.drawable.avatar).
                    asBitmap().placeholder(R.drawable.avatar).centerCrop().into(new BitmapImageViewTarget(imageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    imageView.setImageDrawable(circularBitmapDrawable);
                }
            });
        } else {
            Bitmap decodedByte = Utils.decodeBitmap(base64Image);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            decodedByte.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Glide.with(mContext).load(stream.toByteArray()).
                    asBitmap().placeholder(R.drawable.avatar).centerCrop().into(new BitmapImageViewTarget(imageView) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable =
                            RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                    circularBitmapDrawable.setCircular(true);
                    imageView.setImageDrawable(circularBitmapDrawable);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messagList.size();
    }
}