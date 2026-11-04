package com.pocketide

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import io.github.rosemoe.sora.widget.CodeEditor

class MainActivity : Activity() {

    private lateinit var editor: CodeEditor
    private lateinit var output: TextView
    private lateinit var languagePicker: Spinner

    private val languages = arrayOf(
        "Kotlin",
        "Java",
        "Python",
        "HTML",
        "CSS",
        "JavaScript",
        "C",
        "C++"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
            setBackgroundColor(Color.rgb(24, 24, 32))
        }

        val title = TextView(this).apply {
            text = "Pocket IDE"
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 12)
        }

        languagePicker = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                languages
            )
        }

        editor = CodeEditor(this).apply {
            setText("// Welcome to Pocket IDE!\n// Select a language and start coding.\n")
            typefaceText = Typeface.MONOSPACE
            textSizePx = 16f
            setLineNumberEnabled(true)
        }

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val runButton = Button(this).apply {
            text = "Run"
            setOnClickListener {
                output.text =
                    "Code saved in editor.\n" +
                    "Selected language: ${languagePicker.selectedItem}\n\n" +
                    "Execution engines will be added in a later stage."
            }
        }

        val clearButton = Button(this).apply {
            text = "Clear"
            setOnClickListener {
                editor.setText("")
                output.text = "Editor cleared."
            }
        }

        val saveButton = Button(this).apply {
            text = "Info"
            setOnClickListener {
                output.text =
                    "Pocket IDE\n" +
                    "Editor ready.\n" +
                    "Language: ${languagePicker.selectedItem}"
            }
        }

        buttons.addView(runButton)
        buttons.addView(clearButton)
        buttons.addView(saveButton)

        val outputTitle = TextView(this).apply {
            text = "Console Output"
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 8)
        }

        output = TextView(this).apply {
            text = "Pocket IDE ready!"
            textSize = 14f
            setTextColor(Color.GREEN)
            setBackgroundColor(Color.BLACK)
            setPadding(12, 12, 12, 12)
        }

        root.addView(title)

        root.addView(
            languagePicker,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(buttons)

        root.addView(
            editor,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        root.addView(outputTitle)
        root.addView(output)

        setContentView(root)
    }

    override fun onDestroy() {
        editor.release()
        super.onDestroy()
    }
}
