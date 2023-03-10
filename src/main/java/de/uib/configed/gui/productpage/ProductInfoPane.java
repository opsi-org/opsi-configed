/*
 * ProductInfoPanes.java
 *
 * (open pc server integration) www.opsi.org
 *
 * Copyright (C) 2000-2019 uib.de
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License, version AGPLv3, as published by the Free Software Foundation
 * 
 * @author roeder, Nils Otto
 */

package de.uib.configed.gui.productpage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXPanel;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.logging.Logging;

public class ProductInfoPane extends JSplitPane implements DataChangedObserver, ActionListener {

	private JXPanel productDescriptionsPanel;
	private JXPanel bottomComponent;
	protected JTextField jLabelProductID;
	protected JTextField jLabelProductVersion;
	protected JLabel jLabelLabelProductVersion;
	protected JLabel jLabelProductName;

	private JLabel dependenciesTextLabel;
	private JButton dependenciesActivateButton;
	private JLabel depotForDependenciesLabel;
	private PanelProductDependencies panelProductDependencies;
	private boolean isPanelProductDependenciesVisible;

	private AbstractPanelEditProperties panelEditProperties;
	private JButton propertiesActivateButton;
	private boolean isPanelEditPropertiesVisible = true;

	protected TextMarkdownPane jTextAreaProductAdvice;
	protected TextMarkdownPane jTextAreaProductInfo;

	protected String productName = "";
	private Map<String, Boolean> specificPropertiesExisting;

	protected ConfigedMain mainController;

	/** Creates new ProductInfoPane */
	public ProductInfoPane(ConfigedMain mainController, AbstractPanelEditProperties panelEditProperties) {
		super(JSplitPane.VERTICAL_SPLIT);
		this.mainController = mainController;
		this.panelEditProperties = panelEditProperties;
		initComponents();
	}

	/** Creates new ProductInfoPane */
	public ProductInfoPane(AbstractPanelEditProperties panelEditProperties) {
		this(null, panelEditProperties);
	}

