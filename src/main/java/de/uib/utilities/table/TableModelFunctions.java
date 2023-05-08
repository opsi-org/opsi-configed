/* 
 *
 * 	uib, www.uib.de, 2011-2012
 * 
 *	author Rupert Röder
 *
 */

package de.uib.utilities.table;

import java.util.List;
import java.util.Map;

import de.uib.utilities.Mapping;

public interface TableModelFunctions {
	public class PairOfInt {
		public final int col1;
		public final int col2;

		PairOfInt(int col1, int col2) {
			this.col1 = col1;
			this.col2 = col2;
		}
	}

	Map<Object, List<Object>> getFunction(int col1, int col2);

	Map<Integer, RowStringMap> getPrimarykey2Rowmap();

	Map<Integer, Mapping<Integer, String>> getID2Mapping(int col1st, int col2nd,
			Mapping<Integer, String> col2ndMapping);

}
