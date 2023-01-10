package de.uib.configed.gui;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.Icon;
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

import de.uib.configed.Globals;
import de.uib.configed.Configed;
import de.uib.configed.csv.CSVFormat;
import de.uib.configed.csv.CSVParser;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class CSVImportDataDialog extends FGeneralDialog {
	protected int wLeftLabel = Globals.BUTTON_WIDTH + 20;

	private PanelGenEditTable thePanel;
	private CSVFormat format;
	private CSVParser parser;

	private JRadioButton tabsOption;
	private JRadioButton commaOption;
	private JRadioButton semicolonOption;
	private JRadioButton spaceOption;
	private JRadioButton otherOption;
	private JComboBox<Character> stringSeparatorOptions;

	private JLabel startLineLabel;
	private JFormattedTextField startLineInput;
	private JFormattedTextField otherSeparatorInput;
	private JLabel stringSeparatorLabel;

	private CSVImportDataModifier modifier;

	public CSVImportDataDialog(CSVImportDataModifier modifier, CSVFormat format) {
		super(Globals.mainFrame, Configed.getResourceValue("CSVImportDataDialog.title"), true,
				new String[] { "ok", "cancel" },
				new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "") },
				2, 1000, 600, true, null);

		this.format = format;
		this.parser = new CSVParser(format);
		this.modifier = modifier;
	}

	int startLine = 1;

	@Override
	protected void allLayout() {
		Logging.info(this, "allLayout");

		allpane.setBackground(Globals.BACKGROUND_COLOR_7);
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null)
			centerPanel = new JPanel();

		centerPanel.setBackground(Globals.CSV_CREATE_CLIENT_PANEL_BACKGROUND_COLOR);
		centerPanel.setOpaque(true);

		northPanel = createNorthPanel();
		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE)
				.addComponent(northPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE).addComponent(centerPanel).addGap(Globals.HGAP_SIZE)
				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(northPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)
						.addComponent(southPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE)));
	}

	private JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();
		northPanel.setOpaque(false);
		northPanel.setBackground(Globals.CSV_CREATE_CLIENT_PANEL_BACKGROUND_COLOR);
		northPanel.setOpaque(true);

		JLabel importOptionsLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.importOptionsLabel"));
		importOptionsLabel.setFont(Globals.defaultFontBold);
		JLabel splittingOptionsLabel = new JLabel(
				Configed.getResourceValue("CSVImportDataDialog.splittingOptionsLabel"));
		splittingOptionsLabel.setFont(Globals.defaultFontBold);

		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		numberFormat.setGroupingUsed(false);

		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		startLineLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.startLineLabel"));
		startLineInput = new JFormattedTextField(formatter);

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

		ButtonGroup fieldSeparatorOptions = new ButtonGroup();
		fieldSeparatorOptions.add(tabsOption);
		fieldSeparatorOptions.add(commaOption);
		fieldSeparatorOptions.add(semicolonOption);
		fieldSeparatorOptions.add(spaceOption);
		fieldSeparatorOptions.add(otherOption);

		MaskFormatter maskFormatter = null;
		try {
			maskFormatter = new MaskFormatter("*");
		} catch (ParseException e) {
			Logging.debug(this, "INVALID MASK");
		}
		maskFormatter.setValidCharacters(",.-|?@~!$%&/\\=_:;#+*");
		maskFormatter.setAllowsInvalid(false);
		maskFormatter.setCommitsOnValidEdit(true);
		otherSeparatorInput = new JFormattedTextField(maskFormatter);
		otherSeparatorInput.setToolTipText(Configed.getResourceValue("CSVImportDataDialog.allowedCharacters.tooltip"));
		otherSeparatorInput.setEnabled(false);

		stringSeparatorLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.stringSeparatorLabel"));
		stringSeparatorOptions = new JComboBox<>(new Character[] { '"', '\'' });
		stringSeparatorOptions.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				format.setStringSeparator(stringSeparatorOptions.getSelectedItem().toString().charAt(0));
				modifier.updateTable(parser, startLine, thePanel);
			}
		});

		Enumeration<AbstractButton> iter = fieldSeparatorOptions.getElements();

		while (iter.hasMoreElements()) {
			AbstractButton button = iter.nextElement();

			button.addItemListener((ItemEvent e) -> {
				if (e.getItem() == otherOption) {
					otherSeparatorInput.setEnabled(true);
				} else {
					otherSeparatorInput.setEnabled(false);
				}

				if (e.getStateChange() == ItemEvent.SELECTED) {
					if (!button.getActionCommand().equals("")) {
						format.setFieldSeparator(button.getActionCommand().charAt(0));
						modifier.updateTable(parser, startLine, thePanel);
					}
				}
			});
		}

		((AbstractDocument) otherSeparatorInput.getDocument()).addDocumentListener(new InputListener() {
			@Override
			public void performAction() {
				if (!otherSeparatorInput.getText().isEmpty()) {
					parser.setIgnoreErrors(true);
					format.setFieldSeparator(otherSeparatorInput.getText().charAt(0));
					modifier.updateTable(parser, startLine, thePanel);
				}
			}
		});

		((AbstractDocument) startLineInput.getDocument()).addDocumentListener(new InputListener() {
			@Override
			public void performAction() {
				if (!startLineInput.getText().isEmpty()) {
					parser.setIgnoreErrors(true);
					startLine = Integer.parseInt(startLineInput.getText());
					modifier.updateTable(parser, startLine, thePanel);
				}
			}
		});

		GroupLayout northLayout = new GroupLayout(northPanel);
		northPanel.setLayout(northLayout);

		northLayout.setHorizontalGroup(northLayout.createParallelGroup()
				.addGroup(northLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(importOptionsLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(northLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(startLineLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(startLineInput, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(northLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(splittingOptionsLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(northLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(tabsOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(commaOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(semicolonOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(spaceOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(otherOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(otherSeparatorInput, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(northLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(stringSeparatorLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(stringSeparatorOptions, wLeftLabel, wLeftLabel, wLeftLabel)));

		northLayout.setVerticalGroup(northLayout.createSequentialGroup()
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(importOptionsLabel)
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(startLineLabel)
						.addComponent(startLineInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(splittingOptionsLabel)
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(tabsOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(commaOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(semicolonOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(spaceOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(otherOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(otherSeparatorInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(northLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(stringSeparatorLabel).addComponent(stringSeparatorOptions, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE));

		return northPanel;
	}

	private JPanel createSouthPanel() {
		southPanel = new JPanel();
		southPanel.setOpaque(false);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE / 2)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE).addGap(Globals.HGAP_SIZE / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addComponent(additionalPane, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(jPanelButtonGrid, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2));

		southPanel.setOpaque(false);
		southPanel.setBackground(Globals.CSV_CREATE_CLIENT_PANEL_BACKGROUND_COLOR);
		southPanel.setOpaque(true);

		return southPanel;
	}

	public void setDetectedOptions() {
		if (format.hasHeader() && format.hasHint()) {
			startLineInput.setText("3");
		} else if (format.hasHeader() || format.hasHint()) {
			startLineInput.setText("2");
		} else {
			startLineInput.setText("1");
		}

		switch (format.getFieldSeparator()) {
		case '\t':
			tabsOption.setSelected(true);
			break;
		case ',':
			commaOption.setSelected(true);
			break;
		case ';':
			semicolonOption.setSelected(true);
			break;
		case ' ':
			spaceOption.setSelected(true);
			break;
		default:
			otherOption.setSelected(true);
			otherSeparatorInput.setText(String.valueOf(format.getFieldSeparator()));
			break;
		}

		switch (format.getStringSeparator()) {
		case '\'':
			stringSeparatorOptions.setSelectedItem('\'');
			break;
		case '"':
			stringSeparatorOptions.setSelectedItem('"');
			break;
		}
	}

	protected JPanel initPanel() {
		thePanel = new PanelGenEditTable("", // title
				-1, // don't use a definite max table width
				true, // editing
				0, // generalPopupPosition
				true, // switchLineColors
				new int[] { PanelGenEditTable.POPUP_SORT_AGAIN, PanelGenEditTable.POPUP_RELOAD }, // popupsWanted
				true // withTableseearchPane
		);

		parser.setIgnoreErrors(false);
		boolean updatedSuccessfull = modifier.updateTable(parser, startLine, thePanel);

		return updatedSuccessfull ? thePanel : null;
	}

	private class InputListener implements DocumentListener {
		public void performAction() {
		}

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
		}
	}

	public CSVImportDataModifier getModifier() {
		return modifier;
	}

	// Overriding default mechanism to do nothing when window is closed.
	// By default, when window is closed it acts as if the first button
	// was clicked. The default mechanism is defined in FGeneralDialog.
	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			result = 2;
			leave();
		} else {
			super.processWindowEvent(e);
		}
	}
}
