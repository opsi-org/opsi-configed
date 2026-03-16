/**
 * Copyright (c) UIB GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.features.productpage;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;

import de.uib.configed.core.domain.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.configed.core.domain.serverdata.PersistenceControllerFactory;
import de.uib.configed.gui.Configed;
import de.uib.configed.gui.Globals;
import de.uib.configed.gui.features.productpage.PanelProductSettings.ProductSettingsType;
import de.uib.configed.gui.type.OpsiPackage;
import de.uib.configed.gui.type.OpsiProductInfo;
import de.uib.configed.share.Icons;
import de.uib.configed.share.SplitPaneStateManager;
import de.uib.configed.share.logging.Logging;
import net.miginfocom.swing.MigLayout;

public class ProductInfoPane extends JSplitPane {
	private static final int START_DIVIDER_LOCATION = 260;

	private JTextField jLabelProductID;
	private JTextField jLabelProductVersion;
	private JLabel jLabelLabelProductVersion;
	private JLabel jLabelProductName;

	private JLabel dependenciesTextLabel;
	private JToggleButton dependenciesActivateButton;
	private JLabel depotForDependenciesLabel;
	private PanelProductDependencies panelProductDependencies;

	private AbstractPanelEditProperties panelEditProperties;
	private JToggleButton propertiesActivateButton;

	private JSplitPane productSplitPane;
	private TextMarkdownPane jTextAreaProductAdvice;

	private TextMarkdownPane jTextAreaProductInfo;

	private OpsiServiceNOMPersistenceController persistenceController = PersistenceControllerFactory
			.getPersistenceController();

	private ProductSettingsType type;

	/** Creates new ProductInfoPane */
	public ProductInfoPane(AbstractPanelEditProperties panelEditProperties, ProductSettingsType type) {
		super(JSplitPane.VERTICAL_SPLIT);
		this.type = type;
		this.panelEditProperties = panelEditProperties;
		initComponents();
		setupLayout();
	}

	private void initComponents() {
		jLabelProductName = new JLabel();
		jLabelProductID = new JTextField();
		jLabelProductVersion = new JTextField();
		jLabelLabelProductVersion = new JLabel(Configed.getResourceValue("ProductInfoPane.jLabelProductVersion"));

		jTextAreaProductInfo = new TextMarkdownPane();

		jTextAreaProductAdvice = new TextMarkdownPane();

		dependenciesTextLabel = new JLabel(Configed.getResourceValue("ProductInfoPane.dependenciesTextLabel"));
		dependenciesTextLabel.setMinimumSize(new Dimension());
		depotForDependenciesLabel = new JLabel();
		panelProductDependencies = new PanelProductDependencies(depotForDependenciesLabel);

		// do this so that you can mark and copy content of the label
		jLabelProductID.setFont(jLabelProductID.getFont().deriveFont(Font.BOLD));

		jLabelProductID.setBorder(null);
		jLabelProductID.setEditable(false);

		// do this so that you can mark and copy content of the label
		jLabelProductVersion.setBorder(null);
		jLabelProductVersion.setEditable(false);

		JScrollPane jScrollPaneProductInfo = new JScrollPane();
		jScrollPaneProductInfo.setViewportView(jTextAreaProductInfo);
		jScrollPaneProductInfo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		JScrollPane jScrollPaneProductAdvice = new JScrollPane();
		jScrollPaneProductAdvice.setViewportView(jTextAreaProductAdvice);
		jScrollPaneProductAdvice.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductAdvice.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		productSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		productSplitPane.setTopComponent(jScrollPaneProductInfo);
		productSplitPane.setBottomComponent(jScrollPaneProductAdvice);
		productSplitPane.setResizeWeight(0.5);

		SplitPaneStateManager.registerSplitPane(productSplitPane, getSplitPaneKey(true));

		dependenciesActivateButton = new JToggleButton(Icons.getIntellijIcon("arrowRight"));
		dependenciesActivateButton.setSelectedIcon(Icons.getIntellijIcon("arrowDown"));
		dependenciesActivateButton.addActionListener(event -> toggleDependenciesActive());
		dependenciesActivateButton.setFocusable(false);
		panelProductDependencies.setVisible(false);

		propertiesActivateButton = new JToggleButton(Icons.getIntellijIcon("arrowRight"), true);
		propertiesActivateButton.setSelectedIcon(Icons.getIntellijIcon("arrowDown"));
		propertiesActivateButton.addActionListener(event -> togglePropertiesActive());
		propertiesActivateButton.setFocusable(false);
	}

	private String getSplitPaneKey(boolean isAdvice) {
		String key;
		if (type == ProductSettingsType.LOCALBOOT_PRODUCT_SETTINGS) {
			key = SplitPaneStateManager.LOCALBOOT_PRODUCT_INFO_SPLIT;
		} else if (type == ProductSettingsType.NETBOOT_PRODUCT_SETTINGS) {
			key = SplitPaneStateManager.NETBOOT_PRODUCT_INFO_SPLIT;
		} else {
			key = SplitPaneStateManager.DEPOT_PRODUCT_INFO_SPLIT;
		}

		return isAdvice ? (key + ".info_advice") : key;
	}

	private void setupLayout() {
		setupTopComponent();
		setupBottomComponent();

		// Make it possible to close the info pane
		setMinimumSize(new Dimension());

		setDividerLocation(START_DIVIDER_LOCATION);

		SplitPaneStateManager.registerSplitPane(this, getSplitPaneKey(false));
	}

	private void setupTopComponent() {
		JPanel productDescriptionsPanel = new JPanel();
		productDescriptionsPanel.setLayout(new MigLayout("insets 0, fillx, wrap 1", "[grow, 0:0]",
				"[center]" + Globals.GAP_SIZE + "[]0[]" + Globals.GAP_SIZE + "[grow]"));

		productDescriptionsPanel.add(jLabelProductID, "gap top " + Globals.GAP_SIZE + ", align center");

		productDescriptionsPanel.add(jLabelProductName,
				"h " + Globals.DEFAULT_JLABEL_HEIGHT + "!, gap left " + Globals.GAP_SIZE + ", align left");

		productDescriptionsPanel.add(jLabelLabelProductVersion, "split 2, gap left " + Globals.GAP_SIZE);
		productDescriptionsPanel.add(jLabelProductVersion, "align left, growx");

		productDescriptionsPanel.add(productSplitPane, "grow, push, hmin 0");

		setTopComponent(productDescriptionsPanel);
	}

	private void setupBottomComponent() {
		JPanel bottomComponent = new JPanel();
		bottomComponent.setLayout(new MigLayout("insets 0, fillx, wrap 1", "[grow, 0:0]", "[]0[]0[]0[]"));

		bottomComponent.add(dependenciesActivateButton, "split 3, gapleft " + Globals.GAP_SIZE);
		bottomComponent.add(dependenciesTextLabel, "gapright push");
		bottomComponent.add(depotForDependenciesLabel, "alignx right, gapright " + Globals.MIN_GAP_SIZE + ", wrap");

		bottomComponent.add(panelProductDependencies, "hidemode 3, grow, pushy 50, sgy vshare, hmin 0, gaptop "
				+ Globals.MIN_GAP_SIZE + ", gapbottom " + Globals.MIN_GAP_SIZE);

		bottomComponent.add(propertiesActivateButton, "split 2, gapleft " + Globals.GAP_SIZE);
		bottomComponent.add(panelEditProperties.getTitlePanel(), "growx, wrap");

		bottomComponent.add(panelEditProperties,
				"hidemode 3, grow, pushy 50, sgy vshare, hmin 0, gaptop " + Globals.MIN_GAP_SIZE);

		setBottomComponent(bottomComponent);
	}

	private void toggleDependenciesActive() {
		panelProductDependencies.setVisible(dependenciesActivateButton.isSelected());
	}

	private void togglePropertiesActive() {
		panelEditProperties.setVisible(propertiesActivateButton.isSelected());
		panelEditProperties.setTitlePanelActivated(propertiesActivateButton.isSelected());
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
		OpsiProductInfo info = persistenceController.getDataServices().product.getProduct2VersionInfo2InfosPD()
				.get(productId).get(versionInfo);
		Logging.info(this, "got product infos  productId, versionInfo:  ", productId, ", ", versionInfo, ": ", info);

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
