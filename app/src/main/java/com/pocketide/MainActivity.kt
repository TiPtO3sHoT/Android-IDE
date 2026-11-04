
package com.pocketide

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.res.ColorStateList
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage

import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme

class MainActivity : Activity() {

    private lateinit var editor: CodeEditor
    private lateinit var output: TextView
    private lateinit var languagePicker: Spinner
    private lateinit var previewWebView: WebView
    private lateinit var jsRuntime: WebView

    private var darkTheme = true
    private var editorSize = 16f
    private var jsReady = false

    private val SAVE_FILE_REQUEST = 1001
    private val OPEN_FILE_REQUEST = 1002

    private val languages = arrayOf(
        "Kotlin", "Java", "Python", "HTML",
        "CSS", "JavaScript", "C", "C++"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SCREEN SIZE HELPER
        fun dp(value: Int): Int {
            return (value * resources.displayMetrics.density).toInt()
        }

        // ROOT
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(4), dp(2), dp(4), dp(2))
        }

        // TITLE
        val title = TextView(this).apply {
            text = "Pocket IDE"
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, dp(2), 0, dp(2))
        }

        // CONSOLE
        output = TextView(this).apply {
            text = "Pocket IDE ready!"
            textSize = 13f
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        val outputTitle = TextView(this).apply {
            text = "Console Output"
            textSize = 14f
            setPadding(dp(6), dp(4), dp(6), dp(4))
        }

        // PREVIEW
        previewWebView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            visibility = View.GONE
        }

        previewWebView.webChromeClient =
            object : WebChromeClient() {
                override fun onConsoleMessage(
                    consoleMessage: ConsoleMessage
                ): Boolean {
                    runOnUiThread {
                        output.append(
                            "\n" + consoleMessage.message()
                        )
                    }
                    return true
                }
            }

        // JAVASCRIPT ENGINE
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

        jsRuntime.webViewClient =
            object : WebViewClient() {
                override fun onPageFinished(
                    view: WebView?,
                    url: String?
                ) {
                    jsReady = true
                }
            }

        jsRuntime.loadUrl("about:blank")

        // LANGUAGE PICKER
        languagePicker = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                languages
            ).apply {
                setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
            }
        }

        // CODE EDITOR
        editor = CodeEditor(this).apply {
            typefaceText = Typeface.MONOSPACE
            textSizePx = editorSize
            setLineNumberEnabled(true)
        }

        // BUTTON ROWS
        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val settings = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        // BUTTON CREATOR
        fun makeButton(
            label: String,
            action: () -> Unit
        ): Button {
            return Button(this).apply {
                text = label
                textSize = 10f
                isEnabled = true
                isClickable = true
                minWidth = 0
                minimumWidth = 0
                minimumHeight = dp(34)
                setPadding(dp(2), 0, dp(2), 0)

                setOnClickListener {
                    Toast.makeText(
                        this@MainActivity,
                        "$label clicked",
                        Toast.LENGTH_SHORT
                    ).show()

                    action()
                }
            }
        }

        // MAIN BUTTONS
        val runButton = makeButton("Run") {
            runSelectedCode()
        }

        val clearButton = makeButton("Clear") {
            editor.setText("")
            output.text = ""

            previewWebView.stopLoading()
            previewWebView.loadUrl("about:blank")
            previewWebView.visibility = View.GONE
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

        // THEME BUTTON
        lateinit var themeButton: Button

        themeButton = makeButton("Light") {
            darkTheme = !darkTheme
            applyTheme(root, title)

            themeButton.text =
                if (darkTheme) "Light" else "Dark"
        }

        // FONT BUTTONS
        val smallerButton = makeButton("A-") {
            editorSize =
                (editorSize - 1f).coerceAtLeast(10f)
            editor.textSizePx = editorSize
        }

        val largerButton = makeButton("A+") {
            editorSize =
                (editorSize + 1f).coerceAtMost(32f)
            editor.textSizePx = editorSize
        }

        // ADD MAIN BUTTONS WITH EQUAL WIDTH
        val mainButtonParams =
            LinearLayout.LayoutParams(
                0,
                dp(38),
                1f
            )

        buttons.addView(runButton, mainButtonParams)
        buttons.addView(clearButton, mainButtonParams)
        buttons.addView(infoButton, mainButtonParams)
        buttons.addView(saveButton, mainButtonParams)
        buttons.addView(openButton, mainButtonParams)

        // SETTINGS BUTTONS
        val settingButtonParams =
            LinearLayout.LayoutParams(
                0,
                dp(34),
                1f
            )

        settings.addView(themeButton, settingButtonParams)
        settings.addView(smallerButton, settingButtonParams)
        settings.addView(largerButton, settingButtonParams)

        // EDITOR PAGE
        val editorPage = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        editorPage.addView(title)

        editorPage.addView(
            languagePicker,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(38)
            )
        )

        editorPage.addView(buttons)
        editorPage.addView(settings)

        // EDITOR GETS REMAINING SCREEN SPACE
        editorPage.addView(
            editor,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        // RESULTS PAGE
        val resultsPage = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        // VIEW RESULTS
        val resultsButton = makeButton("View Results") {
            editorPage.visibility = View.GONE
            resultsPage.visibility = View.VISIBLE
        }

        editorPage.addView(
            resultsButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(40)
            )
        )

        // BACK BUTTON
        val backButton = makeButton("Back to Editor") {
            resultsPage.visibility = View.GONE
            editorPage.visibility = View.VISIBLE
        }

        resultsPage.addView(backButton)
        resultsPage.addView(outputTitle)

        resultsPage.addView(
            output,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        resultsPage.addView(
            previewWebView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                2f
            )
        )

        // ADD PAGES
        root.addView(
            editorPage,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            resultsPage,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        setContentView(root)
        applyTheme(root, title)
    }

    // RUN CODE
    private fun runSelectedCode() {

        val language =
            languagePicker.selectedItem.toString()

        val code = editor.text.toString()

        if (code.isBlank()) {
            output.text = "Nothing to run."
            previewWebView.visibility = View.GONE
            return
        }

        output.text = ""
        previewWebView.visibility = View.GONE

        when (language) {

            "HTML" -> {
                output.text = "HTML Preview"
                previewWebView.visibility = View.VISIBLE

                previewWebView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    code,
                    "text/html",
                    "UTF-8",
                    null
                )
            }

            "CSS" -> {
                output.text = "CSS Preview"

                val html = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1">
                        <style>
                            body {
                                font-family: sans-serif;
                                padding: 16px;
                            }
                            $code
                        </style>
                    </head>
                    <body>
                        <h2>CSS Preview</h2>
                        <p>Your CSS styles are applied here.</p>
                        <button>Sample Button</button>
                        <div class="box">Sample Box</div>
                    </body>
                    </html>
                """.trimIndent()

                previewWebView.visibility = View.VISIBLE

                previewWebView.loadDataWithBaseURL(
                    "https://appassets.androidplatform.net/",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }

            "JavaScript" -> {

                if (!jsReady) {
                    output.text =
                        "JavaScript engine is starting. Try again."
                    return
                }

                output.text = "JavaScript Console\n"

                val wrappedCode = """
                    (function() {
                        console.log = function(...args) {
                            AndroidConsole.print(args.join(" "));
                        };

                        console.error = function(...args) {
                            AndroidConsole.print("Error: " + args.join(" "));
                        };

                        console.warn = function(...args) {
                            AndroidConsole.print("Warning: " + args.join(" "));
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

            else -> {
                output.text =
                    "$language execution is not available yet.\n" +
                    "HTML, CSS, and JavaScript are supported."
            }
        }
    }

    // SAVE PROJECT
    private fun saveProject() {

        val extension =
            when (languagePicker.selectedItem.toString()) {
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

        val intent =
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"

                putExtra(
                    Intent.EXTRA_TITLE,
                    "untitled.$extension"
                )
            }

        startActivityForResult(
            intent,
            SAVE_FILE_REQUEST
        )
    }

    // OPEN PROJECT
    private fun openProject() {

        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }

        startActivityForResult(
            intent,
            OPEN_FILE_REQUEST
        )
    }

    // FILE RESULTS
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (resultCode != RESULT_OK || data?.data == null) {
            return
        }

        val uri = data.data!!

        try {
            when (requestCode) {

                SAVE_FILE_REQUEST -> {
                    contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(
                            editor.text.toString()
                                .toByteArray(Charsets.UTF_8)
                        )
                    }

                    output.text = "Project saved successfully!"
                }

                OPEN_FILE_REQUEST -> {
                    contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { reader ->
                            editor.setText(reader.readText())
                        }

                    output.text = "Project opened successfully!"
                }
            }
        } catch (e: Exception) {
            output.text = "File error: ${e.message}"
        }
    }

    // THEME
    private fun applyTheme(
        root: LinearLayout,
        title: TextView
    ) {

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

        languagePicker.setBackgroundTintList(
            ColorStateList.valueOf(foreground)
        )

        output.setTextColor(
            if (darkTheme)
                Color.rgb(120, 255, 160)
            else
                Color.rgb(20, 110, 55)
        )

        output.setBackgroundColor(
            if (darkTheme)
                Color.BLACK
            else
                Color.rgb(225, 230, 235)
        )

        val buttonColor =
            if (darkTheme) Color.WHITE
            else Color.DKGRAY

        val buttonTextColor =
            if (darkTheme) Color.BLACK
            else Color.WHITE

        // STYLE ALL BUTTONS ON BOTH PAGES
        fun styleButtons(view: View) {

            if (view is Button) {
                view.setBackgroundTintList(
                    ColorStateList.valueOf(buttonColor)
                )

                view.setTextColor(buttonTextColor)
            }

            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    styleButtons(view.getChildAt(i))
                }
            }
        }

        styleButtons(root)

        // EDITOR COLORS
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
            if (darkTheme)
                Color.rgb(45, 48, 60)
            else
                Color.rgb(235, 240, 250)
        )

        scheme.setColor(
            EditorColorScheme.KEYWORD,
            if (darkTheme)
                Color.rgb(120, 160, 255)
            else
                Color.rgb(30, 75, 190)
        )

        scheme.setColor(
            EditorColorScheme.LITERAL,
            if (darkTheme)
                Color.rgb(180, 220, 140)
            else
                Color.rgb(30, 130, 65)
        )

        scheme.setColor(
            EditorColorScheme.COMMENT,
            if (darkTheme)
                Color.rgb(130, 145, 155)
            else
                Color.rgb(110, 120, 130)
        )

        scheme.setColor(
            EditorColorScheme.OPERATOR,
            if (darkTheme)
                Color.rgb(255, 190, 120)
            else
                Color.rgb(170, 85, 20)
        )
    }

    // CLEANUP
    override fun onDestroy() {

        if (::editor.isInitialized) {
            editor.release()
        }

        if (::jsRuntime.isInitialized) {
            jsRuntime.stopLoading()
            jsRuntime.loadUrl("about:blank")
            jsRuntime.removeAllViews()
            jsRuntime.destroy()
        }

        if (::previewWebView.isInitialized) {
            previewWebView.stopLoading()
            previewWebView.loadUrl("about:blank")
            previewWebView.removeAllViews()
            previewWebView.destroy()
        }

        super.onDestroy()
    }
}
