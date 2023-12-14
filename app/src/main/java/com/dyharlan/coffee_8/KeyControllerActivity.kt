package com.dyharlan.coffee_8

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar


class KeyControllerActivity : AppCompatActivity() {
    //private var menu: Menu? = null
    private val defaultKeys: IntArray = intArrayOf(
        KeyEvent.KEYCODE_X,
        KeyEvent.KEYCODE_1,
        KeyEvent.KEYCODE_2,
        KeyEvent.KEYCODE_3,
        KeyEvent.KEYCODE_Q,
        KeyEvent.KEYCODE_W,
        KeyEvent.KEYCODE_E,
        KeyEvent.KEYCODE_A,
        KeyEvent.KEYCODE_S,
        KeyEvent.KEYCODE_D,
        KeyEvent.KEYCODE_Z,
        KeyEvent.KEYCODE_C,
        KeyEvent.KEYCODE_4,
        KeyEvent.KEYCODE_R,
        KeyEvent.KEYCODE_F,
        KeyEvent.KEYCODE_V
    )
    var bindableKeys: IntArray? = null
    lateinit var keyAdapter: BindableKeyListAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keycontroller)
        val toolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        toolbar.title = "Setup Physical Controls"
        bindableKeys = intent.getIntArrayExtra("bindableKeys")
        keyAdapter = BindableKeyListAdapter(this,resources.getStringArray(R.array.chip8_keys),bindableKeys)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.keycontroller_activitybar, menu)
        //this.menu = menu;
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            //R.id.menuHelp -> TODO()
            R.id.menuResetBindings -> resetBindingsDialog(bindableKeys, keyAdapter)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetBindingsDialog(bindableKeys: IntArray?, adapter: BindableKeyListAdapter){
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.confirm_reset_keybinds_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnYes = dialog.findViewById<Button>(R.id.btnYes)
        val btnNo = dialog.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            if (bindableKeys != null) {
                for((index, key) in defaultKeys.withIndex()){
                    bindableKeys[index] = key
                }
                adapter.notifyDataSetChanged()
            }
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

}