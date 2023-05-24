package de.uib.configed.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import de.uib.configed.Globals;

/**
 * a button-like panel
 */
public class IconAsButton extends JPanel implements MouseListener {

	private ImageIcon iconActive;
	private ImageIcon iconInactive;
	private ImageIcon iconOver;
	private ImageIcon iconDisabled;
	private String tooltipActive;
	private String tooltipInactive;

	private JLabel label;
	protected boolean activated;
	private boolean mouseOver;
	private List<ActionListener> actionListeners;
	private String description;

	/**
	 * @param desc     : a description used for tooltips and event action
	 *                 performed
	 * @param inactive : the url for the image displayed if inactive
	 * @param over     : the url for the image displayed if the cursor is
	 *                 hovering over the buttion
	 * @param active   : the url for the image displayed if active
	 * @param disabled : the url for the disabled image shall get special
	 *                 attention
	 */
	public IconAsButton(String desc, String inactive, String over, String active, String disabled) {
		super();

		super.setOpaque(false);

		setDisplay(desc, inactive, over, active, disabled);
		label = new JLabel(iconInactive, SwingConstants.CENTER);
		label.setToolTipText(desc);

		activated = false;
		mouseOver = false;
		actionListeners = new ArrayList<>();

		super.setLayout(new BorderLayout());
		label.addMouseListener(this);
		super.add(label);

	}

	public IconAsButton(String desc, String imageRelPath) {
		this(desc, imageRelPath, imageRelPath, imageRelPath, imageRelPath);
	}

	private void setDisplay(String desc, String inactive, String over, String active, String disabled) {
		description = desc;
		tooltipActive = desc;
		tooltipInactive = desc;

		iconInactive = Globals.createImageIcon(inactive, description);
		if (active != null) {
			iconActive = Globals.createImageIcon(active, description);
		}

		if (over != null) {
			iconOver = Globals.createImageIcon(over, description);
		}

		if (disabled != null) {
			iconDisabled = Globals.createImageIcon(disabled, description);
		}

		if (label != null) {
			label.setToolTipText(desc);
		}
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

		super.setEnabled(b);
		setIcon();
		if (isEnabled()) {
			label.setEnabled(true);
		}
	}

	private void setIcon() {
		if (!isEnabled()) {
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
			if (a) {
				label.setToolTipText(tooltipActive);
			} else {
				label.setToolTipText(tooltipInactive);
			}
		}
	}

	public boolean isActivated() {
		return activated;
	}

	public void addActionListener(ActionListener l) {
		boolean newListener = true;
		for (int i = 0; i < actionListeners.size(); i++) {
			if (actionListeners.get(i) == l) {
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
			(actionListeners.get(i)).actionPerformed(e);
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
		/* Not needed */}

	@Override
	public void mouseReleased(java.awt.event.MouseEvent e) {
		/* Not needed */}

	@Override
	public void mouseEntered(java.awt.event.MouseEvent e) {
		if (isEnabled() && !mouseOver) {
			mouseOver = true;
			setIcon();
		}
	}

	@Override
	public void mouseExited(java.awt.event.MouseEvent e) {
		if (isEnabled() && mouseOver) {
			mouseOver = false;
			setIcon();
		}
	}
}
