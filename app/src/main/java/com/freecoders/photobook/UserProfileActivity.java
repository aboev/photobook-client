package com.freecoders.photobook;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
import com.freecoders.photobook.gson.FeedEntryJson;
import com.freecoders.photobook.gson.ImageJson;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Andrei Alikov andrei.alikov@gmail.com
 */
public class UserProfileActivity extends ActionBarActivity {
    private final static String LOG_TAG = "UserProfileActivity";

    private String userId;
    private TextView userNameView;
    private TextView userPhoneView;
    private TextView followersView;
    private ImageView userAvatarView;
    private TextView followButton;
    private ImageLoader imageLoader;
    private LinearLayout userProfileLayout;
    private GridView gridView;
    private PublicGalleryAdapter galleryAdapter;
    private ArrayList<ImageJson> imageList = new ArrayList<ImageJson>();
    private boolean isUserFollowed;
    private Map<String, UserProfile> followers;
    private UserProfile userProfile;
    private LinearLayout followersLayout;

    private ServerInterface serverInterface;

    public void setUserId(String id) {
        userId = id;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Intent intent = getIntent();

        setUserId(intent.getStringExtra("userId"));

        userNameView = (TextView) findViewById(R.id.txtUserName);
        userPhoneView = (TextView) findViewById(R.id.txtUserPhone);
        userAvatarView = (ImageView) findViewById(R.id.imageViewAvatar);
        followersView = (TextView) findViewById(R.id.textFollowerCount);
        userProfileLayout = (LinearLayout) findViewById(R.id.headerLayout);
        gridView = (GridView) findViewById(R.id.userGalleryGridView);
        followersLayout = (LinearLayout) findViewById(R.id.followersLayout);
        followersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFolowersListView(userProfileLayout, followers);
            }
        });
        galleryAdapter = new PublicGalleryAdapter(this,
            R.layout.item_gallery_public, imageList);
        gridView.setAdapter(galleryAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ImageJson image = galleryAdapter.getItem(i);
                if ((userProfile != null) && (image != null)) {
                    FeedEntryJson feedItem = new FeedEntryJson();
                    feedItem.author = userProfile;
                    feedItem.image = image;
                    Intent mIntent = new Intent(Photobook.getMainActivity(),
                            ImageDetailsActivity.class);
                    Photobook.setImageDetails(feedItem);
                    startActivity(mIntent);
                }
            }
        });

        followButton = (TextView) findViewById(R.id.textViewButtonFollow);
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

        FriendEntry friendEntry = Photobook.getFriendsDataSource().getContactByUserId(userId);
        isUserFollowed = friendEntry != null
                && friendEntry.getStatus() == FriendEntry.INT_STATUS_FRIEND;
        setFollowButtonText();

        serverInterface = new ServerInterface();
        ProfileServerResponseHandler serverHandler = new ProfileServerResponseHandler();
        serverInterface.setServerErrorHandler(serverHandler);
        serverInterface.setServerResponseHandler(serverHandler);
        serverInterface.sentFollowersRequest(userId);

        fillUserProfileInfo(new UserProfile());
        ServerInterface.getUserProfileRequest(null, new String[]{userId},
            new Response.Listener<HashMap<String, UserProfile>>() {
                @Override
                public void onResponse(HashMap<String, UserProfile> stringUserProfileHashMap) {
                    UserProfile profile = stringUserProfileHashMap.get(userId);
                    if (profile != null) {
                        fillUserProfileInfo(profile);
                        userProfile = profile;
                    }
                }
            }, null);

        ServerInterface.getImageDetailsRequestJson(this, null, userId,
            new Response.Listener<ArrayList<ImageJson>>() {
                @Override
                public void onResponse(ArrayList<ImageJson> response) {
                    imageList.clear();
                    Log.d(LOG_TAG, "Received " + response.size() + " items");
                    for (int i = 0; i < response.size(); i++) {
                        ImageJson image = response.get(i);
                        imageList.add(image);
                    }
                    galleryAdapter.notifyDataSetChanged();
                }
            }, null
            );
    }

    private Response.Listener<String> createFollowChangeResponse(final boolean followRequest) {
        return new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resJson = new JSONObject(response);
                    String strRes = resJson.getString(Constants.RESPONSE_RESULT);
                    if (strRes.equals(Constants.RESPONSE_RESULT_OK)) {
                        FriendEntry friendEntry = Photobook.getFriendsDataSource().
                            getContactByUserId(userId);
                        if (friendEntry != null) {
                            friendEntry.setStatus(followRequest ? FriendEntry.INT_STATUS_FRIEND :
                                    FriendEntry.INT_STATUS_DEFAULT);
                            Photobook.getFriendsDataSource().updateFriend(friendEntry);
                            Photobook.getFriendsFragmentTab().refreshContactList();
                            isUserFollowed = followRequest;
                            setFollowButtonText();
                        }
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
            diskCache = new DiskLruBitmapCache(this, "DiskCache", 2000000,
                    Bitmap.CompressFormat.JPEG, 100);
        } catch (IOException e) {

        }
        imageLoader = new ImageLoader(VolleySingleton.getInstance(this).getRequestQueue(),
                diskCache);
    }

    private void setFollowButtonText() {
        if (Photobook.getPreferences().intPublicID.toString().equals(userId))
            followButton.setVisibility(View.GONE);
        else
            followButton.setVisibility(View.VISIBLE);
        followButton.setText(isUserFollowed ? R.string.btn_unfollow_text : R.string.btn_follow_text);
    }

    private void fillUserProfileInfo(UserProfile profile) {
        userNameView.setText(profile.name);
        userPhoneView.setText(profile.phone);

        imageLoader.get(profile.avatar, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                if (imageContainer.getBitmap() != null) {
                    userAvatarView.setImageResource(0);
                    userAvatarView.setImageBitmap(imageContainer.getBitmap());
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
            }
        });
    }

    private void getFolowersListView(View view, Map<String, UserProfile> userProfiles) {
        String inflater = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater vi = (LayoutInflater) getSystemService(inflater);
        View popupView = vi.inflate(R.layout.popup, null);
        final int height = ImageUtils.dpToPx(50);
        final int padding = ImageUtils.dpToPx(2);
        final LinearLayout ll = (LinearLayout)popupView.findViewById(R.id.popupLinearLayout);
        final Context context = this;
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(popupView);
        popup.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);
        int[] location = new int[2];
        followersView.getLocationOnScreen(location);
        popup.showAtLocation(followersView, Gravity.NO_GRAVITY, location[0],
                location[1] - (int) (height * 1.5));
        if ((userProfiles == null)||(userProfiles.size() == 0)) return;
        ServerInterface.getUserProfileRequest(this, userProfiles.keySet().toArray(new String[]{}),
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
                        ll.addView(image);
                        final UserProfile user = (UserProfile) pair.getValue();
                        final String id = (String) pair.getKey();
                        if ((user != null) && (user.avatar != null)
                                && (URLUtil.isValidUrl(user.avatar)))
                            imageLoader.get(user.avatar, new ImageLoader.ImageListener() {
                                @Override
                                public void onResponse(ImageLoader.ImageContainer
                                        imageContainer, boolean b) {
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
                                Intent intent = new Intent(UserProfileActivity.this,
                                    UserProfileActivity.class);
                                intent.putExtra("userId", id);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }, null);
    }

    private class ProfileServerResponseHandler extends DefaultServerResponseHandler
            implements ServerErrorHandler {
        @Override
        public void onFollowersResponse(Map<String, UserProfile> users) {
            followersView.setText(String.valueOf(users.size()));
            followers = users;
        }

        @Override
        public void onServerRequestError(String request, VolleyError error) {
            String message = (error.networkResponse == null ? error.toString() :
                    new String(error.networkResponse.data));
            Log.d(LOG_TAG, "Error for request " + request + ": " + message);
        }

        @Override
        public void onServerRequestError(String request, Exception ex) {
            Log.d(LOG_TAG, "Error for request " + request + ": " + ex.getLocalizedMessage());
        }
    }
}
