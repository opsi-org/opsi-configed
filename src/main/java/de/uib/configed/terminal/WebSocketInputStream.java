package de.uib.configed.messagebus;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import de.uib.utilities.logging.Logging;

public final class WebSocketInputStream {
	private static PipedOutputStream writer;
	private static PipedInputStream reader;
	private static boolean connected;

	private WebSocketInputStream() {
	}

	public static void write(byte[] message) throws IOException {
		writer.write(message);
		writer.flush();
	}

	public static PipedInputStream getReader() {
		if (!connected) {
			connect();
		}

		return reader;
	}

	private static void connect() {
		if (writer == null) {
			writer = new PipedOutputStream();
		}

		if (reader == null) {
			reader = new PipedInputStream();
		}

		try {
			reader.connect(writer);
			connected = true;
		} catch (IOException e) {
			Logging.warning("connecting reader with writer, when they're connected: ", e);
		}
	}

	public static void close() throws IOException {
		writer.close();
		reader.close();

		writer = null;
		reader = null;
		connected = false;
	}
}
