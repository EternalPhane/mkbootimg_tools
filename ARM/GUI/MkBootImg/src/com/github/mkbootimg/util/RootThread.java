package com.github.mkbootimg.util;

import java.util.List;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import eu.chainfire.libsuperuser.Shell;

public class RootThread extends Thread {
	private final static String TAG = RootThread.class.getSimpleName();
	private static RootThread singleton = null;
	private Shell.Interactive rootSession = null;
	private Handler mHandler = null;
	private Handler rootHandler;
	private boolean idle = true;
	private StringBuilder result = new StringBuilder();

	private RootThread() {
	}

	public static RootThread getInstance() {
		if (singleton == null) {
			singleton = new RootThread();
		}
		return singleton;
	}

	public static RootThread getInstance(Handler h) {
		while (!singleton.idle) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Log.e(TAG, "Interrupted!");
				e.printStackTrace();
				break;
			}
		}
		singleton.mHandler = h;
		return getInstance();
	}

	public Handler getRootHandler() {
		return rootHandler;
	}

	public void quitLooper() {
		Looper myLooper = Looper.myLooper();
		if (myLooper != null) {
			myLooper.quit();
		}
	}

	private void openRootShell() {
		rootSession = new Shell.Builder().useSU().setWantSTDERR(true).setWatchdogTimeout(20)
				.open(new Shell.OnCommandResultListener() {
					@Override
					public void onCommandResult(int commandCode, int exitCode, List<String> output) {
						if (exitCode != Shell.OnCommandResultListener.SHELL_RUNNING) {
							Log.e(TAG, "Error opening root shell. Exit code: " + exitCode);
							// TODO: Replace RuntimeException after defined
							// custom exception class.
							throw new RuntimeException();
						}
					}
				});
	}

	private void sendRootCommand(String command) {
		result.setLength(0);
		rootSession.addCommand(command, 1, new Shell.OnCommandLineListener() {
			@Override
			public void onLine(String line) {
				result.append(line).append((char) 10);
			}

			@Override
			public void onCommandResult(int commandCode, int exitCode) {
				if (exitCode < Shell.OnCommandLineListener.SHELL_RUNNING) {
					Log.e(TAG, "Error executing commands. Exit code: " + exitCode);
					// TODO: Replace RuntimeException after defined custom
					// exception class.
					throw new RuntimeException();
				}
			}
		});
	}

	@Override
	public void run() {
		openRootShell();
		Looper.prepare();
		idle = false;
		rootHandler = new Handler() {
			@Override
			public void handleMessage(Message mCommand) {
				final String command = mCommand.getData().getString("command");
				Log.i(TAG, "Received command: \n" + command);
				sendRootCommand(command);
				final Message mResult = mHandler.obtainMessage();
				final Bundle bResult = mResult.getData();
				bResult.putString("result", result.toString());
				mResult.setData(bResult);
				mHandler.sendMessage(mResult);
				idle = true;
			}
		};
		Looper.loop();
	}
}
