/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.ConnectionStatus;
import de.uib.configed.clientselection.OperationWithStatus;
import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.clientselection.elements.DescriptionElement;
import de.uib.configed.clientselection.elements.GroupElement;
import de.uib.configed.clientselection.elements.GroupWithSubgroupsElement;
import de.uib.configed.clientselection.elements.IPElement;
import de.uib.configed.clientselection.elements.NameElement;
import de.uib.configed.clientselection.elements.SoftwareActionProgressElement;
import de.uib.configed.clientselection.elements.SoftwareActionResultElement;
import de.uib.configed.clientselection.elements.SoftwareInstallationStatusElement;
import de.uib.configed.clientselection.elements.SoftwareLastActionElement;
import de.uib.configed.clientselection.elements.SoftwareModificationTimeElement;
import de.uib.configed.clientselection.elements.SoftwarePackageVersionElement;
import de.uib.configed.clientselection.elements.SoftwareRequestElement;
import de.uib.configed.clientselection.elements.SoftwareVersionElement;
import de.uib.configed.clientselection.elements.SwAuditArchitectureElement;
import de.uib.configed.clientselection.elements.SwAuditLanguageElement;
import de.uib.configed.clientselection.elements.SwAuditNameElement;
import de.uib.configed.clientselection.elements.SwAuditSoftwareIdElement;
import de.uib.configed.clientselection.elements.SwAuditSubversionElement;
import de.uib.configed.clientselection.elements.SwAuditVersionElement;
import de.uib.configed.clientselection.operations.HardwareOperation;
import de.uib.configed.clientselection.operations.HostOperation;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.type.SavedSearch;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;
import de.uib.utils.swing.LowerCaseTextField;
import de.uib.utils.swing.TextInputField;

/**
 * This dialog shows a number of options you can use to select specific clients.
 */
public class ClientSelectionDialog extends FGeneralDialog implements ActionListener, DocumentListener {
	private static final Pattern searchNamePattern = Pattern.compile("[\\p{javaLowerCase}\\d_-]*");

	private static final int FRAME_WIDTH = 750;
	private static final int FRAME_HEIGHT = 650;

	private GroupLayout layout;
	private GroupLayout.SequentialGroup vGroup;
	private GroupLayout.ParallelGroup hGroupParenthesisClose;
	private GroupLayout.ParallelGroup hGroupParenthesisOpen;
	private GroupLayout.ParallelGroup hGroupRemoveBtn;
	private GroupLayout.ParallelGroup hGroupNegate;
	private GroupLayout.ParallelGroup hGroupConnections;
	private GroupLayout.ParallelGroup hGroupElements;
	private GroupLayout.ParallelGroup hGroupOperations;
	private GroupLayout.ParallelGroup hGroupData;
	private JPanel contentPane;
	private JComboBox<String> newElementBox;
	private JButton buttonReload;
	private JButton buttonRestart;
	private JTextField saveNameField;
	private JTextField saveDescriptionField;
	private JButton saveButton;

	private LinkedList<ComplexGroup> complexElements;

	private SelectionManager manager;
	private ClientTable selectionPanel;
	private SavedSearchesDialog savedSearchesDialog;

