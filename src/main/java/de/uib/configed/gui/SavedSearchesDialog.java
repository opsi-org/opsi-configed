/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.DefaultListModel;
import javax.swing.JMenuItem;
import javax.swing.event.ListSelectionEvent;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;
import de.uib.utilities.swing.FEditStringList;
import de.uib.utilities.swing.list.ListCellRendererByIndex;
import utils.Utils;

public class SavedSearchesDialog extends FEditStringList {
	private SelectionManager manager;
	private List<String> result;
	private DefaultListModel<String> model;

	private JTableSelectionPanel selectionPanel;
	private ConfigedMain configedMain;

	private GlassPane glassPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SavedSearchesDialog(JTableSelectionPanel selectionPanel, ConfigedMain configedMain) {
		this.selectionPanel = selectionPanel;
		this.configedMain = configedMain;

		initDialog();
	}

	private void initDialog() {
		setTitle(Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		setModal(false);
		setLeaveOnCommit(false);
		manager = new SelectionManager(null);
		result = new LinkedList<>();

		model = new DefaultListModel<>();

		setEditable(true);
		setListModel(model);
		resetModel();

		buttonAdd.setVisible(true);
		buttonRemove.setVisible(false);
		extraField.setVisible(false);

		glassPane = new GlassPane();
		setGlassPane(glassPane);
	}

	public void start() {
		buttonAdd.setEnabled(true);

		resetModel();
	}

	@Override
	protected void createComponents() {
		super.createComponents();

		// redefine buttonCommit
		buttonCommit.setToolTipText(Configed.getResourceValue("SavedSearchesDialog.ExecuteButtonTooltip"));
		buttonCommit.setIcon(Utils.createImageIcon("images/executing_command_red_22.png", ""));
		buttonCommit.setSelectedIcon(Utils.createImageIcon("images/executing_command_red_22.png", ""));
		buttonCommit.setDisabledIcon(Utils.createImageIcon("images/execute_disabled.png", ""));
		buttonCommit.setPreferredSize(new java.awt.Dimension(BUTTON_WIDTH, Globals.BUTTON_HEIGHT));

		buttonCancel.setToolTipText(Configed.getResourceValue("buttonCancel"));
	}

	@Override
	protected void initComponents() {
		super.initComponents();

		buttonRemove.addActionListener((ActionEvent e) -> {
			Logging.debug(this, "actionPerformed");
			removeSelectedEntry();
		});

		buttonRemove.setToolTipText(Configed.getResourceValue("SavedSearchesDialog.RemoveButtonTooltip"));

		buttonAdd.setEnabled(true);
		buttonAdd.addActionListener((ActionEvent e) -> {
			Logging.debug(this, "actionPerformed on buttonAdd ");
			addElement();
		});

		JMenuItem reload = new JMenuItem(Configed.getResourceValue("ConfigedMain.reloadTable"));
		reload.setIcon(Utils.createImageIcon("images/reload16.png", ""));
		reload.addActionListener((ActionEvent e) -> {
			Logging.debug(this, "reload action");
			reloadAction();
		});
		popup.add(reload);

		JMenuItem remove = new JMenuItem(Configed.getResourceValue("SavedSearchesDialog.RemoveButtonTooltip"));
		remove.addActionListener((ActionEvent actionEvent) -> {
			Logging.debug(this, "remove action");
			removeSelectedEntry();
		});
		popup.add(remove);

		JMenuItem edit = new JMenuItem(Configed.getResourceValue("SavedSearchesDialog.EditSearchMenu"));
		edit.addActionListener(actionEvent -> editSearch(visibleList.getSelectedValue()));
		popup.add(edit);
	}

	@Override
	public void setVisible(boolean b) {
		Logging.debug(this, "setVisible " + b);
		super.setVisible(b);
	}

	@Override
	public void setDataChanged(boolean b) {
		boolean active = buttonCommit.isEnabled();
		super.setDataChanged(b);
		buttonCommit.setEnabled(active);
	}

	// interface ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		Logging.debug(this, "SavedSearchesDialog ListSelectionListener valueChanged " + e);

		super.valueChanged(e);

		if (e.getValueIsAdjusting()) {
			return;
		}

		buttonCommit.setEnabled(!getSelectedList().isEmpty());
	}

	@Override
	public Object getValue() {
		return result;
	}

	@Override
	protected void commit() {
		glassPane.activate(true);

		buttonCommit.setEnabled(false);
		buttonCancel.setEnabled(false);

		result = new LinkedList<>();

		try {
			List<String> selected = getSelectedList();
			if (!selected.isEmpty()) {
				manager.loadSearch(selected.get(0));

				// test:

				result = manager.selectClients();
			}
			super.commit();
		} finally {
			buttonCommit.setEnabled(true);
			buttonCancel.setEnabled(true);
		}

		Logging.info(this, "commit result == null " + (result == null));
		if (result != null) {
			Logging.info(this, "result size " + result.size());
			selectionPanel.setSelectedValues(result);
		}

		glassPane.activate(false);
	}

	@Override
	protected void cancel() {
		result = new LinkedList<>();

		super.cancel();
	}

	private void removeSelectedEntry() {
		int index = visibleList.getSelectedIndex();
		Logging.debug(this, "remove selected Entry, list index " + index);

		if (index == -1) {
			return;
		}

		Logging.debug(this, "remove entry at " + index);

		removeSavedSearch(model.get(index));
		model.remove(index);
	}

	// overwrite to implement persistency
	private void removeSavedSearch(String name) {
		persistenceController.getConfigDataService().deleteSavedSearch(name);

		manager.removeSearch(name);
	}

	protected void reloadAction() {
		persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		persistenceController.reloadData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST.toString());
		resetModel();
	}

	// overwrite to implement
	private void addElement() {
		configedMain.callClientSelectionDialog();
	}

	// overwrite to implement
	private void editSearch(String name) {
		configedMain.callClientSelectionDialog();
		configedMain.loadSearch(name);
	}

	public void resetModel() {
		Logging.info(this, "resetModel");
		model.removeAllElements();

		SavedSearches savedSearches = manager.getSavedSearches();
		TreeSet<String> nameSet = new TreeSet<>(manager.getSavedSearchesNames());
		Map<String, String> valueMap = new HashMap<>();
		Map<String, String> descMap = new HashMap<>();

		for (String ele : nameSet) {
			model.addElement(ele);
			valueMap.put(ele, ele);
			descMap.put(ele, savedSearches.get(ele).getDescription());
		}

		setCellRenderer(new ListCellRendererByIndex(valueMap, descMap, ""));

		initSelection();
	}

	// interface MouseListener
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			commit();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseExited(MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Not needed */}
}
