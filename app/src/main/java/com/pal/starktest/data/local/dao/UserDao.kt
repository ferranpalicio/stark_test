package com.pal.starktest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pal.starktest.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM user WHERE id = 0 LIMIT 1")
    suspend fun get(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserEntity)
}
