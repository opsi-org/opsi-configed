package de.uib.configed.gui.productpage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.Configed;
/*
 * configed - configuration editor for client work stations in opsi
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2014 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 */
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.gui.IconButton;
import de.uib.utilities.datapanel.DefaultEditMapPanel;

public class PanelEditClientProperties extends AbstractPanelEditProperties {
	private JLabel jLabelProductProperties;
	private IconButton buttonSetValuesFromServerDefaults;
	private IconButton buttonRemoveSpecificValues;

	private JPanel titlePanel;

	public PanelEditClientProperties(ConfigedMain mainController, DefaultEditMapPanel productPropertiesPanel) {
		super(mainController, productPropertiesPanel);
		initComponents();
		initTitlePanel();
	}

	private void initComponents() {
		jLabelProductProperties = new JLabel(Configed.getResourceValue("ProductInfoPane.jLabelProductProperties"));
		if (!ConfigedMain.FONT) {
			jLabelProductProperties.setFont(Globals.defaultFontBold);
		}
		if (!ConfigedMain.THEMES) {
			jLabelProductProperties.setForeground(Globals.lightBlack);
		}

		buttonSetValuesFromServerDefaults = new IconButton(
				Configed.getResourceValue("ProductInfoPane.buttonSetValuesFromServerDefaults"),
				"images/reset_network_defaults.png", "images/reset_network_defaults_over.png", " ", true);

		buttonSetValuesFromServerDefaults.setPreferredSize(new Dimension(15, 30));

		buttonSetValuesFromServerDefaults.addActionListener((ActionEvent e) -> productPropertiesPanel.resetDefaults());

		buttonRemoveSpecificValues = new IconButton(
				Configed.getResourceValue("ProductInfoPane.buttonRemoveSpecificValues"),
				"images/reset_network_eliminate.png", /* "images/edit-delete.png", */
				"images/reset_network_eliminate_over.png", /* "images/edit-delete_over.png", */
				"images/reset_network_eliminate_disabled.png", true);

		buttonRemoveSpecificValues.setPreferredSize(new Dimension(15, 30));

		buttonRemoveSpecificValues.addActionListener((ActionEvent e) -> productPropertiesPanel.setVoid());

		GroupLayout layoutEditProperties = new GroupLayout(this);
		setLayout(layoutEditProperties);

		layoutEditProperties.setHorizontalGroup(layoutEditProperties.createSequentialGroup()
				.addGap(Globals.SMALL_GAP_SIZE, Globals.SMALL_GAP_SIZE, Globals.SMALL_GAP_SIZE)
				.addComponent(productPropertiesPanel, minHSize, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(Globals.SMALL_GAP_SIZE, Globals.SMALL_GAP_SIZE, Globals.SMALL_GAP_SIZE));

		layoutEditProperties.setVerticalGroup(layoutEditProperties.createSequentialGroup()
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE).addComponent(
						productPropertiesPanel, Globals.MIN_TABLE_V_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	private void initTitlePanel() {
		titlePanel = new JPanel();

		GroupLayout titleLayout = new GroupLayout(titlePanel);
		titlePanel.setLayout(titleLayout);

		titleLayout.setHorizontalGroup(titleLayout.createSequentialGroup()
				.addComponent(jLabelProductProperties, minHSize, prefHSize, Short.MAX_VALUE)
				.addGap(Globals.SMALL_GAP_SIZE, Globals.SMALL_GAP_SIZE, Globals.SMALL_GAP_SIZE)
				.addComponent(buttonSetValuesFromServerDefaults, 20, 20, 20)
				.addGap(Globals.SMALL_GAP_SIZE, Globals.SMALL_GAP_SIZE, Globals.SMALL_GAP_SIZE)
				.addComponent(buttonRemoveSpecificValues, 20, 20, 20));

		titleLayout.setVerticalGroup(titleLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(jLabelProductProperties, 0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonSetValuesFromServerDefaults, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonRemoveSpecificValues, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE));
	}

	@Override
	public JPanel getTitlePanel() {
		return titlePanel;
	}

	@Override
	public void setTitlePanelActivated(boolean activated) {
		if (!ConfigedMain.THEMES) {
			jLabelProductProperties.setForeground(activated ? Globals.lightBlack : Globals.greyed);
		}
		buttonSetValuesFromServerDefaults.setEnabled(activated);
		buttonRemoveSpecificValues.setEnabled(activated);
	}

	public void setSpecificPropertiesExisting(String productName, Map<String, Boolean> specificPropertiesExisting) {

		setPropertyResetActivated(false);
		if (specificPropertiesExisting != null && productName != null
				&& specificPropertiesExisting.get(productName) != null) {
			setPropertyResetActivated(specificPropertiesExisting.get(productName));
		}
	}

	private void setPropertyResetActivated(boolean b) {

		buttonRemoveSpecificValues.setEnabled(b);
	}
}
