package de.uib.configed.gui.ssh;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall;

public class SSHPMInstallServerPanel extends SSHPMInstallPanel {
	private JLabel lbl_server_dir = new JLabel();
	private JLabel lbl_opsi_product = new JLabel();
	private JTextField tf_product;

	private JComboBox cb_autocompletion;
	private JButton btn_autocompletion;
	SSHCompletionComboButton autocompletion;

	public SSHPMInstallServerPanel(String fullPathToPackage) {
		super();
		autocompletion = new SSHCompletionComboButton(
				additional_default_paths, ".opsi", fullPathToPackage);

		initComponents();
		setPackagePath(fullPathToPackage);
		initLayout();
		enable(true);
		cb_autocompletion.setSelectedItem(workbench);
	}

	public void setPackagePath(String pPath) {
		if (!(pPath.equals(""))) {
			cb_autocompletion.addItem(pPath);
			cb_autocompletion.setSelectedItem(pPath);
		}
	}

	public void enable(boolean e) {
		cb_autocompletion.setEnabled(e);
		btn_autocompletion.setEnabled(e);
	}

	private void initComponents() {

		lbl_opsi_product.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelOtherPath"));
		tf_product = autocompletion.getTextField();
		// tf_product.setEditable(false);
		cb_autocompletion = autocompletion.getCombobox();
		cb_autocompletion.setToolTipText(configed
				.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button_andopsipackage.combo.tooltip"));

		cb_autocompletion.setEnabled(true);
		btn_autocompletion = autocompletion.getButton();
		btn_autocompletion.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button_andopsipackage"));
		btn_autocompletion.setToolTipText(configed
				.getResourceValue("SSHConnection.ParameterDialog.autocompletion.button_andopsipackage.tooltip"));
	}

	private void initLayout() {
		this.setBackground(Globals.backLightBlue);

		GroupLayout layout = new GroupLayout(this);

		this.setLayout(layout);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(2 * Globals.gapSize)
				.addGroup(layout.createParallelGroup(center)
						.addComponent(lbl_server_dir, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addComponent(cb_autocompletion, Globals.buttonHeight, Globals.buttonHeight,
								Globals.buttonHeight)
						.addComponent(btn_autocompletion, Globals.buttonHeight, Globals.buttonHeight,
								Globals.buttonHeight))
				.addGroup(layout.createParallelGroup(center)
						.addComponent(lbl_opsi_product, Globals.buttonHeight, Globals.buttonHeight,
								Globals.buttonHeight)
						.addComponent(tf_product, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight))
				.addGap(2 * Globals.gapSize));

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(2 * Globals.gapSize)
				.addGroup(layout.createParallelGroup()
						.addComponent(lbl_server_dir, PREF, PREF, PREF)
						.addGroup(layout.createSequentialGroup()
								.addComponent(lbl_opsi_product, PREF, PREF, PREF)))
				.addGap(Globals.gapSize)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup()
								.addComponent(cb_autocompletion, Globals.buttonWidth, Globals.buttonWidth, MAX)
								.addComponent(btn_autocompletion, PREF, PREF, PREF))
						.addGroup(layout.createSequentialGroup()
								.addComponent(tf_product, Globals.buttonWidth, Globals.buttonWidth, MAX)))
				.addGap(2 * Globals.gapSize));
	}

	public CommandOpsiPackageManagerInstall getCommand() {
		return SSHPMInstallServerPanel.getCommand((String) tf_product.getText());
	}

	public static CommandOpsiPackageManagerInstall getCommand(String product) {
		// logging.error("product " + product);
		if ((product == null) || (product.equals("")))
			return null;
		CommandOpsiPackageManagerInstall com = new CommandOpsiPackageManagerInstall();
		com.setOpsiproduct(product.replaceAll("\n", ""));
		return com.checkCommand() ? com : null;
	}
}