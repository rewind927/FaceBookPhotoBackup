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
import android.util.Log;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity {
    private static final String TAG = "ImageGridActivity";

    String photoArray[];
    String albumName;
    String name;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);
        
        photoArray=getIntent().getExtras().getStringArray("albumPhoto");
        
        albumName=getIntent().getExtras().getString("albumName");
        
        name=getIntent().getExtras().getString("userName");
        
        for (int i = 0; i < photoArray.length; i++) {
			Log.w("photo",i+":"+photoArray[i]);
		}


    	
        if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
           
        	
        	Fragment newFragment = new ImageGridFragment();
        	
        	Bundle args = new Bundle();
        	
        	args.putStringArray("photo", photoArray);
        	
        	args.putString("albumName", albumName);
        	
        	args.putString("userName", name);
        	
        	Log.w("downloadpic","ImageGridActivity albumName:"+albumName);
        	
        	newFragment.setArguments(args);
        	
        	final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        	
            ft.add(android.R.id.content, newFragment, TAG);
            ft.commit();
        }
    }
}
