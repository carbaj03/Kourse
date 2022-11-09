package com.example.myapplication.empty

import com.example.myapplication.todo.Book
import com.example.myapplication.todo.Books
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


sealed interface BooksAction {
    object Load : BooksAction
    object Back : BooksAction
    data class Selected(val book: Book) : BooksAction
}

data class BooksState(
    val books: Books,
    val toolbar: ToolbarState?,
    val bottom: BottomState?,
)

data class ToolbarState(
    val title: String,
    val onBack: () -> Unit
)

data class BottomState(
    val actions: List<BottomAction>,
    val onAdd : () -> Unit,
)

enum class BottomAction {
    Select
}


interface BooksThunk {
    fun dispatch(action: BooksAction)
    val state: StateFlow<BooksState>
}

class BooksThunkAndroid(
    val repository: BooksRepository,
    val nav: (NavGraph) -> Unit,
    val initialState : BooksState
) : BooksThunk {
    val s = MutableStateFlow(initialState)

    override fun dispatch(action: BooksAction) {
        when (action) {
            BooksAction.Load -> {
                repository.all().map {
                    s.value = s.value.copy(books = it)
                }
            }

            is BooksAction.Selected -> {
                nav(NavGraph.BookDetail(action.book))
            }

            is BooksAction.Back -> {
                nav(NavGraph.Back)
            }
        }
    }

    override val state: StateFlow<BooksState> = s
}
