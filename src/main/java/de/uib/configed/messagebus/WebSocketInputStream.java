package de.uib.configed.messagebus;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import de.uib.utilities.logging.Logging;

public final class WebSocketInputStream {
	private static WebSocketInputStream instance;

	private static PipedOutputStream writer;
	private static PipedInputStream reader;
	private static boolean connected;

	private WebSocketInputStream() {
	}

	public static WebSocketInputStream getInstance() {
		if (instance == null) {
			instance = new WebSocketInputStream();
		}

		if (writer == null) {
			writer = new PipedOutputStream();
		}

		if (reader == null) {
			reader = new PipedInputStream();
		}

		if (!connected) {
			connect();
		}

		return instance;
	}

	private static void connect() {
		try {
			reader.connect(writer);
			connected = true;
		} catch (IOException e) {
			e.printStackTrace();
			Logging.error(WebSocketInputStream.class, "connecting reader with writer, when they're connected");
		}
	}

	public void write(byte[] message) throws IOException {
		writer.write(message);
		writer.flush();
	}

	public PipedInputStream getReader() {
		return reader;
	}

	public static void close() throws IOException {
		writer.close();
		reader.close();

		instance = null;
		writer = null;
		reader = null;
		connected = false;
	}
}
