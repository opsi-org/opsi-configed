package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import de.uib.configed.gui.licences.PanelLicencesUsage;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlPanelLicencesUsage extends AbstractControlMultiTablePanel {
	PanelLicencesUsage thePanel;

	GenTableModel modelLicencesUsage;
	GenTableModel modelLicencekeys;
	GenTableModel modelWindowsSoftwareIds;
	GenTableModel modelLicencepools;

	AbstractPersistenceController persist;
	ConfigedMain mainController;

	public ControlPanelLicencesUsage(AbstractPersistenceController persist, ConfigedMain mainController) {
		thePanel = new PanelLicencesUsage(this);
		this.persist = persist;
		this.mainController = mainController;

		init();
	}

	public String getSoftwareLicenceReservation(String clientId) {
		if (clientId == null || clientId.equals("")) {
			JOptionPane.showMessageDialog(mainController.licencesFrame,
					Configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectClient"),
					Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		List<String> selectedLPoolIds = thePanel.panelLicencepools.getSelectedKeys();

		if (selectedLPoolIds == null || selectedLPoolIds.size() != 1) {
			JOptionPane.showMessageDialog(mainController.licencesFrame,
					Configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectOneLicencepool"),
					Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		String licencePoolId = selectedLPoolIds.iterator().next();
		String result = persist.getLicenceUsage(clientId, licencePoolId);

		if (result != null) {
			thePanel.panelUsage.reload();

			thePanel.panelUsage.moveToKeyValue(result);
		} else {
			thePanel.panelUsage.moveToValue(clientId, 0, true);
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
		updateCollection = new TableUpdateCollection();

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
				columnNames, classNames, 0);
		modelLicencesUsage = new GenTableModel(updateItemFactoryLicencesUsage,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, () -> {
					persist.licencesUsageRequestRefresh();
					return (Map) persist.getLicencesUsage();
				})), -1, new int[] { 0, 1, 2 }, thePanel.panelUsage, updateCollection);
		updateItemFactoryLicencesUsage.setSource(modelLicencesUsage);

		tableModels.add(modelLicencesUsage);
		tablePanes.add(thePanel.panelUsage);

		modelLicencesUsage.reset();
		thePanel.panelUsage.setTableModel(modelLicencesUsage);
		modelLicencesUsage.setEditableColumns(new int[] { 3, 4 });
		thePanel.panelUsage.setEmphasizedColumns(new int[] { 3, 4 });

		// special treatment of columns
		javax.swing.table.TableColumn col;
		col = thePanel.panelUsage.getColumnModel().getColumn(4);
		col.setCellEditor(new de.uib.utilities.table.gui.CellEditor4TableText());

		// updates
		thePanel.panelUsage.setUpdateController(
				new MapItemsUpdateController(thePanel.panelUsage, modelLicencesUsage, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						return persist.editLicenceUsage((String) rowmap.get(LicenceUsageEntry.CLIENT_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_POOL_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_KEY_KEY),
								(String) rowmap.get(LicenceUsageEntry.NOTES_KEY));

					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencesUsage.requestReload();
						return persist.deleteLicenceUsage((String) rowmap.get(LicenceUsageEntry.CLIENT_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_ID_KEY),
								(String) rowmap.get(LicenceUsageEntry.LICENCE_POOL_ID_KEY));
					}
				}, updateCollection));

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

		// combo clients
		thePanel.setClientsSource(new de.uib.utilities.ComboBoxModeller() {
			@Override
			public ComboBoxModel<String> getComboBoxModel(int row, int column) {
				List<String> choicesAllHosts = new ArrayList<>(persist.getHostInfoCollections()
						.getClientListForDepots(mainController.getSelectedDepots(), mainController.getAllowedClients())
						.keySet());

				choicesAllHosts.set(0, "");

				Logging.debug(this, "choicesAllHosts " + choicesAllHosts);

				return new DefaultComboBoxModel<>(choicesAllHosts.toArray(String[]::new));
			}
		});

	}
}
