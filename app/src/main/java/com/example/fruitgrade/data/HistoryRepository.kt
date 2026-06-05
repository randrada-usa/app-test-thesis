package com.example.fruitgrade.data

import android.content.Context

class HistoryRepository(context: Context) {
    private val scanDao = AppDatabase.getDatabase(context).scanDao()

    suspend fun saveScan(scan: ScanResult) = scanDao.insert(scan)

    suspend fun getAllScans(): List<ScanResult> = scanDao.getAll()

    suspend fun deleteScan(id: Long): Int = scanDao.deleteById(id)
}
