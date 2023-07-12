package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpStatusCode;

import de.uib.Utils;
import de.uib.configed.Globals;

@TestMethodOrder(OrderAnnotation.class)
class OpsiServerVersionRetrieverTest {
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
	@Order(1)
	void testOpsiServerVersionRetrieverWithNullParameters() {
		assertThrows(IllegalArgumentException.class, () -> new OpsiServerVersionRetriever(null, null, null),
				"Should throw IllegalArgumentException");
	}

	@Test
	@Order(2)
	void testShouldRetrieveServerVersionNotFoundText() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()));

		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/";
		OpsiServerVersionRetriever retriever = new OpsiServerVersionRetriever(url, Utils.USERNAME, Utils.PASSWORD);
		retriever.checkServerVersion();
		assertEquals("Server version not found (assume 4.1)", retriever.getServerVersion(),
				"It should say \"Server version not found (assume 4.1)\": " + retriever.getServerVersion());
	}

	@Test
	@Order(3)
	void testShouldRetrieve41Version() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()).withHeader("Server",
						"4.2.3.0 (uvicorn)"));

		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/";
		OpsiServerVersionRetriever retriever = new OpsiServerVersionRetriever(url, Utils.USERNAME, Utils.PASSWORD);
		retriever.checkServerVersion();
		assertEquals("4.1.0.0", retriever.getServerVersion(),
				"The server version should equal 4.1.0.0: " + retriever.getServerVersion());
	}

	@Test
	@Order(4)
	void testShouldRetrieve42Version() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()).withHeader("Server",
						"opsiconfd 4.2.0.309 (uvicorn)"));

		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/";
		OpsiServerVersionRetriever retriever = new OpsiServerVersionRetriever(url, Utils.USERNAME, Utils.PASSWORD);
		retriever.checkServerVersion();
		assertEquals("4.2.0.309", retriever.getServerVersion(),
				"The server version should equal 4.2.0.309: " + retriever.getServerVersion());
	}

	@Test
	@Order(5)
	void testVersionComparisonFor420309() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()).withHeader("Server",
						"opsiconfd 4.2.0.309 (uvicorn)"));

		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/";
		OpsiServerVersionRetriever retriever = new OpsiServerVersionRetriever(url, Utils.USERNAME, Utils.PASSWORD);
		retriever.checkServerVersion();
		assertTrue(retriever.isServerVersionAtLeast("4.2"),
				"Should return true for check if it is at least 4.2: " + retriever.getServerVersion());
	}

	@Test
	@Order(6)
	void testVersionComparisonWithNullParameter() {
		String authorization = Base64.getEncoder()
				.encodeToString((Utils.USERNAME + ":" + Utils.PASSWORD).getBytes(StandardCharsets.UTF_8));

		clientServer.withSecure(true)
				.when(request().withMethod("HEAD").withPath("/").withHeader("Authorization", "Basic " + authorization),
						Times.exactly(1))
				.respond(response().withStatusCode(HttpStatusCode.OK_200.code()).withHeader("Server",
						"opsiconfd 4.2.0.309 (uvicorn)"));

		String url = "https://" + Utils.HOST + ":" + Utils.PORT + "/";
		OpsiServerVersionRetriever retriever = new OpsiServerVersionRetriever(url, Utils.USERNAME, Utils.PASSWORD);
		retriever.checkServerVersion();
		assertFalse(retriever.isServerVersionAtLeast(null),
				"Should return true for check if it is at least 4.2: " + retriever.getServerVersion());
	}
}
