package de.uib.configed.gui;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2013-2014,2017 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import de.uib.configed.Globals;
import de.uib.configed.HostsStatusInfo;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.Containership;

public class HostsStatusPanel extends JPanel
		implements HostsStatusInfo {
	public static final int maxClientnamesInField = 10;

	JLabel labelAllClientsCount;
	JTextField fieldGroupActivated;
	// JTextField fieldAllClientsCount;
	JTextField fieldSelectedClientsNames;
	JTextField fieldActivatedClientsCount;
	JTextField fieldInvolvedDepots;

	public HostsStatusPanel() {
		super();
		createGui();
	}

	public void setGroupName(String s) {
		logging.info(this, "setGroupName " + s);
		resetReportedClients();
		fieldGroupActivated.setText(s);
	}

	private void resetReportedGroup() {
		logging.debug(this, "resetReportedGroup  - void");
		// fieldGroupActivated.setText("");
	}

	public String getSelectedClientNames() {
		return fieldSelectedClientsNames.getText();
	}

	public String getInvolvedDepots() {
		return fieldInvolvedDepots.getText();
	}

	public String getGroupName() {
		return fieldGroupActivated.getText();
	}

	private void resetReportedClients() {
		fieldActivatedClientsCount.setText("");
		fieldSelectedClientsNames.setText("");
		fieldSelectedClientsNames.setToolTipText("");

		fieldInvolvedDepots.setText("");
	}

	private void initializeValues() {
		labelAllClientsCount.setText(configed.getResourceValue("MainFrame.labelClientsTotal") + " ");
		resetReportedClients();
		fieldInvolvedDepots.setText("");
		fieldInvolvedDepots.setToolTipText("");
	}

	public void updateValues(Integer clientsCount, Integer selectedClientsCount, String selectedClientNames,
			String involvedDepots) {
		logging.info(this,
				"updateValues clientsCount, selectedClientsCount " + clientsCount + ", " + selectedClientsCount);
		logging.info(this, "updateValues clientsCount, selectedClientsCount "
				+ clientsCount + ", " + selectedClientsCount);
		// resetReportedGroup();

		// if (clientsCount != null)
		labelAllClientsCount.setText(configed.getResourceValue("MainFrame.labelClientsTotal") + "  " + clientsCount);

		setFieldClientsCount(selectedClientsCount);

		if (selectedClientNames == null) {
			fieldSelectedClientsNames.setText("");
			fieldSelectedClientsNames.setToolTipText(null);
		} else
		// if (selectedClientNames != null)
		{

			/*
			 * String oldSelectedClientNames = fieldSelectedClientsNames.getText();
			 * if (oldSelectedClientNames != null
			 * &&
			 * selectedClientNames.indexOf(oldSelectedClientNames) > -1
			 * )
			 * {
			 * fieldSelectedClientsNames.setSelectionColor(Color.yellow);
			 * fieldSelectedClientsNames.setSelectionStart(oldSelectedClientNames.length());
			 * System.out.println(" sel start " + oldSelectedClientNames.length());
			 * fieldSelectedClientsNames.setSelectionEnd(selectedClientNames.length());
			 * System.out.println(" sel end " + selectedClientNames.length());
			 * }
			 */

			fieldSelectedClientsNames.setText(selectedClientNames);
			// fieldSelectedClientsNames.setCaretPosition(0);
			fieldSelectedClientsNames.setToolTipText(
					"<html><body><p>" + selectedClientNames.replace(";\n", "<br\\ >") + "</p></body></html>");
			// System.out.println("caret " + fieldSelectedClientsNames.getCaret());

		}

		if (involvedDepots != null) {
			fieldInvolvedDepots.setText(involvedDepots);
			fieldInvolvedDepots.setToolTipText(
					"<html><body><p>" + involvedDepots.replace(";\n", "<br\\ >") + "</p></body></html>");
		}
	}

	public void setGroupClientsCount(int n) {
		String newS = null;
		int bracketIndex = fieldActivatedClientsCount.getText().indexOf("(");
		if (bracketIndex > -1) {
			String keep = fieldActivatedClientsCount.getText().substring(0, bracketIndex);
			newS = keep + "(" + n + ")";
		} else
			newS = "(" + n + ")";
		fieldActivatedClientsCount.setText(newS);
	}

	private void setFieldClientsCount(Integer n) {
		String newS = null;
		if (n != null)
			newS = "" + n + " ";
		int bracketIndex = fieldActivatedClientsCount.getText().indexOf("(");
		if (bracketIndex > -1) {
			String keep = fieldActivatedClientsCount.getText().substring(bracketIndex);
			newS = newS + keep;
		}

		fieldActivatedClientsCount.setText(newS);
	}

	private void createGui() {
		Containership csStatusPane = new Containership(this);

		GroupLayout layoutStatusPane = new GroupLayout(this);
		this.setLayout(layoutStatusPane);

		JLabel labelActivated = new JLabel(configed.getResourceValue("MainFrame.activated"));

		JLabel labelGroupActivated = new JLabel(configed.getResourceValue("MainFrame.groupActivated"));
		// labelGroupActivated.setPreferredSize(Globals.counterfieldDimension);

		fieldGroupActivated = new JTextField("");
		// fieldGroupActivated.setFont(Globals.defaultFontStandardBold);
		fieldGroupActivated.setPreferredSize(Globals.counterfieldDimension);
		fieldGroupActivated.setEditable(false);

		labelAllClientsCount = new JLabel("");
		labelAllClientsCount.setPreferredSize(
				Globals.labelDimension);

		JLabel labelSelectedClientsCount = new JLabel(configed.getResourceValue("MainFrame.labelSelected"));
		// labelSelectedClientsCount.setPreferredSize(Globals.shortlabelDimension);

		JLabel labelSelectedClientsNames = new JLabel(configed.getResourceValue("MainFrame.labelNames"));
		// labelSelectedClientsNames.setPreferredSize(Globals.shortlabelDimension);

		JLabel labelInvolvedDepots = new JLabel(configed.getResourceValue("MainFrame.labelInDepot"));
		// labelInvolvedDepots.setPreferredSize(Globals.shortlabelDimension);
		JLabel labelInvolvedDepots2 = new JLabel(configed.getResourceValue("MainFrame.labelInDepot2"));

		// fieldAllClientsCount = new JTextField("");
		// fieldAllClientsCount.setPreferredSize(Globals.counterfieldDimension);

		fieldActivatedClientsCount = new JTextField("");
		fieldActivatedClientsCount.setPreferredSize(Globals.counterfieldDimension);
		fieldActivatedClientsCount.setEditable(false);

		fieldSelectedClientsNames = new JTextField("");

		fieldSelectedClientsNames.setPreferredSize(Globals.counterfieldDimension);
		fieldSelectedClientsNames.setEditable(false);
		fieldSelectedClientsNames.setDragEnabled(true);

		// JScrollPane viewSelectedClientsNames = new
		// JScrollPane(fieldSelectedClientsNames);
		// viewSelectedClientsNames.setPreferredSize(fieldSelectedClientsNames.getMaximumSize());

		fieldInvolvedDepots = new JTextField("");
		fieldInvolvedDepots.setPreferredSize(Globals.counterfieldDimension);
		fieldInvolvedDepots.setEditable(false);
		// JScrollPane viewInvolvedDepots = new JScrollPane(fieldInvolvedDepots);
		// viewInvolvedDepots.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		// viewInvolvedDepots.setPreferredSize(fieldInvolvedDepots.getMaximumSize());

		initializeValues();

		layoutStatusPane.setHorizontalGroup(
				layoutStatusPane.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelAllClientsCount, 0, Globals.counterfieldWidth, Globals.counterfieldWidth)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize)
						.addComponent(labelActivated, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize)
						.addComponent(labelGroupActivated, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize)
						.addComponent(fieldGroupActivated, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize)
						.addComponent(labelSelectedClientsNames, 0, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize / 2)
						.addComponent(fieldSelectedClientsNames, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelSelectedClientsCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize / 2, Globals.hGapSize)
						.addComponent(fieldActivatedClientsCount, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(labelInvolvedDepots, 2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldInvolvedDepots, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(labelInvolvedDepots2, 2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize));

		layoutStatusPane.setVerticalGroup(
				layoutStatusPane.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGroup(layoutStatusPane.createSequentialGroup()
								.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
								.addGroup(layoutStatusPane.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(labelAllClientsCount, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(labelActivated, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(labelGroupActivated, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(fieldGroupActivated, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(labelSelectedClientsCount, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(fieldActivatedClientsCount, Globals.lineHeight,
												Globals.lineHeight, Globals.lineHeight)
										.addComponent(labelSelectedClientsNames, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(fieldSelectedClientsNames, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(labelInvolvedDepots, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(labelInvolvedDepots2, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight)
										.addComponent(fieldInvolvedDepots, Globals.lineHeight, Globals.lineHeight,
												Globals.lineHeight))
								.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)));

		/*
		 * csStatusPane.doForAllContainedCompisOfClass
		 * ("setOpaque", new Object[]{true}, javax.swing.JLabel.class);
		 * csStatusPane.doForAllContainedCompisOfClass
		 * ("setBackground", new Object[]{Globals.backLightBlue},
		 * javax.swing.JLabel.class);
		 */
		csStatusPane.doForAllContainedCompisOfClass("setBackground", new Object[] { Globals.backgroundLightGrey },
				javax.swing.text.JTextComponent.class);
	}
}
