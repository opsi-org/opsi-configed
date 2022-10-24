package de.uib.utilities.swing; 

/*
 * 
 * Author: Rupert Röder, uib 2010
 *
 * roughly based on CustomComboBoxDemo
 *
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import javax.swing.*;
import java.awt.*;
import java.util.*;
import de.uib.configed.*;
import de.uib.utilities.logging.*;

public class CellRendererByIndex extends ImagePlusTextLabel
                           implements ListCellRenderer {
	private Font uhOhFont;
	
	protected Map<String, String> mapOfStrings;
	protected Map<String, String> mapOfTooltips;
	protected Map<String, ImageIcon> mapOfImages;
	final static int imageDefaultWidth = 30; 
	
	public CellRendererByIndex(Map<String,String> mapOfStringValues, String imagesBase)
	{
		this( mapOfStringValues, imagesBase, imageDefaultWidth);
	}
	
	public CellRendererByIndex(Set<String> keySet, String imagesBase, int imageWidth)
	{
		super(imageWidth);
		//System.out.println(" -------------  font " + getFont() );
		setOpaque(true);
		//setHorizontalAlignment(LEFT);
		//setVerticalAlignment(CENTER);
		mapOfImages = new HashMap<String,ImageIcon>();
		mapOfStrings = new HashMap<String, String>();
		
		 //Load the item images 
		if (imagesBase ==  null)
			super.setIconVisible(false);
		
		else
		{
			Iterator<String> iter = keySet.iterator();
			while (iter.hasNext())
			{
				String key = iter.next(); 
				String stringval = key;
				mapOfStrings.put(key, key);
				
				ImageIcon image = null;
				
				if (imagesBase != null)
				{
					String imageFileString = imagesBase + "/" + stringval + ".png";
						//System.out.println (" image file " + imageFileString);
						
					image = de.uib.configed.Globals.createImageIcon(imageFileString, stringval);
					if (image != null) mapOfImages.put(key, image);
				}
			}
		}
		mapOfTooltips = mapOfStrings;
		
	}

	public CellRendererByIndex(Map<String,String> mapOfStringValues, String imagesBase, int imageWidth) 
	{
		this(mapOfStringValues, mapOfStringValues, imagesBase, imageWidth);
	}
	
	public CellRendererByIndex(Map<String,String> mapOfStringValues, Map<String, String> mapOfDescriptions)
	{	
		this(mapOfStringValues, mapOfDescriptions, null, 0);
	}
	
	public CellRendererByIndex(
		Map<String,String> mapOfStringValues,
		Map<String, String> mapOfDescriptions,
		String imagesBase, int imageWidth)
	
	{
		super(imageWidth);
		//System.out.println(" -------------  font " + getFont() );
		setOpaque(true);
		//setHorizontalAlignment(LEFT);
		//setVerticalAlignment(CENTER);
		mapOfStrings = mapOfStringValues;
		mapOfTooltips = mapOfDescriptions;
		mapOfImages = new HashMap<String,ImageIcon>();
		
		
		 //Load the item images 
		if (imagesBase ==  null)
			super.setIconVisible(false);
		
		else
		{
			Iterator iter = mapOfStrings.entrySet().iterator();
			while (iter.hasNext())
			{
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				String stringval = (String) entry.getValue();
				
				ImageIcon image = null;
				
				if (key != null && stringval != null)
				{
					String imageFileString = imagesBase + "/" + stringval + ".png";
					//System.out.println (" image file " + imageFileString);
					
					image = de.uib.configed.Globals.createImageIcon(imageFileString, stringval);
					if (image != null) mapOfImages.put(key, image);
				}
			}
		}
		
	}

	/*
	 * This method finds the image and text corresponding
	 * to the selected value and returns the label, set up
	 * to display the text and image.
	 */
	public Component getListCellRendererComponent(
									   JList list,
									   Object value,
									   int index,
									   boolean isSelected,
									   boolean cellHasFocus) {
		//Get the selected index. (The index param isn't
		//always valid, so just use the value.)

		
		/*
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
			//setForeground(java.awt.Color.black);
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			//setForeground(java.awt.Color.black);
		}
		*/
		
		
	
		Color background;
		Color foreground;
		
		if (isSelected) 
		{
			background = de.uib.configed.Globals.nimbusSelectionBackground;
			foreground = Color.WHITE;
		}
		else 
		{
			background =  de.uib.configed.Globals.nimbusBackground;
			foreground =  de.uib.configed.Globals.nimbusSelectionBackground; //Color.WHITE; // Color.black;
		};
		
		setBackground(background);
		setForeground(foreground);
		
		

		String selectedString = "";
		ImageIcon selectedIcon = null;
		String selectedTooltip = "";

		
		if (uhOhFont == null) { //lazily create this font
			uhOhFont = list.getFont().deriveFont( (float) 10 ); // list.getFont().getSize() - 3); // Font.ITALIC);
		}
		setFont(uhOhFont);
		
		if (value != null)
		{
			if (mapOfStrings != null) selectedString = mapOfStrings.get(value);
			if (mapOfImages != null) selectedIcon = mapOfImages.get(value);
			if (mapOfTooltips != null) selectedTooltip = mapOfTooltips.get(value); 
				
		}
		
		if (selectedString == null) 
			selectedString = "" +  value;
			
		setIcon(selectedIcon);
		setText(selectedString);
		
		
		setToolTipText(selectedTooltip);
		
		setFont(Globals.defaultFont);
		//de.uib.utilities.swing.CellAlternatingColorizer.colorize(this, isSelected, (index % 2 == 0), true);
		
		/*
		if (icon != null) {
			setText(item);
			setFont(list.getFont());
		} else {
			setUhOhText(item + " (no image available)",		list.getFont());
			
		}*/

		return this;
	}

	/*
	//Set the font and text when no image was found.
	protected void setUhOhText(String uhOhText, Font normalFont) {
		if (uhOhFont == null) { //lazily create this font
			uhOhFont = normalFont.deriveFont(Font.ITALIC);
		}
		setFont(uhOhFont);
		setText(uhOhText);
	}
	*/
}
