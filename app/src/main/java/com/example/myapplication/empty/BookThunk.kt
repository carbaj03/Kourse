package com.example.myapplication.empty

import com.example.myapplication.todo.Book
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


sealed interface BookAction {
    object Edit : BookAction
    object Confirm : BookAction
    data class ChangeName(val name: String) : BookAction
    object Back : BookAction
}

data class BookState(
    val book: Book,
    val isEditing: Boolean = false,
    val newName: String = book.title,
)

interface BookThunk {
    fun dispatch(action: BookAction)
    val state: StateFlow<BookState>
}

class BookThunkAndroid(
    val repository: BooksRepository,
    val nav: (NavGraph) -> Unit,
    val initialState: BookState
) : BookThunk {
    val s = MutableStateFlow(initialState)

    override fun dispatch(action: BookAction) {
        when (action) {
            BookAction.Edit -> {
                s.value = s.value.copy(isEditing = true)
            }

            is BookAction.Back -> {
                nav(NavGraph.Back)
            }

            is BookAction.Confirm -> {
                val book = repository.save(s.value.book.copy(title = s.value.newName))
                book.map {
                    s.value = s.value.copy(isEditing = false, book = it, newName = it.title)
                }
            }

            is BookAction.ChangeName -> {
                s.value = s.value.copy(newName = action.name)
            }
        }
    }

    override val state: StateFlow<BookState> = s
}