/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

/**
 * A {@code ConnectionHandler} class handles connection with the server and
 * certificate validation.
 * <p>
 * It connects with the server, that is provided during the initialization of a
 * class. The connection always happens using the HTTPS protocol. The
 * certificate validation depends on whether or not the certificate verification
 * is disabled ({@code Globals.disableCertificateVerification}).
 * <p>
 * Example Usage:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * {@code
 * URL serviceURL = new URL("https://192.168.1.2/rpc");
 * Map<String, String> requestProperties = new HashMap<>();
 * String authorization = Base64.getEncoder().encodeToString(("username:password").getBytes(StandardCharsets.UTF_8));
 * requestProperties.put("Authorization", "Basic " + authorization);
 * requestProperties.put("Content-Type", "application/json");
 * ConnectionHandler handler = new ConnectionHandler(serviceURL, requestProperties);
 * 
 * // true if URL connection should be used for output, otherwise false
 * HttpsURLConnection connection = handler.establishConnection(true);
 * ConnectionState connectionState = handler.getConnectionState();
 * 
 * try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), UTF8DEFAULT);
 * 		BufferedWriter out = new BufferedWriter(writer)) {
 * 	out.write("some text");
 * 	out.flush();
 * } catch (IOException iox) {
 * 	Logging.info(this, "exception on writing json request " + iox);
 * }
 * }
 * </pre>
 * 
 * </blockquote>
 */
public class ConnectionHandler {
	private URL serviceUrl;
	private Map<String, String> requestProperties;
	private ConnectionState conStat;
	private ConnectionErrorObserver observer;
	private ConnectionErrorReporter reporter;
	private String requestMethod = "POST";

	/**
	 * Constructs {@code ConnectionHandler} object with provided information.
	 * 
	 * @param serviceUrl        service URL with which to connect.
	 * @param requestProperties additional request properties.
	 */
	public ConnectionHandler(URL serviceUrl, Map<String, String> requestProperties) {
		this.serviceUrl = serviceUrl;
		this.requestProperties = new HashMap<>(requestProperties);
		this.conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);
		this.observer = ConnectionErrorObserver.getInstance();
		this.reporter = new ConnectionErrorReporter(conStat);
		observer.subscribe(reporter);
	}

	/**
	 * Retrieves the {@link ConnectionState}. {@code ConnectionHandler} uses
	 * {@link ConnectionState} to indicate the state of the connection. The
	 * connection state can change during the {@code establishConnection} method
	 * execution.
	 * <p>
	 * Currently possible {@link ConnectionState} can be:
	 * <ul>
	 * <li>{@code STARTED_CONNECTING} indicates no error (i. e. connection was
	 * successfull).</li>
	 * <li>{@code ERROR} indicates encountered unexpected error during the
	 * connection stage.</li>
	 * <li>{@code RETRY_CONNECTION} indicates encountered expected error during
	 * the connection stage and that the error was resolved.</li>
	 * <li>{@code INTERRUPED} indicates the connection stage was interrupted and
	 * could not complete.</li>
	 * </ul>
	 * 
	 * @return connection state.
	 */
	public ConnectionState getConnectionState() {
		return conStat;
	}

	/**
	 * Sets the request method to use for the connection (during the
	 * {@code establishConnection} method execution). By default the request
	 * method is {@code POST}.
	 * 
	 * @param requestMethod to use for the connection.
	 */
	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * Establishes connection with the server and verifies the server
	 * certificate with the client's certificate. The certificate verification
	 * depends on whether or not the certificate verification feature is
	 * enabled.
	 * <p>
	 * The request method for the connection is by default set to {@code POST}.
	 * However, if you want to change it you can use {@code setRequestMethod}
	 * method. For Example:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * {@code
	 * ConnectionHandler handler = new ConnectionHandler(serviceUrl, requestProperties);
	 * handler.setRequestMethod("GET");
	 * HttpsURLConnection connection = handler.establishConnection(false);
	 * } 
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param doOutput whether or not the DoOutput flag in
	 *                 {@code HttpsURLConnection} should be enabled.
	 * @return established HTTPS connection with the server.
	 */
	public HttpsURLConnection establishConnection(boolean doOutput) {
		CertificateValidator certValidator = !Globals.disableCertificateVerification
				? CertificateValidatorFactory.createSecure()
				: CertificateValidatorFactory.createInsecure();

		HttpsURLConnection connection = null;

		try {
			connection = (HttpsURLConnection) serviceUrl.openConnection();
			connection.setDoOutput(doOutput);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestMethod(requestMethod);

			if (requestProperties != null && !requestProperties.isEmpty()) {
				for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
					connection.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			Logging.debug(this, "https protocols given by system " + Configed.SYSTEM_SSL_VERSION);
			Logging.info(this,
					"retrieveResponse method=" + connection.getRequestMethod() + ", headers="
							+ connection.getRequestProperties() + ", cookie="
							+ (requestProperties.get("Cookie") == null ? "null"
									: (requestProperties.get("Cookie").substring(0, 26) + "...")));

			connection.setSSLSocketFactory(certValidator.createSSLSocketFactory());
			connection.setHostnameVerifier(certValidator.createHostnameVerifier());
			connection.connect();
		} catch (SSLException ex) {
			Logging.debug(this, "caught SSLException: " + ex);

			StringBuilder message = new StringBuilder();
			message.append(Configed.getResourceValue("JSONthroughHTTP.certificateWarning") + "\n\n");

			if (certValidator.certificateLocallyAvailable()) {
				message.append(Configed.getResourceValue("JSONthroughHTTP.certificateIsUnverified") + "\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.unableToVerify"));
			} else {
				message.append(Configed.getResourceValue("JSONthroughHTTP.certificateIsUnverified") + "\n");
				message.append(Configed.getResourceValue("JSONthroughHTTP.noCertificateFound"));
			}

			message.append("\n\n");
			message.append(Configed.getResourceValue("JSONthroughHTTP.stillConnectToServer"));
			observer.notify(message.toString(), ConnectionErrorType.FAILED_CERTIFICATE_VALIDATION_ERROR);

			conStat = reporter.getConnectionState();
			return null;
		} catch (IOException ex) {
			conStat = new ConnectionState(ConnectionState.ERROR, ex.toString());
			Logging.error("Exception on connecting, ", ex);
			return null;
		}

		return connection;
	}
}
