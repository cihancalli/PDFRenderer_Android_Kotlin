package com.diten.tech.pdfrenderer.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diten.tech.pdfrenderer.service.PDFReaderService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class  PDFReaderViewModelState {
    data class OnPDFFile(val file: File):PDFReaderViewModelState()
    data class OnBitmaps(val list: List<Bitmap>): PDFReaderViewModelState()

    data class Error(val message: String):PDFReaderViewModelState()
    data class Progress(val progress: Int):PDFReaderViewModelState()

    object Empty:PDFReaderViewModelState()
    object Loading:PDFReaderViewModelState()
    object None:PDFReaderViewModelState()
}

class PDFReaderViewModel: ViewModel() {

    private val _pdfReaderViewModelState =
        MutableStateFlow<PDFReaderViewModelState>(PDFReaderViewModelState.None)

    val pdfReaderViewModelState: StateFlow<PDFReaderViewModelState> = _pdfReaderViewModelState

    fun pdf(context: Context,url: String) = viewModelScope.launch {

        _pdfReaderViewModelState.value = PDFReaderViewModelState.Loading

        try {
            coroutineScope {
                val pdfService = async {
                    PDFReaderService.pdf(context,url) {
                        Log.d("???","progress $it%")

                        _pdfReaderViewModelState.value = PDFReaderViewModelState.Progress(it)
                    }
                }

                val pdFile = pdfService.await()

                if (pdFile == null){
                    _pdfReaderViewModelState.value = PDFReaderViewModelState.Empty
                    return@coroutineScope
                }

                _pdfReaderViewModelState.value = PDFReaderViewModelState.OnPDFFile(pdFile)

            }
        }catch (e:Exception){
            _pdfReaderViewModelState.value = PDFReaderViewModelState.Error(e.message!!)
        }
    }

    fun bitmaps(file: File,screenWidth: Int) = viewModelScope.launch {
        _pdfReaderViewModelState.value = PDFReaderViewModelState.Loading

        try {
            coroutineScope {
                val bitmaps = async(Dispatchers.IO) {
                    getBitmapsFromPdfRenderer(file,screenWidth)
                }

                val result = bitmaps.await()

                _pdfReaderViewModelState.value = PDFReaderViewModelState.OnBitmaps(result)

            }
        }catch (e:Exception){
            _pdfReaderViewModelState.value = PDFReaderViewModelState.Error(e.message!!)
        }
    }

    private suspend fun getBitmapsFromPdfRenderer(
        file: File,screenWidth: Int
    ): List<Bitmap> {
        return suspendCoroutine { contination ->

            Thread {
                try {
                    val parcelFileDescriptor = ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)

                    val pdfRenderer = PdfRenderer(parcelFileDescriptor)

                    val list = mutableListOf<Bitmap>()

                    for (i in 0 until pdfRenderer.pageCount) {
                        val page = pdfRenderer.openPage(i)

                        val scale = screenWidth.toFloat() / page.width

                        Bitmap.createBitmap(
                            (page.width * scale).toInt(),
                            (page.height * scale).toInt(),
                            Bitmap.Config.ARGB_8888
                        ).also {
                            page.render(it,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                            page.close()

                            list.add(it)
                        }
                    }
                    //end of for

                    parcelFileDescriptor.close()
                    pdfRenderer.close()

                    contination.resume(list)

                }catch (e:Exception){
                        contination.resumeWithException(e)
                }
            }.start()
        }
    }
}