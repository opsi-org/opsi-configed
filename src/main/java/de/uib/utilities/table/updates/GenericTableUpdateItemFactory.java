/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.List;

public class GenericTableUpdateItemFactory extends TableUpdateItemFactory {
	protected List<String> columnNames;
	protected List<String> classNames;
	protected int keyCol;
	protected Object source;

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
	public TableEditItem produceUpdateItem(List oldValues, List rowV) {
		return new GenericTableUpdateItem(source, keyCol, columnNames, classNames, oldValues, rowV);
	}

	@Override
	public TableEditItem produceInsertItem(List rowV) {
		return new GenericTableInsertItem(source, keyCol, columnNames, classNames, rowV);
	}

	@Override
	public TableEditItem produceDeleteItem(List rowV) {
		return new GenericTableDeleteItem(source, keyCol, columnNames, classNames, rowV);
	}

}
