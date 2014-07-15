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

import util.DownloadSinglePicTask;
import util.ImageFetcher;
import util.ImageWorker;
import util.Utils;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;



/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class TagImageDetailFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ImageView mImageView;
    private ImageFetcher mImageFetcher;
    private ImageButton btn_download_pic;
    private static String url;
    private String extStorageDirectory;
    private static String albumName;

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageUrl The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static TagImageDetailFragment newInstance(String imageUrl) {
        final TagImageDetailFragment f = new TagImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);
        
        //url=imageUrl;

        return f;
    }
    
    public static void setAlbumName(String album){
    	albumName=album;
    }
    
    public static void setUrl (String u){
    	url=u;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public TagImageDetailFragment() {}

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link TagImageDetailFragment#newInstance(String)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		extStorageDirectory = Environment.getExternalStorageDirectory()
				.toString();
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        btn_download_pic=(ImageButton) v.findViewById(R.id.btn_download_pic);
        setListener();
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        if (TagImageDetailActivity.class.isInstance(getActivity())) {
            mImageFetcher = ((TagImageDetailActivity) getActivity()).getImageFetcher();
            mImageFetcher.loadImage(mImageUrl, mImageView);
        }

        // Pass clicks on the ImageView to the parent activity to handle
        if (OnClickListener.class.isInstance(getActivity()) && Utils.hasHoneycomb()) {
            mImageView.setOnClickListener((OnClickListener) getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
    }
    
    public void setListener(){
    	btn_download_pic.setOnClickListener(new ImageButton.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(getActivity(), url, Toast.LENGTH_SHORT).show();
				
				DownloadSinglePicTask download=new DownloadSinglePicTask();
				
				download.setContext(getActivity());
				
				download.setAlbunPhotoUrl(url);
				
				Log.w("downloadpic","ImageDetail albumName:"+albumName);
				
				download.setPath(extStorageDirectory + "/DCIM/FBBackup/"
						+ albumName + "/");
				
				
				download.execute("test");
			}
    		
    	});
    }
}
