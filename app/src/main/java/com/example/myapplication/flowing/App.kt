package com.example.myapplication.flowing

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.text.style.TextAlign
import arrow.fx.coroutines.parMapUnordered
import com.example.myapplication.flowing.Square.Companion.toSquare
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import java.nio.file.FileSystems
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.Path
import kotlin.io.path.useLines
import kotlin.time.Duration.Companion.seconds


class App : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var shapes: Any? by remember {
                mutableStateOf(null)
            }
            LaunchedEffect(key1 = Unit) {
                circles.take(10).collect {
                    delay(1000)
                    shapes = it
                    shapes = it.toSquare()
                }
            }

            when (val shape = shapes) {
                is Circle -> CircleComponent(count = shape.count.toString())
                is Square -> SquareComponent(count = shape.count.toString())
                else -> {}
            }
        }
    }
}

@Composable
fun CircleComponent(count: String) {
    Text(
        modifier = Modifier
            .background(Yellow, CircleShape),
        text = count,
        textAlign = TextAlign.Center
    )
}

@Composable
fun SquareComponent(count: String) {
    Text(
        modifier = Modifier
            .background(Blue),
        text = count,
        color = White,
        textAlign = TextAlign.Center
    )
}

fun main(): Unit = runBlocking {
//    circles
//        .take(3)
//        .collect { circle ->
//            circle.toSquare()
//        }
    uploadData3("/Users/acarbajo/IdeaProjects/MyApplication/app/src/main/java/com/example/myapplication/vanilla/data.txt")
}

val circles: Flow<Circle> =
    flow { produceCircles() }

@JvmInline
value class Circle(val count: Int)

context(FlowCollector<Circle>)
        suspend fun produceCircles() {
    var counter = 0
    while (true) {
        Circle(counter++).run {
            println("---")
            println("Creating $this")
            emit(this)
        }
    }
}

@JvmInline
value class Square private constructor(val count: Int) {
    companion object {
        suspend fun Circle.toSquare(): Square {
            println("Creating Square($count)")
            delay(1.seconds)
            return Square(count).also { println("Created $it") }
        }
    }
}

fun Path.readAll() = flow {
    useLines { lines ->
        lines.forEach { line -> emit(line) }
    }
}


class BlobStorage(val blobName: String) : AutoCloseable {
    override fun close() {

    }

    fun uploadBlob(line: String): CompletableFuture<String>{
        println("upload $line")
        return CompletableFuture.completedFuture(line)
    }
}

suspend fun BlobStorage.insert(line: String) : String =
    uploadBlob(line).await()

fun blobStorage(path: String): Flow<BlobStorage> = flow {
    BlobStorage(path).use { emit(it) }
}

suspend fun uploadData(path: String): Unit =
    BlobStorage(path).use { storage ->
        Path(path)
            .readAll()
            .collect { line ->
                storage.insert(line)
            }
    }

suspend fun uploadData2(path: String) =
    blobStorage(path).flatMapConcat { blobStorage ->
        Path("/Users/acarbajo/IdeaProjects/MyApplication/app/src/main/java/com/example/myapplication/vanilla/data.txt")
            .readAll()
            .map { line ->
                blobStorage.insert(line)
            }
    }

suspend fun uploadData3(path: String) =
    blobStorage(path).flatMapConcat { blobStorage ->
        Path("/Users/acarbajo/IdeaProjects/MyApplication/app/src/main/java/com/example/myapplication/vanilla/data.txt")
            .readAll()
            .parMapUnordered(3){ line ->
                blobStorage.insert(line)
            }
    }