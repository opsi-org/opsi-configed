/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share.swing;

import java.awt.FlowLayout;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ButtonTabComponent extends JPanel {
	private final TabButton button;

	public ButtonTabComponent(Icon icon, String title, String toolTipText, Runnable runnable) {
		button = new TabButton(icon, toolTipText, runnable);
		init(title);
	}

	private void init(String tabName) {
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		setOpaque(false);
		setBorder(null);

		JLabel label = new JLabel(tabName);
		add(label);
		add(button);
	}

	public void showButton(boolean show) {
		button.setVisible(show);
	}

	private static class TabButton extends JButton {
		private final Runnable runnable;

		public TabButton(Icon icon, String toolTipText, Runnable runnable) {
			super(icon);
			this.runnable = runnable;
			init(toolTipText);
		}

		private void init(String toolTipText) {
			setOpaque(false);
			setFocusable(false);
			setBorderPainted(false);
			setContentAreaFilled(false);
			setRolloverEnabled(true);

			setToolTipText(toolTipText);

			addActionListener(e -> {
				runnable.run();
				setVisible(false);
			});
		}
	}
}
