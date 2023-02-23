package de.uib.configed.messagebus;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.utilities.logging.Logging;

public class WebSocketClientEndpoint extends WebSocketClient {
	public WebSocketClientEndpoint(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public WebSocketClientEndpoint(URI serverURI) {
		super(serverURI);
	}

	public WebSocketClientEndpoint(URI serverUri, Map<String, String> httpHeaders) {
		super(serverUri, httpHeaders);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		Logging.info(this, "Websocket is opened");
	}

	@Override
	public void onMessage(String message) {
		// We receive message in bytes rather than Strings. Therefore, there is
		// nothing to handle in this method.
	}

	@Override
	public void onMessage(ByteBuffer message) {
		try {
			ObjectMapper mapper = new MessagePackMapper();
			Map<String, Object> data = mapper.readValue(message.array(), new TypeReference<Map<String, Object>>() {
			});

			String type = (String) data.get("type");
			Logging.debug(this, "response data: " + data.toString());

			if (type.startsWith("terminal_")) {
				if (type.equals("terminal_data_read")) {
					WebSocketInputStream.getInstance().write((byte[]) data.get("data"));
				} else if (type.equals("terminal_open_event")) {
					Terminal terminal = Terminal.getInstance();
					terminal.setTerminalId((String) data.get("terminal_id"));
					terminal.setTerminalChannel((String) data.get("back_channel"));
					terminal.unlock();
				} else if (type.equals("terminal_close_event")) {
					Terminal.getInstance().close();
				}
			}

			if (type.equals("file_upload_result")) {
				String filePath = (String) data.get("path");

				data.clear();
				data.put("type", "terminal_data_write");
				data.put("id", UUID.randomUUID().toString());
				data.put("sender", "@");
				data.put("channel", Terminal.getInstance().getTerminalChannel());
				data.put("created", System.currentTimeMillis());
				data.put("expires", System.currentTimeMillis() + 10000);
				data.put("terminal_id", Terminal.getInstance().getTerminalId());
				data.put("data", filePath.getBytes(StandardCharsets.UTF_8));

				byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
				send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
			}
		} catch (IOException e) {
			Logging.error(this, "cannot read received message");
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// The close codes are documented in class org.java_websocket.framing.CloseFrame
		Logging.info(this,
				"Websocket closed by " + (remote ? "remote peer" : "us") + " Code: " + code + " Reason: " + reason);
	}

	@Override
	public void onError(Exception ex) {
		Logging.warning(this, "error encountered in messagebus: " + ex);
	}
}
