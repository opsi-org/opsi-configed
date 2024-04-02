/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.uib.utilities.WebDAVClient;
import de.uib.utilities.logging.Logging;

public class WebDAVBackgroundFileUploader extends AbstractBackgroundFileUploader {
	private String destinationDir;
	private boolean visualizeProgress;

	public WebDAVBackgroundFileUploader(TerminalFrame terminal, File file, String destinationDir,
			boolean visualizeProgress, Runnable callback) {
		super(terminal, visualizeProgress, callback);
		this.currentFile = file;
		this.destinationDir = destinationDir;
		this.visualizeProgress = visualizeProgress;
		this.callback = callback;
	}

	@Override
	protected void upload() {
		try (InputStream inputStream = new BufferedInputStream(new FileInputStream(currentFile))) {
			if (visualizeProgress) {
				updateTotalFilesToUpload();
			}
			ProgressTrackerInputStream progressTrackerInputStream = new ProgressTrackerInputStream(inputStream);
			WebDAVClient webDAVClient = new WebDAVClient();
			webDAVClient.uploadFile(destinationDir + "/" + currentFile.getName(), progressTrackerInputStream);
		} catch (IOException e) {
			Logging.error(this, "Unable to upload file to a server through WebDAV", e);
		}
	}

	@SuppressWarnings({ "java:S2972" })
	private class ProgressTrackerInputStream extends InputStream {
		private final InputStream inputStream;
		private int totalBytesRead;

		ProgressTrackerInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		@Override
		public int read() throws IOException {
			int bytesRead = inputStream.read();
			if (bytesRead != -1) {
				totalBytesRead++;
				publish(totalBytesRead);
			}
			return bytesRead;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			int bytesRead = inputStream.read(b, off, len);
			if (bytesRead != -1) {
				totalBytesRead += bytesRead;
				publish(totalBytesRead);
			}
			return bytesRead;
		}

		@Override
		public void close() throws IOException {
			inputStream.close();
		}
	}
}
