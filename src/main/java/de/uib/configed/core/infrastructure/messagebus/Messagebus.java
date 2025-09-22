/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure.messagebus;

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
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.ServerFacade;
import de.uib.configed.core.infrastructure.certificate.CertificateValidator;
import de.uib.configed.core.infrastructure.certificate.CertificateValidatorFactory;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.ThreadLocker;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

@SuppressWarnings("java:S1258")
public class Messagebus implements MessagebusListener {
	public static final String CONNECTION_USER_CHANNEL = "@";

	private static Messagebus instance;

	private WebSocketClientEndpoint messagebusWebSocket;
	private boolean connected;
	private boolean disconnecting;
	private boolean reconnecting;
	private boolean initialSubscriptionReceived;
	private ConfigedMain configedMain;

	// to check if channel subscription event was received
	private String channelSessionTerminalId;
	private ThreadLocker locker;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public Messagebus(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		locker = new ThreadLocker();
	}

	public WebSocketClientEndpoint getWebSocket() {
		return messagebusWebSocket;
	}

	public boolean connect() throws InterruptedException {
		if (messagebusWebSocket != null && isConnected()) {
			Logging.info(this, "Messagebus is already connected");
			return true;
		}

		initialSubscriptionReceived = false;
		disconnecting = false;
		URI uri = createUri();

		Logging.notice(this, "Connecting to messagebus at", uri);

		String basicAuthEnc = createEncBasicAuth();
		ServerFacade exec = getServerFacadeExecutor();

		messagebusWebSocket = new WebSocketClientEndpoint(uri);
		messagebusWebSocket.registerListener(this);

		if (ConfigedMain.getMainFrame() != null) {
			configedMain.registerMessagebusListeners();
		}

		if (basicAuthEnc != null) {
			messagebusWebSocket.addHeader("Authorization", String.format("Basic %s", basicAuthEnc));
		}
		if (exec.getSessionId() != null) {
			Logging.info("Adding cookie header for session ID");
			messagebusWebSocket.addHeader("Cookie", exec.getSessionId());
		}

		CertificateValidator certValidator = CertificateValidatorFactory.getValidator();
		messagebusWebSocket.setSocketFactory(certValidator.getSSLSocketFactory());
		messagebusWebSocket.setReuseAddr(true);
		messagebusWebSocket.setTcpNoDelay(true);

		if (messagebusWebSocket.connectBlocking() &&
		// Socket is open, but may be closed again soon if unauthorized
				waitForInitialChannelSubscritionEvent(10000)) {
			connected = true;
			Logging.notice(this, "Connected to messagebus");
			sendStandardChannelSubscriptions();
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
				Logging.warning(this, "Timed out after ", timeoutMs, " ms while waiting for inital subscription event");
				return false;
			}
			Utils.threadSleep(this, 50);
		}
		return true;
	}

	private URI createUri() {
		URI uri = null;

		try {
			uri = new URI(produceURL());
		} catch (URISyntaxException ex) {
			Logging.warning(this, ex, "Inavlid URI: ", uri);
		}

		return uri;
	}

	private String produceURL() {
		String host = PersistenceControllerFactory.getPersistenceController().getExecutioner().getHost();
		if (host == null) {
			Logging.error(this, "Host is null");
			return null;
		}

		if (!Utils.hasPort(host)) {
			host = host + ":" + Globals.DEFAULT_PORT;
			Logging.info(this, "Host doesn't have specified port (using default): ", host);
		} else {
			Logging.info(this, "Host does have specified port (using specified port): ", host);
		}

		String protocol = "wss";
		String url = String.format("%s://%s/messagebus/v1", protocol, host);
		Logging.info(this, "Connecting to messagebus using the following URL: ", url);

		return url;
	}

	private ServerFacade getServerFacadeExecutor() {
		return PersistenceControllerFactory.getPersistenceController().getExecutioner();
	}

	private String createEncBasicAuth() {
		ServerFacade exec = getServerFacadeExecutor();
		if (exec.isUseSAML()) {
			return null;
		}
		String basicAuth = String.format("%s:%s", exec.getUsername(), exec.getPassword());
		return Base64.getEncoder().encodeToString(basicAuth.getBytes(StandardCharsets.UTF_8));
	}

	private void sendStandardChannelSubscriptions() {
		List<String> channels = new ArrayList<>();

		channels.add(WebSocketEvent.HOST_CONNECTED.asChannelEvent());
		channels.add(WebSocketEvent.HOST_DISCONNECTED.asChannelEvent());
		channels.add(WebSocketEvent.HOST_CREATED.asChannelEvent());
		channels.add(WebSocketEvent.HOST_DELETED.asChannelEvent());

		channels.add(WebSocketEvent.PRODUCT_ON_CLIENT_CREATED.asChannelEvent());
		channels.add(WebSocketEvent.PRODUCT_ON_CLIENT_UPDATED.asChannelEvent());
		channels.add(WebSocketEvent.PRODUCT_ON_CLIENT_DELETED.asChannelEvent());

		sendChannelSubscriptionRequest(channels);
	}

	private void sendChannelSubscriptionRequest(List<String> channels) {
		Map<String, Object> message = new HashMap<>();
		message.put("type", WebSocketEvent.CHANNEL_SUBSCRIPTION_REQUEST.toString());
		message.put("id", UUID.randomUUID().toString());
		message.put("sender", CONNECTION_USER_CHANNEL);
		message.put("channel", "service:messagebus");
		message.put("created", System.currentTimeMillis());
		message.put("expires", System.currentTimeMillis() + 10000);
		message.put("operation", "add");
		message.put("channels", channels);
		Logging.debug(this, "Sending channel subscription request: ", message);
		sendMessage(message);
	}

	public void sendTerminalOpenRequest(String channel, int rows, int cols) {
		String terminalId = UUID.randomUUID().toString();
		// to verify server response contains this requested channel
		channelSessionTerminalId = String.format("session:%s", terminalId);
		sendChannelSubscriptionRequest(Collections.singletonList(channelSessionTerminalId));
		// need to wait for the subscription to be processed
		locker.lock(5000);

		Map<String, Object> message = new HashMap<>();
		message.put("type", WebSocketEvent.TERMINAL_OPEN_REQUEST.toString());
		message.put("id", UUID.randomUUID().toString());
		message.put("sender", CONNECTION_USER_CHANNEL);
		message.put("channel", channel != null ? channel : "service:config:terminal");
		message.put("back_channel", String.format("session:%s", terminalId));
		message.put("created", System.currentTimeMillis());
		message.put("expires", System.currentTimeMillis() + 10000);
		message.put("terminal_id", terminalId);
		message.put("cols", cols);
		message.put("rows", rows);

		Logging.debug(this, "Sending terminal open request: ", message);
		sendMessage(message);
	}

	public void sendMessage(ByteBuffer message) {
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
				sendMessage(ByteBuffer.wrap(msgpackBytes, 0, msgpackBytes.length));
			} catch (JsonProcessingException ex) {
				Logging.warning(this, ex, "Error occurred while processing msgpack: ");
			}
		} else {
			Logging.warning(this, "Message of type '", message.get("type"), "' not sent, messagebus not connected");
		}
	}

	public boolean isBusy() {
		return messagebusWebSocket != null && messagebusWebSocket.hasBufferedData();
	}

	public boolean isConnected() {
		return connected;
	}

	public static void initMessagebus(ConfigedMain configedMain) {
		if (instance == null) {
			instance = new Messagebus(configedMain);
		}

		if (!instance.isConnected()) {
			try {
				Logging.info("connecting to messagebus");
				instance.connect();
				Logging.info("connected to messagebus");
			} catch (InterruptedException e) {
				Logging.error(e, "could not connect to messagebus");
				Thread.currentThread().interrupt();
			}
		}
	}

	public static Messagebus getInstance() {
		return instance;
	}

	public void disconnect() {
		if (messagebusWebSocket != null && isConnected()) {
			disconnecting = true;
			try {
				messagebusWebSocket.closeBlocking();
				connected = false;
				Logging.info(this, "Connection to messagebus closed");
			} catch (InterruptedException e) {
				Logging.error(this, e, "Error disconnecting messagebus");
				Thread.currentThread().interrupt();
			}
		} else {
			Logging.info(this, "Messagebus not connected");
		}
	}

	public void setReconnecting(boolean reconnecting) {
		this.reconnecting = reconnecting;
	}

	@Override
	public void onOpen(ServerHandshake handshakeData) {
		// Not needed
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		// The close codes are documented in class org.java_websocket.framing.CloseFrame
		Logging.info(this, "Messagebus connection closed by ", remote ? "opsi service" : "us", " Code=", code,
				" Reason='", reason, "', disconnecting=", disconnecting, ", reconnecting=", reconnecting);
		boolean wasDisconnecting = disconnecting;
		connected = false;
		disconnecting = false;
		boolean authenticationError = reason != null && reason.toLowerCase(Locale.ROOT).contains("authentication");

		if (!wasDisconnecting && !reconnecting) {
			new RetryConnectingThread(authenticationError, this).start();
		}
	}

	@Override
	public void onError(Exception ex) {
		Logging.warning(this, ex, "Messagebus connection error");
	}

	@Override
	public void onMessageReceived(Map<String, Object> message) {
		Logging.trace(this, "Messagebus message received: ", message);
		String type = (String) message.get("type");
		if (WebSocketEvent.CHANNEL_SUBSCRIPTION_EVENT.toString().equals(type)) {
			initialSubscriptionReceived = true;
			if (message.get("subscribed_channels") == null) {
				Logging.warning("No channels in subscription event: ", message);
				return;
			}
			List<?> channels = (List<?>) message.get("subscribed_channels");
			if (channels.stream().anyMatch(channel -> channel.toString().equals(channelSessionTerminalId))) {
				// check if the subscripted_channels in response contains the requested channel
				// ensures that we send terminalOpenRequest only after we subscribed the correct channel
				channelSessionTerminalId = null;
				locker.unlock();
			}
		} else if (WebSocketEvent.GENERAL_ERROR.toString().equals(type)) {
			Logging.error(this, "Error occured on the server ", message.get("error"));
		} else {
			// Other events are handled by other listeners.
		}
	}
}
