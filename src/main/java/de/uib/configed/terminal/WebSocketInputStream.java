/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import de.uib.utils.logging.Logging;

public class WebSocketInputStream {
	private PipedOutputStream writer;
	private PipedInputStream reader;

	public WebSocketInputStream() {
		writer = new PipedOutputStream();
		reader = new PipedInputStream();

		try {
			reader.connect(writer);
		} catch (IOException e) {
			Logging.error("I/O error occured while connecting reader with writer: " + e.getMessage());
		}
	}

	public void write(byte[] message) throws IOException {
		writer.write(message);
		writer.flush();
	}

	public PipedInputStream getReader() {
		return reader;
	}

	public void close() throws IOException {
		writer.close();
		reader.close();
	}
}
