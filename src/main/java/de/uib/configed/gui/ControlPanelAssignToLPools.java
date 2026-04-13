/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.features.licenses.LicenseManagement;
import de.uib.configed.gui.features.licenses.PanelAssignToLPools;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.table.DefaultTableModelFilterCondition;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.TableModelFilter;
import de.uib.configed.gui.share.table.TableModelFilterCondition;
import de.uib.configed.gui.share.table.gui.AdaptingCellEditor;
import de.uib.configed.gui.share.table.gui.IconTableCellRenderer;
import de.uib.configed.gui.share.table.provider.DefaultTableProvider;
import de.uib.configed.gui.share.table.updates.MapBasedUpdater;
import de.uib.configed.gui.share.table.updates.MapItemsUpdateController;
import de.uib.configed.gui.share.table.updates.MapTableUpdateItemFactory;
import de.uib.configed.gui.share.table.updates.SelectionMemorizerUpdateController;
import de.uib.configed.gui.type.SWAuditEntry;
import de.uib.configed.gui.type.licenses.LicensepoolEntry;
import de.uib.configed.share.logging.Logging;

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

	private LicenseManagement licensesPane;

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

	public ControlPanelAssignToLPools(LicenseManagement licensesPane) {
		thePanel = new PanelAssignToLPools(this);
		this.licensesPane = licensesPane;

		init();
	}

	@Override
	public PanelAssignToLPools getTabClient() {
		return thePanel;
	}

	public void setSoftwareIdsFromLicensePool() {
		String selectedLicensePool = getSelectedLicensePool();
		Logging.info(this, "setSoftwareIdsFromLicensePoot, selectedLicensePool ", selectedLicensePool);

		setSoftwareIdsFromLicensePool(selectedLicensePool);
	}

	public void setSoftwareIdsFromLicensePool(final String poolID) {
		Logging.info(this, "setSoftwareIdsFromLicensePool ", poolID,
				" should be thePanel.panelLicensepools.getSelectedRow() ",
				thePanel.getPanelLicensepools().getGenEditTable().getSelectedRow());
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

		thePanel.getPanelRegisteredSoftware().getTableSearchPane().setFilterMark(false);

		thePanel.getFieldSelectedLicensePoolId().setText(poolID);
		thePanel.getFieldSelectedLicensePoolId().setToolTipText(poolID);

		List<String> softwareIdsForPool = poolID == null ? new ArrayList<>()
				: persistenceController.getDataServices().software.getSoftwareListByLicensePoolPD(poolID);

		Logging.info(this, "setSoftwareIdsFromLicensePool  softwareIds for licensePool  ", poolID, " : ",
				softwareIdsForPool.size());
		Logging.info(this, "setSoftwareIdsFromLicensePool  unknown softwareIds for licensePool  ", poolID, " : ",
				persistenceController.getDataServices().software.getUnknownSoftwareListForLicensePoolPD(poolID).size());

		resetCounters(poolID);
		thePanel.getFieldCountAllWindowsSoftware().setText("0");

		thePanel.getButtonShowAssignedNotExisting().setEnabled(!persistenceController.getDataServices().software
				.getUnknownSoftwareListForLicensePoolPD(poolID).isEmpty());
		if (thePanel.getFMissingSoftwareInfo() == null) {
			thePanel.setFMissingSoftwareInfo(new GlobalSoftwareInfoDialog(this));
		}

		if (!persistenceController.getDataServices().software.getUnknownSoftwareListForLicensePoolPD(poolID)
				.isEmpty()) {
			thePanel.getFMissingSoftwareInfo().setTableModel(getMissingSoftwareTableModel(poolID));
		}

		thePanel.getFieldCountAssignedStatus().setText(produceCount(softwareIdsForPool.size(), poolID == null));

		thePanel.getFieldCountAssignedStatus().setToolTipText(createTooltip(softwareIdsForPool));

		produceFilterSets(softwareIdsForPool);

		Logging.info(this, "setSoftwareIdsFromLicensePool setUsingFilter ",
				GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED, " to ", wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED,
				wasUsingSelectedFilter);
		thePanel.getPanelRegisteredSoftware().getTableSearchPane().setFilterMark(wasUsingSelectedFilter);

		modelWindowsSoftwareIds.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED,
				softwareShowAllMeans != SoftwareShowAllMeans.ALL);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		Logging.info(this, "modelWindowsSoftwareIds row count ", totalShownEntries);
		thePanel.getFieldCountAllWindowsSoftware().setText(produceCount(modelWindowsSoftwareIds.getRowCount()));
		thePanel.getFieldCountDisplayedWindowsSoftware().setText(produceCount(totalShownEntries));
		thePanel.getFieldCountNotAssignedSoftware().setText(produceCount(
				persistenceController.getDataServices().software.getSoftwareWithoutAssociatedLicensePoolPD().size()));

		List<String> selectKeys;

		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
			selectKeys = softwareIdsForPool;
			thePanel.getFieldCountAssignedInEditing().setText(produceCount(softwareIdsForPool.size(), poolID == null));
		} else {
			selectKeys = thePanel.getPanelRegisteredSoftware().getSelectedKeys();
			Set<Object> intersectionSet = modelWindowsSoftwareIds.getExistingKeys();
			intersectionSet.retainAll(selectKeys);

			thePanel.getFieldCountAssignedInEditing().setText(produceCount(intersectionSet.size()));
		}

		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(false);
		Logging.debug(this, "setSoftwareIdsFromLicensePool  setSelectedValues ", selectKeys);
		thePanel.getPanelRegisteredSoftware().setSelectedValues(selectKeys, WINDOWS_SOFTWARE_ID_KEY_COL);

		if (!selectKeys.isEmpty()) {
			thePanel.getPanelRegisteredSoftware().moveToValue(selectKeys.get(selectKeys.size() - 1),
					WINDOWS_SOFTWARE_ID_KEY_COL, false);
		}

		Logging.debug(this, "setSoftwareIdsFromLicensePool  selectedKeys ",
				thePanel.getPanelRegisteredSoftware().getSelectedKeys());
		if (wasUsingSelectedFilter) {
			setVisualSelection(thePanel.getPanelRegisteredSoftware().getSelectedKeys());
		}
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);
		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(true);
	}

	private GenTableModel getMissingSoftwareTableModel(String poolID) {
		return new GenTableModel(new MapTableUpdateItemFactory(thePanel.getFMissingSoftwareInfo().getColumnNames()),
				DefaultTableProvider.createWithRetrieverMapSource(thePanel.getFMissingSoftwareInfo().getColumnNames(),
						ReloadEvent.ASW_TO_LP_RELATIONS_DATA_RELOAD, () -> getMissingSoftwareMap(poolID)),
				0, new int[] {}, thePanel.getFMissingSoftwareInfo().getPanelGlobalSoftware(), updateCollection);
	}

	private static String createTooltip(List<String> softwareIdsForPool) {
		StringBuilder b = new StringBuilder("<html>");
		b.append(Configed.getResourceValue("PanelAssignToLPools.assignedStatusListTitle"));
		b.append("<br />");
		b.append("<br />");
		for (String ident : softwareIdsForPool) {
			b.append(ident);
			b.append("<br />");
		}
		b.append("</html>");
		return b.toString();
	}

	private Map<String, Map<String, Object>> getMissingSoftwareMap(String poolID) {
		Map<String, Map<String, Object>> missingSoftwareMap = new HashMap<>();
		for (String ID : persistenceController.getDataServices().software
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
		Logging.debug(this, "validateWindowsSoftwareKeys for licensePoolID ", selectedLicensePool);

		if (selectedLicensePool == null) {
			return;
		}

		Logging.debug(this, "validateWindowsSoftwareKeys thePanel.panelRegisteredSoftware.isAwareOfSelectionListener ",
				thePanel.getPanelRegisteredSoftware().isAwareOfSelectionListener());

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

			boolean gotAssociation = persistenceController.getDataServices().software
					.getFSoftware2LicensePoolPD(key) != null;
			Logging.debug(this, "validateWindowsSoftwareKeys key ", key, " gotAssociation ", gotAssociation);

			Boolean newAssociation = null;
			if (gotAssociation) {
				newAssociation = !(persistenceController.getDataServices().software.getFSoftware2LicensePoolPD(key)
						.equals(selectedLicensePool));
				Logging.debug(this, "validateWindowsSoftwareKeys has association to ",
						persistenceController.getDataServices().software.getFSoftware2LicensePoolPD(key));
			}

			if (Boolean.TRUE.equals(newAssociation)) {
				String otherPool = persistenceController.getDataServices().software.getFSoftware2LicensePoolPD(key);

				if (otherPool.equals(Softwarename2LicensePoolDialog.VALUE_NO_LICENSE_POOL)) {
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

		int answer = JOptionPane.showOptionDialog(ConfigedMain.getMainFrame(), info + "\n\n" + option, title,
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				new String[] { Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option1"),
						Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option2") },
				null);

		Logging.info(this, "validateWindowsSoftwareKeys result ", answer);

		if (answer == 0) {
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
		initializeModels();
		initializePanelLicensepools();
		initializePanelProductId2LPool();
		initializePanelRegisteredSoftware();
		initializeVisualSettings();
	}

	private void initializeModels() {
		updateCollection = new ArrayList<>();
		tableModels = new ArrayList<>();
		panelGenEdits = new ArrayList<>();
	}

	private void initializePanelLicensepools() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		MapTableUpdateItemFactory updateItemFactoryLicensepools = new MapTableUpdateItemFactory(modelLicensepools,
				columnNames);
		modelLicensepools = new GenTableModel(updateItemFactoryLicensepools, licensesPane.getLicensePoolTableProvider(),
				0, thePanel.getPanelLicensepools(), updateCollection);
		updateItemFactoryLicensepools.setSource(modelLicensepools);

		tableModels.add(modelLicensepools);
		panelGenEdits.add(thePanel.getPanelLicensepools());

		modelLicensepools.reset();
		thePanel.getPanelLicensepools().setTableModel(modelLicensepools);
		thePanel.getPanelLicensepools().restoreFilter();
		modelLicensepools.setEditableColumns(new int[] { 0, 1 });

		JMenuItem menuItemAddPool = new JMenuItem(Configed.getResourceValue("ConfigedMain.Licenses.NewLicensepool"));
		menuItemAddPool.addActionListener((ActionEvent e) -> {
			String[] a = new String[] { "", "" };
			modelLicensepools.addRow(a);
			thePanel.getPanelLicensepools().moveToValue(a[0], 0);

			// setting back the other tables is provided by ListSelectionListener
		});
		menuItemAddPool.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());

		thePanel.getPanelLicensepools().addPopupItem(menuItemAddPool);
		thePanel.getPanelLicensepools().getGenEditTable().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
	}

	private void initializePanelProductId2LPool() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("productId");
		MapTableUpdateItemFactory updateItemFactoryProductId2LPool = new MapTableUpdateItemFactory(modelProductId2LPool,
				columnNames);
		modelProductId2LPool = new GenTableModel(updateItemFactoryProductId2LPool,
				DefaultTableProvider.createWithRetrieverMapSource(columnNames, ReloadEvent.LICENSE_POOL_DATA_RELOAD,
						persistenceController.getDataServices().license::getRelationsProductId2LPool),
				-1, new int[] { 0, 1 }, thePanel.getPanelProductId2LPool(), updateCollection, true);

		updateItemFactoryProductId2LPool.setSource(modelProductId2LPool);

		tableModels.add(modelProductId2LPool);
		panelGenEdits.add(thePanel.getPanelProductId2LPool());

		modelProductId2LPool.reset();
		thePanel.getPanelProductId2LPool().setTableModel(modelProductId2LPool);
		thePanel.getPanelProductId2LPool().restoreFilter();
		modelProductId2LPool.setEditableColumns(new int[] { 0, 1 });

		JMenuItem menuItemAddRelationProductId2LPool = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.NewRelationProductId2LPool"));
		menuItemAddRelationProductId2LPool.addActionListener((ActionEvent e) -> {
			Object[] a = new Object[] { "", "" };
			if (thePanel.getPanelLicensepools().getGenEditTable().getSelectedRow() > -1) {
				a[0] = modelLicensepools.getValueAt(thePanel.getPanelLicensepools().getSelectedRowInModelTerms(), 0);
			}
			modelProductId2LPool.addRow(a);
			thePanel.getPanelProductId2LPool().moveToValue("" + a[0], 0);
		});
		menuItemAddRelationProductId2LPool
				.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());

		thePanel.getPanelProductId2LPool().addPopupItem(menuItemAddRelationProductId2LPool);

		TableColumn col = thePanel.getPanelProductId2LPool().getGenEditTable().getColumnModel().getColumn(0);
		JComboBox<String> comboLP0 = new JComboBox<>();
		col.setCellEditor(new AdaptingCellEditor(comboLP0, (int row, int column) -> {
			List<String> poolIds = licensesPane.getLicensePoolTableProvider().getOrderedColumn(
					licensesPane.getLicensePoolTableProvider().getColumnNames().indexOf("licensePoolId"), false);
			if (poolIds.size() <= 1) {
				poolIds.add("");
			}
			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		col = thePanel.getPanelProductId2LPool().getGenEditTable().getColumnModel().getColumn(1);
		JComboBox<String> comboLP1 = new JComboBox<>();
		col.setCellEditor(new AdaptingCellEditor(comboLP1, (row, column) -> new DefaultComboBoxModel<>(
				persistenceController.getDataServices().product.getProductIdsPD().toArray(new String[0]))));

		thePanel.getPanelProductId2LPool().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelProductId2LPool(), modelProductId2LPool, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> m) {
						return persistenceController.getDataServices().license.editRelationProductId2LPool(
								(String) m.get("productId"), (String) m.get("licensePoolId"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> m) {
						modelProductId2LPool.requestReload();
						return persistenceController.getDataServices().license.deleteRelationProductId2LPool(
								(String) m.get("productId"), (String) m.get("licensePoolId"));
					}
				}, updateCollection));
	}

	private void initializePanelRegisteredSoftware() {
		List<String> columnNames = new ArrayList<>(SWAuditEntry.getDisplayKeys());
		columnNames.add(COLUMN_MARK_CURSOR_ROW, "");
		columnNames.remove("licenseKey");

		Logging.info(this, "panelRegisteredSoftware constructed with (size) cols (", columnNames.size(), ") ",
				columnNames);

		modelWindowsSoftwareIds = new GenTableModel(null, DefaultTableProvider.createWithRetrieverMapSource(columnNames,
				ReloadEvent.INSTALLED_SOFTWARE_RELOAD,
				persistenceController.getDataServices().software::getInstalledSoftwareInformationForLicensingPD),
				WINDOWS_SOFTWARE_ID_KEY_COL, new int[] {}, thePanel.getPanelRegisteredSoftware(), updateCollection);

		Logging.info(this, "modelWindowsSoftwareIds row count ", modelWindowsSoftwareIds.getRowCount());
		tableModels.add(modelWindowsSoftwareIds);
		panelGenEdits.add(thePanel.getPanelRegisteredSoftware());

		modelWindowsSoftwareIds.reset();
		modelWindowsSoftwareIds.setColMarkCursorRow(COLUMN_MARK_CURSOR_ROW);

		thePanel.getPanelRegisteredSoftware().setTableModel(modelWindowsSoftwareIds);
		thePanel.getPanelRegisteredSoftware().restoreFilter();
		thePanel.getPanelRegisteredSoftware().getGenEditTable()
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modelWindowsSoftwareIds.setEditableColumns(new int[] {});

		Integer[] searchCols = new Integer[columnNames.size()];
		for (int j = 0; j < columnNames.size(); j++) {
			searchCols[j] = j;
		}

		softwareDirectionOfAssignment = SoftwareDirectionOfAssignment.POOL2SOFTWARE;
		thePanel.getPanelRegisteredSoftware().setAwareOfTableChangedListener(
				softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE);

		thePanel.getPanelRegisteredSoftware().setSearchColumns(searchCols);
		thePanel.getPanelRegisteredSoftware().getTableSearchPane().setSelectMode(false);

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

		thePanel.getPanelRegisteredSoftware().getTableSearchPane().setFilterMark(false);
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);

		initializePanelRegisteredSoftwareMenuItems(columnNames);

		thePanel.getPanelRegisteredSoftware().setUpdateController(new SelectionMemorizerUpdateController(
				thePanel.getPanelLicensepools(), 0, thePanel.getPanelRegisteredSoftware(), this));

		thePanel.setFSoftwarename2LicensePool(new Softwarename2LicensePoolDialog(this));
		thePanel.getFSoftwarename2LicensePool().setTableModel();
		thePanel.setDisplaySimilarExist(
				thePanel.getFSoftwarename2LicensePool().checkExistNamesWithVariantLicensepools());

		thePanel.getPanelLicensepools().getGenEditTable().getSelectionModel()
				.addListSelectionListener(this::licensePoolValueChanged);

		setSoftwareIdsFromLicensePool(null);
	}

	private void initializePanelRegisteredSoftwareMenuItems(List<String> columnNames) {
		JMenuItem menuItemSoftwareShowAssigned = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.PopupWindowsSoftwareShowAssigned"));
		menuItemSoftwareShowAssigned.addActionListener((ActionEvent e) -> {
			softwareShow = SoftwareShowMode.ASSIGNED;
			setSWAssignments();
		});

		thePanel.getPanelRegisteredSoftware().getTableSearchPane()
				.setFiltermarkActionListener(actionEvent -> registeredSoftwareFiltermarkAction());

		JMenuItem menuItemSoftwareShowAll = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.PopupWindowsSoftwareShowAll"));
		menuItemSoftwareShowAll.addActionListener((ActionEvent e) -> {
			softwareShow = SoftwareShowMode.ALL;
			setSWAssignments();
		});

		thePanel.getPanelRegisteredSoftware().addPopupItem(menuItemSoftwareShowAll);
		thePanel.getPanelRegisteredSoftware().addPopupItem(menuItemSoftwareShowAssigned);

		TableColumn col = thePanel.getPanelRegisteredSoftware().getGenEditTable().getColumnModel()
				.getColumn(COLUMN_MARK_CURSOR_ROW);
		col.setMaxWidth(12);
		col.setCellRenderer(new IconTableCellRenderer<Boolean>(
				IconTableCellRenderer.booleanMap(Icons.getIntellijIcon("localChanges")), false));

		col = thePanel.getPanelRegisteredSoftware().getGenEditTable().getColumnModel()
				.getColumn(WINDOWS_SOFTWARE_ID_KEY_COL);
		col.setMaxWidth(MAX_WIDTH_ID_COLUMN_FOR_REGISTERED_SOFTWARE);
		col.setHeaderValue("id ...");
		col = thePanel.getPanelRegisteredSoftware().getGenEditTable().getColumnModel()
				.getColumn(columnNames.indexOf("subVersion"));
		col.setHeaderValue("OS variant");
		col.setMaxWidth(80);
		col = thePanel.getPanelRegisteredSoftware().getGenEditTable().getColumnModel()
				.getColumn(columnNames.indexOf("architecture"));
		col.setMaxWidth(80);
		col = thePanel.getPanelRegisteredSoftware().getGenEditTable().getColumnModel()
				.getColumn(columnNames.indexOf("language"));
		col.setMaxWidth(60);
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
		boolean existsNewRow = licensesPane.getLicensePoolTableProvider().getRows().size() < modelLicensepools
				.getRowCount();

		if (existsNewRow && persistenceController.getDataServices().license.getLicensePoolsPD()
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

		return persistenceController.getDataServices().license.editLicensePool(
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
				} else if (persistenceController.getDataServices().software
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
			result = persistenceController.getDataServices().software.setWindowsSoftwareIds2LPool(poolId, softwareIds);
		} else {
			result = persistenceController.getDataServices().software.addWindowsSoftwareIds2LPool(poolId, softwareIds);
		}

		Logging.info(this, "sendUpdate, setSoftwareIdsFromLicensePool poolId ", poolId);
		setSoftwareIdsFromLicensePool(poolId);

		List<String> oldSWListForPool = persistenceController.getDataServices().software
				.getSoftwareListByLicensePoolPD(poolId);

		Logging.info(this, "sendUpdate, adapt Softwarename2LicensePool");
		Logging.info(this, "sendUpdate, we have software ids ", softwareIds.size());
		Logging.info(this, "sendUpdate, we have software ids ", oldSWListForPool.size(), " they are ",
				oldSWListForPool);

		Logging.info(this, "sendUpdate remove ", oldSWListForPool, " from Software2LicensePool ");
		persistenceController.getDataServices().software.getFSoftware2LicensePoolPD().keySet()
				.removeAll(oldSWListForPool);

		for (String ident : softwareIds) {
			persistenceController.getDataServices().software.setFSoftware2LicensePool(ident, poolId);
		}

		if (thePanel.getFSoftwarename2LicensePool() != null) {
			thePanel.getFSoftwarename2LicensePool().getPanelSWnames().getTableModel().requestReload();
			if (thePanel.getFSoftwarename2LicensePool().getPanelSWxLicensepool().getTableModel() != null) {
				thePanel.getFSoftwarename2LicensePool().getPanelSWxLicensepool().getTableModel().requestReload();
			}
		}

		return result;
	}

	private boolean deleteLicensepool(Map<String, Object> rowmap) {
		modelLicensepools.requestReload();
		return persistenceController.getDataServices().license.deleteLicensePool((String) rowmap.get("licensePoolId"));
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
		Logging.debug(this, "setVisualSelection for panelRegisteredSoftware on keys ", keys);
		thePanel.getPanelRegisteredSoftware().setSelectedValues(keys, WINDOWS_SOFTWARE_ID_KEY_COL);

		if (keys != null && !keys.isEmpty()) {
			thePanel.getPanelRegisteredSoftware().moveToValue(keys.get(keys.size() - 1), WINDOWS_SOFTWARE_ID_KEY_COL,
					false);
		}
	}

	private void produceFilter1(List<String> assignedWindowsSoftwareIds) {
		TreeSet<Object> filter1 = null;

		if (softwareShowAllMeans != SoftwareShowAllMeans.ALL) {
			filter1 = new TreeSet<>(
					persistenceController.getDataServices().software.getSoftwareWithoutAssociatedLicensePoolPD());
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

		thePanel.getPanelRegisteredSoftware().getTableSearchPane().setFilterMark(usingShowSelectedFilter);
		setVisualSelection(selectedKeys);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		Logging.info(this, "modelWindowsSoftwareIds row count 2 ", modelWindowsSoftwareIds.getRowCount());
		thePanel.getFieldCountDisplayedWindowsSoftware().setText(produceCount(totalShownEntries));
		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(true);
		thePanel.getPanelRegisteredSoftware().setDataChanged(b);
	}

	public void setSoftwareShowAllMeans(SoftwareShowAllMeans meaning) {
		SoftwareShowAllMeans softwareShowAllMeansOld = softwareShowAllMeans;
		softwareShowAllMeans = meaning;

		if (softwareShowAllMeansOld != softwareShowAllMeans) {
			boolean tableChangeAware = thePanel.getPanelRegisteredSoftware().isAwareOfTableChangedListener();
			thePanel.getPanelRegisteredSoftware().setAwareOfTableChangedListener(false);

			String selectedLicensePool = null;
			Logging.info(this, " setShowOnlyAssociationsToSelectedPoolOrNoPool selected license row ",
					thePanel.getPanelLicensepools().getGenEditTable().getSelectedRow());
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
				thePanel.getPanelRegisteredSoftware().getTableSearchPane().setFiltering();
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

	private boolean gotNewSWKeysForLicensePool(String selectedLicensePool) {
		if (selectedLicensePool == null) {
			return false;
		}

		List<String> oldSWList = persistenceController.getDataServices().software
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
		if (thePanel.getPanelLicensepools().getGenEditTable().getSelectedRow() >= 0) {
			return thePanel.getPanelLicensepools().getGenEditTable()
					.getValueAt(thePanel.getPanelLicensepools().getGenEditTable().getSelectedRow(), 0).toString();
		} else {
			return null;
		}
	}

	@Override
	public void initializeVisualSettings() {
		super.initializeVisualSettings();
		setSoftwareIdsFromLicensePool(null);
		resetCounters(null);

		thePanel.getPanelProductId2LPool().getGenEditTable().clearSelection();
		thePanel.getPanelLicensepools().getGenEditTable().clearSelection();
		thePanel.getPanelRegisteredSoftware().getGenEditTable().clearSelection();
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);
	}
}
