/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.datapanel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FTextArea;
import de.uib.configed.type.ConfigOption;
import de.uib.configed.type.ConfigOption.TYPE;
import de.uib.utils.Icons;
import de.uib.utils.PopupMouseListener;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.PopupMenuTrait;
import de.uib.utils.table.gui.ColorTableCellRenderer;
import de.uib.utils.table.gui.PropertiesCellEditorAndRenderer;

// works on a map of pairs of type String - List
public class EditMapPanelX extends DefaultEditMapPanel {
	protected JScrollPane jScrollPane;
	protected JTable table;

	private TableColumn editableColumn;
	private PropertiesCellEditorAndRenderer propertiesCellEditorAndRenderer;

	private ListModelProducerForVisualDatamap modelProducer;

	private JMenuItem popupItemDeleteEntry0;
	private JMenuItem popupItemDeleteEntry1;
	private JMenuItem popupItemDeleteEntry2;
	private JMenuItem popupItemAddStringListEntry;

	protected Map<String, Object> originalMap;

	private class RemovingSpecificHandler extends AbstractPropertyHandler {
		@Override
		public void removeValue(String key) {
			Logging.info(this, "removing specific value for key ", key);
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
			return Configed.getResourceValue("EditMapPanelX.PopupMenu.RemoveSpecificValue");
		}
	}

	private class SettingDefaultValuesHandler extends AbstractPropertyHandler {
		@Override
		public void removeValue(String key) {
			Logging.info(this, "setting default value for key ", key);
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
			return Configed.getResourceValue("EditMapPanelX.PopupMenu.SetSpecificValueToDefault");
		}
	}

	private AbstractPropertyHandler removingSpecificValuesPropertyHandler;
	private AbstractPropertyHandler settingDefaultValuesPropertyHandler;

	public EditMapPanelX(boolean keylistExtendible, boolean entryRemovable, boolean reloadable) {
		super(reloadable);

		Logging.debug(this, " created EditMapPanelX", keylistExtendible, ",  ", entryRemovable, ",  ", reloadable);
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setEnabled(true);
		ttm.setInitialDelay(Globals.TOOLTIP_INITIAL_DELAY_MS);
		ttm.setDismissDelay(Globals.TOOLTIP_DISMISS_DELAY_MS);
		ttm.setReshowDelay(Globals.TOOLTIP_RESHOW_DELAY_MS);

		buildPanel();

		propertiesCellEditorAndRenderer = new PropertiesCellEditorAndRenderer();

		editableColumn = table.getColumnModel().getColumn(1);

		editableColumn.setCellRenderer(propertiesCellEditorAndRenderer);
		editableColumn.setCellEditor(propertiesCellEditorAndRenderer);

		popupMenu = definePopup();

		super.logPopupElements();

		MouseListener popupNoEditOptionsListener = new PopupMouseListener(popupMenu);
		table.addMouseListener(popupNoEditOptionsListener);
		jScrollPane.getViewport().addMouseListener(popupNoEditOptionsListener);

		if (keylistExtendible) {
			if (popupMenu.getComponentCount() > 0) {
				popupMenu.addSeparator();
			}

			popupItemAddStringListEntry = new JMenuItem(Configed.getResourceValue("EditMapPanel.PopupMenu.AddEntry"));
			Icons.addIntellijIconToMenuItem(popupItemAddStringListEntry, "add");
			popupItemAddStringListEntry.addActionListener(actionEvent -> new CreateConfigDialog(this));
			popupMenu.add(popupItemAddStringListEntry);

			popupItemDeleteEntry0 = new JMenuItem(defaultPropertyHandler.getRemovalMenuText());
			Icons.addIntellijIconToMenuItem(popupItemDeleteEntry0, "remove");
			popupItemDeleteEntry0.addActionListener(actionEvent -> deleteConfigurationEntry());

			popupMenu.add(popupItemDeleteEntry0);
			// the menu item seems to work only for one menu
		}

		if (entryRemovable) {
			if (popupMenu.getComponentCount() > 0) {
				popupMenu.addSeparator();
			}

			// initialize special property handlers
			removingSpecificValuesPropertyHandler = new RemovingSpecificHandler();
			removingSpecificValuesPropertyHandler.setMapTableModel(mapTableModel);

			settingDefaultValuesPropertyHandler = new SettingDefaultValuesHandler();
			settingDefaultValuesPropertyHandler.setMapTableModel(mapTableModel);

			popupItemDeleteEntry1 = new JMenuItem(removingSpecificValuesPropertyHandler.getRemovalMenuText());
			Icons.addIntellijIconToMenuItem(popupItemDeleteEntry1, "remove");
			popupItemDeleteEntry1.addActionListener(actionEvent -> deleteSpecificEntry());

			popupMenu.add(popupItemDeleteEntry1);

			popupItemDeleteEntry2 = new JMenuItem(settingDefaultValuesPropertyHandler.getRemovalMenuText());
			Icons.addIntellijIconToMenuItem(popupItemDeleteEntry2, "locked");
			popupItemDeleteEntry2.addActionListener(actionEvent -> removeDefaultAsSpecificEntry());

			popupMenu.add(popupItemDeleteEntry2);
		}

		propertyHandler.setMapTableModel(mapTableModel);
	}

