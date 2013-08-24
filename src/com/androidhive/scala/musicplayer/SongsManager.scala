package com.androidhive.scala.musicplayer

object SongsManager {
  // SDCard Path
  val MEDIA_PATH = "/sdcard/"

  /**
   * Function to read all audio files from sdcard
   * and store the details in ArrayList
   */
  def getPlayList = {
	import java.util.HashMap

    val home = new java.io.File(MEDIA_PATH)
    val playList = new java.util.ArrayList[HashMap[String, String]]()

    home.listFiles(new FileExtensionFilter).foreach { f =>
      val song = new HashMap[String, String]()
      song.put("songTitle", f.getName.substring(0, f.getName.lastIndexOf('.')))
      song.put("songPath", f.getPath)
      playList.add(song)
    }
    playList
  }

  /**
   * Class to filter files which are having specified extension
   */
  class FileExtensionFilter extends java.io.FilenameFilter {
    def accept(dir: java.io.File, name: String) = {
      List(".wav", ".mp3", ".aac", ".m4a", ".mid").exists(name.toLowerCase.endsWith(_))
    }
  }
}
