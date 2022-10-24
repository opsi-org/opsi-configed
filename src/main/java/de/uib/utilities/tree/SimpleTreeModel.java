package de.uib.utilities.tree;

import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import de.uib.utilities.*;
import de.uib.utilities.logging.*;

public class SimpleTreeModel extends DefaultTreeModel

{
	java.util.LinkedHashMap<String, Object> virtualLines;
	final static String noValue = "NONE";
	
	
	
	public final SimpleIconNode ROOT;
	
	Set<SimpleTreePath> allPathes; 
	Map<SimpleTreePath, SimpleIconNode> path2Node;
	//Map<SimpleTreePath, String> path2DottedString;
	
	Map<String, String> tooltips;
	
	
	public SimpleTreeModel(java.util.Set<String> dottedKeys)
	{
		this(dottedKeys, null);
	}
	
	public SimpleTreeModel(java.util.Set<String> dottedKeys, Map<String, String> tooltips) 
	{
		super(new SimpleIconNode(""));
		//setRootLabel("  (selected client/s)");
		logging.debug(this, "SimpleTreeModel created for " + dottedKeys);
		setAsksAllowsChildren(true);
		
		ROOT = (SimpleIconNode) getRoot();
		//ROOT.setIcon(de.uib.configed.Globals.createImageIcon("images/system-config.png","open table"));
		//logging.info(this, "tooltips " + tooltips);
		this.tooltips = tooltips;
		generateFrom(dottedKeys);
	}
	
	
	
	public TreeSet<String> getGeneratedKeys()
	{
		TreeSet<String> result = new TreeSet<String>();
		
		for (SimpleTreePath path : allPathes)
			result.add(path.dottedString(0, path.size()));
		
		return result;
	}
		
	
	public void setRootLabel(String s)
	{
		((DefaultMutableTreeNode)getRoot()).setUserObject(s);
	}
	
	protected void generateFrom(java.util.Set<String> dottedKeys)
	{
		allPathes = new TreeSet<SimpleTreePath>();
		path2Node = new TreeMap<SimpleTreePath, SimpleIconNode>();
		//path2DottedString = new TreeMap<SimpleTreePath, String>();
		
		if (dottedKeys != null)
		{
			for ( String key : dottedKeys) 
			{
				//logging.debug(this, "generateFrom key ------- " + key);
				String partialKey = "";
				String remainder = key;
				
				int j = -1;
				int k = remainder.indexOf('.');
				SimpleTreePath path = new SimpleTreePath();
				
				while (k > j)
				{
					String componentKey = key.substring(j+1, k);
					path.add(componentKey);
					allPathes.add(new SimpleTreePath(path));
					//logging.debug(this, "add " + path);
					
					partialKey = key.substring(0, k);
					remainder = key.substring(k+1);
					
					//logging.debug(this, "generateFrom partial " + partialKey);
					//logging.debug(this, "generateFrom remainder " + remainder);
					
					//path2DottedString.put(path, partialKey);
					
					j = k;
					k = j  + 1 + remainder.indexOf('.');
				}
				path.add(remainder);
				allPathes.add(path);
				//logging.debug(this, "add " + path);
				
			}
		}
		
		logging.debug(this, "generateFrom allPathes " + allPathes);
		
		
		
		for (SimpleTreePath path : allPathes)
		{
			SimpleIconNode parent = ROOT;
			
			for (int i = 1; i <= path.size(); i++)
			{
				if (i > 1)
					parent = path2Node.get(path.subList(0, i-1));
				
				SimpleTreePath partialPath = path.subList(0, i);
				SimpleIconNode node = path2Node.get(partialPath);
				
				if (node == null)
				//node must be created
				{
					node = new SimpleIconNode( path.get(i-1) );
					node.setIcon(de.uib.configed.Globals.createImageIcon("images/opentable_small.png","open table"));
					node.setNonSelectedIcon(de.uib.configed.Globals.createImageIcon("images/closedtable_small.png","closed table"));
					
					if (tooltips != null)
					{
							String key = partialPath.dottedString(0, partialPath.size());
							String description = tooltips.get( key );
							if (description == null || description.trim().equals(""))
								node.setToolTipText(key);
							else
								node.setToolTipText(description);
					}
					
					path2Node.put(path.subList(0, i), node);
					parent.add(node);
				}
			}
		}
		
		logging.debug(this, "generateFrom allPathes ready");
	}
	

	private static int startX = 10;
	private static int startY = 20;
		
	
	public void produce()
	//test method
	{
		
		
		/* java 1.7
		javax.swing.plaf.nimbus.NimbusLookAndFeel laf = new javax.swing.plaf.nimbus.NimbusLookAndFeel();
		UIManager.setLookAndFeel(laf);
		UIDefaults nimbUID = laf.getDefaults();
		nimbUID.put("Tree.drawHorizontalLines", true);
		nimbUID.put("Tree.drawVerticalLines", true);
		
		*/
		
		XTree tree = new XTree(this);
		//tree.putClientProperty("JTree.lineStyle", "Horizontal");
		
		tree.setCellRenderer(new SimpleIconNodeRenderer());
		
		tree.expandAll();
		//tree.setRootVisible(false);
		
				
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(tree);
		frame.setSize(240,240);
		frame.setLocation(startX, startY);
		frame.setVisible(true);
		startX = startX + 20;
		startY = startY + 20;
		
		
		
	}
			
	public static void main(String[] args)
	{
		logging.wantedDirectory = args[0];
		logging.AKT_DEBUG_LEVEL = logging.LEVEL_DEBUG;
		
		de.uib.configed.configed.configureUI();
		
		
		Set<String> example = new HashSet<String>(
			(Arrays.asList(new String[]{"configed", "configed.saved_search","opsiclientd"}) 
			)
		);
		
		SimpleTreeModel model = new SimpleTreeModel(example);
		model.produce();
		
		
		example = new HashSet<String>(
			(Arrays.asList(new String[]{"", 
			"configed.saved_search",
			"opsiclientd"}) 
				)
			);
		model = new SimpleTreeModel(example);
		model.produce();
		
		
		model = new SimpleTreeModel(example);
		model.produce();
		
		
		example = new HashSet<String>(
			(Arrays.asList(new String[]{"a1.b1.c1","a1.b2.d1", "a2.b1"}) 
				)
			);
		
		model = new SimpleTreeModel(example);
		model.produce();
	}
}
