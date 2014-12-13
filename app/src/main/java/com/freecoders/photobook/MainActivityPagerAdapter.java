package com.freecoders.photobook;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

//Since this is an object collection, use a FragmentStatePagerAdapter,
//and NOT a FragmentPagerAdapter.
public class MainActivityPagerAdapter extends FragmentPagerAdapter {
    private MainActivity mActivity;
    private FriendsFragmentTab friendsFragment;
    private GalleryFragmentTab galleryFragment;
    private FeedFragmentTab feedFragmentTab;

 public MainActivityPagerAdapter(FragmentManager fm, MainActivity activity) {
     super(fm);
     this.mActivity = activity;
     friendsFragment = new FriendsFragmentTab();
     friendsFragment.setMainActivity(mActivity);
     galleryFragment = new GalleryFragmentTab();
     feedFragmentTab = new FeedFragmentTab();
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
