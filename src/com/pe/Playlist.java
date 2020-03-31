package com.pe;

import java.util.ArrayList;
import java.util.Collections;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Represents a playlist.
 * 
 * @author olly.rowe
 */
public class Playlist
{
	// The playlist attributes
	private String name;
	private String author;
	private ArrayList<Song> songs;
	
	// The file to where the playlist is stored
	private File file;
	
	// Parser constants
	public static final String WPL = "wpl";
	public static final String M3U = "m3u";
	private static final String WPL_FILE_PREFIX = "<?wpl version=\"1.0\"?>";
	
	/**
	 * Class Constructor for new playlist.
	 */
	public Playlist()
	{
		this.name = new String();
		this.author = new String();
		this.songs = new ArrayList<Song>();
	}
	
	/**
	 *  Alternative Class Constructor for existing files.
	 *  
	 *  @param File - The file containing the playlist data.
	 */
	public Playlist(File file) throws FileNotFoundException
	{
		// Set the file
		this.file = file;
		
		// Initialise the songs array
		this.songs = new ArrayList<Song>();
				
		// Check that the correct file type as been passed
		if (file.getName().endsWith(".wpl"))
		{
			// Try to read the data from the file
			try
			{
				DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(file);
				Node titleNode = doc.getElementsByTagName("title").item(0);
												
				this.name = titleNode.getTextContent();
				
				Node authorNode = doc.getElementsByTagName("author").item(0);
				
				this.author = authorNode.getTextContent();
												
				NodeList mediaNodes = doc.getElementsByTagName("media");
				
				for (int i = 0; i < mediaNodes.getLength(); i++)
				{										
					Song song = new Song(mediaNodes.item(i).getAttributes().getNamedItem("src").getTextContent());
										
					this.songs.add(song);
				}
			}
			catch (FileNotFoundException e)
			{
				throw new FileNotFoundException();
			}
			catch (ParserConfigurationException | SAXException | IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.err.println("Playlist initialisation error. Unsupportted file format detected.");
		}
	}
	
	/**
	 * Calls the corresponding parser and writes the output to the file
	 */
	public void save(String fileFormat)
	{
		// Initialise output string
		String output = "";
		
		// Choose the correct parser based on the passed file format value
		switch(fileFormat)
		{
			case WPL:
			{
				output = this.parseToWPL();
				break;
			}
			case M3U:
			{
				break;
			}
			default:
			{
				System.err.println("Failed attempt to save playist. Unsupported file format: " + fileFormat);
				return;
			}
		}
		
		// Write the output to the file
		try
		{
			PrintWriter pw = new PrintWriter(this.getFile().getAbsolutePath());
			pw.println(output);
			pw.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Parser for WPL file format.
	 */
	public String parseToWPL()
	{
		// Initialise the output string
        String output = WPL_FILE_PREFIX;
        
        // Attempt to parse playlist
        try
        {
        	// Adapted from: https://examples.javacodegeeks.com/core-java/xml/parsers/documentbuilderfactory/create-xml-file-in-java-using-dom-parser-example/
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            
            Element smil = document.createElement("smil");
            document.appendChild(smil);
 
            Element head = document.createElement("head");
 
            smil.appendChild(head);
            
            Element title = document.createElement("title");
            
            title.appendChild(document.createTextNode(this.getName()));
            
            head.appendChild(title);
            
            Element author = document.createElement("author");
            
            author.appendChild(document.createTextNode(this.getAuthor()));
            
            head.appendChild(author);
            
            Element body = document.createElement("body");
            
            smil.appendChild(body);
            
            Element seq = document.createElement("seq");
            
            body.appendChild(seq);
                        
            for (Song song : this.songs)
            {
            	// Create a new media tag for the song
            	Element media = document.createElement("media");
            	
            	// Set the src attribute to the absolute path of its corresponding file location
            	media.setAttribute("src", song.getFile().getAbsolutePath());
            	// Set the albumTitle attribute
            	media.setAttribute("albumTitle", song.getAlbum());
            	// Set the albumArtist attribute
            	media.setAttribute("albumArtist", song.getArtist());
            	// Set the trackTitle attribute
            	media.setAttribute("trackTitle", song.getTitle());
            	// Set the trackArtist attribute
            	media.setAttribute("trackArtist", song.getArtist());
            	// Set the duration attribute
            	media.setAttribute("duration", Long.toString(song.getLength()));
            	
            	// Add the media tag to the seq tag
            	seq.appendChild(media);
            }

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
            // Output result to the output string
            output += sb.toString();
        }
        catch (ParserConfigurationException | TransformerException e)
        {
            e.printStackTrace();
        }
        return output;
    }
	
	/**
	 * Identifies whether this playlist is currently saved.
	 * 
	 * Also returns true in the special case where the playlist details are completely blank.
	 * 
	 * @return - Whether the playlist is saved.
	 */
	public boolean isSaved()
	{
		// Check whether the detials are blank
		if (this.file == null && this.author.equals("") && this.name.equals("") && this.songs.size() == 0)
		{
			// If all details are blank then there is no need to save, therefore return true in this case
			return true;
		}
		
		// Check if this playlist has a file associated with it yet
		if (this.file == null)
		{
			// If there is not an associated file then it has not yet been saved
			return false;
		}
		// Parse the current state of this playlist
		String parserOutput = this.parseToWPL();
		
		// Read the corresponding file
		String fileOutput = new String();
		try
		{
			fileOutput = Files.readAllLines(this.file.toPath()).get(0);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		// Compare the file's contents with the parser output and then return whether they match or not
		if (parserOutput.equals(fileOutput))
		{
			return true;
		}
		return false;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAuthor()
	{
		return this.author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public ArrayList<Song> getSongs()
	{
		return this.songs;
	}
	
	public Song getSong(int index)
	{
		// Check that the index is valid
		if (index <= (this.songs.size() - 1) && index >= 0)
		{
			return this.songs.get(index);
		}
		return null;
	}
	
	public int getIndexOf(Song song)
	{
		return this.songs.indexOf(song);
	}
	
	public Song getLastSong()
	{
		// Check that the songs array isn't empty
		if (this.songs.size() != 0)
		{
			return this.songs.get(this.songs.size() - 1);
		}
		return null;
	}

	public void addSong(Song song)
	{
		this.songs.add(song);
	}
	
	public void removeSong(int songIndex)
	{
		this.songs.remove(songIndex);
	}
	
	/**
	 * Swaps the order of two songs on the playlist.
	 * 
	 * @param index1 - The index of the first song to be swapped.
	 * @param index2 - The index of the seconds song to be swapped.
	 */
	public void swapSongs(int index1, int index2)
	{
		Collections.swap(this.songs, index1, index2);
	}
	
	public File getFile()
	{
		return this.file;
	}
	
	public void setFile(File file)
	{
		this.file = file;
	}
	
	/**
	 * Returns the size of the songs array.
	 */
	public int getSize()
	{
		return this.songs.size();
	}
	
	/**
	 * Calculates the total play time of all of the songs on this playlist.
	 * 
	 * @return - A String value representing the total play time in hours, minutes and seconds.
	 */
	public String getPlayTime()
	{
		long playTime = 0;
		
		for (Song song : this.songs)
		{
			playTime += song.getLength();
		}

	    int hours = (int) playTime / 3600;
	    int remainder = (int) playTime - hours * 3600;
	    int mins = remainder / 60;
	    remainder = remainder - mins * 60;
	    int secs = remainder;
	    
		return (((hours == 0) ? "" : hours + "h ") + ((hours == 0 && mins == 0) ? "" : mins + "m ") + secs + "s");
	}
}
