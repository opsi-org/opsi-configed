/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import de.uib.configed.core.domain.SavedSearches;
import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.core.domain.serverdata.reload.ReloadEvent;
import de.uib.configed.gui.features.clientselection.SelectionManager;
import de.uib.configed.gui.share.swing.SearchQueryExecutor;
import de.uib.configed.gui.share.swing.list.ListCellRendererByIndex;
import de.uib.configed.gui.share.table.gui.SearchTargetModelFromJList;
import de.uib.configed.share.Icons;
import de.uib.configed.share.PopupMouseListener;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class SavedSearchesDialog extends ListSelectionDialog {
	private SelectionManager manager;
	private Collection<String> result;
	private DefaultListModel<String> model;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SavedSearchesDialog(ConfigedMain configedMain) {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));

		this.configedMain = configedMain;

		manager = new SelectionManager();
		result = new ArrayList<>();
		model = new DefaultListModel<>();
		listSelectionList.setModel(model);
		listSelectionList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					commit();
				}
			}
		});

		super.setSingleSelection();

		// The listSelectionList already needs to exist
		initPopupMenu();

		JButton searchButton = new JButton(Configed.getResourceValue("search"));
		searchButton.addActionListener(actionEvent -> commit());

		jOptionPane.setOptions(new Object[] { searchButton, Configed.getResourceValue("buttonClose") });

		SavedSearchesDialog.this.resetModel();

		Utils.addKeyBindingToJComponent(listSelectionList, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
				this::reloadAction);
	}

	private void initPopupMenu() {
		JMenuItem reload = new JMenuItem(Configed.getResourceValue("reload"));
		reload.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		Icons.addIntellijIconToMenuItem(reload, "refresh");
		reload.addActionListener(actionEvent -> reloadAction());

		JMenuItem remove = new JMenuItem(Configed.getResourceValue("SavedSearchesDialog.RemoveSearch"));
		Icons.addIntellijIconToMenuItem(remove, "remove");
		remove.addActionListener(actionEvent -> removeSelectedEntry());

		JMenuItem edit = new JMenuItem(Configed.getResourceValue("SavedSearchesDialog.EditSearchMenu"));
		Icons.addIntellijIconToMenuItem(edit, "edit");
		edit.addActionListener(actionEvent -> ExtraFrameController.editClientSearch(configedMain,
				listSelectionList.getSelectedValue()));

		JMenuItem add = new JMenuItem(Configed.getResourceValue("SavedSearchesDialog.CreateNewSearch"));
		Icons.addIntellijIconToMenuItem(add, "add");
		add.addActionListener(event -> ExtraFrameController.callNewClientSelectionDialog(configedMain));

		JPopupMenu jPopupMenu = new JPopupMenu();
		jPopupMenu.add(reload);
		jPopupMenu.add(remove);
		jPopupMenu.add(edit);
		jPopupMenu.add(add);

		listSelectionList.addMouseListener(new PopupMouseListener(jPopupMenu));
	}

	private void commit() {
		SearchQueryExecutor executor = new SearchQueryExecutor(() -> {
			result = new HashSet<>();
			String selected = listSelectionList.getSelectedValue();
			if (selected != null) {
				manager.loadSearch(selected);

				result = manager.selectClients();
			}
			return result;
		}, listSelectionList.getSelectedValue());
		executor.execute();

		dialog.setVisible(false);
	}

	private void removeSelectedEntry() {
		int index = listSelectionList.getSelectedIndex();
		Logging.debug(this, "remove selected Entry, list index ", index);

		if (index == -1) {
			return;
		}

		Logging.debug(this, "remove entry at ", index);

		removeSavedSearch(model.get(index));
		model.remove(index);
	}

	// overwrite to implement persistency
	private void removeSavedSearch(String name) {
		persistenceController.getDataServices().config.deleteSavedSearch(name);

		manager.removeSearch(name);
	}

	protected void reloadAction() {
		persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		resetModel();
	}

	public void resetModel() {
		Logging.info(this, "resetModel");
		model.removeAllElements();

		SavedSearches savedSearches = persistenceController.getDataServices().config.getSavedSearchesPD();
		Set<String> nameSet = manager.getSavedSearchesNames();
		Map<String, String> descMap = new TreeMap<>();

		for (String ele : nameSet) {
			model.addElement(ele);
			descMap.put(ele, savedSearches.get(ele).getDescription());
		}

		listSelectionList.setModel(model);

		searchPane.setTargetModel(new SearchTargetModelFromJList(listSelectionList, new ArrayList<>(descMap.keySet()),
				new ArrayList<>(descMap.values())));

		listSelectionList.setCellRenderer(new ListCellRendererByIndex(descMap));
	}
}
