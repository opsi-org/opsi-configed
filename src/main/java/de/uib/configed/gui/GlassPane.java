/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXBusyLabel;

import com.formdev.flatlaf.FlatLaf;

import de.uib.Main;
import de.uib.configed.Globals;

public class GlassPane extends JPanel implements KeyListener {

	private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

	private JXBusyLabel wheel;
	private JLabel jLabelInfo;

	public GlassPane() {
		super.setOpaque(false);
		super.setLayout(new GridBagLayout());
		super.setBackground(initBackground());
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
		Color base;

		if (Main.THEMES && FlatLaf.isLafDark()) {
			base = Color.BLACK;
		} else {
			base = Color.WHITE;
		}

		return new Color(base.getRed(), base.getGreen(), base.getBlue(), 128);
	}

	private void initLayout() {
		wheel = new JXBusyLabel();
		jLabelInfo = new JLabel();

		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(wheel)
				.addGap(Globals.GAP_SIZE).addComponent(jLabelInfo).addGap(0, 0, Short.MAX_VALUE));

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(wheel).addGap(0, 0,
						Short.MAX_VALUE))
				.addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(jLabelInfo)
						.addGap(0, 0, Short.MAX_VALUE)));
	}

	public JLabel getJLabelInfo() {
		return jLabelInfo;
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
	 *  Make the glass pane visible, start the wheel and change the cursor to the wait cursor
	 */
	public void activate(boolean toggle) {

		wheel.setVisible(toggle);
		wheel.setBusy(toggle);
		setVisible(toggle);
		setCursor(getCursor());
		if (isVisible()) {
			requestFocusInWindow();
		}
	}

	@Override
	public Cursor getCursor() {
		return this.isVisible() ? GlassPane.WAIT_CURSOR : null;
	}

	/*
	*  Implement the KeyListener to consume events
	*/
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
