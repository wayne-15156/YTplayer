package com.example.ytplayer

import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    val subtitleObj = ArrayList<SubtitleObj.Result.VideoInfo.CaptionResult.Results.Captions>()

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
                    //event.target.mute().playVideo().unMute()
                  }
            
                  // 5. The API calls this function when the player's state changes.
                  //    The function indicates that when playing a video (state=1),
                  //    the player should play for six seconds and then stop.
                  
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
            if (JSInterface.curStatus == 1 || JSInterface.curStatus == 3)
                binding?.webView?.loadUrl("javascript:player.pauseVideo()")
            else if (JSInterface.curStatus == 0 || JSInterface.curStatus == 2)
                binding?.webView?.loadUrl("javascript:player.playVideo()")


            //binding?.webView?.loadUrl("javascript:callAndroid()")
            //binding?.webView?.loadUrl("javascript:player.cueVideoById(9nhhQhAxhjo, 10)")
            //binding?.webView?.loadUrl("javascript:player.loadVideoById(\"9nhhQhAxhjo\", 10)")
            //binding?.webView?.loadUrl("javascript:player.mute().playVideo().pauseVideo().unMute().playVideo()")

        }

        binding?.recyclerView?.post {
            //定時確認影片時間
            CoroutineScope(Dispatchers.Main).launch {
                var curPos = -1
                var prePos = -1

                while (true) {
                    //監聽播放狀態
                    if (JSInterface.curStatus == -1 || JSInterface.curStatus == 0 || JSInterface.curStatus == 2)
                        binding?.btnPlay?.background = resources.getDrawable(R.drawable.play_icon, null)
                    else
                        binding?.btnPlay?.background = resources.getDrawable(R.drawable.pause_icon, null)

                    binding?.webView?.loadUrl("javascript:JSGetCurTime()")
                    curPos = findPos(JSInterface.curTime)
                    //顯示灰色項目變動
                    if (curPos != -1) {
                        (binding?.recyclerView?.layoutManager as LinearLayoutManager).findViewByPosition(curPos)
                                ?.setBackgroundColor((0xFFE0E0E0).toInt())
                        if (prePos != curPos) {
                            (binding?.recyclerView?.layoutManager as LinearLayoutManager).scrollToPosition(curPos)
                            //(binding?.recyclerView?.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(curPos, 0)

                            if (prePos != -1)
                                (binding?.recyclerView?.layoutManager as LinearLayoutManager).findViewByPosition(prePos)
                                    ?.setBackgroundColor((0xFFFFFFFF).toInt())
                        }
                        prePos = curPos
                    }
                    delay(100)
                }
            }
        }

    }

    private fun findPos(t: Double): Int {
        val size = subtitleObj.size
        subtitleObj.forEachIndexed { idx, obj ->
            if (idx == size - 1) //最後一個idx判斷獨立處理
                if (t >= obj.miniSecond) return idx else idx-1

            if (t < obj.miniSecond)
                return idx - 1
        }
        return -1 //錯誤, 找不到位置
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

                subtitleObj.clear()
                subtitleObj.addAll(resObj.result.videoInfo.captionResult.results[0].captions)

                runOnUiThread {
                    binding?.recyclerView?.adapter = SubtitleAdapter(this@MainActivity, subtitleObj,
                        object: SubtitleAdapter.ClickOnListener {
                            override fun onClick(pos: Int) {
                                //此處原API Sec、MiliSec寫反...嗎?
                                //val sec = subtitleObj[pos].miniSecond
                                //val milisec = subtitleObj[pos].time
                                //val time = sec + milisec/1000
                                binding?.webView?.loadUrl("javascript:player.seekTo(${subtitleObj[pos].miniSecond}, true)")
                          }
                        })

                    /*
                    RecyclerView滾動監聽事件
                    val listener = object: RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(
                            recyclerView: RecyclerView,
                            newState: Int
                        ) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                                binding?.webView?.loadUrl("javascript:player.pauseVideo()")
                                binding?.btnPlay?.background = resources.getDrawable(R.drawable.play_icon, null)
                            }
                        }
                    }
                    binding?.recyclerView?.addOnScrollListener(listener)
                    */
                }
            }
        })
    }
}