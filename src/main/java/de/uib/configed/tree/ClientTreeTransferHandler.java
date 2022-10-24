package de.uib.configed.tree;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import java.io.IOException;

import de.uib.utilities.logging.*;

public class ClientTreeTransferHandler extends TransferHandler 
{
	protected ClientTree tree;
	
	public ClientTreeTransferHandler(ClientTree tree)
	{
		super();
		this.tree = tree;
		//logging.debug(this, "transfer handler constructed");
		
	}
	
	
	private GroupNode transferRepresentsGroup(String treeRepresentation)
	{
		GroupNode result = null;

		if (treeRepresentation == null)
			return null;
		
		String[] parts = treeRepresentation.split(",");
		
		//logging.debug(this, "transferRepresentsGroup : "  + Arrays.toString(parts));
		
		if (parts.length == 1)
			return null;
		
		String node = parts[parts.length-1];
		result = tree.getGroupNode(node);
		
		logging.debug(this, "transferRepresentsGroup : " + treeRepresentation + ", result " + result);
		
		return result;
	}
	
	
	@Override
	public boolean canImport(TransferHandler.TransferSupport support) 
	{
		boolean result = true;
		logging.debug(this, "----------- can import ??");
		
		if (de.uib.configed.Globals.isGlobalReadOnly())
			return false;
		
		if (!support.isDataFlavorSupported(DataFlavor.stringFlavor) || !support.isDrop()) 
		{
			return result = false;
		}
		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		logging.debug(this, "ClientTreeTransferHandler, dropLocation.getPath() " + dropLocation.getPath());
		
		if (dropLocation.getPath() == null)
			return result = false;
		
		String transferData = null;
		
		
		try
		{
			transferData  = (String) support.getTransferable().getTransferData(  DataFlavor.stringFlavor );
			
		}
		catch (java.awt.datatransfer.UnsupportedFlavorException ex)
		{
			logging.warning(this, " unsupported data flavor " + ex);
		}
		catch (java.io.IOException ex)
		{
			logging.warning(this, " transferable io exception " + ex);
		}
		catch (Exception ex)
		{
			logging.warning(this, "canImport " + ex);
		}
		
		
		GroupNode sourceGroupNode = transferRepresentsGroup(transferData);
		Object[] sourceObjectPath = new Object[0];
		if (sourceGroupNode != null) 
			sourceObjectPath = sourceGroupNode.getUserObjectPath();
		
		DefaultMutableTreeNode dropOnThis =
			(DefaultMutableTreeNode) dropLocation.getPath().getLastPathComponent();
		String id = dropOnThis.getUserObject().toString();
		GroupNode targetNode = (GroupNode) tree.getGroupNode(id);
		TreePath dropPath = tree.getActiveTreePath(id);
		Object[] dropObjectPath = new Object[0]; 
		if (targetNode != null)
			dropObjectPath = targetNode.getUserObjectPath();
		
		
		//debugging the if clause
		//logging.info(this, "canImport, dropOnThis  as node " + targetNode 	 + " class " + targetNode);
		if (targetNode != null)
		{
			logging.debug(this, "canImport targetNode.isImmutable() " +  targetNode.isImmutable());
		}
		
		logging.debug(this, "canImport sourceGroupNode " + sourceGroupNode);
		if (sourceGroupNode != null && targetNode != null)
		{
			logging.debug(this, "canImport targetNode.allowsOnlyGroupChilds() " + targetNode.allowsOnlyGroupChilds());
			logging.debug(this, "canImport !targetNode.allowsSubGroups() " + !targetNode.allowsSubGroups());
		}
		if (sourceGroupNode == null && targetNode != null)
		{
			logging.debug(this, "canImport targetNode.allowsOnlyGroupChilds() " + targetNode.allowsOnlyGroupChilds());
		}
		
		logging.debug(this, "canImport, dropOnThis  path " + Arrays.toString(dropObjectPath));
		logging.debug(this, "canImport source path " + Arrays.toString(sourceObjectPath));
		
		if 
			(
				targetNode == null 
				||
				targetNode.isImmutable()
				||
				(sourceGroupNode != null // group 
					&& !targetNode.allowsSubGroups())
				||
				(sourceGroupNode == null // no group
					&& targetNode.allowsOnlyGroupChilds())
				||
				(sourceGroupNode != null // group
					&& sourceObjectPath.length > 1 && dropObjectPath.length > 1
					&& !(sourceObjectPath[1].equals(dropObjectPath[1]))
					//different group branchess
					)
				
			)
			
			result = false;
		
		logging.debug(this, "canImport, dropOnThis " + dropOnThis.getUserObject());
		
		logging.debug(this, "----------- can import " + result);
			
		return result;
	}
	
