package com.example.ytplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SubtitleAdapter(context: Context): RecyclerView.Adapter<SubtitleAdapter.ViewHolder>() {
    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val tvText = v.findViewById<TextView>(R.id.tv_text)
        val tvPos = v.findViewById<TextView>(R.id.tv_pos)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_subtitle, viewGroup, false)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }
}