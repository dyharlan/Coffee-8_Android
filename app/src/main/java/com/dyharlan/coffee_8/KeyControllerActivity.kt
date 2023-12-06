package com.dyharlan.coffee_8

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class KeyControllerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keycontroller)
        val bindableKeys: IntArray? = intent.getIntArrayExtra("bindableKeys")
        val keyAdapter = BindableKeyListAdapter(this,resources.getStringArray(R.array.chip8_keys),bindableKeys)
        val bindableKeysLayout = findViewById<RecyclerView>(R.id.bindableKeyListView)
        bindableKeysLayout.layoutManager = LinearLayoutManager(this);
        bindableKeysLayout.adapter = keyAdapter

        this.onBackPressedDispatcher.addCallback(this) {
            val i = Intent(applicationContext, MainActivity::class.java)
            i.putExtra("bindableKeys",bindableKeys)
            setResult(RESULT_OK, i)
            finish()
        }
    }

}