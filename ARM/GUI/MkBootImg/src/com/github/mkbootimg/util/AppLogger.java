package com.github.mkbootimg.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import android.util.Log;

public class AppLogger {
	private static final String TAG = AppLogger.class.getSimpleName();
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
	private File mLogFile = null;
	private char[] mBuffer = null;
	private int mBufferLength = 0;

	public AppLogger(String filePath) {
		this(filePath, DEFAULT_BUFFER_SIZE);
	}

	public AppLogger(String filePath, int mBufferSize) {
		mLogFile = new File(filePath);
		mBuffer = new char[mBufferSize];
	}

	public AppLogger write(CharSequence text) {
		if (text != null) {
			final int length = text.length();
			for (int i = 0; i < length; i++) {
				try {
					mBuffer[mBufferLength++] = text.charAt(i);
				} catch (ArrayIndexOutOfBoundsException e) {
					flush();
					i--;
				}
			}
		}
		return this;
	}

	public AppLogger writeLine() {
		write("\n");
		return this;
	}

	public AppLogger writeLine(CharSequence text) {
		write(text).writeLine();
		return this;
	}

	public AppLogger logStackTrace(Exception e) {
		write(Log.getStackTraceString(e));
		return this;
	}

	public void flush() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(mLogFile, true));
			writer.append(new String(mBuffer, 0, mBufferLength));
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e(TAG, "IO error. File name: " + mLogFile.getAbsolutePath());
			e.printStackTrace();
		}
		mBufferLength = 0;
	}
}
