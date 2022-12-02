package de.uib.configed.gui;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * FDialogTextfieldWithListSelectio
 * Copyright:     Copyright (c) 2022
 * Organisation:  uib
 * @author Rupert Röder
 */
import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.list.StandardListCellRenderer;

public class FDialogTextfieldWithListSelection extends FGeneralDialog
		implements ListSelectionListener

{
	JList<String> theList;
	JScrollPane scrollpaneForList;
	JTextField theField;
	JLabel labelField;
	JLabel labelList;
	String selectedListElement = null;
	String resultingText = null;

	public FDialogTextfieldWithListSelection(JFrame owner, String title, boolean modal, String[] buttonList,
			Icon[] icons, int lastButtonNo, int preferredWidth, int preferredHeight,
			boolean lazyLayout, JPanel addPane) {
		super(owner, title, modal, buttonList, icons, lastButtonNo, preferredWidth, preferredHeight, lazyLayout,
				addPane);
	}

	public void applyFraming(FramingTextfieldWithListselection defs) {
		setTitle(defs.getTitle());

		// theField = new JTextField("user1");
		labelField = new JLabel(defs.getTextfieldLabel());
		// labelField.setText( defs.getTextfieldLabel() );
		labelList = new JLabel(defs.getListLabel());
		// labelList.setText( defs.getListLabel() );
		labelList.setToolTipText(defs.getListLabelToolTip());
		theList = new JList();
		theList.setListData(defs.getListData());
		if (defs.getListData() != null && defs.getListData().size() > 0)
			theList.setSelectedIndex(0);
		theList.addListSelectionListener(this);
	}

	@Override
	protected void allLayout() {
		// super.allLayout();
		// we could design an adapted layout and infuse it in guiInit
		logging.info(this, "allLayout");

		allpane.setBackground(de.uib.configed.Globals.backLightBlue); // Globals.nimbusBackground);///Globals.backgroundWhite);
																		// //Globals.backLighter);//Globals.backgroundWhite);//(myHintYellow);
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null)
			centerPanel = new JPanel();

		centerPanel.setBackground(Globals.backLightBlue);// Color.white);
		// centerPanel.setOpaque(true);

		southPanel = new JPanel();

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.lineHeight, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE)// GroupLayout.PREFERRED_SIZE)//Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				// .addComponent(additionalPane, 300, 300, Short.MAX_VALUE )
				.addComponent(additionalPane, Globals.lineHeight, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(jPanelButtonGrid, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2));

		// southPanel.setOpaque(true);
		southPanel.setBackground(Globals.backLightBlue);// Color.white );

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addGap(Globals.hGapSize)
				.addComponent(centerPanel, 100, 200, Short.MAX_VALUE)
				.addGap(Globals.hGapSize)
				// .addComponent(southPanel,300, 300, Short.MAX_VALUE)
				.addComponent(southPanel, Globals.lineHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.hGapSize));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(southPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize)));

	}

	public void setListVisible(boolean b) {
		scrollpaneForList.setVisible(b);
		labelList.setVisible(b);
	}

	public JPanel initPanel() {
		JPanel thePanel = new JPanel();
		thePanel.setBackground(Globals.backLightBlue);
		// thePanel.setOpaque( true );

		theList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		theList.addListSelectionListener(this);
		theList.setCellRenderer(new StandardListCellRenderer());

		theField = new JTextField();

		scrollpaneForList = new javax.swing.JScrollPane();
		scrollpaneForList.setViewportView(theList);
		// scrollpaneForList.getViewport().setBackground(Globals.backLightBlue );

		GroupLayout theLayout = new GroupLayout(thePanel);
		thePanel.setLayout(theLayout);

		theLayout.setVerticalGroup(theLayout.createSequentialGroup()
				.addGap(Globals.vGapSize)
				.addGroup(theLayout.createParallelGroup()
						.addComponent(labelField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(theField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight))
				.addGap(Globals.vGapSize)
				.addGroup(theLayout.createParallelGroup()
						.addComponent(labelList, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
						.addComponent(scrollpaneForList, Globals.lineHeight, Globals.lineHeight, Short.MAX_VALUE))

				.addGap(Globals.vGapSize));

		theLayout.setHorizontalGroup(theLayout.createParallelGroup()
				.addGroup(theLayout.createSequentialGroup()
						.addGap(Globals.hGapSize)
						.addComponent(labelField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)// Globals.labelWidth, Globals.labelWidth *2,
															// Globals.labelWidth *2 )
						.addGap(Globals.hGapSize)
						.addGap(Globals.hGapSize)
						.addComponent(theField, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE)
						.addGap(Globals.hGapSize)
						.addGap(Globals.hGapSize))
				.addGroup(theLayout.createSequentialGroup()
						.addGap(Globals.hGapSize)
						.addComponent(labelList, Globals.labelWidth, Globals.labelWidth * 2, Globals.labelWidth * 2)
						.addComponent(scrollpaneForList, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE)
						.addGap(Globals.hGapSize)
						.addGap(Globals.hGapSize))

				.addGap(Globals.vGapSize));

		return thePanel;

	}

	@Override
	protected void preAction1() {
		resultingText = theField.getText();
		selectedListElement = theList.getSelectedValue();

		// logging.info(this, "preAction1 resultingText, selectedListElement " +
		// resultingText + ", " + selectedListElement);
	}

	@Override
	public int getResult() {
		// logging.info(this, "got result resultingText, selectedListElement "
		// + theField.getText() + ", " + theList.getSelectedValue());
		// values are not got at this point
		// override doAction for getting the values

		return super.getResult();
	}

	public String getResultText() {
		return resultingText;
	}

	public String getSelectedListelement() {
		return selectedListElement;
	}

	// ======================
	// interface ListSelectionListener
	public void valueChanged(ListSelectionEvent e) {
		// logging.info(this, "valueChanged " + e);
		// selectedListElement = theList.getSelectedValue();
	}
	// ======================

	public static void main(String[] args) {
		FDialogTextfieldWithListSelection f = new FDialogTextfieldWithListSelection(
				null, // owner frame
				"test FGeneralDialogTest", // title
				false, // modal

				new String[] {
						"ok",
						"cancel"
				},

				new Icon[] {
						de.uib.configed.Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						de.uib.configed.Globals.createImageIcon("images/cancel16_small.png", "")
				},
				1, // lastButtonNo,with "1" we get only the first button
				600, 600,
				true, // lazylayout, i.e, we have a chance to define components and use them for the
						// layout
				null // addPanel predefined
		);

		JPanel centerPanel = f.initPanel();
		// JPanel addPanel = new JPanel();
		// addPanel.setBackground( Color.BLUE );

		// f.setCenterPaneInScrollpane( centerPanel );
		f.setCenterPane(centerPanel);

		// f.setAdditionalPane( addPanel );

		f.setSize(new Dimension(500, 600));

		f.setupLayout();
		f.setVisible(true);
	}

}
