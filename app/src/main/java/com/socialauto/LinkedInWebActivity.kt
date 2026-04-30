package com.socialauto

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LinkedInWebActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var etPostContent: EditText
    private lateinit var btnInject: Button
    private lateinit var btnSubmit: Button
    private lateinit var btnOpenFeed: Button
    private lateinit var tvStatus: TextView
    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linkedin)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        etPostContent = findViewById(R.id.etPostContent)
        btnInject = findViewById(R.id.btnInject)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnOpenFeed = findViewById(R.id.btnOpenFeed)
        tvStatus = findViewById(R.id.tvLinkedInStatus)

        // WebView setup
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            userAgentString = "Mozilla/5.0 (Linux; Android 14; SM-S931B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                tvStatus.text = when {
                    url?.contains("/feed") == true -> "Feed loaded. Ready to post."
                    url?.contains("/login") == true -> "Please sign in to LinkedIn"
                    else -> "Loaded: ${url?.take(40)}..."
                }
                if (url?.contains("/feed") == true) {
                    injectPostButtonFinder()
                }
            }
        }

        // JavaScript Bridge
        webView.addJavascriptInterface(LinkedInJsBridge(), "SocialAuto")

        // Load LinkedIn
        webView.loadUrl("https://www.linkedin.com/login")

        btnOpenFeed.setOnClickListener {
            webView.loadUrl("https://www.linkedin.com/feed/")
        }

        btnInject.setOnClickListener {
            val content = etPostContent.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "Enter post content first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            hideKeyboard()
            openComposerAndFill(content)
        }

        btnSubmit.setOnClickListener {
            hideKeyboard()
            submitPost()
        }
    }

    private fun openComposerAndFill(content: String) {
        tvStatus.text = "Opening composer..."
        // Try to click the "Start a post" or "Create post" button
        val js = """
            (function() {
                var buttons = document.querySelectorAll('button');
                for (var i = 0; i < buttons.length; i++) {
                    var txt = buttons[i].innerText || buttons[i].textContent || '';
                    var aria = buttons[i].getAttribute('aria-label') || '';
                    if (txt.toLowerCase().indexOf('start a post') !== -1 || 
                        txt.toLowerCase().indexOf('create a post') !== -1 ||
                        aria.toLowerCase().indexOf('start a post') !== -1 ||
                        txt.toLowerCase().indexOf('post') !== -1 && buttons[i].className.indexOf('share-box') !== -1) {
                        buttons[i].click();
                        return 'clicked';
                    }
                }
                // Try data-test-id patterns
                var shareBox = document.querySelector('[data-test-id="share-creation-form__header"]');
                if (shareBox) return 'already_open';
                var altBtn = document.querySelector('.share-box-feed-entry__wrapper button');
                if (altBtn) { altBtn.click(); return 'clicked_alt'; }
                return 'not_found';
            })();
        """.trimIndent()
        webView.evaluateJavascript(js) { result ->
            mainHandler.postDelayed({ fillComposer(content) }, 1500)
        }
    }

    private fun fillComposer(content: String) {
        tvStatus.text = "Filling content..."
        val escaped = content.replace("\\", "\\\\").replace("'", "\\'").replace("\n", "\\n")
        val js = """
            (function() {
                var editor = document.querySelector('.ql-editor');
                if (!editor) editor = document.querySelector('[role="textbox"]');
                if (!editor) editor = document.querySelector('.editor-content');
                if (!editor) editor = document.querySelector('[data-test-id="share-creation-form__content-editable"]');
                if (editor) {
                    editor.focus();
                    editor.innerHTML = '<p>$escaped</p>';
                    var evt = document.createEvent('HTMLEvents');
                    evt.initEvent('input', true, true);
                    editor.dispatchEvent(evt);
                    return 'filled';
                }
                return 'editor_not_found';
            })();
        """.trimIndent()
        webView.evaluateJavascript(js) { result ->
            tvStatus.text = if (result?.contains("filled") == true) {
                "Content injected. Tap Submit when ready."
            } else {
                "Could not find editor. Try manually clicking 'Start a post' first."
            }
        }
    }

    private fun submitPost() {
        tvStatus.text = "Submitting post..."
        val js = """
            (function() {
                var buttons = document.querySelectorAll('button');
                for (var i = 0; i < buttons.length; i++) {
                    var txt = buttons[i].innerText || buttons[i].textContent || '';
                    if (txt.toLowerCase().trim() === 'post') {
                        if (!buttons[i].disabled) {
                            buttons[i].click();
                            return 'posted';
                        } else {
                            return 'button_disabled';
                        }
                    }
                }
                var submitBtn = document.querySelector('[data-test-id="share-creation-form__submit-button"]');
                if (submitBtn && !submitBtn.disabled) {
                    submitBtn.click();
                    return 'posted_alt';
                }
                return 'post_button_not_found';
            })();
        """.trimIndent()
        webView.evaluateJavascript(js) { result ->
            val msg = when {
                result?.contains("posted") == true -> "Post submitted!"
                result?.contains("disabled") == true -> "Post button is disabled. Add content first."
                else -> "Could not find Post button."
            }
            tvStatus.text = msg
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun injectPostButtonFinder() {
        // Inject helper JS that can be called from native
        val js = """
            window.SocialAutoFindPostButton = function() {
                var buttons = document.querySelectorAll('button');
                for (var i = 0; i < buttons.length; i++) {
                    var txt = buttons[i].innerText || '';
                    if (txt.toLowerCase().trim() === 'post' && !buttons[i].disabled) return true;
                }
                return false;
            };
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etPostContent.windowToken, 0)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    inner class LinkedInJsBridge {
        @JavascriptInterface
        fun onPageChanged(url: String) {
            mainHandler.post {
                tvStatus.text = "Navigated to: ${url.take(50)}"
            }
        }
    }
}