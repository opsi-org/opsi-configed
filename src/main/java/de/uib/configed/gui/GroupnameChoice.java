package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.swing.SurroundPanel;
import de.uib.utilities.swing.VerticalPositioner;
import de.uib.utilities.swing.XList;

public class GroupnameChoice extends FGeneralDialog implements DocumentListener, ListSelectionListener {

	protected int selIndex = -1;

	protected XList groups;
	private List<String> dataList;

	protected String resultString = "";

	JTextField groupnameField;

	public GroupnameChoice(String extraTitle, List<String> v, int selectedIndex) {
		super(null, extraTitle + " (" + Globals.APPNAME + ")", true, new String[] {
				Configed.getResourceValue("FGeneralDialog.cancel"), Configed.getResourceValue("FGeneralDialog.ok") },
				300, 200);

		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		setAlwaysOnTop(true);

		dataList = v;

		groups = new XList(v);
		groups.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groups.setFont(Globals.defaultFontBig);
		groups.setBackground(Globals.BACKGROUND_COLOR_3);

		groups.addListSelectionListener(this);

		selIndex = selectedIndex;

		groupnameField = new JTextField();
		groupnameField.setFont(Globals.defaultFontBold);
		groupnameField.setPreferredSize(new Dimension(this.getWidth() * 9 / 10, Globals.LINE_HEIGHT + 5));

		groupnameField.getDocument().addDocumentListener(this);

		if (selIndex > -1 && selIndex < v.size()) {
			groups.setSelectedIndex(selIndex);
		} else {
			selIndex = -1;
		}

		if (selIndex == -1) {
			groupnameField.setText("");
		}

		JLabel labelExistingGroups = new JLabel(Configed.getResourceValue("GroupnameChoice.existingGroups") + ":");
		labelExistingGroups.setFont(Globals.defaultFontBig);
		JPanel panelExistingGroups = new JPanel(new GridLayout(1, 2));
		panelExistingGroups.add(labelExistingGroups);
		panelExistingGroups.add(new JLabel(""));

		allpane.add(

				new VerticalPositioner(new SurroundPanel(groupnameField), panelExistingGroups), BorderLayout.NORTH);

		scrollpane.getViewport().add(groups);

		super.pack();
		super.setVisible(true);
	}

	public String getResultString() {
		return resultString;
	}

	@Override
	public void doAction1() {
		result = 1;
		resultString = "";
		leave();
	}

	@Override
	public void doAction2() {
		result = -1;
		resultString = groupnameField.getText();
		leave();
	}

	protected void textvalueChanged() {

		if (dataList.contains(groupnameField.getText())) {
			groups.setSelectedValue(groupnameField.getText(), true);
			selIndex = groups.getSelectedIndex();
		} else {
			groups.clearSelection();
			selIndex = -1;
		}

	}

	// DocumentListener for Document in groupnameField
	@Override
	public void insertUpdate(DocumentEvent e) {
		textvalueChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		textvalueChanged();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		/* Not needed */}

	// ListSelectionListener
	@Override
	public void valueChanged(ListSelectionEvent e) {
		selIndex = groups.getSelectedIndex();

		if (selIndex == -1) {
			return;
		}

		if (groupnameField.getText() == null || !groupnameField.getText().equals(groups.getSelectedValue())) {
			groupnameField.setText(groups.getSelectedValue());
		}
	}

}
