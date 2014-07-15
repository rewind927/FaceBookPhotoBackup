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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fbbackup.ImageGridFragment;
import com.fbbackup.R;


public class DownloadTask extends AsyncTask<String, Integer, String> {

	public static int iFileSize = 0;
	public static double dReadSum = 0;
	public static boolean bIsDownload = false;

	// �ثe�U��̤j��
	private int downloadMax = 0;

	// �ثe�U��
	private int count = 0;

	private File wallpaperDirectory;
	private String downloadDirPath;
	private Context mContext;
	private float per;

	private ProgressBar pb;

	private TextView tx_pbr;

	private TextView tx_number;

	private WeakReference<View> viewReference;

	public void setDownloadMax(int max) {
		downloadMax = max;
	}

	public int getDownloadMax() {
		return downloadMax;
	}

	public void setView(View view) {
		viewReference = new WeakReference<View>(view);
	}

	public void setContext(Context c) {
		this.mContext = c;
	}

	public void setPath(String path) {
		this.downloadDirPath = path;
	}

	@Override
	protected void onPreExecute() {
		
		viewReference.get().findViewById(R.id.rl_prb_download).setVisibility(View.VISIBLE);

		pb = (ProgressBar) viewReference.get().findViewById(R.id.pb_download);

		tx_number = (TextView) viewReference.get().findViewById(R.id.tx_number);

		tx_pbr = (TextView) viewReference.get().findViewById(R.id.tx_pbr);

		pb.setProgress(0);

		tx_pbr.setText("0%");

		count = 0;

		tx_number.setText("0" + "/" + downloadMax);

		per = 0.0f;

		super.onPreExecute();

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);

		Integer v = values[0];

		pb.setProgress(v);

		tx_pbr.setText(v + "%");

		tx_number.setText(count + "/" + downloadMax);

	}

	@Override
	protected void onPostExecute(String result) {
		// ��method�O�bdoInBackground�����H��A�~�|�I�s��
		super.onPostExecute(result);

		pb.setProgress(100);

		Toast.makeText(mContext, mContext.getString(R.string.donwload_notification_msg), Toast.LENGTH_SHORT).show();

		viewReference.get().findViewById(R.id.rl_prb_download)
				.setVisibility(View.GONE);

		tx_pbr.setText("100%");

		tx_number.setText(downloadMax + "/" + downloadMax);

		addNotification();

	}

	/*
	 * �`�N!strUrlFile�O�@��array..�N��ۦb�I�s��class�ɥi�H�a�J�ܦh�ӰѼ�(strUrlFile)�A�p:new
	 * downloadTask().execute("http://www.xxx","http://www.xxx");
	 */
	protected String doInBackground(String... strUrlFile) {

		try {
			

			OutputStream outStream = null;

			// queue���h�֪F�F
			downloadMax = DownloadList.downloadPhotoQueue.size();

			// ��queue���٦�url���ɭԡA�~��U��
			while (!DownloadList.downloadPhotoQueue.isEmpty()) {

				if (isCancelled()){
					DownloadList.downloadPhotoQueue.clear();
					DownloadList.downloadUserNameQueue.clear();
					DownloadList.downloadPhotoAlbumQueue.clear();
					
					break;
				}

				// wallpaperDirectory = new File(downloadDirPath + pathArray[i]
				// + "/");

				wallpaperDirectory = new File(downloadDirPath
						+ DownloadList.downloadUserNameQueue.take() + "/"
						+ DownloadList.downloadPhotoAlbumQueue.take() + "/");

				wallpaperDirectory.mkdirs();

				Bitmap bm;

				BitmapFactory.Options bmOptions;
				bmOptions = new BitmapFactory.Options();
				bmOptions.inSampleSize = 1;

				String url = DownloadList.downloadPhotoQueue.take();

				bm = LoadImage(url, bmOptions);

				File file = new File(wallpaperDirectory,
						Utils.parsePicName(url) + ".JPEG");

				outStream = new FileOutputStream(file);
				bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
				outStream.flush();
				
				
				count=downloadMax-DownloadList.downloadPhotoQueue.size();

				per = ((float) count / (float) downloadMax) * 100.0f;

				publishProgress((int) per);
				
				outStream.close();

			}

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

	public void addNotification() {

		// ��l��Notification Manager
		NotificationManager gNotMgr = (NotificationManager) mContext
				.getSystemService(mContext.NOTIFICATION_SERVICE);

		// ����Notification����A�ó]�w���ݩ�
		Notification tBNot = new Notification(R.drawable.face_album,
				"download", System.currentTimeMillis());

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
