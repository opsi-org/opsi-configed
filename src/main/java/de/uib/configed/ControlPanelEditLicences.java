package de.uib.configed;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import de.uib.configed.gui.licences.PanelEditLicences;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.FEditPane;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.tabbedpane.TabClientAdapter;
import de.uib.utilities.swing.timeedit.FEditDate;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.gui.AdaptingCellEditor;
import de.uib.utilities.table.gui.CellEditor4TableText;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableEditItem;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class ControlPanelEditLicences extends ControlMultiTablePanel
// tab edit licence
{

	PanelEditLicences thePanel;

	GenTableModel modelLicencekeys;
	GenTableModel modelSoftwarelicences;
	GenTableModel modelLicencecontracts;

	PersistenceController persist;
	ConfigedMain mainController;

	public ControlPanelEditLicences(PersistenceController persist, ConfigedMain mainController) {
		thePanel = new PanelEditLicences(this); // extending TabClientAdapter
		this.persist = persist;
		this.mainController = mainController;
		init();

	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public void init() {
		updateCollection = new TableUpdateCollection();

		Vector<String> columnNames;
		Vector<String> classNames;

		// panelKeys
		columnNames = new Vector<>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		classNames = new Vector<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencekeys = new MapTableUpdateItemFactory(modelLicencekeys,
				columnNames, classNames, 0);
		modelLicencekeys = new GenTableModel(updateItemFactoryLicencekeys, mainController.licenceOptionsTableProvider,
				-1, new int[] { 0, 1 }, thePanel.panelKeys, updateCollection);
		updateItemFactoryLicencekeys.setSource(modelLicencekeys);

		tableModels.add(modelLicencekeys);
		tablePanes.add(thePanel.panelKeys);

		modelLicencekeys.reset();
		thePanel.panelKeys.setTableModel(modelLicencekeys);
		modelLicencekeys.setEditableColumns(new int[] { 0, 1, 2 });
		thePanel.panelKeys.setEmphasizedColumns(new int[] { 2 });

		JMenuItemFormatted menuItemAddKey = new JMenuItemFormatted(
				configed.getResourceValue("ConfigedMain.Licences.NewLicencekey"));
		menuItemAddKey.addActionListener(e -> {
			Object[] a = new Object[3];
			a[0] = "";
			a[1] = "";
			a[2] = "";

			modelLicencekeys.addRow(a);
			thePanel.panelKeys.moveToLastRow();
			thePanel.panelKeys.moveToValue("" + a[0], 0);
		});

		thePanel.panelKeys.addPopupItem(menuItemAddKey);

		// special treatment of columns
		javax.swing.table.TableColumn col;

		col = thePanel.panelKeys.getColumnModel().getColumn(1);
		JComboBox<String> comboLP0 = new JComboBox<>();
		comboLP0.setFont(Globals.defaultFontBig);

		col.setCellEditor(new AdaptingCellEditor(comboLP0, (row, column) -> {
			List<String> poolIds = mainController.licencePoolTableProvider.getOrderedColumn(
					mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1)
				poolIds.add("");
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		// updates
		thePanel.panelKeys.setUpdateController(
				new MapItemsUpdateController(thePanel.panelKeys, modelLicencekeys, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						logging.info(this, "sendUpdate " + rowmap);

						return persist.editRelationSoftwareL2LPool((String) rowmap.get("softwareLicenseId"),
								(String) rowmap.get("licensePoolId"), (String) rowmap.get("licenseKey"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						logging.info(this, "sendDelete " + rowmap);
						modelLicencekeys.requestReload();
						return persist.deleteRelationSoftwareL2LPool((String) rowmap.get("softwareLicenseId"),
								(String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));

		// panelSoftwarelicences
		columnNames = new Vector<>();
		columnNames.add(LicenceEntry.idKEY);
		columnNames.add(LicenceEntry.licenceContractIdKEY);
		columnNames.add(LicenceEntry.typeKEY);
		columnNames.add(LicenceEntry.maxInstallationsKEY);
		columnNames.add(LicenceEntry.boundToHostKEY);
		columnNames.add(LicenceEntry.expirationDateKEY);
		classNames = new Vector<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactorySoftwarelicences = new MapTableUpdateItemFactory(
				modelSoftwarelicences, columnNames, classNames, 0);
		modelSoftwarelicences = new GenTableModel(updateItemFactorySoftwarelicences,
				mainController.softwarelicencesTableProvider, 0, thePanel.panelSoftwarelicences, updateCollection);
		updateItemFactorySoftwarelicences.setSource(modelSoftwarelicences);

		tableModels.add(modelSoftwarelicences);
		tablePanes.add(thePanel.panelSoftwarelicences);

		modelSoftwarelicences.reset();
		thePanel.panelSoftwarelicences.setTableModel(modelSoftwarelicences);
		modelSoftwarelicences.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });
		thePanel.panelSoftwarelicences.setEmphasizedColumns(new int[] { 1, 2, 3, 4, 5 });

		// --- special treatment of columns

		
		col = thePanel.panelSoftwarelicences.getColumnModel().getColumn(2);
		JComboBox<String> comboLicenceTypes = new JComboBox<>(LicenceEntry.LICENCE_TYPES);
		comboLicenceTypes.setFont(Globals.defaultFontBig);
		col.setCellEditor(new DefaultCellEditor(comboLicenceTypes));

		
		col = thePanel.panelSoftwarelicences.getColumnModel().getColumn(4);
		JComboBox<String> combo = new JComboBox<>();
		combo.setFont(Globals.defaultFontBig);

		col.setCellEditor(new AdaptingCellEditor(comboLP0, (row, column) -> {
			List<String> poolIds = mainController.licencePoolTableProvider.getOrderedColumn(
					mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1)
				poolIds.add("");
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		col = thePanel.panelSoftwarelicences.getColumnModel().getColumn(5);
		col.setCellEditor(new de.uib.utilities.table.gui.CellEditor4TableText(new FEditDate("", false),
				FEditDate.AREA_DIMENSION));

		// expiration date

		// --- PopupMenu
		JMenuItemFormatted menuItemAddLicence = new JMenuItemFormatted(
				configed.getResourceValue("ConfigedMain.Licences.NewSoftwarelicence"));
		menuItemAddLicence.addActionListener(e -> {
			Object[] a = new Object[6];
			a[0] = "l_" + Globals.getSeconds();
			a[1] = "";
			a[2] = LicenceEntry.LICENCE_TYPES[0];
			a[3] = "1";
			a[4] = "";
			a[5] = Globals.ZERODATE;

			modelSoftwarelicences.addRow(a);
			thePanel.panelSoftwarelicences.moveToValue("" + a[0], 0);
		});

		thePanel.panelSoftwarelicences.addPopupItem(menuItemAddLicence);

		JMenuItemFormatted menuItemPickSoftwarelicence = new JMenuItemFormatted(
				configed.getResourceValue("ConfigedMain.Licences.MenuItemTransferIDFromSoftwarelicenceToLicencekey"));
		menuItemPickSoftwarelicence.addActionListener(e -> {
			boolean keyNew = false;
			Iterator<TableEditItem> iter = updateCollection.iterator();
			while (iter.hasNext() && !keyNew) {
				TableEditItem update = iter.next();
				if (update.getSource() == modelSoftwarelicences && update.keyChanged())
					keyNew = true;
			}
			if (keyNew) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						configed.getResourceValue("ConfigedMain.Licences.PleaseSaveKeyRow"),
						configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			if (thePanel.panelSoftwarelicences.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
						configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			String val = (String) modelSoftwarelicences
					.getValueAt(thePanel.panelSoftwarelicences.getSelectedRowInModelTerms(), 0);

			if (thePanel.panelKeys.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
						configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

				return;
			}

			thePanel.panelKeys.setValueAt(val, thePanel.panelKeys.getSelectedRow(), 0);
		});

		thePanel.panelSoftwarelicences.addPopupItem(menuItemPickSoftwarelicence);

		thePanel.panelSoftwarelicences.setUpdateController(new MapItemsUpdateController(thePanel.panelSoftwarelicences,
				modelSoftwarelicences, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> m) {

						return persist.editSoftwareLicence((String) m.get("softwareLicenseId"),
								(String) m.get("licenseContractId"), (String) m.get("licenseType"),
								LicenceEntry.produceNormalizedCount("" + m.get("maxInstallations")),
								(String) m.get("boundToHost"), (String) m.get("expirationDate"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> m) {
						modelSoftwarelicences.requestReload();
						return persist.deleteSoftwareLicence((String) m.get("softwareLicenseId"));
					}
				}, updateCollection));

		// panelLicencecontracts
		columnNames = new Vector<>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");
		classNames = new Vector<>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencecontracts = new MapTableUpdateItemFactory(columnNames,
				classNames, 0);
		modelLicencecontracts = new GenTableModel(updateItemFactoryLicencecontracts,
				mainController.licenceContractsTableProvider, 0, thePanel.panelLicencecontracts, updateCollection);
		updateItemFactoryLicencecontracts.setSource(modelLicencecontracts);

		tableModels.add(modelLicencecontracts);
		tablePanes.add(thePanel.panelLicencecontracts);

		modelLicencecontracts.reset();
		thePanel.panelLicencecontracts.setTableModel(modelLicencecontracts);
		modelLicencecontracts.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });
		thePanel.panelLicencecontracts.setEmphasizedColumns(new int[] { 1, 2, 3, 4, 5 });

		// --- PopupMenu
		JMenuItemFormatted menuItemAddContract = new JMenuItemFormatted(
				configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
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

		JMenuItemFormatted menuItemPickLicencecontract = new JMenuItemFormatted(configed
				.getResourceValue("ConfigedMain.Licences.MenuItemTransferIDFromLicencecontractToSoftwarelicence"));
		menuItemPickLicencecontract.addActionListener(e -> {
			boolean keyNew = false;
			Iterator<TableEditItem> iter = updateCollection.iterator();
			while (iter.hasNext() && !keyNew) {
				TableEditItem update = iter.next();
				if (update.getSource() == modelLicencecontracts && update.keyChanged())
					keyNew = true;
			}
			if (keyNew) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						configed.getResourceValue("ConfigedMain.Licences.PleaseSaveKeyRow"),
						configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			if (thePanel.panelLicencecontracts.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
						configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			String val = (String) modelLicencecontracts
					.getValueAt(thePanel.panelLicencecontracts.getSelectedRowInModelTerms(), 0);
			if (thePanel.panelSoftwarelicences.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
						configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			thePanel.panelSoftwarelicences.setValueAt(val, thePanel.panelSoftwarelicences.getSelectedRow(), 1);
		});

		thePanel.panelLicencecontracts.addPopupItem(menuItemPickLicencecontract);

		// special treatment of columns

		// col 2
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
