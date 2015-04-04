package com.freecoders.photobook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.*;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.DefaultServerResponseHandler;
import com.freecoders.photobook.network.ServerErrorHandler;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.DiskLruBitmapCache;
import com.freecoders.photobook.utils.ImageUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * @author Andrei Alikov andrei.alikov@gmail.com
 */
public class UserProfileFragment extends DialogFragment {
    private final static String LOG_TAG = "UserProfileFragment";

    private String userId;
    private TextView userNameView;
    private TextView userPhoneView;
    private TextView userEmailView;
    private TextView followersView;
    private ImageView userAvatarView;
    private Button followButton;
    private ImageLoader imageLoader;
    private LinearLayout userProfileLayout;
    private boolean isUserFollowed;
    private Map<String, UserProfile> followers;

    private ServerInterface serverInterface;

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
        followersView = (TextView) view.findViewById(R.id.txtFollowers);
        followersView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLikesListView(userProfileLayout, followers);
            }
        });
        userProfileLayout = (LinearLayout) view.findViewById(R.id.userProfileLayout);

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

        serverInterface = new ServerInterface();
        ProfileServerResponseHandler serverHandler = new ProfileServerResponseHandler();
        serverInterface.setServerErrorHandler(serverHandler);
        serverInterface.setServerResponseHandler(serverHandler);
        serverInterface.sentFollowersRequest(userId);

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

    private void getLikesListView(View view, Map<String, UserProfile> userProfiles){
        int width = ImageUtils.dpToPx(300);
        final int height = ImageUtils.dpToPx(50);
        final int padding = 2;
        final Context context = getActivity();
        final HorizontalScrollView scrollView = new HorizontalScrollView(context);
        final LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollView.setPadding(padding, padding, padding, padding);
        scrollView.setLayoutParams(params);
        scrollView.addView(linearLayout);
        scrollView.setHorizontalScrollBarEnabled(false);
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(scrollView);
        popup.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_2));
        popup.setWidth(width);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        int[] location = new int[2];
        followersView.getLocationInWindow(location);
        popup.showAtLocation(view, Gravity.NO_GRAVITY, location[0],
                location[1] - (int) (height * 1.5));

        ServerInterface.getUserProfileRequest(getActivity(), userProfiles.keySet().toArray(new String[]{}),
                new Response.Listener<HashMap<String, UserProfile>>() {
                    @Override
                    public void onResponse(HashMap<String, UserProfile> response) {
                        Iterator it = response.entrySet().iterator();
                        Log.d(LOG_TAG, "Response size " + response.size());
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            final ImageView image = new ImageView(context);
                            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(height, height);
                            image.setLayoutParams(params);
                            image.setPadding(padding, padding, padding, padding);
                            image.setImageResource(R.drawable.avatar);
                            linearLayout.addView(image);
                            final UserProfile user = (UserProfile) pair.getValue();
                            final String id = (String) pair.getKey();
                            if ((user != null) && (user.avatar != null)
                                    && (URLUtil.isValidUrl(user.avatar)))
                                imageLoader.get(user.avatar, new ImageLoader.ImageListener() {
                                    @Override
                                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                                        if (imageContainer.getBitmap() != null) {
                                            image.setImageResource(0);
                                            image.setImageBitmap(imageContainer.getBitmap());
                                        }
                                    }

                                    @Override
                                    public void onErrorResponse(VolleyError volleyError) {

                                    }
                                });
                            image.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    FragmentManager fm = getFragmentManager();
                                    UserProfileFragment profileDialogFragment =
                                            new UserProfileFragment();
                                    profileDialogFragment.setUserId(id);
                                    profileDialogFragment.show(fm, "users_profile");
                                }
                            });
                        }
                    }
                }, null);
    }

    private class ProfileServerResponseHandler extends DefaultServerResponseHandler implements ServerErrorHandler {
        @Override
        public void onFollowersResponse(Map<String, UserProfile> users) {
            followersView.setText(String.valueOf(users.size()));
            followers = users;
        }

        @Override
        public void onServerRequestError(String request, VolleyError error) {
            Log.d(LOG_TAG, "Error for request " + request + ": " + error.networkResponse.data);
        }
    }
}
