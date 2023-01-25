package de.uib.configed.gui;

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
import java.util.List;

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

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.csv.CSVFormat;
import de.uib.configed.csv.CSVWriter;
import de.uib.utilities.logging.Logging;

public class CSVTemplateCreatorDialog extends FGeneralDialog {
	protected int wLeftLabel = Globals.BUTTON_WIDTH + 20;

	private CSVFormat format;

	private JCheckBox includeFormatHintOption;

	private JFormattedTextField otherSeparatorInput;

	private List<String> columnNames;
	private List<JCheckBox> headerButtons;

	public CSVTemplateCreatorDialog(List<String> columnNames) {
		super(Globals.mainFrame, Configed.getResourceValue("CSVTemplateCreatorDialog.title"), false,
				new String[] { "ok", "cancel" },
				new Icon[] { Globals.createImageIcon("images/checked_withoutbox_blue14.png", ""),
						Globals.createImageIcon("images/cancel16_small.png", "") },
				2, 1000, 400, true, null);

		this.columnNames = columnNames;
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

		southPanel = createSouthPanel();

		GroupLayout allLayout = new GroupLayout(allpane);
		allpane.setLayout(allLayout);

		allLayout.setVerticalGroup(allLayout.createSequentialGroup().addGap(Globals.HGAP_SIZE).addComponent(centerPanel)
				.addGap(Globals.HGAP_SIZE)
				.addComponent(southPanel, Globals.LINE_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE));

		allLayout.setHorizontalGroup(allLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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

	protected JPanel initPanel() {
		format = new CSVFormat();

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

		JLabel stringSeparatorLabel = new JLabel(Configed.getResourceValue("CSVImportDataDialog.stringSeparatorLabel"));

		JComboBox<Character> stringSeparatorOptions = new JComboBox<>(new Character[] { '"', '\'' });
		stringSeparatorOptions.addItemListener((ItemEvent e) -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				format.setStringSeparator(stringSeparatorOptions.getSelectedItem().toString().charAt(0));
			}
		});

		Enumeration<AbstractButton> iter = fieldSeparatorOptions.getElements();

		while (iter.hasMoreElements()) {
			AbstractButton button = iter.nextElement();

			button.addItemListener((ItemEvent e) -> {
				otherSeparatorInput.setEnabled(e.getItem() == otherOption);

				if (e.getStateChange() == ItemEvent.SELECTED && !button.getActionCommand().isEmpty())
					format.setFieldSeparator(button.getActionCommand().charAt(0));

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

		JLabel dataLabel = new JLabel(Configed.getResourceValue("CSVTemplateCreatorDialog.dataOptionsLabel"));
		dataLabel.setFont(Globals.defaultFontBold);
		JLabel dataSelectionLabel = new JLabel(
				Configed.getResourceValue("CSVTemplateCreatorDialog.dataSelectionLabel"));
		JLabel csvFormatLabel = new JLabel(Configed.getResourceValue("CSVTemplateCreatorDialog.csvFormatLabel"));
		csvFormatLabel.setFont(Globals.defaultFontBold);
		JLabel fieldSeparatorLabel = new JLabel(
				Configed.getResourceValue("CSVTemplateCreatorDialog.fieldSeparatorLabel"));

		includeFormatHintOption = new JCheckBox(
				Configed.getResourceValue("CSVTemplateCreatorDialog.includeFormatHintOption"));

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
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(dataLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(dataSelectionLabel)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(headerOptionsPanel)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(csvFormatLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(includeFormatHintOption, Globals.BUTTON_WIDTH, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(fieldSeparatorLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE))
				.addGroup(centerLayout.createSequentialGroup()
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
				.addGroup(centerLayout.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(stringSeparatorLabel, wLeftLabel, wLeftLabel, wLeftLabel)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(stringSeparatorOptions, wLeftLabel, wLeftLabel, wLeftLabel)));

		centerLayout.setVerticalGroup(centerLayout.createSequentialGroup()
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE).addComponent(dataLabel)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE).addComponent(dataSelectionLabel)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE).addComponent(headerOptionsPanel)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE).addComponent(csvFormatLabel)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(includeFormatHintOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
						Globals.BUTTON_HEIGHT)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE).addComponent(fieldSeparatorLabel)
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(centerLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(tabsOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(commaOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(semicolonOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(spaceOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(otherOption, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(otherSeparatorInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addGroup(centerLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(stringSeparatorLabel).addComponent(stringSeparatorOptions, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		return centerPanel;
	}

	private class HeaderOptionsPanel extends JPanel {
		public HeaderOptionsPanel(ListModel<JCheckBox> model) {
			setBackground(Globals.CSV_CREATE_CLIENT_PANEL_BACKGROUND_COLOR);
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
					@Override
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
				@Override
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
			List<String> headers = new ArrayList<>();

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
			Logging.error(this, "Unable to write to file");
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
