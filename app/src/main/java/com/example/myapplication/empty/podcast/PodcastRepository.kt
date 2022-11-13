package com.example.myapplication.empty.podcast

import arrow.core.Either
import arrow.core.right
import com.example.myapplication.todo.DomainError
import com.example.myapplication.todo.Podcast
import com.example.myapplication.todo.PodcastId
import com.example.myapplication.todo.Podcasts

interface PodcastDB

fun PodcastDB() = object : PodcastDB {}

interface PodcastNetwork

fun PodcastNetwork() = object : PodcastNetwork {}

interface PodcastRepository {
    fun all(): Either<DomainError, Podcasts>
    fun byId(id: PodcastId): Either<DomainError, Podcast>
    fun save(podcast: Podcast): Either<DomainError, Podcast>
    fun remove(id: PodcastId): Either<DomainError, Podcast>
}

context(PodcastDB, PodcastNetwork)
fun PodcastRepository(): PodcastRepository =
    object : PodcastRepository {
        var podcast = Podcasts(listOf(Podcast(PodcastId(1), title = "Title", url = "")))
        
        override fun all(): Either<DomainError, Podcasts> = podcast.right()
        
        override fun byId(id: PodcastId): Either<DomainError, Podcast> {
            TODO("Not yet implemented")
        }
        
        override fun save(podcast: Podcast): Either<DomainError, Podcast> {
            TODO("Not yet implemented")
        }
        
        override fun remove(id: PodcastId): Either<DomainError, Podcast> {
            TODO("Not yet implemented")
        }
        
    }