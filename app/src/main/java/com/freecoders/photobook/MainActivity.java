package com.freecoders.photobook;

import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.utils.FileUtils;
import com.soundcloud.android.crop.Crop;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.FragmentTransaction;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends FragmentActivity {
    private static String LOG_TAG = "MainActivity";

    ViewPager mViewPager;

    MainActivityHandler mHandler;
    MainActivityPagerAdapter mPagerAdapter;

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ListView mDrawerList;

    ImageView mDrawerAvatarImage;
    TextView mDrawerUserName;
    TextView mDrawerContactKey;

    ActionBar.Tab friendsTab, galleryTab, feedTab;

    protected Dialog mSplashDialog;

	@SuppressLint("NewApi") 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();

        Photobook.setMainActivity(this);

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

        //mViewPager.setCurrentItem(Photobook.getPreferences().intLastOpenedTab);

        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        getActionBar().setSelectedNavigationItem(position);
                        Photobook.getPreferences().intLastOpenedTab = position;
                        Photobook.getPreferences().savePreferences();
                    }
                });

        String[] mMenuItems = getResources().getStringArray(R.array.menu_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.menu_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        //mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mDrawerAvatarImage = (ImageView) findViewById(R.id.imgAvatarDrawer);
        mDrawerUserName = (TextView) findViewById(R.id.textUserNameDrawer);
        mDrawerContactKey = (TextView) findViewById(R.id.textContactKeyDrawer);

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.item_drawer_list, mMenuItems));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mHandler = new MainActivityHandler();
        mHandler.init(this);
        mHandler.checkLatestVersion();
        mHandler.handleIntent(getIntent());

        getActionBar().show();

        if (mSplashDialog != null) {
            new SplashTimeoutTask().execute();
        }

        Log.d(LOG_TAG, "Loaded main activity");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == Constants.INTENT_PICK_IMAGE && data != null && data.getData() != null) {
            Uri _uri = data.getData();

            Cursor cursor = getContentResolver().query(_uri, new String[] {
                    android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
            cursor.moveToFirst();

            File tmpFile = new File(getCacheDir(), Constants.FILENAME_AVATAR);
            FileUtils.copyFileFromUri(new File(FileUtils.getRealPathFromURI(this, _uri)), tmpFile);
            cursor.close();
            File dstFile = new File(getFilesDir(), Constants.FILENAME_AVATAR);
            new Crop(Uri.fromFile(tmpFile)).output(Uri.fromFile(dstFile)).asSquare().start(this);
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            mHandler.updateAvatar();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        Log.d(LOG_TAG, "Pressed back on "+mViewPager.getCurrentItem());
        if(mViewPager.getCurrentItem()==1) {
            if(!Photobook.getGalleryFragmentTab().getBackToFolders())
                super.onBackPressed();
            // do something
        } else {

            super.onBackPressed();
        }
    }
}
