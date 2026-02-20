/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.csv;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.text.NumberFormatter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.share.swing.PopupMenuTrait;
import de.uib.configed.gui.share.table.gui.PanelGenEdit;
import de.uib.configed.gui.share.table.gui.PanelGenEditPopupManager;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class CSVImportDataDialog {
	private PanelGenEdit thePanel;
	private CSVFormat format;

	private JRadioButton tabsOption;
	private JRadioButton commaOption;
	private JRadioButton semicolonOption;
	private JRadioButton spaceOption;
	private JRadioButton otherOption;
	private JComboBox<Character> quoteOptions;

	private JFormattedTextField otherDelimiterInput;

	private CSVImportDataModifier modifier;

	private int startLine = 1;

	private JOptionPane optionPane;
	private JDialog dialog;

	public CSVImportDataDialog(CSVFormat format, CSVImportDataModifier modifier) {
		this.format = format;
		this.modifier = modifier;

		JPanel panel = createPanel();

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		Utils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("CSVImportDataDialog.title"));
		dialog.setMinimumSize(new Dimension());
		dialog.pack();

		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
	}

	private JPanel createPanel() {
		JFormattedTextField startLineInput = createStartLineTextField();

		tabsOption = createOption(Configed.getResourceValue("CSVImportDataDialog.tabsOption"), "\t");
		commaOption = createOption(Configed.getResourceValue("CSVImportDataDialog.commaOption"), ",");
		semicolonOption = createOption(Configed.getResourceValue("CSVImportDataDialog.semicolonOption"), ";");
		spaceOption = createOption(Configed.getResourceValue("CSVImportDataDialog.spaceOption"), " ");
		otherOption = createOption(Configed.getResourceValue("CSVImportDataDialog.otherOption"), "");

		otherDelimiterInput = new JFormattedTextField(CSVTemplateCreatorDialog.createMaskFormatter());
		otherDelimiterInput.setToolTipText(Configed.getResourceValue("CSVImportDataDialog.allowedCharacters.tooltip"));
		otherDelimiterInput.setEnabled(false);

		quoteOptions = new JComboBox<>(new Character[] { '"', '\'' });
		quoteOptions.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				format = format.builder().setQuote(quoteOptions.getSelectedItem().toString().charAt(0))
						.setQuoteMode(QuoteMode.ALL).get();
				modifier.updateTable(format, startLine, thePanel);
			}
		});

		ButtonGroup delimiterOptions = new ButtonGroup();
		for (AbstractButton button : List.of(tabsOption, commaOption, semicolonOption, spaceOption, otherOption)) {
			delimiterOptions.add(button);
			button.addItemListener((ItemEvent e) -> {
				otherDelimiterInput.setEnabled(e.getItem() == otherOption);

				if (e.getStateChange() == ItemEvent.SELECTED && !button.getActionCommand().isEmpty()) {
					format = format.builder().setDelimiter(button.getActionCommand().charAt(0)).get();
					modifier.updateTable(format, startLine, thePanel);
				}
			});
		}

		otherDelimiterInput.getDocument().addDocumentListener(Utils.onDocumentChangeWithoutRemoveUpdate(() -> {
			if (!otherDelimiterInput.getText().isEmpty()) {
				format = format.builder().setDelimiter(otherDelimiterInput.getText().charAt(0)).get();
				modifier.updateTable(format, startLine, thePanel);
			}
		}));

		startLineInput.getDocument().addDocumentListener(Utils.onDocumentChangeWithoutRemoveUpdate(() -> {
			if (!startLineInput.getText().isEmpty()) {
				startLine = Integer.parseInt(startLineInput.getText());
				modifier.updateTable(format, startLine, thePanel);
			}
		}));

		JLabel quoteLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.stringSeparatorLabel"));
		JLabel startLineLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.startLineLabel"));
		JLabel importOptionsLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.importOptionsLabel"));
		JLabel splittingOptionsLabel = new JLabel(
				Configed.getResourceValue("CSVTemplateCreatorDialog.fieldSeparatorLabel"));

		// don't use a definite max table width (-1), with popups
		thePanel = new PanelGenEdit("", true, 0,
				new int[] { PanelGenEditPopupManager.POPUP_SORT_AGAIN, PopupMenuTrait.POPUP_RELOAD }, true);

		modifier.updateTable(format, startLine, thePanel);

		JPanel panel = new JPanel(new MigLayout("insets 0, wrap 1", "[grow]",
				"[pref!]" + Globals.MIN_GAP_SIZE + "[pref!]" + Globals.MIN_GAP_SIZE + "[pref!]" + Globals.MIN_GAP_SIZE
						+ "[pref!]" + Globals.MIN_GAP_SIZE + "[pref!]" + Globals.MIN_GAP_SIZE + "[grow]"
						+ Globals.MIN_GAP_SIZE));

		panel.add(importOptionsLabel);

		panel.add(startLineLabel, "split 2, gapright " + Globals.GAP_SIZE);
		panel.add(startLineInput, "growx, pushx, wrap");

		panel.add(splittingOptionsLabel);

		panel.add(tabsOption, "split 6");
		panel.add(commaOption, "gapleft " + Globals.GAP_SIZE);
		panel.add(semicolonOption, "gapleft " + Globals.GAP_SIZE);
		panel.add(spaceOption, "gapleft " + Globals.GAP_SIZE);
		panel.add(otherOption, "gapleft " + Globals.GAP_SIZE);
		panel.add(otherDelimiterInput, "gapleft " + Globals.GAP_SIZE + ", growx, pushx, wrap");

		panel.add(quoteLabel, "split 2, gapright " + Globals.GAP_SIZE);
		panel.add(quoteOptions, "growx, pushx, wrap");

		panel.add(thePanel, "grow, hmin 0");

		return panel;
	}

	private static JRadioButton createOption(String label, String actionCommand) {
		JRadioButton option = new JRadioButton(label);
		option.setActionCommand(actionCommand);
		return option;
	}

	private JFormattedTextField createStartLineTextField() {
		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		numberFormat.setGroupingUsed(false);

		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		JFormattedTextField startLineTextField = new JFormattedTextField(formatter);
		startLineTextField.setText(String.valueOf(startLine));

		return startLineTextField;
	}

	public void setDetectedOptions() {
		switch (format.getDelimiterString()) {
		case "\t" -> tabsOption.setSelected(true);
		case "," -> commaOption.setSelected(true);
		case ";" -> semicolonOption.setSelected(true);
		case " " -> spaceOption.setSelected(true);
		default -> {
			otherOption.setSelected(true);
			otherDelimiterInput.setText(String.valueOf(format.getDelimiterString()));
		}
		}

		quoteOptions.setSelectedItem(format.getDelimiterString());
	}

	public CSVImportDataModifier getModifier() {
		return modifier;
	}

	public static CSVImportDataDialog createCSVImportDataDialog(Component parent, String csvFile) {
		Logging.info("createCSVImportDataDialog for file ", csvFile);
		Set<String> columnNames = HostInfo.getKeysForCSV();
		CSVFormatDetector csvFormatDetector = new CSVFormatDetector();
		try {
			csvFormatDetector.detectFormat(csvFile);
			if (csvFormatDetector.hasHeader() && !csvFormatDetector.hasExpectedHeaderNames(columnNames)) {
				JOptionPane.showMessageDialog(parent,
						Configed.getResourceValue("CSVImportDataDialog.infoExpectedHeaderNames.message") + " "
								+ columnNames.toString().replace("[", "").replace("]", ""),
						Configed.getResourceValue("CSVImportDataDialog.infoExpectedHeaderNames.title"),
						JOptionPane.ERROR_MESSAGE);

				return null;
			}
		} catch (IOException e) {
			Logging.error(e, "Unable to detect format of CSV file");
		}

		CSVFormat format = CSVFormat.DEFAULT.builder().setDelimiter(csvFormatDetector.getDelimiter())
				.setQuote(csvFormatDetector.getQuote()).setCommentMarker('#').setHeader()
				.setIgnoreSurroundingSpaces(true).setQuoteMode(QuoteMode.ALL).setSkipHeaderRecord(true).get();
		CSVImportDataModifier modifier = new CSVImportDataModifier(csvFile, columnNames);
		CSVImportDataDialog csvImportDataDialog = new CSVImportDataDialog(format, modifier);
		csvImportDataDialog.setDetectedOptions();

		return csvImportDataDialog;
	}

	public boolean show() {
		dialog.setVisible(true);

		return optionPane.getValue() == (Integer) JOptionPane.OK_OPTION;
	}
}
