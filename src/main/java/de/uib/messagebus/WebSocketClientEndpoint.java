package de.uib.messagebus;

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

import de.uib.configed.ConfigedMain;
import de.uib.configed.terminal.Terminal;
import de.uib.configed.terminal.WebSocketInputStream;
import de.uib.utilities.logging.Logging;

@SuppressWarnings("java:S109")
public class WebSocketClientEndpoint extends WebSocketClient {

	private ConfigedMain configedMain;

	public WebSocketClientEndpoint(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public WebSocketClientEndpoint(URI serverURI) {
		super(serverURI);
	}

	public WebSocketClientEndpoint(URI serverUri, Map<String, String> httpHeaders) {
		super(serverUri, httpHeaders);
	}

	public void setConfigedMain(ConfigedMain configedMain) {
		this.configedMain = configedMain;
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
				switch (type) {
				case "terminal_data_read":
					WebSocketInputStream.write((byte[]) data.get("data"));
					break;
				case "terminal_open_event":
					Terminal terminal = Terminal.getInstance();
					terminal.setTerminalId((String) data.get("terminal_id"));
					terminal.setTerminalChannel((String) data.get("back_channel"));
					terminal.unlock();
					break;
				case "terminal_close_event":
					Terminal.getInstance().close();
					break;
				case "terminal_resize_event":
					break;
				default:
					Logging.warning(this, "unhandeld terminal type response caught: " + type);
				}
			} else if ("file_upload_result".equals(type)) {
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
			} else if ("event".equals(type)) {
				String clientId = (String) ((Map<?, ?>) ((Map<?, ?>) data.get("data")).get("host")).get("id");

				switch ((String) data.get("event")) {
				case "host_connected":
					configedMain.addClientToConnectedList(clientId);
					break;

				case "host_disconnected":
					configedMain.removeClientFromConnectedList(clientId);
					break;

				default:
					break;
				}
			}
		} catch (IOException e) {
			Logging.error(this, "cannot read received message: ", e);
		}
	}

	@SuppressWarnings("java:S1774")
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
