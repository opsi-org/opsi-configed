/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox;
import com.formdev.flatlaf.extras.components.FlatTriStateCheckBox.State;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.features.clientselection.AbstractSelectElement;
import de.uib.configed.gui.features.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.gui.features.clientselection.AbstractSelectOperation;
import de.uib.configed.gui.features.clientselection.ConnectionStatus;
import de.uib.configed.gui.features.clientselection.OperationWithStatus;
import de.uib.configed.gui.features.clientselection.SelectData;
import de.uib.configed.gui.features.clientselection.SelectionManager;
import de.uib.configed.gui.features.clientselection.elements.ConnectionElement;
import de.uib.configed.gui.features.clientselection.elements.DescriptionElement;
import de.uib.configed.gui.features.clientselection.elements.GroupElement;
import de.uib.configed.gui.features.clientselection.elements.GroupWithSubgroupsElement;
import de.uib.configed.gui.features.clientselection.elements.IPElement;
import de.uib.configed.gui.features.clientselection.elements.NameElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareActionProgressElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareActionResultElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareInstallationStatusElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareLastActionElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareModificationTimeElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwarePackageVersionElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareRequestElement;
import de.uib.configed.gui.features.clientselection.elements.SoftwareVersionElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditArchitectureElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditLanguageElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditNameElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditSoftwareIdElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditSubversionElement;
import de.uib.configed.gui.features.clientselection.elements.SwAuditVersionElement;
import de.uib.configed.gui.features.clientselection.operations.HardwareOperation;
import de.uib.configed.gui.features.clientselection.operations.HostOperation;
import de.uib.configed.gui.features.clientselection.operations.SoftwareOperation;
import de.uib.configed.gui.features.clientselection.operations.SwAuditOperation;
import de.uib.configed.gui.share.swing.SearchQueryExecutor;
import de.uib.configed.gui.share.swing.TextInputField;
import de.uib.configed.gui.type.SavedSearch;
import de.uib.configed.share.Icons;
import de.uib.configed.share.Utils;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

/**
 * This dialog shows a number of options you can use to select specific clients.
 */
public class ClientSelectionDialog implements ActionListener, DocumentListener {
	private static final Pattern searchNamePattern = Pattern.compile("[\\p{Alpha}\\d_-]*",
			Pattern.UNICODE_CHARACTER_CLASS);

	private JPanel contentPane;
	private JComboBox<String> newElementBox;
	private JButton buttonReload;
	private JTextField saveNameField;
	private JTextField saveDescriptionField;

	private LinkedList<ComplexGroup> complexElements;

	private SelectionManager manager;
	private SavedSearchesDialog savedSearchesDialog;

	private JScrollPane scrollPane;

	private JOptionPane optionPane;
	private JDialog dialog;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientSelectionDialog(SavedSearchesDialog savedSearchesDialog) {
		this.savedSearchesDialog = savedSearchesDialog;
		manager = new SelectionManager();
		complexElements = new LinkedList<>();
		init();
		JPanel panel = initComponents();
		Utils.addKeyBindingToJComponent(panel, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), this::reload);

		JButton buttonSearch = new JButton(Configed.getResourceValue("search"));
		buttonSearch.addActionListener(event -> doSearch());

		JButton buttonReset = new JButton(Configed.getResourceValue("ClientSelectionDialog.buttonReset"));
		buttonReset.addActionListener(actionEvent -> reset());

