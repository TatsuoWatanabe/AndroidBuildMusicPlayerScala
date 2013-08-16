package com.androidhive.scala.musicplayer

import android.widget.SimpleAdapter
import android.widget.AdapterView

class PlayListActivity extends android.app.ListActivity  {
  // get all songs from sdcard
  val playList = SongsManager.getPlayList

  override protected def onCreate(savedInstanceState: android.os.Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.playlist)

    // Adding menuItems to ListView
    setListAdapter(new SimpleAdapter(
      this,
      playList,
      R.layout.playlist_item,
      Array("songTitle"),
      Array(R.id.songTitle)
    ))

    // listening to single listitem click
    getListView.setOnItemClickListener(new AdapterView.OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: android.view.View, position: Int, id: Long) {
        // Starting new intent
        val in = new android.content.Intent(getApplicationContext(), classOf[AndroidBuildingMusicPlayerActivity])
        
        // Sending songIndex to PlayerActivity
        setResult(100, in.putExtra("songIndex", position))
        finish
      }// end onItemClick
    })// end setOnItemClickListener
  }// end onCreate
  
}