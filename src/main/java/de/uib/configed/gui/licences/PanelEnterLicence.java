/*
 * PanelEnterLicence.java
 * after selecting a pool, one can add licence options for it
 * Created 17.02.2009-2015
 */

package de.uib.configed.gui.licences;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.ControlPanelEnterLicence;
import de.uib.configed.Globals;
import de.uib.configed.type.licences.LicenceEntry;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.timeedit.FEditDate;
import de.uib.utilities.table.gui.PanelGenEditTable;

/**
 * @author R. Röder
 */
public class PanelEnterLicence extends MultiTablePanel implements ActionListener {

	public PanelGenEditTable panelKeys;
	public PanelGenEditTable panelLicencepools;
	public PanelGenEditTable panelLicencecontracts;
	private int splitPaneHMargin = 1;

	private String selectedLicencePool = "";
	private ListSelectionListener licencePoolSelectionListener;

	private static final int MIN_HEIGHT = 50;
	private static final int MIN_PANEL_TABLE_HEIGHT = 60;
	private static final int MAX_WIDTH = 1000;

	private static final int MIN_FIELD_WIDTH = 40;
	private static final int MIN_FIELD_HEIGHT = 6;

	private JButton jButtonCreateStandard;
	private JButton jButtonCreateVolume;
	private JButton jButtonCreateOEM;
	private JButton jButtonCreateConcurrent;
	private JButton jButtonSend;

	private JTextField jTextFieldLicenceID;
	private JTextField jTextFieldLicenceType;

	private JTextField jTextFieldMaxInstallations;
	private JComboBox<String> comboClient;
	private JTextField jTextFieldEndOfLicence;
	private JTextField jTextFieldLicenceContract;
	private JTextField jTextFieldLKey;

	private FEditDate fEditDate;

	protected ControlPanelEnterLicence enterLicenceController;

	private ConfigedMain configedMain;

	private ComboBoxModel<String> emptyComboBoxModel = new DefaultComboBoxModel<>(new String[] { "" });

	/** Creates new form PanelEnterLicence */
	public PanelEnterLicence(ControlPanelEnterLicence enterLicenceController, ConfigedMain configedMain) {
		super(enterLicenceController);

		this.enterLicenceController = enterLicenceController;
		this.configedMain = configedMain;

		initComponents();
		defineListeners();
	}

	private void defineListeners() {
		panelLicencecontracts.getListSelectionModel().addListSelectionListener(listSelectionEvent -> {
			// Ignore extra messages.
			if (listSelectionEvent.getValueIsAdjusting()) {
				return;
			}

			ListSelectionModel lsm = (ListSelectionModel) listSelectionEvent.getSource();

			if (!lsm.isSelectionEmpty()) {
				int selectedRow = lsm.getMinSelectionIndex();
				String keyValue = panelLicencecontracts.getValueAt(selectedRow, 0).toString();

				if (jTextFieldLicenceContract.isEnabled()) {
					jTextFieldLicenceContract.setText(keyValue);
				}
			}
		});

		panelLicencepools.addListSelectionListener(listSelectionEvent -> {
			if (listSelectionEvent.getValueIsAdjusting()) {
				return;
			}

			int i = panelLicencepools.getSelectedRow();

			selectedLicencePool = "";

			if (i > -1) {
				selectedLicencePool = panelLicencepools.getValueAt(i, 0).toString();
			}

			panelLicencepools.setTitle(Configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencepool")
					+ ": " + selectedLicencePool);
		});

	}

	private void deactivate() {
		jTextFieldLicenceID.setEnabled(false);
		jTextFieldLicenceType.setEnabled(false);
		jTextFieldMaxInstallations.setEnabled(false);
		comboClient.setEnabled(false);
		jTextFieldEndOfLicence.setEnabled(false);
		jTextFieldLicenceContract.setEnabled(false);
		jTextFieldLKey.setEnabled(false);
		jButtonSend.setEnabled(false);
	}