		optionPane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,
				new Object[] { buttonSearch, buttonReset, Configed.getResourceValue("buttonClose") });
		Utils.enableDialogResizing(optionPane);

		dialog = optionPane.createDialog(ConfigedMain.getMainFrame(),
				Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"));
		dialog.setMinimumSize(new Dimension());
		dialog.setModalityType(ModalityType.MODELESS);
		dialog.pack();
		dialog.setLocationRelativeTo(ConfigedMain.getMainFrame());
		resizeScrollPane();
	}

	public void show(String searchName) {
		if (searchName != null) {
			loadSearch(searchName);
		}
		dialog.setVisible(true);
	}

	public void loadSearch(String name) {
		Logging.info(this, "loadSearch ", name);

		manager.loadSearch(name);
		loadFromManager();
		SavedSearch search = persistenceController.getDataServices().config.getSavedSearchesPD().get(name);
		saveNameField.setText(search.getName());
		saveDescriptionField.setText(search.getDescription());
	}

	private void doSearch() {
		Logging.info(this, "doSearch");
		String searchName = (saveNameField.getText() != null || !saveNameField.getText().isBlank())
				? saveNameField.getText()
				: "";
		SearchQueryExecutor executor = new SearchQueryExecutor(() -> {
			dialog.setCursor(Globals.WAIT_CURSOR);
			collectData();
			Collection<String> result = manager.selectClients();
			dialog.setCursor(null);
			return result;
		}, searchName);
		executor.execute();
	}

	private JPanel initComponents() {
		JPanel completePanel = new JPanel();

		saveNameField = new JTextField();
		saveNameField.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.searchnameFormat"));

		saveDescriptionField = new JTextField();

		JLabel saveNameLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.inquiryName"));

		JLabel saveDescriptionLabel = new JLabel(Configed.getResourceValue("description"));

		JButton saveButton = new JButton(Icons.getIntellijIcon("save"));
		saveButton.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.saveSearchTooltip"));
		saveButton.addActionListener(actionEvent -> save());

		buttonReload = new JButton(Icons.getIntellijIcon("refresh"));
		buttonReload.setToolTipText(Configed.getResourceValue("reload"));
		buttonReload.addActionListener(actionEvent -> reload());

		completePanel.setLayout(new MigLayout("insets 0, fill", "", "[grow][pref!]"));
		completePanel.add(scrollPane, "grow, span, wrap, gapbottom " + Globals.GAP_SIZE);
		completePanel.add(saveNameLabel, "split 7, gapleft " + Globals.GAP_SIZE);
		completePanel.add(saveNameField, "wmin 40, w 100:200, gapleft " + Globals.MIN_GAP_SIZE);
		completePanel.add(saveDescriptionLabel, "gapleft " + Globals.GAP_SIZE);
		completePanel.add(saveDescriptionField, "wmin 40, w 200, growx, gapleft " + Globals.MIN_GAP_SIZE);
		completePanel.add(saveButton, "gapleft " + Globals.GAP_SIZE);
		completePanel.add(buttonReload, "gapleft " + Globals.GAP_SIZE + ", wrap");
		return completePanel;
	}

	private void init() {
		contentPane = new JPanel(new MigLayout("insets 0, wrap 8",
				"[20!,center][40!,center][pref,left][70!,center][100,grow,center][20!,center][100!,center][pref!,right]",
				"[center]0"));

		newElementBox = new JComboBox<>(
				new String[] { Configed.getResourceValue("ClientSelectionDialog.newElementsBox") });
		newElementBox.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);
		newElementBox.addItem(Configed.getResourceValue("hostName"));
		newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.softwareName"));
		newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.swauditName"));

		// hardware
		List<String> hardwareList = new LinkedList<>(manager.getBackend().getLocalizedHardwareList().keySet());
		Collections.sort(hardwareList);
		for (String hardware : hardwareList) {
			newElementBox.addItem(hardware);
		}
		newElementBox.addActionListener(actionEvent -> addElement());

		complexElements.add(createHostGroup());
		complexElements.add(createSoftwareGroup());
		complexElements.getLast().connectionType.setVisible(false);

		scrollPane = new JScrollPane(contentPane);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

		renderAll();
	}

	private void reload() {
		Logging.info(this, "actionPerformed");
		buttonReload.setEnabled(false);
		dialog.setCursor(Globals.WAIT_CURSOR);
		SwingUtilities.invokeLater(() -> {
			manager.getBackend().setReloadRequested();
			buttonReload.setEnabled(true);
			dialog.setCursor(null);
		});
	}

	/* This creates one line with element, operation, data, ... */
	private SimpleGroup createSimpleGroup(AbstractSelectElement element) {
		SimpleGroup result = new SimpleGroup();
		result.element = element;
		AbstractSelectOperation[] operations = element.supportedOperations().toArray(new AbstractSelectOperation[0]);
		if (operations.length == 0) {
			Logging.warning("Elements without any operations: ", result);
			return result;
		}

		result.negateButton = createNOTCheckBox();
		result.negateButton.setMaximumSize(new Dimension(result.negateButton.getMaximumSize().width,
				result.negateButton.getPreferredSize().height));

		result.connectionType = createANDORCheckBox();
		result.connectionType.addActionListener(actionEvent -> buildParentheses());
		result.connectionType.setMaximumSize(new Dimension(result.connectionType.getMaximumSize().width,
				result.connectionType.getPreferredSize().height));
		result.elementLabel = new JLabel(element.getLocalizedPath());
		result.elementLabel.setMaximumSize(new Dimension(result.elementLabel.getMaximumSize().width,
				result.connectionType.getPreferredSize().height));
		if (operations.length > 1) {
			JComboBox<String> box = new JComboBox<>();
			for (AbstractSelectOperation op : operations) {
				box.addItem(op.getOperationString());
			}
			result.operationComponent = box;
			((JComboBox<?>) result.operationComponent).addActionListener(this::selectOperation);
		} else {
			result.operationComponent = new JLabel(operations[0].getOperationString(), SwingConstants.CENTER);
		}

		result.operationComponent.setMaximumSize(new Dimension(result.operationComponent.getMaximumSize().width,
				result.operationComponent.getPreferredSize().height));

		// placeholder for data component; will be replaced depending on operation
		result.dataComponent = new JLabel();
		result.dataComponent.setMaximumSize(new Dimension(result.dataComponent.getMaximumSize().width,
				result.dataComponent.getPreferredSize().height));

		result.openParenthesis = createParenthesisCheckBox("(", false);
		result.openParenthesis.setVisible(false);

		result.closeParenthesis = createParenthesisCheckBox(")", false);
		result.closeParenthesis.setVisible(false);

		if (operations.length >= 1) {
			addDataComponent(result,
					(result.operationComponent instanceof JComboBox)
							? ((JComboBox<?>) result.operationComponent).getSelectedIndex()
							: 0);
		}

		return result;
	}

	private static JCheckBox createNOTCheckBox() {
		JCheckBox jCheckBox = new JCheckBox("not", new ImageIcon());
		jCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		jCheckBox.setFocusable(false);
		updateNOTCheckBox(jCheckBox);
		jCheckBox.addChangeListener(changeEvent -> updateNOTCheckBox(jCheckBox));

		jCheckBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				jCheckBox.setForeground(new Color(Globals.OPSI_WARNING.getRed(), Globals.OPSI_WARNING.getGreen(),
						Globals.OPSI_WARNING.getBlue(), 128));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				updateNOTCheckBox(jCheckBox);
			}
		});

		return jCheckBox;
	}

	private static void updateNOTCheckBox(JCheckBox jCheckBox) {
		jCheckBox.setForeground(new Color(Globals.OPSI_WARNING.getRed(), Globals.OPSI_WARNING.getGreen(),
				Globals.OPSI_WARNING.getBlue(), jCheckBox.isSelected() ? 255 : 0));
	}

	private static JCheckBox createANDORCheckBox() {
		JCheckBox jCheckBox = new JCheckBox("and", new ImageIcon(), true);
		jCheckBox.setFocusable(false);
		jCheckBox.setForeground(Globals.OPSI_WARNING);
		jCheckBox.addChangeListener(changeEvent -> jCheckBox.setText(jCheckBox.isSelected() ? "and" : "or"));

		jCheckBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				jCheckBox.setText(jCheckBox.isSelected() ? "or" : "and");
				jCheckBox.setForeground(new Color(Globals.OPSI_WARNING.getRed(), Globals.OPSI_WARNING.getGreen(),
						Globals.OPSI_WARNING.getBlue(), 128));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				jCheckBox.setText(jCheckBox.isSelected() ? "and" : "or");
				jCheckBox.setForeground(Globals.OPSI_WARNING);
			}
		});

		return jCheckBox;
	}

	private static JCheckBox createParenthesisCheckBox(String type, boolean enabled) {
		JCheckBox jCheckBox = new JCheckBox(type, new ImageIcon(), true);
		jCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		jCheckBox.setFocusable(false);
		jCheckBox.setDisabledIcon(new ImageIcon());
		jCheckBox.setEnabled(enabled);

		jCheckBox.addItemListener((ItemEvent itemEvent) -> {
			// We change the alpha value of the item. When the checkbox is not selected, it will be less visible
			int alpha = jCheckBox.isSelected() ? 255 : 64;
			Color foreground = jCheckBox.getForeground();
			foreground = new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), alpha);
			jCheckBox.setForeground(foreground);
		});

		// We want to macke the parenthesis a little larger
		jCheckBox.setFont(jCheckBox.getFont().deriveFont((float) (jCheckBox.getFont().getSize() + 5)));
		return jCheckBox;
	}

	private ComplexGroup createHostGroup() {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.HOST_GROUP;
		result.topLabel.setText(Configed.getResourceValue("ClientSelectionDialog.hostGroup") + ":");

		result.groupList.add(createSimpleGroup(new GroupElement(
				persistenceController.getDataServices().group.getHostGroupIds().toArray(new String[0]))));
		result.groupList.add(createSimpleGroup(new GroupWithSubgroupsElement(
				persistenceController.getDataServices().group.getHostGroupIds().toArray(new String[0]))));
		result.groupList.add(createSimpleGroup(
				new NameElement(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientName"))));
		result.groupList.add(createSimpleGroup(new IPElement()));
		result.groupList.add(createSimpleGroup(new ConnectionElement()));
		result.groupList.add(createSimpleGroup(new DescriptionElement()));
		result.groupList.getLast().connectionType.setVisible(false);
		createComplexBottom(result);
		return result;
	}

	private ComplexGroup createSoftwareGroup() {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.SOFTWARE_GROUP;
		result.topLabel.setText(Configed.getResourceValue("ClientSelectionDialog.softwareGroup") + ":");

		result.groupList.add(createSimpleGroup(manager.getNewSoftwareNameElement()));
		result.groupList.getLast().connectionType.setVisible(false);
		result.groupList.getLast().negateButton.setVisible(false);

		result.groupList.add(createSimpleGroup(new SoftwareInstallationStatusElement()));

		result.groupList.add(createSimpleGroup(new SoftwareActionResultElement()));
		result.groupList.add(createSimpleGroup(new SoftwareRequestElement()));
		result.groupList.add(createSimpleGroup(new SoftwareActionProgressElement()));
		result.groupList.add(createSimpleGroup(new SoftwareLastActionElement()));
		result.groupList.add(createSimpleGroup(new SoftwareVersionElement()));
		result.groupList.add(createSimpleGroup(new SoftwarePackageVersionElement()));
		result.groupList.add(createSimpleGroup(new SoftwareModificationTimeElement()));
		result.groupList.getLast().connectionType.setVisible(false);

		createComplexBottom(result);
		return result;
	}

	private ComplexGroup createHardwareGroup(String hardware) {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.HARDWARE_GROUP;
		result.topLabel.setText(hardware);

		List<AbstractSelectElement> elements = manager.getBackend().getLocalizedHardwareList().get(hardware);
		if (elements == null) {
			Logging.warning(this, "hardware ", hardware, " not found in localized hardware list");
		} else {
			for (AbstractSelectElement element : elements) {
				result.groupList.add(createSimpleGroup(element));
			}
			result.groupList.getFirst().connectionType.setVisible(false);

			result.groupList.getLast().connectionType.setVisible(false);
			createComplexBottom(result);
		}

		return result;
	}

	private ComplexGroup createSwAuditGroup() {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.SW_AUDIT_GROUP;
		result.topLabel.setText(Configed.getResourceValue("ClientSelectionDialog.swAuditGroup") + ":");

		result.groupList.add(createSimpleGroup(new SwAuditNameElement()));
		result.groupList.getLast().connectionType.setVisible(false);

		result.groupList.add(createSimpleGroup(new SwAuditVersionElement()));
		result.groupList.add(createSimpleGroup(new SwAuditSubversionElement()));
		result.groupList.add(createSimpleGroup(new SwAuditArchitectureElement()));
		result.groupList.add(createSimpleGroup(new SwAuditLanguageElement()));
		result.groupList.add(createSimpleGroup(new SwAuditSoftwareIdElement()));
		result.groupList.getLast().connectionType.setVisible(false);
		createComplexBottom(result);
		return result;
	}

	/*
	 * This creates one of the groups like software, hardware, ..., containing
	 * multiple SimpleGroups.
	 */
	private ComplexGroup createComplexGroup() {
		ComplexGroup result = new ComplexGroup();

		result.removeButton = new JButton(Icons.getIntellijIcon("delete"));
		result.removeButton.addActionListener(this::removeButton);

		result.negateButton = createNOTCheckBox();
		result.negateButton.setMaximumSize(new Dimension(result.negateButton.getMaximumSize().width,
				result.negateButton.getPreferredSize().height));

		result.topLabel = new JLabel();
		result.topLabel.setFont(result.topLabel.getFont().deriveFont(Font.BOLD));

		result.openParenthesis = createParenthesisCheckBox("(", true);
		result.openParenthesis.setSelected(false);

		result.groupList = new LinkedList<>();
		return result;
	}

	/* This creates the bottom line of a complex group */
	private void createComplexBottom(ComplexGroup group) {
		group.closeParenthesis = createParenthesisCheckBox(")", true);
		group.closeParenthesis.setSelected(false);

		group.connectionType = createANDORCheckBox();
		group.connectionType.addActionListener(actionEvent -> buildParentheses());
		group.connectionType.setMaximumSize(new Dimension(group.connectionType.getMaximumSize().width,
				group.connectionType.getPreferredSize().height));
	}

	/* Gets the selected operation and adds the given data to it. */
	private static AbstractSelectOperation getOperation(SimpleGroup group) {
		int operationIndex;
		if (group.operationComponent instanceof JComboBox) {
			operationIndex = ((JComboBox<?>) group.operationComponent).getSelectedIndex();
		} else {
			operationIndex = 0;
		}

		AbstractSelectOperation operation = group.element.supportedOperations().get(operationIndex);

		SelectData.DataType type = operation.getDataType();

		Object data = getData(type, group.dataComponent);

		if (data == null) {
			return null;
		} else {
			operation.setSelectData(new SelectData(data, type));
			return operation;
		}
	}

	private static Object getData(SelectData.DataType type, JComponent dataComponent) {
		return switch (type) {
		case DOUBLE_TYPE, TEXT_TYPE, DATE_TYPE -> {
			String text = ((TextInputField) dataComponent).getText();
			yield text.isEmpty() ? null : text;
		}

		case INTEGER_TYPE -> {
			Integer integer = (Integer) ((JSpinner) dataComponent).getValue();
			yield (integer == 0) ? null : integer;
		}

		case BIG_INTEGER_TYPE -> {
			Long longValue = (Long) ((JSpinner) dataComponent).getValue();
			yield (longValue == 0) ? null : longValue;
		}

		case BOOLEAN_TYPE -> {
			State state = ((FlatTriStateCheckBox) dataComponent).getState();
			yield state == FlatTriStateCheckBox.State.INDETERMINATE ? null
					: (state == FlatTriStateCheckBox.State.SELECTED);
		}
		case NONE_TYPE -> null;
		default -> {
			Logging.error("Unknown data type: ", type);
			yield null;
		}
		};
	}

	/*
	 * Get the status of this SimpleGroup, i.e. the logical connection to the other
	 * groups.
	 */
	private OperationWithStatus getInformation(SimpleGroup group) {
		OperationWithStatus info = new OperationWithStatus();
		info.setOperation(null);
		info.setParenthesisOpen(group.openParenthesis.isVisible());
		info.setParenthesisClose(group.closeParenthesis.isVisible());
		boolean andSelected = group.connectionType.isSelected();
		Logging.debug(this, group.element.getPath(), ": AND selected: ", andSelected);
		boolean notSelected = group.negateButton.isSelected();
		info.setStatus(getStatus(andSelected, notSelected));
		return info;
	}

	private static OperationWithStatus getInformation(ComplexGroup group) {
		OperationWithStatus info = new OperationWithStatus();
		info.setOperation(null);
		info.setParenthesisOpen(group.openParenthesis.isSelected());
		info.setParenthesisClose(group.closeParenthesis.isSelected());
		boolean andSelected = group.connectionType.isSelected();
		boolean notSelected = group.negateButton.isSelected();
		info.setStatus(getStatus(andSelected, notSelected));
		return info;
	}

	private static ConnectionStatus getStatus(boolean andSelected, boolean notSelected) {
		ConnectionStatus conStatus;
		if (andSelected) {
			if (notSelected) {
				conStatus = ConnectionStatus.AND_NOT;
			} else {
				conStatus = ConnectionStatus.AND;
			}
		} else {
			if (notSelected) {
				conStatus = ConnectionStatus.OR_NOT;
			} else {
				conStatus = ConnectionStatus.OR;
			}
		}
		return conStatus;
	}

	private void renderAll() {
		JLabel negationLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.negateColumn"));
		JLabel nameLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.nameColumn"));
		JLabel dataLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.dataColumn"));
		JLabel connectionLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.connectionColumn"));

		contentPane.removeAll();

		contentPane.add(negationLabel, "skip 1");
		contentPane.add(nameLabel);
		contentPane.add(dataLabel, "skip 1");
		contentPane.add(connectionLabel, "skip 1, w 100!, wrap");

		for (ComplexGroup complex : complexElements) {
			contentPane.add(complex.openParenthesis);
			contentPane.add(complex.negateButton);
			contentPane.add(complex.topLabel, "spanx 4, growx");
			contentPane.add(complex.connectionType, "w 100!");
			contentPane.add(complex.removeButton, "align right, wrap");

			for (SimpleGroup simple : complex.groupList) {
				contentPane.add(simple.openParenthesis);
				contentPane.add(simple.negateButton);
				contentPane.add(simple.elementLabel);
				contentPane.add(simple.operationComponent, "w 70!");
				contentPane.add(simple.dataComponent, "w 100:100, growx");
				contentPane.add(simple.closeParenthesis);
				contentPane.add(simple.connectionType, "w 100!, wrap");
			}

			contentPane.add(complex.closeParenthesis, "skip 5");
			contentPane.add(complex.connectionType, "w 100!, wrap");
		}

		contentPane.add(newElementBox, "span 8, align left, gapleft " + Globals.MIN_GAP_SIZE + ", gaptop "
				+ Globals.GAP_SIZE + ", gapbottom " + Globals.GAP_SIZE + ", wrap");

		contentPane.revalidate();
		contentPane.repaint();
	}

	private static void showParenthesesForGroup(Deque<SimpleGroup> groups) {
		boolean inOr = false;
		for (SimpleGroup group : groups) {
			group.openParenthesis.setVisible(false);
			group.closeParenthesis.setVisible(false);
			if (getOperation(group) == null) {
				continue;
			}

			if (group.connectionType.isSelected() && inOr) {
				inOr = false;
				group.closeParenthesis.setVisible(true);
			}

			if (!group.connectionType.isSelected() && !inOr) {
				inOr = true;
				group.openParenthesis.setVisible(true);
			}
		}

		if (inOr) {
			SimpleGroup group = groups.getLast();
			group.closeParenthesis.setVisible(true);
		}
	}

	/* Show the parentheses making sure that or will be evaluated before and. */
	private void buildParentheses() {
		Logging.debug("BUILDPARENTHESES");
		for (ComplexGroup group : complexElements) {
			showParenthesesForGroup(group.groupList);
		}
	}

	private void repairParentheses() {
		Deque<ComplexGroup> stack = new ArrayDeque<>();
		for (ComplexGroup complex : complexElements) {
			if (complex.openParenthesis.isSelected() && complex.closeParenthesis.isSelected()) {
				complex.openParenthesis.setSelected(false);
				complex.closeParenthesis.setSelected(false);
			} else if (complex.openParenthesis.isSelected()) {
				stack.push(complex);
			} else if (complex.closeParenthesis.isSelected()) {
				if (!stack.isEmpty()) {
					stack.pop();
				} else {
					complex.closeParenthesis.setSelected(false);
				}
			} else {
				// Do nothing when no parenthesis is activated
			}
		}

		for (ComplexGroup stackElement : stack) {
			stackElement.openParenthesis.setSelected(false);
		}
	}

	/*
	 * Create a data component (where the user puts its data) for the given
	 * operation.
	 */
	private void addDataComponent(SimpleGroup sourceGroup, int operationIndex) {
		if (operationIndex == -1) {
			return;
		}
		switch (sourceGroup.element.supportedOperations().get(operationIndex).getDataType()) {
		case TEXT_TYPE -> addTextTypeComponent(sourceGroup);
		case DOUBLE_TYPE -> addDoubleTypeComponent(sourceGroup);
		case DATE_TYPE -> addDateTypeComponent(sourceGroup);
		case INTEGER_TYPE -> addIntegerTypeComponent(sourceGroup);
		case BIG_INTEGER_TYPE -> addBigIntegerTypeComponent(sourceGroup);
		case BOOLEAN_TYPE -> addBooleanTypeComponent(sourceGroup);
		case NONE_TYPE -> {
			// Nothing has to be done here
		}
		}
	}

	private void addTextTypeComponent(SimpleGroup sourceGroup) {
		TextInputField fieldText = new TextInputField(sourceGroup.element.getEnumData());
		if (sourceGroup.element instanceof ConnectionElement) {
			fieldText.setReadOnly(true);
			fieldText.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.readOnlyTextInputToolTip"));
		} else {
			fieldText.setEditable(true);
			fieldText.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.textInputToolTip"));
		}
		fieldText.setClientSelectionDialog(this);
		sourceGroup.dataComponent = fieldText;
	}

	private void addDateTypeComponent(SimpleGroup sourceGroup) {
		TextInputField fieldDate = new TextInputField();
		fieldDate.setToolTipText("yyyy-mm-dd");
		fieldDate.setClientSelectionDialog(this);
		sourceGroup.dataComponent = fieldDate;
	}

	private void addIntegerTypeComponent(SimpleGroup sourceGroup) {
		JSpinner spinner = new JSpinner();
		spinner.addChangeListener(event -> buildParentheses());
		sourceGroup.dataComponent = spinner;
	}

	private void addBigIntegerTypeComponent(SimpleGroup sourceGroup) {
		SpinnerWithExtension swx = new SpinnerWithExtension();
		swx.addChangeListener(event -> buildParentheses());

		sourceGroup.dataComponent = swx;
	}

	private void addDoubleTypeComponent(SimpleGroup sourceGroup) {
		TextInputField fieldDouble = new TextInputField();
		fieldDouble.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.textInputToolTip"));
		fieldDouble.setClientSelectionDialog(this);
		sourceGroup.dataComponent = fieldDouble;
	}

	private static void addBooleanTypeComponent(SimpleGroup sourceGroup) {
		FlatTriStateCheckBox checkBox = new FlatTriStateCheckBox();
		checkBox.setFocusable(false);
		checkBox.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.booleanInputToolTip"));
		sourceGroup.dataComponent = checkBox;
	}

	/*
	 * Collect the data and tell it to the SelectionManager, so it can use it to
	 * start the client filtering.
	 */
	private void collectData() {
		Logging.info(this, "collectData  complexElements ", complexElements);
		manager.clearOperations();
		Logging.info(this, "collectData  complexElements ", complexElements);
		repairParentheses();
		for (ComplexGroup complex : complexElements) {
			OperationWithStatus groupStatus;
			groupStatus = getInformation(complex);

			List<OperationWithStatus> childList = new LinkedList<>();

			for (SimpleGroup group : complex.groupList) {
				AbstractSelectOperation op = getOperation(group);
				if (op != null) {
					OperationWithStatus ows = getInformation(group);
					ows.setOperation(op);
					childList.add(ows);
				}
			}

			if (!childList.isEmpty()) {
				manager.addGroupOperation(complex.type, groupStatus, childList);
			}
		}
	}

	/*
	 * Reset the view to default state, i.e. a host and a software group with empty
	 * data fields.
	 */
	private void reset() {
		Logging.debug(this, "RESET");
		saveNameField.setText("");
		saveDescriptionField.setText("");
		complexElements.clear();
		complexElements.add(createHostGroup());
		complexElements.add(createSoftwareGroup());
		buildParentheses();
		renderAll();
	}

	/*
	 * This is used after a saved search was loaded. It displays the loaded search
	 * in the interface.
	 */
	private void loadFromManager() {
		complexElements.clear();

		List<OperationWithStatus> topList;
		topList = manager.operationsAsList(null);
		Logging.debug(this, "load: size: ", topList.size());
		for (int i = 0; i < topList.size(); i++) {
			OperationWithStatus ows = topList.get(i);
			AbstractSelectOperation op = ows.getOperation();
			if (op == null) {
				reset();
				return;
			}
			ComplexGroup element;
			if (op instanceof HostOperation) {
				element = createHostGroup();
			} else if (op instanceof SoftwareOperation) {
				element = createSoftwareGroup();
			} else if (op instanceof SwAuditOperation) {
				element = createSwAuditGroup();
			} else if (op instanceof HardwareOperation hardwareOperation) {
				element = createHardwareGroup(
						getNonGroupOperation(hardwareOperation).getElement().getLocalizedPathArray()[0]);
			} else {
				Logging.error("Not a group operation: ", op.getClassName());
				reset();
				return;
			}

			complexElements.add(element);
			setConnectionTypes(element.connectionType, element.negateButton, ows.getStatus());
			List<OperationWithStatus> subList;
			subList = manager.operationsAsList(((AbstractSelectGroupOperation) op).getChildOperations().get(0));
			Logging.debug(this, "subload: ", subList.size());
			setGroupValues(element, subList);
		}
		if (!complexElements.isEmpty()) {
			complexElements.getLast().connectionType.setVisible(false);
		}
		buildParentheses();
		renderAll();
	}

	private static AbstractSelectOperation getNonGroupOperation(AbstractSelectGroupOperation operation) {
		AbstractSelectOperation child = operation.getChildOperations().get(0);
		while (child instanceof AbstractSelectGroupOperation abstractSelectGroupOperation) {
			child = abstractSelectGroupOperation.getChildOperations().get(0);
		}
		return child;
	}

	private void setGroupValues(ComplexGroup group, List<OperationWithStatus> owsList) {
		for (OperationWithStatus ows : owsList) {
			for (SimpleGroup simpleGroup : group.groupList) {
				AbstractSelectOperation selectOperation = ows.getOperation();
				if (selectOperation.getElement().getPath().equals(simpleGroup.element.getPath())) {
					treatOperation(ows, selectOperation, simpleGroup);
					break;
				}
			}
		}
	}

	private void treatOperation(OperationWithStatus ows, AbstractSelectOperation selectOperation,
			SimpleGroup simpleGroup) {
		if (selectOperation.getElement().supportedOperations().size() > 1) {
			((JComboBox<?>) simpleGroup.operationComponent).setSelectedItem(selectOperation.getOperationString());
		}
		setComponentData(simpleGroup.dataComponent, selectOperation.getSelectData());
		setConnectionTypes(simpleGroup.connectionType, simpleGroup.negateButton, ows.getStatus());
		Logging.debug(this, "simple, open, closed: ", simpleGroup.element.getClassName(), ows.isParenthesisOpen(),
				ows.isParenthesisClosed());
	}

	private static void setComponentData(JComponent component, SelectData data) {
		if (data == null || data.getData() == null) {
			return;
		}

		switch (component) {
		case TextInputField textInputField -> textInputField.setText(data.getData().toString());
		case SpinnerWithExtension spinnerWithExtension when data
				.getType() == SelectData.DataType.BIG_INTEGER_TYPE -> spinnerWithExtension
						.setValue((Long) data.getData());
		case JSpinner jSpinner when data.getType() == SelectData.DataType.INTEGER_TYPE -> jSpinner
				.setValue(data.getData());
		case FlatTriStateCheckBox flatTriStateCheckBox -> flatTriStateCheckBox.setSelected((Boolean) data.getData());
		default -> Logging.warning("component ", component, " with datatype ", data.getType(), " not treated");
		}
	}

	private static void setConnectionTypes(JCheckBox andOr, AbstractButton not, ConnectionStatus status) {
		switch (status) {
		case AND -> andOr.setSelected(true);
		case OR -> andOr.setSelected(false);
		case AND_NOT -> {
			andOr.setSelected(true);
			not.setSelected(true);
		}
		case OR_NOT -> {
			andOr.setSelected(false);
			not.setSelected(true);
		}
		}
	}

	private static class SimpleGroup {
		private AbstractSelectElement element;
		private JCheckBox negateButton;
		private JCheckBox connectionType;
		private JLabel elementLabel;

		// may be JLabel or JComboBox
		private JComponent operationComponent;
		private JComponent dataComponent;
		private JCheckBox openParenthesis;
		private JCheckBox closeParenthesis;
	}

	public enum GroupType {
		HOST_GROUP, SOFTWARE_GROUP, SW_AUDIT_GROUP, HARDWARE_GROUP
	}

	private static class ComplexGroup {
		private GroupType type;
		private JButton removeButton;
		private JCheckBox negateButton;
		private JLabel topLabel;
		private JCheckBox connectionType;
		private Deque<SimpleGroup> groupList;
		private JCheckBox openParenthesis;
		private JCheckBox closeParenthesis;

		@Override
		public String toString() {
			return "ComplexGroup type " + type;
		}
	}

	private void removeButton(ActionEvent e) {
		Iterator<ComplexGroup> complexIterator = complexElements.iterator();
		while (complexIterator.hasNext()) {
			ComplexGroup group = complexIterator.next();

			Logging.info(this, "removing group of type ", group.type);

			if (group.removeButton == e.getSource()) {
				complexIterator.remove();
				buildParentheses();
				break;
			}
		}

		if (!complexElements.isEmpty()) {
			complexElements.getLast().connectionType.setVisible(false);
		}
		renderAll();
	}

	private void addElement() {
		if (!complexElements.isEmpty()) {
			complexElements.getLast().connectionType.setVisible(true);
		}

		int index = newElementBox.getSelectedIndex();

		ComplexGroup complexGroup = switch (index) {
		case 0 -> null;
		case 1 -> createHostGroup();
		case 2 -> createSoftwareGroup();
		case 3 -> createSwAuditGroup();
		default -> createHardwareGroup(newElementBox.getSelectedItem().toString());
		};

		if (complexGroup != null) {
			complexElements.add(complexGroup);
		}

		renderAll();
		newElementBox.setSelectedIndex(0);
		complexElements.getLast().connectionType.setVisible(false);

		resizeScrollPane();
	}

	private void resizeScrollPane() {
		// This will change the width of the dialog so that all the content will be visible
		// and we only need a vertical scrollbar
		int diffSize = scrollPane.getPreferredSize().width - scrollPane.getSize().width
				+ scrollPane.getVerticalScrollBar().getWidth();
		dialog.setSize(dialog.getWidth() + diffSize, dialog.getHeight());
		SwingUtilities.invokeLater(
				() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
	}

	private void selectOperation(ActionEvent actionEvent) {
		JComponent source = null;
		SimpleGroup sourceGroup = null;
		for (ComplexGroup group : complexElements) {
			for (SimpleGroup simple : group.groupList) {
				if (simple.operationComponent == actionEvent.getSource()) {
					source = simple.operationComponent;
					sourceGroup = simple;
					break;
				}
			}
			if (source != null) {
				break;
			}
		}
		if (source == null) {
			return;
		}

		sourceGroup.dataComponent = null;

		int index = 0;
		if (source instanceof JComboBox) {
			index = ((JComboBox<?>) source).getSelectedIndex();
		} else if (source instanceof JLabel) {
			index = 0;
		} else {
			Logging.warning(this, "unexpected source in selectOperation: ", source);
		}
		addDataComponent(sourceGroup, index);

		buildParentheses();

		renderAll();
	}

	private void save() {
		String text = saveNameField.getText();
		if (text.isEmpty()) {
			JOptionPane.showMessageDialog(dialog, Configed.getResourceValue("ClientSelectionDialog.emptyName"),
					Configed.getResourceValue("ClientSelectionDialog.emptyNameTitle"), JOptionPane.OK_OPTION);
		} else if (searchNamePattern.matcher(text).matches()) {
			collectData();
			manager.saveSearch(text, saveDescriptionField.getText());
			savedSearchesDialog.reloadAction();
		} else {
			JOptionPane.showMessageDialog(dialog, "wrong name", "error", JOptionPane.OK_OPTION);
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		buildParentheses();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		buildParentheses();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		buildParentheses();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		buildParentheses();
	}
}
