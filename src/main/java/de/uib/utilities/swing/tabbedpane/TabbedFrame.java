package de.uib.utilities.swing.tabbedpane;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Image;
import java.util.Map;

import de.uib.configed.ConfigedMain.LicencesTabStatus;
import de.uib.utilities.swing.SecondaryFrame;

//adapting TabbedPaneX to a JFrame
public class TabbedFrame extends SecondaryFrame {
	private TabbedPaneX panel;

	public TabbedFrame(TabController controller) {
		super();
		panel = new TabbedPaneX(controller);
		init();
	}

	@Override
	public void setGlobals(Map<String, Object> globals) {
		panel.setGlobals(globals);
		setIconImage((Image) globals.get("mainIcon"));
		setTitle((String) globals.get("APPNAME"));
	}

	private void init() {
		panel.init();
		add(panel);
		pack();
	}

	@Override
	public void start() {
		setVisible(true);
		setExtendedState(Frame.NORMAL);
	}

	public TabbedPaneX getMainPanel() {
		return panel;
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, using an extra title
	 */
	public void addTab(LicencesTabStatus s, String title, Component c) {
		panel.addTab(s, title, c);
	}

	/**
	 * removes a tab
	 */
	public void removeTab(LicencesTabStatus s) {
		panel.removeTab(s);
	}

}
