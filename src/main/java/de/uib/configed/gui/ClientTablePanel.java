/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ExtraFrameController;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Icons;
import de.uib.utils.logging.Logging;
import de.uib.utils.table.gui.FilterKey;
import de.uib.utils.table.gui.FilterStateManager;
import de.uib.utils.table.gui.SearchTargetModelFromTable;
import de.uib.utils.table.gui.TableFilterState;
import de.uib.utils.table.gui.TableSearchPane;

public class ClientTablePanel extends JPanel implements ListSelectionListener, KeyListener {
	private JScrollPane scrollpane;

	private TableSearchPane searchPane;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	// we put a JTable on a standard JScrollPane
	private ClientTable clientTable;

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

		searchPane = new TableSearchPane(new SearchTargetModelFromTable(clientTable));
		searchPane.setFilterKey(FilterKey.CLIENT_CONFIG);

		clientTable.addKeyListener(searchPane);
		clientTable.addKeyListener(this);

		GroupLayout layoutLeftPane = new GroupLayout(this);
		this.setLayout(layoutLeftPane);

		layoutLeftPane.setHorizontalGroup(layoutLeftPane.createParallelGroup(Alignment.LEADING)
				.addComponent(searchPane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addComponent(scrollpane, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutLeftPane.setVerticalGroup(layoutLeftPane.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(searchPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(scrollpane, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void updateTable() {
		if (scrollpane.getViewport().getView() == clientTable) {
			// Do nothing if we already set the table as view
			return;
		}

		if (persistenceController.getHostInfoCollections().getCountClients() == 0) {
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

	public ClientTable getClientTable() {
		return clientTable;
	}

	// ListSelectionListener for client list
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			configedMain.actOnListSelection();
		}
	}

	public void setMissingDataPanel() {
		JLabel missingData0 = new JLabel(Icons.createImageIcon(Globals.ICON_CONFIGED, ""));
		JLabel missingData1 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label1"));
		JLabel missingData2 = new JLabel(Configed.getResourceValue("JTableSelectionPanel.missingDataPanel.label2"));
		JPanel mdPanel = new JPanel();

		GroupLayout mdLayout = new GroupLayout(mdPanel);
		mdPanel.setLayout(mdLayout);

		mdLayout.setVerticalGroup(mdLayout.createSequentialGroup().addGap(0, Globals.GAP_SIZE, Short.MAX_VALUE)
				.addComponent(missingData0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(missingData1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(missingData2, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(0, Globals.GAP_SIZE, Short.MAX_VALUE));
		mdLayout.setHorizontalGroup(mdLayout.createSequentialGroup().addGap(0, Globals.GAP_SIZE, Short.MAX_VALUE)
				.addGroup(mdLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(missingData0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(missingData1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(missingData2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(0, Globals.GAP_SIZE, Short.MAX_VALUE));

		scrollpane.getViewport().setView(mdPanel);
	}

	@Override
	public synchronized void addMouseListener(MouseListener l) {
		scrollpane.addMouseListener(l);
		clientTable.addMouseListener(l);
	}

	public final void initColumnNames() {
		// New code
		searchPane.setSearchFieldsAll();
	}

	public void restoreFilter() {
		TableFilterState filterState = FilterStateManager.getFilterState(FilterKey.CLIENT_CONFIG);
		searchPane.setFilterState(filterState);
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
			configedMain.actOnListSelection();
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

	// KeyListener interface
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			ExtraFrameController.startRemoteControlFrame(configedMain, persistenceController);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		/* Not needed */}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}
}
