/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableColumn;

import de.uib.Main;
import de.uib.configed.gui.FGlobalSoftwareInfo;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.licences.PanelAssignToLPools;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.DefaultTableModelFilterCondition;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilter;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.gui.AdaptingCellEditor;
import de.uib.utilities.table.gui.BooleanIconTableCellRenderer;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.SelectionMemorizerUpdateController;
import de.uib.utilities.table.updates.TableEditItem;
import utils.Utils;

public class ControlPanelAssignToLPools extends AbstractControlMultiTablePanel {
	private static final int MAX_WIDTH_ID_COLUMN_FOR_REGISTERED_SOFTWARE = 300;

	// introducing a column for displaying the cursor row
	public static final int WINDOWS_SOFTWARE_ID_KEY_COL = 1;

	private static final String LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED = "restrictToNonAssociated";

	private PanelAssignToLPools thePanel;

	private GenTableModel modelLicencepools;
	private GenTableModel modelProductId2LPool;
	private GenTableModel modelWindowsSoftwareIds;

	// we replace the filter from GenTableModel
	private TableModelFilterCondition windowsSoftwareFilterConditonShowOnlySelected;

	private TableModelFilterCondition windowsSoftwareFilterConditionDontShowAssociatedToOtherPool;

	private ConfigedMain mainController;

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

	private int colMarkCursorRow;

	private Map<String, List<String>> removeKeysFromOtherLicencePool;

	public ControlPanelAssignToLPools(ConfigedMain mainController) {
		thePanel = new PanelAssignToLPools(this);
		this.mainController = mainController;
		init();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	private NavigableSet<Object> getUnAssignedSoftwareIds() {
		// the object is cached in persist
		return persistenceController.getSoftwareWithoutAssociatedLicencePool();
	}

	public void setSoftwareIdsFromLicencePool() {
		String selectedLicencePool = getSelectedLicencePool();
		Logging.info(this, "setSoftwareIdsFromLicencePoot, selectedLicencePool " + selectedLicencePool);

		setSoftwareIdsFromLicencePool(selectedLicencePool);

	}

	public void setSoftwareIdsFromLicencePool(final String poolID) {
		Logging.info(this,
				"setSoftwareIdsFromLicencePool " + poolID + " should be thePanel.panelLicencepools.getSelectedRow() "
						+ thePanel.getPanelLicencepools().getSelectedRow());
		Logging.info(this, "setSoftwareIdsFromLicencePool, call thePanel.fSoftwarename2LicencePool.setGlobalPool ");
		if (thePanel.getFSoftwarename2LicencePool() != null) {
			thePanel.getFSoftwarename2LicencePool().setGlobalPool(poolID);
		}

		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(false);

		List<String> selectKeys = thePanel.getPanelRegisteredSoftware().getSelectedKeys();

		boolean wasUsingSelectedFilter = modelWindowsSoftwareIds
				.isUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED);
		Logging.info(this, "setSoftwareIdsFromLicencePool wasUsingSelectedFilter " + wasUsingSelectedFilter);

		// wasUsingSelectedFilter
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED, false);

