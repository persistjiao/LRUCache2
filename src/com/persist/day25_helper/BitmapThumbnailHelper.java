package com.persist.day25_helper;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

public class BitmapThumbnailHelper {

	/**
	 * ��ͼƬ���ж��β�������������ͼ�����ü��ع���ͼƬ�����ڴ����
	 */
	public static Bitmap createThumbnail(byte[] data, int newWidth,
			int newHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(data, 0, data.length, options);
		int oldWidth = options.outWidth;
		int oldHeight = options.outHeight;

		// Log.i("Helper", "--->oldWidth:" + oldWidth);
		// Log.i("Helper", "--->oldHeight:" + oldHeight);

		int ratioWidth = 0;
		int ratioHeight = 0;

		if (newWidth != 0 && newHeight == 0) {
			ratioWidth = oldWidth / newWidth;
			options.inSampleSize = ratioWidth;
			// Log.i("Helper", "--->ratioWidth:" + ratioWidth);

		} else if (newWidth == 0 && newHeight != 0) {
			ratioHeight = oldHeight / newHeight;
			options.inSampleSize = ratioHeight;
		} else {
			ratioHeight = oldHeight / newHeight;
			ratioWidth = oldWidth / newWidth;
			options.inSampleSize = ratioHeight > ratioWidth ? ratioHeight
					: ratioWidth;
		}
		options.inPreferredConfig = Config.ALPHA_8;
		options.inJustDecodeBounds = false;
		Bitmap bm = BitmapFactory
				.decodeByteArray(data, 0, data.length, options);
		return bm;
	}

	public static Bitmap createThumbnail(String pathName, int newWidth,
			int newHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pathName, options);
		int oldWidth = options.outWidth;
		int oldHeight = options.outHeight;

		int ratioWidth = 0;
		int ratioHeight = 0;

		if (newWidth != 0 && newHeight == 0) {
			ratioWidth = oldWidth / newWidth;
			options.inSampleSize = ratioWidth;
		} else if (newWidth == 0 && newHeight != 0) {
			ratioHeight = oldHeight / newHeight;
			options.inSampleSize = ratioHeight;
		} else {
			ratioHeight = oldHeight / newHeight;
			ratioWidth = oldWidth / newWidth;
			options.inSampleSize = ratioHeight > ratioWidth ? ratioHeight
					: ratioWidth;
		}
		options.inPreferredConfig = Config.ALPHA_8;
		options.inJustDecodeBounds = false;
		Bitmap bm = BitmapFactory.decodeFile(pathName, options);
		return bm;
	}

	// ��ȡ��Ƶ�ļ��ĵ���֡��Ϊ����
	public static Bitmap createVideoThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			bitmap = retriever.getFrameAtTime();
		} catch (Exception ex) {
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
			}
		}
		return bitmap;
	}

	// ��ȡ�����ļ������õ�ר��ͼƬ
	public static Bitmap createAlbumThumbnail(String filePath) {
		Bitmap bitmap = null;
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(filePath);
			byte[] art = retriever.getEmbeddedPicture();
			bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
		} catch (Exception ex) {
		} finally {
			try {
				retriever.release();
			} catch (RuntimeException ex) {
			}
		}
		return bitmap;
	}
}
