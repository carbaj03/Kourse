package com.example.myapplication.todo

import arrow.optics.optics
import com.fintonic.domain.commons.redux.types.State
import kotlinx.serialization.Serializable

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
    open val route: String,
) {
    Home("Podcast", null, podcastTabRoute),
    Books("Books", null, booksTabRoute),
}

@optics
data class BottomState(
    val list: List<BottomItem> = emptyList(),
    val selected: BottomItem = BottomItem.Home
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

@JvmInline
value class PodcastId(
    val value: Int
)

data class Podcast(
    val id: PodcastId,
    val title: String,
    val url: String
)

@Serializable
@JvmInline
value class Books(
    val value: List<Book>
)


@Serializable
@JvmInline
value class BookId(
    val value: Int
)

@Serializable
data class Book(
    val id: BookId,
    val title: String,
    val type: Type = Type.Ebook,
    val genre: Genre = Genre.SelfDev,
) {
    enum class Type {
        AudioBook, Ebook, Pdf
    }

    enum class Genre {
        Romance, SelfDev,
    }

    data class Author(
        val name: String,
    )
}

@JvmInline
value class Videos(
    val value: List<Video>
)

@JvmInline
value class VideoId(
    val value: Int
)

data class Video(
    val id: VideoId,
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

@JvmInline
value class BlogId(
    val value: Int
)

data class Blog(
    val id: BlogId,
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
    data class Instagram(override val url: String) : SocialMedia(url)
}