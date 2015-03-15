package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.network.ImageUploader;
import com.freecoders.photobook.utils.FileUtils;
import com.freecoders.photobook.utils.ImageUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

@SuppressLint("NewApi") 
public class GalleryFragmentTab extends Fragment {

    private ArrayList<ImageEntry> mImageList;
    private ImageUploader mImageLoader;
    private GalleryAdapter mAdapter;

    public GalleryFragmentTab(){
        mImageLoader = new ImageUploader();
        mImageList = new ArrayList<ImageEntry>();
        new GalleryLoaderClass().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        StaggeredGridView gridView = (StaggeredGridView) rootView.findViewById(R.id.gridView);
        mAdapter = new GalleryAdapter(getActivity(), R.layout.item_gallery,
                mImageList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(OnItemClickListener);
        setRetainInstance(true);
        return rootView;
    }

    public class GalleryLoaderClass extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            ArrayList<ImageEntry> imgList = Photobook.getImagesDataSource().getAllImages();
            mImageList.addAll(imgList);
            Photobook.getMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdapter != null) mAdapter.notifyDataSetChanged();
                }
            });
            return true;
        }
    }

    AdapterView.OnItemClickListener OnItemClickListener
            = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            ImageEntry image = mImageList.get(position);
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
                imgView.setImageBitmap(b);
                //imgView.setImageURI(Uri.parse(image.getOrigUri()));

                dialog.setView(layout);
                final AlertDialog alertDialog = dialog.create();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageList.get(pos).setTitle(editText.getText().toString());
                        String strOrigUri = mImageList.get(pos).getOrigUri();
                        String strThumbUri = mImageList.get(pos).getThumbUri();
                        String strDestOrigName = mImageList.get(pos).getMediaStoreID();
                        String strDestThumbName = mImageList.get(pos).getMediaStoreID() +
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
                            mImageList.get(pos).setOrigUri(destOrigFile.toString());
                            Log.d(Constants.LOG_TAG, "Saved local image to " +
                                    destOrigFile.toString());
                        }
                        if (FileUtils.copyFileFromUri(new File(strThumbUri), destThumbFile)) {
                            mImageList.get(pos).setThumbUri(destThumbFile.toString());
                            Log.d(Constants.LOG_TAG, "Saved local thumbnail to " +
                                    destThumbFile.toString());
                        }
                        Photobook.getImagesDataSource().saveImage(mImageList.get(pos));
                        mImageLoader.uploadImage(mImageList, pos, mAdapter);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                alertDialog.show();
            } else if (image.getStatus() == ImageEntry.INT_STATUS_SHARED) {
                Intent mIntent = new Intent(Photobook.getMainActivity(), ImageDetailsActivity.class);
                Bundle b = new Bundle();
                b.putBoolean(Photobook.intentExtraImageDetailsSource, true);
                mIntent.putExtras(b);
                Photobook.setGalleryImageDetails(image);
                startActivity(mIntent);
            }
        }
    };

}
