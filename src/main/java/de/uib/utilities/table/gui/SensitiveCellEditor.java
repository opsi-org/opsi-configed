package de.uib.utilities.table.gui;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import de.uib.utilities.logging.logging;
import de.uib.utilities.swing.FEditList;
import de.uib.utilities.table.DefaultListModelProducer;
import de.uib.utilities.table.ListModelProducer;

public class SensitiveCellEditor extends AbstractCellEditor implements TableCellEditor, MouseListener {
	protected JTextField field;
	// protected Object value;
	protected FEditList listeditor;
	private int editingRow = -1;
	private int editingColumn = -1;
	protected String myKey;

	protected ListModelProducer modelProducer;
	protected List<Object> forbiddenValues;

	private boolean usingListEditor;

	public SensitiveCellEditor() {
		this(null);
	}

	private static final Map<Object, SensitiveCellEditor> instances = new HashMap<>();

	public static synchronized SensitiveCellEditor getInstance(Object key) {

		// Zu key gehörige Instanz aus Map holen
		SensitiveCellEditor instance = instances.get(key);

		if (instance == null) {
			// Lazy Creation, falls keine Instanz gefunden
			instance = new SensitiveCellEditor();
			// key.startsWith("secret")true);
			instances.put(key, instance);
			instance.myKey = "" + key;
			logging.debug(instance.getClass().getName() + " produced instance for key " + key + " ; size of instances "
					+ instances.size());
		}
		return instance;
	}

	protected SensitiveCellEditor(ListModelProducer modelProducer) {
		super();

		field = new JTextField();
		/*
		 * {
		 * public void setText(String s)
		 * {
		 * String txt = s;
		 * 
		 * if (s != null && s.length() > 1
		 * && (s.charAt(0) == '[')
		 * && (s.charAt(s.length()-1) == ']')
		 * )
		 * {
		 * txt = s.substring(1, s.length()-1);
		 * }
		 * 
		 * super.setText(txt);
		 * logging.debug(this, "setText " + s + " shortened to " + txt);
		 * }
		 * };
		 */
		field.setEditable(false);
		field.addMouseListener(this);
		listeditor = new FEditList(field, this);
		listeditor.setModal(false);// true has undesired effects in the interaction of the CellEditor and the
									// FEditList
		listeditor.init();
		/*
		 * try
		 * {
		 * listeditor.setAlwaysOnTop(true);
		 * }
		 * catch(SecurityException secex)
		 * {
		 * }
		 * //other popup windows are now not visible
		 */
		setModelProducer(modelProducer);
		
	}

	public void re_init() {
		listeditor.init();
	}

	public void setForbiddenValues(List<Object> forbidden) {
		forbiddenValues = forbidden;
	}

	public void setModelProducer(ListModelProducer producer) {
		
		this.modelProducer = producer;
		if (producer == null)
		// build default producer
		{
			modelProducer = new DefaultListModelProducer();
		}
		
	}

	private void startListEditor(final JTable table, final int row, final int column) {
		field.setEditable(false);
		listeditor.init();

		SwingUtilities.invokeLater(() -> {
			// Rectangle rect = table.getCellRect(row, column, true);
			// Point tablePoint = table.getLocationOnScreen();

			listeditor.setVisible(true);
			listeditor.locateLeftTo(table);
		});
		
		usingListEditor = true;
	}

	public void hideListEditor() {
		SwingUtilities.invokeLater(() -> listeditor.setVisible(false));
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		logging.debug(this, "  celleditor working in " + row + ", " + column + " with value " + value + ", class "
				+ value.getClass().getName());
		// this.value = modelProducer.toList(value);
		// this.value = new ArrayList<>();
		List val = modelProducer.toList(value);

		if (val instanceof List) // is now always
		{
			

			ListModel model = modelProducer.getListModel(row, column);
			logging.debug(this,
					" try list editing, modelproducer tells nullable " + modelProducer.getNullable(row, column));
			listeditor.setVisible(false);
			listeditor.setTitle(modelProducer.getCaption(row, column));

			

			if (model != null) {
				

				listeditor.setListModel(modelProducer.getListModel(row, column));
				
				logging.info(this, "startValue set: " + value);

				listeditor.setSelectionMode(modelProducer.getSelectionMode(row, column));
				listeditor.setEditable(modelProducer.getEditable(row, column));
				listeditor.setNullable(modelProducer.getNullable(row, column));
				listeditor.setSelectedValues(modelProducer.getSelectedValues(row, column));

				listeditor.enter();
				startListEditor(table, row, column);

				editingRow = row;
				editingColumn = column;
			}

			else {
				model = new DefaultListModel();

				listeditor.setListModel(model);
				startListEditor(table, row, column);

				listeditor.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				listeditor.setEditable(true);
				listeditor.setSelectedValues(new ArrayList<>());

				listeditor.enter();
				listeditor.setStartValue("");

				editingRow = -1;
				editingColumn = -1;
			}

			/*
			 * {
			 * logging.warning(this, "no listmodel got, String editing");
			 * field.setEditable(true);
			 * editingRow = -1;
			 * editingColumn = -1;
			 * }
			 */

		}
		/*
		 * else if (value instanceof java.lang.Boolean)
		 * {
		 * field.setEditable(true);
		 * }
		 * else
		 * field.setEditable(true);
		 */

		field.setText("" + value);

		return field;
	}

