package com.example.ytplayer

import android.graphics.PixelFormat
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ytplayer.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Dispatcher
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.Date


class MainActivity : AppCompatActivity() {

    var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.recyclerView?.post {
            (binding?.recyclerView?.layoutManager as LinearLayoutManager).scrollToPosition()
        }

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
                    //event.target.mute().playVideo().unMute()
                  }
            
                  // 5. The API calls this function when the player's state changes.
                  //    The function indicates that when playing a video (state=1),
                  //    the player should play for six seconds and then stop.
                  var done = false;
                  function onPlayerStateChange(event) {             
           
                      window.JSInterface.KTGetVideoStatus(player.getPlayerState())     
                                     
                  }
                  
                  function JSGetCurTime() {
                    window.JSInterface.KTGetVideoTime(player.getCurrentTime())   
                  }
                  
                                    
                  function stopVideo() {
                    player.stopVideo();
                  }
                  function callAndroid() {
                    window.JSInterface.jsCallAndroid()
                  }                                               
                  
                </script>
              </body>
            </html>
            """.trimIndent()

        getSubtitle()

        val webSettings: WebSettings? = binding?.webView?.settings
        webSettings?.javaScriptEnabled = true
        //webSettings?.builtInZoomControls = true

        binding?.webView?.webViewClient = WebViewClient()
        binding?.webView?.addJavascriptInterface(JSInterface(this, binding?.webView), "JSInterface")
        binding?.webView?.loadData(iframeHtml, "text/html", "utf-8")

        binding?.btnPlay?.setOnClickListener {

            //binding?.webView?.loadUrl("javascript:callAndroid()")
            //binding?.webView?.loadUrl("javascript:player.cueVideoById(9nhhQhAxhjo, 10)")
            //binding?.webView?.loadUrl("javascript:player.loadVideoById(\"9nhhQhAxhjo\", 10)")


            //binding?.webView?.loadUrl("javascript:player.mute().playVideo().pauseVideo().unMute().playVideo()")
            Log.e("123", "Time: ${Date().time}")

        }

        //定時確認影片時間
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                binding?.webView?.loadUrl("javascript:JSGetCurTime()")
                delay(100)
            }
        }

    }

    private fun getSubtitle() {
        val guestKey = "44f6cfed-b251-4952-b6ab-34de1a599ae4"
        val videoID = "5edfb3b04486bc1b20c2851a"
        val mode = 0

        val url = "https://api.italkutalk.com/api/video/detail"

        val json = "{" +
                "\"guestKey\": \"${guestKey}\"," +
                "\"videoID\": \"${videoID}\"," +
                "\"mode\": ${mode}," +
                "}"
        val body = json.toRequestBody("application/json;charset=UTF-8".toMediaTypeOrNull())

        val req: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        OkHttpClient().newCall(req).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("onFailure", "$e")
            }
            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                val resObj = Gson().fromJson(res, SubtitleObj::class.java)
                val subtitleObj = ArrayList<SubtitleObj.Result.VideoInfo.CaptionResult.Results.Captions>()
                subtitleObj.clear()
                subtitleObj.addAll(resObj.result.videoInfo.captionResult.results[0].captions)

                subtitleObj.forEach {
                    it.startTime = it.miniSecond + (it.time.toDouble())/1000
                    Log.e("123", "startTime: ${it.startTime}")
                }

                runOnUiThread {

                    binding?.recyclerView?.adapter = SubtitleAdapter(this@MainActivity, subtitleObj,
                        object: SubtitleAdapter.ClickOnListener {
                            override fun onClick(pos: Int) {
                                //此處原API Sec、MiliSec寫反...嗎?
                                //val sec = subtitleObj[pos].miniSecond
                                //val milisec = subtitleObj[pos].time
                                //val time = sec + milisec/1000
                                binding?.webView?.loadUrl("javascript:player.seekTo(${subtitleObj[pos].miniSecond}, true)")
                                Log.e("123", "${subtitleObj[pos].miniSecond}")
                            }
                        })
                }
            }
        })
    }
}