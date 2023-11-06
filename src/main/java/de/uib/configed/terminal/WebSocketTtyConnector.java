/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.Dimension;
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
import com.jediterm.terminal.Questioner;
import com.jediterm.terminal.TtyConnector;

import de.uib.messagebus.WebSocketEvent;
import de.uib.utilities.logging.Logging;

public class WebSocketTtyConnector implements TtyConnector {
	private final BufferedReader reader;
	private final BufferedOutputStream writer;
	private final Terminal terminal;

	public WebSocketTtyConnector(Terminal terminal, OutputStream outputStream, WebSocketInputStream inputStream) {
		this.terminal = terminal;
		this.writer = new BufferedOutputStream(outputStream);
		this.reader = new BufferedReader(new InputStreamReader(inputStream.getReader(), StandardCharsets.UTF_8));
	}

	@Override
	public boolean init(Questioner q) {
		return isConnected();
	}

	@Override
	public void close() {
		try {
			reader.close();
		} catch (IOException e) {
			Logging.warning(this, "failed to close output/input stream: " + e);
		}
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void resize(Dimension termWinSize) {
		Map<String, Object> data = new HashMap<>();
		data.put("type", WebSocketEvent.TERMINAL_RESIZE_REQUEST.toString());
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", "@");
		data.put("channel", terminal.getTerminalChannel());
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("terminal_id", terminal.getTerminalId());
		data.put("rows", terminal.getRowCount());
		data.put("cols", terminal.getColumnCount());

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
		Map<String, Object> data = new HashMap<>();
		data.put("type", WebSocketEvent.TERMINAL_DATA_WRITE.toString());
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", "@");
		data.put("channel", terminal.getTerminalChannel());
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("terminal_id", terminal.getTerminalId());
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
		return true;
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
