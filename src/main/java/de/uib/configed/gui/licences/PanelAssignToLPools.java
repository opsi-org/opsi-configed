package de.uib.configed.gui.licences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.configed.ControlMultiTablePanel;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FGlobalSoftwareInfo;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.type.SWAuditEntry;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.PanelStateSwitch;
import de.uib.utilities.table.gui.PanelGenEditTable;

/**
 * Copyright (C) 2008-2020 uib.de
 * 
 * @author roeder
 */
public class PanelAssignToLPools extends MultiTablePanel implements ChangeListener {
	public JTextField testfield; // for test purposes

	private JLabel titleWindowsSoftware;
	private JLabel titleWindowsSoftware2;
	private JLabel labelSelectedLicencePoolId;
	public JLabel fieldSelectedLicencePoolId;

	private JLabel labelCountAssignedStatus;;
	public JLabel fieldCountAssignedStatus;
	private JLabel labelCountAssignedInEditing;
	public JLabel fieldCountAssignedInEditing;

	private JLabel labelCountAllWindowsSoftware;
	public JLabel fieldCountAllWindowsSoftware;
	private JLabel labelCountDisplayedWindowsSoftware;
	public JLabel fieldCountDisplayedWindowsSoftware;
	private JLabel labelCountNotAssignedSoftware;
	public JLabel fieldCountNotAssignedSoftware;

	public JButton buttonShowAssignedNotExisting;

	private JLabel labelSupplementSimilar;
	public JButton buttonSupplementSimilar;

	private JSplitPane splitPane;
	private JPanel topPane;
	private JPanel bottomPane;
	private int splitPaneHMargin = 1;

	private JPanel panelInfoWindowsSoftware;
	private JPanel panelInfoConfigWindowsSoftware;
	// public PanelGenEditTable panelRegisteredSoftware;
	public PanelRegisteredSoftware panelRegisteredSoftware;
	public PanelGenEditTable panelLicencepools;
	public PanelGenEditTable panelProductId2LPool;

	public FGlobalSoftwareInfo fMissingSoftwareInfo;
	public FSoftwarename2LicencePool fSoftwarename2LicencePool;

	private PanelStateSwitch panelRadiobuttonsPreselectionForName2Pool;
	private JLabel labelSimilarEntriesExist;

	protected int minVSize = 80;

	// public static final String arrowRight = "►";

	public static final int tablesMaxWidth = 1000;
	protected int tablesMaxHeight = Short.MAX_VALUE;

