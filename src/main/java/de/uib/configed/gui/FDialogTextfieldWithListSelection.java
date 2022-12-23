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

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.list.StandardListCellRenderer;

public class FDialogTextfieldWithListSelection extends FGeneralDialog implements ListSelectionListener

{
	JList<String> theList;
	JScrollPane scrollpaneForList;
	JTextField theField;
	JLabel labelField;
	JLabel labelList;
	String selectedListElement = null;
	String resultingText = null;

	public FDialogTextfieldWithListSelection(JFrame owner, String title, boolean modal, String[] buttonList,
			Icon[] icons, int lastButtonNo, int preferredWidth, int preferredHeight, boolean lazyLayout,
			JPanel addPane) {
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
		theList = new JList<>();
		theList.setListData(defs.getListData());
		if (defs.getListData() != null && !defs.getListData().isEmpty())
			theList.setSelectedIndex(0);
		theList.addListSelectionListener(this);
	}

	@Override
	protected void allLayout() {
		// super.allLayout();
		// we could design an adapted layout and infuse it in guiInit
		logging.info(this, "allLayout");

		allpane.setBackground(Globals.backLightBlue); // Globals.nimbusBackground);///Globals.backgroundWhite);
														// //Globals.backLighter);//Globals.backgroundWhite);
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
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE / 2)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE)// GroupLayout.PREFERRED_SIZE)//Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				// .addComponent(additionalPane, 300, 300, Short.MAX_VALUE )
				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		// southPanel.setOpaque(true);
		southPanel.setBackground(Globals.backLightBlue);// Color.white );

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
				.addComponent(centerPanel, 100, 200, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE)
				// .addComponent(southPanel,300, 300, Short.MAX_VALUE)
				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(southPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)));

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

		theLayout.setVerticalGroup(theLayout.createSequentialGroup().addGap(Globals.VGAP_SIZE)
				.addGroup(theLayout.createParallelGroup()
						.addComponent(labelField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(theField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(Globals.VGAP_SIZE)
				.addGroup(theLayout.createParallelGroup()
						.addComponent(labelList, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(scrollpaneForList, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Short.MAX_VALUE))

				.addGap(Globals.VGAP_SIZE));

		theLayout.setHorizontalGroup(theLayout.createParallelGroup()
				.addGroup(theLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
						.addComponent(labelField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)// Globals.labelWidth, Globals.labelWidth *2,
						// Globals.labelWidth *2 )
						.addGap(Globals.HGAP_SIZE).addGap(Globals.HGAP_SIZE)
						.addComponent(theField, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE).addGap(Globals.HGAP_SIZE))
				.addGroup(theLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
						.addComponent(labelList, Globals.LABEL_WIDTH, Globals.LABEL_WIDTH * 2, Globals.LABEL_WIDTH * 2)
						.addComponent(scrollpaneForList, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE).addGap(Globals.HGAP_SIZE))

				.addGap(Globals.VGAP_SIZE));

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
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// logging.info(this, "valueChanged " + e);
		// selectedListElement = theList.getSelectedValue();
	}
	// ======================

	public static void main(String[] args) {
		FDialogTextfieldWithListSelection f = new FDialogTextfieldWithListSelection(null, // owner frame
				"test FGeneralDialogTest", // title
				false, // modal

				new String[] { "ok", "cancel" },

				new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "") },
				1, // lastButtonNo,with "1" we get only the first button
				600, 600, true, // lazylayout, i.e, we have a chance to define components and use them for the
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
