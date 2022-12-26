/* 
 *
 * (c) uib, www.uib.de, 2009-2017
 *
 * author Rupert Röder
 */

package de.uib.utilities.datapanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Map;

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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.logging;
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
	public static Integer objectCounter = 0;
	JScrollPane jScrollPane;
	JTable table;

	TableColumn editableColumn;
	TableCellEditor theCellEditor;
	JComboBox editorfield;
	TableCellEditor defaultCellEditor;

	ListModelProducer modelProducer;

	MouseListener popupListener;
	MouseListener popupEditOptionsListener;
	MouseListener popupNoEditOptionsListener;

	JMenuItem popupItemDeleteEntry0;
	JMenuItem popupItemDeleteEntry1;
	JMenuItem popupItemDeleteEntry2;
	JMenuItem popupItemAddStringListEntry;
	JMenuItem popupItemAddBooleanListEntry;

	ToolTipManager ttm;

	protected class RemovingSpecificHandler extends PropertyHandler {

		@Override
		public void removeValue(String key) {
			logging.info(this, "removing specific value for key " + key);
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
			return configed.getResourceValue("EditMapPanelX.PopupMenu.RemoveSpecificValue");
		}
	}

	protected class SettingDefaultValuesHandler extends PropertyHandler {

		@Override
		public void removeValue(String key) {
			logging.info(this, "setting default value for key " + key);
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
			return configed.getResourceValue("EditMapPanelX.PopupMenu.SetSpecificValueToDefault");

		}
	}

	protected final PropertyHandler removingSpecificValuesPropertyHandler;
	protected final PropertyHandler settingDefaultValuesPropertyHandler;

	protected class DatadependentPopupMenuListener implements PopupMenuListener {
		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		}

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		}
	}

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

		logging.debug(this, " created EditMapPanelX instance No " + objectCounter + "::" + keylistExtendible + ",  "
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

		if (tableCellRenderer == null)
			editableColumn.setCellRenderer(new ColorTableCellRenderer());
		else
			editableColumn.setCellRenderer(tableCellRenderer);

		popupEditOptions = definePopup();
		popupNoEditOptions = definePopup();
		popupmenuAtRow = popupEditOptions;

		logPopupElements(popupmenuAtRow);

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

			table.getTableHeader().setToolTipText(configed.getResourceValue("EditMapPanel.PopupMenu.EditableToolTip"));

			if (keylistExtendible) {

				popupItemAddStringListEntry = new JMenuItemFormatted(
						configed.getResourceValue("EditMapPanel.PopupMenu.AddEntrySingleSelection"));
				popupEditOptions.add(popupItemAddStringListEntry);
				popupItemAddStringListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.String", false));

				popupItemAddStringListEntry = new JMenuItemFormatted(
						configed.getResourceValue("EditMapPanel.PopupMenu.AddEntryMultiSelection"));
				popupEditOptions.add(popupItemAddStringListEntry);
				popupItemAddStringListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.String", true));

				popupItemAddBooleanListEntry = new JMenuItemFormatted(
						configed.getResourceValue("EditMapPanel.PopupMenu.AddBooleanEntry"));
				popupEditOptions.add(popupItemAddBooleanListEntry);
				popupItemAddBooleanListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.Boolean"));

			}

			if (entryRemovable) {

				// the crucial point of the different action listeners is that each uses its
				// special property handler
				// which get associated to different menu items (and they are handled each
				// therefore in a specific manner)

				ActionListener listenerForRemoval = actopmEvent -> {
					logging.info(this, "popupItemDeleteEntry action");
					if (table.getSelectedRowCount() == 0) {

						FTextArea fAsk = new FTextArea(null, Globals.APPNAME, "", true);
						fAsk.setSize(new Dimension(200, 200));
						fAsk.setModal(true);
						fAsk.setMessage(configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"));

						fAsk.setVisible(true);
					} else {
						if (names != null) {
							propertyHandler = defaultPropertyHandler;

							removeProperty(names.elementAt(table.getSelectedRow()));
						}
					}
				};

				ActionListener listenerForRemoval_REMOVE_SPECIFIC = actionEvent -> {
					logging.info(this, "popupItemDeleteEntry action");
					if (table.getSelectedRowCount() == 0) {

						FTextArea fAsk = new FTextArea(null, Globals.APPNAME, "", true);
						fAsk.setSize(new Dimension(200, 200));
						fAsk.setModal(true);
						fAsk.setMessage(configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"));

						fAsk.setVisible(true);
					} else {
						if (names != null) {
							propertyHandler = removingSpecificValuesPropertyHandler;

							removeProperty(names.elementAt(table.getSelectedRow()));
						}
					}
				};

				ActionListener listenerForRemoval_SET_DEFAULT_AS_SPECIFIC = actionEvent -> {
					logging.info(this, "popupItemDeleteEntry action");
					if (table.getSelectedRowCount() == 0) {

						FTextArea fAsk = new FTextArea(null, Globals.APPNAME, "", true);
						fAsk.setSize(new Dimension(200, 200));
						fAsk.setModal(true);
						fAsk.setMessage(configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"));

						fAsk.setVisible(true);
					} else {
						if (names != null) {
							propertyHandler = settingDefaultValuesPropertyHandler;

							removeProperty(names.elementAt(table.getSelectedRow()));
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
				popupItemDeleteEntry1.addActionListener(listenerForRemoval_REMOVE_SPECIFIC);

				popupNoEditOptions.add(popupItemDeleteEntry1);

				popupItemDeleteEntry2 = new JMenuItemFormatted(settingDefaultValuesPropertyHandler.getRemovalMenuText(),
						Globals.createImageIcon("images/fixed-value.png", ""));
				popupItemDeleteEntry2.addActionListener(listenerForRemoval_SET_DEFAULT_AS_SPECIFIC);

				popupNoEditOptions.add(popupItemDeleteEntry2);

			}
		}

		propertyHandler.setMapTableModel(mapTableModel);

	}

	protected JPopupMenu definePopup() {

		logging.info(this, "(EditMapPanelX) definePopup");

		JPopupMenu result = new JPopupMenu();

		if (reloadable) {
			result = new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD }) {
				@Override
				public void action(int p) {
					super.action(p);
					switch (p) {
					case PopupMenuTrait.POPUP_RELOAD:
						actor.reloadData();
						break;
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

					String propertyName = (String) names.get(rowIndex);

					String tooltip = null;

					if (propertyName != null && defaultsMap != null && defaultsMap.get(propertyName) != null) {
						tooltip = "default: ";

						if (Globals.isKeyForSecretValue(propertyName))
							tooltip = tooltip + Globals.STARRED_STRING;
						else
							tooltip = tooltip + defaultsMap.get(propertyName);
					}

					if (propertyName != null && descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
						tooltip = tooltip + "\n\n" + descriptionsMap.get(propertyName);
					}

					jc.setToolTipText(Globals.makeHTMLlines(tooltip));

					// check equals with default

					Object defaultValue = defaultsMap.get(table.getValueAt(rowIndex, 0));
					Object gotValue = table.getValueAt(rowIndex, 1);

					if (defaultValue == null) {
						jc.setForeground(Color.red);
						jc.setToolTipText(configed.getResourceValue("EditMapPanel.MissingDefaultValue"));

						java.awt.Font gotFont = jc.getFont();
						gotFont = gotFont.deriveFont(Font.BOLD);
						jc.setFont(gotFont);
					} else {
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
						} else
							CellAlternatingColorizer.colorizeSecret(jc);

					}

				}
				return c;
			}

		};

		table.setDefaultRenderer(Object.class, colorized);
		table.setRowHeight(Globals.LINE_HEIGHT);
		table.setShowGrid(true);
		table.setGridColor(Color.white);

		table.addMouseWheelListener(mouseWheelEvent -> {

			int selRow = -1;

			if (table.getSelectedRows() == null || table.getSelectedRows().length == 0) {
				selRow = -1;
			}

			else
				selRow = table.getSelectedRows()[0];

			int diff = mouseWheelEvent.getWheelRotation();

			selRow = selRow + diff;

			if (selRow >= table.getRowCount())
				selRow = table.getRowCount() - 1;

			int startRow = 0;

			if (selRow < startRow)
				selRow = startRow;

			setSelectedRow(selRow);
		});

		jScrollPane = new JScrollPane(table);
		jScrollPane.getViewport().setBackground(Globals.backLightBlue);

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

			for (String x : optionsMap.keySet()) {
				logging.debug(this, " key " + x + " is nullable " + optionsMap.get(x).isNullable());
			}

			modelProducer = new ListModelProducerForVisualDatamap(table, optionsMap, visualdata);
		}

		logging.debug(this, "setEditableMap set modelProducer  == null " + (modelProducer == null));
		if (modelProducer != null) {
			logging.debug(this, "setEditableMap test modelProducer " + modelProducer.getClass());
			logging.debug(this, "setEditableMap test modelProducer " + modelProducer.getClass(0, 0));
		}

		mapTableModel.setModelProducer((ListModelProducerForVisualDatamap) modelProducer);

		if (theCellEditor instanceof SensitiveCellEditor) {

			((SensitiveCellEditor) theCellEditor).setModelProducer(modelProducer);

			((SensitiveCellEditor) theCellEditor).setForbiddenValues(mapTableModel.getShowOnlyValues());

			((SensitiveCellEditor) theCellEditor).re_init();
		}

		editableColumn.setCellEditor(theCellEditor);

	}

	@Override
	public void cancelOldCellEditing() {

		if (theCellEditor != null) // && data != null)
		{
			theCellEditor.cancelCellEditing(); // don't shift the old editing state to a new product

			if (theCellEditor instanceof SensitiveCellEditor)
				((SensitiveCellEditor) theCellEditor).hideListEditor();

		}

	}

	private boolean checkKey(String s) {
		boolean ok = false;

		if (s != null && !s.equals("")) {
			ok = true;

			if (names.indexOf(s) > -1) {
				ok =

						(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(Globals.mainContainer,
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
		if (row > -1)
			initial = (String) table.getValueAt(row, 0);

		FEditText fed = new FEditText(initial, configed.getResourceValue("EditMapPanel.KeyToAdd")) {

			@Override
			protected void commit() {
				super.commit();
				String s = getText();

				if (checkKey(s)) {
					setVisible(false);
					if (classname.equals("java.lang.Boolean"))
						addBooleanProperty(s);
					else {
						if (multiselection)
							addEmptyPropertyMultiSelection(s);
						else
							addEmptyProperty(s);
					}
				}

			}
		};

		fed.setModal(true);
		fed.setSingleLine(true);
		fed.select(0, initial.length());
		fed.setTitle(Globals.APPNAME);
		fed.init(new Dimension(300, 50));
		boolean located = false;

		if (row > -1) {
			try {
				Rectangle rect = table.getCellRect(row, 0, true);
				Point tablePoint = table.getLocationOnScreen();

				fed.setLocation((int) tablePoint.getX() + (int) rect.getX() + 50,
						(int) tablePoint.getY() + (int) rect.getY() + Globals.LINE_HEIGHT);
				located = true;
			} catch (Exception ex) {
				logging.warning(this, "get location error " + ex);
			}
		}
		if (!located)
			fed.centerOn(Globals.mainContainer);

		fed.setVisible(true);
	}

	public void addEmptyProperty(String key) {
		ArrayList val = new ArrayList<>();
		val.add("");
		addProperty(key, val);
		optionsMap.put(key, DefaultListCellOptions.getNewEmptyListCellOptions());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap) modelProducer).setData(optionsMap, mapTableModel.getData());
	}

	public void addEmptyPropertyMultiSelection(String key) {
		ArrayList val = new ArrayList<>();
		val.add("");
		addProperty(key, val);
		optionsMap.put(key, DefaultListCellOptions.getNewEmptyListCellOptionsMultiSelection());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap) modelProducer).setData(optionsMap, mapTableModel.getData());
	}

	public void addBooleanProperty(String key) {
		ArrayList<Object> val = new ArrayList<>();
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

		logging.info(this, " EditMapPanelX instance No " + objectCounter + "::" + " removeProperty for key " + key
				+ " via  handler " + propertyHandler);

		propertyHandler.removeValue(key);

		logging.info(this, " EditMapPanelX instance No " + objectCounter + "::" + " handled removeProperty for key "
				+ key + " options " + optionsMap.get(key));
		logging.info(this, "handled removeProperty for key " + key + " default value  " + defaultsMap.get(key)
				+ " - should be identical with - " + optionsMap.get(key).getDefaultValues());

		names = mapTableModel.getKeys();
		logging.info(this, "removeProperty names left: " + names);

	}

	public void stopEditing() {
		if (table.isEditing()) // we prefer not to cancel cell editing
		{
			table.getCellEditor().stopCellEditing();
		}
	}

	// FocusListener
	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {

		stopEditing();
	}

	protected void setSelectedRow(int row) {
		table.setRowSelectionInterval(row, row);

		showSelectedRow();
	}

	protected void showSelectedRow() {
		int row = table.getSelectedRow();
		if (row != -1)
			table.scrollRectToVisible(table.getCellRect(row, 0, false));
	}

	@Override
	public void setOptionsEditable(boolean b) {
		logging.debug(this, "setOptionsEditable " + b);

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
