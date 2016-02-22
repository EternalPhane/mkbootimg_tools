package com.github.mkbootimg.util;

import java.util.List;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import eu.chainfire.libsuperuser.Shell;

public class RootThread extends Thread {
	private static final String TAG = RootThread.class.getSimpleName();
	private Shell.Interactive mRootSession = null;
	private Handler mHandler = null;
	private Handler mRootHandler;
	private boolean mIdle = true;
	private StringBuilder mResult = new StringBuilder();

	private RootThread() {
	}

	private static class RootThreadHolder {
		private static final RootThread instance = new RootThread();
	}

	public static RootThread getInstance() {
		return RootThreadHolder.instance;
	}

	public static RootThread getInstance(Handler h) {
		while (!RootThreadHolder.instance.mIdle) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				Log.e(TAG, "Interrupted!");
				e.printStackTrace();
				break;
			}
		}
		RootThreadHolder.instance.mHandler = h;
		return getInstance();
	}

	public Handler getRootHandler() {
		return mRootHandler;
	}

	public void quitLooper() {
		Looper myLooper = Looper.myLooper();
		if (myLooper != null) {
			myLooper.quit();
		}
	}

	private void openRootShell() {
		mRootSession = new Shell.Builder().useSU().setWantSTDERR(true).setWatchdogTimeout(20)
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
		mResult.setLength(0);
		mRootSession.addCommand(command, 1, new Shell.OnCommandLineListener() {
			@Override
			public void onLine(String line) {
				mResult.append(line).append((char) 10);
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
		mIdle = false;
		mRootHandler = new Handler() {
			@Override
			public void handleMessage(Message mCommand) {
				final String command = mCommand.getData().getString("command");
				Log.i(TAG, "Received command: \n" + command);
				sendRootCommand(command);
				final Message mResult = mHandler.obtainMessage();
				final Bundle bResult = mResult.getData();
				bResult.putString("mResult", mResult.toString());
				mResult.setData(bResult);
				mHandler.sendMessage(mResult);
				mIdle = true;
			}
		};
		Looper.loop();
	}
}
