package com.pocketide

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import android.webkit.WebView
import android.webkit.WebViewClient
import org.json.JSONTokener
import org.json.JSONObject
class MainActivity : Activity() {

    private lateinit var editor: CodeEditor
    private lateinit var output: TextView
    private lateinit var languagePicker: Spinner

    private var darkTheme = true
    private var editorSize = 16f
private val SAVE_FILE_REQUEST = 1001
private val OPEN_FILE_REQUEST = 1002
    private val languages = arrayOf(
        "Kotlin", "Java", "Python", "HTML",
        "CSS", "JavaScript", "C", "C++"
    )
    private lateinit var jsRuntime: WebView
    private var jsReady = false
    private val darkBackground = Color.rgb(30, 32, 40)
    private val lightBackground = Color.rgb(245, 246, 250)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
jsRuntime = WebView(this)

jsRuntime.settings.javaScriptEnabled = true

jsRuntime.addJavascriptInterface(
    object {
        @android.webkit.JavascriptInterface
        fun print(message: String) {
            runOnUiThread {
                output.append("\n$message")
            }
        }
    },
    "AndroidConsole"
)

jsRuntime.webViewClient = object : WebViewClient() {
    override fun onPageFinished(
        view: WebView?,
        url: String?
    ) {
        jsReady = true
    }
}

jsRuntime.loadUrl("about:blank")
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 12, 12, 12)
        }

        val title = TextView(this).apply {
            text = "Pocket IDE"
            textSize = 25f
            gravity = Gravity.CENTER
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
            setText(
                "// Welcome to Pocket IDE!\n" +
                "// Select a language and start coding.\n"
            )
            typefaceText = Typeface.MONOSPACE
            textSizePx = editorSize
            setLineNumberEnabled(true)
        }

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val settings = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        fun makeButton(label: String, action: () -> Unit): Button {
            return Button(this).apply {
                text = label
                textSize = 12f
                setOnClickListener { action() }
            }
        }
        val runButton = makeButton("Run") {
    runSelectedCode()
}

        val clearButton = makeButton("Clear") {
            editor.setText("")
            output.text = "Editor cleared."
        }

        val infoButton = makeButton("Info") {
            output.text =
                "Pocket IDE\nEditor ready.\n" +
                "Language: ${languagePicker.selectedItem}"
        }
        val saveButton = makeButton("Save") {
    saveProject()
}

        val openButton = makeButton("Open") {
    openProject()
}
lateinit var themeButton: Button

themeButton = makeButton("Light Mode") {
    darkTheme = !darkTheme
    applyTheme(root, title)
    themeButton.text =
        if (darkTheme) "Light Mode" else "Dark Mode"
}

        val smallerButton = makeButton("A-") {
            editorSize = (editorSize - 1f).coerceAtLeast(10f)
            editor.textSizePx = editorSize
        }

        val largerButton = makeButton("A+") {
            editorSize = (editorSize + 1f).coerceAtMost(32f)
            editor.textSizePx = editorSize
        }

        buttons.addView(runButton)
        buttons.addView(clearButton)
        buttons.addView(infoButton)
        buttons.addView(saveButton)
        buttons.addView(openButton)
        settings.addView(themeButton)
        settings.addView(smallerButton)
        settings.addView(largerButton)

        val outputTitle = TextView(this).apply {
            text = "Console Output"
            textSize = 17f
            setPadding(0, 12, 0, 8)
        }

        output = TextView(this).apply {
            text = "Pocket IDE ready!"
            textSize = 14f
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
        root.addView(settings)

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

        applyTheme(root, title)
    }
