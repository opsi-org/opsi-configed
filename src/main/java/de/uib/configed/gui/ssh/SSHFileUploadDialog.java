/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

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
import de.uib.opsicommand.sshcommand.CommandOpsiSetRights;
import de.uib.opsicommand.sshcommand.CommandSFTPUpload;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class SSHFileUploadDialog extends FGeneralDialog {
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
	protected JLabel jLabelOverwriteExisting;

	protected JCheckBox jComboBoxSetRights;
	protected JCheckBox jCheckBoxOverwriteExisting;
	private JTextField jTextFieldURL;

	protected GroupLayout.Group horizontalParallelGroup;
	protected GroupLayout.Group verticalParallelGroup;
	protected GroupLayout inputPanelLayout;

	private SSHWgetAuthenticationPanel wgetAuthPanel;
	protected CommandSFTPUpload command;

	public SSHFileUploadDialog(String title, CommandSFTPUpload com) {
		this(title, com, Globals.DIALOG_FRAME_DEFAULT_WIDTH, Globals.DIALOG_FRAME_DEFAULT_HEIGHT + 100);
	}

	public SSHFileUploadDialog(String title, CommandSFTPUpload com, int width, int height) {
		super(null, title, false);
		this.command = com;
		if (this.command == null) {
			command = new CommandSFTPUpload();
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

		jLabelURL = new JLabel(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_url"));
		wgetAuthPanel = new SSHWgetAuthenticationPanel();
		wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH + 90, Globals.BUTTON_HEIGHT);
		wgetAuthPanel.isOpen = true;
		wgetAuthPanel.close();

		jLabelOverwriteExisting = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_overwriteExisting"));

		jLabelSetRights = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_setRights"));
		jLabelmodulesFrom = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_modules_from"));

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
		jCheckBoxOverwriteExisting = new JCheckBox("", true);

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

		initAdditional();
	}

	private void chooseFileDialog() {
		int returnVal = jFileChooserLocal.showOpenDialog(inputPanel);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String pathModules = jFileChooserLocal.getSelectedFile().getPath();
			jTextFieldLocalPath.setText(pathModules);

			command.setFullSourcePath(pathModules);
			jTextFieldLocalPath.setCaretPosition(pathModules.length());
		} else {
			jTextFieldLocalPath.setText("");
		}
	}

	protected void initAdditional() {
		/* To be implemented in subclass(es) */}

	private void enableComponents(boolean isSelected) {
		((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).setEnabled(isSelected);
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
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelOverwriteExisting, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE).addGroup(
						inputPanelLayout.createParallelGroup()
								.addComponent(jComboBoxSetRights, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH)
								.addComponent(jCheckBoxOverwriteExisting, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH));
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
										.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE))
								.addGap(Globals.GAP_SIZE)
								.addGroup(inputPanelLayout.createParallelGroup()
										.addComponent(jTextFieldURL, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE)
										.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
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
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
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
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelOverwriteExisting, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jCheckBoxOverwriteExisting, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
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

		SSHCommandTemplate fullcommand = new SSHCommandTemplate();
		fullcommand.setMainName("FileUpload");
		if (jRadioButtonFromServer.isSelected()) {
			CommandWget wget = new CommandWget();
			wget = doAction1AdditionalSetWget(wget, modulesServerPath);

			wget.setUrl(jTextFieldURL.getText());

			if (((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
				wget.setAuthentication(" --no-check-certificate --user=" + wgetAuthPanel.getUser() + " --password="
						+ wgetAuthPanel.getPw() + " ");
			} else {
				wget.setAuthentication(" ");
			}

			fullcommand.addCommand(wget);
		} else {
			command.setOverwriteMode(jCheckBoxOverwriteExisting.isSelected());
			fullcommand.addCommand(command);
		}

		if (jComboBoxSetRights.isSelected()) {
			fullcommand.addCommand(new CommandOpsiSetRights(""));
		}

		new SSHConnectExec(fullcommand);
	}

	protected CommandWget doAction1AdditionalSetWget(CommandWget c, String path) {
		c.setFileName(path);
		return c;
	}

	protected String doAction1AdditionalSetPath() {
		String modulesServerPath = command.getTargetPath() + command.getTargetFilename();
		command.setTargetFilename(jFileChooserLocal.getSelectedFile().getName());
		return modulesServerPath;
	}

	private void addListener(Component comp) {
		if (comp instanceof JRadioButton) {
			((JRadioButton) comp)
					.addActionListener(actionEvent -> enableComponents(jRadioButtonFromServer.isSelected()));
		}
	}
}
