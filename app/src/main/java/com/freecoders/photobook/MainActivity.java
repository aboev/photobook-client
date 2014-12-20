package com.freecoders.photobook;

import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends FragmentActivity {

    ViewPager mViewPager;

    MainActivityHandler mHandler;
    MainActivityPagerAdapter mPagerAdapter;

	ActionBar.Tab friendsTab, galleryTab, feedTab;

    protected Dialog mSplashDialog;

	@SuppressLint("NewApi") 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();

        if (Photobook.isFirstStart()) {
            mSplashDialog = new Dialog(this, R.style.SplashScreen);
            mSplashDialog.setContentView(R.layout.activity_splash);
            mSplashDialog.setCancelable(false);
            mSplashDialog.show();
        } else {
            mSplashDialog = null;
        }

        setContentView(R.layout.activity_main);

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
                if (mViewPager != null)
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

        mPagerAdapter = new MainActivityPagerAdapter(
                getSupportFragmentManager(), this);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getActionBar().setSelectedNavigationItem(position);
                    }
                });

        mHandler = new MainActivityHandler();
        mHandler.init(this);

        getActionBar().show();

        if (mSplashDialog != null) {
            new SplashTimeoutTask().execute();
        }

        Log.d(Constants.LOG_TAG, "Loaded main activity");
    }

    public class SplashTimeoutTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSplashDialog.dismiss();
                }
            });
            return true;
        }
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
