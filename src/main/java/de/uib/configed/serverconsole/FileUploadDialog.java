/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.serverconsole;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.nio.file.Paths;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.configed.serverconsole.command.CommandExecutor;
import de.uib.configed.serverconsole.command.MultiCommandTemplate;
import de.uib.configed.serverconsole.command.SingleCommandCurl;
import de.uib.configed.serverconsole.command.SingleCommandFileUpload;
import de.uib.configed.serverconsole.command.SingleCommandOpsiSetRights;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utils.Utils;
import de.uib.utils.logging.Logging;

public class FileUploadDialog extends FGeneralDialog {
	private static final String WGET_DEFAULT_URL_TEXT = Configed
			.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url");

	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JRadioButton jRadioButtonFromServer;
	private JRadioButton jRadioButtonLocal;

	protected JFileChooser jFileChooserLocal;
	private JTextField jTextFieldLocalPath;

	private JButton jButtonFileChooser;

	protected JLabel jLabelSetRights;
	protected JLabel jLabelmodulesFrom;
	private JLabel jLabelURL;

	protected JCheckBox jComboBoxSetRights;
	private JTextField jTextFieldURL;

	protected GroupLayout.Group horizontalParallelGroup;
	protected GroupLayout.Group verticalParallelGroup;
	protected GroupLayout inputPanelLayout;

	private CurlAuthenticationPanel wgetAuthPanel;
	protected SingleCommandFileUpload command;

	private ConfigedMain configedMain;

	public FileUploadDialog(String title, SingleCommandFileUpload com, ConfigedMain configedMain) {
		this(title, com, Globals.DIALOG_FRAME_DEFAULT_WIDTH, Globals.DIALOG_FRAME_DEFAULT_HEIGHT + 100, configedMain);
	}

	public FileUploadDialog(String title, SingleCommandFileUpload com, int width, int height,
			ConfigedMain configedMain) {
		super(null, title, false);
		this.configedMain = configedMain;
		this.command = com;
		if (this.command == null) {
			command = new SingleCommandFileUpload();
		}

		init();
		initGUI();

		super.setSize(width, height);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		Logging.info(this.getClass(), "SSHFileUploadDialog build");
	}

