/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.Dimension;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.observer.RunningInstances;
import de.uib.utilities.table.gui.PanelGenEditTable;
import utils.Utils;

public class FPanel extends SecondaryFrame {
	public static final RunningInstances<JFrame> runningInstances = new RunningInstances<>(JFrame.class,
			"edit panel dialog");

	private JPanel innerPanel;
	private boolean checkLeave;
	private boolean left;

	public FPanel(String title, JPanel panel, boolean checkLeave) {
		super();
		this.checkLeave = checkLeave;
		super.setIconImage(Utils.getMainIcon());
		super.setTitle(Globals.APPNAME + " " + title);

		super.setSize(new Dimension(300, 300));
		innerPanel = panel;
		super.getContentPane().add(innerPanel);
		super.centerOnParent();

		super.setVisible(true);
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			runningInstances.add(this, "");
		} else {
			runningInstances.forget(this);
		}

		super.setVisible(b);
	}

	private boolean leaveChecked() {
		if (!checkLeave) {
			return true;
		}

		boolean result = false;

		if (innerPanel instanceof PanelGenEditTable) {
			PanelGenEditTable editPanel = (PanelGenEditTable) innerPanel;

			if (editPanel.isDataChanged()) {
				int returnedOption = JOptionPane.showOptionDialog(masterFrame,
						Configed.getResourceValue("ConfigedMain.saveBeforeCloseText"),
						Globals.APPNAME + " " + Configed.getResourceValue("ConfigedMain.saveBeforeCloseTitle"),
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
				default:
					Logging.warning(this, "no case found for returnedOption in leaveChecked");
					break;
				}
			} else {
				result = true;
			}
		} else {
			result = true;
		}

		Logging.info(this, "--------leaveChecked " + result);
		return result;
	}

	private void leave() {
		Logging.info(this, "leave ");
		setVisible(false);
		dispose();
		left = true;
	}

	public boolean isLeft() {
		return left;
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		boolean leaving = true;

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			leaving = leaveChecked();
			if (leaving) {
				leave();
			} else {
				setVisible(true);
			}
		}
	}
}
