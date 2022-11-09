package com.example.myapplication.todo

import com.example.myapplication.asynchrony.WithScope
import com.example.myapplication.asynchrony.dispatchAction
import com.example.myapplication.dispatch
import com.fintonic.domain.commons.redux.types.State
import com.fintonic.domain.commons.redux.types.Store

//context(WithScope, Store<S>)
//inline fun <S : State> dispatchState(crossinline f: S.() -> S): Unit =
//    dispatchAction<OpticsState, String> {
//        dispatch { f() }
//    }