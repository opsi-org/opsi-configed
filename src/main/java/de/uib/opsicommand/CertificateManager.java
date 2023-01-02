package de.uib.opsicommand;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;

public class CertificateManager {
	private CertificateManager() {
	}

	public static X509Certificate instantiateCertificate(File certificateFile) {
		X509Certificate cert = null;

		try (FileInputStream is = new FileInputStream(certificateFile)) {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			cert = (X509Certificate) certFactory.generateCertificate(is);
		} catch (CertificateException e) {
			logging.error("unable to parse certificate (format is invalid)");
		} catch (FileNotFoundException e) {
			logging.error("unable to find certificate");
		} catch (IOException e) {
			logging.error("unable to close certificate");
		}

		return cert;
	}

	public static List<File> getCertificates() {
		File certificateDir = new File(configed.savedStatesLocationName);
		File[] certificateFiles = certificateDir.listFiles((dir, filename) -> filename.endsWith(".pem"));

		if (certificateFiles.length == 0) {
			return new ArrayList<>();
		}

		return Arrays.asList(certificateFiles);
	}

	public static void saveCertificate(File certificateFile) {
		try {
			Files.copy(certificateFile.toPath(),
					new File(configed.savedStatesLocationName, ConfigedMain.HOST + "-" + Globals.CERTIFICATE_FILE)
							.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logging.error("unable to save certificate");
		}
	}

	private static X509Certificate retrieveCertificate() {
		String certificateContent = PersistenceControllerFactory.getPersistenceController().getOpsiCACert();
		File certificateFile = null;
		try {
			certificateFile = File.createTempFile("opsi-ca-cert", ".pem");
			FileWriter writer = new FileWriter(certificateFile);
			writer.append(certificateContent);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return instantiateCertificate(certificateFile);
	}

	public static void updateCertificate() {
		File certificateDir = new File(configed.savedStatesLocationName);
		File[] certificateFiles = certificateDir.listFiles((dir, filename) -> filename.endsWith(".pem"));

		if (certificateFiles.length != 0) {
			String certificateContent = PersistenceControllerFactory.getPersistenceController().getOpsiCACert();
			X509Certificate tmpCertificate = retrieveCertificate();

			for (File certificateFile : certificateFiles) {
				X509Certificate localCertificate = instantiateCertificate(certificateFile);

				if (localCertificate != null && localCertificate.equals(tmpCertificate)) {
					try (FileWriter writer = new FileWriter(certificateFile, false)) {
						writer.write(certificateContent);
						writer.flush();
					} catch (IOException e) {
						logging.error("unable to update certificate: " + certificateFile.getAbsolutePath());
					}
				}
			}
		}
	}
}
