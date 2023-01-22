package com.example.myapplication.navigation

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

fun <T> CoroutineScope.launchMolecule(
    emitter: (value: T) -> Unit,
    body: @Composable () -> T,
) {

    with(this) {
        val recomposer = Recomposer(coroutineContext)
        val composition = Composition(UnitApplier, recomposer)
        launch(start = CoroutineStart.UNDISPATCHED) {
            recomposer.runRecomposeAndApplyChanges()
        }

        var applyScheduled = false
        val snapshotHandle = Snapshot.registerGlobalWriteObserver {
            if (!applyScheduled) {
                applyScheduled = true
                launch {
                    applyScheduled = false
                    Snapshot.sendApplyNotifications()
                }
            }
        }
        coroutineContext.job.invokeOnCompletion {
            composition.dispose()
            snapshotHandle.dispose()
        }

        composition.setContent {
            emitter(body())
        }
    }
}

enum class RecompositionClock {
    /**
     * Use the MonotonicFrameClock that already exists in the calling CoroutineContext.
     * If none exists, an exception is thrown.
     *
     * Use this option to drive Molecule with the built-in Android frame clock.
     */
    ContextClock,

    /**
     * Install an eagerly recomposing clock. This clock will provide a new frame immediately whenever
     * one is requested. The resulting flow will emit a new item every time the snapshot state is invalidated.
     */
    Immediate,
}

private object UnitApplier : AbstractApplier<Unit>(Unit) {
    override fun insertBottomUp(index: Int, instance: Unit) {}
    override fun insertTopDown(index: Int, instance: Unit) {}
    override fun move(from: Int, to: Int, count: Int) {}
    override fun remove(index: Int, count: Int) {}
    override fun onClear() {}
}