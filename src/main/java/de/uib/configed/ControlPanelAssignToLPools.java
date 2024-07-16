/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.FGlobalSoftwareInfo;
import de.uib.configed.gui.FSoftwarename2LicensePool;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.licenses.PanelAssignToLPools;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licenses.LicensepoolEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.DefaultTableModelFilterCondition;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.TableModelFilter;
import de.uib.utils.table.TableModelFilterCondition;
import de.uib.utils.table.gui.AdaptingCellEditor;
import de.uib.utils.table.gui.BooleanIconTableCellRenderer;
import de.uib.utils.table.provider.DefaultTableProvider;
import de.uib.utils.table.provider.MapRetriever;
import de.uib.utils.table.provider.RetrieverMapSource;
import de.uib.utils.table.updates.MapBasedTableEditItem;
import de.uib.utils.table.updates.MapBasedUpdater;
import de.uib.utils.table.updates.MapItemsUpdateController;
import de.uib.utils.table.updates.MapTableUpdateItemFactory;
import de.uib.utils.table.updates.SelectionMemorizerUpdateController;

public class ControlPanelAssignToLPools extends AbstractControlMultiTablePanel {
	private static final int MAX_WIDTH_ID_COLUMN_FOR_REGISTERED_SOFTWARE = 300;

	// introducing a column for displaying the cursor row
	public static final int WINDOWS_SOFTWARE_ID_KEY_COL = 1;

	private static final String LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED = "restrictToNonAssociated";

	private static final int COLUMN_MARK_CURSOR_ROW = 0;

	private PanelAssignToLPools thePanel;

	private GenTableModel modelLicensepools;
	private GenTableModel modelProductId2LPool;
	private GenTableModel modelWindowsSoftwareIds;

	// we replace the filter from GenTableModel
	private TableModelFilterCondition windowsSoftwareFilterConditonShowOnlySelected;

	private TableModelFilterCondition windowsSoftwareFilterConditionDontShowAssociatedToOtherPool;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	// activate filter for selection in software table
	public enum SoftwareShowMode {
		ALL, ASSIGNED
	}

	public enum SoftwareShowAllMeans {
		ALL, ASSIGNED_OR_ASSIGNED_TO_NOTHING, ASSIGNED_TO_NOTHING
	}

	public enum SoftwareDirectionOfAssignment {
		POOL2SOFTWARE, SOFTWARE2POOL
	}

	private SoftwareShowMode softwareShow = SoftwareShowMode.ALL;
	private SoftwareShowAllMeans softwareShowAllMeans = SoftwareShowAllMeans.ALL;

	private SoftwareDirectionOfAssignment softwareDirectionOfAssignment = SoftwareDirectionOfAssignment.POOL2SOFTWARE;

	private Integer totalShownEntries;

	private Map<String, List<String>> removeKeysFromOtherLicensePool;

	public ControlPanelAssignToLPools(ConfigedMain configedMain) {
		thePanel = new PanelAssignToLPools(this);
		this.configedMain = configedMain;
		init();
	}

	@Override
	public PanelAssignToLPools getTabClient() {
		return thePanel;
	}

	private NavigableSet<Object> getUnAssignedSoftwareIds() {
		// the object is cached in persist
		return persistenceController.getSoftwareDataService().getSoftwareWithoutAssociatedLicensePoolPD();
	}

	public void setSoftwareIdsFromLicensePool() {
		String selectedLicensePool = getSelectedLicensePool();
		Logging.info(this, "setSoftwareIdsFromLicensePoot, selectedLicensePool ", selectedLicensePool);

		setSoftwareIdsFromLicensePool(selectedLicensePool);
	}

