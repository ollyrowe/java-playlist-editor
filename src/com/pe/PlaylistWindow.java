package com.pe;

import com.pe.Song;
import com.pe.Playlist;
import com.pe.UserDefaults;
import com.pe.utils.FileTree;
import com.pe.utils.FileNode;
import com.pe.audio.MusicPlayer;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Toolkit;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import javax.swing.ImageIcon;

/**
 * The main GUI for the playlist editor.
 * 
 * @author olly.rowe
 */
@SuppressWarnings("serial")
public class PlaylistWindow extends JFrame implements ActionListener {
	// The current version number
	private final String VERSION = "1.0.0";

	// The user defaults object
	private UserDefaults userDefaults;

	// The playlist being edited
	private Playlist playlist;

	// The song table and table model
	private JTable songTable;
	private DefaultTableModel songTableModel;

	// The playlist details text fields
	private JTextField playlistNameTextField;
	private JTextField playlistAuthorTextField;
	private JTextField playlistSizeTextField;
	private JTextField playlistPlayTimeTextField;

	// The menu bar components
	private JMenuItem mntmNew;
	private JMenuItem mntmOpen;
	private JMenuItem mntmSave;
	private JMenuItem mntmExit;
	private JMenuItem mntmSplitTrack;
	private JMenuItem mntmReleaseNotes;

	// The button components
	private JButton changeDirectoryButton;
	private JButton addSongButton;
	private JButton removeSongButton;
	private JButton saveButton;
	private JButton exitButton;
	private JButton upArrowButton;
	private JButton downArrowButton;

	// The file explorer tree
	private FileTree fileTree;

	// The music player component
	private MusicPlayer musicPlayer;
	private JPanel albumArtPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PlaylistWindow frame = new PlaylistWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Class Constructor. Generates the GUI.
	 */
	public PlaylistWindow() {
		// Create the playlist object
		this.playlist = new Playlist();

		// Initialise the user defaults
		this.userDefaults = new UserDefaults();

		// Set the window title
		setTitle("Playlist Editor " + VERSION);
		// Set the window icon
		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png")));
		// Prevent the window from being resized
		setResizable(false);

		// Set the GUI appearance to match that of the users operating system
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.put("Slider.focus", UIManager.get("Slider.background"));
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			System.err.println("An error occured setting the UIManager look and feel.");
		}

		// Set the window dimensions
		setBounds(100, 100, 800, 540);

