/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import de.uib.utilities.logging.Logging;

public class FileUpload extends DropTarget {
	private FileUploadQueue queue;
	private MessagebusBackgroundFileUploader fileUploader;
	private TerminalFrame terminal;
	private TerminalWidget terminalWidget;

	public FileUpload(TerminalFrame terminal, TerminalWidget terminalWidget) {
		this.terminal = terminal;
		this.terminalWidget = terminalWidget;
		this.queue = new FileUploadQueue();
		this.fileUploader = new MessagebusBackgroundFileUploader(terminal, terminalWidget, queue);
	}

	@SuppressWarnings("unchecked")
	private List<File> getDroppedFiles(DropTargetDropEvent e) {
		List<File> droppedFiles = null;

		try {
			droppedFiles = (List<File>) e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
			Logging.info(this, "dropped files: " + droppedFiles);
		} catch (UnsupportedFlavorException ex) {
			Logging.warning(this, "this should not happen, unless javaFileListFlavor is no longer supported: " + ex);
		} catch (IOException ex) {
			Logging.warning(this, "cannot retrieve dropped file: ", ex);
		}

		return droppedFiles;
	}

	@Override
	public synchronized void drop(DropTargetDropEvent e) {
		e.acceptDrop(DnDConstants.ACTION_COPY);
		List<File> files = getDroppedFiles(e);

		if (files == null || files.isEmpty()) {
			Logging.info(this, "files are null or empty: " + files);
			return;
		}

		queue.addAll(files);

		if (fileUploader.isDone()) {
			fileUploader = new MessagebusBackgroundFileUploader(terminal, terminalWidget, queue);
			fileUploader.setTotalFilesToUpload(fileUploader.getTotalFilesToUpload() + files.size());
		} else {
			fileUploader.setTotalFilesToUpload(fileUploader.getTotalFilesToUpload() + files.size());
			fileUploader.updateTotalFilesToUpload();
		}

		fileUploader.execute();
	}
}
