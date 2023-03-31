package de.uib.opsidatamodel.datachanges;

import java.util.Collection;

interface CountedCollection extends Collection<UpdateCommand> {
	int accumulatedSize();
}
