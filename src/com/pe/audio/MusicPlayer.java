package com.pe.audio;

import com.pe.Song;
import com.pe.Playlist;

import java.awt.Image;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicSliderUI;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * Represent the music player component.
 * 
 * @author olly.rowe
 */
@SuppressWarnings("serial")
public class MusicPlayer extends JPanel implements ActionListener
{	
	// The javazoom player object
	private Player player;
	
	// The thread object that the player will run within
	private Thread playerThread;
	
	// The member variables associated with the player
	private FileInputStream stream;
	private long totalSongLength;
	
	// Whether the player is currently in a paused state
	private volatile boolean isPaused = false;
	
	// The song that is currently playing
	private Song currentSong;
	
	// The playlist being played
	private Playlist playlist;
	
	// The song title text field
	private JTextField txtSongTitle;
	
	// The button components
	private JButton btnSkipBackSong;
	private JButton btnPlayPause;
	private JButton btnSkipForwardSong;
	
	// The song progress bar
	private JSlider progressBar;
	
	// Play icon
	private final Image PLAY_ICON = (new ImageIcon(getClass().getResource("/images/play.png"))).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
	// Pause icon
	private final Image PAUSE_ICON = (new ImageIcon(getClass().getResource("/images/pause.png"))).getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH);
	
	// The album art JPanel component
	private JLabel albumArtLabel;
		
	// The default album art cover
	private final Image DEFAULT_ALBUM_ART_ICON = (new ImageIcon(getClass().getResource("/images/default album art.png"))).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH);
	
	/**
	 * Class Constructor.
	 * 
	 * @param playlist - The playlist of songs to be played.
	 * @param albumArtPanel - The JLabel where the album art will appear.
	 */
	public MusicPlayer(Playlist playlist, JLabel albumArtLabel)
	{
		super();
		
		// Set the playlist
		this.playlist = playlist;
		// Set the album art label
		this.albumArtLabel = albumArtLabel;
		
		// Remove the layout
		this.setLayout(null);
		
		// Set the default album art icon
		this.albumArtLabel.setIcon(new ImageIcon(DEFAULT_ALBUM_ART_ICON));
		
		// Set up skip backwards button
		Image skipBackwardsIcon = (new ImageIcon(getClass().getResource("/images/skip backwards.png"))).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
		btnSkipBackSong = new JButton(new ImageIcon(skipBackwardsIcon));
		btnSkipBackSong.addActionListener(this);
		btnSkipBackSong.setBounds(10, 20, 80, 23);
		add(btnSkipBackSong);
		
		// Set up play / pause button
		btnPlayPause = new JButton(new ImageIcon(PLAY_ICON));
		btnPlayPause.addActionListener(this);
		btnPlayPause.setBounds(136, 20, 58, 23);
		add(btnPlayPause);
		
		// Set up skip backwards button
		Image skipForwardsIcon = (new ImageIcon(getClass().getResource("/images/skip forwards.png"))).getImage().getScaledInstance(15, 15, Image.SCALE_SMOOTH);
		btnSkipForwardSong = new JButton(new ImageIcon(skipForwardsIcon));
		btnSkipForwardSong.addActionListener(this);
		btnSkipForwardSong.setBounds(240, 20, 80, 23);
		add(btnSkipForwardSong);
		
		// Set up the song title and artist text field
		txtSongTitle = new JTextField();
		txtSongTitle.setBorder(BorderFactory.createEmptyBorder());
		txtSongTitle.setHorizontalAlignment(SwingConstants.CENTER);
		txtSongTitle.setEditable(false);
		txtSongTitle.setBackground(SystemColor.menu);
		txtSongTitle.setBounds(10, 0, 310, 18);
		add(txtSongTitle);
		
		// Set up the song progress bar. Removes listeners that disallow the user to seek a position on the slider by clicking at a certain point.
		progressBar = new JSlider() {
		    {
		        MouseListener[] listeners = getMouseListeners();
		        for (MouseListener l : listeners)
		            removeMouseListener(l); // remove UI-installed TrackListener
		        final BasicSliderUI ui = (BasicSliderUI) getUI();
		        BasicSliderUI.TrackListener tl = ui.new TrackListener()
		        {
		            @Override
		            public void mouseReleased(MouseEvent e)
		            {
		                Point p = e.getPoint();
		                int value = ui.valueForXPosition(p.x);
		                setValue(value);
		                if (!isPaused)
		                {
			                pause();
			                setValue(value);
			                resume();
		                }
		            }
		            
		            @Override public boolean shouldScroll(int dir) {
		                return false;
		            }
		        };
		        addMouseListener(tl);
		    }
		};
		
		// Set the value to 0 by default
		progressBar.setValue(0);
		
		progressBar.setBounds(10, 43, 310, 24);
		add(progressBar);
	}
	
	/**
	 * Button Action listener handler.
	 */
	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Ensure that a playlist has been loaded and isn't empty
		Object source = e.getSource();
		
		if (source == btnSkipBackSong)
		{
			skipBackSong();
		}
		else if (source == btnPlayPause)
		{
			playPauseSong();
		}
		else if (source == btnSkipForwardSong)
		{
			skipForwardSong();
		}
	}
	
	/**
	 * Toggles between playing and pausing the current song.
	 */
	public void playPauseSong()
	{
		if (this.player != null)
		{
			if (this.isPaused)
			{
				this.resume();
			}
			else
			{
				this.pause();
			}
		}
		else
		{
			// Check that the playlist isn't empty
			if (this.playlist.getSong(0) != null)
			{
				// If the player is not yet initialised, default the current song to the first one in the playlist
				this.currentSong = this.playlist.getSongs().get(0);
				// Play the song from the start
				this.playSong(0);
			}
		}
	}
	
	/**
	 * Plays the current song.
	 * 
	 * @param startPoint - The point within the song to start playing form.
	 */
	private void playSong(long startPoint)
	{
		// Check that there is a current song to play
		if (this.currentSong != null)
		{
			this.isPaused = false;

			if (player != null)
			{
				player.close();
				player = null;
			}
			// Attempt to play the audio file
			try
			{
				this.stream = new FileInputStream(this.currentSong.getFile());
				
				this.totalSongLength = this.stream.available();
				
				this.progressBar.setMaximum((int) this.totalSongLength);
				
				this.stream.skip(startPoint);
				
				player = new Player(this.stream);
				
			    this.playerThread = new Thread("Music Player")
			    {
			        public void run()
			        {
			            try
			            {			                
			                updateGUI();
			                		                
			    			btnPlayPause.setIcon(new ImageIcon(PAUSE_ICON));
			    			
			                player.play();
			                		                
			                // Once the player is complete, skip to the next song in the playlist
			                if (player != null && player.isComplete())
			                {
			        			skipForwardSong();
			                }
			            }
			            catch (JavaLayerException e)
			            {
			                e.printStackTrace();
			            }
			        }
			    };
			    
			    this.playerThread.start();
			    
			    // Get all currently running threads
			    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			    
			    // End method if there is currently a progress bar updater thread running
			    for (Thread thread : threadSet)
			    {
			    	if (thread.getName().equals("Progress Bar Updater"))
			    	{
			    		return;
			    	}
			    }
			    
			    // Create a new thread to update the progress bar
				Thread updateProgressBarThread = new Thread("Progress Bar Updater") {
					public void run()
					{
						// maybe check if one of these threads is already running as they can double up when you navigate through a song using progress bar
		        		while (!this.isInterrupted())
		        		{
		        			// If player's state changes before song ends then catch the null pointer exception
		        			try
		        			{
		            			if (player.isComplete() || isPaused)
		            			{
		            				break;
		            			}

		            			// Update the progress bar
		            			updateProgressBar();
		            			
		            			Thread.sleep(100);
		        			}
		        			catch (NullPointerException | InterruptedException e)
		        			{
		        				break;
		        			}
		        		}
					}
				};
				
				updateProgressBarThread.start();
			}
			catch (JavaLayerException | IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Plays the current song from the beginning.
	 */
	private void playSong()
	{
		this.playSong(this.progressBar.getValue());
	}
	
	/**
	 * Pauses the song currently being played in the player.
	 */
	private void pause()
	{	
		this.btnPlayPause.setIcon(new ImageIcon(PLAY_ICON));

		if (player != null)
		{
			this.player.close();
		}
		this.isPaused = true;
	}
	
	/**
	 * Resumes a paused song in the player.
	 */
	private void resume()
	{
		// Play the song from pause location
		playSong(this.progressBar.getValue());
	}
	
	/**
	 * Skips the song currently being played back to the previous one in the playlist.
	 */
	private void skipBackSong()
	{
		// Check that the first song in the playlist isn't currently being played
		if (this.currentSong != this.playlist.getSong(0) && this.playlist.getSize() > 0)
		{
			// Change the current song to the previous on in the playlist
			this.currentSong = this.playlist.getSong(this.playlist.getIndexOf(this.currentSong) - 1);
		}
		// Play the current song from the start
		this.playSong(0);
	}
	
	/**
	 * Skips the song currently being played forward to the next one in the playlist.
	 */
	public void skipForwardSong()
	{
		// Check that the playlist has a final song before doing anything
		if (this.playlist.getLastSong() != null)
		{
			// Check that the final song in the playlist isn't currently being played
			if (this.currentSong != this.playlist.getLastSong())
			{
				// Update the current song to the previous one in the playlist
				this.currentSong = this.playlist.getSong(this.playlist.getIndexOf(this.currentSong) + 1);
				// Play the next song
				this.playSong(0);
			}
			// Otherwise, go into a paused state
			else
			{
				// Enter paused state
				this.pause();
				// Reset the progress bar
				this.progressBar.setValue(0);
			}
		}
	}
	
	/**
	 * Splits the current song at the current progress into two.
	 */
	public void splitCurrentTrackAndWriteToFile(File outputFile, boolean outputPreSplitPoint)
	{		
		// Check that there is a current track
		if (this.currentSong != null)
		{
			try
			{
				// Create a new temporary input stream for the current song
				FileInputStream tempInputStream = new FileInputStream(this.currentSong.getFile());
				
				int startPoint, endPoint;
				
				// Calculate the start and end points based upon argument
				if (outputPreSplitPoint)
				{
					startPoint = 0;
					endPoint = this.progressBar.getValue();
				}
				else
				{
					startPoint = this.progressBar.getValue(); 
					endPoint = (int) this.totalSongLength;
				}
				
				// Skip to the start point
				tempInputStream.skip((long) startPoint);
				
				// Create a new output stream to the output file
				FileOutputStream outputStream = new FileOutputStream(outputFile);
				
				// The number of remaining bytes to be read and outputted
				int remainingBytes = endPoint - startPoint;
				
				// Create the buffer to be written to
				byte[] buffer = new byte[remainingBytes];
				
				// Populate contents of the buffer from the input stream
				tempInputStream.read(buffer);
				// Write the contents of the buffer to the output stream
				outputStream.write(buffer);
				
				// Close the opened streams
				tempInputStream.close();
				outputStream.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
				return;
			}
		}
	}
	
	/**
	 * Updates the current playlist associated with this music player with a new one.
	 * 
	 * @param playlist - The new playlist.
	 */
	public void updatePlaylist(Playlist playlist)
	{
		// Update the playlist member variable
		this.playlist = playlist;
		
		if (playlist.getSize() > 0)
		{
			// Get the first song from the playlist
			Song firstSong = playlist.getSongs().get(0);
			
			// Set the current song
			this.currentSong = firstSong;

		}
		
		// Update the GUI components
		this.updateGUI();
	}
	
	/**
	 * Updates the progress bar with the current song progress.
	 */
	public void updateProgressBar()
	{
 		int progress;
		try
		{
			// Calculate progress as the total length of the song minus the length of song left
			progress = (int) (this.totalSongLength - this.stream.available());
		}
		catch (IOException e)
		{
			return;
		}
		// Update the progress bar value
		this.progressBar.setValue(progress);
	}
	
	/**
	 * Updates the GUI components with the details of the current song.
	 */
	public void updateGUI()
	{
		// Update the album art
		try
		{
			this.albumArtLabel.setIcon(new ImageIcon(new ImageIcon(this.currentSong.getAlbumArt()).getImage().getScaledInstance(70, 70, Image.SCALE_SMOOTH)));
		}
		catch (NullPointerException e)
		{
			this.albumArtLabel.setIcon(new ImageIcon(DEFAULT_ALBUM_ART_ICON));
		}
		
		// Update the song title and artist text field with the details of the current song
		this.txtSongTitle.setText(this.currentSong.getTitle() + " - " + this.currentSong.getArtist());
	}
	
	/**
	 * Sets the current song. Stops the current song from playing and resets the progress bar.
	 * 
	 * @param the song to be set as the current song.
	 */
	public void setCurrentSong(Song song)
	{
		// The the current song
		this.currentSong = song;
		// Pause the song that is currently playing
		this.pause();
		// Reset the progress bar
		this.progressBar.setValue(0);
		// Update the max value
		try
		{
			// Open a new temporary stream
			FileInputStream tempStream = new FileInputStream(this.currentSong.getFile());
			// Set the max value
			this.progressBar.setMaximum((int) tempStream.available());
			// Close the stream
			tempStream.close();
		}
		catch (IOException e)
		{
			return;
		}
	}
	
	/**
	 * Gets the current song.
	 * 
	 * @return the current song.
	 */
	public Song getCurrentSong()
	{
		return this.currentSong;
	}
}