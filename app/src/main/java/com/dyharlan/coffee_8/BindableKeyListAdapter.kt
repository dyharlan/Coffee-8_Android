package com.dyharlan.coffee_8

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView


class BindableKeyListAdapter(private val context: Context, private val keys: Array<String>, private val currentKeyBindings: IntArray?) :
    RecyclerView.Adapter<BindableKeyListAdapter.ViewHolder>() {

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val aKey: LinearLayout
        val chip8Key: TextView
        val boundTo: TextView
        init {
            // Define click listener for the ViewHolder's View
            aKey = view.findViewById(R.id.aKey)
            chip8Key = view.findViewById(R.id.chip8Key)
            boundTo = view.findViewById(R.id.boundTo)
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.bindable_key, viewGroup, false)

        return ViewHolder(view)
    }
    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.chip8Key.text = keys[position]
        viewHolder.boundTo.text = currentKeyBindings?.get(position)?.toString()
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.aKey.setOnClickListener {
            val dialog = object: Dialog(context) {
                override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
                    var isKeyInUse:Boolean = false

                    if(currentKeyBindings!= null){
                        var indexOfKey = 0
                        for(i in currentKeyBindings.indices){
                            if(currentKeyBindings[i] == keyCode){
                                isKeyInUse = true
                                indexOfKey = i
                                break
                            }
                        }
                        if(isKeyInUse){
                            Toast.makeText(context, "Key is in use by ${keys[indexOfKey]}!", Toast.LENGTH_SHORT).show()
                        }else{
                            currentKeyBindings[viewHolder.adapterPosition] = keyCode
                            viewHolder.boundTo.text = keyCode.toString()
                            this.dismiss()
                        }
                    }


                    return true
                }
            }
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.keybind_wait_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val dialogTitle = dialog.findViewById<TextView>(R.id.dialogTitle)
            dialogTitle.text = "Waiting for a keypress for ${keys[position]}"

            val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

            btnCancel.setOnClickListener{
                dialog.dismiss()
            }
            dialog.show()
        }
    }


    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = keys.size

}
