package de.uib.utilities.swing.tabbedpane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;

import de.uib.configed.ConfigedMain.LicencesTabStatus;

public class TabbedPaneX extends JPanel {
	private JTabbedPane jTabbedPaneMain;

	TabController controller;

	List<LicencesTabStatus> tabOrder;

	Map<String, Object> globals;

	public TabbedPaneX(TabController controller) {
		this.controller = controller;
		init();
	}

	public void setGlobals(Map<String, Object> globals) {
		this.globals = globals;
		setFont((Font) globals.get("defaultFont"));
	}

	protected void init() {
		setLayout(new BorderLayout());
		setSize(600, 400);

		setLayout(new BorderLayout());
		jTabbedPaneMain = new JTabbedPane(SwingConstants.TOP);

		tabOrder = new ArrayList<>();

		jTabbedPaneMain.addChangeListener((ChangeEvent changeEvent) -> {
			int newVisualIndex = jTabbedPaneMain.getSelectedIndex();

			LicencesTabStatus newS = tabOrder.get(newVisualIndex);

			// report state change request to controller and look, what it produces
			LicencesTabStatus s = controller.reactToStateChangeRequest(newS);

			// if the controller did not accept the new index set it back
			// observe that we get a recursion since we initiate another state change
			// the recursion breaks since newVisualIndex is identical with
			// the old and does not yield a different value
			if (newS != s) {

				jTabbedPaneMain.setSelectedIndex(tabOrder.indexOf(s));
			}
		});

		add(jTabbedPaneMain, BorderLayout.CENTER);

	}

	/**
	 * adds a tab to the incorporated JTabbedMain, with an icon and a tooltip
	 */
	public void addTab(LicencesTabStatus s, Component c, Icon icon, String tip) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(s.toString(), icon, c, tip);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, with an icon
	 */
	public void addTab(LicencesTabStatus s, Component c, Icon icon) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(s.toString(), icon, c);
	}

	/**
	 * adds a tab to the incorporated JTabbedMainprotected
	 */
	public void addTab(LicencesTabStatus s, Component c) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(s.toString(), c);
	}

	/**
	 * adds a tab to the incorporated JTabbedMain, using an extra title
	 */
	public void addTab(LicencesTabStatus s, String title, Component c) {
		tabOrder.add(s);
		jTabbedPaneMain.addTab(title, c);
	}

	/**
	 * removes a tab
	 */
	public void removeTab(LicencesTabStatus s) {
		int tabIndex = tabOrder.indexOf(s);
		if (tabIndex > 0) {
			jTabbedPaneMain.remove(tabIndex);
			tabOrder.remove(tabIndex);
		}
	}

}
