package com.persist.day25_helper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class SDCardHelper {

	// �ж�SDCard�Ƿ����
	public static boolean isSDCardMounted() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	// ��ȡSDCard�ĸ�Ŀ¼·��
	public static String getSDCardBasePath() {
		if (isSDCardMounted()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		} else {
			return null;
		}
	}

	// ��ȡSDCard�������ռ��С
	@SuppressLint("NewApi")
	public static long getSDCardTotalSize() {
		long size = 0;
		if (isSDCardMounted()) {
			StatFs statFs = new StatFs(getSDCardBasePath());
			if (Build.VERSION.SDK_INT >= 18) {
				size = statFs.getTotalBytes();
			} else {
				size = statFs.getBlockCount() * statFs.getBlockSize();
			}
			return size / 1024 / 1024;
		} else {
			return 0;
		}
	}

	// ��ȡSDCard�Ŀ��ÿռ��С
	@SuppressLint("NewApi")
	public static long getSDCardAvailableSize() {
		long size = 0;
		if (isSDCardMounted()) {
			StatFs statFs = new StatFs(getSDCardBasePath());
			if (Build.VERSION.SDK_INT >= 18) {
				size = statFs.getAvailableBytes();
			} else {
				size = statFs.getAvailableBlocks() * statFs.getBlockSize();
			}
			return size / 1024 / 1024;
		} else {
			return 0;
		}
	}

	// ��ȡSDCard��ʣ��ռ��С
	@SuppressLint("NewApi")
	public static long getSDCardFreeSize() {
		long size = 0;
		if (isSDCardMounted()) {
			StatFs statFs = new StatFs(getSDCardBasePath());
			if (Build.VERSION.SDK_INT >= 18) {
				size = statFs.getFreeBytes();
			} else {
				size = statFs.getFreeBlocks() * statFs.getBlockSize();
			}
			return size / 1024 / 1024;
		} else {
			return 0;
		}
	}

	// ����byte[]�ļ���SDCard��ָ������Ŀ¼
	public static boolean saveFileToSDCardPublicDir(byte[] data, String type,
			String fileName) {
		if (isSDCardMounted()) {
			BufferedOutputStream bos = null;
			File file = Environment.getExternalStoragePublicDirectory(type);

			try {
				bos = new BufferedOutputStream(new FileOutputStream(new File(
						file, fileName)));
				bos.write(data);
				bos.flush();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			} finally {
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			return false;
		}
	}

	// ����byte[]�ļ���SDCard���Զ���Ŀ¼
	public static boolean saveFileToSDCardCustomDir(byte[] data, String dir,
			String fileName) {
		if (isSDCardMounted()) {
			BufferedOutputStream bos = null;
			File file = new File(getSDCardBasePath() + File.separator + dir);
			if (!file.exists()) {
				file.mkdirs();// �ݹ鴴����Ŀ¼
			}
			try {
				bos = new BufferedOutputStream(new FileOutputStream(new File(
						file, fileName)));
				bos.write(data, 0, data.length);
				bos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	// ����byte[]�ļ���SDCard��ָ��˽��FilesĿ¼
	public static boolean saveFileToSDCardPrivateDir(byte[] data, String type,
			String fileName, Context context) {
		if (isSDCardMounted()) {
			BufferedOutputStream bos = null;
			// ��ȡ˽��FilesĿ¼
			File file = context.getExternalFilesDir(type);
			try {
				bos = new BufferedOutputStream(new FileOutputStream(new File(
						file, fileName)));
				bos.write(data, 0, data.length);
				bos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	// ����byte[]�ļ���SDCard��˽��CacheĿ¼
	public static boolean saveFileToSDCardPrivateCacheDir(byte[] data,
			String fileName, Context context) {
		if (isSDCardMounted()) {
			BufferedOutputStream bos = null;
			// ��ȡ˽�е�Cache����Ŀ¼
			File file = context.getExternalCacheDir();
			Log.i("SDCardHelper", "==" + file);
			try {
				bos = new BufferedOutputStream(new FileOutputStream(new File(
						file, fileName)));
				bos.write(data, 0, data.length);
				bos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	// ����bitmapͼƬ��SDCard��˽��CacheĿ¼
	public static boolean saveBitmapToSDCardPrivateCacheDir(Bitmap bitmap,
			String fileName, Context context) {
		if (isSDCardMounted()) {
			BufferedOutputStream bos = null;
			// ��ȡ˽�е�Cache����Ŀ¼
			File file = context.getExternalCacheDir();
			try {
				bos = new BufferedOutputStream(new FileOutputStream(new File(
						file, fileName)));
				if (fileName != null
						&& (fileName.contains(".png") || fileName
								.contains(".PNG"))) {
					bitmap.compress(Bitmap.CompressFormat.PNG, 90, bos);
				} else {
					bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
				}
				bos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return true;
		} else {
			return false;
		}
	}

	// ��SDCard��Ѱ��ָ��Ŀ¼�µ��ļ�������byte[]
	public static byte[] loadFileFromSDCard(String filePath) {
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		File file = new File(filePath);
		if (file.exists()) {
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				byte[] buffer = new byte[1024 * 8];
				int c = 0;
				while ((c = (bis.read(buffer))) != -1) {
					baos.write(buffer, 0, c);
					baos.flush();
				}
				return baos.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bis != null) {
					try {
						bis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (baos != null) {
					try {
						baos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	// ��ȡSDCard˽�е�CacheĿ¼
	public static String getSDCardCachePath(Context context) {
		return context.getExternalCacheDir().getAbsolutePath();
	}

	// ��ȡSDCard˽�е�FilesĿ¼
	public static String getSDCardFilePath(Context context, String type) {
		return context.getExternalFilesDir(type).getAbsolutePath();
	}

	// ��sdcard��ɾ���ļ�
	public static boolean removeFileFromSDCard(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			try {
				file.delete();
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public static String getImageName(String url) {
		String imageName = "";
		if (url != null) {
			imageName = url.substring(url.lastIndexOf("/") + 1);
		}
		return imageName;
	}
}
