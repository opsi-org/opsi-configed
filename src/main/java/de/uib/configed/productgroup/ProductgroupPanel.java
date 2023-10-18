/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.productgroup;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.IconButton;
import de.uib.configed.gui.productpage.PanelProductSettings;
import de.uib.configed.guidata.IFInstallationStateTableModel;
import de.uib.configed.guidata.SearchTargetModelFromInstallationStateTable;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.JComboBoxToolTip;
import de.uib.utilities.swing.list.ListCellRendererByIndex;
import de.uib.utilities.swing.list.StandardListCellRenderer;
import de.uib.utilities.table.gui.TablesearchPane;

public class ProductgroupPanel extends JPanel implements ListSelectionListener, ActionListener, ItemListener {

	private static final String NO_GROUP_ID = Configed.getResourceValue("GroupPanel.NO_GROUP_ID");
	private static final String SAVE_GROUP_ID = Configed.getResourceValue("GroupPanel.SAVE_GROUP_ID");
	private static final String NO_GROUP_DESCRIPTION = Configed.getResourceValue("GroupPanel.NO_GROUP_DESCRIPTION");
	private static final String TEXT_SAVE = Configed.getResourceValue("GroupPanel.TEXT_SAVE");
	private static final String TEXT_DELETE = Configed.getResourceValue("GroupPanel.TEXT_DELETE");

	private static final int MIN_FIELD_WIDTH = 30;
	private static final int MAX_COMBO_WIDTH = 200;
	private static final int MIN_COMBO_WIDTH = 30;

	private JComboBoxToolTip groupsCombo;

	private TablesearchPane searchPane;
	private JTable tableProducts;

	private JTextField saveNameEditor;

	private IconButton buttonCommit;
	private IconButton buttonCancel;

	// use the filter icon of the tablesearchpane
	private IconButton buttonEditDialog;
	private IconButton buttonDelete;

	private IconButton buttonReloadProductStates;

	private IconButton buttonExecuteNow;

	private IconButton buttonCollectiveAction;

	private JComboBoxToolTip comboAggregatedEditing;

	private JLabel labelSave;

	private Map<String, Map<String, String>> theData;

	private PanelProductSettings associate;
	private JPanel panelEdit;

