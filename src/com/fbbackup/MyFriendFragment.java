package com.fbbackup;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ui.RecyclingImageView;
import util.DownloadList;
import util.ImageCache.ImageCacheParams;
import util.ImageFetcher;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.technotalkative.loadwebimage.imageutils.ImageLoader;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MyFriendFragment extends Fragment implements
		AdapterView.OnItemClickListener {

	private static final String TAG = "ImageGridFragment";

	private static final String IMAGE_CACHE_DIR = "thumbs";

	private String[] friendListArray;

	private String[] friendNameArray;

	private String[] friendIDArray;

	private String[] albumCoverArray;
	private String[] albumCoverUrlArray;
	private String[] albumPhotoAccountArray;
	private String[] albumNameArray;
	private String[] albumArray;

	private String token;
	private String uid;
	private String name;

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	private ImageLoader imageLoader;

	private final int GET_ALBUM_COVER_URL = 1;
	private final int CLOSE_PR = 2;

	private int album = 0;

	private ProgressDialog progress;
	private ProgressDialog progressCover;

	Facebook facebook = new Facebook("134348190059034");
	

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mImageThumbSize = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_spacing);

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
		
		
		//��l�Ʃ�ǳ�downlaod��url��queue�Bname��queue�BuserName��queue
		DownloadList.initDownloadPhotoAlbumQueue();
		DownloadList.initDownloadPhotoQueue();
		DownloadList.initDownloadPhotoUserNameQueue();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		final View v = inflater.inflate(R.layout.image_grid_fragment,
				container, false);

		final GridView mGridView = (GridView) v.findViewById(R.id.gridView);

		friendListArray = getArguments().getStringArray("friendPic");

		friendNameArray = getArguments().getStringArray("friendName");

		friendIDArray = getArguments().getStringArray("friendID");

		token = getArguments().getString("token");

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

	@Override
	public void onResume() {
		super.onResume();
		friendListArray = getArguments().getStringArray("friendPic");

		friendNameArray = getArguments().getStringArray("friendName");

		friendIDArray = getArguments().getStringArray("friendID");

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

	// 109654936

	class FriendAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		private Activity mContext;

		public FriendAdapter(Activity context) {
			mContext = context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return friendListArray.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return friendListArray[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int pos, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			InofView holder;

			Log.w("pos", "" + pos);

			// if (null == convertView) {

			convertView = mInflater.inflate(R.layout.friend_list_item, parent,
					false);

			holder = new InofView();

			holder.tx = (TextView) convertView.findViewById(R.id.tx_name);

			holder.tx.setText(friendListArray[pos]);

			holder.iv = (ImageView) convertView.findViewById(R.id.iv_pic);

			holder.iv.setImageBitmap(getBitmapFromURL(friendListArray[pos]));

			// convertView.setTag(holder);

			// }else{
			// holder = (InofView) convertView.getTag();
			// }

			return convertView;
		}

		public Bitmap getBitmapFromURL(String dataurl) {

			try {
				URL url = new URL(dataurl);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				Bitmap myBitmap = BitmapFactory.decodeStream(input);
				return myBitmap;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	static class InofView {
		TextView tx;
		ImageView iv;
	};

	private class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private int mActionBarHeight = 0;
		private GridView.LayoutParams mImageViewLayoutParams;

		public ImageAdapter(Context context) {
			super();
			mContext = context;
			mImageViewLayoutParams = new GridView.LayoutParams(
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
			return friendListArray.length;
		}

		@Override
		public Object getItem(int position) {

			return  friendListArray[position];

		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@Override
		public int getViewTypeCount() {
			// Two types of views, the normal ImageView and the top row of empty
			// views
			return (getCount() > 0) ? getCount() : 1;
		}

		@Override
		public int getItemViewType(int position) {
			return (position > 0) ? position : 1;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			// First check if this is the top row
		//	if (position < mNumColumns) {

				// Set empty view with height of ActionBar

				//return convertView;
			//}

			// Now handle the main ImageView thumbnails
			ImageView imageView;
			if (convertView == null) { // if it's not recycled, instantiate and
										// initialize
				convertView = new View(mContext);
				convertView.setLayoutParams(new AbsListView.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT, mActionBarHeight));
				imageView = new RecyclingImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setLayoutParams(mImageViewLayoutParams);
			} else { // Otherwise re-use the converted view
				imageView = (ImageView) convertView;
			}

			// Check the height matches our calculated column width
			if (imageView.getLayoutParams().height != mItemHeight) {
				imageView.setLayoutParams(mImageViewLayoutParams);
			}

			// Finally load the image asynchronously into the ImageView, this
			// also takes care of
			// setting a placeholder image while the background thread runs
//			mImageFetcher.loadImage(friendListArray[position],
//					imageView);
			
			
			imageLoader.DisplayImage(friendListArray[position], imageView);
			
			return imageView;
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
			mImageViewLayoutParams = new GridView.LayoutParams(
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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		progress = ProgressDialog.show(getActivity(), getString(R.string.loading_pic_title),getString(R.string.loading_pic_text),
				true);
		

		uid = friendIDArray[arg2];
		name = friendNameArray[arg2];

		new Thread(new Runnable() {
			@Override
			public void run() {

				getAlbum();

			}

		}).start();

		// Bundle bundle=new Bundle();
		//
		// bundle.putString("name",
		// friendNameArray[arg2-mAdapter.getNumColumns()]);
		//
		// bundle.putString("id", friendIDArray[arg2-mAdapter.getNumColumns()]);
		//
		// bundle.putString("token", token);
		//
		// Intent it=new Intent();
		//
		//
		// it.putExtras(bundle);
		//
		// it.setClass(getActivity(), GetUserInfo.class);
		//
		// startActivity(it);

	}

	public void getAlbum() {
		String fql = "SELECT name,link,aid,cover_pid,description,photo_count FROM album WHERE owner = "
				+ uid;
		Bundle parameters = new Bundle();

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", token);

		String text = "";

		try {
			String response = facebook.request(parameters);
			Log.w("FB", response);

			JSONArray friendArray = new JSONArray(response);

			Log.w("count", "friendCount:" + friendArray.length());

			// Loop through our JSONArray
			String aId, aCoverId, aDescription, aPhoto_count, aName;
			JSONObject friend;

			albumArray = new String[friendArray.length()];
			albumCoverArray = new String[friendArray.length()];
			albumCoverUrlArray = new String[friendArray.length()];
			albumNameArray = new String[friendArray.length()];
			albumPhotoAccountArray = new String[friendArray.length()];

			album = friendArray.length();
			Log.w("pid", "friendArray.length():" + friendArray.length());

			for (int i = 0; i < friendArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				friend = friendArray.getJSONObject(i);
				// Extract the strings from the JSONObject

				if (friend.has("aid")) {
					aId = friend.getString("aid");
				} else {
					aId = "";
				}

				if (friend.has("cover_pid")) {
					aCoverId = friend.getString("cover_pid");
				} else {
					aCoverId = "";
				}

				if (friend.has("description")) {
					aDescription = friend.getString("description");
				} else {
					aDescription = "";
				}

				if (friend.has("photo_count")) {
					aPhoto_count = friend.getString("photo_count");
				} else {
					aPhoto_count = "";
				}

				if (friend.has("name")) {
					aName = friend.getString("name");
				} else {
					aName = "";
				}

				albumArray[i] = aId;
				albumCoverArray[i] = aCoverId;
				albumNameArray[i] = aName;

				albumPhotoAccountArray[i] = aPhoto_count;

				Log.w("FB", "-------------" + i + "-----------------");
				Log.w("FB", "Friend Added aid: " + aId);
				Log.w("FB", "Friend Added cover_pid: " + aCoverId);
				Log.w("FB", "Friend Added description: " + aDescription);
				Log.w("FB", "Friend Added photo_count: " + aPhoto_count);
				Log.w("FB", "Friend Added name: " + aName);
				Log.w("FB", "-----------------------------------");
			}

			// friend_url.setText(text);

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

		handler.sendMessage(handler.obtainMessage(GET_ALBUM_COVER_URL, 0, 0));

	}
	
	/**
	 * �^��coverurl�A���|���ŭȡA��C�����nquery�A�t�׫ܺC
	 */
	public void getAlbumCoverUrl() {

		try {

			// JSONArray friendCoverArray = new JSONArray(response);

			String aCoverUrl;

			for (int i = 0; i < albumCoverArray.length; i++) {

				String pidSet = "\"" + albumCoverArray[i] + "\"";
				String fql = "SELECT src FROM photo WHERE pid = " + pidSet;

				Log.w("testfql", fql);

				Bundle parameters = new Bundle();

				parameters.putString("query", fql);
				parameters.putString("method", "fql.query");
				parameters.putString("access_token", token);

				String response;
				response = facebook.request(parameters);

				Log.w("pid", response);

				JSONArray friendCoverArray = new JSONArray(response);
				
				if(friendCoverArray.length()==0){
					aCoverUrl = "";
					continue;
				}

				JSONObject coverUrl = friendCoverArray.getJSONObject(0);

				// Get a JSONObject from the JSONArray


				if (coverUrl.has("src")) {
					aCoverUrl = coverUrl.getString("src");
				} else {
					aCoverUrl = "";
				}

				albumCoverUrlArray[i] = aCoverUrl;

				Log.w("pid", "-------------" + i + "-----------------");
				Log.w("pid", "Friend Added albumCoverUrl: "
						+ albumCoverUrlArray[i]);
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

		handler.sendMessage(handler.obtainMessage(3, 0, 0));

	}


	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1:
				if (album > 0) {
					progress.dismiss();
					Bundle b = new Bundle();
					b.putString("token", token);
					b.putStringArray("albumArray", albumArray);
					b.putStringArray("albumCoverArray", albumCoverArray);
					b.putStringArray("albumCoverUrlArray", albumCoverUrlArray);
					b.putStringArray("albumNameArray", albumNameArray);
					b.putStringArray("albumPhotoAccountArray",
							albumPhotoAccountArray);
					b.putString("userName",name);


					Fragment newFragment = new FriendAlbumList();

					newFragment.setArguments(b);

					final FragmentTransaction ft = getFragmentManager()
							.beginTransaction();

					ft.replace(R.id.rl_user_photo, newFragment, "first");
					ft.addToBackStack(null);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.commit();
					
					
				} else {
					handler.sendMessage(handler.obtainMessage(CLOSE_PR, 0, 0));
				}
				break;
			case 2:
				progress.dismiss();
				Toast.makeText(getActivity(), getString(R.string.album_is_no_pic),
						Toast.LENGTH_LONG).show();
				break;
			case 3:
				progressCover.dismiss();
				progress.dismiss();
				if (album > 0) {
					Bundle b = new Bundle();
					b.putString("token", token);
					b.putStringArray("albumArray", albumArray);
					b.putStringArray("albumCoverArray", albumCoverArray);
					b.putStringArray("albumCoverUrlArray", albumCoverUrlArray);
					b.putStringArray("albumNameArray", albumNameArray);
					b.putStringArray("albumPhotoAccountArray",
							albumPhotoAccountArray);
					b.putString("userName",name);


					Fragment newFragment = new FriendAlbumList();

					newFragment.setArguments(b);

					final FragmentTransaction ft = getFragmentManager()
							.beginTransaction();

					ft.replace(R.id.rl_user_photo, newFragment, "first");
					ft.addToBackStack(null);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
					ft.commit();
					
					
				

				} else {
					Toast.makeText(getActivity(),  getString(R.string.album_is_no_pic),
							Toast.LENGTH_LONG).show();
				}
			default:
				break;
			}

		}
	};
}
