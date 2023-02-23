package de.uib.configed.gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.clientselection.AbstractSelectElement;
import de.uib.configed.clientselection.AbstractSelectGroupOperation;
import de.uib.configed.clientselection.AbstractSelectOperation;
import de.uib.configed.clientselection.SelectData;
import de.uib.configed.clientselection.SelectionManager;
import de.uib.configed.clientselection.elements.DescriptionElement;
import de.uib.configed.clientselection.elements.GroupElement;
import de.uib.configed.clientselection.elements.GroupWithSubgroupsElement;
import de.uib.configed.clientselection.elements.IPElement;
import de.uib.configed.clientselection.elements.NameElement;
import de.uib.configed.clientselection.elements.PropertyIdElement;
import de.uib.configed.clientselection.elements.PropertyValueElement;
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
import de.uib.configed.clientselection.operations.PropertiesOperation;
import de.uib.configed.clientselection.operations.SoftwareOperation;
import de.uib.configed.clientselection.operations.SoftwareWithPropertiesOperation;
import de.uib.configed.clientselection.operations.SwAuditOperation;
import de.uib.configed.type.SavedSearch;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.selectionpanel.JTableSelectionPanel;
import de.uib.utilities.swing.LowerCaseTextField;
import de.uib.utilities.swing.TextInputField;

/**
 * This dialog shows a number of options you can use to select specific clients.
 */
public class ClientSelectionDialog extends FGeneralDialog {

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
	private IconAsButton buttonReload;
	private IconAsButton buttonRestart;
	private JTextField saveNameField;
	private JTextField saveDescriptionField;
	private JButton saveButton;

	private LinkedList<ComplexGroup> complexElements;

	private SelectionManager manager;
	private JTableSelectionPanel selectionPanel;
	private SavedSearchesDialog savedSearchesDialog;

	private final boolean withMySQL;

	private ConfigedMain main;

