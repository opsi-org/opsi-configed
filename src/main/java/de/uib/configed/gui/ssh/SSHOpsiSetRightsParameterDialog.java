package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

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
import de.uib.opsicommand.sshcommand.CommandOpsiSetRights;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHOpsiSetRightsParameterDialog extends FGeneralDialog {
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel jLabelInfo;
	private JComboBox<String> jComboBoxAutoCompletion;
	private JButton jButtonSearchDir;

	private JButton jButtonDoAction;
	private JButton jButtonClose;
	private CommandOpsiSetRights commandopsisetrights;
	private List<String> additionalDefaultPaths = new ArrayList<>();
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
		additionalDefaultPaths.add(SSHCommandFactory.getInstance().opsipathVarDepot);
		completion = new SSHCompletionComboButton(additionalDefaultPaths);

		inputPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		buttonPanel.setBackground(Globals.BACKGROUND_COLOR_7);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		jLabelInfo = new JLabel(configed.getResourceValue("SSHConnection.command.opsisetrights.additionalPath"));
		inputPanel.add(jLabelInfo);
		jButtonDoAction = new JButton();
		buttonPanel.add(jButtonDoAction);
		jButtonDoAction.setText(configed.getResourceValue("SSHConnection.buttonExec"));
		jButtonDoAction.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
		if (!(Globals.isGlobalReadOnly()))
			jButtonDoAction.addActionListener(actionEvent -> {
				logging.info(this, "btn_doAction pressed");
				doAction1();
			});

		jButtonClose = new JButton();
		buttonPanel.add(jButtonClose);
		jButtonClose.setText(configed.getResourceValue("SSHConnection.buttonClose"));
		jButtonClose.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
		jButtonClose.addActionListener(actionEvent -> cancel());
		setComponentsEnabled(!Globals.isGlobalReadOnly());

		jButtonSearchDir = completion.getButton();
		jComboBoxAutoCompletion = completion.getCombobox();

		jComboBoxAutoCompletion.setEnabled(true);
		jComboBoxAutoCompletion.addItem("");
		jComboBoxAutoCompletion.setSelectedItem("");
		inputPanel.add(jComboBoxAutoCompletion);
		inputPanel.add(jButtonSearchDir);
	}

	private void setComponentsEnabled(boolean value) {
		jButtonDoAction.setEnabled(value);
	}

	/* This method is called when button 1 is pressed */
	@Override
	public void doAction1() {
		try {
			commandopsisetrights.setDir(completion.comboBoxGetStringItem());
			logging.info(this, "doAction1 opsi-set-rights with path: " + commandopsisetrights.getDir());
			// we are in the event queure
			new Thread() {
				@Override
				public void run() {
					new SSHConnectExec(commandopsisetrights, jButtonDoAction);
				}
			}.start();

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
						.addGroup(inputPanelLayout.createSequentialGroup().addComponent(jLabelInfo,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE)
						.addGroup(inputPanelLayout.createSequentialGroup()
								.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)
								.addComponent(jButtonSearchDir, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGap(Globals.GAP_SIZE))
				.addGap(Globals.GAP_SIZE));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelInfo, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jComboBoxAutoCompletion, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(jButtonSearchDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE));

		this.setSize(600, 200);
		this.setLocationRelativeTo(Globals.mainFrame);
		this.setBackground(Globals.BACKGROUND_COLOR_7);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
}