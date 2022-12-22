package de.uib.configed.gui;

import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import de.uib.configed.Globals;
import de.uib.utilities.logging.logging;
import de.uib.utilities.thread.WaitingCycle;

/**
 * Creates a button with an icon <br>
 * <br>
 * 
 * @version 1.0
 * @author Anna Sucher
 */
public class IconButton extends JButton {
	/** The status if button is active */
	protected boolean activated;
	/** The status if button is/should be enabled */
	protected boolean enabled;

	/** A description used for tooltip if button is active */
	protected String tooltipActive;

	/** A description used for tooltip if button is inactive */
	protected String tooltipInactive;

	/** The url for the image displayed if active */
	protected String imageURLActive;

	/**
	 * The url for the image displayed if the cursor is hovering over the button
	 */
	protected String imageURLOver;

	/** The url for the disabled image */
	protected String imageURLDisabled;

	/**
	 * Default image (if change between default image and special image is used)
	 */
	protected Icon defaultIcon;

	/** Running action icon */
	protected Icon runningActionIcon;

	/** A description used for tooltips anyway */
	protected String description;

	/** the sequence of images for animation */
	protected ImageIcon[] imagesForAnimation;

	/**
	 * we make a composition to a WaitingCycle for implementing a waiting state
	 */
	protected WaitingCycle waitingCycle;

	/** timeout for the waitingCycle **/
	protected int maxWaitSecs;

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
	 * @param desc                     a description used for tooltips
	 * @param imageURLOver             the url for the image displayed if the
	 *                                 cursor is hovering over the button
	 * @param imageURLActive           the url for the image displayed if active
	 * @param imageURLDisabled         the url for the disabled image
	 * @param imagesForAnimatedWaiting the chain of images for animation when
	 *                                 waiting
	 * @param enabled                  if true, sets the iconButton enabled
	 *                                 status true; otherwise false
	 */
	public IconButton(String desc, String imageURLActive, String imageURLOver, String imageURLDisabled,
			String[] imageURLsForAnimatedWaiting, int maxWaitSecs, boolean enabled) {
		super();
		this.tooltipActive = desc;
		this.tooltipInactive = desc;
		this.description = desc;
		this.imageURLActive = imageURLActive;
		this.imageURLOver = imageURLOver;
		this.imageURLDisabled = imageURLDisabled;
		this.enabled = enabled;
		this.maxWaitSecs = maxWaitSecs;

		createIconButton();

		if (imageURLsForAnimatedWaiting != null) {
			imagesForAnimation = new ImageIcon[imageURLsForAnimatedWaiting.length];

			for (int i = 0; i < imageURLsForAnimatedWaiting.length; i++) {
				imagesForAnimation[i] = Globals.createImageIcon(imageURLsForAnimatedWaiting[i], "");
			}
		}
	}

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
		this(desc, imageURLActive, imageURLOver, imageURLDisabled, null, 0, enabled);
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
	public void createIconButton()

	{
		setIcon(Globals.createImageIcon(this.imageURLActive, ""));
		setToolTipText(description);
		setPreferredSize(Globals.graphicButtonDimension);
		setEnabled(this.enabled);
		setSelectedIcon(Globals.createImageIcon(this.imageURLOver, ""));
		if (imageURLDisabled.length() > 3)
			setDisabledIcon(Globals.createImageIcon(this.imageURLDisabled, ""));

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
			boolean enabled)

	{
		setIcon(Globals.createImageIcon(imageURLActive, ""));
		setToolTipText(desc);
		setPreferredSize(Globals.graphicButtonDimension);
		setEnabled(enabled);
		setSelectedIcon(Globals.createImageIcon(imageURLOver, ""));
		if (imageURLDisabled.length() > 3)
			setDisabledIcon(Globals.createImageIcon(imageURLDisabled, ""));
	}

	/**
	 * Sets an active and inactive tooltiptext
	 * 
	 * @param tipActive   sets this tooltip if the button is active
	 * @param tipInactive sets this tooltip if the button is inactive
	 */
	public void setToolTips(String tipActive, String tipInactive) {
		this.tooltipActive = tipActive;
		this.tooltipInactive = tipInactive;
	}

	/**
	 * Sets an image for active icon button and an image if the curser is
	 * hovering over the button
	 * 
	 * @param imageURLActive the new url for the image displayed if active
	 * @param imageURLOver   the new url for the image displayed if the cursor
	 *                       is hovering over the button
	 */
	public void setNewImage(String imageURLActive, String imageURLOver) {
		setIcon(Globals.createImageIcon(imageURLActive, ""));
		setSelectedIcon(Globals.createImageIcon(imageURLOver, ""));
	}

	/**
	 * Sets the tooltiptext for active button if paramaterer "a" is true <br>
	 * and the tooltiptext for inactive button if parameter "a" is false
	 * 
	 * @param a sets the activate status for button used for tooltiptext
	 */
	public void setActivated(boolean a) {
		activated = a;
		if (tooltipActive != null && tooltipInactive != null) {
			if (a)
				setToolTipText(tooltipActive);
			else
				setToolTipText(tooltipInactive);
		}
	}

	/**
	 * @return current active status
	 */
	public boolean isActivated() {
		return activated;
	}

	/**
	 * set the default icon as internal value by location
	 */
	public void setDefaultIcon(String desc) {
		defaultIcon = Globals.createImageIcon(desc, "");
	}

	/**
	 * set the default icon as internal value
	 */
	public void setDefaultIcon(Icon im) {
		defaultIcon = im;
	}

	/**
	 * gets it
	 */
	public Icon getDefaultIcon() {
		return defaultIcon;
	}

	/**
	 * set the running action icon as internal value by location
	 */
	public void setRunningActionIcon(String desc) {
		runningActionIcon = Globals.createImageIcon(desc, "");
	}

	/**
	 * set the running action icon as internal value
	 */
	public void setRunningActionIcon(Icon im) {
		runningActionIcon = im;
	}

	/**
	 * gets it
	 */
	public Icon getRunningActionIcon() {
		return runningActionIcon;
	}

	/**
	 * waiting state
	 */
	public void setWaitingState(boolean b) {
		logging.info(this, "setWaitingState " + b + " (imagesForAnimation == null)  " + (imagesForAnimation == null));
		if (imagesForAnimation == null)
			return;

		if (b) {
			setEnabled(false);

			waitingCycle = new WaitingCycle(maxWaitSecs) {
				@Override
				protected void process(List<Integer> chunks) {
					workWithWaitingSignals(chunks);
				}
			};
			// must be constructed newly for each call according to the definition of
			// SwingWorker

			waitingCycle.execute();
		} else {
			waitingCycle.stop();
		}

	}

	private void workWithWaitingSignals(List<Integer> chunks) {
		if (imagesForAnimation == null)
			return;

		int iconIndex = chunks.get(chunks.size() - 1) % (imagesForAnimation.length);

		logging.debug(this, "workWithWaitingSignals chunks size " + chunks.size());

		setDisabledIcon(imagesForAnimation[iconIndex]);
	}

}
