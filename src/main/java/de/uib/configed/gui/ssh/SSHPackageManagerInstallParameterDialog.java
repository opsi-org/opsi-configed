package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall;
import de.uib.opsicommand.sshcommand.CommandSFTPUpload;
import de.uib.opsicommand.sshcommand.SSHCommandTemplate;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.thread.WaitCursor;

public class SSHPackageManagerInstallParameterDialog extends SSHPackageManagerParameterDialog {
	private JPanel mainPanel = new JPanel();
	private JPanel radioPanel = new JPanel();
	private SSHPMInstallLocalPanel installLocalPanel;
	private SSHPMInstallServerPanel installServerPanel;
	private SSHPMInstallWgetPanel installWgetPanel;
	private SSHPMInstallSettingsPanel installSettingsPanel;
	private JLabel jLabelInstall = new JLabel();

	private JRadioButton jRadioButtonLocal;
	private JRadioButton jRadioButtonServer;
	private JRadioButton jRadioButtonWGet;
	private String fromMakeProductfile;

	public SSHPackageManagerInstallParameterDialog() {
		this(null);
	}

	public SSHPackageManagerInstallParameterDialog(ConfigedMain m) {
		this(m, "");
	}

	public SSHPackageManagerInstallParameterDialog(ConfigedMain m, String fullPathToPackage) {
		super(Globals.APPNAME + "  "
				+ Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.title"));

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

		super.pack();
		super.setSize(new Dimension(frameWidth, frameHeight));
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		super.setVisible(true);
		waitCursor.stop();
	}

	private void initInstances() {
		ButtonGroup group = new ButtonGroup();

		jRadioButtonLocal = new JRadioButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelFromLocal"));
		jRadioButtonServer = new JRadioButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelFromServer"));
		jRadioButtonWGet = new JRadioButton(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelWgetFrom"));
		group.add(jRadioButtonLocal);
		group.add(jRadioButtonServer);
		group.add(jRadioButtonWGet);

		if (fromMakeProductfile != null && !fromMakeProductfile.isEmpty()) {
			jRadioButtonServer.setSelected(true);

			// if true, it can be closed
			installLocalPanel.isOpen = true;
			installLocalPanel.close();

			// if false it can be opened
			installServerPanel.isOpen = false;
			installServerPanel.open();

			installServerPanel.setPackagePath(fromMakeProductfile);
		} else {
			jRadioButtonLocal.setSelected(true);

			// if false it can be opened
			installLocalPanel.isOpen = false;
			installLocalPanel.open();

			// if true, it can be closed
			installServerPanel.isOpen = true;
			installServerPanel.close();
		}
		installWgetPanel.isOpen = true;
		installWgetPanel.close();

		jRadioButtonLocal.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.open();
			installServerPanel.close();
			installWgetPanel.close();
		});
		jRadioButtonServer.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.close();
			installServerPanel.open();
			installWgetPanel.close();
		});
		jRadioButtonWGet.addActionListener((ActionEvent actionEvent) -> {
			installLocalPanel.close();
			installServerPanel.close();
			installWgetPanel.open();
		});
	}

	private void initLayout() {

		GroupLayout radioPanelLayout = new GroupLayout(radioPanel);
		radioPanel.setLayout(radioPanelLayout);

		radioPanelLayout.setHorizontalGroup(radioPanelLayout.createParallelGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonLocal, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installLocalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(jRadioButtonServer, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installServerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addComponent(jRadioButtonWGet, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(installWgetPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(2 * Globals.GAP_SIZE));

		radioPanelLayout.setVerticalGroup(radioPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonLocal, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installLocalPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonServer, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installServerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE)
				.addComponent(jRadioButtonWGet, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addComponent(installWgetPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.GAP_SIZE));

		GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
		mainPanel.setLayout(mainPanelLayout);
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup()

				.addGroup(mainPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE * 2)
						.addComponent(jLabelInstall, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(mainPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(radioPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(mainPanelLayout
						.createSequentialGroup().addGap(Globals.GAP_SIZE).addComponent(installSettingsPanel,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.GAP_SIZE))

		);

		mainPanelLayout.setVerticalGroup(mainPanelLayout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
				.addComponent(jLabelInstall).addGap(Globals.GAP_SIZE).addComponent(radioPanel).addGap(Globals.GAP_SIZE)
				.addComponent(installSettingsPanel).addGap(2 * Globals.GAP_SIZE));
	}

	private void init() {
		if (!Main.THEMES) {
			radioPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			mainPanel.setBackground(Globals.BACKGROUND_COLOR_7);
			buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		}

		getContentPane().add(mainPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		radioPanel.setBorder(BorderFactory.createTitledBorder(""));
		jLabelInstall.setText(
				Configed.getResourceValue("SSHConnection.ParameterDialog.opsipackagemanager_install.jLabelInstall"));
	}

	@Override
	public void doAction3() {
		Logging.info(this, " doAction3 install ");
		boolean sequential = false;
		SSHCommandTemplate commands = new SSHCommandTemplate();
		commands.setMainName("PackageInstallation");
		CommandOpsiPackageManagerInstall pmInstallCom;

		if (jRadioButtonLocal.isSelected()) {
			sequential = true;
			CommandSFTPUpload sftpcom = installLocalPanel.getCommand();
			if (sftpcom == null) {
				Logging.warning(this, "No opsi-package given. 1");
				return;
			}
			commands.addCommand(sftpcom);
			pmInstallCom = SSHPMInstallServerPanel.getCommand(sftpcom.getFullTargetPath());
			if (pmInstallCom == null) {
				Logging.warning(this, "No url given. 2");
				Logging.warning(this, "ERROR 0 command = null");
				return;
			} else {
				commands.addCommand(pmInstallCom);
			}
		} else if (jRadioButtonServer.isSelected()) {
			pmInstallCom = installServerPanel.getCommand();
			if (pmInstallCom == null) {
				Logging.warning(this, "No opsi-package selected. 3");
				return;
			} else {
				commands.addCommand(pmInstallCom);
			}
		} else {
			sequential = true;

			commands = installWgetPanel.getCommand(commands);
			if (commands == null) {
				Logging.warning(this, "No opsi-package given.4");
				return;
			}

			pmInstallCom = SSHPMInstallServerPanel.getCommand(installWgetPanel.getProduct());
			Logging.info(this, "c " + pmInstallCom);
			if (pmInstallCom != null) {
				commands.addCommand(pmInstallCom);
			} else {
				Logging.warning(this, "ERROR 3 command = null");
			}
		}

		installSettingsPanel.updateCommand(pmInstallCom);

		final SSHConnectExec ssh = new SSHConnectExec();
		try {
			ssh.execTemplate(commands, sequential);
			ssh.getDialog().setVisible(true);
			Logging.info(this, "doAction3 end ");
		} catch (Exception e) {
			Logging.error(this, "doAction3 Exception while exec_template", e);
		}
	}

}
