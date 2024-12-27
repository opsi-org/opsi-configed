/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JMenuItem;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.licenses.LicenseManagement;
import de.uib.configed.gui.licenses.MultiTablePanel;
import de.uib.configed.gui.licenses.PanelEnterLicense;
import de.uib.configed.type.licenses.LicenseEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.FEditPane;
import de.uib.utils.table.GenTableModel;
import de.uib.utils.table.gui.CellDateEditor;
import de.uib.utils.table.gui.CellEditor4TableText;
import de.uib.utils.table.updates.MapBasedTableEditItem;
import de.uib.utils.table.updates.MapBasedUpdater;
import de.uib.utils.table.updates.MapItemsUpdateController;
import de.uib.utils.table.updates.MapTableUpdateItemFactory;

public class ControlPanelEnterLicense extends AbstractControlMultiTablePanel {
	private PanelEnterLicense thePanel;

	private GenTableModel modelLicensekeys;
	private GenTableModel modelLicensepools;
	private GenTableModel modelLicensecontracts;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;
	private LicenseManagement licensesPane;

	public ControlPanelEnterLicense(ConfigedMain configedMain, LicenseManagement licensesPane) {
		thePanel = new PanelEnterLicense(this);
		this.configedMain = configedMain;
		this.licensesPane = licensesPane;

		init();
	}

	public Set<String> getChoicesAllHosts() {
		return new TreeSet<>(persistenceController.getHostInfoCollections()
				.getClientsForDepots(configedMain.getSelectedDepots(), configedMain.getAllowedClients()));
	}

	public void saveNewLicense(Map<String, String> m) {
		ConfigedMain.getMainFrame().activateLoadingCursor();

		persistenceController.getSoftwareDataService().editSoftwareLicense(m.get(LicenseEntry.ID_KEY),
				m.get(LicenseEntry.LICENSE_CONTRACT_ID_KEY), m.get(LicenseEntry.TYPE_KEY),
				m.get(LicenseEntry.MAX_INSTALLATIONS_KEY), m.get(LicenseEntry.BOUND_TO_HOST_KEY),
				m.get(LicenseEntry.EXPIRATION_DATE_KEY));
		licensesPane.getSoftwarelicensesTableProvider().requestReloadRows();
		// ensure that the visual tables everywhere get the new data when refreshed

		String keyValue = persistenceController.getSoftwareDataService()
				.editRelationSoftwareL2LPool(m.get(LicenseEntry.ID_KEY), m.get("licensePoolId"), m.get("licenseKey"));

		modelLicensekeys.requestReload();
		modelLicensekeys.reset();
		thePanel.getPanelKeys().setDataChanged(false);
		thePanel.getPanelKeys().moveToKeyValue(keyValue);

		ConfigedMain.getMainFrame().deactivateLoadingCursor();

		Logging.checkErrorList();
	}

	@Override
	public MultiTablePanel getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<MapBasedTableEditItem>();

