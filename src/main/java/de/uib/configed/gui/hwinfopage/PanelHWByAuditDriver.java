package de.uib.configed.gui.hwinfopage;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.*;

import de.uib.configed.*;
import de.uib.configed.tree.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.swing.*;


public class PanelHWByAuditDriver extends JPanel
{
	protected JLabel jLabelTitle;
	
	protected int hGap = de.uib.utilities.Globals.hGapSize/2;
	protected int vGap = de.uib.utilities.Globals.vGapSize/2;
	protected int hLabel = de.uib.utilities.Globals.buttonHeight;

	
	protected String byAuditPath;
	
	ButtonGroup selectionGroup;
	JRadioButton selectionCOMPUTER_SYSTEN;
	JRadioButton selectionBASE_BOARD;

	protected JTextField fieldVendor;
	protected JTextField fieldLabel;

	protected JTextField fieldVendor2;
	protected JTextField fieldLabel2;
	
	protected String title;
	
	JButton buttonUploadDrivers;
	
	FDriverUpload fDriverUpload;
	ConfigedMain main;

	public PanelHWByAuditDriver(String title, ConfigedMain main)
	{
		this.title = title;
		this.main = main;
		buildPanel();
	}
	
	protected void buildPanel()
	{
		jLabelTitle = new JLabel(title);
		jLabelTitle.setOpaque(true);
		
		fieldVendor = new JTextShowField();
		fieldVendor.setBackground(de.uib.utilities.Globals.backgroundLightGrey);
		fieldLabel = new JTextShowField();
		fieldLabel.setBackground(de.uib.utilities.Globals.backgroundLightGrey);

		fieldVendor2 = new JTextShowField();
		fieldVendor2.setBackground(de.uib.utilities.Globals.backgroundLightGrey);

		fieldLabel2 = new JTextShowField();
		fieldLabel2.setBackground(de.uib.utilities.Globals.backgroundLightGrey);


		JLabel labelInfo = new JLabel( configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabels") );
		JLabel labelInfo2 = new JLabel( configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabels2") );

		JLabel labelSeparator = new JLabel(" / ");
		JLabel labelSeparator2 = new JLabel(" / ");
		JLabel labelVendor = new JLabel( configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsVendor") );
		JLabel labelProduct = new JLabel( configed.getResourceValue("PanelHWInfo.byAuditDriverLocationLabelsProduct") );

		
		buttonUploadDrivers = new JButton("",  de.uib.configed.Globals.createImageIcon("images/upload2product.png", "" ));
		buttonUploadDrivers.setSelectedIcon( de.uib.configed.Globals.createImageIcon("images/upload2product.png", "" ) );
		buttonUploadDrivers.setToolTipText(configed.getResourceValue("PanelHWInfo.uploadDrivers"));
		
		buttonUploadDrivers.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e)
				{
					startDriverUploadFrame();
				}
			}
		);

		selectionCOMPUTER_SYSTEN = new JRadioButton("",true);
		selectionBASE_BOARD = new JRadioButton("");
		selectionGroup = new ButtonGroup();
		selectionGroup.add(selectionCOMPUTER_SYSTEN);
		selectionGroup.add(selectionBASE_BOARD);
		
		//JPanel panelByAuditInfo = new PanelLinedComponents(compis);
		//JPanel panelByAuditInfo= new JPanel();
		GroupLayout layoutByAuditInfo = new GroupLayout(this);
		this.setLayout(layoutByAuditInfo);
		int lh = de.uib.utilities.Globals.lineHeight -4;
		layoutByAuditInfo.setVerticalGroup(
			layoutByAuditInfo.createSequentialGroup()
				.addGap(vGap, vGap, vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
					.addComponent(labelInfo, lh, lh, lh)
					.addComponent(labelVendor, lh, lh, lh)
					.addComponent(labelProduct, lh, lh, lh)
					.addGap(hGap, hGap, hGap)
					.addComponent(buttonUploadDrivers, lh, lh, lh)
				)
				// .addGap(vGap, vGap, vGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
					.addGroup(layoutByAuditInfo.createSequentialGroup()
						.addGap(hGap/2+1, hGap/2+1, hGap/2+1)
						.addComponent(selectionCOMPUTER_SYSTEN)
					)
					.addComponent(fieldVendor, lh, lh, lh)
					.addComponent(labelSeparator, lh, lh, lh)
					.addComponent(fieldLabel, lh, lh, lh)
					
				)
				.addGap(vGap/2, vGap/2, vGap/2)
				.addGroup(layoutByAuditInfo.createParallelGroup()

					.addGroup(layoutByAuditInfo.createSequentialGroup()
						.addGap(hGap/2+1, hGap/2+1, hGap/2+1)
						.addComponent(selectionBASE_BOARD)
					)
					.addComponent(fieldVendor2, lh, lh, lh)
					.addComponent(labelSeparator2, lh, lh, lh)
					.addComponent(fieldLabel2, lh, lh, lh)
					
				)
				.addGap(vGap, vGap, vGap)
			)
		;
		
