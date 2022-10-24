package de.uib.utilities.table.gui;

import de.uib.configed.Globals;
import de.uib.configed.configed;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import de.uib.utilities.*;
import de.uib.utilities.swing.*;
import de.uib.utilities.table.*;
import de.uib.utilities.logging.*;

public class SensitiveCellEditor extends AbstractCellEditor
			implements TableCellEditor, MouseListener
{
	protected JTextField field;
	//protected Object value;
	protected FEditList listeditor;
	private int editingRow = -1;
	private int editingColumn = -1;
	protected String myKey;

	protected ListModelProducer modelProducer;
	protected java.util.List<Object> forbiddenValues;

	private boolean usingListEditor;

	public SensitiveCellEditor()
	{
		this(null);
	}

	private static final Map<Object, SensitiveCellEditor> instances = new HashMap<Object, SensitiveCellEditor>();
	public static synchronized SensitiveCellEditor getInstance(Object key) {

		// Zu key gehörige Instanz aus Map holen
		SensitiveCellEditor instance = instances.get(key);

		if (instance == null) {
			// Lazy Creation, falls keine Instanz gefunden
			instance = new SensitiveCellEditor();
			//key.startsWith("secret")true);
			instances.put(key, instance);
			instance.myKey = "" + key;
			logging.debug(instance.getClass().getName() + " produced instance for key " + key + " ; size of instances " + instances.size());
		}
		return instance;
	}


	protected SensitiveCellEditor(ListModelProducer modelProducer)
	{
		super();

		field = new JTextField();
		/*
		{
			public void setText(String s)
			{
				String txt = s;
				
				if (s != null && s.length() > 1
					&& (s.charAt(0) == '[')
					&& (s.charAt(s.length()-1) == ']')
					)
				{
					txt = s.substring(1, s.length()-1);
				}
				
				super.setText(txt);
				logging.debug(this, "setText " + s + " shortened to " + txt);
			}
	};*/
		field.setEditable(false);
		field.addMouseListener(this);
		listeditor = new FEditList(field, this);
		listeditor.setModal(false);//true has undesired effects in the interaction of the CellEditor and the FEditList
		listeditor.init();
		/*
		try
		{
			listeditor.setAlwaysOnTop(true);
	}
		catch(SecurityException secex)
		{
	}
		//other popup windows are now not visible
		*/
		setModelProducer(modelProducer);
		//logging.debug(this, "  constructed");
	}

	public void re_init()
	{
		listeditor.init();
	}


	public void setForbiddenValues(java.util.List<Object> forbidden)
	{
		forbiddenValues = forbidden;
	}

	public void setModelProducer(ListModelProducer producer)
	{
		//logging.info(this, "ListModelProducer set: " + modelProducer);
		this.modelProducer = producer;
		if (producer == null)
			// build default producer
		{
			modelProducer = new DefaultListModelProducer();
		}
		//logging.debug(this, "ListModelProducer set: " + modelProducer);
	}


	private void startListEditor(final JTable table, final int row,  final int column)
	{
		field.setEditable(false);
		listeditor.init();

		SwingUtilities.invokeLater(new Runnable(){
			                           public void run(){
				                           //Rectangle rect = table.getCellRect(row, column, true);
				                           //Point tablePoint = table.getLocationOnScreen();

				                           listeditor.setVisible(true);
				                           listeditor.locateLeftTo( table ); //de.uib.configed.Globals.mainContainer );
				                           //listeditor.setLocation((int) tablePoint.getX()  + (int) rect.getX() + 50, (int) tablePoint.getY() +  (int) rect.getY() +  Globals.lineHeight );
			                           }
		                           }
		                          );
		//listeditor.setVisible(true);
		usingListEditor = true;
	}

	public void hideListEditor()
	{
		SwingUtilities.invokeLater(new Runnable(){
			                           public void run(){
				                           //Rectangle rect = table.getCellRect(row, column, true);
				                           //Point tablePoint = table.getLocationOnScreen();

				                           listeditor.setVisible(false);
				                           //listeditor.setLocation((int) tablePoint.getX()  + (int) rect.getX() + 50, (int) tablePoint.getY() +  (int) rect.getY() +  Globals.lineHeight );
			                           }
		                           }
		                          );
	}



	public Component getTableCellEditorComponent(JTable table,
	        Object value,
	        boolean isSelected,
	        int row,
	        int column)
	{

		logging.debug(this, "  celleditor working in " + row + ", " + column + " with value " + value + ", class " + value.getClass().getName());
		//this.value = modelProducer.toList(value);
		//this.value = new ArrayList();
		java.util.List val = modelProducer.toList(value);

		if (val instanceof java.util.List) //is now always
		{
			//logging.info(this, " try list editing, with modelProducer " + modelProducer);

			ListModel model = modelProducer.getListModel(row, column);
			logging.debug(this, " try list editing, modelproducer tells nullable " + modelProducer.getNullable(row, column));
			listeditor.setVisible(false);
			listeditor.setTitle(modelProducer.getCaption(row, column));


			//logging.info(this, " model got " + model);

			if ( model != null)
			{
				//logging.info(this, "start with model");

				listeditor.setListModel(modelProducer.getListModel(row, column));
				//listeditor.setStartValue(value);
				logging.info(this, "startValue set: " + value);

				listeditor.setSelectionMode(modelProducer.getSelectionMode(row,column));
				listeditor.setEditable(modelProducer.getEditable(row,column));
				listeditor.setNullable(modelProducer.getNullable(row,column));
				listeditor.setSelectedValues(modelProducer.getSelectedValues(row, column));


				listeditor.enter();
				startListEditor(table, row, column);


				editingRow = row;
				editingColumn = column;
			}

			else
			{
				model = new DefaultListModel();

				listeditor.setListModel(model);
				startListEditor(table, row, column);

				listeditor.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				listeditor.setEditable(true);
				listeditor.setSelectedValues(new ArrayList());

				listeditor.enter();
				listeditor.setStartValue("");

				editingRow = -1;
				editingColumn = -1;
			}

			/*
		{
				logging.warning(this, "no listmodel got, String editing");
				field.setEditable(true);
				editingRow = -1;
				editingColumn = -1;
		}
			*/


		}
		/*
		else if (value instanceof java.lang.Boolean)
		{
			field.setEditable(true);
	}
		else 
			field.setEditable(true);
		*/

		field.setText("" + value);

		return field;
	}


	public Object getCellEditorValue()
	{
		//logging.debug(this, "getCellEditorValue, editingRow, editingColumn  " + editingRow  + ", " + editingColumn  +", value :");
		//logging.debug(this, "getCellEditorValue "   + listeditor.getValue() + " has class " + listeditor.getValue().getClass());
		//logging.debug(this, "getCellEditorValue "   + listeditor.getValue() + " original class " + modelProducer.getClass(editingRow, editingColumn));

		//if ( java.util.List.class.isAssignableFrom (modelProducer.getClass(editingRow, editingColumn)) )
		//	logging.debug(this, "getCellEditorValue,  original class instance of java.util.List");

		if (listeditor.getValue() == null)
			return null;

		if (listeditor.getValue() instanceof java.util.List)
		{
			java.util.List list = (java.util.List) listeditor.getValue();

			if (forbiddenValues != null)
			{
				Iterator iter = forbiddenValues.iterator();
				while (iter.hasNext())
				{
					Object element = iter.next();
					//logging.debug(this, "removing ?? " + element);
					list.remove(element);
				}

				//logging.debug(this, "getCellEditorValue, corrected result " + list);
			}

			int n = list.size();

			//logging.info(this, "getCellEditorValue editingRow, editingColumn  " +  editingRow + " , " + editingColumn + "  " +
			//"modelProducer.getClass(editingRow, editingColumn) " + modelProducer.getClass(editingRow, editingColumn) );

			/*
			if ( java.util.List.class.isAssignableFrom (modelProducer.getClass(editingRow, editingColumn)) )
		{
				 logging.info(this, "getCellEditorValue,  original class instance of java.util.List");
				 //ArrayList<String> li = new ArrayList<String>();
				 //li.add("xxx");
				 //return li;
				 return list;
		}
			*/

			if ( java.util.List.class.isAssignableFrom (modelProducer.getClass(editingRow, editingColumn)) )
			{
				//logging.info(this, "getCellEditorValue,  original class instance of java.util.List");
				//ArrayList<String> li = new ArrayList<String>();
				//li.add("xxx");
				//return li;
				return list;
			}


			if ( java.lang.Integer.class.isAssignableFrom (modelProducer.getClass(editingRow, editingColumn)) )
			{
				//logging.info(this, "getCellEditorValue,  original class instance of java.lang.Integer");
				//logging.debug(this, "getCellEditorValue() " + list.get(0));

				if (n == 0)
					return null;

				return list.get(0);
			}

			if ( java.lang.Boolean.class.isAssignableFrom (modelProducer.getClass(editingRow, editingColumn)) )
			{
				//logging.info(this, "getCellEditorValue,  original class instance of java.lang.Boolean");

				if (n == 0)
					return null;

				return list.get(0);
			}



			if (n == 0)
				return "";

			//if ( java.lang.String.class.isAssignableFrom (modelProducer.getClass(editingRow, editingColumn)) )
			//assume String
			{
				StringBuffer buf = new StringBuffer("");

				if (n == 0)
					return "";

				for (int i = 0; i< n -1; i++)
				{
					buf.append("" + list.get(i) + ",");
				}
				buf.append("" + list.get(n-1));

				String result = buf.toString();

				//logging.info(this, "getCellEditorValue, String-Result " + result);

				if (result.equalsIgnoreCase("null"))
					return org.json.JSONObject.NULL;

				return result;
			}

			/*

			try{
				 return modelProducer.getClass(editingRow, editingColumn).cast(list.get(0));
		}
			catch(Exception ex)
		{
				 logging.debug(this, "getCellEditorValue, cast to original class: " + ex);
		}

			if ( ("" + list.get(0)).equalsIgnoreCase("null") )
				 return org.json.JSONObject.NULL;
				 
			return list;
			*/
		}

		return listeditor.getValue();
	}

	/*
		if (editingRow > -1 && editingColumn > -1)
		{
			logging.debug(this, "getCellEditorValue " + listeditor.getValue());
		
			modelProducer.setSelectedValues((java.util.List) listeditor.getValue(), editingRow, editingColumn); 
			return listeditor.getValue();
		}
		else
			return field.getText();
		
}
	*/

	public void finish()
	{
		if (listeditor !=null)
			listeditor. deactivate();
	}


	//MouseListener for textfield
	public void  mouseClicked(MouseEvent e)
	{
		if (e.getSource() == field && usingListEditor)
		{
			if (e.getClickCount() > 1)
			{
				//logging.debug(this, "listeditor activated by doubleclick");
				listeditor.setVisible(true);
			}
		}
	}
	public void  mouseEntered(MouseEvent e) {}
	public void  mouseExited(MouseEvent e)  {}
	public void  mousePressed(MouseEvent e)  {}
	public void  mouseReleased(MouseEvent e)  {}

}
