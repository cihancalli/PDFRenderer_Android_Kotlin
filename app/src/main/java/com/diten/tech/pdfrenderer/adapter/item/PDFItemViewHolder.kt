package com.diten.tech.pdfrenderer.adapter.item

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.diten.tech.pdfrenderer.R
import java.lang.ref.WeakReference

class PDFItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {

    val view = WeakReference(itemView)

    var textView: TextView? = null
    var imageView:ImageView? = null

    var bitmap: Bitmap? = null

    var page:Int = 0

    init {
        view.get()?.let {
            textView = it.findViewById(R.id.textView)
            imageView = it.findViewById(R.id.imageView)
        }
    }


    fun updateView(){
        imageView?.setImageBitmap(bitmap)

        @SuppressLint("SetTextI18n")
        textView?.text = "${page + 1}"
    }
}