	private Map<String, String> namesAndDescriptions;
	private Map<String, String> namesAndDescriptionsSave;
	private MapOfProductGroups productGroupMembers;
	private JTextField groupsEditField;
	private JTextField descriptionField;
	private boolean deleted;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private abstract static class AbstractDocumentListener implements DocumentListener {

		private boolean enabled = true;

		public abstract void doAction();

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (enabled) {
				doAction();
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (enabled) {
				doAction();
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (enabled) {
				doAction();
			}
		}

		public void setEnabled(boolean b) {
			enabled = b;
		}
	}

	private AbstractDocumentListener descriptionFieldListener;

	private ConfigedMain configedMain;

	public ProductgroupPanel(PanelProductSettings associate, ConfigedMain configedMain, JTable table) {
		this.associate = associate;
		this.configedMain = configedMain;
		this.tableProducts = table;

		initData();

		initComponents();
	}

	public void setSearchFields(List<String> fieldList) {
		searchPane.setSearchFields(fieldList);
	}

	public void setGuiIsFiltered(boolean b) {
		Logging.debug(this, "setGuiIsFiltered " + b);
		searchPane.setFilteredMode(b);

	}

	public boolean isGuiFiltered() {
		return searchPane.isFilteredMode();
	}

	public void setReloadActionHandler(ActionListener al) {
		buttonReloadProductStates.addActionListener(al);

	}

	public void setSaveAndExecuteActionHandler(ActionListener al) {
		buttonExecuteNow.addActionListener(al);

	}

	private void enterExistingGroup() {
		Logging.info(this, "enterExistingGroup" + groupsCombo.getSelectedItem());

		saveNameEditorShallFollow();

		if (isGuiFiltered()) {
			Logging.info(this, "enterExistingGroup, was filtered");
			setGuiIsFiltered(false);
			associate.noSelection();
		}

		setMembers();

		setDataChanged(false);
		setDeleted(false);

		isDeleteLegal();
	}

	private void enterEditGroup() {

		descriptionFieldListener.setEnabled(false);

		String currentKey = groupsEditField.getText();

		if (namesAndDescriptionsSave != null && namesAndDescriptionsSave.get(currentKey) != null) {
			descriptionField.setText(namesAndDescriptionsSave.get(currentKey));
		}
		descriptionFieldListener.setEnabled(true);

		if (!currentKey.equals(SAVE_GROUP_ID) && !currentKey.equals(groupsCombo.getSelectedItem())) {
			setDataChanged(true);
		}

		isSaveLegal();
		isDeleteLegal();
	}

	private boolean membersChanged() {
		if (productGroupMembers == null || saveNameEditor == null) {
			return false;
		}

		String currentKey = saveNameEditor.getText();

		if (currentKey == null || currentKey.isEmpty()) {
			return false;
		}

		boolean result = false;
		Set<String> selectedIDs = associate.getSelectedIDs();

		if (namesAndDescriptions.get(currentKey) != null) {
			// case we have an old key

			if (productGroupMembers.get(currentKey) == null || productGroupMembers.get(currentKey).isEmpty()) {
				// there were no products assigned

				if (!selectedIDs.isEmpty()) {
					// but now there are some
					result = true;
				}
			} else {
				// there were products assigned
				if (!productGroupMembers.get(currentKey).equals(selectedIDs)) {
					// but they are different
					result = true;
				}
			}
		} else {
			// we have no old key
			if (!selectedIDs.isEmpty()) {
				result = true;
			}
		}

		return result;
	}

	private void setItemWithoutListener(String key) {
		groupsCombo.removeItemListener(this);
		groupsCombo.setSelectedItem(key);
		groupsCombo.addItemListener(this);

		isDeleteLegal();
	}

	public void findGroup(Set<String> set) {

		if (namesAndDescriptions == null) {
			Logging.info(this, " namesAndDescriptions null ");
			return;
		}

		Iterator<String> iterNames = namesAndDescriptions.keySet().iterator();
		Logging.info(this, " namesAndDescriptions " + namesAndDescriptions);

		boolean theSetFound = false;
		if (set != null) {
			TreeSetBuddy checkSet = new TreeSetBuddy(set);

			while (!theSetFound && iterNames.hasNext()) {

				String name = iterNames.next();

				if (productGroupMembers.get(name) != null && productGroupMembers.get(name).equals(checkSet)) {
					// avoid selection events in groupsCombo
					setItemWithoutListener(name);
					theSetFound = true;
				}
			}
		}
		if (!theSetFound) {
			setItemWithoutListener(NO_GROUP_ID);
		}
	}

	private void updateAssociations() {

		if (membersChanged()) {
			setDataChanged(true);
		}

		if (namesAndDescriptions == null) {
			return;
		}

		isSaveLegal();

		// save name

		findGroup(associate.getSelectedIDs());
	}

	private boolean isDescriptionChanged() {
		boolean result = false;

		String currentKey = saveNameEditor.getText();

		// current key did not exist
		if (namesAndDescriptions.get(currentKey) == null) {
			result = true;
		} else {
			String oldDescription = namesAndDescriptions.get(currentKey);
			if (!oldDescription.equals(descriptionField.getText())) {
				result = true;
			}
		}

		return result;
	}

	private void updateDescription() {
		if (isDescriptionChanged()) {
			setDataChanged(true);
		}
	}

	private void initData() {
		searchPane = new TablesearchPane(new SearchTargetModelFromInstallationStateTable(tableProducts, associate),
				true, null);
		searchPane.setFiltering(true);

		// filter icon inside searchpane
		searchPane.showFilterIcon(true);

		groupsCombo = new JComboBoxToolTip();
		groupsCombo.setEditable(false);
		groupsCombo.setMaximumRowCount(30);

		saveNameEditor = new JTextField("");

		saveNameEditor.setEditable(true);
		saveNameEditor.setToolTipText(Configed.getResourceValue("GroupPanel.GroupnameTooltip"));

		setMembers();
		setGroupEditing(false);
	}

	private void initComponents() {
		buttonCommit = new IconButton(Configed.getResourceValue("GroupPanel.SaveButtonTooltip"), // desc
				"images/apply.png", // inactive
				"images/apply_over.png", // over
				"images/apply_disabled.png", // active
				true); // setEnabled
		buttonCommit.addActionListener(this);
		buttonCommit.setPreferredSize(Globals.NEW_SMALL_BUTTON);

		buttonCancel = new IconButton(Configed.getResourceValue("buttonCancel"), "images/cancel.png",
				"images/cancel_over.png", "images/cancel_disabled.png");
		buttonCancel.addActionListener(this);
		buttonCancel.setPreferredSize(Globals.NEW_SMALL_BUTTON);

		buttonDelete = new IconButton(Configed.getResourceValue("GroupPanel.DeleteButtonTooltip"),
				"images/edit-delete.png", "images/edit-delete_over.png", "images/edit-delete_disabled.png");
		buttonDelete.addActionListener(this);
		buttonDelete.setPreferredSize(Globals.NEW_SMALL_BUTTON);

		buttonReloadProductStates = new IconButton(Configed.getResourceValue("GroupPanel.ReloadButtonTooltip"),
				"images/reload16.png", "images/reload16.png", " ", true);

		buttonReloadProductStates.setToolTipText(Configed.getResourceValue("GroupPanel.ReloadProductStatesTooltip"));

		buttonReloadProductStates.addActionListener(this);
		buttonReloadProductStates.setPreferredSize(Globals.NEW_SMALL_BUTTON);
		buttonReloadProductStates.setVisible(true);

		buttonExecuteNow = new IconButton(Configed.getResourceValue("ConfigedMain.OpsiclientdEvent_on_demand"),
				"images/executing_command_blue_16.png", "images/executing_command_blue_16.png", " ", true);
		buttonExecuteNow.setToolTipText(Configed.getResourceValue("ConfigedMain.OpsiclientdEvent_on_demand"));
		buttonExecuteNow.addActionListener(this);
		buttonExecuteNow.setPreferredSize(Globals.NEW_SMALL_BUTTON);
		buttonExecuteNow.setVisible(true);

		buttonCollectiveAction = new IconButton(Configed.getResourceValue("GroupPanel.buttonAggregateProducts.tooltip"),
				"images/execute16_lightblue.png", "images/execute16_lightblue.png", " ", true);

		buttonCollectiveAction.setToolTipText(Configed.getResourceValue("GroupPanel.buttonAggregateProducts.tooltip"));

		buttonCollectiveAction.addActionListener(this);
		buttonCollectiveAction.setPreferredSize(Globals.NEW_SMALL_BUTTON);
		buttonCollectiveAction.setVisible(true);

		comboAggregatedEditing = new JComboBoxToolTip();

		Map<String, String> values = new LinkedHashMap<>();

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked.tooltip"));

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked.tooltip"));

		values.put(Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked"),
				Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked.tooltip"));

		DefaultListModel<String> modelChooseAction = new DefaultListModel<>();
		for (String key : values.keySet()) {
			modelChooseAction.addElement(key);
		}

		// create list with tooltips
		JList<String> listChooseAction = new JList<>(modelChooseAction);
		StandardListCellRenderer renderActionList = new ListCellRendererByIndex(null, values, "");

		renderActionList.setAlternatingColors(Globals.BACKGROUND_COLOR_7, Globals.BACKGROUND_COLOR_7,
				Globals.BACKGROUND_COLOR_3, Globals.SECONDARY_BACKGROUND_COLOR);

		listChooseAction.setCellRenderer(renderActionList);
		listChooseAction.setVisibleRowCount(2);

		JScrollPane scrollChooseAction = new JScrollPane(listChooseAction,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listChooseAction.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getClickCount() > 1) {
					String s = listChooseAction.getSelectedValue();
					handleCollectiveAction(s, (IFInstallationStateTableModel) tableProducts.getModel());
				}
			}
		});

