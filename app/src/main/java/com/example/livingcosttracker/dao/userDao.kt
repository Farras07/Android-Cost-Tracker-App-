package com.example.livingcosttracker.dao
import androidx.room.*
import com.example.livingcosttracker.db.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM User Where id='1'")
    suspend fun getUser(): User

    @Query("SELECT balance FROM user WHERE id=1")
    suspend fun getUserBalance(): Int

    @Query("UPDATE user SET balance = :newBalance WHERE id = 1")
    suspend fun updateUserBalanceById(newBalance: Int)

}