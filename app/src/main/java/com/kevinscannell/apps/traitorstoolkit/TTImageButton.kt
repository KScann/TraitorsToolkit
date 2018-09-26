package com.kevinscannell.apps.traitorstoolkit

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import java.util.concurrent.CopyOnWriteArrayList

class TTImageButton : ImageButton {

    var mCharacterCode: String = ""

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs){
        context.theme.obtainStyledAttributes(attrs, R.styleable.TTImageButton, 0, 0).apply {
            try{
                mCharacterCode = getString(R.styleable.TTImageButton_character_code)
            } finally {
                recycle()
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int) : super(context, attrs, defStyleAttr){
        context.theme.obtainStyledAttributes(attrs, R.styleable.TTImageButton, 0, 0).apply {
            try{
                mCharacterCode = getString(R.styleable.TTImageButton_character_code)
            } finally {
                recycle()
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr : Int, defStyleRes : Int) : super(context, attrs, defStyleAttr, defStyleRes)

}