	private void initComponents() {
		jLabelProductName = new JLabel();
		jLabelProductID = new JTextField();
		jLabelProductVersion = new JTextField();
		jLabelLabelProductVersion = new JLabel();

		JScrollPane jScrollPaneProductInfo = new JScrollPane();
		jTextAreaProductInfo = new TextMarkdownPane();

		JScrollPane jScrollPaneProductAdvice = new JScrollPane();
		jTextAreaProductAdvice = new TextMarkdownPane();

		dependenciesActivateButton = new JButton();
		dependenciesTextLabel = new JLabel();
		depotForDependenciesLabel = new JLabel();
		panelProductDependencies = new PanelProductDependencies(mainController, depotForDependenciesLabel);

		propertiesActivateButton = new JButton();

		// do this so that you can mark and copy content of the label
		jLabelProductID.setFont(Globals.defaultFontStandardBold);
		jLabelProductID.setBorder(null);
		jLabelProductID.setEditable(false);
		if (!ConfigedMain.OPSI_4_3) {
			jLabelProductID.setBackground(null);
		}

		jLabelProductName.setFont(Globals.defaultFontBold);

		jLabelLabelProductVersion.setFont(Globals.defaultFontBig);
		jLabelLabelProductVersion.setText(Configed.getResourceValue("ProductInfoPane.jLabelProductVersion") + " ");

		// do this so that you can mark and copy content of the label
		jLabelProductVersion.setFont(Globals.defaultFontBold);
		jLabelProductVersion.setBorder(null);
		jLabelProductVersion.setEditable(false);
		if (!ConfigedMain.OPSI_4_3) {
			jLabelProductVersion.setBackground(null);
		}

		jTextAreaProductInfo.setFont(Globals.defaultFont);
		if (!ConfigedMain.OPSI_4_3) {
			jTextAreaProductInfo.setBackground(Globals.BACKGROUND_COLOR_3);
		}

		jScrollPaneProductInfo.setViewportView(jTextAreaProductInfo);
		jScrollPaneProductInfo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		jTextAreaProductAdvice.setFont(Globals.defaultFont);
		if (!ConfigedMain.OPSI_4_3) {
			jTextAreaProductAdvice.setBackground(Globals.BACKGROUND_COLOR_3);
		}

		jScrollPaneProductAdvice.setViewportView(jTextAreaProductAdvice);
		jScrollPaneProductAdvice.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductAdvice.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		dependenciesTextLabel.setText(Configed.getResourceValue("ProductInfoPane.dependenciesTextLabel"));
		dependenciesTextLabel.setFont(Globals.defaultFontBold);
		if (!ConfigedMain.OPSI_4_3) {
			dependenciesTextLabel.setForeground(Globals.greyed);
		}

		depotForDependenciesLabel.setFont(Globals.defaultFontBold);
		if (!ConfigedMain.OPSI_4_3) {
			depotForDependenciesLabel.setForeground(Globals.greyed);
		}

		dependenciesActivateButton.setText("▶");
		dependenciesActivateButton.setFont(Globals.defaultFont);
		if (!ConfigedMain.OPSI_4_3) {
			dependenciesActivateButton.setForeground(Globals.lightBlack);
		}
		dependenciesActivateButton.addActionListener(this);

		panelProductDependencies.setVisible(isPanelProductDependenciesVisible);

		propertiesActivateButton.setText("▼");
		propertiesActivateButton.setFont(Globals.defaultFont);
		if (!ConfigedMain.OPSI_4_3) {
			propertiesActivateButton.setForeground(Globals.lightBlack);
		}
		propertiesActivateButton.addActionListener(this);

		panelEditProperties.setVisible(isPanelEditPropertiesVisible);

		productDescriptionsPanel = new JXPanel();

		GroupLayout layoutDescriptionsPanel = new GroupLayout(productDescriptionsPanel);
		productDescriptionsPanel.setLayout(layoutDescriptionsPanel);

		layoutDescriptionsPanel.setHorizontalGroup(layoutDescriptionsPanel.createParallelGroup(Alignment.LEADING)

				.addComponent(jLabelProductID, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE)

				.addGroup(layoutDescriptionsPanel.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jLabelProductName, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))

				.addGroup(layoutDescriptionsPanel.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(jLabelLabelProductVersion, Globals.MIN_HSIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelProductVersion, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE))

				.addComponent(jScrollPaneProductInfo, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE)

				.addComponent(jScrollPaneProductAdvice, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE)

		);

