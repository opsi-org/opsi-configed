package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
// import javax.swing.border.*;
// import javax.swing.event.*;
// import java.io.*;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

// import java.nio.charset.Charset;
// import java.util.regex.*;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandPackageUpdater;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHPackageUpdaterDialog extends FGeneralDialog {
	private GroupLayout layout;
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel lbl_info;
	private JLabel lbl_repos;
	private JButton btn_searchDir;
	private JComboBox cb_actions;
	private JComboBox cb_repos;
	private JButton btn_doAction;
	private JButton btn_close;
	private CommandPackageUpdater command;

	public SSHPackageUpdaterDialog() {
		this(new CommandPackageUpdater());
	}

	public SSHPackageUpdaterDialog(CommandPackageUpdater command) {
		super(null, configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.title"), false);
		this.command = command;
		logging.info(this, "with command");
		retrieve_repos();
		init();
		initLayout();
	}

	private void retrieve_repos() {
		SSHConnectExec ssh = new SSHConnectExec();
		String result = "";
		try {
			result = ssh.exec((SSHCommand) command, false /* =>without gui */ );
		} catch (Exception e) {
			logging.error(this, "ssh execution error: " + e);
			logging.logTrace(e);

		}

		if (result == null) {
			logging.error("retrieve repos " + "FAILED");
			command.setRepos(null);
		} else {
			String[] lines = result.split("\n");
			HashMap<String, String> repos = new HashMap<String, String>();
			for (int i = 1; i < lines.length; i++) {
				String repostatus = lines[i].split(":")[0];

				// escaping ansicodes
				repostatus = repostatus.replaceAll("\\[[0-9];[0-9][0-9];[0-9][0-9]m", "");
				repostatus = repostatus.replace("", "").replace("\u001B", "");

				String[] repo_status = repostatus.split("\\(");
				repo_status[1] = repo_status[1].split("\\)")[0];
				repos.put(repo_status[0], repo_status[1]);
				command.setRepos(repos);
			}
		}
		command.needSudo = true;
	}

	private void init() {
		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));

		lbl_info = new JLabel(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.info"));
		lbl_repos = new JLabel(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.repos"));
		inputPanel.add(lbl_info);
		inputPanel.add(lbl_repos);
		btn_doAction = new JButton();
		buttonPanel.add(btn_doAction);
		btn_doAction.setText(configed.getResourceValue("SSHConnection.buttonExec"));
		btn_doAction.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			btn_doAction.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					logging.info(this, "btn_doAction pressed");
					doAction1();
				}
			});

		btn_close = new JButton();
		buttonPanel.add(btn_close);
		btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
		btn_close.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		btn_close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		setComponentsEnabled(!Globals.isGlobalReadOnly());

		cb_actions = new JComboBox(command.getActionsText());
		cb_actions.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if ((String) e.getItem() == configed
						.getResourceValue("SSHConnection.command.opsipackageupdater.action.list")) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						cb_repos.setEnabled(false);
					} else {
						cb_repos.setEnabled(true);
					}
				}
			}
		});

		if (command.getRepos() != null) {
			cb_repos = new JComboBox(command.getRepos().keySet().toArray());
		} else {
			cb_repos = new JComboBox();
		}

		cb_repos.addItem(configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.allrepositories"));
		cb_repos.setSelectedItem(
				configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.allrepositories"));
		cb_actions.setEnabled(true);
		// cb_actions.addItem("");
		inputPanel.add(cb_actions);
		inputPanel.add(cb_repos);
	}

	private void setComponentsEnabled(boolean value) {
		btn_doAction.setEnabled(value);
	}

	/* This method is called when button 1 is pressed */
	public void doAction1() {
		try {
			command.setAction(command.getAction((String) cb_actions.getSelectedItem()));
			String repo = (String) cb_repos.getSelectedItem();
			if (repo == configed.getResourceValue("SSHConnection.ParameterDialog.opsipackageupdater.allrepositories"))
				command.setRepo(null);
			else
				command.setRepo(repo);
			logging.info(this, "doAction1 opsi-package-updater: " + command.toString());
			new SSHConnectExec((SSHCommand) command);
			// cancel();
		} catch (Exception e) {
			logging.warning(this, "doAction1, exception occurred " + e);
			logging.logTrace(e);
		}

	}

	// /* This method gets called when button 2 is pressed */
	public void cancel() {
		super.doAction2();
	}

	private void initLayout() {
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup()
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(lbl_info, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(lbl_repos, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup()
						.addComponent(cb_actions, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE)
						.addComponent(cb_repos, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE))
				.addGap(Globals.gapSize));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup()
				.addGap(2 * Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_info, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addComponent(cb_actions, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight))
				.addGap(Globals.gapSize)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_repos, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight)
						.addComponent(cb_repos, Globals.buttonHeight, Globals.buttonHeight, Globals.buttonHeight))
				.addGap(2 * Globals.gapSize));

		this.setSize(600, 210);
		this.centerOn(de.uib.configed.Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
}