		// Set default closing operation to nothing
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// Add custom closing operation
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				exit();
			}
		});

		// Create the menu bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		// Add menu components to the menu bar
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		mntmNew = new JMenuItem("Create New Playlist...");
		mntmNew.addActionListener(this);
		mnFile.add(mntmNew);
		mntmOpen = new JMenuItem("Open Existing Playlist...");
		mntmOpen.addActionListener(this);
		mnFile.add(mntmOpen);
		mntmSave = new JMenuItem("Save");
		mntmSave.addActionListener(this);
		mnFile.add(mntmSave);
		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(this);
		mnFile.add(mntmExit);
		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);
		mntmSplitTrack = new JMenuItem("Split Track");
		mntmSplitTrack.addActionListener(this);
		mnTools.add(mntmSplitTrack);
		JMenu mnAbout = new JMenu("About");
		menuBar.add(mnAbout);
		mntmReleaseNotes = new JMenuItem("Release Notes");
		mntmReleaseNotes.addActionListener(this);
		mnAbout.add(mntmReleaseNotes);

		// Create the main content pane
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// Create the track list section
		JPanel trackListPanel = new JPanel();
		trackListPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Track List",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		trackListPanel.setBounds(338, 87, 446, 262);
		contentPane.add(trackListPanel);
		trackListPanel.setLayout(null);

		// Create the song table and table model
		songTable = new JTable();
		songTableModel = new DefaultTableModel(new Object[][] {}, new String[] { "Track No.", "Artist", "Title" }) {
			// Disable cell-editing
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		// Set the table model
		songTable.setModel(songTableModel);
		// Set table attributes
		songTable.getColumnModel().getColumn(0).setPreferredWidth(56);
		songTable.getColumnModel().getColumn(0).setMaxWidth(56);
		songTable.setColumnSelectionAllowed(false);
		songTable.setFillsViewportHeight(true);
		songTable.setRowSelectionAllowed(true);

		// Create a new scroll pane and assign the songs table to it
		JScrollPane songTableScrollPane = new JScrollPane(songTable);
		songTableScrollPane.setBounds(10, 21, 426, 196);
		songTableScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		songTableScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		trackListPanel.add(songTableScrollPane);

		// Create remove song button
		removeSongButton = new JButton("Remove");
		removeSongButton.setBounds(347, 228, 89, 23);
		removeSongButton.addActionListener(this);
		trackListPanel.add(removeSongButton);

		Image upArrowIcon = (new ImageIcon(getClass().getResource("/images/arrow up.png"))).getImage()
				.getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		upArrowButton = new JButton(new ImageIcon(upArrowIcon));
		upArrowButton.addActionListener(this);
		upArrowButton.setBounds(10, 228, 23, 23);
		trackListPanel.add(upArrowButton);

		Image downArrowIcon = (new ImageIcon(getClass().getResource("/images/arrow down.png"))).getImage()
				.getScaledInstance(10, 10, Image.SCALE_SMOOTH);
		downArrowButton = new JButton(new ImageIcon(downArrowIcon));
		downArrowButton.addActionListener(this);
		downArrowButton.setBounds(37, 228, 23, 23);
		trackListPanel.add(downArrowButton);

		// Create browse for music panel
		JPanel browseForMusicPanel = new JPanel();
		browseForMusicPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Browse for Music",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		browseForMusicPanel.setBounds(10, 11, 318, 469);
		contentPane.add(browseForMusicPanel);
		browseForMusicPanel.setLayout(null);

		// Create add song to playlist button
		addSongButton = new JButton("Add");
		addSongButton.setBounds(190, 435, 118, 23);
		addSongButton.addActionListener(this);
		browseForMusicPanel.add(addSongButton);

		// Change the default leaf icon to music note. As the tree will filter mp3
		// files, only mp3 files will display this icon
		UIManager.put("Tree.leafIcon", new ImageIcon((new ImageIcon(getClass().getResource("/images/music note.png")))
				.getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH)));

		// Create the file explorer tree using the default directory if one exists,
		// otherwise use the user's C: drive. Filter it to show only mp3 files
		fileTree = new FileTree(
				(this.userDefaults.getDefaultBrowserDir() == null) ? "C:\\" : this.userDefaults.getDefaultBrowserDir(),
				Arrays.asList(".mp3"));

		// Add mouse listener to detect double clicks
		fileTree.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (fileTree.getRowForLocation(e.getX(), e.getY()) != -1) {
					FileNode selectedNode = (FileNode) fileTree.getPathForLocation(e.getX(), e.getY())
							.getLastPathComponent();

					// If a double-click is detected, add the song to the current playlist
					if (e.getClickCount() == 2) {
						addSong(selectedNode);
					}
				}
			}
		});

		// Create a scroll pane and add the file tree to it
		JScrollPane fileTreeScrollPane = new JScrollPane(fileTree);
		fileTree.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		fileTreeScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		fileTreeScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		fileTreeScrollPane.setBounds(10, 22, 298, 402);
		browseForMusicPanel.add(fileTreeScrollPane);

		// Create change directory button which changes contents of file tree
		changeDirectoryButton = new JButton("...");
		changeDirectoryButton.setBounds(10, 435, 42, 23);
		browseForMusicPanel.add(changeDirectoryButton);
		changeDirectoryButton.addActionListener(this);

		// Create new playlist details panel
		JPanel playlistDetailsPanel = new JPanel();
		playlistDetailsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Details",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		playlistDetailsPanel.setBounds(338, 11, 446, 65);
		contentPane.add(playlistDetailsPanel);
		playlistDetailsPanel.setLayout(null);

		// Create playlist name label and text field
		JLabel lblName = new JLabel("Name");
		lblName.setBounds(10, 18, 46, 14);
		playlistDetailsPanel.add(lblName);
		playlistNameTextField = new JTextField();
		playlistNameTextField.setBounds(54, 14, 241, 20);
		playlistDetailsPanel.add(playlistNameTextField);
		playlistNameTextField.setColumns(10);
		addChangeListener(playlistNameTextField);

		// Create playlist author label and text field
		JLabel lblAuthor = new JLabel("Author");
		lblAuthor.setBounds(10, 42, 46, 14);
		playlistDetailsPanel.add(lblAuthor);
		playlistAuthorTextField = new JTextField();
		playlistAuthorTextField.setBounds(54, 38, 241, 20);
		playlistDetailsPanel.add(playlistAuthorTextField);
		playlistAuthorTextField.setColumns(10);
		addChangeListener(playlistAuthorTextField);

		// Create playlist size label and text field
		JLabel lblLength = new JLabel("Size");
		lblLength.setBounds(305, 16, 46, 14);
		playlistDetailsPanel.add(lblLength);
		playlistSizeTextField = new JTextField();
		playlistSizeTextField.setText("0");
		playlistSizeTextField.setEditable(false);
		playlistSizeTextField.setBounds(364, 12, 72, 20);
		playlistDetailsPanel.add(playlistSizeTextField);
		playlistSizeTextField.setColumns(10);

		// Create playlist play time label and text field
		JLabel lblPlayTime = new JLabel("Play Time");
		lblPlayTime.setBounds(305, 40, 46, 14);
		playlistDetailsPanel.add(lblPlayTime);
		playlistPlayTimeTextField = new JTextField();
		playlistPlayTimeTextField.setText("0s");
		playlistPlayTimeTextField.setEditable(false);
		playlistPlayTimeTextField.setColumns(10);
		playlistPlayTimeTextField.setBounds(364, 36, 72, 20);
		playlistDetailsPanel.add(playlistPlayTimeTextField);

		// Create the save button
		saveButton = new JButton("Save");
		saveButton.setBounds(596, 457, 89, 23);
		saveButton.addActionListener(this);
		contentPane.add(saveButton);

		// Create the exit button
		exitButton = new JButton("Exit");
		exitButton.setBounds(695, 457, 89, 23);
		exitButton.addActionListener(this);
		contentPane.add(exitButton);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Music Playback",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(338, 360, 356, 86);
		contentPane.add(panel);
		panel.setLayout(null);

		albumArtPanel = new JPanel();
		albumArtPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		albumArtPanel.setBounds(704, 366, 78, 78);
		contentPane.add(albumArtPanel);
		albumArtPanel.setLayout(null);

		JLabel lblAlbumArt = new JLabel();
		lblAlbumArt.setBounds(4, 4, 70, 70);
		albumArtPanel.add(lblAlbumArt);

		// Create the music player component
		musicPlayer = new MusicPlayer(this.playlist, lblAlbumArt);
		musicPlayer.setBounds(13, 16, 330, 64);
		panel.add(musicPlayer);
	}

	/**
	 * Button Action listener handler.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == changeDirectoryButton) {
			browseDirectory();
		} else if (source == addSongButton) {
			addSelectedSongs();
		} else if (source == removeSongButton) {
			removeSelectedSongs();
		} else if ((source == saveButton) || (source == mntmSave)) {
			save();
		} else if ((source == exitButton) || (source == mntmExit)) {
			exit();
		} else if (source == mntmOpen) {
			open();
		} else if ((source == upArrowButton) || (source == downArrowButton)) {
			reorderSongs(source);
		} else if (source == mntmNew) {
			createNewPlaylist(null);
		} else if (source == mntmSplitTrack) {
			splitTrack();
		} else if (source == mntmReleaseNotes) {
			displayReleaseNotes();
		}
	}

	/**
	 * Allows the user to browse for a directory to populate the tree view.
	 */
	public void browseDirectory() {
		// The file chooser
		JFileChooser fileChooser = new JFileChooser();
		// Allow the user to only be able to select directories
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		// Show the dialog and if a directory is selected, update the file tree
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			// Update the directory within the file tree
			fileTree.setDirectory(fileChooser.getSelectedFile().getAbsolutePath());
			// Update the user defaults with the new directory
			this.userDefaults.setDefaultBrowserDir(fileChooser.getSelectedFile().getAbsolutePath());
		}
		return;
	}

	/**
	 * Allows the user to browse for a playlist file to be opened.
	 */
	public void open() {
		// The file chooser. Setting the current directory from the user defaults
		JFileChooser fileChooser = new JFileChooser(new File(
				((this.userDefaults.getDefaultOpenPlaylistDir() != null) ? this.userDefaults.getDefaultOpenPlaylistDir()
						: "")));
		// Create new file filter for the playlist file formats
		FileNameExtensionFilter filter = new FileNameExtensionFilter(null, "wpl");
		// Set the filter
		fileChooser.setFileFilter(filter);
		// Allow the user to only be able to select directories
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Show the dialog and if a directory is selected, update the file tree
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			// Check that the file is of the correct format
			if (fileChooser.getSelectedFile().getName().endsWith(".wpl")) {
				this.createNewPlaylist(fileChooser.getSelectedFile());
				// Set the default for the location of the directory that this file is in
				this.userDefaults.setDefaultOpenPlaylistDir(fileChooser.getSelectedFile().getParent());
			}
		}
	}

	/**
	 * Clears the GUI and creates a new playlist instance.
	 */
	public void createNewPlaylist(File file) {
		// If the current playlist isn't yet saved then ask the user if they would like
		// to save it
		if (!this.playlist.isSaved()) {
			// Ask the user if they want to save the current playlist
			int saveDialog = JOptionPane.showConfirmDialog(this, "Would you like to save the current playlist?");

			// If the user confirmed the dialog
			if (saveDialog == JOptionPane.YES_OPTION) {
				// Save the current playlist
				boolean saveSuccessful = this.save();
				// If the save was unsuccessful then exit this method
				if (!saveSuccessful) {
					return;
				}
			}
			// If the user cancelled the dialog, then do not create new playlist
			else if (saveDialog == JOptionPane.CANCEL_OPTION || saveDialog == JOptionPane.CLOSED_OPTION) {
				return;
			}
		}

		// If a file has been specified then pass it into the playlist constructor when
		// creating the new playlist
		if (file != null) {
			try {
				this.playlist = new Playlist(file);
			} catch (FileNotFoundException e) {
				this.raiseFileNotFoundWarning(file.getAbsolutePath());
			}
		} else {
			this.playlist = new Playlist();
		}

		// Refresh the GUI
		this.refreshPlaylistDetails();

		// Update the music player with the new playlist
		this.musicPlayer.updatePlaylist(this.playlist);
	}

	/**
	 * Updates all components that hold information about the playlist with current
	 * information.
	 */
	public void refreshPlaylistDetails() {
		// Update the playlist details section
		this.playlistNameTextField.setText(this.playlist.getName());
		this.playlistAuthorTextField.setText(this.playlist.getAuthor());
		this.playlistSizeTextField.setText(String.valueOf(this.playlist.getSize()));
		this.playlistPlayTimeTextField.setText(this.playlist.getPlayTime());

		// Remove any songs from the songs table
		int numOfRows = this.songTable.getModel().getRowCount();
		// Iterate through songs currently in table
		for (int i = 0; i < numOfRows; i++) {
			// Remove the song from the table
			((DefaultTableModel) this.songTable.getModel()).removeRow(0);
		}

		// Update the songs table
		for (Song song : this.playlist.getSongs()) {
			songTableModel.addRow(
					new Object[] { (this.playlist.getSongs().indexOf(song) + 1), song.getArtist(), song.getTitle() });
		}
	}

	public void addSong(FileNode nodeOfFile) {
		File file = nodeOfFile.getFile();

		if (file.getName().endsWith(".mp3")) {
			try {
				// Create song object
				Song song = new Song(file.getAbsolutePath());
				// Add the song to the playlist
				this.playlist.addSong(song);

				// If the playlist was empty before adding the song then set this song as the
				// current one
				if (this.playlist.getSize() == 1) {
					this.musicPlayer.setCurrentSong(song);
					// Refresh the GUI components
					this.musicPlayer.updateGUI();
				}

				// Add the song to the table
				songTableModel.addRow(new Object[] { this.playlist.getSize(), song.getArtist(), song.getTitle() });

				// Refresh the playlist details
				refreshPlaylistDetails();
			} catch (FileNotFoundException e) {
				this.raiseFileNotFoundWarning(file.getAbsolutePath());
			}
		}
	}

	/**
	 * Adds the songs that are currently selected in the file tree to the tracks
	 * table.
	 */
	public void addSelectedSongs() {
		// Check that something has been selected in the file tree
		if (fileTree.getSelectionPath() != null) {
			for (TreePath tp : fileTree.getSelectionPaths()) {
				addSong((FileNode) tp.getLastPathComponent());
			}
		}
	}

	/**
	 * Removes the songs that are currently selected in the songs table from the
	 * playlist.
	 */
	public void removeSelectedSongs() {
		// Get the number of selected rows
		int numOfSelectedRows = this.songTable.getSelectedRowCount();

		// Iterate through the selected rows
		for (int i = 0; i < numOfSelectedRows; i++) {
			int index = this.songTable.getSelectedRows()[0];
			// Remove the song from the playlist
			this.playlist.removeSong(index);
			// Remove the song from the table
			((DefaultTableModel) this.songTable.getModel()).removeRow(index);
		}
		// Refresh the playlist details
		refreshPlaylistDetails();
	}

	/**
	 * Updates the track numbers in the songs table.
	 */
	public void updateTrackNumbers() {
		// Update the track numbers in the songs table
		for (int i = 0; i < this.songTable.getModel().getRowCount(); i++) {
			this.songTable.getModel().setValueAt((i + 1), i, 0);
		}
	}

	/**
	 * Reorders the songs within the songs table upon clicking up or down arrow
	 * button.
	 *
	 * @param source - The source object that trigger the action event.
	 */
	public void reorderSongs(Object source) {
		// Ensure that only one row has been selected
		if (this.songTable.getSelectedRowCount() == 1) {
			// Get the index of the selected row
			int indexOfSelectedRow = this.songTable.getSelectedRow();

			// Get the table model
			DefaultTableModel tableModel = ((DefaultTableModel) this.songTable.getModel());

			// If the up arrow button was clicked
			if (source == this.upArrowButton) {
				// Check that the selected row isn't already at the start
				if (indexOfSelectedRow != 0) {
					// Unselected the selected row
					this.songTable.clearSelection();
					// Move the row up in the table
					tableModel.moveRow(indexOfSelectedRow, indexOfSelectedRow, (indexOfSelectedRow - 1));
					// Re-select the moved row
					this.songTable.setRowSelectionInterval((indexOfSelectedRow - 1), (indexOfSelectedRow - 1));
					// Swap the two rows that have changed position in the playlist
					this.playlist.swapSongs(indexOfSelectedRow, (indexOfSelectedRow - 1));
				}
			}
			// Otherwise, if the down arrow button was clicked
			else if (source == this.downArrowButton) {
				// Check that the selected row isn't already at the end
				if (indexOfSelectedRow != (this.songTable.getRowCount() - 1)) {
					// Unselected the selected row
					this.songTable.clearSelection();
					// Move the row down in the table
					tableModel.moveRow(indexOfSelectedRow, indexOfSelectedRow, (indexOfSelectedRow + 1));
					// Re-select the moved row
					this.songTable.setRowSelectionInterval((indexOfSelectedRow + 1), (indexOfSelectedRow + 1));
					// Swap the two rows that have changed position in the playlist
					this.playlist.swapSongs(indexOfSelectedRow, (indexOfSelectedRow + 1));
				}
			}
			//
			this.songTable.scrollRectToVisible(this.songTable.getCellRect(this.songTable.getSelectedRow(), 0, true));
			// Update the track numbers in the songs table
			updateTrackNumbers();
		}
	}

	/**
	 * Saves the playlist currently being edited.
	 * 
	 * @return Boolean value indicating whether the save was successful or not.
	 */
	public boolean save() {
		// Check if the playlist has an associated file, if not prompt the user to
		// select one
		if (playlist.getFile() == null) {
			// Display the file system for the user to pick a location to save the playlist
			JFileChooser fileChooser = new JFileChooser();
			// Set the file name within the file chooser
			fileChooser.setSelectedFile(new File(playlist.getName() + ".wpl"));
			// If the user has selected a file
			if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				// Fetch the selected file
				File chosenFile = fileChooser.getSelectedFile();
				// Check that the file is of the right type
				if (chosenFile.getAbsolutePath().endsWith(Playlist.WPL)) {
					// If the file already exists, then as the user to confirm overwritting the
					// playlist
					if (chosenFile.exists()) {
						int confirmReplaceFileResult = JOptionPane.showConfirmDialog(this,
								chosenFile.getName() + " already exists. Do you want to replace it?", "Confirm Save As",
								JOptionPane.YES_NO_OPTION);
						// If the user didn't says yes to the popup then exit the method
						if (!(confirmReplaceFileResult == JOptionPane.YES_OPTION)) {
							return false;
						}
					}
					// Set the playlist's file
					this.playlist.setFile(fileChooser.getSelectedFile().getAbsoluteFile());
				}
			} else {
				return false;
			}
		}
		// Save the playlist
		this.playlist.save(Playlist.WPL);
		// Display popup
		JOptionPane.showMessageDialog(this, "Your playlist has been saved.");
		return true;
	}

	/**
	 * Splits the current track within the music player and allows the user to
	 * export it to a new file.
	 */
	public void splitTrack() {
		// If their isn't currently a song loaded into the music player then raise an
		// error message to the user and exit method
		if (this.musicPlayer.getCurrentSong() == null) {
			JOptionPane.showMessageDialog(this, "Could not split as there is no current song. Play a song to split it.",
					"Split Track Failed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// Ask the user whether they want to keep the first or second part of the
		// current track
		int splitChoice = JOptionPane.showOptionDialog(this,
				"Would you like to export the first or second part of the split track?", "Split Track Tool",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
				new Object[] { "First Part", "Second Part" }, null);

		// If the user closed the dialog then exit method
		if (splitChoice == JOptionPane.CLOSED_OPTION) {
			return;
		}

		// Ask the user to select a file to write the new split track to
		JFileChooser fileChooser = new JFileChooser(new File(
				((this.userDefaults.getDefaultOpenPlaylistDir() != null) ? this.userDefaults.getDefaultOpenPlaylistDir()
						: "")));
		// Create new file filter for mp3 file format
		FileNameExtensionFilter filter = new FileNameExtensionFilter(null, "mp3");
		// Set the filter
		fileChooser.setFileFilter(filter);
		// Allow the user to only be able to select files
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		// Show the dialog and if a directory is selected, update the file tree
		if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			// The file chosen by the user
			File selectedFile = fileChooser.getSelectedFile();

			// Check that the file is of the correct format
			if (selectedFile.getName().endsWith(".mp3")) {
				// If the file already exists, ask the user if they want to replace it
				if (selectedFile.exists()) {
					int replaceChoice = JOptionPane.showConfirmDialog(this,
							"The file that you have selected already exists, would you like to replace it?",
							"Confirm Replace", JOptionPane.YES_NO_CANCEL_OPTION);
					// If the users didn't agree to replace the file then exit the method
					if (replaceChoice != JOptionPane.YES_OPTION) {
						return;
					}
				}

				// Split the track at the specified points and write the output to the specified
				// file
				this.musicPlayer.splitCurrentTrackAndWriteToFile(selectedFile, (splitChoice == 0) ? true : false);
			} else {
				JOptionPane.showMessageDialog(this, "Unable to perform split. Export file must of mp3 format.",
						"Incorrect Export Format", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * Raises an error dialog informing the user of a resource not being found.
	 * 
	 * @param The name of the resource that could not be found.
	 */
	public void raiseFileNotFoundWarning(String fileName) {
		// The error message to be displayed to the user
		String message;

		if (fileName.endsWith(".wpl")) {
			message = "The playlist or a track within the following playlist file could not be found:";
		} else {
			message = "The following file could not be found:";
		}
		// Display the error message dialog
		JOptionPane.showMessageDialog(this, message + "\n" + fileName, "File Not Found", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Displays a popup containing the release notes
	 */
	private void displayReleaseNotes() {
		// Fetch the text from the release notes file
		String releaseNotes = new String();
		try {
			InputStream input = getClass().getResourceAsStream("/resources/release notes.txt");

			BufferedReader br = new BufferedReader(new InputStreamReader(input));

			StringBuilder sb = new StringBuilder();

			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			releaseNotes = sb.toString();

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// The text pane object containing the release notes
		JTextPane releaseNotesTextPane = new JTextPane();
		releaseNotesTextPane.setPreferredSize(new Dimension(350, 600));
		releaseNotesTextPane.setFont(new Font(Font.SANS_SERIF, 0, 12));
		releaseNotesTextPane.setText(releaseNotes);
		releaseNotesTextPane.setEditable(false);
		releaseNotesTextPane.setCaretPosition(0);

		JScrollPane releaseNotesScrollPane = new JScrollPane(releaseNotesTextPane);
		releaseNotesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		releaseNotesScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		// Display a popup containing the release notes text pane
		JOptionPane.showMessageDialog(this, releaseNotesScrollPane, "Release Notes", JOptionPane.PLAIN_MESSAGE);
	}

	/**
	 * Closes the program.
	 */
	public void exit() {
		// If the current playlist isn't saved then prompt the user to save it.
		if (!this.playlist.isSaved()) {
			// Ask the user if they want to save the current playlist
			int saveDialog = JOptionPane.showConfirmDialog(this,
					"Would you like to save the current playlist before exiting?");

			// If the user confirmed the dialog
			if (saveDialog == JOptionPane.YES_OPTION) {
				// Save the current playlist
				boolean saveSuccessful = this.save();
				// If the save was unsuccessful then exit this method without exiting
				if (!saveSuccessful) {
					return;
				}
			}
			// If the user cancelled the popup then cancelled the exiting sequence
			else if (saveDialog == JOptionPane.CANCEL_OPTION || saveDialog == JOptionPane.CLOSED_OPTION) {
				return;
			}
		}
		// Save the user defaults file
		this.userDefaults.saveDefaultsToFile();
		// Exit the program
		System.exit(0);
	}

	/**
	 * Adds a document listener to the document of a textfield so that a change to
	 * the contents of the textfield calls the textFieldChanged method.
	 * 
	 * @param textField - The JTextField to add the listener to.
	 */
	public void addChangeListener(JTextField textField) {
		// Create the document listener
		DocumentListener dl = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				textFieldChange(textField);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				textFieldChange(textField);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				textFieldChange(textField);
			}
		};
		// Add the document listener to the text field's document
		textField.getDocument().addDocumentListener(dl);
	}

	/**
	 * Called when the author or playlist title text fields are edited.
	 */
	private void textFieldChange(JTextField source) {
		// Update the corresponding playlist value depending up the source of the method
		// call
		if (source == this.playlistNameTextField) {
			this.playlist.setName(this.playlistNameTextField.getText());
		} else if (source == this.playlistAuthorTextField) {
			this.playlist.setAuthor(this.playlistAuthorTextField.getText());

		}
	}
}
