package com.freecoders.photobook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.etsy.android.grid.StaggeredGridView;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.network.ImageUploader;

import java.util.ArrayList;

@SuppressLint("NewApi") 
public class GalleryFragmentTab extends Fragment {

    private ArrayList<ImageEntry> mImageList;
    private ImageUploader mImageLoader;
    private GalleryAdapter mAdapter;

    public GalleryFragmentTab(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);
        StaggeredGridView gridView = (StaggeredGridView) rootView.findViewById(R.id.gridView);
        mImageList = Photobook.getImagesDataSource().getAllImages();
        mImageLoader = new ImageUploader();
        mAdapter = new GalleryAdapter(getActivity(), R.layout.item_gallery,
                mImageList);
        gridView.setAdapter(mAdapter);
        gridView.setOnItemClickListener(OnItemClickListener);
        setRetainInstance(true);
        return rootView;
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
                imgView.setImageURI(Uri.parse(image.getOrigUri()));
                dialog.setView(layout);
                final AlertDialog alertDialog = dialog.create();
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mImageList.get(pos).setTitle(editText.getText().toString());
                        mImageLoader.uploadImage(mImageList, pos, mAdapter);
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        }
    };
}
