/**
 * Copyright (c) uib GmbH <info@uib.de>
 * License: AGPL-3.0
 * This file is part of opsi - https://www.opsi.org
 */

package de.uib.configed.gui.productpage;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import de.uib.Main;
import de.uib.configed.Configed;
import de.uib.configed.Globals;
import de.uib.configed.type.OpsiPackage;
import de.uib.configed.type.OpsiProductInfo;
import de.uib.opsidatamodel.serverdata.OpsiServiceNOMPersistenceController;
import de.uib.opsidatamodel.serverdata.PersistenceControllerFactory;
import de.uib.utilities.DataChangedObserver;
import de.uib.utilities.datapanel.EditMapPanelX;
import de.uib.utilities.logging.Logging;

public class ProductInfoPane extends JSplitPane implements DataChangedObserver, ActionListener {

	private JPanel productDescriptionsPanel;
	private JPanel bottomComponent;
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

	private String productName = "";
	private Map<String, Boolean> specificPropertiesExisting;

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

		dependenciesActivateButton = new JButton();
		dependenciesTextLabel = new JLabel();
		depotForDependenciesLabel = new JLabel();
		panelProductDependencies = new PanelProductDependencies(depotForDependenciesLabel);

		propertiesActivateButton = new JButton();

		// do this so that you can mark and copy content of the label
		if (!Main.FONT) {
			jLabelProductID.setFont(Globals.DEFAULT_FONT_STANDARD_BOLD);
		}
		jLabelProductID.setBorder(null);
		jLabelProductID.setEditable(false);
		if (!Main.THEMES) {
			jLabelProductID.setBackground(null);
		}

		if (!Main.FONT) {
			jLabelProductName.setFont(Globals.DEFAULT_FONT_BIG);
		}

		if (!Main.FONT) {
			jLabelLabelProductVersion.setFont(Globals.DEFAULT_FONT_BIG);
		}
		jLabelLabelProductVersion.setText(Configed.getResourceValue("ProductInfoPane.jLabelProductVersion") + " ");

		// do this so that you can mark and copy content of the label
		if (!Main.FONT) {
			jLabelProductVersion.setFont(Globals.DEFAULT_FONT_BIG);
		}
		jLabelProductVersion.setBorder(null);
		jLabelProductVersion.setEditable(false);
		if (!Main.THEMES) {
			jLabelProductVersion.setBackground(null);
		}

		if (!Main.FONT) {
			jTextAreaProductInfo.setFont(Globals.DEFAULT_FONT);
		}
		if (!Main.THEMES) {
			jTextAreaProductInfo.setBackground(Globals.BACKGROUND_COLOR_3);
		}

		jScrollPaneProductInfo.setViewportView(jTextAreaProductInfo);
		jScrollPaneProductInfo.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductInfo.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		if (!Main.FONT) {
			jTextAreaProductAdvice.setFont(Globals.DEFAULT_FONT);
		}
		if (!Main.THEMES) {
			jTextAreaProductAdvice.setBackground(Globals.BACKGROUND_COLOR_3);
		}

		jScrollPaneProductAdvice.setViewportView(jTextAreaProductAdvice);
		jScrollPaneProductAdvice.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPaneProductAdvice.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		dependenciesTextLabel.setText(Configed.getResourceValue("ProductInfoPane.dependenciesTextLabel"));
		if (!Main.FONT) {
			dependenciesTextLabel.setFont(Globals.DEFAULT_FONT_BIG);
		}
		if (!Main.THEMES) {
			dependenciesTextLabel.setForeground(Globals.GREYED);
		}

		if (!Main.FONT) {
			depotForDependenciesLabel.setFont(Globals.DEFAULT_FONT_BIG);
		}
		if (!Main.THEMES) {
			depotForDependenciesLabel.setForeground(Globals.GREYED);
		}

		dependenciesActivateButton.setText("▶");
		if (!Main.FONT) {
			dependenciesActivateButton.setFont(Globals.DEFAULT_FONT);
		}
		if (!Main.THEMES) {
			dependenciesActivateButton.setForeground(Globals.LIGHT_BLACK);
		}
		dependenciesActivateButton.addActionListener(this);

		panelProductDependencies.setVisible(isPanelProductDependenciesVisible);

		propertiesActivateButton.setText("▼");
		if (!Main.FONT) {
			propertiesActivateButton.setFont(Globals.DEFAULT_FONT);
		}
		if (!Main.THEMES) {
			propertiesActivateButton.setForeground(Globals.LIGHT_BLACK);
		}
		propertiesActivateButton.addActionListener(this);

		panelEditProperties.setVisible(isPanelEditPropertiesVisible);
	}

	private void setupLayout() {
		productDescriptionsPanel = new JPanel();

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
		bottomComponent = new JPanel();

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

			setActivatedButton(dependenciesActivateButton, isPanelProductDependenciesVisible);

			if (!Main.THEMES) {
				dependenciesTextLabel
						.setForeground(isPanelProductDependenciesVisible ? Globals.PANEL_PRODUCT_INFO_PANE_ACTIVE
								: Globals.PANEL_PRODUCT_INFO_PANE_INACTIVE);
			}

			if (!Main.THEMES) {
				depotForDependenciesLabel.setEnabled(isPanelProductDependenciesVisible);
				depotForDependenciesLabel
						.setForeground(isPanelProductDependenciesVisible ? Globals.PANEL_PRODUCT_INFO_PANE_ACTIVE
								: Globals.PANEL_PRODUCT_INFO_PANE_INACTIVE);
			}

			panelProductDependencies.setVisible(isPanelProductDependenciesVisible);
		} else if (event.getSource() == propertiesActivateButton) {
			isPanelEditPropertiesVisible = !isPanelEditPropertiesVisible;

			setActivatedButton(propertiesActivateButton, isPanelEditPropertiesVisible);

			panelEditProperties.setVisible(isPanelEditPropertiesVisible);
			panelEditProperties.setTitlePanelActivated(isPanelEditPropertiesVisible);
		} else {
			Logging.warning(this, "unexpected action performed on source " + event.getSource());
		}
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
		productName = s;
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
		panelProductDependencies.setEditValues("", "");
	}

	//
	// DataChangedObserver
	@Override
	public void dataHaveChanged(Object source) {

		if (source instanceof EditMapPanelX) {
			specificPropertiesExisting.put(productName, true);
		}
	}
}
