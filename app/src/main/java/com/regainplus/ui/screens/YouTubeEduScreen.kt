package com.regainplus.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubeEduScreen(videoId: String, title: String, onBack: () -> Unit) {
    val safeId = remember(videoId) { videoId.filter { it.isLetterOrDigit() || it == '_' || it == '-' } }
    val allowedUrl = remember(safeId) { "https://m.youtube.com/watch?v=$safeId" }
    val displayTitle = title.ifBlank { "Educational video" }
    var webView: WebView? by remember { mutableStateOf(null) }
    var progress by remember { mutableIntStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }

    BackHandler {
        val wv = webView
        if (wv != null && wv.canGoBack()) wv.goBack() else onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Educational YouTube", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(displayTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val wv = webView
                        if (wv != null && wv.canGoBack()) wv.goBack() else onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (progress in 1..99) {
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
            }
            Box(Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.mediaPlaybackRequiresUserGesture = false
                            settings.loadsImagesAutomatically = true
                            settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                            settings.userAgentString = settings.userAgentString.replace("; wv", "")
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress
                                }
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    canGoBack = view?.canGoBack() == true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    canGoBack = view?.canGoBack() == true
                                    view?.evaluateJavascript(hideNonCuratedJs(), null)
                                }

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val rawUrl = request?.url?.toString() ?: return false
                                    val uri = request.url
                                    val host = (uri.host ?: "").lowercase()
                                    val path = uri.path ?: ""
                                    if (!host.contains("youtube.com") && !host.contains("youtu.be") && !host.contains("googlevideo.com") && !host.contains("ytimg.com") && !host.contains("ggpht.com") && !host.contains("googleapis.com")) {
                                        return false
                                    }
                                    val vid = uri.getQueryParameter("v")
                                    val isAllowedWatch = path.startsWith("/watch") && (vid == safeId || vid == null)
                                    val isPlayerAsset = path.startsWith("/s/player") || path.startsWith("/youtubei") || path.startsWith("/api") || path.startsWith("/generate_204") || path.startsWith("/ptracking") || path.startsWith("/pagead") || host.contains("googlevideo.com") || host.contains("ytimg.com") || host.contains("ggpht.com")
                                    if (isAllowedWatch || isPlayerAsset) return false
                                    // Strict curated mode: block navigation to any other video/search/feed/channel.
                                    view?.loadUrl(allowedUrl)
                                    return true
                                }
                            }
                            loadUrl(allowedUrl)
                            webView = this
                        }
                    },
                    update = { wv -> webView = wv }
                )
            }
        }
    }
}

private fun hideNonCuratedJs(): String = """
    (function() {
      try {
        var styleId = 'regain-curated-hide';
        if (!document.getElementById(styleId)) {
          var style = document.createElement('style');
          style.id = styleId;
          style.innerHTML = `
            ytm-item-section-renderer[section-identifier="related-items"],
            ytm-watch-next-feed-renderer,
            .watch-below-the-player ytm-item-section-renderer,
            ytm-compact-autoplay-renderer,
            ytm-endscreen-renderer,
            .ytp-endscreen-content,
            .ytp-ce-element,
            ytm-pivot-bar-renderer,
            ytm-chip-cloud-renderer,
            ytm-comments-entry-point-header-renderer,
            ytm-engagement-panel-section-list-renderer { display: none !important; }
            a[href="/"], a[href^="/feed"], a[href^="/shorts"], a[href^="/explore"], a[href^="/results"], a[href^="/channel"], a[href^="/@"], a[href^="/playlist"] { pointer-events: none !important; }
          `;
          document.head && document.head.appendChild(style);
        }
        var selectors = [
          'ytm-item-section-renderer[section-identifier="related-items"]',
          'ytm-watch-next-feed-renderer',
          'ytm-compact-autoplay-renderer',
          'ytm-endscreen-renderer'
        ];
        selectors.forEach(function(s){ document.querySelectorAll(s).forEach(function(n){ n.remove(); }); });
      } catch(e) {}
    })();
""".trimIndent()