		modelWindowsSoftwareIds.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED, false);

		thePanel.getPanelRegisteredSoftware().showFiltered(false);

		thePanel.getFieldSelectedLicencePoolId().setText(poolID);
		thePanel.getFieldSelectedLicencePoolId().setToolTipText(poolID);

		List<String> softwareIdsForPool = new ArrayList<>();
		if (poolID != null) {
			softwareIdsForPool = persistenceController.getSoftwareListByLicencePool(poolID);
		}

		Logging.info(this, "setSoftwareIdsFromLicencePool  softwareIds for licencePool  " + poolID + " : "
				+ softwareIdsForPool.size());
		Logging.info(this, "setSoftwareIdsFromLicencePool  unknown softwareIds for licencePool  " + poolID + " : "
				+ persistenceController.getUnknownSoftwareListForLicencePool(poolID).size());

		Integer totalUnassignedSWEntries = getUnAssignedSoftwareIds().size();
		Logging.info(this, "setSoftwareIdsFromLicencePool unAssignedSoftwareIds " + totalUnassignedSWEntries);

		resetCounters(poolID);
		thePanel.getFieldCountAllWindowsSoftware().setText("0");

		thePanel.getButtonShowAssignedNotExisting()
				.setEnabled(!persistenceController.getUnknownSoftwareListForLicencePool(poolID).isEmpty());
		if (thePanel.getFMissingSoftwareInfo() == null) {
			thePanel.setFMissingSoftwareInfo(new FGlobalSoftwareInfo(Utils.getMasterFrame(), this));
		}

		if (!persistenceController.getUnknownSoftwareListForLicencePool(poolID).isEmpty()) {
			Map<String, Map<String, Object>> missingSoftwareMap = new HashMap<>();

			for (String ID : persistenceController.getUnknownSoftwareListForLicencePool(poolID)) {
				String[] rowValues = ID.split(Globals.PSEUDO_KEY_SEPARATOR);

				Map<String, Object> rowMap = new HashMap<>();
				for (String colName : thePanel.getFMissingSoftwareInfo().getColumnNames()) {
					rowMap.put(colName, "");
				}

				rowMap.put("ID", ID);

				List<String> identKeys = SWAuditEntry.KEYS_FOR_IDENT;
				if (rowValues.length != identKeys.size()) {
					Logging.warning(this, "illegal ID " + ID);
				} else {
					int i = 0;
					for (String key : identKeys) {
						rowMap.put(key, rowValues[i]);
						i++;
					}
				}

				rowMap.put("ID", ID);
				Logging.info(this, "unknownSoftwareIdsForPool " + rowMap);

				missingSoftwareMap.put(ID, rowMap);
			}

			thePanel.getFMissingSoftwareInfo().setTableModel(new GenTableModel(
					new MapTableUpdateItemFactory(thePanel.getFMissingSoftwareInfo().getColumnNames(), 0), // dummy
					new DefaultTableProvider(new RetrieverMapSource(thePanel.getFMissingSoftwareInfo().getColumnNames(),
							thePanel.getFMissingSoftwareInfo().getClassNames(), () -> missingSoftwareMap)),
					0, new int[] {}, thePanel.getFMissingSoftwareInfo().getPanelGlobalSoftware(), updateCollection));

		}

		thePanel.getFieldCountAssignedStatus().setToolTipText(" <html><br /></html>");

		// softwareIdsForPool is guaranteed not null
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

		Logging.info(this, "setSoftwareIdsFromLicencePool setUsingFilter "
				+ GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED + " to " + wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED,
				wasUsingSelectedFilter);
		thePanel.getPanelRegisteredSoftware().showFiltered(wasUsingSelectedFilter);

		modelWindowsSoftwareIds.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED,
				getSoftwareShowAllMeans() != SoftwareShowAllMeans.ALL);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		Logging.info(this, "modelWindowsSoftwareIds row count " + totalShownEntries);
		thePanel.getFieldCountAllWindowsSoftware().setText(produceCount(totalSWEntries));
		thePanel.getFieldCountDisplayedWindowsSoftware().setText(produceCount(totalShownEntries));
		thePanel.getFieldCountNotAssignedSoftware().setText(produceCount(totalUnassignedSWEntries));

		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
			selectKeys = softwareIdsForPool;
			thePanel.getFieldCountAssignedInEditing().setText(produceCount(softwareIdsForPool.size(), poolID == null));
		} else {
			// selectKeys old keys
			Set<Object> existingKeys = modelWindowsSoftwareIds.getExistingKeys();
			int count = 0;
			for (String key : selectKeys) {
				if (existingKeys.contains(key)) {
					count++;
				}
			}

			thePanel.getFieldCountAssignedInEditing().setText(produceCount(count));
		}

		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(false);
		Logging.debug(this, "setSoftwareIdsFromLicencePool  setSelectedValues " + selectKeys);
		thePanel.getPanelRegisteredSoftware().setSelectedValues(selectKeys, WINDOWS_SOFTWARE_ID_KEY_COL);

		if (!selectKeys.isEmpty()) {
			thePanel.getPanelRegisteredSoftware().moveToValue(selectKeys.get(selectKeys.size() - 1),
					WINDOWS_SOFTWARE_ID_KEY_COL, false);
		}

		Logging.debug(this, "setSoftwareIdsFromLicencePool  selectedKeys "
				+ thePanel.getPanelRegisteredSoftware().getSelectedKeys());
		if (wasUsingSelectedFilter) {
			setVisualSelection(thePanel.getPanelRegisteredSoftware().getSelectedKeys());
		}
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);
		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(true);

	}

	private static String produceCount(Integer count) {
		if (count == null || count < 0) {
			return "";
		}
		return "" + count;
	}

	private static String produceCount(Integer count, boolean licencePoolNull) {
		if (count == null || licencePoolNull || count < 0) {
			return "";
		}
		return "" + count;
	}

	private void resetCounters(String licencePoolId) {
		Logging.info(this, "resetCounters for pool " + licencePoolId);
		String baseCount = "0";
		if (licencePoolId == null) {
			baseCount = "";
		}

		thePanel.getFieldCountAssignedStatus().setText(baseCount);
		thePanel.getFieldCountAssignedInEditing().setText(baseCount);
		thePanel.getButtonShowAssignedNotExisting().setEnabled(false);
	}

	// called by valueChanged method of ListSelectionListener
	public void validateWindowsSoftwareKeys() {

		String selectedLicencePool = getSelectedLicencePool();
		Logging.debug(this, "validateWindowsSoftwareKeys for licencePoolID " + selectedLicencePool);

		if (selectedLicencePool == null) {
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
		Logging.info(this, "validateWindowsSoftwareKeys selectedKeys " + showSelKeys
				+ " associated to selectedLicencePool " + selectedLicencePool);

		if (selKeys == null) {
			resetCounters(selectedLicencePool);
			return;
		}

		List<String> cancelSelectionKeys = new ArrayList<>();
		removeKeysFromOtherLicencePool = new HashMap<>();

		for (String key : selKeys) {
			// key is already assigned to a different licencePool?

			boolean gotAssociation = persistenceController.getFSoftware2LicencePool(key) != null;
			Logging.debug(this, "validateWindowsSoftwareKeys key " + key + " gotAssociation " + gotAssociation);

			Boolean newAssociation = null;
			if (gotAssociation) {
				newAssociation = !(persistenceController.getFSoftware2LicencePool(key).equals(selectedLicencePool));
				Logging.debug(this, "validateWindowsSoftwareKeys has association to "
						+ persistenceController.getFSoftware2LicencePool(key));
			}

			if (Boolean.TRUE.equals(newAssociation)) {
				String otherPool = persistenceController.getFSoftware2LicencePool(key);

				if (otherPool.equals(FSoftwarename2LicencePool.VALUE_NO_LICENCE_POOL)) {
					Logging.info(this, "validateWindowsSoftwareKeys, assigned to valNoLicencepool");
				} else {
					String info = Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned")
							+ "\n\n" + otherPool;
					String option = Configed
							.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.options");
					String title = Configed
							.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.title");

					Logging.info(
							" software with ident \"" + key + "\" already associated to license pool " + otherPool);

					FTextArea dialog = new FTextArea(Utils.getMasterFrame(), Globals.APPNAME + " " + title, true,
							new String[] {
									Configed.getResourceValue(
											"PanelAssignToLPools.warningSoftwareAlreadyAssigned.option1"),
									Configed.getResourceValue(
											"PanelAssignToLPools.warningSoftwareAlreadyAssigned.option2") },
							400, 200);
					dialog.setMessage(info + "\n\n" + option);
					dialog.setVisible(true);

					Logging.info(this, "validateWindowsSoftwareKeys result " + dialog.getResult());

					if (dialog.getResult() == 1) {
						// we cancel the new selection
						cancelSelectionKeys.add(key);
					} else {
						// or delete the assignment to the licence pool
						List<String> removeKeys = removeKeysFromOtherLicencePool.computeIfAbsent(otherPool,
								s -> new ArrayList<>());
						removeKeys.add(key);
					}
				}
			}
		}

		Logging.info(this, "cancelSelectionKeys " + cancelSelectionKeys);
		// without this condition we run into a loop
		if (!cancelSelectionKeys.isEmpty()) {
			selKeys.removeAll(cancelSelectionKeys);
			Logging.info(this, "selKeys after removal " + selKeys);
			thePanel.getPanelRegisteredSoftware().setSelectedValues(selKeys, WINDOWS_SOFTWARE_ID_KEY_COL);
		}

		thePanel.getFieldCountAssignedInEditing().setText("" + selKeys.size());
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<TableEditItem>();

		List<String> columnNames;

		// --- panelLicencepools
		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		MapTableUpdateItemFactory updateItemFactoryLicencepools = new MapTableUpdateItemFactory(modelLicencepools,
				columnNames, 0);
		modelLicencepools = new GenTableModel(updateItemFactoryLicencepools, mainController.licencePoolTableProvider, 0,
				thePanel.getPanelLicencepools(), updateCollection);
		updateItemFactoryLicencepools.setSource(modelLicencepools);

		tableModels.add(modelLicencepools);
		tablePanes.add(thePanel.getPanelLicencepools());

		modelLicencepools.reset();
		thePanel.getPanelLicencepools().setTableModel(modelLicencepools);
		modelLicencepools.setEditableColumns(new int[] { 0, 1 });
		thePanel.getPanelLicencepools().setEmphasizedColumns(new int[] { 0, 1 });

		JMenuItemFormatted menuItemAddPool = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.NewLicencepool"));
		menuItemAddPool.addActionListener((ActionEvent e) -> {
			Object[] a = new Object[2];
			a[0] = "";
			a[1] = "";
			modelLicencepools.addRow(a);
			thePanel.getPanelLicencepools().moveToValue("" + a[0], 0);

			// setting back the other tables is provided by ListSelectionListener
		});

		thePanel.getPanelLicencepools().addPopupItem(menuItemAddPool);

		thePanel.getPanelLicencepools().setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// updates
		thePanel.getPanelLicencepools().setUpdateController(
				new MapItemsUpdateController(thePanel.getPanelLicencepools(), modelLicencepools, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return updateLicencepool(rowmap);
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						return deleteLicencepool(rowmap);
					}
				}, updateCollection));

		// --- panelProductId2LPool
		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("productId");
		List<String> classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryProductId2LPool = new MapTableUpdateItemFactory(modelProductId2LPool,
				columnNames, 0);
		modelProductId2LPool = new GenTableModel(updateItemFactoryProductId2LPool,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames,
						() -> (Map) persistenceController.getRelationsProductId2LPool())),
				-1, new int[] { 0, 1 }, thePanel.getPanelProductId2LPool(), updateCollection);
		updateItemFactoryProductId2LPool.setSource(modelProductId2LPool);

		tableModels.add(modelProductId2LPool);
		tablePanes.add(thePanel.getPanelProductId2LPool());

		modelProductId2LPool.reset();
		thePanel.getPanelProductId2LPool().setTableModel(modelProductId2LPool);
		modelProductId2LPool.setEditableColumns(new int[] { 0, 1 });
		thePanel.getPanelProductId2LPool().setEmphasizedColumns(new int[] { 0, 1 });

		JMenuItemFormatted menuItemAddRelationProductId2LPool = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.NewRelationProductId2LPool"));
		menuItemAddRelationProductId2LPool.addActionListener((ActionEvent e) -> {
			Object[] a = new Object[2];
			a[0] = "";
			if (thePanel.getPanelLicencepools().getSelectedRow() > -1) {
				a[0] = modelLicencepools.getValueAt(thePanel.getPanelLicencepools().getSelectedRowInModelTerms(), 0);
			}

			a[1] = "";

			modelProductId2LPool.addRow(a);

			thePanel.getPanelProductId2LPool().moveToValue("" + a[0], 0);
		});

		thePanel.getPanelProductId2LPool().addPopupItem(menuItemAddRelationProductId2LPool);

		// special treatment of columns
		TableColumn col = thePanel.getPanelProductId2LPool().getColumnModel().getColumn(0);
		JComboBox<String> comboLP0 = new JComboBox<>();
		if (!Main.FONT) {
			comboLP0.setFont(Globals.DEFAULT_FONT_BIG);
		}
		col.setCellEditor(new AdaptingCellEditor(comboLP0, (int row, int column) -> {
			List<String> poolIds = mainController.licencePoolTableProvider.getOrderedColumn(
					mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1) {
				poolIds.add("");
			}
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		col = thePanel.getPanelProductId2LPool().getColumnModel().getColumn(1);
		JComboBox<String> comboLP1 = new JComboBox<>();
		if (!Main.FONT) {
			comboLP1.setFont(Globals.DEFAULT_FONT_BIG);
		}
		col.setCellEditor(new AdaptingCellEditor(comboLP1, (row,
				column) -> new DefaultComboBoxModel<>(persistenceController.getProductIds().toArray(new String[0]))));

		// updates
		thePanel.getPanelProductId2LPool().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelProductId2LPool(), modelProductId2LPool, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> m) {
						return persistenceController.editRelationProductId2LPool((String) m.get("productId"),
								(String) m.get("licensePoolId"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> m) {
						modelProductId2LPool.requestReload();
						return persistenceController.deleteRelationProductId2LPool((String) m.get("productId"),
								(String) m.get("licensePoolId"));
					}
				}, updateCollection));

		// --- panelRegisteredSoftware

		columnNames = new ArrayList<>(SWAuditEntry.getDisplayKeys());

		// introducing a column for displaying the cursor row
		columnNames.add(colMarkCursorRow, "CURSOR");

		columnNames.remove("licenseKey");

		classNames = new ArrayList<>();
		for (int i = 0; i <= columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		// introducing a column for displaying the cursor row
		classNames.set(colMarkCursorRow, "java.lang.Boolean");

		Logging.info(this, "panelRegisteredSoftware constructed with (size) cols " + "(" + columnNames.size() + ") "
				+ columnNames);
		Logging.info(this, "panelRegisteredSoftware constructed with (size) classes " + "(" + classNames.size() + ") "
				+ classNames);

		boolean withRowCounter = false;
		modelWindowsSoftwareIds = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, () -> {
					persistenceController.installedSoftwareInformationRequestRefresh();
					return (Map) persistenceController.getInstalledSoftwareInformationForLicensing();

				}, withRowCounter)), WINDOWS_SOFTWARE_ID_KEY_COL, new int[] {}, thePanel.getPanelRegisteredSoftware(),
				updateCollection);

		Logging.info(this, "modelWindowsSoftwareIds row count " + modelWindowsSoftwareIds.getRowCount());
		tableModels.add(modelWindowsSoftwareIds);
		tablePanes.add(thePanel.getPanelRegisteredSoftware());

		modelWindowsSoftwareIds.reset();
		modelWindowsSoftwareIds.setColMarkCursorRow(colMarkCursorRow);

		thePanel.getPanelRegisteredSoftware().setTableModel(modelWindowsSoftwareIds);
		thePanel.getPanelRegisteredSoftware().setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modelWindowsSoftwareIds.setEditableColumns(new int[] {});
		thePanel.getPanelRegisteredSoftware().setEmphasizedColumns(new int[] {});

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
		thePanel.getPanelRegisteredSoftware().showFilterIcon(true);
		thePanel.getPanelRegisteredSoftware().setFiltermarkToolTipText(
				Configed.getResourceValue("PanelAssignToLPools.searchPane.filtermark.tooltip"));

		windowsSoftwareFilterConditionDontShowAssociatedToOtherPool = new DefaultTableModelFilterCondition(
				WINDOWS_SOFTWARE_ID_KEY_COL);
		modelWindowsSoftwareIds.chainFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED,
				new TableModelFilter(windowsSoftwareFilterConditionDontShowAssociatedToOtherPool));
		modelWindowsSoftwareIds.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_ONLY_NON_ASSOCIATED, false);

		thePanel.getPanelRegisteredSoftware().showFiltered(false);
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);

		JMenuItemFormatted menuItemSoftwareShowAssigned = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAssigned"));
		menuItemSoftwareShowAssigned.addActionListener((ActionEvent e) -> {
			// save values
			softwareShow = SoftwareShowMode.ASSIGNED;
			setSWAssignments();
		});

		thePanel.getPanelRegisteredSoftware()
				.setFiltermarkActionListener(actionEvent -> registeredSoftwareFiltermarkAction());

		JMenuItemFormatted menuItemSoftwareShowAll = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAll"));
		menuItemSoftwareShowAll.addActionListener((ActionEvent e) -> {
			softwareShow = SoftwareShowMode.ALL;
			setSWAssignments();
		});

		thePanel.getPanelRegisteredSoftware().addPopupItem(menuItemSoftwareShowAll);
		thePanel.getPanelRegisteredSoftware().addPopupItem(menuItemSoftwareShowAssigned);

		// special treatment of columns

		if (colMarkCursorRow > -1) {

			// row cursor column
			col = thePanel.getPanelRegisteredSoftware().getColumnModel().getColumn(colMarkCursorRow);
			col.setMaxWidth(12);
			col.setHeaderValue("");

			col.setCellRenderer(
					new BooleanIconTableCellRenderer(Utils.createImageIcon("images/minibarpointerred.png", ""),
							Utils.createImageIcon("images/minibarpointervoid.png", "")));
		}

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

		// updates
		thePanel.getPanelRegisteredSoftware().setUpdateController(new SelectionMemorizerUpdateController(
				thePanel.getPanelLicencepools(), 0, thePanel.getPanelRegisteredSoftware(), this));

		// -- Softwarename --> LicencePool

		Logging.info(this, "frame Softwarename --> LicencePool  in " + Utils.getMasterFrame());

		final ControlPanelAssignToLPools contr = this;
		thePanel.setFSoftwarename2LicencePool(new FSoftwarename2LicencePool(Utils.getMasterFrame(), contr));
		thePanel.getFSoftwarename2LicencePool().setTableModel(); // test
		thePanel.setDisplaySimilarExist(
				thePanel.getFSoftwarename2LicencePool().checkExistNamesWithVariantLicencepools());
		thePanel.getFSoftwarename2LicencePool().setButtonsEnabled(true);

		// combine
		thePanel.getPanelLicencepools().getListSelectionModel().addListSelectionListener(this::licencePoolValueChanged);

		setSoftwareIdsFromLicencePool(null);
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
			Logging.warning(this,
					"softwareShow has Value " + softwareShow + " that does not exist in SoftwareShowMode");
		}
	}

	private String updateLicencepool(Map<String, Object> rowmap) {
		// hack for avoiding unvoluntary reuse of a licence pool id
		boolean existsNewRow = mainController.licencePoolTableProvider.getRows().size() < modelLicencepools
				.getRowCount();

		if (existsNewRow && persistenceController.getLicencepools().containsKey(rowmap.get("licensePoolId"))) {
			// but we leave it until the service methods reflect the situation more
			// accurately

			String info = Configed.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists") + " \n(\""
					+ rowmap.get("licensePoolId") + "\" ?)";

			String title = Configed.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists.title");

			JOptionPane.showMessageDialog(thePanel, info, title, JOptionPane.INFORMATION_MESSAGE);

			return null;
		}

		if (existsNewRow) {
			modelLicencepools.requestReload();
		}

		return persistenceController.editLicencePool((String) rowmap.get(LicencepoolEntry.ID_SERVICE_KEY),
				(String) rowmap.get(LicencepoolEntry.DESCRIPTION_KEY));
	}

	public boolean updateLicencepool(String poolId, List<String> softwareIds) {

		Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
		Logging.info(this, "sendUpdate poolId, removeKeysFromOtherLicencePool " + removeKeysFromOtherLicencePool);

		boolean result = true;

		if (removeKeysFromOtherLicencePool != null) {
			for (Entry<String, List<String>> otherStringPoolEntry : removeKeysFromOtherLicencePool.entrySet()) {
				if (result && !otherStringPoolEntry.getValue().isEmpty()) {
					result = persistenceController.removeAssociations(otherStringPoolEntry.getKey(),
							otherStringPoolEntry.getValue());
					if (result) {
						removeKeysFromOtherLicencePool.remove(otherStringPoolEntry.getKey());
					}
				}
			}
		}

		if (!result) {
			return false;
		}

		// cleanup assignments to other pools since an update would not change them
		// (redmine #3282)
		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
			result = persistenceController.setWindowsSoftwareIds2LPool(poolId, softwareIds);
		} else {
			result = persistenceController.addWindowsSoftwareIds2LPool(poolId, softwareIds);
		}

		Logging.info(this, "sendUpdate, setSoftwareIdsFromLicencePool poolId " + poolId);
		setSoftwareIdsFromLicencePool(poolId);

		// doing it locally for fSoftware2LicencePool
		Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
		Logging.info(this, "sendUpdate, we have software ids " + softwareIds.size());
		Logging.info(this,
				"sendUpdate, we have software ids " + persistenceController.getSoftwareListByLicencePool(poolId).size()
						+ " they are " + persistenceController.getSoftwareListByLicencePool(poolId));

		List<String> oldSWListForPool = persistenceController.getSoftwareListByLicencePool(poolId);

		// remove all old assignements
		for (String swId : oldSWListForPool) {
			Logging.info(this, "sendUpdate remove " + swId + " from Software2LicencePool ");
			persistenceController.getFSoftware2LicencePool().remove(swId);
		}
		// set the current ones
		for (String ident : softwareIds) {
			persistenceController.setFSoftware2LicencePool(ident, poolId);
		}

		if (thePanel.getFSoftwarename2LicencePool() != null) {
			thePanel.getFSoftwarename2LicencePool().getPanelSWnames().requestReload();
		}

		if (thePanel.getFSoftwarename2LicencePool() != null) {
			thePanel.getFSoftwarename2LicencePool().getPanelSWxLicencepool().requestReload();
		}

		return result;
	}

	private boolean deleteLicencepool(Map<String, Object> rowmap) {
		modelLicencepools.requestReload();
		return persistenceController.deleteLicencePool((String) rowmap.get("licensePoolId"));
	}

	private void licencePoolValueChanged(ListSelectionEvent listSelectionEvent) {
		// Ignore extra messages.
		if (listSelectionEvent.getValueIsAdjusting()) {
			return;
		}

		String selectedLicencePool = null;

		// clear selection
		thePanel.getPanelProductId2LPool().setSelectedValues(null, 0);

		ListSelectionModel lsm = (ListSelectionModel) listSelectionEvent.getSource();

		if (lsm.isSelectionEmpty()) {
			Logging.debug(this, "no rows selected");
		} else {
			selectedLicencePool = getSelectedLicencePool();
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
			thePanel.getPanelProductId2LPool().moveToValue(selectedLicencePool, 0);
		}

		setSoftwareIdsFromLicencePool(selectedLicencePool);

		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.SOFTWARE2POOL) {
			thePanel.getPanelRegisteredSoftware().setDataChanged(gotNewSWKeysForLicencePool(selectedLicencePool));
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

		Logging.info(this, "produceFilterSets setFilter dontShowAssociatedToOtherPool " + filterInfo);

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
		Logging.info(this, "produceFilterSets setFilter dontShowAssociatedToOtherPool " + filterInfo);
		windowsSoftwareFilterConditonShowOnlySelected.setFilter(filter0);

		produceFilter1(assignedWindowsSoftwareIds);
	}

	private void setSWAssignments() {
		// save values
		boolean b = thePanel.getPanelRegisteredSoftware().isDataChanged();
		thePanel.getPanelRegisteredSoftware().setAwareOfSelectionListener(false);

		List<String> selectedKeys = thePanel.getPanelRegisteredSoftware().getSelectedKeys();
		Logging.info(this, "setSWAssignments  selectedKeys " + selectedKeys);

		Logging.info(this, "setSWAssignments usingFilter " + (softwareShow == SoftwareShowMode.ASSIGNED)
				+ " selected keys " + selectedKeys);

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
		Logging.info(this, "modelWindowsSoftwareIds row count 2 " + modelWindowsSoftwareIds.getRowCount());
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

			String selectedLicencePool = null;
			Logging.info(this, " setShowOnlyAssociationsToSelectedPoolOrNoPool selected licence row "
					+ thePanel.getPanelLicencepools().getSelectedRow());
			selectedLicencePool = getSelectedLicencePool();
			setSoftwareIdsFromLicencePool(selectedLicencePool);

			thePanel.getPanelRegisteredSoftware().setAwareOfTableChangedListener(tableChangeAware);
		}
	}

	public void setSoftwareDirectionOfAssignment(SoftwareDirectionOfAssignment direction) {
		SoftwareDirectionOfAssignment oldDirection = softwareDirectionOfAssignment;
		this.softwareDirectionOfAssignment = direction;

		if (oldDirection != direction) {
			if (direction == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
				thePanel.getPanelRegisteredSoftware().getTheSearchpane().showFilterIcon(true);
			} else if (direction == SoftwareDirectionOfAssignment.SOFTWARE2POOL) {
				thePanel.getPanelRegisteredSoftware().getTheSearchpane().showFilterIcon(false);
				resetCounters(null);
				thePanel.getFieldCountAssignedInEditing().setText("");
			} else {
				// Should not happen because enum SoftwareDirectionOfAssignment has only two values
			}

			Logging.info(this, "switched to " + direction);
			initializeVisualSettings();
		}
	}

	public SoftwareDirectionOfAssignment getSoftwareDirectionOfAssignment() {
		return this.softwareDirectionOfAssignment;
	}

	private boolean gotNewSWKeysForLicencePool(String selectedLicencePool) {
		if (selectedLicencePool == null) {
			return false;
		}

		List<String> oldSWList = persistenceController.getSoftwareListByLicencePool(selectedLicencePool);
		List<String> newKeys = new ArrayList<>(thePanel.getPanelRegisteredSoftware().getSelectedKeys());
		newKeys.removeAll(oldSWList);

		Logging.info(this, "new keys " + newKeys);

		return !newKeys.isEmpty();
	}

	public boolean acknowledgeChangeForSWList() {
		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
			// any visual change is regarded as a data change
			return true;
		} else {
			// a change in visual sw list is regarded as data changed if there additional
			// entries

			String selectedLicencePool = getSelectedLicencePool();
			if (selectedLicencePool == null) {
				return false;
			}

			validateWindowsSoftwareKeys();

			return gotNewSWKeysForLicencePool(selectedLicencePool);
		}
	}

	public String getSelectedLicencePool() {
		String result = null;

		if (thePanel.getPanelLicencepools().getSelectedRow() >= 0) {
			result = thePanel.getPanelLicencepools().getValueAt(thePanel.getPanelLicencepools().getSelectedRow(), 0)
					.toString();
		}

		return result;
	}

	@Override
	public void initializeVisualSettings() {
		super.initializeVisualSettings();
		setSoftwareIdsFromLicencePool(null);
		resetCounters(null);

		thePanel.getPanelProductId2LPool().getTheTable().clearSelection();
		thePanel.getPanelLicencepools().getTheTable().clearSelection();
		thePanel.getPanelRegisteredSoftware().getTheTable().clearSelection();
		thePanel.getPanelRegisteredSoftware().setDataChanged(false);
	}

	public PanelAssignToLPools getPanel() {
		return thePanel;
	}
}
