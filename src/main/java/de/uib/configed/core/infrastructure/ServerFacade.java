/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.RPCMethodName;
import de.uib.configed.core.infrastructure.ConnectionHandler.RequestMethod;
import de.uib.configed.core.infrastructure.certificate.CertificateManager;
import de.uib.configed.core.infrastructure.messagebus.Messagebus;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.BrowserUtils;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import de.uib.configed.share.logging.TimeCheck;
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
 * connection is established it sends a {@code POST} request, to send and
 * retrieve data from the response sent by the server.
 *
 * @author Rupert Roeder, Naglis Vidziunas
 */
public class ServerFacade extends AbstractPOJOExecutioner {
	private static final int COMPRESS_MIN_SIZE = 10000;
	private static final Pattern userPattern = Pattern.compile("user:");

	private static final int DEFAULT_JSON_ID = 1;

	private static OpsiServerVersionRetriever versionRetriever;
	private CountDownLatch otpWaiter;

	private HostData hostData = new HostData();

	private String sessionId;
	private String hostWithoutPort;
	private int portHTTPS = Globals.DEFAULT_PORT;
	private boolean useSAML;

	public ServerFacade(String host) {
		this(host, true);
	}

	public ServerFacade(String host, boolean connect) {
		Logging.info("ServerFacade ", host, " connect ", connect);
		if (host == null) {
			return;
		}
		hostData.setHost(host);
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

	private synchronized void connect(String host, String username, String password, String otp, boolean useSAML) {
		this.useSAML = useSAML;
		hostData.setHost(host);

		int idx = -1;
		if (host.contains("[") && host.contains("]")) {
			idx = host.indexOf(":", host.indexOf("]"));
		} else {
			idx = host.indexOf(":");
		}

		if (idx > -1) {
			hostWithoutPort = host.substring(0, idx);
			this.portHTTPS = Integer.parseInt(host.substring(idx + 1, host.length()));
		} else {
			hostWithoutPort = host;
		}

		hostData.setUser(username);
		hostData.setPassword(password);
		hostData.setOtp(otp);

		setConnectionState(new ConnectionState());
		if (useSAML && !connectSAML()) {
			Logging.error(this, "SAML connection failed");
			return;
		}
		CertificateManager.init(produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE), hostWithoutPort + "_" + portHTTPS);
		checkServerVersion();
	}

	public boolean isUseSAML() {
		return useSAML;
	}

	public Map<String, List<String>> getHeaders() {
		if (getConnectionState() != null && getConnectionState().getState() == ConnectionState.NOT_CONNECTED) {
			return Map.of();
		}
		Logging.info("getHeaders started");
		int timeout = 2000;
		System.setProperty("sun.net.client.defaultConnectTimeout", timeout + "");
		Map<String, String> requestProperties = new HashMap<>();
		requestProperties.put("Accept", "application/json");
		if (sessionId != null) {
			requestProperties.put("Cookie", sessionId);
		}
		URL url = makeURL("/auth/session_id");
		ConnectionHandler handler = new ConnectionHandler(url, requestProperties, false);
		HttpsURLConnection connection = handler.establishInsecureConnection(true, timeout);
		setConnectionState(handler.getConnectionState());
		if (connection == null || handler.getConnectionState().getState() == ConnectionState.NOT_CONNECTED) {
			Logging.warning("try to get headers, but no connection. ", "conStat ", getConnectionState(), "state: ",
					getConnectionState().getState());
			System.setProperty("sun.net.client.defaultConnectTimeout", Globals.DEFAULT_TIMEOUT + "");
			return Map.of();
		}
		Map<String, List<String>> result = Map.of();
		try {
			handleResponseCode(connection);
			result = connection.getHeaderFields();
		} catch (IOException ex) {
			Logging.error(this, ex, "Exception while trying to get headers");
		}
		System.setProperty("sun.net.client.defaultConnectTimeout", Globals.DEFAULT_TIMEOUT + "");
		return result;
	}

