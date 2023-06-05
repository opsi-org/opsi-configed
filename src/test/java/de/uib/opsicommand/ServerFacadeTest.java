package de.uib.opsicommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.uib.Utils;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;

public class ServerFacadeTest {
	@BeforeAll
	static void setup() {
		Globals.disableCertificateVerification = true;
	}

	@Test
	void testIfRetrievingResponseFunctionsWithValidConnection() {
		ServerFacade facade = new ServerFacade(Utils.HOST, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade
				.retrieveResponse(new OpsiMethodCall("accessControl_authenticated", new Object[0]));
		Logging.devel(this, "result: " + result);

		assertNotNull(result, "returned result should not equal null");
		assertFalse(result.isEmpty(), "returned result should not be empty");
		assertTrue((boolean) result.get("result"), "return should equal true, for authenticated");
		assertEquals(ConnectionState.CONNECTED, facade.getConnectionState().getState(),
				"The connection state should be connected");
	}

	@Test
	void testIfRetrievingResponseFunctionsWithNullOMC() {
		ServerFacade facade = new ServerFacade(Utils.HOST, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade.retrieveResponse(null);

		assertNotNull(result, "returned result should not equal null");
		assertTrue(result.isEmpty(), "returned result should be empty");
		assertEquals(ConnectionState.ERROR, facade.getConnectionState().getState(),
				"The connection state should indicate an error");
	}

	@Test
	@SuppressWarnings("unchecked")
	void testIfRetrievingResponseFunctionsWithNonExistingRPCMethod() {
		ServerFacade facade = new ServerFacade(Utils.HOST, Utils.USERNAME, Utils.PASSWORD);
		Map<String, Object> result = facade.retrieveResponse(new OpsiMethodCall("non_existing_method", new Object[0]));
		Logging.devel(this, "result: " + result);

		assertNotNull(result, "returned result should not equal null");
		assertFalse(result.isEmpty(), "returned result should not be empty");
		assertFalse(((Map<String, Object>) result.get("error")).isEmpty(), "returned result should contain an error");
		assertEquals(ConnectionState.CONNECTED, facade.getConnectionState().getState(),
				"The connection state should be connected");
	}
}
