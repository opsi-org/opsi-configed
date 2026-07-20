/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.share.logging.Logging;

public class RequirementsTableModel {
	public static final String COL_REQUIRED_PRODUCT = "requiredProduct";
	public static final String COL_REQUIREMENT_DEFAULT = "requirementDefault";
	public static final String COL_REQUIREMENT_BEFORE = "requirementBefore";
	public static final String COL_REQUIREMENT_AFTER = "requirementAfter";

	private Set<String> keySet = new TreeSet<>();

	private Map<String, String> requMap;
	private Map<String, String> requBeforeMap;
	private Map<String, String> requAfterMap;
	private Map<String, String> requDeinstallMap;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public RequirementsTableModel() {
		Logging.info(this, "creating");

		init();
	}

	private void retrieveRequirements(String depotId, String product) {
		requMap = persistenceController.getDataServices().product.getProductRequirements(depotId, product);
		requBeforeMap = persistenceController.getDataServices().product.getProductPreRequirements(depotId, product);
		requAfterMap = persistenceController.getDataServices().product.getProductPostRequirements(depotId, product);
		requDeinstallMap = persistenceController.getDataServices().product.getProductDeinstallRequirements(depotId,
				product);
	}

	public List<Map<String, Object>> buildRequirementRows() {
		final String IDENT = "     ";
		String conditionLabel = Configed.getResourceValue("ProductInfoPane.RequirementsTable.requirementCondition");

		List<Map<String, Object>> rows = new ArrayList<>();

		for (String key : keySet) {
			// Row type 0 — product name
			rows.add(buildRow(key, null, null, null));

			// Row type 1 — setup condition
			rows.add(buildRow(IDENT + conditionLabel + " setup", requMap != null ? requMap.get(key) : null,
					requBeforeMap != null ? parenthesize(requBeforeMap.get(key)) : null,
					requAfterMap != null ? parenthesize(requAfterMap.get(key)) : null));

			// Row type 2 — uninstall condition
			rows.add(buildRow(IDENT + conditionLabel + " uninstall",
					requDeinstallMap != null ? requDeinstallMap.get(key) : null, null, null));
		}

		return rows;
	}

	private static Map<String, Object> buildRow(String col0, String col1, String col2, String col3) {
		Map<String, Object> row = new LinkedHashMap<>();
		row.put(COL_REQUIRED_PRODUCT, col0);
		row.put(COL_REQUIREMENT_DEFAULT, col1);
		row.put(COL_REQUIREMENT_BEFORE, col2);
		row.put(COL_REQUIREMENT_AFTER, col3);
		return row;
	}

	private static String parenthesize(String value) {
		return value == null ? null : ("(" + value + ")");
	}

	private void init() {
		// we assume that the productId determines the requirements since we are on a
		// preselected depot
		setActualProduct(null, null);
	}

	public void setActualProduct(String depotId, String product) {
		requMap = null;
		requBeforeMap = null;
		requAfterMap = null;
		requDeinstallMap = null;

		if (product != null) {
			retrieveRequirements(depotId, product);

			keySet = new TreeSet<>();
			if (requMap != null && requMap.keySet() != null) {
				keySet.addAll(requMap.keySet());
			}
			if (requBeforeMap != null && requBeforeMap.keySet() != null) {
				keySet.addAll(requBeforeMap.keySet());
			}
			if (requAfterMap != null && requAfterMap.keySet() != null) {
				keySet.addAll(requAfterMap.keySet());
			}
			if (requDeinstallMap != null && requDeinstallMap.keySet() != null) {
				keySet.addAll(requDeinstallMap.keySet());
			}
		}
	}
}