	private boolean connectSAML() {
		Logging.info(this, "connectSAML started ");
		// register and get new session id (may throw exception)
		ssoRequestSessionId();
		if (!ssoOpenBrowser()) {
			Logging.error("connectSAML error opening browser");
			return false;
		}
		return ssoCheckAuthenticated();
	}

	private void ssoRequestSessionId() {
		Logging.info(this, "ssoRequestSessionId started");
		Map<String, String> requestProperties = new HashMap<>();
		Map<String, Object> jsonProperties = null;
		requestProperties.put("Accept", "application/json");
		String localKeySID = "respondSessionId";
		//////// register and get new session id
		URL urlGetSid = makeURL("/auth/session_id");
		CertificateManager.init(produceBaseURL("/ssl/" + Globals.CERTIFICATE_FILE), hostWithoutPort + "_" + portHTTPS);
		setConnectionState(new ConnectionState(ConnectionState.STARTED_CONNECTING));
		Map<String, Object> result = retrieveResponse(urlGetSid, RequestMethod.GET, requestProperties, jsonProperties,
				localKeySID);
		if (getConnectionState().getState() == ConnectionState.RETRY_CONNECTION) {
			Logging.debug("connectSAML retry connection");
			result = retrieveResponse(urlGetSid, RequestMethod.GET, requestProperties, jsonProperties, localKeySID);
		}

		if (result == null || result.isEmpty() || !result.containsKey(localKeySID)) {
			Logging.error("connectSAML no sessionId received. Result: ", result);
			Logging.error(this, "sessionId not received");
			return;
		}
		this.sessionId = (String) result.get(localKeySID);
		if (sessionId == null) {
			Logging.error(this, "Requested sessionId is null");
		} else {
			sessionId = sessionId.contains("=") ? sessionId : ("opsiconfd-session=" + sessionId);
		}
	}

	private boolean ssoOpenBrowser() {
		Logging.info(this, "ssoOpenBrowser started");

		if (sessionId == null) {
			Logging.error(this, "sessionId is null");
			return false;
		}
		/////// open browser
		String sid = sessionId.contains("=") ? sessionId.split("=")[1] : sessionId;
		String urlBrowserSaml = "/auth/saml/login?session_id=" + sid + "&redirect=close_window";
		URL url = makeURL(urlBrowserSaml);
		if (url != null) {
			BrowserUtils.openLink(url.toString());
			return true;
		} else {
			// But actually this cannot happen
			return false;
		}
	}

	private boolean ssoCheckAuthenticated() {
		/////// check if authenticated
		Logging.info(this, "ssoCheckAuthenticated started");

		Map<String, String> requestProperties = new HashMap<>();
		requestProperties.put("Accept", "application/json");
		requestProperties.put("Cookie", sessionId.contains("=") ? sessionId : ("opsiconfd-session=" + sessionId));

		URL urlAuthenticated = makeURL("/auth/wait_authenticated");
		Map<String, Object> jsonProperties = new HashMap<>();
		jsonProperties.put("wait_time", 60);
		HashMap<String, Object> responseHeader = new HashMap<>();
		Map<String, Object> result = retrieveResponse(urlAuthenticated, RequestMethod.POST, requestProperties,
				jsonProperties, "authenticated", responseHeader);

		boolean isAuthenticated = false;

		if (!result.containsKey("authenticated")) {
			Logging.error(this, "authenticated not received");
		} else if (responseHeader.isEmpty()) {
			Logging.error(this, "responseHeaders not received");
		} else {
			// set credentials for further requests and for information
			String uname = (String) responseHeader.get("x-opsi-user-id");
			if (uname == null) {
				Logging.error(this, "username not received");
			} else {
				hostData.setUser(userPattern.split(uname, 2)[1]);
				isAuthenticated = (boolean) result.get("authenticated");
			}
		}

		return isAuthenticated;
	}

