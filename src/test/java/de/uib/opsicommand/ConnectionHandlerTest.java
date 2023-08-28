package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;

import de.uib.Utils;
import de.uib.configed.Globals;

class ConnectionHandlerTest {
	static ClientAndServer clientServer;

	@BeforeAll
	static void setup() {
		utils.Utils.setDisableCertificateVerification(true);
		clientServer = ClientAndServer.startClientAndServer(Utils.PORT);
	}

	@AfterAll
	static void close() {
		clientServer.stop();
	}

	@Test
	void testEstablishConnectionWithValidCredentials() throws Exception {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("POST").withPath("/login").withHeaders(
						header("Authorization", "Basic " + authorization), header("X-opsi-session-lifetime", "900"),
						header("User-Agent", Globals.APPNAME + " " + Globals.VERSION)))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()));

		String url = "https://" + Utils.HOST + ":" + clientServer.getPort() + "/login";
		Map<String, String> requestProperties = new HashMap<>();

		requestProperties.put("Authorization", "Basic " + authorization);
		requestProperties.put("X-opsi-session-lifetime", "900");
		requestProperties.put("User-Agent", Globals.APPNAME + " " + Globals.VERSION);

		ConnectionHandler handler = new ConnectionHandler(new URL(url), requestProperties);
		HttpsURLConnection connection = handler.establishConnection(false);

		assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode(),
				"Connecting with valid credentials should return HTTP response code 200: "
						+ connection.getResponseCode());
	}

	@Test
	void testEstablishConnectionWithIncorrectCredentials() throws Exception {
		String authorization = Base64.getEncoder()
				.encodeToString(("non-existent:user").getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("POST").withPath("/login").withHeaders(
						header("Authorization", "Basic " + authorization), header("X-opsi-session-lifetime", "900"),
						header("User-Agent", Globals.APPNAME + " " + Globals.VERSION)))
				.respond(response().withStatusCode(HttpStatusCode.UNAUTHORIZED_401.code()));

		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/login";
		Map<String, String> requestProperties = new HashMap<>();

		requestProperties.put("Authorization", "Basic " + authorization);
		requestProperties.put("X-opsi-session-lifetime", "900");
		requestProperties.put("User-Agent", Globals.APPNAME + " " + Globals.VERSION);

		ConnectionHandler handler = new ConnectionHandler(new URL(url), requestProperties);
		HttpsURLConnection connection = handler.establishConnection(false);

		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, connection.getResponseCode(),
				"Connecting with invalid credentials should return HTTP response code 401: "
						+ connection.getResponseCode());
	}

	@Test
	void testEstablishConnectionWithNullParameters() {
		ConnectionHandler handler = new ConnectionHandler(null, null);
		HttpsURLConnection connection = handler.establishConnection(false);

		assertNull(connection, "Connection should be NULL");
	}

	@Test
	void testSetRequestMethodWithNull() throws Exception {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT;
		ConnectionHandler handler = new ConnectionHandler(new URL(url), new HashMap<>());
		handler.setRequestMethod(null);

		assertNull(handler.getRequestMethod(), "Request method should be NULL");
	}

	@Test
	void testSetRequestMethodWithSupportedRequestMethod() throws Exception {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT;
		ConnectionHandler handler = new ConnectionHandler(new URL(url), new HashMap<>());
		handler.setRequestMethod("POST");
		assertEquals("POST", handler.getRequestMethod(), "Request method should be POST");

		handler.setRequestMethod("GET");
		assertEquals("GET", handler.getRequestMethod(), "Request method should be GET");
	}

	@Test
	void testSetRequestMethodWithUnsupportedRequestMethod() throws Exception {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT;
		ConnectionHandler handler = new ConnectionHandler(new URL(url), new HashMap<>());
		assertThrows(IllegalArgumentException.class, () -> handler.setRequestMethod("UNSUPPORTED_REQUEST_METHOD"),
				"Should throw IllegalArgumentException");
	}
}
