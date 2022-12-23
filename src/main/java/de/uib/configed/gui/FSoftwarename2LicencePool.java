package de.uib.configed.gui;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelListener;

/**
 * FSoftwarename2LicencePool
 * Copyright:     Copyright (c) 2020
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.SWAuditEntry;
import de.uib.configed.type.licences.AuditSoftwareXLicencePool;
import de.uib.configed.type.licences.LicencepoolEntry;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.DefaultTableModelFilterCondition;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilterCondition;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;
import de.uib.utilities.table.updates.MapBasedUpdater;
import de.uib.utilities.table.updates.MapItemsUpdateController;
import de.uib.utilities.table.updates.MapTableUpdateItemFactory;
import de.uib.utilities.table.updates.TableUpdateCollection;

public class FSoftwarename2LicencePool extends FDialogSubTable {
	public static final String valNoLicencepool = "---";
	public PanelGenEditTable panelSWnames;
	public GenTableModel modelSWnames;

	private Vector<String> columnNames;
	private Vector<String> classNames;

	public PanelGenEditTable panelSWxLicencepool;
	public GenTableModel modelSWxLicencepool;
	private Vector<String> columnNamesSWxLicencepool;
	private Vector<String> classNamesSWxLicencepool;

	private TableModelFilterCondition showOnlyNamesWithVariantLicences;
	private TableModelFilterCondition showOnlyNamesWithoutLicences;

	private TableUpdateCollection updateCollection;

	protected int keyCol = 0;

	PersistenceController persist;

	private ControlPanelAssignToLPools myController;

	public enum Softwarename2LicencepoolRestriction {
		SHOW_ALL_NAMES, SHOW_ONLY_NAMES_WITH_VARIANT_LICENCEPOOLS, SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENCEPOOL
	};

	public enum Softwarename2LicencepoolChangeOption {
		NO_CHANGE, REMOVE_ALL_ASSIGNEMENTS, SET_ALL_TO_GLOBAL_SELECTED_LICENCEPOOL, SET_ALL_TO_SELECTED_LINE
	}

	JButton buttonRemoveAllAssignments;
	JLabel labelRemoveAllAssignments;
	JButton buttonSetAllAssignmentsToGloballySelectedPool;
	JLabel labelSetAllAssignmentsToGloballySelectedPool;
	JButton buttonSetAllAssignmentsToPoolFromSelectedRow;
	JLabel labelSetAllAssignmentsToPoolFromSelectedRow;

	protected String globalLicencePool;

	boolean foundVariantLicencepools = false;

	public FSoftwarename2LicencePool(JFrame owner, ControlPanelAssignToLPools myController) {
		super(
				// Globals.mainFrame,
				owner, configed.getResourceValue("FSoftwarename2LicencePool.title"), false, new String[] {
						// "save",
						// "cancel",
						configed.getResourceValue("FSoftwarename2LicencePool.buttonClose") },
				new Icon[] {
						// "save",
						// "cancel",
						Globals.createImageIcon("images/cancel16_small.png", "") },
				1, 700, 800);

		this.myController = myController;
		persist = PersistenceControllerFactory.getPersistenceController();

		panelSWnames = new PanelGenEditTable("", // "software assigned, but not existing",
				// configed.getResourceValue("ConfigedMain.LicenctiontitleWindowsSoftware2LPool"),
				0, false, // editing,
				0, true // switchLineColors
				, new int[] { PanelGenEditTable.POPUP_RELOAD },
				// , PanelGenEditTable.POPUPS_NOT_EDITABLE_TABLE_PDF,
				true // searchpane
		) {
			@Override
			public void setDataChanged(boolean b) {
				logging.info(this, "panelSWNames setDataChanged " + b);
				super.setDataChanged(b);
			}
		};

		panelSWxLicencepool = new PanelGenEditTable("", // "software assigned, but not existing",
				// configed.getResourceValue("ConfigedMain.LicenctiontitleWindowsSoftware2LPool"),
				0, // width
				true, // editing,
				0, true // switchLineColors
				, new int[] { PanelGenEditTable.POPUP_RELOAD },
				// PanelGenEditTable.POPUP_SAVE, PanelGenEditTable.POPUP_CANCEL },
				// PanelGenEditTable.POPUPS_NOT_EDITABLE_TABLE_PDF,
				false // searchpane
		) {
			@Override
			public void setDataChanged(boolean b) {
				logging.info(this, "panelSWxLicencepool setDataChanged " + b);
				super.setDataChanged(b);
			}

			@Override
			public void valueChanged(ListSelectionEvent e) {
				logging.info(this, "panelSWxLicencepool ListSelectionEvent " + e);
				super.valueChanged(e);

				String labelText = configed
						.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToPoolFromSelectedRow");

				Object val = null;
				int selRow = getSelectedRow();
				if (selRow > -1)
					val = getValueAt(selRow, 1);

				if (val != null && isSingleSelection() && getTableModel().getRowCount() > 1
						&& !((String) val).equals(valNoLicencepool)) {
					buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(true);
					labelSetAllAssignmentsToPoolFromSelectedRow
							.setText(labelText + " " + getValueAt(getSelectedRow(), 1));
				} else {
					buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
					labelSetAllAssignmentsToPoolFromSelectedRow.setText(labelText);
					configed.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToPoolFromSelectedRow");
				}

			}
		};

		panelSWxLicencepool.setDeleteAllowed(false);

		owner.setVisible(true);

		/*
		 * jButton1 = new de.uib.configed.gui.IconButton (
		 * de.uib.configed.configed.getResourceValue(
		 * "PanelGenEditTable.SaveButtonTooltip") ,
		 * "images/apply.png",
		 * "images/apply_over.png",
		 * "images/apply_disabled.png");
		 * 
		 * jButton2 = new de.uib.configed.gui.IconButton(
		 * de.uib.configed.configed.getResourceValue(
		 * "PanelGenEditTable.CancelButtonTooltip") ,
		 * "images/cancel.png",
		 * "images/cancel_over.png",
		 * "images/cancel_disabled.png");
		 */

		initDataStructure();
		// setTableModel(null);

		buttonRemoveAllAssignments = new JButton();
		buttonRemoveAllAssignments.setIcon(Globals.createImageIcon("images/list-remove-14.png", ""));
		buttonRemoveAllAssignments.setPreferredSize(Globals.shortButtonDimension);
		labelRemoveAllAssignments = new JLabel(
				configed.getResourceValue("FSoftwarename2LicencePool.labelRemoveAllAssignments"));
		buttonRemoveAllAssignments
				.addActionListener(actionEvent -> panelSWxLicencepool.setDataChanged(setSWxColTo(valNoLicencepool)));

		buttonSetAllAssignmentsToGloballySelectedPool = new JButton();
		buttonSetAllAssignmentsToGloballySelectedPool.setEnabled(false);
		buttonSetAllAssignmentsToGloballySelectedPool.setPreferredSize(Globals.shortButtonDimension);
		buttonSetAllAssignmentsToGloballySelectedPool.setIcon(Globals.createImageIcon("images/list-add-14.png", ""));
		labelSetAllAssignmentsToGloballySelectedPool = new JLabel(
				configed.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToGloballySelectedPool"));
		buttonSetAllAssignmentsToGloballySelectedPool
				.addActionListener(actionEvent -> panelSWxLicencepool.setDataChanged(setSWxColTo(globalLicencePool)));

		buttonSetAllAssignmentsToPoolFromSelectedRow = new JButton();
		buttonSetAllAssignmentsToPoolFromSelectedRow.setEnabled(false);
		buttonSetAllAssignmentsToPoolFromSelectedRow.setPreferredSize(Globals.shortButtonDimension);
		buttonSetAllAssignmentsToPoolFromSelectedRow.setIcon(Globals.createImageIcon("images/list-add-14.png", ""));
		labelSetAllAssignmentsToPoolFromSelectedRow = new JLabel(
				configed.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToPoolFromSelectedRow")); // assign
																																												// each
																																												// to
																																												// pool
																																												// from
																																												// selected
																																												// row:
																																												// ")
																																												// ;
		buttonSetAllAssignmentsToPoolFromSelectedRow
				.addActionListener(actionEvent -> panelSWxLicencepool.setDataChanged(
						setSWxColTo((String) panelSWxLicencepool.getValueAt(panelSWxLicencepool.getSelectedRow(), 1))));

		JPanel panelAction = new JPanel();
		panelAction.setBackground(Globals.backgroundWhite);

		GroupLayout panelActionLayout = new GroupLayout(panelAction);
		panelAction.setLayout(panelActionLayout);
		panelActionLayout.setVerticalGroup(panelActionLayout.createSequentialGroup()
				.addGroup(panelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(buttonRemoveAllAssignments, 5, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelRemoveAllAssignments, 5, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(panelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(buttonSetAllAssignmentsToGloballySelectedPool, 5, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSetAllAssignmentsToGloballySelectedPool, 5, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(panelActionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(buttonSetAllAssignmentsToPoolFromSelectedRow, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelSetAllAssignmentsToPoolFromSelectedRow, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		panelActionLayout
				.setHorizontalGroup(
						panelActionLayout.createParallelGroup()
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
										.addComponent(buttonRemoveAllAssignments, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.HGAP_SIZE)
										.addComponent(
												labelRemoveAllAssignments, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.HGAP_SIZE))
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
										.addComponent(buttonSetAllAssignmentsToGloballySelectedPool,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.HGAP_SIZE)
										.addComponent(labelSetAllAssignmentsToGloballySelectedPool,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.HGAP_SIZE))
								.addGroup(panelActionLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
										.addComponent(buttonSetAllAssignmentsToPoolFromSelectedRow,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.HGAP_SIZE)
										.addComponent(labelSetAllAssignmentsToPoolFromSelectedRow,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.HGAP_SIZE)));

		JPanel panelSWx = new JPanel();
		GroupLayout layoutSWx = new GroupLayout(panelSWx);
		panelSWx.setLayout(layoutSWx);

		layoutSWx.setVerticalGroup(layoutSWx.createSequentialGroup().addGap(Globals.VGAP_SIZE)
				.addComponent(panelSWxLicencepool, 100, 200, Short.MAX_VALUE).addGap(Globals.VGAP_SIZE)
				.addComponent(panelAction, 70, 70, 100).addGap(Globals.VGAP_SIZE)

		);

		layoutSWx.setHorizontalGroup(layoutSWx.createParallelGroup()
				.addGroup(layoutSWx.createSequentialGroup().addGap(Globals.HGAP_SIZE)
						.addComponent(panelAction, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE))
				.addGroup(layoutSWx
						.createSequentialGroup().addGap(Globals.HGAP_SIZE).addComponent(panelSWxLicencepool,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE)));

		setAdditionalPane(panelSWx);
		// setTableModelSWxLicencepool( "zypper" );
		additionalPane.setBackground(Globals.backgroundWhite);// Color.YELLOW
																// );//Globals.backLightBlue
																// );
		setCenterPane(panelSWnames);
		additionalPane.setVisible(true);
		// additionalPane.setBackground( Color.RED );
		// additionalPane.setPreferredSize( new Dimension( 400, 400) );
		setupLayout();
		// setSize( new Dimension( 800, 400 ) );

	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	protected void initDataStructure() {
		columnNames = new Vector<>();
		for (String key : de.uib.configed.type.SWAuditEntry.ID_VARIANTS_COLS)
			columnNames.add(key);

		classNames = new Vector<>();
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
		}

		updateCollection = new TableUpdateCollection();

		columnNamesSWxLicencepool = new Vector<>();
		columnNamesSWxLicencepool.add(AuditSoftwareXLicencePool.SwID);
		columnNamesSWxLicencepool.add(LicencepoolEntry.idSERVICEKEY);

		classNamesSWxLicencepool = new Vector<>();
		for (int i = 0; i < columnNamesSWxLicencepool.size(); i++) {
			classNamesSWxLicencepool.add("java.lang.String");
		}

		showOnlyNamesWithVariantLicences = new DefaultTableModelFilterCondition(
				"" + Softwarename2LicencepoolRestriction.SHOW_ONLY_NAMES_WITH_VARIANT_LICENCEPOOLS) {
			@Override
			public void setFilter(TreeSet<Object> filter) {
			}

			@Override
			public boolean test(Vector<Object> row) {
				// logging.info(this, "showOnlyNamesWithVariantLicences testing row " + row);
				return getRangeSWxLicencepool((String) row.get(0)).size() > 1;
			}
		};

		showOnlyNamesWithoutLicences = new DefaultTableModelFilterCondition(
				"" + Softwarename2LicencepoolRestriction.SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENCEPOOL) {
			@Override
			public void setFilter(TreeSet<Object> filter) {
			}

			@Override
			public boolean test(Vector<Object> row) {
				// logging.info(this, "showOnlyNamesWithoutLicences testing row " + row);
				return checkExistNamesWithVariantLicencepools((String) row.get(0));
			}
		};

		panelSWnames.setListSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		panelSWnames.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				logging.info(this, "selectedRow " + panelSWnames.getSelectedRow());

				if (panelSWnames.getSelectedRow() >= 0) {
					String swName = (String) panelSWnames.getValueAt(panelSWnames.getSelectedRow(), 0);

					logging.info(this, " setTableModelSWxLicencepool for " + swName);

					setTableModelSWxLicencepool(swName);

				}

				// setTableModelSWxLicencepool

				// jButton1.setEnabled( panelSWnames.getTheTable().getSelectedRowCount() > 0 );
			}
		});

	}

	private boolean setSWxColTo(String newVal) {
		if (newVal == null)
			return false;

		for (int i = 0; i < modelSWxLicencepool.getRowCount(); i++) {
			modelSWxLicencepool.setValueAt(newVal, i, 1);
		}

		return !updateCollection.isEmpty();

	}

	private void setSWInfo(String swId, String pool) {
		logging.info(this, " setSWInfo for " + swId + " pool " + pool);
		logging.info(this, " setSWInfo in " + persist.getInstalledSoftwareName2SWinfo()
				.get(AuditSoftwareXLicencePool.produceMapFromSWident(swId).get(SWAuditEntry.NAME)));
	}

	public void setTableModel(GenTableModel model) {
		if (model == null)
		// test
		{
			logging.info(this, "init modelSWnames");
			this.modelSWnames = new GenTableModel(null, // no updates
					new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames,
							// new MapRetriever(){
							// public Map retrieveMap()
							// {
							// persist.installedSoftwareInformationRequestRefresh();
							// return persist.getInstalledSoftwareName2SWinfo();
							// }
							// }
							() -> (Map) persist.getInstalledSoftwareName2SWinfo())
					// ,

					),

					0, new int[] {}, (TableModelListener) panelSWnames, updateCollection)

			/*
			 * {
			 * 
			 * @Override
			 * public Object getValueAt( int row , int col)
			 * {
			 * logging.info("modelSWnames getValueAt row, col " + row + ", " + col);
			 * return super.getValueAt( row, col);
			 * }
			 * }
			 */

			{
				@Override
				protected void initColumns() {
					super.initColumns();
				}

				@Override
				public void produceRows() {
					super.produceRows();
					logging.info(this, "producing rows for modelSWnames");
					foundVariantLicencepools = false;
					int i = 0;
					while (!foundVariantLicencepools && i < getRowCount()) {
						foundVariantLicencepools = checkExistNamesWithVariantLicencepools((String) getValueAt(i, 0));
						i++;
					}
					myController.thePanel.setDisplaySimilarExist(foundVariantLicencepools);
				}

				@Override
				public void reset() {
					logging.info(this, "reset");
					super.reset();
				}
			}

			;
		} else {
			logging.info(this, "set modelSWnames");
			this.modelSWnames = model;
		}

		panelSWnames.setTableModel(this.modelSWnames);

		// modelSWnames.setFilterCondition( showOnlyNamesWithVariantLicences );
		// modelSWnames.setFilterCondition( showOnlyNamesWithoutLicences );
		// panelSWnames.setFiltering( true );
		// modelSWnames.reset();
		// logging.info(this, "we did a reset for modelSWnames");

	}

	public void setPreselectionForName2Pool(Softwarename2LicencepoolRestriction val) {

		switch (val) {
		case SHOW_ALL_NAMES:
			modelSWnames.clearFilter();
			break;
		case SHOW_ONLY_NAMES_WITH_VARIANT_LICENCEPOOLS:
			modelSWnames.setFilterCondition(showOnlyNamesWithVariantLicences);
			break;
		case SHOW_ONLY_NAMES_WITHOUT_ASSIGNED_LICENCEPOOL:
			modelSWnames.setFilterCondition(showOnlyNamesWithoutLicences);
			break;
		}

		modelSWnames.reset();
		// modelSWnames.requestReload();

		logging.info(this, "setPreselectionForName2Pool, we did a reset for modelSWnames with " + val);
	}

	private java.util.Set<String> getRangeSWxLicencepool(String swName)
	// nearly done in produceModelSWxLicencepool, but we collect the range of the
	// model-map
	{
		Set<String> range = new HashSet<>();

		for (String swID : persist.getName2SWIdents().get(swName)) {
			String licpool = persist.getFSoftware2LicencePool(swID);

			if (licpool == null)
				range.add(valNoLicencepool);
			else
				range.add(licpool);
		}

		return range;
	}

	private boolean checkExistNamesWithVariantLicencepools(String name) {
		java.util.Set<String> range = getRangeSWxLicencepool(name);
		if (range.size() == 1 && range.contains(valNoLicencepool))
			return true;
		return false;
	}

	public boolean checkExistNamesWithVariantLicencepools() {
		if (modelSWnames == null)
			return false;

		boolean foundVariants = false;
		int i = 0;
		while (!foundVariants && i < modelSWnames.getRowCount()) {
			foundVariants = checkExistNamesWithVariantLicencepools((String) modelSWnames.getValueAt(i, 0));
			i++;
		}

		return foundVariants;
	}

	public void setGlobalPool(String licencePool) {
		this.globalLicencePool = licencePool;
		String labelText = configed
				.getResourceValue("FSoftwarename2LicencePool.labelSetAllAssignmentsToGloballySelectedPool");
		boolean buttonActive = false;
		if (licencePool != null && !licencePool.equals("")) {
			labelText = labelText + " " + licencePool;
			buttonActive = true;
		}

		logging.info(this, "setGlobalPool  labelSetAllAssignmentsToGloballySelectedPool" + labelText);
		labelSetAllAssignmentsToGloballySelectedPool.setText(labelText);
		buttonSetAllAssignmentsToGloballySelectedPool.setEnabled(buttonActive);
	}

	public String getGlobalLicencePool() {
		return globalLicencePool;
	}

	private Map<String, Map<String, String>> produceModelSWxLicencepool(String swName) {
		logging.info(this, "produceModelSWxLicencepool for swName: " + swName);

		TreeMap<String, Map<String, String>> result = new TreeMap<>();

		for (String swID : persist.getName2SWIdents().get(swName)) {
			LinkedHashMap<String, String> rowMap = new LinkedHashMap<>();
			rowMap.put(AuditSoftwareXLicencePool.SwID, swID);
			String licpool = persist.getFSoftware2LicencePool(swID);

			if (licpool == null)
				rowMap.put(LicencepoolEntry.idSERVICEKEY, valNoLicencepool);
			else
				rowMap.put(LicencepoolEntry.idSERVICEKEY, licpool);

			result.put(swID, rowMap);
		}

		logging.info(this, "produceModelSWxLicencepool for swName: " + swName + ": " + result);

		return result;
	}

	private void setTableModelSWxLicencepool(String swName) {
		logging.info(this, " setTableModelSWxLicencepool for " + swName + " with cols " + columnNamesSWxLicencepool
				+ " and classes " + classNamesSWxLicencepool);

		MapTableUpdateItemFactory updateItemFactoySWxLicencepool = new MapTableUpdateItemFactory(
				columnNamesSWxLicencepool, classNamesSWxLicencepool, 0 // keycol
		);

		modelSWxLicencepool = new GenTableModel(updateItemFactoySWxLicencepool, new DefaultTableProvider(
				new RetrieverMapSource(columnNamesSWxLicencepool, classNamesSWxLicencepool, new MapRetriever() {
					@Override
					public Map retrieveMap() {
						logging.info(this, "retrieveMap for swName " + swName);
						return (Map) produceModelSWxLicencepool(swName);
					}
				}
				// () -> (Map) produceModelSWxLicencepool( swName )
				)
		// ,

		),

				keyCol, new int[] {}, (TableModelListener) panelSWnames, updateCollection);
		updateItemFactoySWxLicencepool.setSource(modelSWxLicencepool);
		logging.info(this, "setTableModelSWxLicencepool, we reset the model");
		modelSWxLicencepool.reset();

		panelSWxLicencepool.setTableModel(modelSWxLicencepool);

		// updates
		panelSWxLicencepool.setUpdateController(
				new MapItemsUpdateController(panelSWxLicencepool, modelSWxLicencepool, new MapBasedUpdater() {
					@Override
					public String sendUpdate(Map<String, Object> rowmap) {
						logging.info(this, "sendUpdate " + rowmap);
						// update with new value "---" == valNoLicencepool is interpreted as deleting

						// modelSWxLicencepool.requestReload();
						// myController.thePanel.panelRegisteredSoftware.requestReload();
						// reloads local data (which are not yet updated)
						String swID = (String) rowmap.get(AuditSoftwareXLicencePool.SwID);
						String licensePoolID_old = persist.getFSoftware2LicencePool(swID);
						String licensePoolID_new = (String) rowmap.get(LicencepoolEntry.idSERVICEKEY);

						if (!valNoLicencepool.equals(licensePoolID_new))
							setSWInfo(swID, licensePoolID_new);

						return persist.editPool2AuditSoftware(swID, licensePoolID_old, licensePoolID_new);

					}

					@Override
					public boolean sendDelete(Map<String, Object> rowmap) {
						logging.info(this, "sendDelete " + rowmap);
						// deleting not activated in panel

						return false;

					}
				}, updateCollection));
		// panelSWxLicencepool.setDataChanged ( true );

	}

	@Override
	public void doAction1() {
		super.doAction1();
		/*
		 * logging.debug(this, "doAction1");
		 * 
		 * logging.info(this, "removeAssociations for "
		 * + " licencePool " + myController.getSelectedLicencePool()
		 * + " selected SW keys " + panelSWnames.getSelectedKeys());
		 * 
		 * 
		 * 
		 * boolean success = persist.removeAssociations(
		 * myController.getSelectedLicencePool(),
		 * panelSWnames.getSelectedKeys()
		 * );
		 * 
		 * if (success)
		 * {
		 * for (String key : panelSWnames.getSelectedKeys())
		 * {
		 * int row = panelSWnames.findViewRowFromValue(key, keyCol);
		 * logging.info(this, "doAction1 key, " + key + ", row " + row);
		 * logging.info(this, "doAction1 model row " +
		 * panelSWnames.getTheTable().convertRowIndexToModel( row ) );
		 * panelSWnames.getTableModel().deleteRow(
		 * panelSWnames.getTheTable().convertRowIndexToModel( row ) );
		 * }
		 * result = 1;
		 * }
		 * //owner.setVisible(true);
		 * //leave();
		 */
	}

	@Override
	public void doAction2() {
		logging.debug(this, "doAction2");
		result = 2;
		owner.setVisible(true);
		leave();
	}

	@Override
	public void leave() {
		setVisible(false);
		// we dont dispose the window, dispose it in the enclosing class
		// setEnabled(false);
	}

	public void exit() {
		super.leave();
	}

}