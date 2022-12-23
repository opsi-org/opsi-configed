/*
 * PanelLicencesUsage.java
 *
 */

package de.uib.configed.gui.licences;

import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import de.uib.configed.ControlPanelLicencesUsage;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.utilities.table.gui.PanelGenEditTable;

/**
 * Copyright (C) 2008-2009 uib.de
 * 
 * @author roeder
 */
public class PanelLicencesUsage extends MultiTablePanel implements ActionListener {
	private JSplitPane splitPane;
	private int splitPaneHMargin = 1;

	public JTextField testfield;
	public PanelGenEditTable panelUsage;
	public PanelGenEditTable panelKeys;
	public PanelGenEditTable panelWindowsSoftwareIds;
	public PanelGenEditTable panelLicencepools;

	private javax.swing.JButton buttonGet;
	private javax.swing.JLabel labelGetAndAssignSL;
	private javax.swing.JPanel panelGetAndAssignSL;
	private de.uib.utilities.swing.DynamicCombo comboClient;

	protected int minVSize = 50;
	protected int tablesMaxWidth = 1000;
	protected int buttonHeight = 15;
	protected int buttonWidth = 140;
	protected int lPoolHeight = 100;

	protected de.uib.configed.ControlPanelLicencesUsage licencesUsageController;
	private int initialSplit = 0;

	/** Creates new form panelLicencesUsage */
	public PanelLicencesUsage(ControlPanelLicencesUsage licencesUsageController) {
		super(licencesUsageController);
		this.licencesUsageController = licencesUsageController;
		initSubPanel();
		initComponents();
	}

	private void setupSplit() {
		splitPane.setResizeWeight(0.5f);
	}

	public void setDivider() {
		if (initialSplit < 1) {
			splitPane.setDividerLocation(0.7f);
			initialSplit++;
			revalidate();
		}

	}

	private void initSubPanel() {

		panelLicencepools = new PanelGenEditTable(
				configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicencepools"), tablesMaxWidth, false, 0,
				false, new int[] { PanelGenEditTable.POPUP_RELOAD });

		panelGetAndAssignSL = new javax.swing.JPanel();
		labelGetAndAssignSL = new javax.swing.JLabel(
				configed.getResourceValue("ConfigedMain.Licences.Usage.LabelAssignLicense"));

		comboClient = new de.uib.utilities.swing.DynamicCombo();
		comboClient.setFont(Globals.defaultFontBig);
		comboClient.setPreferredSize(new java.awt.Dimension(200, 20));
		buttonGet = new javax.swing.JButton(configed.getResourceValue("ConfigedMain.Licences.Usage.AssignLicense"));
		buttonGet.addActionListener(this);

		panelGetAndAssignSL.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		javax.swing.GroupLayout panelGetAndAssignSLLayout = new javax.swing.GroupLayout(panelGetAndAssignSL);
		panelGetAndAssignSL.setLayout(panelGetAndAssignSLLayout);
		panelGetAndAssignSLLayout.setHorizontalGroup(
				panelGetAndAssignSLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(panelGetAndAssignSLLayout.createSequentialGroup().addGap(20, 20, 20)
								.addGroup(panelGetAndAssignSLLayout
										.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(panelGetAndAssignSLLayout.createSequentialGroup()
												.addComponent(labelGetAndAssignSL).addGap(20, 20, 20)
												.addComponent(comboClient, javax.swing.GroupLayout.PREFERRED_SIZE, 263,
														javax.swing.GroupLayout.PREFERRED_SIZE))
										.addComponent(panelLicencepools, javax.swing.GroupLayout.Alignment.TRAILING, 20,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(buttonGet, buttonWidth, buttonWidth, buttonWidth))
								.addGap(20, 20, 20)));
		panelGetAndAssignSLLayout.setVerticalGroup(panelGetAndAssignSLLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelGetAndAssignSLLayout.createSequentialGroup()
						.addGap(5, 5, 5)
						.addGroup(panelGetAndAssignSLLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(labelGetAndAssignSL, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(comboClient, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(5, 5, 5).addComponent(panelLicencepools, lPoolHeight, lPoolHeight, Short.MAX_VALUE)
						.addComponent(buttonGet, buttonHeight, buttonHeight, buttonHeight).addGap(5, 5, 5)));

	}

	private void initComponents() {

		// testfield = new JTextField(" ");

		panelUsage = new PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleUsage"), 0,
				true, 0, false, new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true // searchpane
		);
		panelUsage.setMasterFrame(Globals.frame1);
		panelUsage.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		panelUsage.setFiltering(true);
		panelUsage.showFilterIcon(true);

		// panelKeys = new
		// PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicenceUsable"),

		// panelWindowsSoftwareIds = new
		// PanelGenEditTable(configed.getResourceValue("ConfigedMain.Licences.SectiontitleWindowsSoftwareIDs"),

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								layout.createSequentialGroup().addContainerGap()
										.addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE,
												Short.MAX_VALUE)
										.addContainerGap())
						.addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin));

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
		splitPane.setTopComponent(panelUsage);
		splitPane.setBottomComponent(panelGetAndAssignSL);
		setupSplit();

		/*
		 * 
		 * javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
		 * this.setLayout(layout);
		 * layout.setHorizontalGroup(
		 * layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
		 * layout.createSequentialGroup()
		 * .addContainerGap()
		 * .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.
		 * TRAILING)
		 * // for testing purposes:
		 * //.addComponent(testfield, javax.swing.GroupLayout.Alignment.LEADING,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * Short.MAX_VALUE)
		 * .addComponent(panelUsage, javax.swing.GroupLayout.Alignment.LEADING,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * Short.MAX_VALUE)
		 * //.addComponent(panelKeys, javax.swing.GroupLayout.Alignment.LEADING,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * Short.MAX_VALUE)
		 * //.addComponent(panelWindowsSoftwareIds,
		 * javax.swing.GroupLayout.Alignment.LEADING,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * Short.MAX_VALUE)
		 * .addComponent(panelGetAndAssignSL, javax.swing.GroupLayout.Alignment.LEADING,
		 * javax.swing.GroupLayout.PREFERRED_SIZE,
		 * javax.swing.GroupLayout.PREFERRED_SIZE,
		 * javax.swing.GroupLayout.PREFERRED_SIZE)
		 * )
		 * .addContainerGap())
		 * );
		 * layout.setVerticalGroup(
		 * layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(layout.createSequentialGroup()
		 * .addContainerGap()
		 * //.addComponent(testfield, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
		 * .addComponent(panelUsage, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * Short.MAX_VALUE)
		 * .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		 * //.addComponent(panelKeys, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * 150)
		 * //.addComponent(panelWindowsSoftwareIds, minVSize,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, 120)
		 * .addComponent(panelGetAndAssignSL, javax.swing.GroupLayout.PREFERRED_SIZE,
		 * javax.swing.GroupLayout.PREFERRED_SIZE, 150)
		 * .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
		 * )
		 * 
		 * );
		 */
	}

	/*
	 * public void setClientsList(ComboBoxModel m)
	 * {
	 * if (m == null)
	 * comboClient.setModel(emptyComboBoxModel);
	 * else
	 * comboClient.setModel(m);
	 * 
	 * }
	 */

	public void setClientsSource(de.uib.utilities.ComboBoxModeller modelsource) {
		comboClient.setModelSource(modelsource);
	}

	// ActionListener
	@Override
	public void actionPerformed(java.awt.event.ActionEvent evt) {
		if (evt.getSource() == buttonGet) {
			licencesUsageController.getSoftwareLicenceReservation((String) comboClient.getSelectedItem());
		}
	}

}
