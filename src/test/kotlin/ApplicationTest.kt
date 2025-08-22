package com.example

import com.example.models.ApiResponse
import com.example.repository.HeroRepository
import com.example.repository.HeroRepositoryImpl
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent.inject

class ApplicationTest {
    private val heroRepository: HeroRepository by inject(HeroRepository::class.java)

    @Test
    fun `access root endpoint, assert correct information`() =
        testApplication {
            application {
                module()
            }
            client.get("/").apply {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("Welcome to Boruto API", bodyAsText())
            }
        }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query all pages, assert correct information`() =
        testApplication {
            application { module() }
            val heroRepository = HeroRepositoryImpl()
            val pages = 1..5
            val heroes =
                listOf(
                    heroRepository.page1,
                    heroRepository.page2,
                    heroRepository.page3,
                    heroRepository.page4,
                    heroRepository.page5,
                )
            pages.forEach { page ->
                val response = client.get("/boruto/heroes?page=$page")
                assertEquals(
                    expected = HttpStatusCode.OK,
                    actual = response.status,
                )
                val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
                val expected =
                    ApiResponse(
                        success = true,
                        message = "ok",
                        prevPage = calculatePage(page = page)["prevPage"],
                        nextPage = calculatePage(page = page)["nextPage"],
                        heroes = heroes[page - 1],
                    )
                assertEquals(
                    expected = expected,
                    actual = actual,
                )
            }
        }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, assert correct information`() =
        testApplication {
            application {
                module()
            }
            client.get("/boruto/heroes").apply {
                assertEquals(HttpStatusCode.OK, status)
                val expected =
                    ApiResponse(
                        success = true,
                        message = "ok",
                        prevPage = null,
                        nextPage = 2,
                        heroes = heroRepository.page1,
                    )
                val actual = Json.decodeFromString<ApiResponse>(this.bodyAsText())
                assertEquals(expected, actual)
            }
        }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query second page, assert correct information`() =
        testApplication {
            application {
                module()
            }
            client.get("/boruto/heroes?page=2").apply {
                assertEquals(HttpStatusCode.OK, status)
                val expected =
                    ApiResponse(
                        success = true,
                        message = "ok",
                        prevPage = 1,
                        nextPage = 3,
                        heroes = heroRepository.page2,
                    )
                val actual = Json.decodeFromString<ApiResponse>(this.bodyAsText())
                assertEquals(expected, actual)
            }
        }
}

private fun calculatePage(page: Int): Map<String, Int?> {
    var prevPage: Int? = page
    var nextPage: Int? = page
    if (page in 1..4) {
        nextPage = nextPage?.plus(1)
    }
    if (page in 2..5) {
        prevPage = prevPage?.minus(1)
    }
    if (page == 1) {
        prevPage = null
    }
    if (page == 5) {
        nextPage = null
    }
    return mapOf("prevPage" to prevPage, "nextPage" to nextPage)
}
