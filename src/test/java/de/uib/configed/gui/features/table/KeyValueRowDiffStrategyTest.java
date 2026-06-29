package de.uib.configed.gui.features.table;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import de.uib.configed.gui.features.table.RowData.RowState;
import de.uib.configed.gui.share.datapanel.KeyValueRowDiffStrategy;

public class KeyValueRowDiffStrategyTest {
	private static Map<String, Object> createMap(String key, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put(key, value);
		return map;
	}

	private static Map<String, Object> createRowMap(String key, Object value) {
		Map<String, Object> map = new HashMap<>();
		map.put("key", key);
		map.put("value", value);
		return map;
	}

	private static RowData rowData(String key, Object value) {
		return RowData.builder().id(UUID.randomUUID().toString()).values(createRowMap(key, value)).build();
	}

	@Test
	void shouldReturnNormal_whenDefaultsMapIsNull() {
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(null, new HashMap<>(), false);

		RowState result = strategy.getRowStyle(null, "anyCol", "anyValue", null);

		assertEquals(RowState.NORMAL, result);
	}

	@Test
	void shouldReturnMissingData_whenKeyNotInDefaultsMap() {
		Map<String, Object> defaults = createMap("existingKey", "defaultValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), false);
		RowData rowData = rowData("nonExistentKey", null);

		RowState result = strategy.getRowStyle(rowData, "anyCol", "anyValue", null);

		assertEquals(RowState.MISSING_DATA, result);
	}

	@Test
	void shouldReturnMissingData_whenRowDataIsNullAndNoMatchingDefault() {
		Map<String, Object> defaults = createMap("someKey", "someValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), false);

		RowState result = strategy.getRowStyle(null, "anyCol", null, null);

		assertEquals(RowState.MISSING_DATA, result);
	}

	@Test
	void shouldReturnNormal_whenCurrentValueMatchesDefaultAndKeyNotInOriginals() {
		Map<String, Object> defaults = createMap("testKey", "expectedValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), false);
		RowData rowData = rowData("testKey", null);

		RowState result = strategy.getRowStyle(rowData, "anyCol", "expectedValue", null);

		assertEquals(RowState.NORMAL, result);
	}

	@Test
	void shouldReturnModified_whenCurrentValueDiffersFromDefault() {
		Map<String, Object> defaults = createMap("testKey", "defaultValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), false);
		RowData rowData = rowData("testKey", null);

		RowState result = strategy.getRowStyle(rowData, "testKey", "modifiedValue", null);

		assertEquals(RowState.MODIFIED, result);
	}

	@Test
	void shouldReturnModified_whenKeyExistsInOriginalsMapEvenIfValueMatchesDefault() {
		Map<String, Object> defaults = createMap("testKey", "sameAsDefault");
		Map<String, Object> originals = createMap("testKey", "previousValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, originals, false);
		RowData rowData = rowData("testKey", null);

		RowState result = strategy.getRowStyle(rowData, "anyCol", "sameAsDefault", "previousValue");

		assertEquals(RowState.MODIFIED, result);
	}

	@Test
	void shouldReturnModified_whenOriginalsMapIsNullButCurrentValueDiffersFromDefault() {
		Map<String, Object> defaults = createMap("testKey", "defaultValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, null, false);
		RowData rowData = rowData("testKey", null);

		RowState result = strategy.getRowStyle(rowData, "anyCol", "modifiedValue", null);

		assertEquals(RowState.MODIFIED, result);
	}

	@Test
	void shouldUseRowDataValueAsCurrent_whenPinnedPropertyIsTrue() {
		Map<String, Object> defaults = createMap("testKey", "expectedValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), true);
		RowData rowData = rowData("testKey", "actualValue");

		RowState result = strategy.getRowStyle(rowData, "anyCol", "ignoredParam", null);

		assertEquals(RowState.MODIFIED, result);
	}

	@Test
	void shouldReturnNormal_whenPinnedPropertyIsTrueAndRowDataValueMatchesDefault() {
		Map<String, Object> defaults = createMap("testKey", "matchingValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), true);
		RowData rowData = rowData("testKey", "matchingValue");

		RowState result = strategy.getRowStyle(rowData, "anyCol", "ignoredParam", null);

		assertEquals(RowState.NORMAL, result);
	}

	@Test
	void shouldReturnMissingData_whenPinnedPropertyIsTrueAndKeyNotInDefaults() {
		Map<String, Object> defaults = createMap("otherKey", "someValue");
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), true);
		RowData rowData = rowData("missingKey", "someValue");

		RowState result = strategy.getRowStyle(rowData, "anyCol", "ignoredParam", null);

		assertEquals(RowState.MISSING_DATA, result);
	}

	@Test
	void shouldReturnModified_whenObjectValueContentDiffersFromDefault() {
		Map<String, Object> defaults = createMap("config", Map.of("setting1", true));
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), false);
		RowData rowData = rowData("config", null);

		RowState result = strategy.getRowStyle(rowData, "anyCol", Map.of("setting1", false), null);

		assertEquals(RowState.MODIFIED, result);
	}

	@Test
	void shouldReturnNormal_whenObjectValueContentEqualsDefault() {
		Map<String, Object> defaults = createMap("config", Map.of("setting1", true));
		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, new HashMap<>(), false);
		RowData rowData = rowData("config", null);

		RowState result = strategy.getRowStyle(rowData, "anyCol", Map.of("setting1", true), null);

		assertEquals(RowState.NORMAL, result);
	}

	@Test
	void shouldEvaluateCorrectlyAcrossMultipleKeys() {
		Map<String, Object> defaults = new HashMap<>();
		defaults.put("key1", "value1");
		defaults.put("key2", "value2");
		defaults.put("key3", "value3");

		Map<String, Object> originals = createMap("key3", "original");

		KeyValueRowDiffStrategy strategy = new KeyValueRowDiffStrategy(defaults, originals, false);

		RowData unchangedRow = rowData("key1", null);
		RowData modifiedViaOriginals = rowData("key3", null);
		RowData missingRow = rowData("nonExistent", null);

		assertAll(() -> assertEquals(RowState.NORMAL, strategy.getRowStyle(unchangedRow, "col", "value1", null)),
				() -> assertEquals(RowState.MODIFIED,
						strategy.getRowStyle(modifiedViaOriginals, "col", "newValue", null)),
				() -> assertEquals(RowState.MISSING_DATA, strategy.getRowStyle(missingRow, "col", "anything", null)));
	}
}
