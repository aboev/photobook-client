package com.freecoders.photobook;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

//Since this is an object collection, use a FragmentStatePagerAdapter,
//and NOT a FragmentPagerAdapter.
public class MainActivityPagerAdapter extends FragmentPagerAdapter {
 public MainActivityPagerAdapter(FragmentManager fm) {
     super(fm);
 }

 @Override
 public Fragment getItem(int i) {
	 Fragment fragment;
	 if (i == 0) 
		 fragment = new FriendsFragmentTab();
	 else if (i == 1)
		 fragment = new GalleryFragmentTab();
	 else 
		 fragment = new FeedFragmentTab();
     return fragment;
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
