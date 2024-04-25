/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.terminal;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import de.uib.configed.Configed;

public class TerminalMenuBar extends JMenuBar {
	private TerminalFrame frame;
	private boolean restrictView;

	public TerminalMenuBar(TerminalFrame frame, boolean restrictView) {
		this.frame = frame;
		this.restrictView = restrictView;
	}

	public void init() {
		add(createFileMenu());
		add(createViewMenu());
	}

	private JMenu createFileMenu() {
		JMenuItem jMenuItemNewWindow = new JMenuItem(
				Configed.getResourceValue("Terminal.menuBar.fileMenu.openNewWindow"));
		jMenuItemNewWindow.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		jMenuItemNewWindow.addActionListener((ActionEvent e) -> frame.openNewWindow());

		JMenuItem jMenuItemNewSession = new JMenuItem(
				Configed.getResourceValue("Terminal.menuBar.fileMenu.openNewSession"));
		jMenuItemNewSession.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		jMenuItemNewSession.addActionListener((ActionEvent e) -> frame.openNewSession());

		JMenuItem jMenuItemChangeSession = new JMenuItem(
				Configed.getResourceValue("Terminal.menuBar.fileMenu.changeSession"));
		jMenuItemChangeSession.setAccelerator(
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		jMenuItemChangeSession.addActionListener((ActionEvent e) -> frame.displaySessionsDialog());

		JMenu menuFile = new JMenu(Configed.getResourceValue("MainFrame.jMenuFile"));
		menuFile.setEnabled(!restrictView);
		menuFile.add(jMenuItemNewWindow);
		menuFile.add(jMenuItemNewSession);
		menuFile.add(jMenuItemChangeSession);
		return menuFile;
	}

	private JMenu createViewMenu() {
		JMenuItem jMenuViewFontsizePlus = new JMenuItem(Configed.getResourceValue("TextPane.fontPlus"));
		jMenuViewFontsizePlus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizePlus.addActionListener((ActionEvent e) -> {
			TerminalWidget widget = frame.getTabbedPane().getSelectedTerminalWidget();
			if (widget != null) {
				widget.increaseFontSize();
			}
		});

		JMenuItem jMenuViewFontsizeMinus = new JMenuItem(Configed.getResourceValue("TextPane.fontMinus"));
		jMenuViewFontsizeMinus.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		jMenuViewFontsizeMinus.addActionListener((ActionEvent e) -> {
			TerminalWidget widget = frame.getTabbedPane().getSelectedTerminalWidget();
			if (widget != null) {
				widget.decreaseFontSize();
			}
		});

		JMenu jMenuView = new JMenu(Configed.getResourceValue("LogFrame.jMenuView"));
		jMenuView.add(jMenuViewFontsizePlus);
		jMenuView.add(jMenuViewFontsizeMinus);
		return jMenuView;
	}
}
