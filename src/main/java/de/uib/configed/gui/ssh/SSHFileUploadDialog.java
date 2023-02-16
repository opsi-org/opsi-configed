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
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.Logging;

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
	protected GroupLayout inputPanelLayout;

	protected SSHWgetAuthenticationPanel wgetAuthPanel;
	protected CommandSFTPUpload command;
	protected SSHCompletionComboButton autocompletion = new SSHCompletionComboButton();
	protected SSHConnectionExecDialog dia;
	protected ConfigedMain main;
	protected static String wgetDefText;

	protected int height = 410;
	protected int width = 700;

	public SSHFileUploadDialog() {
		this(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.title"), null);
	}

	public SSHFileUploadDialog(String title, CommandSFTPUpload com) {
		super(null, title, false);
		this.command = com;
		if (this.command == null) {
			command = new CommandSFTPUpload();
		}

		wgetAuthPanel = new SSHWgetAuthenticationPanel();
		wgetDefText = Configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url");
		init();
		initGUI();

		super.setSize(Globals.DIALOG_FRAME_DEFAULT_WIDTH, Globals.DIALOG_FRAME_DEFAULT_HEIGHT + 100);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		super.setBackground(Globals.BACKGROUND_COLOR_7);
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		wgetAuthPanel.isOpen = true;
		wgetAuthPanel.close();
		Logging.info(this, "SSHFileUploadDialog build");
	}

	protected void showDialog() {
		this.setSize(width, height);
		this.setVisible(true);
		Logging.info(this, "SSHFileUploadDialog show");
	}

	private void init() {
		inputPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);

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
		wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH + 90, Globals.BUTTON_HEIGHT);

		jLabelURL.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_url"));
		jLabelOverwriteExisting = new JLabel();
		jLabelOverwriteExisting
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_overwriteExisting"));

		jLabelSetRights = new JLabel();
		jLabelSetRights.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_setRights"));
		jLabelmodulesFrom = new JLabel();
		jLabelmodulesFrom
				.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_modules_from"));

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
		jFileChooserLocal.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));

		jFileChooserLocal.setDialogType(JFileChooser.OPEN_DIALOG);
		jFileChooserLocal.setDialogTitle(Globals.APPNAME + " "
				+ Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.filechooser.title"));

		jButtonFileChooser = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setPreferredSize(Globals.smallButtonDimension);
		jButtonFileChooser.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.filechooser.tooltip"));
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
		jButtonExecute.setText(Configed.getResourceValue("SSHConnection.buttonExec"));
		jButtonExecute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			jButtonExecute.addActionListener(actionEvent -> doAction2());

		jButtonClose = new JButton();
		jButtonClose.setText(Configed.getResourceValue("SSHConnection.buttonClose"));
		jButtonClose.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonExecute);

		enableComponents(jRadioButtonFromServer.isSelected());

		new SSHConnectExec().exec(new EmptyCommand(SSHCommandFactory.STRING_COMMAND_FILE_EXISTS_NOT_REMOVE
				.replace(SSHCommandFactory.STRING_REPLACEMENT_FILENAME, command.getTargetPath()) // /etc/opsi/modules.d
		), false);

		initAdditional();
	}

	protected void initAdditional() {
		/* To be implemented in subclass(es) */}

	protected void enableComponents(boolean isSelected) {
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
		try {
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
									.addGroup(inputPanelLayout.createSequentialGroup().addComponent(
											jRadioButtonFromServer, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))

									// parallelGroup can be overwritten by child
									.addGroup(inputPanelLayout.createSequentialGroup().addGroup(horizontalParallelGroup)
									// classes
									)).addGap(Globals.GAP_SIZE))
					.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
							.addGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE * 2)
									.addGroup(GroupLayout.Alignment.LEADING,
											inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
													.addComponent(jTextFieldLocalPath, Globals.BUTTON_WIDTH,
															Globals.BUTTON_WIDTH, Short.MAX_VALUE)
													.addGap(Globals.GAP_SIZE)
													.addComponent(jButtonFileChooser, GroupLayout.PREFERRED_SIZE,
															GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
													.addGap(Globals.GAP_SIZE))
									.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
											.addGroup(inputPanelLayout.createParallelGroup()
													.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE,
															GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
													.addComponent(
															wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
															GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
															GroupLayout.PREFERRED_SIZE))
											.addGap(Globals.GAP_SIZE)
											.addGroup(inputPanelLayout.createParallelGroup()
													.addComponent(jTextFieldURL, Globals.BUTTON_WIDTH,
															Globals.BUTTON_WIDTH, Short.MAX_VALUE)
													.addComponent(
															wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
															GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
															GroupLayout.PREFERRED_SIZE)))
									.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
											.addComponent(wgetAuthPanel, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)))
							.addGap(Globals.GAP_SIZE))
					.addGap(Globals.GAP_SIZE));
			inputPanelLayout
					.setVerticalGroup(
							inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
									.addComponent(jLabelmodulesFrom, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
									.addComponent(jRadioButtonLocal, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(Globals.GAP_SIZE)
									.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
											.addGap(Globals.GAP_SIZE * 3)
											.addComponent(jTextFieldLocalPath, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(jButtonFileChooser, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addGap(Globals.GAP_SIZE * 3))
									.addGap(Globals.GAP_SIZE)
									.addComponent(jRadioButtonFromServer, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addGap(Globals.GAP_SIZE)
									.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
											.addGap(Globals.GAP_SIZE * 3)
											.addComponent(jTextFieldURL, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(jLabelURL, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addGap(Globals.GAP_SIZE * 3))
									.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
											.addGap(Globals.GAP_SIZE * 3)
											.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE)
											.addGap(Globals.GAP_SIZE * 3))
									.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)

											.addGap(Globals.GAP_SIZE)
											.addComponent(wgetAuthPanel, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
									.addGap(Globals.GAP_SIZE * 2)
									.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
											.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(jComboBoxSetRights, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
									.addGap(Globals.GAP_SIZE)
									.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
											.addComponent(jLabelOverwriteExisting, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addComponent(jCheckBoxOverwriteExisting, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
									.addGap(Globals.GAP_SIZE)

									// parallelGroup can be overwritten by child classes
									.addGroup(verticalParallelGroup));
		} catch (Exception e) {
			Logging.error("Error", e);
		}
	}

	// // /* This method gets called when button 1 is pressed */
	public void cancel() {
		super.doAction1();
	}

	/* This method is called when button 2 is pressed */
	@Override
	public void doAction2() {
		Logging.info(this, "doAction2 upload ");
		if (jRadioButtonLocal.isSelected()) {
			if (jTextFieldLocalPath.getText().equals("")) {
				Logging.warning(this, "Please select local file.");
				return;
			}
		} else if (jRadioButtonFromServer.isSelected()
				&& (jTextFieldURL.getText().equals("") || (jTextFieldURL.getText().equals(wgetDefText)))) {
			Logging.warning(this, "Please enter url to file.");
			return;
		}

		String modulesServerPath = doAction1AdditionalSetPath();
		try {
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
				fullcommand.addCommand((new CommandOpsiSetRights("")));
			}

			new SSHConnectExec(fullcommand);
		} catch (Exception e) {
			Logging.warning(this, "doAction2, exception occurred", e);
		}
	}

	protected CommandWget doAction1AdditionalSetWget(CommandWget c, String path) {
		c.setFileName(path);
		return c;
	}

	/* This method is called when button 1 is pressed */
	protected String doAction1AdditionalSetPath() {
		String modulesServerPath = command.getTargetPath() + command.getTargetFilename();
		command.setTargetFilename(jFileChooserLocal.getSelectedFile().getName());
		return modulesServerPath;
	}

	protected void addListener(Component comp) {
		if (comp instanceof JRadioButton) {
			((JRadioButton) comp)
					.addActionListener(actionEvent -> enableComponents(jRadioButtonFromServer.isSelected()));
		}
	}
}