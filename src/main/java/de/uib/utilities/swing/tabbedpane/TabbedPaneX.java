package de.uib.utilities.swing.tabbedpane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.Map;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class TabbedPaneX extends JPanel {
	private javax.swing.JTabbedPane jTabbedPaneMain;

	TabController controller;

	Vector<Enum> tabOrder;

	Map globals;

	private int oldVisualIndex = -1;

	public TabbedPaneX(TabController controller) {
		this.controller = controller;
		init();
	}

	public void setGlobals(Map globals) {
		this.globals = globals;
		setFont((Font) globals.get("defaultFont"));
	}

	protected void init() {
		setLayout(new BorderLayout());
		setSize(600, 400);

		setLayout(new BorderLayout());
		jTabbedPaneMain = new JTabbedPane(JTabbedPane.TOP);

		tabOrder = new Vector<Enum>();

		jTabbedPaneMain.addChangeListener(new javax.swing.event.ChangeListener() {
			public void stateChanged(javax.swing.event.ChangeEvent e) {
				int newVisualIndex = jTabbedPaneMain.getSelectedIndex();

				// logging.debug(" new visual tab index " + newVisualIndex);

				Enum newS = tabOrder.elementAt(newVisualIndex);

				// logging.debug(" new tab state " + newS);

				// report state change request to controller and look, what it produces
				Enum s = controller.reactToStateChangeRequest(newS);

				// logging.debug(" controlled new tab state " + s);

				// if the controller did not accept the new index set it back
				// observe that we get a recursion since we initiate another state change
				// the recursion breaks since newVisualIndex is identical with
				// the old and does not yield a different value
				if (newS != s) {
					// logging.debug(" new tab index " + s);
					jTabbedPaneMain.setSelectedIndex(tabOrder.indexOf(s));
				}
			}
		});

		add(jTabbedPaneMain, BorderLayout.CENTER);

	}

	public void callExit() {
		// logging.debug(" we want to close ");
		controller.exit();
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, with an icon and a tooltip
	 */
	public void addTab(Enum s, Component c, Icon icon, String tip) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(s.toString(), icon, c, tip);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, with an icon
	 */
	public void addTab(Enum s, Component c, Icon icon) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(s.toString(), icon, c);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain
	 */
	public void addTab(Enum s, Component c) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(s.toString(), c);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, using an extra title
	 */
	public void addTab(Enum s, String title, Component c) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(title, c);
	}

	/**
	 * removes a tab
	 */
	public void removeTab(Enum s) {
		int tabIndex = tabOrder.indexOf(s);
		if (tabIndex > 0) {
			jTabbedPaneMain.remove(tabIndex);
			tabOrder.remove(tabIndex);
		}
	}

}
