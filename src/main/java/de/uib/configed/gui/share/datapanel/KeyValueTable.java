/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.share.datapanel;

import java.awt.Component;
import java.awt.Font;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;

import de.uib.configed.core.domain.datachanges.UpdateCollection;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.infrastructure.POJOReMapper;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.productpage.TextMarkdownPane;
import de.uib.configed.gui.features.searchpane.SearchPaneComponent;
import de.uib.configed.gui.features.searchpane.view.SearchTargetModelFromTable;
import de.uib.configed.gui.features.table.GenericTableViewComponent;
import de.uib.configed.gui.features.table.GenericTableViewEffect;
import de.uib.configed.gui.features.table.GenericTableViewEffect.AddRow;
import de.uib.configed.gui.features.table.GenericTableViewEffect.CellEdited;
import de.uib.configed.gui.features.table.GenericTableViewEffect.DeleteRows;
import de.uib.configed.gui.features.table.GenericTableViewModel;
import de.uib.configed.gui.features.table.GenericTableViewMsg;
import de.uib.configed.gui.features.table.RowData;
import de.uib.configed.gui.features.table.RowData.RowState;
import de.uib.configed.gui.features.table.TableColumnConfig;
import de.uib.configed.gui.features.table.TableConfig;
import de.uib.configed.gui.share.PopupMouseListener;
import de.uib.configed.gui.share.icons.Icons;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.ColorTableCellRenderer;
import de.uib.configed.gui.share.table.gui.ListModelProducer;
import de.uib.configed.gui.share.table.gui.PropertiesCellEditorAndRenderer;
import de.uib.configed.gui.type.ConfigOption;
import de.uib.configed.gui.type.ConfigOption.TYPE;
import de.uib.configed.share.AbstractDataChangedKeeper;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

// works on a map of pairs of type String - List
public class KeyValueTable extends JPanel {
	protected JScrollPane jScrollPane;

	private PropertiesCellEditorAndRenderer propertiesCellEditorAndRenderer;

	private ListModelProducer modelProducer;

	private JMenuItem popupRemoveSpecificEntry;
	private JMenuItem setDefaultValue;

	private JMenuItem multiLineEditingItem;

	private JPopupMenu popupMenu;

	protected Map<String, Object> originalMap;

	protected GenericTableViewComponent tableView;

	private List<AbstractDataChangedKeeper> keepers = new ArrayList<>();
	private UpdateCollection updateCollection;
	private Map<String, Object> changes = new HashMap<>();
	private List<String> keys;
	private Map<String, Object> data;

	private boolean pinnedProperty;
	private boolean showToolTip = true;

	protected Map<String, ConfigOption> optionsMap;
	protected Map<String, Object> defaultsMap;
	protected Map<String, String> descriptionsMap;
	private Collection<Map<String, Object>> storeData;

	private Set<String> keysOfReadOnlyEntries;
	private Function<String, Boolean> isEditable;

	private KeyValueRowDiffStrategy diffStrategy;

	public KeyValueTable(boolean keylistExtendible, boolean entryRemovable) {
		this(keylistExtendible, entryRemovable, false);
	}

	public KeyValueTable(boolean keylistExtendible, boolean entryRemovable, boolean includeSearchPane) {
		super();

		Logging.debug(this, " created EditMapPanelX", keylistExtendible, ",  ", entryRemovable);
		ToolTipManager ttm = ToolTipManager.sharedInstance();
		ttm.setEnabled(true);
		ttm.setInitialDelay(Globals.TOOLTIP_INITIAL_DELAY_MS);
		ttm.setDismissDelay(Globals.TOOLTIP_DISMISS_DELAY_MS);
		ttm.setReshowDelay(Globals.TOOLTIP_RESHOW_DELAY_MS);

		buildPanel(keylistExtendible, entryRemovable, includeSearchPane);
	}

