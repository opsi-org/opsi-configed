package de.uib.configed;

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.table.*;
import javax.swing.event.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.swing.tabbedpane.*;
import de.uib.utilities.swing.timeedit.*;
import de.uib.utilities.table.*;
import de.uib.utilities.table.gui.*;
import de.uib.utilities.table.updates.*;
import de.uib.utilities.table.provider.*;
import de.uib.configed.gui.licences.*;
import de.uib.configed.type.licences.*;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.configed.Globals;
import de.uib.utilities.logging.*;

public class ControlPanelEditLicences extends ControlMultiTablePanel
// tab edit licence
{

	PanelEditLicences thePanel;
	TableUpdateCollection updateCollection;

	GenTableModel modelLicencekeys;
	// GenTableModel modelLicencepools;
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

	public TabClientAdapter getTabClient() {
		return thePanel;
	}

	public void init() {
		updateCollection = new TableUpdateCollection();

		Vector<String> columnNames;
		Vector<String> classNames;

		// panelKeys
		columnNames = new Vector<String>();
		columnNames.add("softwareLicenseId");
		columnNames.add("licensePoolId");
		columnNames.add("licenseKey");
		classNames = new Vector<String>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencekeys = new MapTableUpdateItemFactory(modelLicencekeys,
				columnNames, classNames, 0);
		modelLicencekeys = new GenTableModel(updateItemFactoryLicencekeys, mainController.licenceOptionsTableProvider,
				/*
				 * new DefaultTableProvider( new RetrieverMapSource(columnNames, classNames, new
				 * MapRetriever(){ public Map retrieveMap() { return
				 * persist.getRelationsSoftwareL2LPool(); } }) ),
				 */

				-1, new int[] { 0, 1 }, (TableModelListener) thePanel.panelKeys, updateCollection);
		updateItemFactoryLicencekeys.setSource(modelLicencekeys);

		tableModels.add(modelLicencekeys);
		tablePanes.add(thePanel.panelKeys);

		modelLicencekeys.reset();
		thePanel.panelKeys.setTableModel(modelLicencekeys);
		modelLicencekeys.setEditableColumns(new int[] { 0, 1, 2 });
		thePanel.panelKeys.setEmphasizedColumns(new int[] { 2 });

		JMenuItemFormatted menuItemAddKey = new JMenuItemFormatted(
				configed.getResourceValue("ConfigedMain.Licences.NewLicencekey"));
		menuItemAddKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] a = new Object[3];
				a[0] = "";
				a[1] = "";
				a[2] = "";

				modelLicencekeys.addRow(a);
				thePanel.panelKeys.moveToLastRow();
				thePanel.panelKeys.moveToValue("" + a[0], 0);
			}
		});

		thePanel.panelKeys.addPopupItem(menuItemAddKey);

		// special treatment of columns
		javax.swing.table.TableColumn col;

		col = thePanel.panelKeys.getColumnModel().getColumn(1);
		JComboBox comboLP0 = new JComboBox();
		comboLP0.setFont(Globals.defaultFontBig);
		// org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
		// combo.setRenderer ();
		// col.setCellEditor(new DefaultCellEditor(combo));
		col.setCellEditor(new AdaptingCellEditor(comboLP0, new de.uib.utilities.ComboBoxModeller() {
			public ComboBoxModel getComboBoxModel(int row, int column) {

				Vector poolIds = mainController.licencePoolTableProvider.getOrderedColumn(// 1,
						mainController.licencePoolTableProvider.getColumnNames().indexOf("licensePoolId"), false);

				// logging.debug(this, "retrieved poolIds: " + poolIds);

				if (poolIds.size() <= 1)
					poolIds.add("");
				// hack, since combo box shows nothing otherwise

				ComboBoxModel model = new DefaultComboBoxModel(poolIds);

				// logging.debug(this, "got comboboxmodel for poolIds, size " +
				// model.getSize());

				return model;
			}
		}));

		// updates
		thePanel.panelKeys.setUpdateController(
				new MapItemsUpdateController(thePanel.panelKeys, modelLicencekeys, new MapBasedUpdater() {
					public String sendUpdate(Map<String, Object> rowmap) {
						logging.info(this, "sendUpdate " + rowmap);

						return persist.editRelationSoftwareL2LPool((String) rowmap.get("softwareLicenseId"),
								(String) rowmap.get("licensePoolId"), (String) rowmap.get("licenseKey"));
					}

					public boolean sendDelete(Map<String, Object> rowmap) {
						logging.info(this, "sendDelete " + rowmap);
						modelLicencekeys.requestReload();
						return persist.deleteRelationSoftwareL2LPool((String) rowmap.get("softwareLicenseId"),
								(String) rowmap.get("licensePoolId"));
					}
				}, updateCollection));

		// panelLicencepools
		/*
		 * columnNames = new Vector<String>(); columnNames.add("licensePoolId");
		 * columnNames.add("description"); classNames = new Vector<String>();
		 * classNames.add("java.lang.String"); classNames.add("java.lang.String");
		 * MapTableUpdateItemFactory updateItemFactoryLicencepools = new
		 * MapTableUpdateItemFactory(modelLicencepools, columnNames, classNames, 0);
		 * modelLicencepools = new GenTableModel( updateItemFactoryLicencepools,
		 * mainController.licencePoolTableProvider,
		 * 
		 * 0, (TableModelListener) thePanel.panelLicencepools, updateCollection);
		 * updateItemFactoryLicencepools.setSource(modelLicencepools);
		 * 
		 * tableModels.add(modelLicencepools);
		 * tablePanes.add(thePanel.panelLicencepools);
		 * 
		 * modelLicencepools.reset();
		 * thePanel.panelLicencepools.setTableModel(modelLicencepools);
		 * modelLicencepools.setEditableColumns(new int[]{0,1});
		 * //thePanel.panelLicencepools.setEmphasizedColumns(new int[]{0,1});
		 * 
		 * JMenuItemFormatted menuItemAddPool = new
		 * JMenuItemFormatted(configed.getResourceValue(
		 * "ConfigedMain.Licences.NewLicencepool"));
		 * menuItemAddPool.addActionListener(new ActionListener(){ public void
		 * actionPerformed(ActionEvent e) { Object[] a = new Object[2]; a[0] = ""; a[1]
		 * = "";
		 * 
		 * modelLicencepools.addRow(a); thePanel.panelLicencepools.moveToLastRow(); }
		 * });
		 * 
		 * thePanel.panelLicencepools.addPopupItem(menuItemAddPool);
		 * 
		 * JMenuItemFormatted menuItemPickLicencepool = new
		 * JMenuItemFormatted(configed.getResourceValue(
		 * "ConfigedMain.Licences.MenuItemTransferIDFromLicencepoolToLicencekey"));
		 * menuItemPickLicencepool.addActionListener(new ActionListener(){ public void
		 * actionPerformed(ActionEvent e) { boolean keyNew = false; Iterator iter =
		 * updateCollection.iterator(); while (iter.hasNext() && !keyNew) {
		 * TableEditItem update = (TableEditItem) iter.next(); if ( update.getSource()
		 * == modelLicencepools && update.keyChanged() ) keyNew = true; } if (keyNew) {
		 * JOptionPane.showMessageDialog(mainController.licencesFrame,
		 * configed.getResourceValue("ConfigedMain.Licences.PleaseSaveKeyRow"),
		 * configed.getResourceValue("ConfigedMain.Licences.hint.title"),
		 * JOptionPane.OK_OPTION); return; }
		 * 
		 * 
		 * if (thePanel.panelLicencepools.getSelectedRow() == -1) {
		 * JOptionPane.showMessageDialog(mainController.licencesFrame,
		 * configed.getResourceValue(
		 * "ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
		 * configed.getResourceValue("ConfigedMain.Licences.hint.title"),
		 * JOptionPane.OK_OPTION); return; }
		 * 
		 * String val = (String) modelLicencepools.getValueAt(
		 * thePanel.panelLicencepools.getSelectedRowInModelTerms(), 0 );
		 * //System.out.println(" ------- found value: " + val);
		 * 
		 * if (thePanel.panelKeys.getSelectedRow() == -1) {
		 * JOptionPane.showMessageDialog(mainController.licencesFrame,
		 * configed.getResourceValue(
		 * "ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
		 * configed.getResourceValue("ConfigedMain.Licences.hint.title"),
		 * JOptionPane.OK_OPTION);
		 * 
		 * return; }
		 * 
		 * thePanel.panelKeys.setValueAt(val, thePanel.panelKeys.getSelectedRow(), 1); }
		 * });
		 * 
		 * thePanel.panelLicencepools.addPopupItem(menuItemPickLicencepool);
		 * 
		 * 
		 * // special treatment of columns
		 * 
		 * 
		 * col=thePanel.panelLicencepools.getColumnModel().getColumn(0); JComboBox
		 * comboLP0 = new JComboBox(); comboLP0.setFont(Globals.defaultFontBig);
		 * //org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
		 * //combo.setRenderer (); //col.setCellEditor(new DefaultCellEditor(combo));
		 * col.setCellEditor( new de.uib.utilities.swing.AdaptingCellEditor( comboLP0,
		 * new de.uib.utilities.ComboBoxModeller(){ public ComboBoxModel
		 * getComboBoxModel(int row, int column){ return new DefaultComboBoxModel( //new
		 * String[]{"a","b"});// modelLicencepools.getOrderedColumn(0) ); } } ) );
		 * 
		 * 
		 * //updates thePanel.panelLicencepools.setUpdateController( new
		 * MapItemsUpdateController( thePanel.panelLicencepools, modelLicencepools, new
		 * MapBasedUpdater(){ public String sendUpdate(Map<String, Object> rowmap){
		 * return persist.editLicencePool( (String) rowmap.get("licensePoolId"),
		 * (String) rowmap.get("description") ); } public boolean sendDelete(Map<String,
		 * Object> rowmap){ return persist.deleteLicencePool( (String)
		 * rowmap.get("licensePoolId") ); } }, updateCollection ) );
		 */

		// panelSoftwarelicences
		columnNames = new Vector<String>();
		columnNames.add(LicenceEntry.idKEY);
		columnNames.add(LicenceEntry.licenceContractIdKEY);
		columnNames.add(LicenceEntry.typeKEY);
		columnNames.add(LicenceEntry.maxInstallationsKEY);
		columnNames.add(LicenceEntry.boundToHostKEY);
		columnNames.add(LicenceEntry.expirationDateKEY);
		classNames = new Vector<String>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactorySoftwarelicences = new MapTableUpdateItemFactory(
				modelSoftwarelicences, columnNames, classNames, 0);
		modelSoftwarelicences = new GenTableModel(updateItemFactorySoftwarelicences,
				mainController.softwarelicencesTableProvider,
				/*
				 * new DefaultTableProvider( new RetrieverMapSource(columnNames, classNames, new
				 * MapRetriever(){ public Map retrieveMap() { return
				 * persist.getSoftwareLicences(); } }) ),
				 */

				0, (TableModelListener) thePanel.panelSoftwarelicences, updateCollection);
		updateItemFactorySoftwarelicences.setSource(modelSoftwarelicences);

		tableModels.add(modelSoftwarelicences);
		tablePanes.add(thePanel.panelSoftwarelicences);

		modelSoftwarelicences.reset();
		thePanel.panelSoftwarelicences.setTableModel(modelSoftwarelicences);
		modelSoftwarelicences.setEditableColumns(new int[] { 0, 1, 2, 3, 4, 5 });
		thePanel.panelSoftwarelicences.setEmphasizedColumns(new int[] { 1, 2, 3, 4, 5 });

		// --- special treatment of columns

		// "license type"
		col = thePanel.panelSoftwarelicences.getColumnModel().getColumn(2);
		JComboBox comboLicenceTypes = new JComboBox(LicenceEntry.LICENCE_TYPES);
		comboLicenceTypes.setFont(Globals.defaultFontBig);
		col.setCellEditor(new DefaultCellEditor(comboLicenceTypes));

		// "bound to host"
		col = thePanel.panelSoftwarelicences.getColumnModel().getColumn(4);
		JComboBox combo = new JComboBox();
		combo.setFont(Globals.defaultFontBig);
		// org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
		// combo.setRenderer ();
		// col.setCellEditor(new DefaultCellEditor(combo));
		col.setCellEditor(new AdaptingCellEditor(combo, new de.uib.utilities.ComboBoxModeller() {
			public ComboBoxModel getComboBoxModel(int row, int column) {
				Vector choicesAllHosts = new Vector(new TreeMap(persist.getHostInfoCollections()
						.getPcListForDepots(mainController.getSelectedDepots(), mainController.getAllowedClients()))
								.keySet());
				choicesAllHosts.insertElementAt("", 0);
				return new DefaultComboBoxModel(choicesAllHosts);
			}
		}));

		col = thePanel.panelSoftwarelicences.getColumnModel().getColumn(5);
		col.setCellEditor(new de.uib.utilities.table.gui.CellEditor4TableText(new FEditDate("", false),
				FEditDate.AREA_DIMENSION));

		// expiration date
		// col=thePanel.panelSoftwarelicences.getColumnModel().getColumn(5);

		// --- PopupMenu
		JMenuItemFormatted menuItemAddLicence = new JMenuItemFormatted(
				configed.getResourceValue("ConfigedMain.Licences.NewSoftwarelicence"));
		menuItemAddLicence.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] a = new Object[6];
				a[0] = "l_" + Globals.getSeconds();
				a[1] = "";// "lc_";
				a[2] = LicenceEntry.LICENCE_TYPES[0];
				a[3] = "1";
				a[4] = "";
				a[5] = Globals.ZERODATE;

				modelSoftwarelicences.addRow(a);
				// thePanel.panelSoftwarelicences.moveToLastRow();
				thePanel.panelSoftwarelicences.moveToValue("" + a[0], 0);

				// JOptionPane.showMessageDialog(null,
			}
		});

		thePanel.panelSoftwarelicences.addPopupItem(menuItemAddLicence);

		JMenuItemFormatted menuItemPickSoftwarelicence = new JMenuItemFormatted(
				configed.getResourceValue("ConfigedMain.Licences.MenuItemTransferIDFromSoftwarelicenceToLicencekey"));
		menuItemPickSoftwarelicence.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean keyNew = false;
				Iterator iter = updateCollection.iterator();
				while (iter.hasNext() && !keyNew) {
					TableEditItem update = (TableEditItem) iter.next();
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
				// System.out.println(" ------- found value: " + val);

				if (thePanel.panelKeys.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(mainController.licencesFrame,
							configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
							configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);

					return;
				}

				thePanel.panelKeys.setValueAt(val, thePanel.panelKeys.getSelectedRow(), 0);
			}
		});

		thePanel.panelSoftwarelicences.addPopupItem(menuItemPickSoftwarelicence);

		thePanel.panelSoftwarelicences.setUpdateController(new MapItemsUpdateController(thePanel.panelSoftwarelicences,
				modelSoftwarelicences, new MapBasedUpdater() {
					public String sendUpdate(Map<String, Object> m) {

						return persist.editSoftwareLicence((String) m.get("softwareLicenseId"),
								(String) m.get("licenseContractId"), (String) m.get("licenseType"),
								LicenceEntry.produceNormalizedCount("" + m.get("maxInstallations")),
								(String) m.get("boundToHost"), (String) m.get("expirationDate"));
					}

					public boolean sendDelete(Map<String, Object> m) {
						modelSoftwarelicences.requestReload();
						return persist.deleteSoftwareLicence((String) m.get("softwareLicenseId"));
					}
				}, updateCollection));

		// panelLicencecontracts
		columnNames = new Vector<String>();
		columnNames.add("licenseContractId");
		columnNames.add("partner");
		columnNames.add("conclusionDate");
		columnNames.add("notificationDate");
		columnNames.add("expirationDate");
		columnNames.add("notes");
		classNames = new Vector<String>();
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		classNames.add("java.lang.String");
		MapTableUpdateItemFactory updateItemFactoryLicencecontracts = new MapTableUpdateItemFactory(columnNames,
				classNames, 0);
		modelLicencecontracts = new GenTableModel(updateItemFactoryLicencecontracts,
				mainController.licenceContractsTableProvider,
				/*
				 * new DefaultTableProvider( new RetrieverMapSource(columnNames, classNames, new
				 * MapRetriever(){ public Map retrieveMap() { return
				 * persist.getLicenceContracts(); } }) ),
				 */

				0, (TableModelListener) thePanel.panelLicencecontracts, updateCollection);
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
		menuItemAddContract.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] a = new Object[6];
				a[0] = "c_" + Globals.getSeconds();
				a[1] = "";
				a[2] = Globals.getDate(false);
				a[3] = Globals.ZERODATE;
				a[4] = Globals.ZERODATE;
				a[5] = "";

				modelLicencecontracts.addRow(a);
				// thePanel.panelLicencecontracts.moveToLastRow();
				thePanel.panelLicencecontracts.moveToValue("" + a[0], 0);
			}
		});

		thePanel.panelLicencecontracts.addPopupItem(menuItemAddContract);

		JMenuItemFormatted menuItemPickLicencecontract = new JMenuItemFormatted(configed
				.getResourceValue("ConfigedMain.Licences.MenuItemTransferIDFromLicencecontractToSoftwarelicence"));
		menuItemPickLicencecontract.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean keyNew = false;
				Iterator iter = updateCollection.iterator();
				while (iter.hasNext() && !keyNew) {
					TableEditItem update = (TableEditItem) iter.next();
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
				// System.out.println(" ------- found value: " + val);
				if (thePanel.panelSoftwarelicences.getSelectedRow() == -1) {
					JOptionPane.showMessageDialog(mainController.licencesFrame,
							configed.getResourceValue("ConfigedMain.Licences.SourceOrTargetRowNotSelected.text"),
							configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
					return;
				}

				thePanel.panelSoftwarelicences.setValueAt(val, thePanel.panelSoftwarelicences.getSelectedRow(), 1);
			}
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
		/*
		 * {
		 * 
		 * @Override protected void commit() { super.commit(); logging.info(this,
		 * "commit"); }
		 * 
		 * @Override protected void cancel() { super.cancel(); logging.info(this,
		 * "cancel"); } }
		 */
		;

		CellEditor4TableText cellEditorLicenceContractNotes = new de.uib.utilities.table.gui.CellEditor4TableText(
				fedNotes, FEditPane.AREA_DIMENSION)
		/*
		 * {
		 * 
		 * @Override protected void fireEditingCanceled() { super.fireEditingCanceled();
		 * logging.info(this, "fireEditingCanceled"); }
		 * 
		 * @Override protected void fireEditingStopped() { super.fireEditingStopped();
		 * logging.info(this, "fireEditingStopped"); }
		 * 
		 * }
		 */

		;
		fedNotes.setServedCellEditor(cellEditorLicenceContractNotes);
		col.setCellEditor(cellEditorLicenceContractNotes);

		// updates

		thePanel.panelLicencecontracts.setUpdateController(new MapItemsUpdateController(thePanel.panelLicencecontracts,
				modelLicencecontracts, new MapBasedUpdater() {
					public String sendUpdate(Map<String, Object> rowmap) {
						return persist.editLicenceContract((String) rowmap.get("licenseContractId"),
								(String) rowmap.get("partner"), (String) rowmap.get("conclusionDate"),
								(String) rowmap.get("notificationDate"), (String) rowmap.get("expirationDate"),
								(String) rowmap.get("notes"));
					}

					public boolean sendDelete(Map<String, Object> rowmap) {
						modelLicencecontracts.requestReload();
						return persist.deleteLicenceContract((String) rowmap.get("licenseContractId"));
					}
				}, updateCollection));

	}
}
