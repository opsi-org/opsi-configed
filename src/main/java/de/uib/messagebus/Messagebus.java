package de.uib.messagebus;

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
import de.uib.configed.terminal.Terminal;
import de.uib.utilities.logging.Logging;

@SuppressWarnings("java:S109")
public class Messagebus {
	private WebSocketClientEndpoint messagebusWebSocket;

	public WebSocket getWebSocket() {
		return messagebusWebSocket;
	}

	private String createUrl() {
		String uri = "wss";
		String host = ConfigedMain.host;

		if (!hasPort(host)) {
			host = host + ":4447";
			Logging.info(this, "host doesn't have specified port (using default): " + host);
		} else {
			Logging.info(this, "host does have specified port (using specified port): " + host);
		}

		String url = String.format("%s://%s/messagebus/v1", uri, host);
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
			Logging.warning(this, "provider doesn't support algorithm");
		} catch (KeyManagementException e) {
			Logging.warning(this, "failed to initialize SSL context: " + e);
		}

		return sslFactory;
	}

	public boolean connect() throws URISyntaxException, InterruptedException {
		if (messagebusWebSocket != null && isConnected()) {
			Logging.info(this, "messagebus is already connected");
			return true;
		}

		String url = createUrl();
		String basicAuthEnc = createEncBasicAuth();
		SSLSocketFactory factory = createDullSSLSocketFactory();

		messagebusWebSocket = new WebSocketClientEndpoint(new URI(url));
		messagebusWebSocket.addHeader("Authorization", String.format("Basic %s", basicAuthEnc));
		messagebusWebSocket.setSocketFactory(factory);
		messagebusWebSocket.setReuseAddr(true);
		messagebusWebSocket.setTcpNoDelay(true);

		return messagebusWebSocket.connectBlocking();
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

		Logging.debug(this, "channel subscription request: " + data.toString());

		try {
			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
		} catch (JsonProcessingException e) {
			Logging.warning(this, "error occurred while processing JSON: " + e);
		}
	}

	public void connectTerminal() {
		String terminalId = UUID.randomUUID().toString();

		makeChannelSubscriptionRequest(terminalId);

		Terminal terminal = Terminal.getInstance();
		terminal.setMessagebus(this);
		terminal.display();

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

		Logging.debug(this, "terminal open request: " + data.toString());

		try {
			ObjectMapper mapper = new MessagePackMapper();
			byte[] dataJsonBytes = mapper.writeValueAsBytes(data);
			send(ByteBuffer.wrap(dataJsonBytes, 0, dataJsonBytes.length));
		} catch (JsonProcessingException e) {
			Logging.warning(this, "error occurred while processing JSON: " + e);
		}

		terminal.lock();
		terminal.connectWebSocket();
	}

	public void send(ByteBuffer message) {
		if (messagebusWebSocket.getConnection().isOpen()) {
			messagebusWebSocket.send(message);
		} else {
			Logging.info(this, "messagebus not connected");
		}
	}

	public boolean isBusy() {
		return messagebusWebSocket.hasBufferedData();
	}

	public boolean isConnected() {
		return messagebusWebSocket.getConnection().isOpen();
	}

	public void disconnect() throws InterruptedException {
		if (messagebusWebSocket != null && isConnected()) {
			messagebusWebSocket.closeBlocking();
			Logging.info(this, "connection to messagebus closed");
		} else {
			Logging.info(this, "messagebus not connected");
		}
	}
}
