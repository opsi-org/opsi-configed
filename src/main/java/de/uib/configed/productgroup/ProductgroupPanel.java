package de.uib.configed.productgroup;

import java.awt.Color;
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

import javax.swing.DefaultComboBoxModel;
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

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2020 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

/* test

* Wechsel zu einer anderen, vorhandenen Produktgruppe bewirkt:
- Änderung des Inhalts des "Speichern unter"-Feldes zu dem Namen der Gruppe
- Aktivieren des Delete-Buttons
- Disablen des Speichern- und Cancel-Buttons



*/
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FShowList;
import de.uib.configed.gui.productpage.PanelGroupedProductSettings;
import de.uib.configed.guidata.IFInstallationStateTableModel;
import de.uib.configed.guidata.SearchTargetModelFromInstallationStateTable;
import de.uib.opsidatamodel.productstate.ActionRequest;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.JComboBoxToolTip;
import de.uib.utilities.table.gui.TablesearchPane;

public class ProductgroupPanel extends JPanel implements ListSelectionListener // for associated table
		, ActionListener // for internal buttons
		, ItemListener // for combos
{
	JComboBoxToolTip groupsCombo;

	class JComboBoxToolTipX extends JComboBoxToolTip {
		@Override
		public void fireActionEvent()
		// make public
		{

			super.fireActionEvent();
		}
	}

	protected de.uib.utilities.table.gui.TablesearchPane searchPane;
	protected JTable tableProducts;
	protected IFInstallationStateTableModel insTableModel;

	JTextField saveNameEditor;

	protected de.uib.configed.gui.IconButton buttonCommit;
	protected de.uib.configed.gui.IconButton buttonCancel;

	// use the filter icon of the tablesearchpane
	protected de.uib.configed.gui.IconButton buttonEditDialog;
	protected de.uib.configed.gui.IconButton buttonDelete;

	protected de.uib.configed.gui.IconButton buttonReloadProductStates;

	protected de.uib.configed.gui.IconButton buttonSaveAndExecute;

	protected de.uib.configed.gui.IconButton buttonCollectiveAction;

	protected JLabel labelCollectiveAction;
	protected JComboBoxToolTip comboAggregatedEditing;
	protected JList listChooseAction;

	private int actionType = ActionRequest.INVALID;
	protected JLabel labelSave;

	static final String NO_GROUP_ID = configed.getResourceValue("GroupPanel.NO_GROUP_ID");
	static final String SAVE_GROUP_ID = configed.getResourceValue("GroupPanel.SAVE_GROUP_ID");
	static final String NO_GROUP_DESCRIPTION = configed.getResourceValue("GroupPanel.NO_GROUP_DESCRIPTION");
	static final String EMPTIED_GROUPID = "";
	static final String TEXT_SAVE = configed.getResourceValue("GroupPanel.TEXT_SAVE");
	static final String TEXT_DELETE = configed.getResourceValue("GroupPanel.TEXT_DELETE");

	protected Map<String, Map<String, String>> theData;

	PanelGroupedProductSettings associate;
	JPanel panelEdit;
	JPanel panelCombinedSettings;

	protected Set<String> selectedIDs;

	protected DefaultComboBoxModel comboModel;

	protected LinkedHashMap<String, String> namesAndDescriptions;
	protected LinkedHashMap<String, String> namesAndDescriptionsSave;
	protected MapOfProductGroups productGroupMembers;
	protected int editIndex;
	protected String showKey;
	protected String editedKey;
	protected JTextField groupsEditField;
	protected JTextField descriptionField;
	protected boolean dataChanged = false;
	protected boolean groupEditing = false;
	protected boolean deleted = false;

	private final int minFieldWidth = 30;
	private final int maxComboWidth = 200;
	private final int minComboWidth = 30;

	abstract class MyDocumentListener implements DocumentListener {

		protected boolean enabled = true;

		public abstract void doAction();

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (enabled)
				doAction();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (enabled)
				doAction();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (enabled)
				doAction();
		}

		public void setEnabled(boolean b) {
			enabled = b;
		}
	}

	MyDocumentListener descriptionFieldListener;
	MyDocumentListener groupsEditFieldListener;

	protected ConfigedMain mainController;

	public ProductgroupPanel(PanelGroupedProductSettings associate, ConfigedMain mainController, JTable table) {
		this.associate = associate;
		this.mainController = mainController;
		this.tableProducts = table;

		initData();

		initComponents();
	}

	public void setSearchFields(List<String> fieldList) {
		searchPane.setSearchFields(fieldList);
	}

	public void markAllSearchResults() {
		searchPane.markAll();
	}

	public void setGuiIsFiltered(boolean b) {
		logging.debug(this, "setGuiIsFiltered " + b);
		searchPane.setFilteredMode(b);

	}

	public boolean getGuiIsFiltered() {

		return searchPane.isFilteredMode();

	}

	public void setReloadActionHandler(ActionListener al) {
		buttonReloadProductStates.addActionListener(al);

	}

	public void setSaveAndExecuteActionHandler(ActionListener al) {
		buttonSaveAndExecute.addActionListener(al);

	}

	protected void enterExistingGroup() {
		logging.info(this, "enterExistingGroup" + groupsCombo.getSelectedItem());

		saveNameEditorShallFollow();

		if (getGuiIsFiltered()) {
			logging.info(this, "enterExistingGroup, was filtered");
			setGuiIsFiltered(false);
			associate.noSelection();
		}

		setMembers();

		setDataChanged(false);
		setDeleted(false);

		isDeleteLegal();
	}

	protected void enterEditGroup() {

		descriptionFieldListener.setEnabled(false);

		String currentKey = groupsEditField.getText();

		if (namesAndDescriptionsSave != null && namesAndDescriptionsSave.get(currentKey) != null) {
			descriptionField.setText(namesAndDescriptionsSave.get(currentKey));
		}
		descriptionFieldListener.setEnabled(true);

		if ((!currentKey.equals(SAVE_GROUP_ID) && !currentKey.equals((String) groupsCombo.getSelectedItem()))) {
			setDataChanged(true);
		}

		isSaveLegal();
		isDeleteLegal();
	}

	private boolean membersChanged() {
		if (productGroupMembers == null || saveNameEditor == null)
			return false;

		selectedIDs = associate.getSelectedIDs();

		String currentKey = saveNameEditor.getText();

		if (currentKey == null || currentKey.equals(""))
			return false;

		boolean result = false;

		if (namesAndDescriptions.get(currentKey) != null)
		// case we have an old key
		{
			if (productGroupMembers.get(currentKey) == null || ((Set) productGroupMembers.get(currentKey)).isEmpty())
			// there were no products assigned
			{
				if (!selectedIDs.isEmpty())
					// but now there are some
					result = true;
			} else
			// there were products assigned
			{
				if (!productGroupMembers.get(currentKey).equals(selectedIDs)) // but they are different
					result = true;
			}
		} else
		// we have no old key
		{
			if (!selectedIDs.isEmpty())
				result = true;
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
		Iterator iterNames;

		boolean theSetFound = false;

		if (namesAndDescriptions == null) {
			logging.info(this, " namesAndDescriptions null ");
			return;
		}

		iterNames = namesAndDescriptions.keySet().iterator();
		logging.info(this, " namesAndDescriptions " + namesAndDescriptions);

		if (set != null) {
			TreeSetBuddy checkSet = new TreeSetBuddy(set);

			while (!theSetFound && iterNames.hasNext()) {

				String name = (String) iterNames.next();

				if (productGroupMembers.get(name) != null && productGroupMembers.get(name).equals(checkSet)) {
					// avoid selection events in groupsCombo
					setItemWithoutListener(name);
					theSetFound = true;
				}
			}
		}
		if (!theSetFound)
			setItemWithoutListener(NO_GROUP_ID);
	}

	private void updateAssociations() {

		if (membersChanged()) {
			setDataChanged(true);
		}

		if (namesAndDescriptions == null)
			return;

		isSaveLegal();

		// save name

		findGroup(associate.getSelectedIDs());
	}

	private boolean isDescriptionChanged() {
		boolean result = false;

		String currentKey = saveNameEditor.getText();

		if (namesAndDescriptions.get(currentKey) == null) // current key did not exist
			result = true;

		else {
			String oldDescription = namesAndDescriptions.get(currentKey);
			if (!oldDescription.equals(descriptionField.getText()))
				result = true;
		}

		return result;
	}

	protected void updateDescription() {
		if (isDescriptionChanged()) {
			setDataChanged(true);
		}
	}

	protected void updateKey() {

	}

	protected void initData() {
		searchPane = new TablesearchPane(new SearchTargetModelFromInstallationStateTable(tableProducts, associate),
				true, null);
		searchPane.setFiltering(true);
		searchPane.showFilterIcon(true); // filter icon inside searchpane

		groupsCombo = new JComboBoxToolTip();
		groupsCombo.setEditable(false);
		groupsCombo.setMaximumRowCount(30);

		saveNameEditor = new JTextField("");

		saveNameEditor.setEditable(true);
		saveNameEditor.setToolTipText(configed.getResourceValue("GroupPanel.GroupnameTooltip"));

		setMembers();
		setGroupEditing(false);
	}

	protected void initComponents() {
		buttonCommit = new de.uib.configed.gui.IconButton(configed.getResourceValue("GroupPanel.SaveButtonTooltip"), // desc
				"images/apply.png", // inactive
				"images/apply_over.png", // over
				"images/apply_disabled.png", // active
				true); // setEnabled
		buttonCommit.addActionListener(this);
		buttonCommit.setPreferredSize(Globals.newSmallButton);

		buttonCancel = new de.uib.configed.gui.IconButton(configed.getResourceValue("GroupPanel.CancelButtonTooltip"),
				"images/cancel.png", "images/cancel_over.png", "images/cancel_disabled.png");
		buttonCancel.addActionListener(this);
		buttonCancel.setPreferredSize(Globals.newSmallButton);

		buttonDelete = new de.uib.configed.gui.IconButton(configed.getResourceValue("GroupPanel.DeleteButtonTooltip"),
				"images/edit-delete.png", "images/edit-delete_over.png", "images/edit-delete_disabled.png");
		buttonDelete.addActionListener(this);
		buttonDelete.setPreferredSize(Globals.newSmallButton);

		buttonReloadProductStates = new de.uib.configed.gui.IconButton(
				configed.getResourceValue("GroupPanel.ReloadButtonTooltip"), "images/reload_blue16.png",
				"images/reload_blue16.png", " ", true);

		buttonReloadProductStates.setToolTipText(configed.getResourceValue("GroupPanel.ReloadProductStatesTooltip"));

		buttonReloadProductStates.addActionListener(this);
		buttonReloadProductStates.setPreferredSize(Globals.newSmallButton);
		buttonReloadProductStates.setVisible(true);

		buttonSaveAndExecute = new de.uib.configed.gui.IconButton(
				configed.getResourceValue("ConfigedMain.savePOCAndExecute"),
				"images/executing_command_blue-grey_16.png", "images/executing_command_blue-grey_16.png", " ", true);

		buttonSaveAndExecute.setToolTipText(configed.getResourceValue("ConfigedMain.savePOCAndExecute"));

		buttonSaveAndExecute.addActionListener(this);
		buttonSaveAndExecute.setPreferredSize(Globals.newSmallButton);
		buttonSaveAndExecute.setVisible(true);

		labelCollectiveAction = new JLabel(configed.getResourceValue("GroupPanel.labelAggregateProducts"));
		labelCollectiveAction.setFont(Globals.defaultFont);

		buttonCollectiveAction = new de.uib.configed.gui.IconButton(
				configed.getResourceValue("GroupPanel.buttonAggregateProducts.tooltip"),
				"images/execute16_lightblue.png", "images/execute16_lightblue.png", " ", true);

		buttonCollectiveAction.setToolTipText(configed.getResourceValue("GroupPanel.buttonAggregateProducts.tooltip"));

		buttonCollectiveAction.addActionListener(this);
		buttonCollectiveAction.setPreferredSize(Globals.newSmallButton);
		buttonCollectiveAction.setVisible(true);

		comboAggregatedEditing = new JComboBoxToolTip();

		Map<String, String> values = new LinkedHashMap<>();

		values.put(configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked"),
				configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked.tooltip"));

		values.put(configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked"),
				configed.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked.tooltip"));

		values.put(configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked"),
				configed.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked.tooltip"));

		DefaultListModel<String> modelChooseAction = new DefaultListModel<>(); // put values from hashmap into
																				// list
		for (String key : values.keySet()) {
			modelChooseAction.addElement(key);
		}
		JList<String> listChooseAction = new JList<>(modelChooseAction); // create list with tooltips
		de.uib.utilities.swing.list.StandardListCellRenderer renderActionList = new de.uib.utilities.swing.list.ListCellRendererByIndex(
				null, // index is identical with the value
				values, "");

		renderActionList.setAlternatingColors(Globals.backLightBlue, Globals.backLightBlue, Globals.backgroundLightGrey,
				Globals.backgroundWhite);

		listChooseAction.setCellRenderer(renderActionList);
		listChooseAction.setVisibleRowCount(2);
		listChooseAction.setFont(Globals.defaultFontSmallBold);

		listChooseAction.setBackground(Globals.backgroundWhite);
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

		JLabel labelStrip = new JLabel("  " + configed.getResourceValue("GroupPanel.labelAggregateProducts"));

		labelStrip.setBackground(Globals.backLightBlue);
		labelStrip.setOpaque(true);
		labelStrip.setFont(Globals.defaultFont);
		labelStrip.setForeground(Globals.lightBlack);

		JPanel surroundScrollChooseAction = new JPanel();
		GroupLayout surroundActionLayout = new GroupLayout(surroundScrollChooseAction);
		surroundScrollChooseAction.setLayout(surroundActionLayout);

		surroundActionLayout.setVerticalGroup(surroundActionLayout.createSequentialGroup().addGap(30)
				// components
				.addComponent(labelStrip, 15, 15, 15).addComponent(scrollChooseAction, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, 3 * Globals.LINE_HEIGHT));
		surroundActionLayout.setHorizontalGroup(surroundActionLayout.createParallelGroup()
				.addGroup(surroundActionLayout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
						.addComponent(labelStrip, 2 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH, Short.MAX_VALUE))
				.addGroup(surroundActionLayout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addComponent(
						scrollChooseAction, 2 * Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH, Short.MAX_VALUE)))

		;

		surroundScrollChooseAction.setBackground(Globals.backgroundLightGrey);
		surroundScrollChooseAction.setOpaque(true);

		comboAggregatedEditing.setValues(values);
		comboAggregatedEditing.setFont(Globals.defaultFont);

		buttonEditDialog = new de.uib.configed.gui.IconButton(configed.getResourceValue("GroupPanel.EditButtonTooltip"),
				"images/packagegroup_save.png", "images/packagegroup_save_over.png",
				"images/packagegroup_save_disabled.png");

		buttonEditDialog.setToolTips(configed.getResourceValue("GroupPanel.EditButtonTooltipInactive"),
				configed.getResourceValue("GroupPanel.EditButtonTooltipActive"));
		buttonEditDialog.addActionListener(this);
		buttonEditDialog.setPreferredSize(Globals.newSmallButton);

		JLabel labelSelectedGroup = new JLabel(configed.getResourceValue("GroupPanel.selectgroup.label"));

		labelSelectedGroup.setFont(Globals.defaultFont);

		groupsEditField = saveNameEditor;
		groupsEditField.getCaret().setBlinkRate(0);
		groupsEditField.setBackground(Globals.backgroundLightGrey);

		groupsEditFieldListener = new MyDocumentListener() {
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
				logging.debug(this, "focus gained on groupsEditField, groupediting");
				setGroupEditing(true);
			}
		});

		groupsCombo.setPreferredSize(Globals.buttonDimension);
		saveNameEditor.setPreferredSize(Globals.buttonDimension);
		groupsEditField.setBackground(Globals.backgroundLightGrey);

		labelSave = new JLabel();
		labelSave.setText(TEXT_SAVE);
		labelSave.setFont(Globals.defaultFont);

		descriptionField = new JTextField("");
		descriptionField.setPreferredSize(Globals.buttonDimension);
		descriptionField.setFont(Globals.defaultFont);
		descriptionField.setBackground(Globals.backgroundLightGrey);
		descriptionField.getCaret().setBlinkRate(0);

		descriptionFieldListener = new MyDocumentListener() {
			@Override
			public void doAction() {
				logging.debug(this, "description changed, setgroupediting");
				updateDescription();
			}
		};
		descriptionField.getDocument().addDocumentListener(descriptionFieldListener);

		panelEdit = new JPanel();

		panelEdit.setBackground(Globals.backgroundWhite);

		GroupLayout layoutPanelEdit = new GroupLayout(panelEdit);
		panelEdit.setLayout(layoutPanelEdit);

		layoutPanelEdit.setVerticalGroup(layoutPanelEdit.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
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
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		layoutPanelEdit.setHorizontalGroup(layoutPanelEdit
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutPanelEdit.createSequentialGroup()
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE).addComponent(labelSave,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGroup(layoutPanelEdit.createSequentialGroup()
						.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
						.addComponent(saveNameEditor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								maxComboWidth)
						.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(descriptionField, minFieldWidth, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
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

		panelEdit.setBorder(Globals.createPanelBorder());

		GroupLayout layoutMain = new GroupLayout(this);
		this.setLayout(layoutMain);

		JPanel separatingPlace = new JPanel();
		separatingPlace.setForeground(Globals.backLightYellow);
		separatingPlace.setBackground(Color.RED);
		separatingPlace.setOpaque(true);
		separatingPlace.setBorder(new javax.swing.border.LineBorder(Globals.backBlue));

		layoutMain.setVerticalGroup(layoutMain.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(searchPane,

						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)

				.addComponent(separatingPlace, 1, 1, 1)
				.addGroup(layoutMain.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelSelectedGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(groupsCombo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonReloadProductStates, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSaveAndExecute, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addComponent(surroundScrollChooseAction, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, 3 * Globals.LINE_HEIGHT)
						.addComponent(buttonEditDialog, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

				).addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2).addComponent(panelEdit,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));

		layoutMain.setHorizontalGroup(layoutMain.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutMain.createSequentialGroup()

						.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGroup(
						layoutMain.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
								.addComponent(buttonSaveAndExecute, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
								.addComponent(buttonReloadProductStates, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

								.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)

								.addComponent(surroundScrollChooseAction, minComboWidth, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)

								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)

								.addComponent(labelSelectedGroup, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.MIN_GAP_SIZE / 2, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
								.addComponent(groupsCombo, minComboWidth, GroupLayout.PREFERRED_SIZE, maxComboWidth)
								.addGap(0, Globals.MIN_GAP_SIZE / 2, Globals.GAP_SIZE / 2)
								.addComponent(buttonEditDialog, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE))
				.addComponent(panelEdit, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

				.addComponent(separatingPlace, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)

		);

	}

	protected boolean save() {
		boolean result = false;

		if (deleted) {
			String removeGroupID = groupsEditField.getText();

			theData.remove(removeGroupID);

			if (mainController.deleteGroup(removeGroupID)) {
				result = true;
				setInternalGroupsData();
			}

		} else {
			String newGroupID = groupsEditField.getText();
			String newDescription = descriptionField.getText();
			Set selectedProducts = associate.getSelectedIDs();

			logging.debug(this, "save: set groupname, description, assigned_products " + newGroupID + ", "
					+ newDescription + ", " + selectedProducts);

			Set<String> originalSelection = associate.getSelectedIDs();
			Set<String> extendedSelection = mainController.getPersistenceController()
					.extendToDependentProducts(associate.getSelectedIDs(), "bonifax.uib.local");
			Set<String> addedElements = new TreeSet<>(extendedSelection);
			addedElements.removeAll(originalSelection);

			if (!addedElements.isEmpty()) {

				FShowList fList = new FShowList(Globals.mainFrame, Globals.APPNAME, true,
						new String[] { configed.getResourceValue("buttonYES"), configed.getResourceValue("buttonNO") },
						450, 400);

				List<String> outlines = new ArrayList<>();
				outlines.add(configed.getResourceValue("GroupPanel,addAllDependentProducts"));
				outlines.add("__________");
				outlines.add("");
				outlines.addAll(addedElements);
				fList.setLines(new ArrayList<>(outlines));
				fList.setVisible(true);

				if (fList.getResult() == 1)
					associate.setSelection(extendedSelection);
			}

			selectedProducts = associate.getSelectedIDs();

			if (mainController.setProductGroup(newGroupID, newDescription, selectedProducts)) {
				result = true;

				// modify internal model
				HashMap group = new HashMap<>();
				group.put("description", newDescription);
				theData.put(newGroupID, group);

				productGroupMembers.put(newGroupID, new TreeSetBuddy(selectedProducts));

				setInternalGroupsData();
			}

		}

		return result;
	}

	protected void setMembers() {

		if (productGroupMembers == null || groupsCombo == null)
		// || productGroupMembers.get((String) groupsCombo.getSelectedItem()) == null)
		{
			associate.clearSelection();
			return;
		}

		logging.debug(this, "group members " + productGroupMembers.get((String) groupsCombo.getSelectedItem()));

		associate.setSelection((Set) productGroupMembers.get((String) groupsCombo.getSelectedItem()));
	}

	protected void setInternalGroupsData() {

		namesAndDescriptionsSave = new LinkedHashMap();
		namesAndDescriptionsSave.put(SAVE_GROUP_ID, NO_GROUP_DESCRIPTION);
		for (String id : new TreeSet<>(theData.keySet())) {

			namesAndDescriptionsSave.put(id, theData.get(id).get("description"));
		}

		namesAndDescriptions = new LinkedHashMap();
		namesAndDescriptions.put(NO_GROUP_ID, "");
		for (String id : new TreeSet<>(theData.keySet())) {

			namesAndDescriptions.put(id, theData.get(id).get("description"));
		}
		groupsCombo.setValues(namesAndDescriptions);
		comboModel = (DefaultComboBoxModel) groupsCombo.getModel();

		// for reentry
		clearChanges();
	}

	public void setGroupsData(final Map<String, Map<String, String>> data,
			final Map<String, Set<String>> productGroupMembers) {
		logging.debug(this, "setGroupsData " + data);
		setGroupEditing(false);

		this.productGroupMembers = new MapOfProductGroups(productGroupMembers);

		if (data != null)
			theData = data;
		else
			theData = new HashMap<>();

		setInternalGroupsData();

		setGuiIsFiltered(false);
	}

	private void setGroupEditing(boolean b) {

		groupEditing = b;
		if (panelEdit != null) {
			panelEdit.setVisible(b);
			buttonEditDialog.setActivated(!b);
		}

	}

	// ActionListener interface
	@Override
	public void actionPerformed(java.awt.event.ActionEvent e) {
		String s = " ";
		if (e.getSource() == buttonCommit) {
			commit();
		}

		else if (e.getSource() == buttonCancel) {

			cancel();
		}

		else if (e.getSource() == buttonDelete) {
			setDeleted(true);
			setDataChanged(true);
		}

		else if (e.getSource() == buttonEditDialog) {
			setGroupEditing(!panelEdit.isVisible());
		}

		else if (e.getSource() == buttonCollectiveAction) {
			handleCollectiveAction(s, (IFInstallationStateTableModel) tableProducts.getModel());
		}

	}

	protected void handleCollectiveAction(String selected, IFInstallationStateTableModel insTableModel) {

		logging.info(this, "handleCollectiveAction on " + comboAggregatedEditing.getSelectedItem());

		List<String> saveSelectedProducts = associate.getSelectedProducts();

		logging.info(this, "handleCollectiveAction, selected products " + associate.getSelectedRowsInModelTerms());
		logging.info(this, "handleCollectiveAction, selected products " + associate.getSelectedProducts());

		if (!insTableModel.infoIfNoClientsSelected()) {
			insTableModel.initCollectiveChange();

			if (selected == configed.getResourceValue("GroupPanel.comboAggregateProducts.setupMarked"))
				actionType = ActionRequest.SETUP;

			else if (selected == de.uib.configed.configed
					.getResourceValue("GroupPanel.comboAggregateProducts.uninstallMarked"))
				actionType = ActionRequest.UNINSTALL;

			else if (selected == de.uib.configed.configed
					.getResourceValue("GroupPanel.comboAggregateProducts.noneMarked"))
				actionType = ActionRequest.NONE;

			else
				actionType = ActionRequest.INVALID;

			if (!(actionType == ActionRequest.INVALID)) {

				associate.getSelectedRowsInModelTerms().stream()
						.peek(x -> logging.info(" row id " + x + " product " + insTableModel.getValueAt(x, 0)))
						.forEach(x -> insTableModel.collectiveChangeActionRequest(
								(String) insTableModel.getValueAt(x, 0), new ActionRequest(actionType)));
			}

			insTableModel.finishCollectiveChange();
		}

		associate.setSelection(new HashSet<>(saveSelectedProducts));

	}

	// ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {

		// Ignore extra messages.
		if (e.getValueIsAdjusting())
			return;

		// assumed to be list selection model of product table
		updateAssociations();
	}

	// ItemListener
	@Override
	public void itemStateChanged(ItemEvent e) {
		logging.info(this, "itemStateChanged ");
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getSource() == groupsCombo)
				enterExistingGroup();

			else if (e.getSource() == saveNameEditor)
				enterEditGroup();

		}
	}

	private void saveNameEditorShallFollow() {
		int comboIndex = groupsCombo.getSelectedIndex();

		if (comboIndex == 0 || comboIndex == -1)
			saveNameEditor.setText(SAVE_GROUP_ID);
		else
			saveNameEditor.setText("" + groupsCombo.getSelectedItem());
	}

	// handling data changes
	private void clearChanges() {
		// reset internal components
		if (saveNameEditor != null) {

			saveNameEditorShallFollow();
		}

		setDataChanged(false);
		setDeleted(false);
		if (buttonDelete != null)
			buttonDelete.setEnabled(false);

		setMembers();
	}

	public void setDataChanged(boolean b) {

		dataChanged = b;
		if (buttonCommit != null)
			buttonCommit.setEnabled(b);
		if (buttonCancel != null)
			buttonCancel.setEnabled(b);
	}

	protected boolean isSaveLegal() {
		String proposedName = groupsEditField.getText();

		boolean result = true;

		if (proposedName == null)
			result = false;

		if (result) {
			boolean forbidden = proposedName.equals(SAVE_GROUP_ID) || proposedName.equals("");

			result = !forbidden;
		}

		buttonCommit.setEnabled(result);

		return result;
	}

	protected boolean isDeleteLegal() {
		boolean result = false;

		if (groupsCombo != null)
			result = (groupsCombo.getSelectedIndex() > 0);

		buttonDelete.setEnabled(result);

		return result;
	}

	protected void setDeleted(boolean b) {
		if (saveNameEditor != null && descriptionField != null) {
			saveNameEditor.setEnabled(!b);
			saveNameEditor.setEditable(!b);
			descriptionField.setEnabled(!b);
			descriptionField.setEditable(!b);
			buttonDelete.setEnabled(!b);

			if (b)
				labelSave.setText(TEXT_DELETE);
			else
				labelSave.setText(TEXT_SAVE);

			deleted = b;
		}
	}

	public void commit() {
		logging.debug(this, "commit");
		String newGroupID = groupsEditField.getText();
		if (save()) {
			clearChanges();
			groupsCombo.setSelectedItem(newGroupID);
			enterExistingGroup();
		}
	}

	public void cancel() {
		clearChanges();
	}

}
