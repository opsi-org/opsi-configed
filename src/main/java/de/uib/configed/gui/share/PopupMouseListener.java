/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.share;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Predicate;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

public class PopupMouseListener extends MouseAdapter {
	private JPopupMenu popupMenu;
	private Predicate<MouseEvent> condition;

	public PopupMouseListener(JPopupMenu popup, Predicate<MouseEvent> condition, JComponent[] components) {
		popupMenu = popup;
		this.condition = condition;

		if (components != null) {
			for (JComponent component : components) {
				component.addMouseListener(this);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		maybeShowPopup(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		maybeShowPopup(e);
	}

	protected void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger() && (condition == null || condition.test(e))) {
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	public static void addPopupMouseListenerToComponents(JPopupMenu popup, JComponent[] components) {
		new PopupMouseListener(popup, null, components);
	}

	public static void addPopupMouseListenerToComponents(JPopupMenu popup, Predicate<MouseEvent> condition,
			JComponent[] components) {
		new PopupMouseListener(popup, condition, components);
	}
}
