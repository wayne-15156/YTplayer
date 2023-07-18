package com.example.ytplayer

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView

class JSInterface(context: Context, private var mWebView: WebView?) {

    companion object {
        var curStatus = -1
        var curTime = -1.0
    }

    @JavascriptInterface
    fun jsCallAndroid() {
        Log.e("123", "js has called Android!")
    }

    @JavascriptInterface
    fun KTGetVideoStatus(status: Int) {
        curStatus = status
        Log.e("123", "Current Status: $curStatus")
        //    -1 – 尚未開始
        //    0 – 已結束
        //    1 – 播放中
        //    2 – 已暫停
        //    3 – 緩衝處理中
        //    5 – 提示的影片
    }

    @JavascriptInterface
    fun KTGetVideoTime(time: Double){
        curTime = (time*1000).toInt()/1000.toDouble()
        Log.e("123", "VideoTime: ${curTime}")
    }



}