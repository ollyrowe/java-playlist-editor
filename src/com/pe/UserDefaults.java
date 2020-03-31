package com.pe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Class that creates and interacts with defaults.conf file that stores useful user default values.
 * 
 * @author olly.rowe
 *
 */
public class UserDefaults
{
	// The defaults.conf file name
	private static final String FILE_NAME = "defaults.conf";
	
	// The defaults file
	private File file;
	
	// The default directory for the 'Browse for Music' section
	private String defaultBrowserDir;
	
	// The default directoy when browsing to open a playlist
	private String defaultOpenPlaylistDir;
	
	/**
	 * Class Constructor. Will detect existing defaults.conf file and read it.
	 */
	public UserDefaults()
	{
		// Initialise the defaults file
		this.file = new File(FILE_NAME);
		
		// If the file exists, then attempt to parse it
		if (this.file.exists())
		{
			try
			{
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(this.file);
				
				Node defaultBrowserDirNode = doc.getElementsByTagName("defaultBrowserDir").item(0);
				
				// Set the default browser dir variable if the text content isn't empty
				this.defaultBrowserDir = (defaultBrowserDirNode.getTextContent().equals("")) ? null : defaultBrowserDirNode.getTextContent();
				
				Node defaultOpenPlaylistDirNode = doc.getElementsByTagName("defaultOpenPlaylistDir").item(0);
				
				// Set the default open playlist dir variable if the text content isn't empty
				this.defaultOpenPlaylistDir = (defaultOpenPlaylistDirNode.getTextContent().equals("")) ? null : defaultOpenPlaylistDirNode.getTextContent();
			}
			catch (NullPointerException e)
			{
				// If a null pointer error is raised then the file is missing an item of data, ignore this
				return;
			}
			catch (ParserConfigurationException | SAXException | IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Getter for default browser directory string value.
	 * 
	 * @return The String value of the default browser directory.
	 */
	public String getDefaultBrowserDir()
	{
		return this.defaultBrowserDir;
	}
	
	/**
	 * Setter for the default browser directory.
	 * 
	 * @param defaultBrowserDir - The string value of the path of the default browser directory.
	 */
	public void setDefaultBrowserDir(String path)
	{
		this.defaultBrowserDir = path;
	}
	
	/**
	 * Getter for the default open playlist directory string value.
	 * 
	 * @return The String value of the default directory for opening a playlist.
	 */
	public String getDefaultOpenPlaylistDir()
	{
		return this.defaultOpenPlaylistDir;
	}
	
	/**
	 * Setter for the default open playlist directory.
	 * 
	 * @param path - The string value of the path of the default browser directory.
	 */
	public void setDefaultOpenPlaylistDir(String path)
	{
		this.defaultOpenPlaylistDir = path;
	}
	
	/**
	 * Saves the current defaults to the defaults.conf file.
	 */
	public void saveDefaultsToFile()
	{
        // Attempt to parse defaults.conf file
        try
        {
        	// Adapted from: https://examples.javacodegeeks.com/core-java/xml/parsers/documentbuilderfactory/create-xml-file-in-java-using-dom-parser-example/
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            
            Element defaultsElement = document.createElement("Defaults");
            
            document.appendChild(defaultsElement);
            
            Element defaultBrowserDirElement = document.createElement("defaultBrowserDir");

            if (this.defaultBrowserDir != null)
            {
                defaultBrowserDirElement.appendChild(document.createTextNode(this.defaultBrowserDir));   
            }
            
            defaultsElement.appendChild(defaultBrowserDirElement);
            
            Element defaultOpenPlaylistDirElement = document.createElement("defaultOpenPlaylistDir");
            
            if (this.defaultOpenPlaylistDir != null)
            {
            	defaultOpenPlaylistDirElement.appendChild(document.createTextNode(this.defaultOpenPlaylistDir));
            }
            
            defaultsElement.appendChild(defaultOpenPlaylistDirElement);

            // Create new transformation objects
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource domSource = new DOMSource(document);
            // Create a new StringWriter object to output the result to
            StringWriter outWriter = new StringWriter();
            // The stream result, outputting to the String Writer
            StreamResult streamResult = new StreamResult(outWriter);
            // Transform the DOM Object to an XML File
            transformer.transform(domSource, streamResult);
            StringBuffer sb = outWriter.getBuffer();
            
            // Write the output to the file
			PrintWriter pw = new PrintWriter(this.file.getAbsolutePath());
			pw.println(sb.toString());
			pw.close();
        }
        catch (ParserConfigurationException | TransformerException | FileNotFoundException e)
        {
            e.printStackTrace();
        }
	}
}
