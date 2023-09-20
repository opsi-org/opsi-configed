/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.gui.IconButton;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedDocument;

/**
 * @inheritDoc Graphical user interface for editing sshcommands.
 **/
public final class SSHCommandControlDialog extends FGeneralDialog {

	private static final int FRAME_WIDTH = 850;
	private static final int FRAME_HEIGHT = 600;

	/** This instance / Design Patter: singelton **/
	private static SSHCommandControlDialog instance;

	/** button panel instance **/
	private JPanel buttonPanel = new JPanel();
	/** command control panel instance **/
	private JPanel controlPanel = new JPanel();
	/** command - commands control panel instance **/
	private JPanel commandPanel = new JPanel();
	private JPanel commandlistPanel = new JPanel();
	private JPanel parameterPanel;

	/** command control panel Layout instance **/
	private GroupLayout controlPanelLayout;
	/** command - commands control panel Layout instance **/
	private GroupLayout commandPanelLayout;

	private GroupLayout commandlistPanelLayout;

	/** Save Button instance **/
	private JButton buttonSave;

	/** JLabel menu text instance **/
	private JLabel labelMenuText = new JLabel();
	/** JLabel parent menu text instance **/
	private JLabel labelParentMenuText = new JLabel();
	/** JLabel tolbl_parentMenuTextoltip text instance **/
	private JLabel labelTooltipText = new JLabel();
	/** JLabel priority instance **/
	private JLabel labelPriority = new JLabel();
	/** JLabel need sudo instance **/
	private JLabel labelNeedSudo = new JLabel();
	/** JLabel commands instance **/
	private JLabel labelCommands = new JLabel();

	/** JComboBox menu text instance **/
	private JComboBox<String> jComboBoxMenuText;
	/** IconButton delete menu text instance **/
	private IconButton buttonDelete;

	/** JComboBox parent menu text instance **/
	private JComboBox<String> jComboBoxParentMenuText;
	/** JTextField tooltip text instance **/
	private JTextField jTextFieldTooltipText = new JTextField();
	/** JTextField priority instance **/
	private JTextField jTextFieldPriority = new JTextField();
	/** JCheckBox need Sudo instance **/
	private JCheckBox jComboBoxNeedSudo = new JCheckBox("");
	/** JTextPane commands instance **/
	private JTextPane jTextPaneCommands = new JTextPane();

	/** MainFrame instance **/
	private JFrame main;
	private ConfigedMain configedMain;
	/** SSHCommandFactory instance **/
	private final SSHCommandFactory factory = SSHCommandFactory.getInstance();

	private JButton buttonTestCommand;

	/**
	 * Graphical user interface for editing sshcommands.
	 * 
	 * @param owner Usually the MainFrame
	 **/

	private SSHCommandControlDialog(ConfigedMain configedMain, JFrame owner) {
		super(null, Configed.getResourceValue("MainFrame.jMenuSSHCommandControl"));
		Logging.info(this.getClass(), "SSHCommandControlDialog instance " + instance + " main " + main);
		main = owner;
		this.configedMain = configedMain;
		parameterPanel = new SSHCommandControlParameterMethodsPanel(this, Globals.GAP_SIZE * 3, Globals.GAP_SIZE * 3,
				Globals.GAP_SIZE * 2, 0);
		init();
		pack();
		this.setSize(FRAME_WIDTH, FRAME_HEIGHT);
		this.setLocationRelativeTo(owner);
		this.setVisible(true);
	}

	/**
	 * Method allows only one instance Design: Singelton-Pattern
	 * 
	 * @param configedMain the main controller
	 * @param fr           the parent Frame usually the MainFrame
	 * @return SSHCommandControlDialog instance
	 **/
	public static SSHCommandControlDialog getInstance(ConfigedMain configedMain) {
		if (instance == null) {
			instance = new SSHCommandControlDialog(configedMain, ConfigedMain.getMainFrame());
		} else {
			instance.setLocationRelativeTo(ConfigedMain.getMainFrame());
		}

		instance.setVisible(true);
		return instance;
	}