	private void init() {
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));

		ButtonGroup group = new ButtonGroup();
		jRadioButtonFromServer = new JRadioButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.rb_from_server"));
		group.add(jRadioButtonFromServer);
		addListener(jRadioButtonFromServer);
		jRadioButtonLocal = new JRadioButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.rb_local"), true);
		group.add(jRadioButtonLocal);
		addListener(jRadioButtonLocal);

		jLabelURL = new JLabel();
		wgetAuthPanel = new CurlAuthenticationPanel();
		wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH + 90, Globals.BUTTON_HEIGHT);
		wgetAuthPanel.isOpen(true);
		wgetAuthPanel.close();

		jLabelURL.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_url"));

		jLabelSetRights = new JLabel();
		jLabelSetRights.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_setRights"));
		jLabelmodulesFrom = new JLabel();
		jLabelmodulesFrom
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_modules_from"));

		jTextFieldURL = new JTextField();
		jTextFieldURL.setText(WGET_DEFAULT_URL_TEXT);
		jTextFieldURL.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (jTextFieldURL.getText().equals(WGET_DEFAULT_URL_TEXT)) {
					jTextFieldURL.setSelectionStart(0);
					jTextFieldURL.setSelectionEnd(jTextFieldURL.getText().length());
				}
			}
		});
		jTextFieldLocalPath = new JTextField();
		jTextFieldLocalPath.setEditable(false);

		jComboBoxSetRights = new JCheckBox("", true);
		jComboBoxSetRights.setSelected(true);

		jFileChooserLocal = new JFileChooser();
		jFileChooserLocal.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jFileChooserLocal.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));

		jFileChooserLocal.setDialogType(JFileChooser.OPEN_DIALOG);
		jFileChooserLocal.setDialogTitle(
				Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.filechooser.title"));

		jButtonFileChooser = new JButton(Utils.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setSelectedIcon(Utils.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setPreferredSize(Globals.SMALL_BUTTON_DIMENSION);
		jButtonFileChooser.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.filechooser.tooltip"));
		jButtonFileChooser.addActionListener(actionEvent -> chooseFileDialog());

		JButton jButtonExecute = new JButton(Configed.getResourceValue("SSHConnection.buttonExec"));

		if (!PersistenceControllerFactory.getPersistenceController().getUserRolesConfigDataService()
				.isGlobalReadOnly()) {
			jButtonExecute.addActionListener(actionEvent -> doAction2());
		}

		JButton jButtonClose = new JButton(Configed.getResourceValue("buttonClose"));

		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonExecute);

		enableComponents(jRadioButtonFromServer.isSelected());
	}

	private void chooseFileDialog() {
		int returnVal = jFileChooserLocal.showOpenDialog(inputPanel);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String modulesPath = jFileChooserLocal.getSelectedFile().getPath();
			String modulesName = jFileChooserLocal.getSelectedFile().getName();
			jTextFieldLocalPath.setText(modulesPath);

			command.setSourceFileName(modulesName);
			command.setSourcePath(modulesPath);
			jTextFieldLocalPath.setCaretPosition(modulesPath.length());
		} else {
			jTextFieldLocalPath.setText("");
		}
	}

	private void enableComponents(boolean isSelected) {
		((JCheckBox) wgetAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH)).setEnabled(isSelected);
		jTextFieldURL.setEnabled(isSelected);
		jTextFieldLocalPath.setEnabled(!isSelected);
		jButtonFileChooser.setEnabled(!isSelected);
	}

	protected void initGUIAdditional() {
		horizontalParallelGroup = inputPanelLayout.createSequentialGroup()
				.addGroup(inputPanelLayout.createParallelGroup()
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jLabelmodulesFrom,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE).addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(jComboBoxSetRights, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH));
		verticalParallelGroup = inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER);
	}

	private void initGUI() {
		inputPanelLayout = new GroupLayout(inputPanel);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		inputPanel.setLayout(inputPanelLayout);

		initGUIAdditional();
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
						.addGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE * 2)
								.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jRadioButtonLocal,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
								.addGap(Globals.GAP_SIZE * 2)
								.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jRadioButtonFromServer,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
								.addGroup(inputPanelLayout.createSequentialGroup().addGroup(horizontalParallelGroup)))
						.addGap(Globals.GAP_SIZE))
				.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addGroup(inputPanelLayout
						.createParallelGroup().addGap(Globals.GAP_SIZE * 2)
						.addGroup(GroupLayout.Alignment.LEADING,
								inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
										.addComponent(jTextFieldLocalPath, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(jButtonFileChooser, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE))
						.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
								.addGroup(inputPanelLayout.createParallelGroup()
										.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addComponent(wgetAuthPanel.get(CurlAuthenticationPanel.LBLNEEDAUTH),
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.GAP_SIZE)
								.addGroup(inputPanelLayout.createParallelGroup()
										.addComponent(jTextFieldURL, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE)
										.addComponent(wgetAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH),
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)))
						.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(
								wgetAuthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)))
						.addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE));
		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jLabelmodulesFrom, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonLocal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGap(Globals.GAP_SIZE * 3)
						.addComponent(jTextFieldLocalPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonFileChooser, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE * 3))
				.addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonFromServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addGroup(
						inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addGap(Globals.GAP_SIZE * 3)
								.addComponent(jTextFieldURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.GAP_SIZE * 3))
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addGap(Globals.GAP_SIZE * 3)
						.addComponent(wgetAuthPanel.get(CurlAuthenticationPanel.LBLNEEDAUTH),
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(wgetAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH), GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE * 3))
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)

						.addGap(Globals.GAP_SIZE).addComponent(wgetAuthPanel, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE * 2)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE).addGroup(verticalParallelGroup));
	}

	private void cancel() {
		super.doAction1();
	}

	@Override
	public void doAction2() {
		Logging.info(this, "doAction2 upload ");
		if (jRadioButtonLocal.isSelected() && jTextFieldLocalPath.getText().isEmpty()) {
			Logging.warning(this, "Please select local file.");
			return;
		}

		if (jRadioButtonFromServer.isSelected()
				&& (jTextFieldURL.getText().isEmpty() || jTextFieldURL.getText().equals(WGET_DEFAULT_URL_TEXT))) {
			Logging.warning(this, "Please enter url to file.");
			return;
		}

		uploadFile();
	}

	private void uploadFile() {
		String modulesServerPath = doAction1AdditionalSetPath();

		MultiCommandTemplate fullcommand = new MultiCommandTemplate();
		fullcommand.setMainName("FileUpload");
		if (jRadioButtonFromServer.isSelected()) {
			SingleCommandCurl curlCommand = new SingleCommandCurl();
			curlCommand = doAction1AdditionalSetWget(curlCommand, modulesServerPath);

			curlCommand.setUrl(jTextFieldURL.getText());

			if (((JCheckBox) wgetAuthPanel.get(CurlAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
				curlCommand.setAuthentication("--insecure -u " + wgetAuthPanel.getUser() + ":" + wgetAuthPanel.getPw());
			} else {
				curlCommand.setAuthentication("");
			}

			fullcommand.addCommand(curlCommand);
		} else {
			fullcommand.addCommand(command);
		}

		if (jComboBoxSetRights.isSelected()) {
			fullcommand.addCommand(new SingleCommandOpsiSetRights(""));
		}

		CommandExecutor execute = new CommandExecutor(configedMain, fullcommand);
		execute.execute();
	}

	protected SingleCommandCurl doAction1AdditionalSetWget(SingleCommandCurl c, String path) {
		c.setDir(Paths.get(path).getParent().toString());
		return c;
	}

	protected String doAction1AdditionalSetPath() {
		String modulesServerPath = command.getFullTargetPath();
		command.setTargetFileName(jFileChooserLocal.getSelectedFile().getName());
		return modulesServerPath;
	}

	private void addListener(Component comp) {
		if (comp instanceof JRadioButton) {
			((JRadioButton) comp)
					.addActionListener(actionEvent -> enableComponents(jRadioButtonFromServer.isSelected()));
		}
	}
}
