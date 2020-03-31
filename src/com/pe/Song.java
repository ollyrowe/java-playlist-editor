package com.pe;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;

/**
 * Represents a song.
 * 
 * @author olly.rowe
 */
public class Song
{
	// The file associated with this song
	private File file;
	// Song title
	private String title;
	// The artist
	private String artist;
	// The album
	private String album;
	// Length in milliseconds
	private long length;
	// The album art
	private BufferedImage albumArt;
	
	/**
	 * Class Constructor.
	 * 
	 * @param path - The string value of the absolute path to the .mp3 file
	 */
	public Song(String path) throws FileNotFoundException
	{
		// Set the file
		this.file = new File(path);
		// Attempt to extract metadata from the file
		try
		{
			// Utilise MP3agic module to read the ID3v2 tag
			Mp3File song = new Mp3File(this.getFile().getAbsolutePath());
			
			if (song.hasId3v2Tag())
			{
				 // Fetch the ID3v2 tag
			     ID3v2 id3v2tag = song.getId3v2Tag();
			     // Fetch and assign values from the ID3v2 tag
			     this.title = id3v2tag.getTitle();
			     this.artist = id3v2tag.getArtist();
			     this.album = id3v2tag.getAlbum();
			     this.length = song.getLengthInSeconds();
			     // Fetch the album image data
		         byte[] imageData = id3v2tag.getAlbumImage();
		         // Convert the bytes to an image if the data has been found
		         if (imageData != null)
		         {
				     this.albumArt = ImageIO.read(new ByteArrayInputStream(imageData));
		         }
			}					
		}
		catch (FileNotFoundException e)
		{
			throw new FileNotFoundException();
		}
		catch (IOException | InvalidDataException | UnsupportedTagException e)
		{
			e.printStackTrace();
		}
		// Assign default values for variables that have not yet been set
		this.title = (this.title == null) ? (this.file.getName().substring(0, this.file.getName().length() - 4)) : this.title;
		this.artist = (this.artist == null) ? "unknown artist" : this.artist;
		this.album = (this.album == null) ? "unknown album" : this.album;
	}
	
	public String getTitle()
	{
		return this.title;
	}

	public String getArtist()
	{
		return this.artist;
	}

	public String getAlbum()
	{
		return this.album;
	}

	public long getLength()
	{
		return this.length;
	}
	
	public File getFile()
	{
		return this.file;
	}
	
	public BufferedImage getAlbumArt()
	{
		return this.albumArt;
	}
}
