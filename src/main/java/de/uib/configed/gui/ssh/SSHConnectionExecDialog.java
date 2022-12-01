package de.uib.configed.gui.ssh;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import de.uib.configed.configed;
import de.uib.opsicommand.sshcommand.SSHCommandFactory;
import de.uib.opsicommand.sshcommand.SSHConnect;
import de.uib.utilities.logging.logging;

public class SSHConnectionExecDialog extends SSHConnectionOutputDialog {
	protected JButton btn_clear;
	protected JButton btn_killProcess;
	protected JButton btn_reload;

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

			this.centerOn(de.uib.configed.Globals.mainFrame);
			this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			this.setSize(900, 500);
			logging.info(this, "SSHConnectionExecDialog built");
			// this.setVisible(true);
			if (SSHCommandFactory.getInstance().ssh_always_exec_in_background == true)
				this.setVisible(false);
			buildFrame = true;
		}
	}

	public static SSHConnectionExecDialog getInstance() {
		logging.info("SSHConnectionExecDialog.getInstance, existing " + instance);
		/*
		 * if (instance != null)
		 * {
		 * //giving back existing instance does not work, probably because of threading
		 * problems
		 * //SwingUtilities.invokeLater( new Runnable(){
		 * // public void run()
		 * // {
		 * instance.append( "SSHConnectionExecDialog " + new java.util.Date() + "\n" );
		 * // logging.info("SSHConnectionExecDialog.getInstance set it visible ");
		 * // instance.setVisible(true);
		 * // }
		 * //}
		 * //);
		 * }
		 * else
		 */
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
		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				instance.setVisible(true);
			}
		});
		return instance;
	}

	private void initGUI() {
		try {
			btn_killProcess = new de.uib.configed.gui.IconButton(
					de.uib.configed.configed.getResourceValue("SSHConnection.buttonKillProcess"),
					"images/edit-delete.png", "images/edit-delete.png", "images/edit-delete.png", true);
			btn_killProcess.setPreferredSize(new Dimension(de.uib.configed.Globals.graphicButtonWidth + 15,
					de.uib.configed.Globals.buttonHeight + 3));
			btn_killProcess.setToolTipText(configed.getResourceValue("SSHConnection.buttonKillProcess"));

			btn_clear = new de.uib.configed.gui.IconButton(
					de.uib.configed.configed.getResourceValue("SSHConnection.btn_clear"), "images/user-trash.png",
					"images/user-trash.png", "images/user-trash.png", true);
			btn_clear.setPreferredSize(new Dimension(de.uib.configed.Globals.graphicButtonWidth + 15,
					de.uib.configed.Globals.buttonHeight + 3));
			btn_clear.setToolTipText(configed.getResourceValue("SSHConnection.btn_clear"));
			btn_clear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clear();
				}
			});
			createLayout(konsolePanelLayout, jScrollPane, de.uib.configed.Globals.gapSize,
					de.uib.configed.Globals.gapSize, true);
			createLayout(mainPanelLayout, inputPanel, 0, 0, false);
		} catch (Exception e) {
			logging.warning(this, "initGui, exception occurred ", e);
		}
	}

	public boolean showResult = true;

	public void setStatusFinish() {
		if (showResult)
			setVisible(true);
		else
			cancel();
	}

	public void addKillProcessListener(ActionListener l) {
		btn_killProcess.addActionListener(l);
	}

	public void removeKillProcessListener(ActionListener l) {
		btn_killProcess.removeActionListener(l);
	}

	private void createLayout(GroupLayout layout, Component comp, int vgap, int hgap, boolean addInputField) {
		int pref = de.uib.configed.Globals.buttonHeight;
		int max = Short.MAX_VALUE;
		GroupLayout.Alignment leading = GroupLayout.Alignment.LEADING;
		layout.setAutoCreateGaps(true);
		SequentialGroup verticalGroup = layout.createSequentialGroup();

		verticalGroup.addGap(vgap).addComponent(comp);
		if (addInputField)
			verticalGroup.addGroup(layout.createParallelGroup().addGap(vgap, vgap, vgap)
					.addComponent(btn_clear, pref, pref, pref).addComponent(btn_killProcess, pref, pref, pref)
					.addComponent(btn_close, pref, pref, pref).addGap(vgap));
		verticalGroup.addGap(vgap);

		ParallelGroup horizontalGroup = layout.createParallelGroup();
		horizontalGroup.addGroup(layout.createSequentialGroup().addGap(hgap).addComponent(comp).addGap(hgap));
		if (addInputField)
			horizontalGroup.addGroup(layout.createSequentialGroup().addGap(hgap, hgap, max)
					.addComponent(btn_clear, de.uib.configed.Globals.iconWidth, de.uib.configed.Globals.iconWidth,
							de.uib.configed.Globals.iconWidth)
					.addComponent(btn_killProcess, de.uib.configed.Globals.iconWidth, de.uib.configed.Globals.iconWidth,
							de.uib.configed.Globals.iconWidth)
					.addComponent(btn_close, de.uib.configed.Globals.iconWidth, de.uib.configed.Globals.iconWidth,
							de.uib.configed.Globals.iconWidth)
					.addGap(hgap));

		layout.setVerticalGroup(verticalGroup);
		layout.setHorizontalGroup(horizontalGroup);
	}

	public void clear() {
		output.setText("");
	}

	public void appendLater(String line) {
		// logging.info(this, "appendLater " + line);

		append(line);

		/*
		 * SwingUtilities.invokeLater( new Thread(){
		 * public void run()
		 * {
		 * append( line );
		 * }
		 * }
		 * );
		 */

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
