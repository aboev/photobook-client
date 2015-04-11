package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.Response;
import com.freecoders.photobook.classes.BookmarkAdapter;
import com.freecoders.photobook.classes.BookmarkHandler;
import com.freecoders.photobook.classes.GestureListener;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ContactsRetrieverTask;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;

import java.util.ArrayList;

@SuppressLint("NewApi") 
public class FriendsFragmentTab extends Fragment {
    private static String LOG_TAG = "FriendsFragmentTab";

    private ListView listView;
    public MainActivity mActivity;
    public ArrayList<FriendEntry> friendsList;
    public ArrayList<FriendEntry> channelList;
    public FriendsListAdapter adapter;
    private ContactsRetrieverTask contactsRetrieverTask;
    public GestureListener gestureListener;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout;
    private View colorSelector;
    private BookmarkAdapter bookmarkAdapter;
    public BookmarkHandler bookmarkHandler;
    private Boolean boolUpdateList = true;

    public void setMainActivity(MainActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        listView = (ListView) rootView.findViewById(R.id.friendsList);
        horizontalScrollView = (HorizontalScrollView)
                rootView.findViewById(R.id.bookmarkScrollView);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.bookmarkLinearLayout);
        colorSelector = (View) rootView.findViewById(R.id.bookmarkColorSelector1);
        setRetainInstance(true);
        Log.d(LOG_TAG, "Initializing FriendsFragmentTab");

        friendsList =  Photobook.getFriendsDataSource().getFriendsByStatus(
                new int[]{FriendEntry.INT_STATUS_DEFAULT,
                        FriendEntry.INT_STATUS_FRIEND});

        adapter = new FriendsListAdapter(getActivity(),
                R.layout.row_friend_list, friendsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra("userId", friendsList.get(position).getUserId());
                getActivity().startActivity(intent);
            }
        });

        bookmarkHandler = new BookmarkHandler(horizontalScrollView,
                Constants.BOOKMARKS_HEIGHT);
        gestureListener = new GestureListener(getActivity(), listView, bookmarkHandler);
        //listView.setOnTouchListener(gestureListener);

        bookmarkAdapter = new BookmarkAdapter(getActivity(), linearLayout, colorSelector,
                getResources().getStringArray(R.array.contacts_bookmark_items),
                R.array.contacts_bookmark_icons);
        bookmarkAdapter.setOnItemSelectedListener(
                new BookmarkAdapter.onItemSelectedListener() {
                    @Override
                    public void onItemSelected(int position) {
                        if (position == 1)
                            friendsList =  Photobook.getFriendsDataSource().getFriendsByStatus(
                                    new int[]{FriendEntry.INT_STATUS_NULL});
                        else if (position == 0)
                            friendsList =  Photobook.getFriendsDataSource().getFriendsByStatus(
                                    new int[]{FriendEntry.INT_STATUS_DEFAULT,
                                            FriendEntry.INT_STATUS_FRIEND});
                        else if (position == 2) {
                            friendsList.clear();
                            friendsList.addAll(channelList);
                        }
                        adapter.clear();
                        adapter.addAll(friendsList);
                        adapter.notifyDataSetChanged();
                    }
                });


        Photobook.setFriendsFragmentTab(this);

        if (boolUpdateList && !Photobook.getPreferences().strUserID.isEmpty()) {
            refreshContactList();
            refreshChannelList();
            boolUpdateList = false;
        }

        return rootView;
    }

    public void applyFilter(int[] status){
        friendsList =  Photobook.getFriendsDataSource().getFriendsByStatus(status);
        adapter.clear();
        adapter.addAll(friendsList);
        adapter.notifyDataSetChanged();
    }

    public void refreshContactList(){
        Log.d(LOG_TAG, "Refreshing contact list");
        if (Photobook.getPreferences().strUserID.isEmpty()) return;

        contactsRetrieverTask = new ContactsRetrieverTask(this, true);
        contactsRetrieverTask.execute();
    }

    public void refreshChannelList(){
        if (channelList == null) {
            channelList = new ArrayList<FriendEntry>();
        }
        ServerInterface.getChannelsRequest(getActivity(),
            new Response.Listener<ArrayList<UserProfile>>() {
                @Override
                public void onResponse(ArrayList<UserProfile> response) {
                    channelList.clear();
                    for (int i = 0; i < response.size(); i++) {
                        FriendEntry channel = new FriendEntry();
                        channel.setName(response.get(i).name);
                        channel.setAvatar(response.get(i).avatar);
                        channel.setUserId(response.get(i).id);
                        channelList.add(channel);
                    }
                }
            }, null);
    }
}
