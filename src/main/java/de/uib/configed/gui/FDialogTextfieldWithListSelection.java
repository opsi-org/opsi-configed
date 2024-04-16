/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import de.uib.configed.Globals;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.list.StandardListCellRenderer;

public class FDialogTextfieldWithListSelection extends FGeneralDialog {
	private JList<String> theList;
	private JScrollPane scrollpaneForList;
	private JTextField theField;
	private JLabel labelField;
	private JLabel labelList;

	public FDialogTextfieldWithListSelection(JFrame owner, String title, boolean modal, String[] buttonList,
			int lastButtonNo, int preferredWidth, int preferredHeight, boolean lazyLayout) {
		super(owner, title, modal, buttonList, lastButtonNo, preferredWidth, preferredHeight, lazyLayout);
	}

	public void applyFraming(FramingTextfieldWithListselection defs) {
		setTitle(defs.getTitle());

		labelField = new JLabel(defs.getTextfieldLabel());

		labelList = new JLabel(defs.getListLabel());

		labelList.setToolTipText(defs.getListLabelToolTip());
		theList = new JList<>();
		theList.setListData(defs.getListData().toArray(new String[0]));
		if (defs.getListData() != null && !defs.getListData().isEmpty()) {
			theList.setSelectedIndex(0);
		}
	}

	@Override
	protected void allLayout() {
		// we could design an adapted layout and infuse it in guiInit
		Logging.info(this, "allLayout");

		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null) {
			centerPanel = new JPanel();
		}

		southPanel = new JPanel();

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(Alignment.LEADING).addGroup(southLayout
				.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE).addGap(Globals.MIN_GAP_SIZE)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)

				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.MIN_GAP_SIZE));

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(centerPanel, 100, 200, Short.MAX_VALUE).addGap(Globals.GAP_SIZE)

				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE).addComponent(southPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)));
	}

	public void setListVisible(boolean b) {
		scrollpaneForList.setVisible(b);
		labelList.setVisible(b);
	}

	public JPanel initPanel() {
		JPanel thePanel = new JPanel();

		theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		theList.setCellRenderer(new StandardListCellRenderer());

		theField = new JTextField();

		scrollpaneForList = new JScrollPane();
		scrollpaneForList.setViewportView(theList);

		GroupLayout theLayout = new GroupLayout(thePanel);
		thePanel.setLayout(theLayout);

		theLayout.setVerticalGroup(theLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(theLayout.createParallelGroup()
						.addComponent(labelField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(theField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(theLayout.createParallelGroup()
						.addComponent(labelList, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(scrollpaneForList, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Short.MAX_VALUE))

				.addGap(Globals.GAP_SIZE));

		theLayout.setHorizontalGroup(theLayout.createParallelGroup()
				.addGroup(theLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(labelField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

						.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
						.addComponent(theField, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE))
				.addGroup(theLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(labelList, Globals.LABEL_WIDTH, Globals.LABEL_WIDTH * 2, Globals.LABEL_WIDTH * 2)
						.addComponent(scrollpaneForList, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE))

				.addGap(Globals.GAP_SIZE));

		return thePanel;
	}

	public String getResultText() {
		return theField.getText();
	}

	public String getSelectedListelement() {
		return theList.getSelectedValue();
	}
}