	private void buildPanel(boolean keylistExtendible, boolean entryRemovable, boolean includeSearchPane) {
		propertiesCellEditorAndRenderer = new PropertiesCellEditorAndRenderer();

		TableConfig config = TableConfig.builder().fillViewportHeight(true)
				.defauTableCellRenderer(new ColorTableCellRenderer()).selectionMode(ListSelectionModel.SINGLE_SELECTION)
				.autoCreateRowSorter(false).build();

		List<TableColumnConfig> columns = List.of(
				TableColumnConfig.builder().key("key")
						.header(Configed.getResourceValue("EditMapPanel.ColumnHeaderName")).editable(false)
						.prefferedWidth(150).build(),
				TableColumnConfig.builder().key("value")
						.header(Configed.getResourceValue("EditMapPanel.ColumnHeaderValue")).editable(true)
						.renderer(propertiesCellEditorAndRenderer).editor(propertiesCellEditorAndRenderer).build());

		diffStrategy = new KeyValueRowDiffStrategy(defaultsMap, originalMap, pinnedProperty);
		GenericTableViewModel initialModel = GenericTableViewModel.builder().columns(columns).tableConfig(config)
				.diffStrategy(diffStrategy).allowMultipleSelection(false).isDirty(false).keyValueTable(true).build();

		tableView = new GenericTableViewComponent(initialModel, this::handleEffect,
				() -> new PopupMouseListener(buildPopupMenu(keylistExtendible, entryRemovable), e -> updatePopupMenu()),
				(Component component, int row, int col) -> {
					if (!showToolTip) {
						return;
					}
					prepareRendererForJTable((JComponent) component, row, col);
				});
		tableView.setIsCellEditable(
				(Integer row) -> (keysOfReadOnlyEntries == null || !keysOfReadOnlyEntries.contains(keys.get(row)))
						&& (isEditable == null || isEditable.apply(keys.get(row))));
		JComponent component = tableView.initUI();

		this.setLayout(new MigLayout("insets 0, fillx, wrap 1", "[grow]", "[grow]0"));

		if (includeSearchPane) {
			this.setLayout(new MigLayout("insets 0, fillx, wrap 1", "[grow]", "[]" + Globals.MIN_GAP_SIZE + "[grow]0"));

			SearchPaneComponent searchPane = SearchPaneComponent.builder()
					.targetModel(new SearchTargetModelFromTable(tableView.getTable())).component(component).build();
			this.add(searchPane.initUI(), "growx, hmin 0");
		}

		this.add(component, "grow, push, hmin 0");
	}

	private Runnable handleEffect(GenericTableViewEffect effect) {
		return switch (effect) {
		case CellEdited(int rowIdx, _, Object newValue) -> () -> handleCellEdited(rowIdx, newValue);
		case AddRow(Map<String, Object> newRowData) -> () -> handleAddRow(newRowData);
		case DeleteRows(List<RowData> deletedRows) -> () -> handleDeleteRows(deletedRows);
		default -> null;
		};
	}

	private void handleCellEdited(int row, Object newValue) {
		RowData rowData = tableView.getRowByModelIndex(row);
		String key = rowData.getValue("key", String.class);

		if (!pinnedProperty) {
			Map<String, Object> map = POJOReMapper.remap(newValue);

			key = (String) map.get("key");
			Object value = map.get("value");

			changes.put((String) map.get("key"), map.get("value"));
			saveToStore(key, value);
		} else {
			saveToStore(key, defaultsMap.get(key));
		}

		notifyOfChanges();
	}

	private void handleAddRow(Map<String, Object> data) {
		for (Map.Entry<String, Object> entry : data.entrySet()) {
			changes.put(entry.getKey(), entry.getValue());
		}
		notifyOfChanges();
	}

	private void handleDeleteRows(List<RowData> deletedRows) {
		for (RowData rowData : deletedRows) {
			changes.put(rowData.getValue("key", String.class), null);
		}
		notifyOfChanges();
	}

	private void notifyOfChanges() {
		Logging.debug(this, "notifyChange, notify observers ", keepers.size());
		for (int i = 0; i < keepers.size(); i++) {
			keepers.get(i).dataHaveChanged(this);
		}

		updateCollection.addMap(changes);
	}

