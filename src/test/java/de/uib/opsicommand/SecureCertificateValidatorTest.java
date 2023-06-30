package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpStatusCode;

import de.uib.Utils;

@TestClassOrder(OrderAnnotation.class)
@Order(1)
public class SecureCertificateValidatorTest {
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
	void testSecureConnectionWithValidCertificate() throws IOException {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("GET").withPath("/")
						.withHeaders(header("Authorization", "Basic " + authorization)), Times.exactly(1))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()));

		URL serviceURL = new URL("https://" + Utils.HOST + ":" + Utils.PORT + "/");
		HttpsURLConnection connection = (HttpsURLConnection) serviceURL.openConnection();
		connection.setDoOutput(false);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Authorization", "Basic " + authorization);

		SecureCertificateValidator certValidator = new SecureCertificateValidator();
		CertificateManager.loadCertificateToKeyStore(
				new File(getClass().getClassLoader().getResource("test/opsi-ca-cert.pem").getFile()));
		connection.setSSLSocketFactory(certValidator.createSSLSocketFactory());
		connection.setHostnameVerifier(certValidator.createHostnameVerifier());
		connection.connect();

		assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode(), "should return HTTP status code OK 200");
	}

	@Test
	void testSecureConnectionWithInvalidCertificate() throws IOException {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("GET").withPath("/")
						.withHeaders(header("Authorization", "Basic " + authorization)), Times.exactly(1))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()));

		URL serviceURL = new URL("https://" + Utils.HOST + ":" + Utils.PORT + "/");
		HttpsURLConnection connection = (HttpsURLConnection) serviceURL.openConnection();
		connection.setDoOutput(false);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Authorization", "Basic " + authorization);

		SecureCertificateValidator certValidator = new SecureCertificateValidator();
		CertificateManager.loadCertificateToKeyStore(
				new File(getClass().getClassLoader().getResource("test/invalid-opsi-ca-cert.pem").getFile()));
		connection.setSSLSocketFactory(certValidator.createSSLSocketFactory());
		connection.setHostnameVerifier(certValidator.createHostnameVerifier());
		assertThrows(SSLException.class, () -> connection.connect(), "should throw SSLException");
	}
}
