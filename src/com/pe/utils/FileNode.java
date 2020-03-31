package com.pe.utils;

import java.io.File;
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Represents a node within the file tree.
 */
@SuppressWarnings("serial")
public class FileNode extends DefaultMutableTreeNode
{
	// The corresponding file
	private File file;
	
	// Boolean value identifying whether this node has yet been mapped
	private boolean isMapped = false;

	// The icon associated with this node that is called when rendering
	private Icon icon;
	
	/**
	 * Class constructor.
	 * 
	 * @param file - The associated file to this node.
	 */
	public FileNode(File file)
	{
		// Call to super constructor. Pass either the name of the file or, for the root, pass the path
		super(file.getName().equals("") ? file.getAbsolutePath() : file.getName());
				
		this.file = file;
	}
	
	/**
	 * Gets the file.
	 * 
	 * @return The corresponding file to this node.
	 */
	public File getFile()
	{
		return this.file;
	}
	
	/**
	 * Sets the state of isMapped.
	 * 
	 * @param isMapped Whether this node is mapped.
	 */
	public void setMapped(boolean isMapped)
	{
		this.isMapped = isMapped;
	}
	
	/**
	 * Returns whether this node is yet mapped.
	 * 
	 * @return State of isMapped attribute.
	 */
	public boolean isMapped()
	{
		return this.isMapped;
	}
	
	/**
	 * Gets the associated icon.
	 * 
	 * @return the associated icon.
	 */
	public Icon getIcon()
	{
		return this.icon;
	}
	
	/**
	 * Sets the associated icon.
	 * 
	 * @param icon - the icon to be set.
	 */
	public void setIcon(Icon icon)
	{
		this.icon = icon;
	}
}