package com.diten.tech.pdfrenderer.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diten.tech.pdfrenderer.service.PDFReaderService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception

sealed class  PDFReaderViewModelState {
    data class OnPDFFile(val file: File):PDFReaderViewModelState()
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
}