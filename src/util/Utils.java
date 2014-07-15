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

package util;


import java.io.File;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.os.StrictMode;
import android.util.Log;

/**
 * Class containing some static utility methods.
 */
public class Utils {
    private Utils() {};

    @TargetApi(11)
    public static void enableStrictMode() {
        if (Utils.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder =
                    new StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder =
                    new StrictMode.VmPolicy.Builder()
                            .detectAll()
                            .penaltyLog();

            if (Utils.hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
//                vmPolicyBuilder
//                        .setClassInstanceLimit(ImageGridActivity.class, 1)
//                        .setClassInstanceLimit(ImageDetailActivity.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }
    
	public static String getDirName(String albumName){
		
		Log.w("parsePicName",albumName);
		
		String dirName="";
		
		dirName=albumName.replaceAll("[/]|[*]|[?]|[\\s]|[\\r]|[\\?]", "");

		
		dirName=dirName.trim();
		
		Log.w("parsePicName","final:"+dirName);
		
		return dirName;
	}
	
	/**
	 * 從url取得檔案名稱
	 * @param url 原始下載的url
	 * @return 截出 [/ - .]之間的字串當檔名
	 */
	public static String parsePicName(String url){
		
		String result="";
		
		result=url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("."));
		
		return result;
	}

	static public String getStorePath(Context ctx){	
		File store = ctx.getExternalFilesDir(null);
		String path = null ;
		if ( null != store){
		
		  File storeage = new File(ctx.getExternalFilesDir(null), "twmebook");
		  path = storeage.getPath() + "/";
		  storeage.mkdir();
		  storeage = null;
		}else {
			path = ctx.getFilesDir().toString()+"/";
		}
		
		StatFs stat = new StatFs(path);
		calFreeSize(stat);
		
		return path ;
	}
	
	/**
	 * 重新計算剩餘空間
	 */
	static public long calFreeSize(StatFs stat) {
		int size = stat.getBlockSize();
		int num = stat.getAvailableBlocks();
		return (long)num * size ;
	}	
}
