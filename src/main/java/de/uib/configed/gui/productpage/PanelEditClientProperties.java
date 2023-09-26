/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.utilities.datapanel.DefaultEditMapPanel;

public class PanelEditClientProperties extends AbstractPanelEditProperties {
	private JLabel jLabelProductProperties;
	private IconButton buttonSetValuesFromServerDefaults;
	private IconButton buttonRemoveSpecificValues;

	private JPanel titlePanel;

	public PanelEditClientProperties(ConfigedMain configedMain, DefaultEditMapPanel productPropertiesPanel) {
		super(configedMain, productPropertiesPanel);
		initComponents();
		initTitlePanel();
	}

	private void initComponents() {
		jLabelProductProperties = new JLabel(Configed.getResourceValue("ProductInfoPane.jLabelProductProperties"));
		if (!Main.FONT) {
			jLabelProductProperties.setFont(Globals.DEFAULT_FONT_BIG);
		}
		if (!Main.THEMES) {
			jLabelProductProperties.setForeground(Globals.LIGHT_BLACK);
		}

		buttonSetValuesFromServerDefaults = new IconButton(
				Configed.getResourceValue("ProductInfoPane.buttonSetValuesFromServerDefaults"),
				"images/reset_network_defaults.png", "images/reset_network_defaults_over.png", " ", true);

		buttonSetValuesFromServerDefaults.setPreferredSize(new Dimension(15, 30));

		buttonSetValuesFromServerDefaults.addActionListener((ActionEvent e) -> productPropertiesPanel.resetDefaults());

		buttonRemoveSpecificValues = new IconButton(
				Configed.getResourceValue("ProductInfoPane.buttonRemoveSpecificValues"),
				"images/reset_network_eliminate.png", "images/reset_network_eliminate_over.png",
				"images/reset_network_eliminate_disabled.png", true);

		buttonRemoveSpecificValues.setPreferredSize(new Dimension(15, 30));

		buttonRemoveSpecificValues.addActionListener((ActionEvent e) -> productPropertiesPanel.setVoid());

		GroupLayout layoutEditProperties = new GroupLayout(this);
		setLayout(layoutEditProperties);

		layoutEditProperties.setHorizontalGroup(layoutEditProperties.createSequentialGroup()
				.addComponent(productPropertiesPanel, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		layoutEditProperties.setVerticalGroup(layoutEditProperties.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE).addComponent(
						productPropertiesPanel, Globals.MIN_TABLE_V_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	private void initTitlePanel() {
		titlePanel = new JPanel();

		GroupLayout titleLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titleLayout);

		titleLayout.setHorizontalGroup(titleLayout.createSequentialGroup()
				.addComponent(jLabelProductProperties, minHSize, prefHSize, Short.MAX_VALUE)
				.addComponent(buttonSetValuesFromServerDefaults, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE)
				.addComponent(buttonRemoveSpecificValues, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE)
				.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE));

		titleLayout.setVerticalGroup(titleLayout.createSequentialGroup().addGroup(titleLayout
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(jLabelProductProperties, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonSetValuesFromServerDefaults, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonRemoveSpecificValues, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)));
	}

	@Override
	public JPanel getTitlePanel() {
		return titlePanel;
	}

	@Override
	public void setTitlePanelActivated(boolean activated) {
		if (!Main.THEMES) {
			jLabelProductProperties.setForeground(activated ? Globals.LIGHT_BLACK : Globals.GREYED);
		}
		buttonSetValuesFromServerDefaults.setEnabled(activated);
		buttonRemoveSpecificValues.setEnabled(activated);
	}
}