	@Override
	public int getSourceActions(JComponent c) 
	{
		//logging.debug(this," --- ClientTreeTransferHandler , getSourceActions, " 
		//	+ "JComponent " + c + " getSourceActions " + TransferHandler.COPY_OR_MOVE);
		//logging.debug(this, "getSourceActions,  selectedRows " + logging.getIntegers(tree.getSelectionRows() ));
		
		logging.debug(this, "getSourceActions,  activePaths " + tree.getActivePaths()) ;
		
		if ( tree.getActivePaths() == null || tree.getActivePaths().size() == 0 )
		{
			logging.debug(this, "getSourceActions no active pathes, TransferHandler.NONE"); 
			return TransferHandler.NONE;
		}
		
		
		DefaultMutableTreeNode dropThis = 
			(DefaultMutableTreeNode) tree.getActivePaths().get(0).getLastPathComponent(); 
		
		
		if (dropThis instanceof GroupNode)
		{
			GroupNode dropThisVariant = (GroupNode) (tree.getGroupNode(dropThis.toString())); 	
			
			if (dropThis != dropThisVariant)
			{
				logging.warning(this, "getSourceActions,  dropThis != dropThisVariant");
				logging.warning(this,"getSourceActions,  dropThis " + dropThis);
				logging.warning(this,"getSourceActions,  dropThisVariant " + dropThisVariant);
			}
			
			GroupNode parent = (GroupNode) dropThisVariant.getParent(); 
		
			logging.debug(this, "getSourceActions,  dropThis " + dropThis 
				+ " parent " +  parent);
		
			if (parent.isImmutable())
			{
				logging.debug(this, "getSourceActions dropObject is immutable, TransferHandler.NONE");
				return TransferHandler.NONE;
			}
		}
		
		
		String nodeString 
		= dropThis.getUserObject().toString();
		
		if (
			tree.getGroups().keySet().contains(nodeString)
			)
		{
			logging.debug(this, "getSourceActions object already there, TransferHandler.MOVE");
			return TransferHandler.MOVE;
		}
		
		//there can only be one group selected, and it can only be moved 
		//(for top groups the NONE handler was already returned)
		
		Iterator iterPaths = tree.getActivePaths().iterator();
		
		while (iterPaths.hasNext())
		{
			TreePath path = (TreePath) iterPaths.next();
			
			if ( tree.isChildOfALL ( (DefaultMutableTreeNode) path.getLastPathComponent() ) )
			{
				logging.debug(this, "getSourceActions path " + path + " childOfALL, should be TransferHandler.COPY");
				return TransferHandler.COPY;
				// we dont accept to move any item out of ALL
			}
			
			if (tree.isInDIRECTORY(path))
			{
				logging.debug(this, "getSourceActions , isInDIRECTORY true");
				//action depends additionally from target
				//return TransferHandler.COPY;
			}
		}
		
		logging.debug(this, "getSourceActions all remaining, TransferHandler.COPY_OR_MOVE");
		
		return TransferHandler.COPY_OR_MOVE;
	}
	
	
	@Override
	protected Transferable createTransferable(JComponent c) 
	{
		
		StringBuffer buff = new StringBuffer();
		Iterator iterPaths = tree.getActivePaths().iterator();
		
		while (iterPaths.hasNext())
		{
			TreePath path = (TreePath) iterPaths.next();
			
			//String id = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject().toString();
			
			
			int len = path.getPath().length;
			for (int j = 0; j < len; j++)
			{
				buff.append(path.getPath()[j]);
				if ( j < len -1)
					buff.append(",");
			}
			
			//buff.append(path.toString());
			
			if (iterPaths.hasNext())
				buff.append("\n");
		}
			
		return new StringSelection(buff.toString());
		
	}
	
