/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.ArrayList;

public class GenericTableUpdateItemFactory extends TableUpdateItemFactory {
	protected ArrayList<String> columnNames;
	protected ArrayList<String> classNames;
	protected int keyCol;
	protected Object source;

	public GenericTableUpdateItemFactory(Object source, ArrayList<String> columnNames, ArrayList<String> classNames,
			int keyCol) {
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.keyCol = keyCol;
		this.source = source;
	}

	public GenericTableUpdateItemFactory(ArrayList<String> columnNames, ArrayList<String> classNames, int keyCol) {
		this(null, columnNames, classNames, keyCol);
	}

	public GenericTableUpdateItemFactory(int keyCol) {
		this(null, null, null, keyCol);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void setClassNames(ArrayList<String> classNames) {
		this.classNames = classNames;
	}

	public void setColumnNames(ArrayList<String> columnNames) {
		this.columnNames = columnNames;
	}

	public TableEditItem produceUpdateItem(ArrayList oldValues, ArrayList rowV) {
		return new GenericTableUpdateItem(source, keyCol, columnNames, classNames, oldValues, rowV);
	}

	public TableEditItem produceInsertItem(ArrayList rowV) {
		return new GenericTableInsertItem(source, keyCol, columnNames, classNames, rowV);
	}

	public TableEditItem produceDeleteItem(ArrayList rowV) {
		return new GenericTableDeleteItem(source, keyCol, columnNames, classNames, rowV);
	}

}