		listChooseAction.setSelectedIndex(0);

		JLabel labelStrip = new JLabel("  " + Configed.getResourceValue("GroupPanel.labelAggregateProducts"));

		labelStrip.setOpaque(true);

		buttonEditDialog = new IconButton(Configed.getResourceValue("GroupPanel.EditButtonTooltip"),
				"images/packagegroup_save.png", "images/packagegroup_save_over.png",
				"images/packagegroup_save_disabled.png");

		buttonEditDialog.setToolTips(Configed.getResourceValue("GroupPanel.EditButtonTooltipInactive"),
				Configed.getResourceValue("GroupPanel.EditButtonTooltipActive"));
		buttonEditDialog.addActionListener(this);
		buttonEditDialog.setPreferredSize(Globals.NEW_SMALL_BUTTON);

		JLabel labelSelectedGroup = new JLabel(Configed.getResourceValue("GroupPanel.selectgroup.label"));

		groupsEditField = saveNameEditor;
		groupsEditField.getCaret().setBlinkRate(0);

		AbstractDocumentListener groupsEditFieldListener = new AbstractDocumentListener() {
			@Override
			public void doAction() {

				enterEditGroup();
			}
		};

		groupsEditField.getDocument().addDocumentListener(groupsEditFieldListener);

		groupsCombo.addItemListener(this);

