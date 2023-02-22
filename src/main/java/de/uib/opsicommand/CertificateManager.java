package de.uib.opsicommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
import java.util.List;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public final class CertificateManager {
	private static KeyStore ks;
	private static List<String> invalidCertificates = new ArrayList<>();

	private CertificateManager() {
	}

	public static X509Certificate instantiateCertificate(File certificateFile) {
		X509Certificate cert = null;

		if (invalidCertificates.contains(certificateFile.getAbsolutePath())) {
			return null;
		}

		try (FileInputStream is = new FileInputStream(certificateFile)) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) certFactory.generateCertificate(is);
		} catch (CertificateException e) {
			Logging.warning("unable to parse certificate (format is inavlid): " + certificateFile.getAbsolutePath());
			removeCertificateFromKeyStore(certificateFile);
			invalidCertificates.add(certificateFile.getAbsolutePath());
		} catch (FileNotFoundException e) {
			Logging.warning("unable to find certificate: " + certificateFile.getAbsolutePath());
		} catch (IOException e) {
			Logging.warning("unable to close certificate: " + certificateFile.getAbsolutePath());
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
			Logging.warning("unable to remove certificate " + certificateFile.getAbsolutePath() + " from the keystore: "
					+ e.toString());
		}
	}

	public static KeyStore initializeKeyStore() {
		if (ks == null) {
			try {
				ks = KeyStore.getInstance(KeyStore.getDefaultType());
				ks.load(null, null);
			} catch (KeyStoreException e) {
				Logging.warning("keystore wasn't initialized: " + e.toString());
			} catch (NoSuchAlgorithmException e) {
				Logging.warning("used unsupported algorithm, when initializing key store: " + e.toString());
			} catch (CertificateException e) {
				Logging.warning("faulty certificate (should not happen, since no certificate is provided)");
			} catch (IOException e) {
				Logging.warning("unable to initialize keystore: " + e.toString());
			}
		}

		return ks;
	}

	public static void loadCertificatesToKeyStore() {
		List<File> certificates = CertificateManager.getCertificates();

		if (!certificates.isEmpty()) {
			certificates.forEach(CertificateManager::loadCertificateToKeyStore);
		}
	}

	public static void loadCertificateToKeyStore(File certificateFile) {
		try {
			X509Certificate certificate = CertificateManager.instantiateCertificate(certificateFile);
			String alias = certificateFile.getParentFile().getName();
			ks.setCertificateEntry(alias, certificate);
		} catch (KeyStoreException e) {
			Logging.error("unable to load certificate into a keystore");
		}
	}

	public static List<File> getCertificates() {
		final PathMatcher matcher = FileSystems.getDefault()
				.getPathMatcher("glob:**." + Globals.CERTIFICATE_FILE_EXTENSION);
		final List<File> certificateFiles = new ArrayList<>();

		try {
			Files.walkFileTree(Paths.get(Configed.savedStatesLocationName), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (matcher.matches(file)) {
						certificateFiles.add(file.toFile());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return certificateFiles;
	}

	public static void saveCertificate(File certificateFile) {
		try {
			String dirname = ConfigedMain.host;

			if (dirname.contains(":")) {
				dirname = dirname.replace(":", "_");
			}

			File dirFile = new File(Configed.savedStatesLocationName, dirname);

			if (!dirFile.exists()) {
				dirFile.mkdir();
			}

			Files.copy(certificateFile.toPath(),
					new File(Configed.savedStatesLocationName, dirname + File.separator + Globals.CERTIFICATE_FILE)
							.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			Logging.error("unable to save certificate", e);
		}
	}

	public static void updateCertificate() {
		List<File> certificateFiles = getCertificates();

		if (!certificateFiles.isEmpty()) {
			String certificateContent = PersistenceControllerFactory.getPersistenceController().getOpsiCACert();
			X509Certificate tmpCertificate = retrieveCertificate();

			for (File certificateFile : certificateFiles) {
				X509Certificate localCertificate = instantiateCertificate(certificateFile);

				if (localCertificate != null && localCertificate.equals(tmpCertificate)) {
					writeToCertificate(certificateFile, certificateContent);
				}
			}
		}
	}

	private static X509Certificate retrieveCertificate() {
		String certificateContent = PersistenceControllerFactory.getPersistenceController().getOpsiCACert();
		File certificateFile = null;
		try {
			certificateFile = File.createTempFile(Globals.CERTIFICATE_FILE_NAME,
					"." + Globals.CERTIFICATE_FILE_EXTENSION);
			writeToCertificate(certificateFile, certificateContent);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (certificateFile == null) {
			return null;
		}

		return instantiateCertificate(certificateFile);
	}

	private static void writeToCertificate(File certificateFile, String certificateContent) {
		try (FileWriter writer = new FileWriter(certificateFile, false)) {
			writer.write(certificateContent);
			writer.flush();
		} catch (IOException e) {
			Logging.error("unable to write to certificate: " + certificateFile.getAbsolutePath());
		}
	}
}
