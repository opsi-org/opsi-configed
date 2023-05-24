package de.uib.configed.gui.licences;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.uib.Main;
import de.uib.configed.AbstractControlMultiTablePanel;
import de.uib.configed.Configed;
import de.uib.configed.ControlPanelAssignToLPools;
import de.uib.configed.ControlPanelAssignToLPools.SoftwareDirectionOfAssignment;
import de.uib.configed.ControlPanelAssignToLPools.SoftwareShowAllMeans;
import de.uib.configed.Globals;
import de.uib.configed.gui.FGlobalSoftwareInfo;
import de.uib.configed.gui.FSoftwarename2LicencePool;
import de.uib.configed.gui.FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction;
import de.uib.configed.type.SWAuditEntry;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.PanelStateSwitch;
import de.uib.utilities.table.gui.PanelGenEditTable;

public class PanelAssignToLPools extends MultiTablePanel implements ChangeListener {

	private static final int SPLIT_PANE_H_MARGIN = 1;
	public static final int TABLES_MAX_WIDTH = 1000;

	private static final int MIN_V_SIZE = 80;

	public JLabel fieldSelectedLicencePoolId;

	public JLabel fieldCountAssignedStatus;
	public JLabel fieldCountAssignedInEditing;

	public JLabel fieldCountAllWindowsSoftware;
	public JLabel fieldCountDisplayedWindowsSoftware;
	public JLabel fieldCountNotAssignedSoftware;

	public JButton buttonShowAssignedNotExisting;

	public PanelRegisteredSoftware panelRegisteredSoftware;
	public PanelGenEditTable panelLicencepools;
	public PanelGenEditTable panelProductId2LPool;

	public FGlobalSoftwareInfo fMissingSoftwareInfo;
	public FSoftwarename2LicencePool fSoftwarename2LicencePool;

	private PanelStateSwitch<Softwarename2LicencepoolRestriction> panelRadiobuttonsPreselectionForName2Pool;
	private JLabel labelSimilarEntriesExist;

	/** Creates new form panelAssignToLPools */
	public PanelAssignToLPools(AbstractControlMultiTablePanel controller) {
		super(controller);
		initComponents();
	}

	private void initComponents() {

		// splitpane
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.7);

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();