		layoutDescriptionsPanel.setVerticalGroup(layoutDescriptionsPanel.createSequentialGroup()
				.addComponent(jLabelProductID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addComponent(jLabelProductName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(layoutDescriptionsPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(jLabelLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(0, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(jScrollPaneProductInfo, 0, Globals.PREF_VSIZE, Short.MAX_VALUE)
				.addGap(0, Globals.VGAP_SIZE, Globals.VGAP_SIZE)
				.addComponent(jScrollPaneProductAdvice, 0, Globals.PREF_VSIZE, Short.MAX_VALUE));

		// treat the bottom panel
		bottomComponent = new JXPanel();

		GroupLayout layoutBottomComponent = new GroupLayout(bottomComponent);
		bottomComponent.setLayout(layoutBottomComponent);

		layoutBottomComponent.setHorizontalGroup(layoutBottomComponent.createParallelGroup()
				.addGroup(layoutBottomComponent.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(dependenciesActivateButton, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(dependenciesTextLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(depotForDependenciesLabel, 0, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE))

				.addComponent(panelProductDependencies)

				.addGroup(layoutBottomComponent.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(propertiesActivateButton)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(panelEditProperties.getTitlePanel()))
				.addComponent(panelEditProperties));

		layoutBottomComponent.setVerticalGroup(layoutBottomComponent.createSequentialGroup()
				.addGap(Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE, Globals.MIN_VGAP_SIZE)
				.addGroup(layoutBottomComponent.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(dependenciesActivateButton, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(dependenciesTextLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(depotForDependenciesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addComponent(panelProductDependencies, 0, 0, Short.MAX_VALUE)
				.addGroup(layoutBottomComponent.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(propertiesActivateButton, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(panelEditProperties.getTitlePanel()))
				.addComponent(panelEditProperties, 0, 0, Short.MAX_VALUE));

		this.setTopComponent(productDescriptionsPanel);
		this.setBottomComponent(bottomComponent);

	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == dependenciesActivateButton) {
			isPanelProductDependenciesVisible = !isPanelProductDependenciesVisible;

			dependenciesActivateButton.setText(isPanelProductDependenciesVisible ? "▼" : "▶");
			if (!ConfigedMain.OPSI_4_3) {
				dependenciesTextLabel
						.setForeground(isPanelProductDependenciesVisible ? Globals.PANEL_PRODUCT_INFO_PANE_ACTIVE
								: Globals.PANEL_PRODUCT_INFO_PANE_INACTIVE);
			}

			if (!ConfigedMain.OPSI_4_3) {
				depotForDependenciesLabel.setEnabled(isPanelProductDependenciesVisible);
				depotForDependenciesLabel
						.setForeground(isPanelProductDependenciesVisible ? Globals.PANEL_PRODUCT_INFO_PANE_ACTIVE
								: Globals.PANEL_PRODUCT_INFO_PANE_INACTIVE);
			}

			panelProductDependencies.setVisible(isPanelProductDependenciesVisible);
		} else if (event.getSource() == propertiesActivateButton) {
			isPanelEditPropertiesVisible = !isPanelEditPropertiesVisible;

			propertiesActivateButton.setText(isPanelEditPropertiesVisible ? "▼" : "▶");

			panelEditProperties.setVisible(isPanelEditPropertiesVisible);
			panelEditProperties.setTitlePanelActivated(isPanelEditPropertiesVisible);
		}
	}

	protected String fillEmpty(String content) {
		if (content == null || content.equals("") || content.equals("-")) {
			return " ";
		}

		return content;
	}

	public PanelProductDependencies getPanelProductDependencies() {
		return panelProductDependencies;
	}

	public void setProductAdvice(String s) {
		jTextAreaProductAdvice.setText(s);
	}

	public void setProductInfo(String s) {
		jTextAreaProductInfo.setText(s);
	}

	public void setProductId(String s) {
		jLabelProductID.setText(s);
		productName = s;
	}

	public void setProductVersion(String s) {
		jLabelProductVersion.setText(fillEmpty(s));
	}

	public void setProductName(String s) {
		jLabelProductName.setText(s);
	}

	public void setGrey(boolean grey) {

		float alpha;

		if (grey) {
			alpha = 0.1F;
		} else {
			alpha = 1;
		}

		if (productDescriptionsPanel != null) {
			productDescriptionsPanel.setAlpha(alpha);
		}

		if (panelEditProperties != null) {
			panelEditProperties.setAlpha(alpha);
		}

		if (bottomComponent != null) {
			bottomComponent.setAlpha(alpha);
		}
	}

	public void setEditValues(String productId, String productVersion, String packageVersion, String depotId) {

		setGrey(false);
		setProductId(productId);
		setProductVersion(productVersion + "-" + packageVersion);

		if (mainController != null) {
			String versionInfo = OpsiPackage.produceVersionInfo(productVersion, packageVersion);
			OpsiProductInfo info = mainController.getPersistenceController().getProduct2versionInfo2infos()
					.get(productId).get(versionInfo);
			Logging.info(this,
					"got product infos  productId, versionInfo:  " + productId + ", " + versionInfo + ": " + info);

			setProductName(info.getProductName());
			setProductInfo(info.getDescription());
			setProductAdvice(info.getAdvice());
		}

		panelProductDependencies.setEditValues(productId, depotId);
	}

	public void clearEditing() {

		setGrey(false);
		setProductId("");
		setProductVersion("");

		setProductName("");
		setProductInfo("");
		panelProductDependencies.clearEditing();
	}

	//
	// DataChangedObserver
	@Override
	public void dataHaveChanged(Object source) {

		if (source instanceof de.uib.utilities.datapanel.EditMapPanelX) {
			specificPropertiesExisting.put(productName, true);
		}
	}
}
