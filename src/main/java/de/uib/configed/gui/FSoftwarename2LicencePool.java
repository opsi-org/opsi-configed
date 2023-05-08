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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
/**
 * FSoftwarename2LicencePool
 * Copyright:     Copyright (c) 2020
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.Globals;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.DefaultTableModelFilterCondition;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class FSoftwarename2LicencePool extends FDialogSubTable {
	public static final String VALUE_NO_LICENCE_POOL = "---";
	public PanelGenEditTable panelSWnames;
	public GenTableModel modelSWnames;

	private List<String> columnNames;
	private List<String> classNames;

	public PanelGenEditTable panelSWxLicencepool;
	public GenTableModel modelSWxLicencepool;
	private List<String> columnNamesSWxLicencepool;
	private List<String> classNamesSWxLicencepool;

	private TableModelFilterCondition showOnlyNamesWithVariantLicences;
	private TableModelFilterCondition showOnlyNamesWithoutLicences;

	private TableUpdateCollection updateCollection;

	protected int keyCol;

	private AbstractPersistenceController persist;

	private ControlPanelAssignToLPools myController;

	public enum Softwarename2LicencepoolRestriction {
		SHOW_ALL_NAMES, SHOW_ONLY_NAMES_WITH_VARIANT_LICENCEPOOLS, SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENCEPOOL
	}

	public enum Softwarename2LicencepoolChangeOption {
		NO_CHANGE, REMOVE_ALL_ASSIGNEMENTS, SET_ALL_TO_GLOBAL_SELECTED_LICENCEPOOL, SET_ALL_TO_SELECTED_LINE
	}

	JButton buttonRemoveAllAssignments;
	JLabel labelRemoveAllAssignments;
	JButton buttonSetAllAssignmentsToGloballySelectedPool;
	JLabel labelSetAllAssignmentsToGloballySelectedPool;
	JButton buttonSetAllAssignmentsToPoolFromSelectedRow;
	JLabel labelSetAllAssignmentsToPoolFromSelectedRow;

	protected String globalLicencePool;

	boolean foundVariantLicencepools;

	public FSoftwarename2LicencePool(JFrame owner, ControlPanelAssignToLPools myController) {
		super(

				owner, Configed.getResourceValue("FSoftwarename2LicencePool.title"), false, new String[] {

						Configed.getResourceValue("FSoftwarename2LicencePool.buttonClose") },
				new Icon[] {

						Globals.createImageIcon("images/cancel16_small.png", "") },
				1, 700, 800);

		this.myController = myController;
		persist = PersistenceControllerFactory.getPersistenceController();

		panelSWnames = new PanelGenEditTable("",

				0, false, // editing,
				0, true // switchLineColors
				, new int[] { PanelGenEditTable.POPUP_RELOAD },
				// , PanelGenEditTable.POPUPS_NOT_EDITABLE_TABLE_PDF,
				true // searchpane
		) {
			@Override
			public void setDataChanged(boolean b) {
				Logging.info(this, "panelSWNames setDataChanged " + b);
				super.setDataChanged(b);
			}
		};

		panelSWxLicencepool = new PanelGenEditTable("",

				0, // width
				true, // editing,
				0, true // switchLineColors
				, new int[] { PanelGenEditTable.POPUP_RELOAD },

				false // searchpane
		) {
			@Override
			public void setDataChanged(boolean b) {
				Logging.info(this, "panelSWxLicencepool setDataChanged " + b);
				super.setDataChanged(b);
			}

			@Override
			public void valueChanged(ListSelectionEvent e) {
				Logging.info(this, "panelSWxLicencepool ListSelectionEvent " + e);
				super.valueChanged(e);

				String labelText = Configed
						.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToPoolFromSelectedRow");

				Object val = null;
				int selRow = getSelectedRow();
				if (selRow > -1) {
					val = getValueAt(selRow, 1);
				}

				if (val != null && isSingleSelection() && getTableModel().getRowCount() > 1
						&& !((String) val).equals(VALUE_NO_LICENCE_POOL)) {
					buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(true);
					labelSetAllAssignmentsToPoolFromSelectedRow
							.setText(labelText + " " + getValueAt(getSelectedRow(), 1));
				} else {
					buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
					labelSetAllAssignmentsToPoolFromSelectedRow.setText(labelText);
					Configed.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToPoolFromSelectedRow");
				}

			}
		};

		panelSWxLicencepool.setDeleteAllowed(false);

		owner.setVisible(true);

		initDataStructure();

		buttonRemoveAllAssignments = new JButton();
		buttonRemoveAllAssignments.setIcon(Globals.createImageIcon("images/list-remove-14.png", ""));
		buttonRemoveAllAssignments.setPreferredSize(Globals.shortButtonDimension);
		labelRemoveAllAssignments = new JLabel(
				Configed.getResourceValue("FSoftwarename2LicencePool.labelRemoveAllAssignments"));
		buttonRemoveAllAssignments.addActionListener(
				actionEvent -> panelSWxLicencepool.setDataChanged(setSWxColTo(VALUE_NO_LICENCE_POOL)));

		buttonSetAllAssignmentsToGloballySelectedPool = new JButton();
		buttonSetAllAssignmentsToGloballySelectedPool.setEnabled(false);
		buttonSetAllAssignmentsToGloballySelectedPool.setPreferredSize(Globals.shortButtonDimension);
		buttonSetAllAssignmentsToGloballySelectedPool.setIcon(Globals.createImageIcon("images/list-add-14.png", ""));
		labelSetAllAssignmentsToGloballySelectedPool = new JLabel(
				Configed.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToGloballySelectedPool"));
		buttonSetAllAssignmentsToGloballySelectedPool
				.addActionListener(actionEvent -> panelSWxLicencepool.setDataChanged(setSWxColTo(globalLicencePool)));

		buttonSetAllAssignmentsToPoolFromSelectedRow = new JButton();
		buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
		buttonSetAllAssignmentsToPoolFromSelectedRow.setPreferredSize(Globals.shortButtonDimension);
		buttonSetAllAssignmentsToPoolFromSelectedRow.setIcon(Globals.createImageIcon("images/list-add-14.png", ""));
		labelSetAllAssignmentsToPoolFromSelectedRow = new JLabel(
				Configed.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToPoolFromSelectedRow")); // assign
																																												// each
																																												// to
																																												// pool
																																												// from
																																												// selected
																																												// row:
																																												// ")

		buttonSetAllAssignmentsToPoolFromSelectedRow
				.addActionListener(actionEvent -> panelSWxLicencepool.setDataChanged(
						setSWxColTo((String) panelSWxLicencepool.getValueAt(panelSWxLicencepool.getSelectedRow(), 1))));

		JPanel panelAction = new JPanel();
		if (!ConfigedMain.THEMES) {
			panelAction.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

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
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
										.addComponent(buttonRemoveAllAssignments, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.HGAP_SIZE)
										.addComponent(
												labelRemoveAllAssignments, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.HGAP_SIZE))
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
										.addComponent(buttonSetAllAssignmentsToGloballySelectedPool,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.HGAP_SIZE)
										.addComponent(labelSetAllAssignmentsToGloballySelectedPool,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.HGAP_SIZE))
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
										.addComponent(buttonSetAllAssignmentsToPoolFromSelectedRow,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.HGAP_SIZE)
										.addComponent(labelSetAllAssignmentsToPoolFromSelectedRow,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.HGAP_SIZE)));

		JPanel panelSWx = new JPanel();
		GroupLayout layoutSWx = new GroupLayout(panelSWx);
		panelSWx.setLayout(layoutSWx);

		layoutSWx.setVerticalGroup(layoutSWx.createSequentialGroup().addGap(Globals.VGAP_SIZE)
				.addComponent(panelSWxLicencepool, 100, 200, Short.MAX_VALUE).addGap(Globals.VGAP_SIZE)
				.addComponent(panelAction, 70, 70, 100).addGap(Globals.VGAP_SIZE)

		);

		layoutSWx.setHorizontalGroup(layoutSWx.createParallelGroup()
				.addGroup(layoutSWx.createSequentialGroup().addGap(Globals.HGAP_SIZE)
						.addComponent(panelAction, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE))
				.addGroup(layoutSWx
						.createSequentialGroup().addGap(Globals.HGAP_SIZE).addComponent(panelSWxLicencepool,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE)));

		super.setAdditionalPane(panelSWx);

		if (!ConfigedMain.THEMES) {
			additionalPane.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

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

		updateCollection = new TableUpdateCollection();

		columnNamesSWxLicencepool = new ArrayList<>();
		columnNamesSWxLicencepool.add(AuditSoftwareXLicencePool.SW_ID);
		columnNamesSWxLicencepool.add(LicencepoolEntry.ID_SERVICE_KEY);

		classNamesSWxLicencepool = new ArrayList<>();
		for (int i = 0; i < columnNamesSWxLicencepool.size(); i++) {
			classNamesSWxLicencepool.add("java.lang.String");
		}

		showOnlyNamesWithVariantLicences = new DefaultTableModelFilterCondition(
				"" + Softwarename2LicencepoolRestriction.SHOW_ONLY_NAMES_WITH_VARIANT_LICENCEPOOLS) {
			@Override
			public void setFilter(Set<Object> filter) {
				/* Should be empty, but is not empty in superclass */}

			@Override
			public boolean test(List<Object> row) {

				return getRangeSWxLicencepool((String) row.get(0)).size() > 1;
			}
		};

		showOnlyNamesWithoutLicences = new DefaultTableModelFilterCondition(
				"" + Softwarename2LicencepoolRestriction.SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENCEPOOL) {

			@Override
			public void setFilter(Set<Object> filter) {
				/* Should be empty, but is not empty in superclass */}

			@Override
			public boolean test(List<Object> row) {

				return checkExistNamesWithVariantLicencepools((String) row.get(0));
			}
		};

		panelSWnames.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		panelSWnames.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}

				Logging.info(this, "selectedRow " + panelSWnames.getSelectedRow());

				if (panelSWnames.getSelectedRow() >= 0) {
					String swName = (String) panelSWnames.getValueAt(panelSWnames.getSelectedRow(), 0);

					Logging.info(this, " setTableModelSWxLicencepool for " + swName);

					setTableModelSWxLicencepool(swName);

				}
			}
		});
	}

	private boolean setSWxColTo(String newVal) {
		if (newVal == null) {
			return false;
		}

		for (int i = 0; i < modelSWxLicencepool.getRowCount(); i++) {
			modelSWxLicencepool.setValueAt(newVal, i, 1);
		}

		return !updateCollection.isEmpty();

	}

	private void setSWInfo(String swId, String pool) {
		Logging.info(this, " setSWInfo for " + swId + " pool " + pool);
		Logging.info(this, " setSWInfo in " + persist.getInstalledSoftwareName2SWinfo()
				.get(AuditSoftwareXLicencePool.produceMapFromSWident(swId).get(SWAuditEntry.NAME)));
	}

	public void setTableModel(GenTableModel model) {
		if (model == null) {
			// TODO can this be removed inside if? There was a comment "test" here

			Logging.info(this, "init modelSWnames");

			this.modelSWnames = new GenTableModel(null,
					new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames,
							() -> (Map) persist.getInstalledSoftwareName2SWinfo())),
					0, new int[] {}, panelSWnames, updateCollection) {

				@Override
				public void produceRows() {
					super.produceRows();
					Logging.info(this, "producing rows for modelSWnames");
					foundVariantLicencepools = false;
					int i = 0;
					while (!foundVariantLicencepools && i < getRowCount()) {
						foundVariantLicencepools = checkExistNamesWithVariantLicencepools((String) getValueAt(i, 0));
						i++;
					}
					myController.thePanel.setDisplaySimilarExist(foundVariantLicencepools);
				}

				@Override
				public void reset() {
					Logging.info(this, "reset");
					super.reset();
				}
			};

		} else {
			Logging.info(this, "set modelSWnames");
			this.modelSWnames = model;
		}

		panelSWnames.setTableModel(this.modelSWnames);

	}

	public void setPreselectionForName2Pool(Softwarename2LicencepoolRestriction val) {

		switch (val) {
		case SHOW_ALL_NAMES:
			modelSWnames.clearFilter();
			break;
		case SHOW_ONLY_NAMES_WITH_VARIANT_LICENCEPOOLS:
			modelSWnames.setFilterCondition(showOnlyNamesWithVariantLicences);
			break;
		case SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENCEPOOL:
			modelSWnames.setFilterCondition(showOnlyNamesWithoutLicences);
			break;
		}

		modelSWnames.reset();

		Logging.info(this, "setPreselectionForName2Pool, we did a reset for modelSWnames with " + val);
	}

	private Set<String> getRangeSWxLicencepool(String swName) {
		// nearly done in produceModelSWxLicencepool, but we collect the range of the
		// model-map

		Set<String> range = new HashSet<>();

		for (String swID : persist.getName2SWIdents().get(swName)) {
			String licpool = persist.getFSoftware2LicencePool(swID);

			if (licpool == null) {
				range.add(VALUE_NO_LICENCE_POOL);
			} else {
				range.add(licpool);
			}
		}

		return range;
	}

	private boolean checkExistNamesWithVariantLicencepools(String name) {
		Set<String> range = getRangeSWxLicencepool(name);

		return range.size() == 1 && range.contains(VALUE_NO_LICENCE_POOL);
	}

	public boolean checkExistNamesWithVariantLicencepools() {
		if (modelSWnames == null) {
			return false;
		}

		boolean foundVariants = false;
		int i = 0;
		while (!foundVariants && i < modelSWnames.getRowCount()) {
			foundVariants = checkExistNamesWithVariantLicencepools((String) modelSWnames.getValueAt(i, 0));
			i++;
		}

		return foundVariants;
	}

	public void setGlobalPool(String licencePool) {
		this.globalLicencePool = licencePool;
		String labelText = Configed
				.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToGloballySelectedPool");
		boolean buttonActive = false;
		if (licencePool != null && !licencePool.isEmpty()) {
			labelText = labelText + " " + licencePool;
			buttonActive = true;
		}

		Logging.info(this, "setGlobalPool  labelSetAllAssignmentsToGloballySelectedPool" + labelText);
		labelSetAllAssignmentsToGloballySelectedPool.setText(labelText);
		buttonSetAllAssignmentsToGloballySelectedPool.setEnabled(buttonActive);
	}

	public String getGlobalLicencePool() {
		return globalLicencePool;
	}

	private Map<String, Map<String, String>> produceModelSWxLicencepool(String swName) {
		Logging.info(this, "produceModelSWxLicencepool for swName: " + swName);

		TreeMap<String, Map<String, String>> result = new TreeMap<>();

		for (String swID : persist.getName2SWIdents().get(swName)) {
			LinkedHashMap<String, String> rowMap = new LinkedHashMap<>();
			rowMap.put(AuditSoftwareXLicencePool.SW_ID, swID);
			String licpool = persist.getFSoftware2LicencePool(swID);

			if (licpool == null) {
				rowMap.put(LicencepoolEntry.ID_SERVICE_KEY, VALUE_NO_LICENCE_POOL);
			} else {
				rowMap.put(LicencepoolEntry.ID_SERVICE_KEY, licpool);
			}

			result.put(swID, rowMap);
		}

		Logging.info(this, "produceModelSWxLicencepool for swName: " + swName + ": " + result);

		return result;
	}

	private void setTableModelSWxLicencepool(String swName) {
		Logging.info(this, " setTableModelSWxLicencepool for " + swName + " with cols " + columnNamesSWxLicencepool
				+ " and classes " + classNamesSWxLicencepool);

		MapTableUpdateItemFactory updateItemFactoySWxLicencepool = new MapTableUpdateItemFactory(
				columnNamesSWxLicencepool, classNamesSWxLicencepool, 0 // keycol
		);

		modelSWxLicencepool = new GenTableModel(updateItemFactoySWxLicencepool, new DefaultTableProvider(
				new RetrieverMapSource(columnNamesSWxLicencepool, classNamesSWxLicencepool, new MapRetriever() {

					@Override
					public Map retrieveMap() {
						Logging.info(this, "retrieveMap for swName " + swName);
						return produceModelSWxLicencepool(swName);
					}
				})),

				keyCol, new int[] {}, panelSWnames, updateCollection);
		updateItemFactoySWxLicencepool.setSource(modelSWxLicencepool);
		Logging.info(this, "setTableModelSWxLicencepool, we reset the model");
		modelSWxLicencepool.reset();

		panelSWxLicencepool.setTableModel(modelSWxLicencepool);

		// updates
		panelSWxLicencepool.setUpdateController(
				new MapItemsUpdateController(panelSWxLicencepool, modelSWxLicencepool, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						Logging.info(this, "sendUpdate " + rowmap);

						// reloads local data (which are not yet updated)
						String swID = (String) rowmap.get(AuditSoftwareXLicencePool.SW_ID);
						String licensePoolIDOld = persist.getFSoftware2LicencePool(swID);
						String licensePoolIDNew = (String) rowmap.get(LicencepoolEntry.ID_SERVICE_KEY);

						if (!VALUE_NO_LICENCE_POOL.equals(licensePoolIDNew)) {
							setSWInfo(swID, licensePoolIDNew);
						}

						return persist.editPool2AuditSoftware(swID, licensePoolIDOld, licensePoolIDNew);

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

	public void exit() {
		super.leave();
	}

}
