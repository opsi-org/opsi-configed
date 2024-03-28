/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import de.uib.utilities.logging.Logging;

public abstract class AbstractBackgroundFileUploader extends SwingWorker<Void, Integer> {
	private TerminalFrame terminal;

	protected File currentFile;
	private int totalFilesToUpload;
	protected int uploadedFiles;

	private boolean visualizeProgress;

	protected Runnable callback;

	protected AbstractBackgroundFileUploader(TerminalFrame terminal) {
		this(terminal, null);
	}

	protected AbstractBackgroundFileUploader(TerminalFrame terminal, Runnable callback) {
		this(terminal, false, callback);
	}

	protected AbstractBackgroundFileUploader(TerminalFrame terminal, boolean visualizeProgress, Runnable callback) {
		this.terminal = terminal;
		this.visualizeProgress = visualizeProgress;
		this.callback = callback;
	}

	@Override
	protected void process(List<Integer> chunkSizes) {
		if (!visualizeProgress) {
			return;
		}
		for (Integer chunkSize : chunkSizes) {
			if (currentFile == null) {
				return;
			}

			try {
				terminal.updateFileUploadProgressBar(chunkSize, (int) Files.size(currentFile.toPath()));
			} catch (IOException e) {
				Logging.warning(this, "unable to retrieve file size: ", e);
			}
		}
	}

	@Override
	protected Void doInBackground() {
		upload();
		return null;
	}

	protected abstract void upload();

	@Override
	protected void done() {
		if (visualizeProgress) {
			terminal.showFileUploadProgress(false);
		}
		totalFilesToUpload = 0;
		uploadedFiles = 0;
		if (callback != null) {
			callback.run();
		}
	}

	public int getTotalFilesToUpload() {
		return totalFilesToUpload;
	}

	public void setTotalFilesToUpload(int totalFiles) {
		this.totalFilesToUpload = totalFiles;
	}

	public void updateTotalFilesToUpload() {
		SwingUtilities.invokeLater(() -> terminal.indicateFileUpload(currentFile, uploadedFiles, totalFilesToUpload));
	}
}