		// construct content panes
		JPanel panelInfoWindowsSoftware = new JPanel();
		if (!Main.THEMES) {
			panelInfoWindowsSoftware.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

		JPanel panelInfoConfigWindowsSoftware = new JPanel();
		if (!Main.THEMES) {
			panelInfoConfigWindowsSoftware.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

		JLabel titleWindowsSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.Licences.SectiontitleWindowsSoftware2LPool"));
		if (!Main.FONT) {
			titleWindowsSoftware.setFont(Globals.defaultFontStandardBold);
		}

		JLabel titleWindowsSoftware2 = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.Licences.SectiontitleWindowsSoftware2LPool.supplement"));
		if (!Main.FONT) {
			titleWindowsSoftware2.setFont(Globals.defaultFont);
		}

		JLabel labelSelectedLicencePoolId = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelSelectedLicencePoolId"));

		if (!Main.FONT) {
			labelSelectedLicencePoolId.setFont(Globals.defaultFont);
		}

		fieldSelectedLicencePoolId = new JLabel("");
		fieldSelectedLicencePoolId.setPreferredSize(new Dimension(250, Globals.LINE_HEIGHT));
		if (!Main.FONT) {
			fieldSelectedLicencePoolId.setFont(Globals.defaultFontStandardBold);
		}

		JLabel labelCountAllWindowsSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAllWindowsSoftware"));

		if (!Main.FONT) {
			labelCountAllWindowsSoftware.setFont(Globals.defaultFont);
		}

		fieldCountAllWindowsSoftware = new JLabel("");
		fieldCountAllWindowsSoftware.setPreferredSize(Globals.shortlabelDimension);
		if (!Main.FONT) {
			fieldCountAllWindowsSoftware.setFont(Globals.defaultFont);
		}

		JLabel labelCountDisplayedWindowsSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountDisplayedWindowsSoftware"));
		if (!Main.FONT) {
			labelCountDisplayedWindowsSoftware.setFont(Globals.defaultFont);
		}

		fieldCountDisplayedWindowsSoftware = new JLabel("");
		fieldCountDisplayedWindowsSoftware.setPreferredSize(Globals.shortlabelDimension);
		if (!Main.FONT) {
			fieldCountDisplayedWindowsSoftware.setFont(Globals.defaultFont);
		}

		JLabel labelCountNotAssignedSoftware = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountNotAssignedSoftware"));
		if (!Main.FONT) {
			labelCountNotAssignedSoftware.setFont(Globals.defaultFont);
		}

		fieldCountNotAssignedSoftware = new JLabel("");
		fieldCountNotAssignedSoftware.setPreferredSize(Globals.shortlabelDimension);
		if (!Main.FONT) {
			fieldCountNotAssignedSoftware.setFont(Globals.defaultFont);
		}

		JLabel labelCountAssignedStatus = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAssignedStatus"));

		if (!Main.FONT) {
			labelCountAssignedStatus.setFont(Globals.defaultFont);
		}

		fieldCountAssignedStatus = new JLabel("");
		fieldCountAssignedStatus.setPreferredSize(Globals.shortlabelDimension);
		if (!Main.FONT) {
			fieldCountAssignedStatus.setFont(Globals.defaultFontStandardBold);
		}

		JLabel labelCountAssignedInEditing = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.labelCountAssignedInEditing"));

		if (!Main.FONT) {
			labelCountAssignedInEditing.setFont(Globals.defaultFont);
		}

		fieldCountAssignedInEditing = new JLabel("");
		fieldCountAssignedInEditing.setPreferredSize(Globals.shortlabelDimension);
		if (!Main.FONT) {
			fieldCountAssignedInEditing.setFont(Globals.defaultFontStandardBold);
		}

		buttonShowAssignedNotExisting = new JButton(
				Configed.getResourceValue("PanelAssignToLPools.buttonAssignedButMissing"),
				Globals.createImageIcon("images/edit-table-delete-row-16x16.png", ""));

		buttonShowAssignedNotExisting
				.setToolTipText(Configed.getResourceValue("PanelAssignToLPools.buttonAssignedButMissing.tooltip"));
		if (!Main.FONT) {
			buttonShowAssignedNotExisting.setFont(Globals.defaultFont);
		}
		buttonShowAssignedNotExisting.addActionListener((ActionEvent actionEvent) -> {
			fMissingSoftwareInfo.setLocationRelativeTo(Globals.frame1);
			fMissingSoftwareInfo.setVisible(true);
		});

		JLabel labelSupplementSimilar = new JLabel(
				Configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries"));

		labelSupplementSimilar.setVisible(true);
		if (!Main.FONT) {
			labelSupplementSimilar.setFont(Globals.defaultFont);
		}

		JButton buttonSupplementSimilar = new JButton(
				Configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries.button"),
				Globals.createImageIcon("images/edit-table-insert-row-under.png", ""));

		buttonSupplementSimilar.setToolTipText(
				Configed.getResourceValue("PanelAssignToLPools.Licences.supplementSimilarSWEntries.tooltip"));
		if (!Main.FONT) {
			buttonSupplementSimilar.setFont(Globals.defaultFont);
		}

		buttonSupplementSimilar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				if (!fSoftwarename2LicencePool.isVisible()) {
					fSoftwarename2LicencePool.setLocationRelativeTo(Globals.frame1);
				}

				Logging.info(this, "buttonSupplementSimilar actionPerformed, we have selected "
						+ panelRadiobuttonsPreselectionForName2Pool.getValue());
				fSoftwarename2LicencePool.setPreselectionForName2Pool(
						(FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction) panelRadiobuttonsPreselectionForName2Pool
								.getValue());

				fSoftwarename2LicencePool.setVisible(true);

				panelRegisteredSoftware.callName2Pool(panelRegisteredSoftware.getTableModel().getCursorRow());
			}
		});

		labelSimilarEntriesExist = new JLabel();
		labelSimilarEntriesExist.setVisible(true);
		if (!Main.FONT) {
			labelSimilarEntriesExist.setFont(Globals.defaultFont);
		}

