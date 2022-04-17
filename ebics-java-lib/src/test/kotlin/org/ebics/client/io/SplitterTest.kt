package org.ebics.client.io

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.ebics.client.utils.Utils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.security.Security
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class SplitterTest {
    init {
        Security.addProvider(BouncyCastleProvider())
    }

    @Test
    fun testSplitterWithoutInput() {
        val ba = ByteArray(0)
        val nonce = Utils.generateNonce()
        val keySpec = SecretKeySpec(nonce, "EAS")
        val splitter = Splitter(ba, true, keySpec)
        Assertions.assertEquals(16, splitter.content.size)
        Assertions.assertEquals(1, splitter.segmentNumber)
        Assertions.assertEquals(
            16, splitter.getContent(1).content.available()
        )
    }

    @Test
    fun testSplitterWith_1kBInput() {
        val ba = Random.Default.nextBytes(1024)
        val nonce = Utils.generateNonce()
        val keySpec = SecretKeySpec(nonce, "EAS")
        val splitter = Splitter(ba, true, keySpec)
        Assertions.assertEquals(1040, splitter.content.size)
        Assertions.assertEquals(1, splitter.segmentNumber)
        Assertions.assertEquals(
            1040, splitter.getContent(1).content.available()
        )
    }

    @Test
    fun testSplitterWith_oneFullSegment_1MB_without_1B_Input() {
        val ba = Random.Default.nextBytes(1024 * 1024 - 1)
        val nonce = Utils.generateNonce()
        val keySpec = SecretKeySpec(nonce, "EAS")
        val splitter = Splitter(ba, false, keySpec)
        Assertions.assertEquals(1, splitter.segmentNumber)
        val segmentSize = splitter.getContent(1).content.available()
        Assertions.assertEquals(
            splitter.content.size, segmentSize
        )
        Assertions.assertTrue(1024 * 1024 >= segmentSize)
    }

    @Test
    fun testSplitterWith_twoSegments_1MBInput() {
        val ba = Random.Default.nextBytes(1024 * 1024)
        val nonce = Utils.generateNonce()
        val keySpec = SecretKeySpec(nonce, "EAS")
        val splitter = Splitter(ba, false, keySpec)
        Assertions.assertEquals(2, splitter.segmentNumber)
        var totalSize = 0
        for (segmentNr: Int in 1..splitter.segmentNumber + 1) {
            val segmentSize = splitter.getContent(segmentNr).content.available()
            Assertions.assertTrue(1024 * 1024 >= segmentSize)
            totalSize += segmentSize
        }
        Assertions.assertEquals(splitter.content.size, totalSize)
    }

    @Test
    fun testSplitterWith_100MBInput() {
        val ba = Random.Default.nextBytes(100 * (1024 * 1024 - 1))
        val nonce = Utils.generateNonce()
        val keySpec = SecretKeySpec(nonce, "EAS")
        val splitter = Splitter(ba, false, keySpec)
        Assertions.assertEquals(100, splitter.segmentNumber)
        var totalSize = 0
        for (segmentNr: Int in 1..splitter.segmentNumber + 1) {
            val segmentSize = splitter.getContent(segmentNr).content.available()
            Assertions.assertTrue(1024 * 1024 >= segmentSize)
            totalSize += segmentSize
        }
        Assertions.assertEquals(splitter.content.size, totalSize)
    }

}