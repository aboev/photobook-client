package com.freecoders.photobook.db;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.freecoders.photobook.MainActivity;
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.utils.FileUtils;
import com.freecoders.photobook.utils.PhoneUtils;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by maximilian on 11/29/14.
 */

public class ContactsRetrieverTask extends AsyncTask<String, Void,
        ArrayList<ContactsRetrieverTask.ContactEntry>> {
    private static String LOG_TAG = "ContactsRetrieverTask";

    private CallbackInterface callbackInterface;

    public ContactsRetrieverTask(CallbackInterface callbackInterface)
    {
        this.callbackInterface = callbackInterface;
    }

    @Override
    protected ArrayList<ContactEntry> doInBackground(String... params) {

        try{
            return getContacts(); // activity could be null -> ContentResolver null , Cursor null etc.
        }catch(Exception ex){
            Log.d(LOG_TAG, "Error: " + ex.getMessage());
        }  //try

        return new ArrayList<ContactEntry>();
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

        Log.d(LOG_TAG, "Sending post contacts request for " + new Gson().toJson(contactKeys));
        ServerInterface.postContactsRequest(Photobook.getMainActivity(), hContactKeys,
            new Response.Listener<Map<String, UserProfile>>() {
                @Override
                public void onResponse(Map<String, UserProfile> response) {
                    for (int i = 0; i < hContactKeys.size(); i++) {
                        if (response.containsKey(hContactKeys.get(i))) {
                            FriendEntry friend = Photobook.getFriendsDataSource().
                                    getFriendByContactKey(contactKeys.get(i));
                            UserProfile profile = response.get(hContactKeys.get(i));
                            if (friend != null) {
                                friend.setName(profile.name);
                                friend.setAvatar(profile.avatar);
                                friend.setUserId(profile.id);
                                if (friend.getStatus() == FriendEntry.INT_STATUS_NULL) {
                                    if (profile.status != null)
                                        friend.setStatus(profile.status + 1);
                                    else
                                        friend.setStatus(FriendEntry.INT_STATUS_DEFAULT);
                                }
                                Photobook.getFriendsDataSource().updateFriend(friend);
                            }
                        }
                    }
                    ArrayList<FriendEntry> friendsList = Photobook.getFriendsDataSource().
                        getFriendsByStatus(new int[]{FriendEntry.INT_STATUS_DEFAULT,
                        FriendEntry.INT_STATUS_FRIEND});
                    callbackInterface.onResponse(friendsList);
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

    public ArrayList<ContactEntry> getContacts() {
        ArrayList<ContactEntry> res = new ArrayList<ContactEntry>();

        MainActivity mainActivity = Photobook.getMainActivity();
        ContentResolver cr =  mainActivity.getContentResolver();
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