		initPanels();
		initPopupMenu();
		initTreatmentOfColumns();
		setPanelLicenseContractsUpdateController();
	}

	private void initPanels() {
		List<String> columnNames;

		columnNames = new ArrayList<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		MapTableUpdateItemFactory updateItemFactoryLicensekeys = new MapTableUpdateItemFactory(modelLicensekeys,
				columnNames);
		modelLicensekeys = new GenTableModel(updateItemFactoryLicensekeys,
				licensesPane.getLicenseOptionsTableProvider(), -1, new int[] { 0, 1 }, thePanel.getPanelKeys(),
				updateCollection, true);
		updateItemFactoryLicensekeys.setSource(modelLicensekeys);

		tableModels.add(modelLicensekeys);
		tablePanes.add(thePanel.getPanelKeys());

		modelLicensekeys.reset();
		thePanel.getPanelKeys().setTableModel(modelLicensekeys);
		modelLicensekeys.setEditableColumns(new int[] { 2 });

		setPanelKeysUpdateController();

		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		MapTableUpdateItemFactory updateItemFactoryLicensepools = new MapTableUpdateItemFactory(modelLicensepools,
				columnNames);
		modelLicensepools = new GenTableModel(updateItemFactoryLicensepools, licensesPane.getLicensePoolTableProvider(),
				0, thePanel.getPanelLicensePools(), updateCollection);
		updateItemFactoryLicensepools.setSource(modelLicensepools);

		tableModels.add(modelLicensepools);
		tablePanes.add(thePanel.getPanelLicensePools());

		modelLicensepools.reset();
		thePanel.getPanelLicensePools().setTableModel(modelLicensepools);

		columnNames = new ArrayList<>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");
		MapTableUpdateItemFactory updateItemFactoryLicensecontracts = new MapTableUpdateItemFactory(columnNames);
		modelLicensecontracts = new GenTableModel(updateItemFactoryLicensecontracts,
				licensesPane.getLicenseContractsTableProvider(), 0, thePanel.getPanelLicenseContracts(),
				updateCollection);
		updateItemFactoryLicensecontracts.setSource(modelLicensecontracts);

		tableModels.add(modelLicensecontracts);
		tablePanes.add(thePanel.getPanelLicenseContracts());

		modelLicensecontracts.reset();
		thePanel.getPanelLicenseContracts().setTableModel(modelLicensecontracts);
		modelLicensecontracts.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });
	}

	private void initPopupMenu() {
		JMenuItem menuItemAddContract = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.NewLicensecontract"));
		menuItemAddContract.addActionListener(actionEvent -> addContract());

		thePanel.getPanelLicenseContracts().addPopupItem(menuItemAddContract);
	}

	private void initTreatmentOfColumns() {
		TableColumn col;

		col = thePanel.getPanelLicenseContracts().getJTable().getColumnModel().getColumn(2);

		col.setCellEditor(new CellDateEditor());

		col = thePanel.getPanelLicenseContracts().getJTable().getColumnModel().getColumn(3);

		col.setCellEditor(new CellDateEditor());

		col = thePanel.getPanelLicenseContracts().getJTable().getColumnModel().getColumn(4);

		col.setCellEditor(new CellDateEditor());

		col = thePanel.getPanelLicenseContracts().getJTable().getColumnModel().getColumn(5);

		FEditPane fedNotes = new FEditPane("", "Notes");

		CellEditor4TableText cellEditorLicenseContractNotes = new CellEditor4TableText(fedNotes,
				FEditPane.AREA_DIMENSION);

		fedNotes.setServedCellEditor(cellEditorLicenseContractNotes);
		col.setCellEditor(cellEditorLicenseContractNotes);
	}

	private void setPanelKeysUpdateController() {
		thePanel.getPanelKeys().setUpdateController(
				new MapItemsUpdateController(thePanel.getPanelKeys(), modelLicensekeys, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persistenceController.getSoftwareDataService().editRelationSoftwareL2LPool(
								(String) rowmap.get("softwareLicenseId"), (String) rowmap.get("licensePoolId"),
								(String) rowmap.get("licenseKey"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicensekeys.requestReload();
						return persistenceController.getSoftwareDataService().deleteRelationSoftwareL2LPool(
								(String) rowmap.get("softwareLicenseId"), (String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));
	}

	private void setPanelLicenseContractsUpdateController() {
		thePanel.getPanelLicenseContracts().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelLicenseContracts(), modelLicensecontracts, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persistenceController.getLicenseDataService().editLicenseContract(
								(String) rowmap.get("licenseContractId"), (String) rowmap.get("partner"),
								(String) rowmap.get("conclusionDate"), (String) rowmap.get("notificationDate"),
								(String) rowmap.get("expirationDate"), (String) rowmap.get("notes"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicensecontracts.requestReload();
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
		a[3] = "";
		a[4] = "";
		a[5] = "";

		modelLicensecontracts.addRow(a);
		thePanel.getPanelLicenseContracts().moveToValue("" + a[0], 0);
	}
}
