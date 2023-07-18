package com.example.ytplayer


data class SubtitleObj(val result: Result) {
    data class Result(val videoInfo: VideoInfo) {
        data class VideoInfo(val captionResult: CaptionResult, val duration: Int) {
            data class CaptionResult(val results: ArrayList<Results>) {
                data class Results(val captions: ArrayList<Captions>) {
                    data class Captions(val time: Long, val miniSecond: Int, val content: String, var startTime: Double?)
                }
            }
        }
    }
}

class SubtitleAPI {
}