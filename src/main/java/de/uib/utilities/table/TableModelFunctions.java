/* 
 *
 * 	uib, www.uib.de, 2011-2012
 * 
 *	author Rupert Röder
 *
 */

package de.uib.utilities.table;

import java.util.List;

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

	java.util.Map<Object, List<Object>> getFunction(int col1, int col2);

	java.util.Map<Integer, RowStringMap> getPrimarykey2Rowmap();

	void setKeyRepresenter(de.uib.utilities.table.KeyRepresenter kr);
	// must be set to get the following methods to work
	// assumes that a keycol is set

	java.util.Map<Integer, String> getPrimarykeyTranslation();
	// the defining map for the Mapping of getPrimarykeyRepresentation

	de.uib.utilities.Mapping<Integer, String> getPrimarykeyRepresentation();

	abstract java.util.Map<Integer, Mapping<Integer, String>> getID2Mapping(int col1st, int col2nd,
			Mapping<Integer, String> col2ndMapping);

}