private fun runSelectedCode() {
    val language = languagePicker.selectedItem.toString()
    val code = editor.text.toString()

    if (code.isBlank()) {
        output.text = "Nothing to run."
        return
    }

    if (language != "JavaScript") {
        output.text =
            "$language execution is not available yet.\n" +
            "JavaScript is our first execution engine."
        return
    }

    output.text = "Running JavaScript..."
            if (!jsReady) {
    output.text = "JavaScript engine is still starting. Try again shortly."
    return
}

    val wrappedCode = """
        (function() {
            const originalLog = console.log;
            console.log = function(...args) {
                AndroidConsole.print(args.join(" "));
            };

            try {
                $code
            } catch (error) {
                AndroidConsole.print("Error: " + error.message);
            }
        })();
    """.trimIndent()

    jsRuntime.evaluateJavascript(
        wrappedCode,
        null
    )
}
    private fun saveProject() {
    val extension = when (languagePicker.selectedItem.toString()) {
        "Kotlin" -> "kt"
        "Java" -> "java"
        "Python" -> "py"
        "HTML" -> "html"
        "CSS" -> "css"
        "JavaScript" -> "js"
        "C" -> "c"
        "C++" -> "cpp"
        else -> "txt"
    }

    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TITLE,
            "untitled.$extension"
        )
    }

    startActivityForResult(intent, SAVE_FILE_REQUEST)
}

private fun openProject() {
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "*/*"
    }

    startActivityForResult(intent, OPEN_FILE_REQUEST)
}

override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode != RESULT_OK || data?.data == null) {
        return
    }

    val uri = data.data!!

    try {
        when (requestCode) {
            SAVE_FILE_REQUEST -> {
                contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(
                        editor.text.toString().toByteArray(Charsets.UTF_8)
                    )
                }

                output.text = "Project saved successfully!"
            }

            OPEN_FILE_REQUEST -> {
                contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                    editor.setText(reader.readText())
                }

                output.text = "Project opened successfully!"
            }
        }
    } catch (e: Exception) {
        output.text = "File error: ${e.message}"
    }
}
            private fun applyTheme(
        root: LinearLayout,
        title: TextView
    )  {
        val background: Int
        val foreground: Int
        val editorBackground: Int
        val editorForeground: Int

        if (darkTheme) {
            background = Color.rgb(30, 32, 40)
            foreground = Color.WHITE
            editorBackground = Color.rgb(25, 27, 35)
            editorForeground = Color.rgb(230, 230, 240)
        } else {
            background = Color.rgb(245, 246, 250)
            foreground = Color.rgb(30, 30, 35)
            editorBackground = Color.WHITE
            editorForeground = Color.rgb(35, 35, 45)
        }

        root.setBackgroundColor(background)
        title.setTextColor(foreground)
        output.setTextColor(
            if (darkTheme) Color.rgb(120, 255, 160)
            else Color.rgb(20, 110, 55)
        )

        output.setBackgroundColor(
            if (darkTheme) Color.BLACK else Color.rgb(225, 230, 235)
        )

        output.setTextColor(
            if (darkTheme) Color.rgb(120, 255, 160)
            else Color.rgb(20, 110, 55)
        )

        val scheme = editor.colorScheme

        scheme.setColor(
            EditorColorScheme.WHOLE_BACKGROUND,
            editorBackground
        )

        scheme.setColor(
            EditorColorScheme.TEXT_NORMAL,
            editorForeground
        )

        scheme.setColor(
            EditorColorScheme.LINE_NUMBER,
            Color.rgb(130, 140, 155)
        )

        scheme.setColor(
            EditorColorScheme.LINE_NUMBER_BACKGROUND,
            editorBackground
        )

        scheme.setColor(
            EditorColorScheme.CURRENT_LINE,
            if (darkTheme) Color.rgb(45, 48, 60)
            else Color.rgb(235, 240, 250)
        )

        scheme.setColor(
            EditorColorScheme.KEYWORD,
            if (darkTheme) Color.rgb(120, 160, 255)
            else Color.rgb(30, 75, 190)
        )

        scheme.setColor(
            EditorColorScheme.LITERAL,
            if (darkTheme) Color.rgb(180, 220, 140)
            else Color.rgb(30, 130, 65)
        )

        scheme.setColor(
            EditorColorScheme.COMMENT,
            if (darkTheme) Color.rgb(130, 145, 155)
            else Color.rgb(110, 120, 130)
        )

        scheme.setColor(
            EditorColorScheme.OPERATOR,
            if (darkTheme) Color.rgb(255, 190, 120)
            else Color.rgb(170, 85, 20)
        )
    }

    override fun onDestroy() {
    editor.release()

    jsRuntime.stopLoading()
    jsRuntime.loadUrl("about:blank")
    jsRuntime.removeAllViews()
    jsRuntime.destroy()

    super.onDestroy()
}
}
