package de.uib.configed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

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
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.DefaultTableModelFilterCondition;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilter;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.SelectionMemorizerUpdateController;
import de.uib.utilities.table.updates.StrList2BooleanFunction;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlPanelAssignToLPools extends ControlMultiTablePanel {
	private final int maxWidthIdColumnForRegisteredSoftware = 300;
	public PanelAssignToLPools thePanel;

	GenTableModel modelLicencepools;
	GenTableModel modelProductId2LPool;
	GenTableModel modelWindowsSoftwareIds;

	TableModelFilterCondition windowsSoftwareFilterConditon_showOnlySelected; // we replace the filter from
																				// GenTableModel
																				// static String labelWindowsSoftwareFilterCondition_showOnlySelected =

	TableModelFilterCondition windowsSoftwareFilterCondition_dontShowAssociatedToOtherPool;
	static String labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool = "restrictToNonAssociated";

	ConfigedMain mainController;

	public PersistenceController persist;

	public enum SoftwareShowMode {
		ALL, ASSIGNED
	} // activate filter for selection in software table

	public enum SoftwareShowAllMeans {
		ALL, ASSIGNED_OR_ASSIGNED_TO_NOTHING, ASSIGNED_TO_NOTHING
	}

	public enum SoftwareDirectionOfAssignment {
		POOL2SOFTWARE, SOFTWARE2POOL
	}

	private SoftwareShowMode softwareShow = SoftwareShowMode.ALL;
	private SoftwareShowAllMeans softwareShowAllMeans = SoftwareShowAllMeans.ALL;
	// //
	private SoftwareDirectionOfAssignment softwareDirectionOfAssignment = SoftwareDirectionOfAssignment.POOL2SOFTWARE;
	// private SoftwareDirectionOfAssignment softwareDirectionOfAssignment =

	Integer totalSWEntries;
	Integer totalUnassignedSWEntries;
	Integer totalShownEntries;

	// introducing a column for displaying the cursor row
	public final int windowsSoftwareId_KeyCol = 1;
	int colMarkCursorRow = 0;

	private HashMap<String, List<String>> removeKeysFromOtherLicencePool;

	public ControlPanelAssignToLPools(PersistenceController persist, ConfigedMain mainController) {
		thePanel = new PanelAssignToLPools(this);
		this.persist = persist;
		this.mainController = mainController;
		init();

	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	private TreeSet<Object> getUnAssignedSoftwareIds() {
		// the object is cached in persist
		return persist.getSoftwareWithoutAssociatedLicencePool();
	}

	public void setSoftwareIdsFromLicencePool() {
		String selectedLicencePool = getSelectedLicencePool();
		logging.info(this, "setSoftwareIdsFromLicencePoot, selectedLicencePool " + selectedLicencePool);

		setSoftwareIdsFromLicencePool(selectedLicencePool);

	}

	private void setSoftwareIdsFromLicencePool(final String poolID) {
		logging.info(this,
				"setSoftwareIdsFromLicencePool " + poolID + " should be thePanel.panelLicencepools.getSelectedRow() "
						+ thePanel.panelLicencepools.getSelectedRow());
		logging.info(this, "setSoftwareIdsFromLicencePool, call thePanel.fSoftwarename2LicencePool.setGlobalPool ");
		if (thePanel.fSoftwarename2LicencePool != null)
			thePanel.fSoftwarename2LicencePool.setGlobalPool(poolID);

		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);

		List<String> selectKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();

		boolean wasUsingSelectedFilter = modelWindowsSoftwareIds
				.isUsingFilter(GenTableModel.labelFilterConditionShowOnlySelected);
		logging.info(this, "setSoftwareIdsFromLicencePool wasUsingSelectedFilter " + wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.labelFilterConditionShowOnlySelected, false); // wasUsingSelectedFilter

		// 

		modelWindowsSoftwareIds.setUsingFilter(labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool,
				false);

		thePanel.panelRegisteredSoftware.showFiltered(false);

		thePanel.fieldSelectedLicencePoolId.setText(poolID);
		thePanel.fieldSelectedLicencePoolId.setToolTipText(poolID);

		List<String> softwareIdsForPool = new ArrayList<>();
		if (poolID != null) {
			softwareIdsForPool = persist.getSoftwareListByLicencePool(poolID);
		}

		logging.info(this, "setSoftwareIdsFromLicencePool  softwareIds for licencePool  " + poolID + " : "
				+ softwareIdsForPool.size());
		logging.info(this, "setSoftwareIdsFromLicencePool  unknown softwareIds for licencePool  " + poolID + " : "
				+ persist.getUnknownSoftwareListForLicencePool(poolID).size());

		totalUnassignedSWEntries = getUnAssignedSoftwareIds().size();
		logging.info(this, "setSoftwareIdsFromLicencePool unAssignedSoftwareIds " + totalUnassignedSWEntries);

		resetCounters(poolID);
		thePanel.fieldCountAllWindowsSoftware.setText("0");

		thePanel.buttonShowAssignedNotExisting
				.setEnabled(!persist.getUnknownSoftwareListForLicencePool(poolID).isEmpty());
		if (thePanel.fMissingSoftwareInfo == null)
			thePanel.fMissingSoftwareInfo = new FGlobalSoftwareInfo(Globals.frame1, this);

		if (!persist.getUnknownSoftwareListForLicencePool(poolID).isEmpty()) {
			Map<String, Object> missingSoftwareMap = new HashMap<>();

			for (String ID : persist.getUnknownSoftwareListForLicencePool(poolID)) {
				String[] rowValues = ID.split(Globals.PSEUDO_KEY_SEPARATOR);

				Map<String, String> rowMap = new HashMap<>();
				for (String colName : thePanel.fMissingSoftwareInfo.columnNames)
					rowMap.put(colName, "");

				rowMap.put("ID", ID);

				Vector<String> identKeys = de.uib.configed.type.SWAuditEntry.KEYS_FOR_IDENT;
				if (rowValues.length != identKeys.size())
					logging.warning(this, "illegal ID " + ID);
				else {
					int i = 0;
					for (String key : identKeys) {
						rowMap.put(key, rowValues[i]);

						i++;
					}
				}

				rowMap.put("ID", ID);
				logging.info(this, "unknownSoftwareIdsForPool " + rowMap);

				missingSoftwareMap.put(ID, rowMap);
			}

			thePanel.fMissingSoftwareInfo.setTableModel(new GenTableModel(
					new MapTableUpdateItemFactory(thePanel.fMissingSoftwareInfo.columnNames,
							thePanel.fMissingSoftwareInfo.classNames, 0), // dummy
					new DefaultTableProvider(new RetrieverMapSource(thePanel.fMissingSoftwareInfo.columnNames,
							thePanel.fMissingSoftwareInfo.classNames,
							// () -> (Map) persist.getInstalledSoftwareInformation()
							() -> (Map) missingSoftwareMap)),
					0, new int[] {}, (thePanel.fMissingSoftwareInfo.panelGlobalSoftware), updateCollection));

		}

		thePanel.fieldCountAssignedStatus.setToolTipText(" <html><br /></html>");
		if (softwareIdsForPool != null) {
			thePanel.fieldCountAssignedStatus.setText(produceCount(softwareIdsForPool.size(), (poolID == null)));

			StringBuilder b = new StringBuilder("<html>");
			b.append(configed.getResourceValue("PanelAssignToLPools.assignedStatusListTitle"));
			b.append("<br />");
			b.append("<br />");
			for (Object ident : softwareIdsForPool) {
				b.append(ident.toString());
				b.append("<br />");
			}
			b.append("</html>");
			thePanel.fieldCountAssignedStatus.setToolTipText(b.toString());
		}

		if (softwareIdsForPool == null)
			softwareIdsForPool = new ArrayList<>();

		totalSWEntries = modelWindowsSoftwareIds.getRowCount();

		produceFilterSets(softwareIdsForPool);

		logging.info(this, "setSoftwareIdsFromLicencePool setUsingFilter "
				+ GenTableModel.labelFilterConditionShowOnlySelected + " to " + wasUsingSelectedFilter);
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.labelFilterConditionShowOnlySelected,
				wasUsingSelectedFilter);
		thePanel.panelRegisteredSoftware.showFiltered(wasUsingSelectedFilter);

		modelWindowsSoftwareIds.setUsingFilter(labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool,
				getSoftwareShowAllMeans() != SoftwareShowAllMeans.ALL);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		logging.info(this, "modelWindowsSoftwareIds row count " + totalShownEntries);
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
				if (existingKeys.contains(key))
					count++;
			}

			thePanel.fieldCountAssignedInEditing.setText(produceCount(count));
		}

		{
			thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);
			logging.debug(this, "setSoftwareIdsFromLicencePool  setSelectedValues " + selectKeys);
			thePanel.panelRegisteredSoftware.setSelectedValues(selectKeys, windowsSoftwareId_KeyCol);

			if (!selectKeys.isEmpty())
				thePanel.panelRegisteredSoftware.moveToValue(selectKeys.get(selectKeys.size() - 1).toString(),
						windowsSoftwareId_KeyCol, false);

			logging.debug(this, "setSoftwareIdsFromLicencePool  selectedKeys "
					+ thePanel.panelRegisteredSoftware.getSelectedKeys());
			if (wasUsingSelectedFilter) {
				setVisualSelection(thePanel.panelRegisteredSoftware.getSelectedKeys());
			}
			thePanel.panelRegisteredSoftware.setDataChanged(false);
			thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);
		}

	}

	private String produceCount(Integer count) {
		if (count == null || count < 0)
			return "";
		return "" + count;
	}

	private String produceCount(Integer count, boolean licencePoolNull) {
		if (count == null || licencePoolNull || count < 0)
			return "";
		return "" + count;
	}

	private void resetCounters(String licencePoolId) {
		logging.info(this, "resetCounters for pool " + licencePoolId);
		String baseCount = "0";
		if (licencePoolId == null)
			baseCount = "";

		thePanel.fieldCountAssignedStatus.setText(baseCount);
		thePanel.fieldCountAssignedInEditing.setText(baseCount);

		thePanel.buttonShowAssignedNotExisting.setEnabled(false);
	}

	public void validateWindowsSoftwareKeys()
	// called by valueChanged method of ListSelectionListener
	{

		String selectedLicencePool = getSelectedLicencePool();
		logging.debug(this, "validateWindowsSoftwareKeys for licencePoolID " + selectedLicencePool);

		if (selectedLicencePool == null)
			return;

		logging.debug(this, "validateWindowsSoftwareKeys thePanel.panelRegisteredSoftware.isAwareOfSelectionListener "
				+ thePanel.panelRegisteredSoftware.isAwareOfSelectionListener());

		if (!thePanel.panelRegisteredSoftware.isAwareOfSelectionListener())
			return;

		List<String> selKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();
		String showSelKeys = null;
		if (selKeys != null)
			showSelKeys = "" + selKeys.size();
		logging.info(this, "validateWindowsSoftwareKeys selectedKeys " + showSelKeys
				+ " associated to selectedLicencePool " + selectedLicencePool);

		if (selKeys == null) {
			resetCounters(selectedLicencePool);
			return;
		}

		ArrayList<String> cancelSelectionKeys = new ArrayList<>();
		removeKeysFromOtherLicencePool = new HashMap<>();

		for (String key : selKeys) {
			// key is already assigned to a different licencePool?

			boolean gotAssociation = (persist.getFSoftware2LicencePool(key) != null);
			logging.debug(this, "validateWindowsSoftwareKeys key " + key + " gotAssociation " + gotAssociation);

			Boolean newAssociation = null;
			if (gotAssociation) {
				newAssociation = !(persist.getFSoftware2LicencePool(key).equals(selectedLicencePool));
				logging.debug(this,
						"validateWindowsSoftwareKeys has association to " + persist.getFSoftware2LicencePool(key));

			}

			if (newAssociation != null && newAssociation) {
				String otherPool = persist.getFSoftware2LicencePool(key);

				if (otherPool.equals(FSoftwarename2LicencePool.valNoLicencepool)) {
					logging.info(this, "validateWindowsSoftwareKeys, assigned to valNoLicencepool");
				} else {

					String info = configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned")
							+ "\n\n" + otherPool;
					String option = configed
							.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.options");
					String title = configed
							.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.title");

					logging.info(
							" software with ident \"" + key + "\" already associated to license pool " + otherPool);

					FTextArea dialog = new FTextArea(Globals.frame1, Globals.APPNAME + " " + title, true, new String[] {
							configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option1"),
							configed.getResourceValue("PanelAssignToLPools.warningSoftwareAlreadyAssigned.option2") },
							400, 200);
					dialog.setMessage(info + "\n\n" + option);
					dialog.setVisible(true);

					logging.info(this, "validateWindowsSoftwareKeys result " + dialog.getResult());

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

		logging.info(this, "cancelSelectionKeys " + cancelSelectionKeys);
		if (!cancelSelectionKeys.isEmpty()) // without this condition we run into a loop
		{
			selKeys.removeAll(cancelSelectionKeys);
			logging.info(this, "selKeys after removal " + selKeys);
			thePanel.panelRegisteredSoftware.setSelectedValues(selKeys, windowsSoftwareId_KeyCol);
		}

		thePanel.fieldCountAssignedInEditing.setText("" + selKeys.size());

	}

	@Override
	public void init() {
		updateCollection = new TableUpdateCollection();

		Vector<String> columnNames;
		Vector<String> classNames;

		// --- panelLicencepools
		columnNames = new Vector<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		classNames = new Vector<>();
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
				configed.getResourceValue("ConfigedMain.Licences.NewLicencepool"));
		menuItemAddPool.addActionListener(e -> {
			Object[] a = new Object[2];
			a[0] = "";
			a[1] = "";
			modelLicencepools.addRow(a);
			thePanel.panelLicencepools.moveToValue("" + a[0], 0);

			// setting back the other tables is provided by ListSelectionListener
			// setting back the other tables is provided by ListSelectionListener
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

						if (existsNewRow
								&& persist.getLicencepools().containsKey((String) rowmap.get("licensePoolId"))) {

							// but we leave it until the service methods reflect the situation more
							// accurately

							String info = configed.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists")
									+ " \n(\"" + rowmap.get("licensePoolId") + "\" ?)";

							String title = configed
									.getResourceValue("PanelAssignToLPools.licencePoolIdAlreadyExists.title");

							JOptionPane.showMessageDialog(thePanel, info, title, JOptionPane.INFORMATION_MESSAGE);

							return null;
						}

						if (existsNewRow)
							modelLicencepools.requestReload();

						return persist.editLicencePool((String) rowmap.get(LicencepoolEntry.idSERVICEKEY),
								(String) rowmap.get(LicencepoolEntry.descriptionKEY));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencepools.requestReload();
						return persist.deleteLicencePool((String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));

		// --- panelProductId2LPool
		columnNames = new Vector<>();
		columnNames.add("licensePoolId");
		columnNames.add("productId");
		classNames = new Vector<>();
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
				configed.getResourceValue("ConfigedMain.Licences.NewRelationProductId2LPool"));
		menuItemAddRelationProductId2LPool.addActionListener(e -> {
			Object[] a = new Object[2];
			a[0] = "";
			if (thePanel.panelLicencepools.getSelectedRow() > -1)
				a[0] = modelLicencepools.getValueAt(thePanel.panelLicencepools.getSelectedRowInModelTerms(), 0);

			a[1] = "";

			modelProductId2LPool.addRow(a);

			thePanel.panelProductId2LPool.moveToValue("" + a[0], 0);
		});

		thePanel.panelProductId2LPool.addPopupItem(menuItemAddRelationProductId2LPool);

		// special treatment of columns
		col = thePanel.panelProductId2LPool.getColumnModel().getColumn(0);
		JComboBox<String> comboLP0 = new JComboBox<>();
		comboLP0.setFont(Globals.defaultFontBig);
		col.setCellEditor(new de.uib.utilities.table.gui.AdaptingCellEditor(comboLP0, (row, column) -> {
			List<String> poolIds = mainController.licencePoolTableProvider.getOrderedColumn(
					mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1)
				poolIds.add("");
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		col = thePanel.panelProductId2LPool.getColumnModel().getColumn(1);
		JComboBox<String> comboLP1 = new JComboBox<>();
		comboLP1.setFont(Globals.defaultFontBig);
		col.setCellEditor(new de.uib.utilities.table.gui.AdaptingCellEditor(comboLP1,
				(row, column) -> new DefaultComboBoxModel<>(new Vector<>(persist.getProductIds()))));

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

		columnNames = new Vector<>(de.uib.configed.type.SWAuditEntry.getDisplayKeys());
		columnNames.add(colMarkCursorRow, "CURSOR"); // introducing a column for displaying the cursor row

		columnNames.remove("licenseKey");

		classNames = new Vector<>();
		for (int i = 0; i <= columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}
		classNames.setElementAt("java.lang.Boolean", colMarkCursorRow); // introducing a column for displaying the
																		// cursor row

		logging.info(this, "panelRegisteredSoftware constructed with (size) cols " + "(" + columnNames.size() + ") "
				+ columnNames);
		logging.info(this, "panelRegisteredSoftware constructed with (size) classes " + "(" + classNames.size() + ") "
				+ classNames);

		boolean withRowCounter = false;
		modelWindowsSoftwareIds = new GenTableModel(null,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, () -> {
					persist.installedSoftwareInformationRequestRefresh();
					return (Map) persist.getInstalledSoftwareInformationForLicensing();

				}, withRowCounter)
				// ,
				), windowsSoftwareId_KeyCol, // key column
				new int[] {}, thePanel.panelRegisteredSoftware, updateCollection);

		logging.info(this, "modelWindowsSoftwareIds row count " + modelWindowsSoftwareIds.getRowCount());
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

		windowsSoftwareFilterConditon_showOnlySelected = new DefaultTableModelFilterCondition(windowsSoftwareId_KeyCol);
		modelWindowsSoftwareIds.chainFilter(GenTableModel.labelFilterConditionShowOnlySelected,
				new TableModelFilter(windowsSoftwareFilterConditon_showOnlySelected));
		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.labelFilterConditionShowOnlySelected, false);
		thePanel.panelRegisteredSoftware.showFilterIcon(true);
		thePanel.panelRegisteredSoftware.setFiltermarkToolTipText(
				configed.getResourceValue("PanelAssignToLPools.searchPane.filtermark.tooltip"));

		windowsSoftwareFilterCondition_dontShowAssociatedToOtherPool = new DefaultTableModelFilterCondition(
				windowsSoftwareId_KeyCol);
		modelWindowsSoftwareIds.chainFilter(labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool,
				new TableModelFilter(windowsSoftwareFilterCondition_dontShowAssociatedToOtherPool));
		modelWindowsSoftwareIds.setUsingFilter(labelWindowsSoftwareFilterCondition_dontShowAssociatedToOtherPool,
				false);

		thePanel.panelRegisteredSoftware.showFiltered(false);
		thePanel.panelRegisteredSoftware.setDataChanged(false);

		JMenuItemFormatted menuItemSoftwareShowAssigned = new JMenuItemFormatted(
				configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAssigned"));
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
				configed.getResourceValue("ConfigedMain.Licences.PopupWindowsSoftwareShowAll"));
		menuItemSoftwareShowAll.addActionListener(e -> {
			softwareShow = SoftwareShowMode.ALL;
			setSWAssignments();
		});

		thePanel.panelRegisteredSoftware.addPopupItem(menuItemSoftwareShowAll);
		thePanel.panelRegisteredSoftware.addPopupItem(menuItemSoftwareShowAssigned);

		// special treatment of columns

		if (colMarkCursorRow > -1) {
			col = thePanel.panelRegisteredSoftware.getColumnModel().getColumn(colMarkCursorRow); // row cursor column
			col.setMaxWidth(12);
			col.setHeaderValue("");

			col.setCellRenderer(new de.uib.utilities.table.gui.BooleanIconTableCellRenderer(
					Globals.createImageIcon("images/minibarpointerred.png", ""),
					Globals.createImageIcon("images/minibarpointervoid.png", "")));

		}

		col = thePanel.panelRegisteredSoftware.getColumnModel().getColumn(windowsSoftwareId_KeyCol);
		col.setMaxWidth(maxWidthIdColumnForRegisteredSoftware);
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
				.setUpdateController(new SelectionMemorizerUpdateController(thePanel.panelLicencepools, 0,
						thePanel.panelRegisteredSoftware, modelWindowsSoftwareIds, new StrList2BooleanFunction() {
							@Override
							public boolean sendUpdate(String poolId, List softwareIds) {

								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, softwareIds: " + poolId + ", " + softwareIds);
								logging.info(this, "sendUpdate poolId, removeKeysFromOtherLicencePool "
										+ removeKeysFromOtherLicencePool);

								List<String> oldSWListForPool = persist.getSoftwareListByLicencePool(poolId);

								boolean result = true;

								if (removeKeysFromOtherLicencePool != null) {
									for (String otherPool : removeKeysFromOtherLicencePool.keySet()) {
										if (result && !removeKeysFromOtherLicencePool.get(otherPool).isEmpty()) {
											result = persist.removeAssociations(otherPool,
													removeKeysFromOtherLicencePool.get(otherPool));
											if (result)
												removeKeysFromOtherLicencePool.remove(otherPool);
										}
									}
								}

								if (!result)
									return false;

								// cleanup assignments to other pools since an update would not change them
								// (redmine #3282)
								if (softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE) {
									result = persist.setWindowsSoftwareIds2LPool(poolId, softwareIds);
								} else {
									result = persist.addWindowsSoftwareIds2LPool(poolId, softwareIds);
								}

								logging.info(this, "sendUpdate, setSoftwareIdsFromLicencePool poolId " + poolId);
								setSoftwareIdsFromLicencePool(poolId);

								// doing it locally for fSoftware2LicencePool
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, adapt Softwarename2LicencePool");
								logging.info(this, "sendUpdate, we have software ids " + softwareIds.size());
								logging.info(this,
										"sendUpdate, we have software ids "
												+ persist.getSoftwareListByLicencePool(poolId).size() + " they are "
												+ persist.getSoftwareListByLicencePool(poolId));
								// remove all old assignements
								for (String swId : oldSWListForPool) {
									logging.info(this, "sendUpdate remove " + swId + " from Software2LicencePool ");
									persist.getFSoftware2LicencePool().remove(swId);
								}
								// set the current ones
								for (Object ident : softwareIds) {
									persist.setFSoftware2LicencePool((String) ident, poolId);
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

		logging.info(this, "frame Softwarename --> LicencePool  in " + Globals.frame1);

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
				if (e.getValueIsAdjusting())
					return;

				String selectedLicencePool = null;
				thePanel.panelProductId2LPool.setSelectedValues(null, 0);// clear selection

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				if (lsm.isSelectionEmpty()) {
					logging.debug(this, "no rows selected");

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
		logging.debug(this, "setVisualSelection for panelRegisteredSoftware on keys " + keys);
		thePanel.panelRegisteredSoftware.setSelectedValues(keys, windowsSoftwareId_KeyCol);

		if (keys != null && !keys.isEmpty())
			thePanel.panelRegisteredSoftware.moveToValue(keys.get(keys.size() - 1), windowsSoftwareId_KeyCol, false);
	}

	private void produceFilter1(List<String> assignedWindowsSoftwareIds) {
		TreeSet<Object> filter1 = null;

		if (softwareShowAllMeans != SoftwareShowAllMeans.ALL)
			filter1 = new TreeSet<>(getUnAssignedSoftwareIds());

		if (filter1 != null && softwareShowAllMeans == SoftwareShowAllMeans.ASSIGNED_OR_ASSIGNED_TO_NOTHING) {
			if (assignedWindowsSoftwareIds != null)
				filter1.addAll(assignedWindowsSoftwareIds);
		}

		String filterInfo = "null";
		if (filter1 != null)
			filterInfo = "" + filter1.size();

		logging.info(this, "produceFilterSets setFilter dontShowAssociatedToOtherPool " + filterInfo);

		windowsSoftwareFilterCondition_dontShowAssociatedToOtherPool.setFilter(filter1);
	}

	private void produceFilterSets(List<String> assignedWindowsSoftwareIds) {
		TreeSet<Object> filter0 = null;

		if (softwareShow == SoftwareShowMode.ASSIGNED)
			filter0 = new TreeSet<>(assignedWindowsSoftwareIds);

		String filterInfo = "null";
		if (filter0 != null)
			filterInfo = "" + filter0.size();
		logging.info(this, "produceFilterSets setFilter dontShowAssociatedToOtherPool " + filterInfo);
		windowsSoftwareFilterConditon_showOnlySelected.setFilter(filter0);

		produceFilter1(assignedWindowsSoftwareIds);
	}

	private void setSWAssignments() {
		// save values
		boolean b = thePanel.panelRegisteredSoftware.isDataChanged();
		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(false);

		List<String> selectedKeys = thePanel.panelRegisteredSoftware.getSelectedKeys();
		logging.info(this, "setSWAssignments  selectedKeys " + selectedKeys);

		logging.info(this, "setSWAssignments usingFilter " + (softwareShow == SoftwareShowMode.ASSIGNED)
				+ " selected keys " + selectedKeys);

		boolean usingShowSelectedFilter = (softwareShow == SoftwareShowMode.ASSIGNED);
		if (usingShowSelectedFilter)
			windowsSoftwareFilterConditon_showOnlySelected.setFilter(new TreeSet<>(selectedKeys));
		else
			windowsSoftwareFilterConditon_showOnlySelected.setFilter(null);

		thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener(
				softwareDirectionOfAssignment == SoftwareDirectionOfAssignment.POOL2SOFTWARE);

		modelWindowsSoftwareIds.setUsingFilter(GenTableModel.labelFilterConditionShowOnlySelected,
				usingShowSelectedFilter);

		thePanel.panelRegisteredSoftware.showFiltered(usingShowSelectedFilter);
		setVisualSelection(selectedKeys);

		totalShownEntries = modelWindowsSoftwareIds.getRowCount();
		logging.info(this, "modelWindowsSoftwareIds row count 2 " + modelWindowsSoftwareIds.getRowCount());
		thePanel.fieldCountDisplayedWindowsSoftware.setText(produceCount(totalShownEntries));
		thePanel.panelRegisteredSoftware.setAwareOfSelectionListener(true);
		thePanel.panelRegisteredSoftware.setDataChanged(b);
	}

	public SoftwareShowAllMeans getSoftwareShowAllMeans() {
		return softwareShowAllMeans;
	}

	public void setSoftwareShowAllMeans(SoftwareShowAllMeans meaning) {
		SoftwareShowAllMeans softwareShowAllMeans_old = softwareShowAllMeans;
		softwareShowAllMeans = meaning;

		if (softwareShowAllMeans_old != softwareShowAllMeans) {
			boolean tableChangeAware = thePanel.panelRegisteredSoftware.isAwareOfTableChangedListener();
			thePanel.panelRegisteredSoftware.setAwareOfTableChangedListener(false);

			String selectedLicencePool = null;
			logging.info(this, " setShowOnlyAssociationsToSelectedPoolOrNoPool selected licence row "
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
			switch (direction) {
			case POOL2SOFTWARE:
				thePanel.panelRegisteredSoftware.getTheSearchpane().showFilterIcon(true);

				break;

			case SOFTWARE2POOL:
				thePanel.panelRegisteredSoftware.getTheSearchpane().showFilterIcon(false);
				resetCounters(null);
				thePanel.fieldCountAssignedInEditing.setText("");
				break;
			}

			logging.info(this, "switched to " + direction);
			initializeVisualSettings();
		}

	}

	public SoftwareDirectionOfAssignment getSoftwareDirectionOfAssignment() {
		return this.softwareDirectionOfAssignment;
	}

	private boolean gotNewSWKeysForLicencePool(String selectedLicencePool) {
		if (selectedLicencePool == null)
			return false;

		List<String> oldSWList = persist.getSoftwareListByLicencePool(selectedLicencePool);
		List<String> newKeys = new ArrayList<>(thePanel.panelRegisteredSoftware.getSelectedKeys());
		newKeys.removeAll(oldSWList);

		logging.info(this, "new keys " + newKeys);

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
			if (selectedLicencePool == null)
				return false;

			validateWindowsSoftwareKeys();

			return gotNewSWKeysForLicencePool(selectedLicencePool);
		}
	}

	public String getSelectedLicencePool() {
		String result = null;

		if (thePanel.panelLicencepools.getSelectedRow() >= 0)
			result = thePanel.panelLicencepools.getValueAt(thePanel.panelLicencepools.getSelectedRow(), 0).toString();

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
