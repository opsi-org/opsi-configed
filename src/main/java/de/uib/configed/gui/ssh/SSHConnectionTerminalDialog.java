package de.uib.configed.gui.ssh;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.Autocomplete;
import de.uib.opsicommand.sshcommand.SSHConnectTerminal;
import de.uib.utilities.logging.logging;

public class SSHConnectionTerminalDialog extends SSHConnectionOutputDialog {
	private JTextField tf_command;
	private JLabel lbl_command;
	private JCheckBox cb_privat;
	private JPanel parameterPanel;
	private JPanel terminatingPanel;
	public JButton btn_killProcess;
	private JButton btn_executeCommand;
	private final SSHConnectTerminal terminal;
	private ArrayList<String> commandHistory = new ArrayList<>();
	private int historyAddIndex = 0;
	private int historyGetIndex = 0;
	private Autocomplete autoComplete;
	private boolean passwordMode = false;
	private Dimension btn_dim = new Dimension(Globals.GRAPHIC_BUTTON_WIDTH_X, Globals.BUTTON_HEIGHT);
	private Dimension thissize = new Dimension(810, 600);

	private class TerminatingPanel extends JPanel {
		JButton btn_close;

		TerminatingPanel(ActionListener closeListener) {
			super();
			// setBackground(java.awt.Color.GREEN);
			setBackground(Globals.backLightBlue);
			btn_close = new de.uib.configed.gui.IconButton(
					de.uib.configed.configed.getResourceValue("SSHConnection.buttonClose"), "images/cancel.png",
					"images/cancel.png", "images/cancel.png", true);
			btn_close.addActionListener(closeListener);

			btn_close.setPreferredSize(btn_dim);

			GroupLayout layout = new GroupLayout(this);
			setLayout(layout);

			layout.setVerticalGroup(layout
					.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(btn_close,
							GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(3 * Globals.VGAP_SIZE));

			layout.setHorizontalGroup(
					layout.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
							.addComponent(btn_close, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
							.addGap(Globals.GAP_SIZE));

		}
	}

	public SSHConnectionTerminalDialog(String title, final SSHConnectTerminal terminal, boolean visible) {
		super(title);
		output.setBackground(Globals.lightBlack);
		output.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setCLfocus();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				setCLfocus();
			}

		});
		this.terminal = terminal;
		parameterPanel = new SSHCommandControlParameterMethodsPanel(this);
		((SSHCommandControlParameterMethodsPanel) parameterPanel).setGapSize(Globals.GAP_SIZE, Globals.GAP_SIZE,
				Globals.GAP_SIZE, 0);
		((SSHCommandControlParameterMethodsPanel) parameterPanel).initLayout();
		((SSHCommandControlParameterMethodsPanel) parameterPanel).repaint();
		((SSHCommandControlParameterMethodsPanel) parameterPanel).revalidate();

