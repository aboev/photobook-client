package com.freecoders.photobook;

import com.freecoders.photobook.R;
import com.freecoders.photobook.db.FriendsDataSource;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.app.FragmentTransaction;

public class MainActivity extends FragmentActivity {

    ViewPager mViewPager;

    MainActivityHandler mHandler;
    MainActivityPagerAdapter mPagerAdapter;

	ActionBar.Tab friendsTab, galleryTab, feedTab;

	@SuppressLint("NewApi") 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        
        friendsTab = actionBar.newTab().setIcon(R.drawable.ic_action_friends_tab);
        galleryTab = actionBar.newTab().setIcon(R.drawable.ic_action_gallery_tab);
        feedTab = actionBar.newTab().setIcon(R.drawable.ic_action_feed_tab);
        
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
			}
        };
        
        friendsTab.setTabListener(tabListener);
        galleryTab.setTabListener(tabListener);
        feedTab.setTabListener(tabListener);
        
        actionBar.addTab(friendsTab);
        actionBar.addTab(galleryTab);
        actionBar.addTab(feedTab);

        mHandler = new MainActivityHandler();
        mHandler.init(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
