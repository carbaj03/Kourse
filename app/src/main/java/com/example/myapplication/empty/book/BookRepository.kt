package com.example.myapplication.empty.book

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.myapplication.empty.common.DomainError
import com.example.myapplication.empty.common.toEither
import com.example.myapplication.todo.Book
import com.example.myapplication.todo.BookId
import com.example.myapplication.todo.Books
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

interface NetworkError {
    object BadRequest : NetworkError
}

interface BookNetwork {
    suspend fun getAll(): Either<NetworkError, Books>
}

class BookNetworkMock(val books: Books) : BookNetwork {
    override suspend fun getAll(): Either<NetworkError, Books> =
        try {
            books.right()
        } catch (e: Exception) {
            NetworkError.BadRequest.left()
        }
}

fun BookNetwork(userClient: HttpClient): BookNetwork =
    object : BookNetwork {
        override suspend fun getAll(): Either<NetworkError, Books> =
            try {
                userClient.get("http://192.168.0.101:5000/books").body<Books>().right()
            } catch (e: Exception) {
                NetworkError.BadRequest.left()
            }
    }

interface BookDB

fun BookDB(): BookDB = object : BookDB {}

interface BookRepository {
    suspend fun allBooks(): Either<DomainError, Books>
    suspend fun save(book: Book): Either<DomainError, Book>
    suspend fun get(id: BookId): Either<DomainError, Book>
}

context(BookNetwork, BookDB)
fun BookRepository(): BookRepository =
    object : BookRepository {
        private var books = Books(
            buildList {
                repeat(30) {
                    add(Book(BookId(it), "Book $it"))
                }
            }
        )
        
        override suspend fun allBooks(): Either<DomainError, Books> =
            getAll().mapLeft { DomainError.Default }
        
        override suspend fun save(book: Book): Either<DomainError, Book> {
            books = books.value.map { if (it.id == book.id) book else it }.let { Books(it) }
            return get(book.id)
        }
        
        override suspend fun get(id: BookId): Either<DomainError, Book> {
            return books.value.find { it.id == id }.toEither(DomainError.Default)
        }
    }