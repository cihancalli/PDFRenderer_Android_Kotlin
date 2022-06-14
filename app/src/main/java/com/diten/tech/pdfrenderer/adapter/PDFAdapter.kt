package com.diten.tech.pdfrenderer.adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.diten.tech.pdfrenderer.R
import com.diten.tech.pdfrenderer.adapter.item.PDFItemViewHolder

class PDFAdapter(val onLoadMore: () ->Unit): RecyclerView.Adapter<PDFItemViewHolder>() {

    private val list = mutableListOf<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PDFItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_holder_pdf_item,parent,false)
        return PDFItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: PDFItemViewHolder, position: Int) {
        holder.bitmap = list[position]
        holder.page = position
        holder.updateView()
        if (position == list.size - 1){
            onLoadMore()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun reload(list:List<Bitmap>){
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun loadMore(list: List<Bitmap>){
        this.list.addAll(list)
        notifyItemRangeChanged(this.list.size - list.size + 1,list.size)
    }
}