/*
 * FEditRecord.java
 *
 * (c) uib 2012
 * GPL-licensed
 * author Rupert Röder
 */

package de.uib.utilities.swing;
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

 
public class FEditRecord extends FEdit
{
	protected RecordPane recordPane; 
	
	protected Map<String, JLabel> labels;
	protected Map<String, JTextField> textfields;
	protected Map<String, String> data;
	protected Map<String, String> hints;
	protected Map<String, Boolean> editable;
	
	
	final ObservableSubject myObservable = new ObservableSubject()
	{
		@Override
		public void notifyObservers()
		{
			//logging.debug(this, "notifyObservers ");
			super.notifyObservers();
			//System.out.println("notifyObservers ");
		}
		@Override
		public void setChanged()
		{
			//logging.debug(this, "setChanged");
			//System.out.println("setChanged");
			super.setChanged();
			setDataChanged(true);
		}
	};
	
    public FEditRecord()
    {
    		this(null);
    }
    
    public FEditRecord(String hint)
	{
		super("", hint );
		recordPane = new RecordPane()
		{
			@Override
			public void keyPressed (KeyEvent e)
			{
				//logging.debug(this, " key event " + e);
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					commit();
				}
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					cancel();
				}
				
			}
		};
		
	}
    
    
	public void setRecord(
		LinkedHashMap<String, String> data, 
		Map<String, String> labels, 
		Map<String, String> hints,
		Map<String, Boolean> editable
	)
	{
		recordPane.setData(
			data, 
			labels, 
			hints,
			editable
		);
		
		recordPane.setObservableSubject(myObservable);
		
		editingArea.add(recordPane, BorderLayout.CENTER);
	}
	
	
	public LinkedHashMap<String, String> getData()
	{
		return recordPane.getData();
	}
	
	
	
	//test version
	public void setRecord()
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
		
		setRecord(testdata, labels, null, editable);
	}
	
	
	
	public void setObservableSubject(ObservableSubject editingNotifier)
	{
		recordPane.setObservableSubject(editingNotifier);
	}
	

	

	@Override
	public void keyPressed (KeyEvent e)
	{
		super.keyPressed(e);
	}
		
	
	public static void main(String[] args)
	{
		FEditRecord instance = new FEditRecord();
		instance.setModal(true);
		
		
		instance.setRecord();
		instance.init(new Dimension(300, 150));
		instance.setVisible(true);
		
	}

}
