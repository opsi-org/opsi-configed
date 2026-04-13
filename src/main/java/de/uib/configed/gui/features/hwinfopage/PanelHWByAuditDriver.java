/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of OPSI - https://www.opsi.org
 */

package de.uib.configed.gui.features.hwinfopage;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import net.miginfocom.swing.MigLayout;

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
		setLayout(new MigLayout("insets 0", "[pref!]" + Globals.MIN_GAP_SIZE + "[pref!]" + Globals.GAP_SIZE + "[]"
				+ Globals.GAP_SIZE + "[pref!]" + Globals.GAP_SIZE + "[]", "[]0[]" + Globals.MIN_GAP_SIZE + "[]0[]"));

		add(labelInfo, "cell 0 0");
		add(labelComputerSystemVendor, "cell 2 0");
		add(labelProductOrModel, "cell 4 0");

		add(selectionComputerSystem, "cell 1 1");
		add(fieldComputerSystemVendor, "cell 2 1, growx");
		add(labelSeparator, "cell 3 1");
		add(fieldComputerSystemLabel, "cell 4 1, growx");

		add(labelBaseBoardVendor, "cell 2 3");
		add(labelProductOrModel2, "cell 4 3");

		add(buttonUploadDrivers, "cell 0 4");
		add(selectionBaseBoard, "cell 1 4");
		add(fieldBaseBoardVendor, "cell 2 4, growx");
		add(labelSeparator2, "cell 3 4");
		add(fieldBaseBoardLabel, "cell 4 4, growx");
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
