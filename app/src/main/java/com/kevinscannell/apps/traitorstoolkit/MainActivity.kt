package com.kevinscannell.apps.traitorstoolkit

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun showCharacter(view: View){
        if( view is TTButton ){
            val characterIntent = Intent(this, CharacterActivity::class.java)
            characterIntent.putExtra(CharacterActivity.CHARACTER_CODE, view.mCharacterCode)
            startActivity(characterIntent)
        }
    }
}
