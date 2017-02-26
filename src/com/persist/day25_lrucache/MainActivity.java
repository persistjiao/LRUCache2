package com.persist.day25_lrucache;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;

import com.persist.day25_helper.HttpClientHelper;
import com.persist.day25_helper.SDCardHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
/**
 * @ClassName:  MainActivity   
 * @Description:ͼƬ���������棨�ڴ�--�ļ�--���磩   * 
 * @date:   2016-5-27 ����2:02:33   
 *
 */
public class MainActivity extends Activity {
	
	private String url = "http://c.hiphotos.baidu.com/image/pic/item/78310a55b319ebc4856784ed8126cffc1e1716a2.jpg";

	private ImageView imageView;
	
	private LruCache<String, Bitmap> lruCache;
	private Map<String, SoftReference<Bitmap>> softMap = new LinkedHashMap<String, SoftReference<Bitmap>>();
	
	private ProgressDialog dialog;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				dialog.show();
				break;
			case 1:
				Bitmap bitmap = (Bitmap) msg.obj;
				imageView.setImageBitmap(bitmap);
				dialog.dismiss();
				break;
			case 2:
				Toast.makeText(MainActivity.this, "�����쳣��ͼƬ����ʧ��!", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		imageView = (ImageView) findViewById(R.id.imageView);
		dialog = new ProgressDialog(this);
		dialog.setTitle("��ʾ��");
		dialog.setMessage("����Ŭ��������...");
		
		//�õ������(�豸)�������app������ڴ� 32M
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		Log.i("TAG", "MaxMemory="+maxMemory);
		//��Ӧ�ó�����ܵ��ڴ�İ˷�֮һ��Ϊ����ʹ��
		lruCache = new MyLruCache(maxMemory/8);
	}

	//�����ť���ػ�����ʾ���ػ���ͼƬ
	public void click(View view){
		Bitmap bm = getBitmapFromCache(url);
		if (bm!=null) {
			imageView.setImageBitmap(bm);//�ȴӻ�����ȡ������ܴӻ����л�ȡͼƬ�Ͳ�����������
		}else {
			//��������
			new Thread(){
				public void run() {
					if (HttpClientHelper.isNetWorkConn(MainActivity.this)) {
						
						handler.sendEmptyMessage(0);//���Ϳ���Ϣ ��ʾ�Ի���
						
						byte[] data = HttpClientHelper.loadByteFromURL(url);
						//������ͼƬ���Զ��β�����ͼƬ����ѹ������
						
						if (data!=null && data.length!=0) {
							Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
							
							Message message = Message.obtain();
							message.obj=bitmap;
							message.what=1;
							handler.sendMessage(message);//���͸����߳� ����UI
							
							//��ͼƬ�洢��ǿ������
							lruCache.put(url, bitmap);
							Log.i("TAG", "�洢��ǿ�����гɹ�!");
							boolean flag = SDCardHelper.saveBitmapToSDCardPrivateCacheDir(bitmap, url.substring(url.lastIndexOf("/")+1), MainActivity.this);
							if (flag) {
								Log.i("TAG", " ͼƬ�洢��SD���ɹ�!");
							}else {
								Log.i("TAG", " ͼƬ�洢��SD��ʧ��!");
							}
						}
					}else {
						//�����쳣
						handler.sendEmptyMessage(2);
					}
					
				};
			}.start();
		}
	}

	
	public class MyLruCache extends LruCache<String, Bitmap>{

		public MyLruCache(int maxSize) {
			super(maxSize);
		}

		/**
		 * �Ƴ������е���Ŀ,��lrucache�е����ݱ��Ƴ�ʱ�ص��ķ���
		 * 4������:
		 * ��.evicted:�Ƿ��Ƴ�����Ŀ;
		 * ��.key:�洢�������ж����key;
		 * ��.oldValue:Ҫ�Ƴ���ֵ;
		 * ��.newValue:Ҫ������ֵ
		 */
		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			super.entryRemoved(evicted, key, oldValue, newValue);
			if (evicted) {
				//���Ҫ��bitmap��LruCache���Ƴ���,���ǿ����ڴ�ʱ��ֱ���ӵ�
				//���ǰ�Ҫ�ӵ������bitmap����ŵ���������.�� Android 2.3 (API Level 9)��ʼ��������������������ڻ��ճ��������û������õĶ���
				//3.0�Ժ�,GC��ǿ�ƻ�����,Ҳ���������ò�����,�����Ķ���������,Ҳ����û��,���������������õ�
				//Ŀ��:�б�û��ǿ! ������ڴ��˫���漼��
				SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(oldValue);
				softMap.put(key, softReference);
			}
		}

		//��ȡ��ǰ�������ݵĳߴ缴ÿ��Bitmap�Ĵ�С
		@Override
		protected int sizeOf(String key, Bitmap value) {
//			value.getWidth()*value.getHeight()*4;
			//ÿ�е��ֽ���*�߶�(�ж�����)
			return value.getRowBytes()*value.getHeight();
			
		}
		
	}
	
	/**
	 * �ӻ����л�ȡͼƬ
	 * @return
	 */
	public Bitmap getBitmapFromCache(String key){
		Bitmap bitmap = null;
		//1.�ȴ�ǿ�����л�ȡ
		bitmap = lruCache.get(key);
		if (bitmap!=null) {
			Log.i("TAG", "---ǿ�������ҵ�ͼƬ---");
			return bitmap;
		}else {
			//2.�������ü������������ö���
			SoftReference<Bitmap> mySoftReference = softMap.get(key);
			if (mySoftReference!=null) {
				
				//�������ö�����ȡ��Bitmap
				bitmap = mySoftReference.get();
				if (bitmap!=null) {
					// �Ѵ����������ҵ���ͼƬ�ŵ�ǿ������(�洢��ǿ������)
					lruCache.put(key, bitmap);
					// �����������Ƴ�
					softMap.remove(key);
					Log.i("TAG", "---���������ҵ�ͼƬ---");
					return bitmap;
				}
			}else {
				//3.��������Ҳû�У����ļ����棨SD��������
				//storage/sdcard/Android/����/cache/A%252520Photographer.jpg
				String imageName = SDCardHelper.getImageName(key);
				String filePath = SDCardHelper.getSDCardCachePath(MainActivity.this)+File.separator+imageName;
				File file = new File(filePath);
				if (file.exists()) {
					byte[] data  = SDCardHelper.loadFileFromSDCard(filePath);
					if (data!=null && data.length!=0) {
						bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						lruCache.put(key, bitmap);
						Log.i("TAG", "---SD�����ҵ�ͼƬ---");
						return bitmap;
					}
				}
				
			}
		}
		
		return null;
		
	}
}
