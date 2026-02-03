/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.csv;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.AbstractDocument;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import de.uib.configed.gui.CheckBoxList;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.ConfigedMain;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.type.HostInfo;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class CSVTemplateCreatorDialog {
	private static CSVTemplateCreatorDialog csvTemplateCreatorDialog;

	private CSVFormat format;

	private JCheckBox includeFormatHintOption;

	private JFormattedTextField otherDelimiterInput;

	private Set<String> columnNames;
	private List<JCheckBox> headerCheckBoxes;

	private JOptionPane optionPane;
	private JDialog dialog;

	public CSVTemplateCreatorDialog(Set<String> columnNames, Component parent) {
		this.columnNames = columnNames;

		JPanel panel = initPanel();
		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		dialog = optionPane.createDialog(parent, Configed.getResourceValue("NewClientDialog.csvTemplateLabel"));
	}

	private JPanel initPanel() {
		format = CSVFormat.DEFAULT.builder().setDelimiter(';').setCommentMarker('#').get();

		NumberFormatter formatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		((NumberFormat) formatter.getFormat()).setGroupingUsed(false);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		JRadioButton tabsOption = radio("CSVImportDataDialog.tabsOption", "\t", false);
		JRadioButton commaOption = radio("CSVImportDataDialog.commaOption", ",", false);
		JRadioButton semicolonOption = radio("CSVImportDataDialog.semicolonOption", ";", true);
		JRadioButton spaceOption = radio("CSVImportDataDialog.spaceOption", " ", false);
		JRadioButton otherOption = radio("CSVImportDataDialog.otherOption", "", false);

		ButtonGroup delimiterOptions = new ButtonGroup();
		Stream.of(tabsOption, commaOption, semicolonOption, spaceOption, otherOption).forEach(delimiterOptions::add);

		MaskFormatter maskFormatter = createMaskFormatter();

		otherDelimiterInput = new JFormattedTextField(maskFormatter);
		otherDelimiterInput.setToolTipText(Configed.getResourceValue("CSVImportDataDialog.allowedCharacters.tooltip"));
		otherDelimiterInput.setEnabled(false);

		JLabel delimiterLabel = Utils.createBoldLabel("CSVImportDataDialog.stringSeparatorLabel");

		JComboBox<Character> quoteOptions = new JComboBox<>(new Character[] { '"', '\'' });
		quoteOptions.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				format = format.builder().setQuote(quoteOptions.getSelectedItem().toString().charAt(0))
						.setQuoteMode(QuoteMode.ALL).get();
			}
		});

		for (AbstractButton button : Collections.list(delimiterOptions.getElements())) {
			button.addItemListener((e -> delimiterAction(button, otherOption, e)));
		}

		((AbstractDocument) otherDelimiterInput.getDocument()).addDocumentListener(new InputListener() {
			@Override
			public void performAction() {
				if (!otherDelimiterInput.getText().isEmpty()) {
					format = format.builder().setDelimiter(otherDelimiterInput.getText().charAt(0)).get();
				}
			}
		});

		JPanel centerPanel = new JPanel(new MigLayout("insets 0, wrap 1", "[grow]", "[]0"));

		JLabel dataSelectionLabel = Utils.createBoldLabel("CSVTemplateCreatorDialog.dataSelectionLabel");
		JLabel fieldSeparatorLabel = Utils.createBoldLabel("CSVTemplateCreatorDialog.fieldSeparatorLabel");
		includeFormatHintOption = new JCheckBox(
				Configed.getResourceValue("CSVTemplateCreatorDialog.includeFormatHintOption"));

		DefaultListModel<JCheckBox> model = new DefaultListModel<>();
		headerCheckBoxes = new ArrayList<>();
		columnNames.forEach(header -> addHeaderCheckBox(header, model));

		CheckBoxList list = new CheckBoxList(model);
		list.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);

		centerPanel.add(dataSelectionLabel, "gapbottom " + Globals.MIN_GAP_SIZE);

		centerPanel.add(list);

		centerPanel.add(includeFormatHintOption, "gaptop " + Globals.GAP_SIZE);

		centerPanel.add(delimiterLabel, "gaptop " + Globals.GAP_SIZE);

		centerPanel.add(tabsOption);
		centerPanel.add(commaOption);
		centerPanel.add(semicolonOption);
		centerPanel.add(spaceOption);

		centerPanel.add(otherOption, "split 2, gapright " + Globals.GAP_SIZE);
		centerPanel.add(otherDelimiterInput, "wrap");

		centerPanel.add(fieldSeparatorLabel, "gaptop " + Globals.GAP_SIZE);

		centerPanel.add(quoteOptions);

		return centerPanel;
	}

	public static MaskFormatter createMaskFormatter() {
		MaskFormatter maskFormatter;
		try {
			maskFormatter = new MaskFormatter("*");
		} catch (ParseException e) {
			Logging.warning(e, "INVALID MASK");
			maskFormatter = new MaskFormatter();
		}
		maskFormatter.setValidCharacters(",.-|?@~!$%&/\\=_:;+*");
		maskFormatter.setAllowsInvalid(false);
		maskFormatter.setCommitsOnValidEdit(true);
		return maskFormatter;
	}

	private void delimiterAction(AbstractButton button, AbstractButton otherOption, ItemEvent e) {
		otherDelimiterInput.setEnabled(e.getItem() == otherOption);

		if (e.getStateChange() == ItemEvent.SELECTED && !button.getActionCommand().isEmpty()) {
			format = format.builder().setDelimiter(button.getActionCommand().charAt(0)).get();
		}
	}

	private static JRadioButton radio(String key, String cmd, boolean selected) {
		JRadioButton b = new JRadioButton(Configed.getResourceValue(key));
		b.setActionCommand(cmd);
		b.setSelected(selected);
		return b;

	}

	private void addHeaderCheckBox(String header, DefaultListModel<JCheckBox> model) {
		JCheckBox headerCheckBox = new JCheckBox(header, isImportantHeader(header));
		if (isImportantHeader(header)) {
			headerCheckBox.setEnabled(false);
			headerCheckBox.setIcon(Icons.getIntellijIcon("locked"));
		}
		headerCheckBox.setActionCommand(header);

		model.addElement(headerCheckBox);
		headerCheckBoxes.add(headerCheckBox);
	}

	private static boolean isImportantHeader(String header) {
		return CSVImportDataModifier.getImportantHeaders().contains(header);
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

	public void createTemplate() {
		boolean proceed = true;
		for (JCheckBox headerCheckBox : headerCheckBoxes) {
			if (HostInfo.HOST_KEY_KEY.equals(headerCheckBox.getText()) && headerCheckBox.isSelected()
					&& !Utils.includeOpsiHostKey()) {
				proceed = false;
			}
		}

		if (!proceed) {
			return;
		}

		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		jFileChooser.setFileHidingEnabled(false);
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("CSV (.csv)", "csv");
		jFileChooser.addChoosableFileFilter(fileFilter);
		jFileChooser.setAcceptAllFileFilterUsed(false);

		int returnValue = jFileChooser.showSaveDialog(ConfigedMain.getMainFrame());

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String csvFile = jFileChooser.getSelectedFile().getAbsolutePath();
			if (!csvFile.endsWith(".csv")) {
				csvFile = csvFile.concat(".csv");
			}

			write(csvFile);
		}
	}

	private void write(String csvFile) {
		format = format.builder().setQuoteMode(QuoteMode.ALL).get();
		try (BufferedWriter writer = Files.newBufferedWriter(new File(csvFile).toPath(), StandardCharsets.UTF_8);
				CSVPrinter printer = new CSVPrinter(writer, format)) {
			List<String> headers = new ArrayList<>();

			headerCheckBoxes.forEach((JCheckBox headerCheckBox) -> {
				if (headerCheckBox.isSelected()) {
					headers.add(headerCheckBox.getActionCommand());
				}
			});

			if (includeFormatHintOption.isSelected()) {
				format = format.builder().setCommentMarker('#').get();
				printer.printComment("sep=" + format.getDelimiterString() + " -- quote=" + format.getQuoteCharacter());
			}

			printer.printRecord(headers);
		} catch (IOException e) {
			Logging.error(this, e, "Unable to write to file");
		}
	}

	public void show() {
		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setVisible(true);

		if (optionPane.getValue() == (Integer) JOptionPane.OK_OPTION) {
			createTemplate();
		}
	}

	public static void displayCSVTemplateDialog(Component parent) {
		Set<String> columnNames = HostInfo.getKeysForCSV();
		if (csvTemplateCreatorDialog == null) {
			csvTemplateCreatorDialog = new CSVTemplateCreatorDialog(columnNames, parent);
		}

		csvTemplateCreatorDialog.show();
	}
}
