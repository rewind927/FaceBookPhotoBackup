package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.fbbackup.ImageGridFragment;
import com.fbbackup.R;


public class AlbumDownloadTask extends AsyncTask<String, Integer, String> {

	public static int iFileSize = 0;
	public static double dReadSum = 0;
	public static boolean bIsDownload = false;
	private String[] albumPhotoUrlArray;
	private String[] albumNameArray;
	private String[] albumCoverUrlArray;
	private File wallpaperDirectory;
	private String downloadDirPath;
	private Context mContext;
	private float per;
	public static int taskCount = 0;
	private String albumName = "";

	private WeakReference<View> viewReference;

	public void setView(View view) {
		viewReference = new WeakReference<View>(view);
	}

	public void setContext(Context c) {
		this.mContext = c;
	}

	public void setDownloadURLs(String[] urlArray) {
		this.albumPhotoUrlArray = urlArray;
	}

	public String[] getDownloadURLs() {
		return albumPhotoUrlArray;
	}

	public void setDirName(String[] nameArray) {
		this.albumNameArray = nameArray;
	}

	public String[] getName() {
		return albumNameArray;
	}

	public String[] getAlbumCoverUrlArray() {
		return albumCoverUrlArray;
	}

	public void setAlbumCoverUrlArray(String[] array) {
		this.albumCoverUrlArray = array;
	}

	public void setPath(String path) {
		this.downloadDirPath = path;
	}



	@Override
	protected void onPreExecute() {
		// ��method�O�b����doInBackground�H�e�A�~�|�I�s��

		taskCount++;

		wallpaperDirectory = new File(downloadDirPath);

		wallpaperDirectory.mkdirs();


		per = 0.0f;



		super.onPreExecute();

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);

		Integer v = values[0];

		Log.w("testInt", "onProgressUpdate:" + v);

	}

	@Override
	protected void onPostExecute(String result) {
		// ��method�O�bdoInBackground�����H��A�~�|�I�s��
		super.onPostExecute(result);

		taskCount--;

		addNotification();

	}

	/*
	 * �`�N!strUrlFile�O�@��array..�N��ۦb�I�s��class�ɥi�H�a�J�ܦh�ӰѼ�(strUrlFile)�A�p:new
	 * downloadTask().execute("http://www.xxx","http://www.xxx");
	 */
	protected String doInBackground(String... strUrlFile) {

		try {
			int iUrlCount = albumPhotoUrlArray.length;

			OutputStream outStream = null;

			for (int i = 0; i < iUrlCount; i++) {

				Bitmap bm;

				BitmapFactory.Options bmOptions;
				bmOptions = new BitmapFactory.Options();
				bmOptions.inSampleSize = 1;
				bm = LoadImage(albumPhotoUrlArray[i], bmOptions);

				File file = new File(wallpaperDirectory,
						Utils.parsePicName(albumPhotoUrlArray[i]) + ".JPEG");

				outStream = new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
				outStream.flush();

				per = ((float) i / (float) iUrlCount) * 100.0f;

				publishProgress(i);

				Log.w("testInt", "albumName:" + albumName + ",publishProgress:"
						+ per);

			}
			outStream.close();

		} catch (Exception e) {
			Log.d("main", "download---" + e.toString());
		}

		return "";
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

	public static int getTaskCount() {
		return taskCount;
	}

	public void setAlbumName(String name) {
		albumName = name;
	}

	public void addNotification() {

		// ��l��Notification Manager
		NotificationManager gNotMgr = (NotificationManager) mContext
				.getSystemService(mContext.NOTIFICATION_SERVICE);

		// ����Notification����A�ó]�w���ݩ�
		Notification tBNot = new Notification(R.drawable.face_album,
				mContext.getString(R.string.donwload_notification_msg), System.currentTimeMillis());

		// �]�w�_�ʪ��W�v
		long[] tVibrate = { 0, 100, 200, 300 };
		tBNot.vibrate = tVibrate;

		// �]�wLED�O�G�P�t���ɶ��P�C��A�ó]�wflags�q��
		tBNot.ledARGB = 0xff00ff00;
		tBNot.ledOnMS = 300;
		tBNot.ledOffMS = 1000;
		tBNot.flags |= Notification.FLAG_SHOW_LIGHTS;

		Intent notificationIntent = new Intent(mContext,
				ImageGridFragment.class);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				notificationIntent, 0);
		tBNot.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), mContext.getString(R.string.donwload_notification_msg),
				contentIntent);
		gNotMgr.notify(1, tBNot);
	}
}
