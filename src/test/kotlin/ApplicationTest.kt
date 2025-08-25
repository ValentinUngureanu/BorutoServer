package com.example

import com.example.models.ApiResponse
import com.example.repository.HeroRepositoryImpl
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `access root endpoint, assert correct information`() =
        testApplication {
            application {
                module()
            }
            val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Welcome to Boruto API", response.bodyAsText())
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
    fun `access all heroes endpoint, query non existing page number, assert error`() =
        testApplication {
            application {
                module()
            }

            val response = client.get("/boruto/heroes?page=6")
            assertEquals(HttpStatusCode.NotFound, response.status)

            assertEquals(expected = "Page not Found", actual = response.bodyAsText())
        }

    @ExperimentalSerializationApi
    @Test
    fun `access all heroes endpoint, query invalid page number, assert error`() =
        testApplication {
            application {
                module()
            }
            val response = client.get("/boruto/heroes?page=invalid")
            assertEquals(HttpStatusCode.BadRequest, response.status)
            val expected =
                ApiResponse(
                    success = false,
                    message = "Only Numbers Allowed.",
                    prevPage = null,
                    nextPage = null,
                    heroes = emptyList(),
                )
            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())
            assertEquals(expected, actual)
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query hero name, assert single hero result`() =
        testApplication {
            application {
                module()
            }
            val response = client.get("/boruto/heroes/search?name=sas")
            assertEquals(HttpStatusCode.OK, response.status)

            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText()).heroes.size
            assertEquals(expected = 1, actual)
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query hero name, assert multiple heroes result`() =
        testApplication {
            application {
                module()
            }
            val response = client.get("/boruto/heroes/search?name=sa")
            assertEquals(HttpStatusCode.OK, response.status)

            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText()).heroes.size
            assertEquals(expected = 3, actual)
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query an empty hero name, assert empty list as a result`() =
        testApplication {
            application {
                module()
            }
            val response = client.get("/boruto/heroes/search?name=")
            assertEquals(HttpStatusCode.OK, response.status)

            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText()).heroes
            assertEquals(expected = emptyList(), actual)
        }

    @ExperimentalSerializationApi
    @Test
    fun `access search heroes endpoint, query non existing hero, assert empty list as a result`() =
        testApplication {
            application {
                module()
            }
            val response = client.get("/boruto/heroes/search?name=unknown")
            assertEquals(HttpStatusCode.OK, response.status)

            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText()).heroes
            assertEquals(expected = emptyList(), actual)
        }

    @ExperimentalSerializationApi
    @Test
    fun `access non existing endpoint, assert not found`() =
        testApplication {
            application {
                module()
            }
            val response = client.get("/unknown")

            assertEquals(HttpStatusCode.NotFound, response.status)
            assertEquals(expected = "Page not Found", actual = response.bodyAsText())
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