	private synchronized void checkServerVersion() {
		if (useSAML) {
			versionRetriever = new OpsiServerVersionRetriever(produceBaseURL("/"), sessionId);
		} else {
			versionRetriever = new OpsiServerVersionRetriever(produceBaseURL("/"), hostData.getUser(),
					hostData.getPassword());
		}
		versionRetriever.checkServerVersion();
	}

	public static OpsiServerVersionRetriever getOpsiServerVersionRetriever() {
		return versionRetriever;
	}

	private Map<String, String> produceGeneralRequestProperties(byte[] data) {
		Map<String, String> requestProperties = new HashMap<>();
		if (!useSAML) {
			String authorization = Base64.getEncoder()
					.encodeToString((hostData.getUser() + ":" + hostData.getPassword() + hostData.getOtp())
							.getBytes(StandardCharsets.UTF_8));
			requestProperties.put("Authorization", "Basic " + authorization);
		}

		// has to be value between 1 and 43300 [sec]
		requestProperties.put("X-opsi-session-lifetime", "300");
		requestProperties.put("Accept-Encoding", "lz4, gzip");
		requestProperties.put("User-Agent", Globals.APPNAME_SERVER_CONNECTION + " " + Globals.VERSION);
		requestProperties.put("Accept", "application/msgpack");
		requestProperties.put("Content-Type", "application/msgpack");

		int messageSize = data.length;

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

		if (hostData.getHost().contains("://")) {
			url = hostData.getHost() + ":" + portHTTPS + rpcPath;
		} else if (hostData.getHost().contains(":")) {
			url = "https://" + hostData.getHost() + rpcPath;
		} else {
			url = "https://" + hostData.getHost() + ":" + portHTTPS + rpcPath;
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
			Logging.error(this, ex, "URI Syntax error: ", baseURL);
		}

		return serviceURL;
	}

	private byte[] produceMessagePack(RPCMethodName methodname, Object[] parameters) {
		byte[] result = new byte[0];
		try {
			result = new MessagePackMapper().writeValueAsBytes(createMap(methodname, parameters));
		} catch (JsonProcessingException e) {
			Logging.error(this, e, "unable to process JSON");
		}

		return result;
	}

	private static Map<String, Object> createMap(RPCMethodName methodname, Object[] parameters) {
		List<Object> params = new ArrayList<>();

		for (Object parameter : parameters) {
			params.add(parameter instanceof Object[] array ? Arrays.asList(array) : parameter);
		}

		return Map.of("id", DEFAULT_JSON_ID, "method", methodname.toString(), "params", params);
	}

