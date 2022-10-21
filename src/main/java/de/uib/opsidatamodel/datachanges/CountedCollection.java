package de.uib.opsidatamodel.datachanges;

import java.util.*;

interface CountedCollection extends Collection
{
   int accumulatedSize();
}
