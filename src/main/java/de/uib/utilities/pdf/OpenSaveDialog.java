package de.uib.utilities.pdf;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import de.uib.configed.gui.FShowListWithComboSelect;
import de.uib.utilities.logging.logging;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class OpenSaveDialog implements ActionListener
{
 
   JButton openBtn;
   JButton saveBtn;
   Boolean saveAction;
   de.uib.configed.gui.GeneralFrame dialogView;

   public OpenSaveDialog (){
	   this("PDF erzeugen");
   }
   
   public OpenSaveDialog(String title)
   {
	   
	   saveBtn = new JButton(configed.getResourceValue("OpenSaveDialog.save"));
	   saveBtn.setFont (Globals.defaultFont);
	   //saveBtn.setToolTipText(configed.getResourceValue("OpenSaveDialog.save.Tooltipp"));
	   saveBtn.addActionListener(this); 
	   
	   openBtn = new JButton(configed.getResourceValue("OpenSaveDialog.open"));
	   //openBtn.setToolTipText(configed.getResourceValue("OpenSaveDialog.open.ToolTip"));
	   openBtn.setFont (Globals.defaultFont);
	   openBtn.addActionListener(this);
		
	   JPanel buttonPane = new JPanel();
	   buttonPane.add(saveBtn);
	   buttonPane.add(openBtn);
	   JLabel text = new JLabel(configed.getResourceValue("OpenSaveDialog.jLabel_text"));
	   JPanel qPanel = new JPanel();
	   //qPanel.add(text);
	   qPanel.add(buttonPane);
	   dialogView = new de.uib.configed.gui.GeneralFrame(null, de.uib.configed.Globals.APPNAME + " " + title, true); // modal
	   dialogView.addPanel(qPanel);
	   dialogView.setSize(new Dimension(400, 90));
	   dialogView.centerOn(de.uib.utilities.Globals.masterFrame);
	   dialogView.setVisible(true);
      
   }
   public Boolean getSaveAction () {
	   return this.saveAction;
   }
   public void setVisible () {
	   dialogView.setVisible (true);
   }
   private void leave ()
   {
		dialogView.setVisible (false);
		dialogView.dispose ();
   }
   
   @Override
   public void actionPerformed(ActionEvent e) {
		if (e.getSource() == openBtn)
		{ 
			saveAction = false;
			leave();
			
		}
		else if (e.getSource() == saveBtn)
		{ 
			saveAction = true;
			leave();
		}
   }
   
   public static void main(String[] args)
   {
	 
      Runnable r = new Runnable()
                   {
                      @Override
                      public void run()
                      {
                         new OpenSaveDialog("Open or Save PDF");
                      }
                   };
      EventQueue.invokeLater(r);
	   
   }

}