	public ClientSelectionDialog(ConfigedMain main, JTableSelectionPanel selectionPanel,
			SavedSearchesDialog savedSearchesDialog) {
		super(null,
				Configed.getResourceValue("ClientSelectionDialog.title")/* "Select clients" */ + " (" + Globals.APPNAME
						+ ")",
				false,
				new String[] { Configed.getResourceValue("ClientSelectionDialog.buttonClose"),
						Configed.getResourceValue("ClientSelectionDialog.buttonReset"),
						Configed.getResourceValue("ClientSelectionDialog.buttonSet") },
				FRAME_WIDTH, FRAME_HEIGHT);

		AbstractPersistenceController controller = PersistenceControllerFactory.getPersistenceController();
		this.withMySQL = controller.isWithMySQL()
				&& controller.getGlobalBooleanConfigValue(AbstractPersistenceController.KEY_SEARCH_BY_SQL,
						AbstractPersistenceController.DEFAULTVALUE_SEARCH_BY_SQL);

		Logging.info(this, "use mysql " + withMySQL);

		this.main = main;
		this.selectionPanel = selectionPanel;
		this.savedSearchesDialog = savedSearchesDialog;
		super.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		manager = new SelectionManager("OpsiData");
		complexElements = new LinkedList<>();
		init();
		super.pack();

		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Logging.info(this, "ClientSelectionDialog resized");
				// move it up and down for fixing the combobox popup vanishing
				java.awt.Component c = e.getComponent();
				java.awt.Point point = c.getLocation();
				java.awt.Point savePoint = new java.awt.Point(point);
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

	public void setReloadRequested() {
		manager.getBackend().setReloadRequested();
	}

	public void refreshGroups() {
		for (ComplexGroup complex : complexElements) {
			if (complex.type == GroupType.HOST_GROUP) {
				for (SimpleGroup group : complex.groupList) {
					if (group.element instanceof GroupElement) {
						JComboBox<String> box = (JComboBox<String>) group.dataComponent;
						box.removeAllItems();
						for (String data : group.element.getEnumData()) {
							box.addItem(data);
						}
					}
				}
			}
		}
	}

	public void loadSearch(String name) {
		Logging.info(this, "loadSearch " + name);
		try {
			manager.loadSearch(name);
			loadFromManager();
			SavedSearch search = manager.getSavedSearches().get(name);
			saveNameField.setText(search.getName());
			saveDescriptionField.setText(search.getDescription());
		} catch (Exception exc) {
			Logging.error("Could not load search!", exc);
		}
	}

	@Override
	public void doAction3() {
		Logging.info(this, "doAction3");
		List<String> clients = new ArrayList<>();

		collectData();

		// because of potential memory problems we switch to
		// client view
		main.setVisualViewIndex(ConfigedMain.VIEW_CLIENTS);

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
		additionalPane.setBackground(Globals.BACKGROUND_COLOR_7);

		saveNameField = new LowerCaseTextField();
		saveNameField.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.searchnameFormat"));

		saveDescriptionField = new JTextField();

		JLabel saveNameLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.inquiryName"));
		saveNameLabel.setFont(Globals.defaultFont);
		JLabel saveDescriptionLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.inquiryDescription"));
		saveDescriptionLabel.setFont(Globals.defaultFont);

		saveButton = new JButton(Configed.getResourceValue("ClientSelectionDialog.inquirySave"));
		saveButton.setFont(Globals.defaultFont);
		saveButton.addActionListener(new SaveButtonListener());

		buttonReload = new IconAsButton(Configed.getResourceValue("ClientSelectionDialog.buttonReload"),
				"images/reload16.png", "images/reload16_over.png", "images/reload16.png",
				"images/reload16_disabled.png");

		buttonReload.setBackground(Globals.BACKGROUND_COLOR_3);

		final ClientSelectionDialog dialog = this;
		buttonReload.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.info(this, "actionPerformed");
				buttonReload.setEnabled(false);
				buttonRestart.setEnabled(false);
				Cursor saveCursor = dialog.getCursor();
				dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
				SwingUtilities.invokeLater(() -> {
					setReloadRequested();

					buttonReload.setEnabled(true);
					buttonRestart.setEnabled(true);
					dialog.setCursor(saveCursor);

				});
			}
		});

		buttonRestart = new IconAsButton(Configed.getResourceValue("ClientSelectionDialog.buttonRestart"),
				"images/reload16_red.png", "images/reload16_over.png", "images/reload16.png",
				"images/reload16_disabled.png");

		buttonRestart.setBackground(Globals.BACKGROUND_COLOR_3);

		buttonRestart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.info(this, "actionPerformed");
				buttonRestart.setEnabled(false);
				buttonReload.setEnabled(false);
				dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));

				SwingUtilities.invokeLater(() -> {
					setReloadRequested();

					main.callNewClientSelectionDialog();
					// we lose all components of this dialog, there is nothing to reset
				});
			}
		});

		GroupLayout.SequentialGroup saveHGroup = additionalLayout.createSequentialGroup();
		saveHGroup.addGap(Globals.HGAP_SIZE);
		saveHGroup.addComponent(saveNameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE);
		saveHGroup.addGap(Globals.HGAP_SIZE / 2);
		saveHGroup.addComponent(saveNameField, 40, 100, 200);
		saveHGroup.addGap(Globals.HGAP_SIZE);
		saveHGroup.addComponent(saveDescriptionLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
				GroupLayout.PREFERRED_SIZE);
		saveHGroup.addGap(Globals.HGAP_SIZE / 2);
		saveHGroup.addComponent(saveDescriptionField, 40, 200, Short.MAX_VALUE);
		saveHGroup.addGap(Globals.HGAP_SIZE);
		saveHGroup.addComponent(saveButton, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH);
		saveHGroup.addGap(Globals.HGAP_SIZE);
		saveHGroup.addComponent(buttonReload, 20, 20, 20);
		saveHGroup.addGap(Globals.HGAP_SIZE / 2);
		saveHGroup.addComponent(buttonRestart, 20, 20, 20);

		saveHGroup.addGap(Globals.HGAP_SIZE);
		additionalLayout.setHorizontalGroup(saveHGroup);

		GroupLayout.ParallelGroup saveVGroup = additionalLayout.createParallelGroup(GroupLayout.Alignment.CENTER);
		saveVGroup.addComponent(saveNameLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);
		saveVGroup.addComponent(saveNameField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);
		saveVGroup.addComponent(saveDescriptionLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);
		saveVGroup.addComponent(saveDescriptionField, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);
		saveVGroup.addComponent(saveButton, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);
		saveVGroup.addComponent(buttonReload, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);
		saveVGroup.addComponent(buttonRestart, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);

		additionalLayout.setVerticalGroup(saveVGroup);

		additionalPane.setVisible(true);
	}

	private void init() {
		contentPane = new JPanel();
		contentPane.setBackground(Globals.BACKGROUND_COLOR_7);
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
		hMainGroup.addGroup(layout.createSequentialGroup().addGroup(hGroupParenthesisOpen).addGap(3)
				.addGroup(hGroupNegate).addGap(5).addGroup(hGroupElements).addGap(5).addGroup(hGroupOperations)
				.addGap(5).addGroup(hGroupData).addGap(3).addGroup(hGroupParenthesisClose).addGap(5)
				.addGroup(hGroupConnections).addGap(5).addGroup(hGroupRemoveBtn));
		layout.setHorizontalGroup(hMainGroup);

		// columns headline
		Font font = Globals.defaultFontStandardBold;
		JLabel negationLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.negateColumn"));
		negationLabel.setFont(font);
		JLabel nameLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.nameColumn"));
		nameLabel.setFont(font);
		// JLabel operationLabel = new JLabel(

		JLabel dataLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.dataColumn"));
		dataLabel.setFont(font);
		JLabel connectionLabel = new JLabel(Configed.getResourceValue("ClientSelectionDialog.connectionColumn"));
		connectionLabel.setFont(font);
		// JLabel removeLabel = new JLabel(

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
		newElementBox.setFont(Globals.defaultFont);

		newElementBox.setMaximumRowCount(Globals.COMBOBOX_ROW_COUNT);
		newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.hostName"));
		newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.softwareName"));

		// Add properties-Boxes if mysql available
		if (withMySQL) {
			newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.softwarepropertiesonlyName"));
			newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.softwarewithpropertiesName"));
		}

		newElementBox.addItem(Configed.getResourceValue("ClientSelectionDialog.swauditName"));

		// hardware
		List<String> hardwareList = new LinkedList<>(manager.getLocalizedHardwareList().keySet());
		Collections.sort(hardwareList);
		for (String hardware : hardwareList) {
			newElementBox.addItem(hardware);
		}

		newElementBox.addActionListener(new AddElementListener());

		vMainGroup.addComponent(newElementBox, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT);
		hMainGroup.addComponent(newElementBox, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH);
		contentPane.add(newElementBox);

		complexElements.add(createHostGroup());
		complexElements.add(createSoftwareGroup());
		complexElements.getLast().connectionType.setVisible(false);
		scrollpane.getViewport().add(contentPane);
	}

	/* This creates one line with element, operation, data, ... */
	private SimpleGroup createSimpleGroup(AbstractSelectElement element) {
		SimpleGroup result = new SimpleGroup();
		result.element = element;
		AbstractSelectOperation[] operations = element.supportedOperations().toArray(new AbstractSelectOperation[0]);
		if (operations.length == 0) {
			Logging.warning("Elements without any operations: " + result);
			return result;
		}

		result.negateButton = new IconAsButton("" /* configed.getResourceValue("ClientSelectionDialog.not") */,
				"images/boolean_not_disabled.png", "images/boolean_not_over.png", "images/boolean_not.png", null);
		result.negateButton.setActivated(false);
		result.negateButton.setMaximumSize(new Dimension(result.negateButton.getMaximumSize().width,
				result.negateButton.getPreferredSize().height));
		result.negateButton.addActionListener(new NotButtonListener());
		result.connectionType = new AndOrSelectButtonByIcon();
		result.connectionType.addActionListener(new AndOrButtonListener());
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
		result.openParenthesis = new IconAsButton(
				"" /* configed.getResourceValue("ClientSelectionDialog.parenthesisOpen") */,
				"images/parenthesis_open_disabled.png", "images/parenthesis_open_over.png",
				"images/parenthesis_open.png", null);
		result.openParenthesis.setActivated(true);
		result.openParenthesis.setVisible(false);
		result.closeParenthesis = new IconAsButton(
				"" /* configed.getResourceValue("ClientSelectionDialog.parenthesisClose") */,
				"images/parenthesis_close_disabled.png", "images/parenthesis_close_over.png",
				"images/parenthesis_close.png", null);
		result.closeParenthesis.setActivated(true);
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

		contentPane.add(result.negateButton);
		contentPane.add(result.connectionType);
		contentPane.add(result.elementLabel);
		contentPane.add(result.operationComponent);
		contentPane.add(result.dataComponent);
		contentPane.add(result.openParenthesis);
		contentPane.add(result.closeParenthesis);

		if (operations.length > 1) {
			((JComboBox) result.operationComponent).addActionListener(new SelectOperationListener());
			addDataComponent(result, ((JComboBox) result.operationComponent).getSelectedIndex());
		} else if (operations.length == 1) {
			addDataComponent(result, 0);
		}

		return result;
	}

	private ComplexGroup createHostGroup() {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.HOST_GROUP;
		result.topLabel.setText(Configed.getResourceValue("ClientSelectionDialog.hostGroup") + ":");
		result.topLabel.setIcon(Globals.createImageIcon("images/client_small.png",
				Configed.getResourceValue("ClientSelectionDialog.client")));
		result.topLabel.setFont(Globals.defaultFontStandardBold);
		result.groupList
				.add(createSimpleGroup(new GroupElement(manager.getBackend().getGroups().toArray(new String[0]))));
		result.groupList.add(createSimpleGroup(
				new GroupWithSubgroupsElement(manager.getBackend().getGroups().toArray(new String[0]))));
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
		result.topLabel.setIcon(Globals.createImageIcon("images/package.png",
				Configed.getResourceValue("ClientSelectionDialog.softwareGroup")));
		result.topLabel.setFont(Globals.defaultFontStandardBold);

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

	private ComplexGroup createPropertiesGroup() {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.PROPERTIES_GROUP;
		result.topLabel.setText(Configed.getResourceValue("ClientSelectionDialog.softwarepropertiesonlyGroup"));
		result.topLabel.setIcon(Globals.createImageIcon("images/package.png",
				Configed.getResourceValue("ClientSelectionDialog.softwareGroup")));
		result.topLabel.setFont(Globals.defaultFontStandardBold);

		result.groupList.add(createSimpleGroup(manager.getNewSoftwareNameElement()));
		result.groupList.getLast().connectionType.setVisible(false);

		SimpleGroup propertyIdGroup = createSimpleGroup(new PropertyIdElement());
		propertyIdGroup.elementLabel.setForeground(Globals.ClientSelectionDialog_ELEMENT_FOREGROUND);
		propertyIdGroup.negateButton.setVisible(false);
		result.groupList.add(propertyIdGroup);
		result.groupList.getLast().connectionType.setVisible(false);

		SimpleGroup propertyValueGroup = createSimpleGroup(new PropertyValueElement());
		propertyValueGroup.elementLabel.setForeground(Globals.ClientSelectionDialog_ELEMENT_FOREGROUND);
		propertyValueGroup.negateButton.setVisible(false);
		result.groupList.add(propertyValueGroup);
		result.groupList.getLast().connectionType.setVisible(false);

		createComplexBottom(result);

		return result;
	}

	// Group with properties
	private ComplexGroup createSoftwareWithPropertiesGroup() {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.SOFTWARE_WITH_PROPERTIES_GROUP;
		result.topLabel.setText(Configed.getResourceValue("ClientSelectionDialog.softwarewithpropertiesGroup"));
		result.topLabel.setIcon(Globals.createImageIcon("images/package.png",
				Configed.getResourceValue("ClientSelectionDialog.softwareGroup")));
		result.topLabel.setFont(Globals.defaultFontStandardBold);

		result.groupList.add(createSimpleGroup(manager.getNewSoftwareNameElement()));
		result.groupList.getLast().connectionType.setVisible(false);

		result.groupList.add(createSimpleGroup(new SoftwareInstallationStatusElement()));

		result.groupList.add(createSimpleGroup(new SoftwareActionResultElement()));
		result.groupList.add(createSimpleGroup(new SoftwareRequestElement()));
		result.groupList.add(createSimpleGroup(new SoftwareActionProgressElement()));
		result.groupList.add(createSimpleGroup(new SoftwareLastActionElement()));
		result.groupList.add(createSimpleGroup(new SoftwareVersionElement()));
		result.groupList.add(createSimpleGroup(new SoftwarePackageVersionElement()));
		result.groupList.add(createSimpleGroup(new SoftwareModificationTimeElement()));

		result.groupList.getLast().connectionType.setVisible(false);

		SimpleGroup propertyIdGroup = createSimpleGroup(new PropertyIdElement());
		propertyIdGroup.elementLabel.setForeground(Globals.ClientSelectionDialog_ELEMENT_FOREGROUND);
		propertyIdGroup.negateButton.setVisible(false);
		result.groupList.add(propertyIdGroup);
		result.groupList.getLast().connectionType.setVisible(false);

		SimpleGroup propertyValueGroup = createSimpleGroup(new PropertyValueElement());
		propertyValueGroup.elementLabel.setForeground(Globals.ClientSelectionDialog_ELEMENT_FOREGROUND);
		propertyValueGroup.negateButton.setVisible(false);
		result.groupList.add(propertyValueGroup);
		result.groupList.getLast().connectionType.setVisible(false);

		createComplexBottom(result);

		return result;
	}

	private ComplexGroup createHardwareGroup(String hardware) {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.HARDWARE_GROUP;
		result.topLabel.setText(hardware);
		result.topLabel.setIcon(Globals.createImageIcon("images/hwaudit.png",
				Configed.getResourceValue("ClientSelectionDialog.hardwareName")));
		result.topLabel.setFont(Globals.defaultFontStandardBold);

		List<AbstractSelectElement> elements = manager.getLocalizedHardwareList().get(hardware);
		for (AbstractSelectElement element : elements) {
			result.groupList.add(createSimpleGroup(element));
		}
		result.groupList.getFirst().connectionType.setVisible(false);

		result.groupList.getLast().connectionType.setVisible(false);
		createComplexBottom(result);
		return result;
	}

	private ComplexGroup createSwAuditGroup() {
		ComplexGroup result = createComplexGroup();
		result.type = GroupType.SW_AUDIT_GROUP;
		result.topLabel.setText(Configed.getResourceValue("ClientSelectionDialog.swAuditGroup") + ":");
		result.topLabel.setIcon(Globals.createImageIcon("images/swaudit.png",
				Configed.getResourceValue("ClientSelectionDialog.swauditName")));
		result.topLabel.setFont(Globals.defaultFontStandardBold);

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

		result.removeButton = new IconAsButton("", "images/user-trash.png", "images/user-trash_over.png",
				"images/user-trash.png", "images/user-trash_disabled.png");
		result.removeButton.setMaximumSize(new Dimension(result.removeButton.getPreferredSize().width,
				result.removeButton.getPreferredSize().height));
		result.removeButton.addActionListener(new RemoveButtonListener());
		result.negateButton = new IconAsButton("", "images/boolean_not_disabled.png", "images/boolean_not_over.png",
				"images/boolean_not.png", null);
		result.negateButton.setActivated(false);
		result.negateButton.setMaximumSize(new Dimension(result.negateButton.getMaximumSize().width,
				result.negateButton.getPreferredSize().height));
		result.negateButton.addActionListener(new NotButtonListener());
		result.topLabel = new JLabel();
		result.topLabel.setMaximumSize(
				new Dimension(result.topLabel.getMaximumSize().width, result.removeButton.getPreferredSize().height));
		result.topLabel.setFont(Globals.defaultFontStandardBold);
		result.openParenthesis = new IconAsButton("", "images/parenthesis_open_disabled.png",
				"images/parenthesis_open_over.png", "images/parenthesis_open.png", null);
		result.openParenthesis.setActivated(false);
		result.openParenthesis.addActionListener(new ParenthesisListener());

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
		contentPane.add(result.topLabel);

		result.groupList = new LinkedList<>();
		return result;
	}

	/* This creates the bottom line of a complex group */
	private void createComplexBottom(ComplexGroup group) {
		group.closeParenthesis = new IconAsButton("", "images/parenthesis_close_disabled.png",
				"images/parenthesis_close_over.png", "images/parenthesis_close.png", null);
		group.closeParenthesis.setActivated(false);
		group.closeParenthesis.addActionListener(new ParenthesisListener());
		group.connectionType = new AndOrSelectButtonByIcon();
		group.connectionType.addActionListener(new AndOrButtonListener());
		group.connectionType.setMaximumSize(new Dimension(group.connectionType.getMaximumSize().width,
				group.connectionType.getPreferredSize().height));
		GroupLayout.ParallelGroup vRow = layout.createParallelGroup();
		vRow.addComponent(group.connectionType, GroupLayout.Alignment.CENTER);
		vRow.addComponent(group.closeParenthesis, GroupLayout.Alignment.CENTER, 20, 20, 20);
		vGroup.addGroup(vRow);
		hGroupConnections.addComponent(group.connectionType, 100, 100, 100);
		hGroupParenthesisClose.addComponent(group.closeParenthesis, 20, 20, 20);
		contentPane.add(group.connectionType);
	}

	/* Gets the selected operation and adds the given data to it. */
	private AbstractSelectOperation getOperation(SimpleGroup group) {
		int operationIndex;
		if (group.operationComponent instanceof JComboBox) {
			operationIndex = ((JComboBox) group.operationComponent).getSelectedIndex();
		} else {
			operationIndex = 0;
		}

		AbstractSelectOperation operation = group.element.supportedOperations().get(operationIndex);

		Object data = null;
		String text = null;
		SelectData.DataType type = operation.getDataType();
		switch (type) {
		// Do the same for all three cases
		case DOUBLE_TYPE:
		case TEXT_TYPE:
		case DATE_TYPE:
			text = ((TextInputField) (group.dataComponent)).getText();
			if (text.isEmpty()) {
				return null;
			}
			data = text;
			break;
		case INTEGER_TYPE:
			Integer value = (Integer) ((JSpinner) group.dataComponent).getValue();
			if (value == 0) {
				return null;
			}
			data = value;
			break;
		case BIG_INTEGER_TYPE:
			Long value2 = ((SpinnerWithExt) group.dataComponent).getValue();
			if (value2 == 0) {
				return null;
			}
			data = value2;
			break;
		case ENUM_TYPE:

			String textEnum = ((TextInputField) group.dataComponent).getText();
			if (textEnum.isEmpty()) {
				return null;
			}
			data = textEnum;
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
	private SelectionManager.OperationWithStatus getInformation(SimpleGroup group) {
		SelectionManager.OperationWithStatus info = new SelectionManager.OperationWithStatus();
		info.operation = null;
		info.parenthesisOpen = group.openParenthesis.isVisible();
		info.parenthesisClose = group.closeParenthesis.isVisible();
		boolean andSelected = group.connectionType.isAndSelected();
		Logging.debug(this, group.element.getPath() + ": AND selected: " + andSelected);
		boolean notSelected = group.negateButton.isActivated();
		info.status = getStatus(andSelected, notSelected);
		return info;
	}

	private SelectionManager.OperationWithStatus getInformation(ComplexGroup group) {
		SelectionManager.OperationWithStatus info = new SelectionManager.OperationWithStatus();
		info.operation = null;
		info.parenthesisOpen = group.openParenthesis.isActivated();
		info.parenthesisClose = group.closeParenthesis.isActivated();
		boolean andSelected = group.connectionType.isAndSelected();
		boolean notSelected = group.negateButton.isActivated();
		info.status = getStatus(andSelected, notSelected);
		return info;
	}

	private SelectionManager.ConnectionStatus getStatus(boolean andSelected, boolean notSelected) {
		SelectionManager.ConnectionStatus conStatus;
		if (andSelected) {
			if (notSelected) {
				conStatus = SelectionManager.ConnectionStatus.AND_NOT;
			} else {
				conStatus = SelectionManager.ConnectionStatus.AND;
			}
		} else {
			if (notSelected) {
				conStatus = SelectionManager.ConnectionStatus.OR_NOT;
			} else {
				conStatus = SelectionManager.ConnectionStatus.OR;
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

	private void showParenthesesForGroup(Deque<SimpleGroup> groups) {
		boolean inOr = false;
		for (SimpleGroup group : groups) {
			group.openParenthesis.setVisible(false);
			group.closeParenthesis.setVisible(false);
			if (getOperation(group) == null) {
				continue;
			}

			if (group.connectionType.isAndSelected() && inOr) {
				inOr = false;
				group.closeParenthesis.setVisible(true);
			}

			if (group.connectionType.isOrSelected() && !inOr) {
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
			if (complex.openParenthesis.isActivated() && complex.closeParenthesis.isActivated()) {
				complex.openParenthesis.setActivated(false);
				complex.closeParenthesis.setActivated(false);
			} else if (complex.openParenthesis.isActivated()) {
				stack.push(complex);
			} else if (complex.closeParenthesis.isActivated()) {
				if (!stack.isEmpty()) {
					stack.pop();
				} else {
					complex.closeParenthesis.setActivated(false);
				}
			}
		}

		for (ComplexGroup stackElement : stack) {
			stackElement.openParenthesis.setActivated(false);
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
			TextInputField fieldText = new TextInputField("", sourceGroup.element.getEnumData());
			fieldText.setEditable(true);
			fieldText.setSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
			fieldText.setToolTipText(
					/* "Use * as wildcard" */Configed.getResourceValue("ClientSelectionDialog.textInputToolTip"));
			fieldText.addValueChangeListener(new de.uib.utilities.observer.swing.AbstractValueChangeListener() {
				@Override
				protected void actOnChange() {
					buildParentheses();
				}
			});
			sourceGroup.dataComponent = fieldText;
			break;
		case DOUBLE_TYPE:

			TextInputField fieldDouble = new TextInputField("");
			fieldDouble.setSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
			fieldDouble.setToolTipText(
					/* "Use * as wildcard" */Configed.getResourceValue("ClientSelectionDialog.textInputToolTip"));
			fieldDouble.addValueChangeListener(new de.uib.utilities.observer.swing.AbstractValueChangeListener() {
				@Override
				protected void actOnChange() {
					buildParentheses();
				}
			});
			sourceGroup.dataComponent = fieldDouble;
			break;
		case ENUM_TYPE:

			TextInputField box = new TextInputField("", sourceGroup.element.getEnumData());
			box.setEditable(true);
			box.setToolTipText(Configed.getResourceValue("ClientSelectionDialog.textInputToolTip"));
			box.addValueChangeListener(new de.uib.utilities.observer.swing.AbstractValueChangeListener() {
				@Override
				protected void actOnChange() {
					buildParentheses();
				}
			});
			sourceGroup.dataComponent = box;
			break;
		case DATE_TYPE:
			TextInputField fieldDate = new TextInputField(null);
			fieldDate.setSize(new Dimension(Globals.BUTTON_WIDTH, Globals.LINE_HEIGHT));
			fieldDate.setToolTipText("yyyy-mm-dd");
			fieldDate.addValueChangeListener(new de.uib.utilities.observer.swing.AbstractValueChangeListener() {
				@Override
				protected void actOnChange() {
					buildParentheses();
				}
			});
			sourceGroup.dataComponent = fieldDate;
			break;

		case INTEGER_TYPE:
			JSpinner spinner = new JSpinner();
			spinner.addChangeListener(new de.uib.utilities.observer.swing.AbstractValueChangeListener() {
				@Override
				protected void actOnChange() {
					buildParentheses();
				}
			});
			sourceGroup.dataComponent = spinner;
			break;
		case BIG_INTEGER_TYPE:
			SpinnerWithExt swx = new SpinnerWithExt();
			swx.addChangeListener(new de.uib.utilities.observer.swing.AbstractValueChangeListener() {
				@Override
				protected void actOnChange() {
					buildParentheses();
				}
			});

			sourceGroup.dataComponent = swx;
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
			SelectionManager.OperationWithStatus groupStatus;
			groupStatus = getInformation(complex);

			List<SelectionManager.OperationWithStatus> childList = new LinkedList<>();

			for (SimpleGroup group : complex.groupList) {
				AbstractSelectOperation op = getOperation(group);
				if (op != null) {
					SelectionManager.OperationWithStatus ows = getInformation(group);
					ows.operation = op;
					childList.add(ows);
				}
			}

			if (!childList.isEmpty()) {
				switch (complex.type) {
				case SOFTWARE_GROUP:
					manager.addGroupOperation("Software", groupStatus, childList);
					break;

				case PROPERTIES_GROUP:
					manager.addGroupOperation("Properties", groupStatus, childList);
					break;

				case SOFTWARE_WITH_PROPERTIES_GROUP:
					manager.addGroupOperation("SoftwareWithProperties", groupStatus, childList);
					break;

				case SW_AUDIT_GROUP:
					manager.addGroupOperation("SwAudit", groupStatus, childList);
					break;
				case HARDWARE_GROUP:
					manager.addGroupOperation("Hardware", groupStatus, childList);
					break;
				case HOST_GROUP:
					manager.addGroupOperation("Host", groupStatus, childList);
					break;
				}
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

		List<SelectionManager.OperationWithStatus> topList;
		topList = manager.operationsAsList(null);
		Logging.debug(this, "load: size: " + topList.size());
		for (int i = 0; i < topList.size(); i++) {
			SelectionManager.OperationWithStatus ows = topList.get(i);
			AbstractSelectOperation op = ows.operation;
			if (op == null) {
				reset();
				return;
			}
			ComplexGroup element;
			if (op instanceof HostOperation) {
				element = createHostGroup();
			} else if (op instanceof SoftwareOperation) {
				element = createSoftwareGroup();
			} else if (op instanceof PropertiesOperation) {
				element = createPropertiesGroup();
			} else if (op instanceof SoftwareWithPropertiesOperation) {
				element = createSoftwareWithPropertiesGroup();
			} else if (op instanceof SwAuditOperation) {
				element = createSwAuditGroup();
			} else if (op instanceof HardwareOperation) {
				element = createHardwareGroup(
						getNonGroupOperation((HardwareOperation) op).getElement().getLocalizedPathArray()[0]);
			} else {
				Logging.error("Not a group operation: " + op.getClassName());
				reset();
				return;
			}

			complexElements.add(element);
			setConnectionTypes(element.connectionType, element.negateButton, ows.status);
			List<SelectionManager.OperationWithStatus> subList;
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

	private AbstractSelectOperation getNonGroupOperation(AbstractSelectGroupOperation operation) {
		AbstractSelectOperation child = operation.getChildOperations().get(0);
		while (child instanceof AbstractSelectGroupOperation) {
			child = ((AbstractSelectGroupOperation) child).getChildOperations().get(0);
		}
		return child;
	}

	private void setGroupValues(ComplexGroup group, List<SelectionManager.OperationWithStatus> owsList) {
		for (int i = 0; i < owsList.size(); i++) {
			for (SimpleGroup simple : group.groupList) {
				SelectionManager.OperationWithStatus ows = owsList.get(i);
				AbstractSelectOperation op = ows.operation;
				if (op.getElement().getPath().equals(simple.element.getPath())) {
					if (op.getElement().supportedOperations().size() > 1) {
						((JComboBox) simple.operationComponent).setSelectedItem(op.getOperationString());
					}
					setComponentData(simple.dataComponent, op.getSelectData());
					setConnectionTypes(simple.connectionType, simple.negateButton, ows.status);
					Logging.debug(this, "simple, open, closed: " + simple.element.getClassName() + ows.parenthesisOpen
							+ ows.parenthesisClose);

					break;
				}
			}
		}
	}

	private void setComponentData(JComponent component, SelectData data) {
		if (data == null || data.getData() == null) {
			return;
		}

		if (component instanceof TextInputField) {
			((TextInputField) component).setText(data.getData().toString());
		} else if (component instanceof SpinnerWithExt && data.getType() == SelectData.DataType.BIG_INTEGER_TYPE) {
			((SpinnerWithExt) component).setValue((Long) data.getData());
		} else if (component instanceof JSpinner && data.getType() == SelectData.DataType.INTEGER_TYPE) {
			((JSpinner) component).setValue(data.getData());
		}
	}

	private void setConnectionTypes(AndOrSelectButtonByIcon andOr, IconAsButton not,
			SelectionManager.ConnectionStatus status) {
		switch (status) {
		case AND:
			andOr.selectAnd();
			break;
		case OR:
			andOr.selectOr();
			break;
		case AND_NOT:
			andOr.selectAnd();
			not.setActivated(true);
			break;
		case OR_NOT:
			andOr.selectOr();
			not.setActivated(true);
		}
	}

	private class SimpleGroup {
		private AbstractSelectElement element;
		private IconAsButton negateButton;
		private AndOrSelectButtonByIcon connectionType;
		private JLabel elementLabel;

		// may be JLabel or JComboBox
		private JComponent operationComponent;
		private JComponent dataComponent;
		private GroupLayout.ParallelGroup vRow;
		private IconAsButton openParenthesis;
		private IconAsButton closeParenthesis;
	}

	private enum GroupType {
		HOST_GROUP, SOFTWARE_GROUP, PROPERTIES_GROUP, SOFTWARE_WITH_PROPERTIES_GROUP, SW_AUDIT_GROUP, HARDWARE_GROUP
	}

	private class ComplexGroup {
		private GroupType type;
		private IconAsButton removeButton;
		private IconAsButton negateButton;
		private JLabel topLabel = null;
		private AndOrSelectButtonByIcon connectionType;
		private Deque<SimpleGroup> groupList;
		private IconAsButton openParenthesis;
		private IconAsButton closeParenthesis;

		@Override
		public String toString() {
			return "ComplexGroup type " + type;
		}
	}

	private class RemoveButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
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
	}

	private class AddElementListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
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
			} else if (index == 3 && withMySQL) {
				complexElements.add(createPropertiesGroup());
			} else if (index == 4 && withMySQL) {
				complexElements.add(createSoftwareWithPropertiesGroup());
			} else if (index == 5) {
				complexElements.add(createSwAuditGroup());
			} else {
				complexElements.add(createHardwareGroup(newElementBox.getSelectedItem().toString()));
			}

			contentPane.revalidate();
			contentPane.repaint();
			newElementBox.setSelectedIndex(0);
			complexElements.getLast().connectionType.setVisible(false);

		}
	}

	private class SelectOperationListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			JComponent source = null;
			SimpleGroup sourceGroup = null;
			for (ComplexGroup group : complexElements) {
				for (SimpleGroup simple : group.groupList) {
					if (simple.operationComponent == e.getSource()) {
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
				index = ((JComboBox) source).getSelectedIndex();
			} else if (source instanceof JLabel) {
				index = 0;
			}
			addDataComponent(sourceGroup, index);

			buildParentheses();

			contentPane.revalidate();
			contentPane.repaint();
		}
	}

	private class NotButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			if (!(event.getSource() instanceof IconAsButton)) {
				return;
			}
			IconAsButton button = (IconAsButton) event.getSource();
			button.setActivated(!button.isActivated());
			Logging.debug(this, "Negate button is activated: " + button.isActivated());
		}
	}

	private class ParenthesisListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			if (!(event.getSource() instanceof IconAsButton)) {
				return;
			}
			IconAsButton button = (IconAsButton) event.getSource();
			button.setActivated(!button.isActivated());
		}
	}

	private class AndOrButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent event) {
			buildParentheses();
		}
	}

	/*
	 * A spinner for big numbers, with a metric prefix (kilo, mega, ...) selection.
	 */
	private class SpinnerWithExt extends JPanel {
		private JSpinner spinner;
		private JComboBox<String> box;

		public SpinnerWithExt() {
			spinner = new JSpinner(
					new SpinnerNumberModel((Number) Long.valueOf(0), Long.MIN_VALUE, Long.MAX_VALUE, Long.valueOf(1)));
			spinner.setMinimumSize(new Dimension(0, 0));
			box = new JComboBox<>(new String[] { "", "k", "M", "G", "T" });
			box.setMinimumSize(new Dimension(50, 0));
			GroupLayout spinnerLayout = new GroupLayout(this);
			spinnerLayout.setVerticalGroup(spinnerLayout.createParallelGroup().addComponent(spinner).addComponent(box));
			spinnerLayout
					.setHorizontalGroup(spinnerLayout.createSequentialGroup().addComponent(spinner).addComponent(box));

			super.setLayout(spinnerLayout);
			super.add(spinner);
			super.add(box);
		}

		public long getValue() {
			long value = (Long) spinner.getValue();
			for (int i = 0; i < box.getSelectedIndex(); i++) {
				value *= 1024l;
			}
			return value;
		}

		public void setValue(long val) {
			spinner.setValue(val);
			box.setSelectedIndex(0);
		}

		public void addChangeListener(ChangeListener listener) {
			spinner.addChangeListener(listener);
		}
	}

	private class SaveButtonListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String text = saveNameField.getText();
			if (text.isEmpty()) {
				JOptionPane.showMessageDialog(saveButton, Configed.getResourceValue("ClientSelectionDialog.emptyName"),
						Configed.getResourceValue("ClientSelectionDialog.emptyNameTitle") + " (" + Globals.APPNAME
								+ ")",
						JOptionPane.OK_OPTION);
				toFront();

				return;
			} else if (!text.matches("[\\p{javaLowerCase}\\d_-]*")) {
				JOptionPane.showMessageDialog(saveButton, "wrong name", "error", JOptionPane.OK_OPTION);
				toFront();

				return;
			}

			collectData();
			manager.saveSearch(text, saveDescriptionField.getText());
			savedSearchesDialog.reloadAction();
		}
	}
}