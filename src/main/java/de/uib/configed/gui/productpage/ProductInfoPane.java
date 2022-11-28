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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.HyperlinkEvent;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
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
	private javax.swing.JLabel jLabelProductAdvice;
	private javax.swing.JLabel jLabelProductDescription;
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

	private javax.swing.JScrollPane jScrollPaneProductAdvice;
	private javax.swing.JScrollPane jScrollPaneProductInfo;
	protected javax.swing.JTextArea jTextAreaProductAdvice;
	protected JTextPane jTextAreaProductInfo;

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
		// jTextField_SelectedClients = new javax.swing.JTextField();
		jLabelProductName = new javax.swing.JLabel();
		jLabelProductID = new javax.swing.JTextField();
		jLabelProductVersion = new javax.swing.JTextField();
		jLabelLabelProductVersion = new javax.swing.JLabel();
		// jLabelPackageVersion = new javax.swing.JLabel();
		jLabelProductDescription = new javax.swing.JLabel();
		jScrollPaneProductInfo = new javax.swing.JScrollPane();
		jTextAreaProductInfo = new javax.swing.JTextPane();
		jLabelProductAdvice = new javax.swing.JLabel();
		jScrollPaneProductAdvice = new javax.swing.JScrollPane();
		jTextAreaProductAdvice = new javax.swing.JTextArea();

		dependenciesActivateButton = new JButton();
		dependenciesTextLabel = new JLabel();
		depotForDependenciesLabel = new JLabel();
		panelProductDependencies = new PanelProductDependencies(mainController, depotForDependenciesLabel);

		propertiesActivateButton = new JButton();

		/*
		 * jTextField_SelectedClients.setEditable(false);
		 * jTextField_SelectedClients.setFont(Globals.defaultFontBig);
		 * jTextField_SelectedClients.setText(" ");
		 * jTextField_SelectedClients.setBackground(Globals.backgroundLightGrey);
		 */

		// jLabelProductName.setFont(Globals.defaultFontBig);
		// jLabelProductName.setText(
		// configed.getResourceValue("MainFrame.labelProductId") );

		jLabelProductID.setFont(Globals.defaultFontStandardBold);
		jLabelProductID.setBorder(null);
		jLabelProductID.setEditable(false);
		jLabelProductID.setBackground(null);

		jLabelProductName.setFont(Globals.defaultFontBold);

		jLabelLabelProductVersion.setFont(Globals.defaultFontBig);
		jLabelLabelProductVersion.setText(configed.getResourceValue("ProductInfoPane.jLabelProductVersion") + " ");

		jLabelProductVersion.setFont(Globals.defaultFontBold);
		jLabelProductVersion.setBorder(null);
		jLabelProductVersion.setEditable(false);
		jLabelProductVersion.setBackground(null);

		jLabelProductDescription.setFont(Globals.defaultFontStandardBold);
		jLabelProductDescription.setPreferredSize(new Dimension(Globals.prefHSize, Globals.lineHeight));
		jLabelProductDescription.setText(configed.getResourceValue("ProductInfoPane.jLabelProductDescription"));

		//jTextAreaProductInfo.setColumns(20);
		//jTextAreaProductInfo.setRows(5);

		jTextAreaProductInfo.setEditable(false);
		jTextAreaProductInfo.setContentType("text/html");
		jTextAreaProductInfo.addHyperlinkListener(e -> {
			if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType())) {
				Desktop desktop = Desktop.getDesktop();

				// This will now try to open the standard browser with link;
				// if not possible, try to open firefox with the link
				// And if even that is not possible, show Message Dialog with link
				if (desktop.isSupported(Desktop.Action.BROWSE)) {
					try {
						desktop.browse(e.getURL().toURI());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					try {
						Process process = new ProcessBuilder("firefox", e.getURL().toString()).start();

						int exitVal = process.waitFor();
						if (exitVal != 0) {
							JTextPane pa = new JTextPane();
							pa.setEditable(false);
							pa.setText("Browser zum öffnen von \n" + e.getURL().toString()
									+ "\nkann nicht gefunden werden. Bitte manuell durchführen");
							JOptionPane.showMessageDialog(this, pa);
						}
					} catch (IOException ioe) {
						JTextPane pa = new JTextPane();
						pa.setEditable(false);
						pa.setText("Browser zum öffnen von \n" + e.getURL().toString()
								+ "\nkann nicht gefunden werden. Bitte manuell durchführen");
						JOptionPane.showMessageDialog(this, pa);

					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
			}

		});

		//jTextAreaProductInfo.setWrapStyleWord(true);
		//jTextAreaProductInfo.setLineWrap(true);
		Parser parser = Parser.builder().build();
		Node document = parser.parse(
				"Das hier ist ein Abschnitt über Farben. Im folgenden Abschnitt möchten wir einige Beispiele für "
						+ " Farben geben und anschließend noch weitere Ressourcen für weiterreichende Informationen geben.\n## Farben\n * Rot\n"
						+ "* Grün\n" + "* Gelb\n"
						+ "\nFür mehr Farben besuchen Sie doch unsere [Website](https://www.uib.de)");
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		String html = renderer.render(document);

		html = html.replace("<p>", "");
		html = html.replace("</p>", "");

		jTextAreaProductInfo.setText(html);

		jTextAreaProductInfo.setFont(Globals.defaultFont);
		jTextAreaProductInfo.setBackground(Globals.backgroundLightGrey);

		jScrollPaneProductInfo.setViewportView(jTextAreaProductInfo);
		jScrollPaneProductInfo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// jScrollPaneProductInfo.setVerticalScrollBarPolicy(
		// JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPaneProductInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		jLabelProductAdvice.setText(configed.getResourceValue("ProductInfoPane.jLabelProductAdvice"));
		jLabelProductAdvice.setFont(Globals.defaultFontStandardBold);

		jTextAreaProductAdvice.setColumns(20);
		jTextAreaProductAdvice.setRows(5);

		jTextAreaProductAdvice.setEditable(false);
		jTextAreaProductAdvice.setWrapStyleWord(true);
		jTextAreaProductAdvice.setLineWrap(true);
		jTextAreaProductAdvice.setFont(Globals.defaultFont);
		jTextAreaProductAdvice.setBackground(Globals.backgroundLightGrey);

		jScrollPaneProductAdvice.setViewportView(jTextAreaProductAdvice);
		jScrollPaneProductAdvice.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		// jScrollPaneProductAdvice.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
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
				// .addComponent(jLabelProductDescription, minLabelVSize, Globals.buttonHeight,
				// Globals.buttonHeight)
				// .addGap(minGapVSize, minGapVSize, minGapVSize)
				.addComponent(jScrollPaneProductInfo, 0, Globals.prefVSize, Globals.prefVSize)
				.addGap(0, Globals.vGapSize, Globals.vGapSize)
				// .addComponent(jLabelProductAdvice, minLabelVSize, Globals.buttonHeight,
				// Globals.buttonHeight)
				// .addGap(minGapVSize, minGapVSize, minGapVSize)
				.addComponent(jScrollPaneProductAdvice, 0, Globals.prefVSize, Globals.prefVSize)

		// .addComponent(panelProductDependencies, 0, 0, Short.MAX_VALUE)
		);

		// setTopComponent(productDescriptionsPanel);

		// treat the south panel
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
		//jTextAreaProductInfo.setText(s);
		jTextAreaProductInfo.setCaretPosition(0);
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
