/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.ssh;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.Autocomplete;
import de.uib.configed.gui.IconButton;
import de.uib.opsicommand.sshcommand.SSHConnectTerminal;
import de.uib.utilities.logging.Logging;

public class SSHConnectionTerminalDialog extends SSHConnectionOutputDialog {
	private JTextField jTextFieldCommand;
	private JCheckBox jComboBoxPrivat;
	private JPanel parameterPanel;
	private JPanel terminatingPanel;
	private JButton jButtonKillProcess;
	private JButton jButtonExecuteCommand;
	private final SSHConnectTerminal terminal;
	private List<String> commandHistory = new ArrayList<>();
	private int historyAddIndex;
	private int historyGetIndex;
	private Autocomplete autoComplete;
	private boolean passwordMode;
	private Dimension jButtonDimension = new Dimension(Globals.GRAPHIC_BUTTON_WIDTH_X, Globals.BUTTON_HEIGHT);
	private Dimension thissize = new Dimension(810, 600);

	private class TerminatingPanel extends JPanel {
		JButton jButtonClose;

		TerminatingPanel(ActionListener closeListener) {
			super();

			if (!Main.THEMES) {
				super.setBackground(Globals.BACKGROUND_COLOR_7);
			}
			jButtonClose = new IconButton(Configed.getResourceValue("SSHConnection.buttonClose"), "images/cancel.png",
					"images/cancel.png", "images/cancel.png", true);
			jButtonClose.addActionListener(closeListener);

			jButtonClose.setPreferredSize(jButtonDimension);

			GroupLayout layout = new GroupLayout(this);
			super.setLayout(layout);

			layout.setVerticalGroup(layout
					.createSequentialGroup().addGap(Globals.VGAP_SIZE).addComponent(jButtonClose,
							GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(3 * Globals.VGAP_SIZE));

			layout.setHorizontalGroup(
					layout.createSequentialGroup().addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
							.addComponent(jButtonClose, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
							.addGap(Globals.GAP_SIZE));

		}
	}

	public SSHConnectionTerminalDialog(String title, SSHConnectTerminal terminal) {
		super(title);

		// THEME color question
		if (!Main.THEMES) {
			output.setBackground(Globals.lightBlack);
		}
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
				Logging.debug(this, "actionPerformed " + e);
				terminal.disconnect();
				cancel();
				super.actionPerformed(e);
			}
		};

		terminatingPanel = new TerminatingPanel(closeListener);

		// in terminating panel, we place an extra button
		jButtonClose.setVisible(false);

		initGUI();
		super.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		super.setLocationRelativeTo(ConfigedMain.getMainFrame());
		super.setSize(this.thissize);
		super.setMaximumSize(new Dimension(900, 700));

		setComponentsEnabledRO(!Globals.isGlobalReadOnly());
		setCLfocus();
		// ((JTextField) tf_command).setCaretPosition(((JTextField)

		Logging.info(this, "SSHConnectionTerminalDialog build ");
		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Logging.info(this, "SSHConnectionTerminalDialog  resized");
				super.componentResized(e);

				// the ugly effect of wandering

