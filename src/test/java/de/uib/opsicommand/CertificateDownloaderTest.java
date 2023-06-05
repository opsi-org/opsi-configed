package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import de.uib.Utils;
import de.uib.configed.Globals;

public class CertificateDownloaderTest {
	@Test
	void testIfCertificateIsDownloadedProvidedCorrectURL() {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/ssl/" + Globals.CERTIFICATE_FILE;
		CertificateDownloader.init(url);
		CertificateDownloader.downloadCertificateFile();
		assertNotNull(CertificateDownloader.getDownloadedCertificateFile(),
				"Downloading certificate should NOT fail, when correct URL is provided: " + url);
	}
}
