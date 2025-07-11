/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.csv;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
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
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;

public class CSVTemplateCreatorDialog {
	private static CSVTemplateCreatorDialog csvTemplateCreatorDialog;

	private CSVFormat format;

	private JCheckBox includeFormatHintOption;

	private JFormattedTextField otherDelimiterInput;

	private List<String> columnNames;
	private List<JCheckBox> headerCheckBoxes;

	private JOptionPane optionPane;
	private JDialog dialog;

	public CSVTemplateCreatorDialog(List<String> columnNames, Component parent) {
		this.columnNames = columnNames;

		JPanel panel = initPanel();
		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		dialog = optionPane.createDialog(parent, Configed.getResourceValue("NewClientDialog.csvTemplateLabel"));
	}

	private JPanel initPanel() {
		format = CSVFormat.DEFAULT.builder().setDelimiter(";").setCommentMarker('#').get();

		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		numberFormat.setGroupingUsed(false);

		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		JRadioButton tabsOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.tabsOption"));
		tabsOption.setActionCommand("\t");

		JRadioButton commaOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.commaOption"));
		commaOption.setActionCommand(",");

		JRadioButton semicolonOption = new JRadioButton(
				Configed.getResourceValue("CSVImportDataDialog.semicolonOption"));
		semicolonOption.setActionCommand(";");
		semicolonOption.setSelected(true);

		JRadioButton spaceOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.spaceOption"));
		spaceOption.setActionCommand(" ");

		JRadioButton otherOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.otherOption"));
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
		quoteLabel.setFont(quoteLabel.getFont().deriveFont(Font.BOLD));

		JComboBox<Character> quoteOptions = new JComboBox<>(new Character[] { '"', '\'' });
		quoteOptions.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				format = format.builder().setQuote(quoteOptions.getSelectedItem().toString().charAt(0))
						.setQuoteMode(QuoteMode.ALL).get();
			}
		});

		Enumeration<AbstractButton> iter = delimiterOptions.getElements();

		while (iter.hasMoreElements()) {
			AbstractButton button = iter.nextElement();

			button.addItemListener((ItemEvent e) -> {
				otherDelimiterInput.setEnabled(e.getItem() == otherOption);

				if (e.getStateChange() == ItemEvent.SELECTED && !button.getActionCommand().isEmpty()) {
					format = format.builder().setDelimiter(button.getActionCommand().charAt(0)).get();
				}
			});
		}

		((AbstractDocument) otherDelimiterInput.getDocument()).addDocumentListener(new InputListener() {
			@Override
			public void performAction() {
				if (!otherDelimiterInput.getText().isEmpty()) {
					format = format.builder().setDelimiter(otherDelimiterInput.getText().charAt(0)).get();
				}
			}
		});

		JPanel centerPanel = new JPanel();
		GroupLayout centerLayout = new GroupLayout(centerPanel);
		centerPanel.setLayout(centerLayout);

		JLabel dataSelectionLabel = new JLabel(
				Configed.getResourceValue("CSVTemplateCreatorDialog.dataSelectionLabel"));
		dataSelectionLabel.setFont(dataSelectionLabel.getFont().deriveFont(Font.BOLD));
		JLabel fieldSeparatorLabel = new JLabel(
				Configed.getResourceValue("CSVTemplateCreatorDialog.fieldSeparatorLabel"));
		fieldSeparatorLabel.setFont(fieldSeparatorLabel.getFont().deriveFont(Font.BOLD));

		includeFormatHintOption = new JCheckBox(
				Configed.getResourceValue("CSVTemplateCreatorDialog.includeFormatHintOption"));

		DefaultListModel<JCheckBox> model = new DefaultListModel<>();
		headerCheckBoxes = new ArrayList<>();
		columnNames.forEach(header -> addHeaderCheckBox(header, model));

		CheckBoxList list = new CheckBoxList(model);
		list.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")));
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(-1);

		centerLayout.setHorizontalGroup(centerLayout.createParallelGroup().addComponent(dataSelectionLabel)
				.addComponent(list, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(includeFormatHintOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(fieldSeparatorLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(tabsOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(commaOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(semicolonOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addComponent(spaceOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGroup(centerLayout.createSequentialGroup()
						.addComponent(otherOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE).addComponent(otherDelimiterInput, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

				.addComponent(quoteLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(quoteOptions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		centerLayout.setVerticalGroup(centerLayout.createSequentialGroup().addComponent(dataSelectionLabel)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(list, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addComponent(includeFormatHintOption).addGap(Globals.GAP_SIZE)
				.addComponent(fieldSeparatorLabel)
				.addComponent(tabsOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(commaOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(semicolonOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(spaceOption, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGroup(centerLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(otherOption)
						.addComponent(otherDelimiterInput))
				.addGap(Globals.GAP_SIZE).addComponent(quoteLabel).addComponent(quoteOptions));

		return centerPanel;
	}

	private void addHeaderCheckBox(String header, DefaultListModel<JCheckBox> model) {
		JCheckBox headerCheckBox = new JCheckBox(header, isImportantHeader(header));
		headerCheckBox.setActionCommand(header);

		model.addElement(headerCheckBox);
		headerCheckBoxes.add(headerCheckBox);
	}

	private static boolean isImportantHeader(String header) {
		return HostInfo.HOSTNAME_KEY.equals(header) || "domain".equals(header)
				|| HostInfo.DEPOT_OF_CLIENT_KEY.equals(header) || HostInfo.CLIENT_MAC_ADRESS_KEY.equals(header);
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
		List<String> columnNames = HostInfo.getKeysForCSV();
		if (csvTemplateCreatorDialog == null) {
			csvTemplateCreatorDialog = new CSVTemplateCreatorDialog(columnNames, parent);
		}

		csvTemplateCreatorDialog.show();
	}
}
