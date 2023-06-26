package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.BinaryBody.binary;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpStatusCode;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.uib.Utils;
import de.uib.configed.Globals;

public class ServerFacadeTest {
	static ClientAndServer clientServer;

	@BeforeAll
	static void setup() {
		Globals.disableCertificateVerification = true;
		clientServer = ClientAndServer.startClientAndServer(Utils.PORT);
	}

	@AfterAll
	static void close() {
		clientServer.stop();
	}

	@Test
	void testIfRetrievingResponseFunctionsWithValidConnection() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization))
				.respond(response().withHeader("Server", "opsiconfd 4.2.0.309 (uvicorn)"));

		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("jsonrpc", "2.0");
		resultMap.put("method", "accessControl_authenticated");
		resultMap.put("result", "true");

		byte[] resultBytes = null;
		try {
			resultBytes = new MessagePackMapper().writeValueAsBytes(resultMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		clientServer.withSecure(true)
				.when(request().withMethod("POST").withPath("/rpc")
						.withHeaders(header("Authorization", "Basic " + authorization),
								header("X-opsi-session-lifetime", "900"),
								header("User-Agent", Globals.APPNAME + " " + Globals.VERSION),
								header("Accept-Encoding",
										"lz4, gzip"),
								header("Accept", "application/msgpack"), header("Content-Type", "application/msgpack"))
						.withBody(
								"BCJNGGBwczAAAICDpm1ldGhvZLthY2Nlc3NDb250cm9sX2F1dGhlbnRpY2F0ZWSiaWQBpnBhcmFtc5AAAAAA"))
				.respond(response().withHeader(header("Content-Type", "application/msgpack"))
						.withStatusCode(HttpStatusCode.ACCEPTED_202.code()).withBody(binary(resultBytes)));

		ServerFacade facade = new ServerFacade(Utils.HOST + ":" + Utils.PORT, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade
				.retrieveResponse(new OpsiMethodCall("accessControl_authenticated", new Object[0]));

		assertNotNull(result, "returned result should not equal null");
		assertFalse(result.isEmpty(), "returned result should not be empty");
		assertTrue(Boolean.valueOf((String) result.get("result")), "return should equal true, for authenticated");
		assertEquals(ConnectionState.CONNECTED, facade.getConnectionState().getState(),
				"The connection state should be connected");
	}

	@Test
	void testIfRetrievingResponseFunctionsWithNullOMC() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization))
				.respond(response().withHeader("Server", "opsiconfd 4.2.0.309 (uvicorn)"));

		clientServer.withSecure(true)
				.when(request().withMethod("POST").withPath("/rpc").withHeaders(
						header("Authorization", "Basic " + authorization), header("X-opsi-session-lifetime", "900"),
						header("User-Agent", Globals.APPNAME + " " + Globals.VERSION),
						header("Accept-Encoding", "lz4, gzip"), header("Accept", "application/msgpack")))
				.respond(response().withStatusCode(HttpStatusCode.BAD_REQUEST_400.code()));

		ServerFacade facade = new ServerFacade(Utils.HOST + ":" + Utils.PORT, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade.retrieveResponse(null);

		assertNotNull(result, "returned result should not equal null");
		assertTrue(result.isEmpty(), "returned result should be empty");
		assertEquals(ConnectionState.ERROR, facade.getConnectionState().getState(),
				"The connection state should indicate an error");
	}

	@Test
	void testIfRetrievingResponseFunctionsWithNonExistingRPCMethod() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization))
				.respond(response().withHeader("Server", "opsiconfd 4.2.0.309 (uvicorn)"));

		clientServer.withSecure(true)
				.when(request().withMethod("POST").withPath("/rpc")
						.withHeaders(header("Authorization", "Basic " + authorization),
								header("X-opsi-session-lifetime", "900"),
								header("User-Agent", Globals.APPNAME + " " + Globals.VERSION),
								header("Accept-Encoding", "lz4, gzip"), header("Accept", "application/msgpack"))
						.withBody("{\"method\":\"non_existing_method\",\"id\":1,\"params\":[]}"))
				.respond(response().withStatusCode(HttpStatusCode.BAD_REQUEST_400.code()));

		ServerFacade facade = new ServerFacade(Utils.HOST + ":" + Utils.PORT, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade.retrieveResponse(new OpsiMethodCall("non_existing_method", new Object[0]));

		assertNotNull(result, "returned result should not equal null");
		assertTrue(result.isEmpty(), "returned result should be empty");
		assertEquals(ConnectionState.ERROR, facade.getConnectionState().getState(),
				"The connection state should indicate an error");
	}

	@Test
	void testServerFacadeWithNullParamters() {
		assertThrows(IllegalArgumentException.class, () -> new ServerFacade(null, null, null),
				"Should throw IllegalArgumentException");
	}
}
