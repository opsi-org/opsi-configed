/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.messagebus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.java_websocket.handshake.ServerHandshake;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.ConfigedMain;
import de.uib.configed.terminal.Terminal;
import de.uib.configed.terminal.WebSocketInputStream;
import de.uib.opsicommand.CertificateValidator;
import de.uib.opsicommand.CertificateValidatorFactory;
import de.uib.opsicommand.ServerFacade;
import de.uib.opsidatamodel.OpsiserviceNOMPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

@SuppressWarnings("java:S1258")
public class Messagebus implements MessagebusListener {
	private WebSocketClientEndpoint messagebusWebSocket;
	private ConfigedMain configedMain;
	private int reconnectWaitMillis = 15000;
	private boolean connected;
	private boolean disconnecting;
	private boolean reconnecting;
	private boolean initialSubscriptionReceived;

	private OpsiserviceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public Messagebus(ConfigedMain configedMain) {
		this.configedMain = configedMain;
	}

	public WebSocketClientEndpoint getWebSocket() {
		return messagebusWebSocket;
	}

	public boolean connect() throws InterruptedException {
		if (messagebusWebSocket != null && isConnected()) {
			Logging.info(this, "messagebus is already connected");
			return true;
		}

		Logging.info(this, "Connecting to messagebus");

		initialSubscriptionReceived = false;
		disconnecting = false;
		URI uri = createUri();
		String basicAuthEnc = createEncBasicAuth();
		ServerFacade exec = getServerFacadeExecutor();

		messagebusWebSocket = new WebSocketClientEndpoint(uri);
		messagebusWebSocket.registerListener(this);
		if (ConfigedMain.getMainFrame() != null) {
			messagebusWebSocket.registerListener(ConfigedMain.getMainFrame().getMessagebusListener());
		}
		messagebusWebSocket.addHeader("Authorization", String.format("Basic %s", basicAuthEnc));
		if (exec.getSessionId() != null) {
			Logging.debug("Adding cookie header");
			messagebusWebSocket.addHeader("Cookie", exec.getSessionId());
		}

		CertificateValidator certValidator = CertificateValidatorFactory.createSecure();
		messagebusWebSocket.setSocketFactory(certValidator.createSSLSocketFactory());
		messagebusWebSocket.setReuseAddr(true);
		messagebusWebSocket.setTcpNoDelay(true);

		if (messagebusWebSocket.connectBlocking() &&
		// Socket is open, but may be closed again soon if unauthorized
				waitForInitialChannelSubscritionEvent(10000)) {
			connected = true;
			Logging.notice(this, "Connected to messagebus");
			makeStandardChannelSubscriptions();

		}
		return connected;
	}

