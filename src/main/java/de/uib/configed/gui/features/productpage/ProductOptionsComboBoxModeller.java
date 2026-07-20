/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import de.uib.configed.core.domain.productstate.ActionRequest;
import de.uib.configed.core.domain.productstate.InstallationStatus;
import de.uib.configed.core.domain.productstate.ProductState;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.share.table.gui.ComboBoxModeller;
import de.uib.configed.share.logging.Logging;

public class ProductOptionsComboBoxModeller implements ComboBoxModeller {
	private static final Set<String> defaultDisplayValues = new LinkedHashSet<>();
	static {
		defaultDisplayValues.add(ProductConfigurationEngine.NONE_DISPLAY_STRING);
		defaultDisplayValues.add(ProductConfigurationEngine.SUCCESS_DISPLAY_STRING);
		defaultDisplayValues.add(ProductConfigurationEngine.FAILED_DISPLAY_STRING);
	}

	private Map<String, List<String>> possibleActions;
	private GenericTableViewComponent tableViewComponent;
	private ProductConfigurationEngine engine;

	public void setPossibleActions(Map<String, List<String>> possibleActions) {
		this.possibleActions = possibleActions;
	}

	public void setTableViewComponent(GenericTableViewComponent tableViewComponent) {
		this.tableViewComponent = tableViewComponent;
	}

	public void setProductConfigurationEngine(ProductConfigurationEngine engine) {
		this.engine = engine;
	}

	@Override
	public ComboBoxModel<String> getComboBoxModel(int row, int column) {
		String[] possibleOptions;

		String columnKey = tableViewComponent.getColumnByModelIndex(column).getKey();

		String productBeingEdited = tableViewComponent.getRowByModelIndex(row).getValue(ProductState.KEY_PRODUCT_ID,
				String.class);
		engine.setProductBeingEdited(productBeingEdited);

		Logging.debug(this, "getComboBoxModel: row=", row, ", column=", column, ", columnKey=", columnKey,
				"actualProduct=", productBeingEdited);
		if (ActionRequest.KEY.equals(columnKey)) {
			possibleOptions = producePossibleActions(productBeingEdited);
		} else if (InstallationStatus.KEY.equals(columnKey)) {
			possibleOptions = producePossibleInstallationStatus(InstallationStatus.getDisplayLabelsForChoice(),
					productBeingEdited);
		} else if (ProductState.KEY_INSTALLATION_INFO.equals(columnKey)) {
			possibleOptions = producePossibleInstallationInfos((String) tableViewComponent.getValueAt(row, column));
		} else {
			Logging.warning(this, "unexpected column ", column);

			return null;
		}

		return new DefaultComboBoxModel<>(possibleOptions);
	}

	private String[] producePossibleActions(String product) {
		Logging.debug(this, " possible actions  ", possibleActions);
		List<String> actionsForProduct = new ArrayList<>();
		if (possibleActions != null) {
			for (String label : possibleActions.get(product)) {
				actionsForProduct.add(ActionRequest.produceFromLabel(label));
			}

			// Add in values in correct ordering
			String[] displayLabels = ActionRequest.getDisplayLabelsForChoice();
			actionsForProduct.retainAll(List.of(displayLabels));

			Logging.debug("Possible actions as array  ", actionsForProduct);
		}

		if (actionsForProduct.isEmpty()) {
			actionsForProduct.add("null");
		}

		return actionsForProduct.toArray(new String[0]);
	}

	private String[] producePossibleInstallationStatus(String[] defaultValues, String product) {
		if (possibleActions.get(product) == null) {
			String state = engine.getVisualValue(ProductState.KEY_INSTALLATION_STATUS, product);
			if (state == null) {
				Logging.debug(this, "producePossibleInstallationStatus: no possible actions for product ", product,
						" and no state information available");
				return new String[] { "null" };
			}

			Logging.debug(this, "producePossibleInstallationStatus: no possible actions for product ", product);
			return new String[0];
		}

		Logging.debug(this, "producePossibleInstallationStatus: defaultValues=", Arrays.toString(defaultValues),
				", actualProduct=", product);
		return defaultValues;
	}

	private static String[] producePossibleInstallationInfos(String cellValue) {
		if (cellValue == null) {
			cellValue = "";
		}

		Set<String> values = new LinkedHashSet<>();

		Logging.debug("producePossibleInstallationInfos: cellValue=" + cellValue + ", defaultDisplayValues="
				+ defaultDisplayValues);
		if (!defaultDisplayValues.contains(cellValue)) {
			values.add(cellValue);
		}

		values.addAll(defaultDisplayValues);

		Logging.debug("producePossibleInstallationInfos: cellValue=" + cellValue + ", values=" + values);
		return values.toArray(new String[0]);
	}
}
