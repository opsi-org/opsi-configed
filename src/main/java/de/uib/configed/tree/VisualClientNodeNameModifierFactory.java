package de.uib.configed.tree;

import de.uib.configed.*;
import de.uib.utilities.logging.*;
import java.util.*;

public class VisualClientNodeNameModifierFactory
{
	VisualClientNodeNameModifier mod;
	
	class SuppressTerminatingUnderscores implements VisualClientNodeNameModifier
	{
		public String modify( final String in)
		{
			if (in == null)
				return null;
			
			int l = in.length();
			int i = l-1;
			while (i > 0 && (in.charAt( i ) == '_' )) 
			{
				i--;
			}
			
			if ( i == l-1 )
				return in;
			
			return in.substring(0,i+1);
		}
	}
		 
	public VisualClientNodeNameModifierFactory()
	{
		mod = new SuppressTerminatingUnderscores();
	}
	
	public VisualClientNodeNameModifier getModifier()
	{
		return mod;
	}
}
		