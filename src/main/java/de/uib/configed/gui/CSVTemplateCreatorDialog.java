package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.AbstractDocument;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.csv.CSVFormat;
import de.uib.configed.csv.CSVWriter;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class CSVTemplateCreatorDialog extends FGeneralDialog {
	protected int wLeftLabel = Globals.buttonWidth + 20;

	private PanelGenEditTable thePanel;
	private CSVFormat format;

	private ButtonGroup fieldSeparatorOptions;
	private JRadioButton tabsOption;
	private JRadioButton commaOption;
	private JRadioButton semicolonOption;
	private JRadioButton spaceOption;
	private JRadioButton otherOption;
	private JComboBox<Character> stringSeparatorOptions;
	private JCheckBox includeFormatHintOption;

	private JLabel importOptionsLabel;
	private JLabel splittingOptionsLabel;
	private JLabel startLineLabel;
	private JFormattedTextField startLineInput;
	private JFormattedTextField otherSeparatorInput;
	private JLabel stringSeparatorLabel;

	private Vector<String> columnNames;
	private java.util.List<JCheckBox> headerButtons;

	public CSVTemplateCreatorDialog(Vector<String> columnNames) {
		super(Globals.mainFrame,
				configed.getResourceValue("CSVTemplateCreatorDialog.title"),
				false,
				new String[] {
						"ok",
						"cancel"
				},
				new Icon[] {
						Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "")
				},
				2,
				1000, 400,
				true,
				null);

		this.columnNames = columnNames;
	}

	int startLine = 1;

	@Override
	protected void allLayout() {
		logging.info(this, "allLayout");

		allpane.setBackground(de.uib.configed.Globals.backLightBlue);
		allpane.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		allpane.setBorder(BorderFactory.createEtchedBorder());

		if (centerPanel == null)
			centerPanel = new JPanel();

		centerPanel.setBackground(Color.white);
		centerPanel.setOpaque(true);

		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup()
				.addGap(Globals.hGapSize)
				.addComponent(centerPanel)
				.addGap(Globals.hGapSize)
				.addComponent(southPanel, Globals.lineHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.hGapSize));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(centerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize))
				.addGroup(allLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize)
						.addComponent(southPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, 2 * Globals.hGapSize)));
	}

	private JPanel createSouthPanel() {
		southPanel = new JPanel();
		southPanel.setOpaque(false);

		GroupLayout southLayout = new GroupLayout(southPanel);
		southPanel.setLayout(southLayout);

		southLayout.setHorizontalGroup(southLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Short.MAX_VALUE)
						.addComponent(jPanelButtonGrid, Globals.lineHeight, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize / 2, Globals.hGapSize, Short.MAX_VALUE))
				.addGroup(southLayout.createSequentialGroup()
						.addGap(Globals.hGapSize / 2)
						.addComponent(additionalPane, 100, 200, Short.MAX_VALUE)
						.addGap(Globals.hGapSize / 2)));

		southLayout.setVerticalGroup(southLayout.createSequentialGroup()
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2)
				.addComponent(additionalPane, Globals.lineHeight, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(jPanelButtonGrid, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addGap(Globals.vGapSize / 2, Globals.vGapSize / 2, Globals.vGapSize / 2));

		southPanel.setOpaque(false);
		southPanel.setBackground(Color.white);
		southPanel.setOpaque(true);

		return southPanel;
	}

	protected JPanel initPanel() {
		format = new CSVFormat();
		importOptionsLabel = new JLabel(configed.getResourceValue("CSVImportDataDialog.importOptionsLabel"));
		importOptionsLabel.setFont(Globals.defaultFontBold);
		splittingOptionsLabel = new JLabel(configed.getResourceValue("CSVImportDataDialog.splittingOptionsLabel"));
		splittingOptionsLabel.setFont(Globals.defaultFontBold);

		NumberFormat numberFormat = NumberFormat.getIntegerInstance();
		numberFormat.setGroupingUsed(false);

		NumberFormatter formatter = new NumberFormatter(numberFormat);
		formatter.setAllowsInvalid(false);
		formatter.setCommitsOnValidEdit(true);

		startLineLabel = new JLabel(configed.getResourceValue("CSVImportDataDialog.startLineLabel"));
		startLineInput = new JFormattedTextField(formatter);

		tabsOption = new JRadioButton(configed.getResourceValue("CSVImportDataDialog.tabsOption"));
		tabsOption.setActionCommand("\t");

		commaOption = new JRadioButton(configed.getResourceValue("CSVImportDataDialog.commaOption"));
		commaOption.setActionCommand(",");
		commaOption.setSelected(true);

		semicolonOption = new JRadioButton(configed.getResourceValue("CSVImportDataDialog.semicolonOption"));
		semicolonOption.setActionCommand(";");

		spaceOption = new JRadioButton(configed.getResourceValue("CSVImportDataDialog.spaceOption"));
		spaceOption.setActionCommand(" ");

		otherOption = new JRadioButton(configed.getResourceValue("CSVImportDataDialog.otherOption"));
		otherOption.setActionCommand("");

		fieldSeparatorOptions = new ButtonGroup();
		fieldSeparatorOptions.add(tabsOption);
		fieldSeparatorOptions.add(commaOption);
		fieldSeparatorOptions.add(semicolonOption);
		fieldSeparatorOptions.add(spaceOption);
		fieldSeparatorOptions.add(otherOption);

		MaskFormatter maskFormatter = null;
		try {
			maskFormatter = new MaskFormatter("*");
		} catch (ParseException e) {
			logging.debug(this, "INVALID MASK");
		}
		maskFormatter.setValidCharacters(",.-|?@~!$%&/\\=_:;#+*");
		maskFormatter.setAllowsInvalid(false);
		maskFormatter.setCommitsOnValidEdit(true);
		otherSeparatorInput = new JFormattedTextField(maskFormatter);
		otherSeparatorInput.setToolTipText(configed.getResourceValue("CSVImportDataDialog.allowedCharacters.tooltip"));
		otherSeparatorInput.setEnabled(false);

		stringSeparatorLabel = new JLabel(configed.getResourceValue("CSVImportDataDialog.stringSeparatorLabel"));
		stringSeparatorOptions = new JComboBox<>(new Character[] { '"', '\'' });
		stringSeparatorOptions.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				format.setStringSeparator(stringSeparatorOptions.getSelectedItem().toString().charAt(0));
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
					if (!button.getActionCommand().isEmpty()) {
						format.setFieldSeparator(button.getActionCommand().charAt(0));
					}
				}
			});
		}

		((AbstractDocument) otherSeparatorInput.getDocument()).addDocumentListener(new InputListener() {
			@Override
			public void performAction() {
				if (!otherSeparatorInput.getText().isEmpty()) {
					format.setFieldSeparator(otherSeparatorInput.getText().charAt(0));
				}
			}
		});

		JPanel centerPanel = new JPanel();
		GroupLayout centerLayout = new GroupLayout(centerPanel);
		centerPanel.setLayout(centerLayout);

		JLabel dataLabel = new JLabel(configed.getResourceValue("CSVTemplateCreatorDialog.dataOptionsLabel"));
		dataLabel.setFont(Globals.defaultFontBold);
		JLabel dataSelectionLabel = new JLabel(
				configed.getResourceValue("CSVTemplateCreatorDialog.dataSelectionLabel"));
		JLabel csvFormatLabel = new JLabel(configed.getResourceValue("CSVTemplateCreatorDialog.csvFormatLabel"));
		csvFormatLabel.setFont(Globals.defaultFontBold);
		JLabel fieldSeparatorLabel = new JLabel(
				configed.getResourceValue("CSVTemplateCreatorDialog.fieldSeparatorLabel"));

		includeFormatHintOption = new JCheckBox(
				configed.getResourceValue("CSVTemplateCreatorDialog.includeFormatHintOption"));

		DefaultListModel<JCheckBox> model = new DefaultListModel<>();
		headerButtons = new ArrayList<>();
		columnNames.forEach(header -> {
			JCheckBox headerBox = new JCheckBox(header);
			headerBox.setActionCommand(header);

			if (header.equals("hostname"))
				headerBox.setSelected(true);
			if (header.equals("selectedDomain"))
				headerBox.setSelected(true);
			if (header.equals("depotID"))
				headerBox.setSelected(true);
			if (header.equals("macaddress"))
				headerBox.setSelected(true);

			model.addElement(headerBox);
			headerButtons.add(headerBox);
		});

		HeaderOptionsPanel headerOptionsPanel = new HeaderOptionsPanel(model);

		centerLayout.setHorizontalGroup(centerLayout.createParallelGroup()
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(dataLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(dataSelectionLabel)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(headerOptionsPanel)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(csvFormatLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(includeFormatHintOption, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(fieldSeparatorLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(tabsOption, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(commaOption, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(semicolonOption, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(spaceOption, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(otherOption, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(otherSeparatorInput, Globals.buttonWidth, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(stringSeparatorLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
						.addComponent(stringSeparatorOptions, wLeftLabel, wLeftLabel, wLeftLabel)));

		centerLayout.setVerticalGroup(centerLayout.createSequentialGroup()
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(dataLabel)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(dataSelectionLabel)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(headerOptionsPanel)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(csvFormatLabel)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(includeFormatHintOption, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addComponent(fieldSeparatorLabel)
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(centerLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(tabsOption, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addComponent(commaOption, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addComponent(semicolonOption, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addComponent(spaceOption, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addComponent(otherOption, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addComponent(otherSeparatorInput, Globals.buttonHeight, Globals.buttonHeight,
								Globals.buttonHeight))
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
				.addGroup(centerLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(stringSeparatorLabel)
						.addComponent(stringSeparatorOptions, Globals.buttonHeight, Globals.buttonHeight,
								Globals.buttonHeight))
				.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize));

		return centerPanel;
	}

	private class HeaderOptionsPanel extends JPanel {
		public HeaderOptionsPanel(ListModel<JCheckBox> model) {
			setBackground(Color.WHITE);
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			CheckBoxList list = new CheckBoxList(model);
			list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
			list.setVisibleRowCount(-1);

			JScrollPane scroll = new JScrollPane(list);
			scroll.setAlignmentX(LEFT_ALIGNMENT);

			add(Box.createRigidArea(new Dimension(0, 1)));
			add(scroll);
		}

		private class CheckBoxList extends JList {
			protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

			public CheckBoxList() {
				setCellRenderer(new CellRenderer());

				addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						int index = locationToIndex(e.getPoint());

						if (index != -1) {
							JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
							checkbox.setSelected(!checkbox.isSelected());
							repaint();
						}
					}
				});
			}

			public CheckBoxList(ListModel<JCheckBox> model) {
				this();
				setModel(model);
			}

			protected class CellRenderer implements ListCellRenderer {
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
						boolean cellHasFocus) {
					JCheckBox checkbox = (JCheckBox) value;
					checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
					checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
					checkbox.setEnabled(isEnabled());
					checkbox.setFont(getFont());
					checkbox.setFocusPainted(false);
					checkbox.setBorderPainted(true);
					checkbox.setBorder(noFocusBorder);
					// checkbox.setBorder(isSelected ?
					// UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
					return checkbox;
				}
			}
		}
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

	@Override
	public void doAction1() {
		result = 1;

		JFileChooser jFileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("CSV (.csv)", "csv");
		jFileChooser.addChoosableFileFilter(fileFilter);
		jFileChooser.setAcceptAllFileFilterUsed(false);

		int returnValue = jFileChooser.showSaveDialog(Globals.mainFrame);

		if (returnValue == JFileChooser.APPROVE_OPTION) {
			String csvFile = jFileChooser.getSelectedFile().getAbsolutePath();
			if (!csvFile.endsWith(".csv"))
				csvFile = csvFile.concat(".csv");
			write(csvFile);
		}

		leave();
	}

	public void write(String csvFile) {
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile), format);
			Vector<String> headers = new Vector<>();

			headerButtons.forEach(header -> {
				if (header.isSelected()) {
					headers.add(header.getActionCommand());
				}
			});

			if (includeFormatHintOption.isSelected()) {
				writer.insertFormatHint();
			}

			writer.write(headers);
			writer.close();
		} catch (IOException e) {
			logging.error(this, "Unable to write to file");
		}
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
