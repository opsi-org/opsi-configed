/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.SearchTargetModel;
import de.uib.utilities.table.gui.SearchTargetModelFromJList;
import de.uib.utilities.table.gui.TableSearchPane;

public class FSelectionList extends FGeneralDialog {
	private JList<String> jList;
	private TableSearchPane searchPane;

	public FSelectionList(JFrame owner, String title, boolean modal, String[] buttonList, int preferredWidth,
			int preferredHeight) {
		this(owner, title, modal, buttonList, null, preferredWidth, preferredHeight);
	}

	public FSelectionList(JFrame owner, String title, boolean modal, String[] buttonList, Icon[] icons,
			int preferredWidth, int preferredHeight) {
		super(owner, title, modal, buttonList, icons, buttonList.length, preferredWidth, preferredHeight, false, null);
		this.owner = owner;
	}

	@Override
	protected void allLayout() {
		Logging.info(this, "allLayout");

		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null) {
			centerPanel = new JPanel();
		}

		northPanel = new JPanel();
		centerPanel = createCenterPanel();
		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addComponent(northPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(centerPanel, 100, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addComponent(southPanel,
						2 * Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE));
		allLayout.setHorizontalGroup(allLayout.createParallelGroup().addComponent(northPanel, 100, 300, Short.MAX_VALUE)
				.addGroup(allLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(centerPanel, 100, 300, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE))
				.addComponent(southPanel, 100, 300, Short.MAX_VALUE));
	}

	private JPanel createCenterPanel() {
		Logging.info(this, "allLayout");

		JPanel centerPanel = new JPanel();
		GroupLayout centerLayout = new GroupLayout(centerPanel);
		centerPanel.setLayout(centerLayout);

		jList = new JList<>();
		jList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		jList.setVisible(true);
		scrollpane.getViewport().add(jList);

		SearchTargetModel searchTargetModel = new SearchTargetModelFromJList(jList, new ArrayList<>(),
				new ArrayList<>());
		searchPane = new TableSearchPane(searchTargetModel);
		searchPane.setSearchMode(TableSearchPane.SearchMode.FULL_TEXT_SEARCH);
		searchPane.setNarrow(true);

		centerLayout.setHorizontalGroup(centerLayout.createParallelGroup()
				.addComponent(searchPane, 80, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE).addGap(Globals.GAP_SIZE)
				.addComponent(scrollpane));

		centerLayout.setVerticalGroup(centerLayout
				.createSequentialGroup().addComponent(searchPane, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(scrollpane));
		return centerPanel;
	}

	private JPanel createSouthPanel() {
		southPanel = new JPanel();

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(this.additionalPane, 50, 100, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(this.additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.MIN_GAP_SIZE));

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

	public List<String> getSelectedValues() {
		return jList.getSelectedValuesList();
	}

	public void setPreviousSelectionValues(Collection<String> previouslySelectedValues) {
		int[] indices = getPreviouslySelectedIndicesFromValues(previouslySelectedValues);
		jList.setSelectedIndices(indices);
	}

	private int[] getPreviouslySelectedIndicesFromValues(Collection<String> previouslySelectedValues) {
		int[] indices = new int[previouslySelectedValues.size()];
		int n = 0;
		for (int i = 0; i < jList.getModel().getSize(); i++) {
			if (previouslySelectedValues.contains(jList.getModel().getElementAt(i))) {
				indices[n] = i;
				n++;
			}
		}
		return indices;
	}

	public void enableMultiSelection() {
		jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		searchPane.requestFocus();
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
