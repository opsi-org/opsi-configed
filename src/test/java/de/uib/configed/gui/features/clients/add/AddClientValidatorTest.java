package de.uib.configed.gui.features.clients.add;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.features.clients.add.AddClientValidator.RowValidation;
import de.uib.configed.gui.type.HostInfo;

class RowValidationTests {
	AddClientValidator.HostnameDomainValidator hostnameDomainValidator = new AddClientValidator.HostnameDomainValidator();
	AddClientValidator.HostCollisionValidator hostCollisionValidator = new AddClientValidator.HostCollisionValidator();
	AddClientValidator.BooleanValidator booleanValidator = new AddClientValidator.BooleanValidator();
	AddClientValidator.NetbiosValidator netbiosValidator = new AddClientValidator.NetbiosValidator();

	private static AddClientModel baseModel() {
		return AddClientModel.builder().hostnames(List.of("existing.dom")).depots(List.of("depot1.dom")).build();
	}

	private Map<String, Object> map(String hostname, String domain, String depot, String description, String inventory,
			String notes, String ipAddress, String uuid, String macAddress, String netbootProduct, String wanConfig,
			String installOnShutdown, String groups, String key) {
		Map<String, Object> client = new HashMap<>();
		client.put(HostInfo.HOSTNAME_KEY, hostname);
		client.put(HostInfo.CSV_DOMAIN_KEY, domain);
		client.put(HostInfo.DEPOT_OF_CLIENT_KEY, depot);
		client.put(HostInfo.CLIENT_MAC_ADDRESS_KEY, macAddress);
		client.put(HostInfo.CLIENT_DESCRIPTION_KEY, description);
		client.put(HostInfo.CLIENT_INVENTORY_NUMBER_KEY, inventory);
		client.put(HostInfo.CLIENT_NOTES_KEY, notes);
		client.put(HostInfo.CLIENT_SYSTEM_UUID_KEY, uuid);
		client.put(HostInfo.CLIENT_IP_ADDRESS_KEY, ipAddress);
		client.put(HostInfo.CSV_GROUPS_KEY, groups);
		client.put(HostInfo.CLIENT_WAN_CONFIG_KEY, wanConfig);
		client.put(HostInfo.CLIENT_SHUTDOWN_INSTALL_KEY, installOnShutdown);
		client.put(HostInfo.HOST_KEY_KEY, key);
		client.put(HostInfo.CSV_NETBOOT_PRODUCT_KEY, netbootProduct);
		return client;
	}

	@Test
	void shouldReturnDropResult_whenHostnameIsEmpty() {
		String expectedTitle = Configed.getResourceValue("error");
		String expectedMessage = Configed.getResourceValue("NewClientDialog.hostnameRules");
		Map<String, Object> row = map("", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "true",
				"false", "", "");
		RowValidation.Result result = hostnameDomainValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.DROP, result.type());
		assertAll(() -> assertNotNull(result.effect()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.ShowErrorMessage.class, result.effect()),
				() -> assertEquals(expectedTitle,
						((AddClientEffect.UIEffect.ShowErrorMessage) result.effect()).title()),
				() -> assertEquals(expectedMessage,
						((AddClientEffect.UIEffect.ShowErrorMessage) result.effect()).message()));
	}

	@Test
	void shouldReturnPauseResult_whenHostnameAlreadyExists() {
		Map<String, Object> row = map("existing", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "true",
				"false", "", "");
		RowValidation.Result result = hostCollisionValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.PAUSE, result.type());
		assertAll(() -> assertNotNull(result.effect()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.ShowOverwriteHostDialog.class, result.effect()));
	}

	@Test
	void shouldReturnDropResult_whenBooleanFieldsAreInvalid() {
		String expectedTitle = Configed.getResourceValue("NewClientDialog.nonBooleanValue.title");
		String expectedMessage = Configed.getResourceValue("NewClientDialog.nonBooleanValue.message");
		Map<String, Object> row = map("h", "d", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "invalid",
				"true", "g1,g2", "");
		RowValidation.Result result = booleanValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.DROP, result.type());
		assertAll(() -> assertNotNull(result.effect()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.ShowErrorMessage.class, result.effect()),
				() -> assertEquals(expectedTitle,
						((AddClientEffect.UIEffect.ShowErrorMessage) result.effect()).title()),
				() -> assertEquals(expectedMessage,
						((AddClientEffect.UIEffect.ShowErrorMessage) result.effect()).message()));
	}

	@Test
	void shouldReturnPauseResult_whenHostnameExceeds15Chars() {
		Map<String, Object> row = map("thisisaverylonghostname", "dom", "d", "desc", "inv", "notes", "ip", "uuid",
				"mac", "nb", "true", "false", "", "");
		RowValidation.Result result = netbiosValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.PAUSE, result.type());
		assertAll(() -> assertNotNull(result.effect()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.ShowNetbiosConfirmDialog.class, result.effect()));
	}

	@Test
	void shouldReturnPauseResult_whenHostnameContainsOnlyNumbers() {
		Map<String, Object> row = map("123456", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "true",
				"false", "", "");
		RowValidation.Result result = netbiosValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.PAUSE, result.type());
		assertAll(() -> assertNotNull(result.effect()),
				() -> assertInstanceOf(AddClientEffect.UIEffect.ShowNetbiosConfirmDialog.class, result.effect()));
	}

	@Test
	void shouldReturnSuccessResult_whenRowIsValid() {
		Map<String, Object> row = map("validhost", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb",
				"true", "false", "", "");
		assertEquals(RowValidation.ResultType.SUCCESS, hostnameDomainValidator.validate(row, baseModel()).type());
		assertEquals(RowValidation.ResultType.SUCCESS, booleanValidator.validate(row, baseModel()).type());
		assertEquals(RowValidation.ResultType.SUCCESS,
				hostCollisionValidator.validate(row, baseModel().withHostnames(List.of())).type());
		assertEquals(RowValidation.ResultType.SUCCESS, netbiosValidator.validate(row, baseModel()).type());
	}
}
