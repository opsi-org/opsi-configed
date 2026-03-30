/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.datapanel;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.productpage.TextMarkdownPane;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.ListModelProducer;
import de.uib.configed.gui.share.table.gui.PropertiesCellEditorAndRenderer;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.gui.type.ConfigOption.TYPE;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

// works on a map of pairs of type String - List
public class EditMapPanelX extends DefaultEditMapPanel {
	protected JScrollPane jScrollPane;
	protected JTable table;

	private PropertiesCellEditorAndRenderer propertiesCellEditorAndRenderer;

	private ListModelProducer modelProducer;

	private JMenuItem popupRemoveSpecificEntry;
	private JMenuItem setDefaultValue;

	private JMenuItem multiLineEditingItem;

	private JPopupMenu popupMenu;

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

	public EditMapPanelX(boolean keylistExtendible, boolean entryRemovable) {
		super();

		Logging.debug(this, " created EditMapPanelX", keylistExtendible, ",  ", entryRemovable);
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setEnabled(true);
		ttm.setInitialDelay(Globals.TOOLTIP_INITIAL_DELAY_MS);
		ttm.setDismissDelay(Globals.TOOLTIP_DISMISS_DELAY_MS);
		ttm.setReshowDelay(Globals.TOOLTIP_RESHOW_DELAY_MS);

		buildPanel();
		buildPopupMenu(keylistExtendible, entryRemovable);

		propertyHandler.setMapTableModel(mapTableModel);
	}

	private void buildPopupMenu(boolean keylistExtendible, boolean entryRemovable) {
		popupMenu = createBasicPopup();

		Logging.debug(this, "logPopupElements ", popupMenu.getSubElements().length);
		multiLineEditingItem = new JMenuItem(Configed.getResourceValue("EditMapPanelX.openMultiLineEditor"));
		Icons.addIntellijIconToMenuItem(multiLineEditingItem, "edit");
		multiLineEditingItem.addActionListener(event -> startMultiLineEditing());
		multiLineEditingItem
				.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
						.isGlobalReadOnly());

		if (keylistExtendible) {
			if (popupMenu.getComponentCount() > 0) {
				popupMenu.addSeparator();
			}

			JMenuItem popupItemAddStringListEntry = new JMenuItem(
					Configed.getResourceValue("EditMapPanel.PopupMenu.AddEntry"));
			Icons.addIntellijIconToMenuItem(popupItemAddStringListEntry, "add");
			popupItemAddStringListEntry.addActionListener(actionEvent -> new CreateConfigDialog(this));
			popupItemAddStringListEntry
					.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
							.isGlobalReadOnly());
			popupMenu.add(popupItemAddStringListEntry);

			JMenuItem popupItemDeleteEntry0 = new JMenuItem(defaultPropertyHandler.getRemovalMenuText());
			Icons.addIntellijIconToMenuItem(popupItemDeleteEntry0, "remove");
			popupItemDeleteEntry0.addActionListener(actionEvent -> setPropertyHandler(defaultPropertyHandler));
			popupItemDeleteEntry0
					.setEnabled(!PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
							.isGlobalReadOnly());

