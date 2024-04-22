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

import de.uib.utils.logging.Logging;

public abstract class AbstractBackgroundFileUploader extends SwingWorker<Void, Integer> {
	private TerminalFrame terminal;

	protected File currentFile;
	private int totalFilesToUpload;
	protected int uploadedFiles;
	protected boolean isFileUploadSuccessfull;

	private boolean visualizeProgress;

	protected Runnable callback;

	protected AbstractBackgroundFileUploader(TerminalFrame terminal) {
		this(terminal, false);
	}

	protected AbstractBackgroundFileUploader(TerminalFrame terminal, boolean visualizeProgress) {
		this.terminal = terminal;
		this.visualizeProgress = visualizeProgress;
	}

	public void setOnDone(Runnable callback) {
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
				terminal.getTerminalFileUploadProgressIndicator().updateFileUploadProgressBar(chunkSize,
						(int) Files.size(currentFile.toPath()));
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
			terminal.getTerminalFileUploadProgressIndicator().showFileUploadProgress(false);
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
		SwingUtilities.invokeLater(() -> terminal.getTerminalFileUploadProgressIndicator()
				.indicateFileUpload(currentFile, uploadedFiles, totalFilesToUpload));
	}

	public boolean isFileUploaded() {
		return isFileUploadSuccessfull;
	}
}
