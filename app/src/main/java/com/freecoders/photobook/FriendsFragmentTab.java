package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.android.volley.VolleyError;
import com.freecoders.photobook.classes.BookmarkAdapter;
import com.freecoders.photobook.classes.BookmarkHandler;
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.classes.GestureListener;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ChannelsRetrieverTask;
import com.freecoders.photobook.db.ContactsRetrieverTask;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;

import java.util.ArrayList;
import java.util.Map;

@SuppressLint("NewApi") 
public class FriendsFragmentTab extends Fragment {
    private static String LOG_TAG = "FriendsFragmentTab";

    private ListView listView;
    public ArrayList<FriendEntry> contactList = new ArrayList<FriendEntry>();
    public FriendsListAdapter adapter;
    private ContactsRetrieverTask contactsRetrieverTask;
    private ChannelsRetrieverTask channelsRetrieverTask;
    public GestureListener gestureListener;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout;
    private View colorSelector;
    private BookmarkAdapter bookmarkAdapter;
    public BookmarkHandler bookmarkHandler;
    private Boolean boolUpdateList = true;
    private int BOOKMARK_ID_FRIENDS = 0;
    private int BOOKMARK_ID_CONTACTS = 1;
    private int BOOKMARK_ID_CHANNELS = 2;
    private int curPosition = 0;
    private int defaultPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<FriendEntry> list =  Photobook.getFriendsDataSource().getFriendsByStatus(
                new int[]{FriendEntry.INT_STATUS_DEFAULT, FriendEntry.INT_STATUS_FRIEND});
        contactList.clear();
        contactList.addAll(list);
        adapter = new FriendsListAdapter(getActivity(),
                R.layout.row_friend_list, contactList);
        if (boolUpdateList && !Photobook.getPreferences().strUserID.isEmpty()) {
            reloadContactList(null);
            reloadChannelList(null);
            boolUpdateList = false;
        }
        Photobook.setFriendsFragmentTab(this);
        setRetainInstance(true);
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
        Log.d(LOG_TAG, "Initializing FriendsFragmentTab");

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if ((curPosition == BOOKMARK_ID_FRIENDS) || (curPosition == BOOKMARK_ID_CHANNELS)){
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra("userId", contactList.get(position).getUserId());
                getActivity().startActivity(intent);
            }
            }
        });

        bookmarkHandler = new BookmarkHandler(horizontalScrollView,
                Constants.BOOKMARKS_HEIGHT);
        gestureListener = new GestureListener(getActivity(), listView, bookmarkHandler);
        //listView.setOnTouchListener(gestureListener);

        if (bookmarkAdapter == null)
            bookmarkAdapter = new BookmarkAdapter(getActivity(), linearLayout, colorSelector,
                getResources().getStringArray(R.array.contacts_bookmark_items),
                R.array.contacts_bookmark_icons);
        else
            bookmarkAdapter.setParentView(linearLayout);
        bookmarkAdapter.setOnItemSelectedListener(
            new BookmarkAdapter.onItemSelectedListener() {
                @Override
                public void onItemSelected(int position) {
                    curPosition = position;
                    refreshContactList();
                }
            });
        bookmarkAdapter.setSelectedPosition(defaultPosition);
        return rootView;
    }

    public void applyFilter(int[] status){
        contactList =  Photobook.getFriendsDataSource().getFriendsByStatus(status);
        adapter.clear();
        adapter.addAll(contactList);
        adapter.notifyDataSetChanged();
    }

    public void reloadContactList(final CallbackInterface callbackInterface){
        Log.d(LOG_TAG, "Reloading contact list");
        if (Photobook.getPreferences().strUserID.isEmpty()) return;
        contactsRetrieverTask = new ContactsRetrieverTask(new CallbackInterface() {
            @Override
            public void onResponse(Object obj) {
                contactList.clear();
                contactList.addAll((ArrayList<FriendEntry>) obj);
                refreshContactList();
                callbackInterface.onResponse(obj);
            }
        });
        contactsRetrieverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshContactList () {
        ArrayList<FriendEntry> list = null;
        if (curPosition == BOOKMARK_ID_CONTACTS)
            list =  Photobook.getFriendsDataSource().getFriendsByStatus(
                    new int[]{FriendEntry.INT_STATUS_NULL});
        else if (curPosition == BOOKMARK_ID_FRIENDS)
            list =  Photobook.getFriendsDataSource().getFriendsByStatus(
                    new int[]{FriendEntry.INT_STATUS_DEFAULT,
                            FriendEntry.INT_STATUS_FRIEND});
        else if (curPosition == BOOKMARK_ID_CHANNELS)
            list = Photobook.getFriendsDataSource().getAllChannels();
        contactList.clear();
        if (list != null) contactList.addAll(list);
        adapter.notifyDataSetChanged();
    }

    public void reloadChannelList(CallbackInterface callbackInterface){
        Log.d(LOG_TAG, "Reloading channels list");
        if (Photobook.getPreferences().strUserID.isEmpty()) return;
        channelsRetrieverTask = new ChannelsRetrieverTask(callbackInterface);
        channelsRetrieverTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        refreshContactList();
    }

    public void syncChannels(final CallbackInterface callbackInterface) {
        ServerInterface.getFriendsRequest(Photobook.getMainActivity(),
            new Response.Listener<ArrayList<String>>() {
                @Override
                public void onResponse(ArrayList<String> userIds) {
                    for (int i = 0; i < userIds.size(); i++) {
                        FriendEntry channel = Photobook.getFriendsDataSource().
                                getChannelByChannelId(userIds.get(i));
                        if ((channel != null) &&
                                (channel.getStatus() == FriendEntry.INT_STATUS_DEFAULT)) {
                            channel.setStatus(FriendEntry.INT_STATUS_FRIEND);
                            Photobook.getFriendsDataSource().updateFriend(channel);
                        }
                    }
                    if (callbackInterface != null) callbackInterface.onResponse(userIds);
                }
            }, null);
    }

    public void syncChannels() {
        syncChannels(new CallbackInterface() {
            @Override
            public void onResponse(Object obj) {
                if (curPosition == BOOKMARK_ID_CHANNELS) {
                    ArrayList<FriendEntry> list = Photobook.getFriendsDataSource().getAllChannels();
                    contactList.clear();
                    contactList.addAll(list);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
}
