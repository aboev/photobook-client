package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.freecoders.photobook.db.ContactListInterface;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.FriendsDataSource;

import java.util.ArrayList;

@SuppressLint("NewApi") 
public class FriendsFragmentTab extends Fragment {

    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        FriendsDataSource friendsDataSource = new FriendsDataSource(getActivity());
        friendsDataSource.open();

        ArrayList<FriendEntry> list = friendsDataSource.getFriendsByStatus(
                FriendEntry.INT_STATUS_DEFAULT);

        listView = (ListView) rootView.findViewById(R.id.friendsList);
        FriendsListAdapter adapter = new FriendsListAdapter(getActivity(),
                R.layout.row_friend_list, list);
        listView.setAdapter(adapter);

        return rootView;
    }
}