			popupMenu.add(popupItemDeleteEntry0);
			// the menu item seems to work only for one menu
		}

		if (entryRemovable) {
			if (popupMenu.getComponentCount() > 0) {
				popupMenu.addSeparator();
			}

			// initialize special property handlers
			AbstractPropertyHandler settingDefaultValuesPropertyHandler = new SettingDefaultValuesHandler();
			settingDefaultValuesPropertyHandler.setMapTableModel(mapTableModel);

			setDefaultValue = new JMenuItem(settingDefaultValuesPropertyHandler.getRemovalMenuText());
			Icons.addIntellijIconToMenuItem(setDefaultValue, "pin");
			setDefaultValue.addActionListener(actionEvent -> setPropertyHandler(settingDefaultValuesPropertyHandler));

			popupMenu.add(setDefaultValue);

			AbstractPropertyHandler removingSpecificValuesPropertyHandler = new RemovingSpecificHandler();
			removingSpecificValuesPropertyHandler.setMapTableModel(mapTableModel);

			popupRemoveSpecificEntry = new JMenuItem(removingSpecificValuesPropertyHandler.getRemovalMenuText());
			Icons.addIntellijIconToMenuItem(popupRemoveSpecificEntry, "remove");
			popupRemoveSpecificEntry
					.addActionListener(actionEvent -> setPropertyHandler(removingSpecificValuesPropertyHandler));

			popupMenu.add(popupRemoveSpecificEntry);
		}
	}

	protected boolean updatePopupMenu() {
		int row = table.getSelectedRow();

		if (row != -1 && modelProducer.isEditable(row)
				&& modelProducer.getSelectionMode(row) == ListSelectionModel.SINGLE_SELECTION) {
			popupMenu.add(multiLineEditingItem);
		} else {
			popupMenu.remove(multiLineEditingItem);
		}

		if (setDefaultValue != null) {
			setDefaultValue.setEnabled(
					row != -1 && !PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
							.isGlobalReadOnly());
		}

		if (popupRemoveSpecificEntry != null) {
			popupRemoveSpecificEntry.setEnabled(
					row != -1 && !PersistenceControllerFactory.getPersistenceController().getDataServices().userRoles
							.isGlobalReadOnly());
		}

		return true;
	}

	public void startMultiLineEditing() {
		int row = table.getSelectedRow();
		if (row != -1) {
			propertiesCellEditorAndRenderer.editMultiValueSingleLine(table, row);
		}
	}

	public void setOriginalMap(Map<String, Object> originalMap) {
		this.originalMap = originalMap;
	}

	private void setPropertyHandler(AbstractPropertyHandler newPropertyHandler) {
		Logging.info(this, "popupItemDeleteEntry action");
		if (table.getSelectedRowCount() == 0) {
			JOptionPane.showMessageDialog(ConfigedMain.getMainFrame(),
					Configed.getResourceValue("EditMapPanel.RowToRemoveMustBeSelected"),
					Configed.getResourceValue("error"), JOptionPane.ERROR_MESSAGE);
		} else if (names != null) {
			propertyHandler = newPropertyHandler;

			removeProperty(names.get(table.getSelectedRow()));
		} else {
			Logging.warning(this, "names list is null, so cannot remove property with handler ",
					newPropertyHandler.getClass().getName());
		}
	}

	protected JPopupMenu createBasicPopup() {
		Logging.info(this, "(EditMapPanelX) definePopup");
		return PopupMenuTrait.createAndBindJPopupMenu(table, Map.of(), event -> updatePopupMenu());
	}

	private void buildPanel() {
		table = new JTable(mapTableModel) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
				Component c = super.prepareRenderer(renderer, row, col);
				if (!showToolTip) {
					return c;
				}

				prepareRendererForJTable((JComponent) c, table, row, col);
				return c;
			}

			// We need this so that the keybinding for Ctrl+S is not processed by this table
			// but by the main frame, which will trigger the save action.
			// This way, we will not edit the cell when pressing Ctrl+S.
			@Override
			public boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
				if (ks.getKeyCode() == KeyEvent.VK_S && e.isControlDown()) {
					// if we return false here, the keybinding will be processed by the
					// parent component, which is the main frame, and trigger the save action
					return false;
				}

				return super.processKeyBinding(ks, e, condition, pressed);
			}
		};

		table.setDefaultRenderer(Object.class, new ColorTableCellRenderer());
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setFillsViewportHeight(true);

		propertiesCellEditorAndRenderer = new PropertiesCellEditorAndRenderer();

		TableColumn editableColumn = table.getColumnModel().getColumn(1);
		editableColumn.setCellRenderer(propertiesCellEditorAndRenderer);
		editableColumn.setCellEditor(propertiesCellEditorAndRenderer);

		jScrollPane = new JScrollPane(table);

		setLayout(new MigLayout("insets 0, fill", "", "[]0"));
		add(jScrollPane, "grow, hmin 0");
	}

	protected void prepareRendererForJTable(JComponent jComponent, JTable table, int row, int col) {
		jComponent.setToolTipText(createTooltipForPropertyName(names.get(row), defaultsMap, descriptionsMap, null));

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
		} else if (!defaultValue.equals(table.getValueAt(row, 1))
				|| (originalMap != null && originalMap.containsKey(names.get(row)))) {
			jComponent.setFont(jComponent.getFont().deriveFont(Font.BOLD));
		} else {
			// Do nothing when default equals real value
		}

		if (col == 1 && jComponent instanceof JLabel jLabel
				&& PropertiesCellEditorAndRenderer.isKeyForSecretValue((String) mapTableModel.getValueAt(row, 0))) {
			jLabel.setText(Globals.STARRED_STRING);
		}
	}

	protected static String createTooltipForPropertyName(String propertyName, Map<String, Object> defaultsMap,
			Map<String, String> descriptionsMap, String additionalTooltipText) {
		if (propertyName == null) {
			return "";
		}

		StringBuilder tooltip = new StringBuilder();

		if (defaultsMap != null && defaultsMap.get(propertyName) != null) {
			if (additionalTooltipText != null && !additionalTooltipText.isEmpty()) {
				tooltip.append("default (" + additionalTooltipText + "): ");
			} else {
				tooltip.append("default: ");
			}

			if (PropertiesCellEditorAndRenderer.isKeyForSecretValue(propertyName)) {
				tooltip.append(Globals.STARRED_STRING);
			} else {
				tooltip.append(defaultsMap.get(propertyName));
			}
		}

		if (descriptionsMap != null && descriptionsMap.get(propertyName) != null) {
			tooltip.append(TextMarkdownPane.parseMarkdown(descriptionsMap.get(propertyName)));
		}

		if (tooltip.length() > 200) {
			Logging.debug("tooltip length is ", tooltip.length());
			tooltip.insert(0, "<div style='width: 500px'>");
			tooltip.append("</div>");
		}

		return "<html>" + tooltip + "</html>";
	}

	@Override
	public void init() {
		setEditableMap(null, null);
	}

	@Override
	public void setEditableMap(Map<String, Object> visualdata, Map<String, ConfigOption> optionsMap) {
		propertiesCellEditorAndRenderer.stopCellEditing();

		if (optionsMap != null) {
			modelProducer = new ListModelProducerForVisualDatamap(table, optionsMap, visualdata);
		}

		// We need to call this method after creating the modelProducer since
		// it will change the map visualdata
		super.setEditableMap(visualdata, optionsMap);

		Logging.debug(this, "setEditableMap set modelProducer  == null ", modelProducer == null);

		propertiesCellEditorAndRenderer.setModelProducer(modelProducer);
	}

	private boolean checkKey(String s) {
		if (s == null || s.isEmpty()) {
			return false;
		}

		if (!names.contains(s)) {
			return true;
		}
		return JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("EditMapPanelX.entryAlreadyExists"),
				Configed.getResourceValue("EditMapPanelX.titleEntryAlreadyExists"), JOptionPane.OK_CANCEL_OPTION);
	}

	public boolean addEntry(String configName, String description, boolean bool, boolean multivalue, boolean editable,
			List<?> defaultValues, List<?> possibleValues) {
		if (!checkKey(configName)) {
			return false;
		}

		Logging.info(this, "we create configuration entry ", configName, " with values bool, multivalue, editable",
				bool, multivalue, editable);

		ConfigOption configOption = ConfigOption.createConfigOption(description,
				bool ? TYPE.BOOL_CONFIG : TYPE.UNICODE_CONFIG, editable, multivalue, defaultValues, possibleValues);

		mapTableModel.addConfigEntry(configName, defaultValues, possibleValues);
		names = mapTableModel.getKeys();

		optionsMap.put(configName, configOption);
		mapTableModel.setMap(mapTableModel.getData());

		return true;
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
