package com.example.fruitgrade.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ScanDao {
    @Insert
    suspend fun insert(scan: ScanResult): Long

    @Query("SELECT * FROM scan_results ORDER BY timestamp DESC")
    suspend fun getAll(): List<ScanResult>

    @Query("DELETE FROM scan_results WHERE id = :id")
    suspend fun deleteById(id: Long): Int
}
