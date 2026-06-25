package com.darioossa.openbooks.data.remote

import com.darioossa.openbooks.data.remote.dto.SearchDocDto
import com.darioossa.openbooks.data.remote.dto.WorkDto
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlin.test.Test

class BookMappersTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `SearchDocDto maps every field and builds the cover URL`() {
        val dto =
            SearchDocDto(
                key = "/works/OL45804W",
                title = "Oliver Twist",
                authorName = listOf("Charles Dickens"),
                coverId = 12345,
                firstPublishYear = 1838,
            )

        val book = dto.toBook()

        book.key shouldBe "OL45804W"
        book.title shouldBe "Oliver Twist"
        book.authors shouldBe listOf("Charles Dickens")
        book.coverUrl shouldBe "https://covers.openlibrary.org/b/id/12345-M.jpg"
        book.firstPublishYear shouldBe 1838
        book.description shouldBe null
    }

    @Test
    fun `SearchDocDto handles null cover, authors, and year`() {
        val dto = SearchDocDto(key = "/works/OL1W", title = "Untitled")

        val book = dto.toBook()

        book.coverUrl shouldBe null
        book.authors shouldBe emptyList()
        book.firstPublishYear shouldBe null
    }

    @Test
    fun `WorkDto maps description given as a plain string`() {
        val payload = """{ "key": "/works/OL45804W", "title": "Oliver Twist", "description": "A novel." }"""

        val book = json.decodeFromString<WorkDto>(payload).toBook()

        book.key shouldBe "OL45804W"
        book.description shouldBe "A novel."
    }

    @Test
    fun `WorkDto maps description given as a typed object`() {
        val payload =
            """{ "key": "/works/OL45804W", "title": "Oliver Twist",
                 "description": { "type": "/type/text", "value": "A novel." } }"""

        val book = json.decodeFromString<WorkDto>(payload).toBook()

        book.description shouldBe "A novel."
    }

    @Test
    fun `WorkDto builds cover URL from the first cover id`() {
        val payload = """{ "key": "/works/OL45804W", "title": "Oliver Twist", "covers": [99, 100] }"""

        val book = json.decodeFromString<WorkDto>(payload).toBook()

        book.coverUrl shouldBe "https://covers.openlibrary.org/b/id/99-M.jpg"
    }
}