	private JPopupMenu buildPopupMenu(boolean keylistExtendible, boolean entryRemovable) {
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

			JMenuItem popupItemDeleteEntry0 = new JMenuItem(
					Configed.getResourceValue("EditMapPanel.PopupMenu.RemoveEntry"));
			Icons.addIntellijIconToMenuItem(popupItemDeleteEntry0, "remove");
			popupItemDeleteEntry0
					.addActionListener(actionEvent -> tableView.dispatch(new GenericTableViewMsg.DeleteRows(
							List.of(tableView.getRowByModelIndex(tableView.getSelectedRow()).getId()))));
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
			setDefaultValue = new JMenuItem(
					Configed.getResourceValue("EditMapPanelX.PopupMenu.SetSpecificValueToDefault"));
			Icons.addIntellijIconToMenuItem(setDefaultValue, "pin");
			setDefaultValue.addActionListener(actionEvent -> pinProperty(tableView.getSelectedRow()));

			popupMenu.add(setDefaultValue);

			popupRemoveSpecificEntry = new JMenuItem(
					Configed.getResourceValue("EditMapPanelX.PopupMenu.RemoveSpecificValue"));
			Icons.addIntellijIconToMenuItem(popupRemoveSpecificEntry, "remove");
			popupRemoveSpecificEntry.addActionListener(actionEvent -> unpinProperty(tableView.getSelectedRow()));

			popupMenu.add(popupRemoveSpecificEntry);
		}

