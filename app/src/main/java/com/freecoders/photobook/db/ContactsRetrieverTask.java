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
import com.freecoders.photobook.utils.FileUtils;
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

public class ContactsRetrieverTask extends AsyncTask<String, Void,
        ArrayList<ContactsRetrieverTask.ContactEntry>> {
    private static String LOG_TAG = "ContactsRetrieverTask";

    private FriendsFragmentTab mFriendsTab;
    private Boolean boolPopulateTab;

    public ContactsRetrieverTask(FriendsFragmentTab friendsTab, Boolean boolPopulateTab)
    {
        this.mFriendsTab = friendsTab;
        this.boolPopulateTab = boolPopulateTab;
    }

    @Override
    protected ArrayList<ContactEntry> doInBackground(String... params) {
        return getContacts(mFriendsTab.mActivity);
    }

    @Override
    protected void onPostExecute(final ArrayList<ContactEntry> contacts) {
        final ArrayList<String> contactKeys = new ArrayList<String>();
        final ArrayList<String> hContactKeys = new ArrayList<String>();
        for (int i = 0; i < contacts.size(); i++) {
            contactKeys.add(contacts.get(i).strContactKey);
            hContactKeys.add(FileUtils.makeSHA1Hash(contacts.get(i).strContactKey));
        }

        for (int i = 0; i < contacts.size(); i++) {
            if (Photobook.getFriendsDataSource().
                    getFriendByContactKey(contacts.get(i).strContactKey)==null) {
                Photobook.getFriendsDataSource().createFriend(contacts.get(i).strName,
                        contacts.get(i).strContactKey, "", "",
                        FriendEntry.INT_STATUS_NULL);
            }
        }

        ServerInterface.postContactsRequest(mFriendsTab.mActivity,
                contactKeys, Photobook.getPreferences().strUserID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(LOG_TAG, "Response from server " + response.toString());
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONObject objMap = obj.getJSONObject("data");
                            Map<String, UserProfile> retMap =
                                    new Gson().fromJson(objMap.toString(),
                                            new TypeToken<HashMap<String, UserProfile>>() {
                                            }.getType());
                            for (int i = 0; i < contactKeys.size(); i++) {
                                if (retMap.containsKey(contactKeys.get(i))) {
                                    FriendEntry friend = Photobook.getFriendsDataSource().
                                            getFriendByContactKey(contactKeys.get(i));
                                    UserProfile profile = retMap.get(contactKeys.get(i));
                                    if (friend != null) {

                                        friend.setName(profile.name);
                                        friend.setAvatar(profile.avatar);
                                        friend.setUserId(profile.id);
                                        if (friend.getStatus() == FriendEntry.INT_STATUS_NULL) {
                                            friend.setStatus(FriendEntry.INT_STATUS_DEFAULT);
                                        }
                                        Photobook.getFriendsDataSource().updateFriend(friend);
                                    }
                                }
                            }
                            if (boolPopulateTab) {
                                mFriendsTab.friendsList.clear();
                                mFriendsTab.friendsList.addAll(
                                        Photobook.getFriendsDataSource().getFriendsByStatus(
                                                new int[]{
                                                FriendEntry.INT_STATUS_DEFAULT,
                                                FriendEntry.INT_STATUS_FRIEND}
                                         ));
                                mFriendsTab.adapter.notifyDataSetChanged();
                                Log.d(LOG_TAG,
                                        "Notifying data set changed list length"
                                        + mFriendsTab.friendsList.size());
                            }
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Exception" + e.getLocalizedMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LOG_TAG, "Error: " + error.getMessage());
                    }
                });
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

    public ArrayList<ContactEntry> getContacts(Context context) {
        ArrayList<ContactEntry> res = new ArrayList<ContactEntry>();
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
                        ContactEntry contact = new ContactEntry();
                        contact.strContactKey = strNormalizedNumber;
                        contact.strName = name;
                        res.add(contact);
                    }
                    pCur.close();
                }
                Cursor emailCursor = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);

                if (emailCursor.getCount() > 0) {
                    while (emailCursor.moveToNext()) {

                        String email = emailCursor.getString(emailCursor.getColumnIndex(
                                ContactsContract.CommonDataKinds.Email.DATA));
                        ContactEntry contact = new ContactEntry();
                        contact.strContactKey = email.toLowerCase();
                        contact.strName = name;
                        res.add(contact);
                    }
                }
                emailCursor.close();
            }
        }
        cur.close();
        return res;
    }

    public class ContactEntry {
        private String strName = "";
        private String strContactKey = "";
    }
}
