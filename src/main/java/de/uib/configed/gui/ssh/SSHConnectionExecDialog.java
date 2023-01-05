package de.uib.configed.gui.ssh;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnect;
import de.uib.utilities.logging.logging;

public class SSHConnectionExecDialog extends SSHConnectionOutputDialog {
	protected JButton jButtonClear;
	protected JButton jButtonKillProcess;
	protected JButton jButtonReload;

	protected boolean withReload;
	protected String reloadInfo;
	private int infolength = 40;

	private static SSHConnectionExecDialog instance;

	private SSHConnectionExecDialog() {
		super(configed.getResourceValue("SSHConnection.Exec.dialog.commandoutput"));
		if (!SSHConnect.isConnectionAllowed()) {
			logging.warning(this, "Connection forbidden. Close exec dialog.");
			this.cancel();
		} else {
			buildFrame = false;
			initGUI();

			this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

			this.setSize(900, 500);
			this.setLocationRelativeTo(Globals.mainFrame);
			logging.info(this, "SSHConnectionExecDialog built");

			this.setVisible(!SSHCommandFactory.ssh_always_exec_in_background);
			buildFrame = true;
		}
	}

	public static SSHConnectionExecDialog getInstance() {
		logging.info("SSHConnectionExecDialog.getInstance, existing " + instance);

		if (instance == null) {
			instance = getNewInstance();
		}
		return instance;

	}

	public static SSHConnectionExecDialog getNewInstance() {

		if (instance != null) {
			instance.leave();
			instance = null;
		}

		instance = new SSHConnectionExecDialog();
		SwingUtilities.invokeLater(() -> {
			instance.setLocationRelativeTo(Globals.mainFrame);
			instance.setVisible(true);
		});
		return instance;
	}

	private void initGUI() {
		try {
			jButtonKillProcess = new de.uib.configed.gui.IconButton(
					configed.getResourceValue("SSHConnection.buttonKillProcess"), "images/edit-delete.png",
					"images/edit-delete.png", "images/edit-delete.png", true);
			jButtonKillProcess
					.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT + 3));
			jButtonKillProcess.setToolTipText(configed.getResourceValue("SSHConnection.buttonKillProcess"));

			jButtonClear = new de.uib.configed.gui.IconButton(configed.getResourceValue("SSHConnection.btn_clear"),
					"images/user-trash.png", "images/user-trash.png", "images/user-trash.png", true);
			jButtonClear.setPreferredSize(new Dimension(Globals.GRAPHIC_BUTTON_WIDTH + 15, Globals.BUTTON_HEIGHT + 3));
			jButtonClear.setToolTipText(configed.getResourceValue("SSHConnection.btn_clear"));
			jButtonClear.addActionListener(actionEvent -> clear());
			createLayout(konsolePanelLayout, jScrollPane, Globals.GAP_SIZE, Globals.GAP_SIZE, true);
			createLayout(mainPanelLayout, inputPanel, 0, 0, false);
		} catch (Exception e) {
			logging.warning(this, "initGui, exception occurred ", e);
		}
	}

	public boolean showResult = true;

	@Override
	public void setStatusFinish() {
		if (showResult)
			setVisible(true);
		else
			cancel();
	}

	public void addKillProcessListener(ActionListener l) {
		jButtonKillProcess.addActionListener(l);
	}

	public void removeKillProcessListener(ActionListener l) {
		jButtonKillProcess.removeActionListener(l);
	}

	private void createLayout(GroupLayout layout, Component comp, int vgap, int hgap, boolean addInputField) {
		int pref = Globals.BUTTON_HEIGHT;
		int max = Short.MAX_VALUE;
		layout.setAutoCreateGaps(true);
		SequentialGroup verticalGroup = layout.createSequentialGroup();

		verticalGroup.addGap(vgap).addComponent(comp);
		if (addInputField)
			verticalGroup.addGroup(layout.createParallelGroup().addGap(vgap, vgap, vgap)
					.addComponent(jButtonClear, pref, pref, pref).addComponent(jButtonKillProcess, pref, pref, pref)
					.addComponent(jButtonClose, pref, pref, pref).addGap(vgap));
		verticalGroup.addGap(vgap);

		ParallelGroup horizontalGroup = layout.createParallelGroup();
		horizontalGroup.addGroup(layout.createSequentialGroup().addGap(hgap).addComponent(comp).addGap(hgap));
		if (addInputField)
			horizontalGroup.addGroup(layout.createSequentialGroup().addGap(hgap, hgap, max)
					.addComponent(jButtonClear, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
					.addComponent(jButtonKillProcess, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
					.addComponent(jButtonClose, Globals.ICON_WIDTH, Globals.ICON_WIDTH, Globals.ICON_WIDTH)
					.addGap(hgap));

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);
	}

	public void clear() {
		output.setText("");
	}

	public void appendLater(String line) {

		append(line);

	}

	@Override
	public void append(String caller, String line) {
		logging.debug(this, "append " + line);
		int callerlength = caller.length();
		for (int i = callerlength; i <= infolength; i++)
			caller += " ";
		super.append(caller, line);
	}
}