		panelRadiobuttonsPreselectionForName2Pool = new PanelStateSwitch<>(

				null, FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction.SHOW_ALL_NAMES,
				FSoftwarename2LicencePool.Softwarename2LicencepoolRestriction.values(),
				new String[] {
						Configed.getResourceValue(
								"PanelAssignToLPools.Licences.supplementSimilarSWEntries.showAllSwNames"),
						Configed.getResourceValue(
								"PanelAssignToLPools.Licences.supplementSimilarSWEntries.showOnlyNamesWithNotUniformAssignments"),
						Configed.getResourceValue(
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
		panelWorkNamebased.setBorder(new LineBorder(Globals.blueGrey, 3, true));
		if (!Main.THEMES) {
			panelWorkNamebased.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}
		panelWorkNamebased.setOpaque(true);
		GroupLayout layoutNamebased = new GroupLayout(panelWorkNamebased);
		panelWorkNamebased.setLayout(layoutNamebased);

		layoutNamebased.setVerticalGroup(layoutNamebased.createSequentialGroup().addGap(5).addGroup(layoutNamebased
				.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addComponent(labelSupplementSimilar, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT)
				.addComponent(buttonSupplementSimilar, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT)
				.addComponent(labelSimilarEntriesExist, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
						Globals.SMALL_HEIGHT))
				.addComponent(panelRadiobuttonsPreselectionForName2Pool, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(5));

		layoutNamebased.setHorizontalGroup(layoutNamebased.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(layoutNamebased.createSequentialGroup()
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
						.addComponent(labelSupplementSimilar, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
						.addComponent(buttonSupplementSimilar, Globals.BUTTON_WIDTH / 2, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE, Globals.HGAP_SIZE, Short.MAX_VALUE)
						.addComponent(labelSimilarEntriesExist, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE)
						.addGap(Globals.HGAP_SIZE))
				.addGroup(layoutNamebased.createSequentialGroup().addGap(Globals.HGAP_SIZE)
						.addComponent(panelRadiobuttonsPreselectionForName2Pool, GroupLayout.PREFERRED_SIZE,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGap(Globals.HGAP_SIZE / 2)));

		PanelStateSwitch<SoftwareDirectionOfAssignment> panelRadiobuttonsDirectionOfAssignment = new PanelStateSwitch<>(
				Configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.title"),
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.POOL2SOFTWARE,
				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.values(),
				new String[] {
						Configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.POOL2SOFTWARE"),
						Configed.getResourceValue("PanelAssignToLPools.SoftwareDirectionOfAssignment.SOFTWARE2POOL") },

				ControlPanelAssignToLPools.SoftwareDirectionOfAssignment.class,

				(Enum<SoftwareDirectionOfAssignment> val) -> {
					Logging.info(this, " produced " + val);
					((ControlPanelAssignToLPools) controller).setSoftwareDirectionOfAssignment(
							(ControlPanelAssignToLPools.SoftwareDirectionOfAssignment) val);
				});

		JPanel panelRadiobuttonsDirectionOfAssignmentX = new JPanel();

		GroupLayout layoutBorder = new GroupLayout(panelRadiobuttonsDirectionOfAssignmentX);
		panelRadiobuttonsDirectionOfAssignmentX.setLayout(layoutBorder);
		layoutBorder.setVerticalGroup(layoutBorder
				.createSequentialGroup().addGap(2, 5, 5).addComponent(panelRadiobuttonsDirectionOfAssignment,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2, 5, 5));

		layoutBorder.setHorizontalGroup(layoutBorder
				.createSequentialGroup().addGap(2, 5, 5).addComponent(panelRadiobuttonsDirectionOfAssignment,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2, 5, 5));

		if (!Main.THEMES) {
			panelRadiobuttonsDirectionOfAssignmentX.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

		GroupLayout layoutPanelInfo = new GroupLayout(panelInfoWindowsSoftware);
		panelInfoWindowsSoftware.setLayout(layoutPanelInfo);
		if (!Main.THEMES) {
			panelInfoWindowsSoftware.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

		layoutPanelInfo.setHorizontalGroup(layoutPanelInfo.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(titleWindowsSoftware, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(panelWorkNamebased, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(labelSelectedLicencePoolId, 0, GroupLayout.DEFAULT_SIZE,
										GroupLayout.DEFAULT_SIZE)
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(fieldSelectedLicencePoolId, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(titleWindowsSoftware2, 50, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE))
						.addGroup(layoutPanelInfo.createSequentialGroup()
								.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
								.addComponent(panelRadiobuttonsDirectionOfAssignmentX, 20, GroupLayout.PREFERRED_SIZE,
										Short.MAX_VALUE))

				));

		layoutPanelInfo.setVerticalGroup(layoutPanelInfo.createSequentialGroup().addContainerGap()
				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
						titleWindowsSoftware, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT))

				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelSelectedLicencePoolId, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT)
						.addComponent(fieldSelectedLicencePoolId, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT,
								Globals.SMALL_HEIGHT))
				.addGroup(layoutPanelInfo.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
						titleWindowsSoftware2, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT, Globals.SMALL_HEIGHT))

				// //corresponding to bottom config height
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Globals.HGAP_SIZE)
				.addComponent(panelWorkNamebased, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)

				.addGap(2)

				.addComponent(panelRadiobuttonsDirectionOfAssignmentX, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE, Globals.HGAP_SIZE))

		;

		PanelStateSwitch<SoftwareShowAllMeans> panelRadiobuttonsSoftwareselectionX = new PanelStateSwitch<>(null,
				SoftwareShowAllMeans.ALL, SoftwareShowAllMeans.values(),

				new String[] { Configed.getResourceValue("PanelAssignToLPools.radiobuttonALL"),
						Configed.getResourceValue("PanelAssignToLPools.radiobuttonASSIGNED_OR_ASSIGNED_TO_NOTHING"),
						Configed.getResourceValue("PanelAssignToLPools.radiobuttonASSIGNED_TO_NOTHING") },

				ControlPanelAssignToLPools.SoftwareShowAllMeans.class,

				(Enum<SoftwareShowAllMeans> val) -> {
					Logging.info(this, " produced " + val);
					((ControlPanelAssignToLPools) controller)
							.setSoftwareShowAllMeans((ControlPanelAssignToLPools.SoftwareShowAllMeans) val);
				});

		GroupLayout layoutPanelInfoConfig = new GroupLayout(panelInfoConfigWindowsSoftware);
		panelInfoConfigWindowsSoftware.setLayout(layoutPanelInfoConfig);

		// take max width
		int col0width = labelCountAssignedStatus.getPreferredSize().width;
		if (labelCountAllWindowsSoftware.getPreferredSize().width > col0width) {
			col0width = labelCountAllWindowsSoftware.getPreferredSize().width;
		}

		layoutPanelInfoConfig.setHorizontalGroup(layoutPanelInfoConfig.createSequentialGroup()
				.addGap(Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2, Globals.HGAP_SIZE / 2)
				.addGroup(layoutPanelInfoConfig.createParallelGroup(GroupLayout.Alignment.LEADING)
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

				// title height
				.addGap(Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT, Globals.BUTTON_HEIGHT)
				.addGroup(layoutPanelInfoConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
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

				// to get the level of the components of the left side
				.addGap(3)
				.addGroup(layoutPanelInfoConfig.createParallelGroup(GroupLayout.Alignment.CENTER)
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

				.addContainerGap());

		panelLicencepools = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicencepools"), TABLES_MAX_WIDTH, true, 1,
				false, new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);
		panelLicencepools.setResetFilterModeOnNewSearch(false);
		panelLicencepools.setMasterFrame(Globals.frame1);

		panelProductId2LPool = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleProductId2LPool"), TABLES_MAX_WIDTH, true,
				1, false, new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);

		panelProductId2LPool.setMasterFrame(Globals.frame1);

		panelRegisteredSoftware = new PanelRegisteredSoftware((ControlPanelAssignToLPools) controller);
		panelRegisteredSoftware.setFiltering(true, false);
		panelRegisteredSoftware.setMasterFrame(Globals.frame1);

		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
				GroupLayout.Alignment.TRAILING,
				layoutTopPane.createSequentialGroup().addContainerGap()
						.addGroup(layoutTopPane.createParallelGroup(GroupLayout.Alignment.LEADING)

								.addComponent(panelLicencepools, GroupLayout.Alignment.LEADING,
										GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(panelProductId2LPool, GroupLayout.Alignment.LEADING,
										GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addContainerGap()));
		layoutTopPane.setVerticalGroup(layoutTopPane.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutTopPane.createSequentialGroup().addContainerGap()

						.addComponent(panelLicencepools, MIN_V_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

						.addComponent(panelProductId2LPool, MIN_V_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

						.addContainerGap())
				.addGap(Globals.VGAP_SIZE, Globals.VGAP_SIZE, Globals.VGAP_SIZE));

		GroupLayout layoutBottomPane = new GroupLayout(bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		if (!Main.THEMES) {
			bottomPane.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		}

		layoutBottomPane.setHorizontalGroup(layoutBottomPane.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(layoutBottomPane.createSequentialGroup()
						.addComponent(panelInfoWindowsSoftware, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
								Short.MAX_VALUE)
						.addGap(Globals.MIN_HGAP_SIZE, Globals.MIN_HGAP_SIZE, 5 * Globals.HGAP_SIZE)
						.addComponent(panelInfoConfigWindowsSoftware, GroupLayout.DEFAULT_SIZE,
								GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addComponent(panelRegisteredSoftware).addComponent(panelRegisteredSoftware, GroupLayout.DEFAULT_SIZE,
						GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup().addContainerGap()
				.addGroup(layoutBottomPane.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(panelInfoWindowsSoftware).addComponent(panelInfoConfigWindowsSoftware))
				.addComponent(panelRegisteredSoftware, MIN_V_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addContainerGap());

		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);

		add(splitPane);

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(SPLIT_PANE_H_MARGIN, SPLIT_PANE_H_MARGIN, SPLIT_PANE_H_MARGIN)
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(splitPane, 0,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addContainerGap().addGap(SPLIT_PANE_H_MARGIN, SPLIT_PANE_H_MARGIN, SPLIT_PANE_H_MARGIN));

		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(splitPane, 0,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));
	}

	public void setDisplaySimilarExist(boolean b) {
		Logging.info(this, "setDisplaySimilarExist " + b);
		if (b) {
			labelSimilarEntriesExist.setIcon(Globals.createImageIcon("images/checked_box_filled_i_14.png", ""));
			labelSimilarEntriesExist
					.setToolTipText(Configed.getResourceValue("PanelAssignToLPools.Licences.similarSWEntriesExist"));
		} else {
			labelSimilarEntriesExist.setIcon(Globals.createImageIcon("images/checked_box_blue_empty_14.png", ""));
			labelSimilarEntriesExist.setToolTipText(
					Configed.getResourceValue("PanelAssignToLPools.Licences.similarSWEntriesDontExist"));
		}
	}

	// implement ChengeListener
	@Override
	public void stateChanged(ChangeEvent e) {
		Logging.info(this, " stateChanged " + e);
		Logging.info(this,
				" stateChanged modelSWnames filterinfo " + fSoftwarename2LicencePool.modelSWnames.getFilterInfo());
		String resetToSWname = (String) panelRegisteredSoftware.getValueAt(panelRegisteredSoftware.getSelectedRow(),
				panelRegisteredSoftware.getTableModel().getColumnNames().indexOf(SWAuditEntry.NAME));
		Logging.info(this, " stateChanged modelSWnames swname  >>" + resetToSWname + "<<");
		fSoftwarename2LicencePool.modelSWnames.requestReload();
		fSoftwarename2LicencePool.modelSWnames.reset();
		if (fSoftwarename2LicencePool.modelSWxLicencepool == null) {
			return;
		}

		fSoftwarename2LicencePool.modelSWxLicencepool.requestReload();
		fSoftwarename2LicencePool.modelSWnames.reset();

		if (fSoftwarename2LicencePool.modelSWnames.getRowCount() > 0) {
			fSoftwarename2LicencePool.panelSWnames.setSelectedRow(0);
			fSoftwarename2LicencePool.panelSWnames.moveToValue(resetToSWname, 0, true);
		}
	}

}
