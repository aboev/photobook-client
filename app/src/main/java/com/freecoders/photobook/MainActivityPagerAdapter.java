package com.freecoders.photobook;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

//Since this is an object collection, use a FragmentStatePagerAdapter,
//and NOT a FragmentPagerAdapter.
public class MainActivityPagerAdapter extends FragmentPagerAdapter {
    private MainActivity mActivity;

 public MainActivityPagerAdapter(FragmentManager fm, MainActivity activity) {
     super(fm);
     this.mActivity = activity;
 }

 @Override
 public Fragment getItem(int i) {
	 if (i == 0) {
         FriendsFragmentTab fragment = new FriendsFragmentTab();
         fragment.setMainActivity(mActivity);
         return fragment;
     } else if (i == 1) {
         Fragment fragment = new GalleryFragmentTab();
         return fragment;
     } else {
         Fragment fragment = new FeedFragmentTab();
         return fragment;
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
