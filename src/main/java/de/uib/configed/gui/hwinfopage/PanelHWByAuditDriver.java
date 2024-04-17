/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;

public class PanelHWByAuditDriver extends JPanel {
	private String byAuditPath;

	private JRadioButton selectionBaseBoard = new JRadioButton();

	private JTextField fieldComputerSystemVendor = new JTextField();
	private JTextField fieldComputerSystemLabel = new JTextField();

	private JTextField fieldBaseBoardVendor = new JTextField();
	private JTextField fieldBaseBoardLabel = new JTextField();

	private FDriverUpload fDriverUpload;
	private ConfigedMain configedMain;

	public PanelHWByAuditDriver(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		buildPanel();
	}

	private void buildPanel() {
		JLabel labelInfo = new JLabel(Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabels"));
		JLabel labelSeparator = new JLabel(" / ");
		JLabel labelSeparator2 = new JLabel(" / ");
		JLabel labelComputerSystemVendor = new JLabel(
				Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsComputerSystemVendor"));
		JLabel labelBaseBoardVendor = new JLabel(
				Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsBaseBoardVendor"));
		JLabel labelProductOrModel = new JLabel(
				Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsProductOrModel"));
		JLabel labelProductOrModel2 = new JLabel(
				Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsProductOrModel"));

		JButton buttonUploadDrivers = new JButton(Configed.getResourceValue("FDriverUpload.title"));
		buttonUploadDrivers.addActionListener(actionEvent -> startDriverUploadFrame());

		JRadioButton selectionComputerSystem = new JRadioButton("", true);
		ButtonGroup selectionGroup = new ButtonGroup();
		selectionGroup.add(selectionComputerSystem);
		selectionGroup.add(selectionBaseBoard);

		fieldComputerSystemVendor.setEditable(false);
		fieldComputerSystemLabel.setEditable(false);
		fieldBaseBoardVendor.setEditable(false);
		fieldBaseBoardLabel.setEditable(false);

		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);

		layoutByAuditInfo
				.setVerticalGroup(
						layoutByAuditInfo.createSequentialGroup()
								.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(labelInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(labelComputerSystemVendor, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(labelProductOrModel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(selectionComputerSystem, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldComputerSystemVendor, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(labelSeparator, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldComputerSystemLabel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.MIN_GAP_SIZE)
								.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(labelBaseBoardVendor, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(labelProductOrModel2, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(buttonUploadDrivers, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(selectionBaseBoard, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldBaseBoardVendor, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(labelSeparator2, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(fieldBaseBoardLabel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

		layoutByAuditInfo.setHorizontalGroup(layoutByAuditInfo.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addComponent(labelInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonUploadDrivers, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addComponent(selectionComputerSystem, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(selectionBaseBoard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addComponent(labelComputerSystemVendor, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(fieldComputerSystemVendor, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(labelBaseBoardVendor, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(fieldBaseBoardVendor, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutByAuditInfo
						.createParallelGroup().addComponent(labelSeparator).addComponent(labelSeparator2))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addComponent(labelProductOrModel, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(fieldComputerSystemLabel, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(labelProductOrModel2, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(fieldBaseBoardLabel, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2))
				.addGap(2 * Globals.MIN_GAP_SIZE, 4 * Globals.MIN_GAP_SIZE, Short.MAX_VALUE));
	}

	public void emptyByAuditStrings() {
		byAuditPath = "";
		fieldComputerSystemVendor.setText("");
		fieldComputerSystemLabel.setText("");
		fieldBaseBoardVendor.setText("");
		fieldBaseBoardLabel.setText("");

		if (fDriverUpload != null) {
			fDriverUpload.setUploadParameters("");
		}
	}

	private static String eliminateIllegalPathChars(String path) {
		if (path == null) {
			return null;
		}

		final String TO_REPLACE = "<>?\":|\\/*";
		final char REPLACEMENT = '_';

		char[] chars = path.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (TO_REPLACE.indexOf(chars[i]) > -1) {
				chars[i] = REPLACEMENT;
			}
		}

		// requires bootimage >= 4.0.6
		if (chars.length > 0 && (chars[chars.length - 1] == '.' || chars[chars.length - 1] == ' ')) {
			chars[chars.length - 1] = REPLACEMENT;
		}

		return new String(chars);
	}

	public void setByAuditFields(String vendorStringComputerSystem, String vendorStringBaseBoard, String modelString,
			String productString) {
		fieldComputerSystemVendor.setText(vendorStringComputerSystem);
		fieldComputerSystemLabel.setText(modelString);

		fieldBaseBoardVendor.setText(vendorStringBaseBoard);
		fieldBaseBoardLabel.setText(productString);

		if (fDriverUpload != null) {
			fDriverUpload.setUploadParameters(byAuditPath);
		}
	}

	private void startDriverUploadFrame() {
		if (selectionBaseBoard.isSelected()) {
			byAuditPath = eliminateIllegalPathChars(fieldBaseBoardVendor.getText()) + "/"
					+ eliminateIllegalPathChars(fieldBaseBoardLabel.getText());
		} else {
			byAuditPath = eliminateIllegalPathChars(fieldComputerSystemVendor.getText()) + "/"
					+ eliminateIllegalPathChars(fieldComputerSystemLabel.getText());
		}

		if (fDriverUpload == null) {
			fDriverUpload = new FDriverUpload(configedMain);
		}

		fDriverUpload.setSize(Globals.HELPER_FORM_DIMENSION);
		fDriverUpload.setVisible(true);
		fDriverUpload.centerOnParent();

		fDriverUpload.setUploadParameters(byAuditPath);
	}
}
