package com.pe.utils;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

/**
 * FileTree.java
 * 
 * Represents a JTree that displays a given file system.
 * 
 * Threading has been utilised to map the file system using a breadth-first search.
 * Nodes can also be mapped on an as-needed bases to avoid long loading times.
 */
@SuppressWarnings("serial")
public class FileTree extends JTree implements TreeExpansionListener
{	
	// The array of file types to be displayed in the tree
	private ArrayList<String> fileTypesToShow;
	
	/**
	 * Class constructor.
	 * 
	 * @param directory - String value of the path of the directory to be set.
	 * @param fileTypesToShow - array of strings indicating file types to be included.
	 */
	public FileTree(String directory, List<String> fileTypesToShow)
	{
		super(new FileNode(new File(directory)));
				
		this.fileTypesToShow = new ArrayList<String>(fileTypesToShow);
				
		// Map the root node
		this.mapNodes((FileNode) this.getModel().getRoot());
						
		// Add tree expansion listener
		this.addTreeExpansionListener(this);
		
		// Set the cell renderer for assignment of node icons
		this.setCellRenderer(new DefaultTreeCellRenderer()
		{
			@Override
		    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		    {
		    	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		    	
		    	FileNode node = (FileNode) value;
		    	
		    	// If an icon has been set for this node then set it
		    	if (node.getIcon() != null)
		    	{
			    	setIcon(node.getIcon());
		    	}
				return this;
			}
		});
	}
	
	/**
	 * Alternative Class Constructor. By default, will display all file types.
	 * 
	 * @param directory - String value of the path of the directory to be set.
	 */
	public FileTree(String directory)
	{
		this(directory, null);
	}
	
	/**
	 * Maps a node, discovers and creates child nodes.
	 * 
	 * @param parent - the node to be mapped.
	 */
	private void mapNodes(FileNode node)
	{
		// List all child files / directories within parent node
		File[] children = node.getFile().listFiles();
		
		if (children != null)
		{
			// Iterate through each child
			for (File child : children)
			{
				if (child.isFile())
				{
					if (!this.isValidType(child))
					{
						continue;
					}
				}
				// Create a new node
				FileNode childNode = new FileNode(child);
				// Add the new node to the parent node
				node.add(childNode);
				
				// If this child node is a directory
				if (child.isDirectory())
				{
					// Set the icon of the child not to the default folder
					childNode.setIcon(UIManager.getIcon("Tree.openIcon"));
				}
			}
		}
	}
	
	/**
	 * Fired upon expansion of a node. Maps the children of the children of the expanded node.
	 * 
	 * @param e - The event object
	 */
	@Override
	public void treeExpanded(TreeExpansionEvent e)
	{
		// Fetch the last selected node
		FileNode node = (FileNode) e.getPath().getLastPathComponent();
		
		// Check if the node is not yet mapped
		if (!node.isMapped())
		{
			// Iterate through each child node
			for (int i = 0; i < node.getChildCount(); i++)
			{
				FileNode child = (FileNode) node.getChildAt(i);
				// Map the child's children
				this.mapNodes(child);
			}
		}
		// Set the node to mapped
		node.setMapped(true);
	}
	
	/**
	 * Fired upon collapsing of a node.
	 * 
	 * @param e - The event object
	 */
	@Override
	public void treeCollapsed(TreeExpansionEvent e)
	{
		return;
	}
	
	/**
	 * Changes the directory.
	 * 
	 * @param directory - String value of the path of the directory to be set.
	 */
	public void setDirectory(String directory)
	{
		// Get the tree model
		DefaultTreeModel model = (DefaultTreeModel) this.getModel();		
        // Set the new root node
        model.setRoot(new FileNode(new File(directory)));
        // Refresh the GUI
        model.reload();
        // Map the child nodes of the new root
		this.mapNodes((FileNode) this.getModel().getRoot());
	}
	
	/**
	 * Check that the file's type is allow by the file type filters. If there are not filters then it shall return true.
	 * 
	 * @return - the Boolean value of whether the file's type is valid or not.
	 */
	private boolean isValidType(File file)
	{
		if (this.fileTypesToShow != null)
		{
			// Iterate through each valid file type
			for (String fileType : this.fileTypesToShow)
			{
				// If the file type is valid
				if (file.getName().endsWith(fileType))
				{
					return true;
				}
			}
		}
		// If the file types to show array was null or the for loop did not return true at any point then return false
		return false;
	}
}

/**
 * FilteredFileTree.
 * 
 * An extension of the FileTree class that allows the user to pass an array list of String that specify the file types to be displayed in the file tree.
 * 
 * The performance of this current implementation is low and requires threading to be implemented to fix this issue.
 */
@SuppressWarnings("serial")
class FilteredFileTree extends FileTree
{
	// Filters for any specific file types to be displayed
	private ArrayList<String> fileTypesFilter;
	
	/**
	 * Class Constructor.
	 * 
	 * @param directory - String value of the path of the directory to be set.
	 * @param fileTypesFilter - String array containing the types of files to be shown in the file tree.
	 */
	public FilteredFileTree(String directory, ArrayList<String> fileTypesFilter)
	{
		super(directory);
		
		this.fileTypesFilter = fileTypesFilter;
	}
	
	/**
	 * Maps the child node into this current tree. Filtering out unspecified file types.
	 * 
	 * @param parent - the parent file node.
	 */
	@SuppressWarnings("unused")
	private void mapNodes(FileNode parent)
	{
		// List all child files / directories within parent node
		File[] children = parent.getFile().listFiles();
		
		if (children != null)
		{
			// Iterate through each child
			for (File child : children)
			{
				// If the child is a file then perform type validation check
				if (child.isFile())
				{
					// Ensure that the child is of one of the types specified in the type filters. If not then continue for loop.
					if (!isValidType(child))
					{
						continue;
					}
				}
				// Create a new node
				FileNode node = new FileNode(child);
				// Add the new node to the parent node
				parent.add(node);
			}
		}
	}
	
	/**
	 * Check that the file's type is allow by the file type filters. If there are not filters then it shall return true.
	 * 
	 * @return - the Boolean value of whether the file's type is valid or not.
	 */
	private boolean isValidType(File file)
	{
		if (this.fileTypesFilter == null)
		{
			return true;
		}
		else
		{
			// Iterate through each valid file type
			for (String fileType : this.fileTypesFilter)
			{
				// If the file type is valid
				if (file.getName().endsWith(fileType))
				{
					return true;
				}
			}
			// If the for loop did not return true at any point then the file type is invalid.
			return false;
		}
	}
}
