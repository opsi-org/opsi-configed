package de.uib.configed.gui.ssh;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.CommandSFTPUpload;

public class SSHPMInstallLocalPanel extends SSHPMInstallPanel {
	private JLabel lbl_uploadFrom;
	private JLabel lbl_uploadTo;
	private JTextField tf_path;
	private JFileChooser filechooser;
	private JButton btn_filechooser;
	private static SSHPMInstallLocalPanel instance;
	private JComboBox cb_autocompletion;
	private JButton btn_autocompletion;
	SSHCompletionComboButton autocompletion;

	public SSHPMInstallLocalPanel() {
		super();
		autocompletion = new SSHCompletionComboButton(additional_default_paths);
		initComponents();
		initLayout();
		instance = this;
	}

	private void initComponents() {
		lbl_uploadFrom = new JLabel(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelLocalFrom"));
		lbl_uploadTo = new JLabel(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelLocalTo"));
		tf_path = new JTextField();
		tf_path.setPreferredSize(Globals.textfieldDimension);

		cb_autocompletion = autocompletion.getCombobox();
		cb_autocompletion.setSelectedItem(workbench);
		cb_autocompletion.setEnabled(true);
		btn_autocompletion = autocompletion.getButton();

		filechooser = new JFileChooser();
		filechooser.setPreferredSize(Globals.filechooserSize);
		filechooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		filechooser.setApproveButtonText(configed.getResourceValue("FileChooser.approve"));
		filechooser.setDialogType(JFileChooser.OPEN_DIALOG);
		filechooser.setDialogTitle(Globals.APPNAME);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("opsi-paket (*.opsi) ", "opsi");
		filechooser.setFileFilter(filter);

		btn_filechooser = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		btn_filechooser.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		btn_filechooser.setPreferredSize(Globals.smallButtonDimension);
		btn_filechooser.setToolTipText(
				configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.filechooser.tooltip"));
		btn_filechooser.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int returnVal = filechooser.showOpenDialog(instance);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					String path_modules = filechooser.getSelectedFile().getPath();
					tf_path.setText(path_modules);
				} else {
					tf_path.setText("");
				}
			}
		});
	}

	private void initLayout() {
		this.setBackground(Globals.backLightBlue);

		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addGroup(layout
				.createParallelGroup(center)
				.addComponent(lbl_uploadFrom, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(tf_path, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(btn_filechooser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(lbl_uploadTo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(cb_autocompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(btn_autocompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(2 * Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)

				.addGroup(layout.createParallelGroup().addComponent(lbl_uploadFrom, PREF, PREF, PREF)
						.addComponent(lbl_uploadTo, PREF, PREF, PREF))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup().addComponent(tf_path, PREF, PREF, MAX).addComponent(
								btn_filechooser, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH))
						.addGroup(layout.createSequentialGroup().addComponent(cb_autocompletion, PREF, PREF, MAX)
								.addComponent(btn_autocompletion, PREF, PREF, PREF)))
				.addGap(2 * Globals.GAP_SIZE));

	}

	public CommandSFTPUpload getCommand() {
		if ((tf_path.getText() == null) || (tf_path.getText().equals("")))
			return null;
		CommandSFTPUpload com1 = new CommandSFTPUpload("PackegeUpload");
		com1.setCommand("SFTP local file to server");
		com1.setFullSourcePath(tf_path.getText());
		com1.setTargetPath((String) cb_autocompletion.getSelectedItem());
		com1.setTargetFilename(getFilename(com1.getFullSourcePath()));
		return com1;
	}

	private static String getFilename(String fullpathname) {
		return Paths.get(fullpathname).getFileName().toString();
	}
}