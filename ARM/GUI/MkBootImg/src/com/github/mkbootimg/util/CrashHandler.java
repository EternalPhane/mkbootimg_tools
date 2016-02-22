package com.github.mkbootimg.util;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import com.github.mkbootimg.App;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class CrashHandler implements UncaughtExceptionHandler {
	private static final String TAG = CrashHandler.class.getSimpleName();
	private Context mContext = null;
	private Thread.UncaughtExceptionHandler mDefaultHandler = null;
	private String mCrashReportPath = null;

	private CrashHandler() {
	}

	private static class CrashHandlerHolder {
		private static final CrashHandler instance = new CrashHandler();
	}

	public static CrashHandler getInstance() {
		return CrashHandlerHolder.instance;
	}

	public void init(Context context) {
		mContext = context;
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		StringBuilder path = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			path = (new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath()))
					.append(File.separator).append("mkbootimg");
		} else {
			path = new StringBuilder(mContext.getFilesDir().getParent());
		}
		path.append(File.separator).append("crash-reports");
		(new File(path.toString())).mkdirs();
		mCrashReportPath = path.append(File.separator).append("crash-")
				.append(DateFormat.format("yyyy-MM-dd_HH.mm.ss", new Date())).append(".log").toString();
	}

	private String getDeviceInfo() {
		final StringBuilder sb = new StringBuilder();
		try {
			PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),
					PackageManager.GET_META_DATA);
			sb.append("App Package Name: ").append(mContext.getPackageName());
			sb.append("\nApp Version Name: ").append(pInfo.versionName);
			sb.append("\nApp Version Code: ").append(pInfo.versionCode);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "An error occured when geting device info.", e);
		}
		sb.append("\n\nOS Version: ").append(System.getProperty("os.version")).append(" (")
				.append(android.os.Build.VERSION.INCREMENTAL).append(")");
		sb.append("\nOS API Level: ").append(android.os.Build.VERSION.SDK_INT);
		sb.append("\nDevice: ").append(android.os.Build.DEVICE);
		sb.append("\nModel (Product): ").append(android.os.Build.MODEL).append("( ").append(android.os.Build.PRODUCT)
				.append(")");
		sb.append("\nManufacturer: ").append(android.os.Build.MANUFACTURER);
		sb.append("\nSupported ABIs: ");
		for (String str : android.os.Build.SUPPORTED_ABIS) {
			sb.append(str).append(", ");
		}
		sb.setLength(sb.length() - 2);
		sb.append("\nOther Tags: ").append(android.os.Build.TAGS);
		return sb.toString();
	}

	private void generateCrashReport(Throwable ex) {
		final AppLogger crashReport = new AppLogger(mCrashReportPath);
		crashReport.writeLine("=====MkBootImg Crash Report=====");
		crashReport.write("Time: ").writeLine(DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date()));
		crashReport.write("Description: ").writeLine(ex.getMessage()).writeLine();
		crashReport.writeLine("-----Details-----");
		crashReport.writeLine(Log.getStackTraceString(ex)).writeLine();
		crashReport.writeLine("-----Device Info-----");
		crashReport.write(getDeviceInfo());
		crashReport.flush();
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		new Thread() {
			@Override
			public void run() {
				final Toast toast = new Toast(mContext);
				toast.setText("Fatal error has occured and the application is being terminated.\n"
						+ "Please check the crash report: " + mCrashReportPath);
				toast.setDuration(Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}.start();
		generateCrashReport(ex);
		return true;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			((App) mContext).onTerminate();
		}
	}
}
