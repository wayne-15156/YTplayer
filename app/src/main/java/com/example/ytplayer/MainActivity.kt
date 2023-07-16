package com.example.ytplayer

import android.graphics.PixelFormat
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.example.ytplayer.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val wid = displayMetrics.widthPixels
        val hei = wid * 9 / 16

        Log.e("123", "wid: $wid")
        Log.e("123", "hei: $hei")

        val iframeHtml =
            "<html>\n" +
                "<body>\n" +
                "    <iframe width=\"${wid}\" height=\"${hei}\" src=\"https://www.youtube.com/embed/9nhhQhAxhjo\" title=\"YouTube video player\"\n" +
                "        frameborder=\"0\"\n" +
                "        allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share\"\n" +
                "        allowfullscreen>\n" +
                "    </iframe>\n" +
                "</body>\n" +
            "</html>"

        val webSettings: WebSettings? = binding?.webView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.builtInZoomControls = true

        binding?.webView?.webViewClient = WebViewClient()
        binding?.webView?.loadData(iframeHtml, "text/html", "utf-8")

    }
}