	public boolean testConnection(boolean notifyUserOfErrors) {
		ConnectionHandler handler = new ConnectionHandler(makeURL(), produceGeneralRequestProperties(new byte[0]),
				notifyUserOfErrors);
		handler.setRequestMethod(RequestMethod.HEAD);
		handler.establishConnection(false);
		return handler.getConnectionState().getState() == ConnectionState.CONNECTED;
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
	public Map<String, Object> retrieveResponse(RPCMethodName methodname, Object[] parameters) {
		Logging.info(this, "retrieveResponse started");

		if ((hostData.getOtp() == null && Utils.isMultiFactorAuthenticationEnabled())
				|| !ParallelTaskExecutor.isNewTasksAllowed()) {
			if (getConnectionState().getState() == ConnectionState.NOT_CONNECTED && testConnection(false)) {
				ParallelTaskExecutor.allowNewTasks(true);
			} else {
				return Map.of();
			}
		}

		TimeCheck timeCheck = new TimeCheck(this, "retrieveResponse " + methodname);
		timeCheck.start();

		ConnectionHandler handler = new ConnectionHandler(makeURL(),
				produceGeneralRequestProperties(produceMessagePack(methodname, parameters)));
		HttpsURLConnection connection = handler.establishConnection(true);
		setConnectionState(handler.getConnectionState());
		sendPostRequest(connection, methodname, parameters);

		if (connection == null) {
			return null;
		}

		Logging.info(this, "connection cipher suite ", (connection).getCipherSuite());

		Map<String, Object> result = retrieveResponse(connection);

		timeCheck.stop("retrieveResponse " + (result == null ? "empty result" : "non empty result"));
		Logging.info(this, "retrieveResponse ready");

		return result;
	}

	private Map<String, Object> retrieveResponse(HttpsURLConnection connection) {
		if (getConnectionState().getState() == ConnectionState.STARTED_CONNECTING
				|| getConnectionState().getState() == ConnectionState.CONNECTED) {
			try {
				handleResponseCode(connection);

				if (getConnectionState().getState() == ConnectionState.CONNECTED) {
					retrieveSessionIDFromResponse(connection);
					InputStream stream = getInputStreamBasedOnEncoding(connection);

					Logging.info(this, "guessContentType ", URLConnection.guessContentTypeFromStream(stream));

					return retrieveResponseBasedOnContentType(connection.getContentType(), stream);
				} else {
					Logging.warning(this, "Encountered unhandled connection state: ", getConnectionState());
				}
			} catch (SocketTimeoutException ste) {
				Logging.warning(ste, "Timeout exception reached, we have a set timeout of",
						System.getProperty("sun.net.client.defaultConnectTimeout"), "ms");
				setConnectionState(new ConnectionState(ConnectionState.TIMEOUT));
			} catch (IOException ex) {
				Logging.error(this, ex, "Exception while data reading");
			}
		}

		return Map.of();
	}

	public Map<String, Object> retrieveResponse(URL url, RequestMethod requestMethod,
			Map<String, String> requestProperties, Map<String, Object> json, String resultkey) {
		return retrieveResponse(url, requestMethod, requestProperties, json, resultkey, null);
	}

	public Map<String, Object> retrieveResponse(URL url, RequestMethod requestMethod,
			Map<String, String> requestProperties, Map<String, Object> json, String resultkey,
			Map<String, Object> responseHeader) {
		Logging.info(this, "retrieveResponse started ", url, " ", requestMethod.toString(), " ", requestProperties, " ",
				json, " ", resultkey, " ", responseHeader);

		TimeCheck timeCheck = new TimeCheck(this, "retrieveResponse " + url);
		timeCheck.start();

		ConnectionHandler handler = new ConnectionHandler(url, requestProperties);
		handler.setRequestMethod(requestMethod);
		HttpsURLConnection connection = handler.establishConnection(true);
		setConnectionState(handler.getConnectionState());
		if (connection == null) {
			return Map.of();
		}
		// sending data
		if (json != null) {
			String jsonStr = new JSONObject(json).toString();
			Logging.debug("send ", requestMethod, "jsonStr ", jsonStr);
			try (OutputStream writer = getOutputStreamWriterForConnection(connection, jsonStr.length())) {
				writer.write(jsonStr.getBytes(StandardCharsets.UTF_8));
				writer.flush();
			} catch (IOException iox) {
				Logging.info(this, "exception on writing json request ", iox);
			}
		}

		Logging.info(this, "connection cipher suite ", (connection).getCipherSuite());
		Map<String, Object> result = Map.of();
		// receiving data

		if (getConnectionState().getState() == ConnectionState.CONNECTED) {
			try {
				handleResponseCode(connection);

				InputStream stream = getInputStreamBasedOnEncoding(connection);
				Logging.info(this, "guessContentType ", URLConnection.guessContentTypeFromStream(stream));

				result = retrieveResponseBasedOnContentTypeToObject(connection.getContentType(), stream, resultkey);
				addHeaderFieldsToMap(connection, responseHeader);
				Logging.debug(this, "Connection state after communication: ", getConnectionState());
			} catch (SocketTimeoutException ste) {
				Logging.warning(ste, "Timeout exception reached, we have a set timeout of",
						System.getProperty("sun.net.client.defaultConnectTimeout"), "ms");
				setConnectionState(new ConnectionState(ConnectionState.TIMEOUT));
			} catch (IOException ex) {
				Logging.error(this, ex, "Exception while data reading");
			}
		}
		timeCheck.stop("retrieveResponse " + (result == null ? "empty result" : "non empty result"));
		Logging.info(this, "retrieveResponse ready");

		return result;
	}

	private static void addHeaderFieldsToMap(HttpsURLConnection connection, Map<String, Object> responseHeader) {
		if (responseHeader != null) {
			for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
				responseHeader.put(entry.getKey(), entry.getValue().get(0));
			}
		}
	}

