package de.uib.utilities.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import de.uib.utilities.Globals;
import de.uib.utilities.logging.*;


public class JTextHideField extends javax.swing.JPanel
{
	JPasswordField invisibleField;
	JTextField visibleField;
	JPanel panel;
	JButton button;
	boolean hiddenMode;
	boolean multiValue;
	//final static protected String hiddenS = "****";
	
	class FixedDocument extends PlainDocument {
		
		private String fixed = "xxx";
		
		FixedDocument( )
		{
			super();
		}
		
		void setFixed( String s )
		{
			fixed = s;
		}
		
		@Override
		public void insertString(int offs, String str, AttributeSet a)
			throws BadLocationException 
		 {
			super.remove(0, super.getLength());
			
			super.insertString(0, fixed, a);
			
			//logging.info(this, "inserted " + str);
			
			
		 }
		 @Override
		 public void remove(int offs, int len)
		 {
			 return;
		 }
	 }
	
	public JTextHideField()
	{
		invisibleField = new JPasswordField();
		visibleField = new JTextField(){
			
			@Override
			protected Document createDefaultModel() {
				return new FixedDocument();
			}
			
		 };
		 
		hiddenMode = true;
		multiValue = true;
		visibleField.setVisible( true) ;//!hiddenMode );
		invisibleField.setVisible (true);
		visibleField.setEnabled(true);
		invisibleField.setEnabled(false);
		
		button = new JButton( de.uib.configed.Globals.createImageIcon("images/eye_blue_open.png", "show"));
		button.addActionListener( new ActionListener(){
				@Override
				public void  actionPerformed( ActionEvent e )
				{
					if (!multiValue) toggleHidden();
				}
			}
		);
		button.setToolTipText(de.uib.configed.configed.getResourceValue("JTextHideField.toggleHide"));
					
		//button.setEnabled( true );
		//button.setBackground( Color.BLUE );
		//visibleField.setEditable(false);
		
		setupPanel();
		setEnabled( false );
	}
	
	protected void setupPanel()
	{
		GroupLayout layout = new GroupLayout( this );
		this.setLayout( layout );
		
		layout.setHorizontalGroup( layout.createParallelGroup()
			.addGroup( layout.createSequentialGroup()
				.addGroup( layout.createParallelGroup()
					.addComponent( invisibleField, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE)
					.addComponent( visibleField, Globals.buttonWidth, Globals.buttonWidth, Short.MAX_VALUE)
				)
				.addComponent( button, Globals.iconWidth, Globals.iconWidth, Globals.iconWidth)
				.addGap(0, Globals.iconWidth, Globals.buttonWidth )
			)
		);
		
		layout.setVerticalGroup( layout.createParallelGroup()
			.addGroup( layout.createParallelGroup()
				.addComponent( invisibleField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
				.addComponent( visibleField, Globals.lineHeight, Globals.lineHeight, Globals.lineHeight)
			)
			.addComponent( button, Globals.lineHeight/2, Globals.lineHeight/2, Globals.lineHeight/2)
		);
	}		
	
	
			
	public void setHidden()
	{
		if (!hiddenMode)
			toggleHidden();
	}
	
	public void setEnabled(boolean  b)
	{
		visibleField.setEnabled( b );
		button.setEnabled( b );
		
	}
	
	public void setMultiValue( boolean b )
	{
		logging.info(this, "************+ setMultiValue " + b);
		multiValue = b;
		if (multiValue)
		{
			setText("");
			setHidden();
		}
	}
	
	public void toggleHidden()
	{
		hiddenMode = !hiddenMode;
		visibleField.setVisible( !hiddenMode );
		invisibleField.setVisible (hiddenMode);
		validate();
		repaint();
		
	}
	
	public void setBackground( java.awt.Color c )
	{
		if (visibleField != null)
			visibleField.setBackground( c );
	}
	
	
	public void setText(String s)
	{
		String s0 = s;
		if (multiValue)
			s0 = "";
		
		invisibleField.setText( s0 );
		((FixedDocument)(visibleField.getDocument())).setFixed( s0 );
		visibleField.setText( s0 );
	}
	
		

	
	
}
	
	
