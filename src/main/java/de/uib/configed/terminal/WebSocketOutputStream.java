package de.uib.configed.messagebus;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;

import de.uib.utilities.logging.Logging;

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

	@Override
	public void close() {
		if (!webSocket.isClosed() && !webSocket.isClosing()) {
			webSocket.close();
		} else {
			Logging.info(this, "websocket is closed");
		}
	}
}
