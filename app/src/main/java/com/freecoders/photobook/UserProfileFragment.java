package com.freecoders.photobook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.DiskLruBitmapCache;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author Andrei Alikov andrei.alikov@gmail.com
 */
public class UserProfileFragment extends DialogFragment {
    private final static String LOG_TAG = "UserProfileFragment";

    private String userId;
    private TextView userNameView;
    private TextView userPhoneView;
    private TextView userEmailView;
    private ImageView userAvatarView;
    private Button followButton;
    private ImageLoader imageLoader;
    private boolean isUserFollowed;

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

        followButton = (Button) view.findViewById(R.id.btnFollow);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUserFollowed) {
                    ServerInterface.removeFriendRequest(createFollowChangeResponse(false),
                            Photobook.getMainActivity(), new String[]{userId});
                } else {
                    ServerInterface.addFriendRequest(createFollowChangeResponse(true),
                            Photobook.getMainActivity(), new String[]{userId});
                }
            }
        });

        createImageLoader();

        FriendEntry friendEntry = Photobook.getFriendsDataSource().getFriendByUserId(userId);
        isUserFollowed = friendEntry != null && friendEntry.getStatus() == FriendEntry.INT_STATUS_FRIEND;
        setFollowButtonText();

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

        return builder.create();
    }

    private Response.Listener<String> createFollowChangeResponse(final boolean followRequest) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resJson = new JSONObject(response);
                    String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                    if (strRes.equals(Constants.RESPONSE_RESULT_OK)) {
                        FriendEntry friendEntry = Photobook.getFriendsDataSource().getFriendByUserId(userId);
                        friendEntry.setStatus(
                                followRequest ? FriendEntry.INT_STATUS_FRIEND : FriendEntry.INT_STATUS_DEFAULT);
                        Photobook.getFriendsDataSource().updateFriend(friendEntry);
                        isUserFollowed = followRequest;
                        setFollowButtonText();
                    }
                } catch (JSONException e) {
                    Log.d(LOG_TAG, "Exception " + e.getLocalizedMessage());
                }
                Log.d(LOG_TAG, "Response: " + response);
            }
        };
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


    }

    private void setFollowButtonText() {
        followButton.setText(isUserFollowed ? R.string.btn_unfollow_text : R.string.btn_follow_text);
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
