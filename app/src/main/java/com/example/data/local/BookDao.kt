package com.example.data.local

import androidx.room.*
import com.example.data.models.BookTitle
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY updatedAt DESC")
    fun getAllBooks(): Flow<List<BookTitle>>

    @Query("SELECT * FROM books ORDER BY updatedAt DESC")
    suspend fun getAllBooksSync(): List<BookTitle>

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBookCount(): Int

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookByIdFlow(id: String): Flow<BookTitle?>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: String): BookTitle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: BookTitle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<BookTitle>)

    @Update
    suspend fun updateBook(book: BookTitle)

    @Delete
    suspend fun deleteBook(book: BookTitle)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBookById(id: String)

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()
}
