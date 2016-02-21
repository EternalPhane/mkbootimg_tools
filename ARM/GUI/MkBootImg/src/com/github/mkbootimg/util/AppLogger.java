package com.github.mkbootimg.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class AppLogger {
	private static final String TAG = AppLogger.class.getSimpleName();
	private static final int DEFAULT_BUFFER_SIZE = 16384;
	private File logFile = null;
	private char[] buffer = null;
	private int bufferLength = 0;

	public AppLogger(String fileName) {
		this(fileName, DEFAULT_BUFFER_SIZE);
	}

	public AppLogger(String fileName, int bufferSize) {
		logFile = new File(fileName);
		buffer = new char[bufferSize];
	}

	public AppLogger write(String text) {
		if (text != null) {
			for (int i = 0, length = text.length(); i < length; i++) {
				try {
					buffer[bufferLength + i] = text.charAt(i);
				} catch (ArrayIndexOutOfBoundsException e) {
					try {
						BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
						writer.append(buffer.toString());
						writer.flush();
						writer.close();
					} catch (IOException ex) {
						Log.e(TAG, "IO error. File name: " + logFile.getAbsolutePath());
						ex.printStackTrace();
					}
					bufferLength = 0;
					i--;
				}
			}
		}
		return this;
	}

	public AppLogger writeLine(String text) {
		write(text).write("\n");
		return this;
	}

	public void flush() {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
			writer.append(buffer.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Log.e(TAG, "IO error. File name: " + logFile.getAbsolutePath());
			e.printStackTrace();
		}
		bufferLength = 0;
	}
}
