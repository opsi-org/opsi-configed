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

public class ConnectionHandler {
	private URL serviceUrl;
	private Map<String, String> requestProperties;
	private ConnectionState conStat;
	private ConnectionErrorObserver observer;
	private ConnectionErrorReporter reporter;
	private String requestMethod = "POST";

	public ConnectionHandler(URL serviceUrl, Map<String, String> requestProperties, ConnectionState conStat) {
		this.serviceUrl = serviceUrl;
		this.requestProperties = new HashMap<>(requestProperties);
		this.conStat = conStat;
		this.observer = ConnectionErrorObserver.getInstance();
		this.reporter = new ConnectionErrorReporter(conStat);
		observer.subscribe(reporter);
	}

	public ConnectionState getConnectionState() {
		return conStat;
	}

	public void setRequestMethod(String requestMethod) {
		this.requestMethod = requestMethod;
	}

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

			conStat = reporter.getConStat();
			return null;
		} catch (IOException ex) {
			conStat = new ConnectionState(ConnectionState.ERROR, ex.toString());
			Logging.error("Exception on connecting, ", ex);
			return null;
		}

		return connection;
	}
}
