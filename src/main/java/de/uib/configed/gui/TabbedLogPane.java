/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.gui.logpane.LogPane;
import de.uib.utilities.logging.Logging;
import utils.Utils;

public class TabbedLogPane extends JTabbedPane {
	private LogPane[] textPanes;
	private String[] idents = Utils.getLogTypes();
	private final List<String> identsList;

	private ConfigedMain configedMain;

	public TabbedLogPane(ConfigedMain configedMain) {
		this.configedMain = configedMain;

		identsList = Arrays.asList(idents);

		textPanes = new LogPane[idents.length];

		for (int i = 0; i < idents.length; i++) {
			initLogTabComponent(i, Configed.getResourceValue("MainFrame.DefaultTextForLogfiles"));
		}

		super.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				initTabWidth();
			}
		});
		super.addChangeListener(changeEvent -> initTabWidth());
	}

	private void initTabWidth() {
		Insets tabInsets = getTabInsets();
		Insets tabAreaInsets = getTabAreaInsets();
		Insets insets = getInsets();
		int areaWidth = calcWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right;
		int tabCount = getTabCount();
		int tabWidth = 0;
		int gap = 0;
		switch (getTabPlacement()) {
		case LEFT:
		case RIGHT:
			tabWidth = areaWidth / 4;
			gap = 0;
			break;
		case BOTTOM:
		case TOP:
		default:
			tabWidth = areaWidth / tabCount;
			gap = areaWidth - (tabWidth * tabCount);
			break;
		}

		tabWidth = tabWidth - tabInsets.left - tabInsets.right - 3;
		for (int i = 0; i < tabCount; i++) {
			Component tabComponent = getTabComponentAt(i);
			if (tabComponent == null) {
				break;
			}

			if (i < gap) {
				tabWidth = tabWidth + 1;
			}
			tabComponent.setPreferredSize(new Dimension(tabWidth, tabComponent.getPreferredSize().height));
		}
		revalidate();
	}

	private Insets getTabInsets() {
		return getInsets("TabbedPane.tabInsets", Region.TABBED_PANE_TAB);
	}

	private Insets getTabAreaInsets() {
		return getInsets("TabbedPane.tabAreaInsets", Region.TABBED_PANE_TAB_AREA);
	}

	private Insets getInsets(String insetsKey, Region insetsRegion) {
		Insets insets = UIManager.getInsets(insetsKey);
		if (insets == null) {
			SynthStyle style = SynthLookAndFeel.getStyle(this, insetsRegion);
			SynthContext context = new SynthContext(this, insetsRegion, style, SynthConstants.ENABLED);
			insets = style.getInsets(context, null);
		}
		return insets;
	}

	private int calcWidth() {
		double proportionOfTotalWidth = 0.5;
		return (int) (getWidth() * proportionOfTotalWidth);
	}

	@Override
	public void insertTab(String title, Icon icon, Component component, String tip, int index) {
		super.insertTab(title, icon, component, tip, index);
		JLabel label = new JLabel(title, SwingConstants.CENTER);
		Dimension dim = label.getPreferredSize();
		Insets tabInsets = getTabInsets();
		label.setPreferredSize(new Dimension(0, dim.height + tabInsets.top + tabInsets.bottom));
		setTabComponentAt(index, label);
		initTabWidth();
	}

	private void initLogTabComponent(int i, String defaultText) {
		LogTabComponent logTabComponent = new LogTabComponent(defaultText, getFocusTraversalKeysEnabled(),
				configedMain);
		logTabComponent.setLogFileType(idents[i]);
		textPanes[i] = logTabComponent;
		super.addTab(idents[i], textPanes[i]);
	}

	public void loadDocument(String ident) {
		LogTabComponent logTabComponent = (LogTabComponent) getTabComponentAt(getSelectedIndex());
		logTabComponent.loadDocument(ident);
	}

	public void setDocuments(final Map<String, String> documents, final String info) {
		Logging.info(this, "idents.length " + idents.length + " info: " + info);
		for (String ident : idents) {
			setDocument(ident, documents.get(ident), info);
		}
	}

	private void setDocument(String ident, final String document, final String info) {
		int i = identsList.indexOf(ident);
		Logging.info(this, "setDocument " + i + " document == null " + (document == null));
		if (i < 0 || i >= idents.length) {
			return;
		}

		if (document == null) {
			textPanes[i].setText("");
			textPanes[i].setTitle("");
			return;
		}

		textPanes[i].setTitle(idents[i] + "  " + info);
		textPanes[i].setInfo(info);
		textPanes[i].setText(document);
	}
}
