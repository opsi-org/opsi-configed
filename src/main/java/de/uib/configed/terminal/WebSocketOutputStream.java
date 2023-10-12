/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;

public class WebSocketOutputStream extends OutputStream {
	private final WebSocket webSocket;

	public WebSocketOutputStream(WebSocket webSocket) {
		this.webSocket = webSocket;
	}

	@Override
	public void write(int b) {
		webSocket.send(new byte[] { (byte) b });
	}

	@Override
	public void write(byte[] b) {
		webSocket.send(b);
	}

	@Override
	public void write(byte[] b, int off, int len) {
		webSocket.send(ByteBuffer.wrap(b, off, len));
	}
}
