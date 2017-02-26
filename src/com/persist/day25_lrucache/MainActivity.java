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
 * @Description:图片的三级缓存（内存--文件--网络）   * 
 * @date:   2016-5-27 下午2:02:33   
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
				Toast.makeText(MainActivity.this, "网络异常，图片下载失败!", Toast.LENGTH_SHORT).show();
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
		dialog.setTitle("提示：");
		dialog.setMessage("正在努力加载中...");
		
		//得到虚拟机(设备)分配给该app的最大内存 32M
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		Log.i("TAG", "MaxMemory="+maxMemory);
		//将应用程序的总的内存的八分之一作为缓存使用
		lruCache = new MyLruCache(maxMemory/8);
	}

	//点击按钮下载或者显示本地缓存图片
	public void click(View view){
		Bitmap bm = getBitmapFromCache(url);
		if (bm!=null) {
			imageView.setImageBitmap(bm);//先从缓存中取，如果能从缓存中获取图片就不再网络下载
		}else {
			//网络下载
			new Thread(){
				public void run() {
					if (HttpClientHelper.isNetWorkConn(MainActivity.this)) {
						
						handler.sendEmptyMessage(0);//发送空消息 显示对话框
						
						byte[] data = HttpClientHelper.loadByteFromURL(url);
						//下载完图片可以二次采样对图片进行压缩处理
						
						if (data!=null && data.length!=0) {
							Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
							
							Message message = Message.obtain();
							message.obj=bitmap;
							message.what=1;
							handler.sendMessage(message);//发送给主线程 更新UI
							
							//将图片存储到强引用中
							lruCache.put(url, bitmap);
							Log.i("TAG", "存储到强引用中成功!");
							boolean flag = SDCardHelper.saveBitmapToSDCardPrivateCacheDir(bitmap, url.substring(url.lastIndexOf("/")+1), MainActivity.this);
							if (flag) {
								Log.i("TAG", " 图片存储到SD卡成功!");
							}else {
								Log.i("TAG", " 图片存储到SD卡失败!");
							}
						}
					}else {
						//网络异常
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
		 * 移除缓存中的条目,当lrucache中的数据被移除时回调的方法
		 * 4个参数:
		 * ①.evicted:是否移除该条目;
		 * ②.key:存储到缓存中对象的key;
		 * ③.oldValue:要移除的值;
		 * ④.newValue:要进来的值
		 */
		@Override
		protected void entryRemoved(boolean evicted, String key,
				Bitmap oldValue, Bitmap newValue) {
			super.entryRemoved(evicted, key, oldValue, newValue);
			if (evicted) {
				//如果要把bitmap从LruCache给移除掉,我们可以在此时不直接扔掉
				//而是把要扔掉的这个bitmap给存放到软引用中.从 Android 2.3 (API Level 9)开始，垃圾回收器会更倾向于回收持有软引用或弱引用的对象，
				//3.0以后,GC会强制回收它,也就是软引用不靠谱,里面存的东西可能有,也可能没有,但是我们用软引用的
				//目的:有比没有强! 这就是内存的双缓存技术
				SoftReference<Bitmap> softReference = new SoftReference<Bitmap>(oldValue);
				softMap.put(key, softReference);
			}
		}

		//获取当前缓存数据的尺寸即每个Bitmap的大小
		@Override
		protected int sizeOf(String key, Bitmap value) {
//			value.getWidth()*value.getHeight()*4;
			//每行的字节数*高度(有多少行)
			return value.getRowBytes()*value.getHeight();
			
		}
		
	}
	
	/**
	 * 从缓存中获取图片
	 * @return
	 */
	public Bitmap getBitmapFromCache(String key){
		Bitmap bitmap = null;
		//1.先从强引用中获取
		bitmap = lruCache.get(key);
		if (bitmap!=null) {
			Log.i("TAG", "---强引用中找到图片---");
			return bitmap;
		}else {
			//2.从软引用集合中找软引用对象
			SoftReference<Bitmap> mySoftReference = softMap.get(key);
			if (mySoftReference!=null) {
				
				//从软引用对象中取出Bitmap
				bitmap = mySoftReference.get();
				if (bitmap!=null) {
					// 把从软引用中找到的图片放到强引用中(存储到强引用中)
					lruCache.put(key, bitmap);
					// 从软引用中移除
					softMap.remove(key);
					Log.i("TAG", "---软引用中找到图片---");
					return bitmap;
				}
			}else {
				//3.软引用中也没有，从文件缓存（SD卡）中找
				//storage/sdcard/Android/包名/cache/A%252520Photographer.jpg
				String imageName = SDCardHelper.getImageName(key);
				String filePath = SDCardHelper.getSDCardCachePath(MainActivity.this)+File.separator+imageName;
				File file = new File(filePath);
				if (file.exists()) {
					byte[] data  = SDCardHelper.loadFileFromSDCard(filePath);
					if (data!=null && data.length!=0) {
						bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
						lruCache.put(key, bitmap);
						Log.i("TAG", "---SD卡中找到图片---");
						return bitmap;
					}
				}
				
			}
		}
		
		return null;
		
	}
}
