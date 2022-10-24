package de.uib.utilities.swing;
/**
 * SeparatedDocument.java
 * Copyright:     Copyright (c) 2006-2015
 * Organisation:  uib
 * @author Rupert Roeder, Anna Sucher
 */

import java.util.*;
import javax.swing.text.*;
import de.uib.utilities.logging.*;


public class CheckedDocument extends PlainDocument 
{
	char[] allowedChars;
	int size; 
	boolean checkMask = false;

	public CheckedDocument()
	{

	}
	public CheckedDocument(char[] allowedChars, int realSize)
	{
		this.allowedChars = allowedChars;
		this.size = realSize;
	}

	
	public boolean appendCharIfAllowed(StringBuffer s, char c)
	{
		char cCorrected = c;
		
		if (allowedChars == null)
			return false;
		
		boolean result = false;
		
		for (int j=0; j < allowedChars.length; j++)
		{
			if (Character.toLowerCase(allowedChars[j]) == Character.toLowerCase(c))
			{
				if (Character.isLowerCase(allowedChars[j]))
					 cCorrected= Character.toLowerCase(c);
				else if (Character.isUpperCase(allowedChars[j]))
					cCorrected = Character.toUpperCase(c);
				s.append(cCorrected);
				result = true;
				break;
				 
			}
		}
		
		return result;
	}

	
	public String giveAllowedCharacters(String s, int offset)
	{
		// logging.info(this, "giveAllowedCharacters " + s + " " + offset);
		if (s==null) return "";
		
		char[] startchars = s.toCharArray();
		StringBuffer textBuf = new StringBuffer();

		for (int i = 0; i< startchars.length; i++)
		{
			appendCharIfAllowed(textBuf, startchars[i]);
		}

		// logging.info(this, "giveAllowedCharacters textBuf " +  textBuf);
		return textBuf.toString();
	}
	
	protected void applyMask(AttributeSet a, int insertOffs)
		 throws BadLocationException 
	{
	}

	protected void insertStringPlain(int offs, String s, AttributeSet a)
	 throws BadLocationException 
	{
		//logging.info(this, "insertStringPlain super is " + super.getClass().getName());
		super.insertString(offs, s, a);
	}
	
	public void insertString(int offs, String s, AttributeSet a)
	 throws BadLocationException 
	{
		//logging.info(this, "insertString s offs, size " + s + ", " + offs + ", " + size);

		if (s == null) return;

		if (size > -1 && offs >= size)
			return;
		
		//logging.debug(this, "insertString  getText " + getText(0, getLength())); 
		
		String corrected = giveAllowedCharacters(s, offs);
		//logging.info(this, "insertString corrected, offs, size " + corrected + ", " + offs + ", " + size);
		if (size > -1 && offs + corrected.length() > size)
			corrected = corrected.substring(0, size - offs);
		
		//logging.debug(this, "insertString corrected, offs, size " + corrected + ", " + offs + ", " + size);
		
		insertStringPlain(offs, corrected, a);
		if (checkMask) applyMask(a, offs);
	}


}