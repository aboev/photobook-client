package com.freecoders.photobook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.DiskLruBitmapCache;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Andrei Alikov andrei.alikov@gmail.com
 */
public class UserProfileFragment extends DialogFragment {
    private String userId;
    private TextView userNameView;
    private TextView userPhoneView;
    private TextView userEmailView;
    private ImageView userAvatarView;
    private ImageLoader imageLoader;

    public void setUserId(String id) {
        userId = id;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.user_profile, null);
        builder.setView(view)
                .setPositiveButton(R.string.alert_ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        userNameView = (TextView) view.findViewById(R.id.txtUserName);
        userPhoneView = (TextView) view.findViewById(R.id.txtUserPhone);
        userEmailView = (TextView) view.findViewById(R.id.txtUserEmail);
        userAvatarView = (ImageView) view.findViewById(R.id.imageViewAvatar);

        createImageLoader();

        return builder.create();
    }

    private void createImageLoader() {
        DiskLruBitmapCache diskCache = null;
        try {
            diskCache = new DiskLruBitmapCache(getActivity(), "DiskCache", 2000000, Bitmap.CompressFormat.JPEG, 100);
        } catch (IOException e) {

        }
        imageLoader = new ImageLoader(VolleySingleton.getInstance(getActivity()).getRequestQueue(), diskCache);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ServerInterface.getUserProfileRequest(null, new String[] {userId},
                new Response.Listener<HashMap<String, UserProfile>>() {
                    @Override
                    public void onResponse(HashMap<String, UserProfile> stringUserProfileHashMap) {
                        UserProfile profile = stringUserProfileHashMap.get(userId);
                        if (profile != null) {
                            fillUserProfileInfo(profile);
                        }
                    }
                }, null);
    }

    private void fillUserProfileInfo(UserProfile profile) {
        userNameView.setText(profile.name);
        userPhoneView.setText(profile.phone);
        userEmailView.setText(profile.email);

        imageLoader.get(profile.avatar, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                if (imageContainer.getBitmap() != null) {
                    userAvatarView.setImageResource(0);
                    userAvatarView.setImageBitmap(imageContainer.getBitmap());
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {}
        });
    }
}
