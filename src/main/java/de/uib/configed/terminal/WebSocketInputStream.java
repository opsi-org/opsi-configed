/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import de.uib.utilities.logging.Logging;

public final class WebSocketInputStream {
	private static PipedOutputStream writer;
	private static PipedInputStream reader;

	private WebSocketInputStream() {
	}

	public static void init() {
		writer = new PipedOutputStream();
		reader = new PipedInputStream();

		try {
			reader.connect(writer);
		} catch (IOException e) {
			Logging.warning("I/O error occured while connecting reader with writer: ", e.getMessage());
		}
	}

	public static void write(byte[] message) throws IOException {
		writer.write(message);
		writer.flush();
	}

	public static PipedInputStream getReader() {
		return reader;
	}

	public static void close() throws IOException {
		writer.close();
		reader.close();

		writer = null;
		reader = null;
	}
}
