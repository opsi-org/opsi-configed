package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import de.uib.Utils;
import de.uib.configed.Globals;

@TestMethodOrder(OrderAnnotation.class)
public class CertificateDownloaderTest {
	@BeforeAll
	static void setup() {
		ConnectionErrorObserver.destroy();
	}

	@Test
	@Order(1)
	void testDownloadCertificateFileWithNullParameter() {
		CertificateDownloader.init(null);
		CertificateDownloader.downloadCertificateFile();
		assertNull(CertificateDownloader.getDownloadedCertificateFile(), "Should be null");
	}

	@Test
	@Order(2)
	void testIfCertificateIsDownloadedProvidedIncorrectURL() {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/ssl/incorrect";
		CertificateDownloader.init(url);
		CertificateDownloader.downloadCertificateFile();
		assertNotNull(CertificateDownloader.getDownloadedCertificateFile(),
				"Downloading certificate should NOT fail, when correct URL is provided: " + url);
	}

	@Test
	@Order(3)
	void testIfCertificateIsDownloadedProvidedCorrectURL() {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/ssl/" + Globals.CERTIFICATE_FILE;
		CertificateDownloader.init(url);
		CertificateDownloader.downloadCertificateFile();
		assertNotNull(CertificateDownloader.getDownloadedCertificateFile(),
				"Downloading certificate should NOT fail, when correct URL is provided: " + url);
	}
}
