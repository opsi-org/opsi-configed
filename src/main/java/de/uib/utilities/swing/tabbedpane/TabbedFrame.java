package de.uib.utilities.swing.tabbedpane;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JFrame;

import de.uib.utilities.swing.SecondaryFrame;

//adapting TabbedPaneX to a JFrame
public class TabbedFrame extends SecondaryFrame {
	protected TabbedPaneX panel;

	public TabbedFrame(JFrame mainframe, TabController controller) {
		super();
		panel = new TabbedPaneX(controller);
		init();
	}

	public void setGlobals(Map globals) {
		panel.setGlobals(globals);
		setIconImage((Image) globals.get("mainIcon"));
		setTitle((String) globals.get("APPNAME"));
	}

	protected void init() {
		panel.init();
		add(panel);
		pack();
	}

	public void start() {
		setVisible(true);
		setExtendedState(Frame.NORMAL);
	}

	/*
	 * public void centerOnParent(int startX, int startY, int parentWidth, int
	 * parentHeight)
	 * {
	 * Dimension frameSize = getSize();
	 * if (frameSize.height > parentHeight)
	 * {
	 * frameSize.height = parentHeight;
	 * }
	 * if (frameSize.width > parentWidth)
	 * {
	 * frameSize.width = parentWidth;
	 * }
	 * setLocation( startX + (parentWidth - frameSize.width) / 2, startY +
	 * (parentHeight - frameSize.height) / 2);
	 * }
	 */

	public TabbedPaneX getMainPanel() {
		return panel;
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, with an icon and a tooltip
	 */
	public void addTab(Enum s, Component c, Icon icon, String tip) {
		panel.addTab(s, c, icon, tip);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, with an icon
	 */
	public void addTab(Enum s, Component c, Icon icon) {
		panel.addTab(s, c, icon);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain
	 */
	public void addTab(Enum s, Component c) {
		panel.addTab(s, c);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, using an extra title
	 */
	public void addTab(Enum s, String title, Component c) {
		panel.addTab(s, title, c);
	}

	/**
	 * removes a tab
	 */
	public void removeTab(Enum s) {
		panel.removeTab(s);
	}

}
