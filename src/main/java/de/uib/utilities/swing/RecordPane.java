package de.uib.utilities.swing;

/*
* RecordPane.java
* 
* (c) uib 2012
* GPL-licensed
* author Rupert Röder
*
*
*/


import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import de.uib.configed.Globals;
import de.uib.utilities.logging.*;
import de.uib.utilities.observer.*;
import de.uib.utilities.observer.swing.*;



public class RecordPane extends JPanel
	implements KeyListener
{
	protected int lineHeight = Globals.lineHeight;
	protected int vGapSize = Globals.vGapSize;
	protected int hGapSize = Globals.hGapSize;
	protected int minFieldWidth = 60;
	protected int maxFieldWidth = Short.MAX_VALUE;
	protected int minLabelWidth = 30;
	protected int maxLabelWidth = 100;
	
	
	protected class TheObservableSubject extends ObservableSubject
	{
		@Override
		public void notifyObservers()
		{
			//logging.debug(this, "notifyObservers ");
			System.out.println("RecordPane: notifyObservers ");
		}
	}
	
	protected ObservableSubject editingNotifier;
	
	
	//GUI
	protected Map<String, JLabel> labelfields;
	protected Map<String, JTextFieldObserved> datafields;
	
	//Data
	protected LinkedHashMap<String, String> data;
	protected Map<String, String> labels;
	protected Map<String, String> hints;
	protected Map<String, Boolean> editable;
	//protected Component parent;
	
	public RecordPane()
	{
		//call of setData necessary
	}
	
	public RecordPane(
		LinkedHashMap<String, String> data, 
		Map<String, String> labels, 
		Map<String, String> hints,
		Map<String, Boolean> editable
	)
	{
		init(
			data, 
			labels, 
			hints,
			editable
		);
	}
	
	
	
	public void setObservableSubject(ObservableSubject editingNotifier)
	{
		if (editingNotifier == null)
		{
			this.editingNotifier = new TheObservableSubject();
		}
		else
			this.editingNotifier = editingNotifier;
		
		for (String key : datafields.keySet())
		{
			datafields.get(key).setGlobalObservableSubject(this.editingNotifier);
		}
	}
			
	
	public void setData(
		LinkedHashMap<String, String> data, 
		Map<String, String> labels, 
		Map<String, String> hints,
		Map<String, Boolean> editable
	)
	{
		init(
			data, 
			labels, 
			hints,
			editable
		);
	}
		
	
	protected void init(
		LinkedHashMap<String, String> data, 
		Map<String, String> labels, 
		Map<String, String> hints,
		Map<String, Boolean> editable
	)
	{
		this.data = data;
		this.labels = labels;
		this.editable = editable;
		this.hints = hints;
		
		initComponents();
		
	}
	
	
	protected void initComponents() 
	{
		
		setBackground(Globals.backgroundWhite);
		
		labelfields = new HashMap<String, JLabel>();
		datafields = new HashMap<String, JTextFieldObserved>();
		
		
		javax.swing.GroupLayout baseLayout = new javax.swing.GroupLayout(this);
		this.setLayout(baseLayout);
		
		if (data == null)
			return;
		
		for (String key : data.keySet())
		{
			JLabel jLabel = new JLabel();
			
			if (labels == null || labels.get(key) == null)
				jLabel.setText("");
			else
				jLabel.setText(labels.get(key));
			
			jLabel.setFont(Globals.defaultFontBig);
			labelfields.put(key, jLabel);
			
			JTextFieldObserved jTextField = new JTextFieldObserved();
			if (data.get(key) != null)
				jTextField.setText("" + data.get(key));
			else
				jTextField.setText("");
			
			jTextField.setFont(Globals.defaultFontBig);
			jTextField.getCaret().setBlinkRate(0);
			
			if (hints != null)
				jTextField.setToolTipText(hints.get(key));
			
			if (editable != null && editable.get(key) != null)
			{
				jTextField.setEditable(editable.get(key));
				jTextField.setEnabled(editable.get(key));
			}
			else
			{
				jTextField.setEditable(false);
				jTextField.setEnabled(false);
			}
			
			jTextField.addKeyListener(this);
			
			//System.out.println(jTextField.getText() + " "  + jTextField.isEditable()); 
			
			datafields.put(key, jTextField);
		}
		
		
		Map<String, GroupLayout.SequentialGroup> vGroups 
		= new HashMap<String, GroupLayout.SequentialGroup>();
		
		GroupLayout.ParallelGroup hGroup = baseLayout.createParallelGroup();
		
		for (String key : data.keySet())
		{
			hGroup.addGroup(
				baseLayout.createSequentialGroup()
					.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
					.addComponent(labelfields.get(key), minLabelWidth, GroupLayout.PREFERRED_SIZE, maxLabelWidth)
					.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
					.addComponent(datafields.get(key), minFieldWidth, GroupLayout.PREFERRED_SIZE, maxFieldWidth)
					.addGap(Globals.hGapSize/2, Globals.hGapSize/2, Globals.hGapSize/2)
					);
		}
		
		baseLayout.setHorizontalGroup(hGroup);
		
		GroupLayout.SequentialGroup vGroup = baseLayout.createSequentialGroup();

		vGroup.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize);
		for(String key : data.keySet())
		{
			vGroup.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize);
			vGroup.addGroup(
				baseLayout.createParallelGroup()
					.addComponent(labelfields.get(key), lineHeight, lineHeight, lineHeight)
					.addComponent(datafields.get(key), lineHeight, lineHeight, lineHeight)
				);
		}
		vGroup.addGap(Globals.vGapSize, Globals.vGapSize, Globals.vGapSize);
		
		baseLayout.setVerticalGroup(vGroup);
		
	}
	
	public LinkedHashMap<String, String> getData()
	{
		for (String key : data.keySet())
		{
			data.put(key, datafields.get(key).getText());
		}
		return data;
	}
	
	
	
	// interface
	// KeyListener
	public void keyPressed (KeyEvent e)
	{
		//logging.debug(this, " key event " + e);
	}
	public void keyTyped (KeyEvent e)  
	{
	}
	public void keyReleased (KeyEvent e)
	{
	}
	
	
	
	public static void main(String[] args)
	{
		LinkedHashMap<String, String> testdata = new LinkedHashMap<String, String>();
		testdata.put("field1", "test1");
		testdata.put("field2", "test2");
		testdata.put("field3", "test3");
		
		HashMap<String, String> labels = new HashMap<String, String>();
		labels.put("field1", "label1");
		labels.put("field2", "label2");
		labels.put("field3", "labelt3");
		
		HashMap<String, Boolean> editable = new HashMap<String, Boolean>();
		editable.put("field1", true);
		editable.put("field2", true);
		editable.put("field3", true);
		
		
		RecordPane instance = new RecordPane(testdata, labels, null, editable);
		instance.setObservableSubject(null);
		
		JDialog f = new JDialog();
		f.setSize(new Dimension(300, 300));
		f.add(instance);
		f.setModal(true);
		f.setVisible(true);
	}
}
		
	
