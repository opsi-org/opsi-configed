/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dialog.ModalityType;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ExtraFrameController;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.opsidatamodel.SavedSearches;
import de.uib.opsidatamodel.serverdata.CacheIdentifier;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.opsidatamodel.serverdata.reload.ReloadEvent;
import de.uib.utils.Icons;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.SearchQueryExecutor;
import de.uib.utils.swing.list.ListCellRendererByIndex;
import de.uib.utils.table.gui.SearchTargetModel;
import de.uib.utils.table.gui.SearchTargetModelFromJList;
import de.uib.utils.table.gui.TableSearchPane;

public class SavedSearchesDialog {
	private SelectionManager manager;
	private List<String> result;
	private DefaultListModel<String> model;

	private ConfigedMain configedMain;

	private JList<String> visibleList;
	private TableSearchPane searchPane;

	private JOptionPane optionPane;
	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public SavedSearchesDialog(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		JPanel panel = createPanel();

		// The visibleList already needs to exist
		initPopupMenu();

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null,
				new Object[] { Configed.getResourceValue("search"), Configed.getResourceValue("buttonCancel") });
		Utils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetSavedSearch"));
		dialog.setModalityType(ModalityType.MODELESS);

		waitForUserInput();
	}

	private void waitForUserInput() {
		if (optionPane.getValue() != null && optionPane.getValue().equals(Configed.getResourceValue("search"))) {
			commit();
		}
	}

	public void show() {
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		dialog.setVisible(true);

		waitForUserInput();
	}

	private JPanel createPanel() {
		manager = new SelectionManager();
		result = new ArrayList<>();

		model = new DefaultListModel<>();

		visibleList = new JList<>(model);
		visibleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		visibleList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					dialog.setVisible(false);
					commit();
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(visibleList);

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(visibleList, new ArrayList<>(),
				new ArrayList<>());
		searchPane = new TableSearchPane(searchTargetModel);
		searchPane.setNarrow(true);

		resetModel();

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(searchPane).addComponent(scrollPane)));

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(searchPane).addComponent(scrollPane));

		return panel;
	}

	private void initPopupMenu() {
		JMenuItem reload = new JMenuItem(Configed.getResourceValue("ConfigedMain.reloadTable"));
		Icons.addIntellijIconToMenuItem(reload, "refresh");
		reload.addActionListener(actionEvent -> reloadAction());

		JMenuItem remove = new JMenuItem(Configed.getResourceValue("SavedSearchesDialog.RemoveSearch"));
		Icons.addIntellijIconToMenuItem(remove, "remove");
		remove.addActionListener(actionEvent -> removeSelectedEntry());

		JMenuItem edit = new JMenuItem(Configed.getResourceValue("SavedSearchesDialog.EditSearchMenu"));
		Icons.addIntellijIconToMenuItem(edit, "edit");
		edit.addActionListener(
				actionEvent -> ExtraFrameController.editClientSearch(configedMain, visibleList.getSelectedValue()));

		JMenuItem add = new JMenuItem(Configed.getResourceValue("SavedSearchesDialog.CreateNewSearch"));
		Icons.addIntellijIconToMenuItem(add, "add");
		add.addActionListener(event -> ExtraFrameController.callNewClientSelectionDialog(configedMain));

		JPopupMenu jPopupMenu = new JPopupMenu();
		jPopupMenu.add(reload);
		jPopupMenu.add(remove);
		jPopupMenu.add(edit);
		jPopupMenu.add(add);

		visibleList.setComponentPopupMenu(jPopupMenu);
	}

	private void commit() {
		SearchQueryExecutor executor = new SearchQueryExecutor(() -> {
			result = new ArrayList<>();
			List<String> selected = visibleList.getSelectedValuesList();
			if (!selected.isEmpty()) {
				manager.loadSearch(selected.get(0));

				result = manager.selectClients();
			}
			return result;
		}, visibleList.getSelectedValue());
		executor.execute();
	}

	private void removeSelectedEntry() {
		int index = visibleList.getSelectedIndex();
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
		persistenceController.getConfigDataService().deleteSavedSearch(name);

		manager.removeSearch(name);
	}

	protected void reloadAction() {
		persistenceController.reloadData(ReloadEvent.CONFIG_OPTIONS_RELOAD.toString());
		persistenceController.reloadData(CacheIdentifier.RELATIONS_AUDIT_HARDWARE_ON_HOST.toString());
		resetModel();
	}

	public void resetModel() {
		Logging.info(this, "resetModel");
		model.removeAllElements();

		SavedSearches savedSearches = persistenceController.getConfigDataService().getSavedSearchesPD();
		Set<String> nameSet = manager.getSavedSearchesNames();
		Map<String, String> descMap = new TreeMap<>();

		for (String ele : nameSet) {
			model.addElement(ele);
			descMap.put(ele, savedSearches.get(ele).getDescription());
		}

		searchPane.setTargetModel(new SearchTargetModelFromJList(visibleList, new ArrayList<>(descMap.keySet()),
				new ArrayList<>(descMap.values())));

		visibleList.setCellRenderer(new ListCellRendererByIndex(descMap));
	}
}
