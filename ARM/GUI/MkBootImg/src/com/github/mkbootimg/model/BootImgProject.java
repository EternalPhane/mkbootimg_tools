package com.github.mkbootimg.model;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.mkbootimg.App;
import com.github.mkbootimg.util.AppLogger;
import com.github.mkbootimg.util.RootThread;

import android.os.Handler;
import android.os.Message;

public class BootImgProject {
	private static final String SCRIPT_PATH = App.getAppContext().getFilesDir().getAbsolutePath();
	private File mWorkspace = null;
	private File mBootImg = null;
	private File mBootFolder = null;
	private File mNewBootImg = null;
	private String mConsoleResult = null;
	private AppLogger mLog = null;

	public BootImgProject(String workspace, String bootImgPath) throws FileNotFoundException {
		setWorkspace(workspace);
		setBootImg(bootImgPath);
		mLog = new AppLogger(mWorkspace.getAbsolutePath() + "/mkbootimg.log");
		if (!mBootImg.exists()) {
			throw new FileNotFoundException("Path: " + mBootImg);
		}
	}

	public File getWorkspace() {
		return mWorkspace;
	}

	public void setWorkspace(String path) {
		mWorkspace = new File(path);
	}

	public File getBootImg() {
		return mBootImg;
	}

	public void setBootImg(String path) {
		mBootImg = new File(path);
	}

	public File getBootFolder() {
		return mBootFolder;
	}

	public void setBootFolder(String path) {
		mBootFolder = new File(path);
	}

	public File getNewBootImg() {
		return mNewBootImg;
	}

	public void setNewBootImg(String path) {
		mNewBootImg = new File(path);
	}

	/** Splits the boot image. */
	public void splitBootImg() {
		mkBoot(false);
		mLog.writeLine("=====spliting the image ...=====");
		mLog.writeLine(mConsoleResult);
	}

	/** Pack the boot folder to image. */
	public void packBootImg() {
		mkBoot(true);
		mLog.writeLine("=====packing the image ...=====");
		mLog.writeLine(mConsoleResult);
	}

	/**
	 * Executes ARM/mkboot script.
	 * 
	 * @param pack
	 *            Pack or Split the boot image
	 */
	// TODO: Rewrite this method after converted ARM/mkboot and ARM/wrapper to
	// java code.
	private void mkBoot(boolean pack) {
		final RootThread thread = RootThread.getInstance(new Handler() {
			@Override
			public void handleMessage(Message msg) {
				mConsoleResult = msg.getData().getString("result");
			}
		});
		final Handler handler = thread.getRootHandler();
		final Message m = handler.obtainMessage();
		if (pack) {
			m.getData().putString("command",
					SCRIPT_PATH + "/mkboot " + mBootImg.getAbsolutePath() + " " + mBootFolder.getAbsolutePath());
		} else {
			m.getData().putString("command",
					SCRIPT_PATH + "/mkboot " + mBootFolder.getAbsolutePath() + " " + mNewBootImg.getAbsolutePath());
		}
		handler.sendMessage(m);
	}
}
