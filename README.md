# java-playlist-editor

🎶 A simple GUI for creating, editing and listening to music playlists. 🎶

👉[Download v1.0.0](https://github.com/ollyrowe/java-playlist-editor/releases/download/v1.0.0/Playlist.Editor.zip)👈

## Features

➕ Create new playlists<br />
🛠 Load and edit existing playlists<br />
🔎 Browse for music on your local file system<br />
🎧 Listen to your playlists<br />
✂ Split tracks to discard unwanted leading and tailing audio (ideal for cutting down live performance recordings)<br />
📌 Remember previous folder locations between sessions<br />
✨ ...and more

![Playlist Editor v1.0.0 screenshot](https://raw.githubusercontent.com/ollyrowe/java-playlist-editor/master/screenshots/v1.0.0_demo.png)

## Supported File Formats

Below is a list of the currently supported file formats.

##### Playlist File Formats

- WPL

##### Music File Formats

- MP3

## defaults.conf

This file is created upon first using the app and stores the following configurations between sessions for convenience:

- Default Browser Directory (defaultBrowserDir) - Default directory to be opened in the 'Browse for Music' panel.
- Default Open Playlist Directory (defaultOpenPlaylistDir) - Default directory to be opened when opening an existing playlist.

Example:

```
<Defaults>
    <defaultBrowserDir>C:\Music\</defaultBrowserDir>
    <defaultOpenPlaylistDir>C:\Music\My Playlists\</defaultOpenPlaylistDir>
</Defaults>
```

## Built With

- [MP3agic](https://github.com/mpatric/mp3agic) - Java library for reading MP3 files and reading / manipulating the ID3 tags
- [JLayer](http://www.javazoom.net/javalayer/documents.html) - Java library for decoding, playing and converting MP3 files

## Authors

- [Olly Rowe](https://github.com/ollyrowe)
