package de.uib.configed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;

import de.uib.configed.gui.licences.PanelLicencesUsage;
import de.uib.configed.type.licences.LicenceUsageEntry;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlPanelLicencesUsage extends ControlMultiTablePanel {
	PanelLicencesUsage thePanel;

	GenTableModel modelLicencesUsage;
	GenTableModel modelLicencekeys;
	GenTableModel modelWindowsSoftwareIds;
	GenTableModel modelLicencepools;

	PersistenceController persist;
	ConfigedMain mainController;

	public ControlPanelLicencesUsage(PersistenceController persist, ConfigedMain mainController) {
		thePanel = new PanelLicencesUsage(this);
		this.persist = persist;
		this.mainController = mainController;
		init();
	}

	public String getSoftwareLicenceReservation(String clientId) {
		if (clientId == null || clientId.equals("")) {
			JOptionPane.showMessageDialog(mainController.licencesFrame,
					configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectClient"),
					configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		List<String> selectedLPoolIds = thePanel.panelLicencepools.getSelectedKeys();

		if (selectedLPoolIds == null || selectedLPoolIds.size() != 1) {
			JOptionPane.showMessageDialog(mainController.licencesFrame,
					configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectOneLicencepool"),
					configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

			return "";
		}

		String licencePoolId = selectedLPoolIds.iterator().next();
		String result = persist.getLicenceUsage(clientId, licencePoolId);

		if (result != null) {
			thePanel.panelUsage.reload();
			//thePanel.panelUsage.moveToValue(clientId, 0, true);
			thePanel.panelUsage.moveToKeyValue(result);
		} else {
			thePanel.panelUsage.moveToValue(clientId, 0, true);
		}

		return result;

	}

	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	/*
	protected Vector getChoicesAllHosts()
	{
		TreeSet set = new TreeSet();
		set.add("");
		set.addAll(new TreeMap( 
				persist.getHostInfoCollections().getPcListForDepots( mainController.getSelectedDepots() ) 
				).keySet());
		return new Vector(set); 
	}
	*/

	@Override
	public void initializeVisualSettings() {
		thePanel.setDivider();
	}

	public void init() {
		updateCollection = new TableUpdateCollection();

		Vector<String> columnNames;
		Vector<String> classNames;

		//--- panelLicencesUsage
		columnNames = new Vector<>();
		columnNames.add(LicenceUsageEntry.clientIdKEY);
		columnNames.add(LicenceUsageEntry.licenceIdKEY);
		columnNames.add(LicenceUsageEntry.licencepoolIdKEY);
		columnNames.add(LicenceUsageEntry.licencekeyKEY);
		columnNames.add(LicenceUsageEntry.notesKEY);
		classNames = new Vector<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencesUsage = new MapTableUpdateItemFactory(modelLicencesUsage,
				columnNames, classNames, 0);
		modelLicencesUsage = new GenTableModel(updateItemFactoryLicencesUsage,
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					public Map retrieveMap() {
						persist.licencesUsageRequestRefresh();
						return persist.getLicencesUsage();
					}
				})), -1, new int[] { 0, 1, 2 }, thePanel.panelUsage, updateCollection);
		updateItemFactoryLicencesUsage.setSource(modelLicencesUsage);

		tableModels.add(modelLicencesUsage);
		tablePanes.add(thePanel.panelUsage);

		modelLicencesUsage.reset();
		thePanel.panelUsage.setTableModel(modelLicencesUsage);
		modelLicencesUsage.setEditableColumns(new int[] { 3, 4 });
		thePanel.panelUsage.setEmphasizedColumns(new int[] { 3, 4 });

		// --- PopupMenu
		/*
		JMenuItemFormatted menuItemAddUsage = new JMenuItemFormatted("add Usage");//configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemAddUsage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
			}
		});
		
		thePanel.panelUsage.addPopupItem(menuItemAddUsage);
		
		
		JMenuItemFormatted menuItemDeleteRelationLicenceUsage = new JMenuItemFormatted("delete usage");//configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemDeleteRelationLicenceUsage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e)
			{
		
				int selRowsCount = thePanel.panelUsage.getSelectedRowCount();
				
				if (selRowsCount == 0)
				{
					JOptionPane.showMessageDialog( mainController.licencesFrame,
						  "keine Zeile ausgewählt", //configed.getResourceValue("ConfigedMain.Licences.noRowSelected"),
						  configed.getResourceValue("ConfigedMain.Licences.hint.title"),
						  JOptionPane.OK_OPTION);
					
					return;
				}
				else
				{
					modelLicencesUsage.deleteRow(thePanel.panelUsage.getSelectedRowInModelTerms());
				}
			}
		});
		
		thePanel.panelUsage.addPopupItem(menuItemDeleteRelationLicenceUsage);
		*/

		// special treatment of columns
		javax.swing.table.TableColumn col;
		col = thePanel.panelUsage.getColumnModel().getColumn(4);
		col.setCellEditor(new de.uib.utilities.table.gui.CellEditor4TableText());

		//updates
		thePanel.panelUsage.setUpdateController(
				new MapItemsUpdateController(thePanel.panelUsage, modelLicencesUsage, new MapBasedUpdater() {
					public String sendUpdate(Map<String, Object> rowmap) {
						return persist.editLicenceUsage((String) rowmap.get(LicenceUsageEntry.clientIdKEY), //"hostId"),
								(String) rowmap.get(LicenceUsageEntry.licenceIdKEY), //"softwareLicenseId"),
								(String) rowmap.get(LicenceUsageEntry.licencepoolIdKEY), //"licensePoolId"),
								(String) rowmap.get(LicenceUsageEntry.licencekeyKEY), //"licenseKey"),
								(String) rowmap.get(LicenceUsageEntry.notesKEY) //"notes")
						);

					}

					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencesUsage.requestReload();
						return persist.deleteLicenceUsage((String) rowmap.get(LicenceUsageEntry.clientIdKEY), //""hostId"),
								(String) rowmap.get(LicenceUsageEntry.licenceIdKEY), //"softwareLicenseId"),
								(String) rowmap.get(LicenceUsageEntry.licencepoolIdKEY) //"licensePoolId")
						);
					}
				}, updateCollection));

		//--- panelLicencepools
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

		//combo clients
		thePanel.setClientsSource(new de.uib.utilities.ComboBoxModeller() {
			public ComboBoxModel<String> getComboBoxModel(int row, int column) {
				List<String> choicesAllHosts = new ArrayList<>(new TreeMap<>(persist.getHostInfoCollections()
						.getPcListForDepots(mainController.getSelectedDepots(), mainController.getAllowedClients()))
								.keySet());

				choicesAllHosts.set(0, "");

				logging.debug(this, "choicesAllHosts " + choicesAllHosts);

				return new DefaultComboBoxModel<>(choicesAllHosts.toArray(String[]::new));
			}
		});

	}
}
