/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.Font;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.logging.Logging;

public class ProductInfoPane extends JSplitPane {
	private static final Font ACTIVATE_BUTTON_FONT = new Font("TimesRoman", Font.PLAIN, 14);

	private JTextField jLabelProductID;
	private JTextField jLabelProductVersion;
	private JLabel jLabelLabelProductVersion;
	private JLabel jLabelProductName;

	private JLabel dependenciesTextLabel;
	private JButton dependenciesActivateButton;
	private JLabel depotForDependenciesLabel;
	private PanelProductDependencies panelProductDependencies;
	private boolean isPanelProductDependenciesVisible;

	private AbstractPanelEditProperties panelEditProperties;
	private JButton propertiesActivateButton;
	private boolean isPanelEditPropertiesVisible = true;

	private JScrollPane jScrollPaneProductInfo;
	private TextMarkdownPane jTextAreaProductAdvice;

	private JScrollPane jScrollPaneProductAdvice;
	private TextMarkdownPane jTextAreaProductInfo;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	/** Creates new ProductInfoPane */
	public ProductInfoPane(AbstractPanelEditProperties panelEditProperties) {
		super(JSplitPane.VERTICAL_SPLIT);
		this.panelEditProperties = panelEditProperties;
		initComponents();
		setupLayout();
	}

	private void initComponents() {
		jLabelProductName = new JLabel();
		jLabelProductID = new JTextField();
		jLabelProductVersion = new JTextField();
		jLabelLabelProductVersion = new JLabel();

		jScrollPaneProductInfo = new JScrollPane();
		jTextAreaProductInfo = new TextMarkdownPane();

		jScrollPaneProductAdvice = new JScrollPane();
		jTextAreaProductAdvice = new TextMarkdownPane();

		dependenciesTextLabel = new JLabel(Configed.getResourceValue("ProductInfoPane.dependenciesTextLabel"));
		depotForDependenciesLabel = new JLabel();
		panelProductDependencies = new PanelProductDependencies(depotForDependenciesLabel);

		// do this so that you can mark and copy content of the label
		jLabelProductID.setFont(jLabelProductID.getFont().deriveFont(Font.BOLD));

		jLabelProductID.setBorder(null);
		jLabelProductID.setEditable(false);

		jLabelLabelProductVersion.setText(Configed.getResourceValue("ProductInfoPane.jLabelProductVersion") + " ");

		// do this so that you can mark and copy content of the label
		jLabelProductVersion.setBorder(null);
		jLabelProductVersion.setEditable(false);

		jScrollPaneProductInfo.setViewportView(jTextAreaProductInfo);
		jScrollPaneProductInfo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		jScrollPaneProductAdvice.setViewportView(jTextAreaProductAdvice);
		jScrollPaneProductAdvice.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductAdvice.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		dependenciesActivateButton = new JButton("▶");
		dependenciesActivateButton.setFont(ACTIVATE_BUTTON_FONT);
		dependenciesActivateButton.addActionListener(event -> toggleDependenciesActive());

		panelProductDependencies.setVisible(isPanelProductDependenciesVisible);

		propertiesActivateButton = new JButton("▼");
		propertiesActivateButton.setFont(ACTIVATE_BUTTON_FONT);
		propertiesActivateButton.addActionListener(event -> togglePropertiesActive());

		panelEditProperties.setVisible(isPanelEditPropertiesVisible);
	}

	private void setupLayout() {
		setupTopComponent();
		setupBottomComponent();
	}

	private void setupTopComponent() {
		JPanel productDescriptionsPanel = new JPanel();

		GroupLayout layoutDescriptionsPanel = new GroupLayout(productDescriptionsPanel);
		productDescriptionsPanel.setLayout(layoutDescriptionsPanel);

		layoutDescriptionsPanel.setHorizontalGroup(layoutDescriptionsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(layoutDescriptionsPanel.createSequentialGroup()
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE)
						.addComponent(jLabelProductID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.MIN_GAP_SIZE, Globals.MIN_GAP_SIZE, Short.MAX_VALUE))

				.addGroup(layoutDescriptionsPanel.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelProductName, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE))

				.addGroup(layoutDescriptionsPanel.createSequentialGroup().addGap(Globals.GAP_SIZE)
						.addComponent(jLabelLabelProductVersion, Globals.MIN_HSIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelProductVersion, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE))

				.addComponent(jScrollPaneProductInfo, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE)

