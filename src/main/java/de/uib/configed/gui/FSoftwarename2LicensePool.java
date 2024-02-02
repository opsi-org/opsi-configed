/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licenses.AuditSoftwareXLicensePool;
import de.uib.configed.type.licenses.LicensepoolEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.DefaultTableModelFilterCondition;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedTableEditItem;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import utils.Utils;

public class FSoftwarename2LicensePool extends FDialogSubTable {
	public static final String VALUE_NO_LICENSE_POOL = "---";
	private PanelGenEditTable panelSWnames;
	private GenTableModel modelSWnames;

	private List<String> columnNames;
	private List<String> classNames;

	private PanelGenEditTable panelSWxLicensepool;
	private GenTableModel modelSWxLicensepool;
	private List<String> columnNamesSWxLicensepool;
	private List<String> classNamesSWxLicensepool;

	private TableModelFilterCondition showOnlyNamesWithVariantLicenses;
	private TableModelFilterCondition showOnlyNamesWithoutLicenses;

	private List<MapBasedTableEditItem> updateCollection;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ControlPanelAssignToLPools myController;

	public enum Softwarename2LicensepoolRestriction {
		SHOW_ALL_NAMES, SHOW_ONLY_NAMES_WITH_VARIANT_LICENSEPOOLS, SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENSEPOOL
	}

	private JButton buttonSetAllAssignmentsToGloballySelectedPool;
	private JLabel labelSetAllAssignmentsToGloballySelectedPool;
	private JButton buttonSetAllAssignmentsToPoolFromSelectedRow;
	private JLabel labelSetAllAssignmentsToPoolFromSelectedRow;

	private String globalLicensePool;

	private boolean foundVariantLicensepools;

	private ConfigedMain configedMain;

	public FSoftwarename2LicensePool(ControlPanelAssignToLPools myController, ConfigedMain configedMain) {
		super(ConfigedMain.getLicensesFrame(), Configed.getResourceValue("FSoftwarename2LicensePool.title"), false,
				new String[] { Configed.getResourceValue("buttonClose") }, 1, 700, 800);

		this.myController = myController;
		this.configedMain = configedMain;

		panelSWnames = new PanelGenEditTable("", false, 0, new int[] { PanelGenEditTable.POPUP_RELOAD }, true) {
			@Override
			public void setDataChanged(boolean b) {
				Logging.info(this, "panelSWNames setDataChanged " + b);
				super.setDataChanged(b);
			}
		};

		panelSWxLicensepool = new PanelGenEditTable("", true, 0, new int[] { PanelGenEditTable.POPUP_RELOAD }, false) {
			@Override
			public void setDataChanged(boolean b) {
				Logging.info(this, "panelSWxLicensepool setDataChanged " + b);
				super.setDataChanged(b);
			}

			@Override
			public void valueChanged(ListSelectionEvent e) {
				Logging.info(this, "panelSWxLicensepool ListSelectionEvent " + e);
				super.valueChanged(e);

				String labelText = Configed
						.getResourceValue("FSoftwarename2LicensePool.labelSetAllAssignmentsToPoolFromSelectedRow");

				Object val = null;
				int selRow = getSelectedRow();
				if (selRow > -1) {
					val = getValueAt(selRow, 1);
				}

				if (val != null && isSingleSelection() && getTableModel().getRowCount() > 1
						&& !((String) val).equals(VALUE_NO_LICENSE_POOL)) {
					buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(true);
					labelSetAllAssignmentsToPoolFromSelectedRow
							.setText(labelText + " " + getValueAt(getSelectedRow(), 1));
				} else {
					buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
					labelSetAllAssignmentsToPoolFromSelectedRow.setText(labelText);
				}
			}
		};

		panelSWxLicensepool.setDeleteAllowed(false);

		owner.setVisible(true);

		initDataStructure();

		initLayout();
	}