		return popupMenu;
	}

	protected boolean updatePopupMenu() {
		int row = tableView.getSelectedRow();

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
		int row = tableView.getSelectedRow();
		if (row != -1) {
			propertiesCellEditorAndRenderer.editMultiValueSingleLine(tableView.getTable(), row);
		}
	}

	public void setOriginalMap(Map<String, Object> originalMap) {
		this.originalMap = originalMap;
	}

	protected JPopupMenu createBasicPopup() {
		Logging.info(this, "(EditMapPanelX) definePopup");
		return PopupMenuTrait.createJPopupMenu(tableView.getTable(), Map.of());
	}

	protected void prepareRendererForJTable(JComponent jComponent, int row, int col) {
		jComponent.setToolTipText(createTooltipForPropertyName(keys.get(row), defaultsMap, descriptionsMap, null));
		jComponent.setFont(jComponent.getFont().deriveFont(Font.PLAIN));

		RowData rowData = tableView.getRowByModelIndex(row);

		if (rowData.getState() == RowState.MISSING_DATA) {
			jComponent.setForeground(Globals.OPSI_ERROR);

			jComponent.setToolTipText(Configed.getResourceValue("EditMapPanel.MissingDefaultValue"));

			jComponent.setFont(jComponent.getFont().deriveFont(Font.BOLD));
		}

		if (rowData.getState() == RowState.MODIFIED) {
			jComponent.setFont(jComponent.getFont().deriveFont(Font.BOLD));
		}

		if (col == 1 && jComponent instanceof JLabel jLabel
				&& PropertiesCellEditorAndRenderer.isKeyForSecretValue(rowData.getValue("key", String.class))) {
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

	public void init() {
		setEditableMap(null, null);
	}

	public void setEditableMap(Map<String, Object> visualdata, Map<String, ConfigOption> optionsMap) {
		propertiesCellEditorAndRenderer.stopCellEditing();

		if (optionsMap != null) {
			modelProducer = new ListModelProducerForVisualDatamap(tableView.getTable(), optionsMap, visualdata);
		}

		// We need to call this method after creating the modelProducer since
		// it will change the map visualdata
		this.optionsMap = optionsMap != null ? optionsMap : new HashMap<>();

		descriptionsMap = new HashMap<>();
		defaultsMap = new HashMap<>();

		for (Map.Entry<String, ConfigOption> option : this.optionsMap.entrySet()) {
			String description = option.getValue().getDescription();
			Object defaultvalue = option.getValue().getDefaultValues();

			descriptionsMap.put(option.getKey(), description);
			defaultsMap.put(option.getKey(), defaultvalue);
		}

		Logging.debug(this, "setEditableMap set modelProducer  == null ", modelProducer == null);

		diffStrategy.setDefaultsMap(defaultsMap);
		diffStrategy.setOriginalMap(originalMap);
		propertiesCellEditorAndRenderer.setModelProducer(modelProducer);

		visualdata = visualdata != null ? visualdata : new HashMap<>();
		Collator myCollator = Collator.getInstance();
		myCollator.setStrength(Collator.PRIMARY);
		data = Collections.synchronizedSortedMap(new TreeMap<>(myCollator));
		data.putAll(visualdata);
		keys = new ArrayList<>(data.keySet());
		List<Map<String, Object>> transformedData = data.entrySet().stream()
				.map(e -> Map.of("key", e.getKey(), "value", e.getValue())).toList();
		tableView.dispatch(new GenericTableViewMsg.ChangeOriginalSnapshot(transformedData));
	}

	private boolean checkKey(String s) {
		if (s == null || s.isEmpty()) {
			return false;
		}

		if (!keys.contains(s)) {
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

		saveToStore(configName, defaultValues);

		optionsMap.put(configName, configOption);

		tableView.dispatch(new GenericTableViewMsg.AddRow(Map.of(configName, defaultValues)));

		return true;
	}

	private void pinProperty(int keyIndex) {
		this.pinnedProperty = true;

		String key = keys.get(keyIndex);
		changes.put(key, defaultsMap.get(key));

		if (originalMap != null) {
			originalMap.put(key, defaultsMap.get(key));
		}

		fireCellEditedEvent(keyIndex, key);
	}

	private void unpinProperty(int keyIndex) {
		this.pinnedProperty = true;

		String key = keys.get(keyIndex);
		changes.put(key, null);

		if (originalMap != null) {
			originalMap.remove(key);
		}

		fireCellEditedEvent(keyIndex, key);
	}

	private void fireCellEditedEvent(int keyIndex, String key) {
		diffStrategy.setPinnedProperty(pinnedProperty);

		tableView.dispatch(new GenericTableViewMsg.CellEdited(keyIndex, 0, key));

		this.pinnedProperty = false;

		Object defaultValue = defaultsMap.get(key);

		if (defaultValue == null) {
			Logging.info(this, "there was no default value for ", key);
		} else {
			Logging.info(this, "handled removeProperty for key ", key, " default value  ", defaultValue,
					" - should be identical with - ", optionsMap.get(key).getDefaultValues());
		}

		Logging.info(this, "property names left: ", keys);
	}

	public void removeProperties() {
		List<String> rowsToDelete = new ArrayList<>();
		for (int i = 0; i < tableView.getRowCount(); i++) {
			rowsToDelete.add(tableView.getRowByModelIndex(i).getId());
		}

		tableView.dispatch(new GenericTableViewMsg.DeleteRows(rowsToDelete));
	}

	private void saveToStore(String key, Object value) {
		if (storeData != null) {
			for (Map<String, Object> storeDataMap : storeData) {
				storeDataMap.put(key, value);
			}
		}
	}

	public void pinProperties() {
		for (int i = 0; i < tableView.getRowCount(); i++) {
			pinProperty(i);
		}
	}

	public void unpinProperties() {
		for (int i = 0; i < tableView.getRowCount(); i++) {
			unpinProperty(i);
		}
	}

	public void setShowToolTip(boolean showToolTip) {
		this.showToolTip = showToolTip;
	}

	public void setUpdateCollection(UpdateCollection updateCollection) {
		this.updateCollection = updateCollection;
	}

	public void registerDataChangedKeeper(AbstractDataChangedKeeper keeper) {
		keepers.add(keeper);
	}

	public void setKeepers(List<AbstractDataChangedKeeper> keepers) {
		this.keepers = keepers;
	}

	public List<AbstractDataChangedKeeper> getKeepers() {
		return keepers;
	}

	public List<String> getKeys() {
		return keys;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setReadOnlyEntries(Set<String> keys) {
		this.keysOfReadOnlyEntries = keys;
	}

	public void setIsEditable(Function<String, Boolean> isEditable) {
		this.isEditable = isEditable;
	}

	public void setStoreData(Collection<Map<String, Object>> storeData) {
		if (storeData == null) {
			Logging.debug(this, "setStoreData, data is null ");
		}

		this.storeData = storeData;
		changes = new HashMap<>();
	}
}
