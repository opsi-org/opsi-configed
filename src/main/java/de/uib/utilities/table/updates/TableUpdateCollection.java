/* 
 *
 * 	uib, www.uib.de, 2008-2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.updates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.uib.utilities.table.GenTableModel;

public class TableUpdateCollection extends ArrayList<TableEditItem> {
	Set<GenTableModel> modelsToReload = new HashSet<>();
}
