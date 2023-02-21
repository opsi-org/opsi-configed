package de.uib.configed.messagebus;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.java_websocket.WebSocket;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.ConfigedMain;
import de.uib.utilities.logging.Logging;

public class Messagebus {
	private WebSocketClientEndpoint messagebusWebSocket;
	private boolean connected;

	public WebSocket getWebSocket() {
		return messagebusWebSocket;
	}

	private String createUrl() {
		String uri = "wss";
		String host = ConfigedMain.host;

		if (!hasPort(host)) {
			host = host + ":4447";
		}

		return String.format("%s://%s/messagebus/v1", uri, host);
	}

	private boolean hasPort(String host) {
		boolean result = false;

		if (host.contains("[") && host.contains("]")) {
			result = host.indexOf(":", host.indexOf("]")) != -1;
		} else {
			result = host.contains(":");
		}

		return result;
	}

	private String createEncBasicAuth() {
		String username = ConfigedMain.user;
		String password = ConfigedMain.password;
		String basicAuth = String.format("%s:%s", username, password);
		return Base64.getEncoder().encodeToString(basicAuth.getBytes());
	}

	private SSLSocketFactory createDullSSLSocketFactory() {
		// Create a new trust manager that trust all certificates
		@SuppressWarnings("squid:S4830")
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			@Override
			public void checkClientTrusted(X509Certificate[] certs, String authType) {
				// we skip certificate verification.
			}

			@Override
			public void checkServerTrusted(X509Certificate[] certs, String authType) {
				// we skip certificate verification.
			}
		} };

		SSLSocketFactory sslFactory = null;

		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new SecureRandom());
			sslFactory = sslContext.getSocketFactory();
		} catch (NoSuchAlgorithmException e) {
			Logging.error(this, "provider doesn't support algorithm");
		} catch (KeyManagementException e) {
			Logging.error(this, "failed to initialize SSL context");
		}

		return sslFactory;
	}

	public boolean connect() throws URISyntaxException, InterruptedException {
		String url = createUrl();
		String basicAuthEnc = createEncBasicAuth();
		SSLSocketFactory factory = createDullSSLSocketFactory();

		messagebusWebSocket = new WebSocketClientEndpoint(new URI(url));
		messagebusWebSocket.addHeader("Authorization", String.format("Basic %s", basicAuthEnc));
		messagebusWebSocket.setSocketFactory(factory);

		connected = messagebusWebSocket.connectBlocking();

		return connected;
	}

	private void makeChannelSubscriptionRequest(String channel) {
		List<String> channels = new ArrayList<>();
		channels.add(String.format("session:%s", channel));

		Map<String, Object> data = new HashMap<>();
		data.put("type", "channel_subscription_request");
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", "@");
		data.put("channel", "service:messagebus");
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("operation", "add");
		data.put("channels", channels);

		try {
			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
		} catch (JsonProcessingException e) {
			Logging.error(this, "error occurred while processing JSON");
		}
	}

	public void connectTerminal() {
		String terminalId = UUID.randomUUID().toString();

		makeChannelSubscriptionRequest(terminalId);

		Terminal terminal = Terminal.getInstance();
		terminal.setMessagebus(this);
		terminal.createAndShowGUI();

		Map<String, Object> data = new HashMap<>();
		data.put("type", "terminal_open_request");
		data.put("id", UUID.randomUUID().toString());
		data.put("sender", "@");
		data.put("channel", "service:config:terminal");
		data.put("back_channel", String.format("session:%s", terminalId));
		data.put("created", System.currentTimeMillis());
		data.put("expires", System.currentTimeMillis() + 10000);
		data.put("terminal_id", terminalId);
		data.put("cols", terminal.getColumnCount());
		data.put("rows", terminal.getRowCount());

		try {
			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
		} catch (JsonProcessingException e) {
			Logging.error(this, "error occurred while processing JSON");
		}

		terminal.lock();
		terminal.connectWebSocket();
	}

	public void send(ByteBuffer message) {
		if (messagebusWebSocket.getConnection().isOpen()) {
			messagebusWebSocket.send(message);
		} else {
			Logging.info(this, "Messagebus not connected");
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void disconnect() throws InterruptedException {
		if (messagebusWebSocket != null && messagebusWebSocket.getConnection().isOpen()) {
			messagebusWebSocket.closeBlocking();
		} else {
			Logging.info(this, "Messagebus not connected");
		}

		connected = false;
	}
}
