package com.example.myapplication.empty.book

import arrow.core.Either
import arrow.core.right
import com.example.myapplication.empty.common.DomainError
import com.example.myapplication.empty.common.toEither
import com.example.myapplication.todo.Book
import com.example.myapplication.todo.Books

interface BookNetwork

fun BooksNetwork(): BookNetwork = object : BookNetwork {}

interface BookDB

fun BookDB(): BookDB = object : BookDB {}

interface BookRepository {
    fun allBooks(): Either<DomainError, Books>
    fun save(book: Book): Either<DomainError, Book>
    fun get(id: Int): Either<DomainError, Book>
}

context(BookNetwork, BookDB)
fun BookRepository(): BookRepository =
    object : BookRepository {
        private var books = Books(
            buildList {
                repeat(30) {
                    add(Book(it, "Book $it"))
                }
            }
        )
        
        override fun allBooks(): Either<DomainError, Books> {
            return books.right()
        }
        
        override fun save(book: Book): Either<DomainError, Book> {
            books = books.value.map { if (it.id == book.id) book else it }.let { Books(it) }
            return get(book.id)
        }
        
        override fun get(id: Int): Either<DomainError, Book> {
            return books.value.find { it.id == id }.toEither(DomainError.Default)
        }
    }