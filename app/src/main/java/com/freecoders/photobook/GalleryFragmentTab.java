package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.etsy.android.grid.StaggeredGridView;
import com.freecoders.photobook.classes.BookmarkAdapter;
import com.freecoders.photobook.classes.BookmarkHandler;
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.classes.GestureListener;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.db.ImagesDataSource;
import com.freecoders.photobook.gson.ImageJson;
import com.freecoders.photobook.network.ImageUploader;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.utils.FileUtils;
import com.freecoders.photobook.utils.ImageUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressLint("NewApi") 
public class GalleryFragmentTab extends Fragment {
    private static String LOG_TAG = "GalleryFragmentTab";

    private ArrayList<ImageEntry> mImageList = new ArrayList<ImageEntry>();
    private ImageUploader mImageLoader = new ImageUploader();
    private GalleryAdapter mAdapter;
    private StaggeredGridView mGridView;
    public GestureListener gestureListener;
    private HorizontalScrollView horizontalScrollView;
    private LinearLayout linearLayout;
    private View colorSelector;
    private BookmarkAdapter bookmarkAdapter;
    public BookmarkHandler bookmarkHandler;
    private Boolean boolSyncGallery = true;
    private int BOOKMARK_ID_GALLERY = 0;
    private int BOOKMARK_ID_FOLDERS = 1;
    private int BOOKMARK_ID_FOLDERS_SELECTED = -1;
    private int BOOKMARK_ID_SHARES = 2;
    private int curPosition = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new GalleryAdapter(getActivity(), R.layout.item_gallery,
                new ArrayList<ImageEntry>());
        reloadGallery(new CallbackInterface() {
            @Override
            public void onResponse(Object obj) {
                syncGallery();
            }
        });
        if (boolSyncGallery && !Photobook.getPreferences().strUserID.isEmpty()) {
            //syncGallery();
            syncComments();
            boolSyncGallery = false;
        }
        Photobook.setGalleryFragmentTab(this);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        mGridView = (StaggeredGridView) rootView.findViewById(R.id.gridView);
        horizontalScrollView = (HorizontalScrollView)
                rootView.findViewById(R.id.bookmarkScrollView);
        linearLayout = (LinearLayout) rootView.findViewById(R.id.bookmarkLinearLayout);
        colorSelector = (View) rootView.findViewById(R.id.bookmarkColorSelector2);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(OnItemClickListener);
        mGridView.setOnItemLongClickListener(new ImageLongClickListener());
        bookmarkHandler = new BookmarkHandler(horizontalScrollView,
                Constants.BOOKMARKS_HEIGHT);
        gestureListener = new GestureListener(getActivity(), mGridView, bookmarkHandler);
        //mGridView.setOnTouchListener(gestureListener);
        if (bookmarkAdapter == null)
            bookmarkAdapter = new BookmarkAdapter(getActivity(), linearLayout, colorSelector,
                getResources().getStringArray(R.array.gallery_bookmark_items),
                R.array.gallery_bookmark_icons);
        else
            bookmarkAdapter.setParentView(linearLayout);
        bookmarkAdapter.setOnItemSelectedListener(
            new BookmarkAdapter.onItemSelectedListener() {
                @Override
                public void onItemSelected(int position) {
                    curPosition = position;
                    refreshGallery();
                }
            });
        return rootView;
    }

    public class GalleryLoaderClass extends AsyncTask<String, Void, Boolean> {
        private String strBucketId = null;
        private Integer intImageStatus = null;
        private CallbackInterface deliverResult;

        public GalleryLoaderClass(String strBucketId, Integer intImageStatus,
                CallbackInterface deliverResult) {
            this.strBucketId = strBucketId;
            this.intImageStatus = intImageStatus;
            this.deliverResult = deliverResult;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            final ArrayList<ImageEntry> imgList = Photobook.
                    getImagesDataSource().getImageList(strBucketId, intImageStatus);
            Photobook.getMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deliverResult.onResponse(imgList);
                }
            });
            return true;
        }
    }

    private final class ImageLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final ImageEntry image = mAdapter.getItem(position);
            if (image.getStatus() == ImageEntry.INT_STATUS_SHARED) {
                ImageDialogFragment imageDialogFragment = new ImageDialogFragment();
                imageDialogFragment.setImageMenuHandler(new ImageDialogFragment.ImageMenuHandler() {
                    @Override
                    public void onUnShareImage() {
                        ServerInterface.unShareImageRequest(getActivity(), image.getServerId(),
                                new UnShareImageResponse(image), new DefaultErrorListener());
                    }
                });
                FragmentManager fm = getActivity().getFragmentManager();
                imageDialogFragment.show(fm, "image_menu");
                return true;
            }
            return false;
        }
    }

    private static class DefaultErrorListener implements Response.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError volleyError) {

        }
    }

    private class UnShareImageResponse implements Response.Listener<String> {
        private final ImageEntry image;

        private UnShareImageResponse(ImageEntry image) {
            this.image = image;
        }

        @Override
        public void onResponse(String s) {
            image.setStatus(ImageEntry.INT_STATUS_DEFAULT);
            Photobook.getImagesDataSource().updateImage(image);
            refreshGallery();
            if (mAdapter != null) mAdapter.notifyDataSetChanged();
        }
    }

    AdapterView.OnItemClickListener OnItemClickListener
            = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            final ImageEntry image = mAdapter.getItem(position);
            final int pos = position;
            if (image.getStatus() == ImageEntry.INT_STATUS_DEFAULT) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                LayoutInflater inflater = (LayoutInflater) getActivity().
                        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.dialog_upload, parent, false);
                ImageView imgView = (ImageView) layout.findViewById(R.id.fragmentImgView);
                final EditText editText = (EditText) layout.findViewById(R.id.fragmentEditText);
                Button button = (Button) layout.findViewById(R.id.fragmentButton);

                Bitmap b = ImageUtils.decodeSampledBitmap(image.getOrigUri());
                int orientation = ImageUtils.getExifOrientation(image.getOrigUri());
                Log.d(LOG_TAG, "Orientation1 = " + orientation + " for image " + image.getOrigUri()
                        +" "+image.getThumbUri());
                if ((orientation == 90) || (orientation == 270)) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(orientation);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(b, 0, 0,
                            b.getWidth(), b.getHeight(), matrix, true);
                    imgView.setImageBitmap(rotatedBitmap);
                } else
                    imgView.setImageBitmap(b);
                //imgView.setImageURI(Uri.parse(image.getOrigUri()));
                dialog.setView(layout);
                final AlertDialog alertDialog = dialog.create();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageEntry imageToShare = image;//mImageList.get(pos)
                        imageToShare.setTitle(editText.getText().toString());
                        String strOrigUri = imageToShare.getOrigUri();
                        String strThumbUri = imageToShare.getThumbUri();
                        String strDestOrigName = imageToShare.getMediaStoreID();
                        String strDestThumbName = imageToShare.getMediaStoreID() +
                                "_thumb";
                        if (strOrigUri.contains(".")) {
                            String filenameArray[] = strOrigUri.split("\\.");
                            String extension = filenameArray[filenameArray.length - 1];
                            strDestOrigName = strDestOrigName + "." + extension;
                            strDestThumbName = strDestThumbName + "." + extension;
                        }
                        File destOrigFile = new File(Photobook.getMainActivity().getFilesDir(),
                                strDestOrigName);
                        File destThumbFile = new File(Photobook.getMainActivity().getFilesDir(),
                                strDestThumbName);
                        destOrigFile.getParentFile().mkdirs();
                        destThumbFile.getParentFile().mkdirs();
                        if (FileUtils.copyFileFromUri(new File(strOrigUri), destOrigFile)) {
                            imageToShare.setOrigUri(destOrigFile.toString());
                            Log.d(LOG_TAG, "Saved local image to " +
                                    destOrigFile.toString());
                        }
                        if (FileUtils.copyFileFromUri(new File(strThumbUri), destThumbFile)) {
                            imageToShare.setThumbUri(destThumbFile.toString());
                            Log.d(LOG_TAG, "Saved local thumbnail to " +
                                    destThumbFile.toString());
                        }
                        imageToShare.setStatus(ImageEntry.INT_STATUS_SHARING);
                        Photobook.getImagesDataSource().saveImage(imageToShare);
                        //mImageLoader.uploadImage(mImageList, pos, mAdapter);
                        mImageLoader.uploadImageS3(imageToShare, strOrigUri.toLowerCase() ,
                                mAdapter);
                        reloadGallery(null);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                alertDialog.show();
            } else if (image.getStatus() == ImageEntry.INT_STATUS_SHARED) {
                Intent mIntent = new Intent(Photobook.getMainActivity(), ImageDetailsActivity.class);
                Bundle b = new Bundle();
                b.putBoolean(Photobook.extraImageSource, true);
                mIntent.putExtras(b);
                Photobook.setGalleryImageDetails(image);
                startActivity(mIntent);
            }
        }
    };

    public void showBuckets () {
        ArrayList<ImagesDataSource.BucketEntry> buckets =
                Photobook.getImagesDataSource().getBuckets();
        ArrayList<ImageEntry> bucketThumbs = new ArrayList<ImageEntry>();
        for (int i = 0; i < buckets.size(); i++) {
            ImageEntry bucketThumb = new ImageEntry();
            bucketThumb.setTitle(buckets.get(i).strBucketName);
            bucketThumb.setStatus(ImageEntry.INT_STATUS_BUCKET);
            bucketThumb.setRatio(1);
            bucketThumb.setThumbUri(buckets.get(i).strTitleImageUrl);
            bucketThumb.setOrigUri(buckets.get(i).strTitleImageUrl);
            bucketThumb.setBucketId(buckets.get(i).strBucketId);
            bucketThumbs.add(bucketThumb);
        }
        mAdapter.clear();
        mAdapter.addAll(bucketThumbs);
        mAdapter.notifyDataSetChanged();
        mGridView.setOnItemClickListener(OnBucketClickListener);
    }

    public void showSharedImages () {
        ArrayList<ImageEntry> sharedImages = new ArrayList<ImageEntry>();
        for (int i = 0; i < mImageList.size(); i++)
            if ((mImageList.get(i).getStatus() == ImageEntry.INT_STATUS_SHARED) ||
                    (mImageList.get(i).getStatus() == ImageEntry.INT_STATUS_SHARING))
                sharedImages.add(mImageList.get(i));
        mAdapter.clear();
        mAdapter.addAll(sharedImages);
        mAdapter.notifyDataSetChanged();
    }

    AdapterView.OnItemClickListener OnBucketClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ImageEntry bucket = mAdapter.getItem(position);
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            new GalleryLoaderClass(bucket.getBucketId(), null, new CallbackInterface() {
                public void onResponse(Object obj) {
                    mAdapter.clear();
                    mAdapter.addAll((ArrayList<ImageEntry>) obj);
                    mAdapter.notifyDataSetChanged();
                }
            }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mGridView.setOnItemClickListener(OnItemClickListener);
            mGridView.setOnItemLongClickListener(new ImageLongClickListener());
            curPosition = BOOKMARK_ID_FOLDERS_SELECTED;
        }
    };

    public void syncGallery(){
        ServerInterface.getImageDetailsRequestJson(
            Photobook.getMainActivity(),
            null, null,
            new Response.Listener<ArrayList<ImageJson>>() {
                @Override
                public void onResponse(ArrayList<ImageJson> response) {
                    HashMap<String, ImageJson> uriMap =
                            new HashMap<String, ImageJson>();
                    for (int i = 0; i < response.size(); i++)
                        if ((response.get(i).local_uri != null) &&
                                !response.get(i).local_uri.isEmpty() &&
                                response.get(i).status == ImageEntry.INT_SERVER_STATUS_SHARED) {
                            uriMap.put(response.get(i).local_uri.toLowerCase(), response.get(i));
                        }
                    for (int i = 0; i < mImageList.size(); i++)
                        if (uriMap.containsKey(mImageList.get(i).
                                getOrigUri().toLowerCase()) &&
                                (mImageList.get(i).getStatus() ==
                                        ImageEntry.INT_STATUS_DEFAULT) &&
                                (uriMap.get(mImageList.get(i).
                                        getOrigUri().toLowerCase()).status ==
                                        ImageEntry.INT_SERVER_STATUS_SHARED)) {
                            ImageJson remoteImage = uriMap.get(mImageList.get(i).
                                    getOrigUri().toLowerCase());
                            mImageList.get(i).setStatus(ImageEntry.
                                    INT_STATUS_SHARED);
                            mImageList.get(i).setServerId(remoteImage.image_id);
                            mImageList.get(i).setTitle(remoteImage.title);
                            Photobook.getImagesDataSource().saveImage(mImageList.
                                    get(i));
                        }
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                }
            }, null);
    }

    public void syncComments(){
        ServerInterface.getComments(
                null,
                true,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (mAdapter != null) mAdapter.notifyDataSetChanged();
                    }
                }, null);
    }

    public void refreshAdapter() {
        mAdapter.notifyDataSetChanged();
    }

    public void reloadGallery(final CallbackInterface onResponse) {
        new GalleryLoaderClass(null, null, new CallbackInterface() {
            public void onResponse(Object obj) {
                mImageList.clear();
                mImageList.addAll((ArrayList<ImageEntry>) obj);
                refreshGallery();
                if (onResponse != null) onResponse.onResponse(obj);
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshGallery() {
        if (curPosition == BOOKMARK_ID_GALLERY) {
            mAdapter.clear();
            mAdapter.addAll(mImageList);
            mAdapter.notifyDataSetChanged();
            mGridView.setOnItemClickListener(OnItemClickListener);
            mGridView.setOnItemLongClickListener(new ImageLongClickListener());
        } else if (curPosition == BOOKMARK_ID_FOLDERS) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            showBuckets();
        } else if (curPosition == BOOKMARK_ID_SHARES) {
            showSharedImages();
            mGridView.setOnItemClickListener(OnItemClickListener);
            mGridView.setOnItemLongClickListener(new ImageLongClickListener());
        }
    }

    public boolean onBackPressed (){
        if (curPosition == BOOKMARK_ID_FOLDERS_SELECTED) {
            mAdapter.clear();
            mAdapter.notifyDataSetChanged();
            showBuckets();
            curPosition = BOOKMARK_ID_FOLDERS;
            return true;
        } else
            return false;
    }



}
