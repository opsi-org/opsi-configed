/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.certificate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

import javax.net.ssl.HttpsURLConnection;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsicommand.ConnectionErrorReporter;
import de.uib.opsicommand.ConnectionErrorType;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

/**
 * {@code CertificateDownloader} downloads certificate file from the specified
 * server.
 */
public final class CertificateDownloader {
	private static File downloadedCertificateFile;
	private static String urlPath;

	private CertificateDownloader() {
	}

	/**
	 * Initializes URL path to use for downloaded certificate file.
	 * 
	 * @param newUrlPath from which to download certificate.
	 */
	public static void init(String newUrlPath) {
		urlPath = newUrlPath;
	}

	/**
	 * Downloades certificate from the specified URL path (in the
	 * {@link #init(String)} method).
	 */
	public static void downloadCertificateFile() {
		if (urlPath == null) {
			Logging.error("CertificateDownloader wasn't initialized");
			return;
		}

		CertificateValidator validator = CertificateValidatorFactory.createInsecure();
		HttpsURLConnection.setDefaultSSLSocketFactory(validator.createSSLSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(validator.createHostnameVerifier());

		URL url = null;

		try {
			url = new URI(urlPath).toURL();
		} catch (URISyntaxException | MalformedURLException e) {
			Logging.error(e, "url is malformed: ", url);
		}

		if (url == null) {
			return;
		}

		File tmpCertFile = null;

		try {
			tmpCertFile = Files.createTempFile(Globals.CERTIFICATE_FILE_NAME, "." + Globals.CERTIFICATE_FILE_EXTENSION)
					.toFile();
			Utils.restrictAccessToFile(tmpCertFile);
		} catch (IOException e) {
			Logging.error(e, "unable to create tmp certificate file");
		}

		try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream fos = new FileOutputStream(tmpCertFile)) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			if (ConnectionErrorReporter.getInstance() != null) {
				ConnectionErrorReporter.getInstance().notify(
						Configed.getResourceValue("CertificateDownloader.unableToDownloadCertificate") + " " + url,
						ConnectionErrorType.FAILED_CERTIFICATE_DOWNLOAD_ERROR);
			}
			Logging.error(e, "unable to download certificate from specified url: ", url);
		}

		if (tmpCertFile != null && tmpCertFile.length() != 0) {
			downloadedCertificateFile = tmpCertFile;
		}
	}

	/**
	 * Retrieves downloaded certificate.
	 * 
	 * @return downloaded certificate.
	 */
	public static File getDownloadedCertificateFile() {
		return downloadedCertificateFile;
	}
}