	private boolean waitForInitialChannelSubscritionEvent(long timeoutMs) {
		long start = System.currentTimeMillis();
		while (!initialSubscriptionReceived) {
			if (!messagebusWebSocket.isOpen()) {
				Logging.info("Websocket closed while waiting for inital subscription event");
				return false;
			}

			if (System.currentTimeMillis() - start >= timeoutMs) {
				Logging.warning("Timed out after " + timeoutMs + " ms while waiting for inital subscription event");
				return false;
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		return true;
	}

	private URI createUri() {
		URI uri = null;

		try {
			uri = new URI(produceURL());
		} catch (URISyntaxException ex) {
			Logging.warning(this, "inavlid URI: " + uri, ex);
		}

		return uri;
	}

	private String produceURL() {
		String protocol = "wss";
		String host = ConfigedMain.host;

		if (!hasPort(host)) {
			host = host + ":4447";
			Logging.info(this, "host doesn't have specified port (using default): " + host);
		} else {
			Logging.info(this, "host does have specified port (using specified port): " + host);
		}

		String url = String.format("%s://%s/messagebus/v1", protocol, host);
		Logging.info(this, "connecting to messagebus using the following URL: " + url);

		return url;
	}

	private boolean hasPort(String host) {
		boolean result = false;

		if (host.contains("[") && host.contains("]")) {
			Logging.info(this, "host is IPv6: " + host);
			result = host.indexOf(":", host.indexOf("]")) != -1;
		} else {
			Logging.info(this, "host is either IPv4 or FQDN: " + host);
			result = host.contains(":");
		}

		return result;
	}

	private ServerFacade getServerFacadeExecutor() {
		return (ServerFacade) persistenceController.exec;
	}

	private String createEncBasicAuth() {
		ServerFacade exec = getServerFacadeExecutor();
		String basicAuth = String.format("%s:%s", exec.getUsername(), exec.getPassword());
		return Base64.getEncoder().encodeToString(basicAuth.getBytes(StandardCharsets.UTF_8));
	}

	private void makeStandardChannelSubscriptions() {
		List<String> channels = new ArrayList<>();

		channels.add("event:host_connected");
		channels.add("event:host_disconnected");

		channels.add("event:host_created");
		channels.add("event:host_deleted");

		channels.add("event:productOnClient_created");
		channels.add("event:productOnClient_updated");
		channels.add("event:productOnClient_deleted");

		makeChannelSubscriptionRequest(channels);
	}

	private void makeChannelSubscriptionRequest(List<String> channels) {
		Map<String, Object> message = new HashMap<>();
		message.put("type", "channel_subscription_request");
		message.put("id", UUID.randomUUID().toString());
		message.put("sender", "@");
		message.put("channel", "service:messagebus");
		message.put("created", System.currentTimeMillis());
		message.put("expires", System.currentTimeMillis() + 10000);
		message.put("operation", "add");
		message.put("channels", channels);

		Logging.debug(this, "channel subscription request: " + message.toString());

		sendMessage(message);
	}

	public void connectTerminal() {
		String terminalId = UUID.randomUUID().toString();

		makeChannelSubscriptionRequest(Collections.singletonList("session:" + terminalId));

		Terminal terminal = Terminal.getInstance();
		terminal.setMessagebus(this);
		terminal.display();

		Map<String, Object> message = new HashMap<>();
		message.put("type", "terminal_open_request");
		message.put("id", UUID.randomUUID().toString());
		message.put("sender", "@");
		message.put("channel", "service:config:terminal");
		message.put("back_channel", String.format("session:%s", terminalId));
		message.put("created", System.currentTimeMillis());
		message.put("expires", System.currentTimeMillis() + 10000);
		message.put("terminal_id", terminalId);
		message.put("cols", terminal.getColumnCount());
		message.put("rows", terminal.getRowCount());

		Logging.debug(this, "terminal open request: " + message.toString());

		sendMessage(message);

		terminal.lock();
		terminal.connectWebSocket();
	}

	public void send(ByteBuffer message) {
		if (isConnected()) {
			messagebusWebSocket.send(message);
		} else {
			Logging.warning(this, "Message not sent, messagebus not connected");
		}
	}

	public void sendMessage(Map<String, Object> message) {
		if (isConnected()) {
			try {
				ObjectMapper mapper = new MessagePackMapper();
				byte[] msgpackBytes = mapper.writeValueAsBytes(message);
				send(ByteBuffer.wrap(msgpackBytes, 0, msgpackBytes.length));
			} catch (JsonProcessingException ex) {
				Logging.warning(this, "error occurred while processing msgpack: ", ex);
			}
		} else {
			Logging.warning(this, "Message of type '" + message.get("type") + "' not sent, messagebus not connected");
		}
	}

	public boolean isBusy() {
		return messagebusWebSocket != null && messagebusWebSocket.hasBufferedData();
	}

	public boolean isConnected() {
		return connected;
	}

	public void disconnect() throws InterruptedException {
		if (messagebusWebSocket != null && isConnected()) {
			disconnecting = true;
			messagebusWebSocket.closeBlocking();
			Logging.info(this, "connection to messagebus closed");
		} else {
			Logging.info(this, "messagebus not connected");
		}
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not needed
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// The close codes are documented in class org.java_websocket.framing.CloseFrame
		Logging.info(this, "Messagebus connection closed by " + (remote ? "opsi service" : "us") + " Code=" + code
				+ " Reason='" + reason + "', disconnecting=" + disconnecting + ", reconnecting=" + reconnecting);
		boolean wasDisconnecting = disconnecting;
		connected = false;
		disconnecting = false;
		boolean authenticationError = reason != null && reason.toLowerCase(Locale.ROOT).contains("authentication");

		if (!wasDisconnecting && !reconnecting) {
			new RetryConnectingThread(authenticationError).start();
		}
	}

	private class RetryConnectingThread extends Thread {
		private boolean authenticationError;

		public RetryConnectingThread(boolean authenticationError) {
			this.authenticationError = authenticationError;
		}

		@Override
		public void run() {
			reconnecting = true;
			while (!isConnected()) {
				int waitMillis = reconnectWaitMillis;
				if (authenticationError) {
					Logging.notice(this, "Connection to messagebus lost, authentication error");
					persistenceController.makeConnection();
					waitMillis = 1000;
				} else {
					Logging.notice(this,
							"Connection to messagebus lost, reconnecting in " + reconnectWaitMillis + " ms");
				}
				try {
					Thread.sleep(waitMillis);
					if (connect()) {
						break;
					}
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
			reconnecting = false;
		}
	}

	@Override
	public void onError(Exception ex) {
		Logging.warning(this, "Messagebus connection error: " + ex);
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		String type = (String) message.get("type");
		Logging.trace(this, "Messagebus message received: " + message.toString());

		if (type.startsWith("terminal_")) {
			switch (type) {
			case "terminal_data_read":
				onTerminalDataRead((byte[]) message.get("data"));
				break;
			case "terminal_open_event":
				Terminal terminal = Terminal.getInstance();
				terminal.setTerminalId((String) message.get("terminal_id"));
				terminal.setTerminalChannel((String) message.get("back_channel"));
				terminal.unlock();
				break;
			case "terminal_close_event":
				Terminal.getInstance().close();
				break;
			case "terminal_resize_event":
				break;
			default:
				Logging.warning(this, "unhandled terminal type response caught: " + type);
				break;
			}
		} else if ("file_upload_result".equals(type)) {
			String filePath = (String) message.get("path");

			message.clear();
			message.put("type", "terminal_data_write");
			message.put("id", UUID.randomUUID().toString());
			message.put("sender", "@");
			message.put("channel", Terminal.getInstance().getTerminalChannel());
			message.put("created", System.currentTimeMillis());
			message.put("expires", System.currentTimeMillis() + 10000);
			message.put("terminal_id", Terminal.getInstance().getTerminalId());
			message.put("data", filePath.getBytes(StandardCharsets.UTF_8));

			sendMessage(message);
		} else if ("channel_subscription_event".equals(type)) {
			initialSubscriptionReceived = true;
		} else if ("event".equals(type)) {
			onEvent(message);
		} else {
			Logging.warning(this, "unexpected message type " + type);
		}
	}

	private void onTerminalDataRead(byte[] data) {
		try {
			WebSocketInputStream.write(data);
		} catch (IOException e) {
			Logging.error(this, "failed to write message: ", e);
		}
	}

	private void onEvent(Map<String, Object> message) {
		try {
			// Sleep for a little because otherwise we cannot get the needed Data from the Server
			Thread.sleep(5);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		ObjectMapper objectMapper = new ObjectMapper();
		TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
		};
		Map<String, Object> eventData = objectMapper.convertValue(message.get("data"), typeRef);

		switch ((String) message.get("event")) {
		case "host_connected":
			Map<String, Object> connectedHostData = objectMapper.convertValue(eventData.get("host"), typeRef);
			String connectedClientId = (String) connectedHostData.get("id");
			configedMain.addClientToConnectedList(connectedClientId);
			break;

		case "host_disconnected":
			Map<String, Object> disconnectedHostData = objectMapper.convertValue(eventData.get("host"), typeRef);
			String disconnectedClientId = (String) disconnectedHostData.get("id");
			configedMain.removeClientFromConnectedList(disconnectedClientId);
			break;

		case "host_created":
			configedMain.addClientToTable((String) eventData.get("id"));
			break;

		case "host_deleted":
			configedMain.removeClientFromTable((String) eventData.get("id"));
			break;

		case "productOnClient_created":
			configedMain.updateProduct(
					objectMapper.convertValue(message.get("data"), new TypeReference<Map<String, String>>() {
					}));
			break;

		case "productOnClient_updated":
			configedMain.updateProduct(
					objectMapper.convertValue(message.get("data"), new TypeReference<Map<String, String>>() {
					}));
			break;

		case "productOnClient_deleted":
			configedMain.updateProduct(
					objectMapper.convertValue(message.get("data"), new TypeReference<Map<String, String>>() {
					}));
			break;

		default:
			break;
		}
	}
}
