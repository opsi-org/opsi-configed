package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
// import javax.swing.border.*;
// import javax.swing.event.*;
// import java.io.*;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandOpsiSetRights;
import de.uib.opsicommand.sshcommand.SSHCommand;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHOpsiSetRightsParameterDialog extends FGeneralDialog {
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel lbl_info;
	private JComboBox cb_autocompletion;
	private JButton btn_searchDir;

	private JButton btn_doAction;
	private JButton btn_close;
	private CommandOpsiSetRights commandopsisetrights;
	private Vector<String> additional_default_paths = new Vector();
	private SSHCompletionComboButton completion;

	public SSHOpsiSetRightsParameterDialog() {
		super(null, configed.getResourceValue("SSHConnection.command.opsisetrights"), false);
		commandopsisetrights = new CommandOpsiSetRights();
		init();
		initLayout();
	}

	public SSHOpsiSetRightsParameterDialog(CommandOpsiSetRights command) {
		super(null, configed.getResourceValue("SSHConnection.command.opsisetrights"), false);
		commandopsisetrights = command;
		init();
		initLayout();
	}

	private void init() {
		additional_default_paths.addElement(SSHCommandFactory.getInstance().opsipathVarDepot);
		completion = new SSHCompletionComboButton(additional_default_paths);

		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		lbl_info = new JLabel(configed.getResourceValue("SSHConnection.command.opsisetrights.additionalPath"));
		inputPanel.add(lbl_info);
		btn_doAction = new JButton();
		buttonPanel.add(btn_doAction);
		btn_doAction.setText(configed.getResourceValue("SSHConnection.buttonExec"));
		btn_doAction.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			btn_doAction.addActionListener(actionEvent -> {
				logging.info(this, "btn_doAction pressed");
				doAction1();
			});

		btn_close = new JButton();
		buttonPanel.add(btn_close);
		btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
		btn_close.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		btn_close.addActionListener(actionEvent -> cancel());
		setComponentsEnabled(!Globals.isGlobalReadOnly());

		btn_searchDir = completion.getButton();
		cb_autocompletion = completion.getCombobox();
		// completion.doButtonAction();
		cb_autocompletion.setEnabled(true);
		cb_autocompletion.addItem("");
		cb_autocompletion.setSelectedItem("");
		inputPanel.add(cb_autocompletion);
		inputPanel.add(btn_searchDir);
	}

	private void setComponentsEnabled(boolean value) {
		btn_doAction.setEnabled(value);
	}

	/* This method is called when button 1 is pressed */
	@Override
	public void doAction1() {
		try {
			commandopsisetrights.setDir(completion.combobox_getStringItem());;
			logging.info(this, "doAction1 opsi-set-rights with path: " + commandopsisetrights.getDir());
			// we are in the event queure
			new Thread() {
				@Override
				public void run() {
					new SSHConnectExec((SSHCommand) commandopsisetrights, btn_doAction);
				}
			}.start();

			// SSHConnectExec ssh = new SSHConnectExec((SSHCommand) commandopsisetrights );
			// cancel();
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
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(lbl_info,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createSequentialGroup()
								.addComponent(cb_autocompletion, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)
								.addComponent(btn_searchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_info, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(cb_autocompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(btn_searchDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));

		this.setSize(600, 200);
		this.centerOn(Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
}