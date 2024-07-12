/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.maven.artifact.versioning.ComparableVersion;

import de.uib.opsicommand.certificate.CertificateValidator;
import de.uib.opsicommand.certificate.CertificateValidatorFactory;
import de.uib.opsicommand.certificate.InsecureCertificateValidator;
import de.uib.utils.logging.Logging;

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

	private static String serverVersionString = "Server version not found (assume recent version)";
	private static ComparableVersion serverComparableVersion = new ComparableVersion(serverVersionString);

	private String serviceURL;
	private String username;
	private String password;

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
		if (compareVersion == null) {
			return false;
		}
		return serverComparableVersion.compareTo(new ComparableVersion(compareVersion)) >= 0;
	}

	public synchronized String getServerVersion() {
		return serverVersionString;
	}

	/**
	 * Checks if the server version is already known.
	 */
	@SuppressWarnings("java:S2647")
	public synchronized void checkServerVersion() {
		HttpsURLConnection connection;

		try {
			connection = (HttpsURLConnection) new URI(serviceURL).toURL().openConnection();
			String authorization = Base64.getEncoder()
					.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
			connection.setRequestProperty("Authorization", "Basic " + authorization);

			CertificateValidator certValidator = CertificateValidatorFactory.createInsecure();
			connection.setSSLSocketFactory(certValidator.createSSLSocketFactory());
			connection.setHostnameVerifier(certValidator.createHostnameVerifier());
			connection.setRequestMethod("HEAD");
		} catch (URISyntaxException e) {
			Logging.warning(this, "cannot create URI from " + serviceURL, e);
			return;
		} catch (IOException e) {
			Logging.warning(this, "error in testing connection to server for getting server opsi version", e);
			return;
		}

		String server = connection.getHeaderField("Server");

		if (server == null) {
			Logging.error("error in getting server version, Headerfield is null");
			serverVersionString = "Server version not found (assume 4.1)";
			return;
		}

		int[] newServerVersion = new int[EXPECTED_SERVER_VERSION_LENGTH];

		Matcher matcher = versionPattern.matcher(server);
		if (matcher.find()) {
			Logging.info(this, "opsi server version: " + matcher.group(1));
			String[] versionParts = matcher.group(1).split("\\.");
			for (int i = 0; i < versionParts.length && i < EXPECTED_SERVER_VERSION_LENGTH; i++) {
				try {
					newServerVersion[i] = Integer.parseInt(versionParts[i]);
				} catch (NumberFormatException nex) {
					Logging.error(this, "value is unparsable to int");
				}
			}
		} else {
			// Default is 4.3, if this query does not work
			Logging.info("we set opsi version 4.3 because we did not find opsiconfd version in header");
			newServerVersion[0] = 4;
			newServerVersion[1] = 3;
		}

		setServerVersion(newServerVersion);
	}

	private static synchronized void setServerVersion(int[] serverVersion) {
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

		Logging.info("we set the server version: " + serverVersionString);
	}
}
