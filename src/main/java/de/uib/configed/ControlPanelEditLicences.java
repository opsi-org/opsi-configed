package de.uib.configed;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.TableColumn;

import de.uib.configed.gui.licences.PanelEditLicences;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;
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

public class ControlPanelEditLicences extends AbstractControlMultiTablePanel {
	// tab edit licence

	private PanelEditLicences thePanel;

	private GenTableModel modelLicencekeys;
	private GenTableModel modelSoftwarelicences;
	private GenTableModel modelLicencecontracts;

	private AbstractPersistenceController persist;
	ConfigedMain mainController;

	public ControlPanelEditLicences(AbstractPersistenceController persist, ConfigedMain mainController) {

		// extending TabClientAdapter
		thePanel = new PanelEditLicences(this);
		this.persist = persist;
		this.mainController = mainController;
		init();
	}

	@Override
	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	@Override
	public final void init() {
		updateCollection = new ArrayList<TableEditItem>();

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
				Configed.getResourceValue("ConfigedMain.Licences.NewLicencekey"));
		menuItemAddKey.addActionListener((ActionEvent e) -> {
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
		TableColumn col;

		col = thePanel.panelKeys.getColumnModel().getColumn(1);
		JComboBox<String> comboLP0 = new JComboBox<>();
		comboLP0.setFont(Globals.defaultFontBig);

		col.setCellEditor(new AdaptingCellEditor(comboLP0, (int row, int column) -> {
			List<String> poolIds = mainController.licencePoolTableProvider.getOrderedColumn(
					mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1) {
				poolIds.add("");
			}
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		// updates
		thePanel.panelKeys.setUpdateController(
				new MapItemsUpdateController(thePanel.panelKeys, modelLicencekeys, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						Logging.info(this, "sendUpdate " + rowmap);

						return persist.editRelationSoftwareL2LPool((String) rowmap.get("softwareLicenseId"),
								(String) rowmap.get("licensePoolId"), (String) rowmap.get("licenseKey"));
					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						Logging.info(this, "sendDelete " + rowmap);
						modelLicencekeys.requestReload();
						return persist.deleteRelationSoftwareL2LPool((String) rowmap.get("softwareLicenseId"),
								(String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));

		// panelSoftwarelicences
		columnNames = new ArrayList<>();
		columnNames.add(LicenceEntry.ID_KEY);
		columnNames.add(LicenceEntry.LICENCE_CONTRACT_ID_KEY);
		columnNames.add(LicenceEntry.TYPE_KEY);
		columnNames.add(LicenceEntry.MAX_INSTALLATIONS_KEY);
		columnNames.add(LicenceEntry.BOUND_TO_HOST_KEY);
		columnNames.add(LicenceEntry.EXPIRATION_DATE_KEY);
		classNames = new ArrayList<>();
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

		col.setCellEditor(new AdaptingCellEditor(comboLP0, (int row, int column) -> {
			List<String> poolIds = mainController.licencePoolTableProvider.getOrderedColumn(
					mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

			if (poolIds.size() <= 1) {
				poolIds.add("");
			}
			// hack, since combo box shows nothing otherwise

			return new DefaultComboBoxModel<>(poolIds.toArray(String[]::new));
		}));

		col = thePanel.panelSoftwarelicences.getColumnModel().getColumn(5);
		col.setCellEditor(new CellEditor4TableText(new FEditDate("", false), FEditDate.AREA_DIMENSION));

		// expiration date

		// --- PopupMenu
		JMenuItemFormatted menuItemAddLicence = new JMenuItemFormatted(
				Configed.getResourceValue("ConfigedMain.Licences.NewSoftwarelicence"));
		menuItemAddLicence.addActionListener((ActionEvent e) -> {
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
				Configed.getResourceValue("ConfigedMain.Licences.MenuItemTransferIDFromSoftwarelicenceToLicencekey"));
		menuItemPickSoftwarelicence.addActionListener((ActionEvent e) -> {
			boolean keyNew = false;
			Iterator<TableEditItem> iter = updateCollection.iterator();
			while (iter.hasNext() && !keyNew) {
				TableEditItem update = iter.next();
				if (update.getSource() == modelSoftwarelicences && update.keyChanged()) {
					keyNew = true;
				}
			}
			if (keyNew) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						Configed.getResourceValue("ConfigedMain.Licences.PleaseSaveKeyRow"),
						Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			if (thePanel.panelSoftwarelicences.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						Configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
						Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			if (thePanel.panelKeys.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						Configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
						Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

				return;
			}

			String val = (String) modelSoftwarelicences
					.getValueAt(thePanel.panelSoftwarelicences.getSelectedRowInModelTerms(), 0);

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
				Configed.getResourceValue("ConfigedMain.Licences.NewLicencecontract"));
		menuItemAddContract.addActionListener((ActionEvent e) -> {
			Object[] a = new Object[6];
			a[0] = "c_" + Globals.getSeconds();
			a[1] = "";
			a[2] = Globals.getDate();
			a[3] = Globals.ZERODATE;
			a[4] = Globals.ZERODATE;
			a[5] = "";

			modelLicencecontracts.addRow(a);
			thePanel.panelLicencecontracts.moveToValue("" + a[0], 0);
		});

		thePanel.panelLicencecontracts.addPopupItem(menuItemAddContract);

		JMenuItemFormatted menuItemPickLicencecontract = new JMenuItemFormatted(Configed
				.getResourceValue("ConfigedMain.Licences.MenuItemTransferIDFromLicencecontractToSoftwarelicence"));
		menuItemPickLicencecontract.addActionListener((ActionEvent e) -> {
			boolean keyNew = false;
			Iterator<TableEditItem> iter = updateCollection.iterator();
			while (iter.hasNext() && !keyNew) {
				TableEditItem update = iter.next();
				if (update.getSource() == modelLicencecontracts && update.keyChanged()) {
					keyNew = true;
				}
			}
			if (keyNew) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						Configed.getResourceValue("ConfigedMain.Licences.PleaseSaveKeyRow"),
						Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			if (thePanel.panelLicencecontracts.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						Configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
						Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			if (thePanel.panelSoftwarelicences.getSelectedRow() == -1) {
				JOptionPane.showMessageDialog(mainController.licencesFrame,
						Configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
						Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
				return;
			}

			String val = (String) modelLicencecontracts
					.getValueAt(thePanel.panelLicencecontracts.getSelectedRowInModelTerms(), 0);

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
		CellEditor4TableText cellEditorLicenceContractNotes = new CellEditor4TableText(fedNotes,
				FEditPane.AREA_DIMENSION);

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
