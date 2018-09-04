package com.anwesh.uiprojects.linkedbouncedownballview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.bouncedownballview.BounceDownBallView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BounceDownBallView.create(this)
    }
}
