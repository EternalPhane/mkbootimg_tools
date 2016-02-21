package com.github.mkbootimg.project;

import java.io.File;
import java.io.FileNotFoundException;

import com.github.mkbootimg.App;
import com.github.mkbootimg.util.RootThread;

import android.os.Handler;
import android.os.Message;

public class BootImgProject {
	private File workspace = null;
	private File bootImg = null;
	private File bootFolder = null;
	private File newBootImg = null;
	private String consoleResult = null;
	private static final String SCRIPT_PATH = App.getAppContext().getFilesDir().getAbsolutePath();

	public BootImgProject(String workspace, String bootImg) throws FileNotFoundException {
		this.setWorkspace(workspace);
		this.setBootImg(bootImg);
		if (!this.bootImg.exists()) {
			throw new FileNotFoundException("Path: " + bootImg);
		}
	}

	public File getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = new File(workspace);
	}

	public File getBootImg() {
		return bootImg;
	}

	public void setBootImg(String bootImg) {
		this.bootImg = new File(bootImg);
	}

	public File getBootFolder() {
		return bootFolder;
	}

	public void setBootFolder(String bootFolder) {
		this.bootFolder = new File(bootFolder);
	}

	public File getNewBootImg() {
		return newBootImg;
	}

	public void setNewBootImg(String newBootImg) {
		this.newBootImg = new File(newBootImg);
	}

	/** Splits the boot image. */
	public void splitBootImg() {
		mkBoot(false);
	}

	/** Pack the boot folder to image. */
	public void packBootImg() {
		mkBoot(true);
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
				consoleResult = msg.getData().getString("result");
			}
		});
		final Handler handler = thread.getRootHandler();
		final Message m = handler.obtainMessage();
		if (pack) {
			m.getData().putString("command",
					SCRIPT_PATH + "/mkboot " + bootImg.getAbsolutePath() + " " + bootFolder.getAbsolutePath());
		} else {
			m.getData().putString("command",
					SCRIPT_PATH + "/mkboot " + bootFolder.getAbsolutePath() + " " + newBootImg.getAbsolutePath());
		}
		handler.sendMessage(m);
	}
}
