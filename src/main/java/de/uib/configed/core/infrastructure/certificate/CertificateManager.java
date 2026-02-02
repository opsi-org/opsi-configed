/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.core.infrastructure.certificate;

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
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;

import javax.net.ssl.HttpsURLConnection;

import com.formdev.flatlaf.util.SystemInfo;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.ConnectionErrorReporter;
import de.uib.configed.core.infrastructure.ConnectionErrorType;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public final class CertificateManager {
	private static File downloadedCertificateFile;
	private static String urlPath;
	private static String caFolderName;

	private static KeyStore ks;

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
		Logging.info("download with urlPath: ", urlPath, " and caFolderName: ", caFolderName);
		if (urlPath == null) {
			Logging.error("CertificateDownloader wasn't initialized");
			return;
		}

		CertificateValidator validator = CertificateValidatorFactory.getInsecure();
		HttpsURLConnection.setDefaultSSLSocketFactory(validator.getSSLSocketFactory());
		HttpsURLConnection.setDefaultHostnameVerifier(validator.getHostnameVerifier());

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

	@SuppressWarnings("java:S1452")
	public static Collection<? extends Certificate> instantiateCertificate(File certificateFile) {
		Collection<? extends Certificate> certificates = new HashSet<>();

		if (certificateFile == null) {
			Logging.info("the certificate file is null so we return an empty Set of certificates");
			return certificates;
		}

		try (FileInputStream is = new FileInputStream(certificateFile)) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			certificates = certFactory.generateCertificates(is);
		} catch (CertificateException e) {
			Logging.warning(e, "unable to parse certificate (format is inavlid): ", certificateFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			Logging.warning(e, "unable to find certificate: ", certificateFile.getAbsolutePath());
		} catch (IOException e) {
			Logging.warning(e, "unable to close certificate: ", certificateFile.getAbsolutePath());
		}

		return certificates;
	}

	public static KeyStore initializeKeyStore() {
		if (ks == null) {
			try {
				ks = KeyStore.getInstance(KeyStore.getDefaultType());
				ks.load(null, null);
			} catch (KeyStoreException e) {
				Logging.warning(e, "keystore wasn't initialized: ");
			} catch (NoSuchAlgorithmException e) {
				Logging.warning(e, "used unsupported algorithm, when initializing key store: ");
			} catch (CertificateException e) {
				Logging.warning(e, "faulty certificate (should not happen, since no certificate is provided)");
			} catch (IOException e) {
				Logging.warning(e, "unable to initialize keystore: ");
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
			Collection<? extends Certificate> certificates = CertificateManager.instantiateCertificate(certificateFile);
			for (Certificate certificate : certificates) {
				ks.setCertificateEntry(((X509Certificate) certificate).getSerialNumber().toString(), certificate);
			}
		} catch (KeyStoreException e) {
			Logging.error(e, "unable to load certificate into a keystore");
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
				dirFile.mkdirs();
			}

			Files.copy(downloadedCertificateFile.toPath(),
					new File(getPathToCACerts() + File.separator + Globals.CERTIFICATE_FILE).toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Logging.error(e, "unable to save certificate");
		}
	}

	public static void updateCertificate() {
		File certificateFile = getCertificates();

		if (certificateFile != null) {
			String certificateContent = PersistenceControllerFactory.getPersistenceController().getDataServices().user
					.getCACerts();

			Collection<? extends Certificate> tmpCertificates = createTmpCertificate(certificateContent);
			Collection<? extends Certificate> localCertificates = instantiateCertificate(certificateFile);

			if (!localCertificates.equals(tmpCertificates)) {
				writeToCertificate(certificateFile, certificateContent);
			}
		}
	}

	private static Collection<? extends Certificate> createTmpCertificate(String certificateContent) {
		File certificateFile = null;
		try {
			certificateFile = Files
					.createTempFile(Globals.CERTIFICATE_FILE_NAME, "." + Globals.CERTIFICATE_FILE_EXTENSION).toFile();
			Utils.restrictAccessToFile(certificateFile);
			writeToCertificate(certificateFile, certificateContent);
		} catch (IOException e) {
			Logging.warning(e, "error on getting certificateFile");
		}

		if (certificateFile == null) {
			return new HashSet<>();
		}

		return instantiateCertificate(certificateFile);
	}

	private static void writeToCertificate(File certificateFile, String certificateContent) {
		try (FileWriter writer = new FileWriter(certificateFile, StandardCharsets.UTF_8, false)) {
			writer.write(certificateContent);
			writer.flush();
		} catch (IOException e) {
			Logging.error(e, "unable to write to certificate: ", certificateFile.getAbsolutePath());
		}
	}
}
