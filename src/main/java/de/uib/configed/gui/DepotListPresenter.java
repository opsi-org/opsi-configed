package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Vector;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.utilities.logging.logging;
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

	// private JTextField searchField;
	private TablesearchPane searchPane;

	private Vector<? extends String> unfilteredV;

	private boolean multidepot;

	private PersistenceController persist;

	/**
	 * A component for managing (but not displaying) the depotlist
	 */
	public DepotListPresenter(DepotsList depotsList, boolean multidepot, PersistenceController persist) {
		this.depotslist = depotsList;
		this.multidepot = multidepot;
		this.persist = persist;

		Vector<String> values = new Vector<String>();
		Vector<String> descriptions = new Vector<String>();
		Map<String, Map<String, Object>> depotInfo = depotsList.getDepotInfo();

		for (String depot : depotInfo.keySet()) {
			values.add(depot);
			if (depotInfo.get(depot) == null || depotInfo.get(depot).get("description") == null)
				descriptions.add("");
			else
				descriptions.add((String) depotInfo.get(depot).get("description"));
		}

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(depotsList, values, descriptions);

		searchPane = new TablesearchPane(searchTargetModel, "depotlist");
		searchPane.setSearchMode(TablesearchPane.FULL_TEXT_SEARCH);
		searchPane.setSearchFields(new Integer[] { 0, 1 });
		searchPane.setToolTipTextCheckMarkAllColumns(
				configed.getResourceValue("DepotListPresenter.checkmarkAllColumns.tooltip"));

		initComponents();
		layouting();

	}

	/**
	 * exports the scrollpane which is produced in this class but displayed in other
	 * components
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
		return Globals.backgroundWhite;
	}

	/**
	 * allows to show that a depot selection change is in progress
	 * 
	 * @param boolean We are in progress
	 */
	public void setChangedDepotSelectionActive(boolean active) {
		if (active)
			depotslist.setBackground(Globals.backLightYellow);
		else
			depotslist.setBackground(Globals.backgroundWhite);

		// colorize as hint that we have changed the depots selection

		/*
		 * buttonCommitChangedDepotSelection.setEnabled(active);
		 * buttonCancelChangedDepotSelection.setEnabled(active);
		 * popupCommitChangedDepotSelection.setEnabled(active);
		 * popupCancelChangedDepotSelection.setEnabled(active);
		 */
	}

	private void initComponents() {
		labelDepotServer = new JLabel();
		if (multidepot)
			labelDepotServer.setText(configed.getResourceValue("DepotListPresenter.depots"));
		else
			labelDepotServer.setText(configed.getResourceValue("DepotListPresenter.depot"));
		labelDepotServer.setOpaque(false);
		// labelDepotServer.setBackground(Globals.backgroundWhite); //backTabsColor);
		labelDepotServer.setBackground(Globals.backLightBlue);
		labelDepotServer.setFont(Globals.defaultFontStandardBold);
		// labelDepotServer.setFont(Globals.defaultFont);

		buttonSelectDepotsWithEqualProperties = new JButton("", Globals.createImageIcon("images/equalplus.png", ""));
		buttonSelectDepotsWithEqualProperties
				.setToolTipText(configed.getResourceValue("MainFrame.buttonSelectDepotsWithEqualProperties"));
		Globals.formatButtonSmallText(buttonSelectDepotsWithEqualProperties);
		buttonSelectDepotsWithEqualProperties.addActionListener(this);
		buttonSelectDepotsWithEqualProperties.setEnabled(multidepot);

		buttonSelectDepotsAll = new JButton("", Globals.createImageIcon("images/plusplus.png", ""));
		buttonSelectDepotsAll.setToolTipText(configed.getResourceValue("MainFrame.buttonSelectDepotsAll"));
		Globals.formatButtonSmallText(buttonSelectDepotsAll);
		buttonSelectDepotsAll.addActionListener(this);
		buttonSelectDepotsAll.setEnabled(multidepot);

		/*
		 * searchField = new JTextField(""); searchField.setFont(Globals.defaultFont);
		 * searchField.setBackground( getMyColor() ); if (!multidepot)
		 * searchField.setVisible(false);
		 */

		searchPane.setFieldFont(Globals.defaultFont);
		searchPane.setFieldBackground(getMyColor());
		if (!multidepot)
			searchPane.setEnabled(false);
		searchPane.setBackground(getMyColor());
		searchPane.setNarrow(true);

		// not visible in this panel

		depotslist.setMaximumSize(new Dimension(200, 400));

		scrollpaneDepotslist = new JScrollPane();
		scrollpaneDepotslist.getViewport().add(depotslist);
		scrollpaneDepotslist.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollpaneDepotslist.setPreferredSize(depotslist.getMaximumSize());

		depotslist.setFont(Globals.defaultFont);
		depotslist.setBackground(Globals.backgroundWhite);

		/*
		 * depotslist.addMouseListener(new utils.PopupMouseListener(popupDepotList));
		 * popupDepotList.add(popupCommitChangedDepotSelection);
		 * popupDepotList.add(popupCancelChangedDepotSelection);
		 * popupCommitChangedDepotSelection.setText(de.uib.configed.configed.
		 * getResourceValue("MainFrame.buttonChangeDepot") ) ;
		 * popupCommitChangedDepotSelection.setToolTipText(
		 * de.uib.configed.configed.getResourceValue(
		 * "MainFrame.buttonChangeDepot.tooltip") );
		 * popupCommitChangedDepotSelection.addActionListener(new ActionListener() {
		 * public void actionPerformed(ActionEvent e) { logging.debug(this,
		 * "actionPerformed " + e); main.changeDepotSelection(); } });
		 * popupCommitChangedDepotSelection.setEnabled(false);
		 * popupCancelChangedDepotSelection.setText(de.uib.configed.configed.
		 * getResourceValue("MainFrame.buttonCancelDepot") ) ;
		 * popupCancelChangedDepotSelection.setToolTipText(
		 * de.uib.configed.configed.getResourceValue(
		 * "MainFrame.buttonCancelDepot.tooltip") );
		 * popupCancelChangedDepotSelection.addActionListener(new ActionListener() {
		 * public void actionPerformed(ActionEvent e) { logging.debug(this,
		 * "actionPerformed " + e); main.cancelChangeDepotSelection(); } });
		 * popupCancelChangedDepotSelection.setEnabled(false);
		 */

		// setChangedDepotSelectionActive(false); is not initialized
		// must not be set (otherwise the embedding scrollpane does not scroll)
		// depotslist.setPreferredSize(new Dimension(widthColumnServer, line_height));
		// depotslist.setFont(Globals.defaultFont);
		// labelDepotServer.setPreferredSize(new Dimension(widthColumnServer,
		// line_height));

		// popupDepots = new JPopupMenu();
		// depotslist.setComponentPopupMenu(popupDepots);

		/*
		 * buttonCommitChangedDepotSelection = new IconButton(
		 * configed.getResourceValue("MainFrame.buttonChangeDepot.tooltip"),
		 * "images/depot_activate.png", "images/depot_activate_disabled.png",
		 * "images/depot_activate_disabled.png", false);
		 * buttonCommitChangedDepotSelection.setPreferredSize(new
		 * Dimension(Globals.squareButtonWidth, Globals.buttonHeight));
		 * buttonCommitChangedDepotSelection.addActionListener(new ActionListener() {
		 * public void actionPerformed(ActionEvent e) { main.changeDepotSelection(); } }
		 * ); buttonCancelChangedDepotSelection = new IconButton(
		 * configed.getResourceValue("MainFrame.buttonCancelDepot.tooltip"),
		 * "images/cancel22_small.png", "images/cancel22_small.png",
		 * "images/cancel22_small.png", false);
		 * buttonCancelChangedDepotSelection.setPreferredSize(new
		 * Dimension(Globals.squareButtonWidth, Globals.buttonHeight));
		 * buttonCancelChangedDepotSelection.addActionListener(new ActionListener(){
		 * public void actionPerformed(ActionEvent e) {
		 * main.cancelChangeDepotSelection(); } } );
		 */

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
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				// .addComponent(buttonCommitChangedDepotSelection, GroupLayout.PREFERRED_SIZE,
				// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				// .addComponent(buttonCancelChangedDepotSelection, GroupLayout.PREFERRED_SIZE,
				// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				).addGap(5, 5, 10).addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(searchPane, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(5, 5, 10));

		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layout.createSequentialGroup().addGap(10)
						.addComponent(labelDepotServer, 50, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(10)
						.addComponent(buttonSelectDepotsWithEqualProperties, Globals.squareButtonWidth,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonSelectDepotsAll, Globals.squareButtonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						// .addComponent(buttonCommitChangedDepotSelection, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						// .addComponent(buttonCancelChangedDepotSelection, GroupLayout.PREFERRED_SIZE,
						// GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(10, 10, 10))
				.addGroup(layout.createSequentialGroup().addGap(5, 5, 5)
						.addComponent(searchPane, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(5, 5, 5)));
	}

	boolean filtered = false;

	protected void filterOnSelect() {
		logging.info(this, "filterOnSelect, we have " + depotslist.getListData());

		if (!filtered) {
			unfilteredV = depotslist.getListData();
			depotslist.setListData(new Vector<String>(depotslist.getSelectedValuesList()));
		} else
			depotslist.setListData(unfilteredV);

		filtered = !filtered;

	}

	// ActionListener implementation
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonSelectDepotsAll) {
			logging.info(this, "action on buttonSelectDepotsAll");
			// filterOnSelect();
			// depotslist.setSelectionInterval(0, depotslist.getModel().getSize() - 1);
			depotslist.selectAll();
		}

		else if (e.getSource() == buttonSelectDepotsWithEqualProperties) {
			logging.info(this, "action on buttonSelectDepotsWithEqualProperties");

			if (depotslist.getSelectedIndex() > -1) {
				String depotSelected = (String) depotslist.getSelectedValue();
				java.util.List<String> depotsWithEqualStock = persist
						.getAllDepotsWithIdenticalProductStock(depotSelected);
				depotslist.addToSelection(depotsWithEqualStock);

			}
		}

	}
}