	private void setCenterLayout() {
		Logging.debug(this, "setCenterLayout ");
		commandPanelLayout.setAutoCreateGaps(true);

		commandPanelLayout.setHorizontalGroup(
				commandPanelLayout.createParallelGroup().addComponent(commandlistPanel).addComponent(parameterPanel));
		commandPanelLayout.setVerticalGroup(
				commandPanelLayout.createSequentialGroup().addComponent(commandlistPanel).addComponent(parameterPanel));

		parameterPanel.setVisible(true);
	}

	/** Init components **/
	private void init() {
		Logging.debug(this, "init setting up components ");

		if (!Main.THEMES) {
			controlPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			commandPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			commandlistPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		commandPanelLayout = new GroupLayout(commandPanel);
		controlPanelLayout = new GroupLayout(controlPanel);
		commandlistPanelLayout = new GroupLayout(commandlistPanel);

		getContentPane().add(controlPanel, BorderLayout.NORTH);
		getContentPane().add(commandPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		commandlistPanel.setPreferredSize(new Dimension(FRAME_WIDTH - 30, 150));
		commandlistPanel.setSize(new Dimension(FRAME_WIDTH - 30, 150));
		controlPanel.setLayout(controlPanelLayout);
		commandPanel.setLayout(commandPanelLayout);
		commandlistPanel.setLayout(commandlistPanelLayout);

		controlPanel.setBorder(BorderFactory.createTitledBorder(""));
		commandPanel.setBorder(BorderFactory.createTitledBorder(""));
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		setCenterLayout();

		Dimension dimensionJTextField = new Dimension(Globals.FIRST_LABEL_WIDTH - Globals.GRAPHIC_BUTTON_WIDTH,
				Globals.BUTTON_HEIGHT);
		Dimension dimensionJTextFieldLong = new Dimension(Globals.FIRST_LABEL_WIDTH, Globals.BUTTON_HEIGHT);
		Dimension dimensionButton = new Dimension(Globals.GRAPHIC_BUTTON_WIDTH, Globals.BUTTON_HEIGHT);

		labelParentMenuText = new JLabel();
		jComboBoxParentMenuText = new JComboBox<>();
		labelMenuText = new JLabel();
		jComboBoxMenuText = new JComboBox<>();
		labelTooltipText = new JLabel();
		labelPriority = new JLabel();

		jTextFieldPriority = new JTextField(new CheckedDocument(/* allowedChars */
				new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' }, 5),
				String.valueOf(SSHCommandFactory.POSITION_DEFAULT), 1);
		labelNeedSudo = new JLabel();
		labelCommands = new JLabel();
		jTextPaneCommands = new JTextPane();

		buttonTestCommand = new IconButton(Configed.getResourceValue("SSHConnection.CommandControl.btnTestCommand"),
				"images/executing_command_red_22.png", "images/executing_command_red_22.png",
				"images/executing_command_red_22.png", true);
		buttonDelete = new IconButton(Configed.getResourceValue("SSHConnection.CommandControl.rm_menuText.tooltip"),
				"images/list-remove.png", "images/list-remove.png", "images/list-remove_disabled.png", true);
		buttonSave = new IconButton(Configed.getResourceValue("MainFrame.iconButtonSaveConfiguration"),
				"images/apply_over.gif", " ", "images/apply_disabled.gif", false);
		JButton buttonClose = new IconButton(Configed.getResourceValue("MainFrame.iconButtonCancelChanges"),
				"images/cancel-32.png", "images/cancel_over-32.png", " ", true);

		labelMenuText.setText(Configed.getResourceValue("SSHConnection.CommandControl.menuText"));
		labelParentMenuText.setText(Configed.getResourceValue("SSHConnection.CommandControl.parentMenuText"));
		labelTooltipText.setText(Configed.getResourceValue("SSHConnection.CommandControl.tooltipText"));
		labelPriority.setText(Configed.getResourceValue("SSHConnection.CommandControl.priority"));
		labelNeedSudo.setText(Configed.getResourceValue("SSHConnection.CommandControl.needSudo"));
		labelCommands.setText(Configed.getResourceValue("SSHConnection.CommandControl.commands"));

		jComboBoxParentMenuText.addItem(SSHCommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS); // parentNull
		jComboBoxMenuText.addItem(SSHCommandFactory.MENU_NEW);

		jComboBoxMenuText.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.menuText.tooltip"));
		jTextFieldTooltipText
				.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.tooltipText.tooltip"));

		jTextFieldPriority.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.priority.tooltip"));
		jComboBoxNeedSudo.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.needSudo.tooltip"));
		labelCommands.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.commands.tooltip"));
		jTextPaneCommands.setToolTipText(Configed.getResourceValue("SSHConnection.CommandControl.commands.tooltip"));

		labelMenuText.setPreferredSize(dimensionJTextField);
		labelParentMenuText.setPreferredSize(dimensionJTextField);
		jComboBoxParentMenuText.setPreferredSize(dimensionJTextField);
		jComboBoxMenuText.setPreferredSize(dimensionJTextField);
		labelTooltipText.setPreferredSize(dimensionJTextField);
		jTextFieldTooltipText.setPreferredSize(dimensionJTextField);
		labelPriority.setPreferredSize(dimensionJTextField);
		jTextFieldPriority.setPreferredSize(dimensionButton);
		jComboBoxNeedSudo.setSize(dimensionButton);
		buttonDelete.setSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));
		labelNeedSudo.setPreferredSize(dimensionJTextField);
		jComboBoxNeedSudo.setPreferredSize(dimensionButton);
		jTextPaneCommands.setPreferredSize(dimensionJTextFieldLong);
		// btn_changeHelpPanelStatus.setPreferredSize(new

		buttonTestCommand.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));
		buttonDelete.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT));

		jComboBoxMenuText.setEditable(true);
		jComboBoxParentMenuText.setEditable(true);
		jTextFieldPriority.setColumns(4);

		final JTextComponent editor = (JTextComponent) jComboBoxMenuText.getEditor().getEditorComponent();
		jComboBoxMenuText.addItemListener((ItemEvent itemEvent) -> {
			if (editor.getText().trim().equals(SSHCommandFactory.MENU_NEW)) {
				editor.setSelectionStart(0);
				editor.setSelectionEnd(editor.getText().length());
			}
			updateSelectedCommand(editor.getText());
			checkAllTexts();
		});
		jComboBoxParentMenuText.addItemListener(itemEvent -> checkAllTexts());

		jTextFieldTooltipText.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		jTextFieldPriority.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		jComboBoxNeedSudo.addItemListener(itemEvent -> checkAllTexts());
		jTextPaneCommands.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				checkAllTexts();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Plain text components do not fire these events
			}
		});

		showPanel();
		if (!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly()) {
			buttonTestCommand.addActionListener(actionEvent -> doActionTestCommand());
		}

		final SSHCommandControlDialog caller = this;
		if (!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly()) {
			((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonTest().addActionListener(
					actionEvent -> ((SSHCommandControlParameterMethodsPanel) parameterPanel).doActionTestParam(caller));
		}

		if (!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly()) {
			((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonAdd()
					.addActionListener(actionEvent -> ((SSHCommandControlParameterMethodsPanel) parameterPanel)
							.doActionParamAdd(jTextPaneCommands));
		}

		updateLists(true);
		updateSelectedCommand();

		if (!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly()) {
			buttonDelete.addActionListener((ActionEvent actionEvent) -> {
				String menu = (String) jComboBoxMenuText.getSelectedItem();
				factory.deleteSSHCommandByMenu(menu);

				jComboBoxMenuText.setSelectedItem(SSHCommandFactory.MENU_NEW);
				updateLists(true, SSHCommandFactory.MENU_NEW);
				updateSelectedCommand(SSHCommandFactory.MENU_NEW);
				factory.reloadServerMenu();
			});
		}

		if (!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly()) {
			buttonSave.addActionListener(actionEvent -> doAction2());
			buttonClose.addActionListener(actionEvent -> doAction1());
		}

		buttonPanel.add(buttonClose);
		buttonPanel.add(buttonSave);

		initLayout();

		setComponentsEnabledRO(
				!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly());
	}

	/**
	 * Set components for read_only mode
	 * 
	 * @param value False if mode is readonly - setEditable/setEnabled to false
	 **/
	private void setComponentsEnabledRO(boolean value) {
		Logging.info(this, "setComponentsEnabledRO value " + value);
		jComboBoxNeedSudo.setEnabled(value);

		jComboBoxParentMenuText.setEnabled(value);
		jComboBoxParentMenuText.setEditable(value);

		jTextFieldTooltipText.setEnabled(value);
		jTextFieldTooltipText.setEditable(value);

		jTextFieldPriority.setEnabled(value);
		jTextFieldPriority.setEditable(value);

		jTextPaneCommands.setEnabled(value);
		jTextPaneCommands.setEditable(value);

		buttonDelete.setEnabled(value);
		buttonTestCommand.setEnabled(value);
	}

	/** Init grouplayout **/
	private void initLayout() {
		Logging.debug(this, "initLayout ");
		commandlistPanelLayout
				.setHorizontalGroup(commandlistPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
						.addGroup(commandlistPanelLayout.createParallelGroup()
								.addGroup(commandlistPanelLayout.createSequentialGroup()
										.addComponent(labelCommands, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE).addComponent(buttonTestCommand,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)

								).addGap(Globals.MIN_GAP_SIZE).addComponent(jTextPaneCommands,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(Globals.GAP_SIZE * 3));
		commandlistPanelLayout.setVerticalGroup(commandlistPanelLayout.createSequentialGroup()
				.addGap(Globals.GAP_SIZE * 2)
				.addGroup(commandlistPanelLayout.createParallelGroup()
						.addGroup(commandlistPanelLayout.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
								.addComponent(labelCommands, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.MIN_GAP_SIZE).addComponent(buttonTestCommand, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE)

				).addGap(Globals.MIN_GAP_SIZE).addComponent(jTextPaneCommands, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.GAP_SIZE * 1));

		controlPanelLayout.setHorizontalGroup(controlPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(labelTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelNeedSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGroup(controlPanelLayout.createParallelGroup()
						.addGroup(controlPanelLayout.createSequentialGroup()
								.addComponent(jComboBoxMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addGroup(controlPanelLayout.createParallelGroup()
										.addComponent(jTextFieldPriority, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(jComboBoxNeedSudo, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
						.addComponent(jTextFieldTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addComponent(jComboBoxParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE * 3));
		controlPanelLayout.setVerticalGroup(controlPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(labelMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(buttonDelete, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))

				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(jComboBoxParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelParentMenuText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(jTextFieldTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(labelTooltipText, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(labelPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldPriority, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(controlPanelLayout.createParallelGroup()
						.addComponent(labelNeedSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxNeedSudo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.MIN_GAP_SIZE).addGap(Globals.GAP_SIZE * 2));
	}

	/**
	 * Calls
	 * {@link de.uib.configed.gui.ssh.SSHCommandControlDialog.updateLists(requestRefresh,
	 * null) }
	 **/
	private void updateLists(boolean requestRefresh) {
		updateLists(requestRefresh, null);
	}

	/**
	 * Retrieve all needed lists from factory. If refresh is requested retrieve
	 * the newest lists and setSelectedItem to selectedCommand
	 * 
	 * @param requestRefresh  True for request new refresh
	 * @param selectedCommand MenuText for setSelectedItem
	 **/
	private void updateLists(boolean requestRefresh, String selectedCommand) {
		Logging.info(this, "updateLists requestRefresh " + requestRefresh + " selectedCommand " + selectedCommand);
		if (requestRefresh) {
			jComboBoxMenuText.removeAllItems();
			jComboBoxParentMenuText.removeAllItems();
			factory.retrieveSSHCommandListRequestRefresh();
		}

		List<String> commandMenus = factory.getSSHCommandMenuNames();
		List<String> commandParents = factory.getSSHCommandMenuParents();
		for (String menu : commandMenus) {
			if (((DefaultComboBoxModel<String>) jComboBoxMenuText.getModel()).getIndexOf(menu) == -1) {
				jComboBoxMenuText.addItem(menu);
			}
		}

		for (String parent : commandParents) {
			if (((DefaultComboBoxModel<String>) jComboBoxParentMenuText.getModel()).getIndexOf(parent) == -1) {
				jComboBoxParentMenuText.addItem(parent);
			}
		}

		if (selectedCommand == null || selectedCommand.trim().isEmpty()) {
			selectedCommand = SSHCommandFactory.MENU_NEW;
		}

		jComboBoxMenuText.setSelectedItem(selectedCommand);
	}

	/** Calls {@link updateSelectedCommand(null)} **/
	private void updateSelectedCommand() {
		updateSelectedCommand(null);
	}

	/**
	 * update all dependent components for selected menu
	 * 
	 * @param menuText The selected menu text
	 **/
	private void updateSelectedCommand(String menuText) {
		Logging.info(this, "updateSelectedCommand menuText " + menuText);
		if (menuText != null && menuText.equals(SSHCommandFactory.MENU_NEW)) {
			menuText = null;
		}

		if (menuText != null && menuText.length() > 0) {
			SSHCommandTemplate thiscommand = factory.getSSHCommandByMenu(menuText);
			if (thiscommand != null) {
				Logging.debug(this, "updateSelectedCommand menu " + thiscommand.getMenuText() + " parent "
						+ thiscommand.getParentMenuText());
				StringBuilder combuf = new StringBuilder("");
				for (SSHCommand c : thiscommand.getCommands()) {
					String rawCommand = c.getCommandRaw();
					if (rawCommand != null && !rawCommand.isEmpty()) {
						combuf.append(rawCommand).append("\n");
					}
				}

				updateComponents(thiscommand.getParentMenuText(), thiscommand.getToolTipText(),
						thiscommand.getPriority(), thiscommand.needSudo(), combuf.toString());
			}
		} else {
			updateComponents(SSHCommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS/* parentNull */, "",
					SSHCommandFactory.POSITION_DEFAULT, false, "");
		}
	}

	/**
	 * update all dependent components to given values
	 * 
	 * @param parent  The parent menu name
	 * @param tooltip The tooltip text
	 * @param prio    The priority
	 * @param ns      The needSudo state
	 * @param coms    The commands
	 **/
	private void updateComponents(String parent, String tooltip, int prio, boolean ns, String coms) {
		if (parent == null || parent.trim().isEmpty()) {

			parent = SSHCommandFactory.PARENT_DEFAULT_FOR_OWN_COMMANDS;
		}
		jComboBoxParentMenuText.setSelectedItem(parent);
		jTextFieldTooltipText.setText(tooltip);
		jTextFieldPriority.setText(String.valueOf(prio));
		jComboBoxNeedSudo.setSelected(ns);
		jTextPaneCommands.setText(coms);
	}

	/* This method is called when button 2 (save) is pressed */
	@Override
	public void doAction2() {
		Logging.info(this, "doAction2 savecommand ");
		SSHCommandTemplate command = getCommandNow();
		if (command == null) {
			return;
		}
		Logging.debug(this, "doAction2 savecommand " + command.toString());

		String menuText = (String) jComboBoxMenuText.getSelectedItem();

		if (factory.saveSSHCommand(command)) {
			updateLists(true, menuText);
			updateSelectedCommand(menuText);
			factory.reloadServerMenu();
		} else {
			JOptionPane.showInternalMessageDialog(this,
					Configed.getResourceValue("SSHConnection.CommandControl.couldnotsave.title"),
					Configed.getResourceValue("SSHConnection.CommandControl.couldnotsave"),
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Generates a command id for futured features
	 * 
	 * @param name The command menu text
	 **/
	private static String generateId(String name) {
		return name.replace(" ", "_").toLowerCase(Locale.ROOT).trim();
	}

	private void doActionTestCommand() {
		Logging.info(this, "doActionTestCommand testCommand building command ...");
		SSHCommandTemplate command = getCommandNow(true /* testing */);
		if (command == null) {
			return;
		}
		if (command.getMenuText() == null) {
			command.setMenuText(SSHCommandFactory.MENU_NEW);
		}
		Logging.debug(this, "doActionTestCommand buildCommand " + command.toString());
		Logging.debug(this, "doActionTestCommand buildCommand commandlist " + command.commandlistToString());

		new Thread() {
			@Override
			public void run() {
				new SSHConnectExec(configedMain, command);
			}
		}.start();

	}

	private void checkAllTexts() {
		if (!PersistenceControllerFactory.getPersistenceController().getConfigDataService().isGlobalReadOnly()) {
			if (jComboBoxMenuText.getSelectedItem() != null
					&& !((String) jComboBoxMenuText.getSelectedItem()).trim().equals(SSHCommandFactory.MENU_NEW)) {
				Logging.info(this, "checkAllTexts menuText " + jComboBoxMenuText.getSelectedItem());
				SSHCommandTemplate tempCommand = getCommandNow();
				Logging.debug(this, "checkAllTexts command " + tempCommand);
				if (tempCommand == null) {
					return;
				}
				boolean isNotSaved = !factory.isSSHCommandEqualSavedCommand(tempCommand);
				Logging.debug(this, "checkAllTexts factory.isSSHCommandEqualSavedCommand(tmp_com) "
						+ factory.isSSHCommandEqualSavedCommand(tempCommand));
				Logging.debug(this, "checkAllTexts isNotSaved " + isNotSaved);
				buttonSave.setEnabled(isNotSaved);
			} else {
				buttonSave.setEnabled(false);
			}
		}
	}

	private SSHCommandTemplate getCommandNow() {
		return getCommandNow(false);
	}

	private SSHCommandTemplate getCommandNow(boolean testing) {
		Logging.debug(this, "getCommandNow ");
		String menuText = (String) jComboBoxMenuText.getSelectedItem();
		if (!testing && menuText.trim().equals(SSHCommandFactory.MENU_NEW)) {
			return null;
		}
		String parent = (String) jComboBoxParentMenuText.getSelectedItem();
		int prio = 0;
		try {
			prio = Integer.valueOf(jTextFieldPriority.getText());
		} catch (NumberFormatException e) {
			Logging.warning("Cannot get value from priority field Exception: ", e);
		}
		List<String> coms = new LinkedList<>();
		for (String c : jTextPaneCommands.getText().split("\n")) {
			if (!(c == null || c.trim().isEmpty())) {
				coms.add(c);
			}
		}

		SSHCommandTemplate tempCommand = SSHCommandFactory.buildSSHCommand(
				generateId((String) jComboBoxMenuText.getSelectedItem()), parent, menuText,
				jTextFieldTooltipText.getText(), prio, jComboBoxNeedSudo.isSelected(), coms);
		Logging.debug(this, "getCommandNow command: " + tempCommand);

		return tempCommand;
	}

	private void showPanel() {
		Logging.info(this, "showPanel helpPanelStatus always true");

		setCenterLayout();
		commandPanel.setSize(commandPanel.getWidth(), commandPanel.getHeight() + parameterPanel.getHeight());
		this.setSize(this.getWidth(), this.getHeight() + parameterPanel.getHeight());

		repaint();
		revalidate();
	}
}
