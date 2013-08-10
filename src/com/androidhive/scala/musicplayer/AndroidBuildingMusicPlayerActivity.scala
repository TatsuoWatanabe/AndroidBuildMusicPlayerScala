package com.androidhive.scala.musicplayer

import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class AndroidBuildingMusicPlayerActivity extends android.app.Activity with OnCompletionListener with SeekBar.OnSeekBarChangeListener {
  // Handler to update UI timer, progress bar etc,.
  private val mHandler = new android.os.Handler

  // All player buttons
  private object Btns {
    val btnPlay                  = findViewById(R.id.btnPlay).asInstanceOf[ImageButton]
    val btnForward               = findViewById(R.id.btnForward).asInstanceOf[ImageButton]
    val btnBackward              = findViewById(R.id.btnBackward).asInstanceOf[ImageButton]
    val btnNext                  = findViewById(R.id.btnNext).asInstanceOf[ImageButton]
    val btnPrevious              = findViewById(R.id.btnPrevious).asInstanceOf[ImageButton]
    val btnPlaylist              = findViewById(R.id.btnPlaylist).asInstanceOf[ImageButton]
    val btnRepeat                = findViewById(R.id.btnRepeat).asInstanceOf[ImageButton]
    val btnShuffle               = findViewById(R.id.btnShuffle).asInstanceOf[ImageButton]
    val songProgressBar          = findViewById(R.id.songProgressBar).asInstanceOf[SeekBar]
    val songTitleLabel           = findViewById(R.id.songTitle).asInstanceOf[TextView]
    val songCurrentDurationLabel = findViewById(R.id.songCurrentDurationLabel).asInstanceOf[TextView]
    val songTotalDurationLabel   = findViewById(R.id.songTotalDurationLabel).asInstanceOf[TextView]
  }
  
  private object Player {
    val mp = new MediaPlayer
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
    
    def setIndex(i: Int) { index = i; mp.reset; mp.setDataSource(playList.get(i).get("songPath")); mp.prepare }
    def setRandomIndex() { index = (new scala.util.Random).nextInt(playList.size) }
    def initialize() = setIndex(0)
    def getTitle() = playList.get(index).get("songTitle")
    def getProgressPercentage() = Utilities.getProgressPercentage(mp.getCurrentPosition, mp.getDuration)
    def getCurrentTimeString()  = Utilities.milliSecondsToTimer(mp.getCurrentPosition)
    def getTotalTimeString()    = Utilities.milliSecondsToTimer(mp.getDuration)
    def hasNext() = index < playList.size - 1
    def hasPrevious() = index > 0
    def next()     { if(hasNext)     setIndex(index + 1) else setIndex(0) }
    def previous() { if(hasPrevious) setIndex(index - 1) else setIndex(playList.size - 1) }
  } //end object Player
  
  override protected def onCreate(savedInstanceState: android.os.Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.player)
    Player.initialize
    referPlayer
    
    // Listeners
    Btns.songProgressBar.setOnSeekBarChangeListener(this) // Important
    Player.mp.setOnCompletionListener(this) // Important
    
    /**
     * Play button click event
     */
    Btns.btnPlay.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) = Player.mp.isPlaying match {
        case true  => Player.mp.pause; referPlayer
        case false => startPlaying
      }
    })
    
    /**
     * Forward button click event
     * Forwards song specified seconds
     */
    Btns.btnForward.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) = Player.forword
    })
    
    /**
     * Backward button click event
     * Backward song to specified seconds
     */
    Btns.btnBackward.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) = Player.backward
    })
    
    /**
     * Next button click event
     */
    Btns.btnNext.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { Player.next; startPlaying }
    })
    
    /**
     * Back button click event
     */
    Btns.btnPrevious.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { Player.previous; startPlaying }
    })
    
    /**
     * Button Click event for Repeat button
     */
    Btns.btnRepeat.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { 
        Player.isRepeat = !Player.isRepeat
        if(Player.isRepeat){ Player.isShuffle = false } //make shuffle to false
        Toast.makeText(getApplicationContext(), "Repeat is " + (if(Player.isRepeat) "ON" else "OFF" ), Toast.LENGTH_SHORT).show()
        referPlayer
      }
    })
    
    /**
     * Button Click event for Shuffle button
     */
    Btns.btnShuffle.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) {
        Player.isShuffle = !Player.isShuffle
        if(Player.isShuffle){ Player.isRepeat = false } //make repeat to false
        Toast.makeText(getApplicationContext(), "Shuffle is " + (if(Player.isShuffle) "ON" else "OFF" ), Toast.LENGTH_SHORT).show()
        referPlayer
      }
    })
    
    /**
     * Button Click event for Play list click event
     * Launches list activity which displays list of songs
     */
    Btns.btnPlaylist.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { 
        val i = new Intent(getApplicationContext(), classOf[PlayListActivity])
        startActivityForResult(i, 100)
      }
    })
    
  } //end onCreate
  
  /**
   * Receiving song index from playlist view and play the song
   */
  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if(resultCode == 100){
      Player.setIndex(data.getExtras().getInt("songIndex"))
      startPlaying //play selected one
    }
  }
  
  /**
   * Function to play a song
   */
  def startPlaying() {
    try {
      Player.mp.start
      referPlayer
      updateProgressBar //Updating progress bar
    } catch {
      case e: IllegalArgumentException => e.printStackTrace
      case e: IllegalStateException    => e.printStackTrace
      case e: java.io.IOException      => e.printStackTrace
    }
  }
  
  /**
   * Update timer on seekbar
   */
  def updateProgressBar() = mHandler.postDelayed(mUpdateTimeTask, 100)
  
  /**
   * Background Runnable thread
   */
  val mUpdateTimeTask = new Runnable {
    override def run() {
      referPlayerProgress
      mHandler.postDelayed(this, 100) //Running this thread after 100 milliseconds
    }
  }
  
  /**
   * Refer Player progress to Btns
   */
  def referPlayerProgress {
    Btns.songProgressBar.setProgress(Player.getProgressPercentage) //Updating progress bar
    Btns.songCurrentDurationLabel.setText(Player.getCurrentTimeString) //Displaying time completed playing
  }
 
  /**
   * Refer all Player state to Btns
   */
  def referPlayer {
    referPlayerProgress
    Btns.songTitleLabel.setText(Player.getTitle) //Displaying Song title
    Btns.songTotalDurationLabel.setText(Player.getTotalTimeString) //Displaying Total Duration time
    Btns.btnPlay.setImageResource(if(Player.mp.isPlaying) R.drawable.btn_pause else R.drawable.btn_play) //Changing Button Image
    Btns.btnShuffle.setImageResource(if(Player.isShuffle) R.drawable.btn_shuffle_focused else R.drawable.btn_shuffle ) 
    Btns.btnRepeat.setImageResource(if(Player.isRepeat) R.drawable.btn_repeat_focused else R.drawable.btn_repeat )
    Btns.songProgressBar.setProgress(Player.getProgressPercentage) //set Progress bar values
    Btns.songProgressBar.setMax(100)    //set Progress bar values
  }
    
  /**
   * onProgressChanged
   */
  override def onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) { }

  /**
   * When user starts moving the progress handler
   */
  override def onStartTrackingTouch(seekBar: SeekBar) = mHandler.removeCallbacks(mUpdateTimeTask)
  
  /**
   * When user stops moving the progress hanlder
   */
  override def onStopTrackingTouch(seekBar: SeekBar) {
    mHandler.removeCallbacks(mUpdateTimeTask)
    val currentPosition = Utilities.progressToTimer(seekBar.getProgress, Player.mp.getDuration)

    Player.mp.seekTo(currentPosition) //forward or backward to certain seconds
    updateProgressBar //update timer progress again
  }

  /**
   * On Song Playing completed
   */
  override def onCompletion(arg0: MediaPlayer) = (Player.isRepeat, Player.isShuffle) match {
    case(true, _) => startPlaying //repeat is on play same song again
    case(false, true) => Player.setRandomIndex; startPlaying //shuffle is on - play a random song
    case(_, _) if Player.hasNext => Player.next; startPlaying // no repeat or shuffle ON - play next song
    case _ => Player.initialize; referPlayer //to first song
  }
  
  /**
   * onDestroy
   */
  override def onDestroy { super.onDestroy; Player.mp.release }
  
}