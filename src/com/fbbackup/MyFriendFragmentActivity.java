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
import java.net.MalformedURLException;

import util.DownloadList;
import util.DownloadTask;
import util.DownloadTaskNoUpdateUI;
import util.Utils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.android.Facebook;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not
 * much else.
 */
@SuppressLint("NewApi")
public class MyFriendFragmentActivity extends FragmentActivity {
	private static final String TAG = "ImageGridActivity";

	String photoArray[];

	String token = "";

	String friendsID[];
	String friendsName[];
	String friendsPicture[];
	ImageButton btn_tag_me;
	ImageButton btn_photo;
	Button btn_logout;
	ImageButton btn_cancel_download;

	static DownloadTask downloadTask;
	
	static DownloadTaskNoUpdateUI downloadTaskNoUI;

	public static Context mContext;

	public static boolean hasCancel = false;

	String tagMePicture[];

	private static ProgressBar pb_download;
	private static RelativeLayout rl_prb_download;
	private static RelativeLayout rl_controls;

	private AdView adView;
	
	private int mode=0;
	
	Facebook facebook = new Facebook("134348190059034");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.user_photo_layout);

		mContext = this;

		// �إ� adView
		adView = new AdView(this, AdSize.BANNER, "a151aa20ff8c2a2");

//		/* ��ե� */
//		AdRequest adRequest = new AdRequest();
//		adRequest.addTestDevice(AdRequest.TEST_EMULATOR); // �����u��
//		adRequest.addTestDevice("TEST_DEVICE_ID"); // �ΨӴ�ժ� Android �˸m
//		/* ��ե� */
		friendsPicture = getIntent().getExtras().getStringArray("friendPic");

		friendsName = getIntent().getExtras().getStringArray("friendName");

		friendsID = getIntent().getExtras().getStringArray("friendID");

		tagMePicture = getIntent().getExtras().getStringArray("tagMePicture");

		token = getIntent().getExtras().getString("token");

		Fragment newFragment = new MyFriendFragment();

		Bundle args = new Bundle();
		args.putStringArray("friendPic", friendsPicture);
		args.putStringArray("friendName", friendsName);
		args.putStringArray("friendID", friendsID);
		args.putString("token", token);

		newFragment.setArguments(args);

		if(getSupportFragmentManager().getBackStackEntryCount()==0){
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.rl_user_photo, newFragment, "friend");
		//ft.replace(R.id.rl_user_photo, newFragment, "first");
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

		ft.commit();
		}
		setView();
		setLisetner();

		// new download task�A���app�ثe�u���@�ӭt�ddownload task
		downloadTask = new DownloadTask();
		
		downloadTaskNoUI=new DownloadTaskNoUpdateUI();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.w("dow", "resume");
		super.onResume();
	}

	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		//�Ѽ�1:�s��id, �Ѽ�2:itemId, �Ѽ�3:item����, �Ѽ�4:item�W��
		 menu.add(0, 0, 0, getString(R.string.menu_info));
		 menu.add(0, 1, 1, getString(R.string.menu_about));
		return super.onCreatePanelMenu(featureId, menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		
		switch(item.getItemId()) {
		  case 0:
			  Toast.makeText(mContext, "getString(R.string.menu_info)", Toast.LENGTH_LONG).show();
		  break;
		  case 1:
			  
			  PackageInfo pInfo = null;
			try {
				pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  String version = pInfo.versionName;
			  AlertDialog alertDialog = getAlertDialog(getString(R.string.menu_about),getString(R.string.app_name)+version+"\n\ndeveloper : Ryan \n ux design : Zmin\n");
			  
			  alertDialog.show();
			  

			  
			 break;
		}
		
		return super.onMenuItemSelected(featureId, item);
	}

	public void setView() {
		rl_prb_download = (RelativeLayout) findViewById(R.id.rl_prb_download);
		rl_controls = (RelativeLayout) findViewById(R.id.rl_controls);
		btn_tag_me = (ImageButton) findViewById(R.id.btn_tag_me);
		btn_photo = (ImageButton) findViewById(R.id.btn_photo);
		pb_download = (ProgressBar) findViewById(R.id.pb_download);
		btn_cancel_download = (ImageButton) findViewById(R.id.btn_cancel_download);
		btn_logout = (Button) findViewById(R.id.btn_logout);
		LinearLayout layout = (LinearLayout) findViewById(R.id.rl_ad);
		
		btn_photo.setBackgroundResource(R.drawable.album);
		

		// �b�䤤�[�J adView
		layout.addView(adView);
		//
		// // �ҥΪx�νШD�A���H�s�i�@�_��J
		adView.loadAd(new AdRequest());
	}

	@SuppressLint("NewApi")
	public void setLisetner() {

		// Tag
		btn_tag_me.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				Fragment newFragment = new TagMeImageGridFragment();

				Bundle args = new Bundle();
				args.putString("token", token);
				args.putStringArray("tagMePicture", tagMePicture);
				args.putString("userName", friendsName[0]);

				newFragment.setArguments(args);

				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.rl_user_photo, newFragment, "first");
				ft.addToBackStack(null);
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

				ft.commit();

				controlPanelHandler.sendEmptyMessage(HIDE_CONTROL_PANEL);
			}

		});

		btn_cancel_download.setOnClickListener(new ImageButton.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				downloadTask.cancel(true);
				downloadTaskNoUI.cancel(true);
				rl_prb_download.setVisibility(View.GONE);
				hasCancel = true;
			}

		});

		// Photo
		btn_photo.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Fragment newFragment = new MyFriendFragment();

				Bundle args = new Bundle();
				args.putStringArray("friendPic", friendsPicture);
				args.putStringArray("friendName", friendsName);
				args.putStringArray("friendID", friendsID);
				args.putString("token", token);

				newFragment.setArguments(args);

				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.rl_user_photo, newFragment, "first");

				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

				ft.commit();

			}
		});
		
		btn_logout.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
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
				
				try {
					facebook.logout(MyFriendFragmentActivity.this);

					Toast.makeText(MyFriendFragmentActivity.this, "FB logout",
							Toast.LENGTH_LONG).show();

				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				SharedPreferences mPrefs = getSharedPreferences(MainActivity.FIRST_LOGIN_PREFERENCE,MODE_PRIVATE);
				
				Log.w("Ryan","S:"+mPrefs.getBoolean(MainActivity.FIRST_LOGIN, true));
		
				SharedPreferences.Editor editor = mPrefs.edit();
				editor.putBoolean(MainActivity.FIRST_LOGIN, true);
				editor.commit();
				
				Log.w("Ryan","F:"+mPrefs.getBoolean(MainActivity.FIRST_LOGIN, true));
				
				
				
				Intent it=new Intent();
				it.setClass(MyFriendFragmentActivity.this, MainActivity.class);
				startActivity(it);
				finish();
				
			}
		
		});
	}

	public static final int CANCEL_DOWNLOAD_PRB = 0;
	public static final int DOWNLOAD_PRB = 1;
	public static final int HIDE_CONTROL_PANEL = 2;
	public static final int SHOW_CONTROL_PANEL = 3;

	private static Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			// switch (msg.what) {
			// case STOP:
			// rectangleProgressBar.setVisibility(View.GONE);
			// circleProgressBar.setVisibility(View.GONE);
			// Thread.currentThread().interrupt();
			// break;
			// case NEXT:
			if (!Thread.currentThread().isInterrupted()) {
				pb_download.setProgress(iCount);
			}
			// break;
			// }
		}
	};

	private static int iCount = 0;

	public static Handler controlPanelHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_CONTROL_PANEL:
				rl_controls.setVisibility(View.GONE);
				break;
			case SHOW_CONTROL_PANEL:
				rl_controls.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}
	};

	public static Handler downloadHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWNLOAD_PRB:

				// ��donloadTask �S���Q����L�����p
				if (downloadTask.getStatus().equals(
						downloadTask.getStatus().PENDING)) {

					rl_prb_download.setVisibility(View.VISIBLE);

					String extStorageDirectory = Environment
							.getExternalStorageDirectory().toString();

					downloadTask.setPath(extStorageDirectory + "/DCIM/FBBackup/");

					downloadTask.setContext(mContext);
					downloadTask.setView(rl_prb_download);
					downloadTask.execute("start");
					// ��donloadTask���b���檺���p
					Log.w("dow", "downloadTask:1");
				} else if (downloadTask.getStatus().equals(
						downloadTask.getStatus().RUNNING)) {
					Log.w("dow", "downloadTask:2");
					if (hasCancel) {
						downloadTask = new DownloadTask();

						rl_prb_download.setVisibility(View.VISIBLE);

						String extStorageDirectory = Environment
								.getExternalStorageDirectory().toString();

						downloadTask
								.setPath(extStorageDirectory + "/DCIM/FBBackup/");

						downloadTask.setContext(mContext);
						downloadTask.setView(rl_prb_download);
						downloadTask.execute("start");
					} else {
						downloadTask
								.setDownloadMax(downloadTask.getDownloadMax()
										+ DownloadList.getAddNumber());
						hasCancel = false;
					}

					// ��donloadTask �w�g���槹�����p
				} else if (downloadTask.getStatus().equals(
						downloadTask.getStatus().FINISHED)) {

					downloadTask = new DownloadTask();

					rl_prb_download.setVisibility(View.VISIBLE);

					String extStorageDirectory = Environment
							.getExternalStorageDirectory().toString();

					downloadTask.setPath(extStorageDirectory + "/DCIM/FBBackup/");
					downloadTask.setContext(mContext);
					downloadTask.setView(rl_prb_download);
					downloadTask.execute("start");
					Log.w("dow", "downloadTask:3");
				}
				
				
				
				if (downloadTaskNoUI.getStatus().equals(
						downloadTaskNoUI.getStatus().PENDING)) {

					String extStorageDirectory = Environment
							.getExternalStorageDirectory().toString();

					downloadTaskNoUI.setPath(extStorageDirectory + "/DCIM/FBBackup/");

					downloadTaskNoUI.setContext(mContext);
					downloadTaskNoUI.execute("start");
					// ��donloadTask���b���檺���p
					Log.w("dow", "downloadTaskNoUI:1");
				} else if (downloadTaskNoUI.getStatus().equals(
						downloadTaskNoUI.getStatus().RUNNING)) {
					Log.w("dow", "downloadTaskNoUI:2");
					if (hasCancel) {
						downloadTaskNoUI = new DownloadTaskNoUpdateUI();

						String extStorageDirectory = Environment
								.getExternalStorageDirectory().toString();

						downloadTaskNoUI
								.setPath(extStorageDirectory + "/DCIM/FBBackup/");

						downloadTaskNoUI.setContext(mContext);
						downloadTaskNoUI.execute("start");
					} else {

						hasCancel = false;
					}

					// ��donloadTask �w�g���槹�����p
				} else if (downloadTaskNoUI.getStatus().equals(
						downloadTaskNoUI.getStatus().FINISHED)) {

					downloadTaskNoUI = new DownloadTaskNoUpdateUI();


					String extStorageDirectory = Environment
							.getExternalStorageDirectory().toString();

					downloadTaskNoUI.setPath(extStorageDirectory + "/DCIM/FBBackup/");
					downloadTaskNoUI.setContext(mContext);
					downloadTaskNoUI.execute("start");
					Log.w("dow", "downloadTaskNoUI:3");
				}
				
				
				
				break;

			default:
				break;
			}
		}
	};

	/**
	 * �мgback����
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();

		// �p�Gfragmnet stack���O��ƶq��0�A�N�?�ثe��fragment
		if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
			controlPanelHandler.sendEmptyMessage(SHOW_CONTROL_PANEL);
		} else if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			// MainActivity.exitHandler.sendEmptyMessage(0);

			AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
					MyFriendFragmentActivity.this);

			// Setting Dialog Title
			alertDialog2.setTitle(getString(R.string.exit_app_tittle));
			
			// Setting Dialog Message
			alertDialog2.setMessage(getString(R.string.exit_app_text));

			// Setting Icon to Dialog
			// alertDialog2.setIcon(R.drawable.delete);

			// Setting Positive "Yes" Btn
			alertDialog2.setPositiveButton("YES",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Write your code here to execute after dialog
							exit();
							System.exit(0);
						}
					});
			// Setting Negative "NO" Btn
			alertDialog2.setNegativeButton("NO",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// Write your code here to execute after dialog

							Fragment newFragment = new MyFriendFragment();

							Bundle args = new Bundle();
							args.putStringArray("friendPic", friendsPicture);
							args.putStringArray("friendName", friendsName);
							args.putStringArray("friendID", friendsID);
							args.putString("token", token);

							newFragment.setArguments(args);

							FragmentTransaction ft = getSupportFragmentManager()
									.beginTransaction();
							ft.replace(R.id.rl_user_photo, newFragment, "first");

							ft.addToBackStack(null);

							ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

							ft.commit();

							dialog.cancel();
						}
					});

			// Showing Alert Dialog
			alertDialog2.show();

		}
		Log.w("dow", "getSupportFragmentManager().getBackStackEntryCount():"
				+ getSupportFragmentManager().getBackStackEntryCount());
		// press back popup alertdialog ask finish this app

	}

	/**
	 * ���}���app
	 */
	public void exit() {

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}
	
	
	  private AlertDialog getAlertDialog(String title,String message){
          //���ͤ@��Builder����
          Builder builder = new AlertDialog.Builder(MyFriendFragmentActivity.this);
          //�]�wDialog�����D
          builder.setTitle(title);
          //�]�wDialog�����e
          builder.setMessage(message);
          //�]�wPositive���s���
          builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

              }
          });

          //�Q��Builder����إ�AlertDialog
          return builder.create();
    }
	  
}
