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

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandOpsiSetRights;
import de.uib.opsicommand.sshcommand.CommandSFTPUpload;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.Empty_Command;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommand_Template;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHFileUploadDialog extends FGeneralDialog {
	protected GroupLayout layout;
	protected JPanel inputPanel = new JPanel();
	protected JPanel buttonPanel = new JPanel();
	protected JPanel winAuthPanel = new JPanel();

	protected JRadioButton jRadioButtonFromServer;
	protected JRadioButton jRadioButtonLocal;

	protected JFileChooser jFileChooserLocal;
	protected JTextField jTextFieldLocalPath;

	protected JButton jButtonFileChooser;
	protected JButton jButtonExecute;
	protected JButton jButtonClose;

	protected JLabel jLabelSetRights;
	protected JLabel jLabelmodulesFrom;
	protected JLabel jLabelURL;
	protected JLabel jLabelOverwriteExisting;

	protected JButton jButtonSearch;
	protected JCheckBox jComboBoxSetRights;
	protected JCheckBox jCheckBoxOverwriteExisting;
	protected JTextField jTextFieldURL;

	protected GroupLayout.Group horizontalParallelGroup;
	protected GroupLayout.Group verticalParallelGroup;
	protected int PREF = GroupLayout.PREFERRED_SIZE;
	protected int MAX = Short.MAX_VALUE;
	protected GroupLayout inputPanelLayout;

	protected SSHWgetAuthenticationPanel wgetAuthPanel;
	protected CommandSFTPUpload command;
	protected SSHCompletionComboButton autocompletion = new SSHCompletionComboButton();
	protected SSHCommandFactory factory = SSHCommandFactory.getInstance();
	protected SSHConnectionExecDialog dia;
	protected ConfigedMain main;
	protected static String wgetDefText;

	protected int height = 410;
	protected int width = 700;

	public SSHFileUploadDialog() {
		this(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.title"), null);
	}

	public SSHFileUploadDialog(String title, CommandSFTPUpload com) {
		super(null, title, false);
		this.command = com;
		if (this.command == null)
			command = new CommandSFTPUpload();
		wgetAuthPanel = new SSHWgetAuthenticationPanel();
		wgetDefText = configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url");
		init();
		initGUI();
		this.setSize(Globals.DIALOG_FRAME_DEFAULT_WIDTH, Globals.DIALOG_FRAME_DEFAULT_HEIGHT + 100);
		this.setLocationRelativeTo(Globals.mainFrame);
		this.setBackground(Globals.BACKGROUND_COLOR_7);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		wgetAuthPanel.isOpen = true;
		wgetAuthPanel.close();
		logging.info(this, "SSHFileUploadDialog build");
	}

	protected void showDialog() {
		this.setSize(width, height);
		this.setVisible(true);
		logging.info(this, "SSHFileUploadDialog show");
	}

	protected void init() {
		inputPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);

		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));

		ButtonGroup group = new ButtonGroup();
		jRadioButtonFromServer = new JRadioButton(
				configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.rb_from_server"));
		group.add(jRadioButtonFromServer);
		addListener(jRadioButtonFromServer);
		jRadioButtonLocal = new JRadioButton(
				configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.rb_local"), true);
		group.add(jRadioButtonLocal);
		addListener(jRadioButtonLocal);

		jLabelURL = new JLabel();
		wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH + 90, Globals.BUTTON_HEIGHT);

		jLabelURL.setText(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_url"));
		jLabelOverwriteExisting = new JLabel();
		jLabelOverwriteExisting
				.setText(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_overwriteExisting"));

		jLabelSetRights = new JLabel();
		jLabelSetRights.setText(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_setRights"));
		jLabelmodulesFrom = new JLabel();
		jLabelmodulesFrom
				.setText(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_modules_from"));

		jTextFieldURL = new JTextField();
		jTextFieldURL.setText(wgetDefText);
		jTextFieldURL.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (jTextFieldURL.getText().equals(wgetDefText)) {
					jTextFieldURL.setSelectionStart(0);
					jTextFieldURL.setSelectionEnd(jTextFieldURL.getText().length());
				}
			}
		});
		jTextFieldLocalPath = new JTextField();
		jTextFieldLocalPath.setEditable(false);
		jTextFieldLocalPath.setBackground(Globals.BACKGROUND_COLOR_9);

		jComboBoxSetRights = new JCheckBox();
		jComboBoxSetRights.setSelected(true);
		jCheckBoxOverwriteExisting = new JCheckBox();
		jCheckBoxOverwriteExisting.setSelected(true);

		jFileChooserLocal = new JFileChooser();
		jFileChooserLocal.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jFileChooserLocal.setApproveButtonText(configed.getResourceValue("FileChooser.approve"));

		jFileChooserLocal.setDialogType(JFileChooser.OPEN_DIALOG);
		jFileChooserLocal.setDialogTitle(Globals.APPNAME + " "
				+ configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.filechooser.title"));

		jButtonFileChooser = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setPreferredSize(Globals.smallButtonDimension);
		jButtonFileChooser.setToolTipText(
				configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.filechooser.tooltip"));
		jButtonFileChooser.addActionListener(actionEvent -> {
			int returnVal = jFileChooserLocal.showOpenDialog(inputPanel);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathModules = jFileChooserLocal.getSelectedFile().getPath();
				jTextFieldLocalPath.setText(pathModules);

				command.setFullSourcePath(pathModules);
				jTextFieldLocalPath.setCaretPosition(pathModules.length());
			} else {
				jTextFieldLocalPath.setText("");
			}
		});
		jButtonExecute = new JButton();
		buttonPanel.add(jButtonExecute);
		jButtonExecute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
		jButtonExecute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			jButtonExecute.addActionListener(actionEvent -> doAction1());

		jButtonClose = new JButton();
		buttonPanel.add(jButtonClose);
		jButtonClose.setText(configed.getResourceValue("SSHConnection.buttonClose"));
		jButtonClose.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		jButtonClose.addActionListener(actionEvent -> cancel());
		enableComponents(jRadioButtonFromServer.isSelected());

		new SSHConnectExec().exec(new Empty_Command(factory.str_command_fileexists_notremove
				.replace(factory.str_replacement_filename, command.getTargetPath()) // /etc/opsi/modules.d
		), false);

		initAdditional();
	}

	protected void initAdditional() {
	}

	protected void enableComponents(boolean isSelected) {
		((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).setEnabled(isSelected);
		jTextFieldURL.setEnabled(isSelected);
		jTextFieldLocalPath.setEnabled(!isSelected);
		jButtonFileChooser.setEnabled(!isSelected);
	}

	protected void initGUIAdditional() {
		horizontalParallelGroup = inputPanelLayout.createSequentialGroup().addGroup(inputPanelLayout
				.createParallelGroup()
				.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jLabelmodulesFrom, PREF, PREF, PREF))
				.addComponent(jLabelSetRights, PREF, PREF, PREF)
				.addComponent(jLabelOverwriteExisting, PREF, PREF, PREF)).addGap(Globals.GAP_SIZE).addGroup(
						inputPanelLayout.createParallelGroup()
								.addComponent(jComboBoxSetRights, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH)
								.addComponent(jCheckBoxOverwriteExisting, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH));
		verticalParallelGroup = inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER);
	}

	protected void initGUI() {
		try {
			inputPanelLayout = new GroupLayout(inputPanel);
			getContentPane().add(inputPanel, BorderLayout.CENTER);
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			inputPanel.setLayout(inputPanelLayout);

			initGUIAdditional();
			inputPanelLayout.setHorizontalGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE)
					.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
							.addGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE * 2)
									.addGroup(inputPanelLayout.createSequentialGroup()
											.addComponent(jRadioButtonLocal, PREF, PREF, MAX))
									.addGap(Globals.GAP_SIZE * 2)
									.addGroup(inputPanelLayout.createSequentialGroup()
											.addComponent(jRadioButtonFromServer, PREF, PREF, MAX))
									.addGroup(inputPanelLayout.createSequentialGroup().addGroup(horizontalParallelGroup) // parallelGroup can be overwritten by child
																																																																																																																																																									// classes
									)).addGap(Globals.GAP_SIZE))
					.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
							.addGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE * 2)
									.addGroup(GroupLayout.Alignment.LEADING,
											inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
													.addComponent(jTextFieldLocalPath, Globals.BUTTON_WIDTH,
															Globals.BUTTON_WIDTH, MAX)
													.addGap(Globals.GAP_SIZE)
													.addComponent(jButtonFileChooser, PREF, PREF, PREF)
													.addGap(Globals.GAP_SIZE))
									.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
											.addGroup(inputPanelLayout.createParallelGroup()
													.addComponent(jLabelURL, PREF, PREF, PREF).addComponent(
															wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
															PREF, PREF, PREF))
											.addGap(Globals.GAP_SIZE)
											.addGroup(inputPanelLayout.createParallelGroup()
													.addComponent(jTextFieldURL, Globals.BUTTON_WIDTH,
															Globals.BUTTON_WIDTH, MAX)
													.addComponent(
															wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
															PREF, PREF, PREF)))
									.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
											.addComponent(wgetAuthPanel, PREF, PREF, MAX)))
							.addGap(Globals.GAP_SIZE))
					.addGap(Globals.GAP_SIZE));
			inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
					.addComponent(jLabelmodulesFrom, PREF, PREF, PREF).addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
					.addComponent(jRadioButtonLocal, PREF, PREF, PREF).addGap(Globals.GAP_SIZE)
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addGap(Globals.GAP_SIZE * 3).addComponent(jTextFieldLocalPath, PREF, PREF, PREF)
							.addComponent(jButtonFileChooser, PREF, PREF, PREF).addGap(Globals.GAP_SIZE * 3))
					.addGap(Globals.GAP_SIZE).addComponent(jRadioButtonFromServer, PREF, PREF, PREF)
					.addGap(Globals.GAP_SIZE)
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addGap(Globals.GAP_SIZE * 3).addComponent(jTextFieldURL, PREF, PREF, PREF)
							.addComponent(jLabelURL, PREF, PREF, PREF).addGap(Globals.GAP_SIZE * 3))
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addGap(Globals.GAP_SIZE * 3)
							.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH), PREF, PREF, PREF)
							.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH), PREF, PREF, PREF)
							.addGap(Globals.GAP_SIZE * 3))
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)

							.addGap(Globals.GAP_SIZE).addComponent(wgetAuthPanel, PREF, PREF, PREF))
					.addGap(Globals.GAP_SIZE * 2)
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(jLabelSetRights, PREF, PREF, PREF)
							.addComponent(jComboBoxSetRights, PREF, PREF, PREF))
					.addGap(Globals.GAP_SIZE)
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(jLabelOverwriteExisting, PREF, PREF, PREF)
							.addComponent(jCheckBoxOverwriteExisting, PREF, PREF, PREF))
					.addGap(Globals.GAP_SIZE)

					.addGroup(verticalParallelGroup) // parallelGroup can be overwritten by child classes
			);
		} catch (Exception e) {
			logging.error("Error", e);
		}
	}

	// // /* This method gets called when button 2 is pressed */
	public void cancel() {
		super.doAction2();
	}

	/* This method is called when button 1 is pressed */
	@Override
	public void doAction1() {
		logging.info(this, "doAction1 upload ");
		if (jRadioButtonLocal.isSelected()) {
			if (jTextFieldLocalPath.getText().equals("")) {
				logging.warning(this, "Please select local file.");
				return;
			}
		} else if (jRadioButtonFromServer.isSelected()
				&& (jTextFieldURL.getText().equals("") || (jTextFieldURL.getText().equals(wgetDefText)))) {
			logging.warning(this, "Please enter url to file.");
			return;
		}

		String modulesServerPath = doAction1AdditionalSetPath();
		try {
			SSHCommand_Template fullcommand = new SSHCommand_Template();
			fullcommand.setMainName("FileUpload");
			if (jRadioButtonFromServer.isSelected()) {
				CommandWget wget = new CommandWget();
				wget = doAction1AdditionalSetWget(wget, modulesServerPath);

				wget.setUrl(jTextFieldURL.getText());

				if (((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
					wget.setAuthentication(" --no-check-certificate --user=" + wgetAuthPanel.getUser() + " --password="
							+ wgetAuthPanel.getPw() + " ");
				} else
					wget.setAuthentication(" ");
				fullcommand.addCommand(wget);
			} else {
				command.setOverwriteMode(jCheckBoxOverwriteExisting.isSelected());
				fullcommand.addCommand(command);
			}

			if (jComboBoxSetRights.isSelected())
				fullcommand.addCommand((new CommandOpsiSetRights("")));
			new SSHConnectExec(fullcommand);
		} catch (Exception e) {
			logging.warning(this, "doAction1, exception occurred", e);
		}
	}

	protected CommandWget doAction1AdditionalSetWget(CommandWget c, String path) {
		c.setFilename(path);
		return c;
	}

	/* This method is called when button 1 is pressed */
	protected String doAction1AdditionalSetPath() {
		String modulesServerPath = command.getTargetPath() + command.getTargetFilename();
		command.setTargetFilename(jFileChooserLocal.getSelectedFile().getName());
		return modulesServerPath;
	}

	protected void addListener(Component comp) {
		if (comp instanceof JRadioButton)
			((JRadioButton) comp)
					.addActionListener(actionEvent -> enableComponents(jRadioButtonFromServer.isSelected()));
	}
}