	private ConfigedMain configedMain;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	public ClientSelectionDialog(ConfigedMain configedMain, ClientTable selectionPanel,
			SavedSearchesDialog savedSearchesDialog) {
		super(null, Configed.getResourceValue("MainFrame.jMenuClientselectionGetGroup"), false,
				new String[] { Configed.getResourceValue("buttonClose"),
						Configed.getResourceValue("ClientSelectionDialog.buttonReset"),
						Configed.getResourceValue("ClientSelectionDialog.buttonSet") },
				FRAME_WIDTH, FRAME_HEIGHT);

		this.configedMain = configedMain;
		this.selectionPanel = selectionPanel;
		this.savedSearchesDialog = savedSearchesDialog;
		super.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		manager = new SelectionManager("OpsiData");
		complexElements = new LinkedList<>();
		init();

		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Logging.info(this, "ClientSelectionDialog resized");
				// move it up and down for fixing the combobox popup vanishing
				Component c = e.getComponent();
				Point point = c.getLocation();
				Point savePoint = new Point(point);
				point.setLocation(point.getX(), point.getY() + 1.0);
				c.setLocation(point);
				c.revalidate();
				c.repaint();
				c.setLocation(savePoint);
				c.revalidate();
				c.repaint();
			}
		});
	}

	public void loadSearch(String name) {
		Logging.info(this, "loadSearch " + name);

		manager.loadSearch(name);
		loadFromManager();
		SavedSearch search = persistenceController.getConfigDataService().getSavedSearchesPD().get(name);
		saveNameField.setText(search.getName());
		saveDescriptionField.setText(search.getDescription());
	}

	@Override
	public void doAction3() {
		Logging.info(this, "doAction3");
		List<String> clients = new ArrayList<>();

		collectData();

		// because of potential memory problems we switch to
		// client view
		configedMain.setVisualViewIndex(ConfigedMain.VIEW_CLIENTS);

		if (manager != null) {
			clients = manager.selectClients();
		}

		if (clients == null) {
			return;
		}

		Logging.debug(this, clients.toString());
		selectionPanel.setSelectedValues(clients);
	}

	@Override
	public void doAction2() {
		reset();
	}

	@Override
	protected void initComponents() {
		additionalPane = new JPanel();

		GroupLayout additionalLayout = new GroupLayout(additionalPane);
		additionalPane.setLayout(additionalLayout);
		additionalPane.setMinimumSize(new Dimension(200, 200));

		saveNameField = new LowerCaseTextField();
		saveNameField.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.searchnameFormat"));

		saveDescriptionField = new JTextField();

		JLabel saveNameLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.inquiryName"));

		JLabel saveDescriptionLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.inquiryDescription"));

		saveButton = new JButton(Utils.getIntellijIcon("save"));
		saveButton.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.saveSearchTooltip"));
		saveButton.addActionListener(actionEvent -> save());

		buttonReload = new JButton(Utils.getIntellijIcon("refresh"));
		buttonReload.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.buttonReload"));
		buttonReload.addActionListener((ActionEvent e) -> reload());

		buttonRestart = new JButton(Utils.getIntellijIcon("reset"));
		buttonRestart.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.buttonRestart"));

		buttonRestart.addActionListener((ActionEvent e) -> restart());

		additionalLayout.setHorizontalGroup(additionalLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(saveNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(saveNameField, 40, 100, 200).addGap(Globals.GAP_SIZE)
				.addComponent(saveDescriptionLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE).addComponent(saveDescriptionField, 40, 200, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(saveButton, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE)
				.addComponent(buttonReload, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(buttonRestart, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.GAP_SIZE));

		additionalLayout.setVerticalGroup(additionalLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(saveNameLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(saveNameField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(saveDescriptionLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(saveDescriptionField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(saveButton, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonReload, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonRestart, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT));

		additionalPane.setVisible(true);
	}

	private void init() {
		contentPane = new JPanel();

		layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);

		layout.setAutoCreateContainerGaps(true);
		layout.setHonorsVisibility(false);

		GroupLayout.SequentialGroup vMainGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup hMainGroup = layout.createParallelGroup();

		vGroup = layout.createSequentialGroup();
		GroupLayout.ParallelGroup vHeadlines = layout.createParallelGroup();
		vGroup.addGroup(vHeadlines);
		vMainGroup.addGroup(vGroup);

		layout.setVerticalGroup(vMainGroup);
		hGroupParenthesisClose = layout.createParallelGroup();
		hGroupParenthesisOpen = layout.createParallelGroup();
		hGroupRemoveBtn = layout.createParallelGroup();
		hGroupNegate = layout.createParallelGroup();
		hGroupConnections = layout.createParallelGroup();
		hGroupElements = layout.createParallelGroup();
		hGroupOperations = layout.createParallelGroup();
		hGroupData = layout.createParallelGroup();
		hMainGroup.addGroup(layout.createSequentialGroup().addGroup(hGroupParenthesisOpen).addGap(Globals.MIN_GAP_SIZE)
				.addGroup(hGroupNegate).addGap(Globals.MIN_GAP_SIZE).addGroup(hGroupElements)
				.addGap(Globals.MIN_GAP_SIZE).addGroup(hGroupOperations).addGap(Globals.MIN_GAP_SIZE)
				.addGroup(hGroupData).addGap(Globals.MIN_GAP_SIZE).addGroup(hGroupParenthesisClose)
				.addGap(Globals.MIN_GAP_SIZE).addGroup(hGroupConnections).addGap(Globals.MIN_GAP_SIZE)
				.addGroup(hGroupRemoveBtn));
		layout.setHorizontalGroup(hMainGroup);

		// columns headline
		JLabel negationLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.negateColumn"));

		JLabel nameLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.nameColumn"));

		JLabel dataLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.dataColumn"));

		JLabel connectionLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.connectionColumn"));

		vHeadlines.addComponent(negationLabel);
		vHeadlines.addComponent(nameLabel);

		vHeadlines.addComponent(dataLabel);
		vHeadlines.addComponent(connectionLabel);

		hGroupNegate.addComponent(negationLabel, GroupLayout.Alignment.CENTER);
		hGroupElements.addComponent(nameLabel, GroupLayout.Alignment.CENTER);

		hGroupData.addComponent(dataLabel, GroupLayout.Alignment.CENTER);
		hGroupConnections.addComponent(connectionLabel, GroupLayout.Alignment.CENTER);

		newElementBox = new JComboBox<>(
				new String[] { Configed.getResourceValue("ClientSelectionDialog.newElementsBox") });

		newElementBox.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);
		newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.hostName"));
		newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.softwareName"));
		newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.swauditName"));

		// hardware
		List<String> hardwareList = new LinkedList<>(manager.getBackend().getLocalizedHardwareList().keySet());
		Collections.sort(hardwareList);
		for (String hardware : hardwareList) {
			newElementBox.addItem(hardware);
		}

		newElementBox.addActionListener(actionEvent -> addElement());

		vMainGroup.addComponent(newElementBox, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);
		hMainGroup.addComponent(newElementBox, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH);

		complexElements.add(createHostGroup());
		complexElements.add(createSoftwareGroup());
		complexElements.getLast().connectionType.setVisible(false);
		scrollpane.getViewport().add(contentPane);
	}

	private void reload() {
		Logging.info(this, "actionPerformed");
		buttonReload.setEnabled(false);
		buttonRestart.setEnabled(false);
		setCursor(Globals.WAIT_CURSOR);
		SwingUtilities.invokeLater(() -> {
			manager.getBackend().setReloadRequested();
			buttonReload.setEnabled(true);
			buttonRestart.setEnabled(true);
			setCursor(null);
		});
	}

	private void restart() {
		Logging.info(this, "actionPerformed");
		buttonRestart.setEnabled(false);
		buttonReload.setEnabled(false);
		setCursor(Globals.WAIT_CURSOR);
		SwingUtilities.invokeLater(() -> {
			manager.getBackend().setReloadRequested();
			configedMain.callNewClientSelectionDialog();
			setCursor(null);
			// we lose all components of this dialog, there is nothing to reset
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
		} else {
			result.operationComponent = new JLabel(operations[0].getOperationString(), SwingConstants.CENTER);
		}
		result.operationComponent.setMaximumSize(new Dimension(result.operationComponent.getMaximumSize().width,
				result.operationComponent.getPreferredSize().height));

		// to reserve the place
		result.dataComponent = new JLabel();
		result.dataComponent.setMaximumSize(new Dimension(result.dataComponent.getMaximumSize().width,
				result.dataComponent.getPreferredSize().height));

		result.openParenthesis = createParenthesisCheckBox("(", false);
		result.openParenthesis.setVisible(false);

		result.closeParenthesis = createParenthesisCheckBox(")", false);
		result.closeParenthesis.setVisible(false);

		result.vRow = layout.createParallelGroup();
		result.vRow.addComponent(result.negateButton, GroupLayout.Alignment.CENTER);
		result.vRow.addComponent(result.connectionType, GroupLayout.Alignment.CENTER);
		result.vRow.addComponent(result.elementLabel, GroupLayout.Alignment.CENTER);
		result.vRow.addComponent(result.operationComponent, GroupLayout.Alignment.CENTER);
		result.vRow.addComponent(result.dataComponent, GroupLayout.Alignment.CENTER);
		result.vRow.addComponent(result.openParenthesis, GroupLayout.Alignment.CENTER, 20, 20, 20);
		result.vRow.addComponent(result.closeParenthesis, GroupLayout.Alignment.CENTER, 20, 20, 20);

		vGroup.addGroup(result.vRow);

		hGroupNegate.addComponent(result.negateButton, 10, 40, 50);
		hGroupConnections.addComponent(result.connectionType, 100, 100, 100);
		hGroupElements.addComponent(result.elementLabel);
		hGroupOperations.addComponent(result.operationComponent, 65, 70, 70);
		hGroupData.addComponent(result.dataComponent, 100, 100, Short.MAX_VALUE);
		hGroupParenthesisOpen.addComponent(result.openParenthesis, 20, 20, 20);
		hGroupParenthesisClose.addComponent(result.closeParenthesis, 20, 20, 20);

		if (operations.length > 1) {
			((JComboBox<?>) result.operationComponent).addActionListener(this::selectOperation);
			addDataComponent(result, ((JComboBox<?>) result.operationComponent).getSelectedIndex());
		} else if (operations.length == 1) {
			addDataComponent(result, 0);
		} else {
			// Do nothing with no operations
		}

		return result;
	}

	private static JCheckBox createNOTCheckBox() {
		JCheckBox jCheckBox = new JCheckBox(new ImageIcon());
		jCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		jCheckBox.setForeground(Globals.OPSI_WARNING);
		jCheckBox.addActionListener(actionEvent -> jCheckBox.setText(jCheckBox.isSelected() ? "not" : ""));

		jCheckBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				jCheckBox.setText("not");
				jCheckBox.setForeground(new Color(Globals.OPSI_WARNING.getRed(), Globals.OPSI_WARNING.getGreen(),
						Globals.OPSI_WARNING.getBlue(), 128));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				jCheckBox.setText(jCheckBox.isSelected() ? "not" : "");
				jCheckBox.setForeground(Globals.OPSI_WARNING);
			}
		});

		return jCheckBox;
	}

	private static JCheckBox createANDORCheckBox() {
		JCheckBox jCheckBox = new JCheckBox("and", new ImageIcon(), true);
		jCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		jCheckBox.setForeground(Globals.OPSI_WARNING);

		jCheckBox.addActionListener(actionEvent -> jCheckBox.setText(jCheckBox.isSelected() ? "and" : "or"));

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
				persistenceController.getGroupDataService().getHostGroupIds().toArray(new String[0]))));
		result.groupList.add(createSimpleGroup(new GroupWithSubgroupsElement(
				persistenceController.getGroupDataService().getHostGroupIds().toArray(new String[0]))));
		result.groupList.add(createSimpleGroup(
				new NameElement(Configed.getResourceValue("ConfigedMain.pclistTableModel.clientName"))));
		result.groupList.add(createSimpleGroup(new IPElement()));
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

		result.removeButton = new JButton(Utils.getIntellijIcon("delete"));
		result.removeButton.addActionListener(this::removeButton);

		result.negateButton = createNOTCheckBox();
		result.negateButton.setMaximumSize(new Dimension(result.negateButton.getMaximumSize().width,
				result.negateButton.getPreferredSize().height));

		result.topLabel = new JLabel();
		result.topLabel.setMaximumSize(
				new Dimension(result.topLabel.getMaximumSize().width, result.removeButton.getPreferredSize().height));

		result.topLabel.setFont(result.topLabel.getFont().deriveFont(Font.BOLD));

		result.openParenthesis = createParenthesisCheckBox("(", true);
		result.openParenthesis.setSelected(false);

		GroupLayout.ParallelGroup vRow = layout.createParallelGroup();
		vRow.addComponent(result.topLabel, GroupLayout.Alignment.CENTER, 20, 20, 20);
		vRow.addComponent(result.removeButton, GroupLayout.Alignment.CENTER);
		vRow.addComponent(result.negateButton, GroupLayout.Alignment.CENTER);
		vRow.addComponent(result.openParenthesis, GroupLayout.Alignment.CENTER, 20, 20, 20);
		vGroup.addGroup(vRow);
		hGroupNegate.addComponent(result.negateButton, 10, 40, 50);
		hGroupRemoveBtn.addComponent(result.removeButton);
		hGroupElements.addComponent(result.topLabel);
		hGroupParenthesisOpen.addComponent(result.openParenthesis, 20, 20, 20);

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
		GroupLayout.ParallelGroup vRow = layout.createParallelGroup();
		vRow.addComponent(group.connectionType, GroupLayout.Alignment.CENTER);
		vRow.addComponent(group.closeParenthesis, GroupLayout.Alignment.CENTER, 20, 20, 20);
		vGroup.addGroup(vRow);
		hGroupConnections.addComponent(group.connectionType, 100, 100, 100);
		hGroupParenthesisClose.addComponent(group.closeParenthesis, 20, 20, 20);
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

		Object data = null;
		SelectData.DataType type = operation.getDataType();
		switch (type) {
		// Do the same for all four cases
		case DOUBLE_TYPE, TEXT_TYPE, DATE_TYPE:
			data = ((TextInputField) group.dataComponent).getText();
			if (((String) data).isEmpty()) {
				return null;
			}
			break;
		case INTEGER_TYPE:
			data = ((JSpinner) group.dataComponent).getValue();
			if (((Integer) data) == 0) {
				return null;
			}
			break;
		case BIG_INTEGER_TYPE:
			data = ((SpinnerWithExtension) group.dataComponent).getValue();
			if (((Long) data) == 0) {
				return null;
			}
			break;
		case NONE_TYPE:
		default:
		}

		operation.setSelectData(new SelectData(data, type));
		return operation;
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
		Logging.debug(this, group.element.getPath() + ": AND selected: " + andSelected);
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

	/* Remove a simple group from the display */
	private void removeGroup(SimpleGroup group) {
		contentPane.remove(group.negateButton);
		contentPane.remove(group.connectionType);
		contentPane.remove(group.elementLabel);
		contentPane.remove(group.operationComponent);
		if (group.dataComponent != null) {
			contentPane.remove(group.dataComponent);
		}
		contentPane.remove(group.openParenthesis);
		contentPane.remove(group.closeParenthesis);
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
		case TEXT_TYPE:
			addTextTypeComponent(sourceGroup);
			break;

		case DOUBLE_TYPE:
			addDoubleTypeComponent(sourceGroup);
			break;

		case DATE_TYPE:
			addDateTypeComponent(sourceGroup);
			break;

		case INTEGER_TYPE:
			addIntegerTypeComponent(sourceGroup);
			break;

		case BIG_INTEGER_TYPE:
			addBigIntegerTypeComponent(sourceGroup);
			break;

		case NONE_TYPE:
			return;
		}
		sourceGroup.dataComponent.setMaximumSize(new Dimension(sourceGroup.dataComponent.getMaximumSize().width,
				sourceGroup.dataComponent.getMinimumSize().height));
		int minHeight = Globals.LINE_HEIGHT;
		sourceGroup.vRow.addComponent(sourceGroup.dataComponent, GroupLayout.Alignment.CENTER, minHeight, minHeight,
				minHeight);
		hGroupData.addComponent(sourceGroup.dataComponent, 100, 100, Short.MAX_VALUE);
	}

	private void addTextTypeComponent(SimpleGroup sourceGroup) {
		TextInputField fieldText = new TextInputField("", sourceGroup.element.getEnumData());
		fieldText.setEditable(true);
		fieldText.setSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
		fieldText.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.textInputToolTip"));
		fieldText.setClientSelectionDialog(this);
		sourceGroup.dataComponent = fieldText;
	}

	private void addDateTypeComponent(SimpleGroup sourceGroup) {
		TextInputField fieldDate = new TextInputField(null);
		fieldDate.setSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
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
		TextInputField fieldDouble = new TextInputField("");
		fieldDouble.setSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
		fieldDouble.setToolTipText(
				/* "Use * as wildcard" */Configed.getResourceValue("ClientSelectionDialog.textInputToolTip"));
		fieldDouble.setClientSelectionDialog(this);
		sourceGroup.dataComponent = fieldDouble;
	}

	/*
	 * Collect the data and tell it to the SelectionManager, so it can use it to
	 * start the client filtering.
	 */
	private void collectData() {
		Logging.info(this, "collectData  complexElements " + complexElements);
		manager.clearOperations();
		Logging.info(this, "collectData  complexElements " + complexElements);
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
		for (ComplexGroup group : complexElements) {
			contentPane.remove(group.topLabel);
			contentPane.remove(group.removeButton);
			contentPane.remove(group.connectionType);
			contentPane.remove(group.negateButton);
			contentPane.remove(group.openParenthesis);
			contentPane.remove(group.closeParenthesis);
			for (SimpleGroup simple : group.groupList) {
				removeGroup(simple);
			}
		}
		saveNameField.setText("");
		saveDescriptionField.setText("");
		complexElements.clear();
		complexElements.add(createHostGroup());
		complexElements.add(createSoftwareGroup());
		buildParentheses();
		contentPane.revalidate();
		contentPane.repaint();
	}

	/*
	 * This is used after a saved search was loaded. It displays the loaded search
	 * in the interface.
	 */
	private void loadFromManager() {
		for (ComplexGroup group : complexElements) {
			contentPane.remove(group.topLabel);
			contentPane.remove(group.removeButton);
			contentPane.remove(group.connectionType);
			contentPane.remove(group.negateButton);
			contentPane.remove(group.openParenthesis);
			contentPane.remove(group.closeParenthesis);
			for (SimpleGroup simple : group.groupList) {
				removeGroup(simple);
			}
		}
		complexElements.clear();

		List<OperationWithStatus> topList;
		topList = manager.operationsAsList(null);
		Logging.debug(this, "load: size: " + topList.size());
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
			Logging.debug(this, "subload: " + subList.size());
			setGroupValues(element, subList);
		}
		if (!complexElements.isEmpty()) {
			complexElements.getLast().connectionType.setVisible(false);
		}
		buildParentheses();
		contentPane.revalidate();
		contentPane.repaint();
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
		Logging.debug(this, "simple, open, closed: " + simpleGroup.element.getClassName() + ows.isParenthesisOpen()
				+ ows.isParenthesisClosed());
	}

	private static void setComponentData(JComponent component, SelectData data) {
		if (data == null || data.getData() == null) {
			return;
		}

		if (component instanceof TextInputField textInputField) {
			textInputField.setText(data.getData().toString());
		} else if (component instanceof SpinnerWithExtension spinnerWithExtension
				&& data.getType() == SelectData.DataType.BIG_INTEGER_TYPE) {
			spinnerWithExtension.setValue((Long) data.getData());
		} else if (component instanceof JSpinner jSpinner && data.getType() == SelectData.DataType.INTEGER_TYPE) {
			jSpinner.setValue(data.getData());
		} else {
			Logging.warning("component ", component, " with datatype ", data.getType(), " not treated");
		}
	}

	private static void setConnectionTypes(JCheckBox andOr, AbstractButton not, ConnectionStatus status) {
		switch (status) {
		case AND:
			andOr.setSelected(true);
			break;
		case OR:
			andOr.setSelected(false);
			break;
		case AND_NOT:
			andOr.setSelected(true);
			not.setSelected(true);
			break;
		case OR_NOT:
			andOr.setSelected(false);
			not.setSelected(true);
			break;
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
		private GroupLayout.ParallelGroup vRow;
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

			Logging.info(this, "removing group of type " + group.type);

			if (group.removeButton == e.getSource()) {
				contentPane.remove(group.topLabel);
				contentPane.remove(group.removeButton);
				contentPane.remove(group.connectionType);
				contentPane.remove(group.negateButton);
				contentPane.remove(group.openParenthesis);
				contentPane.remove(group.closeParenthesis);
				for (SimpleGroup simple : group.groupList) {
					removeGroup(simple);
				}
				contentPane.revalidate();
				contentPane.repaint();
				complexIterator.remove();
				buildParentheses();
				break;
			}
		}

		if (!complexElements.isEmpty()) {
			complexElements.getLast().connectionType.setVisible(false);
		}
	}

	private void addElement() {
		if (!complexElements.isEmpty()) {
			complexElements.getLast().connectionType.setVisible(true);
		}

		int index = newElementBox.getSelectedIndex();

		if (index == 0) {
			return;
		} else if (index == 1) {
			complexElements.add(createHostGroup());
		} else if (index == 2) {
			complexElements.add(createSoftwareGroup());
		} else if (index == 3) {
			complexElements.add(createSwAuditGroup());
		} else {
			complexElements.add(createHardwareGroup(newElementBox.getSelectedItem().toString()));
		}

		contentPane.revalidate();
		contentPane.repaint();
		newElementBox.setSelectedIndex(0);
		complexElements.getLast().connectionType.setVisible(false);
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

		if (sourceGroup.dataComponent != null) {
			contentPane.remove(sourceGroup.dataComponent);
			sourceGroup.dataComponent = null;
		}

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

		contentPane.revalidate();
		contentPane.repaint();
	}

	private void save() {
		String text = saveNameField.getText();
		if (text.isEmpty()) {
			JOptionPane.showMessageDialog(saveButton, Configed.getResourceValue("ClientSelectionDialog.emptyName"),
					Configed.getResourceValue("ClientSelectionDialog.emptyNameTitle"), JOptionPane.OK_OPTION);
			toFront();
		} else if (searchNamePattern.matcher(text).matches()) {
			collectData();
			manager.saveSearch(text, saveDescriptionField.getText());
			savedSearchesDialog.reloadAction();
		} else {
			JOptionPane.showMessageDialog(saveButton, "wrong name", "error", JOptionPane.OK_OPTION);
			toFront();
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
