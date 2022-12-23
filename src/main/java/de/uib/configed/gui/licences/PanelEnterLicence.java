/*
 * PanelEnterLicence.java
 * after selecting a pool, one can add licence options for it
 * Created 17.02.2009-2015
 */

package de.uib.configed.gui.licences;

import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.timeedit.FEditDate;
import de.uib.utilities.table.gui.PanelGenEditTable;

/**
 * @author R. Röder
 */
public class PanelEnterLicence extends MultiTablePanel implements ActionListener {

	public PanelGenEditTable panelKeys;
	public PanelGenEditTable panelLicencepools;
	public PanelGenEditTable panelLicencecontracts;
	private JSplitPane splitPane;
	private JPanel topPane;
	private JPanel bottomPane;
	private int splitPaneHMargin = 1;

	public String selectedLicencePool = "";
	private ListSelectionListener licencePoolSelectionListener;

	protected int minVSize = 50;
	protected int minPanelTableHeight = 60;
	protected int maxHSize = 1000;
	// protected int tablesMaxWidth = PanelAssignToLPools.tablesMaxWidth;
	// protected int hSizePanelLicencepools = 600;

	protected int minFieldWidth = 40;
	protected int minFieldHeight = 6;

	private javax.swing.JButton jButtonCreateStandard;
	private javax.swing.JButton jButtonCreateVolume;
	private javax.swing.JButton jButtonCreateOEM;
	private javax.swing.JButton jButtonCreateConcurrent;
	private javax.swing.JButton jButtonSend;
	// private javax.swing.JLabel jLabelContract;
	private javax.swing.JLabel jLabelLKey;
	private javax.swing.JLabel jLabelLicencePool;
	private javax.swing.JLabel jLabelTask;
	private javax.swing.JLabel jLabelConfigure;
	private javax.swing.JLabel jLabelSLid1;
	private javax.swing.JLabel jLabelSLid2;
	private javax.swing.JLabel jLabelSLid3;
	private javax.swing.JLabel jLabelSLid4;
	private javax.swing.JLabel jLabelSLid5;
	private javax.swing.JLabel jLabelSLid6;
	private javax.swing.JLabel jLabelSLid3info;
	private javax.swing.JPanel panelTask;
	private javax.swing.JPanel panelEnterKey;
	private javax.swing.JPanel panelLicenceModel;
	private javax.swing.JTextField jTextField_licenceID;
	private javax.swing.JTextField jTextField_licenceType;
	// private javax.swing.JFormattedTextField jTextField_maxInstallations;
	private javax.swing.JTextField jTextField_maxInstallations;
	private javax.swing.JComboBox comboClient;
	private javax.swing.JTextField jTextField_endOfLicence;
	private javax.swing.JTextField jTextField_licenceContract;
	private javax.swing.JTextField jTextFieldLKey;

	private FEditDate fEditDate;

	protected de.uib.configed.ControlPanelEnterLicence enterLicenceController;

	private ComboBoxModel emptyComboBoxModel = new DefaultComboBoxModel<>(new String[] { "" });

