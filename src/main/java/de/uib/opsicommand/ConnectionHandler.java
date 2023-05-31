/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.io.IOException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class ConnectionHandler {
	private ConnectionState conStat;
	private ConnectionErrorObserver observer;
	private ConnectionErrorReporter reporter;

	public ConnectionHandler(ConnectionState conStat) {
		this.conStat = conStat;
		this.observer = ConnectionErrorObserver.getInstance();
		this.reporter = new ConnectionErrorReporter(conStat);
		observer.subscribe(reporter);
	}

	public ConnectionState getConnectionState() {
		return conStat;
	}

	public boolean establishConnection(HttpsURLConnection connection) {
		CertificateValidator certValidator = !Globals.disableCertificateVerification
				? CertificateValidatorFactory.createSecure()
				: CertificateValidatorFactory.createInsecure();

		try {
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
			return false;
		} catch (IOException ex) {
			conStat = new ConnectionState(ConnectionState.ERROR, ex.toString());
			Logging.error("Exception on connecting, ", ex);
			return false;
		}

		return true;
	}
}
