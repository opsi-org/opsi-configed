package de.uib.configed.gui.ssh;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.CommandModulesUpload;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHModulesUploadDialog extends SSHFileUploadDialog {
	private JLabel jLabelCopyToModulesD;
	private JCheckBox jComboBoxCopyToModulesD;
	private static String title = configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.title");

	public SSHModulesUploadDialog() {
		super(title, new CommandModulesUpload());
		this.setVisible(true);
		logging.info(this, "SSHModulesUploadDialog build");
		height = 430;
		showDialog();
	}

	/**
	 * is called in the end of super.init() init additionial components, which
	 * are needed in this dialog
	 */
	@Override
	protected void initAdditional() {
		jLabelCopyToModulesD = new JLabel();
		jLabelCopyToModulesD.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_copy_to_modules_d"));

		jComboBoxCopyToModulesD = new JCheckBox();
		jComboBoxCopyToModulesD.setSelected(false);

		SSHConnectExec testFile = new SSHConnectExec();
		testFile.exec(new EmptyCommand(factory.str_command_fileexists_notremove.replace(
				factory.str_replacement_filename, ((CommandModulesUpload) command).unofficial_modules_directory) // /etc/opsi/modules.d
		), false);

		jLabelCopyToModulesD.setVisible(false);
		jComboBoxCopyToModulesD.setVisible(false);
		jComboBoxCopyToModulesD.setSelected(false);

	}

	/**
	 * is called at beginning of super.initGUI() add additionial components to
	 * layout, which are needed in this dialog
	 */
	@Override
	protected void initGUIAdditional() {
		horizontalParallelGroup = inputPanelLayout.createSequentialGroup().addGroup(inputPanelLayout
				.createParallelGroup()
				.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jLabelmodulesFrom, PREF, PREF, PREF))
				.addComponent(jLabelSetRights, PREF, PREF, PREF).addComponent(jLabelOverwriteExisting, PREF, PREF, PREF)
				.addComponent(jLabelCopyToModulesD, PREF, PREF, PREF)).addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(jComboBoxSetRights, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addComponent(jCheckBoxOverwriteExisting, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
								Globals.ICON_WIDTH)
						.addComponent(jComboBoxCopyToModulesD, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
								Globals.ICON_WIDTH));
		verticalParallelGroup = inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(jLabelCopyToModulesD, PREF, PREF, PREF)
				.addComponent(jComboBoxCopyToModulesD, PREF, PREF, PREF);
	}

	@Override
	protected String doAction1AdditionalSetPath() {
		String modulesServerPath = ((CommandModulesUpload) command).actually_modules_directory;
		if (jComboBoxCopyToModulesD.isVisible() && jComboBoxCopyToModulesD.isSelected()) {
			modulesServerPath = ((CommandModulesUpload) command).unofficial_modules_directory;
			command.setTargetPath(modulesServerPath);
			command.setTargetFilename(jFileChooserLocal.getSelectedFile().getName());
		} else {
			command.setTargetPath(((CommandModulesUpload) command).actually_modules_directory);
			command.setTargetFilename(CommandModulesUpload.DEFAULT_FILENAME);
		}
		return modulesServerPath;
	}

	@Override
	protected CommandWget doAction1AdditionalSetWget(CommandWget c, String path) {
		if (jComboBoxCopyToModulesD.isVisible() && jComboBoxCopyToModulesD.isSelected())
			c.setDir(path + command.getTargetFilename());
		else
			c.setFilename(path + command.getTargetFilename());
		return c;
	}

}