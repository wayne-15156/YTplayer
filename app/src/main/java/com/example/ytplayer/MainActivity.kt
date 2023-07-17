package com.example.ytplayer

import android.graphics.PixelFormat
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.webkit.JavascriptInterface
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


//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//
//        val wid = displayMetrics.widthPixels
//        val hei = wid * 9 / 16
//
//
//        Log.e("123", "wid: $wid")
//        Log.e("123", "hei: $hei")

        val iframeHtml =
            """
            <html>
                <body>
                <!-- 1. The <iframe> (and video player) will replace this <div> tag. -->
                <div id="player"></div>
            
                <script>
                  // 2. This code loads the IFrame Player API code asynchronously.
                  var tag = document.createElement('script');
            
                  tag.src = "https://www.youtube.com/iframe_api";
                  var firstScriptTag = document.getElementsByTagName('script')[0];
                  firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
            
                  // 3. This function creates an <iframe> (and YouTube player)
                  //    after the API code downloads.
                  var player;
                  function onYouTubeIframeAPIReady() {
                    player = new YT.Player('player', {
                      height: '100%',
                      width: '100%',
                      videoId: '9nhhQhAxhjo',
                      playerVars: {
                        'playsinline': 1
                      },
                      events: {
                        'onReady': onPlayerReady,
                        'onStateChange': onPlayerStateChange
                      }
                    });
                  }
            
                  // 4. The API will call this function when the video player is ready.
                  function onPlayerReady(event) {
                    event.target.playVideo();
                  }
            
                  // 5. The API calls this function when the player's state changes.
                  //    The function indicates that when playing a video (state=1),
                  //    the player should play for six seconds and then stop.
                  var done = false;
                  function onPlayerStateChange(event) {
                    if (event.data == YT.PlayerState.PLAYING && !done) {
                      //setTimeout(stopVideo, 6000);
                      done = true;
                    }
                  }
                  function stopVideo() {
                    player.stopVideo();
                  }
                </script>
              </body>
            </html>
            """.trimIndent()

        val webSettings: WebSettings? = binding?.webView?.settings
        webSettings?.javaScriptEnabled = true
        //webSettings?.builtInZoomControls = true

        binding?.webView?.webViewClient = WebViewClient()
        //binding?.webView?.addJavascriptInterface(JSCalls,"JavaScriptMoth")

        binding?.webView?.loadData(iframeHtml, "text/html", "utf-8")

        binding?.btnPlay?.setOnClickListener {
            //binding?.webView?.loadUrl("javascript:player.playVideo()")
            //binding?.webView?.loadUrl("javascript:player.cueVideoById(9nhhQhAxhjo, 10)")
            //binding?.webView?.loadUrl("javascript:player.loadVideoById(\"9nhhQhAxhjo\", 10)")

        }

    }
}