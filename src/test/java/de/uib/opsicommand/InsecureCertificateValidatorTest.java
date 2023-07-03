package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;

import de.uib.Utils;

public class InsecureCertificateValidatorTest {
	static ClientAndServer clientServer;

	@BeforeAll
	static void setup() {
		clientServer = ClientAndServer.startClientAndServer(Utils.PORT);
	}

	@AfterAll
	static void close() {
		clientServer.stop();
	}

	@Test
	void testInsecureConnection() throws IOException {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("GET").withPath("/")
						.withHeaders(header("Authorization", "Basic " + authorization)))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()));

		URL serviceURL = new URL("https://" + Utils.HOST + ":" + Utils.PORT + "/");
		HttpsURLConnection connection = (HttpsURLConnection) serviceURL.openConnection();
		connection.setDoOutput(false);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Authorization", "Basic " + authorization);

		InsecureCertificateValidator certValidator = new InsecureCertificateValidator();
		connection.setSSLSocketFactory(certValidator.createSSLSocketFactory());
		connection.setHostnameVerifier(certValidator.createHostnameVerifier());
		connection.connect();

		assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode(), "should be 200");
	}
}
