/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.Vector;

public class GenericTableUpdateItemFactory extends TableUpdateItemFactory {
	protected Vector<String> columnNames;
	protected Vector<String> classNames;
	protected int keyCol;
	protected Object source;

	public GenericTableUpdateItemFactory(Object source, Vector<String> columnNames, Vector<String> classNames,
			int keyCol) {
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.keyCol = keyCol;
		this.source = source;
	}

	public GenericTableUpdateItemFactory(Vector<String> columnNames, Vector<String> classNames, int keyCol) {
		this(null, columnNames, classNames, keyCol);
	}

	public GenericTableUpdateItemFactory(int keyCol) {
		this(null, null, null, keyCol);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void setClassNames(Vector<String> classNames) {
		this.classNames = classNames;
	}

	public void setColumnNames(Vector<String> columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public TableEditItem produceUpdateItem(Vector oldValues, Vector rowV) {
		return new GenericTableUpdateItem(source, keyCol, columnNames, classNames, oldValues, rowV);
	}

	@Override
	public TableEditItem produceInsertItem(Vector rowV) {
		return new GenericTableInsertItem(source, keyCol, columnNames, classNames, rowV);
	}

	@Override
	public TableEditItem produceDeleteItem(Vector rowV) {
		return new GenericTableDeleteItem(source, keyCol, columnNames, classNames, rowV);
	}

}
