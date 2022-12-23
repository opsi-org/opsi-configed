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
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandOpsiSetRights;
import de.uib.opsicommand.sshcommand.CommandSFTPUpload;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.Empty_Command;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommand_Template;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHFileUploadDialog extends FGeneralDialog {
	protected GroupLayout layout;
	protected JPanel inputPanel = new JPanel();
	protected JPanel buttonPanel = new JPanel();
	protected JPanel winAuthPanel = new JPanel();

	protected JRadioButton rb_from_server;
	protected JRadioButton rb_local;

	protected JFileChooser filechooser_local;
	protected JTextField tf_local_path;

	protected JButton btn_filechooser;
	protected JButton btn_execute;
	protected JButton btn_close;

	protected JLabel lbl_set_rights;
	protected JLabel lbl_modules_from;
	protected JLabel lbl_url;
	protected JLabel lbl_overwriteExisting;

	protected JButton btn_search;
	protected JCheckBox cb_setRights;
	protected JCheckBox cb_overwriteExisting;
	protected JTextField tf_url;

	protected GroupLayout.Group h_parallelGroup;
	protected GroupLayout.Group v_parallelGroup;
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
		this.centerOn(Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);

		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));

		ButtonGroup group = new ButtonGroup();
		rb_from_server = new JRadioButton(
				configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.rb_from_server"));
		group.add(rb_from_server);
		addListener(rb_from_server);
		rb_local = new JRadioButton(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.rb_local"),
				true);
		group.add(rb_local);
		addListener(rb_local);

		lbl_url = new JLabel();
		wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH + 90, Globals.BUTTON_HEIGHT);

		lbl_url.setText(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_url"));
		lbl_overwriteExisting = new JLabel();
		lbl_overwriteExisting
				.setText(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_overwriteExisting"));

		lbl_set_rights = new JLabel();
		lbl_set_rights.setText(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_setRights"));
		lbl_modules_from = new JLabel();
		lbl_modules_from
				.setText(configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.lbl_modules_from"));

		tf_url = new JTextField();
		tf_url.setText(wgetDefText);
		tf_url.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (tf_url.getText().equals(wgetDefText)) {
					tf_url.setSelectionStart(0);
					tf_url.setSelectionEnd(tf_url.getText().length());
				}
			}
		});
		tf_local_path = new JTextField();
		tf_local_path.setEditable(false);
		tf_local_path.setBackground(Globals.backLightYellow);

		cb_setRights = new JCheckBox();
		cb_setRights.setSelected(true);
		cb_overwriteExisting = new JCheckBox();
		cb_overwriteExisting.setSelected(true);

		filechooser_local = new JFileChooser();
		filechooser_local.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filechooser_local.setApproveButtonText(configed.getResourceValue("FileChooser.approve"));

		filechooser_local.setDialogType(JFileChooser.OPEN_DIALOG);
		filechooser_local.setDialogTitle(Globals.APPNAME + " "
				+ configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.filechooser.title"));

		btn_filechooser = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		btn_filechooser.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		btn_filechooser.setPreferredSize(Globals.smallButtonDimension);
		btn_filechooser.setToolTipText(
				configed.getResourceValue("SSHConnection.ParameterDialog.fileupload.filechooser.tooltip"));
		btn_filechooser.addActionListener(actionEvent -> {
			int returnVal = filechooser_local.showOpenDialog(inputPanel);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String path_modules = filechooser_local.getSelectedFile().getPath();
				tf_local_path.setText(path_modules);
				
				command.setFullSourcePath(path_modules);
				tf_local_path.setCaretPosition(path_modules.length());
			} else {
				tf_local_path.setText("");
			}
		});
		btn_execute = new JButton();
		buttonPanel.add(btn_execute);
		btn_execute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
		btn_execute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			btn_execute.addActionListener(actionEvent -> doAction1());

		btn_close = new JButton();
		buttonPanel.add(btn_close);
		btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
		btn_close.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		btn_close.addActionListener(actionEvent -> cancel());
		enableComponents(rb_from_server.isSelected());

		new SSHConnectExec().exec(new Empty_Command(factory.str_command_fileexists_notremove
				.replaceAll(factory.str_replacement_filename, command.getTargetPath()) // /etc/opsi/modules.d
		), false);

		init_additional();
		/*
		 * init_additional in ModulesUploadDialog do something like
		 * 
		 * //lbl_copy_to_modules_d.setText(configed.getResourceValue(
		 * "SSHConnection.ParameterDialog.fileupload.lbl_copy_to_modules_d"));
		 * 
		 * 
		 * 
		 * if (result.trim().equals(factory.str_file_exists))
		 * {
		 * lbl_copy_to_modules_d.setVisible(true);
		 * 
		 * 
		 * }
		 * else
		 * {
		 * lbl_copy_to_modules_d.setVisible(false);
		 * 
		 * 
		 * }
		 */
	}

	protected void init_additional() {
	}

	protected void enableComponents(boolean rb_local_isSelected) {
		((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).setEnabled(rb_local_isSelected);
		tf_url.setEnabled(rb_local_isSelected);
		tf_local_path.setEnabled(!rb_local_isSelected);
		btn_filechooser.setEnabled(!rb_local_isSelected);
	}

	protected void initGUI_additional() {
		h_parallelGroup = inputPanelLayout.createSequentialGroup().addGroup(inputPanelLayout.createParallelGroup()
				.addGroup(inputPanelLayout.createSequentialGroup().addComponent(lbl_modules_from, PREF, PREF, PREF))
				.addComponent(lbl_set_rights, PREF, PREF, PREF).addComponent(lbl_overwriteExisting, PREF, PREF, PREF))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(cb_setRights, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addComponent(cb_overwriteExisting, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
								Globals.ICON_WIDTH));
		v_parallelGroup = inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER);
	}

	protected void initGUI() {
		try {
			inputPanelLayout = new GroupLayout((JComponent) inputPanel);
			getContentPane().add(inputPanel, BorderLayout.CENTER);
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			inputPanel.setLayout(inputPanelLayout);

			initGUI_additional();
			inputPanelLayout.setHorizontalGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE)
					.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
							.addGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE * 2)
									.addGroup(inputPanelLayout
											.createSequentialGroup().addComponent(rb_local, PREF, PREF, MAX))
									.addGap(Globals.GAP_SIZE * 2)
									.addGroup(inputPanelLayout.createSequentialGroup().addComponent(rb_from_server,
											PREF, PREF, MAX))
									.addGroup(inputPanelLayout.createSequentialGroup().addGroup(h_parallelGroup) // parallelGroup can be overwritten by child
																																																																																																																																																			// classes
									)).addGap(Globals.GAP_SIZE))
					.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
							.addGroup(inputPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE * 2).addGroup(
									GroupLayout.Alignment.LEADING,
									inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
											.addComponent(tf_local_path, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
													MAX)
											.addGap(Globals.GAP_SIZE).addComponent(btn_filechooser, PREF, PREF, PREF)
											.addGap(Globals.GAP_SIZE))
									.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 3)
											.addGroup(inputPanelLayout.createParallelGroup()
													.addComponent(lbl_url, PREF, PREF, PREF).addComponent(
															wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH),
															PREF, PREF, PREF))
											.addGap(Globals.GAP_SIZE)
											.addGroup(inputPanelLayout.createParallelGroup()
													.addComponent(tf_url, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
															MAX)
													.addComponent(
															wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
															PREF, PREF, PREF)))
									.addGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
											.addComponent(wgetAuthPanel, PREF, PREF, MAX)))
							.addGap(Globals.GAP_SIZE))
					.addGap(Globals.GAP_SIZE));
			inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
					.addComponent(lbl_modules_from, PREF, PREF, PREF).addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
					.addComponent(rb_local, PREF, PREF, PREF).addGap(Globals.GAP_SIZE)
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addGap(Globals.GAP_SIZE * 3).addComponent(tf_local_path, PREF, PREF, PREF)
							.addComponent(btn_filechooser, PREF, PREF, PREF).addGap(Globals.GAP_SIZE * 3))
					.addGap(Globals.GAP_SIZE).addComponent(rb_from_server, PREF, PREF, PREF).addGap(Globals.GAP_SIZE)
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addGap(Globals.GAP_SIZE * 3).addComponent(tf_url, PREF, PREF, PREF)
							.addComponent(lbl_url, PREF, PREF, PREF).addGap(Globals.GAP_SIZE * 3))
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addGap(Globals.GAP_SIZE * 3)
							.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH), PREF, PREF, PREF)
							.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH), PREF, PREF, PREF)
							.addGap(Globals.GAP_SIZE * 3))
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)

							.addGap(Globals.GAP_SIZE).addComponent(wgetAuthPanel, PREF, PREF, PREF))
					.addGap(Globals.GAP_SIZE * 2)
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(lbl_set_rights, PREF, PREF, PREF)
							.addComponent(cb_setRights, PREF, PREF, PREF))
					.addGap(Globals.GAP_SIZE)
					.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(lbl_overwriteExisting, PREF, PREF, PREF)
							.addComponent(cb_overwriteExisting, PREF, PREF, PREF))
					.addGap(Globals.GAP_SIZE)

					.addGroup(v_parallelGroup) // parallelGroup can be overwritten by child classes
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
		if (rb_local.isSelected()) {
			if (tf_local_path.getText().equals("")) {
				logging.warning(this, "Please select local file.");
				return;
			}
		} else if (rb_from_server.isSelected()) {
			if (tf_url.getText().equals("") || (tf_url.getText().equals(wgetDefText))) {
				logging.warning(this, "Please enter url to file.");
				return;
			}

		}

		String modules_server_path = doAction1_additional_setPath();
		try {
			SSHCommand_Template fullcommand = new SSHCommand_Template();
			fullcommand.setMainName("FileUpload");
			if (rb_from_server.isSelected()) {
				CommandWget wget = new CommandWget();
				wget = doAction1_additional_setWget(wget, modules_server_path);

				wget.setUrl(tf_url.getText());

				if (((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
					wget.setAuthentication(" --no-check-certificate --user=" + wgetAuthPanel.getUser() + " --password="
							+ wgetAuthPanel.getPw() + " ");
				} else
					wget.setAuthentication(" ");
				fullcommand.addCommand((SSHCommand) wget);
			} else {
				command.setOverwriteMode(cb_overwriteExisting.isSelected());
				fullcommand.addCommand((SSHCommand) command);
			}

			if (cb_setRights.isSelected())
				fullcommand.addCommand(((SSHCommand) new CommandOpsiSetRights("")));
			new SSHConnectExec(fullcommand);
		} catch (Exception e) {
			logging.warning(this, "doAction1, exception occurred", e);
		}
	}

	protected CommandWget doAction1_additional_setWget(CommandWget c, String path) {
		c.setFilename(path);
		return c;
	}

	/* This method is called when button 1 is pressed */
	protected String doAction1_additional_setPath() {
		String modules_server_path = command.getTargetPath() + command.getTargetFilename();
		command.setTargetFilename(filechooser_local.getSelectedFile().getName());
		return modules_server_path;
	}

	protected void addListener(Component comp) {
		if (comp instanceof JRadioButton)
			((JRadioButton) comp).addActionListener(actionEvent -> enableComponents(rb_from_server.isSelected()));
	}
}