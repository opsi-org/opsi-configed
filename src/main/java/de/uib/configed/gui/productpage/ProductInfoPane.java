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
 
 */

package de.uib.configed.gui.productpage;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

import org.jdesktop.swingx.JXPanel;

import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.utilities.logging.logging;

/**
 * @author roeder
 */
public class ProductInfoPane extends javax.swing.JSplitPane
		implements de.uib.utilities.DataChangedObserver, ActionListener {

	private JXPanel productDescriptionsPanel;
	private JXPanel bottomComponent;
	protected javax.swing.JTextField jLabelProductID;
	protected javax.swing.JTextField jLabelProductVersion;
	protected javax.swing.JLabel jLabelLabelProductVersion;
	protected javax.swing.JLabel jLabelProductName;

	private JLabel dependenciesTextLabel;
	private JButton dependenciesActivateButton;
	private JLabel depotForDependenciesLabel;
	private PanelProductDependencies panelProductDependencies;
	private boolean isPanelProductDependenciesVisible = false;

	private DefaultPanelEditProperties panelEditProperties;
	private JButton propertiesActivateButton;
	private boolean isPanelEditPropertiesVisible = true;

	protected TextMarkdownPane jTextAreaProductAdvice;
	protected TextMarkdownPane jTextAreaProductInfo;

	protected String productName = "";
	private Map<String, Boolean> specificPropertiesExisting;

	protected ConfigedMain mainController;

	/** Creates new ProductInfoPane */
	public ProductInfoPane(ConfigedMain mainController, DefaultPanelEditProperties panelEditProperties) {
		super(JSplitPane.VERTICAL_SPLIT);
		this.mainController = mainController;
		this.panelEditProperties = panelEditProperties;
		initComponents();
	}

	/** Creates new ProductInfoPane */
	public ProductInfoPane(DefaultPanelEditProperties panelEditProperties) {
		this(null, panelEditProperties);
	}

	private void initComponents() {
		jLabelProductName = new javax.swing.JLabel();
		jLabelProductID = new javax.swing.JTextField();
		jLabelProductVersion = new javax.swing.JTextField();
		jLabelLabelProductVersion = new javax.swing.JLabel();

		JScrollPane jScrollPaneProductInfo = new javax.swing.JScrollPane();
		jTextAreaProductInfo = new TextMarkdownPane();

		JScrollPane jScrollPaneProductAdvice = new javax.swing.JScrollPane();
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
		jLabelProductID.setBackground(null);

		jLabelProductName.setFont(Globals.defaultFontBold);

		jLabelLabelProductVersion.setFont(Globals.defaultFontBig);
		jLabelLabelProductVersion.setText(configed.getResourceValue("ProductInfoPane.jLabelProductVersion") + " ");

		// do this so that you can mark and copy content of the label
		jLabelProductVersion.setFont(Globals.defaultFontBold);
		jLabelProductVersion.setBorder(null);
		jLabelProductVersion.setEditable(false);
		jLabelProductVersion.setBackground(null);

		jTextAreaProductInfo.setFont(Globals.defaultFont);
		jTextAreaProductInfo.setBackground(Globals.backgroundLightGrey);

		jScrollPaneProductInfo.setViewportView(jTextAreaProductInfo);
		jScrollPaneProductInfo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		jTextAreaProductAdvice.setFont(Globals.defaultFont);
		jTextAreaProductAdvice.setBackground(Globals.backgroundLightGrey);

		jScrollPaneProductAdvice.setViewportView(jTextAreaProductAdvice);
		jScrollPaneProductAdvice.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductAdvice.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		dependenciesTextLabel.setText(configed.getResourceValue("ProductInfoPane.dependenciesTextLabel"));
		dependenciesTextLabel.setFont(Globals.defaultFontBold);
		dependenciesTextLabel.setForeground(Globals.greyed);

		depotForDependenciesLabel.setFont(Globals.defaultFontBold);
		depotForDependenciesLabel.setForeground(Globals.greyed);

		dependenciesActivateButton.setText("▶");
		dependenciesActivateButton.setFont(Globals.defaultFont);
		dependenciesActivateButton.setForeground(Globals.lightBlack);
		dependenciesActivateButton.addActionListener(this);

		panelProductDependencies.setVisible(isPanelProductDependenciesVisible);

		propertiesActivateButton.setText("▼");
		propertiesActivateButton.setFont(Globals.defaultFont);
		propertiesActivateButton.setForeground(Globals.lightBlack);
		propertiesActivateButton.addActionListener(this);

		panelEditProperties.setVisible(isPanelEditPropertiesVisible);

		productDescriptionsPanel = new JXPanel();

		javax.swing.GroupLayout layoutDescriptionsPanel = new javax.swing.GroupLayout(productDescriptionsPanel);
		productDescriptionsPanel.setLayout(layoutDescriptionsPanel);

		layoutDescriptionsPanel.setHorizontalGroup(
				layoutDescriptionsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)

						.addComponent(jLabelProductID, Globals.minHSize, Globals.prefHSize, Short.MAX_VALUE)

						.addGroup(layoutDescriptionsPanel.createSequentialGroup()
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jLabelProductName, Globals.minHSize, Globals.prefHSize, Short.MAX_VALUE)
								.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))

						.addGroup(layoutDescriptionsPanel.createSequentialGroup()
								.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
								.addComponent(jLabelLabelProductVersion, Globals.minHSize, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelProductVersion, Globals.minHSize, Globals.prefHSize,
										Short.MAX_VALUE)
								.addGap(Globals.minHGapSize, Globals.minHGapSize, Globals.minHGapSize))

						.addComponent(jScrollPaneProductInfo, Globals.minHSize, Globals.prefHSize, Short.MAX_VALUE)

						.addComponent(jScrollPaneProductAdvice, Globals.minHSize, Globals.prefHSize, Short.MAX_VALUE)

		);

		layoutDescriptionsPanel.setVerticalGroup(layoutDescriptionsPanel.createSequentialGroup()
				.addComponent(jLabelProductID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addComponent(jLabelProductName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addGroup(layoutDescriptionsPanel.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(jLabelLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(0, Globals.vGapSize, Globals.vGapSize)
				.addComponent(jScrollPaneProductInfo, 0, Globals.prefVSize, Short.MAX_VALUE)
				.addGap(0, Globals.vGapSize, Globals.vGapSize)
				.addComponent(jScrollPaneProductAdvice, 0, Globals.prefVSize, Short.MAX_VALUE));

		// treat the bottom panel
		bottomComponent = new JXPanel();

		GroupLayout layoutBottomComponent = new javax.swing.GroupLayout(bottomComponent);
		bottomComponent.setLayout(layoutBottomComponent);

		layoutBottomComponent
				.setHorizontalGroup(
						layoutBottomComponent.createParallelGroup()
								.addGroup(layoutBottomComponent.createSequentialGroup()
										.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
										.addComponent(dependenciesActivateButton, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
										.addComponent(dependenciesTextLabel, Globals.minHSize,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(0, 0, Short.MAX_VALUE)
										.addComponent(depotForDependenciesLabel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize))

								.addComponent(panelProductDependencies)

								.addGroup(layoutBottomComponent.createSequentialGroup()
										.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
										.addComponent(propertiesActivateButton)
										.addGap(Globals.hGapSize, Globals.hGapSize, Globals.hGapSize)
										.addComponent(panelEditProperties.getTitlePanel()))
								.addComponent(panelEditProperties));

		layoutBottomComponent.setVerticalGroup(layoutBottomComponent.createSequentialGroup()
				.addGap(Globals.minVGapSize, Globals.minVGapSize, Globals.minVGapSize)
				.addGroup(layoutBottomComponent.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(dependenciesActivateButton, Globals.buttonHeight, Globals.buttonHeight,
								Globals.buttonHeight)
						.addComponent(dependenciesTextLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(depotForDependenciesLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addComponent(panelProductDependencies, 0, 0, Short.MAX_VALUE)
				.addGroup(layoutBottomComponent.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(propertiesActivateButton, Globals.buttonHeight, Globals.buttonHeight,
								Globals.buttonHeight)
						.addComponent(panelEditProperties.getTitlePanel()))
				.addComponent(panelEditProperties, 0, 0, Short.MAX_VALUE));

		this.setTopComponent(productDescriptionsPanel);
		this.setBottomComponent(bottomComponent);
		// setDividerLocation(250);

	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == dependenciesActivateButton) {
			isPanelProductDependenciesVisible = !isPanelProductDependenciesVisible;

			dependenciesActivateButton.setText(isPanelProductDependenciesVisible ? "▼" : "▶");
			dependenciesTextLabel.setForeground(isPanelProductDependenciesVisible ? Color.BLACK : Globals.greyed);

			panelProductDependencies.setVisible(isPanelProductDependenciesVisible);
			depotForDependenciesLabel.setForeground(isPanelProductDependenciesVisible ? Color.BLACK : Globals.greyed);
		}

		else if (event.getSource() == propertiesActivateButton) {
			isPanelEditPropertiesVisible = !isPanelEditPropertiesVisible;

			propertiesActivateButton.setText(isPanelEditPropertiesVisible ? "▼" : "▶");

			panelEditProperties.setVisible(isPanelEditPropertiesVisible);
			panelEditProperties.setTitlePanelActivated(isPanelEditPropertiesVisible);
		}
	}

	protected String fillEmpty(String content) {
		if (content == null || content.equals("") || content.equals("-"))
			return " ";

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

	public void setGrey(boolean b) {
		float alpha = (float) 1.0f;
		if (b) {
			alpha = (float) .1f;
		}

		if (productDescriptionsPanel != null)
			productDescriptionsPanel.setAlpha(alpha);

		if (panelEditProperties != null)
			panelEditProperties.setAlpha(alpha);

		if (bottomComponent != null)
			bottomComponent.setAlpha(alpha);
	}

	public void setEditValues(String productId, String productVersion, String packageVersion, String depotId) {

		setGrey(false);
		setProductId(productId);
		setProductVersion(productVersion + "-" + packageVersion);

		if (mainController != null) {
			String versionInfo = OpsiPackage.produceVersionInfo(productVersion, packageVersion);
			OpsiProductInfo info = mainController.getPersistenceController().getProduct2versionInfo2infos()
					.get(productId).get(versionInfo);
			logging.info(this,
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
	public void dataHaveChanged(Object source) {
		// logging.debug(this, "dataHaveChanged " + source );
		if (source instanceof de.uib.utilities.datapanel.EditMapPanelX) {
			specificPropertiesExisting.put(productName, true);
		}
	}
}
