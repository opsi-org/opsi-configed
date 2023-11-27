/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.csv;

import java.awt.Dimension;
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
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.HeaderOptionsPanel;
import de.uib.utilities.logging.Logging;

public class CSVTemplateCreatorDialog extends FGeneralDialog {
	private static final int WIDTH_LEFT_LABEL = Globals.BUTTON_WIDTH + 20;

	private CSVFormat format;

	private JCheckBox includeFormatHintOption;

	private JFormattedTextField otherDelimiterInput;

	private List<String> columnNames;
	private List<JCheckBox> headerButtons;

	public CSVTemplateCreatorDialog(List<String> columnNames) {
		super(ConfigedMain.getMainFrame(), Configed.getResourceValue("NewClientDialog.csvTemplateLabel"), false,
				new String[] { Configed.getResourceValue("buttonCancel"), Configed.getResourceValue("buttonOK") }, 2,
				1000, 420, true);

		this.columnNames = columnNames;
	}

	@Override
	protected void allLayout() {
		Logging.info(this, "allLayout");

		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null) {
			centerPanel = new JPanel();
		}

		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(centerPanel)
				.addGap(Globals.GAP_SIZE)
				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(Alignment.LEADING)
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

	public JPanel initPanel() {
		format = CSVFormat.DEFAULT.builder().setCommentMarker('#').build();

		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		numberFormat.setGroupingUsed(false);

		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		JRadioButton tabsOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.tabsOption"));
		tabsOption.setActionCommand("\t");

		JRadioButton commaOption = new JRadioButton(Configed.getResourceValue("CSVImportDataDialog.commaOption"));
		commaOption.setActionCommand(",");
		commaOption.setSelected(true);

		JRadioButton semicolonOption = new JRadioButton(
				Configed.getResourceValue("CSVImportDataDialog.semicolonOption"));
		semicolonOption.setActionCommand(";");

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

		JComboBox<Character> quoteOptions = new JComboBox<>(new Character[] { '"', '\'' });
		quoteOptions.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				format = format.builder().setQuote(quoteOptions.getSelectedItem().toString().charAt(0))
						.setQuoteMode(QuoteMode.ALL).build();
			}
		});

		Enumeration<AbstractButton> iter = delimiterOptions.getElements();

		while (iter.hasMoreElements()) {
			AbstractButton button = iter.nextElement();

			button.addItemListener((ItemEvent e) -> {
				otherDelimiterInput.setEnabled(e.getItem() == otherOption);

				if (e.getStateChange() == ItemEvent.SELECTED && !button.getActionCommand().isEmpty()) {
					format = format.builder().setDelimiter(button.getActionCommand().charAt(0)).build();
				}
			});
		}

		((AbstractDocument) otherDelimiterInput.getDocument()).addDocumentListener(new InputListener() {
			@Override
			public void performAction() {
				if (!otherDelimiterInput.getText().isEmpty()) {
					format = format.builder().setDelimiter(otherDelimiterInput.getText().charAt(0)).build();
				}
			}
		});

		JPanel centerPanel = new JPanel();
		GroupLayout centerLayout = new GroupLayout(centerPanel);
		centerPanel.setLayout(centerLayout);

		JLabel dataLabel = new JLabel(Configed.getResourceValue("CSVTemplateCreatorDialog.dataOptionsLabel"));
		JLabel dataSelectionLabel = new JLabel(
				Configed.getResourceValue("CSVTemplateCreatorDialog.dataSelectionLabel"));
		JLabel csvFormatLabel = new JLabel(Configed.getResourceValue("CSVTemplateCreatorDialog.csvFormatLabel"));
		JLabel fieldSeparatorLabel = new JLabel(
				Configed.getResourceValue("CSVTemplateCreatorDialog.fieldSeparatorLabel"));

		includeFormatHintOption = new JCheckBox(
				Configed.getResourceValue("CSVTemplateCreatorDialog.includeFormatHintOption"));

		DefaultListModel<JCheckBox> model = new DefaultListModel<>();
		headerButtons = new ArrayList<>();
		columnNames.forEach(header -> setSelectedColumn(header, model));

		HeaderOptionsPanel headerOptionsPanel = new HeaderOptionsPanel(model);

		centerLayout.setHorizontalGroup(centerLayout.createParallelGroup()
				.addGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(dataLabel, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(dataSelectionLabel)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(headerOptionsPanel)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(csvFormatLabel, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(includeFormatHintOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(fieldSeparatorLabel, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL).addGap(
								Globals.MIN_GAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
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
				.addGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(quoteLabel, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)
						.addGap(Globals.GAP_SIZE)
						.addComponent(quoteOptions, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL, WIDTH_LEFT_LABEL)));

		centerLayout.setVerticalGroup(centerLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(dataLabel).addGap(Globals.GAP_SIZE).addComponent(dataSelectionLabel)
				.addGap(Globals.GAP_SIZE).addComponent(headerOptionsPanel).addGap(Globals.GAP_SIZE)
				.addComponent(csvFormatLabel).addGap(Globals.GAP_SIZE)
				.addComponent(includeFormatHintOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
						Globals.BUTTON_HEIGHT)
				.addGap(Globals.GAP_SIZE).addComponent(fieldSeparatorLabel).addGap(Globals.GAP_SIZE)
				.addGroup(centerLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(tabsOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(commaOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(semicolonOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(spaceOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(otherOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(otherDelimiterInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(centerLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(quoteLabel)
						.addComponent(quoteOptions, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));

		return centerPanel;
	}

	private void setSelectedColumn(String header, DefaultListModel<JCheckBox> model) {
		JCheckBox headerBox = new JCheckBox(header);
		headerBox.setActionCommand(header);

		if ("hostname".equals(header)) {
			headerBox.setSelected(true);
		}
		if ("selectedDomain".equals(header)) {
			headerBox.setSelected(true);
		}
		if ("depotID".equals(header)) {
			headerBox.setSelected(true);
		}
		if ("macaddress".equals(header)) {
			headerBox.setSelected(true);
		}

		model.addElement(headerBox);
		headerButtons.add(headerBox);
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

	@Override
	public void doAction2() {
		result = 2;

		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
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

		leave();
	}

	private void write(String csvFile) {
		format = format.builder().setQuoteMode(QuoteMode.ALL).build();
		try (BufferedWriter writer = Files.newBufferedWriter(new File(csvFile).toPath(), StandardCharsets.UTF_8);
				CSVPrinter printer = new CSVPrinter(writer, format)) {
			List<String> headers = new ArrayList<>();

			headerButtons.forEach((JCheckBox header) -> {
				if (header.isSelected()) {
					headers.add(header.getActionCommand());
				}
			});

			if (includeFormatHintOption.isSelected()) {
				format = format.builder().setCommentMarker('#').build();
				printer.printComment("sep=" + format.getDelimiterString() + " -- quote=" + format.getQuoteCharacter());
			}

			printer.printRecord(headers);
		} catch (IOException e) {
			Logging.error(this, "Unable to write to file", e);
		}
	}
}
