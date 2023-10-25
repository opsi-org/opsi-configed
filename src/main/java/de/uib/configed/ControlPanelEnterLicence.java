/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenuItem;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.licences.PanelEnterLicence;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.swing.FEditPane;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.swing.timeedit.FEditDate;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.CellEditor4TableText;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableEditItem;
import utils.Utils;

// tab new licence
public class ControlPanelEnterLicence extends AbstractControlMultiTablePanel {
	private PanelEnterLicence thePanel;

	private GenTableModel modelLicencekeys;
	private GenTableModel modelLicencepools;
	private GenTableModel modelLicencecontracts;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;

	public ControlPanelEnterLicence(ConfigedMain configedMain) {

		thePanel = new PanelEnterLicence(this, configedMain);
		this.configedMain = configedMain;
		init();
	}

	public List<String> getChoicesAllHosts() {
		return new ArrayList<>(new TreeMap<>(persistenceController.getHostInfoCollections()
				.getClientListForDepots(configedMain.getSelectedDepots(), configedMain.getAllowedClients())).keySet());
	}

	public void saveNewLicence(Map<String, String> m) {

		configedMain.getLicencesFrame().activateLoadingCursor();

		persistenceController.getSoftwareDataService().editSoftwareLicence(m.get(LicenceEntry.ID_KEY),
				m.get(LicenceEntry.LICENCE_CONTRACT_ID_KEY), m.get(LicenceEntry.TYPE_KEY),
				m.get(LicenceEntry.MAX_INSTALLATIONS_KEY), m.get(LicenceEntry.BOUND_TO_HOST_KEY),
				m.get(LicenceEntry.EXPIRATION_DATE_KEY));
		configedMain.softwarelicencesTableProvider.requestReloadRows();
		// ensure that the visual tables everywhere get the new data when refreshed

		String keyValue = persistenceController.getSoftwareDataService()
				.editRelationSoftwareL2LPool(m.get(LicenceEntry.ID_KEY), m.get("licensePoolId"), m.get("licenseKey"));

		modelLicencekeys.requestReload();
		modelLicencekeys.reset();
		thePanel.getPanelKeys().setDataChanged(false);
		thePanel.getPanelKeys().moveToKeyValue(keyValue);

		configedMain.getLicencesFrame().disactivateLoadingCursor();

		configedMain.checkErrorList();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<TableEditItem>();

		initPanels();
		initPopupMenu();
		// special treatment of columns
		initTreatmentOfColumns();
		// updates
		setPanelLicenceContractsUpdateController();
	}

