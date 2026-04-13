/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.maven.artifact.versioning.ComparableVersion;

import de.uib.configed.core.infrastructure.certificate.CertificateValidator;
import de.uib.configed.core.infrastructure.certificate.CertificateValidatorFactory;
import de.uib.configed.core.infrastructure.certificate.InsecureCertificateValidator;
import de.uib.configed.gui.Configed;
import de.uib.configed.share.logging.Logging;

/**
 * {@code OpsiServerVersionRetriever} retrieves version of the opsi server.
 * <p>
 * It sends a {@code HEAD} request method to the specified URL to retrieve
 * headers. The {@code Server} header is then used to retrieve the server
 * version. For establishing connection with the server, it uses
 * {@link InsecureCertificateValidator}, to avoid dealing with certificate
 * verificaiton.
 */
public class OpsiServerVersionRetriever {
	private static final Pattern versionPattern = Pattern.compile("opsiconfd ([\\d\\.]+)");
	private static final int EXPECTED_SERVER_VERSION_LENGTH = 4;

	private String serverVersionString = "Server version not found (assume recent version)";
	private ComparableVersion serverComparableVersion = new ComparableVersion(serverVersionString);

	private String serviceURL;
	private String sessionId;
	private String username;
	private String password;

	public OpsiServerVersionRetriever(String serviceURL, String sessionId) {
		if (serviceURL == null || sessionId == null) {
			throw new IllegalArgumentException("Provided parameters are null");
		}

		this.serviceURL = serviceURL;
		this.sessionId = sessionId;
		this.username = null;
		this.password = null;
	}

	public OpsiServerVersionRetriever(String serviceURL, String username, String password) {
		if (serviceURL == null || username == null || password == null) {
			throw new IllegalArgumentException("Provided parameters are null");
		}

		this.serviceURL = serviceURL;
		this.username = username;
		this.password = password;
	}

	/**
	 * returns true, if the server has a newer version (or same version)
	 * compared to the version in the argument
	 *
	 * @param compareVersion version to compare to of format x.y.z...
	 */
	public boolean isServerVersionAtLeast(String compareVersion) {
		return compareVersion != null && serverComparableVersion.compareTo(new ComparableVersion(compareVersion)) >= 0;
	}

	public String getServerVersion() {
		return serverVersionString;
	}

	/**
	 * Checks if the server version is already known.
	 */
	public void checkServerVersion() {
		String server = retrieveServerHeader();
		if (server == null) {
			Logging.warning("error in getting server version, Headerfield is null");
			setServerVersionNotFound();
			return;
		}

		int[] newServerVersion = new int[EXPECTED_SERVER_VERSION_LENGTH];

		Matcher matcher = versionPattern.matcher(server);
		if (matcher.find()) {
			Logging.info(this, "opsi server version: ", matcher.group(1));
			String[] versionParts = matcher.group(1).split("\\.");
			for (int i = 0; i < versionParts.length && i < EXPECTED_SERVER_VERSION_LENGTH; i++) {
				try {
					newServerVersion[i] = Integer.parseInt(versionParts[i]);
				} catch (NumberFormatException nex) {
					Logging.error(this, "value is unparsable to int");
				}
			}
			setServerVersion(newServerVersion);
			Logging.notice(this, "opsi server version: ", serverVersionString);
		} else {
			// Default is 4.3, if this query does not work
			Logging.error("we set opsi version 4.3 because we did not find opsiconfd version in header");
			setServerVersionNotFound();
		}
		Logging.info("server version: ", serverVersionString, serverComparableVersion);
	}

	private String retrieveServerHeader() {
		HttpsURLConnection connection = openConnection();
		return connection != null ? connection.getHeaderField("Server") : null;
	}

	private HttpsURLConnection openConnection() {
		HttpsURLConnection connection = null;
		String authorization = null;
		try {
			connection = (HttpsURLConnection) new URI(serviceURL).toURL().openConnection();
			Logging.secret("Session id for connection ", sessionId);
			if (sessionId != null) {
				authorization = sessionId;
				connection.setRequestProperty("Cookie", authorization);
				if (sessionId.contains("=")) {
					authorization = sessionId.split("=")[1];
					connection.setRequestProperty("Cookie", "session-id=" + authorization);
					connection.setRequestProperty("Cookie", "sessionId=" + authorization);
					Logging.info(this, "Using existing session id for connection");
					Logging.info(this, "Connection:", connection.getRequestProperties());
				}
			} else if (username != null && password != null) {
				authorization = Base64.getEncoder()
						.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
				connection.setRequestProperty("Authorization", "Basic " + authorization);
			} else {
				Logging.error("No session id or username/password provided");
				return null;
			}

			Logging.info("Fetching service info for", serviceURL);

			CertificateValidator certValidator = CertificateValidatorFactory.getInsecure();
			connection.setSSLSocketFactory(certValidator.getSSLSocketFactory());
			connection.setHostnameVerifier(certValidator.getHostnameVerifier());
			connection.setRequestMethod("HEAD");
		} catch (URISyntaxException e) {
			Logging.warning(this, e, "cannot create URI from ", serviceURL);
		} catch (IOException e) {
			Logging.warning(this, e, "error in testing connection to server for getting server opsi version");
		}
		return connection;
	}

	private void setServerVersionNotFound() {
		setServerVersion(new int[] { 4, 3, 0, 0 });
		serverVersionString = Configed.getResourceValue("ServerFacade.serverVersionString.notFound");
	}

	private void setServerVersion(int[] serverVersion) {
		if (serverVersion == null || serverVersion.length == 0) {
			return;
		}

		StringBuilder serverVersionBuilder = new StringBuilder(String.valueOf(serverVersion[0]));

		for (int i = 1; i < serverVersion.length; i++) {
			serverVersionBuilder.append(".");
			serverVersionBuilder.append(String.valueOf(serverVersion[i]));
		}

		serverVersionString = serverVersionBuilder.toString();
		serverComparableVersion = new ComparableVersion(serverVersionString);

		Logging.info("we set the server version: ", serverVersionString);
	}
}
