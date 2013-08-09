package com.androidhive.scala.musicplayer

import java.io.File
import java.io.FilenameFilter

object SongsManager {
	// SDCard Path
	val MEDIA_PATH = "/sdcard/"

  /**
   * Function to read all mp3 files from sdcard
   * and store the details in ArrayList
   * */
  def getPlayList = {
	import java.util.HashMap
	//TODO: NO MEDIA ...
    val home = new File(MEDIA_PATH)
    val returnToJava = new java.util.ArrayList[HashMap[String, String]]()
    
    // return songs list array
    home.listFiles(new FileExtensionFilter).map { f =>
      /*
      scala.collection.mutable.HashMap(
        "songTitle" -> f.getName.substring(0, f.getName.lastIndexOf('.')), 
        "songPath"  -> f.getPath
      ).asInstanceOf[HashMap[String,String]]
      */
      val song = new HashMap[String, String]();
      song.put("songTitle", f.getName.substring(0, f.getName.lastIndexOf('.')))
      song.put("songPath", f.getPath)
      song
    }.foreach(m => returnToJava.add(m))
    returnToJava
  }
	
  /**
   * Class to filter files which are having .mp3 extension
   * */
  class FileExtensionFilter extends FilenameFilter {
    def accept(dir: File, name: String) = name.endsWith(".mp3") || name.endsWith(".MP3")
  }
}
