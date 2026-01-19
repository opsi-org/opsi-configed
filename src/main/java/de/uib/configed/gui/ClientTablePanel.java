/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.data.SearchTargetModelFromClientTable;
import de.uib.configed.gui.share.table.gui.FilterKey;
import de.uib.configed.gui.share.table.gui.TableSearchPane;
import de.uib.configed.share.Icons;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class ClientTablePanel extends JPanel implements ListSelectionListener {
	private JScrollPane scrollpane;

	private TableSearchPane searchPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	// we put a JTable on a standard JScrollPane
	private ClientTable clientTable;

	private int[] lastSelectedRows = new int[0];

	private DefaultListSelectionModel selectionModel;
	private ConfigedMain configedMain;

	public ClientTablePanel(ConfigedMain configedMain) {
		super();
		this.configedMain = configedMain;

		initComponents();
	}

	private void initComponents() {
		scrollpane = new JScrollPane();
		clientTable = new ClientTable(configedMain);

		// Ask to be notified of selection changes.
		selectionModel = (DefaultListSelectionModel) clientTable.getSelectionModel();
		// the default implementation in JTable yields this type

		activateListSelectionListener();

		searchPane = new TableSearchPane(new SearchTargetModelFromClientTable(configedMain, clientTable));
		searchPane.setFilterKey(FilterKey.CLIENT_TABLE);
		searchPane.setFiltering();

		clientTable.addKeyListener(searchPane);

		setLayout(new MigLayout("insets " + Globals.GAP_SIZE + " 0 0 0, fillx, wrap 1", "[grow, fill]",
				"[]" + Globals.GAP_SIZE + "[grow, fill]"));

		add(searchPane);
		add(scrollpane, "grow, push");
	}

	public void updateTable() {
		if (scrollpane.getViewport().getView() == clientTable) {
			// Do nothing if we already set the table as view
			return;
		}

		if (persistenceController.getDataServices().hostInfoCollections.getCountClients() == 0) {
			setMissingDataPanel();
		} else {
			scrollpane.getViewport().setView(clientTable);
		}
	}

	public void activateListSelectionListener() {
		// We want to prevent, that the listSelectionListener is added more than once
		if (!Arrays.asList(selectionModel.getListSelectionListeners()).contains(this)) {
			selectionModel.addListSelectionListener(this);
		}
	}

	// This returns if the selectionListener was actually deactivated
	// if the list only contains one listener, it's only the JTable itself
	// that is listening, but not our other listener
	public boolean deactivateListSelectionListener() {
		if (selectionModel.getListSelectionListeners().length == 1) {
			return false;
		} else {
			selectionModel.removeListSelectionListener(this);
			return true;
		}
	}

	public boolean isFilteredMode() {
		return searchPane.isFilteredMode();
	}

	public ClientTable getClientTable() {
		return clientTable;
	}

	// ListSelectionListener for client list
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			actOnListSelection();
		}
	}

	private void actOnListSelection() {
		if (ChangedDataManager.checkSaveAll(true)) {
			configedMain.actOnListSelection();
			lastSelectedRows = clientTable.getSelectedRows();
		} else {
			deactivateListSelectionListener();
			selectionModel.setValueIsAdjusting(true);
			selectionModel.clearSelection();
			for (int row : lastSelectedRows) {
				selectionModel.addSelectionInterval(row, row);
			}
			selectionModel.setValueIsAdjusting(false);
			activateListSelectionListener();
		}
	}

	public void setMissingDataPanel() {
		JLabel missingData0 = new JLabel(Icons.createImageIcon(Globals.ICON_CONFIGED, ""));
		JLabel missingData1 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label1"));
		JLabel missingData2 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label2"));
		JPanel mdPanel = new JPanel();

		mdPanel.setLayout(new MigLayout("fill"));

		JPanel panel = new JPanel(new MigLayout("wrap 1, aligny center, alignx center, gap 0", "[center]", "[]0"));
		panel.add(missingData0);
		panel.add(missingData1, "gapy " + Globals.GAP_SIZE);
		panel.add(missingData2, "gapy " + Globals.GAP_SIZE);

		mdPanel.add(panel, "grow, center");

		scrollpane.getViewport().setView(mdPanel);
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		scrollpane.addMouseListener(l);
		clientTable.addMouseListener(l);
	}

	public void setFilterMark(boolean selected) {
		searchPane.setFilterMark(selected);
	}

	public final void initColumnNames() {
		// New code
		searchPane.setSearchFieldsAll();
	}

	public void restoreFilter() {
		searchPane.restoreFilter();
	}

	public void setSelectedValues(Collection<String> clientsToSelect) {
		String valuesListS = null;
		if (clientsToSelect != null) {
			valuesListS = "" + clientsToSelect.size();
		}

		Logging.info(this, "setSelectedValues ", valuesListS);

		if (clientsToSelect == null) {
			// Clear selection when empty
			selectionModel.clearSelection();
		} else if (clientsToSelect.isEmpty() && selectionModel.isSelectionEmpty()) {
			// Also act on list selection when there is no client to select.
			// For example when the last client is unselected in the client list,
			// this method is not called automatically by the selection listener,
			// so we do it manually
			actOnListSelection();
		} else {
			// because of ordering , we create a TreeSet view of the list
			selectionModel.setValueIsAdjusting(true);
			selectionModel.clearSelection();
			for (int i = 0; i < clientTable.getRowCount(); i++) {
				Logging.debug(this, "setSelectedValues checkValue for i ", i, ": ", clientTable.getValueAt(i, 0));

				if (clientsToSelect.contains(clientTable.getClientName(i))) {
					selectionModel.addSelectionInterval(i, i);
					Logging.debug(this, "setSelectedValues add interval ", i);
				}
			}

			selectionModel.setValueIsAdjusting(false);

			clientTable.moveToFirstSelected();

			Logging.info(this, "setSelectedValues  produced ", clientTable.getSelectedRowCount());
		}
	}

	public DefaultTableModel getTableModel() {
		return (DefaultTableModel) clientTable.getModel();
	}

	public int findModelRowFromClientName(String clientName) {
		int result = -1;

		if (clientName == null) {
			return result;
		}

		boolean found = false;
		int row = 0;

		while (!found && row < getTableModel().getRowCount()) {
			String compareName = clientTable.getClientName(row);

			if (clientName.equals(compareName)) {
				found = true;
				result = row;
			}

			if (!found) {
				row++;
			}
		}

		return result;
	}
}
