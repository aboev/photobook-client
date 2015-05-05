package com.freecoders.photobook;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.freecoders.photobook.common.Photobook;

//Since this is an object collection, use a FragmentStatePagerAdapter,
//and NOT a FragmentPagerAdapter.
public class MainActivityPagerAdapter extends FragmentPagerAdapter {
    private FriendsFragmentTab friendsFragment;
    private GalleryFragmentTab galleryFragment;
    private FeedFragmentTab feedFragmentTab;

 public MainActivityPagerAdapter(FragmentManager fm) {
     super(fm);
     if (Photobook.getFriendsFragmentTab() == null)
        friendsFragment = new FriendsFragmentTab();
     else
        friendsFragment = Photobook.getFriendsFragmentTab();
     if (Photobook.getGalleryFragmentTab() == null)
        galleryFragment = new GalleryFragmentTab();
     else
        galleryFragment = Photobook.getGalleryFragmentTab();
     if (Photobook.getFeedFragmentTab() == null)
        feedFragmentTab = new FeedFragmentTab();
     else
        feedFragmentTab = Photobook.getFeedFragmentTab();
 }

 @Override
 public Fragment getItem(int i) {
	 if (i == 0) {
         return friendsFragment;
     } else if (i == 1) {
         return galleryFragment;
     } else {
         return feedFragmentTab;
     }
 }

 @Override
 public int getCount() {
     return 3;
 }

 @Override
 public CharSequence getPageTitle(int position) {
     return "OBJECT " + (position + 1);
 }
}
