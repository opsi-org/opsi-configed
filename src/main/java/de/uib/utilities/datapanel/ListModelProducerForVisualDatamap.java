package de.uib.utilities.datapanel;
	
import java.awt.*;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;
import de.uib.utilities.table.*;


/*
	ListModelProducerForDatamap 
	produces list models based on keys (given in column 0 of a table) 
	for which data exist (to be placed in column 1 of the table)
*/

public class ListModelProducerForVisualDatamap 
	extends DefaultListModelProducer
{
	Map<Integer, ListModel> listmodels = new HashMap<Integer, ListModel>();
	
	Map<String,  ListCellOptions> optionsMap; 
	Map<String, java.util.List> currentData;
	Map<String, Class> originalTypes;
	JTable table;
	
	public ListModelProducerForVisualDatamap(
		JTable tableVisualizingMap,
		Map<String,  ListCellOptions> optionsMap,
		Map currentData
	)
	{
		this.table =  tableVisualizingMap;
		setData(optionsMap, currentData);
	}
	
	public void setData(	
		Map<String,  ListCellOptions> optionsMap,
		Map currentData)
	{
		this.optionsMap = optionsMap;
		//logging.info(this, "setData " + optionsMap + " optionsMap.get(type) " + optionsMap.get("type")); 
		mapTypes(currentData);
	}
	
	public void updateData(Map currentData)
	{
		mapTypes(currentData);
	}
	
	private ListCellOptions getListCellOptions(String key)
	{
		ListCellOptions options = optionsMap.get(key);
		if ( options == null )
		{
			options = new DefaultListCellOptions();
			optionsMap.put(key, options); 
		}
		return options;
	}
	
	
		
	private void mapTypes(final Map currentData)
	{
		this.currentData = new HashMap<String, java.util.List>();
		logging.debug(this, "mapTypes  " + currentData);
		originalTypes = new HashMap<String, Class>();
		for (Object key : currentData.keySet())
		{
			Object value = currentData.get(key);
			//logging.debug(this, "mapTypes key, value " + key + ", " + value);
			//logging.debug(this, "mapTypes key, value class " + key + ", " + value.getClass());
			originalTypes.put((String) key, value.getClass());
			this.currentData.put((String) key, toList(value));
		}
		
	}
	
	public ListModel getListModel(int row, int column)
	{
		//column can be assumed to be 1
		
		if (listmodels.get(row) != null)
		{
			// we already built a model
			return listmodels.get(row);
		}
		
		logging.info(this, "getListModel, row " + row + ", column " + column);
		
		//build listmodel
		//logging.info(this, "getListModel table.getValueAt(row, 0) " + table.getValueAt(row, 0)); 
		String key = (String) table.getValueAt(row, 0);
		
		//logging.debug(this, "key = table.getValueAt( " + row + ", 0 ), " + key);
		
		ListCellOptions options = getListCellOptions(key);
		
		java.util.List values  = options.getPossibleValues();
		logging.info(this, "getListModel key " + key + " the option values " + values);
		logging.info(this, "getListModel key " + key + " options  " + options);
		
		//logging.debug(this, "we produce a list model");
		DefaultListModel model = new DefaultListModel();
		Iterator iter = ((java.util.List) values).iterator();
		while (iter.hasNext())
		{
			model.addElement( iter.next() );
		}
		if ( currentData.get(key) != null && currentData.get(key) instanceof java.util.List )
		{
			iter =  ((java.util.List) currentData.get(key)).iterator();
			
			while (iter.hasNext())
			{
				Object entry = iter.next();
				if (!model.contains(entry))
					model.addElement( entry );
			}
		}
		listmodels.put(row, model);
		
		return model;
	}

	public java.util.List getSelectedValues(int row, int column)
	{
		//logging.info(this, "getSelectedValues row " + row);
		String key = (String) table.getValueAt(row, 0);
		return (java.util.List) currentData.get(key);
	}

	public void setSelectedValues( java.util.List newValues, int row, int column )
	{
		//logging.info(this, "setSelectedValues row " + row);
		String key = (String) table.getValueAt(row, 0);
		currentData.put(key, newValues);
		table.setValueAt(newValues, row, 1);
	}
	
	public int getSelectionMode(int row, int column)
	{
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).getSelectionMode();
	}

	public boolean getEditable(int row, int column)
	{
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).isEditable();
	}
	
	public boolean getNullable(int row, int column)
	{
		String key = (String) table.getValueAt(row, 0);
		return getListCellOptions(key).isNullable();
	}
	
	public String getCaption(int row, int column)
	{
		return (String) table.getValueAt(row, 0);
	}
	
	public Class getClass(int row, int column)
	{
		//logging.info(this, "getClass  for row, col " + row + ", " + column);
		String key = (String) table.getValueAt(row, 0);
		//logging.info(this, "getClass  key " + key);
		
		return originalTypes.get(key);
	}
		
}	
		
	

