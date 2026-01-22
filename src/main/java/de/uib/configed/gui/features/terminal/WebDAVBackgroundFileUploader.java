/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.terminal;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.uib.configed.share.WebDAVClient;
import de.uib.configed.share.logging.Logging;

public class WebDAVBackgroundFileUploader extends AbstractBackgroundFileUploader {
	private String destinationDir;
	private boolean visualizeProgress;

	public WebDAVBackgroundFileUploader(TerminalFrame terminal, File file, String destinationDir,
			boolean visualizeProgress) {
		super(terminal, visualizeProgress);
		this.currentFile = file;
		this.destinationDir = destinationDir;
		this.visualizeProgress = visualizeProgress;
	}

	@Override
	protected void upload() {
		try (InputStream inputStream = new ProgressTrackerInputStream(
				new BufferedInputStream(new FileInputStream(currentFile)))) {
			uploadedFiles += 1;
			if (visualizeProgress) {
				updateTotalFilesToUpload();
			}
			WebDAVClient webDAVClient = new WebDAVClient();
			webDAVClient.uploadFile(destinationDir + "/" + currentFile.getName(), inputStream);
			isFileUploadSuccessfull = true;
		} catch (IOException e) {
			isFileUploadSuccessfull = false;
			Logging.error(this, e, "Unable to upload file to a server through WebDAV");
		}
	}

	@SuppressWarnings({ "java:S2972" })
	private class ProgressTrackerInputStream extends FilterInputStream {
		private int totalBytesRead;

		ProgressTrackerInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public int read() throws IOException {
			if (isCancelled()) {
				Logging.info(this, "File upload stopped");
				return -1;
			}
			int bytesRead = super.read();
			if (bytesRead != -1) {
				totalBytesRead++;
				publish(totalBytesRead);
			}
			return bytesRead;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (isCancelled()) {
				Logging.info(this, "File upload stopped");
				return -1;
			}
			int bytesRead = super.read(b, off, len);
			if (bytesRead != -1) {
				totalBytesRead += bytesRead;
				publish(totalBytesRead);
			}
			return bytesRead;
		}
	}
}
