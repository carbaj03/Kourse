package com.example.myapplication.empty.blog

import arrow.core.Either
import com.example.myapplication.empty.common.DomainError
import com.example.myapplication.todo.Blog
import com.example.myapplication.todo.BlogId
import com.example.myapplication.todo.Blogs

interface BlogRepository {
    fun all(): Either<DomainError, Blogs>
    fun byId(id: BlogId): Either<DomainError, Blog>
    fun save(blog: Blog): Either<DomainError, Blog>
    fun remove(id: BlogId): Either<DomainError, Blog>
}
