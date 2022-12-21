package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall;
import de.uib.opsicommand.sshcommand.CommandSFTPUpload;
import de.uib.opsicommand.sshcommand.SSHCommand_Template;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;
import de.uib.utilities.thread.WaitCursor;

public class SSHPackageManagerInstallParameterDialog extends SSHPackageManagerParameterDialog {
	private JPanel mainPanel = new JPanel();
	private JPanel radioPanel = new JPanel();
	private SSHPMInstallLocalPanel installLocalPanel;
	private SSHPMInstallServerPanel installServerPanel;
	private SSHPMInstallWgetPanel installWgetPanel;
	private SSHPMInstallSettingsPanel installSettingsPanel;
	private JLabel lbl_install = new JLabel();

	private JRadioButton rb_local;
	private JRadioButton rb_server;
	private JRadioButton rb_wget;
	private String fromMakeProductfile;

	public SSHPackageManagerInstallParameterDialog() {
		this(null);
	}

	public SSHPackageManagerInstallParameterDialog(ConfigedMain m) {
		this(m, "");
	}

	public SSHPackageManagerInstallParameterDialog(ConfigedMain m, String fullPathToPackage) {
		super(Globals.APPNAME + "  "
				+ configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.title"));

		WaitCursor waitCursor = new WaitCursor(this.getContentPane());
		main = m;
		fromMakeProductfile = fullPathToPackage;
		super.initButtons(this);
		installLocalPanel = new SSHPMInstallLocalPanel();
		installServerPanel = new SSHPMInstallServerPanel(fromMakeProductfile);
		installWgetPanel = new SSHPMInstallWgetPanel();
		installSettingsPanel = new SSHPMInstallSettingsPanel(this);

		initInstances();
		init();
		initLayout();

		pack();
		this.setSize(new Dimension(frameWidth, frameHeight));
		this.centerOn(Globals.mainFrame);
		this.setVisible(true);
		waitCursor.stop();
	}

	private void initInstances() {
		ButtonGroup group = new ButtonGroup();

		rb_local = new JRadioButton(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelFromLocal"));
		rb_server = new JRadioButton(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelFromServer"));
		rb_wget = new JRadioButton(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetFrom"));
		group.add(rb_local);
		group.add(rb_server);
		group.add(rb_wget);

		if ((fromMakeProductfile != null) && (!fromMakeProductfile.equals(""))) {
			rb_server.setSelected(true);
			installLocalPanel.isOpen = true; // if true, it can be closed
			installLocalPanel.close();

			installServerPanel.isOpen = false; // if false it can be opened
			installServerPanel.open();

			installServerPanel.setPackagePath(fromMakeProductfile);
		} else {

			rb_local.setSelected(true);
			installLocalPanel.isOpen = false; // if false it can be opened
			installLocalPanel.open();

			installServerPanel.isOpen = true; // if true, it can be closed
			installServerPanel.close();
		}
		installWgetPanel.isOpen = true;
		installWgetPanel.close();

		rb_local.addActionListener(actionEvent -> {
			installLocalPanel.open();
			installServerPanel.close();
			installWgetPanel.close();
		});
		rb_server.addActionListener(actionEvent -> {
			installLocalPanel.close();
			installServerPanel.open();
			installWgetPanel.close();
		});
		rb_wget.addActionListener(actionEvent -> {
			installLocalPanel.close();
			installServerPanel.close();
			installWgetPanel.open();
		});
	}

	private void initLayout() {
		int PREF = GroupLayout.PREFERRED_SIZE;
		int MAX = Short.MAX_VALUE;

		GroupLayout radioPanelLayout = new GroupLayout(radioPanel);
		radioPanel.setLayout(radioPanelLayout);

		radioPanelLayout.setHorizontalGroup(
				radioPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE).addComponent(rb_local, PREF, PREF, PREF)
						.addComponent(installLocalPanel, PREF, PREF, MAX).addComponent(rb_server, PREF, PREF, PREF)
						.addComponent(installServerPanel, PREF, PREF, MAX).addComponent(rb_wget, PREF, PREF, PREF)
						.addComponent(installWgetPanel, PREF, PREF, MAX).addGap(2 * Globals.GAP_SIZE));

		radioPanelLayout.setVerticalGroup(radioPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(rb_local, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installLocalPanel, PREF, PREF, PREF).addGap(Globals.GAP_SIZE)
				.addComponent(rb_server, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installServerPanel, PREF, PREF, PREF).addGap(Globals.GAP_SIZE)
				.addComponent(rb_wget, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installWgetPanel, PREF, PREF, PREF).addGap(Globals.GAP_SIZE));

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup()
				// .addGap(3*Globals.gapSize)
				.addGroup(mainPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
						.addComponent(lbl_install, PREF, PREF, PREF).addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(mainPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(radioPanel, PREF, PREF, MAX).addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE).addGroup(mainPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(installSettingsPanel, PREF, PREF, PREF).addGap(Globals.GAP_SIZE))
		// .addGap(3*Globals.gapSize)
		);

		mainPanelLayout.setVerticalGroup(mainPanelLayout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
				.addComponent(lbl_install).addGap(Globals.GAP_SIZE).addComponent(radioPanel).addGap(Globals.GAP_SIZE)
				.addComponent(installSettingsPanel).addGap(2 * Globals.GAP_SIZE));
	}

	protected void init() {
		radioPanel.setBackground(Globals.backLightBlue);
		mainPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);

		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		radioPanel.setBorder(BorderFactory.createTitledBorder(""));
		lbl_install.setText(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelInstall"));
	}

	@Override
	public void doAction1() {
		logging.info(this, " doAction1 install ");
		boolean sequential = false;
		final SSHConnectExec ssh = new SSHConnectExec();
		SSHCommand_Template commands = new SSHCommand_Template();
		commands.setMainName("PackageInstallation");
		CommandOpsiPackageManagerInstall pmInstallCom;

		if (rb_local.isSelected()) {
			sequential = true;
			CommandSFTPUpload sftpcom = installLocalPanel.getCommand();
			if (sftpcom == null) {
				logging.warning(this, "No opsi-package given. 1");
				return;
			}
			commands.addCommand(sftpcom);
			pmInstallCom = SSHPMInstallServerPanel.getCommand(sftpcom.getFullTargetPath());
			if (pmInstallCom == null) {
				logging.warning(this, "No url given. 2");
				return;
			}
			if (pmInstallCom != null)
				commands.addCommand(pmInstallCom);
			else
				logging.warning(this, "ERROR 0 command = null");
		}

		else if (rb_server.isSelected()) {
			pmInstallCom = installServerPanel.getCommand();
			if (pmInstallCom == null) {
				logging.warning(this, "No opsi-package selected. 3");
				return;
			}
			if (pmInstallCom != null)
				commands.addCommand(pmInstallCom);
			else
				logging.warning(this, "ERROR 1 command = null");
		}

		else { // if (rb_wget.isSelected()) {
			sequential = true;

			commands = installWgetPanel.getCommand(commands);
			if (commands == null) {
				logging.warning(this, "No opsi-package given.4");
				return;
			}

			pmInstallCom = SSHPMInstallServerPanel.getCommand(installWgetPanel.getProduct());
			logging.info(this, "c " + pmInstallCom);
			if (pmInstallCom != null)
				commands.addCommand(pmInstallCom);
			else
				logging.warning(this, "ERROR 3 command = null");
		}

		pmInstallCom = installSettingsPanel.updateCommand((CommandOpsiPackageManagerInstall) pmInstallCom);

		try {
			((SSHConnectExec) ssh).exec_template(commands, sequential);
			((SSHConnectExec) ssh).getDialog().setVisible(true);
			logging.info(this, "doAction1 end ");
		} catch (Exception e) {
			logging.error(this, "doAction1 Exception while exec_template", e);
		}
	}

}
