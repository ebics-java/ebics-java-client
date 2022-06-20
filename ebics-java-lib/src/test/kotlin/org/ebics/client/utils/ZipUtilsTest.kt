package org.ebics.client.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.math.roundToInt
import kotlin.random.Random

class ZipUtilsTest {

    @Test
    fun test1() {
        zipInputAndUnzipIt_theResultIsAgainInput(Random.nextBytes(100));
    }

    @Test
    fun test2() {
        zipInputAndUnzipIt_theResultIsAgainInput(Random.nextBytes(1024));
    }

    @Test
    fun test3() {
        zipInputAndUnzipIt_theResultIsAgainInput(Random.nextBytes(1));
    }

    @Test
    fun test4() {
        zipInputAndUnzipIt_theResultIsAgainInput(Random.nextBytes(0));
    }

    @Test
    fun test5() {
        zipInputAndUnzipIt_theResultIsAgainInput(Random.nextBytes(1024 * 100));
    }

    @Test
    fun test6() {
        zipInputAndUnzipIt_theResultIsAgainInput(Random.nextBytes(1024 * 1024));
    }

    @Test
    fun test7() {
        zipInputAndUnzipIt_theResultIsAgainInput("11111111111111111111xxxxxxxxxxxxxxxxxx11111111111111111111xxxxxxxxxxxxxxxx1111111111111111".toByteArray());
    }

    @Test
    fun test8() {
        zipInputAndUnzipIt_theResultIsAgainInput("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Document xmlns=\"http://www.six-interbank-clearing.com/de/pain.002.001.03.ch.02.xsd\"><CstmrPmtStsRpt><GrpHdr><MsgId>11313ABJW132157297310</MsgId><CreDtTm>2021-09-22T13:21:57.297+02:00</CreDtTm><InitgPty><Id><OrgId><BICOrBEI>ZKBKCHZZ</BICOrBEI></OrgId></Id></InitgPty></GrpHdr><OrgnlGrpInfAndSts><OrgnlMsgId>NOTPROVIDED</OrgnlMsgId><OrgnlMsgNmId>NOTPROVIDED</OrgnlMsgNmId><GrpSts>RJCT</GrpSts><StsRsnInf><Rsn><Cd>FF01</Cd></Rsn><AddtlInf><![CDATA[Fataler Fehler: Ein pain.001 Parsing Fehler ist aufgetreten.]]></AddtlInf></StsRsnInf></OrgnlGrpInfAndSts></CstmrPmtStsRpt></Document>".toByteArray());
    }

    private fun zipInputAndUnzipIt_theResultIsAgainInput(input: ByteArray) {
        val zippedInput = Utils.zip(input)
        val output = Utils.unzip(zippedInput)
        println("Input length: ${input.size}");
        println("Compressed length: ${zippedInput.size} compression ratio: ${(zippedInput.size.toDouble() / input.size * 100).roundToInt()}%")
        Assertions.assertArrayEquals(output, input)
    }
}