				.addComponent(jScrollPaneProductAdvice, Globals.MIN_HSIZE, Globals.PREF_HSIZE, Short.MAX_VALUE));

		layoutDescriptionsPanel.setVerticalGroup(layoutDescriptionsPanel.createSequentialGroup()
				.addGap(Globals.MIN_GAP_SIZE)
				.addComponent(jLabelProductID, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(jLabelProductName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.MIN_GAP_SIZE)
				.addGroup(layoutDescriptionsPanel.createParallelGroup(Alignment.LEADING)
						.addComponent(jLabelLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(jLabelProductVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE))
				.addGap(0, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(jScrollPaneProductInfo, 0, Globals.PREF_VSIZE, Short.MAX_VALUE)
				.addGap(0, Globals.GAP_SIZE, Globals.GAP_SIZE)
				.addComponent(jScrollPaneProductAdvice, 0, Globals.PREF_VSIZE, Short.MAX_VALUE));

		setTopComponent(productDescriptionsPanel);
	}

	private void setupBottomComponent() {
		// treat the bottom panel
		JPanel bottomComponent = new JPanel();

		GroupLayout layoutBottomComponent = new GroupLayout(bottomComponent);
		bottomComponent.setLayout(layoutBottomComponent);

		layoutBottomComponent
				.setHorizontalGroup(
						layoutBottomComponent.createParallelGroup()
								.addGroup(layoutBottomComponent.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(dependenciesActivateButton, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE)
										.addComponent(dependenciesTextLabel, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.GAP_SIZE, Globals.GAP_SIZE, Short.MAX_VALUE)
										.addComponent(depotForDependenciesLabel, 0, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE)
										.addGap(Globals.MIN_GAP_SIZE))

								.addComponent(panelProductDependencies)

								.addGroup(layoutBottomComponent.createSequentialGroup().addGap(Globals.GAP_SIZE)
										.addComponent(propertiesActivateButton).addGap(Globals.GAP_SIZE)
										.addComponent(panelEditProperties.getTitlePanel()))

								.addComponent(panelEditProperties));

		layoutBottomComponent
				.setVerticalGroup(layoutBottomComponent.createSequentialGroup().addGap(Globals.MIN_GAP_SIZE)
						.addGroup(layoutBottomComponent.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(dependenciesActivateButton, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(dependenciesTextLabel, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(depotForDependenciesLabel, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(panelProductDependencies, 0, 0, Short.MAX_VALUE)
						.addGroup(layoutBottomComponent.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addComponent(propertiesActivateButton, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
										Globals.BUTTON_HEIGHT)
								.addComponent(panelEditProperties.getTitlePanel()))
						.addComponent(panelEditProperties, 0, 0, Short.MAX_VALUE));

		setBottomComponent(bottomComponent);
	}

	private void toggleDependenciesActive() {
		isPanelProductDependenciesVisible = !isPanelProductDependenciesVisible;

		setActivatedButton(dependenciesActivateButton, isPanelProductDependenciesVisible);

		panelProductDependencies.setVisible(isPanelProductDependenciesVisible);
	}

	private void togglePropertiesActive() {
		isPanelEditPropertiesVisible = !isPanelEditPropertiesVisible;

		setActivatedButton(propertiesActivateButton, isPanelEditPropertiesVisible);

		panelEditProperties.setVisible(isPanelEditPropertiesVisible);
		panelEditProperties.setTitlePanelActivated(isPanelEditPropertiesVisible);
	}

	private static void setActivatedButton(JButton jButton, boolean isPropertiesVisible) {
		if (isPropertiesVisible) {
			jButton.setText("▼");
		} else {
			jButton.setText("▶");
		}
	}

	private static String fillEmpty(String content) {
		if (content == null || content.isEmpty() || "-".equals(content)) {
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
	}

	public void setProductVersion(String s) {
		jLabelProductVersion.setText(fillEmpty(s));
	}

	public void setProductName(String s) {
		jLabelProductName.setText(s);
	}

	public void setEditValues(String productId, String productVersion, String packageVersion, String depotId) {
		setProductId(productId);
		setProductVersion(productVersion + "-" + packageVersion);

		String versionInfo = OpsiPackage.produceVersionInfo(productVersion, packageVersion);
		OpsiProductInfo info = persistenceController.getProductDataService().getProduct2VersionInfo2InfosPD()
				.get(productId).get(versionInfo);
		Logging.info(this,
				"got product infos  productId, versionInfo:  " + productId + ", " + versionInfo + ": " + info);

		setProductName(info.getProductName());
		setProductInfo(info.getDescription());
		setProductAdvice(info.getAdvice());

		panelProductDependencies.setEditValues(productId, depotId);
	}

	public void clearEditing() {
		setProductId("");
		setProductVersion("");

		setProductName("");
		setProductInfo("");
		setProductAdvice("");
		panelProductDependencies.setEditValues("", "");
	}
}
