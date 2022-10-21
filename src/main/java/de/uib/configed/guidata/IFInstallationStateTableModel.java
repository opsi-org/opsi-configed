package de.uib.configed.guidata;

import java.util.*;
import javax.swing.table.*;
import javax.swing.event.*;
import de.uib.utilities.*;
import de.uib.opsidatamodel.productstate.*;
import java.util.function.*;

public interface IFInstallationStateTableModel extends TableModel, ComboBoxModeller
{
		public int getColumnIndex(String columnName);
			
		public void clearCollectChangedStates();
	
		public String getLastStateChange(int row);
		
		public  Map<String, Map<String, Object>> getGlobalProductInfos();
		
		//not used public void setActionRequestWithCondition(ActionRequest ar,  IntPredicate rowCondition);
		
		public boolean infoIfNoClientsSelected();
			
		public void initCollectiveChange();
		
		public void collectiveChangeActionRequest( String productId, ActionRequest ar );
		
		public void finishCollectiveChange();
		
		//public void reset();
}		

