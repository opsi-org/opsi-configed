package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandPackageUpdater;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHPackageUpdaterDialog extends FGeneralDialog {
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel jLabelInfo;
	private JLabel jLabelRepos;
	private JComboBox<String> jComboBoxActions;
	private JComboBox<String> jComboBoxRepos;
	private JButton jButtonDoAction;
	private CommandPackageUpdater command;

	public SSHPackageUpdaterDialog() {
		this(new CommandPackageUpdater());
	}

	public SSHPackageUpdaterDialog(CommandPackageUpdater command) {
		super(null, configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.title"), false);
		this.command = command;
		logging.info(this, "with command");
		retrieveRepos();
		init();
		initLayout();
	}

	private void retrieveRepos() {
		SSHConnectExec ssh = new SSHConnectExec();
		String result = "";
		try {
			result = ssh.exec(command, false /* =>without gui */ );
		} catch (Exception e) {
			logging.error(this, "ssh execution error", e);

		}

		if (result == null) {
			logging.error("retrieve repos " + "FAILED");
			command.setRepos(null);
		} else {
			String[] lines = result.split("\n");
			HashMap<String, String> repos = new HashMap<>();
			for (int i = 1; i < lines.length; i++) {
				String repostatus = lines[i].split(":")[0];

				// escaping ansicodes
				repostatus = repostatus.replace("\\[[0-9];[0-9][0-9];[0-9][0-9]m", "");
				repostatus = repostatus.replace("", "").replace("\u001B", "");

				String[] repoStatus = repostatus.split("\\(");
				repoStatus[1] = repoStatus[1].split("\\)")[0];
				repos.put(repoStatus[0], repoStatus[1]);
				command.setRepos(repos);
			}
		}
		command.needSudo = true;
	}

	private void init() {
		inputPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));

		jLabelInfo = new JLabel(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.info"));
		jLabelRepos = new JLabel(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.repos"));
		inputPanel.add(jLabelInfo);
		inputPanel.add(jLabelRepos);
		jButtonDoAction = new JButton();
		buttonPanel.add(jButtonDoAction);
		jButtonDoAction.setText(configed.getResourceValue("SSHConnection.buttonExec"));
		jButtonDoAction.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			jButtonDoAction.addActionListener(actionEvent -> {
				logging.info(this, "btn_doAction pressed");
				doAction1();
			});

		JButton jButtonClose = new JButton();
		buttonPanel.add(jButtonClose);
		jButtonClose.setText(configed.getResourceValue("SSHConnection.buttonClose"));
		jButtonClose.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		jButtonClose.addActionListener(actionEvent -> cancel());
		setComponentsEnabled(!Globals.isGlobalReadOnly());

		jComboBoxActions = new JComboBox<>(command.getActionsText());
		jComboBoxActions.addItemListener(itemEvent -> {
			if (((String) itemEvent.getItem())
					.equals(configed.getResourceValue("SSHConnection.command.opsipackageupdater.action.list")))
				jComboBoxRepos.setEnabled(itemEvent.getStateChange() != ItemEvent.SELECTED);
		});

		if (command.getRepos() != null) {
			jComboBoxRepos = new JComboBox<>(command.getRepos().keySet().toArray(new String[0]));
		} else {
			jComboBoxRepos = new JComboBox<>();
		}

		jComboBoxRepos
				.addItem(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.allrepositories"));
		jComboBoxRepos.setSelectedItem(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.allrepositories"));
		jComboBoxActions.setEnabled(true);
		inputPanel.add(jComboBoxActions);
		inputPanel.add(jComboBoxRepos);
	}

	private void setComponentsEnabled(boolean value) {
		jButtonDoAction.setEnabled(value);
	}

	/* This method is called when button 1 is pressed */
	@Override
	public void doAction1() {
		try {
			command.setAction(command.getAction((String) jComboBoxActions.getSelectedItem()));
			String repo = (String) jComboBoxRepos.getSelectedItem();
			if (repo.equals(
					configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.allrepositories")))
				command.setRepo(null);
			else
				command.setRepo(repo);
			logging.info(this, "doAction1 opsi-package-updater: " + command.toString());
			new SSHConnectExec(command);

		} catch (Exception e) {
			logging.warning(this, "doAction1, exception occurred", e);
		}

	}

	// /* This method gets called when button 2 is pressed */
	public void cancel() {
		super.doAction2();
	}

	private void initLayout() {
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(jLabelInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelRepos, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(jComboBoxActions, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addComponent(jComboBoxRepos, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE))
				.addGap(Globals.GAP_SIZE));

		inputPanelLayout
				.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(2 * Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelInfo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jComboBoxActions, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(jLabelRepos, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(jComboBoxRepos, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT))
						.addGap(2 * Globals.GAP_SIZE));

		this.setSize(600, 210);
		this.setLocationRelativeTo(Globals.mainFrame);
		this.setBackground(Globals.BACKGROUND_COLOR_7);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
}