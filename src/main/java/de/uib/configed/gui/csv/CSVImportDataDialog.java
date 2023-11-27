/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.csv;

import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class CSVImportDataDialog extends FGeneralDialog {
	private static final int WIDTH_LEFT_LABEL = Globals.BUTTON_WIDTH + 20;

	private PanelGenEditTable thePanel;
	private CSVFormat format;

	private JRadioButton tabsOption;
	private JRadioButton commaOption;
	private JRadioButton semicolonOption;
	private JRadioButton spaceOption;
	private JRadioButton otherOption;
	private JComboBox<Character> quoteOptions;

	private JFormattedTextField startLineInput;
	private JFormattedTextField otherDelimiterInput;

	private CSVImportDataModifier modifier;

	private int startLine = 1;

	public CSVImportDataDialog(CSVFormat format, CSVImportDataModifier modifier) {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("CSVImportDataDialog.title"), true,
				new String[] { Configed.getResourceValue("buttonCancel"), Configed.getResourceValue("buttonOK") }, 2,
				1000, 600, true);

		this.format = format;
		this.modifier = modifier;
	}

	@Override
	protected void allLayout() {
		Logging.info(this, "allLayout");

		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null) {
			centerPanel = new JPanel();
		}

		northPanel = createNorthPanel();
		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(northPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(centerPanel).addGap(Globals.GAP_SIZE)
				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE)
						.addComponent(northPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.GAP_SIZE, 2 * Globals.GAP_SIZE))
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

	private JPanel createNorthPanel() {
		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		numberFormat.setGroupingUsed(false);

		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		startLineInput = new JFormattedTextField(formatter);
		startLineInput.setText(String.valueOf(startLine));

		tabsOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.tabsOption"));
		tabsOption.setActionCommand("\t");

		commaOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.commaOption"));
		commaOption.setActionCommand(",");

		semicolonOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.semicolonOption"));
		semicolonOption.setActionCommand(";");

		spaceOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.spaceOption"));
		spaceOption.setActionCommand(" ");

		otherOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.otherOption"));
		otherOption.setActionCommand("");

		ButtonGroup delimiterOptions = new ButtonGroup();
		delimiterOptions.add(tabsOption);
		delimiterOptions.add(commaOption);
		delimiterOptions.add(semicolonOption);
		delimiterOptions.add(spaceOption);
		delimiterOptions.add(otherOption);

		MaskFormatter maskFormatter = null;
		try {
			maskFormatter = new MaskFormatter("*");
		} catch (ParseException e) {
			Logging.debug(this, "INVALID MASK");
			return null;
		}
		maskFormatter.setValidCharacters(",.-|?@~!$%&/\\=_:;+*");
		maskFormatter.setAllowsInvalid(false);
		maskFormatter.setCommitsOnValidEdit(true);
		otherDelimiterInput = new JFormattedTextField(maskFormatter);
		otherDelimiterInput.setToolTipText(Configed.getResourceValue("CSVImportDataDialog.allowedCharacters.tooltip"));
		otherDelimiterInput.setEnabled(false);

		JLabel quoteLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.stringSeparatorLabel"));
		quoteOptions = new JComboBox<>(new Character[] { '"', '\'' });
		quoteOptions.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				format = format.builder().setQuote(quoteOptions.getSelectedItem().toString().charAt(0))
						.setQuoteMode(QuoteMode.ALL).build();
				modifier.updateTable(format, startLine, thePanel);
			}
		});

		Enumeration<AbstractButton> iter = delimiterOptions.getElements();

		while (iter.hasMoreElements()) {
			AbstractButton button = iter.nextElement();

			button.addItemListener((ItemEvent e) -> {
				otherDelimiterInput.setEnabled(e.getItem() == otherOption);

				if (e.getStateChange() == ItemEvent.SELECTED && !button.getActionCommand().isEmpty()) {
					format = format.builder().setDelimiter(button.getActionCommand().charAt(0)).build();
					modifier.updateTable(format, startLine, thePanel);
				}
			});
		}

		((AbstractDocument) otherDelimiterInput.getDocument()).addDocumentListener(new InputListener() {
			@Override
			public void performAction() {
				if (!otherDelimiterInput.getText().isEmpty()) {
					format = format.builder().setDelimiter(otherDelimiterInput.getText().charAt(0)).build();
					modifier.updateTable(format, startLine, thePanel);
				}
			}
		});

		((AbstractDocument) startLineInput.getDocument()).addDocumentListener(new InputListener() {
			@Override
			public void performAction() {
				if (!startLineInput.getText().isEmpty()) {
					startLine = Integer.parseInt(startLineInput.getText());
					modifier.updateTable(format, startLine, thePanel);
				}
			}
		});

		JLabel startLineLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.startLineLabel"));

		JLabel importOptionsLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.importOptionsLabel"));

		JLabel splittingOptionsLabel = new JLabel(
				Configed.getResourceValue("CSVTemplateCreatorDialog.fieldSeparatorLabel"));

		JPanel northPanel = new JPanel();
		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		northLayout.setHorizontalGroup(northLayout.createParallelGroup()
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(importOptionsLabel, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(startLineLabel, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(startLineInput, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(splittingOptionsLabel, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL,
								WIDTH_LEFT_LABEL)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(tabsOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(commaOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(semicolonOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(spaceOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(otherOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE)
						.addComponent(otherDelimiterInput, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(northLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(quoteLabel, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(quoteOptions, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)));

		northLayout.setVerticalGroup(northLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
				.addComponent(importOptionsLabel).addGap(Globals.MIN_GAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(startLineLabel)
						.addComponent(startLineInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE).addComponent(splittingOptionsLabel).addGap(Globals.MIN_GAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(tabsOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(commaOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(semicolonOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(spaceOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(otherOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(otherDelimiterInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(quoteLabel)
						.addComponent(quoteOptions, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_GAP_SIZE));

		return northPanel;
	}

	private JPanel createSouthPanel() {
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

		return southPanel;
	}

	public void setDetectedOptions() {
		switch (format.getDelimiterString()) {
		case "\t":
			tabsOption.setSelected(true);
			break;
		case ",":
			commaOption.setSelected(true);
			break;
		case ";":
			semicolonOption.setSelected(true);
			break;
		case " ":
			spaceOption.setSelected(true);
			break;
		default:
			otherOption.setSelected(true);
			otherDelimiterInput.setText(String.valueOf(format.getDelimiterString()));
			break;
		}

		quoteOptions.setSelectedItem(format.getDelimiterString());
	}

	public JPanel initPanel() {
		// don't use a definite max table width (-1), with popups
		thePanel = new PanelGenEditTable("", true, 0, true,
				new int[] { PanelGenEditTable.POPUP_SORT_AGAIN, PanelGenEditTable.POPUP_RELOAD }, true);

		boolean updatedSuccessfull = modifier.updateTable(format, startLine, thePanel);

		if (updatedSuccessfull) {
			return thePanel;
		} else {
			return null;
		}
	}

	private static class InputListener implements DocumentListener {
		public void performAction() {
			/* Should be overridden in actual implementation */}

		@Override
		public void insertUpdate(DocumentEvent e) {
			performAction();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			performAction();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			/* Not needed */}
	}

	public CSVImportDataModifier getModifier() {
		return modifier;
	}
}
