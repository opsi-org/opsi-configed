package de.uib.configed.gui.ssh;

import java.nio.file.Paths;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.opsicommand.sshcommand.CommandSFTPUpload;

public class SSHPMInstallLocalPanel extends SSHPMInstallPanel {
	private JLabel jLabelUploadFrom;
	private JLabel jLabelUploadTo;
	private JTextField jTextFieldPath;
	private JButton jButtonFileChooser;
	private static SSHPMInstallLocalPanel instance;
	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonAutoCompletion;
	SSHCompletionComboButton autocompletion;

	public SSHPMInstallLocalPanel() {
		super();
		autocompletion = new SSHCompletionComboButton(additionalDefaultPaths);
		initComponents();
		initLayout();
		instance = this;
	}

	private void initComponents() {
		jLabelUploadFrom = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelLocalFrom"));
		jLabelUploadTo = new JLabel(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelLocalTo"));
		jTextFieldPath = new JTextField();
		jTextFieldPath.setPreferredSize(Globals.textfieldDimension);

		jComboBoxAutoCompletion = autocompletion.getCombobox();
		jComboBoxAutoCompletion.setSelectedItem(workbench);
		jComboBoxAutoCompletion.setEnabled(true);
		jButtonAutoCompletion = autocompletion.getButton();

		JFileChooser jFileChooser = new JFileChooser();
		jFileChooser.setPreferredSize(Globals.filechooserSize);
		jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jFileChooser.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		jFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		jFileChooser.setDialogTitle(Globals.APPNAME);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("opsi-paket (*.opsi) ", "opsi");
		jFileChooser.setFileFilter(filter);

		jButtonFileChooser = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		jButtonFileChooser.setPreferredSize(Globals.smallButtonDimension);
		jButtonFileChooser.setToolTipText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.modulesupload.filechooser.tooltip"));
		jButtonFileChooser.addActionListener(actionEvent -> {
			int returnVal = jFileChooser.showOpenDialog(instance);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathModules = jFileChooser.getSelectedFile().getPath();
				jTextFieldPath.setText(pathModules);
			} else {
				jTextFieldPath.setText("");
			}
		});
	}

	private void initLayout() {
		this.setBackground(Globals.BACKGROUND_COLOR_7);

		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE).addGroup(layout
				.createParallelGroup(center)
				.addComponent(jLabelUploadFrom, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(jTextFieldPath, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(jButtonFileChooser, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(jLabelUploadTo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jButtonAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(2 * Globals.GAP_SIZE));

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)

				.addGroup(layout.createParallelGroup()
						.addComponent(jLabelUploadFrom, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelUploadTo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(jTextFieldPath, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE)
								.addComponent(jButtonFileChooser, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH))
						.addGroup(layout.createSequentialGroup()
								.addComponent(jComboBoxAutoCompletion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
								.addComponent(jButtonAutoCompletion, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)))
				.addGap(2 * Globals.GAP_SIZE));

	}

	public CommandSFTPUpload getCommand() {
		if ((jTextFieldPath.getText() == null) || (jTextFieldPath.getText().equals(""))) {
			return null;
		}

		CommandSFTPUpload com1 = new CommandSFTPUpload("PackegeUpload");
		com1.setCommand("SFTP local file to server");
		com1.setFullSourcePath(jTextFieldPath.getText());
		com1.setTargetPath((String) jComboBoxAutoCompletion.getSelectedItem());
		com1.setTargetFilename(getFilename(com1.getFullSourcePath()));
		return com1;
	}

	private static String getFilename(String fullpathname) {
		return Paths.get(fullpathname).getFileName().toString();
	}
}