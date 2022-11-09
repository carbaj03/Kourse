package com.example.myapplication.empty

import arrow.core.Either
import arrow.core.right
import com.example.myapplication.todo.*

interface Repository {
    fun podcast(): Either<DomainError, Podcasts>
    fun blogs(): Either<DomainError, Blogs>
}

class RepositoryAndroid : Repository {
    override fun podcast(): Either<DomainError, Podcasts> {
        return Podcasts(listOf(Podcast("aaa2", ""))).right()
    }

    override fun blogs(): Either<DomainError, Blogs> {
        return Blogs(listOf(Blog(Author("", "a", SocialMedias(listOf(SocialMedia.Twitter(""))))))).right()
    }
}