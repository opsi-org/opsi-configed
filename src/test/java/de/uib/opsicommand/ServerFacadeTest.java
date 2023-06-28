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
import static org.mockserver.model.JsonBody.json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.MatchType;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpStatusCode;
import org.msgpack.jackson.dataformat.MessagePackMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.uib.Utils;
import de.uib.configed.Globals;
import net.jpountz.lz4.LZ4FrameOutputStream;

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
	void testIfRetrievingResponseFunctionsWithValidConnectionAndMessagePackSerialization() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withHeader("Server", "opsiconfd 4.2.0.309 (uvicorn)"));

		OpsiMethodCall omc = new OpsiMethodCall("accessControl_authenticated", new Object[0]);
		byte[] requestBytes = produceMessagePack(omc.getOMCMap());

		Map<String, String> resultMap = new HashMap<>();
		resultMap.put("jsonrpc", "2.0");
		resultMap.put("method", "accessControl_authenticated");
		resultMap.put("result", "true");

		byte[] resultBytes = produceMessagePack(resultMap);

		clientServer
				.withSecure(true).when(
						request().withMethod("POST").withPath("/rpc")
								.withHeaders(header("Authorization", "Basic " + authorization),
										header("X-opsi-session-lifetime", "900"),
										header("User-Agent", Globals.APPNAME + " " + Globals.VERSION),
										header("Accept-Encoding", "lz4, gzip"), header("Accept", "application/msgpack"),
										header("Content-Type", "application/msgpack"))
								.withBody(binary(requestBytes)),
						Times.exactly(1))
				.respond(response().withHeader(header("Content-Type", "application/msgpack"))
						.withStatusCode(HttpStatusCode.ACCEPTED_202.code()).withBody(binary(resultBytes)));

		ServerFacade facade = new ServerFacade(Utils.HOST + ":" + Utils.PORT, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade.retrieveResponse(omc);

		assertNotNull(result, "returned result should not equal null");
		assertFalse(result.isEmpty(), "returned result should not be empty");
		assertTrue(Boolean.valueOf((String) result.get("result")), "return should equal true, for authenticated");
		assertEquals(ConnectionState.CONNECTED, facade.getConnectionState().getState(),
				"The connection state should be connected");
	}

	@Test
	void testIfRetrievingResponseFunctionsWithValidConnectionAndJSONSerialization() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withHeader("Server", "opsiconfd 4.1.0.0 (uvicorn)"));

		clientServer.withSecure(true).when(request().withMethod("POST").withPath("/rpc").withHeaders(
				header("Authorization", "Basic " + authorization), header("X-opsi-session-lifetime", "900"),
				header("User-Agent", Globals.APPNAME + " " + Globals.VERSION), header("Accept-Encoding", "lz4, gzip"),
				header("Accept", "application/json"), header("Content-Type", "application/json")).withBody(
						json("{\"method\":\"accessControl_authenticated\",\"id\":1,\"params\":[]}", MatchType.STRICT)),
				Times.exactly(1))
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
	@SuppressWarnings("unchecked")
	void testIfRetrievingResponseFunctionsWithValidConnectionAndLZ4Compression() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withHeader("Server", "opsiconfd 4.2.0.309 (uvicorn)"));

		List<String> list = new ArrayList<>();
		for (int i = 0; i < 10000; i++) {
			list.add("A");
		}
		OpsiMethodCall omc = new OpsiMethodCall("config_updateObjects", list.toArray());

		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("jsonrcp", "2.0");
		resultMap.put("method", "config_updateObjects");
		resultMap.put("result", new Object[0]);

		byte[] responseBytes = compressWithLZ4(produceMessagePack(resultMap));

		clientServer.withSecure(true).when(
				request().withMethod("POST").withPath("/rpc")
						.withHeaders(header("Authorization", "Basic " + authorization),
								header("X-opsi-session-lifetime", "900"),
								header("User-Agent", Globals.APPNAME + " " + Globals.VERSION),
								header("Accept-Encoding", "lz4, gzip"), header("Accept",
										"application/msgpack"),
								header("Content-Type", "application/msgpack"), header("Content-Encoding", "lz4"))
						.withBody(
								"BCJNGGBwc4YAAAD/HoOmbWV0aG9ktGNvbmZpZ191cGRhdGVPYmplY3RzomlkAaZwYXJhbXPcJxChQQIA////////////////////////////////////////////////////////////////////////////////////////////////////////VFBBoUGhQQAAAAA="),
				Times.exactly(1))
				.respond(response().withStatusCode(HttpStatusCode.ACCEPTED_202.code())
						.withHeaders(header("Content-Encoding", "lz4"), header("Content-Type", "application/msgpack"))
						.withBody(binary(responseBytes)));

		ServerFacade facade = new ServerFacade(Utils.HOST + ":" + Utils.PORT, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade.retrieveResponse(omc);

		assertNotNull(result, "returned response should not equal null");
		assertFalse(result.isEmpty(), "returned response should not be empty");
		assertTrue(((List<Object>) result.get("result")).isEmpty(), "returned result should be empty");
		assertEquals(ConnectionState.CONNECTED, facade.getConnectionState().getState(),
				"The connection state should be connected");
	}

	@Test
	void testIfRetrievingResponseFunctionsWithNullOMC() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withHeader("Server", "opsiconfd 4.2.0.309 (uvicorn)"));

		clientServer.withSecure(true)
				.when(request().withMethod("POST").withPath("/rpc").withHeaders(
						header("Authorization", "Basic " + authorization), header("X-opsi-session-lifetime", "900"),
						header("User-Agent", Globals.APPNAME + " " + Globals.VERSION),
						header("Accept-Encoding", "lz4, gzip"), header("Accept", "application/msgpack")),
						Times.exactly(1))
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
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withHeader("Server", "opsiconfd 4.2.0.309 (uvicorn)"));

		OpsiMethodCall omc = new OpsiMethodCall("non_existing_method", new Object[0]);
		byte[] requestBytes = produceMessagePack(omc.getOMCMap());

		clientServer.withSecure(true).when(
				request().withMethod("POST").withPath("/rpc")
						.withHeaders(header("Authorization", "Basic " + authorization),
								header("X-opsi-session-lifetime", "900"),
								header("User-Agent", Globals.APPNAME + " " + Globals.VERSION),
								header("Accept-Encoding", "lz4, gzip"), header("Accept", "application/msgpack"),
								header("Content-Type", "application/msgpack"))
						.withBody(binary(requestBytes)),
				Times.exactly(1)).respond(response().withStatusCode(HttpStatusCode.BAD_REQUEST_400.code()));

		ServerFacade facade = new ServerFacade(Utils.HOST + ":" + Utils.PORT, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade.retrieveResponse(omc);

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

	private <T, V> byte[] produceMessagePack(Map<T, V> data) {
		byte[] result = new byte[0];
		try {
			result = new MessagePackMapper().writeValueAsBytes(data);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return result;
	}

	private byte[] compressWithLZ4(byte[] data) {
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		try (LZ4FrameOutputStream compressedOutput = new LZ4FrameOutputStream(byteOutput)) {
			compressedOutput.write(data);
			compressedOutput.flush();
			return Arrays.copyOfRange(byteOutput.toByteArray(), 0, byteOutput.toByteArray().length);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return new byte[0];
	}
}
