/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.licences.PanelLicencesUsage;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.ComboBoxModeller;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.CellEditor4TableText;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableEditItem;

public class ControlPanelLicencesUsage extends AbstractControlMultiTablePanel {
	private PanelLicencesUsage thePanel;

	private GenTableModel modelLicencesUsage;
	private GenTableModel modelLicencepools;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();
	private ConfigedMain mainController;

	public ControlPanelLicencesUsage(ConfigedMain mainController) {
		thePanel = new PanelLicencesUsage(this);
		this.mainController = mainController;

		init();
	}

	public String getSoftwareLicenceReservation(String clientId) {
		if (clientId == null || clientId.isEmpty()) {
			JOptionPane.showMessageDialog(mainController.getLicencesFrame(),
					Configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectClient"),
					Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		List<String> selectedLPoolIds = thePanel.getPanelLicencePools().getSelectedKeys();

		if (selectedLPoolIds == null || selectedLPoolIds.size() != 1) {
			JOptionPane.showMessageDialog(mainController.getLicencesFrame(),
					Configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectOneLicencepool"),
					Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		String licencePoolId = selectedLPoolIds.iterator().next();
		String result = persistenceController.getLicenceUsage(clientId, licencePoolId);

		if (result != null) {
			thePanel.getPanelUsage().reload();

			thePanel.getPanelUsage().moveToKeyValue(result);
		} else {
			thePanel.getPanelUsage().moveToValue(clientId, 0, true);
		}

		return result;

	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public void initializeVisualSettings() {
		thePanel.setDivider();
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<TableEditItem>();

		initPanels();

		// combo clients
		thePanel.setClientsSource(new ComboBoxModeller() {
			@Override
			public ComboBoxModel<String> getComboBoxModel(int row, int column) {
				List<String> choicesAllHosts = new ArrayList<>(persistenceController.getHostInfoCollections()
						.getClientListForDepots(mainController.getSelectedDepots(), mainController.getAllowedClients())
						.keySet());

				choicesAllHosts.set(0, "");

				Logging.debug(this, "choicesAllHosts " + choicesAllHosts);

				return new DefaultComboBoxModel<>(choicesAllHosts.toArray(String[]::new));
			}
		});

	}

	private void initPanels() {
		List<String> columnNames;
		List<String> classNames;

		// --- panelLicencesUsage
		columnNames = new ArrayList<>();
		columnNames.add(LicenceUsageEntry.CLIENT_ID_KEY);
		columnNames.add(LicenceUsageEntry.LICENCE_ID_KEY);
		columnNames.add(LicenceUsageEntry.LICENCE_POOL_ID_KEY);
		columnNames.add(LicenceUsageEntry.LICENCE_KEY_KEY);
		columnNames.add(LicenceUsageEntry.NOTES_KEY);
		classNames = new ArrayList<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencesUsage = new MapTableUpdateItemFactory(modelLicencesUsage,
				columnNames, 0);
		modelLicencesUsage = new GenTableModel(updateItemFactoryLicencesUsage,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, () -> {
					persistenceController.licencesUsageRequestRefresh();
					return (Map) persistenceController.getLicencesUsage();
				})), -1, new int[] { 0, 1, 2 }, thePanel.getPanelUsage(), updateCollection);
		updateItemFactoryLicencesUsage.setSource(modelLicencesUsage);

		tableModels.add(modelLicencesUsage);
		tablePanes.add(thePanel.getPanelUsage());

		modelLicencesUsage.reset();
		thePanel.getPanelUsage().setTableModel(modelLicencesUsage);
		modelLicencesUsage.setEditableColumns(new int[] { 3, 4 });
		thePanel.getPanelUsage().setEmphasizedColumns(new int[] { 3, 4 });

		// special treatment of columns
		TableColumn col;
		col = thePanel.getPanelUsage().getColumnModel().getColumn(4);
		col.setCellEditor(new CellEditor4TableText());

		// updates
		setPanelUsageUpdateController();

		// --- getPanelLicencePools()
		columnNames = new ArrayList<>();
		columnNames.add("licensePoolId");
		columnNames.add("description");
		MapTableUpdateItemFactory updateItemFactoryLicencepools = new MapTableUpdateItemFactory(modelLicencepools,
				columnNames, 0);
		modelLicencepools = new GenTableModel(updateItemFactoryLicencepools, mainController.licencePoolTableProvider, 0,
				thePanel.getPanelLicencePools(), updateCollection);
		updateItemFactoryLicencepools.setSource(modelLicencepools);

		tableModels.add(modelLicencepools);
		tablePanes.add(thePanel.getPanelLicencePools());

		modelLicencepools.reset();
		thePanel.getPanelLicencePools().setTableModel(modelLicencepools);
	}

	private void setPanelUsageUpdateController() {
		thePanel.getPanelUsage().setUpdateController(
				new MapItemsUpdateController(thePanel.getPanelUsage(), modelLicencesUsage, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persistenceController.editLicenceUsage(
								(String) rowmap.get(LicenceUsageEntry.CLIENT_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_POOL_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_KEY_KEY),
								(String) rowmap.get(LicenceUsageEntry.NOTES_KEY));

					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencesUsage.requestReload();
						return persistenceController.deleteLicenceUsage(
								(String) rowmap.get(LicenceUsageEntry.CLIENT_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_POOL_ID_KEY));
					}
				}, updateCollection));
	}
}
