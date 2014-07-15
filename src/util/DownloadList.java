package util;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class DownloadList {

	// 用來儲存所有download url的array

	public static String userName = "";

	public static BlockingQueue<String> downloadPhotoQueue;

	public static BlockingQueue<String> downloadPhotoAlbumQueue;
	
	public static BlockingQueue<String> downloadUserNameQueue;
	
	public static int addNumber=0;

	public static void setAddNumber(int i){
		addNumber=i;
	}
	
	public static int getAddNumber(){
		return addNumber;
	}
	
	public static void initDownloadPhotoUserNameQueue(){
		downloadUserNameQueue=new LinkedBlockingQueue<String>();
	}
	

	public static void setDownloadPhotoUserNameQueue(BlockingQueue<String> q) {
		downloadUserNameQueue = q;
	}

	public static BlockingQueue<String> getDownloadUserNameQueue() {
		return downloadUserNameQueue;
	}

	@SuppressLint("NewApi")
	public static void initDownloadPhotoAlbumQueue() {
		downloadPhotoAlbumQueue = new LinkedBlockingQueue<String>();
	}

	public static void setDownloadPhotoAlbumQueue(BlockingQueue<String> q) {
		downloadPhotoAlbumQueue = q;
	}

	public static BlockingQueue<String> getDownloadPhotoAlbumQueue() {
		return downloadPhotoAlbumQueue;
	}

	@SuppressLint("NewApi")
	public static void initDownloadPhotoQueue() {
		downloadPhotoQueue = new LinkedBlockingQueue<String>();
	}

	public static void setDownloadPhotoQueue(BlockingQueue<String> q) {
		downloadPhotoQueue = q;
	}

	public static BlockingQueue<String> getDownloadPhotoQueue() {
		return downloadPhotoQueue;
	}

	public static void setUserName(String name) {
		userName = name;
	}

	public static String getUserName() {
		return userName;
	}
}
