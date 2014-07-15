package com.fbbackup;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import com.facebook.widget.LoginButton;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class MainActivity extends FragmentActivity {

	// view
	private Button btn;
	private Button btn2;
	private Button postWall_btn;
	private Button btn_info;
	private EditText postWall_etx;
	private LoginButton fblogin_btn;

	// facebook permission
	public static final String[] fbPermission = {"user_photos", "friends_photos" };
	
//	public static final String[] fbPermission = { "email", "publish_checkins",
//		"friends_birthday", "friends_checkins", "friends_likes",
//		"user_status", "friends_status", "user_location",
//		"friends_location", "user_photos", "friends_photos" };

	public static final String FIRST_LOGIN = "first_login";
	public static final String FIRST_LOGIN_PREFERENCE = "first_login_pref";

	String token = "";

	String friendsID[];
	String friendsName[];
	String friendsGender[];
	String friendsPicture[];
	String friendBirthday[];
	String tagMePicture[];
	private final int GET_USER_LIST = 1;
	private ProgressDialog progress;

	private static Activity mContext;

	@SuppressWarnings("deprecation")
	Facebook facebook = new Facebook("134348190059034");

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mContext = this;
		setView();

		setListener();

		if (checkNetworkConnected()) {
			if (!isFirstLogin()) {
				progress = ProgressDialog.show(MainActivity.this, getString(R.string.loading_data_title),
						getString(R.string.loading_data_text), true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						getFriendPic();

					}

				}).start();
			}
		} else {
			Toast.makeText(MainActivity.this,
					getString(R.string.network_error_msg), Toast.LENGTH_LONG)
					.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	@SuppressWarnings("deprecation")
	public void postToWall() {
		// post on user's wall.
		facebook.dialog(this, "feed", new DialogListener() {

			@Override
			public void onFacebookError(FacebookError e) {
			}

			@Override
			public void onError(DialogError e) {
			}

			@Override
			public void onComplete(Bundle values) {
			}

			@Override
			public void onCancel() {
			}
		});

	}

	/*
	 * SELECT checkin_id,message,post_id,timestamp,coords,author_uid,page_id
	 * FROM checkin WHERE author_uid IN (SELECT uid2 FROM friend WHERE uid1 =
	 * me())
	 */
	public void getFriendPlace() {

		String fql = "SELECT checkin_id,message,post_id,timestamp,coords,author_uid,page_id FROM checkin WHERE author_uid  IN (SELECT uid2 FROM friend WHERE uid1 = me())";

		Bundle parameters = new Bundle();

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", facebook.getAccessToken());

		try {
			String response = facebook.request(parameters);

			Log.w("FB", response);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void getTagPicForMe() {
		String fql = "SELECT src_big FROM photo WHERE pid in (SELECT pid FROM photo_tag WHERE subject=me())";

		Bundle parameters = new Bundle();

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", token);
		// parameters.putString("access_token", facebook.getAccessToken());
		// token = facebook.getAccessToken();

		Log.w("FB", "facebook.getAccessToken():" + token);

		try {
			String response = facebook.request(parameters);
			Log.w("FB", "tag:" + response);

			// JSONObject json;
			// json = Util.parseJson(response);

			// Get the JSONArry from our response JSONObject
			// JSONArray friendArray = json.getJSONArray("data");
			JSONArray friendArray = new JSONArray(response);

			Log.w("count", "friendCount:" + friendArray.length());

			// Loop through our JSONArray

			String fPic;
			JSONObject friend;

			tagMePicture = new String[friendArray.length()];

			for (int i = 0; i < friendArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				friend = friendArray.getJSONObject(i);
				// Extract the strings from the JSONObject

				if (friend.has("src_big")) {
					fPic = friend.getString("src_big");
				} else {
					fPic = "";
				}

				tagMePicture[i] = fPic;

				Log.w("FB", "-------------" + i + "-----------------");
				Log.w("FB", "Friend Added tag pic: " + fPic);
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
	
	/**
	 * @author Ryan
	 * 
	 * get friend id name 
	 */
	public void getFriendPic() {

		String fql = "SELECT uid,name,pic_big FROM user WHERE uid=me()OR uid in (SELECT uid2 FROM friend where uid1 = me())";

		Bundle parameters = new Bundle();

		SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
		token = mPrefs.getString("access_token", "");

		if (token.equals("")) {
			token = facebook.getAccessToken();
		}

		parameters.putString("query", fql);
		parameters.putString("method", "fql.query");
		parameters.putString("access_token", token);

		Log.w("FB", "facebook.getAccessToken():" + token);

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

			friendsID = new String[friendArray.length()];
			friendsName = new String[friendArray.length()];
			friendsPicture = new String[friendArray.length()];

			for (int i = 0; i < friendArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				friend = friendArray.getJSONObject(i);
				// Extract the strings from the JSONObject
				fId = friend.getString("uid");
				fNm = friend.getString("name");

				if (friend.has("pic_big")) {
					fPic = friend.getString("pic_big");
				} else {
					fPic = "";
				}

				// Set the values to our arrays
				friendsID[friendCount] = fId;
				friendsName[friendCount] = fNm;
				friendsPicture[friendCount] = fPic;

				friendCount++;
				Log.w("FB", "-------------" + friendCount + "-----------------");
				Log.w("FB", "Friend Added nam: " + fNm);
				Log.w("FB", "Friend Added uid: " + fId);
				Log.w("FB", "Friend Added pic: " + fPic);
				Log.w("FB", "-----------------------------------");
			}

			getTagPicForMe();

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

		handler.sendMessage(handler.obtainMessage(GET_USER_LIST, 0, 0));

	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			progress.dismiss();
			addFragment();

		}
	};

	public void getFriend() {
		Bundle bundle = new Bundle();

		bundle.putString("fields", "name, gender, picture,birthday");

		try {
			String result = facebook.request("me/friends", bundle);

			Log.w("FB", result);

			JSONObject json;
			json = Util.parseJson(result);

			// Get the JSONArry from our response JSONObject
			JSONArray friendArray = json.getJSONArray("data");

			// Loop through our JSONArray
			int friendCount = 0;
			String fId, fNm, fGen, fPic, fBir;
			JSONObject friend;

			friendsID = new String[friendArray.length()];
			friendsName = new String[friendArray.length()];
			friendsGender = new String[friendArray.length()];
			friendsPicture = new String[friendArray.length()];
			friendBirthday = new String[friendArray.length()];

			for (int i = 0; i < friendArray.length(); i++) {
				// Get a JSONObject from the JSONArray
				friend = friendArray.getJSONObject(i);
				// Extract the strings from the JSONObject
				fId = friend.getString("id");
				fNm = friend.getString("name");
				if (friend.has("gender")) {
					fGen = friend.getString("gender");
				} else {
					fGen = "no asign gender";
				}
				if (friend.has("picture")) {
					JSONObject picture = friend.getJSONObject("picture")
							.getJSONObject("data");
					fPic = picture.getString("url");

				} else {
					fPic = "";
				}

				if (friend.has("birthday")) {
					fBir = friend.getString("birthday");
				} else {
					fBir = "";
				}
				// Set the values to our arrays
				friendsID[friendCount] = fId;
				friendsName[friendCount] = fNm;
				friendsGender[friendCount] = fGen;
				friendsPicture[friendCount] = fPic;
				friendBirthday[friendCount] = fBir;

				friendCount++;
				Log.w("FB", "-------------" + friendCount + "-----------------");
				Log.w("FB", "Friend Added nam: " + fNm);
				Log.w("FB", "Friend Added uid: " + fId);
				Log.w("FB", "Friend Added gen: " + fGen);
				Log.w("FB", "Friend Added pic: " + fPic);
				Log.w("FB", "Friend Added bir: " + fBir);
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

	/**
	 * set View
	 */
	public void setView() {
		btn_info = (Button) findViewById(R.id.btn_info);
		btn = (Button) findViewById(R.id.button1);
		btn2 = (Button) findViewById(R.id.button2);

		postWall_btn = (Button) findViewById(R.id.button3);
		// fblogin_btn = (LoginButton) findViewById(R.id.authButton);

	}

	/**
	 * set Listener
	 */
	public void setListener() {
		btn_info.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// Intent it = new Intent();
				// it.setClass(MainActivity.this, UserGuide.class);
				// startActivity(it);

				progress = ProgressDialog.show(MainActivity.this, getString(R.string.loading_data_title),
						getString(R.string.loading_data_text), true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						getFriendPic();
					}

				}).start();
			}

		});

		btn.setOnClickListener(new Button.OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				// facebook Login
				facebook.authorize(MainActivity.this, fbPermission,
						new DialogListener() {

							@Override
							public void onComplete(Bundle values) {

								// save login data
								saveFBAuthData();

								// set is first login flag
								setFirstLogin();

								Toast.makeText(MainActivity.this,
										"facebook login success",
										Toast.LENGTH_LONG).show();

								progress = ProgressDialog.show(
										MainActivity.this, getString(R.string.loading_data_title),
										getString(R.string.loading_data_text), true);
								new Thread(new Runnable() {
									@Override
									public void run() {
										getFriendPic();
									}

								}).start();

							}

							@Override
							public void onFacebookError(FacebookError error) {
								Toast.makeText(MainActivity.this,
										"facebook on error", Toast.LENGTH_LONG)
										.show();
							}

							@Override
							public void onError(DialogError e) {
								Toast.makeText(MainActivity.this,
										"facebook login error",
										Toast.LENGTH_LONG).show();
							}

							@Override
							public void onCancel() {
								Toast.makeText(MainActivity.this,
										"facebook login cancel",
										Toast.LENGTH_LONG).show();
							}
						});

			}

		});

		btn2.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (checkNetworkConnected()) {
					try {
						facebook.logout(MainActivity.this);

						Toast.makeText(MainActivity.this, "FB logout",
								Toast.LENGTH_LONG).show();

					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Toast.makeText(MainActivity.this,
							getString(R.string.network_error_msg),
							Toast.LENGTH_LONG).show();
				}
			}
		});

		postWall_btn.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				postToWall();
			}

		});
	}

	public void addFragment() {

		Bundle args = new Bundle();
		args.putStringArray("friendPic", friendsPicture);
		args.putStringArray("friendName", friendsName);
		args.putStringArray("friendID", friendsID);
		args.putStringArray("tagMePicture", tagMePicture);
		args.putString("token", token);

		Intent it = new Intent();

		it.putExtras(args);

		it.setClass(MainActivity.this, MyFriendFragmentActivity.class);

		startActivity(it);

	}

	/**
	 * save fb token and expires
	 */
	public void saveFBAuthData() {
		SharedPreferences mPrefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putString("access_token", facebook.getAccessToken());
		editor.putLong("access_expires", facebook.getAccessExpires());
		editor.commit();
	}

	/**
	 * set is first login flag, false is not login in first time , true is in
	 * first time
	 */
	public void setFirstLogin() {
		SharedPreferences mPrefs = getSharedPreferences(FIRST_LOGIN_PREFERENCE,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(FIRST_LOGIN, false);
		editor.commit();
	}

	/**
	 * is first login
	 * 
	 * @return is first login ,true is yes , false is not
	 */
	public boolean isFirstLogin() {
		SharedPreferences mPrefs = getSharedPreferences(FIRST_LOGIN_PREFERENCE,
				MODE_PRIVATE);
		Log.w("Ryan", "M:" + mPrefs.getBoolean(FIRST_LOGIN, true));
		return mPrefs.getBoolean(FIRST_LOGIN, true);
	}

	public static Handler exitHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mContext.finish();
		}

	};

	private boolean checkNetworkConnected() {
		boolean result = false;
		ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (CM == null) {
			result = false;
		} else {
			NetworkInfo info = CM.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				if (!info.isAvailable()) {
					result = false;
				} else {
					result = true;
				}
			}
		}
		return result;
	}

}
