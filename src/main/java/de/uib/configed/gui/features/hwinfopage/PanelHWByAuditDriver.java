/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;

public class PanelHWByAuditDriver extends JPanel {
	private String byAuditPath;

	private JRadioButton selectionBaseBoard = new JRadioButton();

	private JLabel labelInfo;
	private JLabel labelSeparator;
	private JLabel labelSeparator2;
	private JLabel labelComputerSystemVendor;
	private JLabel labelBaseBoardVendor;
	private JLabel labelProductOrModel;
	private JLabel labelProductOrModel2;

	private JButton buttonUploadDrivers;
	private JRadioButton selectionComputerSystem;

	private JTextField fieldComputerSystemVendor = new JTextField();
	private JTextField fieldComputerSystemLabel = new JTextField();

	private JTextField fieldBaseBoardVendor = new JTextField();
	private JTextField fieldBaseBoardLabel = new JTextField();

	private DriverUploadDialog driverUploadDialog;
	private ConfigedMain configedMain;

	public PanelHWByAuditDriver(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		initComponents();
		setupLayout();
	}

	private void initComponents() {
		labelInfo = new JLabel(Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabels"));
		labelSeparator = new JLabel(" / ");
		labelSeparator2 = new JLabel(" / ");
		labelComputerSystemVendor = new JLabel(
				Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsComputerSystemVendor"));
		labelBaseBoardVendor = new JLabel(
				Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsBaseBoardVendor"));
		labelProductOrModel = new JLabel(
				Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsProductOrModel"));
		labelProductOrModel2 = new JLabel(
				Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsProductOrModel"));

		buttonUploadDrivers = new JButton(Configed.getResourceValue("FDriverUpload.title"));
		buttonUploadDrivers.addActionListener(actionEvent -> startDriverUploadFrame());

		selectionComputerSystem = new JRadioButton("", true);
		ButtonGroup selectionGroup = new ButtonGroup();
		selectionGroup.add(selectionComputerSystem);
		selectionGroup.add(selectionBaseBoard);

		fieldComputerSystemVendor.setEditable(false);
		fieldComputerSystemLabel.setEditable(false);
		fieldBaseBoardVendor.setEditable(false);
		fieldBaseBoardLabel.setEditable(false);
	}

	private void setupLayout() {
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
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addComponent(labelComputerSystemVendor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldComputerSystemVendor, 0, 0, Short.MAX_VALUE)
						.addComponent(labelBaseBoardVendor, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldBaseBoardVendor, 0, 0, Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutByAuditInfo
						.createParallelGroup().addComponent(labelSeparator).addComponent(labelSeparator2))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addComponent(labelProductOrModel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldComputerSystemLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(labelProductOrModel2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(fieldBaseBoardLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE));
	}

	public void emptyByAuditStrings() {
		byAuditPath = "";
		fieldComputerSystemVendor.setText("");
		fieldComputerSystemLabel.setText("");
		fieldBaseBoardVendor.setText("");
		fieldBaseBoardLabel.setText("");

		if (driverUploadDialog != null) {
			driverUploadDialog.setUploadParameters("");
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

		if (driverUploadDialog != null) {
			driverUploadDialog.setUploadParameters(byAuditPath);
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

		if (driverUploadDialog == null) {
			driverUploadDialog = new DriverUploadDialog(configedMain);
		}

		driverUploadDialog.show();

		driverUploadDialog.setUploadParameters(byAuditPath);
	}
}
