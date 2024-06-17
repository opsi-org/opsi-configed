/**
 * Copyright (c) uib GmbH <info@uib.de>
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

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.Globals;

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
		Color base;

		if (FlatLaf.isLafDark()) {
			base = Color.BLACK;
		} else {
			base = Color.WHITE;
		}

		return new Color(base.getRed(), base.getGreen(), base.getBlue(), 128);
	}

	private void initLayout() {
		JProgressBar jLabelAnimation = new JProgressBar()
		jLabelAnimation.setIndeterminate(true);
		jLabelInfo = new JLabel();

		GroupLayout grouplayout = new GroupLayout(this);
		setLayout(grouplayout);

		grouplayout.setVerticalGroup(grouplayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
				.addComponent(jLabelAnimation,10,10,10).addComponent(jLabelInfo).addGap(0, 0, Short.MAX_VALUE));

		grouplayout
				.setHorizontalGroup(
						grouplayout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE)
								.addGroup(grouplayout.createParallelGroup(GroupLayout.Alignment.CENTER)
										.addComponent(jLabelAnimation,150,150,150).addComponent(jLabelInfo))
								.addGap(0, 0, Short.MAX_VALUE));
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
