/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.table.TableColumn;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.licenses.LicenseManagement;
import de.uib.configed.gui.features.licenses.MultiTablePanel;
import de.uib.configed.gui.features.licenses.PanelEnterLicense;
import de.uib.configed.gui.share.table.GenTableModel;
import de.uib.configed.gui.share.table.gui.CellDateEditor;
import de.uib.configed.gui.share.table.gui.CellInputDialogEditor;
import de.uib.configed.gui.share.table.updates.MapBasedTableEditItem;
import de.uib.configed.gui.share.table.updates.MapBasedUpdater;
import de.uib.configed.gui.share.table.updates.MapItemsUpdateController;
import de.uib.configed.gui.share.table.updates.MapTableUpdateItemFactory;
import de.uib.configed.gui.type.licenses.LicenseEntry;
import de.uib.configed.share.logging.Logging;

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
		return persistenceController.getDataServices().hostInfoCollections
				.getClientsForDepots(configedMain.getSelectedDepots(), configedMain.getAllowedClients());
	}

	public void saveNewLicense(Map<String, String> m) {
		ConfigedMain.getMainFrame().activateLoadingCursor();

		persistenceController.getDataServices().software.editSoftwareLicense(m.get(LicenseEntry.ID_KEY),
				m.get(LicenseEntry.LICENSE_CONTRACT_ID_KEY), m.get(LicenseEntry.TYPE_KEY),
				m.get(LicenseEntry.MAX_INSTALLATIONS_KEY), m.get(LicenseEntry.BOUND_TO_HOST_KEY),
				m.get(LicenseEntry.EXPIRATION_DATE_KEY));
		licensesPane.getSoftwarelicensesTableProvider().requestReloadRows();
		// ensure that the visual tables everywhere get the new data when refreshed

		String keyValue = persistenceController.getDataServices().software
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
		panelGenEdits.add(thePanel.getPanelKeys());

		modelLicensekeys.reset();
		thePanel.getPanelKeys().setTableModel(modelLicensekeys);
		thePanel.getPanelKeys().restoreFilter();
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
		panelGenEdits.add(thePanel.getPanelLicensePools());

		modelLicensepools.reset();
		thePanel.getPanelLicensePools().setTableModel(modelLicensepools);
		thePanel.getPanelLicensePools().restoreFilter();

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
		panelGenEdits.add(thePanel.getPanelLicenseContracts());

		modelLicensecontracts.reset();
		thePanel.getPanelLicenseContracts().setTableModel(modelLicensecontracts);
		thePanel.getPanelLicenseContracts().restoreFilter();
		modelLicensecontracts.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });
	}

	private void initPopupMenu() {
		JMenuItem menuItemAddContract = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.NewLicensecontract"));
		menuItemAddContract.addActionListener(actionEvent -> ControlPanelEditLicenses.addContract(modelLicensecontracts,
				thePanel.getPanelLicenseContracts()));
		menuItemAddContract.setEnabled(!persistenceController.getDataServices().userRoles.isGlobalReadOnly());

		thePanel.getPanelLicenseContracts().addPopupItem(menuItemAddContract);
	}

	private void initTreatmentOfColumns() {
		TableColumn col;

		col = thePanel.getPanelLicenseContracts().getGenEditTable().getColumnModel().getColumn(2);

		col.setCellEditor(new CellDateEditor());

		col = thePanel.getPanelLicenseContracts().getGenEditTable().getColumnModel().getColumn(3);

		col.setCellEditor(new CellDateEditor());

		col = thePanel.getPanelLicenseContracts().getGenEditTable().getColumnModel().getColumn(4);

		col.setCellEditor(new CellDateEditor());

		col = thePanel.getPanelLicenseContracts().getGenEditTable().getColumnModel().getColumn(5);

		col.setCellEditor(new CellInputDialogEditor());
	}

	private void setPanelKeysUpdateController() {
		thePanel.getPanelKeys().setUpdateController(
				new MapItemsUpdateController(thePanel.getPanelKeys(), modelLicensekeys, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persistenceController.getDataServices().software.editRelationSoftwareL2LPool(
								(String) rowmap.get("softwareLicenseId"), (String) rowmap.get("licensePoolId"),
								(String) rowmap.get("licenseKey"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicensekeys.requestReload();
						return persistenceController.getDataServices().software.deleteRelationSoftwareL2LPool(
								(String) rowmap.get("softwareLicenseId"), (String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));
	}

	private void setPanelLicenseContractsUpdateController() {
		thePanel.getPanelLicenseContracts().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelLicenseContracts(), modelLicensecontracts, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persistenceController.getDataServices().license.editLicenseContract(
								(String) rowmap.get("licenseContractId"), (String) rowmap.get("partner"),
								(String) rowmap.get("conclusionDate"), (String) rowmap.get("notificationDate"),
								(String) rowmap.get("expirationDate"), (String) rowmap.get("notes"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicensecontracts.requestReload();
						return persistenceController.getDataServices().license
								.deleteLicenseContract((String) rowmap.get("licenseContractId"));
					}
				}, updateCollection));
	}
}