	//@Override
	//public Icon getVisualRepresentation(Transferable t)
	
	
	private boolean chooseMOVE(
		TransferHandler.TransferSupport support,
		String sourceGroupName,
		TreePath dropPath,
		boolean isLeaf
		)
	{
		logging.info(this, "chooseMOVE  support " + support);
		logging.info(this, "chooseMOVE  sourceGroupName, dropPath " + sourceGroupName + " , " + dropPath);
		logging.info(this, "chooseMOVE support.getUserDropAction() == TransferHandler.MOVE " 
			+ (support.getUserDropAction() == TransferHandler.MOVE) 
			+ " support.getUserDropAction() " + support.getUserDropAction());
		
		boolean result = false;	
		
		boolean stayInsideDIRECTORY = tree.isInDIRECTORY(sourceGroupName) && tree.isInDIRECTORY(dropPath);
		boolean stayInsideGROUPS = tree.isInGROUPS(sourceGroupName) && tree.isInGROUPS(dropPath);
			
		logging.info(this, "chooseMOVE  stayInsideDIRECTORY,  stayInsideGROUPS " + stayInsideDIRECTORY + ", " +  stayInsideGROUPS);
		
		if 
			(
				stayInsideDIRECTORY 
				||  
				(stayInsideGROUPS && !isLeaf)
				//stayInsideDIRECTORY ||  stayInsideGROUPS
				//moving inside DIRECTORY or inside GROUPS
				
				//stayInsideDIRECTORY
				//inside DIRECTORY only moving 
			)
		{	
			result = true;
		}
		/*
		else
			if
			(
				support.getUserDropAction() == TransferHandler.MOVE
			)
			{
				result = true;
			}
		*/	
		
		logging.debug(this, "chooseMOVE  " + result);
		
		return result;
		
	}
			
	
	private void handleClientID(
		String importID,
		TransferHandler.TransferSupport support,
		TreePath sourcePath,
		GroupNode sourceParentNode,
		String sourceParentID,
		TreePath dropPath,
		DefaultMutableTreeNode dropParentNode,
		String dropParentID
		)
		
	{
		logging.debug(this,"handleClientID importID, sourcePath, sourceParentID, sourceParentNode, dropParentID,  " + importID 
			+ ", " + sourcePath + " , " +  sourceParentID + ", " + sourceParentNode + ", " +  dropParentID);
		
		boolean moving = false;
		
		String adaptedSourceParentID = sourceParentID;
		GroupNode adaptedSourceParentNode = sourceParentNode;
		
		if (sourcePath == null)
			//we are in table and did not get a real souce path
		{
			String  firstDIRECTORYgroupname = null;
			java.util.Set<GroupNode> locations = tree.getLocationsInDIRECTORY(importID);
			if (locations != null && locations.size() > 0)
			{
				logging.debug(this, "handleClientID tree.getLocationsInDIRECTORY 1");
				Iterator<GroupNode> iter = tree.getLocationsInDIRECTORY(importID).iterator();
				firstDIRECTORYgroupname = iter.next().toString();
				logging.debug(this, "handleClientID tree.getLocationsInDIRECTORY firstDIRECTORYgroupname " + firstDIRECTORYgroupname);
				adaptedSourceParentID = firstDIRECTORYgroupname;
				moving = chooseMOVE(support, firstDIRECTORYgroupname,  dropPath, true);
				
				adaptedSourceParentNode = tree.getGroupNode(adaptedSourceParentID);
			}
		}
		
		else
		{
			moving = chooseMOVE(support,  adaptedSourceParentID, dropPath, true);
		}
			
			
		tree.clientCopyOrMoveTo(
				importID,
				sourcePath,
				adaptedSourceParentID,
				adaptedSourceParentNode,
				
				dropParentNode,
				dropPath,
				dropParentID,
				
				moving
				);
		
	}
		
	
	
