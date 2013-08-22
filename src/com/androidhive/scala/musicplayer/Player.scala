package com.androidhive.scala.musicplayer

object Player {
  val mp = new android.media.MediaPlayer
  val playList = SongsManager.getPlayList
  val seekForwardTime  = 5000 //milliseconds
  val seekBackwardTime = 5000 //milliseconds
  var isShuffle = false
  var isRepeat  = false
  private var index = 0
  
  def forword() = mp.getCurrentPosition match {
    case(cp: Int) if cp + seekForwardTime <= mp.getDuration => mp.seekTo(cp + seekForwardTime) //forward position
    case _ => mp.seekTo(mp.getDuration) //forward to end position
  }
  
  def backward() = mp.getCurrentPosition match {
    case(cp: Int) if cp - seekBackwardTime >= 0 => mp.seekTo(cp - seekBackwardTime) //backward position
    case _ => mp.seekTo(0) //backward to starting position
  }
  
  def togglePlaying() { if(mp.isPlaying) mp.pause else mp.start }
  def setIndex(i: Int) { index = i; mp.reset; mp.setDataSource(playList.get(i).get("songPath")); mp.prepare }
  def setRandomIndex() { setIndex((new scala.util.Random).nextInt(playList.size)) }
  def initialize() = setIndex(0)
  def getTitle() = playList.get(index).get("songTitle")
  def getTimeStringByPercentage(p: Int) = Utilities.progressToTimeString(p, mp.getDuration)
  def getProgressPercentage() = Utilities.getProgressPercentage(mp.getCurrentPosition, mp.getDuration)
  def getCurrentTimeString()  = Utilities.milliSecondsToTimeString(mp.getCurrentPosition)
  def getTotalTimeString()  = Utilities.milliSecondsToTimeString(mp.getDuration)
  def hasNext() = index < playList.size - 1
  def hasPrevious() = index > 0
  def next()   { if(hasNext)   setIndex(index + 1) else setIndex(0) }
  def previous() { if(hasPrevious) setIndex(index - 1) else setIndex(playList.size - 1) }
} //end object Player