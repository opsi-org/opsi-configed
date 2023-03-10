/**
	(c) uib.de
	2016-2017
	
*/

package de.uib.configed.gui.hwinfopage;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.io.FileUtils;

import de.uib.configed.Configed;
import de.uib.configed.ConfigedMain;
import de.uib.configed.Globals;
import de.uib.configed.productaction.PanelMountShare;
import de.uib.connectx.SmbConnect;
import de.uib.opsicommand.sshcommand.EmptyCommand;
import de.uib.opsicommand.sshcommand.SSHConnectExec;
import de.uib.opsidatamodel.AbstractPersistenceController;
import de.uib.utilities.FileX;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.CheckedLabel;
import de.uib.utilities.swing.FLoadingWaiter;
import de.uib.utilities.swing.ImagePanel;
import de.uib.utilities.swing.JTextShowField;
import de.uib.utilities.thread.WaitCursor;

public class PanelDriverUpload extends JPanel implements de.uib.utilities.NameProducer {

	int firstLabelWidth = Globals.FIRST_LABEL_WIDTH;
	int hFirstGap = Globals.HFIRST_GAP;

	protected int hGap = Globals.HGAP_SIZE / 2;
	protected int vGap = Globals.VGAP_SIZE / 2;
	protected int hLabel = Globals.BUTTON_HEIGHT;

	protected String byAuditPath = "";

	protected JTextShowField fieldByAuditPath;
	protected JTextShowField fieldClientname;

	JComboBox<String> comboChooseDepot;
	JComboBox<String> comboChooseWinProduct;
	JButton btnShowDrivers;
	JButton btnCreateDrivers;

	JLabel labelDriverToIntegrate;
	PanelMountShare panelMountShare;

	String depotProductDirectory = "";
	boolean smbMounted = false;
	String driverDirectory = "";

	boolean stateDriverPath = false;
	CheckedLabel driverPathChecked;
	boolean stateServerPath = false;
	CheckedLabel serverPathChecked;

	JLabel jLabelUploading;
	ImagePanel waitingImage;

	class RadioButtonIntegrationType extends JRadioButton {
		private String subdir;

		public RadioButtonIntegrationType(String text, String subdir) {
			super(text);
			this.subdir = subdir;
		}

		public String getSubdir() {
			return subdir;
		}
	}

	class FileNameDocumentListener implements DocumentListener {
		private boolean checkFiles() {
			boolean result = false;

			if (fieldServerPath != null && fieldDriverPath != null) {
				try {
					targetPath = new File(fieldServerPath.getText());
					driverPath = new File(fieldDriverPath.getText());

					stateServerPath = targetPath.isDirectory();
					serverPathChecked.setSelected(stateServerPath);
					Logging.info(this, "checkFiles  stateServerPath targetPath " + targetPath);
					Logging.info(this, "checkFiles  stateServerPath driverPath " + driverPath);
					Logging.info(this, "checkFiles  stateServerPath isDirectory " + stateServerPath);

					stateDriverPath = driverPath.exists();
					driverPathChecked.setSelected(stateDriverPath);
					Logging.info(this, "checkFiles stateDriverPath " + stateDriverPath);

					if (stateServerPath && stateDriverPath) {
						result = true;
					}
				} catch (Exception ex) {
					Logging.info(this, "checkFiles " + ex);
				}

			}

			Logging.info(this, "checkFiles " + result);

			if (buttonUploadDrivers != null) {
				buttonUploadDrivers.setEnabled(result);

				if (result) {
					buttonUploadDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.execute"));
				} else {
					buttonUploadDrivers.setToolTipText("Treiber- bzw. Zielpfad noch nicht gefunden");
				}
			}

			return result;
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			Logging.debug(this, "changedUpdate ");
			checkFiles();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			Logging.debug(this, "insertUpdate ");
			checkFiles();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			Logging.debug(this, "removeUpdate ");
			checkFiles();
		}
	}

