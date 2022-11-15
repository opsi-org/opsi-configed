package de.uib.utilities.table.gui;

import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

import de.uib.opsidatamodel.modulelicense.LicensingInfoMap;
import de.uib.utilities.logging.logging;
import de.uib.utilities.table.GenTableModel;

public class LicensingInfoPanelGenEditTable extends PanelGenEditTable{

    //LicensingInfoMap lInfoMap;

    public LicensingInfoPanelGenEditTable(String title, int maxTableWidth,
			boolean editing, 
			int generalPopupPosition, 	//if -1 dont use a standard popup
											//if > 0 the popup is added later after installing another popup
			boolean switchLineColors, 
			int[] popupsWanted,
			boolean withTablesearchPane)
    {
        super(title, maxTableWidth, editing, generalPopupPosition, switchLineColors, popupsWanted, withTablesearchPane);
        //lInfoMap = LicensingInfoMap.getInstance();
        //theTable.getTableHeader().setDefaultRenderer(new MarkBoldHeaderCellRenderer(lInfoMap));

    }
    
    @Override
    protected void setCellRenderers()
    {
    	//logging.info(this, "setCellRenderers");
    	LicensingInfoMap lInfoMap = LicensingInfoMap.getInstance();
        for (int i = 0; i < tableModel.getColumnCount(); i++)
		{
			Class cl = tableModel.getColumnClass(i);
			String name = tableModel.getColumnName(i);
			TableColumn col = theTable.getColumn(name);
			String classname = tableModel.getClassNames().get(i);

			col.setCellRenderer(new LicensingInfoTableCellRenderer( lInfoMap ) );
        
		}
    }

    @Override 
    public void reload()
    {
        //logging.info(this, "LicensingInfoPanel reload here ");
        super.reload();
    }

    public void setMarkBoldHeaderCellRenderer()
    {
        theTable.getTableHeader().setDefaultRenderer(new MarkLatestDateBoldHeaderCellRenderer(theTable.getTableHeader().getDefaultRenderer() ,
        	LicensingInfoMap.getInstance()) );
        theTable.getTableHeader().setReorderingAllowed(false);
    }
}