	private void initLayout() {
		JButton buttonRemoveAllAssignments = new JButton(Utils.createImageIcon("images/list-remove-14.png", ""));
		buttonRemoveAllAssignments.setPreferredSize(Globals.SHORT_BUTTON_DIMENSION);
		JLabel labelRemoveAllAssignments = new JLabel(
				Configed.getResourceValue("FSoftwarename2LicensePool.labelRemoveAllAssignments"));
		buttonRemoveAllAssignments.addActionListener(
				actionEvent -> panelSWxLicensepool.setDataChanged(setSWxColTo(VALUE_NO_LICENSE_POOL)));

		buttonSetAllAssignmentsToGloballySelectedPool = new JButton(
				Utils.createImageIcon("images/list-add-14.png", ""));
		buttonSetAllAssignmentsToGloballySelectedPool.setEnabled(false);
		buttonSetAllAssignmentsToGloballySelectedPool.setPreferredSize(Globals.SHORT_BUTTON_DIMENSION);
		labelSetAllAssignmentsToGloballySelectedPool = new JLabel(
				Configed.getResourceValue("FSoftwarename2LicensePool.labelSetAllAssignmentsToGloballySelectedPool"));
		buttonSetAllAssignmentsToGloballySelectedPool
				.addActionListener(actionEvent -> panelSWxLicensepool.setDataChanged(setSWxColTo(globalLicensePool)));

		buttonSetAllAssignmentsToPoolFromSelectedRow = new JButton(Utils.createImageIcon("images/list-add-14.png", ""));
		buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
		buttonSetAllAssignmentsToPoolFromSelectedRow.setPreferredSize(Globals.SHORT_BUTTON_DIMENSION);
		labelSetAllAssignmentsToPoolFromSelectedRow = new JLabel(
				Configed.getResourceValue("FSoftwarename2LicensePool.labelSetAllAssignmentsToPoolFromSelectedRow")); // assign

		buttonSetAllAssignmentsToPoolFromSelectedRow
				.addActionListener(actionEvent -> panelSWxLicensepool.setDataChanged(
						setSWxColTo((String) panelSWxLicensepool.getValueAt(panelSWxLicensepool.getSelectedRow(), 1))));

		JPanel panelAction = new JPanel();

		GroupLayout panelActionLayout = new GroupLayout(panelAction);
		panelAction.setLayout(panelActionLayout);
		panelActionLayout.setVerticalGroup(panelActionLayout.createSequentialGroup()
				.addGroup(panelActionLayout.createParallelGroup(Alignment.CENTER)
						.addComponent(buttonRemoveAllAssignments, 5, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelRemoveAllAssignments, 5, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(panelActionLayout.createParallelGroup(Alignment.CENTER)
						.addComponent(buttonSetAllAssignmentsToGloballySelectedPool, 5, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSetAllAssignmentsToGloballySelectedPool, 5, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(panelActionLayout.createParallelGroup(Alignment.CENTER)
						.addComponent(buttonSetAllAssignmentsToPoolFromSelectedRow, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSetAllAssignmentsToPoolFromSelectedRow, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		panelActionLayout
				.setHorizontalGroup(
						panelActionLayout.createParallelGroup()
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(buttonRemoveAllAssignments, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(
												labelRemoveAllAssignments, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE))
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(buttonSetAllAssignmentsToGloballySelectedPool,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(labelSetAllAssignmentsToGloballySelectedPool,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE))
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(buttonSetAllAssignmentsToPoolFromSelectedRow,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(labelSetAllAssignmentsToPoolFromSelectedRow,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE)));

		JPanel panelSWx = new JPanel();
		GroupLayout layoutSWx = new GroupLayout(panelSWx);
		panelSWx.setLayout(layoutSWx);

		layoutSWx.setVerticalGroup(layoutSWx.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(panelSWxLicensepool, 100, 200, Short.MAX_VALUE).addGap(Globals.GAP_SIZE)
				.addComponent(panelAction, 70, 70, 100).addGap(Globals.GAP_SIZE));

		layoutSWx.setHorizontalGroup(layoutSWx.createParallelGroup()
				.addGroup(layoutSWx.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(panelAction, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE))
				.addGroup(layoutSWx
						.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(panelSWxLicensepool,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE)));

		super.setAdditionalPane(panelSWx);

		super.setCenterPane(panelSWnames);
		additionalPane.setVisible(true);

		super.setupLayout();
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	private void initDataStructure() {
		columnNames = new ArrayList<>();
		for (String key : SWAuditEntry.ID_VARIANTS_COLS) {
			columnNames.add(key);
		}

		classNames = new ArrayList<>();
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		updateCollection = new ArrayList<>();

		columnNamesSWxLicensepool = new ArrayList<>();
		columnNamesSWxLicensepool.add(AuditSoftwareXLicensePool.SW_ID);
		columnNamesSWxLicensepool.add(LicensepoolEntry.ID_SERVICE_KEY);

		classNamesSWxLicensepool = new ArrayList<>();
		for (int i = 0; i < columnNamesSWxLicensepool.size(); i++) {
			classNamesSWxLicensepool.add("java.lang.String");
		}

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

		panelSWnames.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		panelSWnames.addListSelectionListener((ListSelectionEvent e) -> {
			if (!e.getValueIsAdjusting()) {
				Logging.info(this, "selectedRow " + panelSWnames.getSelectedRow());

				if (panelSWnames.getSelectedRow() >= 0) {
					String swName = (String) panelSWnames.getValueAt(panelSWnames.getSelectedRow(), 0);

					Logging.info(this, " setTableModelSWxLicensepool for " + swName);

					setTableModelSWxLicensepool(swName);
				}
			}
		});
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
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!configedMain.isAllLicenseDataReloaded()) {
							persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getSoftwareDataService().getInstalledSoftwareName2SWinfoPD();
					}
				})), 0, new int[] {}, panelSWnames, updateCollection, true) {
			@Override
			public void produceRows() {
				super.produceRows();
				Logging.info(this, "producing rows for modelSWnames");
				foundVariantLicensepools = false;
				int i = 0;
				while (!foundVariantLicensepools && i < getRowCount()) {
					foundVariantLicensepools = checkExistNamesWithVariantLicensepools((String) getValueAt(i, 0));
					i++;
				}
				myController.getTabClient().setDisplaySimilarExist(foundVariantLicensepools);
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
		case SHOW_ALL_NAMES:
			modelSWnames.clearFilter();
			break;
		case SHOW_ONLY_NAMES_WITH_VARIANT_LICENSEPOOLS:
			modelSWnames.setFilterCondition(showOnlyNamesWithVariantLicenses);
			break;
		case SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENSEPOOL:
			modelSWnames.setFilterCondition(showOnlyNamesWithoutLicenses);
			break;
		}

		modelSWnames.reset();

		Logging.info(this, "setPreselectionForName2Pool, we did a reset for modelSWnames with " + val);
	}

	private Set<String> getRangeSWxLicensepool(String swName) {
		// nearly done in produceModelSWxLicensepool, but we collect the range of the
		// model-map

		Set<String> range = new HashSet<>();

		for (String swID : persistenceController.getSoftwareDataService().getName2SWIdentsPD().get(swName)) {
			String licpool = persistenceController.getSoftwareDataService().getFSoftware2LicensePoolPD(swID);

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

		Logging.info(this, "setGlobalPool  labelSetAllAssignmentsToGloballySelectedPool" + labelText);
		labelSetAllAssignmentsToGloballySelectedPool.setText(labelText);
		buttonSetAllAssignmentsToGloballySelectedPool.setEnabled(buttonActive);
	}

	private Map<String, Map<String, String>> produceModelSWxLicensepool(String swName) {
		Logging.info(this, "produceModelSWxLicensepool for swName: " + swName);

		TreeMap<String, Map<String, String>> result = new TreeMap<>();

		for (String swID : persistenceController.getSoftwareDataService().getName2SWIdentsPD().get(swName)) {
			Map<String, String> rowMap = new LinkedHashMap<>();
			rowMap.put(AuditSoftwareXLicensePool.SW_ID, swID);
			String licpool = persistenceController.getSoftwareDataService().getFSoftware2LicensePoolPD(swID);

			if (licpool == null) {
				rowMap.put(LicensepoolEntry.ID_SERVICE_KEY, VALUE_NO_LICENSE_POOL);
			} else {
				rowMap.put(LicensepoolEntry.ID_SERVICE_KEY, licpool);
			}

			result.put(swID, rowMap);
		}

		Logging.info(this, "produceModelSWxLicensepool for swName: " + swName + ": " + result);

		return result;
	}

	private void setTableModelSWxLicensepool(String swName) {
		Logging.info(this, " setTableModelSWxLicensepool for " + swName + " with cols " + columnNamesSWxLicensepool
				+ " and classes " + classNamesSWxLicensepool);

		MapTableUpdateItemFactory updateItemFactoySWxLicensepool = new MapTableUpdateItemFactory(
				columnNamesSWxLicensepool);

		if (modelSWxLicensepool == null) {
			modelSWxLicensepool = new GenTableModel(updateItemFactoySWxLicensepool, new DefaultTableProvider(
					new RetrieverMapSource(columnNamesSWxLicensepool, classNamesSWxLicensepool, new MapRetriever() {
						@Override
						public void reloadMap() {
							Logging.info(this, "retrieveMap for swName " + swName);
							if (!configedMain.isAllLicenseDataReloaded()) {
								persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
							}
						}

						@Override
						public Map<String, Map<String, Object>> retrieveMap() {
							return (Map) produceModelSWxLicensepool(swName);
						}
					})), 0, new int[] {}, panelSWnames, updateCollection);
		}
		updateItemFactoySWxLicensepool.setSource(modelSWxLicensepool);
		Logging.info(this, "setTableModelSWxLicensepool, we reset the model");
		modelSWxLicensepool.reset();

		panelSWxLicensepool.setTableModel(modelSWxLicensepool);

		// updates
		panelSWxLicensepool.setUpdateController(
				new MapItemsUpdateController(panelSWxLicensepool, modelSWxLicensepool, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						Logging.info(this, "sendUpdate " + rowmap);

						// reloads local data (which are not yet updated)
						String swID = (String) rowmap.get(AuditSoftwareXLicensePool.SW_ID);
						String licensePoolIDOld = persistenceController.getSoftwareDataService()
								.getFSoftware2LicensePoolPD(swID);
						String licensePoolIDNew = (String) rowmap.get(LicensepoolEntry.ID_SERVICE_KEY);

						return persistenceController.getSoftwareDataService().editPool2AuditSoftware(swID,
								licensePoolIDOld, licensePoolIDNew);
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						Logging.info(this, "sendDelete " + rowmap);
						// deleting not activated in panel

						return false;
					}
				}, updateCollection));
	}

	@Override
	public void doAction2() {
		Logging.debug(this, "doAction2");
		result = 2;
		owner.setVisible(true);
		leave();
	}

	@Override
	public void leave() {
		setVisible(false);
		// we dont dispose the window, dispose it in the enclosing class
	}

	public PanelGenEditTable getPanelSWnames() {
		return panelSWnames;
	}

	public GenTableModel getModelSWnames() {
		return modelSWnames;
	}

	public PanelGenEditTable getPanelSWxLicensepool() {
		return panelSWxLicensepool;
	}

	public GenTableModel getModelSWxLicensepool() {
		return modelSWxLicensepool;
	}
}