		groupsEditField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				Logging.debug(this, "focus gained on groupsEditField, groupediting");
				setGroupEditing(true);
			}
		});

		groupsCombo.setPreferredSize(Globals.BUTTON_DIMENSION);
		saveNameEditor.setPreferredSize(Globals.BUTTON_DIMENSION);

		labelSave = new JLabel();
		labelSave.setText(TEXT_SAVE);

		descriptionField = new JTextField("");
		descriptionField.setPreferredSize(Globals.BUTTON_DIMENSION);

		descriptionField.getCaret().setBlinkRate(0);

		descriptionFieldListener = new AbstractDocumentListener() {
			@Override
			public void doAction() {
				Logging.debug(this, "description changed, setgroupediting");
				updateDescription();
			}
		};
		descriptionField.getDocument().addDocumentListener(descriptionFieldListener);

		panelEdit = new JPanel();

		GroupLayout layoutPanelEdit = new GroupLayout(panelEdit);
		panelEdit.setLayout(layoutPanelEdit);

		layoutPanelEdit.setVerticalGroup(layoutPanelEdit.createSequentialGroup()
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
				.addGroup(layoutPanelEdit.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(labelSave,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(1, 1, 2)
				.addGroup(layoutPanelEdit.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(saveNameEditor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(descriptionField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2));

		layoutPanelEdit.setHorizontalGroup(layoutPanelEdit.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutPanelEdit.createSequentialGroup()
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE).addComponent(labelSave,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layoutPanelEdit.createSequentialGroup()
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(saveNameEditor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								MAX_COMBO_WIDTH)
						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(descriptionField, MIN_FIELD_WIDTH, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(buttonCommit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(buttonCancel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)));

		setGroupEditing(false);

		GroupLayout layoutMain = new GroupLayout(this);
		this.setLayout(layoutMain);

		layoutMain
				.setVerticalGroup(
						layoutMain.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
								.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
								.addGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(labelSelectedGroup, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(groupsCombo, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonReloadProductStates, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonExecuteNow, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

										.addGroup(layoutMain.createSequentialGroup()
												.addComponent(labelStrip, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(scrollChooseAction, GroupLayout.PREFERRED_SIZE,
														GroupLayout.PREFERRED_SIZE, 3 * Globals.LINE_HEIGHT))

										.addComponent(buttonEditDialog, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

								).addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
								.addComponent(panelEdit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE));

		layoutMain.setHorizontalGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.LEADING)

				.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGroup(layoutMain.createSequentialGroup()
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(buttonExecuteNow, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(buttonReloadProductStates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)

						.addGroup(layoutMain.createParallelGroup()
								.addGroup(layoutMain.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addComponent(
										labelStrip, 2 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
										Short.MAX_VALUE))
								.addGroup(layoutMain.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addComponent(
										scrollChooseAction, 2 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)))

						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)

						.addComponent(labelSelectedGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(groupsCombo, MIN_COMBO_WIDTH, GroupLayout.PREFERRED_SIZE, MAX_COMBO_WIDTH)
						.addGap(0, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(buttonEditDialog, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE))
				.addComponent(panelEdit, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

	}

	private boolean save() {
		boolean result = false;

		if (deleted) {
			String removeGroupID = groupsEditField.getText();

			theData.remove(removeGroupID);

			if (configedMain.deleteGroup(removeGroupID)) {
				result = true;
				setInternalGroupsData();
			}

		} else {
			String newGroupID = groupsEditField.getText();
			String newDescription = descriptionField.getText();
			Set<String> selectedProducts = associate.getSelectedIDs();

			Logging.debug(this, "save: set groupname, description, assigned_products " + newGroupID + ", "
					+ newDescription + ", " + selectedProducts);

			Set<String> originalSelection = associate.getSelectedIDs();
			Set<String> extendedSelection = persistenceController.getProductDataService()
					.extendToDependentProducts(associate.getSelectedIDs(), "bonifax.uib.local");
			Set<String> addedElements = new TreeSet<>(extendedSelection);
			addedElements.removeAll(originalSelection);

			if (!addedElements.isEmpty()) {

				FShowList fList = new FShowList(ConfigedMain.getMainFrame(), Globals.APPNAME, true,
						new String[] { Configed.getResourceValue("buttonNO"), Configed.getResourceValue("buttonYES") },
						450, 400);

				List<String> outlines = new ArrayList<>();
				outlines.add(Configed.getResourceValue("GroupPanel.addAllDependentProducts"));
				outlines.add("__________");
				outlines.add("");
				outlines.addAll(addedElements);
				fList.setLines(new ArrayList<>(outlines));
				fList.setVisible(true);

				if (fList.getResult() == 2) {
					associate.setSelection(extendedSelection);
				}
			}

			selectedProducts = associate.getSelectedIDs();

			if (configedMain.setProductGroup(newGroupID, newDescription, selectedProducts)) {
				result = true;

				// modify internal model
				Map<String, String> group = new HashMap<>();
				group.put("description", newDescription);
				theData.put(newGroupID, group);

				productGroupMembers.put(newGroupID, new TreeSetBuddy(selectedProducts));

				setInternalGroupsData();
			}

		}

		return result;
	}

	private void setMembers() {

		if (productGroupMembers == null || groupsCombo == null) {
			associate.clearSelection();
			return;
		}

		Logging.debug(this, "group members " + productGroupMembers.get(groupsCombo.getSelectedItem()));

		associate.setSelection(productGroupMembers.get(groupsCombo.getSelectedItem()));
	}

	private void setInternalGroupsData() {

		namesAndDescriptionsSave = new LinkedHashMap<>();
		namesAndDescriptionsSave.put(SAVE_GROUP_ID, NO_GROUP_DESCRIPTION);
		for (String id : new TreeSet<>(theData.keySet())) {

			namesAndDescriptionsSave.put(id, theData.get(id).get("description"));
		}

		namesAndDescriptions = new LinkedHashMap<>();
		namesAndDescriptions.put(NO_GROUP_ID, "");
		for (String id : new TreeSet<>(theData.keySet())) {

			namesAndDescriptions.put(id, theData.get(id).get("description"));
		}
		groupsCombo.setValues(namesAndDescriptions);

		// for reentry
		clearChanges();
	}

	public void setGroupsData(final Map<String, Map<String, String>> data,
			final Map<String, Set<String>> productGroupMembers) {
		Logging.debug(this, "setGroupsData " + data);
		setGroupEditing(false);

		this.productGroupMembers = new MapOfProductGroups(productGroupMembers);

		if (data != null) {
			theData = data;
		} else {
			theData = new HashMap<>();
		}

		setInternalGroupsData();

		setGuiIsFiltered(false);
	}

	private void setGroupEditing(boolean b) {

		if (panelEdit != null) {
			panelEdit.setVisible(b);
		}
	}

	// ActionListener interface
	@Override
	public void actionPerformed(ActionEvent e) {
		String s = " ";
		if (e.getSource() == buttonCommit) {
			commit();
		} else if (e.getSource() == buttonCancel) {

			clearChanges();
		} else if (e.getSource() == buttonDelete) {

			setDeleted(true);
			setDataChanged(true);
		} else if (e.getSource() == buttonEditDialog) {

			setGroupEditing(!panelEdit.isVisible());
		} else if (e.getSource() == buttonCollectiveAction) {

			handleCollectiveAction(s, (IFInstallationStateTableModel) tableProducts.getModel());
		} else {
			Logging.warning(this, "unexpected action performed on source " + e.getSource());
		}
	}

	private void handleCollectiveAction(String selected, IFInstallationStateTableModel insTableModel) {

		Logging.info(this, "handleCollectiveAction on " + comboAggregatedEditing.getSelectedItem());

		List<String> saveSelectedProducts = associate.getSelectedProducts();

		Logging.info(this, "handleCollectiveAction, selected products " + associate.getSelectedRowsInModelTerms());
		Logging.info(this, "handleCollectiveAction, selected products " + associate.getSelectedProducts());

		if (!insTableModel.infoIfNoClientsSelected()) {
			insTableModel.initCollectiveChange();

			int actionType;
			if (selected.equals(Configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked"))) {
				actionType = ActionRequest.SETUP;
			} else if (selected
					.equals(Configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked"))) {
				actionType = ActionRequest.UNINSTALL;
			} else if (selected.equals(Configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked"))) {
				actionType = ActionRequest.NONE;
			} else {
				actionType = ActionRequest.INVALID;
			}

			if (actionType != ActionRequest.INVALID) {

				associate.getSelectedRowsInModelTerms().stream().forEach((Integer x) -> {
					Logging.info(" row id " + x + " product " + insTableModel.getValueAt(x, 0));
					insTableModel.collectiveChangeActionRequest((String) insTableModel.getValueAt(x, 0),
							new ActionRequest(actionType));
				});
			}

			insTableModel.finishCollectiveChange();
		}

		associate.setSelection(new HashSet<>(saveSelectedProducts));

	}

	// ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// Ignore extra messages.
		if (e.getValueIsAdjusting()) {
			return;
		}

		// assumed to be list selection model of product table
		updateAssociations();
	}

	// ItemListener
	@Override
	public void itemStateChanged(ItemEvent e) {
		Logging.info(this, "itemStateChanged ");
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getSource() == groupsCombo) {
				enterExistingGroup();
			} else if (e.getSource() == saveNameEditor) {
				enterEditGroup();
			} else {
				Logging.warning(this, "unexpected itemEvent on source " + e.getSource());
			}
		}
	}

	private void saveNameEditorShallFollow() {
		int comboIndex = groupsCombo.getSelectedIndex();

		if (comboIndex == 0 || comboIndex == -1) {
			saveNameEditor.setText(SAVE_GROUP_ID);
		} else {
			saveNameEditor.setText("" + groupsCombo.getSelectedItem());
		}
	}

	// handling data changes
	private void clearChanges() {
		// reset internal components
		if (saveNameEditor != null) {
			saveNameEditorShallFollow();
		}

		setDataChanged(false);
		setDeleted(false);
		if (buttonDelete != null) {
			buttonDelete.setEnabled(false);
		}

		setMembers();
	}

	private void setDataChanged(boolean b) {

		if (buttonCommit != null) {
			buttonCommit.setEnabled(b);
		}

		if (buttonCancel != null) {
			buttonCancel.setEnabled(b);
		}
	}

	private boolean isSaveLegal() {
		String proposedName = groupsEditField.getText();

		boolean result = true;

		if (proposedName == null) {
			result = false;
		}

		if (result) {
			boolean forbidden = proposedName.equals(SAVE_GROUP_ID) || proposedName.isEmpty();

			result = !forbidden;
		}

		buttonCommit.setEnabled(result);

		return result;
	}

	private boolean isDeleteLegal() {
		boolean result = false;

		if (groupsCombo != null) {
			result = groupsCombo.getSelectedIndex() > 0;
		}

		buttonDelete.setEnabled(result);

		return result;
	}

	private void setDeleted(boolean b) {
		if (saveNameEditor != null && descriptionField != null) {
			saveNameEditor.setEnabled(!b);
			saveNameEditor.setEditable(!b);
			descriptionField.setEnabled(!b);
			descriptionField.setEditable(!b);
			buttonDelete.setEnabled(!b);

			if (b) {
				labelSave.setText(TEXT_DELETE);
			} else {
				labelSave.setText(TEXT_SAVE);
			}

			deleted = b;
		}
	}

	private void commit() {
		Logging.debug(this, "commit");
		String newGroupID = groupsEditField.getText();
		if (save()) {
			clearChanges();
			groupsCombo.setSelectedItem(newGroupID);
			enterExistingGroup();
		}
	}
}
