package com.example.fruitgrade.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fruitgrade.data.HistoryRepository
import com.example.fruitgrade.data.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HistoryRepository(application)
    private val _history = MutableStateFlow<List<ScanResult>>(emptyList())
    val history: StateFlow<List<ScanResult>> = _history.asStateFlow()

    fun loadHistory() {
        viewModelScope.launch {
            _history.value = repository.getAllScans()
        }
    }

    fun deleteScan(id: Long) {
        viewModelScope.launch {
            repository.deleteScan(id)
            loadHistory()
        }
    }
}
