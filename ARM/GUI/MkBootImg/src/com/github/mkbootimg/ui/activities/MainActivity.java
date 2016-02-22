package com.github.mkbootimg.ui.activities;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import com.github.mkbootimg.App;
import com.github.mkbootimg.R;
import com.github.mkbootimg.util.RootThread;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends Activity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private Startup mStartup = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		((App) getApplication()).addActivity(this);
		Log.i(TAG, "Creating and starting Startup ...");
		mStartup = new Startup();
		mStartup.setContext(this).execute();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mStartup.cancel(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class Startup extends AsyncTask<Void, String, Void> {
		private static final int BUFFER_SIZE = 16 * 1024;
		private boolean mSuAvailable = false;
		private ProgressDialog mDialog = null;
		private Context mContext = null;
		private Resources mRes = null;

		public Startup setContext(Context context) {
			this.mContext = context;
			mRes = context.getResources();
			return this;
		}

		private void outputRawToFile(int id) {
			final String name = mRes.getResourceEntryName(id);
			final InputStream is = mRes.openRawResource(id);
			try {
				final FileOutputStream os = mContext.openFileOutput(name, MODE_PRIVATE);
				byte[] buffer = new byte[BUFFER_SIZE];
				int read = 0;
				while ((read = is.read(buffer)) > 0) {
					os.write(buffer, 0, read);
				}
				os.close();
				is.close();
			} catch (FileNotFoundException e) {
				Log.e(TAG, "Data directory not found.");
			} catch (IOException e) {
				Log.e(TAG, "IO error. Resource name: " + name + "\nMessage: " + e.getMessage());
			}
			if (!mContext.getFileStreamPath(name).setExecutable(true, true)) {
				Log.e(TAG, "Failed to set executable. Resource name: " + name);
			}
		}

		@Override
		protected void onPreExecute() {
			mDialog = new ProgressDialog(mContext);
			mDialog.setMessage("Checking for root ...");
			mDialog.setIndeterminate(true);
			mDialog.setCancelable(false);
			mDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (android.os.Debug.isDebuggerConnected()) {
				android.os.Debug.waitForDebugger();
			}
			mSuAvailable = Shell.SU.available();
			if (mSuAvailable) {
				publishProgress("Initializing ...");
				final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
				if (!prefs.getBoolean("extracted", false)) {
					final R.raw rawRes = new R.raw();
					for (Field f : R.raw.class.getDeclaredFields()) {
						try {
							outputRawToFile(f.getInt(rawRes));
						} catch (Exception e) {
							continue;
						}
					}
					prefs.edit().putBoolean("extracted", true);
				}
				final RootThread thread = RootThread.getInstance();
				thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
					@Override
					public void uncaughtException(Thread thread, Throwable ex) {
						Log.e(TAG, "RootThread error.");
					}
				});
				try {
					thread.start();
				} catch (IllegalThreadStateException e) {
					Log.e(TAG, "RootThread already exists.");
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mDialog.dismiss();
			if (!mSuAvailable) {
				final Toast toast = new Toast(mContext);
				toast.setText("Root privilege is needed!");
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				((App) getApplication()).onTerminate();
			}
		}

		@Override
		protected void onProgressUpdate(String... values) {
			mDialog.setMessage(values[0]);
		}
	}
}
