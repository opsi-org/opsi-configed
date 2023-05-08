package de.uib.utilities.tree;

import java.awt.Image;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultMutableTreeNode;

public class SimpleIconNode extends DefaultMutableTreeNode {
	private Icon defaultIcon;
	private Icon nonSelectedIcon;
	private Icon closedIcon;
	private Icon disabledClosedIcon;
	private Icon enabledClosedIcon;

	private Icon leafIcon;
	private Icon disabledLeafIcon;
	private Icon enabledLeafIcon;

	private Icon nonSelectedLeafIcon;

	private Icon openIcon;
	private Icon disabledOpenIcon;
	private Icon enabledOpenIcon;

	private Icon emphasizedIcon;

	private String toolTipText;

	private boolean emphasized;

	public SimpleIconNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

	public SimpleIconNode(Object userObject) {
		this(userObject, true);
	}

	public void setToolTipText(String s) {
		toolTipText = s;
	}

	public String getToolTipText() {
		return toolTipText;
	}

	public void setEnabled(boolean aFlag) {
		if (!aFlag) {
			// Lazy creation: avoids unneccessary objects if the tree
			// could not have disabled state.
			if ((closedIcon != null) && (disabledClosedIcon == null)) {
				disabledClosedIcon = createDisabledIcon(enabledClosedIcon);
			}

			setDisabledLeafIcon();

			if ((openIcon != null) && (disabledOpenIcon == null)) {
				disabledOpenIcon = createDisabledIcon(enabledOpenIcon);
			}
			// end of lazy creation

			closedIcon = disabledClosedIcon;
			leafIcon = disabledLeafIcon;
			openIcon = disabledOpenIcon;
		} else {
			closedIcon = enabledClosedIcon;
			leafIcon = enabledLeafIcon;
			openIcon = enabledOpenIcon;
		}
	}

	/**
	 * Try to create grayed icon from aIcon and return it, or return null.
	 */
	private static Icon createDisabledIcon(Icon anIcon) {
		// copied from your example: e601. Creating a Gray Version of an Icon
		if (anIcon instanceof ImageIcon) {
			Image grayImage = GrayFilter.createDisabledImage(((ImageIcon) anIcon).getImage());
			return new ImageIcon(grayImage);
		}
		// Cannot convert
		return null;
	}

	public Icon getClosedIcon() {
		return closedIcon;
	}

	// set the icon as default for all types of icons
	public void setIcon(Icon anIcon) {
		defaultIcon = anIcon;
		nonSelectedIcon = defaultIcon;
		setClosedIcon(anIcon);
		setLeafIcon(anIcon);
		setOpenIcon(anIcon);
		setNonSelectedLeafIcon(anIcon);
		setEmphasizedIcon(anIcon);
		setDisabledLeafIcon();
	}

	public void setNonSelectedIcon(Icon aClosedIcon) {
		nonSelectedIcon = aClosedIcon;
	}

	public void setClosedIcon(Icon aClosedIcon) {
		closedIcon = aClosedIcon;
		enabledClosedIcon = aClosedIcon;
	}

	public Icon getLeafIcon() {
		return leafIcon;
	}

	public void setNonSelectedLeafIcon(Icon aLeafIcon) {
		nonSelectedLeafIcon = aLeafIcon;
	}

	public Icon getNonSelectedLeafIcon() {
		if (nonSelectedLeafIcon == null && leafIcon != null) {
			nonSelectedLeafIcon = createDisabledIcon(leafIcon);
		}

		return nonSelectedLeafIcon;
	}

	public Icon getNonSelectedIcon() {
		if (nonSelectedIcon == null && defaultIcon != null) {
			nonSelectedIcon = createDisabledIcon(leafIcon);
		}

		return nonSelectedIcon;
	}

	public void setLeafIcon(Icon aLeafIcon) {
		leafIcon = aLeafIcon;
		enabledLeafIcon = aLeafIcon;
	}

	public Icon getOpenIcon() {
		return openIcon;
	}

	public void setOpenIcon(Icon anOpenIcon) {
		openIcon = anOpenIcon;
		enabledOpenIcon = anOpenIcon;
	}

	public void setDisabledLeafIcon() {
		if (leafIcon != null) {
			disabledLeafIcon = createDisabledIcon(leafIcon);
		}
	}

	public void setEmphasizedIcon(Icon anEmphasizedIcon) {
		if (anEmphasizedIcon != null) {
			emphasizedIcon = anEmphasizedIcon;
		}
	}

	public Icon getEmphasizedIcon() {
		return emphasizedIcon;
	}

	public void setEmphasized(boolean b) {
		emphasized = b;
	}

	public boolean isEmphasized() {
		return emphasized;
	}

}
