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

import util.Utils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not
 * much else.
 */
public class FriendAlbumListActivity extends FragmentActivity {
	private static final String TAG = "FriendAlbumListActivity";

	private String[] albumArray;
	private String[] albumCoverArray;
	private String[] albumCoverUrlArray;
	private String[] albumNameArray;
	private String[] albumPhotoAccountArray;
	private String token;
	private String name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		super.onCreate(savedInstanceState);

		albumArray = getIntent().getExtras().getStringArray("albumArray");
		albumCoverArray = getIntent().getExtras().getStringArray("albumCoverArray");
		albumCoverUrlArray = getIntent().getExtras().getStringArray("albumCoverUrlArray");
		albumNameArray=getIntent().getExtras().getStringArray("albumNameArray");
		albumPhotoAccountArray=getIntent().getExtras().getStringArray("albumPhotoAccountArray");
		token = getIntent().getExtras().getString("token");
		name=getIntent().getExtras().getString("userName");

		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {

			Fragment newFragment = new FriendAlbumList();

			Bundle args = new Bundle();

			args.putStringArray("albumArray",albumArray);
			args.putStringArray("albumCoverArray",albumCoverArray);
			args.putStringArray("albumCoverUrlArray",albumCoverUrlArray);
			args.putStringArray("albumNameArray",albumNameArray);
			args.putStringArray("albumPhotoAccountArray", albumPhotoAccountArray);
			args.putString("token",token);
			args.putString("userName", name);

			newFragment.setArguments(args);

			final FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();

			ft.add(android.R.id.content, newFragment, TAG);
			ft.commit();
		}
	}
}
