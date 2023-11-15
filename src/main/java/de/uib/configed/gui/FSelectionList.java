/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModel;
import de.uib.utilities.table.gui.SearchTargetModelFromJList;
import de.uib.utilities.table.gui.TablesearchPane;

public class FSelectionList extends FGeneralDialog {
	private JList<String> jList;
	private TablesearchPane searchPane;

	public FSelectionList(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		super(owner, title, modal, buttonList, preferredWidth, preferredHeight);
		this.owner = owner;
	}

	@Override
	protected void allLayout() {
		Logging.info(this, "allLayout");

		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null) {
			centerPanel = new JPanel();
		}

		northPanel = new JPanel();
		centerPanel = createAditionalPane();
		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addComponent(northPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(centerPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(southPanel,
						2 * Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		allLayout.setHorizontalGroup(allLayout.createParallelGroup().addComponent(northPanel, 100, 300, Short.MAX_VALUE)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
						.addComponent(centerPanel, 100, 300, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2))
				.addComponent(southPanel, 100, 300, Short.MAX_VALUE));
	}

	private JPanel createAditionalPane() {
		Logging.info(this, "allLayout");

		JPanel additionalPanel = new JPanel();
		GroupLayout additionalLayout = new GroupLayout(additionalPanel);
		additionalPanel.setLayout(additionalLayout);

		jList = new JList<>();
		jList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		jList.setVisible(true);
		scrollpane.getViewport().add(jList);

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(jList, new ArrayList<>(),
				new ArrayList<>());

		searchPane = new TablesearchPane(searchTargetModel, "sessionlist");
		searchPane.setSearchMode(TablesearchPane.FULL_TEXT_SEARCH);
		searchPane.setNarrow(true);

		additionalLayout.setHorizontalGroup(additionalLayout.createParallelGroup()
				.addComponent(searchPane, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE).addComponent(scrollpane));

		additionalLayout.setVerticalGroup(additionalLayout.createSequentialGroup()
				.addComponent(searchPane, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE).addComponent(scrollpane));
		return additionalPanel;
	}

	private JPanel createSouthPanel() {
		southPanel = new JPanel();

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.GAP_SIZE / 2)
						.addComponent(additionalPane, 50, 100, Short.MAX_VALUE).addGap(Globals.GAP_SIZE / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2)
				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2, Globals.GAP_SIZE / 2));

		return southPanel;
	}

	@Override
	protected boolean wantToBeRegisteredWithRunningInstances() {
		return false;
	}

	public void setListData(List<String> v) {
		jList.setListData(v.toArray(String[]::new));
		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(jList, v, v);
		searchPane.setTargetModel(searchTargetModel);
	}

	public String getSelectedValue() {
		return jList.getSelectedValue();
	}

	@Override
	public void doAction1() {
		Logging.debug(this, "doAction1");
		result = 1;
		leave();
	}

	@Override
	public void doAction2() {
		Logging.debug(this, "doAction2");
		result = 2;
		leave();
	}

	@Override
	public void leave() {
		setVisible(false);
	}

	public void exit() {
		super.leave();
	}
}
