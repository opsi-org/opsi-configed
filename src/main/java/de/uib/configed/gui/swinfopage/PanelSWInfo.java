package de.uib.configed.gui.swinfopage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.DatedRowList;
import de.uib.configed.type.SWAuditClientEntry;
import de.uib.configed.type.SWAuditEntry;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.ExporterToCSV;
import de.uib.utilities.table.ExporterToPDF;
import de.uib.utilities.table.GenTableModel;
import de.uib.utilities.table.TableModelFilter;
import de.uib.utilities.table.gui.ColorHeaderCellRenderer;
import de.uib.utilities.table.gui.PanelGenEditTable;
import de.uib.utilities.table.gui.StandardTableCellRenderer;
import de.uib.utilities.table.gui.TablesearchPane;
import de.uib.utilities.table.provider.DefaultTableProvider;
import de.uib.utilities.table.provider.MapRetriever;
import de.uib.utilities.table.provider.RetrieverMapSource;

public class PanelSWInfo extends JPanel {
	protected PanelGenEditTable panelTable;
	protected ExporterToCSV csvExportTable;
	protected ExporterToPDF pdfExportTable;

	protected JPanel subPanelTitle;

	protected JScrollPane scrollPaneSWInfo;
	protected JTable jTable;
	protected final SWInfoTableModel voidTableModel = new SWInfoTableModel();
	protected GenTableModel modelSWInfo;

	protected JLabel labelSuperTitle;

	protected String supertitle = "";
	protected String title = "";
	protected DatedRowList swRows;
	protected String hostId = "";
	protected boolean withPopup;

	protected String scanInfo = "";

	private PopupMenuTrait popupTrait;

	protected Boolean askingForKindOfAction;
	protected boolean askForOverwrite = true;

	protected int hGap = Globals.HGAP_SIZE / 2;
	protected int vGap = Globals.VGAP_SIZE / 2;
	protected int hLabel = Globals.BUTTON_HEIGHT;

	protected ConfigedMain mainController;
	protected PersistenceController persist;

	public enum KindOfExport {
		PDF, CSV
	};

	protected KindOfExport kindOfExport;

	boolean withMsUpdates = false;
	boolean withMsUpdates2 = true;
	String exportFilename = null;

	JCheckBox checkWithMsUpdates;
	static final String FILTER_MS_UPDATES = "withMsUpdates";
	int indexOfColWindowsSoftwareID;

	de.uib.utilities.table.TableModelFilterCondition filterConditionWithMsUpdates = new de.uib.utilities.table.TableModelFilterCondition() {
		@Override
		public void setFilter(TreeSet<Object> filter) {
		}

		@Override
		public boolean test(Vector<Object> row) {
			String entry = (String) row.get(indexOfColWindowsSoftwareID);
			boolean isKb = entry.startsWith("kb");

			/*
			 * if (!isBK)
			 * {
			 * String[] parts = entry.split
			 * isKb = entry.endsWiths(
			 * }
			 */

			return !isKb;
			// on filtering active everything is taken if not isKb

		}
	};

	JCheckBox checkWithMsUpdates2;
	static final String FILTER_MS_UPDATES2 = "withMsUpdates2";

	final java.util.regex.Pattern patternWithKB = java.util.regex.Pattern.compile("\\{.*\\}\\p{Punct}kb.*");

	de.uib.utilities.table.TableModelFilterCondition filterConditionWithMsUpdates2 = new de.uib.utilities.table.TableModelFilterCondition() {
		@Override
		public void setFilter(TreeSet<Object> filter) {
		}

		@Override
		public boolean test(Vector<Object> row) {
			String entry = (String) row.get(indexOfColWindowsSoftwareID);
			boolean isKb = (patternWithKB.matcher(entry)).matches();

			return !isKb;
			// on filtering active everything is taken if not isKb

		}
	};

	public PanelSWInfo(ConfigedMain mainController) {
		this(true, mainController);
		askingForKindOfAction = false;
	}

	public PanelSWInfo(boolean withPopup, ConfigedMain mainController) {
		this.withPopup = withPopup;
		this.mainController = mainController;
		persist = mainController.getPersistenceController();

		initTable();

		buildPanel();
		askingForKindOfAction = true;
	}

