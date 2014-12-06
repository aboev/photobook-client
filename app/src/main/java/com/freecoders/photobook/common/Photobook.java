package com.freecoders.photobook.common;

import android.app.Application;

import com.freecoders.photobook.FriendsFragmentTab;
import com.freecoders.photobook.MainActivity;
import com.freecoders.photobook.db.FriendsDataSource;
import com.freecoders.photobook.db.ImagesDataSource;

/**
 * Created by Alex on 2014-11-30.
 */
public class Photobook extends Application {
    private static FriendsDataSource friendsDataSource;
    private static ImagesDataSource imagesDataSource;
    private static FriendsFragmentTab mFriendsTab;
    private static MainActivity mActivity;
    private static Preferences mPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        friendsDataSource = new FriendsDataSource(this);
        friendsDataSource.open();
        imagesDataSource = new ImagesDataSource(this);
        imagesDataSource.open();
        mPreferences = new Preferences(this);
        mPreferences.loadPreferences();
    }

    public final static FriendsDataSource getFriendsDataSource(){
        return friendsDataSource;
    }

    public final static ImagesDataSource getImagesDataSource(){
        return imagesDataSource;
    }

    public final static void setMainActivity(MainActivity activity){
        mActivity = activity;
    }

    public final static MainActivity getMainActivity(){
        return mActivity;
    }

    public final static void setFriendsFragmentTab(FriendsFragmentTab tab){
        mFriendsTab = tab;
    }

    public final static FriendsFragmentTab getFriendsFragmentTab(){
        return mFriendsTab;
    }

    public final static Preferences getPreferences() {return mPreferences;}
}
