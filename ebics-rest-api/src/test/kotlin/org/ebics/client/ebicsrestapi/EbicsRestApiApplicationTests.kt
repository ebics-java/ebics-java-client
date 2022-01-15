package org.ebics.client.ebicsrestapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [TestContext::class])
class EbicsRestApiApplicationTests {

	@Test
	fun contextLoads() {
	}

}
