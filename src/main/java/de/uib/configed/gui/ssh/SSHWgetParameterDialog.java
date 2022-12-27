package de.uib.configed.gui.ssh;

import java.awt.BorderLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGeneralDialog;
import de.uib.opsicommand.sshcommand.CommandWget;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.utilities.logging.logging;

public class SSHWgetParameterDialog extends FGeneralDialog {
	private JPanel inputPanel = new JPanel();
	private JPanel buttonPanel = new JPanel();

	private JLabel lbl_url = new JLabel();
	private JLabel lbl_dir = new JLabel();
	private JLabel lbl_verbosity = new JLabel();
	private JLabel lbl_freeInput = new JLabel();
	private JLabel lbl_fullCommand = new JLabel();

	private JButton btn_help;
	private JButton btn_execute;
	private JButton btn_close;
	private JButton btn_searchDir;

	private JTextField tf_url;
	private JTextField tf_dir;
	private JComboBox cb_dir;
	private JComboBox cb_verbosity;
	private JTextField tf_freeInput;

	CommandWget commandWget = new CommandWget();
	SSHCompletionComboButton completion = new SSHCompletionComboButton();
	private SSHWgetAuthenticationPanel wgetAuthPanel;

	public SSHWgetParameterDialog() {
		this(null);
	}

	public SSHWgetParameterDialog(ConfigedMain m) {
		super(null, configed.getResourceValue("SSHConnection.ParameterDialog.wget.title"), false);

		wgetAuthPanel = new SSHWgetAuthenticationPanel();
		init();
		initLayout();
		pack();
		setSize(Globals.dialogFrameDefaultSize);
		this.centerOn(Globals.mainFrame);
		this.setBackground(Globals.backLightBlue);
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.setVisible(true);
		if (Globals.isGlobalReadOnly())
			setComponentsEnabled_RO(false);
		cb_dir.setEnabled(true);
	}

	private void setComponentsEnabled_RO(boolean value) {
		tf_url.setEnabled(value);
		tf_url.setEditable(value);
		tf_dir.setEnabled(value);
		tf_dir.setEditable(value);
		cb_dir.setEnabled(value);
		cb_dir.setEditable(value);
		cb_verbosity.setEnabled(value);

		tf_freeInput.setEnabled(value);
		tf_freeInput.setEditable(value);

		btn_execute.setEnabled(value);
		btn_help.setEnabled(value);

	}