	private boolean checkAndStart() {
		if (panelLicencepools.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(configedMain.licencesFrame,
					Configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectLicencepool"),
					Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
			return false;
		}

		if (panelLicencecontracts.getSelectedRow() == -1) {
			JOptionPane.showMessageDialog(configedMain.licencesFrame,
					Configed.getResourceValue("ConfigedMain.Licences.hint.pleaseSelectLicencecontract"),
					Configed.getResourceValue("ConfigedMain.Licences.hint.title"), JOptionPane.OK_OPTION);
			return false;
		}

		jTextFieldLicenceID.setEnabled(true);
		jTextFieldLicenceID.setText("l_" + Globals.getSeconds());

		jTextFieldEndOfLicence.setEnabled(true);
		jTextFieldEndOfLicence.setText("");
		jTextFieldLicenceContract.setEnabled(true);
		jTextFieldLicenceContract
				.setText("" + panelLicencecontracts.getValueAt(panelLicencecontracts.getSelectedRow(), 0));
		jTextFieldLicenceContract.setEditable(false);

		jTextFieldLKey.setEnabled(true);

		jButtonSend.setEnabled(true);

		return true;
	}

	private void startStandard() {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenceType.setEnabled(true);
		jTextFieldLicenceType.setText("RETAIL");
		jTextFieldLicenceType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText("1");
		jTextFieldMaxInstallations.setEditable(false);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);

	}

