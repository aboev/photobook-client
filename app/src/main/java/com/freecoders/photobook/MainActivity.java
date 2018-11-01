package com.freecoders.photobook;

import android.app.*;
import android.support.v7.app.AppCompatActivity;
import android.view.*;

import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.utils.FileUtils;
import com.freecoders.photobook.utils.Permission;
import com.soundcloud.android.crop.Crop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.design.widget.TabLayout;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static String LOG_TAG = "MainActivity";

    TabLayout tabLayout;
    ViewPager mViewPager;

    MainActivityHandler mHandler;
    MainActivityPagerAdapter mPagerAdapter;

    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    ListView mDrawerList;

    ImageView mDrawerAvatarImage;
    TextView mDrawerUserName;
    TextView mDrawerContactKey;
    private TextView aboutTextView;

    android.support.design.widget.TabLayout.Tab friendsTab, galleryTab, feedTab;

    protected Dialog mSplashDialog;
    private int FRAGMENT_ID_CONTACTS = 0;
    private int FRAGMENT_ID_GALLERY = 1;
    private int FRAGMENT_ID_FEED = 2;

	@SuppressLint("NewApi") 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        final MainActivity mainActivity = this;
        Permission.requestPermissions(new CallbackInterface() {
            @Override
            public void onResponse(Object obj) {
                tabLayout = (TabLayout) findViewById(R.id.tab_layout);
                friendsTab = tabLayout.newTab().setIcon(R.drawable.ic_action_friends_tab);
                galleryTab = tabLayout.newTab().setIcon(R.drawable.ic_action_gallery_tab);
                feedTab = tabLayout.newTab().setIcon(R.drawable.ic_action_feed_tab);

                tabLayout.addTab(friendsTab);
                tabLayout.addTab(galleryTab);
                tabLayout.addTab(feedTab);

                tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if (mViewPager != null)
                            mViewPager.setCurrentItem(tab.getPosition());

                        Photobook.getPreferences().intLastOpenedTab = tab.getPosition();
                        Photobook.getPreferences().savePreferences();
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

                mPagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager());
                mViewPager = (ViewPager) findViewById(R.id.pager);
                mViewPager.setAdapter(mPagerAdapter);
                mViewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

                String[] mMenuItems = getResources().getStringArray(R.array.menu_items);
                mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerList = (ListView) findViewById(R.id.menu_drawer);
                mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

                mDrawerAvatarImage = (ImageView) findViewById(R.id.imgAvatarDrawer);
                mDrawerUserName = (TextView) findViewById(R.id.textUserNameDrawer);
                mDrawerContactKey = (TextView) findViewById(R.id.textContactKeyDrawer);

                aboutTextView = (TextView) findViewById(R.id.aboutTextView);
                aboutTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog aboutDialog = new AlertDialog.Builder(MainActivity.this).create();
                        aboutDialog.setTitle(R.string.about);
                        aboutDialog.setMessage(getString(R.string.aboutText));
                        aboutDialog.show();

                        TextView messageText = (TextView)aboutDialog.findViewById(android.R.id.message);
                        messageText.setGravity(Gravity.CENTER);
                    }
                });
                mDrawerList.setAdapter(new ArrayAdapter<String>(mainActivity,
                        R.layout.item_drawer_list, mMenuItems));

                mDrawerToggle = new ActionBarDrawerToggle(mainActivity, mDrawerLayout,
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
                mHandler.init(mainActivity);
                mHandler.checkLatestVersion();
                mHandler.handleIntent(getIntent());

                if (mSplashDialog != null) {
                    new SplashTimeoutTask().execute();
                }

                Log.d(LOG_TAG, "Loaded main activity");
            }
        });
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
        // getMenuInflater().inflate(R.menu.main, menu);
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
        if(mViewPager.getCurrentItem()!=FRAGMENT_ID_GALLERY ||
            !Photobook.getGalleryFragmentTab().onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        mHandler.handleIntent(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if ((Photobook.getPermissionHandlers().containsKey(requestCode)) &&
                (Photobook.getPermissionHandlers().get(requestCode) != null))
            Photobook.getPermissionHandlers().get(requestCode).onResponse(null);
    }
}

