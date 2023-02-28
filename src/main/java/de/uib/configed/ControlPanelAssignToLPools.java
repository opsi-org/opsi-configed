package de.uib.configed;

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
import javax.swing.event.ListSelectionListener;

import de.uib.configed.gui.FGlobalSoftwareInfo;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.gui.licences.PanelAssignToLPools;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.DefaultTableModelFilterCondition;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilter;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.gui.AdaptingCellEditor;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.AbstractSelectionMemorizerUpdateController;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.StrList2BooleanFunction;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlPanelAssignToLPools extends AbstractControlMultiTablePanel {
	private static final int MAX_WIDTH_ID_COLUMN_FOR_REGISTERED_SOFTWARE = 300;
	public PanelAssignToLPools thePanel;

	GenTableModel modelLicencepools;
	GenTableModel modelProductId2LPool;
	GenTableModel modelWindowsSoftwareIds;

	// we replace the filter from GenTableModel
	TableModelFilterCondition windowsSoftwareFilterConditonShowOnlySelected;

	TableModelFilterCondition windowsSoftwareFilterConditionDontShowAssociatedToOtherPool;
	private static final String LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_DONT_SHOW_ASSOCIATED_TO_OTHER_POOL = "restrictToNonAssociated";

	ConfigedMain mainController;

	public AbstractPersistenceController persist;

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

	Integer totalSWEntries;
	Integer totalUnassignedSWEntries;
	Integer totalShownEntries;

	// introducing a column for displaying the cursor row
	public static final int WINDOWS_SOFTWARE_ID_KEY_COL = 1;
	int colMarkCursorRow = 0;

	private Map<String, List<String>> removeKeysFromOtherLicencePool;

	public ControlPanelAssignToLPools(AbstractPersistenceController persist, ConfigedMain mainController) {
		thePanel = new PanelAssignToLPools(this);
		this.persist = persist;
		this.mainController = mainController;
		init();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	private NavigableSet<Object> getUnAssignedSoftwareIds() {
		// the object is cached in persist
		return persist.getSoftwareWithoutAssociatedLicencePool();
	}

	public void setSoftwareIdsFromLicencePool() {
		String selectedLicencePool = getSelectedLicencePool();
		Logging.info(this, "setSoftwareIdsFromLicencePoot, selectedLicencePool " + selectedLicencePool);

		setSoftwareIdsFromLicencePool(selectedLicencePool);

	}

	private void setSoftwareIdsFromLicencePool(final String poolID) {
		Logging.info(this,
				"setSoftwareIdsFromLicencePool " + poolID + " should be thePanel.panelLicencepools.getSelectedRow() "
						+ thePanel.panelLicencepools.getSelectedRow());
		Logging.info(this, "setSoftwareIdsFromLicencePool, call thePanel.fSoftwarename2LicencePool.setGlobalPool ");
		if (thePanel.fSoftwarename2LicencePool != null) {
			thePanel.fSoftwarename2LicencePool.setGlobalPool(poolID);
		}

		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);

		List<String> selectKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();

		boolean wasUsingSelectedFilter = modelWindowsSoftwareIds
				.isUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED);
		Logging.info(this, "setSoftwareIdsFromLicencePool wasUsingSelectedFilter " + wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED, false); // wasUsingSelectedFilter

		modelWindowsSoftwareIds
				.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_DONT_SHOW_ASSOCIATED_TO_OTHER_POOL, false);

		thePanel.panelRegisteredSoftware.showFiltered(false);

		thePanel.fieldSelectedLicencePoolId.setText(poolID);
		thePanel.fieldSelectedLicencePoolId.setToolTipText(poolID);

		List<String> softwareIdsForPool = new ArrayList<>();
		if (poolID != null) {
			softwareIdsForPool = persist.getSoftwareListByLicencePool(poolID);
		}

		Logging.info(this, "setSoftwareIdsFromLicencePool  softwareIds for licencePool  " + poolID + " : "
				+ softwareIdsForPool.size());
		Logging.info(this, "setSoftwareIdsFromLicencePool  unknown softwareIds for licencePool  " + poolID + " : "
				+ persist.getUnknownSoftwareListForLicencePool(poolID).size());

		totalUnassignedSWEntries = getUnAssignedSoftwareIds().size();
		Logging.info(this, "setSoftwareIdsFromLicencePool unAssignedSoftwareIds " + totalUnassignedSWEntries);

		resetCounters(poolID);
		thePanel.fieldCountAllWindowsSoftware.setText("0");

		thePanel.buttonShowAssignedNotExisting
				.setEnabled(!persist.getUnknownSoftwareListForLicencePool(poolID).isEmpty());
		if (thePanel.fMissingSoftwareInfo == null) {
			thePanel.fMissingSoftwareInfo = new FGlobalSoftwareInfo(Globals.frame1, this);
		}

		if (!persist.getUnknownSoftwareListForLicencePool(poolID).isEmpty()) {
			Map<String, Map<String, Object>> missingSoftwareMap = new HashMap<>();

			for (String ID : persist.getUnknownSoftwareListForLicencePool(poolID)) {
				String[] rowValues = ID.split(Globals.PSEUDO_KEY_SEPARATOR);

				Map<String, Object> rowMap = new HashMap<>();
				for (String colName : thePanel.fMissingSoftwareInfo.columnNames) {
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

			thePanel.fMissingSoftwareInfo.setTableModel(new GenTableModel(
					new MapTableUpdateItemFactory(thePanel.fMissingSoftwareInfo.columnNames,
							thePanel.fMissingSoftwareInfo.classNames, 0), // dummy
					new DefaultTableProvider(new RetrieverMapSource(thePanel.fMissingSoftwareInfo.columnNames,
							thePanel.fMissingSoftwareInfo.classNames, () -> missingSoftwareMap)),
					0, new int[] {}, thePanel.fMissingSoftwareInfo.panelGlobalSoftware, updateCollection));

		}

		thePanel.fieldCountAssignedStatus.setToolTipText(" <html><br /></html>");

		// softwareIdsForPool is guaranteed not null
		thePanel.fieldCountAssignedStatus.setText(produceCount(softwareIdsForPool.size(), (poolID == null)));

		StringBuilder b = new StringBuilder("<html>");
		b.append(Configed.getResourceValue("PanelAssignToLPools.assignedStatusListTitle"));
		b.append("<br />");
		b.append("<br />");
		for (Object ident : softwareIdsForPool) {
			b.append(ident.toString());
			b.append("<br />");
		}
		b.append("</html>");
		thePanel.fieldCountAssignedStatus.setToolTipText(b.toString());

		totalSWEntries = modelWindowsSoftwareIds.getRowCount();

		produceFilterSets(softwareIdsForPool);

		Logging.info(this, "setSoftwareIdsFromLicencePool setUsingFilter "
				+ GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED + " to " + wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED,
				wasUsingSelectedFilter);
		thePanel.panelRegisteredSoftware.showFiltered(wasUsingSelectedFilter);

		modelWindowsSoftwareIds.setUsingFilter(
				LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_DONT_SHOW_ASSOCIATED_TO_OTHER_POOL,
				getSoftwareShowAllMeans() != SoftwareShowAllMeans.ALL);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		Logging.info(this, "modelWindowsSoftwareIds row count " + totalShownEntries);
		thePanel.fieldCountAllWindowsSoftware.setText(produceCount(totalSWEntries));

		thePanel.fieldCountDisplayedWindowsSoftware.setText(produceCount(totalShownEntries));
		thePanel.fieldCountNotAssignedSoftware.setText(produceCount(totalUnassignedSWEntries));

		if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
			selectKeys = softwareIdsForPool;
			thePanel.fieldCountAssignedInEditing.setText(produceCount(softwareIdsForPool.size(), (poolID == null)));
		} else {
			// selectKeys old keys
			Set<Object> existingKeys = modelWindowsSoftwareIds.getExistingKeys();
			int count = 0;
			for (String key : selectKeys) {
				if (existingKeys.contains(key)) {
					count++;
				}
			}

			thePanel.fieldCountAssignedInEditing.setText(produceCount(count));
		}

		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);
		Logging.debug(this, "setSoftwareIdsFromLicencePool  setSelectedValues " + selectKeys);
		thePanel.panelRegisteredSoftware.setSelectedValues(selectKeys, WINDOWS_SOFTWARE_ID_KEY_COL);

		if (!selectKeys.isEmpty()) {
			thePanel.panelRegisteredSoftware.moveToValue(selectKeys.get(selectKeys.size() - 1),
					WINDOWS_SOFTWARE_ID_KEY_COL, false);
		}

		Logging.debug(this,
				"setSoftwareIdsFromLicencePool  selectedKeys " + thePanel.panelRegisteredSoftware.getSelectedKeys());
		if (wasUsingSelectedFilter) {
			setVisualSelection(thePanel.panelRegisteredSoftware.getSelectedKeys());
		}
		thePanel.panelRegisteredSoftware.setDataChanged(false);
		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);

	}

	private String produceCount(Integer count) {
		if (count == null || count < 0) {
			return "";
		}
		return "" + count;
	}

	private String produceCount(Integer count, boolean licencePoolNull) {
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

		thePanel.fieldCountAssignedStatus.setText(baseCount);
		thePanel.fieldCountAssignedInEditing.setText(baseCount);

		thePanel.buttonShowAssignedNotExisting.setEnabled(false);
	}

	// called by valueChanged method of ListSelectionListener
	public void validateWindowsSoftwareKeys() {

		String selectedLicencePool = getSelectedLicencePool();
		Logging.debug(this, "validateWindowsSoftwareKeys for licencePoolID " + selectedLicencePool);

		if (selectedLicencePool == null) {
			return;
		}

		Logging.debug(this, "validateWindowsSoftwareKeys thePanel.panelRegisteredSoftware.isAwareOfSelectionListener "
				+ thePanel.panelRegisteredSoftware.isAwareOfSelectionListener());

		if (!thePanel.panelRegisteredSoftware.isAwareOfSelectionListener()) {
			return;
		}

		List<String> selKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();
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

			boolean gotAssociation = (persist.getFSoftware2LicencePool(key) != null);
			Logging.debug(this, "validateWindowsSoftwareKeys key " + key + " gotAssociation " + gotAssociation);

			Boolean newAssociation = null;
			if (gotAssociation) {
				newAssociation = !(persist.getFSoftware2LicencePool(key).equals(selectedLicencePool));
				Logging.debug(this,
						"validateWindowsSoftwareKeys has association to " + persist.getFSoftware2LicencePool(key));
			}

			if (newAssociation != null && newAssociation) {
				String otherPool = persist.getFSoftware2LicencePool(key);

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

					FTextArea dialog = new FTextArea(Globals.frame1, Globals.APPNAME + " " + title, true, new String[] {
							Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option1"),
							Configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option2") },
							400, 200);
					dialog.setMessage(info + "\n\n" + option);
					dialog.setVisible(true);

					Logging.info(this, "validateWindowsSoftwareKeys result " + dialog.getResult());

					if (dialog.getResult() == 1) {
						// we cancel the new selection
						cancelSelectionKeys.add(key);
					} else {
						// or delete the assignment to the licence pool
						List<String> removeKeys = removeKeysFromOtherLicencePool.get(otherPool);
						if (removeKeys == null) {
							removeKeys = new ArrayList<>();
							removeKeysFromOtherLicencePool.put(otherPool, removeKeys);
						}
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
			thePanel.panelRegisteredSoftware.setSelectedValues(selKeys, WINDOWS_SOFTWARE_ID_KEY_COL);
		}

		thePanel.fieldCountAssignedInEditing.setText("" + selKeys.size());
	}

	@Override
	public final void init() {
		updateCollection = new TableUpdateCollection();

		List<String> columnNames;
		List<String> classNames;

		// --- panelLicencepools
		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencepools = new MapTableUpdateItemFactory(modelLicencepools,
				columnNames, classNames, 0);
		modelLicencepools = new GenTableModel(updateItemFactoryLicencepools, mainController.licencePoolTableProvider, 0,
				thePanel.panelLicencepools, updateCollection);
		updateItemFactoryLicencepools.setSource(modelLicencepools);

		tableModels.add(modelLicencepools);
		tablePanes.add(thePanel.panelLicencepools);

		modelLicencepools.reset();
		thePanel.panelLicencepools.setTableModel(modelLicencepools);
		modelLicencepools.setEditableColumns(new int[] { 0, 1 });
		thePanel.panelLicencepools.setEmphasizedColumns(new int[] { 0, 1 });

		JMenuItemFormatted menuItemAddPool = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.NewLicencepool"));
		menuItemAddPool.addActionListener(e -> {
			Object[] a = new Object[2];
			a[0] = "";
			a[1] = "";
			modelLicencepools.addRow(a);
			thePanel.panelLicencepools.moveToValue("" + a[0], 0);

			// setting back the other tables is provided by ListSelectionListener
		});

		thePanel.panelLicencepools.addPopupItem(menuItemAddPool);

		// special treatment of columns
		javax.swing.table.TableColumn col;
		thePanel.panelLicencepools.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// updates
		thePanel.panelLicencepools.setUpdateController(
				new MapItemsUpdateController(thePanel.panelLicencepools, modelLicencepools, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						// hack for avoiding unvoluntary reuse of a licence pool id
						boolean existsNewRow = (mainController.licencePoolTableProvider.getRows()
								.size() < modelLicencepools.getRowCount());

						if (existsNewRow && persist.getLicencepools().containsKey(rowmap.get("licensePoolId"))) {
							// but we leave it until the service methods reflect the situation more
							// accurately

							String info = Configed.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists")
									+ " \n(\"" + rowmap.get("licensePoolId") + "\" ?)";

							String title = Configed
									.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists.title");

							JOptionPane.showMessageDialog(thePanel, info, title, JOptionPane.INFORMATION_MESSAGE);

							return null;
						}

						if (existsNewRow) {
							modelLicencepools.requestReload();
						}

						return persist.editLicencePool((String) rowmap.get(LicencepoolEntry.ID_SERVICE_KEY),
								(String) rowmap.get(LicencepoolEntry.DESCRIPTION_KEY));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencepools.requestReload();
						return persist.deleteLicencePool((String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));

		// --- panelProductId2LPool
		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("productId");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryProductId2LPool = new MapTableUpdateItemFactory(modelProductId2LPool,
				columnNames, classNames, 0);
		modelProductId2LPool = new GenTableModel(updateItemFactoryProductId2LPool,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames,
						() -> (Map) persist.getRelationsProductId2LPool())),
				-1, new int[] { 0, 1 }, thePanel.panelProductId2LPool, updateCollection);
		updateItemFactoryProductId2LPool.setSource(modelProductId2LPool);

		tableModels.add(modelProductId2LPool);
		tablePanes.add(thePanel.panelProductId2LPool);

		modelProductId2LPool.reset();
		thePanel.panelProductId2LPool.setTableModel(modelProductId2LPool);
		modelProductId2LPool.setEditableColumns(new int[] { 0, 1 });
		thePanel.panelProductId2LPool.setEmphasizedColumns(new int[] { 0, 1 });

		JMenuItemFormatted menuItemAddRelationProductId2LPool = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.NewRelationProductId2LPool"));
		menuItemAddRelationProductId2LPool.addActionListener(e -> {
			Object[] a = new Object[2];
			a[0] = "";
			if (thePanel.panelLicencepools.getSelectedRow() > -1) {
				a[0] = modelLicencepools.getValueAt(thePanel.panelLicencepools.getSelectedRowInModelTerms(), 0);
			}

			a[1] = "";

			modelProductId2LPool.addRow(a);

			thePanel.panelProductId2LPool.moveToValue("" + a[0], 0);
		});

		thePanel.panelProductId2LPool.addPopupItem(menuItemAddRelationProductId2LPool);

		// special treatment of columns
		col = thePanel.panelProductId2LPool.getColumnModel().getColumn(0);
		JComboBox<String> comboLP0 = new JComboBox<>();
		comboLP0.setFont(Globals.defaultFontBig);
		col.setCellEditor(new AdaptingCellEditor(comboLP0, (row, column) -> {
			List<String> poolIds = mainController.licencePoolTableProvider.getOrderedColumn(
					mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1) {
				poolIds.add("");
			}
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		col = thePanel.panelProductId2LPool.getColumnModel().getColumn(1);
		JComboBox<String> comboLP1 = new JComboBox<>();
		comboLP1.setFont(Globals.defaultFontBig);
		col.setCellEditor(new AdaptingCellEditor(comboLP1,
				(row, column) -> new DefaultComboBoxModel<>(persist.getProductIds().toArray(new String[0]))));

		// updates
		thePanel.panelProductId2LPool.setUpdateController(new MapItemsUpdateController(thePanel.panelProductId2LPool,
				modelProductId2LPool, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> m) {
						return persist.editRelationProductId2LPool((String) m.get("productId"),
								(String) m.get("licensePoolId"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> m) {
						modelProductId2LPool.requestReload();
						return persist.deleteRelationProductId2LPool((String) m.get("productId"),
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
					persist.installedSoftwareInformationRequestRefresh();
					return (Map) persist.getInstalledSoftwareInformationForLicensing();

				}, withRowCounter)), WINDOWS_SOFTWARE_ID_KEY_COL, new int[] {}, thePanel.panelRegisteredSoftware,
				updateCollection);

		Logging.info(this, "modelWindowsSoftwareIds row count " + modelWindowsSoftwareIds.getRowCount());
		tableModels.add(modelWindowsSoftwareIds);
		tablePanes.add(thePanel.panelRegisteredSoftware);

		modelWindowsSoftwareIds.reset();
		modelWindowsSoftwareIds.setColMarkCursorRow(colMarkCursorRow);

		thePanel.panelRegisteredSoftware.setTableModel(modelWindowsSoftwareIds);
		thePanel.panelRegisteredSoftware.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		modelWindowsSoftwareIds.setEditableColumns(new int[] {});
		thePanel.panelRegisteredSoftware.setEmphasizedColumns(new int[] {});

		Integer[] searchCols = new Integer[columnNames.size()];
		for (int j = 0; j < columnNames.size(); j++) {
			searchCols[j] = j;
		}

		softwareDirectionOfAssignment = SoftwareDirectionOfAssignment.POOL2SOFTWARE;
		thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener(
				softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE);

		thePanel.panelRegisteredSoftware.setSearchColumns(searchCols);
		thePanel.panelRegisteredSoftware.setSearchSelectMode(false);

		windowsSoftwareFilterConditonShowOnlySelected = new DefaultTableModelFilterCondition(
				WINDOWS_SOFTWARE_ID_KEY_COL);
		modelWindowsSoftwareIds.chainFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED,
				new TableModelFilter(windowsSoftwareFilterConditonShowOnlySelected));
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED, false);
		thePanel.panelRegisteredSoftware.showFilterIcon(true);
		thePanel.panelRegisteredSoftware.setFiltermarkToolTipText(
				Configed.getResourceValue("PanelAssignToLPools.searchPane.filtermark.tooltip"));

		windowsSoftwareFilterConditionDontShowAssociatedToOtherPool = new DefaultTableModelFilterCondition(
				WINDOWS_SOFTWARE_ID_KEY_COL);
		modelWindowsSoftwareIds.chainFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_DONT_SHOW_ASSOCIATED_TO_OTHER_POOL,
				new TableModelFilter(windowsSoftwareFilterConditionDontShowAssociatedToOtherPool));
		modelWindowsSoftwareIds
				.setUsingFilter(LABEL_WINDOWS_SOFTWARE_FILTER_CONDITION_DONT_SHOW_ASSOCIATED_TO_OTHER_POOL, false);

		thePanel.panelRegisteredSoftware.showFiltered(false);
		thePanel.panelRegisteredSoftware.setDataChanged(false);

		JMenuItemFormatted menuItemSoftwareShowAssigned = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAssigned"));
		menuItemSoftwareShowAssigned.addActionListener(e -> {
			// save values
			softwareShow = SoftwareShowMode.ASSIGNED;
			setSWAssignments();
		});

		thePanel.panelRegisteredSoftware.setFiltermarkActionListener(e -> {
			if (softwareShow == SoftwareShowMode.ALL) {
				softwareShow = SoftwareShowMode.ASSIGNED;
				setSWAssignments();
			} else if (softwareShow == SoftwareShowMode.ASSIGNED) {
				softwareShow = SoftwareShowMode.ALL;
				setSWAssignments();
			}

		});

		JMenuItemFormatted menuItemSoftwareShowAll = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAll"));
		menuItemSoftwareShowAll.addActionListener(e -> {
			softwareShow = SoftwareShowMode.ALL;
			setSWAssignments();
		});

		thePanel.panelRegisteredSoftware.addPopupItem(menuItemSoftwareShowAll);
		thePanel.panelRegisteredSoftware.addPopupItem(menuItemSoftwareShowAssigned);

		// special treatment of columns

		if (colMarkCursorRow > -1) {

			// row cursor column
			col = thePanel.panelRegisteredSoftware.getColumnModel().getColumn(colMarkCursorRow);
			col.setMaxWidth(12);
			col.setHeaderValue("");

			col.setCellRenderer(new de.uib.utilities.table.gui.BooleanIconTableCellRenderer(
					Globals.createImageIcon("images/minibarpointerred.png", ""),
					Globals.createImageIcon("images/minibarpointervoid.png", "")));
		}

		col = thePanel.panelRegisteredSoftware.getColumnModel().getColumn(WINDOWS_SOFTWARE_ID_KEY_COL);
		col.setMaxWidth(MAX_WIDTH_ID_COLUMN_FOR_REGISTERED_SOFTWARE);
		col.setHeaderValue("id ...");
		col = thePanel.panelRegisteredSoftware.getColumnModel().getColumn(columnNames.indexOf("subVersion"));
		col.setHeaderValue("OS variant");
		col.setMaxWidth(80);
		col = thePanel.panelRegisteredSoftware.getColumnModel().getColumn(columnNames.indexOf("architecture"));
		col.setMaxWidth(80);
		col = thePanel.panelRegisteredSoftware.getColumnModel().getColumn(columnNames.indexOf("language"));
		col.setMaxWidth(60);

		// updates
		thePanel.panelRegisteredSoftware
				.setUpdateController(new AbstractSelectionMemorizerUpdateController(thePanel.panelLicencepools, 0,
						thePanel.panelRegisteredSoftware, modelWindowsSoftwareIds, new StrList2BooleanFunction() {
							@Override
							public boolean sendUpdate(String poolId, List<String> softwareIds) {

								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								Logging.info(this, "sendUpdate poolId, removeKeysFromOtherLicencePool "
										+ removeKeysFromOtherLicencePool);

								List<String> oldSWListForPool = persist.getSoftwareListByLicencePool(poolId);

								boolean result = true;

								if (removeKeysFromOtherLicencePool != null) {
									for (Entry<String, List<String>> otherStringPoolEntry : removeKeysFromOtherLicencePool
											.entrySet()) {
										if (result && !otherStringPoolEntry.getValue().isEmpty()) {
											result = persist.removeAssociations(otherStringPoolEntry.getKey(),
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
									result = persist.setWindowsSoftwareIds2LPool(poolId, softwareIds);
								} else {
									result = persist.addWindowsSoftwareIds2LPool(poolId, softwareIds);
								}

								Logging.info(this, "sendUpdate, setSoftwareIdsFromLicencePool poolId " + poolId);
								setSoftwareIdsFromLicencePool(poolId);

								// doing it locally for fSoftware2LicencePool
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								Logging.info(this, "sendUpdate, we have software ids " + softwareIds.size());
								Logging.info(this,
										"sendUpdate, we have software ids "
												+ persist.getSoftwareListByLicencePool(poolId).size() + " they are "
												+ persist.getSoftwareListByLicencePool(poolId));
								// remove all old assignements
								for (String swId : oldSWListForPool) {
									Logging.info(this, "sendUpdate remove " + swId + " from Software2LicencePool ");
									persist.getFSoftware2LicencePool().remove(swId);
								}
								// set the current ones
								for (String ident : softwareIds) {
									persist.setFSoftware2LicencePool(ident, poolId);
								}

								if (thePanel.fSoftwarename2LicencePool != null) {
									thePanel.fSoftwarename2LicencePool.panelSWnames.requestReload();
								}

								if (thePanel.fSoftwarename2LicencePool != null) {
									thePanel.fSoftwarename2LicencePool.panelSWxLicencepool.requestReload();
								}

								return result;
							}
						}) {

					@Override
					public boolean cancelChanges() {
						setSoftwareIdsFromLicencePool(null);
						return true;
					}
				}

				);

		// -- Softwarename --> LicencePool

		Logging.info(this, "frame Softwarename --> LicencePool  in " + Globals.frame1);

		final ControlPanelAssignToLPools contr = this;
		thePanel.fSoftwarename2LicencePool = new FSoftwarename2LicencePool(Globals.frame1, contr);
		thePanel.fSoftwarename2LicencePool.setTableModel(null); // test
		thePanel.setDisplaySimilarExist(thePanel.fSoftwarename2LicencePool.checkExistNamesWithVariantLicencepools());
		thePanel.fSoftwarename2LicencePool.setButtonsEnabled(true);

		// combine
		thePanel.panelLicencepools.getListSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// Ignore extra messages.
				if (e.getValueIsAdjusting()) {
					return;
				}

				String selectedLicencePool = null;

				// clear selection
				thePanel.panelProductId2LPool.setSelectedValues(null, 0);

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				if (lsm.isSelectionEmpty()) {
					Logging.debug(this, "no rows selected");
				} else {
					selectedLicencePool = getSelectedLicencePool();
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
					thePanel.panelProductId2LPool.moveToValue(selectedLicencePool, 0);
				}

				setSoftwareIdsFromLicencePool(selectedLicencePool);

				if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.SOFTWARE2POOL) {
					thePanel.panelRegisteredSoftware.setDataChanged(gotNewSWKeysForLicencePool(selectedLicencePool));
				}

			}
		});

		setSoftwareIdsFromLicencePool(null);
		initializeVisualSettings();
	}

	private void setVisualSelection(List<String> keys) {
		Logging.debug(this, "setVisualSelection for panelRegisteredSoftware on keys " + keys);
		thePanel.panelRegisteredSoftware.setSelectedValues(keys, WINDOWS_SOFTWARE_ID_KEY_COL);

		if (keys != null && !keys.isEmpty()) {
			thePanel.panelRegisteredSoftware.moveToValue(keys.get(keys.size() - 1), WINDOWS_SOFTWARE_ID_KEY_COL, false);
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
		boolean b = thePanel.panelRegisteredSoftware.isDataChanged();
		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);

		List<String> selectedKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();
		Logging.info(this, "setSWAssignments  selectedKeys " + selectedKeys);

		Logging.info(this, "setSWAssignments usingFilter " + (softwareShow == SoftwareShowMode.ASSIGNED)
				+ " selected keys " + selectedKeys);

		boolean usingShowSelectedFilter = (softwareShow == SoftwareShowMode.ASSIGNED);
		if (usingShowSelectedFilter) {
			windowsSoftwareFilterConditonShowOnlySelected.setFilter(new TreeSet<>(selectedKeys));
		} else {
			windowsSoftwareFilterConditonShowOnlySelected.setFilter(null);
		}

		thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener(
				softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE);

		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.LABEL_FILTER_CONDITION_SHOW_ONLY_SELECTED,
				usingShowSelectedFilter);

		thePanel.panelRegisteredSoftware.showFiltered(usingShowSelectedFilter);
		setVisualSelection(selectedKeys);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		Logging.info(this, "modelWindowsSoftwareIds row count 2 " + modelWindowsSoftwareIds.getRowCount());
		thePanel.fieldCountDisplayedWindowsSoftware.setText(produceCount(totalShownEntries));
		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);
		thePanel.panelRegisteredSoftware.setDataChanged(b);
	}

	public SoftwareShowAllMeans getSoftwareShowAllMeans() {
		return softwareShowAllMeans;
	}

	public void setSoftwareShowAllMeans(SoftwareShowAllMeans meaning) {
		SoftwareShowAllMeans softwareShowAllMeansOld = softwareShowAllMeans;
		softwareShowAllMeans = meaning;

		if (softwareShowAllMeansOld != softwareShowAllMeans) {
			boolean tableChangeAware = thePanel.panelRegisteredSoftware.isAwareOfTableChangedListener();
			thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener(false);

			String selectedLicencePool = null;
			Logging.info(this, " setShowOnlyAssociationsToSelectedPoolOrNoPool selected licence row "
					+ thePanel.panelLicencepools.getSelectedRow());
			selectedLicencePool = getSelectedLicencePool();
			setSoftwareIdsFromLicencePool(selectedLicencePool);

			thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener(tableChangeAware);
		}
	}

	public void setSoftwareDirectionOfAssignment(SoftwareDirectionOfAssignment direction) {
		SoftwareDirectionOfAssignment oldDirection = softwareDirectionOfAssignment;
		this.softwareDirectionOfAssignment = direction;

		if (oldDirection != direction) {
			if (direction == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
				thePanel.panelRegisteredSoftware.getTheSearchpane().showFilterIcon(true);
			} else if (direction == SoftwareDirectionOfAssignment.SOFTWARE2POOL) {
				thePanel.panelRegisteredSoftware.getTheSearchpane().showFilterIcon(false);
				resetCounters(null);
				thePanel.fieldCountAssignedInEditing.setText("");
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

		List<String> oldSWList = persist.getSoftwareListByLicencePool(selectedLicencePool);
		List<String> newKeys = new ArrayList<>(thePanel.panelRegisteredSoftware.getSelectedKeys());
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

		if (thePanel.panelLicencepools.getSelectedRow() >= 0) {
			result = thePanel.panelLicencepools.getValueAt(thePanel.panelLicencepools.getSelectedRow(), 0).toString();
		}

		return result;
	}

	@Override
	public void initializeVisualSettings() {
		super.initializeVisualSettings();
		setSoftwareIdsFromLicencePool(null);
		resetCounters(null);

		thePanel.panelProductId2LPool.getTheTable().clearSelection();
		thePanel.panelLicencepools.getTheTable().clearSelection();
		thePanel.panelRegisteredSoftware.getTheTable().clearSelection();
		thePanel.panelRegisteredSoftware.setDataChanged(false);
	}
}