	@Override
	public boolean importData(TransferHandler.TransferSupport support) 
	{
		if (!canImport(support)) 
		{
			return false;
		}
		//we are at a group node 
		
		//where we want to move/copy to
		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		TreePath dropPath = dropLocation.getPath();
		
		
		DefaultMutableTreeNode dropParentNode = (DefaultMutableTreeNode) dropPath.getLastPathComponent();
		String dropParentID = dropParentNode.getUserObject().toString();
		
		logging.debug(this, "dropPath " + dropPath);
		
		//what is to be moved/copied
		Transferable transferable = support.getTransferable();
		String transferData = null;
		
		
		/*
			instead of the following code which retrieves the data from the clipboard 
			we use the data from the source data
			
		try 
		{
			//java.security.AccessController.checkPermission(new java.awt.AWTPermission("accessClipboard"));
			transferData = (String) transferable.getTransferData(DataFlavor.stringFlavor);
		} 
		catch (IOException e) {
			return false;
		} 
		catch (UnsupportedFlavorException e) {
			return false;
		}
		
		catch(java.security.AccessControlException ex)
		{
			logging.debug(this, "dropPath " + dropPath); 
			logging.logTrace(ex);
			
		}
		
		catch(Exception exx)
		{
			logging.logTrace(exx);
			return false;
		}
		
		logging.debug(this, " transferData " + transferData.toString());
		
		String[] values = transferData.split("\n");
		*/
		

		
		//logging.debug(this, "importData, getSelectedClientsInTable(): " + tree.getSelectedClientsInTable());
		logging.debug(this, "importData. ++++++++++ getActivePaths(): " + tree.getActivePaths());
		
		String[] values = tree.getSelectedClientsInTable().toArray(new String[]{});
		
		if (values.length == 0)
		// possibly transfer of a group node
		{
			ArrayList<TreePath> activePaths = tree.getActivePaths();
			if (activePaths != null && activePaths.size() == 1)
			{
				
				String importID = (String) 
					(
					((DefaultMutableTreeNode) 
					(activePaths.get(0)).getLastPathComponent()).getUserObject()
					);
				values = new String[]{importID};
			}
		}
		
		logging.debug(this, "importData, ------------- values " + logging.getStrings(values));
		
		TreePath groupPathActivatedByTree = tree.getGroupPathActivatedByTree();
		logging.debug(this, "importData,  ++++++++++++++++ groupPathActivatedByTree " + groupPathActivatedByTree);
		
		// if the source is the tree then we arranged lines for the transfer
		// the other possible source are lines from the JTable, as well arranged to lines

		// Perform the actual import.  
        for (int i = 0; i < values.length; i++)
        {
        		String value = values[i];
        		
        		String importID = null;
        		String sourceParentID = null;
        		String sourceParentID_in_tree = null;
        		TreePath oldPath = null;
        		try
        		{
        			// if values not got from transferable, the following reduces 
        			// to setting importId = value;
        			
        			//logging.debug(this, "transferData split by tab:" + logging.getStrings(value.split("\t")));
        			//logging.debug(this, "transferData split by ,:" + logging.getStrings(value.split(",")));
        			//logging.debug(this, "transferData split by ' ' " + logging.getStrings(value.split(" ")));
        			
        			if ((value.split("\t").length > 1))
        			//probably an import from the JTable
        			{
        				//we assume a table source with first fieldvalue being a clientID
        				importID = value.split("\t")[0];
        			}
        			else
        			{
					String[] parts = value.split(",");
					//oldPath = new TreePath(parts);
					importID = parts[parts.length-1];
					
					/*
					if (parts.length > 1) 
						//probably our own transfer
						sourceParentID = parts[parts.length-2];
					//logging.debug(this, "got importID " + importID + ", sourceParentID " + sourceParentID);
					*/
				}
        			
        		}
        		catch(Exception ex)
        		{
        			logging.info(this, " no tree parts got " + ex);
        		}
        		
        		//importID; //= values[i].split("\t")[0]; for table lines
        		
        		//if (oldPath == null)
        		//	continue;
        		
        		logging.debug(this, "importData  ----------------   " + i + " values[i] " + importID);
        		
        		TreePath sourcePath = tree.getActiveTreePath(importID);
        		logging.debug(this," active source tree path ++++++++++++++ for importID " + importID + ": " + sourcePath);
        		
        		GroupNode sourceParentNode = null;
        		GroupNode groupNode =  null;
        		
        		if (sourcePath != null) 
        		{
        			sourceParentID =  (String) ((DefaultMutableTreeNode) sourcePath.getParentPath().getLastPathComponent()).getUserObject();
        			sourceParentNode = tree.getGroupNode(sourceParentID);
        			groupNode =  tree.getGroupNode(importID);
        		}
        			
        		else
        		//coming from table, replace!
        		{
        			//sourceParentID = tree.ALL_NAME;
				//sourceParentNode = tree.ALL;
        		}
        		
        		
        		logging.debug(this,"importData, sourceParentID " + sourceParentID);
        		logging.debug(this, "importData, sourceParentNode " + sourceParentNode);
        		logging.debug(this, "importData, groupNode " + groupNode );
        		
        		if ( groupNode != null  )
        		{
        			//it is a group and it could be moved
        			if (chooseMOVE(support, sourceParentID, dropPath, false))
						// it is a group, and it will be moved, but only inside one partial tree
				{
						tree.moveGroupTo( 
							importID,
							groupNode,
							sourceParentNode,
							dropParentNode,
							dropPath,
							dropParentID
						);
						
        			}
        			else
        			{
        				logging.info(this, "importData: this group will not be moved");
        			}
        			
        		}
        		else
        		{
        			// client node
        			logging.debug(this,"importData handling client ID " + importID);
        			
        			handleClientID(importID,
        				support,
        				sourcePath,
        				sourceParentNode,
        				sourceParentID,
        				dropPath,
        				dropParentNode,
        				dropParentID
        				);
        				
        		}
        		
        		logging.debug(this, "importData  ----------------  ready " + i + " importID " + importID); 
        				
        }
        				
		return true;
	}
	

	
	@Override
	public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException
	{
		logging.debug(this, " exportToClipboard " + comp + " , " + clip + ", " + action); 
		super.exportToClipboard(comp, clip, action);
	}
		
	
	/*
	@Override
	protected void exportDone(JComponent c, Transferable data, int action) 
	{
		//cleanup(c, action == TransferHandler.MOVE);
	}
	
	//If the remove argument is true, the drop has been
    //successful and it's time to remove the source node
   protected void cleanup(JComponent c, boolean remove)
   {
   	   
   }
   */
}

