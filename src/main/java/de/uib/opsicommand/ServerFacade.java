/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.awt.Cursor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.thread.WaitCursor;
import net.jpountz.lz4.LZ4FrameInputStream;

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
	public static final Charset UTF8DEFAULT = StandardCharsets.UTF_8;

	private static OpsiServerVersionRetriever versionRetriever;

	private boolean gzipTransmission;
	private boolean lz4Transmission;

	private String host;
	private String username;
	private String password;
	private String sessionId;
	private int portHTTPS = 4447;

	private boolean background;
	private WaitCursor waitCursor;

	/**
	 * Constructs {@code ServerFacade} object with provided information.
	 * 
	 * @param host     server FQDN or IPv4/IPv6 address.
	 * @param username to use for the authentication.
	 * @param password to use for the authentication.
	 */
	public ServerFacade(String host, String username, String password) {
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
		conStat = new ConnectionState();

		CertificateDownloader.init(produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE));
	}

	/**
	 * Retrieve opsi server version. The server's version is retrieved before
	 * the connection with the server is established.
	 * 
	 * @return opsi server version.
	 */
	public static String getServerVersion() {
		return versionRetriever.getServerVersion();
	}

	/**
	 * Check whether or not opsi server uses opsi 4.3 version. This method is
	 * used for enabling features that are only available for opsi 4.3 version.
	 * 
	 * @return whether or not opsi server uses opsi 4.3 version.
	 */
	public static boolean isOpsi43() {
		return versionRetriever.isServerVersionAtLeast("4.3");
	}

	private Map<String, String> produceGeneralRequestProperties() {
		Map<String, String> requestProperties = new HashMap<>();

		String authorization = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		requestProperties.put("Authorization", "Basic " + authorization);

		// has to be value between 1 and 43300 [sec]
		requestProperties.put("X-opsi-session-lifetime", "900");

		if (lz4Transmission) {
			requestProperties.put("Accept-Encoding", "lz4");
		} else if (gzipTransmission) {
			requestProperties.put("Accept-Encoding", "gzip");
		} else {
			/* Theoretically this should never occur, since lz4Transmission and
			   gzipTransmission cannot be both false at the same time */
			Logging.info("no encoding is specified");
		}

		requestProperties.put("User-Agent", Globals.APPNAME + " " + Globals.VERSION);
		requestProperties.put("Accept", "application/msgpack");

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
			serviceURL = new URL(baseURL);
		} catch (MalformedURLException ex) {
			Logging.error(this, "Malformed URL: " + baseURL, ex);
		}

		return serviceURL;
	}

	private static String produceJSONstring(OpsiMethodCall omc) {
		return omc != null ? omc.getJsonString() : "";
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
	public synchronized Map<String, Object> retrieveResponse(OpsiMethodCall omc) {
		background = false;
		Logging.info(this, "retrieveResponse started");
		waitCursor = null;

		if (omc != null && !omc.isBackgroundDefault()) {
			waitCursor = new WaitCursor(null, new Cursor(Cursor.DEFAULT_CURSOR), this.getClass().getName());
		} else {
			background = true;
		}

		conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);

		TimeCheck timeCheck = new TimeCheck(this, "retrieveResponse " + omc);
		timeCheck.start();

		enableFeaturesBasedOnServerVersion();

		stopWaitCursor();

		ConnectionHandler handler = new ConnectionHandler(makeURL(), produceGeneralRequestProperties());
		HttpsURLConnection connection = handler.establishConnection(true);
		conStat = handler.getConnectionState();
		sendPOSTReqeust(connection, omc);

		if (connection == null) {
			return null;
		}

		Logging.info(this, "connection cipher suite " + (connection).getCipherSuite());

		Map<String, Object> result = new HashMap<>();

		if (conStat.getState() == ConnectionState.STARTED_CONNECTING) {
			try {
				Logging.debug(this, "Response " + connection.getResponseCode() + " " + connection.getResponseMessage());

				handleResponseCode(connection, omc);

				if (conStat.getState() == ConnectionState.CONNECTED) {
					retrieveSessionIDFromResponse(connection);
					InputStream stream = getInputStreamBasedOnEncoding(connection);

					Logging.info(this, "guessContentType " + URLConnection.guessContentTypeFromStream(stream));

					result = retrieveResponseBasedOnContentType(connection.getContentType(), stream);
				}
			} catch (Exception ex) {
				if (waitCursor != null) {
					waitCursor.stop();
				}
				WaitCursor.stopAll();
				Logging.error(this, "Exception while data reading", ex);
			}
		}

		timeCheck.stop("retrieveResponse " + (result == null ? "empty result" : "non empty result"));
		Logging.info(this, "retrieveResponse ready");
		if (waitCursor != null) {
			waitCursor.stop();
		}
		return result;
	}

	private void sendPOSTReqeust(HttpsURLConnection connection, OpsiMethodCall omc) {
		if (connection == null) {
			return;
		}

		try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), UTF8DEFAULT);
				BufferedWriter out = new BufferedWriter(writer)) {
			String json = produceJSONstring(omc);
			Logging.debug(this, "(POST) sending: " + json);
			out.write(json);
			out.flush();
		} catch (IOException iox) {
			Logging.info(this, "exception on writing json request " + iox);
		}
	}

	private synchronized void enableFeaturesBasedOnServerVersion() {
		if (versionRetriever == null) {
			versionRetriever = new OpsiServerVersionRetriever(produceBaseURL("/"), username, password);
		}

		versionRetriever.checkServerVersion();

		if (versionRetriever.isServerVersionAtLeast("4.2")) {
			gzipTransmission = false;
			lz4Transmission = true;
		} else {
			gzipTransmission = true;
			lz4Transmission = false;

			// The way we check the certificate does not work before opsi server version 4.2
			Globals.disableCertificateVerification = true;
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
			Logging.error(this, "Unsupported Content-Type: " + contentType);
		}

		return result;
	}

	private void handleResponseCode(HttpsURLConnection connection, OpsiMethodCall omc) throws IOException {
		String errorInfo = retrieveErrorFromResponse(connection);

		if (connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED
				|| connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
			conStat = new ConnectionState(ConnectionState.CONNECTED, "ok");
		} else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
			conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());

			Logging.debug("Unauthorized: background=" + background + ", " + sessionId + ", mfa="
					+ Globals.isMultiFactorAuthenticationEnabled);
			if (Globals.isMultiFactorAuthenticationEnabled && ConfigedMain.getMainFrame() != null) {
				stopWaitCursor();
				ConnectionErrorObserver.getInstance().notify("", ConnectionErrorType.MFA_ERROR);
				retrieveResponse(omc);
			}
		} else {
			conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());
			Logging.error(this, "Response " + connection.getResponseCode() + " " + connection.getResponseMessage() + " "
					+ errorInfo);
		}
	}

	private String retrieveErrorFromResponse(HttpsURLConnection connection) {
		StringBuilder errorInfo = new StringBuilder("");

		if (connection.getErrorStream() != null) {
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getErrorStream(), UTF8DEFAULT))) {
				while (in.ready()) {
					errorInfo.append(in.readLine());
					errorInfo.append("  ");
				}
			} catch (IOException iox) {
				Logging.error(this, "exception on reading error stream " + iox);
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

	private void stopWaitCursor() {
		if (!background) {
			if (waitCursor != null) {
				waitCursor.stop();
			}
			WaitCursor.stopAll();
		}
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
