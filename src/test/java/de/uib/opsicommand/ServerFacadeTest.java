package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.Header.header;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.HttpStatusCode;

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

		clientServer.withSecure(true)
				.when(request().withMethod("POST").withPath("/rpc")
						.withHeaders(header("Authorization", "Basic " + authorization),
								header("X-opsi-session-lifetime", "900"),
								header("User-Agent", Globals.APPNAME + " " + Globals.VERSION),
								header("Accept-Encoding", "lz4, gzip"), header("Accept", "application/msgpack"))
						.withBody("{\"method\":\"accessControl_authenticated\",\"id\":1,\"params\":[]}"))
				.respond(response().withStatusCode(HttpStatusCode.ACCEPTED_202.code())
						.withBody(json(
								"{\"jsonrpc\": \"2.0\", \"method\": \"accessControl_authenticated\", \"result\": true}",
								MatchType.STRICT)));

		ServerFacade facade = new ServerFacade(Utils.HOST + ":" + Utils.PORT, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade
				.retrieveResponse(new OpsiMethodCall("accessControl_authenticated", new Object[0]));

		assertNotNull(result, "returned result should not equal null");
		assertFalse(result.isEmpty(), "returned result should not be empty");
		assertTrue((boolean) result.get("result"), "return should equal true, for authenticated");
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
