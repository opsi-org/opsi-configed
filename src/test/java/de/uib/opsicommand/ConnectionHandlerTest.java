package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
	void testEstablishingConnectionWithValidCredentials() throws Exception {
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
	void testEstablishingConnectionWithIncorrectCredentials() throws Exception {
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
}
