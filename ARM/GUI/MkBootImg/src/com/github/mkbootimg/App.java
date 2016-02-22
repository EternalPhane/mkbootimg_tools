package com.github.mkbootimg;

import java.util.ArrayList;
import java.util.List;

import com.github.mkbootimg.util.CrashHandler;
import com.github.mkbootimg.util.RootThread;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class App extends Application {
	private static Context sContext = null;
	private List<Activity> mActivities = new ArrayList<Activity>();

	public void addActivity(Activity activity) {
		mActivities.add(activity);
	}

	public static Context getAppContext() {
		return sContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sContext = getApplicationContext();
		CrashHandler.getInstance().init(sContext);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		for (Activity activity : mActivities) {
			activity.finish();
			mActivities.remove(activity);
		}
		RootThread.getInstance().quitLooper();
		System.exit(0);
	}
}
