package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandOpsiSetRights;
import de.uib.opsicommand.sshcommand.CommandOpsimakeproductfile;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.Logging;

public class SSHMakeProductFileDialog extends FGeneralDialog {
	// In dieser Klasse gibt es Linux-Befehle (folgend), die zu Konstanten
	// ausgelagert werden sollen (noch nicht funktioniert)
	private JLabel jLabelProductVersionControlFile;
	private JLabel jLabelPackageVersionControlFile;
	private JTextField jTextFieldPckageVersion;
	private JTextField jTextFieldProductVersion;
	private JComboBox<String> jComboBoxMainDir;
	private JCheckBox jCheckBoxmd5sum;
	private JCheckBox jCheckBoxzsync;
	private JCheckBox jCheckBoxOverwrite;
	private JCheckBox jCheckBoxSetRights;
	private JPanel mainpanel;

	private JButton jButtonToPackageManager;
	private JButton jButtonExec;
	private String filename;
	private ConfigedMain main = null;
	boolean isAdvancedOpen = true;
	SSHCompletionComboButton autocompletion = new SSHCompletionComboButton();

	public SSHMakeProductFileDialog(ConfigedMain m) {
		super(null, Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.title"), false);
		main = m;
		initGUI();

		filename = "";

		autocompletion.doButtonAction();
		doSetActionGetVersions();
		showAdvancedSettings();
		setComponentsEnabled(!Globals.isGlobalReadOnly());

		initFrame();
	}

	private void initFrame() {
		setLocationRelativeTo(ConfigedMain.getMainFrame());
		setBackground(Globals.BACKGROUND_COLOR_7);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(new java.awt.Dimension(Globals.DIALOG_FRAME_DEFAULT_WIDTH + 100,
				Globals.DIALOG_FRAME_DEFAULT_HEIGHT + 100));
		setVisible(true);
	}

	private void setComponentsEnabled(boolean value) {

		jButtonExec.setEnabled(value);
		if (!value) {
			jTextFieldPckageVersion.setEnabled(value);
			jTextFieldProductVersion.setEnabled(value);
		}
		jComboBoxMainDir.setEnabled(value);
		jCheckBoxmd5sum.setEnabled(value);
		jCheckBoxzsync.setEnabled(value);
		jCheckBoxOverwrite.setEnabled(value);
	}

	private String setOpsiPackageFilename(String path) {
		filename = path;
		jButtonToPackageManager.setEnabled(true);
		jButtonToPackageManager.setToolTipText(Configed.getResourceValue(
				"SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager.tooltip") + " " + filename);
		return filename;
	}

	private void initGUI() {
		try {
			JPanel workbenchpanel = new JPanel();
			mainpanel = new JPanel();
			JPanel buttonPanel = new JPanel();
			workbenchpanel.setBackground(Globals.BACKGROUND_COLOR_7);
			mainpanel.setBackground(Globals.BACKGROUND_COLOR_7);
			buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);

			JPanel mainButtonPanel = new JPanel();
			mainButtonPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			mainButtonPanel.setLayout(new BorderLayout());
			mainButtonPanel.add(mainpanel, BorderLayout.NORTH);
			mainButtonPanel.add(buttonPanel, BorderLayout.SOUTH);

			getContentPane().add(workbenchpanel, BorderLayout.CENTER);
			getContentPane().add(mainButtonPanel, BorderLayout.SOUTH);

			GroupLayout mainpanelLayout = new GroupLayout(mainpanel);
			GroupLayout workbenchpanelLayout = new GroupLayout(workbenchpanel);
			workbenchpanel.setLayout(workbenchpanelLayout);
			mainpanel.setLayout(mainpanelLayout);

			workbenchpanel.setBorder(BorderFactory.createTitledBorder(""));
			mainpanel.setBorder(BorderFactory.createTitledBorder(""));
			buttonPanel.setBorder(BorderFactory.createTitledBorder(""));

			JLabel jLabelDir = new JLabel(
					Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.serverDir"));

			autocompletion.setCombobox(new SSHCompletionComboBox<>(
					new DefaultComboBoxModel<>(autocompletion.getDefaultValues().toArray(new String[0]))) {
				@Override
				public void setSelectedItem(Object item) {
					super.setSelectedItem(item);
					doSetActionGetVersions();
				}
			});
			autocompletion.initCombobox();
			jComboBoxMainDir = autocompletion.getCombobox();

			JButton jButtonSearchDir = autocompletion.getButton();
			jButtonSearchDir.removeActionListener(jButtonSearchDir.getActionListeners()[0]);
			jButtonSearchDir.addActionListener(actionEvent -> {
				autocompletion.doButtonAction();
				doSetActionGetVersions();
			});

			JLabel jLabelPackageVersion = new JLabel();
			jLabelPackageVersion.setText(
					"    " + Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.packageVersion"));
			JLabel jLabelProductVersion = new JLabel();
			jLabelProductVersion.setText(
					"    " + Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.productVersion"));
			JLabel jLabelVersionsControlFile = new JLabel();
			JLabel jLabelVersions = new JLabel();
			jLabelVersionsControlFile.setText(
					Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.versions_controlfile"));
			jLabelVersions.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.versions"));
			JLabel jLabelSetRights = new JLabel();
			jLabelSetRights
					.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights"));
			JLabel jLabelSetRightsNow = new JLabel();
			jLabelSetRightsNow
					.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.setRights_now"));
			JLabel jLabelRemoveExistingPackage = new JLabel();
			jLabelRemoveExistingPackage
					.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.removeExisting"));
			JLabel jLabelRemoveExistingPackage2 = new JLabel();
			jLabelRemoveExistingPackage2.setText(
					Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.removeExisting2"));

			jLabelProductVersionControlFile = new JLabel();
			jLabelPackageVersionControlFile = new JLabel();
			jTextFieldPckageVersion = new JTextField();

			jTextFieldProductVersion = new JTextField();

			enableTfVersions(false);

			JLabel jLabelmd5sum = new JLabel();
			jLabelmd5sum.setText(
					Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.lbl_createMd5sum"));
			jCheckBoxmd5sum = new JCheckBox();
			jCheckBoxmd5sum.setSelected(true);
			JLabel jLabelzsync = new JLabel();
			jLabelzsync.setText(
					Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.lbl_createZsync"));
			jCheckBoxzsync = new JCheckBox();
			jCheckBoxzsync.setSelected(true);
			jCheckBoxOverwrite = new JCheckBox();
			jCheckBoxOverwrite.setSelected(true);
			jCheckBoxSetRights = new JCheckBox();
			jCheckBoxSetRights.setSelected(true);

			JButton jButtonAdvancedSettings = new JButton();
			jButtonAdvancedSettings.setText(
					Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_advancedSettings"));

			if (!(Globals.isGlobalReadOnly()))
				jButtonAdvancedSettings.addActionListener(actionEvent -> showAdvancedSettings());

			jButtonAdvancedSettings.setPreferredSize(jButtonSearchDir.getPreferredSize());
			jTextFieldProductVersion.setPreferredSize(jButtonSearchDir.getPreferredSize());
			jTextFieldPckageVersion.setPreferredSize(jButtonSearchDir.getPreferredSize());

			JButton jButtonSetRights = new JButton();
			jButtonSetRights
					.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_setRights"));
			jButtonSetRights.setToolTipText(
					Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.btn_setRights.tooltip"));
			if (!(Globals.isGlobalReadOnly()))
				jButtonSetRights.addActionListener(actionEvent -> doExecSetRights());

			jButtonToPackageManager = new JButton();
			jButtonToPackageManager.setEnabled(false);
			jButtonToPackageManager.setText(
					Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager"));
			jButtonToPackageManager.setToolTipText(Configed
					.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.buttonToPackageManager.tooltip"));

			if (!(Globals.isGlobalReadOnly()))
				jButtonToPackageManager.addActionListener(actionEvent -> {
					if (main != null)
						new SSHPackageManagerInstallParameterDialog(main, filename);
				});

			jButtonExec = new JButton();
			jButtonExec.setText(Configed.getResourceValue("SSHConnection.buttonExec"));
			jButtonExec.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
			jButtonExec.setEnabled(false);
			if (!(Globals.isGlobalReadOnly()))
				jButtonExec.addActionListener(actionEvent -> doAction2());

			JButton jButtonCancel = new JButton();
			jButtonCancel.setText(Configed.getResourceValue("SSHConnection.buttonClose"));
			jButtonCancel.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
			jButtonCancel.addActionListener(actionEvent -> cancel());
			buttonPanel.add(jButtonCancel);
			buttonPanel.add(jButtonToPackageManager);
			buttonPanel.add(jButtonExec);

			workbenchpanelLayout
					.setHorizontalGroup(workbenchpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelSetRightsNow, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelPackageVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelRemoveExistingPackage, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(jComboBoxMainDir, Globals.BUTTON_WIDTH, 2 * Globals.BUTTON_WIDTH,
											Short.MAX_VALUE)
									.addComponent(jLabelVersionsControlFile, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jButtonSetRights, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelProductVersionControlFile, Globals.BUTTON_WIDTH,
											Globals.BUTTON_WIDTH + 25, Short.MAX_VALUE)
									.addComponent(jLabelPackageVersionControlFile, Globals.BUTTON_WIDTH,
											Globals.BUTTON_WIDTH + 25, Short.MAX_VALUE)

									.addGroup(workbenchpanelLayout.createSequentialGroup()
											.addComponent(jCheckBoxOverwrite, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
											.addGap(Globals.GAP_SIZE)
											.addComponent(jLabelRemoveExistingPackage2, GroupLayout.PREFERRED_SIZE,
													GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
									.addComponent(jLabelVersions, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jTextFieldProductVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jTextFieldPckageVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jButtonAdvancedSettings, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)));

			workbenchpanelLayout
					.setVerticalGroup(workbenchpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(jLabelDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(jComboBoxMainDir, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(jLabelSetRightsNow, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jButtonSetRights, GroupLayout.Alignment.LEADING,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(jLabelVersionsControlFile, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelVersions, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelProductVersionControlFile, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jTextFieldProductVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(jLabelPackageVersion, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelPackageVersionControlFile, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jTextFieldPckageVersion, GroupLayout.Alignment.LEADING,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE).addGap(Globals.GAP_SIZE)
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(jLabelRemoveExistingPackage, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jCheckBoxOverwrite, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelRemoveExistingPackage2, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGroup(workbenchpanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
									.addComponent(jButtonAdvancedSettings, GroupLayout.Alignment.LEADING,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE))

					);

			mainpanelLayout
					.setHorizontalGroup(mainpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
							.addGroup(mainpanelLayout.createParallelGroup()
									.addComponent(jLabelzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE)
									.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE)
							.addGroup(mainpanelLayout.createParallelGroup()
									.addComponent(jCheckBoxzsync, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jCheckBoxmd5sum, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(jCheckBoxSetRights, GroupLayout.PREFERRED_SIZE,
											GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(Globals.GAP_SIZE));
			mainpanelLayout.setVerticalGroup(mainpanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
					.addGroup(mainpanelLayout.createParallelGroup()
							.addComponent(jLabelzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(jCheckBoxzsync, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE)
					.addGroup(mainpanelLayout.createParallelGroup()
							.addComponent(jLabelmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(jCheckBoxmd5sum, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE)
					.addGroup(mainpanelLayout.createParallelGroup()
							.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE)
							.addComponent(jCheckBoxSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
									GroupLayout.PREFERRED_SIZE))
					.addGap(Globals.GAP_SIZE));

		} catch (Exception e) {
			Logging.error("Error", e);
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
		String dir = jComboBoxMainDir.getEditor().getItem() + "/OPSI/control";
		Logging.info(this, "doActionGetVersions, dir " + dir);
		EmptyCommand getVersions = new EmptyCommand(SSHCommandFactory.STRING_COMMAND_GET_VERSIONS
				.replace(SSHCommandFactory.STRING_REPLACEMENT_DIRECTORY, dir));
		SSHConnectExec ssh = new SSHConnectExec();
		Logging.info(this, "doActionGetVersions, command " + getVersions);
		String result = ssh.exec(getVersions, false);
		Logging.info(this, "doActionGetVersions result " + result);

		if (result == null) {
			Logging.warning(this,
					"doActionGetVersions, could not find versions in file " + dir
							+ ".Please check if directory exists and contains the file OPSI/control.\n"
							+ "Please also check the rights of the file/s.");
		} else {
			String[] versions = result.replace("version: ", "").split("\n");
			Logging.info(this, "doActionGetVersions, getDirectories result " + java.util.Arrays.toString(versions));
			if (versions.length < 1) {
				Logging.info(this,
						"doActionGetVersions, not expected versions array " + java.util.Arrays.toString(versions));
				return "";
			}
			return versions[0] + ";;;" + versions[1];
		}
		return "";
	}

	public final void doSetActionGetVersions() {
		String versions = doActionGetVersions();
		if (versions.contains(";;;")) {
			enableTfVersions(true);
			jTextFieldPckageVersion.setText(versions.split(";;;")[0]);
			jLabelPackageVersionControlFile.setText(versions.split(";;;")[0]);

			jTextFieldProductVersion.setText(versions.split(";;;")[1]);
			jLabelProductVersionControlFile.setText(versions.split(";;;")[1]);

			jButtonExec.setEnabled(true);
		} else {
			enableTfVersions(false);
			jTextFieldPckageVersion.setText("");
			jLabelPackageVersionControlFile.setText("");
			jTextFieldProductVersion.setText("");
			jLabelProductVersionControlFile.setText("");

			jButtonExec.setEnabled(false);
		}
	}

	private void enableTfVersions(boolean enable) {
		jTextFieldPckageVersion.setEnabled(enable);
		jTextFieldProductVersion.setEnabled(enable);
	}

	public void doExecSetRights() {
		String dir = (String) jComboBoxMainDir.getEditor().getItem();
		EmptyCommand setRights = new EmptyCommand("set-rights", "opsi-set-rights " + dir, "set-rights", true);
		SSHConnectExec ssh = new SSHConnectExec();
		SSHConnectionExecDialog.getInstance().setVisible(true);
		ssh.exec(setRights);
	}

	public void cancel() {
		super.doAction1();
	}

	@Override
	public void doAction2() {
		if ((jLabelProductVersionControlFile.getText() == null)
				|| (jLabelProductVersionControlFile.getText().equals(""))) {
			Logging.warning(this, "Please select a valid opsi product directory.");
			return;
		}
		SSHCommandTemplate str2exec = new SSHCommandTemplate();
		String dir = (String) jComboBoxMainDir.getEditor().getItem();

		String prodVersion = jTextFieldProductVersion.getText();
		String packVersion = jTextFieldPckageVersion.getText();
		prodVersion = checkVersion(prodVersion,
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"), "");
		packVersion = checkVersion(packVersion,
				Configed.getResourceValue("SSHConnection.ParameterDialog.makeproductfile.keepVersions"), "");
		CommandOpsimakeproductfile makeProductFile = new CommandOpsimakeproductfile(dir, packVersion, prodVersion,
				jCheckBoxmd5sum.isSelected(), jCheckBoxzsync.isSelected());
		str2exec.setMainName(makeProductFile.getMenuText());
		if (jCheckBoxOverwrite.isSelected()) {
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

			EmptyCommand removeExistingPackage = new EmptyCommand(command);
			str2exec.addCommand(removeExistingPackage);

			command = "[ -f " + filename + ".zsync ] &&  rm " + filename + ".zsync && echo \"File " + filename
					+ ".zsync removed\" || echo \"File  " + filename + ".zsync did not exist\"";
			// removeExistingPackage = new
			// Empty_Command(str_command_filezsyncExists.replace(str_replacement_filename,

			removeExistingPackage = new EmptyCommand(command);
			str2exec.addCommand(removeExistingPackage);

			command = "[ -f " + filename + ".md5 ] &&  rm " + filename + ".md5 && echo \"File " + filename
					+ ".md5 removed\" || echo \"File  " + filename + ".md5 did not exist\"";
			removeExistingPackage = new EmptyCommand(command);
			// removeExistingPackage = new
			// Empty_Command(str_command_filemd5Exists.replace(str_replacement_filename,

			str2exec.addCommand(removeExistingPackage);
		}
		if (jCheckBoxSetRights.isSelected()) {
			str2exec.addCommand(new CommandOpsiSetRights(dir));
		}
		str2exec.addCommand(makeProductFile);

		Logging.info(this, "SSHConnectExec " + str2exec);
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
		EmptyCommand getPackageId = new EmptyCommand(SSHCommandFactory.STRING_COMMAND_CAT_DIRECTORY
				.replace(SSHCommandFactory.STRING_REPLACEMENT_DIRECTORY, dir));
		SSHConnectExec ssh = new SSHConnectExec();
		String result = ssh.exec(getPackageId, false);
		Logging.debug(this, "getPackageID result " + result);
		if (result != null)
			return result.replace("id:", "").trim();
		return "";

	}
}