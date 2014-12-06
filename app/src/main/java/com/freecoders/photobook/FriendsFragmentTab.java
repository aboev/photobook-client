package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.common.Preferences;
import com.freecoders.photobook.db.ContactsRetrieverTask;
import com.freecoders.photobook.db.FriendEntry;

import java.util.ArrayList;

@SuppressLint("NewApi") 
public class FriendsFragmentTab extends Fragment {

    private ListView listView;
    public MainActivity mActivity;
    public ArrayList<FriendEntry> friendsList;
    public FriendsListAdapter adapter;
    private ContactsRetrieverTask contactsRetrieverTask;
    private Boolean boolUpdateList = true;

    public void setMainActivity(MainActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        listView = (ListView) rootView.findViewById(R.id.friendsList);
        setRetainInstance(true);
        Log.d(Constants.LOG_TAG, "Initializing FriendsFragmentTab");

        friendsList =  Photobook.getFriendsDataSource().getFriendsByStatus(
                FriendEntry.INT_STATUS_DEFAULT);

        adapter = new FriendsListAdapter(getActivity(),
                R.layout.row_friend_list, friendsList);
        listView.setAdapter(adapter);

        Photobook.setFriendsFragmentTab(this);

        if (boolUpdateList) {
            refreshContactList();
            boolUpdateList = false;
        }
        return rootView;
    }

    public void refreshContactList(){
        Log.d(Constants.LOG_TAG, "Refreshing contact list");
        if (Photobook.getPreferences().strUserID.isEmpty()) return;

        contactsRetrieverTask = new ContactsRetrieverTask(this, true);
        contactsRetrieverTask.execute();
    }
}