	@Override
	public Object getCellEditorValue() {
		
		// editingRow + ", " + editingColumn +", value :");
		
		// class " + listeditor.getValue().getClass());
		
		// original class " + modelProducer.getClass(editingRow, editingColumn));

		// if ( List.class.isAssignableFrom
		// (modelProducer.getClass(editingRow, editingColumn)) )
		
		// List");

		if (listeditor.getValue() == null)
			return null;

		if (listeditor.getValue() instanceof List) {
			List list = (List) listeditor.getValue();

			if (forbiddenValues != null) {
				Iterator iter = forbiddenValues.iterator();
				while (iter.hasNext()) {
					Object element = iter.next();
					
					list.remove(element);
				}

				
			}

			int n = list.size();

			
			// editingRow + " , " + editingColumn + " " +
			// "modelProducer.getClass(editingRow, editingColumn) " +
			// modelProducer.getClass(editingRow, editingColumn) );

			/*
			 * if ( List.class.isAssignableFrom
			 * (modelProducer.getClass(editingRow, editingColumn)) )
			 * {
			 * logging.info(this,
			 * "getCellEditorValue,  original class instance of List");
			 * //ArrayList<String> li = new ArrayList<>();
			 * //li.add("xxx");
			 * //return li;
			 * return list;
			 * }
			 */

			if (List.class.isAssignableFrom(modelProducer.getClass(editingRow, editingColumn))) {
				
				// List");
				// ArrayList<String> li = new ArrayList<>();
				// li.add("xxx");
				// return li;
				return list;
			}

			if (java.lang.Integer.class.isAssignableFrom(modelProducer.getClass(editingRow, editingColumn))) {
				
				// java.lang.Integer");
				

				if (n == 0)
					return null;

				return list.get(0);
			}

			if (java.lang.Boolean.class.isAssignableFrom(modelProducer.getClass(editingRow, editingColumn))) {
				
				// java.lang.Boolean");

				if (n == 0)
					return null;

				return list.get(0);
			}

			if (n == 0)
				return "";

			// if ( java.lang.String.class.isAssignableFrom
			// (modelProducer.getClass(editingRow, editingColumn)) )
			// assume String
			{
				StringBuffer buf = new StringBuffer("");

				if (n == 0)
					return "";

				for (int i = 0; i < n - 1; i++) {
					buf.append("" + list.get(i) + ",");
				}
				buf.append("" + list.get(n - 1));

				String result = buf.toString();

				

				if (result.equalsIgnoreCase("null"))
					return org.json.JSONObject.NULL;

				return result;
			}

			/*
			 * 
			 * try{
			 * return modelProducer.getClass(editingRow, editingColumn).cast(list.get(0));
			 * }
			 * catch(Exception ex)
			 * {
			 * logging.debug(this, "getCellEditorValue, cast to original class: " + ex);
			 * }
			 * 
			 * if ( ("" + list.get(0)).equalsIgnoreCase("null") )
			 * return org.json.JSONObject.NULL;
			 * 
			 * return list;
			 */
		}

		return listeditor.getValue();
	}

	/*
	 * if (editingRow > -1 && editingColumn > -1)
	 * {
	 * logging.debug(this, "getCellEditorValue " + listeditor.getValue());
	 * 
	 * modelProducer.setSelectedValues((List) listeditor.getValue(),
	 * editingRow, editingColumn);
	 * return listeditor.getValue();
	 * }
	 * else
	 * return field.getText();
	 * 
	 * }
	 */

	public void finish() {
		if (listeditor != null)
			listeditor.deactivate();
	}

	// MouseListener for textfield
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == field && usingListEditor) {
			if (e.getClickCount() > 1) {
				
				listeditor.setVisible(true);
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

}
