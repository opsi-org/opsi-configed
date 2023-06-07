package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.mockserver.model.MediaType;

import de.uib.Utils;
import de.uib.configed.Globals;

@TestMethodOrder(OrderAnnotation.class)
public class CertificateDownloaderTest {
	static ClientAndServer clientServer;

	@BeforeAll
	static void setup() {
		ConnectionErrorObserver.destroy();
		clientServer = ClientAndServer.startClientAndServer(Utils.PORT);
	}

	@AfterAll
	static void close() {
		clientServer.stop();
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
		clientServer.withSecure(true).when(request().withMethod("GET").withPath("/ssl/non-existent-file.pem"))
				.respond(response().withStatusCode(HttpStatusCode.NOT_FOUND_404.code()));

		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/ssl/non-existent-file.pem";
		CertificateDownloader.init(url);
		CertificateDownloader.downloadCertificateFile();
		assertNull(CertificateDownloader.getDownloadedCertificateFile(),
				"Downloading certificate should fail, when incorrect URL is provided: " + url);
	}

	@Test
	@Order(3)
	void testIfCertificateIsDownloadedProvidedCorrectURL() throws Exception {
		byte[] fileBytes = IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("test/opsi-ca-cert.pem"));

		clientServer.withSecure(true).when(request().withMethod("GET").withPath("/ssl/" + Globals.CERTIFICATE_FILE))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code())
						.withHeaders(header("Content-Type", MediaType.TEXT_PLAIN.toString()),
								header("Content-Disposition",
										"form-data; name=\"opsi-ca-cert.pem\"; filename=\"opsi-ca-cert.pem\""))
						.withBody(binary(fileBytes)));

		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/ssl/" + Globals.CERTIFICATE_FILE;
		CertificateDownloader.init(url);
		CertificateDownloader.downloadCertificateFile();
		assertNotNull(CertificateDownloader.getDownloadedCertificateFile(),
				"Downloading certificate should NOT fail, when correct URL is provided: " + url);
	}
}
