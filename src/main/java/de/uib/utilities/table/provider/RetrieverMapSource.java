/* 
 *
 * 	uib, www.uib.de, 2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;

public class RetrieverMapSource extends MapSource
// the map is not given via a parameter but by a pointer to a function
{
	protected MapRetriever retriever;

	public RetrieverMapSource(ArrayList<String> columnNames, ArrayList<String> classNames, MapRetriever retriever,
			boolean rowCounting) {
		super(columnNames, classNames, null, rowCounting);
		this.retriever = retriever;
		rows = new ArrayList();
	}

	public RetrieverMapSource(ArrayList<String> columnNames, ArrayList<String> classNames, MapRetriever retriever) {
		this(columnNames, classNames, retriever, false);
	}

	protected void fetchData() {
		table = retriever.retrieveMap();
		// logging.debug ( " -------- RetrieverMapSource fetchData() : " + table);
		super.fetchData();
	}
}
