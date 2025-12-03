package de.uib.configed.gui.features.clients.add;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.features.clients.add.AddClientValidator.RowValidation;

class RowValidationTests {
	AddClientValidator.HostnameDomainValidator hostnameDomainValidator = new AddClientValidator.HostnameDomainValidator();
	AddClientValidator.HostCollisionValidator hostCollisionValidator = new AddClientValidator.HostCollisionValidator();
	AddClientValidator.BooleanValidator booleanValidator = new AddClientValidator.BooleanValidator();
	AddClientValidator.NetbiosValidator netbiosValidator = new AddClientValidator.NetbiosValidator();

	private static AddClientModel baseModel() {
		return AddClientModel.builder().hostnames(List.of("existing.dom")).depots(List.of("depot1.dom")).build();
	}

	private List<Object> row(String hostname, String domain, String depot, String desc, String inv, String notes,
			String ip, String uuid, String mac, String netboot, String shutdown, String wan, String groups) {
		return List.of(hostname, domain, depot, desc, inv, notes, ip, uuid, mac, netboot, shutdown, wan, groups);
	}

	@Test
	void shouldReturnDropResult_whenHostnameIsEmpty() {
		List<Object> row = row("", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "true", "false", "");
		RowValidation.Result result = hostnameDomainValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.DROP, result.type());
		assertNotNull(result.effect());
	}

	@Test
	void shouldReturnPauseResult_whenHostnameAlreadyExists() {
		List<Object> row = row("existing", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "true",
				"false", "");
		RowValidation.Result result = hostCollisionValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.PAUSE, result.type());
		assertTrue(result.effect() instanceof AddClientEffect.UIEffect.ShowOverwriteHostDialog);
	}

	@Test
	void shouldReturnDropResult_whenBooleanFieldsAreInvalid() {
		List<Object> row = row("h", "d", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "invalid", "true",
				"g1;g2");
		RowValidation.Result result = booleanValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.DROP, result.type());
		assertNotNull(result.effect());
	}

	@Test
	void shouldReturnPauseResult_whenHostnameExceeds15Chars() {
		List<Object> row = row("thisisaverylonghostname", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb",
				"true", "false", "");
		RowValidation.Result result = netbiosValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.PAUSE, result.type());
		assertNotNull(result.effect());
	}

	@Test
	void shouldReturnPauseResult_whenHostnameContainsOnlyNumbers() {
		List<Object> row = row("123456", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "true", "false",
				"");
		RowValidation.Result result = netbiosValidator.validate(row, baseModel());
		assertEquals(RowValidation.ResultType.PAUSE, result.type());
		assertNotNull(result.effect());
	}

	@Test
	void shouldReturnSuccessResult_whenRowIsValid() {
		List<Object> row = row("validhost", "dom", "d", "desc", "inv", "notes", "ip", "uuid", "mac", "nb", "true",
				"false", "");
		assertEquals(RowValidation.ResultType.SUCCESS, hostnameDomainValidator.validate(row, baseModel()).type());
		assertEquals(RowValidation.ResultType.SUCCESS, booleanValidator.validate(row, baseModel()).type());
		assertEquals(RowValidation.ResultType.SUCCESS,
				hostCollisionValidator.validate(row, baseModel().withHostnames(List.of())).type());
		assertEquals(RowValidation.ResultType.SUCCESS, netbiosValidator.validate(row, baseModel()).type());
	}
}
