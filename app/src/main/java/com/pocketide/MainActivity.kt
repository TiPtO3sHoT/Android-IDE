
package com.pocketide

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var codeEditor: EditText
    private lateinit var outputView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(24, 24, 32))
            setPadding(16, 16, 16, 16)
        }

        val title = TextView(this).apply {
            text = "Pocket IDE"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        }

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val runButton = Button(this).apply {
            text = "Run"
            setOnClickListener {
                outputView.text =
                    "Pocket IDE is ready!\n\n" +
                    "Code execution will be added in a later stage."
            }
        }

        val clearButton = Button(this).apply {
            text = "Clear"
            setOnClickListener {
                codeEditor.setText("")
                outputView.text = "Output cleared."
            }
        }

        buttons.addView(runButton)
        buttons.addView(clearButton)

        codeEditor = EditText(this).apply {
            hint = "Write your code here..."
            setTextColor(Color.WHITE)
            setHintTextColor(Color.LTGRAY)
            setBackgroundColor(Color.rgb(35, 35, 45))
            gravity = Gravity.TOP or Gravity.START
            textSize = 14f
            typeface = android.graphics.Typeface.MONOSPACE
            setPadding(12, 12, 12, 12)
            minLines = 10
        }

        val outputTitle = TextView(this).apply {
            text = "Output"
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 8)
        }

        outputView = TextView(this).apply {
            text = "Welcome to Pocket IDE!"
            textSize = 14f
            setTextColor(Color.GREEN)
            setBackgroundColor(Color.BLACK)
            setPadding(12, 12, 12, 12)
        }

        root.addView(title)
        root.addView(
            buttons,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(
            codeEditor,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(outputTitle)
        root.addView(
            outputView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }
}
