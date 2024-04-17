/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utils.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Globals;
import de.uib.utils.logging.Logging;

//a class similar to JCheckBox
public class CheckedLabel extends JPanel {
	private static final int SET_CHECKED_ON = 1;
	private static final int SET_CHECKED_OFF = 0;
	private static final String CMD_SET_CHECKED_ON = "cmdSetOn";
	private static final String CMD_SET_CHECKED_OFF = "cmdSetOff";
	private boolean changeStateAutonomously = true;

	private JLabel selectedLabel;
	private JLabel unselectedLabel;
	private JLabel nullLabel;
	private JLabel textLabel;

	private Boolean selected;

	private List<ActionListener> myListeners;

	private static class GeneralMouseListener extends MouseAdapter {
		private String source;

		public GeneralMouseListener(String source) {
			this.source = source;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			Logging.info(this, "CheckLabel mouseClicked on " + source);
		}
	}

	public CheckedLabel(Icon selectedIcon, Icon unselectedIcon, boolean selected) {
		this("", selectedIcon, unselectedIcon, (Icon) null, selected);
	}

	public CheckedLabel(String text, Icon selectedIcon, Icon unselectedIcon, Icon nullIcon, boolean selected) {
		super();

		super.setFocusable(true);
		super.setRequestFocusEnabled(true);

		myListeners = new ArrayList<>();

		textLabel = new JLabel(text);

		selectedLabel = new JLabel(selectedIcon);
		unselectedLabel = new JLabel(unselectedIcon);
		nullLabel = new JLabel(nullIcon);

		this.selected = selected;

		setLayout();

		addInternalListeners();
	}

	public void setChangeStateAutonomously(boolean b) {
		changeStateAutonomously = b;
	}

	private void addInternalListeners() {
		final CheckedLabel me = this;
		textLabel.addMouseListener(new GeneralMouseListener("textLabel"));
		selectedLabel.addMouseListener(new GeneralMouseListener("selectedLabel") {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEnabled()) {
					return;
				}

				super.mouseClicked(e);
				if (changeStateAutonomously) {
					me.setSelected(false);
				}

				notifyActionListeners(
						new ActionEvent(me, SET_CHECKED_OFF, CMD_SET_CHECKED_OFF, System.currentTimeMillis(), 0));
			}
		});

		unselectedLabel.addMouseListener(new GeneralMouseListener("unselectedLabel") {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEnabled()) {
					return;
				}

				super.mouseClicked(e);
				if (changeStateAutonomously) {
					me.setSelected(true);
				}

				notifyActionListeners(
						new ActionEvent(me, SET_CHECKED_ON, CMD_SET_CHECKED_ON, System.currentTimeMillis(), 0));
			}
		});

		nullLabel.addMouseListener(new GeneralMouseListener("nullLabel") {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isEnabled()) {
					return;
				}

				super.mouseClicked(e);
				if (changeStateAutonomously) {
					me.setSelected(false);
				}

				notifyActionListeners(
						new ActionEvent(me, SET_CHECKED_OFF, CMD_SET_CHECKED_OFF, System.currentTimeMillis(), 0));
			}
		});

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				Logging.info(this, "event " + e);
				if (!isEnabled()) {
					return;
				}

				super.keyPressed(e);
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					me.setSelected(selected);
					notifyActionListeners(
							new ActionEvent(me, SET_CHECKED_OFF, CMD_SET_CHECKED_OFF, System.currentTimeMillis(), 0));
				}
			}
		});
	}

	public void setText(String s) {
		textLabel.setText(s);
	}

	@Override
	public void setToolTipText(String s) {
		super.setToolTipText(s);
		nullLabel.setToolTipText(s);
		selectedLabel.setToolTipText(s);
		unselectedLabel.setToolTipText(s);
		textLabel.setToolTipText(s);
	}

	private void setLayout() {
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setVerticalGroup(layout.createParallelGroup()
				.addComponent(textLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(selectedLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(unselectedLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(nullLabel, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT));

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(textLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(selectedLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(unselectedLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(nullLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));

		setSelected(selected);
	}

	public void setSelected(Boolean b) {
		nullLabel.setVisible(b == null);
		selectedLabel.setVisible(Boolean.TRUE.equals(b));
		unselectedLabel.setVisible(Boolean.FALSE.equals(b));
		selected = b;
	}

	public Boolean isSelected() {
		return selected;
	}

	public void disableSelection() {
		super.setEnabled(false);
	}

	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		textLabel.setEnabled(b);
	}

	public void addActionListener(ActionListener al) {
		myListeners.add(al);
	}

	public void removeAllActionListeners() {
		myListeners.clear();
	}

	public void notifyActionListeners(ActionEvent ae) {
		for (ActionListener al : myListeners) {
			al.actionPerformed(ae);
		}
	}
}
