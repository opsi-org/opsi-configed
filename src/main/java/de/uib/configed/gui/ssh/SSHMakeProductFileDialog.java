package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandOpsiSetRights;
import de.uib.opsicommand.sshcommand.CommandOpsimakeproductfile;
import de.uib.opsicommand.sshcommand.Empty_Command;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommand_Template;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHMakeProductFileDialog extends FGeneralDialog {
	// In dieser Klasse gibt es Linux-Befehle (folgend), die zu Konstanten
	// ausgelagert werden sollen (noch nicht funktioniert)
	public JLabel lbl_exitcode = new JLabel();
	private JLabel lbl_dir = null;
	private JLabel lbl_productVersion;
	private JLabel lbl_productVersion_controlfile;
	private JLabel lbl_packageVersion;
	private JLabel lbl_packageVersion_controlfile;
	private JLabel lbl_md5sum;
	private JLabel lbl_zsync;
	private JLabel lbl_versions_controlfile;
	private JLabel lbl_versions;
	private JLabel lbl_setRights_now;
	private JLabel lbl_setRights;
	private JLabel lbl_removeExistingPackage;
	private JLabel lbl_removeExistingPackage2;
	private JTextField tf_packageVersion;
	private JTextField tf_productVersion;
	private JComboBox cb_mainDir;
	private JCheckBox cb_md5sum;
	private JCheckBox cb_zsync;
	private JCheckBox cb_overwrite;
	private JCheckBox cb_setRights;
	private JPanel workbenchpanel;
	private JPanel mainpanel;
	private JPanel buttonPanel;

	private JButton btn_advancedSettings;
	private JButton btn_setRights;
	private JButton btn_toPackageManager;
	private JButton btn_exec;
	private JButton btn_cancel;
	private JButton btn_searchDir;
	private String filename;
	private ConfigedMain main = null;
	boolean isAdvancedOpen = true;
	private SSHCommandFactory factory = SSHCommandFactory.getInstance();
	SSHCompletionComboButton autocompletion = new SSHCompletionComboButton();

	public SSHMakeProductFileDialog(ConfigedMain m) {
		super(null, configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.title"), false);
		main = m;
		initGUI();

		this.centerOn(Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		filename = "";

		this.setSize(new java.awt.Dimension(Globals.DIALOG_FRAME_DEFAULT_WIDTH + 100,
				// workbenchpanel.getHeight() + buttonPanel.getHeight()
				Globals.DIALOG_FRAME_DEFAULT_HEIGHT + 100));
		autocompletion.doButtonAction();
		doSetActionGetVersions();
		showAdvancedSettings();
		setComponentsEnabled(!Globals.isGlobalReadOnly());
		this.setVisible(true);
	}

	private void setComponentsEnabled(boolean value) {

		btn_exec.setEnabled(value);
		if (value == false) {
			tf_packageVersion.setEnabled(value);
			tf_productVersion.setEnabled(value);
		}
		cb_mainDir.setEnabled(value);
		cb_md5sum.setEnabled(value);
		cb_zsync.setEnabled(value);
		cb_overwrite.setEnabled(value);
	}

	private String setOpsiPackageFilename(String path) {
		filename = path;
		btn_toPackageManager.setEnabled(true);
		btn_toPackageManager.setToolTipText(configed.getResourceValue(
				"SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager.tooltip") + " " + filename);
		return filename;
	}

	private void initGUI() {
		try {

			workbenchpanel = new JPanel();
			mainpanel = new JPanel();
			buttonPanel = new JPanel();
			workbenchpanel.setBackground(Globals.backLightBlue);
			mainpanel.setBackground(Globals.backLightBlue);
			buttonPanel.setBackground(Globals.backLightBlue);

			JPanel main_button_panel = new JPanel();
			main_button_panel.setBackground(Globals.backLightBlue);
			main_button_panel.setLayout(new BorderLayout());
			main_button_panel.add(mainpanel, BorderLayout.NORTH);
			main_button_panel.add(buttonPanel, BorderLayout.SOUTH);

			getContentPane().add(workbenchpanel, BorderLayout.CENTER);
			getContentPane().add(main_button_panel, BorderLayout.SOUTH);

			GroupLayout mainpanelLayout = new GroupLayout((JComponent) mainpanel);
			GroupLayout workbenchpanelLayout = new GroupLayout((JComponent) workbenchpanel);
			workbenchpanel.setLayout(workbenchpanelLayout);
			mainpanel.setLayout(mainpanelLayout);

			workbenchpanel.setBorder(BorderFactory.createTitledBorder(""));
			mainpanel.setBorder(BorderFactory.createTitledBorder(""));
			buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

			lbl_dir = new JLabel(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.serverDir"));
			{
				autocompletion.setCombobox(new SSHCompletionComboBox(
						new DefaultComboBoxModel<>(autocompletion.getDefaultValues().toArray())) {
					@Override
					public void setSelectedItem(Object item) {
						super.setSelectedItem(item);
						doSetActionGetVersions();
					}
				});
				autocompletion.initCombobox();
				cb_mainDir = autocompletion.getCombobox();

				btn_searchDir = autocompletion.getButton();
				btn_searchDir.removeActionListener(btn_searchDir.getActionListeners()[0]);
				btn_searchDir.addActionListener(actionEvent -> {
					autocompletion.doButtonAction();
					doSetActionGetVersions();
				});
			}
			{
				lbl_packageVersion = new JLabel();
				lbl_packageVersion.setText("    "
						+ configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.packageVersion"));
				lbl_productVersion = new JLabel();
				lbl_productVersion.setText("    "
						+ configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.productVersion"));
				lbl_versions_controlfile = new JLabel();
				lbl_versions = new JLabel();
				lbl_versions_controlfile.setText(configed
						.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.versions_controlfile"));
				lbl_versions
						.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.versions"));
				lbl_setRights = new JLabel();
				lbl_setRights
						.setText(configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights"));
				lbl_setRights_now = new JLabel();
				lbl_setRights_now.setText(
						configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights_now"));
				lbl_removeExistingPackage = new JLabel();
				lbl_removeExistingPackage.setText(
						configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.removeExisting"));
				lbl_removeExistingPackage2 = new JLabel();
				lbl_removeExistingPackage2.setText(
						configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.removeExisting2"));
			}
			{
				lbl_productVersion_controlfile = new JLabel();
				lbl_packageVersion_controlfile = new JLabel();
				tf_packageVersion = new JTextField();

				tf_productVersion = new JTextField();

				enableTfVersions(false);
			}
			{
				lbl_md5sum = new JLabel();
				lbl_md5sum.setText(
						configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.lbl_createMd5sum"));
				cb_md5sum = new JCheckBox();
				cb_md5sum.setSelected(true);
				lbl_zsync = new JLabel();
				lbl_zsync.setText(
						configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.lbl_createZsync"));
				cb_zsync = new JCheckBox();
				cb_zsync.setSelected(true);
				cb_overwrite = new JCheckBox();
				cb_overwrite.setSelected(true);
				cb_setRights = new JCheckBox();
				cb_setRights.setSelected(true);
			}
			{
				btn_advancedSettings = new JButton();
				btn_advancedSettings.setText(configed
						.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_advancedSettings"));

				if (!(Globals.isGlobalReadOnly()))
					btn_advancedSettings.addActionListener(actionEvent -> showAdvancedSettings());

				btn_advancedSettings.setPreferredSize(btn_searchDir.getPreferredSize());
				tf_productVersion.setPreferredSize(btn_searchDir.getPreferredSize());
				tf_packageVersion.setPreferredSize(btn_searchDir.getPreferredSize());

				btn_setRights = new JButton();
				btn_setRights.setText(
						configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_setRights"));
				btn_setRights.setToolTipText(configed
						.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_setRights.tooltip"));
				if (!(Globals.isGlobalReadOnly()))
					btn_setRights.addActionListener(actionEvent -> doExecSetRights());
			}
			{
				btn_toPackageManager = new JButton();
				btn_toPackageManager.setEnabled(false);
				btn_toPackageManager.setText(configed
						.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager"));
				btn_toPackageManager.setToolTipText(configed.getResourceValue(
						"SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager.tooltip"));

				if (!(Globals.isGlobalReadOnly()))
					btn_toPackageManager.addActionListener(actionEvent -> {
						if (main != null)
							new SSHPackageManagerInstallParameterDialog(main, filename);
					});

				btn_exec = new JButton();
				btn_exec.setText(configed.getResourceValue("SSHConnection.buttonExec"));
				btn_exec.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
				btn_exec.setEnabled(false);
				if (!(Globals.isGlobalReadOnly()))
					btn_exec.addActionListener(actionEvent -> doAction1());

				btn_cancel = new JButton();
				btn_cancel.setText(configed.getResourceValue("SSHConnection.buttonClose"));
				btn_cancel.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
				btn_cancel.addActionListener(actionEvent -> cancel());
				buttonPanel.add(btn_exec);
				buttonPanel.add(btn_toPackageManager);
				buttonPanel.add(btn_cancel);
			}

			workbenchpanelLayout
					.setHorizontalGroup(workbenchpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(lbl_dir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(lbl_setRights_now, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lbl_productVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lbl_packageVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lbl_removeExistingPackage, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(cb_mainDir, Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
											Short.MAX_VALUE)
									.addComponent(lbl_versions_controlfile, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(btn_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(lbl_productVersion_controlfile, Globals.BUTTON_WIDTH,
											Globals.BUTTON_WIDTH + 25, Short.MAX_VALUE)
									.addComponent(lbl_packageVersion_controlfile, Globals.BUTTON_WIDTH,
											Globals.BUTTON_WIDTH + 25, Short.MAX_VALUE)

									.addGroup(workbenchpanelLayout.createSequentialGroup()
											.addComponent(cb_overwrite, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addGap(Globals.GAP_SIZE)
											.addComponent(lbl_removeExistingPackage2, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(lbl_versions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(tf_productVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(tf_packageVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(btn_searchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(btn_advancedSettings, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

			workbenchpanelLayout.setVerticalGroup(workbenchpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
					.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(lbl_dir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(cb_mainDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(btn_searchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE)
					.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(lbl_setRights_now, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(btn_setRights, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
					.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(lbl_versions_controlfile, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lbl_versions, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE)
					.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(lbl_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(lbl_productVersion_controlfile, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(tf_productVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE)
					.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(lbl_packageVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(lbl_packageVersion_controlfile, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(tf_packageVersion, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
					.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
							.addComponent(lbl_removeExistingPackage, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(cb_overwrite, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(lbl_removeExistingPackage2, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(
							btn_advancedSettings, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE,
							GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))

			);

			mainpanelLayout.setHorizontalGroup(mainpanelLayout
					.createSequentialGroup().addGap(Globals.GAP_SIZE)
					.addGroup(mainpanelLayout.createParallelGroup()
							.addComponent(lbl_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(lbl_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(lbl_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE)
					.addGroup(mainpanelLayout.createParallelGroup()
							.addComponent(cb_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(cb_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(cb_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE));
			mainpanelLayout.setVerticalGroup(mainpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
					.addGroup(mainpanelLayout.createParallelGroup()
							.addComponent(lbl_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(cb_zsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE)
					.addGroup(mainpanelLayout.createParallelGroup()
							.addComponent(lbl_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(cb_md5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE)
					.addGroup(mainpanelLayout.createParallelGroup()
							.addComponent(lbl_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(cb_setRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE));

		} catch (Exception e) {
			logging.error("Error", e);
		}
	}

	private void showAdvancedSettings() {
		if (isAdvancedOpen) {
			isAdvancedOpen = false;
			this.setSize(this.getWidth(), this.getHeight() - mainpanel.getHeight());
			mainpanel.setVisible(isAdvancedOpen);
		} else {
			isAdvancedOpen = true;
			this.setSize(this.getWidth(), this.getHeight() + mainpanel.getHeight());
			mainpanel.setVisible(isAdvancedOpen);
		}
	}

	private String doActionGetVersions() {
		String dir = cb_mainDir.getEditor().getItem().toString() + "/OPSI/control";
		logging.info(this, "doActionGetVersions, dir " + dir);
		Empty_Command getVersions = new Empty_Command(
				factory.str_command_getVersions.replace(factory.str_replacement_dir, dir));
		SSHConnectExec ssh = new SSHConnectExec();
		logging.info(this, "doActionGetVersions, command " + getVersions);
		String result = ssh.exec(getVersions, false);
		logging.info(this, "doActionGetVersions result " + result);

		if (result == null) {
			logging.warning(this,
					"doActionGetVersions, could not find versions in file " + dir
							+ ".Please check if directory exists and contains the file OPSI/control.\n"
							+ "Please also check the rights of the file/s.");
		} else {
			String[] versions = result.replace("version: ", "").split("\n");
			logging.info(this, "doActionGetVersions, getDirectories result " + java.util.Arrays.toString(versions));
			if (versions.length < 1) {
				logging.info(this,
						"doActionGetVersions, not expected versions array " + java.util.Arrays.toString(versions));
				return "";
			}
			return versions[0] + ";;;" + versions[1];
		}
		return "";
	}

	public void doSetActionGetVersions() {
		String versions = doActionGetVersions();
		if (versions.contains(";;;")) {
			enableTfVersions(true);
			tf_packageVersion.setText(versions.split(";;;")[0]);
			lbl_packageVersion_controlfile.setText(versions.split(";;;")[0]);

			tf_productVersion.setText(versions.split(";;;")[1]);
			lbl_productVersion_controlfile.setText(versions.split(";;;")[1]);

			btn_exec.setEnabled(true);
		} else {
			enableTfVersions(false);
			tf_packageVersion.setText("");
			lbl_packageVersion_controlfile.setText("");
			tf_productVersion.setText("");
			lbl_productVersion_controlfile.setText("");

			btn_exec.setEnabled(false);
		}
	}

	private void enableTfVersions(boolean enable) {
		tf_packageVersion.setEnabled(enable);
		tf_productVersion.setEnabled(enable);
	}

	public void doExecSetRights() {
		String dir = cb_mainDir.getEditor().getItem().toString() + "";
		Empty_Command setRights = new Empty_Command("set-rights", "opsi-set-rights " + dir, "set-rights", true);
		SSHConnectExec ssh = new SSHConnectExec();
		SSHConnectionExecDialog.getInstance().setVisible(true);
		ssh.exec(setRights);
	}

	public void cancel() {
		super.doAction2();
	}

	@Override
	public void doAction1() {
		if ((lbl_productVersion_controlfile.getText() == null)
				|| (lbl_productVersion_controlfile.getText().equals(""))) {
			logging.warning(this, "Please select a valid opsi product directory.");
			return;
		}
		SSHCommand_Template str2exec = new SSHCommand_Template();
		String dir = cb_mainDir.getEditor().getItem().toString();

		String prodVersion = tf_productVersion.getText();
		String packVersion = tf_packageVersion.getText();
		prodVersion = checkVersion(prodVersion,
				configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"), "");
		packVersion = checkVersion(packVersion,
				configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"), "");
		CommandOpsimakeproductfile makeProductFile = new CommandOpsimakeproductfile(dir, packVersion, prodVersion,
				cb_md5sum.isSelected(), cb_zsync.isSelected());
		str2exec.setMainName(makeProductFile.getMenuText());
		if (cb_overwrite.isSelected()) {
			String versions = doActionGetVersions();
			prodVersion = checkVersion(prodVersion, "", versions.split(";;;")[1]);
			packVersion = checkVersion(packVersion, "", versions.split(";;;")[0]);
			setOpsiPackageFilename(dir + "" + getPackageID(dir) + "_" + prodVersion + "-" + packVersion + ".opsi");

			// ToDo: command_strings in sshcommandfactory auslagern
			//
			//
			String command = "[ -f " + filename + " ] &&  rm " + filename + " && echo \"File " + filename
					+ " removed\" || echo \"File did not exist\"";
			// Empty_Command removeExistingPackage = new
			// Empty_Command(str_command_fileexists.replace(str_replacement_filename,

			Empty_Command removeExistingPackage = new Empty_Command(command);
			str2exec.addCommand(removeExistingPackage);

			command = "[ -f " + filename + ".zsync ] &&  rm " + filename + ".zsync && echo \"File " + filename
					+ ".zsync removed\" || echo \"File  " + filename + ".zsync did not exist\"";
			// removeExistingPackage = new
			// Empty_Command(str_command_filezsyncExists.replace(str_replacement_filename,

			removeExistingPackage = new Empty_Command(command);
			str2exec.addCommand(removeExistingPackage);

			command = "[ -f " + filename + ".md5 ] &&  rm " + filename + ".md5 && echo \"File " + filename
					+ ".md5 removed\" || echo \"File  " + filename + ".md5 did not exist\"";
			removeExistingPackage = new Empty_Command(command);
			// removeExistingPackage = new
			// Empty_Command(str_command_filemd5Exists.replace(str_replacement_filename,

			str2exec.addCommand(removeExistingPackage);
		}
		if (cb_setRights.isSelected()) {
			str2exec.addCommand(new CommandOpsiSetRights(dir));
		}
		str2exec.addCommand(makeProductFile);

		logging.info(this, "SSHConnectExec " + str2exec);
		new Thread() {
			@Override
			public void run() {
				new SSHConnectExec(str2exec);
			}
		}.start();
	}

	private String checkVersion(String v, String compareWith, String overwriteWith) {
		if (v.equals(compareWith))
			return overwriteWith;
		return v;
	}

	private String getPackageID(String dir) {
		// cat " + dir + "OPSI/control | grep "id: "
		Empty_Command getPackageId = new Empty_Command(
				factory.str_command_catDir.replace(factory.str_replacement_dir, dir));
		SSHConnectExec ssh = new SSHConnectExec();
		String result = ssh.exec(getPackageId, false);
		logging.debug(this, "getPackageID result " + result);
		if (result != null)
			return result.replace("id:", "").trim();
		return "";

	}
}