package com.diten.tech.pdfrenderer.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenCreated
import com.diten.tech.pdfrenderer.R
import com.diten.tech.pdfrenderer.viewmodel.PDFReaderViewModel
import com.diten.tech.pdfrenderer.viewmodel.PDFReaderViewModelState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {



    private  val viewModel by viewModels<PDFReaderViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFlow()

        viewModel.pdf(this,"https://file-examples.com/storage/fe18f1f61e62a7141a165f0/2017/10/file-example_PDF_1MB.pdf")

        //permission
    }

    private fun initFlow() {
        lifecycleScope.launch(Dispatchers.Main){
            whenCreated {
                viewModel.pdfReaderViewModelState.collect{
                    when (it) {
                        is PDFReaderViewModelState.OnPDFFile -> {
                            Log.d("???","pdf file ${it.file.length()}")
                        }
                        is PDFReaderViewModelState.Error -> {
                            Toast.makeText(this@MainActivity, it.message,Toast.LENGTH_SHORT).show()
                        }

                        is PDFReaderViewModelState.Empty -> {
                            Toast.makeText(this@MainActivity, "empty",Toast.LENGTH_SHORT).show()
                        }

                        is PDFReaderViewModelState.Loading -> {
                            //TODO progress
                        }

                        is PDFReaderViewModelState.Progress -> {
                            //TODO progress
                        }

                        is PDFReaderViewModelState.None -> Unit
                    }
                }
            }
        }
    }
}