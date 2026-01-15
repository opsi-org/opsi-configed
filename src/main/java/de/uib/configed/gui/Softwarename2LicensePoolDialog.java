/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.DefaultTableModelFilterCondition;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.TableModelFilterCondition;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.provider.MapRetriever;
import de.uib.configed.gui.share.table.provider.RetrieverMapSource;
import de.uib.configed.gui.share.table.updates.MapBasedTableEditItem;
import de.uib.configed.gui.share.table.updates.MapBasedUpdater;
import de.uib.configed.gui.share.table.updates.MapItemsUpdateController;
import de.uib.configed.gui.share.table.updates.MapTableUpdateItemFactory;
import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.gui.type.licenses.AuditSoftwareXLicensePool;
import de.uib.configed.gui.type.licenses.LicensepoolEntry;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class Softwarename2LicensePoolDialog {
	public static final String VALUE_NO_LICENSE_POOL = "---";
	private PanelGenEdit panelSWnames;
	private GenTableModel modelSWnames;

	private List<String> columnNames;

	private PanelGenEdit panelSWxLicensepool;
	private GenTableModel modelSWxLicensepool;
	private List<String> columnNamesSWxLicensepool;

	private TableModelFilterCondition showOnlyNamesWithVariantLicenses;
	private TableModelFilterCondition showOnlyNamesWithoutLicenses;

	private List<MapBasedTableEditItem> updateCollection;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ControlPanelAssignToLPools controlPanelAssignToLPools;

	public enum Softwarename2LicensepoolRestriction {
		SHOW_ALL_NAMES, SHOW_ONLY_NAMES_WITH_VARIANT_LICENSEPOOLS, SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENSEPOOL
	}

	private JButton buttonSetAllAssignmentsToGloballySelectedPool;
	private JLabel labelSetAllAssignmentsToGloballySelectedPool;

	// This button and label  needs to be instanciated because it will be used in the class PanelSoftwareLicencepool
	private JButton buttonSetAllAssignmentsToPoolFromSelectedRow = new JButton();
	private JLabel labelSetAllAssignmentsToPoolFromSelectedRow = new JLabel();

	private JButton buttonRemoveAllAssignments;
	private JLabel labelRemoveAllAssignments;

	private String globalLicensePool;

	private JOptionPane optionPane;
	private JDialog dialog;

	public Softwarename2LicensePoolDialog(ControlPanelAssignToLPools controlPanelAssignToLPools) {
		this.controlPanelAssignToLPools = controlPanelAssignToLPools;

		panelSWnames = new PanelGenEdit("", false, 0, new int[] { PopupMenuTrait.POPUP_RELOAD }, true);

		panelSWxLicensepool = new PanelSoftwareLicencepool(controlPanelAssignToLPools,
				buttonSetAllAssignmentsToPoolFromSelectedRow, labelSetAllAssignmentsToPoolFromSelectedRow);

		panelSWxLicensepool.setDeleteAllowed(false);

		initDataStructure();
		initButtonsAndLabels();

		JPanel panel = createPanel(createPanelAction());

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("FSoftwarename2LicensePool.title"));
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);
	}

	private void initButtonsAndLabels() {
		buttonRemoveAllAssignments = new JButton(Icons.getIntellijIcon("remove"));
		labelRemoveAllAssignments = new JLabel(
				Configed.getResourceValue("FSoftwarename2LicensePool.labelRemoveAllAssignments"));
		buttonRemoveAllAssignments.addActionListener(
				actionEvent -> panelSWxLicensepool.setDataChanged(setSWxColTo(VALUE_NO_LICENSE_POOL)));
		buttonRemoveAllAssignments.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());

		buttonSetAllAssignmentsToGloballySelectedPool = new JButton(Icons.getIntellijIcon("add"));
		buttonSetAllAssignmentsToGloballySelectedPool.setEnabled(false);
		labelSetAllAssignmentsToGloballySelectedPool = new JLabel(
				Configed.getResourceValue("FSoftwarename2LicensePool.labelSetAllAssignmentsToGloballySelectedPool"));
		buttonSetAllAssignmentsToGloballySelectedPool
				.addActionListener(actionEvent -> panelSWxLicensepool.setDataChanged(setSWxColTo(globalLicensePool)));

		buttonSetAllAssignmentsToPoolFromSelectedRow.setIcon(Icons.getIntellijIcon("add"));
		buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
		labelSetAllAssignmentsToPoolFromSelectedRow.setText(
				Configed.getResourceValue("FSoftwarename2LicensePool.labelSetAllAssignmentsToPoolFromSelectedRow"));
		buttonSetAllAssignmentsToPoolFromSelectedRow.addActionListener(
				actionEvent -> panelSWxLicensepool.setDataChanged(setSWxColTo((String) panelSWxLicensepool
						.getValueAt(panelSWxLicensepool.getGenEditTable().getSelectedRow(), 1))));
	}

	private JPanel createPanelAction() {
		JPanel panelAction = new JPanel();
		panelAction.setLayout(new MigLayout("insets " + Globals.GAP_SIZE + ", fillx, wrap 2", "", "[]0"));

		panelAction.add(buttonRemoveAllAssignments, "aligny center");
		panelAction.add(labelRemoveAllAssignments, "aligny center");

		panelAction.add(buttonSetAllAssignmentsToGloballySelectedPool, "aligny center");
		panelAction.add(labelSetAllAssignmentsToGloballySelectedPool, "aligny center");

		panelAction.add(buttonSetAllAssignmentsToPoolFromSelectedRow, "aligny center");
		panelAction.add(labelSetAllAssignmentsToPoolFromSelectedRow, "aligny center");

		return panelAction;
	}

	private JPanel createPanel(JPanel panelAction) {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("insets 0, fill, wrap 1", "[grow, fill]",
				"[200!]" + Globals.GAP_SIZE + "[200!]" + Globals.GAP_SIZE + "[pref]"));
		panel.add(panelSWnames, "grow");
		panel.add(panelSWxLicensepool, "grow");
		panel.add(panelAction, "growx");

		return panel;
	}

	private void initDataStructure() {
		columnNames = new ArrayList<>(SWAuditEntry.ID_VARIANTS_COLS);

		updateCollection = new ArrayList<>();

		columnNamesSWxLicensepool = new ArrayList<>();
		columnNamesSWxLicensepool.add(AuditSoftwareXLicensePool.SW_ID);
		columnNamesSWxLicensepool.add(LicensepoolEntry.ID_SERVICE_KEY);

		showOnlyNamesWithVariantLicenses = new DefaultTableModelFilterCondition(
				"" + Softwarename2LicensepoolRestriction.SHOW_ONLY_NAMES_WITH_VARIANT_LICENSEPOOLS) {
			@Override
			public void setFilter(Set<Object> filter) {
				/* Should be empty, but is not empty in superclass */}

			@Override
			public boolean test(List<Object> row) {
				return getRangeSWxLicensepool((String) row.get(0)).size() > 1;
			}
		};

		showOnlyNamesWithoutLicenses = new DefaultTableModelFilterCondition(
				"" + Softwarename2LicensepoolRestriction.SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENSEPOOL) {
			@Override
			public void setFilter(Set<Object> filter) {
				/* Should be empty, but is not empty in superclass */}

			@Override
			public boolean test(List<Object> row) {
				return checkExistNamesWithVariantLicensepools((String) row.get(0));
			}
		};

		panelSWnames.getGenEditTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		panelSWnames.addListSelectionListener(this::updateTableModelSWxLicensepool);
	}

	private void updateTableModelSWxLicensepool(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() || dialog.isVisible()) {
			return;
		}

		Logging.info(this, "selectedRow ", panelSWnames.getGenEditTable().getSelectedRow());

		if (panelSWnames.getGenEditTable().getSelectedRow() >= 0) {
			String swName = (String) panelSWnames.getValueAt(panelSWnames.getGenEditTable().getSelectedRow(), 0);

			Logging.info(this, " setTableModelSWxLicensepool for ", swName);

			setTableModelSWxLicensepool(swName);
		}
	}

	private boolean setSWxColTo(String newVal) {
		if (newVal == null) {
			return false;
		}

		for (int i = 0; i < modelSWxLicensepool.getRowCount(); i++) {
			modelSWxLicensepool.setValueAt(newVal, i, 1);
		}

		return !updateCollection.isEmpty();
	}

	public void setTableModel() {
		Logging.info(this, "init modelSWnames");

		this.modelSWnames = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getDataServices().software
								.getInstalledSoftwareName2SWinfoPD();
					}
				})), 0, new int[] {}, panelSWnames, updateCollection, true) {
			@Override
			public void produceRows() {
				super.produceRows();
				Logging.info(this, "producing rows for modelSWnames");
				boolean foundVariantLicensepools = false;
				int i = 0;
				while (!foundVariantLicensepools && i < getRowCount()) {
					foundVariantLicensepools = checkExistNamesWithVariantLicensepools((String) getValueAt(i, 0));
					i++;
				}
				controlPanelAssignToLPools.getTabClient().setDisplaySimilarExist(foundVariantLicensepools);
			}

			@Override
			public void reset() {
				Logging.info(this, "reset");
				super.reset();
			}
		};

		panelSWnames.setTableModel(this.modelSWnames);
	}

	public void setPreselectionForName2Pool(Softwarename2LicensepoolRestriction val) {
		switch (val) {
		case SHOW_ALL_NAMES -> modelSWnames.clearFilter();
		case SHOW_ONLY_NAMES_WITH_VARIANT_LICENSEPOOLS -> modelSWnames
				.setFilterCondition(showOnlyNamesWithVariantLicenses);
		case SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENSEPOOL -> modelSWnames
				.setFilterCondition(showOnlyNamesWithoutLicenses);
		}

		modelSWnames.reset();

		Logging.info(this, "setPreselectionForName2Pool, we did a reset for modelSWnames with ", val);
	}

	private Set<String> getRangeSWxLicensepool(String swName) {
		// nearly done in produceModelSWxLicensepool, but we collect the range of the
		// model-map

		Set<String> range = new HashSet<>();

		for (String swID : persistenceController.getDataServices().software.getName2SWIdentsPD().get(swName)) {
			String licpool = persistenceController.getDataServices().software.getFSoftware2LicensePoolPD(swID);

			if (licpool == null) {
				range.add(VALUE_NO_LICENSE_POOL);
			} else {
				range.add(licpool);
			}
		}

		return range;
	}

	private boolean checkExistNamesWithVariantLicensepools(String name) {
		Set<String> range = getRangeSWxLicensepool(name);

		return range.size() == 1 && range.contains(VALUE_NO_LICENSE_POOL);
	}

	public boolean checkExistNamesWithVariantLicensepools() {
		if (modelSWnames == null) {
			return false;
		}

		boolean foundVariants = false;
		int i = 0;
		while (!foundVariants && i < modelSWnames.getRowCount()) {
			foundVariants = checkExistNamesWithVariantLicensepools((String) modelSWnames.getValueAt(i, 0));
			i++;
		}

		return foundVariants;
	}

	public void setGlobalPool(String licensePool) {
		this.globalLicensePool = licensePool;
		String labelText = Configed
				.getResourceValue("FSoftwarename2LicensePool.labelSetAllAssignmentsToGloballySelectedPool");
		boolean buttonActive = false;
		if (licensePool != null && !licensePool.isEmpty()) {
			labelText = labelText + " " + licensePool;
			buttonActive = true;
		}

		Logging.info(this, "setGlobalPool  labelSetAllAssignmentsToGloballySelectedPool", labelText);
		labelSetAllAssignmentsToGloballySelectedPool.setText(labelText);
		buttonSetAllAssignmentsToGloballySelectedPool
				.setEnabled(buttonActive && !persistenceController.getDataServices().userRoles.isGlobalReadOnly());
	}

	private Map<String, Map<String, Object>> produceModelSWxLicensepool(String swName) {
		Logging.info(this, "produceModelSWxLicensepool for swName: ", swName);

		Map<String, Map<String, Object>> result = new TreeMap<>();

		for (String swID : persistenceController.getDataServices().software.getName2SWIdentsPD().get(swName)) {
			Map<String, Object> rowMap = new HashMap<>();
			rowMap.put(AuditSoftwareXLicensePool.SW_ID, swID);
			String licpool = persistenceController.getDataServices().software.getFSoftware2LicensePoolPD(swID);

			if (licpool == null) {
				rowMap.put(LicensepoolEntry.ID_SERVICE_KEY, VALUE_NO_LICENSE_POOL);
			} else {
				rowMap.put(LicensepoolEntry.ID_SERVICE_KEY, licpool);
			}

			result.put(swID, rowMap);
		}

		Logging.info(this, "produceModelSWxLicensepool for swName: ", swName, ": ", result);

		return result;
	}

	private void setTableModelSWxLicensepool(String swName) {
		Logging.info(this, " setTableModelSWxLicensepool for ", swName, " with cols ", columnNamesSWxLicensepool);

		MapTableUpdateItemFactory updateItemFactoySWxLicensepool = new MapTableUpdateItemFactory(
				columnNamesSWxLicensepool);

		modelSWxLicensepool = new GenTableModel(updateItemFactoySWxLicensepool,
				new DefaultTableProvider(new RetrieverMapSource(columnNamesSWxLicensepool, new MapRetriever() {
					@Override
					public void reloadMap() {
						Logging.info(this, "retrieveMap for swName ", swName);
						persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return produceModelSWxLicensepool(swName);
					}
				})), 0, new int[] {}, panelSWnames, updateCollection);
		updateItemFactoySWxLicensepool.setSource(modelSWxLicensepool);
		Logging.info(this, "setTableModelSWxLicensepool, we reset the model");
		modelSWxLicensepool.reset();

		panelSWxLicensepool.setTableModel(modelSWxLicensepool);

		// updates
		panelSWxLicensepool.setUpdateController(
				new MapItemsUpdateController(panelSWxLicensepool, modelSWxLicensepool, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						Logging.info(this, "sendUpdate ", rowmap);

						// reloads local data (which are not yet updated)
						String swID = (String) rowmap.get(AuditSoftwareXLicensePool.SW_ID);
						String licensePoolIDOld = persistenceController.getDataServices().software
								.getFSoftware2LicensePoolPD(swID);
						String licensePoolIDNew = (String) rowmap.get(LicensepoolEntry.ID_SERVICE_KEY);

						return persistenceController.getDataServices().software.editPool2AuditSoftware(swID,
								licensePoolIDOld, licensePoolIDNew);
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						Logging.info(this, "sendDelete ", rowmap);
						// deleting not activated in panel

						return false;
					}
				}, updateCollection));
	}

	public PanelGenEdit getPanelSWnames() {
		return panelSWnames;
	}

	public GenTableModel getModelSWnames() {
		return modelSWnames;
	}

	public PanelGenEdit getPanelSWxLicensepool() {
		return panelSWxLicensepool;
	}

	public GenTableModel getModelSWxLicensepool() {
		return modelSWxLicensepool;
	}
}
