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

public final class CertificateDownloader {
	private static File downloadedCertificateFile;

	private CertificateDownloader() {
	}

	public static void downloadCertificateFile(String urlPath) {
		CertificateValidator validator = CertificateValidatorFactory.create(false);
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

	public static File getDownloadedCertificateFile() {
		return downloadedCertificateFile;
	}
}
