/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.hwinfopage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.utilities.swing.JTextShowField;
import utils.Utils;

public class PanelHWByAuditDriver extends JPanel {

	private String byAuditPath;

	private JRadioButton selectionBaseBoard;

	private JTextField fieldVendor;
	private JTextField fieldLabel;

	private JTextField fieldVendor2;
	private JTextField fieldLabel2;

	private FDriverUpload fDriverUpload;
	private ConfigedMain configedMain;

	public PanelHWByAuditDriver(ConfigedMain configedMain) {
		this.configedMain = configedMain;
		buildPanel();
	}

	private void buildPanel() {

		fieldVendor = new JTextShowField();
		if (!Main.THEMES) {
			fieldVendor.setBackground(Globals.BACKGROUND_COLOR_3);
		}
		fieldLabel = new JTextShowField();
		if (!Main.THEMES) {
			fieldLabel.setBackground(Globals.BACKGROUND_COLOR_3);
		}

		fieldVendor2 = new JTextShowField();
		if (!Main.THEMES) {
			fieldVendor2.setBackground(Globals.BACKGROUND_COLOR_3);
		}

		fieldLabel2 = new JTextShowField();
		if (!Main.THEMES) {
			fieldLabel2.setBackground(Globals.BACKGROUND_COLOR_3);
		}

		JLabel labelInfo = new JLabel(Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabels"));

		JLabel labelSeparator = new JLabel(" / ");
		JLabel labelSeparator2 = new JLabel(" / ");
		JLabel labelVendor = new JLabel(Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsVendor"));
		JLabel labelProduct = new JLabel(Configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsProduct"));

		JButton buttonUploadDrivers = new JButton("", Utils.createImageIcon("images/upload2product.png", ""));
		buttonUploadDrivers.setSelectedIcon(Utils.createImageIcon("images/upload2product.png", ""));
		buttonUploadDrivers.setToolTipText(Configed.getResourceValue("PanelHWInfo.uploadDrivers"));

		buttonUploadDrivers.addActionListener(actionEvent -> startDriverUploadFrame());

		JRadioButton selectionComputerSystem = new JRadioButton("", true);
		selectionBaseBoard = new JRadioButton("");
		ButtonGroup selectionGroup = new ButtonGroup();
		selectionGroup.add(selectionComputerSystem);
		selectionGroup.add(selectionBaseBoard);

		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);
		int lh = Globals.LINE_HEIGHT - 4;
		layoutByAuditInfo
				.setVerticalGroup(
						layoutByAuditInfo.createSequentialGroup()
								.addGroup(layoutByAuditInfo.createParallelGroup().addComponent(labelInfo, lh, lh, lh)
										.addComponent(labelVendor, lh, lh, lh).addComponent(labelProduct, lh, lh, lh)
										.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
										.addComponent(buttonUploadDrivers, lh, lh, lh))
								.addGroup(layoutByAuditInfo.createParallelGroup()
										.addGroup(layoutByAuditInfo.createSequentialGroup()
												.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
														Globals.MIN_GAP_SIZE)
												.addComponent(selectionComputerSystem))
										.addComponent(fieldVendor, lh, lh, lh).addComponent(labelSeparator, lh, lh, lh)
										.addComponent(fieldLabel, lh, lh, lh)

								).addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
								.addGroup(layoutByAuditInfo.createParallelGroup()

										.addGroup(layoutByAuditInfo.createSequentialGroup()
												.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE,
														Globals.MIN_GAP_SIZE)
												.addComponent(selectionBaseBoard))
										.addComponent(fieldVendor2, lh, lh, lh)
										.addComponent(labelSeparator2, lh, lh, lh).addComponent(fieldLabel2, lh, lh, lh)

								).addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));

		layoutByAuditInfo.setHorizontalGroup(layoutByAuditInfo.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE * 2, Globals.MIN_GAP_SIZE * 2, Globals.MIN_GAP_SIZE * 2)
				.addComponent(labelInfo, 5, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2, Globals.MIN_GAP_SIZE * 4, Globals.MIN_GAP_SIZE * 4)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addComponent(selectionComputerSystem, 2, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(selectionBaseBoard, 2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addGroup(layoutByAuditInfo.createSequentialGroup().addGap(2, 2, 2).addComponent(labelVendor,
								Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2))
						.addComponent(fieldVendor, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(fieldVendor2, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2))
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup().addComponent(labelSeparator)
						.addComponent(labelSeparator2))
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addGroup(layoutByAuditInfo.createParallelGroup()
						.addGroup(layoutByAuditInfo.createSequentialGroup().addGap(2, 2, 2).addComponent(labelProduct,
								Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2))
						.addComponent(fieldLabel, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2)
						.addComponent(fieldLabel2, Globals.BUTTON_WIDTH / 2, Globals.BUTTON_WIDTH,
								Globals.BUTTON_WIDTH * 2))

				.addGap(5 * Globals.MIN_GAP_SIZE, 10 * Globals.MIN_GAP_SIZE, 10 * Globals.MIN_GAP_SIZE)
				.addComponent(buttonUploadDrivers, Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH,
						Globals.GRAPHIC_BUTTON_WIDTH)
				.addGap(2 * Globals.MIN_GAP_SIZE, 4 * Globals.MIN_GAP_SIZE, Short.MAX_VALUE));
		if (!Main.THEMES) {
			setBackground(Globals.BACKGROUND_COLOR_7);
		}

		if (!Main.THEMES) {
			setBorder(BorderFactory.createLineBorder(Globals.GREYED));
		}
	}

	public void emptyByAuditStrings() {
		byAuditPath = "";
		fieldVendor.setText("");
		fieldLabel.setText("");
		fieldVendor2.setText("");
		fieldLabel2.setText("");

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
		fieldVendor.setText(vendorStringComputerSystem);
		fieldLabel.setText(modelString);

		fieldVendor2.setText(vendorStringBaseBoard);
		fieldLabel2.setText(productString);

		if (fDriverUpload != null) {
			fDriverUpload.setUploadParameters(byAuditPath);
		}
	}

	private void startDriverUploadFrame() {
		if (selectionBaseBoard.isSelected()) {
			byAuditPath = eliminateIllegalPathChars(fieldVendor2.getText()) + "/"
					+ eliminateIllegalPathChars(fieldLabel2.getText());
		} else {
			byAuditPath = eliminateIllegalPathChars(fieldVendor.getText()) + "/"
					+ eliminateIllegalPathChars(fieldLabel.getText());
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
