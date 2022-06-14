package com.diten.tech.pdfrenderer.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.diten.tech.pdfrenderer.R
import com.diten.tech.pdfrenderer.adapter.PDFAdapter
import com.diten.tech.pdfrenderer.viewmodel.PDFReaderViewModel
import com.diten.tech.pdfrenderer.viewmodel.PDFReaderViewModelState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {



    private  val viewModel by viewModels<PDFReaderViewModel>()

    private lateinit var adapter:PDFAdapter

    private val totalPDFBitmapList = mutableListOf<Bitmap>()

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFlow()
        initList()
        setListener()

        viewModel.pdf(this,"https://file-examples.com/storage/fe6869c4db62a7ab6a021f9/2017/10/file-example_PDF_1MB.pdf")

        //permission
    }

    private fun initFlow() {
        lifecycleScope.launch(Dispatchers.Main){
            whenCreated {
                viewModel.pdfReaderViewModelState.collect{
                    when (it) {
                        is PDFReaderViewModelState.OnPDFFile -> {
                            Log.d("???","pdf file ${it.file.length()}")
                            hideProgress()

                            viewModel.bitmaps(it.file, getScreenWidth(this@MainActivity))
                        }

                        is PDFReaderViewModelState.OnBitmaps -> {

                            hideProgress()

                            totalPDFBitmapList.clear()
                            totalPDFBitmapList.addAll(it.list)

                            @SuppressLint("SetTextI18n")
                            textViewPage.text = "${currentIndex + 1} / ${totalPDFBitmapList.size}"

                            reload()
                        }
                        is PDFReaderViewModelState.Error -> {
                            Toast.makeText(this@MainActivity, it.message,Toast.LENGTH_SHORT).show()
                            hideProgress()
                        }

                        is PDFReaderViewModelState.Empty -> {
                            Toast.makeText(this@MainActivity, "empty",Toast.LENGTH_SHORT).show()
                            hideProgress()
                        }

                        is PDFReaderViewModelState.Loading -> {
                            showProgress()
                        }

                        is PDFReaderViewModelState.Progress -> {
                            textViewProgress.post {
                                textViewProgress.text = if (it.progress != 0) {
                                    "${it.progress}"
                                }else{
                                    ""
                                }
                            }
                        }

                        is PDFReaderViewModelState.None -> Unit
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setListener() {

        buttonLeft.setOnClickListener {
            currentIndex -=1
            if (currentIndex <=0){
                currentIndex = 0
            }
            recyclerView.smoothScrollToPosition(currentIndex)
            "${currentIndex + 1} / ${totalPDFBitmapList.size}".also { textViewPage.text = it }
        }

        buttonRight.setOnClickListener {
            currentIndex +=1
            if (currentIndex > totalPDFBitmapList.size -1){
                currentIndex = totalPDFBitmapList.size -1
            }
            recyclerView.smoothScrollToPosition(currentIndex)
            textViewPage.text = "${currentIndex + 1} / ${totalPDFBitmapList.size}"
        }
    }

    private fun initList(){
        recyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        adapter = PDFAdapter {
            loadMore()
        }

        recyclerView.adapter = adapter

        recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE){
                    currentIndex = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                    // current page
                    @SuppressLint("SetTextI18n")
                    textViewPage.text = "${currentIndex + 1} / ${totalPDFBitmapList.size}"
                }
            }
        })

        PagerSnapHelper().attachToRecyclerView(recyclerView)
    }

    private fun reload() {
        recyclerView.post {
            adapter.reload(fetcData(0,10))
        }
    }

    private fun loadMore() {
        recyclerView.post {
            adapter.loadMore(fetcData(adapter.itemCount,5))
        }
    }

    //if a pdf file has so many pages,
    // we need pagination.
    //download one time and fetch pages with load more
    private fun fetcData(offset: Int, limit:Int): List<Bitmap>{
        val list = mutableListOf<Bitmap>()

        for (i in offset until offset + limit){
            if (i > totalPDFBitmapList.size - 1){
                break
            }
            list.add(totalPDFBitmapList[i])
        }
        return list
    }

    private fun getScreenWidth(context: Context): Int{
        val outMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.R){
            val display = context.display
            display?.getRealMetrics(outMetrics)
        }else{
            val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            display.getMetrics(outMetrics)
        }

        return outMetrics.widthPixels
    }

    private fun showProgress(){
        progress.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        progress.visibility = View.GONE
    }
}