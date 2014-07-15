/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.fbbackup;

import java.io.File;

import util.DownloadSinglePicTask;
import util.ImageCache;
import util.ImageFetcher;
import util.Utils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ImageDetailActivity extends FragmentActivity implements
		OnClickListener {
	private static final String IMAGE_CACHE_DIR = "images";
	public static final String EXTRA_IMAGE = "extra_image";

	private ImagePagerAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	private ViewPager mPager;

	private String photoArray[];

	private String albumName;

	private String name;

	private Button btn_share;
	private ImageButton btn_download_photo;
	private TextView tx_album_name;

	// �ΨӰO���I���ĴX��pos
	private int img_pos = 0;

	private String clipBoardText = "";
	private String extStorageDirectory;

	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.image_detail_pager);

		extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		// Fetch screen height and width, to use as our max size when loading
		// images as this
		// activity runs full screen
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		photoArray = getIntent().getExtras().getStringArray("photo");
		albumName = getIntent().getExtras().getString("albumName");
		img_pos = getIntent().getExtras().getInt("albumPosition");
		name = getIntent().getExtras().getString("userName");

		setView();
		setListener();

		// For this sample we'll use half of the longest width to resize our
		// images. As the
		// image scaling ensures the image is larger than this, we should be
		// left with a
		// resolution that is appropriate for both portrait and landscape. For
		// best image quality
		// we shouldn't divide by 2, but this will use more memory and require a
		// larger memory
		// cache.
		final int longest = (height > width ? height : width) /2;
		
		Log.w("testSize","longest:"+longest);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				this, IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(this, longest);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		mImageFetcher.setImageFadeIn(false);

		// Set up ViewPager and backing adapter
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(),
				photoArray.length);
		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		mPager.setPageMargin((int) getResources().getDimension(
				R.dimen.image_detail_pager_margin));
		mPager.setOffscreenPageLimit(2);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());

		// Set up activity to go full screen
		// getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

		// Enable some additional newer visibility and ActionBar features to
		// create a more
		// immersive photo viewing experience
//		if (Utils.hasHoneycomb()) {
//			final ActionBar actionBar = getActionBar();
//
//			// Hide title text and set home as up
//			actionBar.setDisplayShowTitleEnabled(false);
//			actionBar.setDisplayHomeAsUpEnabled(true);
//
//			// Hide and show the ActionBar as the visibility changes
//			mPager.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//				@Override
//				public void onSystemUiVisibilityChange(int vis) {
//					if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
//						actionBar.hide();
//					} else {
//						actionBar.show();
//					}
//				}
//			});
//
//			// Start low profile mode and hide ActionBar
//			mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
//			actionBar.hide();
//		}

		// Set the current item based on the extra passed in to this activity
		final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
		if (extraCurrentItem != -1) {
			mPager.setCurrentItem(extraCurrentItem);
		}
	}

	private void setView() {
		btn_download_photo = (ImageButton) findViewById(R.id.btn_download_photo);
		btn_share = (Button) findViewById(R.id.btn_share);
		tx_album_name = (TextView) findViewById(R.id.tx_album_name);
		tx_album_name.setText(albumName);

	}

	private void setListener() {
		btn_share.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Gets a handle to the clipboard service.
				ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(clipBoardText);

				PackageManager pm = getPackageManager();
				Intent intent = pm
						.getLaunchIntentForPackage("jp.naver.line.android");
				
				File file=new File(extStorageDirectory + "/DCIM/FBBackup/test.jpg");
				
				Uri outputFileUri = Uri.fromFile(file);
				

				String[] path={extStorageDirectory + "/DCIM/FBBackup/test.jpg"};
//				MediaScannerConnection.scanFile(this, path, null,  
//						new MediaScannerConnection.OnScanCompletedListener() {
//				      public void onScanCompleted(String path, Uri uri) {
//				          Log.i("ExternalStorage", "Scanned " + path + ":");
//				          Log.i("ExternalStorage", "-> uri=" + uri);
//				      }
//				});

				MediaScannerConnection.scanFile(getApplicationContext(), path, null, null);
				
				
				Log.w("RyanWang",outputFileUri.getPath());
				
				intent.setAction(Intent.ACTION_SEND);
				intent.putExtra(Intent.EXTRA_STREAM, outputFileUri);
				intent.setType("image/jpeg");
				
				startActivity(intent);

			}

		});

		btn_download_photo.setOnClickListener(new ImageButton.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				DownloadSinglePicTask download = new DownloadSinglePicTask();

				download.setContext(ImageDetailActivity.this);

				download.setAlbunPhotoUrl(clipBoardText);

				Log.w("downloadpic", "ImageDetail albumName:" + albumName);

				download.setPath(extStorageDirectory + "/DCIM/FBBackup/" + name
						+ "/" + Utils.getDirName(albumName) + "/");

				download.execute("test");
			}

		});
	}

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	public class MyOnPageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageSelected(int position) {

			ImageDetailFragment.setUrl(photoArray[position]);
			clipBoardText = photoArray[position];
			Log.w("pos", "pageListener:" + position);

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case android.R.id.home:
	// NavUtils.navigateUpFromSameTask(this);
	// return true;
	// case R.id.clear_cache:
	// mImageFetcher.clearCache();
	// Toast.makeText(
	// this, R.string.clear_cache_complete_toast,Toast.LENGTH_SHORT).show();
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }
	//
	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.main_menu, menu);
	// return true;
	// }

	/**
	 * Called by the ViewPager child fragments to load images via the one
	 * ImageFetcher
	 */
	public ImageFetcher getImageFetcher() {
		return mImageFetcher;
	}

	/**
	 * The main adapter that backs the ViewPager. A subclass of
	 * FragmentStatePagerAdapter as there could be a large number of items in
	 * the ViewPager and we don't want to retain them all in memory at once but
	 * create/destroy them on the fly.
	 */
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		private final int mSize;

		public ImagePagerAdapter(FragmentManager fm, int size) {
			super(fm);
			mSize = size;
			ImageDetailFragment.setAlbumName(albumName);
			ImageDetailFragment.setUrl(photoArray[img_pos]);
			clipBoardText = photoArray[img_pos];
		}

		@Override
		public int getCount() {
			return mSize;
		}

		@Override
		public Fragment getItem(int position) {
			// ImageDetailFragment.setUrl(photoArray[position]);
			Log.w("pos", "getItem:" + position);
			// clipBoardText=photoArray[position];
			return ImageDetailFragment.newInstance(photoArray[position]);
		}
	}

	/**
	 * Set on the ImageView in the ViewPager children fragments, to
	 * enable/disable low profile mode when the ImageView is touched.
	 */
	@TargetApi(11)
	@Override
	public void onClick(View v) {
		final int vis = mPager.getSystemUiVisibility();
		if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
			mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
		} else {
			mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}

}
