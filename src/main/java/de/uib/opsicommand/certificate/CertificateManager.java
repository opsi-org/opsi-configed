/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.opsicommand.certificate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public final class CertificateManager {
	private static KeyStore ks;
	private static Set<String> invalidCertificates = new HashSet<>();

	private CertificateManager() {
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
			Logging.warning(e, "unable to parse certificate (format is inavlid): ", certificateFile.getAbsolutePath());
			removeCertificateFromKeyStore(certificateFile);
			invalidCertificates.add(certificateFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			Logging.warning(e, "unable to find certificate: ", certificateFile.getAbsolutePath());
		} catch (IOException e) {
			Logging.warning(e, "unable to close certificate: ", certificateFile.getAbsolutePath());
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
			Logging.warning(e, "unable to remove certificate ", certificateFile.getAbsolutePath(),
					" from the keystore: ");
		}
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
		List<File> certificates = CertificateManager.getCertificates();

		certificates.forEach(CertificateManager::loadCertificateToKeyStore);
	}

	public static void loadCertificateToKeyStore(File certificateFile) {
		try {
			X509Certificate certificate = CertificateManager.instantiateCertificate(certificateFile);
			String alias = certificateFile.getParentFile().getName();
			ks.setCertificateEntry(alias, certificate);
		} catch (KeyStoreException e) {
			Logging.error(e, "unable to load certificate into a keystore");
		}
	}

	public static List<File> getCertificates() {
		if (Configed.getSavedStatesLocationName() == null) {
			return new ArrayList<>();
		}

		final PathMatcher matcher = FileSystems.getDefault()
				.getPathMatcher("glob:**." + Globals.CERTIFICATE_FILE_EXTENSION);
		final List<File> certificateFiles = new ArrayList<>();

		try {
			Files.walkFileTree(Paths.get(Configed.getSavedStatesLocationName()), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (matcher.matches(file)) {
						certificateFiles.add(file.toFile());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			Logging.warning(ex, "error on getting certificate");
		}

		return certificateFiles;
	}

	public static void saveCertificate(File certificateFile) {
		try {
			String dirname = Configed.getHost();

			if (dirname.contains(":")) {
				dirname = dirname.replace(":", "_");
			}

			File dirFile = new File(Configed.getSavedStatesLocationName(), dirname);

			if (!dirFile.exists()) {
				dirFile.mkdir();
			}

			Files.copy(certificateFile.toPath(),
					new File(Configed.getSavedStatesLocationName(), dirname + File.separator + Globals.CERTIFICATE_FILE)
							.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Logging.error(e, "unable to save certificate");
		}
	}

	public static void updateCertificate() {
		List<File> certificateFiles = getCertificates();

		if (!certificateFiles.isEmpty()) {
			String certificateContent = PersistenceControllerFactory.getPersistenceController().getUserDataService()
					.getOpsiCACert();
			X509Certificate tmpCertificate = createTmpCertificate(certificateContent);

			for (File certificateFile : certificateFiles) {
				X509Certificate localCertificate = instantiateCertificate(certificateFile);
				if (localCertificate != null && localCertificate.equals(tmpCertificate)) {
					writeToCertificate(certificateFile, certificateContent);
				}
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
			Logging.warning(e, "error on getting certificateFile");
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
			Logging.error(e, "unable to write to certificate: ", certificateFile.getAbsolutePath());
		}
	}
}