	RadioButtonIntegrationType buttonStandard;
	RadioButtonIntegrationType buttonPreferred;
	RadioButtonIntegrationType buttonNotPreferred;
	RadioButtonIntegrationType buttonAdditional;
	RadioButtonIntegrationType buttonByAudit;

	List<RadioButtonIntegrationType> radioButtons;

	JButton buttonCallSelectDriverFiles;
	protected JTextShowField fieldDriverPath;
	JFileChooser chooserDriverPath;

	// server path finding
	JTextShowField fieldServerPath;
	JButton buttonCallChooserServerpath;
	JFileChooser chooserServerpath;

	File driverPath;
	File targetPath;

	JButton buttonUploadDrivers;

	String selectedDepot = null;
	String winProduct = "";

	JLabel jLabelTopic;
	int wLeftText;

	AbstractPersistenceController persist;
	ConfigedMain main;
	String server;
	JFrame rootFrame;

	public PanelDriverUpload(ConfigedMain main, AbstractPersistenceController persist, JFrame root) {
		this.main = main;
		this.persist = persist;
		this.rootFrame = root;
		server = main.getConfigserver();

		defineChoosers();

		selectedDepot = (String) comboChooseDepot.getSelectedItem();
		depotProductDirectory = SmbConnect.getInstance().buildSambaTarget(selectedDepot,
				de.uib.connectx.SmbConnect.PRODUCT_SHARE_RW);
		Logging.info(this, "depotProductDirectory " + depotProductDirectory);

		jLabelTopic = new JLabel(Configed.getResourceValue("PanelDriverUpload.topic"));
		wLeftText = jLabelTopic.getPreferredSize().width;

		labelDriverToIntegrate = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));

		panelMountShare = new PanelMountShare(this, main, root, labelDriverToIntegrate.getPreferredSize().width + hGap)

		{
			@Override
			protected boolean checkConnectionToShare() {
				boolean connected = super.checkConnectionToShare();
				if (comboChooseWinProduct != null // we have an initialized gui
						&& connected) {
					evaluateWinProducts();
				}

				return connected;
			}
		};

		jLabelUploading = new JLabel("uploading");

		initComponents();

		Logging.info(this, "depotProductDirectory " + depotProductDirectory);
		smbMounted = new File(depotProductDirectory).exists();
		panelMountShare.mount(smbMounted);

		evaluateWinProducts();

		buildPanel();
	}

	private void defineChoosers() {
		comboChooseDepot = new JComboBox<>();
		comboChooseDepot.setSize(Globals.textfieldDimension);

		comboChooseDepot.setModel(new DefaultComboBoxModel<>(main.getLinkedDepots().toArray(new String[0])));

		comboChooseDepot.setEnabled(false);

		comboChooseDepot.addActionListener(actionEvent -> {
			selectedDepot = (String) comboChooseDepot.getSelectedItem();
			Logging.info(this, "actionPerformed  depot selected " + selectedDepot);
		});

		comboChooseWinProduct = new JComboBox<>();
		comboChooseWinProduct.setSize(Globals.textfieldDimension);
		comboChooseWinProduct.addActionListener(actionEvent -> {
			winProduct = "" + comboChooseWinProduct.getSelectedItem();
			Logging.info(this, "winProduct  " + winProduct);
			produceTarget();
		});

		chooserDriverPath = new JFileChooser();
		chooserDriverPath.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooserDriverPath.setPreferredSize(Globals.filechooserSize);
		chooserDriverPath.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", Configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserDriverPath);

		chooserDriverPath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserDriverPath.setDialogTitle(
				Globals.APPNAME + " " + Configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));

		chooserServerpath = new JFileChooser();
		chooserServerpath.setPreferredSize(Globals.filechooserSize);
		chooserServerpath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserServerpath.setApproveButtonText(Configed.getResourceValue("FileChooser.approve"));
		UIManager.put("FileChooser.cancelButtonText", Configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserServerpath);

		chooserServerpath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserServerpath.setDialogTitle(
				Globals.APPNAME + " " + Configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));

	}

	private void initComponents() {
		defineChoosers();
	}

	private void evaluateWinProducts() {
		retrieveWinProducts();

		winProduct = (String) comboChooseWinProduct.getSelectedItem();
		produceTarget();
	}

	private void retrieveWinProducts() {
		Logging.info(this, "retrieveWinProducts in " + depotProductDirectory);

		if (depotProductDirectory == null) {
			return;
		}

		// not yet a depot selected

		smbMounted = new File(depotProductDirectory).exists();

		Logging.info(this, "retrieveWinProducts smbMounted " + smbMounted);

		List<String> winProducts = persist.getWinProducts(server, depotProductDirectory);

		comboChooseWinProduct.setModel(new DefaultComboBoxModel<>(winProducts.toArray(new String[0])));
	}

	protected void buildPanel() {

		fieldByAuditPath = new JTextShowField();

		fieldClientname = new JTextShowField();

		JLabel jLabelDepotServer = new JLabel(Configed.getResourceValue("PanelDriverUpload.DepotServer"));
		JLabel jLabelWinProduct = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelWinProduct"));

		buttonCallSelectDriverFiles = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectDriverFiles.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallSelectDriverFiles.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallSelectDriverFiles
				.setToolTipText(Configed.getResourceValue("PanelDriverUpload.hintDriverToIntegrate"));

		fieldServerPath = new JTextShowField(true);
		fieldServerPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		fieldServerPath.setForeground(Globals.greyed);

		buttonCallChooserServerpath = new JButton("", Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setSelectedIcon(Globals.createImageIcon("images/folder_16.png", ""));
		buttonCallChooserServerpath.setPreferredSize(Globals.graphicButtonDimension);
		buttonCallChooserServerpath.setToolTipText(Configed.getResourceValue("PanelDriverUpload.determineServerPath"));

		buttonCallChooserServerpath.addActionListener(actionEvent -> chooseServerpath());

		JLabel jLabelShowDrivers = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelShowDrivers"));
		btnShowDrivers = new JButton("", Globals.createImageIcon("images/show-menu.png", ""));
		btnShowDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.btnShowDrivers.tooltip"));
		btnShowDrivers.addActionListener(actionEvent -> new Thread() {
			@Override
			public void run() {
				new SSHConnectExec(main,
						// Empty_Command(String id, String c, String mt, boolean ns)
						(new EmptyCommand("show_drivers.py", // id not needed
								"/var/lib/opsi/depot/" + comboChooseWinProduct.getSelectedItem() + "/show_drivers.py "
										+ fieldClientname.getText(),
								"show_drivers.py", // menuText - not needed
								false // needSudo?
				)));
			}
		}.start());

		JLabel jLabelCreateDrivers = new JLabel(Configed.getResourceValue("PanelDriverUpload.labelCreateDriverLinks"));
		btnCreateDrivers = new JButton("", Globals.createImageIcon("images/run-build-file.png", ""));
		btnCreateDrivers.setToolTipText(Configed.getResourceValue("PanelDriverUpload.btnCreateDrivers.tooltip"));
		btnCreateDrivers.addActionListener(actionEvent -> new SSHConnectExec(main,
				// Empty_Command(String id, String c, String mt, boolean ns)
				new EmptyCommand("create_driver_links.py", // id not needed
						"/var/lib/opsi/depot/" + comboChooseWinProduct.getSelectedItem() + "/create_driver_links.py ",
						"create_driver_links.py", // menutext - not needed
						true // need sudo ?
				)));

		JLabel labelTargetPath = new JLabel(Configed.getResourceValue("CompleteWinProducts.labelTargetPath"));
		fieldServerPath = new JTextShowField(true);
		fieldServerPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		fieldDriverPath = new JTextShowField(true);
		fieldDriverPath.getDocument().addDocumentListener(new FileNameDocumentListener());

		final JPanel thisPanel = this;

		buttonCallSelectDriverFiles.addActionListener(actionEvent -> {
			int returnVal = chooserDriverPath.showOpenDialog(thisPanel);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String pathInstallFiles = chooserDriverPath.getSelectedFile().getPath();
				fieldDriverPath.setText(pathInstallFiles);
				fieldDriverPath.setCaretPosition(pathInstallFiles.length());
			} else {
				fieldDriverPath.setText("");
			}
		});

		JLabel jLabelByAuditDriverLocationPath = new JLabel(
				Configed.getResourceValue("PanelDriverUpload.byAuditDriverLocationPath"));
		JLabel labelDriverLocationType = new JLabel(Configed.getResourceValue("PanelDriverUpload.type"));

		radioButtons = new ArrayList<>();

		buttonStandard = new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.standard"),
				FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS));
		buttonPreferred = new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.preferred"),
				FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS_PREFERRED));
		buttonNotPreferred = new RadioButtonIntegrationType(
				Configed.getResourceValue("PanelDriverUpload.type.excluded"),
				FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS_EXCLUDED));
		buttonAdditional = new RadioButtonIntegrationType(
				Configed.getResourceValue("PanelDriverUpload.type.additional"),
				FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS_ADDITIONAL));
		buttonByAudit = new RadioButtonIntegrationType(Configed.getResourceValue("PanelDriverUpload.type.byAudit"),
				FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS_BY_AUDIT));

		radioButtons.add(buttonStandard);
		radioButtons.add(buttonPreferred);
		radioButtons.add(buttonNotPreferred);
		radioButtons.add(buttonAdditional);
		radioButtons.add(buttonByAudit);

		ButtonGroup buttonGroup = new ButtonGroup();

		for (final RadioButtonIntegrationType button : radioButtons) {
			buttonGroup.add(button);
			button.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						Logging.debug(this, " " + e);
						driverDirectory = button.getSubdir();

						produceTarget();
					}
				}
			});
		}

		buttonByAudit.setSelected(true);

		JPanel panelButtonGroup = new JPanel();
		GroupLayout layoutButtonGroup = new GroupLayout(panelButtonGroup);
		panelButtonGroup.setLayout(layoutButtonGroup);
		panelButtonGroup.setBorder(new javax.swing.border.LineBorder(Globals.blueGrey, 1, true));

		layoutButtonGroup.setVerticalGroup(layoutButtonGroup.createSequentialGroup().addGap(vGap, vGap, vGap)
				.addComponent(labelDriverLocationType, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(vGap, vGap, vGap)
				.addComponent(buttonStandard, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonPreferred, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonNotPreferred, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonAdditional, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addComponent(buttonByAudit, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGroup(layoutButtonGroup.createParallelGroup()
						.addComponent(jLabelByAuditDriverLocationPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldByAuditPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(vGap, vGap, vGap));

		layoutButtonGroup
				.setHorizontalGroup(
						layoutButtonGroup.createSequentialGroup().addGap(hGap, hGap, hGap)
								.addGroup(layoutButtonGroup.createParallelGroup()
										.addComponent(labelDriverLocationType, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonStandard, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonPreferred, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonNotPreferred, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonAdditional, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(buttonByAudit, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
										.addGroup(layoutButtonGroup.createSequentialGroup().addGap(50, 50, 50)
												.addComponent(jLabelByAuditDriverLocationPath, 10,
														GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
												.addGap(hGap, hGap, hGap)
												.addComponent(fieldByAuditPath, Globals.BUTTON_WIDTH,
														Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
												.addGap(hGap, hGap, hGap)))
								.addGap(hGap, hGap, hGap)

				);

		driverPathChecked = new CheckedLabel(Configed.getResourceValue("PanelDriverUpload.driverpathConnected"),
				Globals.createImageIcon("images/checked_withoutbox.png", ""),
				Globals.createImageIcon("images/checked_empty_withoutbox.png", ""), stateDriverPath);

		serverPathChecked = new CheckedLabel(Configed.getResourceValue("PanelDriverUpload.targetdirConnected"),
				Globals.createImageIcon("images/checked_withoutbox.png", "Z"),
				Globals.createImageIcon("images/checked_empty_withoutbox.png", ""), true);

		buttonUploadDrivers = new JButton("", Globals.createImageIcon("images/upload2product.png", ""));
		buttonUploadDrivers.setEnabled(false);
		buttonUploadDrivers.setSelectedIcon(Globals.createImageIcon("images/upload2product.png", ""));
		// buttonUploadDrivers.setDisabledIcon(

		buttonUploadDrivers.setEnabled(false);

		buttonUploadDrivers.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Logging.info(this, "actionPerformed on buttonUploadDrivers from " + fieldDriverPath.getText() + " to "
						+ fieldServerPath.getText());
				final Color saveColor = buttonUploadDrivers.getBackground();

				buttonUploadDrivers.setBackground(Globals.FAILED_BACKGROUND_COLOR);
				execute();
				buttonUploadDrivers.setBackground(saveColor);
			}
		});

		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);
		int lh = Globals.LINE_HEIGHT - 4;
		layoutByAuditInfo.setVerticalGroup(layoutByAuditInfo.createSequentialGroup().addGap(vGap, vGap * 2, vGap * 2)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelTopic, lh, lh, lh).addComponent(fieldClientname, lh, lh, lh))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(jLabelDepotServer, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(comboChooseDepot, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(jLabelWinProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(comboChooseWinProduct, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jLabelShowDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(btnShowDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(jLabelCreateDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(btnCreateDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(labelDriverToIntegrate, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(buttonCallSelectDriverFiles, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldDriverPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addComponent(panelButtonGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,
						GroupLayout.PREFERRED_SIZE)
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addComponent(panelMountShare, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
				.addGap(Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2, Globals.VGAP_SIZE / 2)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(labelTargetPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonCallChooserServerpath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT)
						.addComponent(fieldServerPath, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT))
				.addGap(2 * vGap, 3 * vGap, 3 * vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(driverPathChecked, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(serverPathChecked, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT)
						.addComponent(buttonUploadDrivers, Globals.LINE_HEIGHT, Globals.LINE_HEIGHT,
								Globals.LINE_HEIGHT))

				.addGap(vGap, vGap * 2, vGap * 2)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)

						.addGap(Globals.LINE_HEIGHT)

				).addGap(vGap, vGap * 2, vGap * 2));

		layoutByAuditInfo
				.setHorizontalGroup(
						layoutByAuditInfo.createParallelGroup()
								.addGroup(
										layoutByAuditInfo.createSequentialGroup()
												.addGap(hFirstGap, hFirstGap, hFirstGap)
												.addGroup(layoutByAuditInfo.createParallelGroup()
														.addGroup(layoutByAuditInfo.createSequentialGroup()
																.addComponent(jLabelTopic, 5, wLeftText, wLeftText)
																.addGap(hFirstGap, hFirstGap, hFirstGap)

																.addComponent(fieldClientname, Globals.BUTTON_WIDTH,
																		Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2))
														.addGroup(
																layoutByAuditInfo.createSequentialGroup().addComponent(
																		panelMountShare, GroupLayout.PREFERRED_SIZE,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.PREFERRED_SIZE))
														.addGroup(layoutByAuditInfo.createSequentialGroup()
																.addComponent(jLabelDepotServer)
																.addGap(hGap, hGap, hGap)
																.addComponent(comboChooseDepot, Globals.BUTTON_WIDTH,
																		Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2)
																.addGap(hGap, hGap, hGap)
																.addComponent(jLabelWinProduct,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.PREFERRED_SIZE)
																.addGap(hGap, hGap, hGap)
																.addComponent(comboChooseWinProduct,
																		Globals.BUTTON_WIDTH, Globals.BUTTON_WIDTH * 2,
																		Globals.BUTTON_WIDTH * 3))
														.addGroup(layoutByAuditInfo.createSequentialGroup()
																.addComponent(jLabelShowDrivers, Globals.BUTTON_WIDTH,
																		Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
																.addGap(hGap, hGap, hGap).addComponent(
																		btnShowDrivers, Globals.GRAPHIC_BUTTON_WIDTH,
																		Globals.GRAPHIC_BUTTON_WIDTH,
																		Globals.GRAPHIC_BUTTON_WIDTH))
														.addGroup(layoutByAuditInfo.createSequentialGroup()
																.addComponent(jLabelCreateDrivers, Globals.BUTTON_WIDTH,
																		Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE)
																.addGap(hGap, hGap, hGap).addComponent(
																		btnCreateDrivers, Globals.GRAPHIC_BUTTON_WIDTH,
																		Globals.GRAPHIC_BUTTON_WIDTH,
																		Globals.GRAPHIC_BUTTON_WIDTH))
														.addGroup(layoutByAuditInfo.createSequentialGroup()
																.addComponent(labelDriverToIntegrate,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.PREFERRED_SIZE,
																		GroupLayout.PREFERRED_SIZE)
																.addGap(hGap, hGap, hGap)
																.addComponent(buttonCallSelectDriverFiles,
																		Globals.GRAPHIC_BUTTON_WIDTH,
																		Globals.GRAPHIC_BUTTON_WIDTH,
																		Globals.GRAPHIC_BUTTON_WIDTH)
																.addGap(hFirstGap, hFirstGap, hFirstGap)
																.addComponent(fieldDriverPath, Globals.BUTTON_WIDTH,
																		Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE))
														.addComponent(panelButtonGroup, GroupLayout.PREFERRED_SIZE,
																GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
														.addGroup(
																layoutByAuditInfo.createSequentialGroup().addComponent(
																		panelMountShare, GroupLayout.PREFERRED_SIZE,
																		GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE))
														.addGroup(layoutByAuditInfo.createSequentialGroup()
																.addComponent(labelTargetPath,
																		labelDriverToIntegrate.getPreferredSize().width,
																		labelDriverToIntegrate.getPreferredSize().width,
																		labelDriverToIntegrate.getPreferredSize().width)
																.addGap(hGap, hGap, hGap)
																.addComponent(buttonCallChooserServerpath,
																		Globals.GRAPHIC_BUTTON_WIDTH,
																		Globals.GRAPHIC_BUTTON_WIDTH,
																		Globals.GRAPHIC_BUTTON_WIDTH)
																.addGap(hFirstGap, hFirstGap, hFirstGap)
																.addComponent(fieldServerPath, Globals.BUTTON_WIDTH * 2,
																		Globals.BUTTON_WIDTH * 2, Short.MAX_VALUE))

												).addGap(hFirstGap, hFirstGap, hFirstGap))
								.addGroup(layoutByAuditInfo.createSequentialGroup().addGap(5, 5, Short.MAX_VALUE)

										.addComponent(driverPathChecked, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

										.addGap(hGap, 2 * hGap, 2 * hGap)

										.addComponent(serverPathChecked, GroupLayout.PREFERRED_SIZE,
												GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)

										.addGap(hGap, 2 * hGap, 2 * hGap)
										.addComponent(buttonUploadDrivers, Globals.GRAPHIC_BUTTON_WIDTH,
												Globals.GRAPHIC_BUTTON_WIDTH, Globals.GRAPHIC_BUTTON_WIDTH)
										.addGap(hFirstGap / 2, hFirstGap / 2, hFirstGap / 2))

								.addGroup(layoutByAuditInfo.createSequentialGroup()

										.addGap(hGap, hGap, Short.MAX_VALUE)

										.addGap(hFirstGap / 2, hFirstGap / 2, hFirstGap / 2)));

		setBackground(Globals.BACKGROUND_COLOR_3);

	}

	public void makePath(File path) {
		Logging.info(this, "makePath for " + path);

		if (path != null && !path.exists()) {

			int returnedOption = JOptionPane.showOptionDialog(rootFrame,
					Configed.getResourceValue("PanelDriverUpload.makeFilePath.text"),
					Configed.getResourceValue("PanelDriverUpload.makeFilePath.title"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, null, null);

			if (returnedOption == JOptionPane.YES_OPTION) {
				path.mkdirs();
			}
		}

		Logging.info(this, "makePath result " + path);
	}

	protected void execute() {

		final FLoadingWaiter waiter = new FLoadingWaiter(this, Globals.APPNAME,
				Configed.getResourceValue("PanelDriverUpload.execute.running"));
		waiter.startWaiting();

		final WaitCursor waitCursor = new WaitCursor(rootFrame);

		new Thread() {
			@Override
			public void run() {
				try {

					Logging.info(this, "copy  " + driverPath + " to " + targetPath);

					makePath(targetPath);

					stateServerPath = targetPath.exists();
					serverPathChecked.setSelected(stateServerPath);
					if (stateServerPath) {
						try {
							if (driverPath.isDirectory()) {
								FileUtils.copyDirectoryToDirectory(driverPath, targetPath);
							} else {
								FileUtils.copyFileToDirectory(driverPath, targetPath);
							}
						} catch (IOException iox) {
							waitCursor.stop();
							Logging.error("copy error:\n" + iox, iox);
						}
					} else {
						Logging.info(this, "execute: targetPath does not exist");
					}

					if (stateServerPath) {
						String driverDir = "/" + SmbConnect.unixPath(SmbConnect.directoryProducts) + "/" + winProduct
								+ "/" + SmbConnect.unixPath(SmbConnect.DIRECTORY_DRIVERS);
						Logging.info(this, "set rights for " + driverDir);
						persist.setRights(driverDir);
					}

					waitCursor.stop();

					if (waiter != null) {
						waiter.setReady();
					}
				} catch (Exception ex) {
					waitCursor.stop();
					Logging.error("error in uploading :\n" + ex, ex);
				}

			}
		}.start();

	}

	public void setByAuditPath(String s) {
		byAuditPath = s;
		fieldByAuditPath.setText(s);
		produceTarget();
	}

	public void setClientName(String s) {
		fieldClientname.setText(s);
	}

	public void setDepot(String s) {
		comboChooseDepot.setModel(new DefaultComboBoxModel<>(new String[] { s }));
	}

	private void produceTarget() {
		if (fieldServerPath == null) {
			return; // caution we are not yet initialized
		}

		String result = depotProductDirectory + File.separator + winProduct + File.separator + driverDirectory;

		if (buttonByAudit.isSelected()) {
			result = result + File.separator + byAuditPath;
		}

		fieldServerPath.setText(result);

	}

	private void chooseServerpath() {

		String oldServerPath = fieldServerPath.getText();
		File currentDirectory = new File(oldServerPath);

		makePath(currentDirectory);
		chooserServerpath.setCurrentDirectory(currentDirectory);

		int returnVal = chooserServerpath.showOpenDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String serverPathGot = chooserServerpath.getSelectedFile().getPath();
			fieldServerPath.setText(serverPathGot);
			fieldServerPath.setCaretPosition(serverPathGot.length());
		}

	}

	// implements NameProducer
	@Override
	public String produceName() {

		if (fieldServerPath != null) {
			Logging.info(this, "produceName ? fieldServerPath , depotProductDirectory " + fieldServerPath.getText()
					+ " , " + depotProductDirectory);
		}

		if (fieldServerPath == null || fieldServerPath.getText().equals("")
				|| fieldServerPath.getText().startsWith(depotProductDirectory)) {
			return depotProductDirectory;
		}

		return fieldServerPath.getText();
	}

	@Override
	public String getDefaultName() {
		return byAuditPath;
	}
}
