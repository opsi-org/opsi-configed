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
	protected JLabel jLabelVerbosity = new JLabel();
	protected JLabel jLabelFreeInput = new JLabel();

	protected JButton jButtonHelp;
	protected JButton jButtonExecute;
	protected JButton jButtonReload;
	protected JButton jButtonClose;

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
		this.setLocationRelativeTo(Globals.mainFrame);
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
		logging.info(this, "getRepositoriesFromConfigs depot " + depot);
		NavigableMap<String, Map<String, Object>> depotProperties = (NavigableMap<String, Map<String, Object>>) persist
				.getHostInfoCollections().getAllDepots();
		logging.info(this, "getRepositoriesFromConfigs depotProperties " + depotProperties);

		Map<String, Object> firstDepot;
		if (depot == null || depot.equals(defaultDepot) || depot.equals("all"))
			firstDepot = depotProperties.get(depotProperties.firstKey());
		else
			firstDepot = depotProperties.get(depot);
		logging.info(this, "getRepositoriesFromConfigs firstDepot " + firstDepot);

		String oRepo = ((String) firstDepot.get(configRepo)).replace("file://", "");
		if (oRepo != null && !oRepo.equals("null") && !oRepo.trim().equals(""))
			opsiRepo = oRepo + "/";

		logging.info(this, "getRepositoriesFromConfigs o_repo " + oRepo);
		logging.info(this, "getRepositoriesFromConfigs opsiRepo " + opsiRepo);

		// try

		logging.info(this, "getRepositoriesFromConfigs opsiRepo " + opsiRepo);
		logging.info(this, "getRepositoriesFromConfigs opsiProd " + opsiProd);

	}

	protected void initLabels() {
		jLabelVerbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
		jLabelFreeInput.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
	}

	protected void initButtons(final SSHPackageManagerParameterDialog caller) {

		jButtonHelp = new JButton("", Globals.createImageIcon("images/help-about.png", ""));
		jButtonHelp.setText(configed.getResourceValue("SSHConnection.buttonHelp"));

		jButtonHelp.addActionListener(actionEvent -> doActionHelp(caller));

		jButtonExecute = new JButton();
		buttonPanel.add(jButtonExecute);
		jButtonExecute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
		jButtonExecute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			jButtonExecute.addActionListener(actionEvent -> {
				if (caller instanceof SSHPackageManagerUninstallParameterDialog)
					((SSHPackageManagerUninstallParameterDialog) caller).doAction1();
				else if (caller instanceof SSHPackageManagerInstallParameterDialog)
					((SSHPackageManagerInstallParameterDialog) caller).doAction1();
			});
		jButtonReload = new JButton();
		buttonPanel.add(jButtonReload);
		jButtonReload.setText(configed.getResourceValue("SSHConnection.buttonPackagesReload"));
		jButtonReload.setIcon(Globals.createImageIcon("images/reloadcomplete16.png", ""));
		jButtonReload.setToolTipText(configed.getResourceValue("SSHConnection.buttonPackagesReload.tooltip"));
		if (!(Globals.isGlobalReadOnly()))
			jButtonReload.addActionListener(actionEvent -> {
				logging.debug(this, "ActionEvent on btn_reload");
				main.reload();
				consolidate();
			});

		jButtonClose = new JButton();
		buttonPanel.add(jButtonClose);
		jButtonClose.setText(configed.getResourceValue("SSHConnection.buttonClose"));
		jButtonClose.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		jButtonClose.addActionListener(actionEvent -> cancel());

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
		super.doAction2();
	}
}