	/** Creates new form PanelEnterLicence */
	public PanelEnterLicence(de.uib.configed.ControlPanelEnterLicence enterLicenceController) {
		super(enterLicenceController);
		this.enterLicenceController = enterLicenceController;
		initComponents();
		defineListeners();
		addSettings();

		final JPanel THIS = this;
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				// logging.info(this, "size " + THIS.getSize());
			}
		});

	}

	protected void addSettings() {

	}

	protected void defineListeners() {
		panelLicencecontracts.getListSelectionModel().addListSelectionListener(listSelectionEvent -> {
			// Ignore extra messages.
			if (listSelectionEvent.getValueIsAdjusting())
				return;

			ListSelectionModel lsm = (ListSelectionModel) listSelectionEvent.getSource();

			if (lsm.isSelectionEmpty()) {
				// logging.debug(this, "no rows selected");
			} else {
				int selectedRow = lsm.getMinSelectionIndex();
				String keyValue = panelLicencecontracts.getValueAt(selectedRow, 0).toString();

				if (jTextField_licenceContract.isEnabled())
					jTextField_licenceContract.setText(keyValue);
			}
		});

		panelLicencepools.addListSelectionListener(listSelectionEvent -> {
			if (listSelectionEvent.getValueIsAdjusting())
				return;

			int i = panelLicencepools.getSelectedRow();

			selectedLicencePool = "";

			if (i > -1)
				selectedLicencePool = panelLicencepools.getValueAt(i, 0).toString();

			panelLicencepools.setTitle(configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencepool")
					+ ": " + selectedLicencePool);
		});

	}

	private void deactivate() {
		jTextField_licenceID.setEnabled(false);
		jTextField_licenceType.setEnabled(false);
		jTextField_maxInstallations.setEnabled(false);
		comboClient.setEnabled(false);
		jTextField_endOfLicence.setEnabled(false);
		jTextField_licenceContract.setEnabled(false);
		jTextFieldLKey.setEnabled(false);
		jButtonSend.setEnabled(false);
	}

	private boolean check_and_start() {
		if (panelLicencepools.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(enterLicenceController.mainController.licencesFrame,
					configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectLicencepool"),
					configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
			return false;
		}

		if (panelLicencecontracts.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(enterLicenceController.mainController.licencesFrame,
					configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectLicencecontract"),
					configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
			return false;
		}

		jTextField_licenceID.setEnabled(true);
		jTextField_licenceID.setText("l_" + Globals.getSeconds());

		jTextField_endOfLicence.setEnabled(true);
		jTextField_endOfLicence.setText("");
		jTextField_licenceContract.setEnabled(true);
		jTextField_licenceContract
				.setText("" + panelLicencecontracts.getValueAt(panelLicencecontracts.getSelectedRow(), 0));
		jTextField_licenceContract.setEditable(false);

		jTextFieldLKey.setEnabled(true);

		jButtonSend.setEnabled(true);

		return true;
	}

	private void startStandard() {
		if (!check_and_start())
			return;

		jTextField_licenceType.setEnabled(true);
		jTextField_licenceType.setText("RETAIL");
		jTextField_licenceType.setEditable(false);
		jTextField_maxInstallations.setEnabled(true);
		// jTextField_maxInstallations.setValue(1);//setText("1");
		jTextField_maxInstallations.setText("1");
		jTextField_maxInstallations.setEditable(false);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);

	}

	private void startVolume() {
		if (!check_and_start())
			return;

		jTextField_licenceType.setEnabled(true);
		jTextField_licenceType.setText("VOLUME");
		jTextField_licenceType.setEditable(false);
		jTextField_maxInstallations.setEnabled(true);
		// jTextField_maxInstallations.setValue(0);//setText("0");
		jTextField_maxInstallations.setText("0");
		jTextField_maxInstallations.setEditable(true);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);

	}

	private void startOEM() {
		if (!check_and_start())
			return;

		jTextField_licenceType.setEnabled(true);
		jTextField_licenceType.setText("OEM");
		jTextField_licenceType.setEditable(false);
		jTextField_maxInstallations.setEnabled(true);
		// jTextField_maxInstallations.setValue(1);//setText("1");
		jTextField_maxInstallations.setText("1");
		jTextField_maxInstallations.setEditable(false);
		comboClient.setModel(new DefaultComboBoxModel<>(enterLicenceController.getChoicesAllHosts()));
		comboClient.setEnabled(true);

	}

	private void startConcurrent() {
		if (!check_and_start())
			return;

		jTextField_licenceID.setEnabled(true);
		jTextField_licenceID.setText("l_" + Globals.getSeconds());
		jTextField_licenceType.setEnabled(true);
		jTextField_licenceType.setText("CONCURRENT");
		jTextField_licenceType.setEditable(false);
		jTextField_maxInstallations.setEnabled(true);
		// jTextField_maxInstallations.setValue(0);//setText("0");
		jTextField_maxInstallations.setText("0");
		jTextField_maxInstallations.setEditable(false);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);

	}

	private void initComponents() {
		panelKeys = new PanelGenEditTable(
				configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicenceOptionsView"), 0, true, 0, false,
				new int[] {
						// PanelGenEditTable.POPUP_DELETE_ROW,
						// PanelGenEditTable.POPUP_SAVE,
						// PanelGenEditTable.POPUP_CANCEL,
						PanelGenEditTable.POPUP_RELOAD },
				false // searchpane
		);

		panelKeys.setMasterFrame(Globals.frame1);
		// panelKeys.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// panelKeys.showFilterIcon( true ); //supply implementation of
		// SearchTargetModelFromTable.setFiltered
		// panelKeys.setFiltering( true );

		panelLicencepools = new PanelGenEditTable(
				configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencepool"), maxHSize, false, 0,
				false, new int[] { PanelGenEditTable.POPUP_RELOAD }, true // with tablesearchpane
		);

		panelLicencepools.setMasterFrame(Globals.frame1);
		// panelLicencepools.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// panelLicencepools.showFilterIcon( true ); //supply implementation of
		// SearchTargetModelFromTable.setFiltered
		// panelLicencepools.setFiltering( true );

		panelLicencecontracts = new PanelGenEditTable(
				configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencecontract"), 0, true, 1, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true // with tablesearchpane
		);
		panelLicencecontracts.setMasterFrame(Globals.frame1);
		// panelLicencecontracts.setListSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		// panelLicencecontracts.showFilterIcon( true ); //supply implementation of
		// SearchTargetModelFromTable.setFiltered
		// panelLicencecontracts.setFiltering( true );

		jLabelLicencePool = new javax.swing.JLabel();
		jButtonCreateStandard = new javax.swing.JButton();
		jButtonCreateStandard.setPreferredSize(Globals.buttonDimension);
		jButtonCreateVolume = new javax.swing.JButton();
		jButtonCreateVolume.setPreferredSize(Globals.buttonDimension);
		jButtonCreateOEM = new javax.swing.JButton();
		jButtonCreateOEM.setPreferredSize(Globals.buttonDimension);
		jButtonCreateConcurrent = new javax.swing.JButton();
		jButtonCreateConcurrent.setPreferredSize(Globals.buttonDimension);

		jLabelTask = new javax.swing.JLabel();
		jLabelConfigure = new javax.swing.JLabel();
		panelLicenceModel = new javax.swing.JPanel();
		jLabelSLid1 = new javax.swing.JLabel();
		jLabelSLid2 = new javax.swing.JLabel();
		jLabelSLid3 = new javax.swing.JLabel();
		jLabelSLid4 = new javax.swing.JLabel();
		jLabelSLid5 = new javax.swing.JLabel();
		jLabelSLid6 = new javax.swing.JLabel();
		jTextField_licenceID = new javax.swing.JTextField();
		jTextField_licenceType = new javax.swing.JTextField();
		jTextField_maxInstallations = new javax.swing.JTextField();
		// jTextField_maxInstallations = new javax.swing.JFormattedTextField();
		// jTextField_maxInstallations.setInputVerifier(new
		// utils.FormattedTextFieldVerifier());

		comboClient = new javax.swing.JComboBox();
		comboClient.setFont(Globals.defaultFontBig);
		// org.jdesktop.swingx.autocomplete.AutoCompleteDecorator.decorate(combo);
		// combo.setRenderer ();
		comboClient.setPreferredSize(new java.awt.Dimension(200, 20));

		jLabelSLid3info = new javax.swing.JLabel();

		jTextField_endOfLicence = new javax.swing.JTextField();

		jTextField_endOfLicence.setEditable(false); // edit only via fEditDate
		jTextField_endOfLicence.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// logging.debug( " mouse clicked on textfield 5 ");
				if (e.getClickCount() > 1 || e.getButton() != MouseEvent.BUTTON1) {
					if (fEditDate == null)
						fEditDate = new FEditDate(jTextField_endOfLicence.getText(), false);
					else
						fEditDate.setStartText(jTextField_endOfLicence.getText());

					fEditDate.setCaller(jTextField_endOfLicence);
					fEditDate.init();
					try {
						java.awt.Point pointField = jTextField_endOfLicence.getLocationOnScreen();
						fEditDate.setLocation((int) pointField.getX() + 30, (int) pointField.getY() + 20);
					} catch (Exception ex) {
						logging.info(this, "locationOnScreen ex " + ex);
					}

					fEditDate.setTitle(" (" + Globals.APPNAME + ") "
							+ configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid5"));

					fEditDate.setVisible(true);
				}

			}
		});
		jTextField_endOfLicence.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (fEditDate != null)
					fEditDate.deactivate();
			}
		});

		jTextField_licenceContract = new javax.swing.JTextField();
		// jLabelContract = new javax.swing.JLabel();
		jButtonSend = new javax.swing.JButton();
		jButtonSend.setPreferredSize(Globals.buttonDimension);
		jLabelLKey = new javax.swing.JLabel();
		jTextFieldLKey = new javax.swing.JTextField();

		deactivate();

		panelEnterKey = new JPanel();

		jLabelLicencePool.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.Label"));

		jButtonCreateStandard.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.StandardLicense"));
		jButtonCreateStandard.setToolTipText(
				configed.getResourceValue("ConfigedMain.Licences.EnterLicense.StandardLicense.ToolTip"));
		jButtonCreateStandard.addActionListener(this);

		jButtonCreateVolume.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.VolumeLicense"));
		jButtonCreateVolume
				.setToolTipText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.VolumeLicense.ToolTip"));
		jButtonCreateVolume.addActionListener(this);

		jButtonCreateOEM.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.OEMLicense"));
		jButtonCreateOEM
				.setToolTipText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.OEMLicense.ToolTip"));
		jButtonCreateOEM.addActionListener(this);

		jButtonCreateConcurrent
				.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.ConcurrentLicense"));
		jButtonCreateConcurrent.setToolTipText(
				configed.getResourceValue("ConfigedMain.Licences.EnterLicense.ConcurrentLicense.ToolTip"));
		jButtonCreateConcurrent.addActionListener(this);

		/*
		 * jButtonCreateUserAtTime.setText("UserAtTime");
		 * jButtonCreateUserAtTime.
		 * setToolTipText("n Lizenzen, für max n gleichzeitige User nutzbar");
		 * jButtonCreateUserAtTime.addActionListener(this);
		 */

		/*
		 * jButtonCreateUserAtTime.setText("FixedUsers");
		 * jButtonCreateUserAtTime.setToolTipText("n Lizenzen,n fixierte User nutzbar");
		 * jButtonCreateUserAtTime.addActionListener(this);
		 */
		jButtonSend.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.Execute"));
		jButtonSend.addActionListener(this);

		jLabelTask.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.Task") + ":");
		jLabelTask.setFont(Globals.defaultFontBold);
		jLabelConfigure.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.ChooseType"));
		jLabelConfigure.setFont(Globals.defaultFontStandardBold);

		panelLicenceModel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jLabelSLid1.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid1"));
		jLabelSLid2.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid2"));
		jLabelSLid3.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid3"));
		jLabelSLid4.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid4"));
		jLabelSLid5.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid5"));
		jLabelSLid6.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid6"));

		jLabelSLid3info.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid3info"));

		/*
		 * jTextField_licenceID.setText("jTextField_licenceID");
		 * jTextField_licenceType.setText("jTextField_licenceType");
		 * jTextField_maxInstallations.setText("jTextField_maxInstallations");
		 * comboClient.setText("comboClient");
		 * jTextField_endOfLicence.setText("jTextField_endOfLicence");
		 * jTextField_licenceContract.setText("jTextField_licenceContract");
		 */

		de.uib.utilities.swing.Containership cs = new de.uib.utilities.swing.Containership(this);
		cs.doForAllContainedCompisOfClass("setFont", new Object[] { Globals.defaultFont }, JTextField.class);

		javax.swing.GroupLayout panelLicenceModelLayout = new javax.swing.GroupLayout(panelLicenceModel);
		panelLicenceModel.setLayout(panelLicenceModelLayout);
		panelLicenceModelLayout.setHorizontalGroup(panelLicenceModelLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelLicenceModelLayout.createSequentialGroup().addContainerGap()
						.addGroup(panelLicenceModelLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
								.addComponent(jLabelSLid4, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabelSLid3, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabelSLid2, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(jLabelSLid1, javax.swing.GroupLayout.DEFAULT_SIZE,
										javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
						// .addGap(5,5,5)
						.addGroup(panelLicenceModelLayout
								.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, true)

								.addGroup(javax.swing.GroupLayout.Alignment.LEADING,
										panelLicenceModelLayout.createSequentialGroup().addGroup(panelLicenceModelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, true)
												.addComponent(comboClient, minFieldWidth, 208, Short.MAX_VALUE)
												.addGroup(panelLicenceModelLayout.createSequentialGroup()
														.addComponent(jTextField_maxInstallations, minFieldWidth, 112,
																javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(5, 5, 5).addComponent(jLabelSLid3info, minFieldWidth,
																112, javax.swing.GroupLayout.PREFERRED_SIZE))
												.addComponent(jTextField_licenceID, minFieldWidth, 208, Short.MAX_VALUE)
												.addComponent(
														jTextField_licenceType, minFieldWidth, 239,
														javax.swing.GroupLayout.PREFERRED_SIZE))
												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34,
														Short.MAX_VALUE)
												.addGroup(panelLicenceModelLayout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING,
																true)
														.addGroup(panelLicenceModelLayout.createSequentialGroup()
																.addComponent(jLabelSLid6,
																		javax.swing.GroupLayout.PREFERRED_SIZE, 99,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																// .addGap(5,5,5)
																.addComponent(jTextField_licenceContract, minFieldWidth,
																		200, javax.swing.GroupLayout.PREFERRED_SIZE))
														.addGroup(panelLicenceModelLayout.createSequentialGroup()
																.addComponent(jLabelSLid5,
																		javax.swing.GroupLayout.PREFERRED_SIZE, 100,
																		javax.swing.GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																// .addGap(5,5,5)
																.addComponent(jTextField_endOfLicence, minFieldWidth,
																		200, javax.swing.GroupLayout.PREFERRED_SIZE)))))
						// .addContainerGap()
						.addContainerGap(10, Short.MAX_VALUE)));
		panelLicenceModelLayout
				.setVerticalGroup(panelLicenceModelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
								panelLicenceModelLayout.createSequentialGroup().addGap(0, 1, 3)
										.addGroup(panelLicenceModelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabelSLid1, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jTextField_licenceID, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabelSLid5, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jTextField_endOfLicence, minFieldHeight,
														Globals.LINE_HEIGHT, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(0, 1, 3)
										.addGroup(panelLicenceModelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabelSLid2, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jTextField_licenceType, minFieldHeight,
														Globals.LINE_HEIGHT, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGap(0, 1, 3)
										.addGroup(panelLicenceModelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabelSLid3, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jTextField_maxInstallations, minFieldHeight,
														Globals.LINE_HEIGHT, javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabelSLid3info, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jLabelSLid6, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(jTextField_licenceContract, minFieldHeight,
														Globals.LINE_HEIGHT, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
										.addGap(0, 1, 3)
										.addGroup(panelLicenceModelLayout
												.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
												.addComponent(jLabelSLid4, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE)
												.addComponent(comboClient, minFieldHeight, Globals.LINE_HEIGHT,
														javax.swing.GroupLayout.PREFERRED_SIZE))
						// .addGap(0, 1, 3)
						));

		panelEnterKey.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jLabelLKey.setText(configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelLicenseKey"));

		javax.swing.GroupLayout panelEnterKeyLayout = new javax.swing.GroupLayout(panelEnterKey);
		panelEnterKey.setLayout(panelEnterKeyLayout);
		panelEnterKeyLayout.setHorizontalGroup(panelEnterKeyLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelEnterKeyLayout.createSequentialGroup().addContainerGap()
						.addComponent(jLabelLKey, javax.swing.GroupLayout.PREFERRED_SIZE, 133,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(5, 5, 5)
						.addComponent(jTextFieldLKey, minFieldWidth, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addContainerGap(10, Short.MAX_VALUE)));
		panelEnterKeyLayout.setVerticalGroup(panelEnterKeyLayout
				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(panelEnterKeyLayout.createSequentialGroup().addGap(0, 1, 5)
						.addGroup(panelEnterKeyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jLabelLKey, minFieldHeight, Globals.LINE_HEIGHT,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldLKey, minFieldHeight, Globals.LINE_HEIGHT,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(0, 1, 5)));

		panelTask = new JPanel();
		panelTask.setBackground(Globals.backgroundWhite);

		javax.swing.GroupLayout layoutTask = new javax.swing.GroupLayout(panelTask);
		panelTask.setLayout(layoutTask);

		layoutTask.setHorizontalGroup(layoutTask.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutTask.createSequentialGroup().addGap(5, 5, 5).addGroup(layoutTask
						.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layoutTask.createSequentialGroup()
								.addGroup(layoutTask.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(layoutTask.createSequentialGroup()
												// .addGap(10, 10, 10)
												.addGroup(layoutTask
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(panelLicencecontracts, 50, 300, maxHSize))))
						// .addGap(10, 10, 10)
						)
						.addGroup(layoutTask.createSequentialGroup()
								.addComponent(jButtonSend, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(1587, Short.MAX_VALUE))
						.addGroup(layoutTask.createSequentialGroup()
								.addComponent(jButtonCreateStandard, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addComponent(jButtonCreateVolume, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addComponent(jButtonCreateOEM, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addComponent(jButtonCreateConcurrent, javax.swing.GroupLayout.PREFERRED_SIZE,
										javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
								.addContainerGap(1226, Short.MAX_VALUE))
						.addGroup(layoutTask.createSequentialGroup().addGap(5, 5, 5).addComponent(jLabelTask)
								.addContainerGap(1515, Short.MAX_VALUE))
						.addGroup(layoutTask.createSequentialGroup().addGap(10, 10, 10).addComponent(jLabelConfigure)
								.addContainerGap(1515, Short.MAX_VALUE))
						.addGroup(layoutTask.createSequentialGroup()
								.addGroup(layoutTask
										.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, true)
										.addComponent(panelEnterKey, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, maxHSize)
										.addComponent(panelLicenceModel, javax.swing.GroupLayout.Alignment.LEADING,
												javax.swing.GroupLayout.DEFAULT_SIZE,
												javax.swing.GroupLayout.DEFAULT_SIZE, maxHSize))
								.addGap(10, 10, 10)))));
		layoutTask.setVerticalGroup(layoutTask.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutTask.createSequentialGroup().addGap(5, 5, 5).addComponent(jLabelTask).addGap(5, 5, 5)
						.addComponent(panelLicencecontracts, minPanelTableHeight, minPanelTableHeight, Short.MAX_VALUE)
						.addGap(5, 5, 5).addComponent(jLabelConfigure).addGap(2, 2, 2)
						.addGroup(layoutTask.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(jButtonCreateStandard, javax.swing.GroupLayout.PREFERRED_SIZE, 20,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonCreateOEM, javax.swing.GroupLayout.PREFERRED_SIZE, 20,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonCreateVolume, javax.swing.GroupLayout.PREFERRED_SIZE, 20,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addComponent(jButtonCreateConcurrent, javax.swing.GroupLayout.PREFERRED_SIZE, 20,
										javax.swing.GroupLayout.PREFERRED_SIZE))
						.addGap(5, 5, 6)
						.addComponent(panelLicenceModel, minVSize, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(2, 2, 2)
						.addComponent(panelEnterKey, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(2, 2, 2).addComponent(jButtonSend, 20, 20, 20).addGap(5, 5, 5)));

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.3f);
		// splitPane.setDividerLocation(1f); //maximum for top when starting

		topPane = new JPanel();
		bottomPane = new JPanel();
		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);
		topPane.setBackground(Globals.backgroundWhite);
		bottomPane.setBackground(Globals.backgroundWhite);

		javax.swing.GroupLayout layoutTopPane = new javax.swing.GroupLayout((JPanel) topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane
				.createSequentialGroup().addGap(10, 10, 10).addComponent(panelLicencepools,
						javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, maxHSize)
				.addGap(10, 10, 10));
		layoutTopPane.setVerticalGroup(layoutTopPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layoutTopPane.createSequentialGroup().addGap(5, 5, 5)
						.addComponent(panelLicencepools, minPanelTableHeight, minPanelTableHeight, Short.MAX_VALUE)
						.addGap(5, 5, 5)));

		javax.swing.GroupLayout layoutBottomPane = new javax.swing.GroupLayout((JPanel) bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		layoutBottomPane
				.setHorizontalGroup(
						layoutBottomPane.createSequentialGroup().addGap(10, 10, 10)
								.addGroup(
										layoutBottomPane.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(layoutBottomPane.createSequentialGroup().addGap(10, 10, 10)
														.addComponent(panelTask, javax.swing.GroupLayout.DEFAULT_SIZE,
																maxHSize, maxHSize)
														.addGap(10, 10, 10))
												.addGroup(layoutBottomPane.createSequentialGroup().addGap(10, 10, 10)
														.addComponent(panelKeys, javax.swing.GroupLayout.DEFAULT_SIZE,
																maxHSize, maxHSize)
														.addGap(10, 10, 10)))
								.addGap(10, 10, 10));
		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup().addGap(5, 5, 5)
				.addComponent(panelTask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE,
						Short.MAX_VALUE)
				.addGap(10, 10, 10).addComponent(panelKeys, minPanelTableHeight - 2 * Globals.LINE_HEIGHT,
						minPanelTableHeight - 2 * Globals.LINE_HEIGHT, Short.MAX_VALUE)
				.addGap(5, 5, 5));

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout((JPanel) this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
								.addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin));

		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

		/*
		 * javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		 * this.setLayout(layout);
		 * 
		 * layout.setHorizontalGroup(
		 * layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(layout.createSequentialGroup()
		 * .addGap(10, 10, 10)
		 * .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.
		 * LEADING)
		 * .addGroup(layout.createSequentialGroup()
		 * .addComponent(panelLicencepools, javax.swing.GroupLayout.DEFAULT_SIZE,
		 * javax.swing.GroupLayout.DEFAULT_SIZE, maxHSize)
		 * .addGap(10, 10, 10)
		 * )
		 * .addGroup(layout.createSequentialGroup()
		 * .addComponent(panelKeys, javax.swing.GroupLayout.DEFAULT_SIZE, maxHSize,
		 * maxHSize)
		 * .addGap(10, 10, 10)
		 * )
		 * .addGroup(layout.createSequentialGroup()
		 * .addComponent(panelTask, javax.swing.GroupLayout.DEFAULT_SIZE, maxHSize,
		 * Short.MAX_VALUE)
		 * .addGap(10, 10, 10)
		 * )
		 * )
		 * )
		 * );
		 * layout.setVerticalGroup(
		 * layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		 * .addGroup(layout.createSequentialGroup()
		 * .addGap(5, 5, 5)
		 * .addComponent(panelLicencepools, minVSize,
		 * javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE )
		 * .addGap(5, 5, 5)
		 * .addComponent(panelTask, javax.swing.GroupLayout.PREFERRED_SIZE,
		 * javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE )
		 * .addGap(10, 10, 10)
		 * .addComponent(panelKeys, minVSize, minVSize, Short.MAX_VALUE)
		 * .addGap(5,5,5)
		 * )
		 * );
		 */

	}

	public void saveCurrentLicenceData() {
		HashMap<String, String> m = new HashMap<>();

		m.put(LicenceEntry.idKEY, jTextField_licenceID.getText());
		m.get(LicenceEntry.licenceContractIdKEY);
		m.put(LicenceEntry.typeKEY, jTextField_licenceType.getText());
		m.put(LicenceEntry.maxInstallationsKEY,
				LicenceEntry.produceNormalizedCount(jTextField_maxInstallations.getText()));
		m.put(LicenceEntry.boundToHostKEY, comboClient.getSelectedItem().toString());
		m.put(LicenceEntry.expirationDateKEY, jTextField_endOfLicence.getText());

		String contractSendValue = jTextField_licenceContract.getText();
		if (contractSendValue.equals("null"))
			contractSendValue = "";
		m.put("licenseContractId", contractSendValue);

		m.put("licensePoolId", panelLicencepools.getValueAt(panelLicencepools.getSelectedRow(), 0).toString());
		m.put("licenseKey", jTextFieldLKey.getText());

		enterLicenceController.saveNewLicence(m);

	}

	// ActionListener
	@Override
	public void actionPerformed(java.awt.event.ActionEvent evt) {
		if (evt.getSource() == jButtonCreateStandard) {
			startStandard();
		} else if (evt.getSource() == jButtonCreateVolume) {
			startVolume();
		} else if (evt.getSource() == jButtonCreateOEM) {
			startOEM();
		} else if (evt.getSource() == jButtonCreateConcurrent) {
			startConcurrent();
		} else if (evt.getSource() == jButtonSend) {
			deactivate();
			saveCurrentLicenceData();
			jTextFieldLKey.setText("");
		}
	}

	@Override
	public void reset() {
		panelLicencepools.removeListSelectionListener(licencePoolSelectionListener);
		super.reset();
		deactivate();
		panelLicencepools.addListSelectionListener(licencePoolSelectionListener);
		panelLicencepools.moveToValue(selectedLicencePool, 0);
	}

}