	public void setOriginalMap(Map<String, Object> originalMap) {
		this.originalMap = originalMap;
	}

	private void deleteConfigurationEntry() {
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

		if (reloadable) {
			return new PopupMenuTrait(new Integer[] { PopupMenuTrait.POPUP_RELOAD }) {
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
		} else {
			return new JPopupMenu();
		}
	}

	protected void buildPanel() {
		setLayout(new BorderLayout());

		table = new JTable(mapTableModel) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				Component c = super.prepareRenderer(renderer, row, col);
				if (!showToolTip) {
					return c;
				}

				prepareRendererForJTable((JComponent) c, row, col);
				return c;
			}
		};

		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		jScrollPane = new JScrollPane(table);

		add(jScrollPane, BorderLayout.CENTER);
	}

	private void prepareRendererForJTable(JComponent jComponent, int row, int col) {
		jComponent.setToolTipText(generateTooltip(row));

		// check equals with default
		Object defaultValue;

		jComponent.setFont(jComponent.getFont().deriveFont(Font.PLAIN));

		if (defaultsMap == null) {
			Logging.warning(this, "no default values available, defaultsMap is null");
		} else if ((defaultValue = defaultsMap.get(table.getValueAt(row, 0))) == null) {
			Logging.warning(this, "no default Value found");

			jComponent.setForeground(Globals.OPSI_ERROR);

			jComponent.setToolTipText(Configed.getResourceValue("EditMapPanel.MissingDefaultValue"));

			jComponent.setFont(jComponent.getFont().deriveFont(Font.BOLD));
		} else if (!defaultValue.equals(table.getValueAt(row, 1))) {
			jComponent.setFont(jComponent.getFont().deriveFont(Font.BOLD));
		} else {
			// Do nothing when default equals real value
		}

		if (col == 1 && jComponent instanceof JLabel jLabel
				&& Utils.isKeyForSecretValue((String) mapTableModel.getValueAt(row, 0))) {
			jLabel.setText(Globals.STARRED_STRING);
		}
	}

	private String generateTooltip(int row) {
		String propertyName = names.get(row);

		StringBuilder tooltip = new StringBuilder();

		if (propertyName != null) {
			if (defaultsMap != null && defaultsMap.get(propertyName) != null) {
				tooltip.append("default: ");

				if (Utils.isKeyForSecretValue(propertyName)) {
					tooltip.append(Globals.STARRED_STRING);
				} else {
					tooltip.append(defaultsMap.get(propertyName));
				}
			}

			if (descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
				// We want to have new lines in the html form "<br>" so they'll be shown correctly in the tooltip
				tooltip.append("<br/><br/>").append(descriptionsMap.get(propertyName).replace("\n", "<br>"));
			}
		}

		return "<html>" + tooltip + "</html>";
	}

	@Override
	public void init() {
		setEditableMap(null, null);
	}

	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ConfigOption> optionsMap) {
		if (optionsMap != null) {
			modelProducer = new ListModelProducerForVisualDatamap(table, optionsMap, visualdata);
			mapTableModel.setModelProducer(modelProducer);
		}

		// We need to call this method after creating the modelProducer since
		// it will change the map visualdata
		super.setEditableMap(visualdata, optionsMap);

		Logging.debug(this, "setEditableMap set modelProducer  == null ", modelProducer == null);

		propertiesCellEditorAndRenderer.setModelProducer(modelProducer);
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

	public void addEntry(String configName, String description, boolean bool, boolean multivalue, boolean editable,
			List<?> defaultValues, List<?> possibleValues) {
		if (!checkKey(configName)) {
			return;
		}

		Logging.info(this, "we create configuration entry ", configName, " with values bool, multivalue, editable",
				bool, multivalue, editable);

		ConfigOption configOption = ConfigOption.createConfigOption(description,
				bool ? TYPE.BOOL_CONFIG : TYPE.UNICODE_CONFIG, editable, multivalue, defaultValues, possibleValues);

		mapTableModel.addConfigEntry(configName, defaultValues, possibleValues);
		names = mapTableModel.getKeys();

		optionsMap.put(configName, configOption);
		mapTableModel.setMap(mapTableModel.getData());
		modelProducer.setData(optionsMap, mapTableModel.getData());
	}

	/**
	 * deleting an entry
	 *
	 * @param String key - the key to delete
	 */
	public void removeProperty(String key) {
		Logging.info(this, " EditMapPanelX, removeProperty for key ", key, " via  handler ", propertyHandler);

		propertyHandler.removeValue(key);

		Logging.info(this, " EditMapPanelX, handled removeProperty for key ", key, " options ", optionsMap.get(key));

		Object defaultValue = defaultsMap.get(key);

		if (defaultValue == null) {
			Logging.info(this, "there was no default value for ", key);
		} else {
			Logging.info(this, "handled removeProperty for key ", key, " default value  ", defaultValue,
					" - should be identical with - ", optionsMap.get(key).getDefaultValues());
		}

		names = mapTableModel.getKeys();
		Logging.info(this, "removeProperty names left: ", names);
	}
}