	/** Creates new form panelAssignToLPools */
	public PanelAssignToLPools(ControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {

		// splitpane
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.7f);
		topPane = new JPanel();
		bottomPane = new JPanel();

		testfield = new JTextField("                     ");

		// construct content panes
		panelInfoWindowsSoftware = new JPanel();
		panelInfoWindowsSoftware.setBackground(Globals.backgroundWhite);

		panelInfoConfigWindowsSoftware = new JPanel();
		panelInfoConfigWindowsSoftware.setBackground(Globals.backgroundWhite);

		titleWindowsSoftware = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.Licences.SectiontitleWindowsSoftware2LPool"));
		titleWindowsSoftware.setFont(Globals.defaultFontStandardBold);

		titleWindowsSoftware2 = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.Licences.SectiontitleWindowsSoftware2LPool.supplement"));
		titleWindowsSoftware2.setFont(Globals.defaultFont);

		labelSelectedLicencePoolId = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.labelSelectedLicencePoolId"));
		// labelSelectedLicencePoolId.setPreferredSize(Globals.counterfieldDimension);
		labelSelectedLicencePoolId.setFont(Globals.defaultFont);

		fieldSelectedLicencePoolId = new JLabel("");
		fieldSelectedLicencePoolId.setPreferredSize(new java.awt.Dimension(250, Globals.LINE_HEIGHT));
		fieldSelectedLicencePoolId.setFont(Globals.defaultFontStandardBold);

		labelCountAllWindowsSoftware = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.labelCountAllWindowsSoftware"));
		// labelCountAllWindowsSoftware.setPreferredSize(Globals.counterfieldDimension);
		labelCountAllWindowsSoftware.setFont(Globals.defaultFont);

		fieldCountAllWindowsSoftware = new JLabel("");
		fieldCountAllWindowsSoftware.setPreferredSize(Globals.shortlabelDimension);
		fieldCountAllWindowsSoftware.setFont(Globals.defaultFont);

		labelCountDisplayedWindowsSoftware = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.labelCountDisplayedWindowsSoftware"));
		labelCountDisplayedWindowsSoftware.setFont(Globals.defaultFont);

		fieldCountDisplayedWindowsSoftware = new JLabel("");
		fieldCountDisplayedWindowsSoftware.setPreferredSize(Globals.shortlabelDimension);
		fieldCountDisplayedWindowsSoftware.setFont(Globals.defaultFont);

		labelCountNotAssignedSoftware = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.labelCountNotAssignedSoftware"));
		labelCountNotAssignedSoftware.setFont(Globals.defaultFont);

		fieldCountNotAssignedSoftware = new JLabel("");
		fieldCountNotAssignedSoftware.setPreferredSize(Globals.shortlabelDimension);
		fieldCountNotAssignedSoftware.setFont(Globals.defaultFont);

		labelCountAssignedStatus = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.labelCountAssignedStatus"));
		// labelCountAssignedStatus.setPreferredSize(Globals.counterfieldDimension);
		labelCountAssignedStatus.setFont(Globals.defaultFont);

		fieldCountAssignedStatus = new JLabel("");
		fieldCountAssignedStatus.setPreferredSize(Globals.shortlabelDimension);
		fieldCountAssignedStatus.setFont(Globals.defaultFontStandardBold);

		labelCountAssignedInEditing = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.labelCountAssignedInEditing"));
		// labelCountAssignedInEditing.setPreferredSize(Globals.counterfieldDimension);
		labelCountAssignedInEditing.setFont(Globals.defaultFont);

		fieldCountAssignedInEditing = new JLabel("");
		fieldCountAssignedInEditing.setPreferredSize(Globals.shortlabelDimension);
		fieldCountAssignedInEditing.setFont(Globals.defaultFontStandardBold);

		buttonShowAssignedNotExisting = new JButton(
				configed.getResourceValue("PanelAssignToLPools.buttonAssignedButMissing"),
				Globals.createImageIcon("images/edit-table-delete-row-16x16.png", ""));

		buttonShowAssignedNotExisting
				.setToolTipText(configed.getResourceValue("PanelAssignToLPools.buttonAssignedButMissing.tooltip"));
		buttonShowAssignedNotExisting.setFont(Globals.defaultFont);
		buttonShowAssignedNotExisting.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fMissingSoftwareInfo.centerOn(Globals.frame1);
				fMissingSoftwareInfo.setVisible(true);
			}
		});

		labelSupplementSimilar = new JLabel(
				configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries"));
		// "PanelAssignToLPools.Licences.supplementSimilarSWEntries") );
		labelSupplementSimilar.setVisible(true);
		labelSupplementSimilar.setFont(Globals.defaultFont);
		// labelSupplementSimilar.setIcon(
		// Globals.createImageIcon("images/edit-table-insert-row-under-16x16.png", "")
		// );

		// buttonSupplementSimilar = new JButton( configed.getResourceValue(
		// "PanelAssignToLPools.Licences.supplementSimilarSWEntries.button") );

		buttonSupplementSimilar = new JButton(
				configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries.button"),
				Globals.createImageIcon("images/edit-table-insert-row-under.png", ""));

		buttonSupplementSimilar.setToolTipText(
				configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries.tooltip"));
		buttonSupplementSimilar.setFont(Globals.defaultFont);

		buttonSupplementSimilar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				/*
				 * if (fSoftwarename2LicencePool == null)
				 * {
				 * return;
				 * }
				 */

				if (!fSoftwarename2LicencePool.isVisible())
					fSoftwarename2LicencePool.centerOn(Globals.frame1);

				logging.info(this, "buttonSupplementSimilar actionPerformed, we have selected "
						+ panelRadiobuttonsPreselectionForName2Pool.getValue());
				fSoftwarename2LicencePool.setPreselectionForName2Pool(
						(FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction) panelRadiobuttonsPreselectionForName2Pool
								.getValue());
				// System.exit(0);
				fSoftwarename2LicencePool.setVisible(true);

				panelRegisteredSoftware.callName2Pool(panelRegisteredSoftware.getTableModel().getCursorRow());
			}
		});

		labelSimilarEntriesExist = new JLabel();
		labelSimilarEntriesExist.setVisible(true);
		labelSimilarEntriesExist.setFont(Globals.defaultFont);

		panelRadiobuttonsPreselectionForName2Pool = new PanelStateSwitch(
				// "",//configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.title"),
				null, FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction.SHOW_ALL_NAMES, // start value
				FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction.values(),
				new String[] {
						configed.getResourceValue(
								"PanelAssignToLPools.Licences.supplementSimilarSWEntries.showAllSwNames"),
						configed.getResourceValue(
								"PanelAssignToLPools.Licences.supplementSimilarSWEntries.showOnlyNamesWithNotUniformAssignments"),
						configed.getResourceValue(
								"PanelAssignToLPools.Licences.supplementSimilarSWEntries.showOnlyNamesWithoutAssignments") },

				FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction.class, null) {
			@Override
			protected void notifyChangeListeners(ChangeEvent e) {
				fSoftwarename2LicencePool.setPreselectionForName2Pool(
						(FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction) this.getValue());
				super.notifyChangeListeners(e);
			}
		};

		panelRadiobuttonsPreselectionForName2Pool.addChangeListener(this);

		JPanel panelWorkNamebased = new JPanel();
		panelWorkNamebased.setBorder(new javax.swing.border.LineBorder(Globals.blueGrey, 3, true));
		panelWorkNamebased.setBackground(Globals.backgroundWhite);
		panelWorkNamebased.setOpaque(true);
		GroupLayout layoutNamebased = new GroupLayout(panelWorkNamebased);
		panelWorkNamebased.setLayout(layoutNamebased);

		layoutNamebased.setVerticalGroup(layoutNamebased.createSequentialGroup().addGap(5).addGroup(layoutNamebased
				.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
				.addComponent(labelSupplementSimilar, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT)
				.addComponent(buttonSupplementSimilar, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT)
				.addComponent(labelSimilarEntriesExist, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
						Globals.SMALL_HEIGHT))
				.addComponent(panelRadiobuttonsPreselectionForName2Pool, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(5));

		layoutNamebased.setHorizontalGroup(layoutNamebased.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
				.addGroup(layoutNamebased.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelSupplementSimilar, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(buttonSupplementSimilar, Globals.BUTTON_WIDTH / 2,
								javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(labelSimilarEntriesExist, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE))
				.addGroup(layoutNamebased.createSequentialGroup().addGap(Globals.HGAP_SIZE)
						.addComponent(panelRadiobuttonsPreselectionForName2Pool, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2)));

		PanelStateSwitch panelRadiobuttonsDirectionOfAssignment = new PanelStateSwitch(
				configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.title"),
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.POOL2SOFTWARE, // start value
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.values(),
				new String[] {
						configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.POOL2SOFTWARE"),
						configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.SOFTWARE2POOL") },

				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.class,

				val -> {
					logging.info(this, " produced " + val);
					((ControlPanelAssignToLPools) controller).setSoftwareDirectionOfAssignment(
							(ControlPanelAssignToLPools.SoftwareDirectionOfAssignment) val);
				});

		JPanel panelRadiobuttonsDirectionOfAssignmentX = new JPanel();
		// panelRadiobuttonsDirectionOfAssignmentX.setBorder( new
		// javax.swing.border.LineBorder(Globals.backLightBlue, 3, true)
		// );
		GroupLayout layoutBorder = new GroupLayout(panelRadiobuttonsDirectionOfAssignmentX);
		panelRadiobuttonsDirectionOfAssignmentX.setLayout(layoutBorder);
		layoutBorder.setVerticalGroup(layoutBorder
				.createSequentialGroup().addGap(2, 5, 5).addComponent(panelRadiobuttonsDirectionOfAssignment,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2, 5, 5));

		layoutBorder.setHorizontalGroup(layoutBorder.createSequentialGroup() // createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGap(2, 5, 5).addComponent(panelRadiobuttonsDirectionOfAssignment, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2, 5, 5));

		panelRadiobuttonsDirectionOfAssignmentX.setBackground(Globals.backgroundWhite);

		GroupLayout layoutPanelInfo = new javax.swing.GroupLayout(panelInfoWindowsSoftware);
		panelInfoWindowsSoftware.setLayout(layoutPanelInfo);
		panelInfoWindowsSoftware.setBackground(Globals.backgroundWhite);

		layoutPanelInfo.setHorizontalGroup(layoutPanelInfo.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addGroup(layoutPanelInfo.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(titleWindowsSoftware, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(panelWorkNamebased, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(labelSelectedLicencePoolId, 0, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(fieldSelectedLicencePoolId, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(titleWindowsSoftware2, 50, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(panelRadiobuttonsDirectionOfAssignmentX, 20, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))

				));

		layoutPanelInfo.setVerticalGroup(layoutPanelInfo.createSequentialGroup().addContainerGap()
				.addGroup(layoutPanelInfo.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
						titleWindowsSoftware, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT))

				.addGroup(layoutPanelInfo.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(labelSelectedLicencePoolId, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(fieldSelectedLicencePoolId, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT))
				.addGroup(layoutPanelInfo.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(
						titleWindowsSoftware2, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT))
				// .addGap(Globals.buttonHeight,Globals.buttonHeight, Short.MAX_VALUE)
				// //corresponding to bottom config height
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(panelWorkNamebased, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(2)

				.addComponent(panelRadiobuttonsDirectionOfAssignmentX, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Globals.HGAP_SIZE))

		;

		PanelStateSwitch panelRadiobuttonsSoftwareselectionX = new PanelStateSwitch(null, // title
				ControlPanelAssignToLPools.SoftwareShowAllMeans.ALL,
				ControlPanelAssignToLPools.SoftwareShowAllMeans.values(),

				new String[] { configed.getResourceValue("PanelAssignToLPools.radiobuttonALL"),
						configed.getResourceValue("PanelAssignToLPools.radiobuttonASSIGNED_OR_ASSIGNED_TO_NOTHING"),
						configed.getResourceValue("PanelAssignToLPools.radiobuttonASSIGNED_TO_NOTHING") },

				ControlPanelAssignToLPools.SoftwareShowAllMeans.class,

				val -> {
					logging.info(this, " produced " + val);
					((ControlPanelAssignToLPools) controller)
							.setSoftwareShowAllMeans((ControlPanelAssignToLPools.SoftwareShowAllMeans) val);
				});

		GroupLayout layoutPanelInfoConfig = new javax.swing.GroupLayout(panelInfoConfigWindowsSoftware);
		panelInfoConfigWindowsSoftware.setLayout(layoutPanelInfoConfig);

		// take max width
		int col0width = labelCountAssignedStatus.getPreferredSize().width;
		if (labelCountAllWindowsSoftware.getPreferredSize().width > col0width)
			col0width = labelCountAllWindowsSoftware.getPreferredSize().width;

		layoutPanelInfoConfig.setHorizontalGroup(layoutPanelInfoConfig.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addGroup(layoutPanelInfoConfig.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layoutPanelInfoConfig.createSequentialGroup()
								.addComponent(labelCountAssignedStatus, col0width, col0width, col0width)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(fieldCountAssignedStatus, Globals.BUTTON_WIDTH / 3,
										Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(labelCountAssignedInEditing, 5, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(fieldCountAssignedInEditing, Globals.BUTTON_WIDTH / 3,
										Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(buttonShowAssignedNotExisting, Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH,
										Globals.BUTTON_WIDTH)
								.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, 2 * Globals.HGAP_SIZE))

						.addGroup(layoutPanelInfoConfig.createSequentialGroup()
								.addComponent(labelCountAllWindowsSoftware, col0width, col0width, col0width)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(fieldCountAllWindowsSoftware, Globals.BUTTON_WIDTH / 3,
										Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(labelCountDisplayedWindowsSoftware, 5, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(fieldCountDisplayedWindowsSoftware, Globals.BUTTON_WIDTH / 3,
										Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(labelCountNotAssignedSoftware, 5, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(fieldCountNotAssignedSoftware, Globals.BUTTON_WIDTH / 3,
										Globals.BUTTON_WIDTH / 3, Globals.BUTTON_WIDTH / 3)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2))

						.addComponent(panelRadiobuttonsSoftwareselectionX, 20, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)

				));
		layoutPanelInfoConfig.setVerticalGroup(layoutPanelInfoConfig.createSequentialGroup().addContainerGap()
				.addGap(Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT) // title height
				.addGroup(layoutPanelInfoConfig.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(labelCountAllWindowsSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(fieldCountAllWindowsSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(labelCountDisplayedWindowsSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(fieldCountDisplayedWindowsSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(labelCountNotAssignedSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT)
						.addComponent(fieldCountNotAssignedSoftware, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT,
								Globals.BUTTON_HEIGHT))
				.addGap(3) // to get the level of the components of the left side
				.addGroup(layoutPanelInfoConfig.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
						.addComponent(labelCountAssignedStatus, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(fieldCountAssignedStatus, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(labelCountAssignedInEditing, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(fieldCountAssignedInEditing, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(buttonShowAssignedNotExisting, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT))
				.addGap(5, 5, 5)

				.addComponent(panelRadiobuttonsSoftwareselectionX, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

				/*
				 * .addGroup(layoutPanelInfoConfig.createParallelGroup(javax.swing.GroupLayout.
				 * Alignment.CENTER)
				 * .addComponent(labelShowAssociationsToSelectedPoolOrNoPool,
				 * Globals.buttonHeight,Globals.buttonHeight,Globals.buttonHeight)
				 * .addComponent(checkShowOnlyAssociationsToSelectedPoolOrNoPool,
				 * Globals.buttonHeight,Globals.buttonHeight,Globals.buttonHeight)
				 * )
				 */
				.addContainerGap());

		panelLicencepools = new PanelGenEditTable(
				configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicencepools"), tablesMaxWidth, true, 1, // position of general popups
				false, // switchLineColors //does not matter
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD
				// ,
				// PanelGenEditTable.POPUP_PDF
				}, true // with tablesearchpane
		)

		/*
		 * the purpose of this construction is fulfilled
		 * by the construction of
		 * a getListSelectionModel().addListSelectionListener in
		 * in the controller
		 * 
		 * {
		 * 
		 * //ListSelectionListener
		 * public void valueChanged(javax.swing.event.ListSelectionEvent e)
		 * {
		 * super.valueChanged(e);
		 * 
		 * if (e.getValueIsAdjusting()) return;
		 * 
		 * logging.info(this, "selectionListener valueChanged, not more adjusting");
		 * }
		 * }
		 * 
		 */
		;
		panelLicencepools.setResetFilterModeOnNewSearch(false);
		panelLicencepools.setMasterFrame(Globals.frame1);

		panelProductId2LPool = new PanelGenEditTable(
				configed.getResourceValue("ConfigedMain.Licences.SectiontitleProductId2LPool"), tablesMaxWidth, true, 1,
				false, new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true // with tablesearchpane
		);

		panelProductId2LPool.setMasterFrame(Globals.frame1);
		boolean switchLineColors = true;

		panelRegisteredSoftware = new PanelRegisteredSoftware((ControlPanelAssignToLPools) controller);
		panelRegisteredSoftware.setFiltering(true, false);
		panelRegisteredSoftware.setMasterFrame(Globals.frame1);

		javax.swing.GroupLayout layoutTopPane = new javax.swing.GroupLayout((JPanel) topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
						layoutTopPane.createSequentialGroup().addContainerGap()
								.addGroup(layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										// for testing purposes:
										// .addComponent(testfield, javax.swing.GroupLayout.Alignment.LEADING,
										// javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
										// Short.MAX_VALUE)
										.addComponent(panelLicencepools, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(panelProductId2LPool, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap()));
		layoutTopPane.setVerticalGroup(layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutTopPane.createSequentialGroup().addContainerGap()
						// .addComponent(testfield, javax.swing.GroupLayout.DEFAULT_SIZE,
						// javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE)
						.addComponent(panelLicencepools, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE,
								tablesMaxHeight)
						// .addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
						.addComponent(panelProductId2LPool, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE,
								tablesMaxHeight)

						.addContainerGap())
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		javax.swing.GroupLayout layoutBottomPane = new javax.swing.GroupLayout((JPanel) bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		bottomPane.setBackground(Globals.backgroundWhite);

		layoutBottomPane
				.setHorizontalGroup(layoutBottomPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layoutBottomPane.createSequentialGroup()
								.addComponent(panelInfoWindowsSoftware, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, 5 * Globals.HGAP_SIZE)
								.addComponent(panelInfoConfigWindowsSoftware, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addComponent(panelRegisteredSoftware).addComponent(panelRegisteredSoftware,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE));
		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup().addContainerGap()
				.addGroup(layoutBottomPane.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(panelInfoWindowsSoftware).addComponent(panelInfoConfigWindowsSoftware))
				.addComponent(panelRegisteredSoftware, minVSize, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap());

		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		add(splitPane);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addContainerGap().addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin));

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void setDisplaySimilarExist(boolean b) {
		logging.info(this, "setDisplaySimilarExist " + b);
		if (b) {
			labelSimilarEntriesExist.setIcon(Globals.createImageIcon("images/checked_box_filled_i_14.png", ""));
			labelSimilarEntriesExist
					.setToolTipText(configed.getResourceValue("PanelAssignToLPools.Licences.similarSWEntriesExist"));
		} else {
			labelSimilarEntriesExist.setIcon(Globals.createImageIcon("images/checked_box_blue_empty_14.png", ""));
			labelSimilarEntriesExist.setToolTipText(
					configed.getResourceValue("PanelAssignToLPools.Licences.similarSWEntriesDontExist"));
		}
	}

	// implement ChengeListener
	@Override
	public void stateChanged(ChangeEvent e) {
		logging.info(this, " stateChanged " + e);
		logging.info(this,
				" stateChanged modelSWnames filterinfo " + fSoftwarename2LicencePool.modelSWnames.getFilterInfo());
		String resetToSWname = (String) panelRegisteredSoftware.getValueAt(panelRegisteredSoftware.getSelectedRow(),
				panelRegisteredSoftware.getTableModel().getColumnNames().indexOf(SWAuditEntry.NAME));
		logging.info(this, " stateChanged modelSWnames swname  >>" + resetToSWname + "<<");
		fSoftwarename2LicencePool.modelSWnames.requestReload();
		fSoftwarename2LicencePool.modelSWnames.reset();
		if (fSoftwarename2LicencePool.modelSWxLicencepool == null)
			return;

		fSoftwarename2LicencePool.modelSWxLicencepool.requestReload();
		fSoftwarename2LicencePool.modelSWnames.reset();

		if (fSoftwarename2LicencePool.modelSWnames.getRowCount() > 0) {
			fSoftwarename2LicencePool.panelSWnames.setSelectedRow(0);
			fSoftwarename2LicencePool.panelSWnames.moveToValue(resetToSWname, 0, true);
		}
	}

}
