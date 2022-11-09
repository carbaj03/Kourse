package com.example.myapplication.todo

import arrow.optics.optics
import com.fintonic.domain.commons.redux.types.State

@optics
data class OpticsState(
    val books: Books = Books(emptyList()),
    val videos: Videos = Videos(emptyList()),
    val blogs: Blogs = Blogs(emptyList()),
    val podcast: Podcasts = Podcasts(emptyList()),
    val isLoading: Boolean = false,
    val error: String? = null,
) : State {
    companion object
}

@optics
data class HomeState(
    val title: String = "asdfdasf",
    val subTitle: String? = null,
) : State {
    companion object
}


@optics
data class ToolbarState(
    val title: String = "asdfdasf",
    val subTitle: String? = null,
) : State {
    companion object
}


enum class BottomItem(
    open val title: String = "asdfdasf",
    open val subTitle: String? = null,
    open val route: String
) {
    Home("Podcast", null, podcastTabRoute),
    Books("Books", null, booksTabRoute),
}

@optics
data class BottomState(
    val list: List<BottomItem>,
    val selected: BottomItem
) : State {
    companion object
}

sealed interface ToolbarNavigation {
    object OnBack : ToolbarNavigation
}

@optics
data class BooksState(
    val books: Books = Books(emptyList()),
    val isLoading: Boolean = false,
    val error: String? = null,
) : State {
    companion object
}

@optics
data class PodcastsState(
    val podcast: Podcasts = Podcasts(emptyList()),
    val isLoading: Boolean = false,
    val error: String? = null,
) : State {
    companion object
}

@optics
data class VideosState(
    val videos: Videos = Videos(emptyList()),
    val isLoading: Boolean = false,
    val error: String? = null,
) : State {
    companion object
}

@optics
data class BlogsState(
    val blogs: Blogs = Blogs(emptyList()),
    val isLoading: Boolean = false,
    val error: String? = null,
) : State {
    companion object
}

@JvmInline
value class Podcasts(
    val value: List<Podcast>
)

data class Podcast(
    val title: String,
    val url: String
)


@JvmInline
value class Books(
    val value: List<Book>
)

data class Book(
    val id: Int,
    val title: String
)


@JvmInline
value class Videos(
    val value: List<Video>
)

data class Video(
    val type: Type,
    val url: String
) {
    enum class Type {
        Youtube, Netflix, Disney
    }
}


@JvmInline
value class Blogs(
    val value: List<Blog>
)

data class Blog(
    val author: Author
)


data class Author(
    val web: String,
    val name: String,
    val socialMedias: SocialMedias
)

@JvmInline
value class SocialMedias(
    val value: List<SocialMedia>
)

sealed class SocialMedia(open val url: String) {
    data class Twitter(override val url: String) : SocialMedia(url)
    data class Facebook(override val url: String) : SocialMedia(url)
    data class Youtube(override val url: String) : SocialMedia(url)
}