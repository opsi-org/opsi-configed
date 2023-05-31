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
import java.net.ProtocolException;
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

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.logging.TimeCheck;
import de.uib.utilities.thread.WaitCursor;
import net.jpountz.lz4.LZ4FrameInputStream;

/**
 * @author Rupert Roeder, Naglis Vidziunas
 */
public class ServerFacade extends AbstractPOJOExecutioner {
	public static final Charset UTF8DEFAULT = StandardCharsets.UTF_8;
	private static final int POST = 0;

	protected static String host;
	protected static int portHTTPS = 4447;

	private boolean gzipTransmission;
	private boolean lz4Transmission;

	public String username;
	public String password;
	protected URL serviceURL;
	public String sessionId;
	private int requestMethod = POST;

	private static OpsiServerVersionRetriever versionRetriever;

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

		this.versionRetriever = new OpsiServerVersionRetriever(username, password);
	}

	public static String getServerVersion() {
		return versionRetriever.getServerVersion();
	}

	public static boolean isOpsi43() {
		return versionRetriever.isServerVersionAtLeast("4.3");
	}

	private void setGeneralRequestProperties(HttpURLConnection connection) {
		String authorization = Base64.getEncoder()
				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		connection.setRequestProperty("Authorization", "Basic " + authorization);

		// has to be value between 1 and 43300 [sec]
		connection.setRequestProperty("X-opsi-session-lifetime", "900");

		if (lz4Transmission) {
			connection.setRequestProperty("Accept-Encoding", "lz4");
		} else if (gzipTransmission) {
			connection.setRequestProperty("Accept-Encoding", "gzip");
		}

		connection.setRequestProperty("User-Agent", Globals.APPNAME + " " + Globals.VERSION);
		connection.setRequestProperty("Accept", "application/msgpack");
	}

	public static String produceBaseURL(String rpcPath) {
		return "https://" + host + ":" + portHTTPS + rpcPath;
	}

	/**
	 * Opening the connection and set the SSL parameters
	 */
	private HttpsURLConnection produceConnection() throws IOException {
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

		makeURL();
		Logging.info(this, "produceConnection, url; " + serviceURL);
		HttpsURLConnection connection = (HttpsURLConnection) serviceURL.openConnection();
		setGeneralRequestProperties(connection);

		return connection;
	}

	private static class JSONCommunicationException extends Exception {
		JSONCommunicationException(String message) {
			super(message);
		}
	}

	private void makeURL() {
		if (serviceURL != null) {
			return;
		}

		Logging.debug(this, "make url ");

		String urlS = produceBaseURL("/rpc");

		try {
			serviceURL = new URL(urlS);
		} catch (MalformedURLException ex) {
			Logging.error("Malformed URL: " + urlS, ex);
		}
	}

	private static String produceJSONstring(OpsiMethodCall omc) {
		return omc.getJsonString();
	}

	/**
	 * This method receives response via HTTP in MessagePack format.
	 */
	@Override
	public synchronized Map<String, Object> retrieveResponse(OpsiMethodCall omc) {
		boolean background = false;
		Logging.info(this, "retrieveResponse started");
		WaitCursor waitCursor = null;

		if (omc != null && !omc.isBackgroundDefault()) {
			waitCursor = new WaitCursor(null, new Cursor(Cursor.DEFAULT_CURSOR), this.getClass().getName());
		} else {
			background = true;
		}

		conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);

		TimeCheck timeCheck = new TimeCheck(this, "retrieveResponse " + omc);
		timeCheck.start();

		HttpsURLConnection connection = null;
		try {
			// the underlying network connection can be shared,
			// only disconnect() may close the underlying socket.
			connection = produceConnection();

			if (!background) {
				if (waitCursor != null) {
					waitCursor.stop();
				}
				WaitCursor.stopAll();
			}

			if (sessionId != null) {
				connection.setRequestProperty("Cookie", sessionId);
			}

			if (requestMethod == POST) {
				sendPOSTReqeust(connection, omc);
			} else {
				sendGETRequest(connection);
			}

		} catch (IOException ex) {
			if (!background) {
				if (waitCursor != null) {
					waitCursor.stop();
				}
				WaitCursor.stopAll();
			}

			conStat = new ConnectionState(ConnectionState.ERROR, ex.toString());
			Logging.error("Exception on connecting, ", ex);

			return null;
		}

		Map<String, Object> result = new HashMap<>();

		if (conStat.getState() == ConnectionState.STARTED_CONNECTING) {
			try {
				Logging.debug(this, "Response " + connection.getResponseCode() + " " + connection.getResponseMessage());

				StringBuilder errorInfo = retrieveErrorFromResponse(connection);

				if (connection.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED
						|| connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					conStat = new ConnectionState(ConnectionState.CONNECTED, "ok");
				} else if (connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
					conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());

					Logging.debug("Unauthorized: background=" + background + ", " + sessionId + ", mfa="
							+ Globals.isMultiFactorAuthenticationEnabled);
					if (Globals.isMultiFactorAuthenticationEnabled && ConfigedMain.getMainFrame() != null) {
						if (!background) {
							if (waitCursor != null) {
								waitCursor.stop();
							}
							WaitCursor.stopAll();
						}
						ConnectionErrorObserver.getInstance().notify("", ConnectionErrorType.MFA_ERROR);
						return retrieveResponse(omc);
					}
				} else {
					conStat = new ConnectionState(ConnectionState.ERROR, connection.getResponseMessage());
					Logging.error(this, "Response " + connection.getResponseCode() + " "
							+ connection.getResponseMessage() + " " + errorInfo.toString());
				}

				if (conStat.getState() == ConnectionState.CONNECTED) {
					retrieveSessionIDFromResponse(connection);
					InputStream stream = getInputStreamBasedOnEncoding(connection);

					Logging.info(this, "guessContentType " + URLConnection.guessContentTypeFromStream(stream));

					if (connection.getContentType().contains("application/json")) {
						ObjectMapper mapper = new ObjectMapper();
						result = mapper.readValue(stream, new TypeReference<Map<String, Object>>() {
						});
					} else if (connection.getContentType().contains("application/msgpack")) {
						ObjectMapper mapper = new MessagePackMapper();
						result = mapper.readValue(stream, new TypeReference<Map<String, Object>>() {
						});
					} else {
						Logging.error(this, "Unsupported Content-Type: " + connection.getContentType());
					}
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
		ConnectionHandler handler = new ConnectionHandler(conStat);

		try {
			connection.setRequestMethod("POST");
		} catch (ProtocolException ex) {
			Logging.error(this, "method cannot be reset", ex);
		}
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);

		Logging.debug(this, "https protocols given by system " + Configed.SYSTEM_SSL_VERSION);
		Logging.info(this,
				"retrieveResponse method=" + connection.getRequestMethod() + ", headers="
						+ connection.getRequestProperties() + ", cookie="
						+ (sessionId == null ? "null" : (sessionId.substring(0, 26) + "...")));

		if (!handler.establishConnection(connection)) {
			Logging.error(this, "unable to establish connection");
			conStat = handler.getConnectionState();

			return;
		}

		Logging.info(this, "connection cipher suite " + (connection).getCipherSuite());

		conStat = handler.getConnectionState();

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

	private void sendGETRequest(HttpsURLConnection connection) {
		ConnectionHandler handler = new ConnectionHandler(conStat);

		try {
			connection.setRequestMethod("GET");
		} catch (ProtocolException ex) {
			Logging.error(this, "method cannot be reset", ex);
		}

		Logging.debug(this, "https protocols given by system " + Configed.SYSTEM_SSL_VERSION);
		Logging.info(this,
				"retrieveResponse method=" + connection.getRequestMethod() + ", headers="
						+ connection.getRequestProperties() + ", cookie="
						+ (sessionId == null ? "null" : (sessionId.substring(0, 26) + "...")));

		if (!handler.establishConnection(connection)) {
			Logging.error(this, "unable to establish connection");
		}

		Logging.info(this, "connection cipher suite " + (connection).getCipherSuite());
	}

	private StringBuilder retrieveErrorFromResponse(HttpsURLConnection connection) throws JSONCommunicationException {
		StringBuilder errorInfo = new StringBuilder("");

		if (connection.getErrorStream() != null) {
			try (BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getErrorStream(), UTF8DEFAULT))) {
				while (in.ready()) {
					errorInfo.append(in.readLine());
					errorInfo.append("  ");
				}
			} catch (IOException iox) {
				Logging.warning(this, "exception on reading error stream " + iox);
				throw new JSONCommunicationException("error on reading error stream");
			}
		}

		return errorInfo;
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
}
