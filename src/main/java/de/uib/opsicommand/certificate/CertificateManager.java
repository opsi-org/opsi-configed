/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import com.formdev.flatlaf.util.SystemInfo;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsicommand.ConnectionErrorReporter;
import de.uib.opsicommand.ConnectionErrorType;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public final class CertificateManager {
	private static File downloadedCertificateFile;
	private static String urlPath;
	private static String caFolderName;

	private static KeyStore ks;
	private static Set<String> invalidCertificates = new HashSet<>();

	private CertificateManager() {
	}

	/**
	 * Initializes URL path to use for downloaded certificate file.
	 * 
	 * @param urlPath from which to download certificate.
	 */
	public static void init(String urlPath, String caFolderName) {
		CertificateManager.urlPath = urlPath;
		CertificateManager.caFolderName = caFolderName;
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
			Logging.error("url is malformed: " + url, e);
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
			Logging.error("unable to create tmp certificate file", e);
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
			Logging.error("unable to download certificate from specified url: " + url.toString(), e);
		}

		if (tmpCertFile != null && tmpCertFile.length() != 0) {
			downloadedCertificateFile = tmpCertFile;
		}
	}

	private static String getPathToCACerts() {
		if (SystemInfo.isWindows) {
			return System.getenv(Logging.WINDOWS_ENV_VARIABLE_APPDATA_DIRECTORY) + "/opsi/services/" + caFolderName;
		} else {
			return System.getProperty(Logging.ENV_VARIABLE_FOR_USER_DIRECTORY) + "/.config/opsi/services/"
					+ caFolderName;
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

	public static X509Certificate instantiateCertificate(File certificateFile) {
		if (invalidCertificates.contains(certificateFile.getAbsolutePath())) {
			return null;
		}

		X509Certificate cert = null;

		try (FileInputStream is = new FileInputStream(certificateFile)) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) certFactory.generateCertificate(is);
		} catch (CertificateException e) {
			Logging.warning("unable to parse certificate (format is inavlid): " + certificateFile.getAbsolutePath(), e);
			removeCertificateFromKeyStore(certificateFile);
			invalidCertificates.add(certificateFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			Logging.warning("unable to find certificate: " + certificateFile.getAbsolutePath(), e);
		} catch (IOException e) {
			Logging.warning("unable to close certificate: " + certificateFile.getAbsolutePath(), e);
		}

		return cert;
	}

	private static void removeCertificateFromKeyStore(File certificateFile) {
		try {
			if (ks.isCertificateEntry(certificateFile.getParentFile().getName())) {
				Logging.info("removing certificate from keystore, since it is invalid certificate: "
						+ certificateFile.getAbsolutePath());
				ks.deleteEntry(certificateFile.getParentFile().getName());
			}
		} catch (KeyStoreException e) {
			Logging.warning(
					"unable to remove certificate " + certificateFile.getAbsolutePath() + " from the keystore: ", e);
		}
	}

	public static KeyStore initializeKeyStore() {
		if (ks == null) {
			try {
				ks = KeyStore.getInstance(KeyStore.getDefaultType());
				ks.load(null, null);
			} catch (KeyStoreException e) {
				Logging.warning("keystore wasn't initialized: ", e);
			} catch (NoSuchAlgorithmException e) {
				Logging.warning("used unsupported algorithm, when initializing key store: ", e);
			} catch (CertificateException e) {
				Logging.warning("faulty certificate (should not happen, since no certificate is provided)", e);
			} catch (IOException e) {
				Logging.warning("unable to initialize keystore: ", e);
			}
		}

		return ks;
	}

	public static void loadCertificatesToKeyStore() {
		File certificateFile = CertificateManager.getCertificates();

		if (certificateFile != null) {
			loadCertificateToKeyStore(certificateFile);
		}
	}

	public static void loadCertificateToKeyStore(File certificateFile) {
		try {
			X509Certificate certificate = CertificateManager.instantiateCertificate(certificateFile);
			String alias = certificateFile.getParentFile().getName();
			ks.setCertificateEntry(alias, certificate);
		} catch (KeyStoreException e) {
			Logging.error("unable to load certificate into a keystore", e);
		}
	}

	public static File getCertificates() {
		File file = new File(getPathToCACerts(), Globals.CERTIFICATE_FILE);

		if (file.exists()) {
			return file;
		} else {
			return null;
		}
	}

	public static void saveCertificate() {
		try {
			File dirFile = new File(getPathToCACerts());

			if (!dirFile.exists()) {
				dirFile.mkdir();
			}

			Files.copy(downloadedCertificateFile.toPath(),
					new File(getPathToCACerts() + File.separator + Globals.CERTIFICATE_FILE).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Logging.error("unable to save certificate", e);
		}
	}

	public static void updateCertificate() {
		File certificateFile = getCertificates();

		if (certificateFile != null) {
			String certificateContent = PersistenceControllerFactory.getPersistenceController().getUserDataService()
					.getOpsiCACert();
			X509Certificate tmpCertificate = createTmpCertificate(certificateContent);

			X509Certificate localCertificate = instantiateCertificate(certificateFile);
			if (localCertificate != null && localCertificate.equals(tmpCertificate)) {
				writeToCertificate(certificateFile, certificateContent);
			}
		}
	}

	private static X509Certificate createTmpCertificate(String certificateContent) {
		File certificateFile = null;
		try {
			certificateFile = Files
					.createTempFile(Globals.CERTIFICATE_FILE_NAME, "." + Globals.CERTIFICATE_FILE_EXTENSION).toFile();
			Utils.restrictAccessToFile(certificateFile);
			writeToCertificate(certificateFile, certificateContent);
		} catch (IOException e) {
			Logging.warning("error on getting certificateFile", e);
		}

		if (certificateFile == null) {
			return null;
		}

		return instantiateCertificate(certificateFile);
	}

	private static void writeToCertificate(File certificateFile, String certificateContent) {
		try (FileWriter writer = new FileWriter(certificateFile, StandardCharsets.UTF_8, false)) {
			writer.write(certificateContent);
			writer.flush();
		} catch (IOException e) {
			Logging.error("unable to write to certificate: " + certificateFile.getAbsolutePath(), e);
		}
	}
}
