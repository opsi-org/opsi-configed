package de.uib.configed.gui.ssh;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.CommandModulesUpload;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.Empty_Command;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHModulesUploadDialog extends SSHFileUploadDialog {
	private JLabel lbl_copy_to_modules_d;
	private JCheckBox cb_copy_to_modules_d;
	private static String title = configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.title");

	public SSHModulesUploadDialog() {
		this(null);
	}

	public SSHModulesUploadDialog(CommandModulesUpload com) {
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
	protected void init_additional() {
		lbl_copy_to_modules_d = new JLabel();
		lbl_copy_to_modules_d.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.lbl_copy_to_modules_d"));

		cb_copy_to_modules_d = new JCheckBox();
		cb_copy_to_modules_d.setSelected(false);

		SSHConnectExec testFile = new SSHConnectExec();
		String result = testFile.exec(new Empty_Command(factory.str_command_fileexists_notremove.replaceAll(
				factory.str_replacement_filename, ((CommandModulesUpload) command).unofficial_modules_directory) // /etc/opsi/modules.d
		), false);
		/*
		 * if (result.trim().equals(factory.str_file_exists))
		 * {
		 * lbl_copy_to_modules_d.setVisible(true);
		 * cb_copy_to_modules_d.setVisible(true);
		 * cb_copy_to_modules_d.setSelected(true);
		 * }
		 * else
		 */
		{
			lbl_copy_to_modules_d.setVisible(false);
			cb_copy_to_modules_d.setVisible(false);
			cb_copy_to_modules_d.setSelected(false);
		}
	}

	/**
	 * is called at beginning of super.initGUI() add additionial components to
	 * layout, which are needed in this dialog
	 */
	@Override
	protected void initGUI_additional() {
		h_parallelGroup = inputPanelLayout.createSequentialGroup().addGroup(inputPanelLayout.createParallelGroup()
				.addGroup(inputPanelLayout.createSequentialGroup().addComponent(lbl_modules_from, PREF, PREF, PREF))
				.addComponent(lbl_set_rights, PREF, PREF, PREF).addComponent(lbl_overwriteExisting, PREF, PREF, PREF)
				.addComponent(lbl_copy_to_modules_d, PREF, PREF, PREF)).addGap(Globals.GAP_SIZE).addGroup(
						inputPanelLayout.createParallelGroup()
								.addComponent(cb_setRights, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
								.addComponent(cb_overwriteExisting, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH)
								.addComponent(cb_copy_to_modules_d, Globals.ICON_WIDTH, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH));
		v_parallelGroup = inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(lbl_copy_to_modules_d, PREF, PREF, PREF)
				.addComponent(cb_copy_to_modules_d, PREF, PREF, PREF);
	}

	@Override
	protected String doAction1_additional_setPath() {
		String modules_server_path = ((CommandModulesUpload) command).actually_modules_directory;
		if (cb_copy_to_modules_d.isVisible() && cb_copy_to_modules_d.isSelected()) {
			modules_server_path = ((CommandModulesUpload) command).unofficial_modules_directory;
			command.setTargetPath(modules_server_path);
			command.setTargetFilename(filechooser_local.getSelectedFile().getName());
		} else {
			command.setTargetPath(((CommandModulesUpload) command).actually_modules_directory);
			command.setTargetFilename(((CommandModulesUpload) command).DEFAULT_FILENAME);
		}
		return modules_server_path;
	}

	@Override
	protected CommandWget doAction1_additional_setWget(CommandWget c, String path) {
		if (cb_copy_to_modules_d.isVisible() && cb_copy_to_modules_d.isSelected())
			c.setDir(path + command.getTargetFilename());
		else
			c.setFilename(path + command.getTargetFilename());
		return c;
	}

}