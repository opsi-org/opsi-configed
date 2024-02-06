/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.datapanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.DefaultCellEditor;
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
import javax.swing.text.JTextComponent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditText;
import de.uib.utilities.swing.PopupMenuTrait;
import de.uib.utilities.table.DefaultListCellOptions;
import de.uib.utilities.table.ListCellOptions;
import de.uib.utilities.table.ListModelProducer;
import de.uib.utilities.table.gui.ColorTableCellRenderer;
import de.uib.utilities.table.gui.SensitiveCellEditor;
import utils.PopupMouseListener;
import utils.Utils;

// works on a map of pairs of type String - List
public class EditMapPanelX extends DefaultEditMapPanel implements FocusListener {
	private static int objectCounter;
	protected JScrollPane jScrollPane;
	protected JTable table;

	private TableColumn editableColumn;
	private TableCellEditor theCellEditor;
	private JComboBox<?> editorfield;

	private ListModelProducer<String> modelProducer;

	private JMenuItem popupItemDeleteEntry0;
	private JMenuItem popupItemDeleteEntry1;
	private JMenuItem popupItemDeleteEntry2;
	private JMenuItem popupItemAddStringListEntry;
	private JMenuItem popupItemAddBooleanListEntry;

	private ToolTipManager ttm;

	private Map<String, Object> originalMap;

	private class RemovingSpecificHandler extends AbstractPropertyHandler {
		@Override
		public void removeValue(String key) {
			Logging.info(this, "removing specific value for key " + key);
			// signal removal of entry to persistence modul
			mapTableModel.removeEntry(key);

			// set for this session to default, without storing the value separately)
			mapTableModel.addEntry(key, defaultsMap.get(key), false);
			if (originalMap != null) {
				originalMap.remove(key);
			}
		}

		@Override
		public String getRemovalMenuText() {
			super.getRemovalMenuText();
			return Configed.getResourceValue("EditMapPanelX.PopupMenu.RemoveSpecificValue");
		}
	}

	private class SettingDefaultValuesHandler extends AbstractPropertyHandler {
		@Override
		public void removeValue(String key) {
			Logging.info(this, "setting default value for key " + key);
			// signal removal of entry to persistence modul
			mapTableModel.removeEntry(key);

			// set for this session to default, without storing the value separately)
			mapTableModel.addEntry(key, defaultsMap.get(key), true);
			if (originalMap != null) {
				originalMap.put(key, defaultsMap.get(key));
			}
		}

		@Override
		public String getRemovalMenuText() {
			super.getRemovalMenuText();
			return Configed.getResourceValue("EditMapPanelX.PopupMenu.SetSpecificValueToDefault");
		}
	}

	private final AbstractPropertyHandler removingSpecificValuesPropertyHandler;
	private final AbstractPropertyHandler settingDefaultValuesPropertyHandler;

	public EditMapPanelX(TableCellRenderer tableCellRenderer, boolean keylistExtendible, boolean entryRemovable) {
		this(tableCellRenderer, keylistExtendible, entryRemovable, false);
	}

	public EditMapPanelX(TableCellRenderer tableCellRenderer, boolean keylistExtendible, boolean entryRemovable,
			boolean reloadable) {
		super(tableCellRenderer, keylistExtendible, entryRemovable, reloadable);
		objectCounter++;

		Logging.debug(this.getClass(), " created EditMapPanelX instance No " + objectCounter + "::" + keylistExtendible
				+ ",  " + entryRemovable + ",  " + reloadable);
		ttm = ToolTipManager.sharedInstance();
		ttm.setEnabled(true);
		ttm.setInitialDelay(Globals.TOOLTIP_INITIAL_DELAY_MS);
		ttm.setDismissDelay(Globals.TOOLTIP_DISMISS_DELAY_MS);
		ttm.setReshowDelay(Globals.TOOLTIP_RESHOW_DELAY_MS);

		buildPanel();

		editableColumn = table.getColumnModel().getColumn(1);

		editorfield = new JComboBox<>();
		editorfield.setEditable(true);
		theCellEditor = new DefaultCellEditor(editorfield);

		if (tableCellRenderer == null) {
			editableColumn.setCellRenderer(new ColorTableCellRenderer());
		} else {
			editableColumn.setCellRenderer(tableCellRenderer);
		}

		popupEditOptions = definePopup();
		popupNoEditOptions = definePopup();
		popupmenuAtRow = popupEditOptions;

		super.logPopupElements();

		MouseListener popupNoEditOptionsListener = new PopupMouseListener(popupNoEditOptions);
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
				popupItemAddStringListEntry = new JMenuItem(
						Configed.getResourceValue("EditMapPanel.PopupMenu.AddEntrySingleSelection"));
				popupEditOptions.add(popupItemAddStringListEntry);
				popupItemAddStringListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.String", false));

				popupItemAddStringListEntry = new JMenuItem(
						Configed.getResourceValue("EditMapPanel.PopupMenu.AddEntryMultiSelection"));
				popupEditOptions.add(popupItemAddStringListEntry);
				popupItemAddStringListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.String", true));

				popupItemAddBooleanListEntry = new JMenuItem(
						Configed.getResourceValue("EditMapPanel.PopupMenu.AddBooleanEntry"));
				popupEditOptions.add(popupItemAddBooleanListEntry);
				popupItemAddBooleanListEntry.addActionListener(actionEvent -> addEntryFor("java.lang.Boolean"));
			}

