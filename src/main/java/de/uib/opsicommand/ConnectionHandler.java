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
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import de.uib.configed.Configed;
import de.uib.opsicommand.certificate.CertificateValidator;
import de.uib.opsicommand.certificate.CertificateValidatorFactory;
import de.uib.utilities.logging.Logging;

public class ConnectionHandler {
	private static final String[] SUPPORTED_REQUEST_METHODS = { "POST", "GET" };

	private URL serviceURL;
	private Map<String, String> requestProperties;
	private ConnectionState conStat;
	private ConnectionErrorReporter reporter;
	private String requestMethod = "POST";

	/**
	 * Constructs {@code ConnectionHandler} object with provided information.
	 * 
	 * @param serviceURL        service URL with which to connect.
	 * @param requestProperties additional request properties.
	 */
	public ConnectionHandler(URL serviceURL, Map<String, String> requestProperties) {
		this.serviceURL = serviceURL;
		this.requestProperties = requestProperties != null ? new HashMap<>(requestProperties) : null;
		this.conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);
		this.reporter = ConnectionErrorReporter.getNewInstance(conStat);
	}

	/**
	 * Retrieves the {@link ConnectionState}. {@code ConnectionHandler} uses
	 * {@link ConnectionState} to indicate the state of the connection. The
	 * connection state can change during the
	 * {@link #establishConnection(boolean)} method execution.
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
	 * {@link #establishConnection(boolean)} method execution). By default the
	 * request method is {@code POST}.
	 * <p>
	 * You can only pass supported request methods and a null, if no request
	 * method should be used. Currently supported request methods are
	 * {@code POST} and {@code GET}.
	 * 
	 * @param requestMethod to use for the connection.
	 * @throws IllegalArgumentException if request method is not supported.
	 */
	public void setRequestMethod(String requestMethod) throws IllegalArgumentException {
		if (requestMethod == null) {
			Logging.info(this, "no request method is used");
			this.requestMethod = requestMethod;
			return;
		}

		boolean isMethodSupported = false;

		for (String supportedRequestMethod : SUPPORTED_REQUEST_METHODS) {
			if (supportedRequestMethod.equals(requestMethod)) {
				isMethodSupported = true;
				break;
			}
		}

		if (isMethodSupported) {
			Logging.info(this, "request method is supported: " + requestMethod);
			this.requestMethod = requestMethod;
		} else {
			Logging.warning(this, "request method is unsupported: " + requestMethod);
			throw new IllegalArgumentException("request method is unsupported: " + requestMethod);
		}
	}

	/**
	 * Retrieve used request method by the {@code ConnectionHandler}.
	 * 
	 * @return used request method.
	 */
	public String getRequestMethod() {
		return requestMethod;
	}

	/**
	 * Establishes connection with the server and verifies the server
	 * certificate with the client's certificate. The certificate verification
	 * depends on whether or not the certificate verification feature is
	 * enabled.
	 * <p>
	 * The request method for the connection is by default set to {@code POST}.
	 * However, if you want to change it you can use
	 * {@link #setRequestMethod(String)} method. For Example:
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
	 * @return established HTTPS connection with the server; null indicates
	 *         unsuccessful connection.
	 */
	public HttpsURLConnection establishConnection(boolean doOutput) {
		if (serviceURL == null) {
			return null;
		}

		CertificateValidator certValidator = CertificateValidatorFactory.createValidator();
		HttpsURLConnection connection = null;

		try {
			connection = (HttpsURLConnection) serviceURL.openConnection();
			connection.setDoOutput(doOutput);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			if (requestMethod != null) {
				connection.setRequestMethod(requestMethod);
			}

			if (requestProperties != null) {
				for (Entry<String, String> entry : requestProperties.entrySet()) {
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

			if (reporter.getConnectionState().getState() != ConnectionState.INTERRUPTED) {
				reporter.notify(produceCertificateWarningMessage(certValidator),
						ConnectionErrorType.FAILED_CERTIFICATE_VALIDATION_ERROR);
			}

			conStat = reporter.getConnectionState();
			connection = null;
		} catch (IOException ex) {
			if (reporter.getConnectionState().getState() == ConnectionState.INTERRUPTED) {
				conStat = reporter.getConnectionState();
			} else {
				conStat = new ConnectionState(ConnectionState.ERROR, ex.toString());
				Logging.error("Exception on connecting, ", ex);
			}

			connection = null;
		}

		return connection;
	}

	private static String produceCertificateWarningMessage(CertificateValidator certValidator) {
		StringBuilder message = new StringBuilder();
		message.append(Configed.getResourceValue("ConnectionHandler.certificateWarning") + "\n\n");

		if (certValidator.certificateLocallyAvailable()) {
			message.append(Configed.getResourceValue("ConnectionHandler.certificateIsUnverified") + "\n");
			message.append(Configed.getResourceValue("ConnectionHandler.unableToVerify"));
		} else {
			message.append(Configed.getResourceValue("ConnectionHandler.certificateIsUnverified") + "\n");
			message.append(Configed.getResourceValue("ConnectionHandler.noCertificateFound"));
		}

		message.append("\n\n");
		message.append(Configed.getResourceValue("ConnectionHandler.stillConnectToServer"));

		return message.toString();
	}
}
