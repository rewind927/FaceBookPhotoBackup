package com.fbbackup;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.Facebook;

public class GetUserInfo extends Activity {

	private String uid;
	private String name;
	private String token;

	private String[] albumArray;
	private String[] albumCoverArray;
	private String[] albumCoverUrlArray;

	private String[] albumNameArray;

	private String[] albumPhotoUrlArray;

	private String[] albumPhotoAccountArray;

	private int album = 0;

	private ProgressDialog progress;

	Facebook facebook = new Facebook("134348190059034");

	private final int GET_ALBUM_COVER_URL = 1;
	private final int CLOSE_PR = 2;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_data_layout);

		Bundle bundle = getIntent().getExtras();

		name = bundle.getString("name");

		uid = bundle.getString("id");

		token = bundle.getString("token");

		progress = ProgressDialog.show(this, getString(R.string.loading_pic_title), getString(R.string.loading_pic_text), true);

		new Thread(new Runnable() {
			@Override
			public void run() {

				getAlbum();

			}

		}).start();

	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case 1:
				if (album > 0) {
					getAlbumCoverUrl();
				} else {
					handler.sendMessage(handler.obtainMessage(CLOSE_PR, 0, 0));
				}
				break;
			case 2:
				progress.dismiss();
				Toast.makeText(GetUserInfo.this,  getString(R.string.album_is_no_pic),
						Toast.LENGTH_LONG).show();
				break;
			case 3:
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

					Log.w("fbtest", "GetUser:" + token);

					Intent intent = new Intent();

					intent.putExtras(b);

					intent.setClass(GetUserInfo.this,
							FriendAlbumListActivity.class);

					startActivity(intent);
				} else {
					Toast.makeText(GetUserInfo.this,  getString(R.string.album_is_no_pic),
							Toast.LENGTH_LONG).show();
				}
			default:
				break;
			}

		}
	};

	public void getAlbumPhotoUrl(String aid) {

		String fql = "SELECT src_big FROM photo WHERE aid = " + aid;

		Bundle parameters = new Bundle();

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", token);

		String response;
		try {
			response = facebook.request(parameters);

			JSONArray friendAlbumPhotoArray = new JSONArray(response);

			String aPhotoUrl;
			JSONObject coverUrl;

			albumPhotoUrlArray = new String[friendAlbumPhotoArray.length()];

			for (int i = 0; i < friendAlbumPhotoArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				coverUrl = friendAlbumPhotoArray.getJSONObject(i);

				if (coverUrl.has("src_big")) {
					aPhotoUrl = coverUrl.getString("src_big");
				} else {
					aPhotoUrl = "";
				}

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

	public void getAlbumCoverUrl() {

		String pidSet = "(";

		for (int i = 0; i < (albumCoverArray.length - 1); i++) {

			pidSet += "\"" + albumCoverArray[i] + "\"" + " , ";

		}

		Log.w("pid", "albumCoverArray:" + albumCoverArray.length);

		pidSet += "\"" + albumCoverArray[(albumCoverArray.length - 1)] + "\""
				+ ")";

		String fql = "SELECT src FROM photo WHERE pid IN " + pidSet;

		Log.w("testfql", fql);

		Bundle parameters = new Bundle();

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", token);

		try {
			String response;
			response = facebook.request(parameters);
			Log.w("pid", response);

			JSONArray friendCoverArray = new JSONArray(response);

			String aCoverUrl;
			JSONObject coverUrl;

			for (int i = 0; i < friendCoverArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				coverUrl = friendCoverArray.getJSONObject(i);

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
			int friendCount = 0;
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

				friendCount++;
				text += "-------------" + friendCount + "-----------------\n";
				text += "Friend Added aid: " + aId + "\n";
				text += "Friend Added cover_pid: " + aCoverId + "\n";
				text += "Friend Added description: " + aDescription + "\n";
				text += "Friend Added photo_count: " + aPhoto_count + "\n";
				text += "Friend Added name: " + aName + "\n";
				text += "-----------------------------------\n";

				Log.w("FB", "-------------" + friendCount + "-----------------");
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

	public void getFriendPic() {

		String fql = "SELECT src_big FROM photo WHERE owner = " + uid;

		Bundle parameters = new Bundle();

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", token);

		Log.w("FB", "facebook.getAccessToken():" + token);

		String text = "";

		try {
			String response = facebook.request(parameters);
			Log.w("FB", response);

			// JSONObject json;
			// json = Util.parseJson(response);

			// Get the JSONArry from our response JSONObject
			// JSONArray friendArray = json.getJSONArray("data");
			JSONArray friendArray = new JSONArray(response);

			Log.w("count", "friendCount:" + friendArray.length());

			// Loop through our JSONArray
			int friendCount = 0;
			String fId, fNm, fGen, fPic, fBir;
			JSONObject friend;

			for (int i = 0; i < friendArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				friend = friendArray.getJSONObject(i);
				// Extract the strings from the JSONObject

				if (friend.has("src_big")) {
					fPic = friend.getString("src_big");
				} else {
					fPic = "";
				}

				friendCount++;
				text += "-------------" + friendCount + "-----------------\n";
				text += "Friend Added pic: " + fPic + "\n";
				text += "-----------------------------------\n";

				Log.w("FB", "-------------" + friendCount + "-----------------");
				Log.w("FB", "Friend Added pic: " + fPic);
				Log.w("FB", "-----------------------------------");
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

}
