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

import java.util.ArrayList;
import java.util.HashMap;

import util.DownloadList;
import util.ImageCache.ImageCacheParams;
import util.ImageFetcher;
import util.Utils;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.technotalkative.loadwebimage.imageutils.ImageLoader;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight
 * forward GridView implementation with the key addition being the ImageWorker
 * class w/ImageCache to load children asynchronously, keeping the UI nice and
 * smooth and caching thumbnails for quick retrieval. The cache is retained over
 * configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class ImageGridFragment extends Fragment implements
		AdapterView.OnItemClickListener {
	private static final String TAG = "ImageGridFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	private ImageLoader imageLoader;

	private String photoArray[];
	private String albumName;

	private ImageButton btn_download_photo;
	private CheckBox cb_all;
	private ImageView iv_cache_img;

	private HashMap<Integer, Boolean> isSelected;

	private String extStorageDirectory;

	private String name;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public ImageGridFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

		extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		photoArray = getArguments().getStringArray("albumPhoto");
		albumName = getArguments().getString("albumName");

		name = getArguments().getString("userName");

		for (int i = 0; i < photoArray.length; i++) {
			Log.w("imagetest", photoArray[i]);
		}

		mAdapter = new ImageAdapter(getActivity());
		imageLoader=new ImageLoader(getActivity());
		

		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(),
				cacheParams);

	}

	public void imageRun() {

		if (photoArray.length > 3) {

			ArrayList<String> t1 = new ArrayList<String>();
			ArrayList<String> t2 = new ArrayList<String>();
			ArrayList<String> t3 = new ArrayList<String>();

			for (int i = 0; i < photoArray.length; i++) {
				if (i %3==0)
					t1.add(photoArray[i]);
				else if(i%3==1)
					t2.add(photoArray[i]);
				else
					t3.add(photoArray[i]);
			}

			final String[] photo1 = new String[t1.size()];
			t1.toArray(photo1);

			final String[] photo2 = new String[t2.size()];
			t2.toArray(photo2);
			
			final String[] photo3 = new String[t3.size()];
			t3.toArray(photo3);

			new Thread() {

				@Override
				public void run() {

					for (String photo : photo1) {
						Log.w("RyanWang", "photo1 download:" + photo);
						mImageFetcher.processBitmap(photo);
					}

				}

			}.start();

			new Thread() {

				@Override
				public void run() {

					for (String photo : photo2) {
						Log.w("RyanWang", "photo2 download:" + photo);
						mImageFetcher.processBitmap(photo);
					}

				}

			}.start();
			
			new Thread() {

				@Override
				public void run() {

					for (String photo : photo3) {
						Log.w("RyanWang", "photo3 download:" + photo);
						mImageFetcher.processBitmap(photo);
					}

				}

			}.start();

		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.album_photo_grid, container,
				false);
		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);

		TextView tx_user_name = (TextView) v.findViewById(R.id.tx_album_name);

		tx_user_name.setText(albumName);

		cb_all = (CheckBox) v.findViewById(R.id.cb_all_photo);

		cb_all.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton v, boolean choose) {
				// TODO Auto-generated method stub

				if (choose) {

					// Toast.makeText(getActivity(), "choose:" + choose,
					// Toast.LENGTH_SHORT).show();

				} else {
					// Toast.makeText(getActivity(), "choose:" + choose,
					// Toast.LENGTH_SHORT).show();
				}
				setSelectedData(choose);

				mAdapter.notifyDataSetChanged();

			}

		});

		btn_download_photo = (ImageButton) v
				.findViewById(R.id.btn_download_photo);

		btn_download_photo
				.setOnClickListener(new ImageButton.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						String dir = Utils.getDirName(albumName);

						int select = 0;

						// ��ܦ��Q��쪺array�U��
						for (int i = 0; i < isSelected.size(); i++) {

							if (isSelected.get(i)) {
								DownloadList.downloadPhotoAlbumQueue.add(dir);
								DownloadList.downloadPhotoQueue
										.add(photoArray[i]);
								DownloadList.downloadUserNameQueue.add(name);

								select++;
							}

						}

						DownloadList.setAddNumber(select);

						DownloadList.setUserName(name);

						MyFriendFragmentActivity.downloadHandler
								.sendMessage(MyFriendFragmentActivity.downloadHandler
										.obtainMessage(
												MyFriendFragmentActivity.DOWNLOAD_PRB,
												0, 0));

					}

				});

		isSelected = new HashMap<Integer, Boolean>();

		initSelectedData();

		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					mImageFetcher.setPauseWork(true);
				} else {
					mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		// This listener is used to get the final width of the GridView and then
		// calculate the
		// number of columns and the width of each column. The width of each
		// column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used
		// to set the height
		// of each view so we get nice square thumbnails.
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (mAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math.floor(mGridView
									.getWidth()
									/ (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth = (mGridView.getWidth() / numColumns)
										- mImageThumbSpacing;
								mAdapter.setNumColumns(numColumns);
								mAdapter.setItemHeight(columnWidth);
								if (BuildConfig.DEBUG) {
									Log.d(TAG,
											"onCreateView - numColumns set to "
													+ numColumns);
								}
							}
						}
					}
				});
		//imageRun();
		return v;
	}

	private void initSelectedData() {
		Log.w("initSelectedData", "initSelectedData");
		for (int i = 0; i < photoArray.length; i++) {
			isSelected.put(i, false);
		}
	}

	private void setSelectedData(boolean selected) {
		for (int i = 0; i < photoArray.length; i++) {
			isSelected.put(i, selected);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// photoArray = getArguments().getStringArray("photo");
		// albumName = getArguments().getString("albumName");

		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@TargetApi(16)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		final Intent i = new Intent(getActivity(), ImageDetailActivity.class);

		Bundle bundle = new Bundle();

		bundle.putStringArray("photo", photoArray);

		bundle.putInt("albumPosition", position);

		bundle.putString("albumName", albumName);

		bundle.putString("userName", name);

		i.putExtras(bundle);

		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
		if (Utils.hasJellyBean()) {
			// makeThumbnailScaleUpAnimation() looks kind of ugly here as the
			// loading spinner may
			// show plus the thumbnail image in GridView is cropped. so using
			// makeScaleUpAnimation() instead.
			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v,
					0, 0, v.getWidth(), v.getHeight());
			getActivity().startActivity(i, options.toBundle());
		} else {
			startActivity(i);
		}
	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	// inflater.inflate(R.menu.main_menu, menu);
	// }
	//
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.clear_cache:
	// mImageFetcher.clearCache();
	// Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
	// Toast.LENGTH_SHORT).show();
	// return true;
	// }
	// return super.onOptionsItemSelected(item);
	// }

	/**
	 * The main adapter that backs the GridView. This is fairly standard except
	 * the number of columns in the GridView is used to create a fake top row of
	 * empty views as we use a transparent ActionBar and don't want the real top
	 * row of images to start off covered by it.
	 */
	private class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private int mActionBarHeight = 0;
		private int num = 0;
		private RelativeLayout.LayoutParams mImageViewLayoutParams;
		private LayoutInflater mInflater;

		public ImageAdapter(Context context) {
			super();
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			// Calculate ActionBar height
			TypedValue tv = new TypedValue();
			if (context.getTheme().resolveAttribute(
					android.R.attr.actionBarSize, tv, true)) {
				mActionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, context.getResources().getDisplayMetrics());
			}
		}

		@Override
		public int getCount() {
			// Size + number of columns for top empty row

			return photoArray.length;
		}

		@Override
		public Object getItem(int position) {

			return photoArray[position];

		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup container) {

			final ViewHolder holder;
			num++;

			if (num > 30) {
				num = 0;
				mImageFetcher.flushCache();
				System.gc();
			}

			// Now handle the main ImageView thumbnails
			// ImageView imageView;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.photo_grid_item,
						container, false);

				holder = new ViewHolder();

				holder.iv_image = (ImageView) convertView
						.findViewById(R.id.iv_image);

				holder.iv_image.setScaleType(ImageView.ScaleType.CENTER_CROP);
				holder.iv_image.setLayoutParams(mImageViewLayoutParams);

				holder.cb_choose = (CheckBox) convertView
						.findViewById(R.id.cb_choose);

				holder.cb_choose
						.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(CompoundButton v,
									boolean isChecked) {
								// TODO Auto-generated method stub

								int getPosition = (Integer) v.getTag();
								Log.w("check", "check position:" + getPosition
										+ " ,isChecked:" + isChecked);
								isSelected.put(getPosition, isChecked);
							}

						});

				convertView.setTag(holder);

				// convertView = new View(mContext);

				// if it's not recycled, instantiate and initialize
				// imageView = new RecyclingImageView(mContext);
				// imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				// imageView.setLayoutParams(mImageViewLayoutParams);
				Log.w("ViewHolder", "new one:" + position);

			} else { // Otherwise re-use the converted view
				// imageView = (ImageView) convertView;

				holder = (ViewHolder) convertView.getTag();

				Log.w("ViewHolder", "reuse:" + position);
			}

			Log.w("ViewHolder", "mNumColumns:" + mNumColumns);

			Log.w("ViewHolder", "mItemHeight:" + mItemHeight);

			Log.w("ViewHolder", "mActionBarHeight:" + mActionBarHeight);

			// convertView.setLayoutParams(new AbsListView.LayoutParams(
			// ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));

			// Check the height matches our calculated column width
			if (holder.iv_image.getLayoutParams().height != mItemHeight) {
				holder.iv_image.setLayoutParams(mImageViewLayoutParams);
			}

			//mImageFetcher.loadImage(photoArray[position], holder.iv_image);
			
			imageLoader.DisplayImage(photoArray[position], holder.iv_image);

			// Finally load the image asynchronously into the ImageView, this
			// also takes care of
			// setting a placeholder image while the background thread runs

			holder.cb_choose.setTag(position);
			holder.cb_choose.setChecked(isSelected.get(position));

			return convertView;
		}

		/**
		 * Sets the item height. Useful for when we know the column width so the
		 * height can be set to match.
		 * 
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, mItemHeight);
			mImageFetcher.setImageSize(height);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}

		public class ViewHolder {
			private ImageView iv_image;
			private CheckBox cb_choose;
		}
	}
}
