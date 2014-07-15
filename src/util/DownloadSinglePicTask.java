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

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class DownloadSinglePicTask extends AsyncTask<String, Integer, String> {

	public static int iFileSize = 0;
	public static double dReadSum = 0;
	public static boolean bIsDownload = false;
	private String[] albumPhotoUrlArray;
	private String[] albumNameArray;
	private String[] albumCoverUrlArray;
	private File wallpaperDirectory;
	private String downloadDirPath;
	private Context mContext;

	private static int count = 0;

	private String albumPhotoUrl;

	private ProgressDialog progressDialog;

	private WeakReference<View> viewReference;

	public void setView(View view) {
		viewReference = new WeakReference<View>(view);
	}

	public void setAlbunPhotoUrl(String url) {
		albumPhotoUrl = url;
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
	

		wallpaperDirectory = new File(downloadDirPath);

		wallpaperDirectory.mkdirs();

		super.onPreExecute();

	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);

		Integer v = values[0];

	}

	@Override
	protected void onPostExecute(String result) {

		super.onPostExecute(result);

		Toast.makeText(mContext, "download ok!", Toast.LENGTH_SHORT).show();

	}


	protected String doInBackground(String... strUrlFile) {

		try {

			OutputStream outStream = null;

			Bitmap bm;

			BitmapFactory.Options bmOptions;
			bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;
			bm = LoadImage(albumPhotoUrl, bmOptions);

			File file = new File(wallpaperDirectory,
					Utils.parsePicName(albumPhotoUrl) + ".JPEG");

			outStream = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
			outStream.flush();

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

}
