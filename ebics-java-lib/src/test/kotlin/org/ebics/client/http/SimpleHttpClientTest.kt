package org.ebics.client.http

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.apache.http.Header
import org.apache.http.ProtocolVersion
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicStatusLine
import org.ebics.client.exception.EbicsException
import org.ebics.client.io.ByteArrayContentFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.net.URL

@ExtendWith(MockKExtension::class)
class SimpleHttpClientTest {
    @MockK
    lateinit var httpClient: CloseableHttpClient

    @Test
    fun sendRequestWithoutHeaderAndWithResponse200_then_getResponse() {
        val configuration: HttpClientRequestConfiguration = object : HttpClientRequestConfiguration {
            override val displayName: String = "default"
            override val httpContentType: String? = null
        }
        val mockedClientResponse = mockk<CloseableHttpResponse>()
        every { httpClient.execute(allAny()) } returns mockedClientResponse
        every { mockedClientResponse.statusLine } returns BasicStatusLine(ProtocolVersion("HTTP", 1, 1), 200, "OK")
        every { mockedClientResponse.entity } returns ByteArrayEntity("test".toByteArray())
        every { mockedClientResponse.close() } returns Unit

        val client = SimpleHttpClient(httpClient, configuration)
        val response = client.send(URL("http://not.existing.url.com.xx"), ByteArrayContentFactory("aaa".toByteArray()))
        Assertions.assertArrayEquals("test".toByteArray(), response.content.readBytes())
    }

    @Test
    fun sendRequestWithHeaderAndResponse200_then_setHeaderAndGetResponse() {
        val configuration: HttpClientRequestConfiguration = object : HttpClientRequestConfiguration {
            override val displayName: String = "default"
            override val httpContentType: String = "text/xml; charset=ISO-8859-1"
        }
        val mockedClientResponse = mockk<CloseableHttpResponse>()
        every { httpClient.execute(allAny()) } returns mockedClientResponse
        every { mockedClientResponse.statusLine } returns BasicStatusLine(ProtocolVersion("HTTP", 1, 1), 200, "OK")
        every { mockedClientResponse.entity } returns ByteArrayEntity("test".toByteArray())
        every { mockedClientResponse.close() } returns Unit

        val client = SimpleHttpClient(httpClient, configuration)
        val response = client.send(URL("http://not.existing.url.com.xx"), ByteArrayContentFactory("aaa".toByteArray()))
        Assertions.assertArrayEquals("test".toByteArray(), response.content.readBytes())
    }

    @Test
    fun sendRequestWithResponse500_then_throwException() {
        val configuration: HttpClientRequestConfiguration = object : HttpClientRequestConfiguration {
            override val displayName: String = "default"
            override val httpContentType: String = "text/xml; charset=ISO-8859-1"
        }
        val mockedClientResponse = mockk<CloseableHttpResponse>()
        every { httpClient.execute(allAny()) } returns mockedClientResponse
        every { mockedClientResponse.statusLine } returns BasicStatusLine(
            ProtocolVersion("HTTP", 1, 1),
            500,
            "Error 500"
        )
        every { mockedClientResponse.entity } returns ByteArrayEntity("Some strange server HTTP error 500".toByteArray())
        every { mockedClientResponse.close() } returns Unit
        every { mockedClientResponse.allHeaders } returns arrayOf(BasicHeader("test", "testval"), BasicHeader("Test2", "Test3"))

        val client = SimpleHttpClient(httpClient, configuration)
        val exception = Assertions.assertThrows(EbicsException::class.java) {
            client.send(URL("http://not.existing.url.com.xx"), ByteArrayContentFactory("aaa".toByteArray()))
        }
        Assertions.assertEquals("Wrong returned HTTP code: 500 Error 500", exception.message)
    }

    @Test
    fun sendRequestWithResponse302_then_throwException() {
        val configuration: HttpClientRequestConfiguration = object : HttpClientRequestConfiguration {
            override val displayName: String = "default"
            override val httpContentType: String = "text/xml; charset=ISO-8859-1"
        }
        val mockedClientResponse = mockk<CloseableHttpResponse>()
        every { httpClient.execute(allAny()) } returns mockedClientResponse
        every { mockedClientResponse.statusLine } returns BasicStatusLine(
            ProtocolVersion("HTTP", 1, 1),
            302,
            "Error 302"
        )
        every { mockedClientResponse.entity } returns ByteArrayEntity("Some strange server HTTP error 302".toByteArray())
        every { mockedClientResponse.close() } returns Unit
        every { mockedClientResponse.allHeaders } returns null

        val client = SimpleHttpClient(httpClient, configuration)
        val exception = Assertions.assertThrows(EbicsException::class.java) {
            client.send(URL("http://not.existing.url.com.xx"), ByteArrayContentFactory("aaa".toByteArray()))
        }
        Assertions.assertEquals("Wrong returned HTTP code: 302 Error 302", exception.message)
    }
}