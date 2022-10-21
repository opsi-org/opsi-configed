/*
 * FEditObject.java
 *
 * Created 28.6.2010
 */

package de.uib.utilities.swing;
/**
 *
 * @author roeder
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import de.uib.configed.Globals;
import de.uib.utilities.logging.*;
import de.uib.utilities.observer.*;


public class FEditObject extends javax.swing.JDialog
			implements ActionListener, KeyListener, DocumentListener
{
	public static RunningInstances<JDialog> runningInstances 
		= new RunningInstances(JDialog.class, "leaving dialog" );
	
	public Dimension areaDimension = new Dimension(300,240);

	protected Object initialValue = "";
	protected boolean leaveOnCommit = true;


	protected JPanel framingPanel;
	protected JPanel editingArea;
	protected JPanel loggingPanel;
	protected JScrollPane scrollpaneL;
	protected JSplitPane splitPane;
	private int splitPaneHMargin = 1;


	//private javax.swing.JButton cancelbutton;
	//private javax.swing.JButton buttonCommit;
	protected de.uib.configed.gui.IconButton buttonCommit;
	protected de.uib.configed.gui.IconButton buttonCancel;
	protected de.uib.configed.gui.IconButton buttonAdd;
	protected de.uib.configed.gui.IconButton buttonRemove;



	protected String tooltipCommit = de.uib.configed.configed.getResourceValue("FEditObject.SaveButtonTooltip");


	protected boolean dataChanged = false;
	protected boolean editable = true;
	protected boolean finished = false;
	protected int buttonWidth = 30;

	protected JTextField extraField;
	protected JLabel extraLabel;
	protected JTextArea loggingArea;

	public FEditObject(Object initialValue)
	{
		//runningInstances.add(this, "");
		setIconImage (Globals.mainIcon);
		//initComponents();
		if (initialValue != null)
			this.initialValue = initialValue;

		createComponents();
		// components initialized lazily in init()
	}

	protected void createComponents()
	{
		framingPanel = new JPanel();
		editingArea = new JPanel(new BorderLayout());
		loggingPanel = new JPanel(new BorderLayout());
		loggingPanel.setVisible(false);

		loggingArea = new JTextArea("");
		loggingArea.setEditable(false);
		//loggingArea.setPreferredSize(new Dimension(100, 50));

		loggingArea.setBackground(Globals.backgroundWhite);

		scrollpaneL = new javax.swing.JScrollPane();
		scrollpaneL.setViewportView(loggingArea);
		scrollpaneL.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		loggingPanel.add(scrollpaneL, BorderLayout.CENTER);

		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT );


		//editingArea.addFocusListener(this);

		buttonCommit = new de.uib.configed.gui.IconButton(
		                   de.uib.configed.configed.getResourceValue("FEditObject.SaveButtonTooltip") ,
		                   "images/apply.png", "images/apply_over.png", "images/apply_disabled.png",true);
		buttonCommit.setPreferredSize(new Dimension(buttonWidth, de.uib.utilities.Globals.buttonHeight));

		buttonCancel = new de.uib.configed.gui.IconButton(
		                   de.uib.configed.configed.getResourceValue("FEditObject.CancelButtonTooltip") ,
		                   "images/cancel.png", "images/cancel_over.png", "images/cancel_disabled.png",true);
		buttonCancel.setPreferredSize(new Dimension(buttonWidth, de.uib.utilities.Globals.buttonHeight));
		buttonCancel.setEnabled(true);

		buttonRemove = new de.uib.configed.gui.IconButton(
		                   de.uib.configed.configed.getResourceValue("FEditObject.RemoveButtonTooltip") ,
		                   "images/list-remove.png", "images/list-remove.png", "images/list-remove_disabled.png",true);
		buttonRemove.setPreferredSize(new Dimension(buttonWidth, de.uib.utilities.Globals.buttonHeight));
		buttonRemove.setVisible(false);

		buttonAdd = new de.uib.configed.gui.IconButton(
		                de.uib.configed.configed.getResourceValue("FEditObject.AddButtonTooltip") ,
		                "images/list-add.png", "images/list-add.png", "images/list-add_disabled.png",true);
		buttonAdd.setPreferredSize(new Dimension(buttonWidth, de.uib.utilities.Globals.buttonHeight));
		buttonAdd.setVisible(false);

		extraField = new JTextField("");
		extraField.setPreferredSize( new Dimension(de.uib.utilities.Globals.buttonWidth, de.uib.utilities.Globals.lineHeight) );
		extraField.setVisible(false);

		extraLabel = new JLabel("");
		extraLabel.setPreferredSize( new Dimension(de.uib.utilities.Globals.buttonWidth, de.uib.utilities.Globals.lineHeight) );
		extraLabel.setVisible(false);

	}


	public void setDividerLocation(double loc)
	{
		splitPane.setDividerLocation(loc);
	}

	protected void initComponents()
	{

		framingPanel.setBackground(Globals.backgroundWhite);
		setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);


		editingArea.addKeyListener(this);

		//okbutton = new javax.swing.JButton();
		//cancelbutton = new javax.swing.JButton();
		//okbutton.setText("ok");
		//cancelbutton.setText("cancel");

		buttonCommit.addActionListener(this);
		buttonCancel.addActionListener(this);

		buttonCommit.addKeyListener(this);
		buttonCancel.addKeyListener(this);

		buttonRemove.addActionListener(this);
		buttonRemove.addKeyListener(this);

		buttonAdd.addActionListener(this);
		buttonAdd.addKeyListener(this);

		extraField.getDocument().addDocumentListener(this);


		//scrollpane = new javax.swing.JScrollPane();

		//textarea = new javax.swing.JTextArea();


		//textarea.setColumns(20);
		//textarea.setRows(5);
		//scrollpane.setViewportView(textarea);





		javax.swing.GroupLayout layout1 = new javax.swing.GroupLayout(framingPanel);
		framingPanel.setLayout(layout1);
		layout1.setHorizontalGroup(
		    layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(layout1.createSequentialGroup()
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addGroup(layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		                        .addComponent(editingArea, 60, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		                        .addGroup(layout1.createSequentialGroup()
		                                  .addComponent(buttonCommit, 20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                                  .addComponent(buttonCancel, 20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                                  .addGap(Globals.vGapSize, 2 * Globals.vGapSize, 2 * Globals.vGapSize)
		                                  .addComponent(buttonRemove, 20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                                  .addComponent(buttonAdd, 20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                                  .addComponent(extraField, 20, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		                                  .addComponent(extraLabel, 20, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		                                 )
		                       )
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		             )
		);
		layout1.setVerticalGroup(
		    layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
		    .addGroup(layout1.createSequentialGroup()
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addComponent(editingArea, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		              .addGroup(layout1.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
		                        .addComponent(buttonCommit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                        .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                        .addComponent(buttonRemove, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                        .addComponent(buttonAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                        .addComponent(extraField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                        .addComponent(extraLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
		                       )
		              .addGap(Globals.vGapSize/2, Globals.vGapSize/2, Globals.vGapSize/2)
		             )
		);

		if (loggingPanel.isVisible())
		{
			splitPane.setTopComponent(framingPanel);
			splitPane.setBottomComponent(loggingPanel);

			add(splitPane);




			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
			this.setLayout(layout);
			layout.setHorizontalGroup(layout.createSequentialGroup()
			                          .addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
			                          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			                                    .addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			                                   )
			                          .addContainerGap()
			                          .addGap(splitPaneHMargin, splitPaneHMargin, splitPaneHMargin)
			                         );

			layout.setVerticalGroup(
			    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			    .addComponent(splitPane, 0, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			)
			;

		}

		else

		{


			javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
			getContentPane().setLayout(layout);
			layout.setHorizontalGroup(
			    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			    .addGroup(layout.createSequentialGroup()
			              .addContainerGap()
			              .addComponent(framingPanel, 100, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			              .addContainerGap())
			    .addGroup(layout.createSequentialGroup()
			              .addContainerGap()
			              .addComponent(loggingPanel, 100, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			              .addContainerGap())
			);
			layout.setVerticalGroup(
			    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
			    .addGroup(layout.createSequentialGroup()
			              .addContainerGap(20, 20)
			              .addComponent(framingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
			              .addComponent(loggingPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, Short.MAX_VALUE)
			              .addContainerGap(20, 20))
			);

		}


		pack();



	}

	public void setEditable(boolean b)
	{
		editable = b;
	}

	public void setModal(boolean b)
	{
		super.setModal(b);
		logging.debug(this, "setModal " + b);
		if (b)
			setAlwaysOnTop(true);
	}


	public void setDataChanged(boolean b)
	{
		logging.debug(this, "setDataChanged " + b);

		if (Globals.forbidEditingTargetSpecific() && b) return;

		dataChanged = b;
		buttonCommit.setEnabled(b);
		//buttonCancel.setEnabled(b);
	}

	public void setStartValue(Object s)
	{
		//System.out.println("FEditObject.setStartText(): " + s);
		initialValue = s;
	}

	public Object getValue()
	{
		//System.out.println("FEditObject.getText()");
		return initialValue;
	}

	public void setLeaveOnCommit(boolean b)
	{
		leaveOnCommit = b;
	}


	private int intHalf (double x)
	{
		return (int) (x/2);
	}

	public void locateLeftTo(Component master)
	{
		int startX = 0;
		int startY = 0;

		if (master == null)
		{
			//center on Screen

			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();


			startX = (screenSize.width  - getSize().width)/ 2;

			startY = (screenSize.height - getSize().height)/2;

		}
		else
		{
			Point masterOnScreen = new Point(50, 50);
							
			try
			{
				masterOnScreen = master.getLocationOnScreen();
			}
			catch(Exception ex)
			{
				logging.info(this, "not located master " + master  + " ex: " + ex);
			}
			
			logging.debug(this, "centerOn (int) masterOnScreen.getX()  " + (int) masterOnScreen.getX());
			logging.debug(this, "centerOn (int) masterOnScreen.getY()  " + (int) masterOnScreen.getY());
			logging.debug(this, "centerOn master.getWidth()  " +  master.getWidth() / 2);
			logging.debug(this, "centerOn master.getHeight()  " +  master.getHeight() / 2) ;
			logging.debug(this, "centerOn this.getSize() " + getSize());

			logging.debug(this, "centerOn " + master.getClass() + ", " + master);

			//startX = (int) masterOnScreen.getX() +  intHalf ( master.getWidth() )   -  intHalf( getSize().getWidth() );
			//startY = (int) masterOnScreen.getY() +  intHalf ( master.getHeight() )  -  intHalf( getSize().getHeight() );
			startX = (int) masterOnScreen.getX() - (int) (getSize().getWidth())  - Globals.minHGapSize;
			startY = (int) masterOnScreen.getY(); 

		
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			logging.debug(this, "centerOn screenSize " + screenSize);
			//logging.info(this, "startX + getSize().width " + (startX + getSize().width));
			//logging.info(this, "(startY + getSize().height " + (startY + getSize().height));

			/*
			if (startX + getSize().width > screenSize.width)
				startX = screenSize.width - getSize().width;

			if (startY + getSize().height > screenSize.height)
				startY = screenSize.height - getSize().height;
			*/

		}

		setLocation(startX, startY);
	}

	public boolean init(Dimension usableAreaSize)
	{
		if (editingArea.getComponentCount() != 1)
		{
			logging.error(" editing area not filled with component" );
			return false;
		}
		//System.out.println(" editingArea used by " + editingArea.getComponent(0).getClass());
		editingArea.getComponent(0).setPreferredSize(usableAreaSize);
		initComponents();
		initEditing();
		return true;
	}

	protected void initEditing()
	{
		logging.debug(this, "FEditObject.initEditing");
		setDataChanged(false);
		buttonAdd.setEnabled(false);
		buttonRemove.setEnabled(false);
		initExtraField();
	}

	public boolean init()
	{
		return init(areaDimension);
	}

	public boolean isFinished()
	{
		return finished;
	}

	protected void initExtraField()
	{
		extraField.setText("");
	}

	public void setExtraLabel(String s)
	{
		extraLabel.setVisible(true);
		extraLabel.setText(s);
	}

	public void enter()
	{
		logging.debug(this, "enter");
		//initEditing();
	}

	public void deactivate()
	{
		leave();
	}

	protected void leave()
	{
		//logging.debug(this, "FEditObject.leave()");

		setVisible (false);
		finished = true;
		//runningInstances.forget(this);
		//deactivate(); //no effect probably because of reentering the field
	}
	
	@Override
	public void setVisible(boolean b)
	{
		if (b)
			runningInstances.add(this, "");
		else
			runningInstances.forget(this);
		super.setVisible(b);
	}
		
			

	protected void processWindowEvent(WindowEvent e)
	{
		if (e.getID() == WindowEvent.WINDOW_CLOSING)
		{
			/*
			setStartValue(initialValue);
			leave();
			*/
			cancel();
			//System.out.println(" window closing, text " + getText());
		}
		else if (e.getID() == WindowEvent.WINDOW_ACTIVATED)
		{
			//System.out.println(" window activated");
			enter();
		}
		else if (e.getID() == WindowEvent.WINDOW_DEACTIVATED)
		{
			//System.out.println(" window deactivated");
			//deactivate();

		}

		super.processWindowEvent(e);
	}

	protected void commit()
	{
		logging.debug(this, "FEditObject.commit");

		if (Globals.forbidEditingTargetSpecific())
			cancel();
		else
		{
			setStartValue(getValue());
			if (leaveOnCommit) leave();
		}
	}

	protected void cancel()
	{
		//logging.debug(this, "cancel, resetting ---------- initialValue " + initialValue);
		setStartValue(initialValue);
		leave();
	}


	//-------------
	// interface ActionListener
	public void actionPerformed(java.awt.event.ActionEvent e)
	{
		logging.debug(this, "actionPerformed");
		//commit1();
		if (e.getSource() == buttonCommit)
		{
			commit();
		}
		else if (e.getSource() == buttonCancel)
		{
			//System.out.println (" -------- buttonCancel " + e);
			cancel();
		}
	}
	//-------------

	//-------------
	// interface KeyListener
	public void keyPressed (KeyEvent e)
	{
		//loggig.debug(this, " key event " + e);
		//if (e.getSource() == buttonCommit)
		if (e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			commit();
		}
		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			cancel();
		}
	}
	public void keyTyped (KeyEvent e)
	{
	}
	public void keyReleased (KeyEvent e)
	{
	}
	//-------------



	protected void extraFieldChanged(boolean b)
	{
		buttonAdd.setEnabled(b);
	}



	//-------------
	// interface DocumentListener
	public void changedUpdate(DocumentEvent e)
	{
		extraFieldChanged(true);
	}
	public void insertUpdate(DocumentEvent e)
	{
		extraFieldChanged(true);
	}
	public void removeUpdate(DocumentEvent e)
	{
		extraFieldChanged(true);
	}
	//-------------


}
