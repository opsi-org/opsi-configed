package de.uib.configed.gui.ssh;

import java.awt.Dimension;
import java.util.Map;
import java.util.NavigableMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerInstall;
import de.uib.opsicommand.sshcommand.CommandOpsiPackageManagerUninstall;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.opsidatamodel.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class SSHPackageManagerParameterDialog extends /* javax.swing.JDialog */ FGeneralDialog {

	protected AbstractPersistenceController persist = PersistenceControllerFactory.getPersistenceController();

	protected int frameWidth = 900;
	protected int frameHeight = 600;

	protected JPanel buttonPanel = new JPanel();
	protected JLabel jLabelVerbosity = new JLabel();
	protected JLabel jLabelFreeInput = new JLabel();

	protected JButton jButtonHelp;
	protected JButton jButtonExecute;
	protected JButton jButtonReload;
	protected JButton jButtonClose;

	protected String defaultProduct = Configed.getResourceValue("SSHConnection.ParameterDialog.defaultProduct");
	protected String defaultDepot = Configed.getResourceValue("SSHConnection.ParameterDialog.defaultDepot");

	protected String opsiProd = AbstractPersistenceController.configedWorkbenchDefaultValue;
	protected String opsiRepo = "/var/lib/opsi/repository/";

	private String configRepo = "repositoryLocalUrl";

	protected ConfigedMain main;

	public SSHPackageManagerParameterDialog(String title) {
		super(null, title);

		setTitle(title);
		setFont(Globals.defaultFont);
		setIconImage(Globals.mainIcon);

		this.setSize(new Dimension(Globals.DIALOG_FRAME_DEFAULT_WIDTH, frameHeight));
		this.setLocationRelativeTo(ConfigedMain.getMainFrame());
		this.setBackground(Globals.BACKGROUND_COLOR_7);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	}

	protected void setComponentsEnabled(boolean value) {
		if (jButtonHelp != null)
			jButtonHelp.setEnabled(value);
		if (jButtonExecute != null)
			jButtonExecute.setEnabled(value);
	}

	protected void getRepositoriesFromConfigs(String depot) {
		Logging.info(this, "getRepositoriesFromConfigs depot " + depot);
		NavigableMap<String, Map<String, Object>> depotProperties = (NavigableMap<String, Map<String, Object>>) persist
				.getHostInfoCollections().getAllDepots();
		Logging.info(this, "getRepositoriesFromConfigs depotProperties " + depotProperties);

		Map<String, Object> firstDepot;
		if (depot == null || depot.equals(defaultDepot) || depot.equals("all"))
			firstDepot = depotProperties.get(depotProperties.firstKey());
		else
			firstDepot = depotProperties.get(depot);
		Logging.info(this, "getRepositoriesFromConfigs firstDepot " + firstDepot);

		String oRepo = ((String) firstDepot.get(configRepo)).replace("file://", "");
		if (oRepo != null && !oRepo.equals("null") && !oRepo.trim().equals(""))
			opsiRepo = oRepo + "/";

		Logging.info(this, "getRepositoriesFromConfigs o_repo " + oRepo);
		Logging.info(this, "getRepositoriesFromConfigs opsiRepo " + opsiRepo);

		// try

		Logging.info(this, "getRepositoriesFromConfigs opsiRepo " + opsiRepo);
		Logging.info(this, "getRepositoriesFromConfigs opsiProd " + opsiProd);

	}

	protected void initLabels() {
		jLabelVerbosity.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		jLabelFreeInput.setText(Configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
	}

	protected void initButtons(final SSHPackageManagerParameterDialog caller) {

		jButtonHelp = new JButton("", Globals.createImageIcon("images/help-about.png", ""));
		jButtonHelp.setText(Configed.getResourceValue("SSHConnection.buttonHelp"));

		jButtonHelp.addActionListener(actionEvent -> doActionHelp(caller));

		jButtonExecute = new JButton();
		jButtonExecute.setText(Configed.getResourceValue("SSHConnection.buttonExec"));
		jButtonExecute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			jButtonExecute.addActionListener(actionEvent -> {
				if (caller instanceof SSHPackageManagerUninstallParameterDialog)
					((SSHPackageManagerUninstallParameterDialog) caller).doAction2();
				else if (caller instanceof SSHPackageManagerInstallParameterDialog)
					((SSHPackageManagerInstallParameterDialog) caller).doAction2();
			});
		jButtonReload = new JButton();
		jButtonReload.setText(Configed.getResourceValue("SSHConnection.buttonPackagesReload"));
		jButtonReload.setIcon(Globals.createImageIcon("images/reloadcomplete16.png", ""));
		jButtonReload.setToolTipText(Configed.getResourceValue("SSHConnection.buttonPackagesReload.tooltip"));
		if (!(Globals.isGlobalReadOnly()))
			jButtonReload.addActionListener(actionEvent -> {
				Logging.debug(this, "ActionEvent on btn_reload");
				main.reload();
				consolidate();
			});

		jButtonClose = new JButton();
		jButtonClose.setText(Configed.getResourceValue("SSHConnection.buttonClose"));
		jButtonClose.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		jButtonClose.addActionListener(actionEvent -> cancel());

		buttonPanel.add(jButtonClose);
		buttonPanel.add(jButtonReload);
		buttonPanel.add(jButtonExecute);

		setComponentsEnabled(!Globals.isGlobalReadOnly());
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
		super.doAction1();
	}
}