	private void sendPostRequest(HttpsURLConnection connection, RPCMethodName methodname, Object[] parameters) {
		if (connection == null) {
			return;
		}

		byte[] message = produceMessagePack(methodname, parameters);

		try (OutputStream writer = getOutputStreamWriterForConnection(connection, message.length)) {
			writer.write(message);
			writer.flush();

			Logging.debug(this, "(POST) sending: ", methodname);
		} catch (IOException iox) {
			Logging.info(this, "exception on writing json request ", iox);
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
		if (contentType.contains("application/json")) {
			ObjectMapper mapper = new ObjectMapper();
			return mapper.readValue(stream, new TypeReference<HashMap<String, Object>>() {
			});
		} else if (contentType.contains("application/msgpack")) {
			ObjectMapper mapper = new MessagePackMapper();
			return mapper.readValue(stream, new TypeReference<HashMap<String, Object>>() {
			});
		} else {
			Logging.error(this, "Unsupported Content-Type: ", contentType);
			return Map.of();
		}
	}

	private static String readInputStream(InputStream fis) {
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
		Logging.debug("retrieveResponseBasedOnContentType ", contentType);
		Map<String, Object> result;
		if (contentType.contains("application/json")) {
			result = getJSONResult(stream, resultKey);
		} else if (contentType.contains("application/msgpack")) {
			result = new ObjectMapper().readValue(stream, new TypeReference<Map<String, Object>>() {
			});
		} else {
			Logging.error(this, "Unsupported Content-Type: ", contentType);
			result = new HashMap<>();
		}
		return result;
	}

	private static Map<String, Object> getJSONResult(InputStream stream, String resultKey) throws IOException {
		Map<String, Object> result = new HashMap<>();

		ObjectMapper mapper = new ObjectMapper();
		String resultStr = readInputStream(stream).strip();
		if (resultStr.isEmpty()) {
			result.put(resultStr, result);
		} else if (resultStr.startsWith("{")) {
			result = mapper.readValue(resultStr, new TypeReference<Map<String, Object>>() {
			});
		} else if (resultStr.startsWith("[")) {
			result.put(resultKey, mapper.readValue(resultStr, new TypeReference<Object[]>() {
			}));
		} else if (resultStr.startsWith("\"")) {
			result.put(resultKey, mapper.readValue(resultStr, new TypeReference<Object>() {
			}));
		} else if ("true".equals(resultStr)) {
			result.put(resultKey, true);
		} else if ("false".equals(resultStr)) {
			result.put(resultKey, false);
		} else if ("null".equals(resultStr)) {
			result.put(resultKey, null);
		} else if (resultStr.contains(".")) {
			result.put(resultKey, Float.parseFloat(resultStr));
		} else {
			result.put(resultKey, Integer.parseInt(resultStr));
		}

		return result;
	}

	private void handleResponseCode(HttpsURLConnection connection) throws IOException {
		int responseCode = connection.getResponseCode();
		String responseMessage = connection.getResponseMessage();
		Logging.debug(this, "Response ", responseCode, " ", responseMessage);

		if (responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_OK) {
			// Normal response; clear error flag if needed
			setConnectionState(new ConnectionState(ConnectionState.CONNECTED, "ok"));
		} else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
			Logging.debug("Unauthorized: ", sessionId, ", mfa=", Utils.isMultiFactorAuthenticationEnabled());
			if (Utils.isMultiFactorAuthenticationEnabled() && ConfigedMain.getMainFrame() != null) {
				ParallelTaskExecutor.cancelAllExecutorsTasks();

				// Don't initiate Messagebus reconnection, since the connection is restablished once
				// correct OTP is provided. Otherwise, Messagebus reconnection attempts may block client
				// IP address and may seem as suspicious activity.
				Messagebus.getInstance().setReconnecting(true);
				ConnectionErrorReporter.getInstance().notify("", ConnectionErrorType.MFA_ERROR);
				setConnectionState(new ConnectionState(ConnectionState.NOT_CONNECTED));
				if (hostData.getOtp() != null) {
					Logging.debug(this, "MFA error encountered, we wait for new OTP input");
					hostData.setOtp(waitForOTPInput());
				} else {
					Logging.debug(this, "old OTP was used, we set it to use new OTP");
					hostData.setOtp(hostData.getOtp());
				}
				setConnectionState(new ConnectionState(ConnectionState.RETRY_CONNECTION));
			} else {
				setConnectionState(new ConnectionState(ConnectionState.ERROR, responseMessage));
			}
		} else {
			ParallelTaskExecutor.cancelAllExecutorsTasks();
			setConnectionState(new ConnectionState(ConnectionState.ERROR, responseMessage));
			Logging.error(this, "Response ", responseCode, " ", responseMessage, " ",
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
			Logging.debug(this, "gzipped ", gzipped);
			deflated = "deflate".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));
			Logging.debug(this, "deflated\r\n", "\t\t\t\t ", deflated);
			lz4compressed = "lz4".equalsIgnoreCase(connection.getHeaderField("Content-Encoding"));

			Logging.debug(this, "lz4compressed ", lz4compressed);
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

	public HostData getHostData() {
		return hostData;
	}

	/**
	 * Retrieve used session by the connection.
	 *
	 * @return used session by the connection.
	 */
	public String getSessionId() {
		return sessionId;
	}

	public synchronized void setOTP(String otp) {
		hostData.setOtp(otp);
		if (otpWaiter != null) {
			otpWaiter.countDown();
		}
	}

	/**
	 * Resets the OTP wait cycle. Should be called before initiating a new OTP
	 * input cycle from the MFA dialog.
	 */
	public synchronized void resetOTPWaiter() {
		otpWaiter = new CountDownLatch(1);
	}

	/**
	 * Blocks execution until the OTP is provided via
	 * {@link #setOTP(String otp)}. It is recommended to call this method only
	 * when MFA is enabled, to wait for user input.
	 * 
	 * @return the OTP string provided by the user.
	 */
	public synchronized String waitForOTPInput() {
		try {
			otpWaiter.await();
			otpWaiter = null;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Logging.error("ConfigedMain waiting for OTP interrupted: " + e.getMessage());
			return null;
		}
		return hostData.getOtp();
	}

	/**
	 * Securely clears all authentication-related fields, overwriting sensitive
	 * data in memory.
	 */
	public void clearAuthenticationData() {
		wipeSensitiveString(hostData.getPassword());
		wipeSensitiveString(hostData.getOtp());

		hostData = new HostData();

		wipeSensitiveString(sessionId);
		sessionId = null;

		otpWaiter = null;
	}

	/**
	 * Overwrites the contents of a String with null characters to reduce the
	 * risk of sensitive data lingering in memory.
	 * 
	 * @param value the String to wipe
	 */
	private static void wipeSensitiveString(String value) {
		if (value != null) {
			char[] chars = value.toCharArray();
			Arrays.fill(chars, '\0');
		}
	}
}
