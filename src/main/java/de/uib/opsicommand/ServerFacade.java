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
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.certificate.CertificateManager;
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
	private boolean useSSO = false;
	private int portHTTPS = Globals.DEFAULT_PORT;

	public ServerFacade(String host) {
		this(host, true);
	}

	public ServerFacade(String host, boolean connect) {
		if (host == null) {
			return;
			// throw new IllegalArgumentException("All or some parameters are null");
		}
		this.host = host;
		if (connect) {
			connect(host, null, null, null, true);
		}
	}

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
		connect(host, username, password, otp, false);
	}

	private synchronized boolean connectSSO() {
		Logging.info(this, "connectSSO started ");
		// register and get new session id (may throw exception)
		ssoRequestSessionId();
		ssoOpenBrowser();
		return ssoCheckAuthenticated();
	}

	private synchronized void connect(String host, String username, String password, String otp, boolean useSSO) {
		this.useSSO = useSSO;
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

		if (useSSO && !connectSSO()) {
			// Logging.error("sso connection failed");
			throw new RuntimeException("sso connection failed");
		}

		checkServerVersion();

		if (versionRetriever.isServerVersionAtLeast("4.3.18.15")) {
			CertificateManager.init(produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE), this.host + "_" + portHTTPS);
		} else {
			CertificateManager.init(
					produceBaseURL(
							"/ssl/" + Globals.OPSI_CERTIFICATE_FILE_NAME + "." + Globals.CERTIFICATE_FILE_EXTENSION),
					this.host + "_" + portHTTPS);
		}
	}

	public Map<String, List<String>> getHeaders() {
		Logging.info("getHeaders started");
		Map<String, String> requestProperties = new HashMap<>();
		requestProperties.put("Accept", "application/json");
		requestProperties.put("User-Agent", Globals.APPNAME_SERVER_CONNECTION + " " + Globals.VERSION);
		if (sessionId != null) {
			requestProperties.put("Cookie", sessionId);
		}
		URL url = makeURL("/auth/session_id");
		ConnectionHandler handler = new ConnectionHandler(url, requestProperties);
		HttpsURLConnection connection = handler.establishConnection(true, true);
		conStat = handler.getConnectionState();
		if (connection == null) {
			Logging.warning("try to get headers, but connection is null. " + "conStat ",
					conStat + " state: " + conStat.getState());
			return new HashMap<>();
		}
		Map<String, List<String>> result = new HashMap<>();
		try {
			handleResponseCode(connection);
			result = connection.getHeaderFields();
		} catch (IOException ex) {
			Logging.error("Exception while trying to get headers" + ex);
		}
		// CertificateManager.init(null, null);
		return result;
	}

	private synchronized void ssoRequestSessionId() {
		Logging.info(this, "ssoRequestSessionId started");
		Map<String, String> requestProperties = new HashMap<>();
		Map<String, Object> jsonProperties = null;
		requestProperties.put("Accept", "application/json");
		requestProperties.put("User-Agent", Globals.APPNAME_SERVER_CONNECTION + " " + Globals.VERSION);
		String localKeySID = "respondSessionId";
		//////// register and get new session id
		URL url_get_sid = makeURL("/auth/session_id");
		CertificateManager.init(produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE), host + "_" + portHTTPS);
		conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);
		Map<String, Object> result = retrieveResponse(url_get_sid, "GET", requestProperties, jsonProperties,
				localKeySID);
		if (conStat.getState() == ConnectionState.RETRY_CONNECTION) {
			Logging.debug("connectSSO retry connection");
			result = retrieveResponse(url_get_sid, "GET", requestProperties, jsonProperties, localKeySID);
		}

		if (result == null || result.isEmpty() || !result.containsKey(localKeySID)) {
			Logging.error("connectSSO no sessionId received. Result: " + result);
			throw new RuntimeException("sessionId not received");
		}
		this.sessionId = (String) result.get(localKeySID);
		if (sessionId == null) {
			throw new RuntimeException("Requested sessionId is null");
		} else {
			sessionId = sessionId.contains("=") ? sessionId : ("opsiconfd-session=" + sessionId);
		}

	}

	private boolean ssoOpenBrowser() {
		Logging.info(this, "ssoOpenBrowser started");
		/////// open browser
		String sid = sessionId.contains("=") ? sessionId.split("=")[1] : sessionId;
		String urlBrowserSso = "/auth/saml/login?session_id=" + sid + "&redirect=close_window";
		URL url = makeURL(urlBrowserSso);
		if (url != null) {
			Utils.showExternalDocument(url.toString());
			return true;
		} else {
			// But actually this cannot happen
			return false;
		}
	}

	private boolean ssoCheckAuthenticated() {
		/////// check if authenticated
		Logging.info(this, "ssoCheckAuthenticated started");
		URL url_authenticated = makeURL("/auth/wait_authenticated");

		Map<String, String> requestProperties = new HashMap<>();
		requestProperties.put("Accept", "application/json");
		requestProperties.put("User-Agent", Globals.APPNAME_SERVER_CONNECTION + " " + Globals.VERSION);
		requestProperties.put("Cookie", sessionId.contains("=") ? sessionId : ("opsiconfd-session=" + sessionId));

		Map<String, Object> jsonProperties = new HashMap<>();
		jsonProperties.put("wait_time", 60);
		HashMap<String, Object> responseHeader = new HashMap<>();
		Map<String, Object> result = retrieveResponse(url_authenticated, "POST", requestProperties, jsonProperties,
				"authenticated", responseHeader);

		if (result == null || result.isEmpty() || !result.containsKey("authenticated")) {
			throw new RuntimeException("authenticated not received");
		}
		if (responseHeader.isEmpty()) {
			throw new RuntimeException("responseHeaders not received");
		}

		// set credentials for further requests and for information
		String uname = (String) responseHeader.get("x-opsi-user-id");
		if (uname == null) {
			throw new RuntimeException("username not received");
		}
		username = uname.split("user:")[1];
		ConfigedMain.setUser(username);
		if (host != null && !host.equals(ConfigedMain.getHost())) {
			ConfigedMain.setHost(host);
		}

		return (boolean) result.get("authenticated");
	}

	private synchronized void checkServerVersion() {
		if (useSSO) {
			versionRetriever = new OpsiServerVersionRetriever(produceBaseURL("/"), sessionId);
		} else {
			versionRetriever = new OpsiServerVersionRetriever(produceBaseURL("/"), username, password);
		}
		versionRetriever.checkServerVersion();
	}

	public static OpsiServerVersionRetriever getOpsiServerVersionRetriever() {
		return versionRetriever;
	}

	private Map<String, String> produceGeneralRequestProperties(OpsiMethodCall omc) {
		Map<String, String> requestProperties = new HashMap<>();
		if (!useSSO) {
			String authorization = Base64.getEncoder()
					.encodeToString((username + ":" + password + otp).getBytes(StandardCharsets.UTF_8));
			requestProperties.put("Authorization", "Basic " + authorization);
		}

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
			requestProperties.put("Cookie", sessionId.contains("=") ? sessionId : ("opsiconfd-session=" + sessionId));
		}

		return requestProperties;
	}

	private String produceBaseURL(String rpcPath) {
		String url;
		if (host.contains("://")) {
			url = host + ":" + portHTTPS + rpcPath;
		} else if (host.contains(":")) {
			url = "https://" + host + rpcPath;
		} else {
			url = "https://" + host + ":" + portHTTPS + rpcPath;
		}
		return url;
	}

	private URL makeURL() {
		return makeURL("/rpc");
	}

	private URL makeURL(String urlpath) {
		URL serviceURL = null;
		String baseURL = produceBaseURL(urlpath);

		try {
			serviceURL = new URI(baseURL).toURL();
		} catch (MalformedURLException | URISyntaxException ex) {
			Logging.error(this, "URI Syntax error: " + baseURL, ex);
		}

		return serviceURL;
	}

	private byte[] produceMessagePack(OpsiMethodCall omc) {
		Map<String, Object> omcMap = omc != null ? omc.getOMCMap() : new HashMap<>();
		byte[] result = new byte[0];
		try {
			result = new MessagePackMapper().writeValueAsBytes(omcMap);
		} catch (JsonProcessingException e) {
			Logging.error(this, "unable to process JSON", e);
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
				Logging.error(this, "Exception while data reading", ex);
			}
		}

		timeCheck.stop("retrieveResponse " + (result == null ? "empty result" : "non empty result"));
		Logging.info(this, "retrieveResponse ready");

		return result;
	}

	public synchronized Map<String, Object> retrieveResponse(URL url, String requestMethod,
			Map<String, String> requestProperties, Map<String, Object> json, String resultkey) {
		return retrieveResponse(url, requestMethod, requestProperties, json, resultkey, null);
	}

	public synchronized Map<String, Object> retrieveResponse(URL url, String requestMethod,
			Map<String, String> requestProperties, Map<String, Object> json, String resultkey,
			Map<String, Object> responseHeader) {
		Logging.info(this, "retrieveResponse started " + url + " " + requestMethod + " " + requestProperties + " "
				+ json + " " + resultkey + " " + responseHeader);

		conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);
		TimeCheck timeCheck = new TimeCheck(this, "retrieveResponse " + url);
		timeCheck.start();

		ConnectionHandler handler = new ConnectionHandler(url, requestProperties);
		handler.setRequestMethod(requestMethod);
		HttpsURLConnection connection = handler.establishConnection(true);
		conStat = handler.getConnectionState();
		if (connection == null) {
			return new HashMap<>();
		}
		// sending data
		if (json != null) {
			String jsonStr = new JSONObject(json).toString();
			Logging.debug("send " + requestMethod + "jsonStr " + jsonStr);
			try (OutputStream writer = getOutputStreamWriterForConnection(connection, jsonStr.length())) {
				writer.write(jsonStr.getBytes(StandardCharsets.UTF_8));
				writer.flush();
			} catch (IOException iox) {
				Logging.error("exception on writing json request ", iox);
			}

		}

		Logging.info(this, "connection cipher suite " + (connection).getCipherSuite());
		Map<String, Object> result = new HashMap<>();
		// receiving data

		if (conStat.getState() == ConnectionState.STARTED_CONNECTING) {
			try {
				handleResponseCode(connection);

				InputStream stream = getInputStreamBasedOnEncoding(connection);
				Logging.info(this, "guessContentType " + URLConnection.guessContentTypeFromStream(stream));

				result = retrieveResponseBasedOnContentTypeToObject(connection.getContentType(), stream, resultkey);
				if (responseHeader != null) {
					for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
						responseHeader.put(entry.getKey(), entry.getValue().get(0));
					}
					// responseHeader.putAll(connection.getHeaderFields());
				}
				Logging.debug(this, "Connection state after communication: " + conStat);
			} catch (IOException ex) {
				Logging.error(this, "Exception while data reading", ex);
				return null;
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
			Logging.error(this, "Unsupported Content-Type: " + contentType);
		}

		return result;
	}

	private String readInputStream(InputStream fis) {
		StringBuilder sb = new StringBuilder();

		String thisLine = null;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
			while ((thisLine = br.readLine()) != null) {
				sb.append(thisLine);
				sb.append("\n");
			}
			br.close();
		} catch (IOException ex) {
			Logging.error(ex, "Error reading input stream");
		}
		return sb.toString();
	}

	private Map<String, Object> retrieveResponseBasedOnContentTypeToObject(String contentType, InputStream stream,
			String resultKey) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String resultStr = readInputStream(stream).strip();
		Logging.debug("retrieveResponseBasedOnContentType " + contentType + ": " + resultStr);

		Map<String, Object> result = new HashMap<>();
		if (contentType.contains("application/json")) {
			if (resultStr != null && !resultStr.isEmpty() && resultStr.startsWith("{")) {
				result = mapper.readValue(resultStr, new TypeReference<Map<String, Object>>() {
				});
			} else if (resultStr != null && !resultStr.isEmpty() && resultStr.startsWith("[")) {
				result.put(resultKey, mapper.readValue(resultStr, new TypeReference<Object[]>() {
				}));
			} else if (resultStr != null && !resultStr.isEmpty() && resultStr.startsWith("\"")) {
				result.put(resultKey, mapper.readValue(resultStr, new TypeReference<Object>() {
				}));
			} else if (resultStr != null && (resultStr.equals("true") || resultStr.equals("\"true\""))) {
				result.put(resultKey, true);
			} else if (resultStr != null && (resultStr.equals("false") || resultStr.equals("\"false\""))) {
				result.put(resultKey, false);

			} else if (resultStr == null || resultStr.equals("null") || resultStr.equals("\"null\"")) {
				result.put(resultKey, null);
			} else if (resultStr != null && !resultStr.isEmpty() && resultStr.contains(".")) {
				result.put(resultKey, Float.parseFloat(resultStr));
			} else {
				result.put(resultKey, Integer.parseInt(resultStr));
			}

		} else if (contentType.contains("application/msgpack")) {
			result = mapper.readValue(stream, new TypeReference<Map<String, Object>>() {
			});
		} else {
			Logging.error(this, "Unsupported Content-Type: " + contentType);
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
			Logging.error(this, "Response " + connection.getResponseCode() + " " + connection.getResponseMessage() + " "
					+ retrieveErrorFromResponse(connection));
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
