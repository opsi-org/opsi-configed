/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import javax.swing.JButton;

import de.uib.configed.Globals;
import utils.Utils;

public class IconButton extends JButton {

	/** The url for the image displayed if active */
	private String imageURLActive;

	/**
	 * The url for the image displayed if the cursor is hovering over the button
	 */
	private String imageURLOver;

	/** The url for the disabled image */
	private String imageURLDisabled;

	/** A description used for tooltips anyway */
	private String description;

	/**
	 * Just calling super constructor
	 */
	public IconButton() {
		super();
	}

	/**
	 * Calling super constructor with text and icon public IconButton() {
	 * super(String text, Icon icon); }
	 */

	/**
	 * Sets the parameter as global variables and create an icon with
	 * "createIconButton" method
	 *
	 * @param desc             a description used for tooltips
	 * @param imageURLOver     the url for the image displayed if the cursor is
	 *                         hovering over the button
	 * @param imageURLActive   the url for the image displayed if active
	 * @param imageURLDisabled the url for the disabled image
	 * @param enabled          if true, sets the iconButton enabled status true;
	 *                         otherwise false
	 */
	public IconButton(String desc, String imageURLActive, String imageURLOver, String imageURLDisabled,
			boolean enabled) {
		super();
		this.description = desc;
		this.imageURLActive = imageURLActive;
		this.imageURLOver = imageURLOver;
		this.imageURLDisabled = imageURLDisabled;

		createIconButton(enabled);
	}

	/**
	 * Sets the parameter as global variables and create an icon with
	 * "createIconButton" method<br>
	 * Also sets the default value for enabled status "true"
	 * 
	 * @param desc             a description used for tooltips
	 * @param imageURLOver     the url for the image displayed if the cursor is
	 *                         hovering over the button
	 * @param imageURLActive   the url for the image displayed if active
	 * @param imageURLDisabled the url for the disabled image
	 */
	public IconButton(String desc, String imageURLActive, String imageURLOver, String imageURLDisabled) {
		this(desc, imageURLActive, imageURLOver, imageURLDisabled, true);
	}

	/**
	 * Creates an icon with global variables <br>
	 * (icon, description, preferred size, enabled status, selected icon and (if
	 * given) a disabled icon)
	 */
	private void createIconButton(boolean enabled) {
		setIcon(Utils.createImageIcon(this.imageURLActive, ""));
		setToolTipText(description);
		setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		setEnabled(enabled);
		setSelectedIcon(Utils.createImageIcon(this.imageURLOver, ""));
		if (imageURLDisabled.length() > 3) {
			setDisabledIcon(Utils.createImageIcon(this.imageURLDisabled, ""));
		}
	}

	/**
	 * Creates an icon with parameter
	 * 
	 * @param desc             a description used for tooltips
	 * @param imageURLOver     the url for the image displayed if the cursor is
	 *                         hovering over the button
	 * @param imageURLActive   the url for the image displayed if active
	 * @param imageURLDisabled the url for the disabled image
	 * @param enabled          if true, sets the enabled status true; otherwise
	 *                         false
	 */
	public void createIconButton(String desc, String imageURLActive, String imageURLOver, String imageURLDisabled,
			boolean enabled) {
		setIcon(Utils.createImageIcon(imageURLActive, ""));
		setToolTipText(desc);
		setPreferredSize(Globals.GRAPHIC_BUTTON_DIMENSION);
		setEnabled(enabled);
		setSelectedIcon(Utils.createImageIcon(imageURLOver, ""));
		if (imageURLDisabled.length() > 3) {
			setDisabledIcon(Utils.createImageIcon(imageURLDisabled, ""));
		}
	}
}
