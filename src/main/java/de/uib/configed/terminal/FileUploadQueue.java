/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.File;
import java.util.concurrent.LinkedBlockingDeque;

import de.uib.utilities.logging.Logging;

public class FileUploadQueue {
	private static final LinkedBlockingDeque<File> queue = new LinkedBlockingDeque<>();

	public void add(File file) {
		if (file == null) {
			Logging.info(this, "file is null");
			return;
		}

		try {
			queue.put(file);
		} catch (InterruptedException e) {
			Logging.warning(this, "thread was interrupted");
			Thread.currentThread().interrupt();
		}
	}

	public void addAll(Iterable<File> files) {
		for (File file : files) {
			add(file);
		}
	}

	public File get() {
		return queue.peek();
	}

	public int size() {
		return queue.size();
	}

	public boolean remove(File file) {
		return queue.remove(file);
	}

	public void clear() {
		queue.clear();
	}
}
