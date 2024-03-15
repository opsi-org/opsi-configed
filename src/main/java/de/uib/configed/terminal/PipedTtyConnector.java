/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.nio.charset.StandardCharsets;

import com.jediterm.terminal.TtyConnector;

import de.uib.utilities.logging.Logging;

public class PipedTtyConnector implements TtyConnector {
	private PipedReader reader;
	private PipedWriter writer;
	private TerminalWidget terminalWidget;

	public PipedTtyConnector(TerminalWidget terminalWidget) {
		this.terminalWidget = terminalWidget;
		try {
			this.writer = new PipedWriter();
			this.reader = new PipedReader(writer);
		} catch (IOException e) {
			Logging.error("failed to connect piped writer with piped reader", e);
		}
	}

	@Override
	public void close() {
		try {
			if (writer != null) {
				writer.close();
				writer = null;
			}
			if (reader != null) {
				reader.close();
				reader = null;
			}
		} catch (IOException e) {
			Logging.warning(this, "failed to close output/input stream: " + e);
		}
	}

	@Override
	public String getName() {
		return terminalWidget.getTitle();
	}

	@Override
	public int read(char[] buf, int offset, int length) throws IOException {
		return reader.read(buf, offset, length);
	}

	@Override
	public void write(byte[] bytes) {
		if (terminalWidget.disableUserInput()) {
			return;
		}

		try {
			writer.write(new String(bytes));
			writer.flush();
		} catch (IOException e) {
			Logging.error(this, "failed to write bytes", e);
		}
	}

	@Override
	public boolean isConnected() {
		return writer != null && reader != null;
	}

	@Override
	public void write(String string) throws IOException {
		write(string.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public int waitFor() {
		return 0;
	}

	@Override
	public boolean ready() throws IOException {
		return reader.ready();
	}
}
