/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.uib.configed.gui.Configed;
import de.uib.configed.gui.share.Icons;
import de.uib.configed.gui.share.datapanel.DefaultEditMapPanel;
import net.miginfocom.swing.MigLayout;

public class PanelEditClientProperties extends AbstractPanelEditProperties {
	private JLabel jLabelProductProperties;
	private JButton buttonSetValuesFromServerDefaults;
	private JButton buttonRemoveSpecificValues;

	private JPanel titlePanel;

	public PanelEditClientProperties(DefaultEditMapPanel productPropertiesPanel) {
		super(productPropertiesPanel);
		initComponents();
		initTitlePanel();
	}

	private void initComponents() {
		jLabelProductProperties = new JLabel(Configed.getResourceValue("ProductInfoPane.jLabelProductProperties"));
		jLabelProductProperties.setMinimumSize(new Dimension());

		buttonSetValuesFromServerDefaults = new JButton(Icons.getIntellijIcon("locked"));
		buttonSetValuesFromServerDefaults
				.setToolTipText(Configed.getResourceValue("ProductInfoPane.buttonSetValuesFromServerDefaults"));
		buttonSetValuesFromServerDefaults.addActionListener(actionEvent -> productPropertiesPanel.resetDefaults());

		buttonRemoveSpecificValues = new JButton(Icons.getIntellijIcon("remove"));
		buttonRemoveSpecificValues
				.setToolTipText(Configed.getResourceValue("ProductInfoPane.buttonRemoveSpecificValues"));
		buttonRemoveSpecificValues.addActionListener(actionEvent -> productPropertiesPanel.setVoid());

		this.setLayout(new MigLayout("insets 0, fill", "", "[]0"));
		this.add(productPropertiesPanel, "grow");
	}

	private void initTitlePanel() {
		titlePanel = new JPanel();
		titlePanel.setLayout(new MigLayout("insets 0, fill", "[center]push[center][center]", "[]0"));

		titlePanel.add(jLabelProductProperties);
		titlePanel.add(buttonSetValuesFromServerDefaults);
		titlePanel.add(buttonRemoveSpecificValues);
	}

	@Override
	public JPanel getTitlePanel() {
		return titlePanel;
	}

	@Override
	public void setTitlePanelActivated(boolean activated) {
		buttonSetValuesFromServerDefaults.setEnabled(activated);
		buttonRemoveSpecificValues.setEnabled(activated);
	}
}
