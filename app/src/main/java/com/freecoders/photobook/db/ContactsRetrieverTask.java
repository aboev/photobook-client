package com.freecoders.photobook.db;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.FriendsFragmentTab;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.utils.PhoneUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by maximilian on 11/29/14.
 */

public class ContactsRetrieverTask extends AsyncTask<String, Void, ArrayList<String>> {

    private FriendsFragmentTab mFriendsTab;
    private Boolean boolPopulateTab;

    public ContactsRetrieverTask(FriendsFragmentTab friendsTab, Boolean boolPopulateTab)
    {
        this.mFriendsTab = friendsTab;
        this.boolPopulateTab = boolPopulateTab;
    }

    @Override
    protected ArrayList<String> doInBackground(String... params) {
        return getContacts(mFriendsTab.mActivity);
    }

    @Override
    protected void onPostExecute(final ArrayList<String> contactList) {
        for (int i = 0; i < contactList.size(); i++) {
            if (Photobook.getFriendsDataSource().getFriendByContactKey(contactList.get(i))==null) {
                Photobook.getFriendsDataSource().createFriend("", contactList.get(i), "", "",
                        FriendEntry.INT_STATUS_NULL);
            }
        }

        ServerInterface.postContactsRequest(mFriendsTab.mActivity,
                contactList, Photobook.getPreferences().strUserID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(Constants.LOG_TAG, "Response from server " + response.toString());
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject objMap = obj.getJSONObject("data");
                            Map<String, UserProfile> retMap =
                                    new Gson().fromJson(objMap.toString(),
                                            new TypeToken<HashMap<String, UserProfile>>() {
                                            }.getType());
                            for (int i = 0; i < contactList.size(); i++) {
                                if (retMap.containsKey(contactList.get(i))) {
                                    FriendEntry friend = Photobook.getFriendsDataSource().
                                            getFriendByContactKey(contactList.get(i));
                                    UserProfile profile = retMap.get(contactList.get(i));
                                    if (friend != null) {

                                        friend.setName(profile.name);
                                        friend.setAvatar(profile.avatar);
                                        friend.setUserId(profile.id);
                                        friend.setStatus(FriendEntry.INT_STATUS_DEFAULT);
                                        Photobook.getFriendsDataSource().updateFriend(friend);
                                    }
                                }
                            }
                            if (boolPopulateTab) {
                                mFriendsTab.friendsList.clear();
                                mFriendsTab.friendsList.addAll(
                                        Photobook.getFriendsDataSource().getFriendsByStatus(
                                                FriendEntry.INT_STATUS_DEFAULT));
                                mFriendsTab.adapter.notifyDataSetChanged();
                                Log.d(Constants.LOG_TAG,
                                        "Notifying data set changed list length"
                                        + mFriendsTab.friendsList.size());
                            }
                        } catch (Exception e) {
                            Log.d(Constants.LOG_TAG, "Exception" + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(Constants.LOG_TAG, "Error: " + error.getMessage());
                    }
                });
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

    public ArrayList<String> getContacts(Context context) {

        ArrayList<String> telNumbers = new ArrayList<String>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String strNumber = pCur.getString(pCur.getColumnIndex
                                (ContactsContract.CommonDataKinds.Phone.NUMBER));
                        String strNormalizedNumber = PhoneUtils.getNormalizedPhoneNumber(strNumber);
                        telNumbers.add(strNormalizedNumber);
                    }
                    pCur.close();
                }
            }
        }

        return telNumbers;
    }
}