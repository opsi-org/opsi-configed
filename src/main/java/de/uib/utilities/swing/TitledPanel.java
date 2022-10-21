package de.uib.utilities.swing;

import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import java.awt.*;
import javax.swing.*;

public class TitledPanel extends JPanel
{
	protected JLabel label1;
	protected JLabel label2;
	

	public TitledPanel()
	{
		this ("", "");
	}
		
	public TitledPanel(String title1, String title2)
	{
		label1 = new JLabel();
		label2 = new JLabel();
		initGui();
		setTitle( title1, title2 );
	}
	
	public void setTitle( String s1, String s2 )
	{
		label1.setText( s1 );
		label2.setText( s2 );
		label2.setVisible( s2 != null );
	}

	protected void initGui()
	{
		setBackground( de.uib.utilities.Globals.backLightBlue);
		
		label1.setFont(Globals.defaultFontBig);
		label2.setFont(Globals.defaultFontBig);
		
		GroupLayout innerLayout = new GroupLayout( this );
		this.setLayout( innerLayout );
		innerLayout.setVerticalGroup(innerLayout.createSequentialGroup()
			.addGap( 2*Globals.vGapSize, 3*Globals.vGapSize, 3*Globals.vGapSize)
			.addComponent( label1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap( Globals.vGapSize, Globals.vGapSize, Globals.vGapSize)
			.addComponent( label2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGap( Globals.vGapSize, Globals.vGapSize, Short.MAX_VALUE)
			)
		;
		
		innerLayout.setHorizontalGroup(innerLayout.createParallelGroup()
			.addGroup( innerLayout.createSequentialGroup()
				.addGap( Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE)
				.addComponent( label1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE )
				.addGap( Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE)
				)
			.addGroup( innerLayout.createSequentialGroup()
				.addGap( Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE)
				.addComponent( label2, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE )
				.addGap( Globals.hGapSize, Globals.hGapSize, Short.MAX_VALUE)
				)
			)
		;
		
	}
}