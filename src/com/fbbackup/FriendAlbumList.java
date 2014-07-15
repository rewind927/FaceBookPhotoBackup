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

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import util.DownloadList;
import util.DownloadTask;
import util.ImageCache.ImageCacheParams;
import util.ImageFetcher;
import util.Utils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.technotalkative.loadwebimage.imageutils.ImageLoader;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight
 * forward GridView implementation with the key addition being the ImageWorker
 * class w/ImageCache to load children asynchronously, keeping the UI nice and
 * smooth and caching thumbnails for quick retrieval. The cache is retained over
 * configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
@SuppressLint("NewApi")
public class FriendAlbumList extends Fragment implements
		AdapterView.OnItemClickListener {
	private static final String TAG = "FriendAlbumList";
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageFetcher mImageFetcher;

	private String[] albumArray;
	private String[] albumCoverUrlArray;
	private String[] albumPhotoUrlArray;
	private String[] albumNameArray;
	private String[] albumPhotoAccountArray;
	private String[] albumCoverArray;
	private String token;

	private String name;

	private Facebook facebook;

	private String extStorageDirectory;

	private CheckBox cb_all;

	private ListView mGridView;

	private HashMap<Integer, Boolean> isSelected;

	private ImageButton btn_download;

	private ProgressDialog progress;

	private List<String> downloadAlbumPhotoList;
	private List<String> downloadAlbumNameList;
	private List<Integer> downloadAlbumPhotoCountList;

	private HashMap<String, List<String>> downloadAlbumPhotoHashList;
	private ImageLoader imageLoader;

	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public FriendAlbumList() {
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork() // or
																			// .detectAll()
																			// for
																			// all
																			// detectable
																			// problems
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
					.penaltyLog().penaltyDeath().build());

		}

		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();

		Log.w("fbtest", extStorageDirectory);

		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

		facebook = new Facebook("134348190059034");

		albumArray = getArguments().getStringArray("albumArray");

		albumCoverArray = getArguments().getStringArray("albumCoverArray");

		albumCoverUrlArray = getArguments()
				.getStringArray("albumCoverUrlArray");

		albumNameArray = getArguments().getStringArray("albumNameArray");

		albumPhotoAccountArray = getArguments().getStringArray(
				"albumPhotoAccountArray");

		token = getArguments().getString("token");

		name = getArguments().getString("userName");

		Log.w("fbtest", "first token:" + token);

		mAdapter = new ImageAdapter(getActivity());
		
		imageLoader=new ImageLoader(getActivity());

		getAlbumCoverUrl();

		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
		mImageFetcher.setLoadingImage(R.drawable.ic_launcher);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(),
				cacheParams);

		MyFriendFragmentActivity.controlPanelHandler
				.sendEmptyMessage(MyFriendFragmentActivity.HIDE_CONTROL_PANEL);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.friend_album_list, container,
				false);

		btn_download = (ImageButton) v.findViewById(R.id.btn_download);

		btn_download.setOnClickListener(new ImageButton.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				// progress = ProgressDialog.show(getActivity(), "�B�z�U���Ƥ�",
				// "Ū���ï��...�еy��!", true);

				// new Thread(new Runnable() {
				// @Override
				// public void run() {
				//
				downloadAlbumPhotoList = new ArrayList<String>();
				downloadAlbumNameList = new ArrayList<String>();
				downloadAlbumPhotoCountList = new ArrayList<Integer>();

				// �ŧihashmap List �ΨӰO��aid->�������ۤ�url
				// key aid
				// arrayList �Ӥ�url�C��
				downloadAlbumPhotoHashList = new HashMap<String, List<String>>();
				//

				DownloadList.setUserName(name);

				downloadAlbum();

				// MyFirstFragmentActivity.downloadHandler.sendMessage(MyFirstFragmentActivity.downloadHandler.obtainMessage(MyFirstFragmentActivity.DOWNLOAD_PRB,
				// 0, 0));

				// }
				//
				// }).start();

			}

		});

		final TextView tx_user_name = (TextView) v
				.findViewById(R.id.tx_user_name);

		tx_user_name.setText(name);

		cb_all = (CheckBox) v.findViewById(R.id.cb_all);

		isSelected = new HashMap<Integer, Boolean>();

		initSelectedData();

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

		mGridView = (ListView) v.findViewById(R.id.gridView);
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

		return v;
	}

	private void downloadAlbum() {

		new Thread() {
			public void run() {
				for (int i = 0; i < isSelected.size(); i++) {

					if (isSelected.get(i)) {
						downloadAlbumNameList.add(Utils
								.getDirName(albumNameArray[i]));

						getDownloadAlbumPhotoUrl(albumArray[i]);

					}

				}

				// �C����ï���W�r
				for (int j = 0; j < downloadAlbumNameList.size(); j++) {
					// ��o�C����ï���ƶq
					int count = downloadAlbumPhotoCountList.get(j);

					for (int w = 0; w < count; w++) {

						// ���ï���W�r�[��queue
						DownloadList.downloadPhotoAlbumQueue
								.add(downloadAlbumNameList.get(j));

						DownloadList.downloadUserNameQueue.add(name);
					}
				}

				int addNumber = 0;

				for (int i = 0; i < downloadAlbumNameList.size(); i++) {
					addNumber += downloadAlbumPhotoCountList.get(i);
				}

				DownloadList.setAddNumber(addNumber);

				MyFriendFragmentActivity.downloadHandler
						.sendMessage(MyFriendFragmentActivity.downloadHandler
								.obtainMessage(
										MyFriendFragmentActivity.DOWNLOAD_PRB,
										0, 0));
			}
		}.start();

	}

	private void initSelectedData() {
		for (int i = 0; i < albumArray.length; i++) {
			isSelected.put(i, false);
		}
	}

	private void setSelectedData(boolean selected) {
		for (int i = 0; i < albumArray.length; i++) {
			isSelected.put(i, selected);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		Log.w("onresume", "FreindAlbumList onResume()");

		albumArray = getArguments().getStringArray("albumArray");

		albumCoverUrlArray = getArguments()
				.getStringArray("albumCoverUrlArray");

		albumNameArray = getArguments().getStringArray("albumNameArray");

		albumPhotoAccountArray = getArguments().getStringArray(
				"albumPhotoAccountArray");

		token = getArguments().getString("token");

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

	// Your created method
	public void onBackPressed() {
		// Handle any cleanup you don't always want done in the normal lifecycle

		MyFriendFragmentActivity.controlPanelHandler
				.sendEmptyMessage(MyFriendFragmentActivity.SHOW_CONTROL_PANEL);
	}

	@TargetApi(16)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		// TODO Auto-generated method stub

		downloadAlbumPhotoList = new ArrayList<String>();
		downloadAlbumNameList = new ArrayList<String>();
		downloadAlbumPhotoCountList = new ArrayList<Integer>();

		getAlbumPhotoUrl(albumArray[position]);

		// final Intent i = new Intent(getActivity(), ImageGridActivity.class);

		Bundle bundle = new Bundle();

		bundle.putStringArray("albumPhoto", albumPhotoUrlArray);

		bundle.putString("albumName", albumNameArray[position]);

		bundle.putString("userName", name);

		Log.w("downloadpic", "FriendAlbumList albumName:"
				+ albumNameArray[position]);

		// i.putExtras(bundle);

		Fragment newFragment = new ImageGridFragment();

		newFragment.setArguments(bundle);

		final FragmentTransaction ft = getFragmentManager().beginTransaction();

		ft.replace(R.id.rl_user_photo, newFragment, "first");
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();

	}

	public void getAlbumPhotoUrl(String aid) {

		String fql = "SELECT src_big FROM photo WHERE aid = " + "\"" + aid
				+ "\"" + " limit 3000";

		Bundle parameters = new Bundle();

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", token);

		Log.w("fbtest", " fql:" + fql);
		Log.w("fbtest", " token:" + token);

		String response;
		try {
			response = facebook.request(parameters);

			JSONArray friendAlbumPhotoArray = new JSONArray(response);

			String aPhotoUrl;
			JSONObject coverUrl;

			albumPhotoUrlArray = new String[friendAlbumPhotoArray.length()];

			if (friendAlbumPhotoArray == null) {
				// �O�����ï���X�i�Ϥ�
				downloadAlbumPhotoCountList.add(0);
			} else {
				// �O�����ï���X�i�Ϥ�
				downloadAlbumPhotoCountList.add(friendAlbumPhotoArray.length());
			}

			// �ŧi url List �ΨӨ�o�Ӭ�ï���Ӥ�
			List url = new ArrayList<String>();

			for (int i = 0; i < friendAlbumPhotoArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				coverUrl = friendAlbumPhotoArray.getJSONObject(i);

				if (coverUrl.has("src_big")) {
					aPhotoUrl = coverUrl.getString("src_big");
				} else {
					aPhotoUrl = "";
				}

				downloadAlbumPhotoList.add(aPhotoUrl);

				// ���ï�����ۤ�url�[�i�h
				url.add(aPhotoUrl);

				albumPhotoUrlArray[i] = aPhotoUrl;

				Log.w("pid", "-------------" + i + "-----------------");
				Log.w("pid",
						"albumPhotoUrlArray Friend Added albumPhotoUrlArray: "
								+ albumPhotoUrlArray[i]);
				Log.w("pid", "-----------------------------------");
			}

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void getDownloadAlbumPhotoUrl(String aid) {

		String fql = "SELECT src_big FROM photo WHERE aid = " + "\"" + aid
				+ "\"" + " limit 1000";

		Bundle parameters = new Bundle();

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", token);

		Log.w("fbtest", " fql:" + fql);
		Log.w("fbtest", " token:" + token);

		String response;
		try {
			response = facebook.request(parameters);

			JSONArray friendAlbumPhotoArray = new JSONArray(response);

			String aPhotoUrl;
			JSONObject coverUrl;

			albumPhotoUrlArray = new String[friendAlbumPhotoArray.length()];

			if (friendAlbumPhotoArray == null) {
				// �O�����ï���X�i�Ϥ�
				downloadAlbumPhotoCountList.add(0);
			} else {
				// �O�����ï���X�i�Ϥ�
				downloadAlbumPhotoCountList.add(friendAlbumPhotoArray.length());
			}

			// �ŧi url List �ΨӨ�o�Ӭ�ï���Ӥ�
			List url = new ArrayList<String>();

			for (int i = 0; i < friendAlbumPhotoArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				coverUrl = friendAlbumPhotoArray.getJSONObject(i);

				if (coverUrl.has("src_big")) {
					aPhotoUrl = coverUrl.getString("src_big");
				} else {
					aPhotoUrl = "";
				}

				downloadAlbumPhotoList.add(aPhotoUrl);

				// ���ï�����ۤ�url�[�i�h
				url.add(aPhotoUrl);

				albumPhotoUrlArray[i] = aPhotoUrl;

				// ���ï���ۤ�url�[�Jqueue
				DownloadList.downloadPhotoQueue.add(aPhotoUrl);

				Log.w("pid", "-------------" + i + "-----------------");
				Log.w("pid",
						"albumPhotoUrlArray Friend Added albumPhotoUrlArray: "
								+ albumPhotoUrlArray[i]);
				Log.w("pid", "-----------------------------------");
			}

			downloadAlbumPhotoHashList.put(aid, url);

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	Handler downloadViewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			for (int i = 0; i < isSelected.size(); i++) {

				if (isSelected.get(i)) {

					ArrayList list = (ArrayList) downloadAlbumPhotoHashList
							.get(albumArray[i]);

					String downloadArray[] = new String[list.size()];

					list.toArray(downloadArray);

					Log.w("dwo", "downloadArray[" + i + "]"
							+ downloadArray.length);

				}
			}

		}
	};

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
		private ListView.LayoutParams mImageViewLayoutParams;
		private LayoutInflater mInflater;;

		public ImageAdapter(Context context) {
			super();
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mImageViewLayoutParams = new ListView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
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
			return albumArray.length;
		}

		@Override
		public Object getItem(int position) {

			return albumArray[position];

		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return getCount();
		}

		@Override
		public int getItemViewType(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup container) {
			// First check if this is the top row

			final ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.friend_list_item,
						container, false);

				holder = new ViewHolder();

				holder.iv_cover = (ImageView) convertView
						.findViewById(R.id.iv_pic);

				holder.tx_name = (TextView) convertView
						.findViewById(R.id.tx_name);

				holder.tx_photo_count = (TextView) convertView
						.findViewById(R.id.tx_photo_count);

				holder.btn_download = (Button) convertView
						.findViewById(R.id.btn_download);

				holder.tx_download_per = (TextView) convertView
						.findViewById(R.id.tx_download_per);

				holder.pb_download_per = (ProgressBar) convertView
						.findViewById(R.id.pb_download_per);

				holder.cb_check_for_download = (CheckBox) convertView
						.findViewById(R.id.cb_check_for_download);

				holder.iv_cover.setScaleType(ImageView.ScaleType.CENTER_CROP);
				// holder.iv_cover.setLayoutParams(mImageViewLayoutParams);

				convertView.setTag(holder);

				Log.w("listview", "create:" + position);

			} else {
				holder = (ViewHolder) convertView.getTag();
				Log.w("listview", "resue:" + position);
			}
			// Set empty view with height of ActionBar
			convertView.setLayoutParams(new AbsListView.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));

			// if (holder.iv_cover.getLayoutParams().height != mItemHeight) {
			// holder.iv_cover.setLayoutParams(mImageViewLayoutParams);
			// }

//			mImageFetcher.loadImage(albumCoverUrlArray[position],
//					holder.iv_cover);
			imageLoader.DisplayImage(albumCoverUrlArray[position], holder.iv_cover);

			holder.cb_check_for_download.setChecked(isSelected.get(position));

			holder.cb_check_for_download
					.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub

							isSelected.put(position, isChecked);

						}

					});

			holder.tx_name.setText(albumNameArray[position]);

			holder.tx_photo_count.setText(albumPhotoAccountArray[position]
					+ " photos");

			holder.btn_download
					.setOnClickListener(new Button.OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							getAlbumPhotoUrl(albumArray[position]);

							DownloadTask downloadTask;

							downloadTask = new DownloadTask();

							downloadTask.setView((View) v.getParent());

							downloadTask.setPath(extStorageDirectory
									+ "/DCIM/FBBackup/"
									+ Utils.getDirName(albumNameArray[position])
									+ "/");
							downloadTask.setContext(mContext);
							downloadTask.execute("start");

							Toast.makeText(getActivity(), "download",
									Toast.LENGTH_LONG).show();
						}

					});

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
			mImageViewLayoutParams = new ListView.LayoutParams(
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
	}

	public static class ViewHolder {
		ImageView iv_cover;
		TextView tx_name;
		TextView tx_photo_count;
		Button btn_download;
		TextView tx_download_per;
		ProgressBar pb_download_per;
		CheckBox cb_check_for_download;
	}

	private Bitmap LoadImage(String URL, BitmapFactory.Options options) {
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			in = OpenHttpConnection(URL);
			bitmap = BitmapFactory.decodeStream(in, null, options);
			in.close();
		} catch (IOException e1) {
		}
		return bitmap;
	}

	private InputStream OpenHttpConnection(String strURL) throws IOException {
		InputStream inputStream = null;
		URL url = new URL(strURL);
		URLConnection conn = url.openConnection();

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = httpConn.getInputStream();
			}
		} catch (Exception ex) {
		}
		return inputStream;
	}

	Handler refreshViewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			mAdapter.notifyDataSetChanged();
		}
	};

	/**
	 * �^��coverurl�A���|���ŭȡA��C�����nquery�A�t�׫ܺC�A�o��t�~�}�@��Thread�Ӱ��
	 * Ato avoid main thread hang on
	 */
	public void getAlbumCoverUrl() {

		// JSONArray friendCoverArray = new JSONArray(response);
		// new Thread to process albumCoverUrl
		new Thread() {
			public void run() {
				for (int i = 0; i < albumCoverArray.length; i++) {
					String aCoverUrl = null;
					String pidSet = "\"" + albumCoverArray[i] + "\"";
					String fql = "SELECT src FROM photo WHERE pid = " + pidSet
							+ " limit 1000";

					Log.w("testfql", fql);

					Bundle parameters = new Bundle();

					parameters.putString("query", fql);
					parameters.putString("method", "fql.query");
					parameters.putString("access_token", token);

					String response = null;
					try {
						response = facebook.request(parameters);
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					Log.w("pid", response);

					JSONArray friendCoverArray = null;
					try {
						friendCoverArray = new JSONArray(response);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (friendCoverArray.length() == 0) {
						aCoverUrl = "";
						continue;
					}

					JSONObject coverUrl = null;
					try {
						coverUrl = friendCoverArray.getJSONObject(0);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Get a JSONObject from the JSONArray

					if (coverUrl.has("src")) {
						try {
							aCoverUrl = coverUrl.getString("src");
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						aCoverUrl = "";
					}

					albumCoverUrlArray[i] = aCoverUrl;

					// call message to refresh listview
					refreshViewHandler.sendEmptyMessage(0);

					Log.w("pid", "-------------" + i + "-----------------");
					Log.w("pid", "Friend Added albumCoverUrl: "
							+ albumCoverUrlArray[i]);
					Log.w("pid", "-----------------------------------");
				}

			}
		}.start();
	}

	Handler proViewHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			progress.dismiss();

		}
	};
}
