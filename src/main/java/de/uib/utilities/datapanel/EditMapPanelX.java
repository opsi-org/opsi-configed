/* 
 *
 * (c) uib, www.uib.de, 2009-2017
 *
 * author Rupert Röder
 */

package de.uib.utilities.datapanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CellAlternatingColorizer;
import de.uib.utilities.swing.FEditText;
import de.uib.utilities.swing.JMenuItemFormatted;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.swing.XCellEditor;
import de.uib.utilities.table.DefaultListCellOptions;
import de.uib.utilities.table.ListCellOptions;
import de.uib.utilities.table.ListModelProducer;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
import de.uib.utilities.table.gui.SensitiveCellEditor;

public class EditMapPanelX extends DefaultEditMapPanel implements FocusListener
// works on a map of pairs of type String - List
{
	private static Integer objectCounter = 0;
	JScrollPane jScrollPane;
	JTable table;

	TableColumn editableColumn;
	TableCellEditor theCellEditor;
	JComboBox editorfield;
	TableCellEditor defaultCellEditor;

	ListModelProducer<String> modelProducer;

	MouseListener popupListener;
	MouseListener popupNoEditOptionsListener;

	JMenuItem popupItemDeleteEntry0;
	JMenuItem popupItemDeleteEntry1;
	JMenuItem popupItemDeleteEntry2;
	JMenuItem popupItemAddStringListEntry;
	JMenuItem popupItemAddBooleanListEntry;

	ToolTipManager ttm;

	protected class RemovingSpecificHandler extends AbstractPropertyHandler {

		@Override
		public void removeValue(String key) {
			Logging.info(this, "removing specific value for key " + key);
			// signal removal of entry to persistence modul
			mapTableModel.removeEntry(key);

			// set for this session to default, without storing the value separately)
			mapTableModel.addEntry(key, defaultsMap.get(key),
					// optionsMap.get(key).getDefaultValues(),
					false);
		}

		@Override
		public String getRemovalMenuText() {
			super.getRemovalMenuText();
			return Configed.getResourceValue("EditMapPanelX.PopupMenu.RemoveSpecificValue");
		}
	}

	protected class SettingDefaultValuesHandler extends AbstractPropertyHandler {

		@Override
		public void removeValue(String key) {
			Logging.info(this, "setting default value for key " + key);
			// signal removal of entry to persistence modul
			mapTableModel.removeEntry(key);

			// set for this session to default, without storing the value separately)
			mapTableModel.addEntry(key, defaultsMap.get(key),
					// optionsMap.get(key).getDefaultValues(),
					true // we save the value specifically
			);
		}

		@Override
		public String getRemovalMenuText() {
			super.getRemovalMenuText();
			return Configed.getResourceValue("EditMapPanelX.PopupMenu.SetSpecificValueToDefault");

		}
	}

	protected final AbstractPropertyHandler removingSpecificValuesPropertyHandler;
	protected final AbstractPropertyHandler settingDefaultValuesPropertyHandler;

	protected boolean markDeviation = true;

	public EditMapPanelX() {
		this(null);
	}

	public EditMapPanelX(TableCellRenderer tableCellRenderer) {
		this(tableCellRenderer, false);
	}

	public EditMapPanelX(TableCellRenderer tableCellRenderer, boolean keylistExtendible) {
		this(tableCellRenderer, keylistExtendible, true);
	}

	public EditMapPanelX(TableCellRenderer tableCellRenderer, boolean keylistExtendible, boolean entryRemovable) {
		this(tableCellRenderer, keylistExtendible, entryRemovable, false);
	}

	public EditMapPanelX(TableCellRenderer tableCellRenderer, boolean keylistExtendible, boolean entryRemovable,
			boolean reloadable) {
		super(tableCellRenderer, keylistExtendible, entryRemovable, reloadable);
		objectCounter++;

		Logging.debug(this, " created EditMapPanelX instance No " + objectCounter + "::" + keylistExtendible + ",  "
				+ entryRemovable + ",  " + reloadable);
		ttm = ToolTipManager.sharedInstance();
		ttm.setEnabled(true);
		ttm.setInitialDelay(Globals.TOOLTIP_INITIAL_DELAY_MS);
		ttm.setDismissDelay(Globals.TOOLTIP_DISMISS_DELAY_MS);
		ttm.setReshowDelay(Globals.TOOLTIP_RESHOW_DELAY_MS);

		buildPanel();
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(Globals.TABLE_ROW_HEIGHT);

		editableColumn = table.getColumnModel().getColumn(1);

		editorfield = new JComboBox<>();
		editorfield.setEditable(true);
		defaultCellEditor = new XCellEditor(editorfield);
		theCellEditor = defaultCellEditor;

		if (tableCellRenderer == null) {
			editableColumn.setCellRenderer(new ColorTableCellRenderer());
		} else {
			editableColumn.setCellRenderer(tableCellRenderer);
		}

		popupEditOptions = definePopup();
		popupNoEditOptions = definePopup();
		popupmenuAtRow = popupEditOptions;

		super.logPopupElements();

		popupNoEditOptionsListener = new utils.PopupMouseListener(popupNoEditOptions);
		table.addMouseListener(popupNoEditOptionsListener);
		jScrollPane.getViewport().addMouseListener(popupNoEditOptionsListener);

		// initialize special property handlers
		removingSpecificValuesPropertyHandler = new RemovingSpecificHandler();
		removingSpecificValuesPropertyHandler.setMapTableModel(mapTableModel);

		settingDefaultValuesPropertyHandler = new SettingDefaultValuesHandler();
		settingDefaultValuesPropertyHandler.setMapTableModel(mapTableModel);

		if (keylistExtendible || entryRemovable) {
			popupEditOptions.addSeparator();

			table.getTableHeader().setToolTipText(Configed.getResourceValue("EditMapPanel.PopupMenu.EditableToolTip"));

			if (keylistExtendible) {

				popupItemAddStringListEntry = new JMenuItemFormatted(
						Configed.getResourceValue("EditMapPanel.PopupMenu.AddEntrySingleSelection"));
				popupEditOptions.add(popupItemAddStringListEntry);
				popupItemAddStringListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.String", false));

				popupItemAddStringListEntry = new JMenuItemFormatted(
						Configed.getResourceValue("EditMapPanel.PopupMenu.AddEntryMultiSelection"));
				popupEditOptions.add(popupItemAddStringListEntry);
				popupItemAddStringListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.String", true));

				popupItemAddBooleanListEntry = new JMenuItemFormatted(
						Configed.getResourceValue("EditMapPanel.PopupMenu.AddBooleanEntry"));
				popupEditOptions.add(popupItemAddBooleanListEntry);
				popupItemAddBooleanListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.Boolean"));

			}

			if (entryRemovable) {

				// the crucial point of the different action listeners is that each uses its
				// special property handler
				// which get associated to different menu items (and they are handled each
				// therefore in a specific manner)

				ActionListener listenerForRemoval = actionEvent -> {
					Logging.info(this, "popupItemDeleteEntry action");
					if (table.getSelectedRowCount() == 0) {

						FTextArea fAsk = new FTextArea(ConfigedMain.getMainFrame(), Globals.APPNAME,
								Configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"), true,
								new String[] { Configed.getResourceValue("buttonOK") }, 200, 200);

						fAsk.setVisible(true);
					} else {
						if (names != null) {
							propertyHandler = defaultPropertyHandler;

							removeProperty(names.get(table.getSelectedRow()));
						}
					}
				};

				ActionListener listenerForRemovalRemoveSpecific = actionEvent -> {
					Logging.info(this, "popupItemDeleteEntry action");
					if (table.getSelectedRowCount() == 0) {

						FTextArea fAsk = new FTextArea(ConfigedMain.getMainFrame(), Globals.APPNAME,
								Configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"), true,
								new String[] { Configed.getResourceValue("buttonOK") }, 200, 200);

						fAsk.setVisible(true);
					} else {
						if (names != null) {
							propertyHandler = removingSpecificValuesPropertyHandler;

							removeProperty(names.get(table.getSelectedRow()));
						}
					}
				};

				ActionListener listenerForRemovalSetDefaultAsSpecific = actionEvent -> {
					Logging.info(this, "popupItemDeleteEntry action");
					if (table.getSelectedRowCount() == 0) {

						FTextArea fAsk = new FTextArea(ConfigedMain.getMainFrame(), Globals.APPNAME,
								Configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"), true,
								new String[] { Configed.getResourceValue("buttonOK") }, 200, 200);

						fAsk.setVisible(true);
					} else {
						if (names != null) {
							propertyHandler = settingDefaultValuesPropertyHandler;

							removeProperty(names.get(table.getSelectedRow()));
						}
					}
				};

				popupItemDeleteEntry0 = new JMenuItemFormatted(defaultPropertyHandler.getRemovalMenuText());
				popupItemDeleteEntry0.addActionListener(listenerForRemoval);

				popupEditOptions.add(popupItemDeleteEntry0);
				// the menu item seems to work only for one menu

				popupItemDeleteEntry1 = new JMenuItemFormatted(
						removingSpecificValuesPropertyHandler.getRemovalMenuText(),
						Globals.createImageIcon("images/no-value.png", ""));
				popupItemDeleteEntry1.addActionListener(listenerForRemovalRemoveSpecific);

				popupNoEditOptions.add(popupItemDeleteEntry1);

				popupItemDeleteEntry2 = new JMenuItemFormatted(settingDefaultValuesPropertyHandler.getRemovalMenuText(),
						Globals.createImageIcon("images/fixed-value.png", ""));
				popupItemDeleteEntry2.addActionListener(listenerForRemovalSetDefaultAsSpecific);

				popupNoEditOptions.add(popupItemDeleteEntry2);

			}
		}

		propertyHandler.setMapTableModel(mapTableModel);

	}

	protected JPopupMenu definePopup() {

		Logging.info(this, "(EditMapPanelX) definePopup");

		JPopupMenu result = new JPopupMenu();

		if (reloadable) {
			result = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD }) {
				@Override
				public void action(int p) {
					super.action(p);
					if (p == PopupMenuTrait.POPUP_RELOAD) {
						actor.reloadData();
					}
				}

			};
		}

		return result;
	}

	@Override
	protected void buildPanel() {
		setLayout(new BorderLayout());

		TableCellRenderer colorized = new ColorTableCellRenderer();

		table = new JTable(mapTableModel) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
				if (c instanceof JComponent && showToolTip) {
					JComponent jc = (JComponent) c;

					String propertyName = names.get(rowIndex);

					String tooltip = null;

					if (propertyName != null && defaultsMap != null && defaultsMap.get(propertyName) != null) {
						tooltip = "default: ";

						if (Globals.isKeyForSecretValue(propertyName)) {
							tooltip = tooltip + Globals.STARRED_STRING;
						} else {
							tooltip = tooltip + defaultsMap.get(propertyName);
						}
					}

					if (propertyName != null && descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
						tooltip = tooltip + "\n\n" + descriptionsMap.get(propertyName);
					}

					jc.setToolTipText(Globals.makeHTMLlines(tooltip));

					// check equals with default

					Object defaultValue;

					if (defaultsMap == null) {
						Logging.warning(this, "no default values available, defaultsMap is null");
					} else if ((defaultValue = defaultsMap.get(table.getValueAt(rowIndex, 0))) == null) {
						Logging.warning(this, "no default Value found");

						jc.setForeground(Globals.EDIT_MAP_PANEL_X_FOREGROUND_COLOR);
						jc.setToolTipText(Configed.getResourceValue("EditMapPanel.MissingDefaultValue"));

						java.awt.Font gotFont = jc.getFont();
						gotFont = gotFont.deriveFont(Font.BOLD);
						jc.setFont(gotFont);
					} else {

						Object gotValue = table.getValueAt(rowIndex, 1);
						if (markDeviation && !defaultValue.equals(gotValue)) {
							java.awt.Font gotFont = jc.getFont();
							gotFont = gotFont.deriveFont(Font.BOLD);
							jc.setFont(gotFont);
						}
					}

					if (vColIndex == 1 && Globals.isKeyForSecretValue((String) mapTableModel.getValueAt(rowIndex, 0))) {
						if (jc instanceof JLabel) {
							((JLabel) jc).setText(Globals.STARRED_STRING);
						} else if (jc instanceof javax.swing.text.JTextComponent) {
							((javax.swing.text.JTextComponent) jc).setText(Globals.STARRED_STRING);
						} else {
							CellAlternatingColorizer.colorizeSecret(jc);
						}
					}

				}
				return c;
			}

		};

		table.setDefaultRenderer(Object.class, colorized);
		table.setRowHeight(Globals.LINE_HEIGHT);
		table.setShowGrid(true);
		table.setGridColor(Globals.EDIT_MAP_PANEL_X_GRID_COLOR);

		table.addMouseWheelListener(mouseWheelEvent -> {

			int selRow = -1;

			if (table.getSelectedRows() == null || table.getSelectedRows().length == 0) {
				selRow = -1;
			}

			else {
				selRow = table.getSelectedRows()[0];
			}

			int diff = mouseWheelEvent.getWheelRotation();

			selRow = selRow + diff;

			if (selRow >= table.getRowCount()) {
				selRow = table.getRowCount() - 1;
			}

			int startRow = 0;

			if (selRow < startRow) {
				selRow = startRow;
			}

			setSelectedRow(selRow);
		});

		jScrollPane = new JScrollPane(table);
		jScrollPane.getViewport().setBackground(Globals.BACKGROUND_COLOR_7);

		add(jScrollPane, BorderLayout.CENTER);
	}

	@Override
	public void init() {
		setEditableMap(null, null);
	}

	/**
	 * setting all data for displaying and editing <br />
	 * 
	 * @param Map visualdata - the source for the table model
	 * @param Map optionsMap - the description for producing cell editors
	 */

	public void setCellEditor(SensitiveCellEditor cellEditor) {
		theCellEditor = cellEditor;
	}

	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ListCellOptions> optionsMap) {
		super.setEditableMap(visualdata, optionsMap);
		cancelOldCellEditing();

		if (optionsMap != null) {

			for (Entry<String, ListCellOptions> option : optionsMap.entrySet()) {
				Logging.debug(this, " key " + option.getKey() + " is nullable " + option.getValue().isNullable());
			}

			modelProducer = new ListModelProducerForVisualDatamap(table, optionsMap, visualdata);
		}

		Logging.debug(this, "setEditableMap set modelProducer  == null " + (modelProducer == null));
		if (modelProducer != null) {
			Logging.debug(this, "setEditableMap test modelProducer " + modelProducer.getClass());
			Logging.debug(this, "setEditableMap test modelProducer " + modelProducer.getClass(0, 0));
		}

		mapTableModel.setModelProducer((ListModelProducerForVisualDatamap) modelProducer);

		if (theCellEditor instanceof SensitiveCellEditor) {

			((SensitiveCellEditor) theCellEditor).setModelProducer(modelProducer);

			((SensitiveCellEditor) theCellEditor).setForbiddenValues(mapTableModel.getShowOnlyValues());

			((SensitiveCellEditor) theCellEditor).reInit();
		}

		editableColumn.setCellEditor(theCellEditor);

	}

	@Override
	public void cancelOldCellEditing() {
		super.cancelOldCellEditing();

		if (theCellEditor instanceof SensitiveCellEditor) {
			((SensitiveCellEditor) theCellEditor).hideListEditor();
		}
	}

	private boolean checkKey(String s) {
		boolean ok = false;

		if (s != null && !s.equals("")) {
			ok = true;

			if (names.indexOf(s) > -1) {
				ok =

						(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
								"Ein Eintrag mit diesem Namen existiert bereits. Überschreiben des bisherigen Eintrags?",
								Globals.APPNAME, JOptionPane.OK_CANCEL_OPTION));
			}

		}

		return ok;
	}

	private void addEntryFor(final String classname) {
		addEntryFor(classname, false);
	}

	private void addEntryFor(final String classname, final boolean multiselection) {
		String initial = "";
		int row = table.getSelectedRow();
		if (row > -1) {
			initial = (String) table.getValueAt(row, 0);
		}

		FEditText fed = new FEditText(initial, Configed.getResourceValue("EditMapPanel.KeyToAdd")) {

			@Override
			protected void commit() {
				super.commit();
				String s = getText();

				if (checkKey(s)) {
					setVisible(false);
					if (classname.equals("java.lang.Boolean")) {
						addBooleanProperty(s);
					} else {
						if (multiselection) {
							addEmptyPropertyMultiSelection(s);
						} else {
							addEmptyProperty(s);
						}
					}
				}

			}
		};

		fed.setModal(true);
		fed.setSingleLine(true);
		fed.select(0, initial.length());
		fed.setTitle(Globals.APPNAME);
		fed.init(new Dimension(300, 50));

		Logging.info(this, "locate frame fed on center of mainFrame and then make it visible");
		fed.setLocationRelativeTo(ConfigedMain.getMainFrame());
		fed.setVisible(true);
	}

	public void addEmptyProperty(String key) {
		List<String> val = new ArrayList<>();
		val.add("");
		addProperty(key, val);
		optionsMap.put(key, DefaultListCellOptions.getNewEmptyListCellOptions());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap) modelProducer).setData(optionsMap, mapTableModel.getData());
	}

	public void addEmptyPropertyMultiSelection(String key) {
		List<String> val = new ArrayList<>();
		val.add("");
		addProperty(key, val);
		optionsMap.put(key, DefaultListCellOptions.getNewEmptyListCellOptionsMultiSelection());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap) modelProducer).setData(optionsMap, mapTableModel.getData());
	}

	public void addBooleanProperty(String key) {
		List<Object> val = new ArrayList<>();
		val.add(false);
		addProperty(key, val);
		optionsMap.put(key, DefaultListCellOptions.getNewBooleanListCellOptions());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap) modelProducer).setData(optionsMap, mapTableModel.getData());
	}

	/**
	 * adding an entry to the table model and, finally, to the table
	 * 
	 * @param String key
	 * @param Object value (if null then an empty String is the value)
	 */
	protected final void addProperty(String key, Object newval) {
		mapTableModel.addEntry(key, newval);
		names = mapTableModel.getKeys();

	}

	/**
	 * deleting an entry
	 * 
	 * @param String key - the key to delete
	 */
	public void removeProperty(String key) {

		Logging.info(this, " EditMapPanelX instance No " + objectCounter + "::" + " removeProperty for key " + key
				+ " via  handler " + propertyHandler);

		propertyHandler.removeValue(key);

		Logging.info(this, " EditMapPanelX instance No " + objectCounter + "::" + " handled removeProperty for key "
				+ key + " options " + optionsMap.get(key));
		Logging.info(this, "handled removeProperty for key " + key + " default value  " + defaultsMap.get(key)
				+ " - should be identical with - " + optionsMap.get(key).getDefaultValues());

		names = mapTableModel.getKeys();
		Logging.info(this, "removeProperty names left: " + names);

	}

	public void stopEditing() {
		// we prefer not to cancel cell editing
		if (table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
	}

	// FocusListener

	@Override
	public void focusLost(FocusEvent e) {
		stopEditing();
	}

	@Override
	public void focusGained(FocusEvent e) {
		/* Not needed */}

	protected void setSelectedRow(int row) {
		table.setRowSelectionInterval(row, row);

		showSelectedRow();
	}

	protected void showSelectedRow() {
		int row = table.getSelectedRow();
		if (row != -1) {
			table.scrollRectToVisible(table.getCellRect(row, 0, false));
		}
	}

	@Override
	public void setOptionsEditable(boolean b) {
		Logging.debug(this, "setOptionsEditable " + b);

		if (b) {

			popupmenuAtRow = popupEditOptions;

		} else {

			popupmenuAtRow = popupNoEditOptions;

		}

		popupListener = new utils.PopupMouseListener(popupmenuAtRow);
		table.addMouseListener(popupListener);
		jScrollPane.getViewport().addMouseListener(popupListener);
	}

}
