package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import de.uib.configed.gui.licences.PanelEnterLicence;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.swing.FEditPane;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.swing.timeedit.FEditDate;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.CellEditor4TableText;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;
import de.uib.utilities.thread.WaitCursor;

public class ControlPanelEnterLicence extends AbstractControlMultiTablePanel
// tab new licence
{
	PanelEnterLicence thePanel;

	GenTableModel modelLicencekeys;
	GenTableModel modelLicencepools;
	GenTableModel modelLicencecontracts;

	AbstractPersistenceController persist;
	private ConfigedMain configedMain;

	public ControlPanelEnterLicence(AbstractPersistenceController persist, ConfigedMain configedMain) {

		thePanel = new PanelEnterLicence(this, configedMain);
		this.persist = persist;
		this.configedMain = configedMain;
		init();
	}

	public List<String> getChoicesAllHosts() {
		return new ArrayList<>(new TreeMap<>(persist.getHostInfoCollections()
				.getClientListForDepots(configedMain.getSelectedDepots(), configedMain.getAllowedClients())).keySet());
	}

	public void saveNewLicence(Map<String, String> m) {
		WaitCursor waitCursor = new WaitCursor(Globals.container1, configedMain.licencesFrame.getCursor());
		persist.editSoftwareLicence(m.get(LicenceEntry.ID_KEY), m.get(LicenceEntry.LICENCE_CONTRACT_ID_KEY),
				m.get(LicenceEntry.TYPE_KEY), m.get(LicenceEntry.MAX_INSTALLATIONS_KEY),
				m.get(LicenceEntry.BOUND_TO_HOST_KEY), m.get(LicenceEntry.EXPIRATION_DATE_KEY));
		configedMain.softwarelicencesTableProvider.requestReloadRows();
		// ensure that the visual tables everywhere get the new data when refreshed

		String keyValue = persist.editRelationSoftwareL2LPool(m.get(LicenceEntry.ID_KEY), m.get("licensePoolId"),
				m.get("licenseKey"));

		modelLicencekeys.requestReload();
		modelLicencekeys.reset();
		thePanel.panelKeys.setDataChanged(false);
		thePanel.panelKeys.moveToKeyValue(keyValue);
		waitCursor.stop();
		configedMain.checkErrorList();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new TableUpdateCollection();

		List<String> columnNames;
		List<String> classNames;

		// panelKeys
		columnNames = new ArrayList<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencekeys = new MapTableUpdateItemFactory(modelLicencekeys,
				columnNames, classNames, 0);
		modelLicencekeys = new GenTableModel(updateItemFactoryLicencekeys, configedMain.licenceOptionsTableProvider, -1,
				new int[] { 0, 1 }, thePanel.panelKeys, updateCollection);
		updateItemFactoryLicencekeys.setSource(modelLicencekeys);

		tableModels.add(modelLicencekeys);
		tablePanes.add(thePanel.panelKeys);

		modelLicencekeys.reset();
		thePanel.panelKeys.setTableModel(modelLicencekeys);
		modelLicencekeys.setEditableColumns(new int[] { 2 });
		thePanel.panelKeys.setEmphasizedColumns(new int[] { 2 });

		thePanel.panelKeys.setUpdateController(
				new MapItemsUpdateController(thePanel.panelKeys, modelLicencekeys, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persist.editRelationSoftwareL2LPool((String) rowmap.get("softwareLicenseId"),
								(String) rowmap.get("licensePoolId"), (String) rowmap.get("licenseKey"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencekeys.requestReload();
						return persist.deleteRelationSoftwareL2LPool((String) rowmap.get("softwareLicenseId"),
								(String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));

		// panelLicencepools
		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencepools = new MapTableUpdateItemFactory(modelLicencepools,
				columnNames, classNames, 0);
		modelLicencepools = new GenTableModel(updateItemFactoryLicencepools, configedMain.licencePoolTableProvider, 0,
				thePanel.panelLicencepools, updateCollection);
		updateItemFactoryLicencepools.setSource(modelLicencepools);

		tableModels.add(modelLicencepools);
		tablePanes.add(thePanel.panelLicencepools);

		modelLicencepools.reset();
		thePanel.panelLicencepools.setTableModel(modelLicencepools);

		// panelLicencecontracts
		columnNames = new ArrayList<>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencecontracts = new MapTableUpdateItemFactory(columnNames,
				classNames, 0);
		modelLicencecontracts = new GenTableModel(updateItemFactoryLicencecontracts,
				configedMain.licenceContractsTableProvider, 0, thePanel.panelLicencecontracts, updateCollection);
		updateItemFactoryLicencecontracts.setSource(modelLicencecontracts);

		tableModels.add(modelLicencecontracts);
		tablePanes.add(thePanel.panelLicencecontracts);

		modelLicencecontracts.reset();
		thePanel.panelLicencecontracts.setTableModel(modelLicencecontracts);
		modelLicencecontracts.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });
		thePanel.panelLicencecontracts.setEmphasizedColumns(new int[] { 1, 2, 3, 4, 5 });

		// --- PopupMenu
		JMenuItemFormatted menuItemAddContract = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemAddContract.addActionListener(e -> {
			Object[] a = new Object[6];
			a[0] = "c_" + Globals.getSeconds();
			a[1] = "";
			a[2] = Globals.getDate(false);
			a[3] = Globals.ZERODATE;
			a[4] = Globals.ZERODATE;
			a[5] = "";

			modelLicencecontracts.addRow(a);
			thePanel.panelLicencecontracts.moveToValue("" + a[0], 0);
		});

		thePanel.panelLicencecontracts.addPopupItem(menuItemAddContract);

		// special treatment of columns
		javax.swing.table.TableColumn col;

		col = thePanel.panelLicencecontracts.getColumnModel().getColumn(2);

		FEditDate fedConclusionDate = new FEditDate("", false);

		CellEditor4TableText cellEditorConclusionDate = new CellEditor4TableText(fedConclusionDate,
				FEditDate.AREA_DIMENSION);

		fedConclusionDate.setServedCellEditor(cellEditorConclusionDate);
		col.setCellEditor(cellEditorConclusionDate);

		// col 3
		col = thePanel.panelLicencecontracts.getColumnModel().getColumn(3);
		FEditDate fedNotificationDate = new FEditDate("", false);

		CellEditor4TableText cellEditorNotificationDate = new CellEditor4TableText(fedNotificationDate,
				FEditDate.AREA_DIMENSION);

		fedNotificationDate.setServedCellEditor(cellEditorNotificationDate);
		col.setCellEditor(cellEditorNotificationDate);

		// col 4
		col = thePanel.panelLicencecontracts.getColumnModel().getColumn(4);
		FEditDate fedExpirationDate = new FEditDate("", false);

		CellEditor4TableText cellEditorExpirationDate = new CellEditor4TableText(fedExpirationDate,
				FEditDate.AREA_DIMENSION);

		fedExpirationDate.setServedCellEditor(cellEditorExpirationDate);
		col.setCellEditor(cellEditorExpirationDate);

		// col 5
		col = thePanel.panelLicencecontracts.getColumnModel().getColumn(5);

		FEditPane fedNotes = new FEditPane("", "Notes");

		CellEditor4TableText cellEditorLicenceContractNotes = new de.uib.utilities.table.gui.CellEditor4TableText(
				fedNotes, FEditPane.AREA_DIMENSION);

		fedNotes.setServedCellEditor(cellEditorLicenceContractNotes);
		col.setCellEditor(cellEditorLicenceContractNotes);

		// updates
		thePanel.panelLicencecontracts.setUpdateController(new MapItemsUpdateController(thePanel.panelLicencecontracts,
				modelLicencecontracts, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persist.editLicenceContract((String) rowmap.get("licenseContractId"),
								(String) rowmap.get("partner"), (String) rowmap.get("conclusionDate"),
								(String) rowmap.get("notificationDate"), (String) rowmap.get("expirationDate"),
								(String) rowmap.get("notes"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencecontracts.requestReload();
						return persist.deleteLicenceContract((String) rowmap.get("licenseContractId"));
					}
				}, updateCollection));

	}
}
