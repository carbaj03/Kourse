package com.example.myapplication.asynchrony

import arrow.core.continuations.EffectScope
import arrow.optics.Copy
import arrow.optics.Setter
import arrow.optics.Traversal
import com.example.myapplication.redux.createAction
import com.example.myapplication.redux.createAction1
import com.example.myapplication.redux.createActionSlice
import com.example.myapplication.redux.types.*
import com.fintonic.domain.commons.redux.types.*
import kotlin.experimental.ExperimentalTypeInference

context(WithScope)
        inline fun <reified S : State, E> actionSlice(
    key: String? = null,
    crossinline onError: context(Dispatcher) (E) -> Unit = { _ -> },
    crossinline f: suspend context(EffectScope<E>, Dispatcher) (S) -> Unit,
): AsyncAction<CombineState> =
    createActionSlice<S> { s, d ->
        key?.let { jobs[key]?.invoke() }
        eitherIo(
            onError = { onError(d, it) },
            f = { f(this, d, s) }
        ).let { newJob ->
            key?.let {
                jobs[key] = { newJob.cancel() }
            }
        }
    }

context(WithScope)
        @OptIn(ExperimentalTypeInference::class)
fun <S : State, E> action(
    key: String? = null,
    onError: suspend context(Dispatcher) (E) -> Unit = { _ -> },
    @BuilderInference f: suspend context(EffectScope<E>, Dispatcher) (S) -> Unit
): AsyncAction<S> =
    createAction { s, d ->
        key?.let { jobs[key]?.invoke() }
        eitherIo(
            onError = { onError(d, it) },
            f = { f(this, d, s) }
        ).also { newJob ->
            key?.let {
                jobs[key] = { newJob.cancel() }
            }
        }
    }

context(WithScope)
fun <S : State, E> action2(
    key: String? = null,
    onError: suspend context(Dispatcher) (E) -> Unit = { _ -> },
    f: suspend context(EffectScope<E>, Dispatcher, Copy<S>) () -> Unit
): AsyncAction1<S> =
    createAction1 { s, d ->
        key?.let { jobs[key]?.invoke() }
        eitherIo(
            onError = { onError(d, it) },
            f = { f(this, d, CopyImpl(s(), d)) }
        ).also { newJob ->
            key?.let {
                jobs[key] = { newJob.cancel() }
            }
        }
    }

context(WithScope)
fun <S : State, E> action1(
    key: String? = null,
    onError: suspend context(Dispatcher) (E) -> Unit = { _ -> },
    f: suspend context(EffectScope<E>, Dispatcher, Copy<S>) (S, () -> S) -> Unit
): AsyncAction1<S> =
    createAction1 { s, d ->
        key?.let { jobs[key]?.invoke() }
        eitherIo(
            onError = { onError(d, it) },
            f = { f(this, d, CopyImpl(s(), d), s(), s) }
        ).also { newJob ->
            key?.let {
                jobs[key] = { newJob.cancel() }
            }
        }
    }

//@JvmInline
//value class ActionState<S : State>(val state: S.() -> S) : Action


//context(Store<A>)
//@OptIn(ExperimentalTypeInference::class)
//fun <A : State> A.copy( @BuilderInference f: Copy<A>.() -> Unit): A {
//    CopyImpl().also(f)
//    return state.value
//}
//
//context(Store<A>)
//class CopyImpl<A : State> : Copy<A> {
//    override fun <B> Setter<A, B>.set(b: B) {
//        dispatch(ActionState(this.set(state.value, b)))
//    }
//
//    override fun <B> Traversal<A, B>.transform(f: (B) -> B) {
//        dispatch(ActionState(this.modify(state.value, f)))
//    }
//}

@OptIn(ExperimentalTypeInference::class)
fun <A : State> A.copy(dispatch: Dispatcher, @BuilderInference f: Copy<A>.() -> Unit): A =
    CopyImpl(this, dispatch).also(f).current

class CopyImpl<A : State>(
    var current: A,
    private val dispatch: Dispatcher
) : Copy<A> {
    override fun <B> Setter<A, B>.set(b: B) {
        current = this.set(current, b)
        dispatch(ActionState<A> { current })
    }

    override fun <B> Traversal<A, B>.transform(f: (B) -> B) {
        current = this.modify(current, f)
        dispatch(ActionState<A> { current })
    }
}

context(WithScope)
fun <S : State, E> actionMain(
    f: suspend context(EffectScope<E>, Dispatcher) (S) -> Unit
): AsyncAction<S> =
    createAction { s, d ->
        eitherMain { f(this, d, s) }
    }

context(WithScope)
@OptIn(ExperimentalTypeInference::class)
fun <S : State, E : Error> Store<S>.dispatchAction(
    key: String? = null,
    onError: suspend context(Dispatcher) (E) -> Unit = {},
    @BuilderInference f: suspend context(EffectScope<E>, Dispatcher) (S) -> Unit
) {
    action(key = key, onError = onError, f = f).dispatch()
}

context(WithScope)
fun <S : State> Store<S>.dispatchAction(
    key: String? = null,
    f: suspend context(EffectScope<Error>, Dispatcher) (S) -> Unit
) {
    action(key = key, f = f).dispatch()
}

//context(WithScope, Store<S>)
//fun <S : State, E> dispatchActionMutable(
//    key: String? = null,
//    onError: suspend context(Dispatcher) (E) -> Unit = { _ -> },
//    f: suspend context(EffectScope<E>, Dispatcher, Copy<S>) () -> Unit
//) {
//    action2(key, onError, f).dispatch()
//}

//context(WithScope)
//inline fun Store<CombineState>.dispatchAction(
//    key: String? = null,
//    crossinline onError: context(Dispatcher) (Error) -> Unit = { _ -> },
//    crossinline f: suspend context(EffectScope<Error>, Dispatcher) () -> Unit
//) {
//    action<CombineState,E>(key = key, onError = onError, f = f).dispatch()
//}