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

public class TableUpdateCollection extends ArrayList<TableEditItem> {

	HashSet<de.uib.utilities.table.GenTableModel> modelsToReload = new HashSet<>();

}
