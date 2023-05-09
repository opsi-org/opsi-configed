/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class GenericTableUpdateItemFactory implements TableUpdateItemInterface {
	private List<String> columnNames;
	private List<String> classNames;
	private int keyCol;
	private Object source;

	public GenericTableUpdateItemFactory(Object source, List<String> columnNames, List<String> classNames, int keyCol) {
		this.columnNames = columnNames;
		this.classNames = classNames;
		this.keyCol = keyCol;
		this.source = source;
	}

	public GenericTableUpdateItemFactory(List<String> columnNames, List<String> classNames, int keyCol) {
		this(null, columnNames, classNames, keyCol);
	}

	public GenericTableUpdateItemFactory(int keyCol) {
		this(null, null, null, keyCol);
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public void setClassNames(List<String> classNames) {
		this.classNames = classNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	@Override
	public TableEditItem produceUpdateItem(List<Object> oldValues, List<Object> rowV) {
		return new MapBasedTableEditItem(source, keyCol, columnNames, rowV);
	}

	@Override
	public TableEditItem produceInsertItem(List<Object> rowV) {
		return new GenericTableInsertItem(source, keyCol, columnNames, classNames, rowV);
	}

	@Override
	public TableEditItem produceDeleteItem(List<Object> rowV) {
		return new GenericTableDeleteItem(source, keyCol, columnNames, classNames, rowV);
	}

}
