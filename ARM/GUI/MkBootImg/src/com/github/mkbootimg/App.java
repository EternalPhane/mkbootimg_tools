package com.github.mkbootimg;

import java.util.ArrayList;
import java.util.List;

import com.github.mkbootimg.util.RootThread;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class App extends Application {
	private static Context context = null;
	private List<Activity> activities = new ArrayList<Activity>();

	public void addActivity(Activity activity) {
		activities.add(activity);
	}

	public static Context getAppContext() {
		return context;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		context = getApplicationContext();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		for (Activity activity : activities) {
			activity.finish();
			activities.remove(activity);
		}
		RootThread.getInstance().quitLooper();
		System.exit(0);
	}
}
