package com.freecoders.photobook.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public final class ImageUtils {

	public static final int SMALL_WIDTH = 300;
	
	public static final int MEDIUM_WIDTH = 600;
	
	public static final int LARGE_WIDTH = 1200;
	
	public static final Bitmap resizeBitmap(Bitmap b, int newWidth) {
		int width = b.getWidth();
		int height = b.getHeight();
		
		float scale = (float) newWidth / width;

		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		
		Bitmap resizedBitmap = Bitmap.createBitmap(b, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}


}
