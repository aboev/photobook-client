package com.freecoders.photobook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * @author Andrei Alikov andrei.alikov@gmail.com
 */
public class ImageDialogFragment extends DialogFragment {
    private ImageMenuHandler imageMenuHandler;

    public void setImageMenuHandler(ImageMenuHandler imageMenuHandler) {
        this.imageMenuHandler = imageMenuHandler;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.image).setItems(R.array.image_menu, new ImageMenuListener(imageMenuHandler));
        return builder.create();
    }

    private static class ImageMenuListener implements DialogInterface.OnClickListener {
        public static final int UNSHARE_ITEM_INDEX = 0;

        private final ImageMenuHandler imageMenuHandler;

        private ImageMenuListener(ImageMenuHandler imageMenuHandler) {
            this.imageMenuHandler = imageMenuHandler;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (imageMenuHandler == null) {
                return;
            }
            switch (which) {
                case UNSHARE_ITEM_INDEX:
                    imageMenuHandler.onUnShareImage();
                    break;
            }
        }
    }

    public interface ImageMenuHandler {
        void onUnShareImage();
    }
}
