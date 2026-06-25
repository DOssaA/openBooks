package com.darioossa.openbooks.data.remote

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class BooksRemoteTest {
    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun remoteReturning(
        body: String,
        onRequestPath: (String) -> Unit = {},
    ): BooksRemote {
        val engine =
            MockEngine { request ->
                onRequestPath(request.url.encodedPath + "?" + request.url.encodedQuery)
                respond(content = body, status = HttpStatusCode.OK, headers = jsonHeaders)
            }
        return BooksRemote(openLibraryHttpClient(engine))
    }

    @Test
    fun `search hits search_json with query, page and limit and parses docs`() =
        runTest {
            var requested = ""
            val body =
                """
                { "numFound": 1, "docs": [
                  { "key": "/works/OL45804W", "title": "Oliver Twist",
                    "author_name": ["Charles Dickens"], "cover_i": 12345, "first_publish_year": 1838 }
                ] }
                """.trimIndent()
            val remote = remoteReturning(body) { requested = it }

            val books = remote.search(query = "oliver twist", page = 2)

            requested.shouldContainPath("/search.json")
            requested.shouldContainParam("q=oliver")
            requested.shouldContainParam("page=2")
            requested.shouldContainParam("limit=")
            books.size shouldBe 1
            books.first().key shouldBe "OL45804W"
            books.first().coverUrl shouldBe "https://covers.openlibrary.org/b/id/12345-M.jpg"
        }

    @Test
    fun `getBook hits works endpoint and parses the description`() =
        runTest {
            var requested = ""
            val body =
                """{ "key": "/works/OL45804W", "title": "Oliver Twist",
                     "description": { "type": "/type/text", "value": "A novel." } }"""
            val remote = remoteReturning(body) { requested = it }

            val book = remote.getBook("OL45804W")

            requested.shouldContainPath("/works/OL45804W.json")
            book.title shouldBe "Oliver Twist"
            book.description shouldBe "A novel."
        }

    private fun String.shouldContainPath(path: String) = contains(path) shouldBe true

    private fun String.shouldContainParam(param: String) = contains(param) shouldBe true
}
