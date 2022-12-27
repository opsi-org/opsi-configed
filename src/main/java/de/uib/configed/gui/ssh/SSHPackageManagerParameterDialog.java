package de.uib.configed.gui.ssh;

import java.awt.Dimension;
import java.util.Map;
import java.util.NavigableMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall;
import de.uib.opsidatamodel.PersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.logging;

public class SSHPackageManagerParameterDialog extends /* javax.swing.JDialog */ FGeneralDialog {

	protected PersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	protected int frameWidth = 900;
	protected int frameHeight = 600;

	protected JPanel buttonPanel = new JPanel();
	protected JLabel lbl_verbosity = new JLabel();
	protected JLabel lbl_freeInput = new JLabel();

	protected JButton btn_help;
	protected JButton btn_execute;
	protected JButton btn_reload;
	protected JButton btn_close;

	protected String defaultProduct = configed.getResourceValue("SSHConnection.ParameterDialog.defaultProduct");
	protected String defaultDepot = configed.getResourceValue("SSHConnection.ParameterDialog.defaultDepot");

	protected String opsiProd = PersistenceController.configedWORKBENCH_defaultvalue;
	protected String opsiRepo = "/var/lib/opsi/repository/";

	private String configRepo = "repositoryLocalUrl";

	protected ConfigedMain main;

	public SSHPackageManagerParameterDialog(String title) {
		super(null, title);

		setTitle(title);
		setFont(Globals.defaultFont);
		setIconImage(Globals.mainIcon);

		this.setSize(new Dimension(Globals.DIALOG_FRAME_DEFAULT_WIDTH, frameHeight));
		this.centerOn(Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	}

	protected void setComponentsEnabled(boolean value) {
		if (btn_help != null)
			btn_help.setEnabled(value);
		if (btn_execute != null)
			btn_execute.setEnabled(value);
	}

	protected void getRepositoriesFromConfigs(String depot) {
		logging.info(this, "getRepositoriesFromConfigs depot " + depot);
		NavigableMap<String, Map<String, Object>> depotProperties = (NavigableMap) persist.getHostInfoCollections()
				.getAllDepots();
		logging.info(this, "getRepositoriesFromConfigs depotProperties " + depotProperties);

		Map<String, Object> firstDepot;
		if (depot == null || depot.equals(defaultDepot) || depot.equals("all"))
			firstDepot = depotProperties.get(depotProperties.firstKey());
		else
			firstDepot = depotProperties.get(depot);
		logging.info(this, "getRepositoriesFromConfigs firstDepot " + firstDepot);

		String o_repo = ((String) firstDepot.get(configRepo)).replace("file://", "");
		if (o_repo != null && !o_repo.equals("null") && !o_repo.trim().equals(""))
			opsiRepo = o_repo + "/";

		logging.info(this, "getRepositoriesFromConfigs o_repo " + o_repo);
		logging.info(this, "getRepositoriesFromConfigs opsiRepo " + opsiRepo);

		// try

		logging.info(this, "getRepositoriesFromConfigs opsiRepo " + opsiRepo);
		logging.info(this, "getRepositoriesFromConfigs opsiProd " + opsiProd);

	}

	protected void initLabels() {
		lbl_verbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		lbl_freeInput.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
	}

	protected void initButtons(final SSHPackageManagerParameterDialog caller) {
		{

			btn_help = new JButton("", Globals.createImageIcon("images/help-about.png", ""));
			btn_help.setText(configed.getResourceValue("SSHConnection.buttonHelp"));

			btn_help.addActionListener(actionEvent -> doActionHelp(caller));

			btn_execute = new JButton();
			buttonPanel.add(btn_execute);
			btn_execute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
			btn_execute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
			if (!(Globals.isGlobalReadOnly()))
				btn_execute.addActionListener(actionEvent -> {
					if (caller instanceof SSHPackageManagerUninstallParameterDialog)
						((SSHPackageManagerUninstallParameterDialog) caller).doAction1();
					else if (caller instanceof SSHPackageManagerInstallParameterDialog)
						((SSHPackageManagerInstallParameterDialog) caller).doAction1();
				});
			btn_reload = new JButton();
			buttonPanel.add(btn_reload);
			btn_reload.setText(configed.getResourceValue("SSHConnection.buttonPackagesReload"));
			btn_reload.setIcon(Globals.createImageIcon("images/reloadcomplete16.png", ""));
			btn_reload.setToolTipText(configed.getResourceValue("SSHConnection.buttonPackagesReload.tooltip"));
			if (!(Globals.isGlobalReadOnly()))
				btn_reload.addActionListener(actionEvent -> {
					logging.debug(this, "ActionEvent on btn_reload");
					main.reload();
					consolidate();
				});

			btn_close = new JButton();
			buttonPanel.add(btn_close);
			btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
			btn_close.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
			btn_close.addActionListener(actionEvent -> cancel());

			setComponentsEnabled(!Globals.isGlobalReadOnly());
		}
	}

	protected void consolidate() {
		main.reload();
	}

	protected void doActionHelp(final SSHPackageManagerParameterDialog caller) {
		SSHConnectionExecDialog dia = null;
		if (caller instanceof SSHPackageManagerUninstallParameterDialog) {
			dia = new CommandOpsiPackageManagerUninstall().startHelpDialog();
		} else if (caller instanceof SSHPackageManagerInstallParameterDialog) {
			dia = new CommandOpsiPackageManagerInstall().startHelpDialog();
		}

		if (dia != null)
			dia.setVisible(true);
	}

	public void cancel() {
		super.doAction2();
	}
}