/**
	(c) uib.de
	2016-2017
	
*/

package de.uib.configed.gui.hwinfopage;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.io.*;

import java.nio.file.*;

import de.uib.configed.*;
import de.uib.configed.tree.*;
import de.uib.utilities.*;
import de.uib.utilities.thread.WaitCursor;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;

import de.uib.opsidatamodel.*;
import de.uib.connectx.SmbConnect;
import de.uib.configed.productaction.*;

import de.uib.opsicommand.sshcommand.*;

import org.apache.commons.io.FileUtils; 


public class PanelDriverUpload extends JPanel
	implements de.uib.utilities.NameProducer
{
	
	int firstLabelWidth = de.uib.configed.Globals.firstLabelWidth;
	int hFirstGap = de.uib.configed.Globals.hFirstGap;
	
	protected int hGap = de.uib.utilities.Globals.hGapSize/2;
	protected int vGap = de.uib.utilities.Globals.vGapSize/2;
	protected int hLabel = de.uib.utilities.Globals.buttonHeight;

	
	protected String byAuditPath = "";
	
	protected JTextShowField fieldByAuditPath;
	protected JTextShowField fieldClientname;
	
	JComboBox  comboChooseDepot;
	JComboBox  comboChooseWinProduct;
	JButton btnShowDrivers;
	JButton btnCreateDrivers;
	
	JLabel label_driverToIntegrate;
	PanelMountShare panelMountShare;
	
	String depotProductDirectory = "";
	boolean smbMounted = false;
	String driverDirectory = "";
	
	boolean stateDriverPath = false;
	CheckedLabel driverPathChecked;
	boolean stateServerPath = false;
	CheckedLabel serverPathChecked;
	
	JLabel label_uploading;
	ImagePanel waitingImage;
	
	
	class RadioButtonIntegrationType extends JRadioButton
	{
		private String subdir;
		public RadioButtonIntegrationType(String text, String subdir)
		{
			super(text);
			this.subdir = subdir;
		}
		
		public String getSubdir()
		{
			return subdir;
		}
	}
	
	class FileNameDocumentListener implements DocumentListener
	{
		public void changedUpdate(DocumentEvent e)
		{
			logging.debug(this, "changedUpdate ");
			checkFiles();
		}
		public void insertUpdate(DocumentEvent e)
		{
			logging.debug(this, "insertUpdate ");
			checkFiles();
		}
		public void removeUpdate(DocumentEvent e)
		{
			logging.debug(this, "removeUpdate ");
			checkFiles();
		}
	}
	
			
	
	RadioButtonIntegrationType buttonStandard;
	RadioButtonIntegrationType buttonPreferred;
	RadioButtonIntegrationType buttonNotPreferred;
	RadioButtonIntegrationType buttonAdditional;
	RadioButtonIntegrationType buttonByAudit;
	
	Vector<RadioButtonIntegrationType> radioButtons;
	
	
	JButton buttonCallSelectDriverFiles;
	protected JTextShowField fieldDriverPath;
	JFileChooser chooserDriverPath;
	
	
	//server path finding
	JTextShowField fieldServerPath;
	JButton buttonCallChooserServerpath;
	JFileChooser chooserServerpath;
	
	
	File driverPath;
	File targetPath;
			
	
	JButton buttonUploadDrivers;
	
	String selectedDepot = null;
	String winProduct = "";
	
	JLabel label_topic;
	int wLeftText;
		
	PersistenceController persist;
	ConfigedMain main;
	String server;
	JFrame rootFrame;


	public PanelDriverUpload(ConfigedMain main, PersistenceController persist, JFrame root)
	{
		this.main =main; 
		this.persist = persist;
		this.rootFrame = root;
		server = main.getConfigserver();
		
		
		defineChoosers();
		
		selectedDepot = "" + comboChooseDepot.getSelectedItem();
		depotProductDirectory = SmbConnect.getInstance().buildSambaTarget(selectedDepot, de.uib.connectx.SmbConnect.PRODUCT_SHARE_RW);
		logging.info(this, "depotProductDirectory " + depotProductDirectory);
		
		label_topic = new JLabel(configed.getResourceValue("PanelDriverUpload.topic"));
		wLeftText = label_topic.getPreferredSize().width;
		
		label_driverToIntegrate = new JLabel(configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));
		
		
		panelMountShare = new PanelMountShare((de.uib.utilities.NameProducer) this, main, root, label_driverToIntegrate.getPreferredSize().width + hGap)
		
			{
				@Override
				protected boolean checkConnectionToShare()
				{
					boolean connected = super.checkConnectionToShare();
					if (comboChooseWinProduct != null //we have an initialized gui
						&& connected)
					{
						evaluateWinProducts();
					}
					
					return connected;
				}
			}
		;

		
		label_uploading = new JLabel("uploading");
		//waitingImage = new ImagePanel(de.uib.configed.Globals.createImage("images/waitingcircle.gif"));
		//waitingImage.setVisible(true);
		initComponents();
		
		logging.info(this, "depotProductDirectory " + depotProductDirectory);
		smbMounted = new File(depotProductDirectory).exists();
		panelMountShare.mount(smbMounted);
			
			
		evaluateWinProducts();
		
		
		buildPanel();
	}
	
	
	private void defineChoosers()
	{
		comboChooseDepot = new JComboBox();
		comboChooseDepot.setSize(de.uib.configed.Globals.textfieldDimension);
		
		comboChooseDepot.setModel(new DefaultComboBoxModel(main.getLinkedDepots()));
		
		
		comboChooseDepot.setEnabled(false); 
		
		comboChooseDepot.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					selectedDepot = "" + comboChooseDepot.getSelectedItem();
					logging.info(this, "actionPerformed  depot selected " + selectedDepot);
					
					//buildSambaTarget(selectedDepot);
					
				}
			}
		);
		
		
		comboChooseWinProduct = new JComboBox();
		comboChooseWinProduct.setSize(de.uib.configed.Globals.textfieldDimension);
		comboChooseWinProduct.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					winProduct = "" + comboChooseWinProduct.getSelectedItem();
					logging.info(this, "winProduct  "  + winProduct);
					produceTarget();
				}
			}
		);
		
		
		chooserDriverPath = new JFileChooser();
		chooserDriverPath.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooserDriverPath.setPreferredSize( de.uib.utilities.Globals.filechooserSize );
		chooserDriverPath.setApproveButtonText( configed.getResourceValue("FileChooser.approve") );
		UIManager.put("FileChooser.cancelButtonText", configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserDriverPath);
		//chooserDriverPath.setControlButtonsAreShown(false);
		
		chooserDriverPath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserDriverPath.setDialogTitle(de.uib.configed.Globals.APPNAME + " " +configed.getResourceValue("PanelDriverUpload.labelDriverToIntegrate"));
		
		
		
		chooserServerpath = new JFileChooser();
		chooserServerpath.setPreferredSize( de.uib.utilities.Globals.filechooserSize );
		chooserServerpath.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooserServerpath.setApproveButtonText( configed.getResourceValue("FileChooser.approve") );
		UIManager.put("FileChooser.cancelButtonText", configed.getResourceValue("FileChooser.cancel"));
		SwingUtilities.updateComponentTreeUI(chooserServerpath);
		//chooserServerpath.setControlButtonsAreShown(false);
		
		chooserServerpath.setDialogType(JFileChooser.OPEN_DIALOG);
		chooserServerpath.setDialogTitle(de.uib.configed.Globals.APPNAME + " " +configed.getResourceValue("InstallOpsiPackage.chooserServerPath"));
		
		
	}
	
	private void initComponents()
	{
		defineChoosers();
	}	
	
	private void evaluateWinProducts()
	{
		retrieveWinProducts();
		
		winProduct = "" + comboChooseWinProduct.getSelectedItem();
		produceTarget();
	}
	
	private void retrieveWinProducts()
	{
		logging.info(this, "retrieveWinProducts in " + depotProductDirectory);
		
		if (depotProductDirectory == null)
			return;
		
		//not yet a depot selected
		
		smbMounted = new File(depotProductDirectory).exists();
		
		logging.info(this, "retrieveWinProducts smbMounted " + smbMounted);
		
		Vector<String> winProducts = persist.getWinProducts(server,depotProductDirectory);
		
		comboChooseWinProduct.setModel(new DefaultComboBoxModel(winProducts));
	}
	
	
		
	protected void buildPanel()
	{
		//setLayout(new BorderLayout());

		fieldByAuditPath = new JTextShowField();
		
		
		fieldClientname = new JTextShowField();

		JLabel label_DepotServer = new JLabel(configed.getResourceValue("PanelDriverUpload.DepotServer"));
		JLabel label_winProduct = new JLabel(configed.getResourceValue("PanelDriverUpload.labelWinProduct"));
		
		
		buttonCallSelectDriverFiles = new JButton( "", de.uib.configed.Globals.createImageIcon("images/folder_16.png", "" ));
		buttonCallSelectDriverFiles.setSelectedIcon( de.uib.configed.Globals.createImageIcon("images/folder_16.png", "" ) );
		buttonCallSelectDriverFiles.setPreferredSize(de.uib.configed.Globals.graphicButtonDimension);
		buttonCallSelectDriverFiles.setToolTipText(configed.getResourceValue("PanelDriverUpload.hintDriverToIntegrate")) ;
		
		fieldServerPath = new JTextShowField( true ); //opsiWorkBenchDirectoryS );
		fieldServerPath.getDocument().addDocumentListener(new FileNameDocumentListener());
		
		fieldServerPath.setForeground(de.uib.configed.Globals.greyed);
		
		JLabel labelMakeServerpath = new JLabel(configed.getResourceValue("PanelDriverUpload.labelMakeDir"));
		
		buttonCallChooserServerpath = new JButton( "", de.uib.configed.Globals.createImageIcon("images/folder_16.png", "" ));
		buttonCallChooserServerpath.setSelectedIcon( de.uib.configed.Globals.createImageIcon("images/folder_16.png", "" ) );
		buttonCallChooserServerpath.setPreferredSize(de.uib.configed.Globals.graphicButtonDimension);
		buttonCallChooserServerpath.setToolTipText(configed.getResourceValue("PanelDriverUpload.determineServerPath")) ;
			
		
		buttonCallChooserServerpath.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					chooseServerpath();
				}
			}
		);
		
		JLabel label_showDrivers = new JLabel(configed.getResourceValue("PanelDriverUpload.labelShowDrivers"));
		btnShowDrivers = new JButton("", de.uib.configed.Globals.createImageIcon("images/show-menu.png", "" ));
		btnShowDrivers.setToolTipText(configed.getResourceValue("PanelDriverUpload.btnShowDrivers.tooltip"));
		btnShowDrivers.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new Thread(){
					public void run()
					{
						new SSHConnectExec(main, 
							//  Empty_Command(String id, String c, String mt, boolean ns)
							((SSHCommand)new Empty_Command(
								"show_drivers.py", // id not needed
								"/var/lib/opsi/depot/" + comboChooseWinProduct.getSelectedItem() + "/show_drivers.py " + fieldClientname.getText(),
								"show_drivers.py", // menuText - not needed
								false //needSudo?
								)
							) 
						);
					}
				}.start();
			}
		});

		JLabel label_createDrivers = new JLabel(configed.getResourceValue("PanelDriverUpload.labelCreateDriverLinks"));
		btnCreateDrivers = new JButton("", de.uib.configed.Globals.createImageIcon("images/run-build-file.png", "" ));
		btnCreateDrivers.setToolTipText(configed.getResourceValue("PanelDriverUpload.btnCreateDrivers.tooltip"));
		btnCreateDrivers.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				new SSHConnectExec(main, 
					//  Empty_Command(String id, String c, String mt, boolean ns)
					((SSHCommand)new Empty_Command(
						"create_driver_links.py",// id not needed
						"/var/lib/opsi/depot/" + comboChooseWinProduct.getSelectedItem() + "/create_driver_links.py " ,
						"create_driver_links.py", // menutext - not needed
						true // need sudo ?
						)
					)
				);
			}
		});
		
		
		JLabel labelTargetPath = new JLabel(configed.getResourceValue("CompleteWinProducts.labelTargetPath"));
		fieldServerPath = new JTextShowField( true );
		fieldServerPath.getDocument().addDocumentListener(new FileNameDocumentListener());
		
		fieldDriverPath = new JTextShowField( true );
		fieldDriverPath.getDocument().addDocumentListener(new FileNameDocumentListener());
		//fieldDriverPath.setToolTipText(configed.getResourceValue("PanelDriverUpload.hintDriverToIntegrate"));
		
		final JPanel thisPanel = this;
		
		buttonCallSelectDriverFiles.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					int returnVal = chooserDriverPath.showOpenDialog( thisPanel );
					
					if (returnVal == JFileChooser.APPROVE_OPTION)
					{
						String pathInstallFiles = chooserDriverPath.getSelectedFile().getPath();
						fieldDriverPath.setText(pathInstallFiles);
						fieldDriverPath.setCaretPosition(pathInstallFiles.length());
					}
					else
					{
						fieldDriverPath.setText("");
					}
				}
			}
		);
		

		JLabel label_byAuditDriverLocationPath = new JLabel(configed.getResourceValue("PanelDriverUpload.byAuditDriverLocationPath"));
		JLabel labelDriverLocationType = new JLabel(configed.getResourceValue("PanelDriverUpload.type"));
		
		radioButtons = new Vector<RadioButtonIntegrationType>();
		
		buttonStandard  = new RadioButtonIntegrationType(
			configed.getResourceValue("PanelDriverUpload.type.standard"),
			FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS));
		buttonPreferred  = new RadioButtonIntegrationType(configed.getResourceValue("PanelDriverUpload.type.preferred"),
			FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS_PREFERRED));
		buttonNotPreferred  = new RadioButtonIntegrationType(configed.getResourceValue("PanelDriverUpload.type.excluded"),
			FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS_EXCLUDED));
		buttonAdditional  = new RadioButtonIntegrationType(configed.getResourceValue("PanelDriverUpload.type.additional"),
			FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS_ADDITIONAL));
		buttonByAudit  = new RadioButtonIntegrationType(configed.getResourceValue("PanelDriverUpload.type.byAudit"),
			FileX.getLocalsystemPath(SmbConnect.DIRECTORY_DRIVERS_BY_AUDIT));
		
		radioButtons.add(buttonStandard);
		radioButtons.add(buttonPreferred);
		radioButtons.add(buttonNotPreferred);
		radioButtons.add(buttonAdditional);
		radioButtons.add(buttonByAudit);
		
		ButtonGroup buttonGroup = new ButtonGroup();
		
		for (final RadioButtonIntegrationType button : radioButtons)
		{
			buttonGroup.add(button);
			button.addItemListener(new ItemListener()
				{
					public void itemStateChanged(ItemEvent e)
					{
						if (e.getStateChange() == ItemEvent.SELECTED)
						{
							logging.debug(this, " " + e);
							driverDirectory =  button.getSubdir();
							
							produceTarget();
						}
					}
				}
			);
		}
		
		buttonByAudit.setSelected(true);
		
		JPanel panelButtonGroup = new JPanel();
		GroupLayout layoutButtonGroup = new GroupLayout(panelButtonGroup);
		panelButtonGroup.setLayout(layoutButtonGroup);
		panelButtonGroup.setBorder(new javax.swing.border.LineBorder(de.uib.configed.Globals.blueGrey, 1, true));
		
		layoutButtonGroup.setVerticalGroup(layoutButtonGroup.createSequentialGroup()
			.addGap(vGap, vGap, vGap)
			.addComponent(labelDriverLocationType, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			.addGap(vGap, vGap, vGap)
			.addComponent(buttonStandard, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			.addComponent(buttonPreferred, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			.addComponent(buttonNotPreferred, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			.addComponent(buttonAdditional, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			.addComponent(buttonByAudit, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			.addGroup(layoutButtonGroup.createParallelGroup()
				.addComponent(label_byAuditDriverLocationPath, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
				.addComponent(fieldByAuditPath, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
			)
			.addGap(vGap, vGap, vGap)
		);
		
		layoutButtonGroup.setHorizontalGroup(layoutButtonGroup.createSequentialGroup()
			.addGap(hGap, hGap, hGap)
			.addGroup(layoutButtonGroup.createParallelGroup()
				.addComponent(labelDriverLocationType, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonStandard, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonPreferred, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonNotPreferred, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonAdditional, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(buttonByAudit, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGroup(layoutButtonGroup.createSequentialGroup()
					.addGap(50, 50, 50)
					.addComponent(label_byAuditDriverLocationPath, 10, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(hGap, hGap, hGap)
					.addComponent(fieldByAuditPath, de.uib.configed.Globals.buttonWidth,  de.uib.configed.Globals.buttonWidth*2, Short.MAX_VALUE)
					.addGap(hGap, hGap, hGap)
				)
			)
			.addGap(hGap, hGap, hGap)
				
		);
		
			
				
		driverPathChecked = new CheckedLabel(configed.getResourceValue("PanelDriverUpload.driverpathConnected"),
			de.uib.configed.Globals.createImageIcon("images/checked_withoutbox.png", ""),
			de.uib.configed.Globals.createImageIcon("images/checked_empty_withoutbox.png", ""),
			stateDriverPath);
		
		
		serverPathChecked = new CheckedLabel(configed.getResourceValue("PanelDriverUpload.targetdirConnected"),
			de.uib.configed.Globals.createImageIcon("images/checked_withoutbox.png", "Z"), 
			de.uib.configed.Globals.createImageIcon("images/checked_empty_withoutbox.png", ""),
			true);
		//serverPathChecked.setEnabled(false);
		
		buttonUploadDrivers = new JButton("",  de.uib.configed.Globals.createImageIcon("images/upload2product.png", "" ));
		buttonUploadDrivers .setEnabled(false);
		buttonUploadDrivers.setSelectedIcon( de.uib.configed.Globals.createImageIcon("images/upload2product.png", "" ) );
		//buttonUploadDrivers.setDisabledIcon( de.uib.configed.Globals.createImageIcon("images/waitingcircle.gif", "") );
		
		
		buttonUploadDrivers.setEnabled(false);
		
		
		buttonUploadDrivers.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					logging.info(this, "actionPerformed on buttonUploadDrivers from " 
								+ fieldDriverPath.getText()  + " to " + fieldServerPath.getText()); 
					final Color saveColor = buttonUploadDrivers.getBackground();
					//final Icon saveIcon = buttonUploadDrivers.getIcon();
					buttonUploadDrivers.setBackground(de.uib.configed.Globals.failedBackColor);
					execute();
					buttonUploadDrivers.setBackground(saveColor);
				}
			}
		);
		
		
		
		
		//JPanel panelByAuditInfo = new PanelLinedComponents(compis);
		//JPanel panelByAuditInfo= new JPanel();
		
		
		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);
		int lh = de.uib.utilities.Globals.lineHeight -4;
		layoutByAuditInfo.setVerticalGroup(
			layoutByAuditInfo.createSequentialGroup()
				.addGap(vGap, vGap*2, vGap*2)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(label_topic, lh, lh, lh)
					.addComponent(fieldClientname,lh, lh, lh)
				)
				.addGap(2*vGap, 3*vGap, 3*vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(label_DepotServer, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(comboChooseDepot, de.uib.utilities.Globals.lineHeight,   de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(label_winProduct, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(comboChooseWinProduct, de.uib.utilities.Globals.lineHeight,   de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
				)
				.addGap(2*vGap, 3*vGap, 3*vGap)
				.addGroup( layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE) 
					.addComponent(label_showDrivers, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(btnShowDrivers, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					)
				.addGap(2*vGap, 3*vGap, 3*vGap)
				.addGroup( layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE) 
					.addComponent(label_createDrivers, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(btnCreateDrivers, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					)
				.addGap(2*vGap, 3*vGap, 3*vGap)
				.addGroup( layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.BASELINE) 
					.addComponent(label_driverToIntegrate, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(buttonCallSelectDriverFiles, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(fieldDriverPath, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					)
				.addGap(2*vGap, 3*vGap, 3*vGap)
				.addComponent(panelButtonGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addGap(2*vGap, 3*vGap, 3*vGap)
				.addComponent(panelMountShare, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
				.addGap(de.uib.configed.Globals.vGapSize/2, de.uib.configed.Globals.vGapSize/2, de.uib.configed.Globals.vGapSize/2)
				.addGroup( layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER) 
					.addComponent(labelTargetPath,  de.uib.utilities.Globals.lineHeight,  de.uib.utilities.Globals.lineHeight,  de.uib.utilities.Globals.lineHeight)
					.addComponent(buttonCallChooserServerpath, de.uib.utilities.Globals.lineHeight,  de.uib.utilities.Globals.lineHeight,  de.uib.utilities.Globals.lineHeight)
					.addComponent(fieldServerPath,  de.uib.utilities.Globals.lineHeight,  de.uib.utilities.Globals.lineHeight,  de.uib.utilities.Globals.lineHeight)
					)
				.addGap(2*vGap, 3*vGap, 3*vGap)
				.addGroup( layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addComponent(driverPathChecked, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(serverPathChecked, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addComponent(buttonUploadDrivers, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
				)
				
				.addGap(vGap, vGap*2, vGap*2)
				.addGroup( layoutByAuditInfo.createParallelGroup(GroupLayout.Alignment.CENTER)
					//.addComponent(label_uploading, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
					.addGap(de.uib.utilities.Globals.lineHeight)
					//.addComponent(waitingImage,  de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight, de.uib.utilities.Globals.lineHeight)
				)
				.addGap(vGap, vGap*2, vGap*2)
			)
		;
		
		
		layoutByAuditInfo.setHorizontalGroup(
			layoutByAuditInfo.createParallelGroup()
				.addGroup(layoutByAuditInfo.createSequentialGroup()
					.addGap(hFirstGap, hFirstGap, hFirstGap)
					.addGroup(layoutByAuditInfo.createParallelGroup()
						.addGroup(layoutByAuditInfo.createSequentialGroup()
							.addComponent(label_topic, 5, wLeftText, wLeftText)
							.addGap(hFirstGap, hFirstGap, hFirstGap)
							//.addGap(de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth,  de.uib.configed.Globals.graphicButtonWidth)
							//.addGap(hFirstGap, hFirstGap, hFirstGap)
							.addComponent(fieldClientname, de.uib.configed.Globals.buttonWidth ,  de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth*2)
						)
						.addGroup(layoutByAuditInfo.createSequentialGroup()
							.addComponent(panelMountShare, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						)
						.addGroup(layoutByAuditInfo.createSequentialGroup()
							.addComponent(label_DepotServer)
							.addGap(hGap, hGap, hGap)
							.addComponent(comboChooseDepot, de.uib.configed.Globals.buttonWidth ,  de.uib.configed.Globals.buttonWidth, de.uib.configed.Globals.buttonWidth*2)
							.addGap(hGap, hGap, hGap)
							.addComponent(label_winProduct, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(hGap, hGap, hGap)
							.addComponent(comboChooseWinProduct, de.uib.configed.Globals.buttonWidth ,  de.uib.configed.Globals.buttonWidth*2, de.uib.configed.Globals.buttonWidth*3)
						)
						.addGroup(layoutByAuditInfo.createSequentialGroup()
							.addComponent(label_showDrivers, de.uib.configed.Globals.buttonWidth,  de.uib.configed.Globals.buttonWidth*2, Short.MAX_VALUE)
							.addGap(hGap, hGap, hGap)
							.addComponent(btnShowDrivers, de.uib.configed.Globals.graphicButtonWidth ,  de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth)
						)
						.addGroup(layoutByAuditInfo.createSequentialGroup()
							.addComponent(label_createDrivers, de.uib.configed.Globals.buttonWidth,  de.uib.configed.Globals.buttonWidth*2, Short.MAX_VALUE)
							.addGap(hGap, hGap, hGap)
							.addComponent(btnCreateDrivers, de.uib.configed.Globals.graphicButtonWidth ,  de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth)
						)
						.addGroup( layoutByAuditInfo.createSequentialGroup() 
							.addComponent(label_driverToIntegrate, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(hGap, hGap, hGap)
							.addComponent(buttonCallSelectDriverFiles, de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth,  de.uib.configed.Globals.graphicButtonWidth)
							.addGap(hFirstGap, hFirstGap, hFirstGap)
							.addComponent(fieldDriverPath, de.uib.configed.Globals.buttonWidth,  de.uib.configed.Globals.buttonWidth*2, Short.MAX_VALUE) 
						)
						.addComponent(panelButtonGroup, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
						.addGroup(layoutByAuditInfo.createSequentialGroup()
							.addComponent(panelMountShare, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  Short.MAX_VALUE)
							)
						.addGroup(layoutByAuditInfo.createSequentialGroup()
							.addComponent(labelTargetPath, label_driverToIntegrate.getPreferredSize().width,  label_driverToIntegrate.getPreferredSize().width,  label_driverToIntegrate.getPreferredSize().width)
							.addGap(hGap, hGap, hGap)
							.addComponent(buttonCallChooserServerpath, de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth,  de.uib.configed.Globals.graphicButtonWidth)
							.addGap(hFirstGap, hFirstGap, hFirstGap)
							.addComponent(fieldServerPath, de.uib.configed.Globals.buttonWidth *2 ,  de.uib.configed.Globals.buttonWidth*2, Short.MAX_VALUE)
							)
						//.addComponent(buttonUploadDrivers, de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth,  de.uib.configed.Globals.graphicButtonWidth)
					)
					.addGap(hFirstGap, hFirstGap, hFirstGap)
				)
				.addGroup(layoutByAuditInfo.createSequentialGroup()
					.addGap(5, 5, Short.MAX_VALUE)
					//.addComponent(driverPathChecked, de.uib.configed.Globals.checkBoxWidth, de.uib.configed.Globals.checkBoxWidth,  de.uib.configed.Globals.checkBoxWidth)
					.addComponent(driverPathChecked, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					//.addGap(2,2,2)
					.addGap(hGap, 2* hGap, 2* hGap)
					//.addComponent(serverPathChecked, de.uib.configed.Globals.checkBoxWidth, de.uib.configed.Globals.checkBoxWidth,  de.uib.configed.Globals.checkBoxWidth)
					.addComponent(serverPathChecked, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					//.addGap(2,2,2)
					.addGap(hGap, 2* hGap, 2* hGap)
					.addComponent(buttonUploadDrivers, de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth,  de.uib.configed.Globals.graphicButtonWidth)
					.addGap(hFirstGap/2, hFirstGap/2, hFirstGap/2)
				)
				
				.addGroup(layoutByAuditInfo.createSequentialGroup()
				/*
					//.addGap(hGap, hGap, Short.MAX_VALUE)
					.addComponent(label_uploading,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					.addGap(hFirstGap, hFirstGap, hFirstGap)
					.addComponent(waitingImage, de.uib.configed.Globals.iconWidth,  de.uib.configed.Globals.iconWidth,  de.uib.configed.Globals.iconWidth)
					.addGap(hFirstGap, hFirstGap, hFirstGap)
				)
				*/
					.addGap(hGap, hGap, Short.MAX_VALUE)
					//.addComponent(label_uploading,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					//.addComponent(waitingImage, de.uib.configed.Globals.iconWidth,  de.uib.configed.Globals.iconWidth,  de.uib.configed.Globals.iconWidth)
					.addGap(hFirstGap/2, hFirstGap/2, hFirstGap/2)
				)
			)
		;
		
		
		setBackground(de.uib.utilities.Globals.backgroundLightGrey);
		
	}
	
	
	public void makePath(File path, boolean ask)
	{
		logging.info(this, "makePath for " + path);
		
		if (path != null && !path.exists())
		{
			int returnedOption = JOptionPane.NO_OPTION;
	
			returnedOption = JOptionPane.showOptionDialog( rootFrame,
							 configed.getResourceValue("PanelDriverUpload.makeFilePath.text"),
							 configed.getResourceValue("PanelDriverUpload.makeFilePath.title"),
							 JOptionPane.YES_NO_OPTION,
							 JOptionPane.QUESTION_MESSAGE,
							 null, null, null);
				
			if (returnedOption == JOptionPane.YES_OPTION)
			{
				path.mkdirs();
			}
		}
		
		logging.info(this, "makePath result " + path);
	}
			
	
	protected void execute()
	{	
		//waitingImage.setVisible(true);
		
		//	Thread.yield();
		
		final FLoadingWaiter waiter = new FLoadingWaiter(de.uib.configed.Globals.APPNAME,
			configed.getResourceValue("PanelDriverUpload.execute.running"));
		waiter.startWaiting();
		
		final WaitCursor waitCursor = new WaitCursor( rootFrame );
		
		
		//try{
	
		//	SwingUtilities.invokeLater(new Thread(){
		//			public void run()
		new Thread(){
			public void run()
			{
				try
				{
			
						
						logging.info(this, "copy  " + driverPath + " to " + targetPath);
						
						//Thread.currentThread().sleep(100);
				
						
						makePath(targetPath, true);
						
						stateServerPath = targetPath.exists();
						serverPathChecked.setSelected(stateServerPath);
						if (stateServerPath)
						{
							try{
								if (driverPath.isDirectory())
									FileUtils.copyDirectoryToDirectory(driverPath, targetPath);
								else 
									FileUtils.copyFileToDirectory(driverPath, targetPath);
							}
							catch(IOException iox)
							{
								waitCursor.stop();
								logging.error("copy error:\n" + iox, iox);
							}
						}
						else
							logging.info(this, "execute: targetPath does not exist");
						
						if (stateServerPath)
						{
							String driverDir = "/" + 
											SmbConnect.unixPath(SmbConnect.directoryProducts) +
											"/" + 
											winProduct +
											"/" +
											SmbConnect.unixPath(SmbConnect.DIRECTORY_DRIVERS);
							logging.info(this, "set rights for " + driverDir);
							persist.setRights(driverDir);
						}
						
						waitCursor.stop();
						
						if (waiter != null)
							waiter.setReady();
							
						/*
						JOptionPane.showMessageDialog( rootFrame,
													   "Ready", //resultMessage, 
													   configed.getResourceValue("CompleteWinProduct.reportTitle"),
													   JOptionPane.INFORMATION_MESSAGE);
						*/
						
					
				}
				catch(Exception ex)
				{
					waitCursor.stop();
					logging.error("error in uploading :\n" + ex, ex);
				}
				//waitingImage.setVisible(false);
			}
		}.start();
			
		
		//waitCursor = null;
	}
	
	public void setByAuditPath(String s)
	{
		byAuditPath = s;
		fieldByAuditPath.setText(s);
		produceTarget();
	}
	
	public void setClientName(String s)
	{
		fieldClientname.setText(s);
	}
	
	public void setDepot(String s)
	{
		comboChooseDepot.setModel(new DefaultComboBoxModel(new String[]{s}));
	}

	private boolean checkFiles()
	{
		boolean result = false;
		
		if (fieldServerPath != null && fieldDriverPath != null)
		{
			try 
			{
				targetPath = new File(fieldServerPath.getText());
				driverPath= new File(fieldDriverPath.getText());
				
				stateServerPath = targetPath.isDirectory();
				serverPathChecked.setSelected(stateServerPath);
				logging.info(this, "checkFiles  stateServerPath targetPath " + targetPath);
				logging.info(this, "checkFiles  stateServerPath driverPath " + driverPath);
				logging.info(this, "checkFiles  stateServerPath isDirectory " + stateServerPath);
				
				stateDriverPath = driverPath.exists();
				driverPathChecked.setSelected(stateDriverPath);
				logging.info(this, "checkFiles stateDriverPath " + stateDriverPath);
				
				if ( stateServerPath && stateDriverPath )
				{
					result = true;
				}
			}
			catch(Exception ex)
			{
				logging.info(this, "checkFiles " + ex);
			}
			
		}
		
		logging.info(this, "checkFiles " + result);
		
		if (buttonUploadDrivers != null)
		{
			buttonUploadDrivers.setEnabled(result);
			
			if (result)
				buttonUploadDrivers.setToolTipText(configed.getResourceValue("PanelDriverUpload.execute"));
			else
				buttonUploadDrivers.setToolTipText("Treiber- bzw. Zielpfad noch nicht gefunden");
		}
		
		return result;
	}
	
	
	private void produceTarget()
	{
		if (fieldServerPath == null)
			return; //caution we are not yet initialized
			
		String result = depotProductDirectory + File.separator + winProduct + File.separator 
								+ driverDirectory;
								
		if (buttonByAudit.isSelected())
			result = result + File.separator + byAuditPath;
							
		fieldServerPath.setText(result);
		
		/*
			if (buttonCallExecute != null)
				buttonCallExecute.setEnabled(
					new File( fieldTargetPath.getText()).isDirectory()
				);
		*/	
	}
	
	
	private void chooseServerpath()
	{
		
		String oldServerPath = fieldServerPath.getText();
		File currentDirectory = new File(oldServerPath);
		
		makePath(currentDirectory, true);
		chooserServerpath.setCurrentDirectory(currentDirectory);
		
		int returnVal = chooserServerpath.showOpenDialog(this);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			String serverPathGot = chooserServerpath.getSelectedFile().getPath();
			fieldServerPath.setText( serverPathGot );
			fieldServerPath.setCaretPosition( serverPathGot.length() );
		}
		
	}
	
	
	// =======
	//implements NameProducer
	public String produceName()
	{
		
		if (fieldServerPath != null) logging.info(this, "produceName ? fieldServerPath , depotProductDirectory " + fieldServerPath.getText() + " , " + depotProductDirectory);
		if (fieldServerPath == null || fieldServerPath.getText().equals("") ||fieldServerPath.getText().startsWith(depotProductDirectory))
			return depotProductDirectory;
		
		return fieldServerPath.getText();
	}
	
	public String getDefaultName()
	{
		return byAuditPath;
		//return de.uib.connectx.SmbConnect.PRODUCT_SHARE_RW;
	}
	
	
}


