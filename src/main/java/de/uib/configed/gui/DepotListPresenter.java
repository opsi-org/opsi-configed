
package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModel;
import de.uib.utilities.table.gui.SearchTargetModelFromJList;
import de.uib.utilities.table.gui.TablesearchPane;

/**
 * DepotListPresenter.java Copyright (c) 2017 Organisation: uib
 * 
 * @author Rupert Röder
 */

public class DepotListPresenter extends JPanel implements ActionListener {
	private DepotsList depotslist;
	private JScrollPane scrollpaneDepotslist;
	// this will not be shown in this panel but exported for use in other panels

	private JLabel labelDepotServer;
	private JButton buttonSelectDepotsWithEqualProperties;
	private JButton buttonSelectDepotsAll;

	private TablesearchPane searchPane;

	private List<String> unfilteredV;

	private boolean multidepot;

	private AbstractPersistenceController persist;

	/**
	 * A component for managing (but not displaying) the depotlist
	 */
	public DepotListPresenter(DepotsList depotsList, boolean multidepot, AbstractPersistenceController persist) {
		this.depotslist = depotsList;
		this.multidepot = multidepot;
		this.persist = persist;

		List<String> values = new ArrayList<>();
		List<String> descriptions = new ArrayList<>();
		Map<String, Map<String, Object>> depotInfo = depotsList.getDepotInfo();

		for (Entry<String, Map<String, Object>> depotInfoEntry : depotInfo.entrySet()) {
			values.add(depotInfoEntry.getKey());
			if (depotInfoEntry.getValue() == null || depotInfoEntry.getValue().get("description") == null) {
				descriptions.add("");
			} else {
				descriptions.add((String) depotInfoEntry.getValue().get("description"));
			}
		}

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(depotsList, values, descriptions);

		searchPane = new TablesearchPane(searchTargetModel, "depotlist");
		searchPane.setSearchMode(TablesearchPane.FULL_TEXT_SEARCH);
		searchPane.setSearchFields(new Integer[] { 0, 1 });
		searchPane.setToolTipTextCheckMarkAllColumns(
				Configed.getResourceValue("DepotListPresenter.checkmarkAllColumns.tooltip"));

		initComponents();
		layouting();
	}

	/**
	 * exports the scrollpane which is produced in this class but displayed in
	 * other components
	 *
	 * @return a scrollpane which shows the depotslist
	 */
	public JScrollPane getScrollpaneDepotslist() {
		return scrollpaneDepotslist;
	}

	/**
	 * gives the color for setting back color of this panel in master panel
	 * 
	 * @return java.awt.Color
	 */
	public Color getMyColor() {
		return Globals.SECONDARY_BACKGROUND_COLOR;
	}

	/**
	 * allows to show that a depot selection change is in progress
	 * 
	 * @param boolean We are in progress
	 */
	public void setChangedDepotSelectionActive(boolean active) {
		if (active) {
			depotslist.setBackground(Globals.BACKGROUND_COLOR_9);
		} else {
			depotslist.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}
		// colorize as hint that we have changed the depots selection
	}

	private void initComponents() {
		labelDepotServer = new JLabel();
		if (multidepot) {
			labelDepotServer.setText(Configed.getResourceValue("DepotListPresenter.depots"));
		} else {
			labelDepotServer.setText(Configed.getResourceValue("DepotListPresenter.depot"));
		}
		labelDepotServer.setOpaque(false);

		labelDepotServer.setBackground(Globals.BACKGROUND_COLOR_7);
		labelDepotServer.setFont(Globals.defaultFontStandardBold);

		buttonSelectDepotsWithEqualProperties = new JButton("", Globals.createImageIcon("images/equalplus.png", ""));
		buttonSelectDepotsWithEqualProperties
				.setToolTipText(Configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties"));
		Globals.formatButtonSmallText(buttonSelectDepotsWithEqualProperties);
		buttonSelectDepotsWithEqualProperties.addActionListener(this);
		buttonSelectDepotsWithEqualProperties.setEnabled(multidepot);

		buttonSelectDepotsAll = new JButton("", Globals.createImageIcon("images/plusplus.png", ""));
		buttonSelectDepotsAll.setToolTipText(Configed.getResourceValue("MainFrame.buttonSelectDepotsAll"));
		Globals.formatButtonSmallText(buttonSelectDepotsAll);
		buttonSelectDepotsAll.addActionListener(this);
		buttonSelectDepotsAll.setEnabled(multidepot);

		searchPane.setFieldFont(Globals.defaultFont);
		searchPane.setFieldBackground(getMyColor());
		if (!multidepot) {
			searchPane.setEnabled(false);
		}
		searchPane.setBackground(getMyColor());
		searchPane.setNarrow(true);

		// not visible in this panel

		depotslist.setMaximumSize(new Dimension(200, 400));

		scrollpaneDepotslist = new JScrollPane();
		scrollpaneDepotslist.getViewport().add(depotslist);
		scrollpaneDepotslist.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneDepotslist.setPreferredSize(depotslist.getMaximumSize());

		depotslist.setFont(Globals.defaultFont);
		depotslist.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
	}

	private void layouting() {
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(5, 5, 10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelDepotServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSelectDepotsWithEqualProperties, GroupLayout.Alignment.TRAILING,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSelectDepotsAll, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(5, 5, 10).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(searchPane, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(5, 5, 10));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup().addGap(10)
						.addComponent(labelDepotServer, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(10)
						.addComponent(buttonSelectDepotsWithEqualProperties, Globals.SQUARE_BUTTON_WIDTH,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSelectDepotsAll, Globals.SQUARE_BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(10, 10, 10))
				.addGroup(layout.createSequentialGroup().addGap(5, 5, 5)
						.addComponent(searchPane, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(5, 5, 5)));
	}

	boolean filtered = false;

	protected void filterOnSelect() {
		Logging.info(this, "filterOnSelect, we have " + depotslist.getListData());

		if (!filtered) {
			unfilteredV = depotslist.getListData();
			depotslist.setListData(new ArrayList<>(depotslist.getSelectedValuesList()));
		} else {
			depotslist.setListData(unfilteredV);
		}

		filtered = !filtered;
	}

	// ActionListener implementation
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonSelectDepotsAll) {
			Logging.info(this, "action on buttonSelectDepotsAll");

			depotslist.selectAll();
		} else if (e.getSource() == buttonSelectDepotsWithEqualProperties) {
			Logging.info(this, "action on buttonSelectDepotsWithEqualProperties");

			if (depotslist.getSelectedIndex() > -1) {
				String depotSelected = depotslist.getSelectedValue();
				List<String> depotsWithEqualStock = persist.getAllDepotsWithIdenticalProductStock(depotSelected);
				depotslist.addToSelection(depotsWithEqualStock);
			}
		}
	}
}