	public void setSoftwareIdsFromLicensePool(final String poolID) {
		Logging.info(this, "setSoftwareIdsFromLicensePool ", poolID,
				" should be thePanel.panelLicensepools.getSelectedRow() ",
				thePanel.getPanelLicensepools().getSelectedRow());
		Logging.info(this, "setSoftwareIdsFromLicensePool, call thePanel.fSoftwarename2LicensePool.setGlobalPool ");
		if (thePanel.getFSoftwarename2LicensePool() != null) {
			thePanel.getFSoftwarename2LicensePool().setGlobalPool(poolID);
		}

		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(false);

		boolean wasUsingSelectedFilter = modelWindowsSoftwareIds
				.isUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED);
		Logging.info(this, "setSoftwareIdsFromLicensePool wasUsingSelectedFilter ", wasUsingSelectedFilter);

		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED, false);

		modelWindowsSoftwareIds.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED, false);

		thePanel.getPanelRegisteredSoftware().showFiltered(false);

		thePanel.getFieldSelectedLicensePoolId().setText(poolID);
		thePanel.getFieldSelectedLicensePoolId().setToolTipText(poolID);

		List<String> softwareIdsForPool = new ArrayList<>();
		if (poolID != null) {
			softwareIdsForPool = persistenceController.getSoftwareDataService().getSoftwareListByLicensePoolPD(poolID);
		}

		Logging.info(this, "setSoftwareIdsFromLicensePool  softwareIds for licensePool  ", poolID, " : ",
				softwareIdsForPool.size());
		Logging.info(this, "setSoftwareIdsFromLicensePool  unknown softwareIds for licensePool  ", poolID, " : ",
				persistenceController.getSoftwareDataService().getUnknownSoftwareListForLicensePoolPD(poolID).size());

		Integer totalUnassignedSWEntries = getUnAssignedSoftwareIds().size();
		Logging.info(this, "setSoftwareIdsFromLicensePool unAssignedSoftwareIds ", totalUnassignedSWEntries);

		resetCounters(poolID);
		thePanel.getFieldCountAllWindowsSoftware().setText("0");

		thePanel.getButtonShowAssignedNotExisting().setEnabled(!persistenceController.getSoftwareDataService()
				.getUnknownSoftwareListForLicensePoolPD(poolID).isEmpty());
		if (thePanel.getFMissingSoftwareInfo() == null) {
			thePanel.setFMissingSoftwareInfo(new FGlobalSoftwareInfo(ConfigedMain.getLicensesFrame(), this));
		}

		if (!persistenceController.getSoftwareDataService().getUnknownSoftwareListForLicensePoolPD(poolID).isEmpty()) {
			thePanel.getFMissingSoftwareInfo().setTableModel(new GenTableModel(
					new MapTableUpdateItemFactory(thePanel.getFMissingSoftwareInfo().getColumnNames()),
					new DefaultTableProvider(new RetrieverMapSource(thePanel.getFMissingSoftwareInfo().getColumnNames(),
							new MapRetriever() {
								@Override
								public void reloadMap() {
									if (!configedMain.isAllLicenseDataReloaded()) {
										persistenceController
												.reloadData(ReloadEvent.ASW_TO_LP_RELATIONS_DATA_RELOAD.toString());
									}
								}

								@Override
								public Map<String, Map<String, Object>> retrieveMap() {
									return getMissingSoftwareMap(poolID);
								}
							})),
					0, new int[] {}, thePanel.getFMissingSoftwareInfo().getPanelGlobalSoftware(), updateCollection));
		}

		thePanel.getFieldCountAssignedStatus().setToolTipText(" <html><br /></html>");

		thePanel.getFieldCountAssignedStatus().setText(produceCount(softwareIdsForPool.size(), poolID == null));

		StringBuilder b = new StringBuilder("<html>");
		b.append(Configed.getResourceValue("PanelAssignToLPools.assignedStatusListTitle"));
		b.append("<br />");
		b.append("<br />");
		for (Object ident : softwareIdsForPool) {
			b.append(ident.toString());
			b.append("<br />");
		}
		b.append("</html>");
		thePanel.getFieldCountAssignedStatus().setToolTipText(b.toString());

		Integer totalSWEntries = modelWindowsSoftwareIds.getRowCount();

		produceFilterSets(softwareIdsForPool);

		Logging.info(this, "setSoftwareIdsFromLicensePool setUsingFilter ",
				GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED, " to ", wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED,
				wasUsingSelectedFilter);
		thePanel.getPanelRegisteredSoftware().showFiltered(wasUsingSelectedFilter);

		modelWindowsSoftwareIds.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED,
				getSoftwareShowAllMeans() != SoftwareShowAllMeans.ALL);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		Logging.info(this, "modelWindowsSoftwareIds row count ", totalShownEntries);
		thePanel.getFieldCountAllWindowsSoftware().setText(produceCount(totalSWEntries));
		thePanel.getFieldCountDisplayedWindowsSoftware().setText(produceCount(totalShownEntries));
		thePanel.getFieldCountNotAssignedSoftware().setText(produceCount(totalUnassignedSWEntries));

		List<String> selectKeys = thePanel.getPanelRegisteredSoftware().getSelectedKeys();

		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
			selectKeys = softwareIdsForPool;
			thePanel.getFieldCountAssignedInEditing().setText(produceCount(softwareIdsForPool.size(), poolID == null));
		} else {
			Set<Object> existingKeys = modelWindowsSoftwareIds.getExistingKeys();
			Set<Object> intersectionSet = new HashSet<>(existingKeys);
			intersectionSet.retainAll(selectKeys);

			thePanel.getFieldCountAssignedInEditing().setText(produceCount(intersectionSet.size()));
		}

		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(false);
		Logging.debug(this, "setSoftwareIdsFromLicensePool  setSelectedValues " + selectKeys);
		thePanel.getPanelRegisteredSoftware().setSelectedValues(selectKeys, WINDOWS_SOFTWARE_ID_KEY_COL);

		if (!selectKeys.isEmpty()) {
			thePanel.getPanelRegisteredSoftware().moveToValue(selectKeys.get(selectKeys.size() - 1),
					WINDOWS_SOFTWARE_ID_KEY_COL, false);
		}

		Logging.debug(this, "setSoftwareIdsFromLicensePool  selectedKeys "
				+ thePanel.getPanelRegisteredSoftware().getSelectedKeys());
		if (wasUsingSelectedFilter) {
			setVisualSelection(thePanel.getPanelRegisteredSoftware().getSelectedKeys());
		}
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);
		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(true);
	}

	private Map<String, Map<String, Object>> getMissingSoftwareMap(String poolID) {
		Map<String, Map<String, Object>> missingSoftwareMap = new HashMap<>();
		for (String ID : persistenceController.getSoftwareDataService()
				.getUnknownSoftwareListForLicensePoolPD(poolID)) {
			String[] rowValues = ID.split(Globals.PSEUDO_KEY_SEPARATOR);

			Map<String, Object> rowMap = new HashMap<>();
			for (String colName : thePanel.getFMissingSoftwareInfo().getColumnNames()) {
				rowMap.put(colName, "");
			}

			rowMap.put("ID", ID);

			List<String> identKeys = SWAuditEntry.KEYS_FOR_IDENT;
			if (rowValues.length != identKeys.size()) {
				Logging.warning(this, "illegal ID ", ID);
			} else {
				int i = 0;
				for (String key : identKeys) {
					rowMap.put(key, rowValues[i]);
					i++;
				}
			}

			rowMap.put("ID", ID);
			Logging.info(this, "unknownSoftwareIdsForPool ", rowMap);

			missingSoftwareMap.put(ID, rowMap);
		}
		return missingSoftwareMap;
	}

	private static String produceCount(Integer count) {
		if (count == null || count < 0) {
			return "";
		}
		return "" + count;
	}

	private static String produceCount(Integer count, boolean licensePoolNull) {
		if (count == null || licensePoolNull || count < 0) {
			return "";
		}
		return "" + count;
	}

	private void resetCounters(String licensePoolId) {
		Logging.info(this, "resetCounters for pool ", licensePoolId);
		String baseCount = "0";
		if (licensePoolId == null) {
			baseCount = "";
		}

		thePanel.getFieldCountAssignedStatus().setText(baseCount);
		thePanel.getFieldCountAssignedInEditing().setText(baseCount);
		thePanel.getButtonShowAssignedNotExisting().setEnabled(false);
	}

	// called by valueChanged method of ListSelectionListener
	public void validateWindowsSoftwareKeys() {
		String selectedLicensePool = getSelectedLicensePool();
		Logging.debug(this, "validateWindowsSoftwareKeys for licensePoolID " + selectedLicensePool);

		if (selectedLicensePool == null) {
			return;
		}

		Logging.debug(this, "validateWindowsSoftwareKeys thePanel.panelRegisteredSoftware.isAwareOfSelectionListener "
				+ thePanel.getPanelRegisteredSoftware().isAwareOfSelectionListener());

		if (!thePanel.getPanelRegisteredSoftware().isAwareOfSelectionListener()) {
			return;
		}

		List<String> selKeys = thePanel.getPanelRegisteredSoftware().getSelectedKeys();
		String showSelKeys = null;
		if (selKeys != null) {
			showSelKeys = "" + selKeys.size();
		}
		Logging.info(this, "validateWindowsSoftwareKeys selectedKeys ", showSelKeys,
				" associated to selectedLicensePool ", selectedLicensePool);

		if (selKeys == null) {
			resetCounters(selectedLicensePool);
			return;
		}

		List<String> cancelSelectionKeys = new ArrayList<>();
		removeKeysFromOtherLicensePool = new HashMap<>();

		for (String key : selKeys) {
			// key is already assigned to a different licensePool?

			boolean gotAssociation = persistenceController.getSoftwareDataService()
					.getFSoftware2LicensePoolPD(key) != null;
			Logging.debug(this, "validateWindowsSoftwareKeys key " + key + " gotAssociation " + gotAssociation);

			Boolean newAssociation = null;
			if (gotAssociation) {
				newAssociation = !(persistenceController.getSoftwareDataService().getFSoftware2LicensePoolPD(key)
						.equals(selectedLicensePool));
				Logging.debug(this, "validateWindowsSoftwareKeys has association to "
						+ persistenceController.getSoftwareDataService().getFSoftware2LicensePoolPD(key));
			}

			if (Boolean.TRUE.equals(newAssociation)) {
				String otherPool = persistenceController.getSoftwareDataService().getFSoftware2LicensePoolPD(key);

				if (otherPool.equals(FSoftwarename2LicensePool.VALUE_NO_LICENSE_POOL)) {
					Logging.info(this, "validateWindowsSoftwareKeys, assigned to valNoLicensepool");
				} else {
					askForAddingKey(key, otherPool, cancelSelectionKeys);
				}
			}
		}

		Logging.info(this, "cancelSelectionKeys ", cancelSelectionKeys);
		// without this condition we run into a loop
		if (!cancelSelectionKeys.isEmpty()) {
			selKeys.removeAll(cancelSelectionKeys);
			Logging.info(this, "selKeys after removal ", selKeys);
			thePanel.getPanelRegisteredSoftware().setSelectedValues(selKeys, WINDOWS_SOFTWARE_ID_KEY_COL);
		}

		thePanel.getFieldCountAssignedInEditing().setText("" + selKeys.size());
	}

	private void askForAddingKey(String key, String otherPool, List<String> cancelSelectionKeys) {
		String info = Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned") + "\n\n"
				+ otherPool;
		String option = Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.options");
		String title = Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.title");

		Logging.info(" software with ident \"", key, "\" already associated to license pool ", otherPool);

		FTextArea dialog = new FTextArea(ConfigedMain.getLicensesFrame(), title, true,
				new String[] { Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option1"),
						Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option2") },
				400, 200);
		dialog.setMessage(info + "\n\n" + option);
		dialog.setVisible(true);

		Logging.info(this, "validateWindowsSoftwareKeys result ", dialog.getResult());

		if (dialog.getResult() == 1) {
			// we cancel the new selection
			cancelSelectionKeys.add(key);
		} else {
			// or delete the assignment to the license pool
			List<String> removeKeys = removeKeysFromOtherLicensePool.computeIfAbsent(otherPool, s -> new ArrayList<>());
			removeKeys.add(key);
		}
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<MapBasedTableEditItem>();

		List<String> columnNames;

		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		MapTableUpdateItemFactory updateItemFactoryLicensepools = new MapTableUpdateItemFactory(modelLicensepools,
				columnNames);
		modelLicensepools = new GenTableModel(updateItemFactoryLicensepools, configedMain.licensePoolTableProvider, 0,
				thePanel.getPanelLicensepools(), updateCollection);
		updateItemFactoryLicensepools.setSource(modelLicensepools);

		tableModels.add(modelLicensepools);
		tablePanes.add(thePanel.getPanelLicensepools());

		modelLicensepools.reset();
		thePanel.getPanelLicensepools().setTableModel(modelLicensepools);
		modelLicensepools.setEditableColumns(new int[] { 0, 1 });

		JMenuItem menuItemAddPool = new JMenuItem(Configed.getResourceValue("ConfigedMain.Licenses.NewLicensepool"));
		menuItemAddPool.addActionListener((ActionEvent e) -> {
			Object[] a = new Object[2];
			a[0] = "";
			a[1] = "";
			modelLicensepools.addRow(a);
			thePanel.getPanelLicensepools().moveToValue("" + a[0], 0);

			// setting back the other tables is provided by ListSelectionListener
		});

		thePanel.getPanelLicensepools().addPopupItem(menuItemAddPool);

		thePanel.getPanelLicensepools().setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// updates
		thePanel.getPanelLicensepools().setUpdateController(
				new MapItemsUpdateController(thePanel.getPanelLicensepools(), modelLicensepools, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return updateLicensepool(rowmap);
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						return deleteLicensepool(rowmap);
					}
				}, updateCollection));

		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("productId");
		MapTableUpdateItemFactory updateItemFactoryProductId2LPool = new MapTableUpdateItemFactory(modelProductId2LPool,
				columnNames);
		modelProductId2LPool = new GenTableModel(updateItemFactoryProductId2LPool,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!configedMain.isAllLicenseDataReloaded()) {
							persistenceController.reloadData(ReloadEvent.LICENSE_POOL_DATA_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getLicenseDataService().getRelationsProductId2LPool();
					}
				})), -1, new int[] { 0, 1 }, thePanel.getPanelProductId2LPool(), updateCollection, true);
		updateItemFactoryProductId2LPool.setSource(modelProductId2LPool);

		tableModels.add(modelProductId2LPool);
		tablePanes.add(thePanel.getPanelProductId2LPool());

		modelProductId2LPool.reset();
		thePanel.getPanelProductId2LPool().setTableModel(modelProductId2LPool);
		modelProductId2LPool.setEditableColumns(new int[] { 0, 1 });

		JMenuItem menuItemAddRelationProductId2LPool = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.NewRelationProductId2LPool"));
		menuItemAddRelationProductId2LPool.addActionListener((ActionEvent e) -> {
			Object[] a = new Object[2];
			a[0] = "";
			if (thePanel.getPanelLicensepools().getSelectedRow() > -1) {
				a[0] = modelLicensepools.getValueAt(thePanel.getPanelLicensepools().getSelectedRowInModelTerms(), 0);
			}

			a[1] = "";

			modelProductId2LPool.addRow(a);

			thePanel.getPanelProductId2LPool().moveToValue("" + a[0], 0);
		});

		thePanel.getPanelProductId2LPool().addPopupItem(menuItemAddRelationProductId2LPool);

		TableColumn col = thePanel.getPanelProductId2LPool().getColumnModel().getColumn(0);
		JComboBox<String> comboLP0 = new JComboBox<>();

		col.setCellEditor(new AdaptingCellEditor(comboLP0, (int row, int column) -> {
			List<String> poolIds = configedMain.licensePoolTableProvider.getOrderedColumn(
					configedMain.licensePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1) {
				poolIds.add("");
			}
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		col = thePanel.getPanelProductId2LPool().getColumnModel().getColumn(1);
		JComboBox<String> comboLP1 = new JComboBox<>();
		col.setCellEditor(new AdaptingCellEditor(comboLP1, (row, column) -> new DefaultComboBoxModel<>(
				persistenceController.getProductDataService().getProductIdsPD().toArray(new String[0]))));

		thePanel.getPanelProductId2LPool().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelProductId2LPool(), modelProductId2LPool, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> m) {
						return persistenceController.getLicenseDataService().editRelationProductId2LPool(
								(String) m.get("productId"), (String) m.get("licensePoolId"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> m) {
						modelProductId2LPool.requestReload();
						return persistenceController.getLicenseDataService().deleteRelationProductId2LPool(
								(String) m.get("productId"), (String) m.get("licensePoolId"));
					}
				}, updateCollection));

		columnNames = new ArrayList<>(SWAuditEntry.getDisplayKeys());

		columnNames.add(COLUMN_MARK_CURSOR_ROW, "");

		columnNames.remove("licenseKey");

		Logging.info(this, "panelRegisteredSoftware constructed with (size) cols (", columnNames.size(), ") ",
				columnNames);

		boolean withRowCounter = false;
		modelWindowsSoftwareIds = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, new MapRetriever() {
					@Override
					public void reloadMap() {
						if (!configedMain.isAllLicenseDataReloaded()) {
							persistenceController.reloadData(ReloadEvent.INSTALLED_SOFTWARE_RELOAD.toString());
						}
					}

					@Override
					public Map<String, Map<String, Object>> retrieveMap() {
						return (Map) persistenceController.getSoftwareDataService()
								.getInstalledSoftwareInformationForLicensingPD();
					}
				}, withRowCounter)), WINDOWS_SOFTWARE_ID_KEY_COL, new int[] {}, thePanel.getPanelRegisteredSoftware(),
				updateCollection);

		Logging.info(this, "modelWindowsSoftwareIds row count ", modelWindowsSoftwareIds.getRowCount());
		tableModels.add(modelWindowsSoftwareIds);
		tablePanes.add(thePanel.getPanelRegisteredSoftware());

		modelWindowsSoftwareIds.reset();
		modelWindowsSoftwareIds.setColMarkCursorRow(COLUMN_MARK_CURSOR_ROW);

		thePanel.getPanelRegisteredSoftware().setTableModel(modelWindowsSoftwareIds);
		thePanel.getPanelRegisteredSoftware().setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modelWindowsSoftwareIds.setEditableColumns(new int[] {});

		Integer[] searchCols = new Integer[columnNames.size()];
		for (int j = 0; j < columnNames.size(); j++) {
			searchCols[j] = j;
		}

		softwareDirectionOfAssignment = SoftwareDirectionOfAssignment.POOL2SOFTWARE;
		thePanel.getPanelRegisteredSoftware().setAwareOfTableChangedListener(
				softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE);

		thePanel.getPanelRegisteredSoftware().setSearchColumns(searchCols);
		thePanel.getPanelRegisteredSoftware().setSearchSelectMode(false);

		windowsSoftwareFilterConditonShowOnlySelected = new DefaultTableModelFilterCondition(
				WINDOWS_SOFTWARE_ID_KEY_COL);
		modelWindowsSoftwareIds.chainFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED,
				new TableModelFilter(windowsSoftwareFilterConditonShowOnlySelected));
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED, false);

		windowsSoftwareFilterConditionDontShowAssociatedToOtherPool = new DefaultTableModelFilterCondition(
				WINDOWS_SOFTWARE_ID_KEY_COL);
		modelWindowsSoftwareIds.chainFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED,
				new TableModelFilter(windowsSoftwareFilterConditionDontShowAssociatedToOtherPool));
		modelWindowsSoftwareIds.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED, false);

		thePanel.getPanelRegisteredSoftware().showFiltered(false);
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);

		JMenuItem menuItemSoftwareShowAssigned = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.PopupWindowsSoftwareShowAssigned"));
		menuItemSoftwareShowAssigned.addActionListener((ActionEvent e) -> {
			softwareShow = SoftwareShowMode.ASSIGNED;
			setSWAssignments();
		});

		thePanel.getPanelRegisteredSoftware()
				.setFiltermarkActionListener(actionEvent -> registeredSoftwareFiltermarkAction());

		JMenuItem menuItemSoftwareShowAll = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.PopupWindowsSoftwareShowAll"));
		menuItemSoftwareShowAll.addActionListener((ActionEvent e) -> {
			softwareShow = SoftwareShowMode.ALL;
			setSWAssignments();
		});

		thePanel.getPanelRegisteredSoftware().addPopupItem(menuItemSoftwareShowAll);
		thePanel.getPanelRegisteredSoftware().addPopupItem(menuItemSoftwareShowAssigned);

		col = thePanel.getPanelRegisteredSoftware().getColumnModel().getColumn(COLUMN_MARK_CURSOR_ROW);
		col.setMaxWidth(12);
		col.setCellRenderer(new BooleanIconTableCellRenderer(Utils.getIntellijIcon("localChanges"), null));

		col = thePanel.getPanelRegisteredSoftware().getColumnModel().getColumn(WINDOWS_SOFTWARE_ID_KEY_COL);
		col.setMaxWidth(MAX_WIDTH_ID_COLUMN_FOR_REGISTERED_SOFTWARE);
		col.setHeaderValue("id ...");
		col = thePanel.getPanelRegisteredSoftware().getColumnModel().getColumn(columnNames.indexOf("subVersion"));
		col.setHeaderValue("OS variant");
		col.setMaxWidth(80);
		col = thePanel.getPanelRegisteredSoftware().getColumnModel().getColumn(columnNames.indexOf("architecture"));
		col.setMaxWidth(80);
		col = thePanel.getPanelRegisteredSoftware().getColumnModel().getColumn(columnNames.indexOf("language"));
		col.setMaxWidth(60);

		thePanel.getPanelRegisteredSoftware().setUpdateController(new SelectionMemorizerUpdateController(
				thePanel.getPanelLicensepools(), 0, thePanel.getPanelRegisteredSoftware(), this));

		final ControlPanelAssignToLPools contr = this;
		thePanel.setFSoftwarename2LicensePool(new FSoftwarename2LicensePool(contr, configedMain));
		thePanel.getFSoftwarename2LicensePool().setTableModel(); // test
		thePanel.setDisplaySimilarExist(
				thePanel.getFSoftwarename2LicensePool().checkExistNamesWithVariantLicensepools());
		thePanel.getFSoftwarename2LicensePool().setButtonsEnabled(true);

		thePanel.getPanelLicensepools().getListSelectionModel().addListSelectionListener(this::licensePoolValueChanged);

		setSoftwareIdsFromLicensePool(null);
		initializeVisualSettings();
	}

	private void registeredSoftwareFiltermarkAction() {
		if (softwareShow == SoftwareShowMode.ALL) {
			softwareShow = SoftwareShowMode.ASSIGNED;
			setSWAssignments();
		} else if (softwareShow == SoftwareShowMode.ASSIGNED) {
			softwareShow = SoftwareShowMode.ALL;
			setSWAssignments();
		} else {
			// Should not happen because SoftwareShowMode has only two elements
			Logging.warning(this, "softwareShow has Value ", softwareShow, " that does not exist in SoftwareShowMode");
		}
	}

	private String updateLicensepool(Map<String, Object> rowmap) {
		// hack for avoiding unvoluntary reuse of a license pool id
		boolean existsNewRow = configedMain.licensePoolTableProvider.getRows().size() < modelLicensepools.getRowCount();

		if (existsNewRow && persistenceController.getLicenseDataService().getLicensePoolsPD()
				.containsKey(rowmap.get("licensePoolId"))) {
			// but we leave it until the service methods reflect the situation more
			// accurately

			String info = Configed.getResourceValue("PanelAssignToLPools.licensePoolIdAlreadyExists") + " \n(\""
					+ rowmap.get("licensePoolId") + "\" ?)";

			String title = Configed.getResourceValue("PanelAssignToLPools.licensePoolIdAlreadyExists.title");

			JOptionPane.showMessageDialog(thePanel, info, title, JOptionPane.INFORMATION_MESSAGE);

			return null;
		}

		if (existsNewRow) {
			modelLicensepools.requestReload();
		}

		return persistenceController.getLicenseDataService().editLicensePool(
				(String) rowmap.get(LicensepoolEntry.ID_SERVICE_KEY),
				(String) rowmap.get(LicensepoolEntry.DESCRIPTION_KEY));
	}

	public boolean updateLicensepool(String poolId, List<String> softwareIds) {
		Logging.info(this, "sendUpdate poolId, softwareIds: ", poolId, ", ", softwareIds);
		Logging.info(this, "sendUpdate poolId, removeKeysFromOtherLicensePool ", removeKeysFromOtherLicensePool);

		if (removeKeysFromOtherLicensePool != null) {
			for (Entry<String, List<String>> otherStringPoolEntry : removeKeysFromOtherLicensePool.entrySet()) {
				if (otherStringPoolEntry.getValue().isEmpty()) {
					Logging.info(this, "we wont remove association since entry is empty: ",
							otherStringPoolEntry.getKey());
				} else if (persistenceController.getSoftwareDataService()
						.removeAssociations(otherStringPoolEntry.getKey(), otherStringPoolEntry.getValue())) {
					removeKeysFromOtherLicensePool.remove(otherStringPoolEntry.getKey());
				} else {
					return false;
				}
			}
		}
		boolean result;

		// cleanup assignments to other pools since an update would not change them
		// (redmine #3282)
		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
			result = persistenceController.getSoftwareDataService().setWindowsSoftwareIds2LPool(poolId, softwareIds);
		} else {
			result = persistenceController.getSoftwareDataService().addWindowsSoftwareIds2LPool(poolId, softwareIds);
		}

		Logging.info(this, "sendUpdate, setSoftwareIdsFromLicensePool poolId ", poolId);
		setSoftwareIdsFromLicensePool(poolId);

		Logging.info(this, "sendUpdate, adapt Softwarename2LicensePool");
		Logging.info(this, "sendUpdate, we have software ids ", softwareIds.size());
		Logging.info(this, "sendUpdate, we have software ids ",
				persistenceController.getSoftwareDataService().getSoftwareListByLicensePoolPD(poolId).size(),
				" they are " + persistenceController.getSoftwareDataService().getSoftwareListByLicensePoolPD(poolId));

		List<String> oldSWListForPool = persistenceController.getSoftwareDataService()
				.getSoftwareListByLicensePoolPD(poolId);

		Logging.info(this, "sendUpdate remove ", oldSWListForPool, " from Software2LicensePool ");
		persistenceController.getSoftwareDataService().getFSoftware2LicensePoolPD().keySet()
				.removeAll(oldSWListForPool);

		for (String ident : softwareIds) {
			persistenceController.getSoftwareDataService().setFSoftware2LicensePool(ident, poolId);
		}

		if (thePanel.getFSoftwarename2LicensePool() != null) {
			thePanel.getFSoftwarename2LicensePool().getPanelSWnames().requestReload();
		}

		if (thePanel.getFSoftwarename2LicensePool() != null) {
			thePanel.getFSoftwarename2LicensePool().getPanelSWxLicensepool().requestReload();
		}

		return result;
	}

	private boolean deleteLicensepool(Map<String, Object> rowmap) {
		modelLicensepools.requestReload();
		return persistenceController.getLicenseDataService().deleteLicensePool((String) rowmap.get("licensePoolId"));
	}

	private void licensePoolValueChanged(ListSelectionEvent listSelectionEvent) {
		if (listSelectionEvent.getValueIsAdjusting()) {
			return;
		}

		String selectedLicensePool = null;

		thePanel.getPanelProductId2LPool().setSelectedValues(null, 0);

		ListSelectionModel lsm = (ListSelectionModel) listSelectionEvent.getSource();

		if (lsm.isSelectionEmpty()) {
			Logging.debug(this, "no rows selected");
		} else {
			selectedLicensePool = getSelectedLicensePool();
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicensePool, 0);
		}

		setSoftwareIdsFromLicensePool(selectedLicensePool);

		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.SOFTWARE2POOL) {
			thePanel.getPanelRegisteredSoftware().setDataChanged(gotNewSWKeysForLicensePool(selectedLicensePool));
		}
	}

	private void setVisualSelection(List<String> keys) {
		Logging.debug(this, "setVisualSelection for panelRegisteredSoftware on keys " + keys);
		thePanel.getPanelRegisteredSoftware().setSelectedValues(keys, WINDOWS_SOFTWARE_ID_KEY_COL);

		if (keys != null && !keys.isEmpty()) {
			thePanel.getPanelRegisteredSoftware().moveToValue(keys.get(keys.size() - 1), WINDOWS_SOFTWARE_ID_KEY_COL,
					false);
		}
	}

	private void produceFilter1(List<String> assignedWindowsSoftwareIds) {
		TreeSet<Object> filter1 = null;

		if (softwareShowAllMeans != SoftwareShowAllMeans.ALL) {
			filter1 = new TreeSet<>(getUnAssignedSoftwareIds());
		}

		if (filter1 != null && softwareShowAllMeans == SoftwareShowAllMeans.ASSIGNED_OR_ASSIGNED_TO_NOTHING
				&& assignedWindowsSoftwareIds != null) {
			filter1.addAll(assignedWindowsSoftwareIds);
		}

		String filterInfo = "null";
		if (filter1 != null) {
			filterInfo = "" + filter1.size();
		}

		Logging.info(this, "produceFilterSets setFilter dontShowAssociatedToOtherPool ", filterInfo);

		windowsSoftwareFilterConditionDontShowAssociatedToOtherPool.setFilter(filter1);
	}

	private void produceFilterSets(List<String> assignedWindowsSoftwareIds) {
		TreeSet<Object> filter0 = null;

		if (softwareShow == SoftwareShowMode.ASSIGNED) {
			filter0 = new TreeSet<>(assignedWindowsSoftwareIds);
		}

		String filterInfo = "null";
		if (filter0 != null) {
			filterInfo = "" + filter0.size();
		}
		Logging.info(this, "produceFilterSets setFilter dontShowAssociatedToOtherPool ", filterInfo);
		windowsSoftwareFilterConditonShowOnlySelected.setFilter(filter0);

		produceFilter1(assignedWindowsSoftwareIds);
	}

	private void setSWAssignments() {
		boolean b = thePanel.getPanelRegisteredSoftware().isDataChanged();
		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(false);

		List<String> selectedKeys = thePanel.getPanelRegisteredSoftware().getSelectedKeys();
		Logging.info(this, "setSWAssignments  selectedKeys ", selectedKeys);

		Logging.info(this, "setSWAssignments usingFilter ", softwareShow == SoftwareShowMode.ASSIGNED,
				" selected keys ", selectedKeys);

		boolean usingShowSelectedFilter = softwareShow == SoftwareShowMode.ASSIGNED;
		if (usingShowSelectedFilter) {
			windowsSoftwareFilterConditonShowOnlySelected.setFilter(new TreeSet<>(selectedKeys));
		} else {
			windowsSoftwareFilterConditonShowOnlySelected.setFilter(null);
		}

		thePanel.getPanelRegisteredSoftware().setAwareOfTableChangedListener(
				softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE);

		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED,
				usingShowSelectedFilter);

		thePanel.getPanelRegisteredSoftware().showFiltered(usingShowSelectedFilter);
		setVisualSelection(selectedKeys);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		Logging.info(this, "modelWindowsSoftwareIds row count 2 ", modelWindowsSoftwareIds.getRowCount());
		thePanel.getFieldCountDisplayedWindowsSoftware().setText(produceCount(totalShownEntries));
		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(true);
		thePanel.getPanelRegisteredSoftware().setDataChanged(b);
	}

	public SoftwareShowAllMeans getSoftwareShowAllMeans() {
		return softwareShowAllMeans;
	}

	public void setSoftwareShowAllMeans(SoftwareShowAllMeans meaning) {
		SoftwareShowAllMeans softwareShowAllMeansOld = softwareShowAllMeans;
		softwareShowAllMeans = meaning;

		if (softwareShowAllMeansOld != softwareShowAllMeans) {
			boolean tableChangeAware = thePanel.getPanelRegisteredSoftware().isAwareOfTableChangedListener();
			thePanel.getPanelRegisteredSoftware().setAwareOfTableChangedListener(false);

			String selectedLicensePool = null;
			Logging.info(this, " setShowOnlyAssociationsToSelectedPoolOrNoPool selected license row ",
					thePanel.getPanelLicensepools().getSelectedRow());
			selectedLicensePool = getSelectedLicensePool();
			setSoftwareIdsFromLicensePool(selectedLicensePool);

			thePanel.getPanelRegisteredSoftware().setAwareOfTableChangedListener(tableChangeAware);
		}
	}

	public void setSoftwareDirectionOfAssignment(SoftwareDirectionOfAssignment direction) {
		SoftwareDirectionOfAssignment oldDirection = softwareDirectionOfAssignment;
		this.softwareDirectionOfAssignment = direction;

		if (oldDirection != direction) {
			if (direction == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
				thePanel.getPanelRegisteredSoftware().getTheSearchpane().setFiltering();
			} else if (direction == SoftwareDirectionOfAssignment.SOFTWARE2POOL) {
				resetCounters(null);
				thePanel.getFieldCountAssignedInEditing().setText("");
			} else {
				// Should not happen because enum SoftwareDirectionOfAssignment has only two values
			}

			Logging.info(this, "switched to ", direction);
			initializeVisualSettings();
		}
	}

	public SoftwareDirectionOfAssignment getSoftwareDirectionOfAssignment() {
		return this.softwareDirectionOfAssignment;
	}

	private boolean gotNewSWKeysForLicensePool(String selectedLicensePool) {
		if (selectedLicensePool == null) {
			return false;
		}

		List<String> oldSWList = persistenceController.getSoftwareDataService()
				.getSoftwareListByLicensePoolPD(selectedLicensePool);
		List<String> newKeys = new ArrayList<>(thePanel.getPanelRegisteredSoftware().getSelectedKeys());
		newKeys.removeAll(oldSWList);

		Logging.info(this, "new keys ", newKeys);

		return !newKeys.isEmpty();
	}

	public boolean acknowledgeChangeForSWList() {
		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
			// any visual change is regarded as a data change
			return true;
		} else {
			// a change in visual sw list is regarded as data changed if there additional
			// entries

			String selectedLicensePool = getSelectedLicensePool();
			if (selectedLicensePool == null) {
				return false;
			}

			validateWindowsSoftwareKeys();

			return gotNewSWKeysForLicensePool(selectedLicensePool);
		}
	}

	public String getSelectedLicensePool() {
		if (thePanel.getPanelLicensepools().getSelectedRow() >= 0) {
			return thePanel.getPanelLicensepools().getValueAt(thePanel.getPanelLicensepools().getSelectedRow(), 0)
					.toString();
		} else {
			return null;
		}
	}

	@Override
	public void initializeVisualSettings() {
		super.initializeVisualSettings();
		setSoftwareIdsFromLicensePool(null);
		resetCounters(null);

		thePanel.getPanelProductId2LPool().getTheTable().clearSelection();
		thePanel.getPanelLicensepools().getTheTable().clearSelection();
		thePanel.getPanelRegisteredSoftware().getTheTable().clearSelection();
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);
	}
}