		layoutByAuditInfo.setHorizontalGroup(
			layoutByAuditInfo.createSequentialGroup()
				// .addGap(hGap, hGap, hGap)
				.addGroup(layoutByAuditInfo.createSequentialGroup()
					.addGap(hGap*2, hGap*2, hGap*2)
					.addComponent(labelInfo, 5, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				)
				.addGap(2, hGap*4, hGap*4)
				.addGroup(layoutByAuditInfo.createParallelGroup()
					.addComponent(selectionCOMPUTER_SYSTEN, 2, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
					.addComponent(selectionBASE_BOARD, 2, GroupLayout.PREFERRED_SIZE,  GroupLayout.PREFERRED_SIZE)
				)
				.addGap(hGap, hGap, hGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
					.addGroup(layoutByAuditInfo.createSequentialGroup()
						.addGap(2,2,2)
						.addComponent(labelVendor, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth,  de.uib.utilities.Globals.buttonWidth*2)
					)
					.addComponent(fieldVendor, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth,  de.uib.utilities.Globals.buttonWidth*2)
					.addComponent(fieldVendor2, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth,  de.uib.utilities.Globals.buttonWidth*2)
				)
				.addGap(hGap, hGap, hGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
					.addComponent(labelSeparator)
					.addComponent(labelSeparator2)
				)
				.addGap(hGap, hGap, hGap)
				.addGroup(layoutByAuditInfo.createParallelGroup()
					.addGroup(layoutByAuditInfo.createSequentialGroup()
						.addGap(2,2,2)
						.addComponent(labelProduct, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth,  de.uib.utilities.Globals.buttonWidth*2)
					)
					.addComponent(fieldLabel, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth, de.uib.utilities.Globals.buttonWidth*2)
					.addComponent(fieldLabel2, de.uib.utilities.Globals.buttonWidth/2, de.uib.utilities.Globals.buttonWidth, de.uib.utilities.Globals.buttonWidth*2)
				)
				
				.addGap(5*hGap, 10*hGap, 10*hGap)
				.addComponent(buttonUploadDrivers, de.uib.configed.Globals.graphicButtonWidth, de.uib.configed.Globals.graphicButtonWidth,  de.uib.configed.Globals.graphicButtonWidth)
				.addGap(2*hGap, 4* hGap, Short.MAX_VALUE)
			)
		;
		setBackground(de.uib.configed.Globals.backLightBlue);
		setBorder(BorderFactory.createLineBorder(de.uib.utilities.Globals.greyed));
	}

	public void setTitle(String s)
	{
		title = s;
	}
	
	public void emptyByAuditStrings()
	{
		byAuditPath = "";
		fieldVendor.setText("");
		fieldLabel.setText("");
		fieldVendor2.setText("");
		fieldLabel2.setText("");
		// selectionGroup.clearSelection();

		//fieldByAuditPath.setText("");
		if (fDriverUpload != null)
			fDriverUpload.setUploadParameters("");
	}
	
	
	private String eliminateIllegalPathChars(String path)
	{
		final String toReplace = "<>?\":|\\/*";
		final char replacement = '_';
		
		if (path == null)
			return null;
		
		char[] chars = path.toCharArray();
		for (int i = 0; i < chars.length; i++)
		{
			if (toReplace.indexOf(chars[i]) > -1)
				chars[i] = replacement;
		}
		
		
		//requires bootimage >= 4.0.6
		if (
			chars.length > 0 
			&& 
			(chars[chars.length-1] == '.'  || chars[chars.length-1] == ' ')
		)
		{
			chars[chars.length-1] = replacement;
		}
		
		
		
		return new String(chars);
	}
	
	public void setByAuditFields(
		String vendorStringCOMPUTER_SYSTEM,
		String vendorStringBASE_BOARD,
		String modelString,
		String productString)
	{
		fieldVendor.setText(vendorStringCOMPUTER_SYSTEM);
		fieldLabel.setText(modelString);

		fieldVendor2.setText(vendorStringBASE_BOARD);
		fieldLabel2.setText(productString);
		
		if (fDriverUpload != null)
			fDriverUpload.setUploadParameters(byAuditPath);
		// fieldByAuditPath.setText(byAuditPath);
	}
	
	
	private void startDriverUploadFrame()
	{
		if (selectionBASE_BOARD.isSelected())
		{
			byAuditPath = eliminateIllegalPathChars(fieldVendor2.getText()) 
				+ "/" + eliminateIllegalPathChars(fieldLabel2.getText());
		}
		else // if (selectionCOMPUTER_SYSTEN.isSelected())
		{
			byAuditPath = eliminateIllegalPathChars(fieldVendor.getText())
				+ "/" + eliminateIllegalPathChars(fieldLabel.getText());
		}

		if (fDriverUpload == null)
		{
			fDriverUpload = new FDriverUpload(main, main.getPersistenceController(), null);
		}
		
		fDriverUpload.setSize(de.uib.configed.Globals.helperFormDimension);
		fDriverUpload.setVisible(true);
		fDriverUpload.centerOnParent();
		
		fDriverUpload.setUploadParameters(byAuditPath);
	}
}