				setCLfocus();
			}
		});
		setOutSize();
	}

	public SSHConnectionTerminalDialog(String title) {
		this(title, null);
	}

	private void setComponentsEnabledRO(boolean value) {
		Logging.info(this, "setComponentsEnabledRO value " + value);
		jTextFieldCommand.setEnabled(value);
		jButtonExecuteCommand.setEnabled(value);
		jButtonKillProcess.setEnabled(value);
	}

	private void setCLfocus() {
		Logging.info(this, "setCLfocus");

		jTextFieldCommand.setCaretPosition(jTextFieldCommand.getText().length());
		jTextFieldCommand.requestFocus();

		// some thread seems to prevent setting it directly
		SwingUtilities.invokeLater(() -> {
			int timeoutRuns = 2;
			int counter = 0;
			while (!jTextFieldCommand.hasFocus() && counter < timeoutRuns) {
				counter++;
				Globals.threadSleep(this, 100);
				jTextFieldCommand.requestFocus();
				Logging.info(this, "repeated requestFocus " + counter + " times");
			}
		});
	}

	private void setOutSize() {

		double noOutputHeight = Globals.GAP_SIZE * 4 + jTextFieldCommand.getHeight() + parameterPanel.getHeight()
				+ jButtonDimension.getHeight() + terminatingPanel.getHeight();

		this.thissize = this.getSize();

		double w = this.getSize().getWidth() - (Globals.GAP_SIZE * 4);
		double h = this.getSize().getHeight() - noOutputHeight;
		if (w > 1500) {
			w = 810;
		}

		if (h > 1500) {
			h = 600;
		}

		Dimension outputSize = new Dimension();
		outputSize.setSize(w, h);

		this.output.setSize(outputSize);
		this.output.setPreferredSize(outputSize);
		this.output.setMaximumSize(outputSize);
		this.jScrollPane.setSize(outputSize);
		this.jScrollPane.setPreferredSize(outputSize);

		this.revalidate();

	}

	public void renewJButtonKillActionListener(ActionListener connectionKeyListener) {
		jButtonKillProcess.removeActionListener(connectionKeyListener);
		jButtonKillProcess.addActionListener(connectionKeyListener);
	}

	public JTextField getInputField() {
		if (jTextFieldCommand == null) {
			return null;
		}

		return jTextFieldCommand;
	}

	public boolean hasPrivateStatus() {
		return passwordMode;
	}

	public void setPrivate(boolean pr) {
		Logging.info(this, "setPrivate " + pr);
		if (pr) {
			changeEchoChar('*');
		} else {
			changeEchoChar((char) 0);
		}
	}

	public void setLastHistoryIndex() {
		if (!commandHistory.isEmpty()) {
			historyGetIndex = commandHistory.size();
		}
	}

	public void addToHistory(String co) {
		if (co != null && !co.trim().isEmpty()) {
			Logging.debug(this,
					"addToHistory \"" + co + "\" at index " + historyAddIndex + " getIndex " + (historyAddIndex + 1));
			commandHistory.add(historyAddIndex, co);
			historyAddIndex = historyAddIndex + 1;
			historyGetIndex = historyAddIndex;
		}
	}

	public String getPrevCommandUp() {
		Logging.debug(this, "getPrevCommand_up historySize " + commandHistory.size() + " getIndex " + historyGetIndex);
		if (commandHistory.isEmpty()) {
			return "";
		}

		if (historyGetIndex - 1 < 0) {
			historyGetIndex = 0;
			if (commandHistory.get(historyGetIndex) != null) {
				return commandHistory.get(historyGetIndex);
			} else {
				return "";
			}
		}
		historyGetIndex = historyGetIndex - 1;
		return commandHistory.get(historyGetIndex);
	}

	public String getPrevCommandDown() {
		Logging.debug(this,
				"getPrevCommand_down historySize " + commandHistory.size() + " getIndex " + historyGetIndex);
		if ((historyGetIndex + 1) >= commandHistory.size()) {
			historyGetIndex = commandHistory.size();
			return "";
		}
		historyGetIndex = historyGetIndex + 1;
		return commandHistory.get(historyGetIndex);
	}

	public JTextPane getOutputField() {
		if (output == null) {
			return null;
		}

		return output;
	}

	private void initGUI() {
		Logging.info(this, "initGUI ");
		jTextFieldCommand = new JPasswordField() {
			@Override
			public void addNotify() {
				super.addNotify();
				requestFocusInWindow();
				setCLfocus();
			}
		};

		jTextFieldCommand
				.setPreferredSize(new Dimension(Globals.FIRST_LABEL_WIDTH + Globals.GAP_SIZE, Globals.LINE_HEIGHT));

		setCLfocus();

		jComboBoxPrivat = new JCheckBox(Configed.getResourceValue("SSHConnection.passwordButtonText"));
		jComboBoxPrivat.setPreferredSize(jButtonDimension);
		if (!(Globals.isGlobalReadOnly())) {
			jComboBoxPrivat.addItemListener((ItemEvent itemEvent) -> {
				if (passwordMode) {
					changeEchoChar('*');
					removeAutocompleteListener();
					passwordMode = false;
				} else {
					changeEchoChar((char) 0);
					if (terminal.commandsCompgen != null) {
						setAutocompleteList(terminal.commandsCompgen);
					}
					passwordMode = true;
				}
				setCLfocus();
			});
		}

		changeEchoChar((char) 0);
		passwordMode = true;
		final SSHConnectionTerminalDialog caller = this;
		if (!(Globals.isGlobalReadOnly())) {
			((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonTest().addActionListener(
					actionEvent -> ((SSHCommandControlParameterMethodsPanel) parameterPanel).doActionTestParam(caller));
		}

		if (!(Globals.isGlobalReadOnly())) {
			((SSHCommandControlParameterMethodsPanel) parameterPanel).getButtonAdd()
					.addActionListener(actionEvent -> ((SSHCommandControlParameterMethodsPanel) parameterPanel)
							.doActionParamAdd(jTextFieldCommand));
		}

		jButtonKillProcess = new IconButton(Configed.getResourceValue("SSHConnection.buttonKillProcess"),
				"images/edit-delete.png", "images/edit-delete.png", "images/edit-delete.png", true);
		jButtonKillProcess.setPreferredSize(jButtonDimension);
		jButtonKillProcess.setToolTipText(Configed.getResourceValue("SSHConnection.buttonKillProcess"));

		jButtonExecuteCommand = new IconButton(
				Configed.getResourceValue("SSHConnection.CommandControl.btnExecuteCommand"), "images/execute_blue.png",
				"images/execute_blue.png", "images/execute_blue.png", true);
		jButtonExecuteCommand.setPreferredSize(jButtonDimension);
		if (!(Globals.isGlobalReadOnly())) {
			jButtonExecuteCommand.addActionListener((ActionEvent actionEvent) -> {
				String text = getInputField().getText() + "\n";
				if (terminal != null) {
					terminal.exec(text);
					getInputField().setText("");
					setCLfocus();
				}
			});
		}

		createLayout();
		setCLfocus();

		setCenterLayout();
	}

	public void setAutocompleteList(List<String> list) {
		if (list.isEmpty()) {
			return;
		}

		final String COMMIT_ACTION = "commit";
		// Without this, cursor always leaves text field
		jTextFieldCommand.setFocusTraversalKeysEnabled(false);
		if (autoComplete != null) {
			jTextFieldCommand.getDocument().removeDocumentListener(autoComplete);
		}

		autoComplete = new Autocomplete(jTextFieldCommand, list);
		jTextFieldCommand.getDocument().addDocumentListener(autoComplete);

		// Maps the tab key to the commit action, which finishes the autocomplete
		// when given a suggestion
		jTextFieldCommand.getInputMap().put(KeyStroke.getKeyStroke("TAB"), COMMIT_ACTION);
		jTextFieldCommand.getActionMap().put(COMMIT_ACTION, autoComplete.new CommitAction());

	}

	public void removeAutocompleteListener() {
		if (autoComplete != null) {
			jTextFieldCommand.getDocument().removeDocumentListener(autoComplete);
		}
	}

	private void createLayout() {
		Logging.info(this, "createLayout ");
		jScrollPane.setPreferredSize(output.getMaximumSize());

		int gap = Globals.GAP_SIZE;

		JLabel jLabelCommand = new JLabel(Configed.getResourceValue("SSHConnection.commandLine"));

		konsolePanelLayout.setVerticalGroup(konsolePanelLayout.createSequentialGroup().addGap(gap)
				.addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(gap)
				.addGroup(konsolePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelCommand, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxPrivat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jTextFieldCommand, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonExecuteCommand, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonKillProcess, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonClose, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(gap));

		konsolePanelLayout.setHorizontalGroup(konsolePanelLayout.createParallelGroup()
				.addGroup(konsolePanelLayout.createSequentialGroup().addGap(gap)
						.addComponent(jScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								Short.MAX_VALUE)
						.addGap(gap))
				.addGroup(konsolePanelLayout.createSequentialGroup().addGap(gap)
						.addComponent(jLabelCommand, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jComboBoxPrivat, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(gap)
						.addComponent(jTextFieldCommand, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH, Short.MAX_VALUE)
						.addGap(gap)
						.addComponent(jButtonExecuteCommand, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addComponent(jButtonKillProcess, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
						.addComponent(jButtonClose, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
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

						.addComponent(this.terminatingPanel));

		this.setSize(this.getWidth(),
				this.getHeight() + this.parameterPanel.getHeight() + this.terminatingPanel.getHeight());
		this.revalidate();
	}

	public void changeEchoChar(char c) {

		Logging.debug(this, "changeEchoChar char " + c);
		((JPasswordField) jTextFieldCommand).setEchoChar(c);
		Logging.debug(this, "changeEchoChar checkbox set Selected " + passwordMode);
		jComboBoxPrivat.setSelected(passwordMode);
	}
}
