/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.licenses.MultiTablePanel;
import de.uib.configed.gui.licenses.PanelEditLicenses;
import de.uib.configed.type.licenses.LicenseEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditDate;
import de.uib.utilities.swing.FEditPane;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.AdaptingCellEditor;
import de.uib.utilities.table.gui.CellEditor4TableText;
import de.uib.utilities.table.updates.MapBasedTableEditItem;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import utils.Utils;

public class ControlPanelEditLicenses extends AbstractControlMultiTablePanel {
	private PanelEditLicenses thePanel;

	private GenTableModel modelLicensekeys;
	private GenTableModel modelSoftwarelicenses;
	private GenTableModel modelLicensecontracts;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain configedMain;

	public ControlPanelEditLicenses(ConfigedMain configedMain) {
		thePanel = new PanelEditLicenses(this);
		this.configedMain = configedMain;
		init();
	}

	@Override
	public MultiTablePanel getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<MapBasedTableEditItem>();

		initPanelKeys();

		initPanelSoftwarelicenses();

		initPanelLicenseContracts();
	}

	private void initPanelKeys() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		MapTableUpdateItemFactory updateItemFactoryLicensekeys = new MapTableUpdateItemFactory(modelLicensekeys,
				columnNames);
		modelLicensekeys = new GenTableModel(updateItemFactoryLicensekeys, configedMain.licenseOptionsTableProvider, -1,
				new int[] { 0, 1 }, thePanel.getPanelKeys(), updateCollection, true);
		updateItemFactoryLicensekeys.setSource(modelLicensekeys);

		tableModels.add(modelLicensekeys);
		tablePanes.add(thePanel.getPanelKeys());

		modelLicensekeys.reset();
		thePanel.getPanelKeys().setTableModel(modelLicensekeys);
		modelLicensekeys.setEditableColumns(new int[] { 0, 1, 2 });

		JMenuItem menuItemAddKey = new JMenuItem(Configed.getResourceValue("ConfigedMain.Licenses.NewLicensekey"));
		menuItemAddKey.addActionListener((ActionEvent e) -> {
			Object[] a = new Object[3];
			a[0] = "";
			a[1] = "";
			a[2] = "";

			modelLicensekeys.addRow(a);
			thePanel.getPanelKeys().moveToLastRow();
			thePanel.getPanelKeys().moveToValue("" + a[0], 0);
		});

		thePanel.getPanelKeys().addPopupItem(menuItemAddKey);

		TableColumn col = thePanel.getPanelKeys().getColumnModel().getColumn(1);
		JComboBox<String> selectionComboBox = new JComboBox<>();
		col.setCellEditor(new AdaptingCellEditor(selectionComboBox, (int row, int column) -> {
			List<String> poolIds = configedMain.licensePoolTableProvider.getOrderedColumn(
					configedMain.licensePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1) {
				poolIds.add("");
			}
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		thePanel.getPanelKeys().setUpdateController(
				new MapItemsUpdateController(thePanel.getPanelKeys(), modelLicensekeys, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						Logging.info(this, "sendUpdate " + rowmap);

						return persistenceController.getSoftwareDataService().editRelationSoftwareL2LPool(
								(String) rowmap.get("softwareLicenseId"), (String) rowmap.get("licensePoolId"),
								(String) rowmap.get("licenseKey"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						Logging.info(this, "sendDelete " + rowmap);
						modelLicensekeys.requestReload();
						return persistenceController.getSoftwareDataService().deleteRelationSoftwareL2LPool(
								(String) rowmap.get("softwareLicenseId"), (String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));
	}

	private void initPanelSoftwarelicenses() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add(LicenseEntry.ID_KEY);
		columnNames.add(LicenseEntry.LICENSE_CONTRACT_ID_KEY);
		columnNames.add(LicenseEntry.TYPE_KEY);
		columnNames.add(LicenseEntry.MAX_INSTALLATIONS_KEY);
		columnNames.add(LicenseEntry.BOUND_TO_HOST_KEY);
		columnNames.add(LicenseEntry.EXPIRATION_DATE_KEY);
		MapTableUpdateItemFactory updateItemFactorySoftwarelicenses = new MapTableUpdateItemFactory(
				modelSoftwarelicenses, columnNames);
		modelSoftwarelicenses = new GenTableModel(updateItemFactorySoftwarelicenses,
				configedMain.softwarelicensesTableProvider, 0, thePanel.getPanelSoftwarelicenses(), updateCollection);
		updateItemFactorySoftwarelicenses.setSource(modelSoftwarelicenses);

		tableModels.add(modelSoftwarelicenses);
		tablePanes.add(thePanel.getPanelSoftwarelicenses());

		modelSoftwarelicenses.reset();
		thePanel.getPanelSoftwarelicenses().setTableModel(modelSoftwarelicenses);
		modelSoftwarelicenses.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });

		TableColumn col = thePanel.getPanelSoftwarelicenses().getColumnModel().getColumn(2);
		JComboBox<String> comboLicenseTypes = new JComboBox<>(LicenseEntry.LICENSE_TYPES.toArray(String[]::new));
		col.setCellEditor(new DefaultCellEditor(comboLicenseTypes));

		col = thePanel.getPanelSoftwarelicenses().getColumnModel().getColumn(4);
		JComboBox<String> selectionComboBox = new JComboBox<>();
		col.setCellEditor(new AdaptingCellEditor(selectionComboBox, (int row, int column) -> {
			List<String> choicesAllHosts = new ArrayList<>(new TreeSet<>(persistenceController.getHostInfoCollections()
					.getClientsForDepots(configedMain.getSelectedDepots(), configedMain.getAllowedClients())));
			choicesAllHosts.set(0, "");
			return new DefaultComboBoxModel<>(choicesAllHosts.toArray(String[]::new));
		}));

		col = thePanel.getPanelSoftwarelicenses().getColumnModel().getColumn(5);
		col.setCellEditor(new CellEditor4TableText(new FEditDate(""), FEditDate.AREA_DIMENSION));

		JMenuItem menuItemAddLicense = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.NewSoftwarelicense"));
		menuItemAddLicense.addActionListener((ActionEvent e) -> addLicense());

		thePanel.getPanelSoftwarelicenses().addPopupItem(menuItemAddLicense);

		JMenuItem menuItemPickSoftwarelicense = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.MenuItemTransferIDFromSoftwarelicenseToLicensekey"));
		menuItemPickSoftwarelicense.addActionListener((ActionEvent e) -> pickSoftwareLicense());

		thePanel.getPanelSoftwarelicenses().addPopupItem(menuItemPickSoftwarelicense);

		thePanel.getPanelSoftwarelicenses().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelSoftwarelicenses(), modelSoftwarelicenses, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> m) {
						return persistenceController.getSoftwareDataService().editSoftwareLicense(
								(String) m.get("softwareLicenseId"), (String) m.get("licenseContractId"),
								(String) m.get("licenseType"),
								LicenseEntry.produceNormalizedCount("" + m.get("maxInstallations")),
								(String) m.get("boundToHost"), (String) m.get("expirationDate"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> m) {
						modelSoftwarelicenses.requestReload();
						return persistenceController.getSoftwareDataService()
								.deleteSoftwareLicense((String) m.get("softwareLicenseId"));
					}
				}, updateCollection));
	}

	private void initPanelLicenseContracts() {
		List<String> columnNames = new ArrayList<>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");
		MapTableUpdateItemFactory updateItemFactoryLicensecontracts = new MapTableUpdateItemFactory(columnNames);
		modelLicensecontracts = new GenTableModel(updateItemFactoryLicensecontracts,
				configedMain.licenseContractsTableProvider, 0, thePanel.getPanelLicensecontracts(), updateCollection);
		updateItemFactoryLicensecontracts.setSource(modelLicensecontracts);

		tableModels.add(modelLicensecontracts);
		tablePanes.add(thePanel.getPanelLicensecontracts());

		modelLicensecontracts.reset();
		thePanel.getPanelLicensecontracts().setTableModel(modelLicensecontracts);
		modelLicensecontracts.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });

		JMenuItem menuItemAddContract = new JMenuItem(
				Configed.getResourceValue("ConfigedMain.Licenses.NewLicensecontract"));
		menuItemAddContract.addActionListener((ActionEvent e) -> addContract());

		thePanel.getPanelLicensecontracts().addPopupItem(menuItemAddContract);

		JMenuItem menuItemPickLicensecontract = new JMenuItem(Configed
				.getResourceValue("ConfigedMain.Licenses.MenuItemTransferIDFromLicensecontractToSoftwarelicense"));
		menuItemPickLicensecontract.addActionListener((ActionEvent e) -> pickLicenseContract());

		thePanel.getPanelLicensecontracts().addPopupItem(menuItemPickLicensecontract);

		TableColumn col = thePanel.getPanelLicensecontracts().getColumnModel().getColumn(2);

		FEditDate fedConclusionDate = new FEditDate("");

		CellEditor4TableText cellEditorConclusionDate = new CellEditor4TableText(fedConclusionDate,
				FEditDate.AREA_DIMENSION);

		fedConclusionDate.setServedCellEditor(cellEditorConclusionDate);
		col.setCellEditor(cellEditorConclusionDate);

		col = thePanel.getPanelLicensecontracts().getColumnModel().getColumn(3);
		FEditDate fedNotificationDate = new FEditDate("");

		CellEditor4TableText cellEditorNotificationDate = new CellEditor4TableText(fedNotificationDate,
				FEditDate.AREA_DIMENSION);

		fedNotificationDate.setServedCellEditor(cellEditorNotificationDate);
		col.setCellEditor(cellEditorNotificationDate);

		col = thePanel.getPanelLicensecontracts().getColumnModel().getColumn(4);
		FEditDate fedExpirationDate = new FEditDate("");

		CellEditor4TableText cellEditorExpirationDate = new CellEditor4TableText(fedExpirationDate,
				FEditDate.AREA_DIMENSION);

		fedExpirationDate.setServedCellEditor(cellEditorExpirationDate);
		col.setCellEditor(cellEditorExpirationDate);

		col = thePanel.getPanelLicensecontracts().getColumnModel().getColumn(5);

		FEditPane fedNotes = new FEditPane("", "Notes");
		CellEditor4TableText cellEditorLicenseContractNotes = new CellEditor4TableText(fedNotes,
				FEditPane.AREA_DIMENSION);

		fedNotes.setServedCellEditor(cellEditorLicenseContractNotes);
		col.setCellEditor(cellEditorLicenseContractNotes);

		thePanel.getPanelLicensecontracts().setUpdateController(new MapItemsUpdateController(
				thePanel.getPanelLicensecontracts(), modelLicensecontracts, new MapBasedUpdater() {
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

	private void addLicense() {
		Object[] a = new Object[6];
		a[0] = "l_" + Utils.getSeconds();
		a[1] = "";
		a[2] = LicenseEntry.LICENSE_TYPES.get(0);
		a[3] = "1";
		a[4] = "";
		a[5] = "";

		modelSoftwarelicenses.addRow(a);
		thePanel.getPanelSoftwarelicenses().moveToValue("" + a[0], 0);
	}

	private void pickSoftwareLicense() {
		boolean keyNew = false;
		Iterator<MapBasedTableEditItem> iter = updateCollection.iterator();
		while (iter.hasNext() && !keyNew) {
			MapBasedTableEditItem update = iter.next();
			if (update.getSource() == modelSoftwarelicenses && update.keyChanged()) {
				keyNew = true;
			}
		}
		if (keyNew) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.PleaseSaveKeyRow"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return;
		}

		if (thePanel.getPanelSoftwarelicenses().getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.SourceOrTargetRowNotSelected.text"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return;
		}

		if (thePanel.getPanelKeys().getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.SourceOrTargetRowNotSelected.text"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);

			return;
		}

		String val = (String) modelSoftwarelicenses
				.getValueAt(thePanel.getPanelSoftwarelicenses().getSelectedRowInModelTerms(), 0);

		thePanel.getPanelKeys().setValueAt(val, thePanel.getPanelKeys().getSelectedRow(), 0);
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
		thePanel.getPanelLicensecontracts().moveToValue("" + a[0], 0);
	}

	private void pickLicenseContract() {
		boolean keyNew = false;
		Iterator<MapBasedTableEditItem> iter = updateCollection.iterator();
		while (iter.hasNext() && !keyNew) {
			MapBasedTableEditItem update = iter.next();
			if (update.getSource() == modelLicensecontracts && update.keyChanged()) {
				keyNew = true;
			}
		}
		if (keyNew) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.PleaseSaveKeyRow"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return;
		}

		if (thePanel.getPanelLicensecontracts().getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.SourceOrTargetRowNotSelected.text"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return;
		}

		if (thePanel.getPanelSoftwarelicenses().getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(ConfigedMain.getLicensesFrame(),
					Configed.getResourceValue("ConfigedMain.Licenses.SourceOrTargetRowNotSelected.text"),
					Configed.getResourceValue("ConfigedMain.Licenses.hint.title"), JOptionPane.OK_OPTION);
			return;
		}

		String val = (String) modelLicensecontracts
				.getValueAt(thePanel.getPanelLicensecontracts().getSelectedRowInModelTerms(), 0);

		thePanel.getPanelSoftwarelicenses().setValueAt(val, thePanel.getPanelSoftwarelicenses().getSelectedRow(), 1);
	}
}