	private void init() {
		inputPanel.setBackground(Globals.backLightBlue);
		buttonPanel.setBackground(Globals.backLightBlue);
		getContentPane().add(inputPanel, BorderLayout.CENTER);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		buttonPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setBorder(BorderFactory.createTitledBorder(""));
		inputPanel.setPreferredSize(new java.awt.Dimension(376, 220));
		{
			lbl_url.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelUrl"));
			tf_url = new JTextField();
			tf_url.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url"));
			tf_url.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent documentEvent) {
					changeUrl();
				}

				@Override
				public void insertUpdate(DocumentEvent documentEvent) {
					changeUrl();
				}

				@Override
				public void removeUpdate(DocumentEvent documentEvent) {
					changeUrl();
				}
			});
			tf_url.addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					if (tf_url.getText().equals(
							configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url"))) {
						tf_url.setSelectionStart(0);
						tf_url.setSelectionEnd(tf_url.getText().length());
					}
				}
			});

			lbl_dir.setText(configed.getResourceValue("SSHConnection.ParameterDialog.wget.jLabelDirectory"));
			tf_dir = new JTextField();

			cb_dir = completion.getCombobox();
			btn_searchDir = completion.getButton();
		}
		{
			lbl_verbosity.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelVerbosity"));
			cb_verbosity = new JComboBox<>();
			cb_verbosity.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.verbosity"));
			for (int i = 0; i < 5; i++)
				cb_verbosity.addItem(i);
			cb_verbosity.setSelectedItem(1);
			cb_verbosity.addItemListener(itemEvent -> {
				commandWget.setVerbosity(((int) cb_verbosity.getSelectedItem()));
				updateCommand();
			});
		}
		{
			lbl_freeInput.setText(configed.getResourceValue("SSHConnection.ParameterDialog.jLabelFreeInput"));
			tf_freeInput = new JTextField();
			tf_freeInput.setToolTipText(configed.getResourceValue("SSHConnection.ParameterDialog.tooltip.freeInput"));
			tf_freeInput.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(DocumentEvent documentEvent) {
					changeFreeInput();
				}

				@Override
				public void insertUpdate(DocumentEvent documentEvent) {
					changeFreeInput();
				}

				@Override
				public void removeUpdate(DocumentEvent documentEvent) {
					changeFreeInput();
				}
			});
		}
		{
			((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).setSelected(false);
			wgetAuthPanel.isOpen = true;
			wgetAuthPanel.close();
			wgetAuthPanel.setLabelSizes(Globals.BUTTON_WIDTH + 67, Globals.BUTTON_HEIGHT);
		}
		{

			btn_help = new JButton("", Globals.createImageIcon("images/help-about.png", ""));
			btn_help.setText(configed.getResourceValue("SSHConnection.buttonParameterInfo"));
			btn_help.setToolTipText(configed.getResourceValue("SSHConnection.buttonParameterInfo.tooltip"));
			buttonPanel.add(btn_help);
			btn_help.addActionListener(actionEvent -> doActionHelp());

			btn_execute = new JButton();
			buttonPanel.add(btn_execute);
			btn_execute.setText(configed.getResourceValue("SSHConnection.buttonExec"));
			btn_execute.setIcon(Globals.createImageIcon("images/execute16_blue.png", ""));
			btn_execute.addActionListener(actionEvent -> {
				if (!(Globals.isGlobalReadOnly()))
					doAction1();
			});

			btn_close = new JButton();
			buttonPanel.add(btn_close);
			btn_close.setText(configed.getResourceValue("SSHConnection.buttonClose"));
			btn_close.setIcon(Globals.createImageIcon("images/cancelbluelight16.png", ""));
			btn_close.addActionListener(actionEvent -> cancel());
		}
		{
			lbl_fullCommand.setText("wget ");

			changeUrl();
			changeFreeInput();
		}
	}

	private void updateCommand() {
		lbl_fullCommand.setText(commandWget.getCommand());
	}

	private void changeFreeInput() {
		if (!tf_freeInput.getText().trim().equals(""))
			commandWget.setFreeInput(tf_freeInput.getText().trim());
		else
			commandWget.setFreeInput("");
		updateCommand();
	}

	private void changeUrl() {
		if (!(tf_url.getText().equals("")))
			commandWget.setUrl(tf_url.getText().trim());
		else
			commandWget.setUrl("");
		updateCommand();
	}

	/* This method is called when button 1 is pressed */
	@Override
	public void doAction1() {
		if ((tf_url.getText()
				.equals(configed.getResourceValue("SSHConnection.ParameterDialog.wget.tooltip.tf_wget_url")))
				|| (tf_url.getText().equals(""))) {
			logging.warning(this, "Please enter url.");
			return;
		}

		commandWget.setDir((String) cb_dir.getSelectedItem());
		if (((JCheckBox) wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH)).isSelected()) {
			commandWget.setAuthentication(" --no-check-certificate --user=" + wgetAuthPanel.getUser() + " --password="
					+ wgetAuthPanel.getPw() + " ");
		} else
			commandWget.setAuthentication(" ");
		updateCommand();

		if (commandWget.checkCommand()) {
			new Thread() {
				@Override
				public void run() {
					try {
						logging.info(this, "doAction1 wget ");
						new SSHConnectExec(commandWget, btn_execute);
						// btn_execute.setEnabled( true ) transferred to SwingWorker.done()

					} catch (Exception e) {
						logging.warning(this, "doAction1, exception occurred", e);
					}
				}
			}.start();
		}
	}

	public void doActionHelp() {
		SSHConnectionExecDialog dia = commandWget.startHelpDialog();
		dia.setVisible(true);
	}

	public void cancel() {
		super.doAction2();
	}

	private void initLayout() {
		GroupLayout inputPanelLayout = new GroupLayout((JComponent) inputPanel);
		inputPanel.setLayout(inputPanelLayout);
		int PREF = GroupLayout.PREFERRED_SIZE;
		int MAX = Short.MAX_VALUE;
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup()

				.addGap(Globals.GAP_SIZE)

				.addGroup(inputPanelLayout.createParallelGroup().addGroup(inputPanelLayout.createSequentialGroup()
						.addGroup(inputPanelLayout.createParallelGroup().addComponent(lbl_url, PREF, PREF, PREF)
								.addComponent(lbl_dir, PREF, PREF, PREF).addComponent(lbl_verbosity, PREF, PREF, PREF)
								.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH), PREF, PREF,
										PREF)
								.addComponent(lbl_freeInput, PREF, PREF, PREF))
						.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(inputPanelLayout.createParallelGroup()
								.addGroup(inputPanelLayout.createSequentialGroup().addComponent(tf_url, PREF, PREF,
										Short.MAX_VALUE))
								.addGroup(inputPanelLayout.createSequentialGroup()
										.addComponent(cb_dir, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
												Short.MAX_VALUE)
										.addComponent(btn_searchDir, PREF, PREF, PREF))
								.addComponent(cb_verbosity, GroupLayout.Alignment.LEADING, Globals.ICON_WIDTH,
										Globals.ICON_WIDTH, Globals.ICON_WIDTH)
								.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
										GroupLayout.Alignment.LEADING, PREF, PREF, PREF)
								.addComponent(tf_freeInput, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Short.MAX_VALUE)))
						.addComponent(wgetAuthPanel, PREF, PREF, MAX))
				.addGap(Globals.GAP_SIZE));

		inputPanelLayout.setVerticalGroup(inputPanelLayout.createSequentialGroup().addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(tf_url, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(lbl_url, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(btn_searchDir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(cb_dir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(lbl_dir, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(cb_verbosity, GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(lbl_verbosity, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.LBLNEEDAUTH), Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(wgetAuthPanel.get(SSHWgetAuthenticationPanel.CBNEEDAUTH),
								GroupLayout.Alignment.LEADING, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addComponent(wgetAuthPanel, PREF, PREF, PREF)

				.addGap(Globals.GAP_SIZE)
				.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(tf_freeInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
						.addComponent(lbl_freeInput, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(Globals.GAP_SIZE).addContainerGap(70, 70));
	}
}
