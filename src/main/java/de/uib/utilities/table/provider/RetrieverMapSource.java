/* 
 *
 * 	uib, www.uib.de, 2009
 * 
 *	author Rupert Röder 
 *
 */

package de.uib.utilities.table.provider;

import java.util.ArrayList;
import java.util.List;

public class RetrieverMapSource extends MapSource
// the map is not given via a parameter but by a pointer to a function
{
	protected MapRetriever retriever;

	public RetrieverMapSource(List<String> columnNames, List<String> classNames, MapRetriever retriever,
			boolean rowCounting) {
		super(columnNames, classNames, null, rowCounting);
		this.retriever = retriever;
		rows = new ArrayList<>();
	}

	public RetrieverMapSource(List<String> columnNames, List<String> classNames, MapRetriever retriever) {
		this(columnNames, classNames, retriever, false);
	}

	@Override
	protected void fetchData() {
		table = retriever.retrieveMap();

		super.fetchData();
	}
}
