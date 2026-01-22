/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.formdev.flatlaf.FlatLaf;

import net.miginfocom.swing.MigLayout;

public class GlassPane extends JComponent implements KeyListener {
	private JLabel jLabelInfo;

	public GlassPane() {
		super.setBackground(initBackground());
		super.setOpaque(false);

		initLayout();
		addEventCatchers();
	}

	private void addEventCatchers() {
		addMouseListener(new MouseAdapter() {
		});
		addMouseMotionListener(new MouseMotionAdapter() {
		});
		addKeyListener(this);
		setFocusTraversalKeysEnabled(false);
	}

	private static Color initBackground() {
		int brightness = FlatLaf.isLafDark() ? 0 : 255;

		return new Color(brightness, brightness, brightness, 128);
	}

	private void initLayout() {
		JProgressBar jLabelAnimation = new JProgressBar();
		jLabelAnimation.setIndeterminate(true);
		jLabelInfo = new JLabel();

		setLayout(new MigLayout("fill"));
		JPanel panel = new JPanel(new MigLayout("wrap 1, alignx center, aligny center, gap 0", "[center]", "[]0[]"));
		panel.setBackground(initBackground());
		panel.setOpaque(false);

		panel.add(jLabelAnimation, "w 150!, h 10!");
		panel.add(jLabelInfo);
		add(panel, "grow, center");
	}

	public void setInfoText(String s) {
		jLabelInfo.setText(s);
	}

	/*
	 *  The component is transparent but we want to paint the background
	 *  to give it the disabled look.
	 */
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getSize().width, getSize().height);
	}

	/*
	 *  Make the glass pane and wheel visible, and change the cursor to the wait cursor.
	 */
	public void activate(boolean toggle) {
		setVisible(toggle);
		setCursor(getCursor());
		if (isVisible()) {
			requestFocusInWindow();
		} else {
			setInfoText(null);
		}
	}

	public void activateLoadingCursor() {
		setCursor(Globals.WAIT_CURSOR);
	}

	public void deactivateLoadingCursor() {
		setCursor(null);
	}

	@Override
	public Cursor getCursor() {
		return this.isVisible() ? Globals.WAIT_CURSOR : null;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		e.consume();
	}

	@Override
	public void keyTyped(KeyEvent e) {
		/* Not needed */}
}
