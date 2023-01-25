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

import de.uib.configed.Globals;
import de.uib.utilities.logging.Logging;
import de.uib.utilities.swing.FEditList;
import de.uib.utilities.table.DefaultListModelProducer;
import de.uib.utilities.table.ListModelProducer;

public class SensitiveCellEditor extends AbstractCellEditor implements TableCellEditor, MouseListener {
	protected JTextField field;

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

			instances.put(key, instance);
			instance.myKey = "" + key;
			Logging.debug(instance.getClass().getName() + " produced instance for key " + key + " ; size of instances "
					+ instances.size());
		}
		return instance;
	}

	protected SensitiveCellEditor(ListModelProducer modelProducer) {
		super();

		field = new JTextField();

		field.setEditable(false);
		field.addMouseListener(this);
		listeditor = new FEditList(field, this);
		listeditor.setModal(false);// true has undesired effects in the interaction of the CellEditor and the
									// FEditList
		listeditor.init();

		setModelProducer(modelProducer);

	}

	public void reInit() {
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

	private void startListEditor() {
		field.setEditable(false);
		listeditor.init();

		SwingUtilities.invokeLater(() -> {
			// center on mainFrame
			listeditor.setLocationRelativeTo(Globals.mainFrame);
			listeditor.setVisible(true);
		});

		usingListEditor = true;
	}

	public void hideListEditor() {
		SwingUtilities.invokeLater(() -> listeditor.setVisible(false));
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		Logging.debug(this, "  celleditor working in " + row + ", " + column + " with value " + value + ", class "
				+ value.getClass().getName());

		List val = modelProducer.toList(value);

		if (val instanceof List) // is now always
		{

			ListModel<Object> model = modelProducer.getListModel(row, column);
			Logging.debug(this,
					" try list editing, modelproducer tells nullable " + modelProducer.getNullable(row, column));
			listeditor.setVisible(false);
			listeditor.setTitle(modelProducer.getCaption(row, column));

			if (model != null) {

				listeditor.setListModel(modelProducer.getListModel(row, column));

				Logging.info(this, "startValue set: " + value);

				listeditor.setSelectionMode(modelProducer.getSelectionMode(row, column));
				listeditor.setEditable(modelProducer.getEditable(row, column));
				listeditor.setNullable(modelProducer.getNullable(row, column));
				listeditor.setSelectedValues(modelProducer.getSelectedValues(row, column));

				listeditor.enter();
				startListEditor();

				editingRow = row;
				editingColumn = column;
			}

			else {
				model = new DefaultListModel<>();

				listeditor.setListModel(model);
				startListEditor();

				listeditor.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				listeditor.setEditable(true);
				listeditor.setSelectedValues(new ArrayList<>());

				listeditor.enter();
				listeditor.setStartValue("");

				editingRow = -1;
				editingColumn = -1;
			}

		}

		field.setText("" + value);

		return field;
	}

	@Override
	public Object getCellEditorValue() {

		if (listeditor.getValue() == null)
			return null;

		if (listeditor.getValue() instanceof List) {
			List list = (List) listeditor.getValue();

			if (forbiddenValues != null) {
				Iterator<Object> iter = forbiddenValues.iterator();
				while (iter.hasNext()) {
					Object element = iter.next();

					list.remove(element);
				}

			}

			int n = list.size();

			if (List.class.isAssignableFrom(modelProducer.getClass(editingRow, editingColumn))) {

				return list;
			}

			if (java.lang.Integer.class.isAssignableFrom(modelProducer.getClass(editingRow, editingColumn))) {

				if (n == 0)
					return null;

				return list.get(0);
			}

			if (java.lang.Boolean.class.isAssignableFrom(modelProducer.getClass(editingRow, editingColumn))) {

				if (n == 0)
					return null;

				return list.get(0);
			}

			if (n == 0)
				return "";

			StringBuilder buf = new StringBuilder("");

			for (int i = 0; i < n - 1; i++) {
				buf.append("" + list.get(i) + ",");
			}
			buf.append("" + list.get(n - 1));

			String result = buf.toString();

			if (result.equalsIgnoreCase("null"))
				return org.json.JSONObject.NULL;

			return result;

		}

		return listeditor.getValue();
	}

	public void finish() {
		if (listeditor != null)
			listeditor.deactivate();
	}

	// MouseListener for textfield
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == field && usingListEditor && e.getClickCount() > 1)
			listeditor.setVisible(true);
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
