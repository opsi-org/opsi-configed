package de.uib.configed.gui;

/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2006 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import de.uib.configed.Globals;

/**
 * a button-like panel
 */
public class IconAsButton extends JPanel implements MouseListener {

	protected String imageURLActive;
	protected String imageURLAttention;
	protected String imageURLInactive;
	protected String imageURLOver;
	protected String imageURLDisabled;
	protected ImageIcon iconActive;
	protected ImageIcon iconInactive;
	protected ImageIcon iconOver;
	protected ImageIcon iconDisabled;
	protected String tooltipActive;
	protected String tooltipInactive;

	protected JLabel label;
	protected boolean activated;
	protected boolean enabled;
	protected boolean getAttention;
	protected boolean mouseOver;
	protected ArrayList<ActionListener> actionListeners;
	public String description;

	/**
	 * @param desc           : a description used for tooltips and event action
	 *                       performed
	 * @param inactive       : the url for the image displayed if inactive
	 * @param over           : the url for the image displayed if the cursor is
	 *                       hovering over the buttion
	 * @param active         : the url for the image displayed if active
	 * @param disabled       : the url for the disabled image
	 * @param attentionImage : the url for the image displayed if active and
	 *                       shall get special attention
	 */
	public IconAsButton(String desc, String inactive, String over, String active, String attentionImage,
			String disabled) {
		super();

		setOpaque(false);

		setDisplay(desc, inactive, over, active, attentionImage, disabled);
		label = new JLabel(iconInactive, SwingConstants.CENTER);
		label.setToolTipText(desc);

		activated = false;
		enabled = true;
		mouseOver = false;
		actionListeners = new ArrayList<>();

		setLayout(new BorderLayout());
		label.addMouseListener((MouseListener) this);
		add(label);

	}

	public IconAsButton(String desc, String imageRelPath) {
		this(desc, imageRelPath, imageRelPath, imageRelPath, imageRelPath);
	}

	public IconAsButton(String desc, String inactive, String over, String active, String disabled) {
		this(desc, inactive, over, active, null, disabled);
	}

	public void setDisplay(String desc, String inactive, String over, String active, String disabled) {
		setDisplay(desc, inactive, over, active, null, disabled);
	}

	public void setDisplay(String desc, String inactive, String over, String active, String attentionImage,
			String disabled) {
		description = desc;
		tooltipActive = desc;
		tooltipInactive = desc;
		imageURLActive = active;
		imageURLAttention = attentionImage;
		if (imageURLAttention == null || imageURLAttention.equals(""))
			imageURLAttention = imageURLActive;
		imageURLInactive = inactive;
		imageURLOver = over;
		imageURLDisabled = disabled;

		iconInactive = Globals.createImageIcon(imageURLInactive, description);
		if (imageURLActive != null) {
			iconActive = Globals.createImageIcon(imageURLActive, description);
		}

		if (imageURLOver != null) {
			iconOver = Globals.createImageIcon(imageURLOver, description);
		}

		if (imageURLDisabled != null) {
			iconDisabled = Globals.createImageIcon(imageURLDisabled, description);
		}

		if (label != null)
			label.setToolTipText(desc);

	}

	@Override
	public void setToolTipText(String s) {
		description = s;
		label.setToolTipText(s);
		this.tooltipActive = s;
		this.tooltipInactive = s;
	}

	public void setToolTips(String tipActive, String tipInactive) {
		this.tooltipActive = tipActive;
		this.tooltipInactive = tipInactive;
	}

	@Override
	public void setEnabled(boolean b) {

		enabled = b;
		super.setEnabled(enabled);
		setIcon();
		if (enabled)
			label.setEnabled(true);

	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	public void setAttention(boolean b) {
		getAttention = b;
	}

	private void setIcon() {
		if (!enabled) {
			if (iconDisabled != null) {
				label.setIcon(iconDisabled);
			} else {
				label.setEnabled(false);
			}
		} else {
			if (mouseOver) {
				if (iconOver != null) {
					label.setIcon(iconOver);
				} else if (!activated && iconActive != null) {
					label.setIcon(iconActive);
				} else {
					label.setIcon(iconInactive);
				}
			} else {

				if (activated && iconActive != null) {

					label.setIcon(iconActive);
				} else {

					label.setIcon(iconInactive);
				}
			}
		}
		repaint();
	}

	public void setActivated(boolean a) {
		activated = a;
		mouseOver = false;
		setIcon();
		if (tooltipActive != null && tooltipInactive != null) {
			if (a)
				label.setToolTipText(tooltipActive);
			else
				label.setToolTipText(tooltipInactive);
		}
	}

	public boolean isActivated() {
		return activated;
	}

	public ArrayList<ActionListener> getActionListeners() {
		return actionListeners;
	}

	public void addActionListener(ActionListener l) {
		boolean newListener = true;
		for (int i = 0; i < actionListeners.size(); i++) {
			if ((ActionListener) actionListeners.get(i) == l) {
				newListener = false;
				break;
			}
		}
		if (newListener) {
			actionListeners.add(l);
		}
	}

	public void fireActionPerformed(ActionEvent e) {
		for (int i = 0; i < actionListeners.size(); i++) {
			((ActionListener) actionListeners.get(i)).actionPerformed(e);
		}
	}

	/*****************************
	 * implementing MouseListener *
	 *****************************/

	@Override
	public void mouseClicked(java.awt.event.MouseEvent e) {

		if (isEnabled()) {
			ActionEvent action = new ActionEvent(this, 1, description);
			fireActionPerformed(action);
		}
	}

	@Override
	public void mousePressed(java.awt.event.MouseEvent e) {

	}

	@Override
	public void mouseReleased(java.awt.event.MouseEvent e) {

	}

	@Override
	public void mouseEntered(java.awt.event.MouseEvent e) {
		if (enabled && !mouseOver) {
			mouseOver = true;
			setIcon();
		}
	}

	@Override
	public void mouseExited(java.awt.event.MouseEvent e) {
		if (enabled && mouseOver) {
			mouseOver = false;
			setIcon();
		}
	}
}
