/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.utilities.swing;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import com.formdev.flatlaf.FlatLaf;

import de.uib.configed.Configed;
import de.uib.configed.Globals;

public abstract class AbstractClosableTabComponent extends JPanel {
	private final JTabbedPane pane;

	protected AbstractClosableTabComponent(final JTabbedPane pane) {
		this.pane = pane;
		init();
	}

	private void init() {
		setOpaque(false);
		JLabel label = new JLabel() {
			@Override
			public String getText() {
				int i = pane.indexOfTabComponent(AbstractClosableTabComponent.this);
				if (i != -1) {
					return pane.getTitleAt(i);
				}
				return null;
			}
		};
		add(label);
		add(new CloseTabButton());
	}

	public void close() {
	}

	@SuppressWarnings({ "java:S2972" })
	private class CloseTabButton extends JButton implements ActionListener {
		public CloseTabButton() {
			init();
		}

		private void init() {
			setPreferredSize(new Dimension(20, 20));
			setToolTipText(Configed.getResourceValue("AbstractClosableTabComponent.tooltip.text"));
			setUI(new BasicButtonUI());
			setContentAreaFilled(false);
			setFocusable(false);
			setBorderPainted(false);
			setRolloverEnabled(true);
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			close();
		}

		@Override
		public void updateUI() {
			// We don't want to update UI for this button.
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setStroke(new BasicStroke(2));
			if (FlatLaf.isLafDark()) {
				g2.setColor(Globals.OPSI_FOREGROUND_DARK);
			} else {
				g2.setColor(Globals.OPSI_FOREGROUND_LIGHT);
			}
			if (getModel().isRollover()) {
				if (FlatLaf.isLafDark()) {
					g2.setColor(Globals.OPSI_DARK_MAGENTA);
				} else {
					g2.setColor(Globals.OPSI_LIGHT_MAGENTA);
				}
			}
			drawX(g2);
			g2.dispose();
		}

		private void drawX(Graphics g) {
			int offset = 6;
			Coordinate topLeftCorner = new Coordinate(offset, offset);
			Coordinate bottomRightCorner = new Coordinate(getWidth() - offset, getHeight() - offset);
			g.drawLine(topLeftCorner.getX(), topLeftCorner.getY(), bottomRightCorner.getX(), bottomRightCorner.getY());
			Coordinate topRightCorner = new Coordinate(getWidth() - offset, offset);
			Coordinate bottomLeftCorner = new Coordinate(offset, getHeight() - offset);
			g.drawLine(topRightCorner.getX(), topRightCorner.getY(), bottomLeftCorner.getX(), bottomLeftCorner.getY());
		}
	}

	private static class Coordinate {
		private int x;
		private int y;

		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}
}