		closeListener = new DialogCloseListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				logging.debug(this, "actionPerformed " + e);
				terminal.disconnect();
				cancel();
				super.actionPerformed(e);
			}
		};

		terminatingPanel = new TerminatingPanel(closeListener);

		btn_close.setVisible(false); // in terminating panel, we place an extra button

		initGUI();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.centerOn(Globals.mainFrame);
		this.setSize(this.thissize);
		this.setMaximumSize(new Dimension(900, 700));
		// if (terminal != null) terminal.exec("bash\n");
		// btn_close.removeActionListener(closeListener);
		// btn_close.addActionListener(this.closeListener);
		setComponentsEnabled_RO(!Globals.isGlobalReadOnly());
		setCLfocus();
		// ((JTextField) tf_command).setCaretPosition(((JTextField)
		// tf_command).getText().length());
		logging.info(this, "SSHConnectionTerminalDialog build ");
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				logging.info(this, "SSHConnectionTerminalDialog  resized");
				super.componentResized(e);
				// Point loc = getLocationOnScreen();
				// loc.setLocation(loc.getX(), loc.getY() - 1);
				// setLocation(loc);//repairs vanishing of combobox popup on enlarging but has
				// the ugly effect of wandering
				// revalidate();
				// this.repaint();
				// setOutSize();
				setCLfocus();
				// append("", tf_command); // try to set scrollpane to end of textpane and focus
				// on tf_command
			}
		});
		setOutSize();
	}

	public SSHConnectionTerminalDialog(String title, SSHConnectTerminal terminal) {
		this(title, terminal, true);
	}

	public SSHConnectionTerminalDialog(String title, boolean visible) {
		this(title, null, visible);
	}

	public SSHConnectionTerminalDialog(String title) {
		this(title, true);
	}

	private void setComponentsEnabled_RO(boolean value) {
		logging.info(this, "setComponentsEnabled_RO value " + value);
		tf_command.setEnabled(value);
		btn_executeCommand.setEnabled(value);
		btn_killProcess.setEnabled(value);
	}

	private void setCLfocus() {
		logging.info(this, "setCLfocus");
		/*
		 * try
		 * {
		 * Thread.sleep(1000);
		 * }
		 * catch(Exception ex)
		 * {
		 * }
		 */
		tf_command.setCaretPosition(tf_command.getText().length());
		tf_command.requestFocus();

		// some thread seems to prevent setting it directly
		SwingUtilities.invokeLater(() -> {
			int timeoutRuns = 2;
			int counter = 0;
			while (!tf_command.hasFocus() && counter < timeoutRuns) {
				counter++;
				try {
					Thread.sleep(100);
				} catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				tf_command.requestFocus();
				logging.info(this, "repeated requestFocus " + counter + " times");
			}
		});

	}
	/*
	 * private void printWindowSize()
	 * {
	 * logging.debug("this.getsize " + this.getSize());
	 * logging.debug("this.getwidth / getHeight " + this.getWidth() + " " +
	 * this.getHeight());
	 * logging.debug("output.getwidth / getHeight " + this.output.getWidth() + " " +
	 * this.output.getHeight());
	 * logging.debug("jScrollPane.getwidth / getHeight " +
	 * this.jScrollPane.getWidth() + " " + this.jScrollPane.getHeight());
	 * }
	 */

	private void setOutSize() {
		// printWindowSize();
		double no_output_Height = (Globals.GAP_SIZE * 4) + tf_command.getHeight() + parameterPanel.getHeight()
				+ btn_dim.getHeight() + terminatingPanel.getHeight();

		this.thissize = this.getSize();
		// double w = 900 - (Globals.gapSize*4);
		// double h = 700 - no_output_Height;
		double w = this.getSize().getWidth() - (Globals.GAP_SIZE * 4);
		double h = this.getSize().getHeight() - no_output_Height;
		if (w > 1500)
			w = 810;
		if (h > 1500)
			h = 600;
		Dimension output_size = new Dimension();
		output_size.setSize(w, h);

		this.output.setSize(output_size);
		this.output.setPreferredSize(output_size);
		this.output.setMaximumSize(output_size);
		this.jScrollPane.setSize(output_size);
		this.jScrollPane.setPreferredSize(output_size);
		// this.jScrollPane.setMaximumSize(output_size);
		this.revalidate();
		// this.repaint();
		// printWindowSize();
	}

	public JTextField getInputField() {
		if (tf_command == null)
			return null;
		return tf_command;
	}

	public boolean getPrivateStatus() {
		return passwordMode;
	}

	public void setPrivate(boolean pr) {
		logging.info(this, "setPrivate " + pr);
		if (pr)
			changeEchoChar('*');
		else
			changeEchoChar((char) 0);
	}

	public void setLastHistoryIndex() {
		if (commandHistory.size() > 0)
			historyGetIndex = commandHistory.size();
	}

	public void addToHistory(String co) {
		if ((co != null) && (!co.trim().equals(""))) {
			logging.debug(this,
					"addToHistory \"" + co + "\" at index " + historyAddIndex + " getIndex " + (historyAddIndex + 1));
			commandHistory.add(historyAddIndex, co);
			historyAddIndex = historyAddIndex + 1;
			historyGetIndex = historyAddIndex;
		}
	}

	public String getPrevCommand_up() {
		logging.debug(this, "getPrevCommand_up historySize " + commandHistory.size() + " getIndex " + historyGetIndex);
		if (commandHistory.size() <= 0)
			return "";
		if (historyGetIndex - 1 < 0) {
			historyGetIndex = 0;
			if (commandHistory.get(historyGetIndex) != null)
				return commandHistory.get(historyGetIndex);
			else
				return "";
		}
		historyGetIndex = historyGetIndex - 1;
		return commandHistory.get(historyGetIndex);
	}

	public String getPrevCommand_down() {
		logging.debug(this,
				"getPrevCommand_down historySize " + commandHistory.size() + " getIndex " + historyGetIndex);
		if ((historyGetIndex + 1) >= commandHistory.size()) {
			historyGetIndex = commandHistory.size();
			return "";
		}
		historyGetIndex = historyGetIndex + 1;
		return commandHistory.get(historyGetIndex);
	}

	public JTextPane getOutputField() {
		if (output == null)
			return null;
		return output;
	}

	private void initGUI() {
		logging.info(this, "initGUI ");
		tf_command = new JPasswordField() {
			@Override
			public void addNotify() {
				super.addNotify();
				requestFocusInWindow();
				setCLfocus();
			}
		};
		tf_command.setPreferredSize(new Dimension(Globals.FIRST_LABEL_WIDTH + Globals.GAP_SIZE, Globals.LINE_HEIGHT));

		setCLfocus();
		// ((JTextField) tf_command).setCaretPosition(((JTextField)
		// tf_command).getText().length());

		cb_privat = new JCheckBox(configed.getResourceValue("SSHConnection.passwordButtonText"));
		cb_privat.setPreferredSize(btn_dim);
		if (!(Globals.isGlobalReadOnly()))
			cb_privat.addItemListener(itemEvent -> {
				if (passwordMode) {
					changeEchoChar('*');
					removeAutocompleteListener();
					passwordMode = false;
				} else {
					changeEchoChar((char) 0);
					if (terminal.commands_compgen != null)
						setAutocompleteList(terminal.commands_compgen);
					passwordMode = true;
				}
				setCLfocus();
			});
		changeEchoChar((char) 0);
		passwordMode = true;
		final SSHConnectionTerminalDialog caller = this;
		if (!(Globals.isGlobalReadOnly()))
			((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonTest().addActionListener(
					actionEvent -> ((SSHCommandControlParameterMethodsPanel) parameterPanel).doActionTestParam(caller));

		if (!(Globals.isGlobalReadOnly()))
			((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonAdd()
					.addActionListener(actionEvent -> ((SSHCommandControlParameterMethodsPanel) parameterPanel)
							.doActionParamAdd(tf_command));
		btn_killProcess = new de.uib.configed.gui.IconButton(
				de.uib.configed.configed.getResourceValue("SSHConnection.buttonKillProcess"), "images/edit-delete.png",
				"images/edit-delete.png", "images/edit-delete.png", true);
		btn_killProcess.setPreferredSize(btn_dim);
		btn_killProcess.setToolTipText(configed.getResourceValue("SSHConnection.buttonKillProcess"));

		btn_executeCommand = new de.uib.configed.gui.IconButton(
				de.uib.configed.configed.getResourceValue("SSHConnection.CommandControl.btnExecuteCommand"),
				"images/execute_blue.png", "images/execute_blue.png", "images/execute_blue.png", true);
		btn_executeCommand.setPreferredSize(btn_dim);
		if (!(Globals.isGlobalReadOnly()))
			btn_executeCommand.addActionListener(actionEvent -> {
				String text = getInputField().getText() + "\n";
				if (terminal != null) {
					terminal.exec(text);
					getInputField().setText("");
					setCLfocus();
				}
			});

		try {
			createLayout();
			setCLfocus();
		} catch (java.lang.NullPointerException npe) {
			logging.error("NullPointerException in createLayout ");
			logging.error("looks like a thread problem");
			logging.error("" + npe);
		} catch (Exception e) {
			logging.error("Exception in createLayout ");
			logging.error("looks like a thread problem");
			logging.error("" + e);
		}
		// this.setSize(this.getWidth(), this.getHeight() + parameterPanel.getHeight()
		// );
		setCenterLayout();
	}

	public void setAutocompleteList(java.util.List<String> list) {
		if (list == null)
			return;
		final String COMMIT_ACTION = "commit";
		// Without this, cursor always leaves text field
		tf_command.setFocusTraversalKeysEnabled(false);
		if (autoComplete != null)
			tf_command.getDocument().removeDocumentListener(autoComplete);

		autoComplete = new Autocomplete(tf_command, list);
		tf_command.getDocument().addDocumentListener(autoComplete);

		// Maps the tab key to the commit action, which finishes the autocomplete
		// when given a suggestion
		tf_command.getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
		tf_command.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());

	}

	public void removeAutocompleteListener() {
		if (autoComplete != null)
			tf_command.getDocument().removeDocumentListener(autoComplete);
	}

	protected void createLayout() {
		logging.info(this, "createLayout ");
		jScrollPane.setPreferredSize(output.getMaximumSize());

		int gap = Globals.GAP_SIZE;

		lbl_command = new JLabel(configed.getResourceValue("SSHConnection.commandLine"));

		konsolePanelLayout.setVerticalGroup(konsolePanelLayout.createSequentialGroup().addGap(gap)
				.addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(gap)
				.addGroup(konsolePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(lbl_command, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_privat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(tf_command, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_executeCommand, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_killProcess, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(btn_close, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(gap));

		konsolePanelLayout.setHorizontalGroup(konsolePanelLayout.createParallelGroup()
				.addGroup(konsolePanelLayout.createSequentialGroup().addGap(gap)
						.addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(gap))
				.addGroup(konsolePanelLayout.createSequentialGroup().addGap(gap)
						.addComponent(lbl_command, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(cb_privat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(gap)
						.addComponent(tf_command, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(gap)
						.addComponent(btn_executeCommand, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addComponent(btn_killProcess, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addComponent(btn_close, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addGap(gap)));
		setCenterLayout();

	}

	private void setCenterLayout() {
		mainPanelLayout.setAutoCreateGaps(true);
		mainPanelLayout.setHorizontalGroup(mainPanelLayout.createParallelGroup().addComponent(this.inputPanel)
				.addComponent(this.parameterPanel).addComponent(this.terminatingPanel)

		);
		mainPanelLayout.setVerticalGroup(
				mainPanelLayout.createSequentialGroup().addComponent(this.inputPanel).addComponent(this.parameterPanel)
						// .addGap(50)
						.addComponent(this.terminatingPanel));

		this.setSize(this.getWidth(),
				this.getHeight() + this.parameterPanel.getHeight() + this.terminatingPanel.getHeight());
		this.revalidate();
	}

	public void changeEchoChar(char c) {
		// if (passwordMode)
		logging.debug(this, "changeEchoChar char " + c);
		((JPasswordField) tf_command).setEchoChar(c);
		logging.debug(this, "changeEchoChar checkbox set Selected " + passwordMode);
		cb_privat.setSelected(passwordMode);
	}
}