			if (entryRemovable) {
				popupItemDeleteEntry0 = new JMenuItem(defaultPropertyHandler.getRemovalMenuText());
				popupItemDeleteEntry0.addActionListener(actionEvent -> deleteEntry());

				popupEditOptions.add(popupItemDeleteEntry0);
				// the menu item seems to work only for one menu

				popupItemDeleteEntry1 = new JMenuItem(removingSpecificValuesPropertyHandler.getRemovalMenuText(),
						Utils.createImageIcon("images/no-value.png", ""));
				popupItemDeleteEntry1.addActionListener(actionEvent -> deleteSpecificEntry());

				popupNoEditOptions.add(popupItemDeleteEntry1);

				popupItemDeleteEntry2 = new JMenuItem(settingDefaultValuesPropertyHandler.getRemovalMenuText(),
						Utils.createImageIcon("images/fixed-value.png", ""));
				popupItemDeleteEntry2.addActionListener(actionEvent -> removeDefaultAsSpecificEntry());

				popupNoEditOptions.add(popupItemDeleteEntry2);
			}
		}

		propertyHandler.setMapTableModel(mapTableModel);
	}

	public void setOriginalMap(Map<String, Object> originalMap) {
		this.originalMap = originalMap;
	}

	private void deleteEntry() {
		Logging.info(this, "popupItemDeleteEntry action");
		if (table.getSelectedRowCount() == 0) {
			FTextArea fAsk = new FTextArea(ConfigedMain.getMainFrame(), Globals.APPNAME,
					Configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"), true,
					new String[] { Configed.getResourceValue("buttonClose") }, 200, 200);

			fAsk.setVisible(true);
		} else if (names != null) {
			propertyHandler = defaultPropertyHandler;

			removeProperty(names.get(table.getSelectedRow()));
		} else {
			Logging.warning(this, "names list is null, so cannot remove property in deleteEntry");
		}
	}

	private void deleteSpecificEntry() {
		Logging.info(this, "popupItemDeleteEntry action");
		if (table.getSelectedRowCount() == 0) {
			FTextArea fAsk = new FTextArea(ConfigedMain.getMainFrame(), Globals.APPNAME,
					Configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"), true,
					new String[] { Configed.getResourceValue("buttonClose") }, 200, 200);

			fAsk.setVisible(true);
		} else if (names != null) {
			propertyHandler = removingSpecificValuesPropertyHandler;

			removeProperty(names.get(table.getSelectedRow()));
		} else {
			Logging.warning(this, "names list is null, so cannot remove property in deleteSpecificEntry");
		}
	}

	private void removeDefaultAsSpecificEntry() {
		Logging.info(this, "popupItemDeleteEntry action");
		if (table.getSelectedRowCount() == 0) {
			FTextArea fAsk = new FTextArea(ConfigedMain.getMainFrame(), Globals.APPNAME,
					Configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"), true,
					new String[] { Configed.getResourceValue("buttonClose") }, 200, 200);

			fAsk.setVisible(true);
		} else if (names != null) {
			propertyHandler = settingDefaultValuesPropertyHandler;

			removeProperty(names.get(table.getSelectedRow()));
		} else {
			Logging.warning(this, "names list is null, so cannot remove property in removeDefaultAsSpecificEntry");
		}
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
						ConfigedMain.getMainFrame().activateLoadingCursor();
						actor.reloadData();
						ConfigedMain.getMainFrame().deactivateLoadingCursor();
					}
				}
			};
		}

		return result;
	}

	@Override
	protected void buildPanel() {
		setLayout(new BorderLayout());

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

						if (Utils.isKeyForSecretValue(propertyName)) {
							tooltip = tooltip + Globals.STARRED_STRING;
						} else {
							tooltip = tooltip + defaultsMap.get(propertyName);
						}
					}

					if (propertyName != null && descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
						tooltip = tooltip + "<br/><br/>" + descriptionsMap.get(propertyName);
					}

					jc.setToolTipText("<html>" + tooltip + "</html>");

					// check equals with default

					Object defaultValue;

					if (defaultsMap == null) {
						Logging.warning(this, "no default values available, defaultsMap is null");
					} else if ((defaultValue = defaultsMap.get(table.getValueAt(rowIndex, 0))) == null) {
						Logging.warning(this, "no default Value found");

						jc.setForeground(Globals.OPSI_ERROR);

						jc.setToolTipText(Configed.getResourceValue("EditMapPanel.MissingDefaultValue"));

						jc.setFont(jc.getFont().deriveFont(Font.BOLD));
					} else {
						Object gotValue = table.getValueAt(rowIndex, 1);
						if (!defaultValue.equals(gotValue)) {
							jc.setFont(jc.getFont().deriveFont(Font.BOLD));
						}
					}

					if (vColIndex == 1 && Utils.isKeyForSecretValue((String) mapTableModel.getValueAt(rowIndex, 0))) {
						if (jc instanceof JLabel) {
							((JLabel) jc).setText(Globals.STARRED_STRING);
						} else if (jc instanceof JTextComponent) {
							((JTextComponent) jc).setText(Globals.STARRED_STRING);
						} else {
							// Do nothing
						}
					}
				}
				return c;
			}
		};

		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		table.addMouseWheelListener(mouseWheelEvent -> reactToMouseWheelEvent(mouseWheelEvent.getWheelRotation()));

		jScrollPane = new JScrollPane(table);

		add(jScrollPane, BorderLayout.CENTER);
	}

	protected void reactToMouseWheelEvent(int wheelRotation) {
		int selRow = -1;

		if (table.getSelectedRows() == null || table.getSelectedRows().length == 0) {
			selRow = -1;
		} else {
			selRow = table.getSelectedRows()[0];
		}

		selRow = selRow + wheelRotation;

		if (selRow >= table.getRowCount()) {
			selRow = table.getRowCount() - 1;
		}

		int startRow = 0;

		if (selRow < startRow) {
			selRow = startRow;
		}

		setSelectedRow(selRow);
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

			modelProducer = new ListModelProducerForVisualDatamap<>(table, optionsMap, visualdata);
		}

		Logging.debug(this, "setEditableMap set modelProducer  == null " + (modelProducer == null));
		if (modelProducer != null) {
			Logging.debug(this, "setEditableMap test modelProducer " + modelProducer.getClass());
		}

		mapTableModel.setModelProducer((ListModelProducerForVisualDatamap<String>) modelProducer);

		if (theCellEditor instanceof SensitiveCellEditor) {
			((SensitiveCellEditor) theCellEditor).setModelProducer(modelProducer);
			((SensitiveCellEditor) theCellEditor).reInit();
		}

		editableColumn.setCellEditor(theCellEditor);
	}

	public void cancelOldCellEditing() {
		if (theCellEditor instanceof SensitiveCellEditor) {
			((SensitiveCellEditor) theCellEditor).hideListEditor();
		}
	}

	private boolean checkKey(String s) {
		boolean ok = false;

		if (s != null && !s.isEmpty()) {
			ok = true;

			if (names.contains(s)) {
				ok = JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
						Configed.getResourceValue("EditMapPanelX.entryAlreadyExists"), Globals.APPNAME,
						JOptionPane.OK_CANCEL_OPTION);
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
				String s = getText().strip();

				if (checkKey(s)) {
					setVisible(false);
					if ("java.lang.Boolean".equals(classname)) {
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

	private void addEmptyProperty(String key) {
		List<String> val = new ArrayList<>();
		val.add("");
		addProperty(key, val);
		optionsMap.put(key, DefaultListCellOptions.getNewEmptyListCellOptions());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap<String>) modelProducer).setData(optionsMap, mapTableModel.getData());
	}

	private void addEmptyPropertyMultiSelection(String key) {
		List<String> val = new ArrayList<>();
		val.add("");
		addProperty(key, val);
		optionsMap.put(key, DefaultListCellOptions.getNewEmptyListCellOptionsMultiSelection());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap<String>) modelProducer).setData(optionsMap, mapTableModel.getData());
	}

	private void addBooleanProperty(String key) {
		List<Object> val = new ArrayList<>();
		val.add(false);
		addProperty(key, val);
		optionsMap.put(key, DefaultListCellOptions.getNewBooleanListCellOptions());
		mapTableModel.setMap(mapTableModel.getData());
		((ListModelProducerForVisualDatamap<String>) modelProducer).setData(optionsMap, mapTableModel.getData());
	}

	/**
	 * adding an entry to the table model and, finally, to the table
	 *
	 * @param String key
	 * @param Object value (if null then an empty String is the value)
	 */
	private final void addProperty(String key, Object newval) {
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

		Object defaultValue = defaultsMap.get(key);

		if (defaultValue == null) {
			Logging.info(this, "there was no default value for " + key);
		} else {
			Logging.info(this, "handled removeProperty for key " + key + " default value  " + defaultValue
					+ " - should be identical with - " + optionsMap.get(key).getDefaultValues());
		}

		names = mapTableModel.getKeys();
		Logging.info(this, "removeProperty names left: " + names);
	}

	private void stopEditing() {
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

	private void setSelectedRow(int row) {
		table.setRowSelectionInterval(row, row);

		showSelectedRow();
	}

	private void showSelectedRow() {
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

		MouseListener popupListener = new PopupMouseListener(popupmenuAtRow);
		table.addMouseListener(popupListener);
		jScrollPane.getViewport().addMouseListener(popupListener);
	}
}