	private void startVolume() {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenceType.setEnabled(true);
		jTextFieldLicenceType.setText("VOLUME");
		jTextFieldLicenceType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText("0");
		jTextFieldMaxInstallations.setEditable(true);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);

	}

	private void startOEM() {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenceType.setEnabled(true);
		jTextFieldLicenceType.setText("OEM");
		jTextFieldLicenceType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText("1");
		jTextFieldMaxInstallations.setEditable(false);
		comboClient.setModel(
				new DefaultComboBoxModel<>(enterLicenceController.getChoicesAllHosts().toArray(new String[0])));
		comboClient.setEnabled(true);

	}

	private void startConcurrent() {
		if (!checkAndStart()) {
			return;
		}

		jTextFieldLicenceID.setEnabled(true);
		jTextFieldLicenceID.setText("l_" + Globals.getSeconds());
		jTextFieldLicenceType.setEnabled(true);
		jTextFieldLicenceType.setText("CONCURRENT");
		jTextFieldLicenceType.setEditable(false);
		jTextFieldMaxInstallations.setEnabled(true);

		jTextFieldMaxInstallations.setText("0");
		jTextFieldMaxInstallations.setEditable(false);
		comboClient.setEnabled(false);
		comboClient.setModel(emptyComboBoxModel);

	}

	private void initComponents() {
		panelKeys = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleLicenceOptionsView"), 0, true, 0, false,
				new int[] { PanelGenEditTable.POPUP_RELOAD }, false // searchpane
		);

		panelKeys.setMasterFrame(Globals.frame1);

		panelLicencepools = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencepool"), MAX_WIDTH, false, 0,
				false, new int[] { PanelGenEditTable.POPUP_RELOAD }, true);

		panelLicencepools.setMasterFrame(Globals.frame1);

		panelLicencecontracts = new PanelGenEditTable(
				Configed.getResourceValue("ConfigedMain.Licences.SectiontitleSelectLicencecontract"), 0, true, 1, false,
				new int[] { PanelGenEditTable.POPUP_DELETE_ROW, PanelGenEditTable.POPUP_SAVE,
						PanelGenEditTable.POPUP_CANCEL, PanelGenEditTable.POPUP_RELOAD },
				true);

		panelLicencecontracts.setMasterFrame(Globals.frame1);

		JLabel jLabelLicencePool = new JLabel();
		jButtonCreateStandard = new JButton();
		jButtonCreateStandard.setPreferredSize(Globals.buttonDimension);
		jButtonCreateVolume = new JButton();
		jButtonCreateVolume.setPreferredSize(Globals.buttonDimension);
		jButtonCreateOEM = new JButton();
		jButtonCreateOEM.setPreferredSize(Globals.buttonDimension);
		jButtonCreateConcurrent = new JButton();
		jButtonCreateConcurrent.setPreferredSize(Globals.buttonDimension);

		JLabel jLabelTask = new JLabel();
		JLabel jLabelConfigure = new JLabel();
		JPanel panelLicenceModel = new JPanel();
		JLabel jLabelSLid1 = new JLabel();
		JLabel jLabelSLid2 = new JLabel();
		JLabel jLabelSLid3 = new JLabel();
		JLabel jLabelSLid4 = new JLabel();
		JLabel jLabelSLid5 = new JLabel();
		JLabel jLabelSLid6 = new JLabel();
		jTextFieldLicenceID = new JTextField();
		jTextFieldLicenceType = new JTextField();
		jTextFieldMaxInstallations = new JTextField();

		comboClient = new JComboBox<>();
		comboClient.setFont(Globals.defaultFontBig);

		comboClient.setPreferredSize(new Dimension(200, 20));

		JLabel jLabelSLid3info = new JLabel();

		jTextFieldEndOfLicence = new JTextField();

		// edit only via fEditDate
		jTextFieldEndOfLicence.setEditable(false);
		jTextFieldEndOfLicence.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() > 1 || e.getButton() != MouseEvent.BUTTON1) {
					if (fEditDate == null) {
						fEditDate = new FEditDate(jTextFieldEndOfLicence.getText(), false);
					} else {
						fEditDate.setStartText(jTextFieldEndOfLicence.getText());
					}

					fEditDate.setCaller(jTextFieldEndOfLicence);
					fEditDate.init();
					try {
						java.awt.Point pointField = jTextFieldEndOfLicence.getLocationOnScreen();
						fEditDate.setLocation((int) pointField.getX() + 30, (int) pointField.getY() + 20);
					} catch (Exception ex) {
						Logging.info(this, "locationOnScreen ex " + ex);
					}

					fEditDate.setTitle(" (" + Globals.APPNAME + ") "
							+ Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid5"));

					fEditDate.setVisible(true);
				}

			}
		});
		jTextFieldEndOfLicence.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				/* Not needed */}

			@Override
			public void focusLost(FocusEvent e) {
				if (fEditDate != null) {
					fEditDate.deactivate();
				}
			}
		});

		jTextFieldLicenceContract = new JTextField();

		jButtonSend = new JButton();
		jButtonSend.setPreferredSize(Globals.buttonDimension);

		JLabel jLabelLKey = new JLabel();
		jTextFieldLKey = new JTextField();

		deactivate();

		JPanel panelEnterKey = new JPanel();

		jLabelLicencePool.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.Label"));

		jButtonCreateStandard.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.StandardLicense"));
		jButtonCreateStandard.setToolTipText(
				Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.StandardLicense.ToolTip"));
		jButtonCreateStandard.addActionListener(this);

		jButtonCreateVolume.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.VolumeLicense"));
		jButtonCreateVolume
				.setToolTipText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.VolumeLicense.ToolTip"));
		jButtonCreateVolume.addActionListener(this);

		jButtonCreateOEM.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.OEMLicense"));
		jButtonCreateOEM
				.setToolTipText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.OEMLicense.ToolTip"));
		jButtonCreateOEM.addActionListener(this);

		jButtonCreateConcurrent
				.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.ConcurrentLicense"));
		jButtonCreateConcurrent.setToolTipText(
				Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.ConcurrentLicense.ToolTip"));
		jButtonCreateConcurrent.addActionListener(this);

		jButtonSend.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.Execute"));
		jButtonSend.addActionListener(this);

		jLabelTask.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.Task") + ":");
		jLabelTask.setFont(Globals.defaultFontBold);
		jLabelConfigure.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.ChooseType"));
		jLabelConfigure.setFont(Globals.defaultFontStandardBold);

		panelLicenceModel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

		jLabelSLid1.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid1"));
		jLabelSLid2.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid2"));
		jLabelSLid3.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid3"));
		jLabelSLid4.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid4"));
		jLabelSLid5.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid5"));
		jLabelSLid6.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid6"));

		jLabelSLid3info.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelSLid3info"));

		de.uib.utilities.swing.Containership cs = new de.uib.utilities.swing.Containership(this);
		cs.doForAllContainedCompisOfClass("setFont", new Object[] { Globals.defaultFont }, JTextField.class);

		GroupLayout panelLicenceModelLayout = new GroupLayout(panelLicenceModel);
		panelLicenceModel.setLayout(panelLicenceModelLayout);
		panelLicenceModelLayout.setHorizontalGroup(panelLicenceModelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(panelLicenceModelLayout.createSequentialGroup().addContainerGap()
						.addGroup(panelLicenceModelLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(jLabelSLid4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jLabelSLid3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jLabelSLid2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE)
								.addComponent(jLabelSLid1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
										Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.UNRELATED)

						.addGroup(panelLicenceModelLayout.createParallelGroup(Alignment.LEADING, true)

								.addGroup(Alignment.LEADING, panelLicenceModelLayout.createSequentialGroup()
										.addGroup(panelLicenceModelLayout.createParallelGroup(Alignment.LEADING, true)
												.addComponent(comboClient, MIN_FIELD_WIDTH, 208, Short.MAX_VALUE)
												.addGroup(panelLicenceModelLayout.createSequentialGroup()
														.addComponent(jTextFieldMaxInstallations, MIN_FIELD_WIDTH, 112,
																GroupLayout.PREFERRED_SIZE)
														.addGap(5, 5, 5).addComponent(jLabelSLid3info, MIN_FIELD_WIDTH,
																112, GroupLayout.PREFERRED_SIZE))
												.addComponent(jTextFieldLicenceID, MIN_FIELD_WIDTH, 208,
														Short.MAX_VALUE)
												.addComponent(jTextFieldLicenceType, MIN_FIELD_WIDTH, 239,
														GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
										.addGroup(panelLicenceModelLayout.createParallelGroup(Alignment.LEADING, true)
												.addGroup(panelLicenceModelLayout.createSequentialGroup()
														.addComponent(jLabelSLid6, GroupLayout.PREFERRED_SIZE, 99,
																GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.UNRELATED)

														.addComponent(jTextFieldLicenceContract, MIN_FIELD_WIDTH, 200,
																GroupLayout.PREFERRED_SIZE))
												.addGroup(panelLicenceModelLayout.createSequentialGroup()
														.addComponent(jLabelSLid5, GroupLayout.PREFERRED_SIZE, 100,
																GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.UNRELATED)

														.addComponent(jTextFieldEndOfLicence, MIN_FIELD_WIDTH, 200,
																GroupLayout.PREFERRED_SIZE)))))
						.addContainerGap(10, Short.MAX_VALUE)));
		panelLicenceModelLayout.setVerticalGroup(panelLicenceModelLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, panelLicenceModelLayout.createSequentialGroup().addGap(0, 1, 3)
						.addGroup(panelLicenceModelLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jLabelSLid1, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldLicenceID, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelSLid5, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldEndOfLicence, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE))
						.addGap(0, 1, 3)
						.addGroup(panelLicenceModelLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jLabelSLid2, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldLicenceType, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE))
						.addGap(0, 1, 3)
						.addGroup(panelLicenceModelLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jLabelSLid3, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldMaxInstallations, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelSLid3info, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jLabelSLid6, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldLicenceContract, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED).addGap(0, 1, 3)
						.addGroup(panelLicenceModelLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jLabelSLid4, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(comboClient, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE))

				));

		panelEnterKey.setBorder(BorderFactory.createEtchedBorder());

		jLabelLKey.setText(Configed.getResourceValue("ConfigedMain.Licences.EnterLicense.LabelLicenseKey"));

		GroupLayout panelEnterKeyLayout = new GroupLayout(panelEnterKey);
		panelEnterKey.setLayout(panelEnterKeyLayout);
		panelEnterKeyLayout.setHorizontalGroup(panelEnterKeyLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(panelEnterKeyLayout.createSequentialGroup().addContainerGap()
						.addComponent(jLabelLKey, GroupLayout.PREFERRED_SIZE, 133, GroupLayout.PREFERRED_SIZE)
						.addGap(5, 5, 5).addComponent(jTextFieldLKey, MIN_FIELD_WIDTH, 326, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(10, Short.MAX_VALUE)));
		panelEnterKeyLayout.setVerticalGroup(panelEnterKeyLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(panelEnterKeyLayout.createSequentialGroup().addGap(0, 1, 5)
						.addGroup(panelEnterKeyLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(jLabelLKey, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(jTextFieldLKey, MIN_FIELD_HEIGHT, Globals.LINE_HEIGHT,
										GroupLayout.PREFERRED_SIZE))
						.addGap(0, 1, 5)));

		JPanel panelTask = new JPanel();
		panelTask.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

		GroupLayout layoutTask = new GroupLayout(panelTask);
		panelTask.setLayout(layoutTask);

		layoutTask.setHorizontalGroup(layoutTask.createParallelGroup(Alignment.LEADING).addGroup(layoutTask
				.createSequentialGroup().addGap(5, 5, 5)
				.addGroup(layoutTask.createParallelGroup(Alignment.LEADING)
						.addGroup(layoutTask.createSequentialGroup()
								.addGroup(layoutTask.createParallelGroup(Alignment.LEADING)
										.addGroup(layoutTask.createSequentialGroup()

												.addGroup(layoutTask.createParallelGroup(Alignment.LEADING)
														.addComponent(panelLicencecontracts, 50, 300, MAX_WIDTH))))

						)
						.addGroup(layoutTask.createSequentialGroup()
								.addComponent(jButtonSend, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addContainerGap(1587, Short.MAX_VALUE))
						.addGroup(
								layoutTask.createSequentialGroup()
										.addComponent(jButtonCreateStandard, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18)
										.addComponent(jButtonCreateVolume, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18)
										.addComponent(jButtonCreateOEM, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGap(18, 18, 18)
										.addComponent(jButtonCreateConcurrent, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addContainerGap(1226, Short.MAX_VALUE))
						.addGroup(layoutTask.createSequentialGroup().addGap(5, 5, 5).addComponent(jLabelTask)
								.addContainerGap(1515, Short.MAX_VALUE))
						.addGroup(layoutTask.createSequentialGroup().addGap(10, 10, 10).addComponent(jLabelConfigure)
								.addContainerGap(1515, Short.MAX_VALUE))
						.addGroup(layoutTask.createSequentialGroup()
								.addGroup(layoutTask.createParallelGroup(Alignment.TRAILING, true)
										.addComponent(panelEnterKey, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, MAX_WIDTH)
										.addComponent(panelLicenceModel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, MAX_WIDTH))
								.addGap(10, 10, 10)))));
		layoutTask.setVerticalGroup(layoutTask.createParallelGroup(Alignment.LEADING).addGroup(layoutTask
				.createSequentialGroup().addGap(5, 5, 5).addComponent(jLabelTask).addGap(5, 5, 5)
				.addComponent(panelLicencecontracts, MIN_PANEL_TABLE_HEIGHT, MIN_PANEL_TABLE_HEIGHT, Short.MAX_VALUE)
				.addGap(5, 5, 5).addComponent(jLabelConfigure).addGap(2, 2, 2)
				.addGroup(layoutTask.createParallelGroup(Alignment.BASELINE)
						.addComponent(jButtonCreateStandard, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCreateOEM, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCreateVolume, GroupLayout.PREFERRED_SIZE, 20, GroupLayout.PREFERRED_SIZE)
						.addComponent(jButtonCreateConcurrent, GroupLayout.PREFERRED_SIZE, 20,
								GroupLayout.PREFERRED_SIZE))
				.addGap(5, 5, 6)
				.addComponent(panelLicenceModel, MIN_HEIGHT, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2, 2, 2)
				.addComponent(panelEnterKey, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(2, 2, 2).addComponent(jButtonSend, 20, 20, 20).addGap(5, 5, 5)));

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setResizeWeight(0.3f);

		JPanel topPane = new JPanel();
		JPanel bottomPane = new JPanel();
		splitPane.setTopComponent(topPane);
		splitPane.setBottomComponent(bottomPane);
		topPane.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);
		bottomPane.setBackground(Globals.SECONDARY_BACKGROUND_COLOR);

		GroupLayout layoutTopPane = new GroupLayout(topPane);
		topPane.setLayout(layoutTopPane);
		layoutTopPane.setHorizontalGroup(layoutTopPane.createSequentialGroup().addGap(10, 10, 10)
				.addComponent(panelLicencepools, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, MAX_WIDTH)
				.addGap(10, 10, 10));
		layoutTopPane
				.setVerticalGroup(layoutTopPane.createParallelGroup(Alignment.LEADING)
						.addGroup(layoutTopPane
								.createSequentialGroup().addGap(5, 5, 5).addComponent(panelLicencepools,
										MIN_PANEL_TABLE_HEIGHT, MIN_PANEL_TABLE_HEIGHT, Short.MAX_VALUE)
								.addGap(5, 5, 5)));

		GroupLayout layoutBottomPane = new GroupLayout(bottomPane);
		bottomPane.setLayout(layoutBottomPane);
		layoutBottomPane.setHorizontalGroup(
				layoutBottomPane
						.createSequentialGroup().addGap(10, 10,
								10)
						.addGroup(
								layoutBottomPane.createParallelGroup(Alignment.LEADING)
										.addGroup(layoutBottomPane.createSequentialGroup().addGap(10, 10, 10)
												.addComponent(panelTask, GroupLayout.DEFAULT_SIZE, MAX_WIDTH, MAX_WIDTH)
												.addGap(10, 10, 10))
										.addGroup(layoutBottomPane.createSequentialGroup().addGap(10, 10, 10)
												.addComponent(panelKeys, GroupLayout.DEFAULT_SIZE, MAX_WIDTH, MAX_WIDTH)
												.addGap(10, 10, 10)))
						.addGap(10, 10, 10));
		layoutBottomPane.setVerticalGroup(layoutBottomPane.createSequentialGroup().addGap(5, 5, 5)
				.addComponent(panelTask, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
				.addGap(10, 10, 10).addComponent(panelKeys, MIN_PANEL_TABLE_HEIGHT - 2 * Globals.LINE_HEIGHT,
						MIN_PANEL_TABLE_HEIGHT - 2 * Globals.LINE_HEIGHT, Short.MAX_VALUE)
				.addGap(5, 5, 5));

		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		layout.setHorizontalGroup(
				layout.createSequentialGroup().addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
						.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(splitPane, 0,
								GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
						.addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin));

		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(splitPane, 0,
				GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE));

	}

	public void saveCurrentLicenceData() {
		HashMap<String, String> m = new HashMap<>();

		m.put(LicenceEntry.ID_KEY, jTextFieldLicenceID.getText());
		m.put(LicenceEntry.LICENCE_CONTRACT_ID_KEY, jTextFieldLicenceContract.getText());
		m.put(LicenceEntry.TYPE_KEY, jTextFieldLicenceType.getText());
		m.put(LicenceEntry.MAX_INSTALLATIONS_KEY,
				LicenceEntry.produceNormalizedCount(jTextFieldMaxInstallations.getText()));
		m.put(LicenceEntry.BOUND_TO_HOST_KEY, comboClient.getSelectedItem().toString());
		m.put(LicenceEntry.EXPIRATION_DATE_KEY, jTextFieldEndOfLicence.getText());

		String contractSendValue = jTextFieldLicenceContract.getText();
		if (contractSendValue.equals("null")) {
			contractSendValue = "";
		}

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