	private void initPanels() {
		List<String> columnNames;

		// getPanelKeys()
		columnNames = new ArrayList<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		MapTableUpdateItemFactory updateItemFactoryLicencekeys = new MapTableUpdateItemFactory(modelLicencekeys,
				columnNames, 0);
		modelLicencekeys = new GenTableModel(updateItemFactoryLicencekeys, configedMain.licenceOptionsTableProvider, -1,
				new int[] { 0, 1 }, thePanel.getPanelKeys(), updateCollection);
		updateItemFactoryLicencekeys.setSource(modelLicencekeys);

		tableModels.add(modelLicencekeys);
		tablePanes.add(thePanel.getPanelKeys());

		modelLicencekeys.reset();
		thePanel.getPanelKeys().setTableModel(modelLicencekeys);
		modelLicencekeys.setEditableColumns(new int[] { 2 });
		thePanel.getPanelKeys().setEmphasizedColumns(new int[] { 2 });

		setPanelKeysUpdateController();

		// getPanelLicencepools()
		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		MapTableUpdateItemFactory updateItemFactoryLicencepools = new MapTableUpdateItemFactory(modelLicencepools,
				columnNames, 0);
		modelLicencepools = new GenTableModel(updateItemFactoryLicencepools, configedMain.licencePoolTableProvider, 0,
				thePanel.getPanelLicencepools(), updateCollection);
		updateItemFactoryLicencepools.setSource(modelLicencepools);

		tableModels.add(modelLicencepools);
		tablePanes.add(thePanel.getPanelLicencepools());

		modelLicencepools.reset();
		thePanel.getPanelLicencepools().setTableModel(modelLicencepools);

		// getPanelLicencecontracts()
		columnNames = new ArrayList<>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");
		MapTableUpdateItemFactory updateItemFactoryLicencecontracts = new MapTableUpdateItemFactory(columnNames, 0);
		modelLicencecontracts = new GenTableModel(updateItemFactoryLicencecontracts,
				configedMain.licenceContractsTableProvider, 0, thePanel.getPanelLicencecontracts(), updateCollection);
		updateItemFactoryLicencecontracts.setSource(modelLicencecontracts);

		tableModels.add(modelLicencecontracts);
		tablePanes.add(thePanel.getPanelLicencecontracts());

		modelLicencecontracts.reset();
		thePanel.getPanelLicencecontracts().setTableModel(modelLicencecontracts);
		modelLicencecontracts.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });
		thePanel.getPanelLicencecontracts().setEmphasizedColumns(new int[] { 1, 2, 3, 4, 5 });

	}

	private void initPopupMenu() {
		// --- PopupMenu
		JMenuItem menuItemAddContract = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemAddContract.addActionListener((ActionEvent e) -> addContract());

		thePanel.getPanelLicencecontracts().addPopupItem(menuItemAddContract);
	}

	private void initTreatmentOfColumns() {
		TableColumn col;

		col = thePanel.getPanelLicencecontracts().getColumnModel().getColumn(2);

		FEditDate fedConclusionDate = new FEditDate("", false);

		CellEditor4TableText cellEditorConclusionDate = new CellEditor4TableText(fedConclusionDate,
				FEditDate.AREA_DIMENSION);

		fedConclusionDate.setServedCellEditor(cellEditorConclusionDate);
		col.setCellEditor(cellEditorConclusionDate);

		// col 3
		col = thePanel.getPanelLicencecontracts().getColumnModel().getColumn(3);
		FEditDate fedNotificationDate = new FEditDate("", false);

		CellEditor4TableText cellEditorNotificationDate = new CellEditor4TableText(fedNotificationDate,
				FEditDate.AREA_DIMENSION);

		fedNotificationDate.setServedCellEditor(cellEditorNotificationDate);
		col.setCellEditor(cellEditorNotificationDate);

		// col 4
		col = thePanel.getPanelLicencecontracts().getColumnModel().getColumn(4);
		FEditDate fedExpirationDate = new FEditDate("", false);

		CellEditor4TableText cellEditorExpirationDate = new CellEditor4TableText(fedExpirationDate,
				FEditDate.AREA_DIMENSION);

		fedExpirationDate.setServedCellEditor(cellEditorExpirationDate);
		col.setCellEditor(cellEditorExpirationDate);

		// col 5
		col = thePanel.getPanelLicencecontracts().getColumnModel().getColumn(5);

		FEditPane fedNotes = new FEditPane("", "Notes");

		CellEditor4TableText cellEditorLicenceContractNotes = new CellEditor4TableText(fedNotes,
				FEditPane.AREA_DIMENSION);

		fedNotes.setServedCellEditor(cellEditorLicenceContractNotes);
		col.setCellEditor(cellEditorLicenceContractNotes);
	}

	private void setPanelKeysUpdateController() {
		thePanel.getPanelKeys().setUpdateController(
				new MapItemsUpdateController(thePanel.getPanelKeys(), modelLicencekeys, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persistenceController.getSoftwareDataService().editRelationSoftwareL2LPool(
								(String) rowmap.get("softwareLicenseId"), (String) rowmap.get("licensePoolId"),
								(String) rowmap.get("licenseKey"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencekeys.requestReload();
						return persistenceController.getSoftwareDataService().deleteRelationSoftwareL2LPool(
								(String) rowmap.get("softwareLicenseId"), (String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));
	}

	private void setPanelLicenceContractsUpdateController() {
		thePanel.getPanelLicencecontracts().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelLicencecontracts(), modelLicencecontracts, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persistenceController.getLicenseDataService().editLicenseContract(
								(String) rowmap.get("licenseContractId"), (String) rowmap.get("partner"),
								(String) rowmap.get("conclusionDate"), (String) rowmap.get("notificationDate"),
								(String) rowmap.get("expirationDate"), (String) rowmap.get("notes"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencecontracts.requestReload();
						return persistenceController.getLicenseDataService()
								.deleteLicenseContract((String) rowmap.get("licenseContractId"));
					}
				}, updateCollection));
	}

	private void addContract() {
		Object[] a = new Object[6];
		a[0] = "c_" + Utils.getSeconds();
		a[1] = "";
		a[2] = Utils.getDate();
		a[3] = Globals.ZERODATE;
		a[4] = Globals.ZERODATE;
		a[5] = "";

		modelLicencecontracts.addRow(a);
		thePanel.getPanelLicencecontracts().moveToValue("" + a[0], 0);
	}
}
