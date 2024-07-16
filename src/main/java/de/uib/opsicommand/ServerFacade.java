/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.certificate.CertificateDownloader;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.logging.TimeCheck;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

/**
 * Provides communication layer with the server for the
 * {@link AbstractPOJOExecutioner} class.
 * <p>
 * {@code ServerFacade} is built using the Facade design pattern. It uses the
 * {@link ConnectionHandler} to establish connection with the server and verify
 * the server's certificate. The connection uses HTTPS protocol.
 * <p>
 * Before establishing connection with the server, it check server's version to
 * disable/enable features according to the server's version. Once the
 * connection is established it sends a {@code POST} request, to send
 * {@code OpsiMethodCall}, and retrieves data from the response send by the
 * server.
 * 
 * @author Rupert Roeder, Naglis Vidziunas
 */
public class ServerFacade extends AbstractPOJOExecutioner {
	private static final int COMPRESS_MIN_SIZE = 10000;

	private static OpsiServerVersionRetriever versionRetriever;

	private String host;
	private String username;
	private String password;
	private String otp;
	private String sessionId;
	private int portHTTPS = Globals.DEFAULT_PORT;

	/**
	 * Constructs {@code ServerFacade} object with provided information.
	 * 
	 * @param host     server FQDN or IPv4/IPv6 address.
	 * @param username to use for the authentication.
	 * @param password to use for the authentication.
	 */
	public ServerFacade(String host, String username, String password, String otp) {
		if (host == null || username == null || password == null) {
			throw new IllegalArgumentException("All or some parameters are null");
		}

		this.host = host;
		int idx = -1;
		if (host.contains("[") && host.contains("]")) {
			idx = host.indexOf(":", host.indexOf("]"));
		} else {
			idx = host.indexOf(":");
		}

		if (idx > -1) {
			this.host = host.substring(0, idx);
			this.portHTTPS = Integer.parseInt(host.substring(idx + 1, host.length()));
		}
		this.username = username;
		this.password = password;
		this.otp = otp;
		conStat = new ConnectionState();

		CertificateDownloader.init(produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE));
		checkServerVersion();
	}

	private synchronized void checkServerVersion() {
		versionRetriever = new OpsiServerVersionRetriever(produceBaseURL("/"), username, password);
		versionRetriever.checkServerVersion();
	}

	public static OpsiServerVersionRetriever getOpsiServerVersionRetriever() {
		return versionRetriever;
	}

	private Map<String, String> produceGeneralRequestProperties(OpsiMethodCall omc) {
		Map<String, String> requestProperties = new HashMap<>();

		String authorization = Base64.getEncoder()
				.encodeToString((username + ":" + password + otp).getBytes(StandardCharsets.UTF_8));
		requestProperties.put("Authorization", "Basic " + authorization);

		// has to be value between 1 and 43300 [sec]
		requestProperties.put("X-opsi-session-lifetime", "900");
		requestProperties.put("Accept-Encoding", "lz4, gzip");
		requestProperties.put("User-Agent", Globals.APPNAME_SERVER_CONNECTION + " " + Globals.VERSION);
		requestProperties.put("Accept", "application/msgpack");
		requestProperties.put("Content-Type", "application/msgpack");

		int messageSize = produceMessagePack(omc).length;

		if (messageSize > COMPRESS_MIN_SIZE) {
			requestProperties.put("Content-Encoding", "lz4");
		}

		if (sessionId != null) {
			requestProperties.put("Cookie", sessionId);
		}

		return requestProperties;
	}

	private String produceBaseURL(String rpcPath) {
		return "https://" + host + ":" + portHTTPS + rpcPath;
	}

	private URL makeURL() {
		URL serviceURL = null;
		String baseURL = produceBaseURL("/rpc");

		try {
			serviceURL = new URI(baseURL).toURL();
		} catch (MalformedURLException | URISyntaxException ex) {
			Logging.error(this, ex, "URI Syntax error: ", baseURL);
		}

		return serviceURL;
	}

	private byte[] produceMessagePack(OpsiMethodCall omc) {
		Map<String, Object> omcMap = omc != null ? omc.getOMCMap() : new HashMap<>();
		byte[] result = new byte[0];
		try {
			result = new MessagePackMapper().writeValueAsBytes(omcMap);
		} catch (JsonProcessingException e) {
			Logging.error(this, e, "unable to process JSON");
		}

		return result;
	}

	/**
	 * Retrieves response from the server.
	 * <p>
	 * It establishes connection with the server and sends the {@code POST}
	 * request. Then retrieves response sent by the server. The response is
	 * accepted in JSON and MessagePack format.
	 * 
	 * @param omc RPC method to execute.
	 * @return retrieved response from the server.
	 */
	@Override
	@SuppressWarnings("java:S1168")
	public synchronized Map<String, Object> retrieveResponse(OpsiMethodCall omc) {
		Logging.info(this, "retrieveResponse started");

		conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);

		TimeCheck timeCheck = new TimeCheck(this, "retrieveResponse " + omc);
		timeCheck.start();

		ConnectionHandler handler = new ConnectionHandler(makeURL(), produceGeneralRequestProperties(omc));
		HttpsURLConnection connection = handler.establishConnection(true);
		conStat = handler.getConnectionState();
		sendPostRequest(connection, omc);

		if (connection == null) {
			return null;
		}

		Logging.info(this, "connection cipher suite " + (connection).getCipherSuite());

		Map<String, Object> result = new HashMap<>();

		if (conStat.getState() == ConnectionState.STARTED_CONNECTING) {
			try {
				handleResponseCode(connection);

				if (conStat.getState() == ConnectionState.CONNECTED) {
					retrieveSessionIDFromResponse(connection);
					InputStream stream = getInputStreamBasedOnEncoding(connection);

					Logging.info(this, "guessContentType " + URLConnection.guessContentTypeFromStream(stream));

					result = retrieveResponseBasedOnContentType(connection.getContentType(), stream);
				} else if (conStat.getState() == ConnectionState.UNAUTHORIZED) {
					return retrieveResponse(omc);
				} else {
					Logging.warning(this, "Encountered unhandled connection state: " + conStat);
				}
			} catch (IOException ex) {
				Logging.error(this, ex, "Exception while data reading");
			}
		}

		timeCheck.stop("retrieveResponse " + (result == null ? "empty result" : "non empty result"));
		Logging.info(this, "retrieveResponse ready");

		return result;
	}

	private void sendPostRequest(HttpsURLConnection connection, OpsiMethodCall omc) {
		if (connection == null) {
			return;
		}

		byte[] message = produceMessagePack(omc);

		try (OutputStream writer = getOutputStreamWriterForConnection(connection, message.length)) {
			writer.write(message);
			writer.flush();

			Map<String, Object> omcMap = omc != null ? omc.getOMCMap() : new HashMap<>();
			Logging.debug(this, "(POST) sending: " + omcMap);
		} catch (IOException iox) {
			Logging.info(this, "exception on writing json request " + iox);
		}
	}

	private static OutputStream getOutputStreamWriterForConnection(HttpsURLConnection connection, int messageSize)
			throws IOException {
		if (messageSize <= COMPRESS_MIN_SIZE) {
			return connection.getOutputStream();
		} else {
			return new LZ4FrameOutputStream(connection.getOutputStream());
		}
	}

	private Map<String, Object> retrieveResponseBasedOnContentType(String contentType, InputStream stream)
			throws IOException {
		Map<String, Object> result = new HashMap<>();

		if (contentType.contains("application/json")) {
			ObjectMapper mapper = new ObjectMapper();
			result = mapper.readValue(stream, new TypeReference<Map<String, Object>>() {
			});
		} else if (contentType.contains("application/msgpack")) {
			ObjectMapper mapper = new MessagePackMapper();
			result = mapper.readValue(stream, new TypeReference<Map<String, Object>>() {
			});
		} else {
			Logging.error(this, "Unsupported Content-Type: ", contentType);
		}

		return result;
	}

	private void handleResponseCode(HttpsURLConnection connection) throws IOException {
		Logging.debug(this, "Response " + connection.getResponseCode() + " " + connection.getResponseMessage());

		if (connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED
				|| connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			conStat = new ConnectionState(ConnectionState.CONNECTED, "ok");
		} else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
			Logging.debug("Unauthorized: " + sessionId + ", mfa=" + Utils.isMultiFactorAuthenticationEnabled());
			if (Utils.isMultiFactorAuthenticationEnabled() && ConfigedMain.getMainFrame() != null) {
				ConnectionErrorReporter.getInstance().notify("", ConnectionErrorType.MFA_ERROR);
				password = ConfigedMain.getPassword();
				conStat = new ConnectionState(ConnectionState.UNAUTHORIZED);
			} else {
				conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());
			}
		} else {
			conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());
			Logging.error(this, "Response ", connection.getResponseCode(), " ", connection.getResponseMessage(), " ",
					retrieveErrorFromResponse(connection));
		}
	}

	private String retrieveErrorFromResponse(HttpsURLConnection connection) {
		StringBuilder errorInfo = new StringBuilder();

		if (connection.getErrorStream() != null) {
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
				while (in.ready()) {
					errorInfo.append(in.readLine());
					errorInfo.append("  ");
				}
			} catch (IOException iox) {
				Logging.error(this, iox, "exception on reading error stream ");
			}
		}

		return errorInfo.toString();
	}

	private void retrieveSessionIDFromResponse(HttpsURLConnection connection) {
		String cookieVal = connection.getHeaderField("Set-Cookie");

		if (cookieVal != null) {
			String lastSessionId = sessionId;
			sessionId = cookieVal.substring(0, cookieVal.indexOf(";"));

			boolean gotNewSession = sessionId != null && !sessionId.equals(lastSessionId);

			if (gotNewSession) {
				Logging.info(this, "retrieveResponse got new session");
			}
		}
	}

	private InputStream getInputStreamBasedOnEncoding(HttpsURLConnection connection) throws IOException {
		boolean gzipped = false;
		boolean deflated = false;
		boolean lz4compressed = false;

		if (connection.getHeaderField("Content-Encoding") != null) {
			gzipped = "gzip".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));
			Logging.debug(this, "gzipped " + gzipped);
			deflated = "deflate".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));
			Logging.debug(this, "deflated " + deflated);
			lz4compressed = "lz4".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));

			Logging.debug(this, "lz4compressed " + lz4compressed);
		}

		InputStream stream = null;
		Logging.info(this, "initiating input stream");

		if (lz4compressed) {
			Logging.info(this, "initiating LZ4FrameInputStream");
			stream = new LZ4FrameInputStream(connection.getInputStream());
		} else if (gzipped || deflated) {
			if (deflated || connection.getHeaderField("Content-Type").startsWith("gzip-application")) {
				// not valid gzippt, we take inflater
				Logging.info(this, "initiating InflaterInputStream");
				InputStream str = connection.getInputStream();
				stream = new InflaterInputStream(str);
			} else {
				Logging.info(this, "initiating GZIPInputStream");

				// not working, if no GZIP
				stream = new GZIPInputStream(connection.getInputStream());
			}
		} else {
			Logging.info(this, "initiating plain input stream");
			stream = connection.getInputStream();
		}

		return stream;
	}

	/**
	 * Retrieve used username by the connection.
	 * 
	 * @return used username by the connection.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Retrieve used password by the connection.
	 * 
	 * @return used password by the connection.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Retrieve used session by the connection.
	 * 
	 * @return used session by the connection.
	 */
	public String getSessionId() {
		return sessionId;
	}
}
