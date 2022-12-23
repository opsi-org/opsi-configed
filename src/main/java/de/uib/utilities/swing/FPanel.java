/*
 * FPanel.java
 *
 * By uib, www.uib.de, 2018
 * Author: Rupert Röder
 * 
 */

package de.uib.utilities.swing;

import java.awt.Dimension;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.logging.logging;
import de.uib.utilities.observer.RunningInstances;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class FPanel extends SecondaryFrame {
	public static RunningInstances<JFrame> runningInstances = new RunningInstances(JFrame.class, "edit panel dialog");

	JPanel innerPanel;
	boolean checkLeave;
	private boolean left;

	public FPanel(String title, JPanel panel, boolean checkLeave) {
		this(title, panel, checkLeave, 300, 300);
	}

	public FPanel(String title, JPanel panel, boolean checkLeave, int initialWidth, int initialHeight) {
		super();
		this.checkLeave = checkLeave;
		setIconImage(Globals.mainIcon);
		setTitle(Globals.APPNAME + " " + title);
		
		setSize(new Dimension(initialWidth, initialHeight));
		innerPanel = panel;
		getContentPane().add(innerPanel);
		centerOnParent();

		setVisible(true);

	}

	protected boolean wantToBeRegisteredWithRunningInstances() {
		return true;
	}

	protected void registerWithRunningInstances() {
		logging.info(this, "registerWithRunningInstances");
		if (wantToBeRegisteredWithRunningInstances())
			FPanel.runningInstances.add(this, "");
	}

	@Override
	public void setVisible(boolean b) {

		if (b)
			runningInstances.add(this, "");
		else
			runningInstances.forget(this);

		super.setVisible(b);
	}

	protected boolean leaveChecked() {
		if (!checkLeave)
			return true;

		boolean result = false;

		if (innerPanel instanceof PanelGenEditTable) {
			PanelGenEditTable editPanel = (PanelGenEditTable) innerPanel;

			if (editPanel.isDataChanged()) {
				int returnedOption = JOptionPane.CANCEL_OPTION;
				returnedOption = JOptionPane.showOptionDialog(masterFrame,
						configed.getResourceValue("ConfigedMain.saveBeforeCloseText"),
						Globals.APPNAME + " " + configed.getResourceValue("ConfigedMain.saveBeforeCloseTitle"),
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

				switch (returnedOption) {
				case JOptionPane.YES_OPTION:
					editPanel.commit();
					result = true;
					break;
				case JOptionPane.NO_OPTION:
					editPanel.cancel();
					result = true;
					break;
				case JOptionPane.CANCEL_OPTION:
					break;
				}
			}

			else
				result = true;
		} else
			result = true;

		logging.info(this, "--------leaveChecked " + result);
		return result;

	}

	public void leave() {
		logging.info(this, "leave ");
		setVisible(false);
		dispose();
		left = true;
	}

	public boolean isLeft() {
		return left;
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {

		// logging.info(this, " FPanel --------processWindowEvent " + e);
		// super.processWindowEvent(e);

		boolean leaving = true;

		if ((e.getID() == WindowEvent.WINDOW_CLOSING)
		// ||
		// (e.getID() == WindowEvent.WINDOW_DEACTIVATED)
		// ||
		// (e.getID() == WindowEvent.WINDOW_CLOSED)
		) {
			leaving = leaveChecked();
			if (leaving) {

				leave();

				// super.processWindowEvent(e);
			} else
				setVisible(true);
		}

	}

	public static void main(String[] args) {

		JPanel testpanel = new JPanel();
		testpanel.add(new JLabel("hallo 1 "));

		FPanel testF = new FPanel("hardware classes / database columns", testpanel, true);
		testF.setVisible(true);
	}

}
