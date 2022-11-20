package com.example.myapplication.empty.video

import arrow.core.Either
import arrow.core.right
import com.example.myapplication.empty.common.DomainError
import com.example.myapplication.todo.Video
import com.example.myapplication.todo.VideoId
import com.example.myapplication.todo.Videos

interface VideoNetwork

fun VideoNetwork() = object : VideoNetwork {}

interface VideoDB

fun VideoDB() = object : VideoDB {}

interface VideoRepository {
    fun allVideos(): Either<DomainError, Videos>
    fun byId(id: VideoId): Either<DomainError, Video>
    fun save(video: Video): Either<DomainError, Video>
    fun remove(id: VideoId): Either<DomainError, Video>
}

context(VideoNetwork, VideoDB)
fun VideoRepository() =
    object : VideoRepository {
        var videos: Videos = Videos(listOf())
        
        override fun allVideos(): Either<DomainError, Videos> = videos.right()
        
        override fun byId(id: VideoId): Either<DomainError, Video> {
            TODO("Not yet implemented")
        }
        
        override fun save(video: Video): Either<DomainError, Video> {
            TODO("Not yet implemented")
        }
        
        override fun remove(id: VideoId): Either<DomainError, Video> {
            TODO("Not yet implemented")
        }
    }