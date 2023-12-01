/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;

import de.uib.messagebus.WebSocketEvent;
import de.uib.utilities.logging.Logging;

public class WebSocketTtyConnector implements TtyConnector {
	private BufferedReader reader;
	private BufferedOutputStream writer;
	private TerminalWidget terminalWidget;

	public WebSocketTtyConnector(TerminalWidget terminalWidget, OutputStream outputStream,
			WebSocketInputStream inputStream) {
		this.terminalWidget = terminalWidget;
		this.writer = new BufferedOutputStream(outputStream);
		this.reader = new BufferedReader(new InputStreamReader(inputStream.getReader(), StandardCharsets.UTF_8));
	}

	@Override
	public void close() {
		try {
			if (reader != null) {
				reader.close();
				reader = null;
			}
			if (writer != null) {
				writer.close();
				writer = null;
			}
		} catch (IOException e) {
			Logging.warning(this, "failed to close output/input stream: " + e);
		}
	}

	@Override
	public String getName() {
		return terminalWidget.getSessionChannel();
	}

	@Override
	public void resize(TermSize termSize) {
		Map<String, Object> data = new HashMap<>();
		data.put("type", WebSocketEvent.TERMINAL_RESIZE_REQUEST.toString());
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", "@");
		data.put("channel", terminalWidget.getTerminalChannel());
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("terminal_id", terminalWidget.getTerminalId());
		data.put("rows", termSize.getRows());
		data.put("cols", termSize.getColumns());

		try {
			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			writer.write(dataJsonBytes);
			writer.flush();
		} catch (IOException ex) {
			Logging.warning(this, "cannot resize terminal window: ", ex);
		}
	}

	@Override
	public int read(char[] buf, int offset, int length) throws IOException {
		return reader.read(buf, offset, length);
	}

	@Override
	public void write(byte[] bytes) {
		if (terminalWidget.ignoreKeyEvent()) {
			return;
		}

		Map<String, Object> data = new HashMap<>();
		data.put("type", WebSocketEvent.TERMINAL_DATA_WRITE.toString());
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", "@");
		data.put("channel", terminalWidget.getTerminalChannel());
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("terminal_id", terminalWidget.getTerminalId());
		data.put("data", bytes);

		try {
			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			writer.write(dataJsonBytes);
			writer.flush();
		} catch (IOException ex) {
			Logging.warning("cannot send message to server: ", ex);
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
