package com.androidhive.scala.musicplayer

import android.os.Bundle
import android.view.View
import android.widget._

class PlayListActivity extends android.app.ListActivity  {
  // get all songs from sdcard
  val songsList = SongsManager.getPlayList

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.playlist)

    // Adding menuItems to ListView
    setListAdapter(new SimpleAdapter(
      this,
      songsList,
      R.layout.playlist_item,
      Array("songTitle"),
      Array(R.id.songTitle)
    ))

    // listening to single listitem click
    getListView.setOnItemClickListener(new AdapterView.OnItemClickListener {
      override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
        // Starting new intent
        val in = new android.content.Intent(getApplicationContext(), classOf[AndroidBuildingMusicPlayerActivity])
        
        // Sending songIndex to PlayerActivity
        setResult(100, in.putExtra("songIndex", position))
        finish();
      }// end onItemClick
    })// end setOnItemClickListener
  }// end onCreate
  
}