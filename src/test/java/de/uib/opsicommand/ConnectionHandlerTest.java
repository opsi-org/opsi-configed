package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.uib.Utils;
import de.uib.configed.Globals;

public class ConnectionHandlerTest {
	@BeforeAll
	static void setup() {
		Globals.disableCertificateVerification = true;
	}

	@Test
	void testEstablishConnectionWithValidCredentials() throws Exception {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/login?redirect=/admin";
		Map<String, String> requestProperties = new HashMap<>();

		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));
		requestProperties.put("Authorization", "Basic " + authorization);
		requestProperties.put("X-opsi-session-lifetime", "900");
		requestProperties.put("Accept-Encoding", "lz4");
		requestProperties.put("User-Agent", Globals.APPNAME + " " + Globals.VERSION);
		requestProperties.put("Accept", "application/msgpack");

		ConnectionHandler handler = new ConnectionHandler(new URL(url), requestProperties);
		handler.setRequestMethod(null);
		HttpsURLConnection connection = handler.establishConnection(true);

		assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode(),
				"Connecting with valid credentials should return HTTP response code 200: "
						+ connection.getResponseCode());
	}

	@Test
	void testEstablishConnectionWithIncorrectCredentials() throws Exception {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/rpc";
		Map<String, String> requestProperties = new HashMap<>();

		String authorization = Base64.getEncoder()
				.encodeToString(("non-existent:user").getBytes(StandardCharsets.UTF_8));
		requestProperties.put("Authorization", "Basic " + authorization);
		requestProperties.put("X-opsi-session-lifetime", "900");
		requestProperties.put("Accept-Encoding", "lz4");
		requestProperties.put("User-Agent", Globals.APPNAME + " " + Globals.VERSION);
		requestProperties.put("Accept", "application/msgpack");

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
		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/rpc";
		ConnectionHandler handler = new ConnectionHandler(new URL(url), new HashMap<>());
		handler.setRequestMethod(null);

		assertNull(handler.getRequestMethod(), "Request method should be NULL");
	}

	@Test
	void testSetRequestMethodWithSupportedRequestMethod() throws Exception {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/rpc";
		ConnectionHandler handler = new ConnectionHandler(new URL(url), new HashMap<>());
		handler.setRequestMethod("POST");
		assertEquals("POST", handler.getRequestMethod(), "Request method should be POST");

		handler.setRequestMethod("GET");
		assertEquals("GET", handler.getRequestMethod(), "Request method should be GET");
	}

	@Test
	void testSetRequestMethodWithUnsupportedRequestMethod() throws Exception {
		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/rpc";
		ConnectionHandler handler = new ConnectionHandler(new URL(url), new HashMap<>());
		assertThrows(IllegalArgumentException.class, () -> handler.setRequestMethod("UNSUPPORTED_REQUEST_METHOD"),
				"Should throw IllegalArgumentException");
	}
}
