package de.uib.configed.gui.ssh;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsicommand.sshcommand.CommandModulesUpload;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.Logging;

public class SSHModulesUploadDialog extends SSHFileUploadDialog {
	private JLabel jLabelCopyToModulesD;
	private JCheckBox jComboBoxCopyToModulesD;
	private static final String TITLE = Configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.title");

	public SSHModulesUploadDialog() {
		super(TITLE, new CommandModulesUpload());
		super.setVisible(true);
		Logging.info(this, "SSHModulesUploadDialog build");
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
				Configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_copy_to_modules_d"));

		jComboBoxCopyToModulesD = new JCheckBox();
		jComboBoxCopyToModulesD.setSelected(false);

		SSHConnectExec testFile = new SSHConnectExec();
		testFile.exec(new EmptyCommand(SSHCommandFactory.STRING_COMMAND_FILE_EXISTS_NOT_REMOVE.replace(
				SSHCommandFactory.STRING_REPLACEMENT_FILENAME, CommandModulesUpload.UNOFFICIAL_MODULES_DIRECTORY) // /etc/opsi/modules.d
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
		horizontalParallelGroup = inputPanelLayout.createSequentialGroup()
				.addGroup(inputPanelLayout.createParallelGroup()
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jLabelmodulesFrom,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(jLabelSetRights, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelOverwriteExisting, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelCopyToModulesD, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(jComboBoxSetRights, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addComponent(jCheckBoxOverwriteExisting, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
								Globals.ICON_WIDTH)
						.addComponent(jComboBoxCopyToModulesD, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
								Globals.ICON_WIDTH));
		verticalParallelGroup = inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(jLabelCopyToModulesD, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(jComboBoxCopyToModulesD, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE);
	}

	@Override
	protected String doAction1AdditionalSetPath() {
		String modulesServerPath = CommandModulesUpload.ACTUAL_MODULES_DIRECTORY;
		if (jComboBoxCopyToModulesD.isVisible() && jComboBoxCopyToModulesD.isSelected()) {
			modulesServerPath = CommandModulesUpload.UNOFFICIAL_MODULES_DIRECTORY;
			command.setTargetPath(modulesServerPath);
			command.setTargetFilename(jFileChooserLocal.getSelectedFile().getName());
		} else {
			command.setTargetPath(CommandModulesUpload.ACTUAL_MODULES_DIRECTORY);
			command.setTargetFilename(CommandModulesUpload.DEFAULT_FILENAME);
		}
		return modulesServerPath;
	}

	@Override
	protected CommandWget doAction1AdditionalSetWget(CommandWget c, String path) {
		if (jComboBoxCopyToModulesD.isVisible() && jComboBoxCopyToModulesD.isSelected()) {
			c.setDir(path + command.getTargetFilename());
		} else {
			c.setFileName(path + command.getTargetFilename());
		}

		return c;
	}
}