package com.example.myapplication.vanilla

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.myapplication.empty.*
import com.example.myapplication.empty.user.RepositoryDispatcher
import com.example.myapplication.empty.user.User
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

fun createAppGraph(): AppGraph =
    object : AppGraph {
        override val repositoryDispatcher: RepositoryDispatcher =
            object : RepositoryDispatcher {
                override val io: CoroutineContext = Dispatchers.IO
                override val default: CoroutineContext = Dispatchers.Default
            }

        override val client: HttpClient by lazy(LazyThreadSafetyMode.NONE) {
            HttpClient(OkHttp) {
                engine {
                    config {
                        followRedirects(true)
                    }
                }
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.HEADERS
                }
                install(ContentNegotiation) {
                    json()
                }
            }
        }

        override val authService: AuthService =
            object : AuthService {
                override suspend fun login(
                    userName: String,
                    password: String,
                ): Either<AuthError, UserGraph> =
                    try {
                        val userClient = HttpClient(OkHttp) {
                            engine {
                                config {
                                    followRedirects(true)
                                }
                            }
                            install(Logging) {
                                logger = Logger.DEFAULT
                                level = LogLevel.HEADERS
                            }
                            install(ContentNegotiation) {
                                json()
                            }
                            install(Auth) {
                                basic {
                                    credentials {
                                        BasicAuthCredentials(username = userName, password = password)
                                    }
                                    realm = "Access to the '/' path"
                                }
                            }
                        }
                        if (userClient.get("http://192.168.0.101:5000/validate").status == HttpStatusCode.OK) {
                            with(BookGraph(userClient)) {
                                with(PodcastGraph()) {
                                    with(VideoGraph()) {
                                        createUserGraph(User(userName, password), userClient)
                                    }
                                }
                            }.right()
                        } else
                            AuthError.InvalidUser.left()
                    } catch (ex: Exception) {
                        AuthError.InvalidUser.left()
                    }
            }

    }