	protected void initTable() {

		labelSuperTitle = new JLabel();
		/*
		 * {
		 * 
		 * @Override
		 * public void setText(String s)
		 * {
		 * logging.info(this, "setText " + s);
		 * super.setText(s);
		 * }
		 * };
		 */

		labelSuperTitle.setFont(Globals.defaultFontBold);

		panelTable = new PanelGenEditTable("title", 0, false, 0, true, new int[] {
				// PanelGenEditTable.POPUP_RELOAD,
				// PanelGenEditTable.POPUP_FLOATINGCOPY,
				// PanelGenEditTable.POPUP_PDF
		}, true) {
			/*
			 * @Override
			 * protected void floatExternal()
			 * {
			 * floatExternalX();
			 * }
			 * 
			 * @Override
			 * public void reload()
			 * {
			 * 
			 * 
			 * super.reload();
			 * }
			 */
		};

		panelTable.setTitle("");

		// therefore postponed
		panelTable.setColumnSelectionAllowed(false); // up to now, true is destroying search function

		panelTable.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelTable.setSearchSelectMode(true);
		panelTable.setSearchMode(TablesearchPane.FULL_TEXT_SEARCH);

		Vector<String> columnNames;
		Vector<String> classNames;

		columnNames = new Vector<>(SWAuditClientEntry.KEYS);
		columnNames.remove(0);
		classNames = new Vector<>();
		int[] finalColumns = new int[columnNames.size()];
		for (int i = 0; i < columnNames.size(); i++) {
			classNames.add("java.lang.String");
			finalColumns[i] = i;
		}

		modelSWInfo = new GenTableModel(null, // no updates
				new DefaultTableProvider(new RetrieverMapSource(columnNames, classNames, new MapRetriever() {
					@Override
					public Map<String, Map> retrieveMap() {
						logging.info(this, "retrieving data for " + hostId);
						Map<String, Map> tableData = persist.retrieveSoftwareAuditData(hostId);

						if (tableData == null || tableData.keySet().isEmpty()) {
							scanInfo = de.uib.configed.configed.getResourceValue("PanelSWInfo.noScanResult");
							title = scanInfo;
						} else {
							logging.debug(this, "retrieved size  " + tableData.keySet().size());
							scanInfo = "Scan " + persist.getLastSoftwareAuditModification(hostId);
							title = scanInfo;

						}

						setSuperTitle(scanInfo);

						logging.debug(this, " got scanInfo " + scanInfo);

						return tableData;
					}
				})), -1, finalColumns, null, null);

		indexOfColWindowsSoftwareID = columnNames.indexOf(SWAuditEntry.WINDOWSsOFTWAREid);
		modelSWInfo.chainFilter(FILTER_MS_UPDATES, new TableModelFilter(filterConditionWithMsUpdates));
		modelSWInfo.reset();

		panelTable.setDataChanged(false);
		checkWithMsUpdates = new JCheckBox("", withMsUpdates);
		checkWithMsUpdates.setForeground(Globals.blue);
		checkWithMsUpdates.addItemListener(itemEvent -> setWithMsUpdatesValue(checkWithMsUpdates.isSelected()));
		setWithMsUpdatesValue(withMsUpdates);

		modelSWInfo.chainFilter(FILTER_MS_UPDATES2, new TableModelFilter(filterConditionWithMsUpdates2));
		modelSWInfo.reset();

		panelTable.setDataChanged(false);
		checkWithMsUpdates2 = new JCheckBox("", withMsUpdates2);
		checkWithMsUpdates2.setForeground(Globals.blue);
		checkWithMsUpdates2.addItemListener(itemEvent -> setWithMsUpdatesValue2(checkWithMsUpdates2.isSelected()));
		setWithMsUpdatesValue2(withMsUpdates2);

		subPanelTitle = new JPanel();

		JLabel labelWithMSUpdates = new JLabel(configed.getResourceValue("PanelSWInfo.withMsUpdates"));
		JLabel labelWithMSUpdates2 = new JLabel(configed.getResourceValue("PanelSWInfo.withMsUpdates2"));

		subPanelTitle.setBackground(Globals.backLightBlue);

		GroupLayout layoutSubPanelTitle = new GroupLayout(subPanelTitle);
		subPanelTitle.setLayout(layoutSubPanelTitle);

		layoutSubPanelTitle.setHorizontalGroup(layoutSubPanelTitle.createSequentialGroup().addGap(hGap, hGap, hGap)
				.addGroup(layoutSubPanelTitle.createParallelGroup()
						.addComponent(labelSuperTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGroup(layoutSubPanelTitle.createSequentialGroup()
								.addComponent(labelWithMSUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(hGap, hGap, hGap).addGap(hGap, hGap, hGap)
								.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(hGap, hGap, hGap)
				.addGroup(layoutSubPanelTitle.createSequentialGroup()
						.addComponent(labelWithMSUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(hGap, hGap, hGap).addGap(hGap, hGap, hGap).addComponent(checkWithMsUpdates2,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(hGap, hGap, hGap));
		layoutSubPanelTitle.setVerticalGroup(layoutSubPanelTitle.createSequentialGroup().addGap(vGap, vGap, vGap)
				.addComponent(labelSuperTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(vGap, vGap, vGap)
				// .addGap(vGap, vGap, vGap)
				.addGroup(layoutSubPanelTitle.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelWithMSUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkWithMsUpdates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelWithMSUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(checkWithMsUpdates2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(vGap, vGap, vGap).addGap(vGap, vGap, vGap));

		panelTable.setTableModel(modelSWInfo);
		panelTable.setSearchColumnsAll();

		panelTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		panelTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		panelTable.getColumnModel().getColumn(2).setPreferredWidth(100);

		csvExportTable = new ExporterToCSV(panelTable.getTheTable());

	}

	protected void buildPanel() {

		labelSuperTitle.setOpaque(true);
		labelSuperTitle.setBackground(Globals.backgroundLightGrey);

		/*
		 * logging.info(this, "voidTableModel cols hopefully   " +
		 * SWAuditEntry.KEYS);
		 * for (int i = 0; i<voidTableModel.getColumnCount(); i++)
		 * {
		 * logging.info(this, "voidTableModel col " + i + " " +
		 * voidTableModel.getColumnName(i));
		 * }
		 * System.exit(0);
		 */

		jTable = new JTable(voidTableModel, null);

		jTable.setAutoCreateRowSorter(true);
		TableRowSorter tableSorter = (TableRowSorter) jTable.getRowSorter();
		ArrayList<RowSorter.SortKey> list = new ArrayList<>(1);
		list.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
		tableSorter.setSortKeys(list);
		tableSorter.sort();

		jTable.setDefaultRenderer(Object.class, new StandardTableCellRenderer());
		jTable.getColumnModel().getColumn(0).setPreferredWidth(400);
		jTable.getColumnModel().getColumn(1).setPreferredWidth(200);
		jTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		jTable.getTableHeader()
				.setDefaultRenderer(new ColorHeaderCellRenderer(jTable.getTableHeader().getDefaultRenderer()));
		jTable.setColumnSelectionAllowed(true);
		jTable.setRowSelectionAllowed(true);
		jTable.setDragEnabled(true);
		scrollPaneSWInfo = new JScrollPane(jTable);
		scrollPaneSWInfo.getViewport().setBackground(Globals.backLightBlue);

		GroupLayout layoutEmbed = new GroupLayout(this);
		setLayout(layoutEmbed);

		layoutEmbed
				.setHorizontalGroup(layoutEmbed
						.createSequentialGroup().addGap(hGap, hGap, hGap).addGroup(layoutEmbed.createParallelGroup()
								/*
								 * .addGroup(layoutEmbed.createSequentialGroup()
								 * .addGap(hGap, hGap, hGap)
								 * .addComponent(jlabelSuperTitle, javax.swing.GroupLayout.PREFERRED_SIZE,
								 * javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								 * .addGap(hGap, hGap, hGap)
								 * )
								 */
								// .addComponent(scrollPaneSWInfo, javax.swing.GroupLayout.PREFERRED_SIZE,
								// javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(subPanelTitle, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								// .addComponent(subPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE,
								// javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(panelTable, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(hGap, hGap, hGap));

		layoutEmbed.setVerticalGroup(layoutEmbed.createSequentialGroup()
				// .addGap(vGap, vGap, vGap)
				// .addComponent(jlabelSuperTitle, hLabel, hLabel, hLabel)
				// .addGap(vGap, vGap, vGap)
				// .addComponent(scrollPaneSWInfo, javax.swing.GroupLayout.PREFERRED_SIZE,
				// javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(vGap, vGap, vGap)
				.addComponent(subPanelTitle, javax.swing.GroupLayout.PREFERRED_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
				// .addComponent(subPanelButtons, javax.swing.GroupLayout.PREFERRED_SIZE,
				// javax.swing.GroupLayout.PREFERRED_SIZE,
				// javax.swing.GroupLayout.PREFERRED_SIZE)
				.addComponent(panelTable, javax.swing.GroupLayout.PREFERRED_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(vGap, vGap, vGap));

		if (withPopup) {
			popupTrait = new PopupMenuTrait(
					new Integer[] { PopupMenuTrait.POPUP_EXPORT_CSV, PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV,

							PopupMenuTrait.POPUP_RELOAD, PopupMenuTrait.POPUP_PDF,

							PopupMenuTrait.POPUP_FLOATINGCOPY }) {
				@Override
				public void action(int p) {

					switch (p) {
					case PopupMenuTrait.POPUP_RELOAD:
						reload();
						break;

					case PopupMenuTrait.POPUP_FLOATINGCOPY:
						floatExternalX();
						break;

					case PopupMenuTrait.POPUP_PDF:
						sendToPDF();
						break;

					case PopupMenuTrait.POPUP_EXPORT_CSV:
						sendToCSV();
						break;

					case PopupMenuTrait.POPUP_EXPORT_SELECTED_CSV:
						sendToCSVonlySelected();
						break;

					}
				}

			};

			popupTrait.addPopupListenersTo(new JComponent[] { this, panelTable.getTheTable(),
					panelTable.getTheScrollpane(), jTable, scrollPaneSWInfo, scrollPaneSWInfo.getViewport() });

		}

	}

	public void setAskingForKindOfAction(boolean b) {
		askingForKindOfAction = b;
	}

	public void setWriteToFile(String path) {
		exportFilename = path;
	}

	public void setAskForOverwrite(boolean b) {
		askForOverwrite = b;
	}

	public void setKindOfExport(KindOfExport k) {
		kindOfExport = k;
	}

	public void setWithMsUpdates(boolean b) {
		checkWithMsUpdates.setSelected(b);
		setWithMsUpdatesValue(b);
	}

	public void setWithMsUpdates2(boolean b) {
		checkWithMsUpdates2.setSelected(b);
		setWithMsUpdatesValue2(b);
	}

	private void setWithMsUpdatesValue(boolean b) {
		withMsUpdates = b;

		// setting filter true means that the specified values are not included

		if (panelTable != null && modelSWInfo != null) {
			boolean saveDataChanged = panelTable.isDataChanged();
			modelSWInfo.setUsingFilter(FILTER_MS_UPDATES, !b);
			panelTable.setDataChanged(saveDataChanged);
		}
	}

	private void setWithMsUpdatesValue2(boolean b) {
		withMsUpdates2 = b;

		// setting filter true means that the specified values are not included

		if (panelTable != null && modelSWInfo != null) {
			boolean saveDataChanged = panelTable.isDataChanged();
			modelSWInfo.setUsingFilter(FILTER_MS_UPDATES2, !b);
			panelTable.setDataChanged(saveDataChanged);
		}
	}

	public void sendToTerminal() {

		de.uib.configed.gui.swinfopage.SWterminalExporter exporter = new de.uib.configed.gui.swinfopage.SWterminalExporter(
				PersistenceControllerFactory.getPersistenceController());

		exporter.setHost(hostId);

		if (panelTable.getSelectedRowCount() > 0)
			exporter.setOnlySelectedRows();

		exporter.setPanelTableForExportTable(panelTable);

		exporter.export();

	}

	public void export() {
		csvExportTable.setAskForOverwrite(askForOverwrite);
		String exportPath = exportFilename;
		switch (kindOfExport) {
		case CSV:
			logging.info(this, "export to " + exportPath);
			csvExportTable.execute(exportPath, false);
			break;

		case PDF:
			sendToPDF();
			break;
		}
	}

	public void sendToCSV() {
		csvExportTable.execute(null, false);
	}

	public void sendToCSVonlySelected() {
		csvExportTable.execute(null, true);
	}

	public void sendToPDF() {
		logging.info(this, "------------- create report swaudit for " + hostId + " check");

		HashMap<String, String> metaData = new HashMap<>();
		// metaData.put("header", " " + mf.format( new String[] { hostId,

		metaData.put("title", "Client " + hostId);
		metaData.put("subtitle", scanInfo);
		metaData.put("subject", "report of table");
		metaData.put("keywords", "software inventory");

		ExporterToPDF pdfExportTable = new ExporterToPDF(panelTable);
		pdfExportTable.setClient(hostId);
		pdfExportTable.setMetaData(metaData);
		pdfExportTable.setPageSizeA4_Landscape();
		pdfExportTable.execute(exportFilename, false);

		/*
		 * old pdf exporting
		 * 
		 * 
		 * filename, metadata
		 * //the real filename is summoned in the toPDF method call
		 * DocumentToPdf tableToPDF = new DocumentToPdf ("report_swaudit_" + clientName,
		 * metaData); // no filename, metadata
		 * tableToPDF.setAskForOverwrite( askForOverwrite );
		 * 
		 * ArrayList list = new ArrayList<>();
		 * list.add(0); // column(s)
		 * de.uib.utilities.pdf.DocumentElementToPdf.setAlignmentLeft(list);
		 * 
		 * 
		 * 
		 * 
		 * 
		 * //tableToPDF.createContentElement("tablemodel", sourceModel); //we use the
		 * model in order to avoid any graphical component
		 * 
		 * 
		 * modelSWInfo.setSorting(0, true);
		 * tableToPDF.createContentElement("tablemodel", modelSWInfo); //we use the
		 * model in order to avoid any graphical component
		 * tableToPDF.setPageSizeA4_Landscape(); //
		 * if (askingForKindOfAction)
		 * tableToPDF.toPDF(null, pdfFilename);// create Pdf, ask if it shall be shown
		 * or saved
		 * else
		 * tableToPDF.toPDF(true, pdfFilename );// create Pdf, and save it, if
		 * exportFilename null, ask for it
		 **/
	}

	private void setSuperTitle(String s) {
		supertitle = "" + s;
		logging.info(this, "setSuperTitle " + s);
		labelSuperTitle.setText(supertitle);

	}

	/** overwrite in subclasses */
	protected void reload() {
		logging.debug(this, "reload action");
	}

	protected void floatExternalX() {

		PanelSWInfo copyOfMe;
		de.uib.configed.gui.GeneralFrame externalView;

		copyOfMe = new PanelSWInfo(false, mainController);
		copyOfMe.setHost(hostId);
		copyOfMe.updateModel();

		externalView = new de.uib.configed.gui.GeneralFrame(null, title, false);
		externalView.addPanel(copyOfMe);
		externalView.setup();
		externalView.setSize(this.getSize());
		externalView.centerOn(Globals.mainFrame);

		externalView.setVisible(true);
	}

	public void updateModel() {
		logging.info(this, "update+++++");

		logging.info(this, "update+++++ modelSWInfo.getRowCount() " + modelSWInfo.getRowCount());

		modelSWInfo.requestReload();
		modelSWInfo.reset();

	}

	public void setSoftwareNullInfo(String hostId) {
		logging.info(this, "setSoftwareNullInfo,  " + hostId + " -- ");

		this.hostId = "" + hostId;
		title = this.hostId;
		this.swRows = new DatedRowList();

		String timeS = "" + Globals.getToday();
		String[] parts = timeS.split(":");
		if (parts.length > 2)
			timeS = parts[0] + ":" + parts[1];

		scanInfo = " (no software audit data, checked at time:  " + timeS + ")";

		return;
	}

	public void setHost(String hostId) {
		logging.info(this, "setHost" + hostId + " -- ");

		this.hostId = "" + hostId;
	}

	/*
	 * public void setSoftwareInfo (String hostId)
	 * {
	 * logging.info(this, "setSoftwareInfo for " + hostId + " -- " );
	 * 
	 * this.hostId = "" + hostId;
	 * 
	 * 
	 * 
	 * String timeS = "" + Globals.getToday();
	 * String[] parts = timeS.split(":");
	 * if (parts.length > 2)
	 * timeS = parts[0] + ":" + parts[1];
	 * 
	 * 
	 * //panelTable.setTitle("(no software audit data, checked at time:  " + timeS +
	 * ")" );
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * title = hostId;
	 * 
	 * this.swRows = swRows;
	 * if (swRows == null)
	 * this.swRows = new DatedRowList();
	 * 
	 * jLabelTitle.setText(" (no software audit data, checked at time:  " + timeS +
	 * ")");
	 * 
	 * 
	 * if (swRows == null)
	 * {
	 * voidTableModel.setData(this.swRows); 
	 * return;
	 * }
	 * 
	 * 
	 * if (swRows.getDate() != null)
	 * {
	 * MessageFormat mf = new MessageFormat(
	 * configed.getResourceValue("PanelSWInfo.jLabel_title") );
	 * jLabelTitle.setText(" " + mf.format( new String[] { hostId, swRows.getDate()}
	 * ));
	 * title = title + "   " + configed.getResourceValue("PanelSWInfo.title");
	 * }
	 * 
	 * //jLabelTitle.setText(" " + swRows.getDate());
	 * 
	 * 
	 * 
	 * 
	 * 
	 * }
	 */

	protected class SWInfoTableModel extends AbstractTableModel {
		private List<String[]> data;

		public SWInfoTableModel() {
			super();
			data = new ArrayList<>();
		}

		public void setData(DatedRowList datedList) {
			this.data = datedList.getRows();

			fireTableDataChanged();
		}

		@Override
		public int getRowCount() {
			return data.size();
		}

		@Override
		public int getColumnCount() {
			return SWAuditClientEntry.getDisplayKeys().size(); // not key "ID";
		}

		@Override
		public String getColumnName(int column) {
			return SWAuditClientEntry.getDisplayKey(column + 1);
		}

		@Override
		public Object getValueAt(int row, int col) {
			return (/* encodeString */ (String[]) data.get(row))[col + 1];
		}
	}

}
