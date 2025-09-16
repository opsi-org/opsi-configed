/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import de.uib.configed.core.domain.serverdata.ParallelTaskExecutor;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.certificate.CertificateValidator;
import de.uib.configed.core.infrastructure.certificate.CertificateValidatorFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.logging.Logging;

public class ConnectionHandler {
	private static final int DEFAULT_READ_TIMEOUT_MS = 60_000;
	private static final String HEADER_OPSI_SERVER_ROLE = "X-opsi-server-role";
	private static final String ROLE_CONFIGSERVER = "configserver";

	private URL serviceURL;
	private Map<String, String> requestProperties;
	private ConnectionState conStat;
	private ConnectionErrorReporter reporter;
	private RequestMethod requestMethod = RequestMethod.POST;
	private boolean notifyUserOfErrors;

	public enum RequestMethod {
		POST, GET, HEAD
	}

	/**
	 * Constructs {@code ConnectionHandler} object with provided information.
	 * 
	 * @param serviceURL        service URL with which to connect.
	 * @param requestProperties additional request properties.
	 */
	public ConnectionHandler(URL serviceURL, Map<String, String> requestProperties) {
		this(serviceURL, requestProperties, true);
	}

	/**
	 * Constructs {@code ConnectionHandler} object with provided information.
	 *
	 * @param serviceURL        service URL with which to connect.
	 * @param requestProperties additional request properties.
	 * @param notifyUserOfError whether user should be nodified of an
	 *                          encountered connection error.
	 */
	public ConnectionHandler(URL serviceURL, Map<String, String> requestProperties, boolean notifyUserOfErrors) {
		this.serviceURL = serviceURL;
		this.requestProperties = requestProperties != null ? new HashMap<>(requestProperties) : null;
		this.conStat = new ConnectionState(ConnectionState.STARTED_CONNECTING);
		this.reporter = ConnectionErrorReporter.getNewInstance(conStat);
		this.notifyUserOfErrors = notifyUserOfErrors;
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
	 * 
	 * @param requestMethod to use for the connection.
	 */
	public void setRequestMethod(RequestMethod requestMethod) {
		this.requestMethod = requestMethod;
	}

	/**
	 * Retrieve used request method by the {@code ConnectionHandler}.
	 * 
	 * @return used request method.
	 */
	public RequestMethod getRequestMethod() {
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
		return establishConnection(doOutput, false);
	}

	public HttpsURLConnection establishConnection(boolean doOutput, boolean useInsecure) {
		return establishConnection(doOutput, useInsecure, Globals.DEFAULT_TIMEOUT);
	}

	public HttpsURLConnection establishInsecureConnection(boolean doOutput, int timeout) {
		return establishConnection(doOutput, true, timeout);
	}

	private HttpsURLConnection establishConnection(boolean doOutput, boolean useInsecure, int timeout) {
		if (serviceURL == null) {
			return null;
		}
		Logging.info(this, "establishing connection with ", serviceURL);

		CertificateValidator certValidator = useInsecure ? CertificateValidatorFactory.getInsecure()
				: CertificateValidatorFactory.getValidator();
		HttpsURLConnection connection = null;

		try {
			if (!isTargetConfigServer(certValidator, timeout)) {
				Logging.info(this, "Connection not established: target is not a configserver. endpoint=",
						safeEndpoint(serviceURL), ", userNotified=", notifyUserOfErrors,
						". Enable DEBUG for preflight details.");
				conStat = new ConnectionState(ConnectionState.ERROR,
						"Connection attempt to depot server blocked – only configservers are permitted.");
				if (notifyUserOfErrors) {
					reporter.notify(Configed.getResourceValue("ConnectionHandler.connectionDenied"),
							ConnectionErrorType.GENERAL_ERROR);
				}
				// Reset validators so next attempt builds fresh state
				CertificateValidatorFactory.resetCertificateValidators();
				return null;
			}

			connection = (HttpsURLConnection) serviceURL.openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MS);
			connection.setDoOutput(doOutput);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			if (requestMethod != null) {
				connection.setRequestMethod(requestMethod.toString());
			}

			if (requestProperties != null) {
				for (Entry<String, String> entry : requestProperties.entrySet()) {
					connection.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			Logging.debug(this, "https protocols given by system ", Configed.SYSTEM_SSL_VERSION);
			Logging.info(this, "retrieveResponse method=", connection.getRequestMethod(), ", headers=",
					connection.getRequestProperties(), ", cookie=", (requestProperties.get("Cookie") == null ? "null"
							: (requestProperties.get("Cookie").substring(0, 26) + "...")));

			connection.setSSLSocketFactory(certValidator.getSSLSocketFactory());
			connection.setHostnameVerifier(certValidator.getHostnameVerifier());
			connection.connect();

			conStat = new ConnectionState(ConnectionState.CONNECTED);
		} catch (SSLException ex) {
			Logging.debug(this, "caught SSLException: ", ex);

			reportSSLException(certValidator);

			conStat = reporter.getConnectionState();
			connection = null;

			// We need to reset the certificate validators when the validation failed
			// so that new validators can be created on the next try
			CertificateValidatorFactory.resetCertificateValidators();
		} catch (SocketTimeoutException ste) {
			conStat = new ConnectionState(ConnectionState.TIMEOUT, ste.toString());
			Logging.warning(ste, "Timeout exception reached, we have a set timeout of",
					System.getProperty("sun.net.client.defaultConnectTimeout"), "ms");

			// We need to reset the certificate validators when the validation failed
			// so that new validators can be created on the next try
			CertificateValidatorFactory.resetCertificateValidators();
		} catch (IOException ex) {
			reportIOException(ex);

			connection = null;

			// We need to reset the certificate validators when the validation failed
			// so that new validators can be created on the next try
			CertificateValidatorFactory.resetCertificateValidators();
		}

		return connection;
	}

	private boolean isTargetConfigServer(CertificateValidator certValidator, int timeout) throws IOException {
		HttpsURLConnection connection = null;
		try {
			connection = (HttpsURLConnection) serviceURL.openConnection();
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(DEFAULT_READ_TIMEOUT_MS);
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(false);
			connection.setRequestMethod(RequestMethod.HEAD.toString());

			if (requestProperties != null) {
				for (Entry<String, String> entry : requestProperties.entrySet()) {
					connection.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			connection.setSSLSocketFactory(certValidator.getSSLSocketFactory());
			connection.setHostnameVerifier(certValidator.getHostnameVerifier());

			connection.connect();

			int code = connection.getResponseCode();
			String role = connection.getHeaderField(HEADER_OPSI_SERVER_ROLE);
			Logging.debug(this, "preflight HEAD response code=", code, ", role=", role, ", endpoint=",
					safeEndpoint(serviceURL), ", timeoutMs=", timeout);

			return ROLE_CONFIGSERVER.equals(role);
		} finally {
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (RuntimeException e) {
					// Best-effort cleanup: failures during disconnect must not mask the original error
					// (from connect/getResponseCode). HttpsURLConnection is not AutoCloseable and the
					// underlying resources will be reclaimed by the runtime. Log at debug for diagnostics.
					Logging.debug(this, "Ignoring exception during disconnect of preflight HEAD connection: ", e);
				}
			}
		}
	}

	private static String safeEndpoint(URL url) {
		if (url == null) {
			return "<null>";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(url.getProtocol()).append("://").append(url.getHost());
		int port = url.getPort();
		if (port > 0) {
			sb.append(':').append(port);
		}
		return sb.toString();
	}

	private void reportSSLException(CertificateValidator certValidator) {
		if (reporter.getConnectionState().getState() != ConnectionState.INTERRUPTED && notifyUserOfErrors) {
			reporter.notify(produceCertificateWarningMessage(certValidator),
					ConnectionErrorType.FAILED_CERTIFICATE_VALIDATION_ERROR);
		}
	}

	private void reportIOException(IOException ex) {
		if (reporter.getConnectionState().getState() == ConnectionState.INTERRUPTED) {
			conStat = reporter.getConnectionState();
		} else {
			ParallelTaskExecutor.cancelAllExecutorsTasks();
			conStat = new ConnectionState(ConnectionState.NOT_CONNECTED, ex.toString());
			if (notifyUserOfErrors) {
				reporter.notify(ConfigedMain.getMainFrame() == null
						? new MessageFormat(Configed.getResourceValue("LoginDialog.noConnectionMessageDialog.content"))
								.format(new Object[] { PersistenceControllerFactory.getConnectionState().getMessage() })
						: Configed.getResourceValue("ConnectionHandler.noConnection"),
						ConnectionErrorType.GENERAL_ERROR);
			}
			Logging.warning(ex, "Exception on connecting");
		}
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
