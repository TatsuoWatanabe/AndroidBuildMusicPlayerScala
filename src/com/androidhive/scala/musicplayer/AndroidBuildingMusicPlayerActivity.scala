package com.androidhive.scala.musicplayer

import android.util.Log
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.view.View
import android.view.KeyEvent
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.content.BroadcastReceiver
import android.content.Context

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

  override protected def onCreate(savedInstanceState: android.os.Bundle) {
    Log.d("Watch", "Watch -- onCreate!")
    super.onCreate(savedInstanceState)
    setContentView(R.layout.player)

    // Start listening for button presses
    // http://techbooster.jpn.org/andriod/ui/10298/
    val am = getSystemService(Context.AUDIO_SERVICE).asInstanceOf[android.media.AudioManager]
    val myEventReceiver = new android.content.ComponentName(getPackageName, classOf[RemoteControlReceiver].getName)
    am.registerMediaButtonEventReceiver(myEventReceiver)

    if(Player.playList.isEmpty){
      val s = SongsManager.MEDIA_PATH + " に音声ファイルが見つかりませんでした。"
      (new android.app.AlertDialog.Builder(this)).setMessage(s).setPositiveButton(android.R.string.ok, null).show()
      return
    }

    Player.initialize
    referPlayer

    // Listeners
    Btns.songProgressBar.setOnSeekBarChangeListener(this) // Important
    Player.mp.setOnCompletionListener(this) // Important

    /**
     * Play button click event
     */
    Btns.btnPlay.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { Player.togglePlaying; referPlayer }
    })

    /**
     * Forward button click event
     * Forwards song specified seconds
     */
    Btns.btnForward.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { Player.forword; referPlayerProgress }
    })

    /**
     * Backward button click event
     * Backward song to specified seconds
     */
    Btns.btnBackward.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) { Player.backward; referPlayerProgress }
    })

    /**
     * Next button click event
     */
    Btns.btnNext.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) = Player.mp.isPlaying match {
        case true  => Player.next; startPlaying
        case false => Player.next; referPlayer
      }
    })

    /**
     * Previous button click event
     */
    Btns.btnPrevious.setOnClickListener(new View.OnClickListener {
      override def onClick(arg0: View) = Player.mp.isPlaying match {
        case true  => Player.previous; startPlaying
        case false => Player.previous; referPlayer
      }
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
   * dispatchKeyEvent
   */
  override def dispatchKeyEvent(e: KeyEvent) = {
    Log.d("Watch", "Watch -- dispatchKeyEvent! keyCode = " + e.getKeyCode)
    (e.getAction, e.getKeyCode)  match {
      case (_, KeyEvent.KEYCODE_BACK) => false //don't destroy this activity
      case (KeyEvent.ACTION_UP, _ ) => referPlayer; super.dispatchKeyEvent(e) //for remote controller
      case (_, _) => super.dispatchKeyEvent(e)
    }
  }

  /**
   * Receiving song index from playlist view and play the song
   */
  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if(resultCode == 100){
      val afterFunc = if(Player.mp.isPlaying){ () => startPlaying }else{ () => referPlayer } // for keep playing state
      Player.setIndex(data.getExtras().getInt("songIndex"))
      afterFunc()
    }
  }

  /**
   * When user starts moving the progress handler
   */
  override def onStartTrackingTouch(seekBar: SeekBar) { stopUpdateProgressBar }

  /**
   * onProgressChanged
   */
  override def onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) = fromTouch match {
    case true => Btns.songCurrentDurationLabel.setText(Player.getTimeStringByPercentage(progress))
    case false =>
  }

  /**
   * When user stops moving the progress hanlder
   */
  override def onStopTrackingTouch(seekBar: SeekBar) {
    stopUpdateProgressBar //stop update timer progress
    val currentPosition = Utilities.progressToMilliSeconds(seekBar.getProgress, Player.mp.getDuration)

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
   * onStart
   */
  override def onStart {
    super.onStart
    Log.d("Watch", "Watch -- onStart!")
    referPlayer
    updateProgressBar

  }

  /**
   * onStop
   */
  override def onStop {
    super.onStop
    Log.d("Watch", "Watch -- onStop!")
    stopUpdateProgressBar
  }

  /**
   * onDestroy
   */
  override def onDestroy {
    super.onDestroy
    Log.d("Watch", "Watch -- onDestroy!")
    Player.mp.release
  }

  /**
   * Function to play a song
   */
  def startPlaying() {
    Player.mp.start
    referPlayer
  }

  /**
   * Function to pause a song
   */
  def pausePlaying() {
    Player.mp.pause
    referPlayer
  }

  /**
   * Update timer on seekbar
   */
  def updateProgressBar()     = mHandler.postDelayed(mUpdateTimeTask, 100)
  def stopUpdateProgressBar() = mHandler.removeCallbacks(mUpdateTimeTask)

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
    Log.d("Watch", "Watch -- referPlayer!")
    referPlayerProgress
    Btns.songTitleLabel.setText(Player.getTitle) //Displaying Song title
    Btns.songTotalDurationLabel.setText(Player.getTotalTimeString) //Displaying Total Duration time
    Btns.btnPlay.setImageResource(if(Player.mp.isPlaying) R.drawable.btn_pause else R.drawable.btn_play) //Changing Button Image
    Btns.btnShuffle.setImageResource(if(Player.isShuffle) R.drawable.btn_shuffle_focused else R.drawable.btn_shuffle )
    Btns.btnRepeat.setImageResource(if(Player.isRepeat) R.drawable.btn_repeat_focused else R.drawable.btn_repeat )
    Btns.songProgressBar.setProgress(Player.getProgressPercentage) //set Progress bar values
    Btns.songProgressBar.setMax(100)    //set Progress bar values
  }
} //end AndroidBuildingMusicPlayerActivity

/**
 * RemoteControlReceiver
 * http://firespeed.org/diary.php?diary=kenz-1519-junl18
 */
class RemoteControlReceiver extends BroadcastReceiver {
  override def onReceive(context: Context, intent: Intent){
    Log.d("Watch", "Watch -- onReceive!")
    if(intent.getAction != Intent.ACTION_MEDIA_BUTTON){ return }

    val keyEv = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT).asInstanceOf[KeyEvent]
    if(keyEv.getAction != KeyEvent.ACTION_DOWN){ return }

    val keyCode = keyEv.getKeyCode
    val KEYCODE_MEDIA_PLAY  = 126
    val KEYCODE_MEDIA_PAUSE = 127

    keyCode match {
      case KEYCODE_MEDIA_PLAY | KEYCODE_MEDIA_PAUSE | KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE => Player.togglePlaying
      case _ =>
    }
  }// end onReceive
}
