/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;



/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageDownloader {
    private static final String LOG_TAG = "ImageDownloader";

    public enum Mode { NO_ASYNC_TASK, NO_DOWNLOADED_DRAWABLE, CORRECT }
    private Mode mode = Mode.NO_ASYNC_TASK;
	private String mDlPath;

	//*********************Ryan 20130327*************
    private int bookcase=0;//0:?çË≤ªË©¶Èñ±???1:?ëÁ??∏Ê???:Ê®ÇË?È§?    //*********************Ryan 20130327*************

    /**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void download(String url, ImageView imageView , Bitmap defBmp) {   	
        //resetPurgeTimer();
        Bitmap bitmap = getBitmapFromCache(url);
        
		if (bitmap == null) {
			String fileName = url.substring(url.lastIndexOf("/") + 1);
			File cover = new File(mDlPath + fileName);
			if (cover.exists()) {
				bitmap = loadBmpFromFile(mDlPath + fileName);

				if (null == bitmap) {
					forceDownload(url, imageView, defBmp);
				} else {

					cancelPotentialDownload(url, imageView);
					//we don't need to re-download thumbnail image, but we may need to update our database 
					//updateDb(url);
					addBitmapToCache(url, bitmap);
					imageView.setImageBitmap(bitmap);
				}
			} else {
				forceDownload(url, imageView, defBmp);
			}
		} else {
			cancelPotentialDownload(url, imageView);
			imageView.setImageBitmap(bitmap);
		}
    }
    
    private String str5 = null;
    private String str9 = null;
    public void setCursor( Cursor c ){
		str5 = c.getString(5);
		str9 = c.getString(9);
    }
    
    private void updateDb(String url ){
    	if ( null == str5 || null == str9){
    		return ;
    	}
    	String fileName = url.substring(url.lastIndexOf("/") + 1);
    	
		
		//if ( null == str9 || str9.length() <= 0 || str9.startsWith("ivi_nonepict") ){
			//mDb.updateByDeliveryId("freeBooks",str5, "coverpath" , mDlPath+fileName);
			//mDb.updateByDeliveryId(str5 , "cover" , url);
			
		 	//***********************Ryan 20130327***********************
	    	//‰∏çÂ??∏Ê?Ë¶ÅÂ?Áµ¶db‰∏çÂ??ÑÂ?
	    	switch (bookcase) {
			case 0:
				//mDb.updateByDeliveryId("freeBooks",str5, "coverpath" , mDlPath+fileName);
				break;
			case 1:
				//mDb.updateByDeliveryId("myBooks",str5, "coverpath" , mDlPath+fileName);
				break;
			case 2:
				//mDb.updateByDeliveryId("monthlyBooks",str5, "coverpath" , mDlPath+fileName);
				break;
			default:
				//mDb.updateByDeliveryId("freeBooks",str5, "coverpath" , mDlPath+fileName);
				break;
			}
	    	//***********************Ryan 20130327***********************
				//mDb.updateByDeliveryId("freeBooks",str5, "coverpath" , mDlPath+fileName);
				//mDb.updateByDeliveryId(str5 , "cover" , url);

			
			Log.d("BookDownloader" , "did:"+str5+ " coverPath:"+ mDlPath+fileName + " cover:"+url + " str9:"+str9 );
		//}
    }
    
    /**
     * Ë®≠Â?ÂæûÂì™?ãÊõ∏Ê´ÉÂëº?´imageDonwnloader
     * @param bookcase 0:?çË≤ªË©¶Èñ±???1:?ëÁ??∏Ê???:Ê®ÇË?È§?     */
    public void setBookcase(int bookcase){
    	this.bookcase=bookcase;
    }
    

    /*
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
       private void forceDownload(String url, ImageView view) {
          forceDownload(url, view, null);
       }
     */

    /**
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageView imageView , Bitmap defBmp) {
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (url == null) {
            imageView.setImageDrawable(new BitmapDrawable(defBmp));
            return;
        }
        
//        updateDb(url);

        if (cancelPotentialDownload(url, imageView)) {
            switch (mode) {
                case NO_ASYNC_TASK:
                    Bitmap bitmap = downloadBitmap(url);
                    addBitmapToCache(url, bitmap);
                    imageView.setImageBitmap(bitmap);
                    break;

                case NO_DOWNLOADED_DRAWABLE:
                    //imageView.setMinimumHeight(156);
                    BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
                    task.execute(url);
                    break;

                case CORRECT:
                    task = new BitmapDownloaderTask(imageView);
                    DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task, defBmp);
                    imageView.setImageDrawable(downloadedDrawable);
//                    imageView.setMinimumHeight(48);
                    
                    try{
                    	task.execute(url);
                    } catch (RejectedExecutionException e){
                    	e.printStackTrace();
                    	task.cancel(true);
                    }
                    break;
            }
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    Bitmap downloadBitmap(String url) {
//        final int IO_BUFFER_SIZE = 4 * 1024;

        // AndroidHttpClient is not allowed to be used from the main thread
        final HttpClient client = (mode == Mode.NO_ASYNC_TASK) ? new DefaultHttpClient() :
            AndroidHttpClient.newInstance("Android");
//        try {
//			//url = UriUtils.encodeHttpUrl(url ,"US-ASCII");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        HttpGet getRequest = null;

        try {
        	getRequest = new HttpGet(url);
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("ImageDownloader", "Error " + statusCode +
                        " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    Bitmap b = BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                    if ( null != b ){
                    	saveBitmap(b , url);
                    }
                    
                    // return BitmapFactory.decodeStream(inputStream);
                    // Bug on slow connections, fixed in future release.
                    return b;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (IOException e) {
        	if (getRequest != null)
        		getRequest.abort();
            Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
        	if (getRequest != null)
        		getRequest.abort();
            Log.w(LOG_TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
        	if (getRequest != null)
        		getRequest.abort();
            Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
        } finally {
            if ((client instanceof AndroidHttpClient)) {
                ((AndroidHttpClient) client).close();
            }
        }
        return null;
    }
    
    private void saveBitmap(Bitmap bm , String url){

		String fileName = url.substring(url.lastIndexOf("/") + 1);
		FileOutputStream fos;
		try {
			fileName = Uri.decode(fileName);
			fos = new FileOutputStream(mDlPath+fileName);
			
			bm.compress(Bitmap.CompressFormat.JPEG, 85, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}    	
    }    

    /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     */
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            return downloadBitmap(url);
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            
            if ( null == bitmap){
            	return ;
            }

            addBitmapToCache(url, bitmap);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                // Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)
                if ( (this == bitmapDownloaderTask) || (mode != Mode.CORRECT)) {
                	imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    /**
     * A fake Drawable that will be attached to the imageView while the download is in progress.
     *
     * <p>Contains a reference to the actual download task, so that a download task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the download finish order.</p>
     */
    static class DownloadedDrawable extends BitmapDrawable/*ColorDrawable*/ {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask , Bitmap defBmp) {
            //super(Color.BLACK);
        	super(defBmp);
            bitmapDownloaderTaskReference =
                new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        clearCache();
    }
    
    public void setDlPath(String path){
    	mDlPath = path ;
    }
    


    public static int computeSampleSize(BitmapFactory.Options options,
	        int minSideLength, int maxNumOfPixels) {
	    int initialSize = computeInitialSampleSize(options, minSideLength,
	            maxNumOfPixels);
	 
	    int roundedSize;
	    if (initialSize <= 8) {
	        roundedSize = 1;
	        while (roundedSize < initialSize) {
	            roundedSize <<= 1;
	        }
	    } else {
	        roundedSize = (initialSize + 7) / 8 * 8;
	    }
	 
	    return roundedSize;
	}
	 
	private static int computeInitialSampleSize(BitmapFactory.Options options,
	        int minSideLength, int maxNumOfPixels) {
	    double w = options.outWidth;
	    double h = options.outHeight;
	 
	    int lowerBound = (maxNumOfPixels == -1) ? 1 :
	            (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
	    int upperBound = (minSideLength == -1) ? 128 :
	            (int) Math.min(Math.floor(w / minSideLength),
	            Math.floor(h / minSideLength));
	 
	    if (upperBound < lowerBound) {
	        // return the larger one when there is no overlapping zone.
	        return lowerBound;
	    }
	 
	    if ((maxNumOfPixels == -1) &&
	            (minSideLength == -1)) {
	        return 1;
	    } else if (minSideLength == -1) {
	        return lowerBound;
	    } else {
	        return upperBound;
	    }
	}       
    
    private Bitmap loadBmpFromFile(String file){
    	
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
//		BitmapFactory.decodeFile(file, opts);

		opts.inSampleSize = computeSampleSize(opts, -1, 200 * 200);
		opts.inJustDecodeBounds = false;
		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeFile(file, opts);
		} catch (OutOfMemoryError err) {
		}   	
    	return bmp;
    }
    
    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int HARD_CACHE_CAPACITY = 40;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final static HashMap<String, Bitmap> sHardBitmapCache =
        new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new WeakReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, WeakReference<Bitmap>> sSoftBitmapCache =
        new ConcurrentHashMap<String, WeakReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     */
    public static void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (sHardBitmapCache) {
                sHardBitmapCache.put(url, bitmap);
            }
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    public static Bitmap getBitmapFromCache(String url) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last

                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        WeakReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }
        return null;
    }
 
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
}
