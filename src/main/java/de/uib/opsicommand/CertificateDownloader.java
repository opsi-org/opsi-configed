/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.net.ssl.HttpsURLConnection;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

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
		}

		CertificateValidator validator = CertificateValidatorFactory.createInsecure();
		HttpsURLConnection.setDefaultSSLSocketFactory(validator.createSSLSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(validator.createHostnameVerifier());

		URL url = null;

		try {
			url = new URL(urlPath);
		} catch (MalformedURLException e) {
			Logging.error("url is malformed: " + url);
		}

		if (url == null) {
			return;
		}

		File tmpCertFile = null;

		try {
			tmpCertFile = File.createTempFile(Globals.CERTIFICATE_FILE_NAME, "." + Globals.CERTIFICATE_FILE_EXTENSION);
		} catch (IOException e) {
			Logging.error("unable to create tmp certificate file", e);
		}

		try (ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream fos = new FileOutputStream(tmpCertFile)) {
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		} catch (IOException e) {
			ConnectionErrorObserver.getInstance().notify(
					Configed.getResourceValue("JSONthroughHTTP.unableToDownloadCertificate") + " " + url,
					ConnectionErrorType.FAILED_CERTIFICATE_DOWNLOAD_ERROR);
			Logging.error("unable to download certificate from specified url: " + url.toString(), e);
		}

		downloadedCertificateFile = tmpCertFile;
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
